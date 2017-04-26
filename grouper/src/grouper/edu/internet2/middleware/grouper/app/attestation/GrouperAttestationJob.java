package edu.internet2.middleware.grouper.app.attestation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * attestation daemon
 */
@DisallowConcurrentExecution
public class GrouperAttestationJob extends OtherJobBase {
  
  /**
   * 
   */
  public static final String ATTESTATION_LAST_EMAILED_DATE = "attestationLastEmailedDate";
  /**
   * 
   */
  public static final String ATTESTATION_SEND_EMAIL = "attestationSendEmail";
  /**
   * 
   */
  public static final String ATTESTATION_DATE_CERTIFIED = "attestationDateCertified";
  /**
   * 
   */
  public static final String ATTESTATION_DEF = "attestationDef";
  /**
   * 
   */
  public static final String ATTESTATION_DAYS_UNTIL_RECERTIFY = "attestationDaysUntilRecertify";
  /**
   * 
   */
  public static final String ATTESTATION_DAYS_BEFORE_TO_REMIND = "attestationDaysBeforeToRemind";
  /**
   * 
   */
  public static final String ATTESTATION_EMAIL_ADDRESSES = "attestationEmailAddresses";
  /**
   * 
   */
  public static final String ATTESTATION_DIRECT_ASSIGNMENT = "attestationDirectAssignment";
  /**
   * 
   */
  public static final String ATTESTATION_VALUE_DEF = "attestation";
  /**
   * 
   */
  public static final String ATTESTATION_STEM_SCOPE = "attestationStemScope";

  /**
   * 
   * @return the stem name
   */
  public static String attestationStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:attestation";
  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperAttestationJob.class);
  
  /**
   * get map of email addresses to email objects for group attributes
   * @param groupAttributeAssigns
   * @return
   */
  private Map<String, Set<EmailObject>> buildAttestationGroupEmails(Set<AttributeAssign> groupAttributeAssigns) {
    
    // map of email address to email object (group id, group name, ccList)
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: groupAttributeAssigns) {
      
      String attestationDateCertified = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DATE_CERTIFIED);
      String attestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_UNTIL_RECERTIFY);
      String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_SEND_EMAIL);
      String attestationDaysBeforeToRemind = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_BEFORE_TO_REMIND);
      
      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+attributeAssign.getOwnerGroup().getDisplayName()+" attestationSendEmail attribute is set to true so skipping sending email.");
        continue;
      }

      
      try {
        String attestationLastEmailedDate = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_LAST_EMAILED_DATE);
        if (!StringUtils.isBlank(attestationLastEmailedDate)) {
          Date lastEmailedDate = new SimpleDateFormat("yyyy/MM/dd").parse(attestationLastEmailedDate);
          //must not be today
          Calendar calendar = new GregorianCalendar();
          calendar.set(Calendar.HOUR_OF_DAY, 0);
          calendar.set(Calendar.MINUTE, 0);
          calendar.set(Calendar.SECOND, 0);
          calendar.set(Calendar.MILLISECOND, 0);
          if (lastEmailedDate.getTime() == calendar.getTimeInMillis()) {
            LOG.debug("For "+attributeAssign.getOwnerGroup().getDisplayName()+" attestationLastEmailedDate attribute is set to today so skipping sending email.");
            continue;
          }
        }
      } catch (ParseException e) {
        LOG.error("Could not convert "+attestationDateCertified+" to date. Attribute assign id is: "+attributeAssign.getId(), e);
        continue;
      }

      
      int daysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
      if (! StringUtils.isBlank(attestationDaysUntilRecertify)) {
        daysUntilRecertify = Integer.valueOf(attestationDaysUntilRecertify);
      }
      
      int daysBeforeReminderEmail = 0;
      if (! StringUtils.isBlank(attestationDaysBeforeToRemind)) {
        daysBeforeReminderEmail = Integer.valueOf(attestationDaysBeforeToRemind);
      }
      
      boolean sendEmail = false;
      if (StringUtils.isBlank(attestationDateCertified)) {
        sendEmail = true;
      } else {
        // find the difference between today's date and last certified date
        // and if the difference is greater than daysUntilRecertify minus attestationDaysBeforeToRemind, then sendEmail
        try {
          Date lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(attestationDateCertified);
          long diff = new Date().getTime() - lastCertifiedDate.getTime();
          long diffInDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
          if (diffInDays > (daysUntilRecertify - daysBeforeReminderEmail)) {
            sendEmail = true;
          }
        } catch (ParseException e) {
          LOG.error("Could not convert "+attestationDateCertified+" to date. Attribute assign id is: "+attributeAssign.getId(), e);
          continue;
        }
      }
      
      if (sendEmail) {
        // grab the list of email addresses from the attribute
        String[] emailAddresses = getEmailAddresses(attributeAssign, attributeAssign.getOwnerGroup());
        addEmailObject(attributeAssign, emailAddresses, emails, attributeAssign.getOwnerGroup());
      }
      
    }
    
    return emails;
    
  }
  
  /**
   * build array of email addresses from either the attribute itself or from the group admins/readers/updaters.
   * @param attributeAssign
   * @param group
   * @return
   */
  private String[] getEmailAddresses(AttributeAssign attributeAssign, Group group) {
    
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_EMAIL_ADDRESSES);
    String[] emailAddresses = null;
    if (StringUtils.isBlank(attestationEmailAddresses)) {
      
      // get the group's admins/updaters/readers 
      Set<Subject> groupMembers = group.getAdmins();
      groupMembers.addAll(group.getReaders());
      groupMembers.addAll(group.getUpdaters());
      
      Set<String> addresses = new HashSet<String>();
      
      // go through each subject and find the email address.
      for (Subject subject: groupMembers) {
        String emailAttributeName = GrouperEmailUtils.emailAttributeNameForSource(subject.getSourceId());
        if (!StringUtils.isBlank(emailAttributeName) && !StringUtils.isBlank(subject.getAttributeValue(emailAttributeName))) {
          addresses.add(subject.getAttributeValue(emailAttributeName));
        }
      }
      
      emailAddresses = addresses.toArray(new String[addresses.size()]);
      
    } else {
      emailAddresses = attestationEmailAddresses.split(",");
    }
    
    return emailAddresses;
    
  }
  
  /**
   * Add new key (email address) to map or update the value (set of email objects) 
   * @param attributeAssign
   * @param emailAddresses
   * @param emails
   * @param group
   */
  private void addEmailObject(AttributeAssign attributeAssign, String[] emailAddresses, Map<String, Set<EmailObject>> emails, Group group) {
    
    if (emailAddresses == null || emailAddresses.length == 0) {
      LOG.error("Could not find any emails for attribute assign id "+attributeAssign.getId()+". Group name is "+group.getDisplayName());
    } else {
      
      for (int i=0; i<emailAddresses.length; i++) {
        
        String primaryEmailAddress = emailAddresses[i].trim();
        
        Set<String> ccEmailAddresses =  getElements(emailAddresses, i);
        
        EmailObject emailObject = new EmailObject(group.getId(), group.getDisplayName(), ccEmailAddresses);
        
        if (emails.containsKey(primaryEmailAddress)) {
          Set<EmailObject> emailObjects = emails.get(primaryEmailAddress);
          emailObjects.add(emailObject);
        } else {
          Set<EmailObject> emailObjects = new HashSet<GrouperAttestationJob.EmailObject>();
          emailObjects.add(emailObject);
          emails.put(primaryEmailAddress, emailObjects);
        }
      }
      
    }
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    AttributeDef attributeDef = AttributeDefFinder.findByName(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DEF, false);
    if (attributeDef == null) {
      LOG.error(GrouperAttestationJob.attestationStemName() + ":" + "attestationDef attribute def doesn't exist. Job will not proceed.");
      return null;
    }
    
    Set<AttributeAssign> groupAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.group,
        attributeDef.getId(), null, null,
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = buildAttestationGroupEmails(groupAttributeAssigns);
    
    LOG.info("got "+emails.size()+" from group attributes, starting building map from stem attributes.");

    otherJobInput.getHib3GrouperLoaderLog().store();
    
    Map<String, Set<EmailObject>> stemEmails = buildAttestationStemEmails(attributeDef);
    
    LOG.info("got "+stemEmails.size()+" from stem attributes, start merging group and stem attributes.");
    otherJobInput.getHib3GrouperLoaderLog().store();

    mergeEmailObjects(emails, stemEmails);
    
    otherJobInput.getHib3GrouperLoaderLog().setInsertCount(emails.size());
    otherJobInput.getHib3GrouperLoaderLog().store();
    
    LOG.info("start sending emails to "+emails.size()+" email addresses.");

    sendEmail(emails);

    otherJobInput.getHib3GrouperLoaderLog().store();

    LOG.info("Set attestationLastEmailedDate attribute to each of the groups.");
    setLastEmailedDate(emails, otherJobInput.getGrouperSession());

    //count line items
    int lineItems = 0;
    for (String email : GrouperUtil.nonNull(emails).keySet()) {
      lineItems += GrouperUtil.length(emails.get(email));
    }
    
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage("Sent " + emails.size() + " emails with " 
        + lineItems + " line items about " + GrouperUtil.length(groupAttributeAssigns) + " attestation assignments");

    LOG.info("GrouperAttestationJob finished successfully.");
    return null;
    
  }
  

  /**
   * build email body/subject and send email.
   * @param emailObjects
   */
  private void sendEmail(Map<String, Set<EmailObject>> emailObjects) {
    String uiUrl = GrouperConfig.getGrouperUiUrl(false);
    if (StringUtils.isBlank(uiUrl)) {
      LOG.error("grouper.properties grouper.ui.url is blank/null. Please fix that first. GrouperAttestationJob will not proceed. No emails have been sent.");
    }
    
    String subject = GrouperConfig.retrieveConfig().propertyValueString("attestation.reminder.email.subject");
    String body = GrouperConfig.retrieveConfig().propertyValueString("attestation.reminder.email.body");
    if (StringUtils.isBlank(subject)) {
      subject = "You have $groupCount$ groups that require attestation";
    }
    if (StringUtils.isBlank(body)) {
      body = "You need to attest the memberships of the following groups.  Review the memberships of each group and click: More actions -> Attestation -> Members of this group have been reviewed";
    }
    
    for (Map.Entry<String, Set<EmailObject>> entry: emailObjects.entrySet()) {

      String sub = StringUtils.replace(subject, "$groupCount$", String.valueOf(entry.getValue().size()));
      
      // build body of the email
      StringBuilder emailBody = new StringBuilder(body);
      emailBody.append("\n");
      int start = 1; // show only attestation.email.group.count groups in one email
      int end = GrouperConfig.retrieveConfig().propertyValueInt("attestation.email.group.count", 100);
      lbl: for (EmailObject emailObject: entry.getValue()) {
       emailBody.append("\n");
       emailBody.append(start+". "+emailObject.getGroupName()+"  ");
       // set the cc if any
       if (emailObject.getCcEmails() != null && emailObject.getCcEmails().size() > 0) {
         emailBody.append("(cc'd ");
         emailBody.append(StringUtils.join(emailObject.getCcEmails(), ","));
         emailBody.append(")");
       }
       emailBody.append("\n");
       emailBody.append(uiUrl);
       emailBody.append("grouperUi/app/UiV2Main.index?operation=UiV2Group.viewGroup&groupId="+emailObject.getGroupId());
       start = start + 1;
       if (start > end) {
         String more = GrouperConfig.retrieveConfig().propertyValueString("attestation.reminder.email.body.greaterThan100");
         if (StringUtils.isBlank(more)) {
           more = "There are $remaining$ more groups to be attested.";
         }
         more = StringUtils.replace(more, "$remaining$", String.valueOf(entry.getValue().size() - end));
         emailBody.append("\n");
         emailBody.append(more);
         break lbl;
       }
      }
      new GrouperEmail().setBody(emailBody.toString()).setSubject(sub).setTo(entry.getKey()).send();
    }
  }
  
  /**
   * set last emailed date attribute to each of the groups.
   * @param emailObjects
   * @param session
   */
  private void setLastEmailedDate(Map<String, Set<EmailObject>> emailObjects, GrouperSession session) {
    
    for (Map.Entry<String, Set<EmailObject>> entry: emailObjects.entrySet()) { 
      
      for (EmailObject emailObject: entry.getValue()) { 
        Group group = GroupFinder.findByUuid(session, emailObject.getGroupId(), false);
        if (group != null) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_VALUE_DEF, false);
          if (!group.getAttributeDelegate().hasAttributeByName(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_VALUE_DEF)) {
            group.getAttributeDelegate().assignAttribute(attributeDefName);
          } 
          AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
          
          String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
          attributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_LAST_EMAILED_DATE, date);
         
          attributeAssign.saveOrUpdate(false);
        }
      }
      
    }
    
  }
  
  
  /**
   *  Merge map2 into map1
   * @param map1
   * @param map2
   */
  private void mergeEmailObjects(Map<String, Set<EmailObject>> map1, Map<String, Set<EmailObject>> map2) {
    
    for (Map.Entry<String, Set<EmailObject>> entry: map1.entrySet()) {
      
      if (map2.containsKey(entry.getKey())) {
        entry.getValue().addAll(map2.get(entry.getKey()));
      }      
    }
    
  }
  
  /**
   * get map of email addresses to email objects for stem attributes 
   * @param attributeDef
   * @return
   */
  private Map<String, Set<EmailObject>> buildAttestationStemEmails(AttributeDef attributeDef) {
  
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        attributeDef.getId(), null, null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_VALUE_DEF, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_SEND_EMAIL);
      
      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" attestationSendEmail attribute is not set to true so skipping sending email.");
        continue;
      }
      
      String attestationStemScope = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_STEM_SCOPE);
      
      // go through each group and check if they have their own attestation attribute and use them if they are present.
      // if not, then use the stem attributes.
      Set<Group> childGroups = attributeAssign.getOwnerStem().getChildGroups(Scope.valueOfIgnoreCase(attestationStemScope, false) == null ? Scope.SUB:
        Scope.valueOfIgnoreCase(attestationStemScope, false));
      
      for (Group group: childGroups) {
        
        if (group.getAttributeDelegate().hasAttribute(attributeDefName)) { // group has attestation
          
          AttributeAssignable attributeAssignable = group.getAttributeDelegate().getAttributeOrAncestorAttribute(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_VALUE_DEF, false);
          AttributeAssign groupAttributeAssign = attributeAssignable.getAttributeDelegate().retrieveAssignment(null, attributeDefName, false, false);
          String attestationDirectAssignment = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DIRECT_ASSIGNMENT);
          if (GrouperUtil.booleanValue(attestationDirectAssignment, false)) { // group has direct attestation, don't use stem attributes at all.
            Set<AttributeAssign> singleGroupAttributeAssign = new HashSet<AttributeAssign>();
            singleGroupAttributeAssign.add(groupAttributeAssign);
            mergeEmailObjects(emails, buildAttestationGroupEmails(singleGroupAttributeAssign));
          } else { // group doesn't have direct attestation attribute, merge stem and group attributes.
            
            // clone group attribute assign so we don't update the original one
            AttributeAssign clonedGroupAttributeAssign = groupAttributeAssign.clone();
            
            // we need to overwrite the null/blank group attestation attributes with stem attestation attributes.
            if (StringUtils.isBlank(clonedGroupAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_EMAIL_ADDRESSES))) {
              clonedGroupAttributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_EMAIL_ADDRESSES,
                  attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_EMAIL_ADDRESSES));
            }
            
            if (StringUtils.isBlank(clonedGroupAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_BEFORE_TO_REMIND))) {
              clonedGroupAttributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_BEFORE_TO_REMIND,
                  attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_BEFORE_TO_REMIND));
            }
            
            if (StringUtils.isBlank(clonedGroupAttributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_UNTIL_RECERTIFY))) {
              clonedGroupAttributeAssign.getAttributeValueDelegate().assignValue(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_UNTIL_RECERTIFY,
                  attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_UNTIL_RECERTIFY));
            }
            
            Set<AttributeAssign> singleGroupAttributeAssign = new HashSet<AttributeAssign>();
            singleGroupAttributeAssign.add(groupAttributeAssign);
            mergeEmailObjects(emails, buildAttestationGroupEmails(singleGroupAttributeAssign));
          }
          
        } else { // group doesn't have any attestation attribute, get the attestation attributes from stem.
          
          // grab the list of email addresses from the attribute
          String[] emailAddresses = getEmailAddresses(attributeAssign, group);
          
          addEmailObject(attributeAssign, emailAddresses, emails, group);
          
        }
      }
    }
    
    return emails;
    
  }
  
  /**
   * get unique elements from array except specified by index except.
   * @param array
   * @param except
   * @return
   */
  private Set<String> getElements(String[] array, int except) {
    Set<String> result = new HashSet<String>();
    for (int j=0; j<array.length; j++) {
      if (except != j) {
        result.add(array[j].trim());
      }
    }
    return result;
  }
  
  /**
   * Object to represent value in the map.
   */
  class EmailObject {
    
    private String groupId;
    private String groupName;
    private Set<String> ccEmails;
    
    EmailObject(String groupId, String groupName, Set<String> ccEmails) {
      this.groupId = groupId;
      this.groupName = groupName;
      this.ccEmails = ccEmails;
    }
    
    public String getGroupId() {
      return groupId;
    }

    
    public String getGroupName() {
      return groupName;
    }
    
    public Set<String> getCcEmails() {
      return ccEmails;
    }

    @Override
    public int hashCode() {
     return new HashCodeBuilder()
     .append(groupId)
     .append(groupName)
     .append(ccEmails)
     .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      EmailObject other = (EmailObject) obj;
    
      return new EqualsBuilder()
          .append(this.groupId, other.groupId)
          .append(this.groupName, other.groupName)
          .append(this.ccEmails, other.ccEmails)
          .isEquals();
    }

    @Override
    public String toString() {
      return "EmailObject [groupId=" + groupId + ", groupName=" + groupName
          + ", ccEmails=" + ccEmails + "]";
    }
    
  }

}
