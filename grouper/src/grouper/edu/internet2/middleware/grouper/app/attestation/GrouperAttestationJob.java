package edu.internet2.middleware.grouper.app.attestation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;

/**
 * attestation daemon
 */
@DisallowConcurrentExecution
public class GrouperAttestationJob extends OtherJobBase {
  
  /**
   * two weeks days left
   */
  public static Set<Object> TWO_WEEKS_DAYS_LEFT = GrouperUtil.toSetObjectType("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
  
  /**
   * 
   */
  public static final String ATTESTATION_LAST_EMAILED_DATE = "attestationLastEmailedDate";

  /**
   * last emailed attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailedDate() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_LAST_EMAILED_DATE);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation emailed date attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   */
  public static final String ATTESTATION_CALCULATED_DAYS_LEFT = "attestationCalculatedDaysLeft";

  /**
   * calculated days left attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameCalculatedDaysLeft() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_CALCULATED_DAYS_LEFT);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation calculated days attribute def name be found?");
    }
    return attributeDefName;

  }
  
  public static AttributeDef retrieveAttributeDef() {
    AttributeDef attributeDef = retrieveAttributeDefFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DEF);
    
    if (attributeDef == null) {
      throw new RuntimeException("Why cant attestation attributeDef not be found?");
    }
    
    return attributeDef;
  }

  /**
   * 
   */
  public static final String ATTESTATION_SEND_EMAIL = "attestationSendEmail";

  /**
   * send email attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameSendEmail() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_SEND_EMAIL);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation send email attribute def name be found?");
    }
    return attributeDefName;

  }
  
 /**
   * 
   */
  public static final String ATTESTATION_DATE_CERTIFIED = "attestationDateCertified";

  /**
   * date certified attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDateCertified() {

    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DATE_CERTIFIED);
    
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation date certified attribute def name be found?");
    }
    return attributeDefName;

  }  

  /** attribute def name cache */
  private static ExpirableCache<String, AttributeDefName> attributeDefNameCache = new ExpirableCache<String, AttributeDefName>(5);
  
  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDefName retrieveAttributeDefNameFromDbOrCache(final String name) {
    
    AttributeDefName attributeDefName = attributeDefNameCache.get(name);

    if (attributeDefName == null) {
      
      attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefNameFinder.findByName(name, false);
          
        }
        
      });
      if (attributeDefName == null) {
        return null;
      }
      attributeDefNameCache.put(name, attributeDefName);
    }
    
    return attributeDefName;
  }
  
  /** attribute def cache */
  private static ExpirableCache<String, AttributeDef> attributeDefCache = new ExpirableCache<String, AttributeDef>(5);
  
  /**
   * cache this.  note, not sure if its necessary
   */
  private static AttributeDef retrieveAttributeDefFromDbOrCache(final String name) {
    
    AttributeDef attributeDef = attributeDefCache.get(name);

    if (attributeDef == null) {
      
      attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(name, false);
          
        }
        
      });
      if (attributeDef == null) {
        return null;
      }
      attributeDefCache.put(name, attributeDef);
    }
    
    return attributeDef;
  }
  
  /**
   * 
   */
  private static final String ATTESTATION_DEF = "attestationDef";
  /**
   * 
   */
  public static final String ATTESTATION_DAYS_UNTIL_RECERTIFY = "attestationDaysUntilRecertify";
  
  /**
   * days until recertify attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDaysUntilRecertify() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_UNTIL_RECERTIFY);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation days until recertify attribute def name be found?");
    }
    return attributeDefName;

  }
  

  /**
   * 
   */
  public static final String ATTESTATION_DAYS_BEFORE_TO_REMIND = "attestationDaysBeforeToRemind";
  
  /**
   * days before remind attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDaysBeforeToRemind() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DAYS_BEFORE_TO_REMIND);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation days before to remind attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   */
  public static final String ATTESTATION_EMAIL_ADDRESSES = "attestationEmailAddresses";
  
  /**
   * email addresses attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameEmailAddresses() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_EMAIL_ADDRESSES);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation email addresses attribute def name be found?");
    }
    return attributeDefName;

  }
  

  /**
   * 
   */
  public static final String ATTESTATION_DIRECT_ASSIGNMENT = "attestationDirectAssignment";
  
  /**
   * direct assignment attribute def name
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameDirectAssignment() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DIRECT_ASSIGNMENT);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation direct assignment attribute def name be found?");
    }
    return attributeDefName;

  }
  
  /**
   * 
   */
  public static final String ATTESTATION_VALUE_DEF = "attestation";
  
  /**
   * attribute def name assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameValueDef() {
    
    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_VALUE_DEF);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation value def attribute def name be found?");
    }
    return attributeDefName;
  }
  
  
  /**
   * 
   */
  public static final String ATTESTATION_STEM_SCOPE = "attestationStemScope";

  /**
   * attribute def name of which scope when assigned to stem
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameStemScope() {

    AttributeDefName attributeDefName = retrieveAttributeDefNameFromDbOrCache(
        GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_STEM_SCOPE);

    if (attributeDefName == null) {
      throw new RuntimeException("Why cant attestation def name for stem scope be found?");
    }
    return attributeDefName;
  }

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
   * update the calculated days until recertify
   * @param group group to calculate
   * @param attributeAssignBase that has the settings, stem or group
   */
  public static void updateCalculatedDaysUntilRecertify(Group group, AttributeAssign attributeAssign) {

    AttributeAssign groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    
    String attestationDateCertified = null;
    
    if (groupAttributeAssign != null) {
      attestationDateCertified = groupAttributeAssign.getAttributeValueDelegate()
          .retrieveValueString(retrieveAttributeDefNameDateCertified().getName());
    }
    String configuredAttestationDaysUntilRecertify = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysUntilRecertify().getName());
    
    int configuredDaysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (! StringUtils.isBlank(configuredAttestationDaysUntilRecertify)) {
      configuredDaysUntilRecertify = Integer.valueOf(configuredAttestationDaysUntilRecertify);
    }
    
    // find the difference between today's date and last certified date
    // and if the difference is greater than daysUntilRecertify minus attestationDaysBeforeToRemind, then sendEmail
    int daysUntilNeedsCertify = 0;
    if (!StringUtils.isBlank(attestationDateCertified)) {
      Date lastCertifiedDate = null;
      try {
        lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(attestationDateCertified);
      } catch (ParseException e) {
        LOG.error("Could not convert "+attestationDateCertified+" to date. Attribute assign id is: "+attributeAssign.getId(), e);
        return;
      }
      long millisSinceCertify = new Date().getTime() - lastCertifiedDate.getTime();
      int daysSinceCertify = (int)TimeUnit.DAYS.convert(millisSinceCertify, TimeUnit.MILLISECONDS);
      daysUntilNeedsCertify = configuredDaysUntilRecertify - daysSinceCertify;
      if (daysUntilNeedsCertify < 0) {
        daysUntilNeedsCertify = 0;
      }
    }

    groupAttributeAssign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "" + daysUntilNeedsCertify);
    
  }
  
  /**
   * get map of email addresses to email objects for group attributes
   * @param groupAttributeAssigns
   * @return the map of email objects
   */
  protected static Map<String, Set<EmailObject>> buildAttestationGroupEmails(AttributeAssign stemAttributeAssign, Set<AttributeAssign> groupAttributeAssigns) {
    
    // map of email address to email object (group id, group name, ccList)
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign groupAttributeAssign: groupAttributeAssigns) {

      Group group = groupAttributeAssign.getOwnerGroup();
      
      AttributeAssign configurationAttributeAssign = GrouperUtil.defaultIfNull(stemAttributeAssign, groupAttributeAssign);
      
      updateCalculatedDaysUntilRecertify(group, configurationAttributeAssign);

      String daysUntilRecertifyString = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameCalculatedDaysLeft().getName());
      
      //should never be blank
      if (StringUtils.isBlank(daysUntilRecertifyString)) {
        continue;
      }
      
      int daysUntilRecertify = GrouperUtil.intValue(daysUntilRecertifyString);
      
      String attestationSendEmail = configurationAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
      String attestationDaysBeforeToRemind = configurationAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDaysBeforeToRemind().getName());
      
      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+group.getDisplayName()+" attestationSendEmail attribute is set to true so skipping sending email.");
        continue;
      }

      
      String attestationLastEmailedDate = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameEmailedDate().getName());
      if (!StringUtils.isBlank(attestationLastEmailedDate)) {
        String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        if (StringUtils.equals(attestationLastEmailedDate, today)) {
          LOG.debug("For "+groupAttributeAssign.getOwnerGroup().getDisplayName()+" attestationLastEmailedDate attribute is set to today so skipping sending email.");
          continue;
        }
      }

      
      int daysBeforeReminderEmail = 0;
      if (! StringUtils.isBlank(attestationDaysBeforeToRemind)) {
        daysBeforeReminderEmail = Integer.valueOf(attestationDaysBeforeToRemind);
      }
      
      boolean sendEmail = daysUntilRecertify <= daysBeforeReminderEmail;
            
      if (sendEmail) {
        // grab the list of email addresses from the attribute
        String[] emailAddresses = getEmailAddresses(configurationAttributeAssign, groupAttributeAssign.getOwnerGroup());
        addEmailObject(configurationAttributeAssign, emailAddresses, emails, groupAttributeAssign.getOwnerGroup());
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
  private static String[] getEmailAddresses(AttributeAssign attributeAssign, Group group) {
    
    String attestationEmailAddresses = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameEmailAddresses().getName());
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
        String emailAddress = subject.getAttributeValue(emailAttributeName);
        if (!StringUtils.isBlank(emailAttributeName) && !StringUtils.isBlank(emailAddress)) {
          addresses.add(emailAddress);
        }
      }
      
      emailAddresses = addresses.toArray(new String[addresses.size()]);
      
    } else {
      emailAddresses = GrouperUtil.splitTrim(attestationEmailAddresses, ",");
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
  private static void addEmailObject(AttributeAssign attributeAssign, String[] emailAddresses, Map<String, Set<EmailObject>> emails, Group group) {
    
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
   * run the daemon
   * @param args
   */
  public static void main(String[] args) {
    runDaemonStandalone();
  }

  /**
   * run standalone
   */
  public static void runDaemonStandalone() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setHost(GrouperUtil.hostname());
    String jobName = "OTHER_JOB_attestationDaemon";

    hib3GrouperLoaderLog.setJobName(jobName);
    hib3GrouperLoaderLog.setJobType(GrouperLoaderType.OTHER_JOB.name());
    hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
    hib3GrouperLoaderLog.store();
    
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName(jobName);
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    otherJobInput.setGrouperSession(grouperSession);
    new GrouperAttestationJob().run(otherJobInput);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    AttributeDef attributeDef = retrieveAttributeDef();
    if (attributeDef == null) {
      LOG.error(GrouperAttestationJob.attestationStemName() + ":" + ATTESTATION_DEF + " attribute def doesn't exist. Job will not proceed.");
      return null;
    }
    
    Set<AttributeAssign> groupAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.group,
        attributeDef.getId(), null, null,
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    //take out inherited
    Iterator<AttributeAssign> iterator = groupAttributeAssigns.iterator();
    
    while (iterator.hasNext()) {
      AttributeAssign attributeAssign = iterator.next();
      String directAssignmentString = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDirectAssignment().getName());

      // group has inherited attestation, don't process as group, this will be processed as stem descendent
      if (!GrouperUtil.booleanValue(directAssignmentString, false)) { 
        iterator.remove();
      }
    }
    
    Map<String, Set<EmailObject>> emails = buildAttestationGroupEmails(null, groupAttributeAssigns);
    
    LOG.info("got "+emails.size()+" from group attributes, starting building map from stem attributes.");

    otherJobInput.getHib3GrouperLoaderLog().store();
    
    Map<String, Set<EmailObject>> stemEmails = buildAttestationStemEmails();
    
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
      try {
        new GrouperEmail().setBody(emailBody.toString()).setSubject(sub).setTo(entry.getKey()).send();
      } catch (Exception e) {
        LOG.error("Error sending email", e);
      }
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
          AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(
              null, retrieveAttributeDefNameValueDef(), false, false);
          
          String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
          attributeAssign.getAttributeValueDelegate().assignValue(retrieveAttributeDefNameEmailedDate().getName(), date);
         
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
  private static void mergeEmailObjects(Map<String, Set<EmailObject>> map1, Map<String, Set<EmailObject>> map2) {
    
    for (Map.Entry<String, Set<EmailObject>> entry: map1.entrySet()) {
      
      if (map2.containsKey(entry.getKey())) {
        entry.getValue().addAll(map2.get(entry.getKey()));
      }      
    }
    
    for (Map.Entry<String, Set<EmailObject>> entry: map2.entrySet()) {
      
      if (!map1.containsKey(entry.getKey())) {
        map1.put(entry.getKey(), entry.getValue());
      }      
    }
  }
  
  /**
   * get map of email addresses to email objects for stem attributes 
   * @param attributeDef
   * @return
   */
  private Map<String, Set<EmailObject>> buildAttestationStemEmails() {
  
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.stem,
        null, retrieveAttributeDefNameValueDef().getId(), null, 
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      String attestationSendEmail = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameSendEmail().getName());
      
      boolean sendEmailAttributeValue = GrouperUtil.booleanValue(attestationSendEmail, true);
      
      // skip sending email for this attribute assign
      if (!sendEmailAttributeValue) {
        LOG.debug("For "+attributeAssign.getOwnerStem().getDisplayName()+" attestationSendEmail attribute is not set to true so skipping sending email.");
      }

      Map<String, Set<EmailObject>> localEmailMap = stemAttestationProcessHelper(attributeAssign);
      if (sendEmailAttributeValue) {
        mergeEmailObjects(emails, localEmailMap);

      }
    }
    
    return emails;
    
  }

  /**
   * take a stem attribute assign and process it
   * @param attributeAssign
   */
  public static Map<String, Set<EmailObject>> stemAttestationProcessHelper(AttributeAssign attributeAssign) {
    
    Map<String, Set<EmailObject>> emails = new HashMap<String, Set<EmailObject>>();
    
    String attestationStemScope = attributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameStemScope().getName());
    
    // go through each group and check if they have their own attestation attribute and use them if they are present.
    // if not, then use the stem attributes.
    Scope scope = GrouperUtil.defaultIfNull(Scope.valueOfIgnoreCase(attestationStemScope, false), Scope.SUB);
        
    Set<Group> childGroups = attributeAssign.getOwnerStem().getChildGroups(scope);
    
    Stem stem = attributeAssign.getOwnerStem();
    
    for (Group group: childGroups) {
      
      AttributeAssign groupAttributeAssign = group.getAttributeDelegate().retrieveAssignment(null, retrieveAttributeDefNameValueDef(), false, false);
              
      if (groupAttributeAssign == null) {
        groupAttributeAssign = group.getAttributeDelegate().assignAttribute(retrieveAttributeDefNameValueDef()).getAttributeAssign();
      }
      
      String directAssignmentString = groupAttributeAssign.getAttributeValueDelegate().retrieveValueString(retrieveAttributeDefNameDirectAssignment().getName());
      
      if (StringUtils.isBlank(directAssignmentString)) {
        groupAttributeAssign.getAttributeValueDelegate().assignValueString(retrieveAttributeDefNameDirectAssignment().getName(), "false");
        directAssignmentString = "false";
      }

      // group has direct attestation, don't use stem attributes at all.  This will be in group assignment calculations
      if (GrouperUtil.booleanValue(directAssignmentString, false)) { 
        continue;
      }

      //start at stem and look for assignment
      AttributeAssignable attributeAssignable = group.getParentStem().getAttributeDelegate()
        .getAttributeOrAncestorAttribute(retrieveAttributeDefNameValueDef().getName(), false);

      //make sure its the right stem that has the assignment
      if (!StringUtils.equals(((Stem)attributeAssignable).getName(), stem.getName())) {
        continue;
      }
      
      Set<AttributeAssign> singleGroupAttributeAssign = new HashSet<AttributeAssign>();
      singleGroupAttributeAssign.add(groupAttributeAssign);
      
      // skip sending email for this attribute assign
      Map<String, Set<EmailObject>> buildAttestationGroupEmails = buildAttestationGroupEmails(attributeAssign, singleGroupAttributeAssign);
     
      mergeEmailObjects(emails, buildAttestationGroupEmails);

    }
    
    return emails;
  }
  
  /**
   * get unique elements from array except specified by index except.
   * @param array
   * @param except
   * @return
   */
  private static Set<String> getElements(String[] array, int except) {
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
  static class EmailObject {
    
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
