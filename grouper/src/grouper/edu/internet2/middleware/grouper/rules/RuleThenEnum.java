/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleThenEnum {

  /** remove the member (the current one being acted on) from the owner group */
  removeMemberFromOwnerGroup {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
      
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), 
          ruleDefinition.getAttributeAssignType().getOwnerGroupId(), true);
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), rulesBean.getMemberId(), true);
      return group.deleteMember(member, false);
    }
    
  }, 
  
  /** assign privilege(s) to subject on the group being acted on (groupId) */
  assignGroupPrivilegeToGroupId {
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(subjectString)) {
        return "ruleThenEnumArg0 is the subject string to assign to and is required, e.g. g:gsa::::::someFolder:someGroup";
      }
      
      if (StringUtils.isBlank(privileges)) {
        return "ruleThenEnumArg1 are the access privileges, e.g. read,update,admin";
      }
      
      try {
        SubjectFinder.findByPackedSubjectString(subjectString, true);
      } catch (Exception e) {
        return e.getMessage();
      }

      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
      for (String privilegeString : privilegesSet) {
        Privilege privilege = null;
        
        try {
          privilege = Privilege.getInstance(privilegeString);
        } catch (Exception e) {
          return e.getMessage();
        }
        if (!Privilege.isAccess(privilege)) {
          return "Privilege '" + privilegeString + "' must be an access privilege, e.g. admin, read, update, view, optin, optout";
        }
      }
      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      Group group = rulesBean.getGroup();
      
      Subject subject = SubjectFinder.findByPackedSubjectString(ruleDefinition.getThen().getThenEnumArg0(), true);

      String privileges = ruleDefinition.getThen().getThenEnumArg1();

      if (LOG.isDebugEnabled()) {
        LOG.debug("assignGroupPrivilege: from group: " + group 
            + ", subject: " + GrouperUtil.subjectToString(subject) 
            + " privilegeNamesCommaSeparated: " + privileges);
      }

      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");

      boolean result = false;

      for (String privilegeString : privilegesSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        if (group.grantPriv(subject, privilege, true)) {
          result = true;
        }
      }
      
      return result;
    }
    
  }, 
  
  /** assign privilege(s) to subject on the stem being acted on (stemId) */
  assignStemPrivilegeToStemId{
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(subjectString)) {
        return "ruleThenEnumArg0 is the subject string to assign to and is required, e.g. g:gsa::::::someFolder:someGroup";
      }
      
      if (StringUtils.isBlank(privileges)) {
        return "ruleThenEnumArg1 are the naming privileges, e.g. stem,create";
      }
      
      try {
        SubjectFinder.findByPackedSubjectString(subjectString, true);
      } catch (Exception e) {
        return e.getMessage();
      }
  
      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
      for (String privilegeString : privilegesSet) {
        Privilege privilege = null;
        
        try {
          privilege = Privilege.getInstance(privilegeString);
        } catch (Exception e) {
          return e.getMessage();
        }
        if (!Privilege.isNaming(privilege)) {
          return "Privilege '" + privilegeString + "' must be a naming privilege, e.g. stem, create";
        }
      }
      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
  
      Stem stem = rulesBean.getStem();
      
      Subject subject = SubjectFinder.findByPackedSubjectString(ruleDefinition.getThen().getThenEnumArg0(), true);
  
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
  
      if (LOG.isDebugEnabled()) {
        LOG.debug("assignStemPrivilege: from stem: " + stem 
            + ", subject: " + GrouperUtil.subjectToString(subject) 
            + " privilegeNamesCommaSeparated: " + privileges);
      }
  
      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
  
      boolean result = false;
  
      for (String privilegeString : privilegesSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        if (stem.grantPriv(subject, privilege, true)) {
          result = true;
        }
      }
      
      return result;
    }
    
  }, 
  
  /** assign privilege(s) to subject on the attributeDef being acted on (attributeDefId) */
  assignAttributeDefPrivilegeToAttributeDefId {
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {

      if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg2())) {
        return "ruleThenEnumArg2 should not be entered for this ruleThenEnum: " + this.name();
      }

      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      String privileges = ruleDefinition.getThen().getThenEnumArg1();
      
      if (StringUtils.isBlank(subjectString)) {
        return "ruleThenEnumArg0 is the subject string to assign to and is required, e.g. g:gsa::::::someFolder:someGroup";
      }
      
      if (StringUtils.isBlank(privileges)) {
        return "ruleThenEnumArg1 are the attrDef privileges, e.g. attrRead,attrUpdate,attrAdmin";
      }
      
      try {
        SubjectFinder.findByPackedSubjectString(subjectString, true);
      } catch (Exception e) {
        return e.getMessage();
      }
  
      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
      for (String privilegeString : privilegesSet) {
        Privilege privilege = null;
        
        try {
          privilege = Privilege.getInstance(privilegeString);
        } catch (Exception e) {
          return e.getMessage();
        }
        if (!Privilege.isAttributeDef(privilege)) {
          return "Privilege '" + privilegeString + "' must be an attributeDef privilege, e.g. attrAdmin, attrRead, attrUpdate, attrView, attrOptin, attrOptout";
        }
      }
      return null;
    }
    
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
  
      AttributeDef attributeDef = rulesBean.getAttributeDef();
      
      Subject subject = SubjectFinder.findByPackedSubjectString(ruleDefinition.getThen().getThenEnumArg0(), true);
  
      String privileges = ruleDefinition.getThen().getThenEnumArg1();

      if (LOG.isDebugEnabled()) {
        LOG.debug("assignAttributeDefPrivilege: from attributeDef: " + attributeDef 
            + ", subject: " + GrouperUtil.subjectToString(subject) 
            + " privilegeNamesCommaSeparated: " + privileges);
      }

      Set<String> privilegesSet = GrouperUtil.splitTrimToSet(privileges, ",");
  
      boolean result = false;
  
      for (String privilegeString : privilegesSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        if (attributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, true)) {
          result = true;
        }
      }
      
      return result;
    }
    
  }, 
  
  /** <pre>
   * send an email about this action.
   * arg0: comma separated email addresses to send to.  ${subjectEmail} is a variable which evaluates to the email of the subject (if applicable)
   * arg1: subject (some text/EL), or template: templateName 
   * arg2: body (some text/EL), or template: templateName
   * The template name comes from the directory in grouper.properties: rules.emailTemplatesFolder
   * </pre>
   */
  sendEmail {
  
    /**
     * @see RuleThenEnum#validate(RuleDefinition)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      String toAddressesString = ruleDefinition.getThen().getThenEnumArg0();
      String subjectString = ruleDefinition.getThen().getThenEnumArg1();
      String bodyString = ruleDefinition.getThen().getThenEnumArg2();
      
      if (StringUtils.isBlank(toAddressesString)) {
        return "sendEmail ruleThenEnum requires ruleThenArg0 to be the comma separated addresses to send the email to";
      }
        
      if (StringUtils.isBlank(subjectString)) {
        return "sendEmail ruleThenEnum requires ruleThenArg1 to be the subject EL script or template: templateName ";
      }
        
      if (StringUtils.isBlank(bodyString)) {
        return "sendEmail ruleThenEnum requires ruleThenArg2 to be the body EL script or template: templateName ";
      }

      //see if these are templated, and if so, see if they exist and stuff
      try {
        RuleUtils.emailTemplate(subjectString);
        RuleUtils.emailTemplate(bodyString);
      } catch (Exception e) {
        return e.getMessage();
      }
      
      
      return null;
    }
    

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

      String toAddressesString = ruleDefinition.getThen().getThenEnumArg0();
      
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg1();
      String bodyString = ruleDefinition.getThen().getThenEnumArg2();
      
      String subjectTemplate = RuleUtils.emailTemplate(subjectString);
      String bodyTemplate = RuleUtils.emailTemplate(bodyString);
      
      Map<String, Object> variableMap =  new HashMap<String, Object>();

      Subject actAsSubject = ruleDefinition.getActAs().subject(true);
      boolean hasAccessToEl = RuleEngine.hasAccessToElApi(actAsSubject);

      ruleDefinition.addElVariables(variableMap, rulesBean, hasAccessToEl);
      
      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", EL variables: ");
        for (String varName : variableMap.keySet()) {
          logDataForThisDefinition.append(varName);
          Object value = variableMap.get(varName);
          if (value instanceof String) {
            logDataForThisDefinition.append("(").append(value).append(")");
          }
          logDataForThisDefinition.append(",");
        }
      }
      
      toAddressesString = GrouperUtil.substituteExpressionLanguage(toAddressesString, variableMap);

      String subject = GrouperUtil.substituteExpressionLanguage(subjectTemplate, variableMap);
      
      String body = GrouperUtil.substituteExpressionLanguage(bodyTemplate, variableMap);

      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", toAddressesString: ").append(toAddressesString);
        logDataForThisDefinition.append(", subject: ").append(subject);
        logDataForThisDefinition.append(", body: ").append(body);
      }
      
      new GrouperEmail().setTo(toAddressesString).setBody(body).setSubject(subject).send();
      
      return true;
    }
    
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleThenEnum valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleThenEnum.class, 
        string, exceptionOnNull);

  }

  /**
   * fire this rule
   * @param ruleDefinition
   * @param ruleEngine
   * @param rulesBean
   * @param logDataForThisDefinition is null if not logging, and non null if things should be appended
   * @return something for log
   */
  public abstract Object fireRule(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean, StringBuilder logDataForThisDefinition);
  
  /**
   * validate the rule
   * @param ruleDefinition
   * @return the validation reason
   */
  public String validate(RuleDefinition ruleDefinition) {
    if (!StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg0()) 
        || !StringUtils.isBlank(ruleDefinition.getThen().getThenEnumArg1())) {
      return "This ruleThenEnum does not take any arguments: " + this;
    }
    return null;
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleThenEnum.class);

}
