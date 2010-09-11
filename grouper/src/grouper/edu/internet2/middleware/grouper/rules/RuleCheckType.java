package edu.internet2.middleware.grouper.rules;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.beans.RulesAttributeDefBean;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.rules.beans.RulesGroupBean;
import edu.internet2.middleware.grouper.rules.beans.RulesMembershipBean;
import edu.internet2.middleware.grouper.rules.beans.RulesPermissionBean;
import edu.internet2.middleware.grouper.rules.beans.RulesStemBean;
import edu.internet2.middleware.grouper.subj.SafeSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * type of checking for rules
 * @author mchyzer
 *
 */
public enum RuleCheckType {

  /** query daily for permissions that are enabled, but have a disabled date coming up */
  permissionDisabledDate {

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#checkKey(edu.internet2.middleware.grouper.rules.RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a group
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerGroupId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerAttributeDefId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to attribute def");
        }
      }
      return ruleCheck;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#addElVariables(edu.internet2.middleware.grouper.rules.RuleDefinition, java.util.Map, edu.internet2.middleware.grouper.rules.beans.RulesBean, boolean)
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition,
        Map<String, Object> variableMap, RulesBean rulesBean, boolean hasAccessToElApi) {
      permissionAssignToSubject.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {

      //this should never fire normally, we will use it from a daemon
      return null;
    }

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {

      int timestampFrom = -1;
      try {
        timestampFrom = GrouperUtil.intValue(ruleCheck.getCheckArg0(), -1);      
      } catch (Exception e) {
        return "checkArg0 timestampFrom problem " + e.getMessage();
      }
      
      int timestampTo = -1;
      try {
        timestampTo = GrouperUtil.intValue(ruleCheck.getCheckArg1(), -1);      
      } catch (Exception e) {
        return "checkArg1 timestampTo problem " + e.getMessage();
      }
      
      if (timestampTo < 0 && timestampFrom < 0) {
        return "Enter checkArg0 or checkArg1 or most likely both, " +
            "this is the days in future lower bound and upper bound";
      }
      
      return this.validate(true, ruleDefinition, ruleCheck, false, false, false, true);

    }

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#runDaemon(edu.internet2.middleware.grouper.rules.RuleDefinition)
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      boolean shouldLogThisDefinition = LOG.isDebugEnabled() || ruleDefinition.shouldLog();
      final StringBuilder logDataForThisDefinition = shouldLogThisDefinition 
        ? new StringBuilder() : null;

      try {
        AttributeDef permissionDef = RuleUtils.attributeDef(ruleDefinition.getCheck().getCheckOwnerId(),
            ruleDefinition.getCheck().getCheckOwnerName(), 
            ruleDefinition.getAttributeAssignType().getOwnerAttributeDefId(), false, true);
        
        int daysFrom = GrouperUtil.intValue(ruleDefinition.getCheck().getCheckArg0(), -1); 
        int daysTo = GrouperUtil.intValue(ruleDefinition.getCheck().getCheckArg1(), -1); 
        
        Timestamp disabledFrom = daysFrom < 0 ? null : 
          new Timestamp(System.currentTimeMillis() + daysFrom * 24 * 60 * 60 * 1000);
        Timestamp disabledTo = daysTo < 0 ? null : 
          new Timestamp(System.currentTimeMillis() + daysTo * 24 * 60 * 60 * 1000);
        
        //lets get the memberships that are of issue.  Note, if someone has
        //two memberships which are both eligible, there will be both returned...
        //probably rare but should fix at some point
        Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry()
        .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
            disabledFrom, disabledTo);
        
        RuleEngine ruleEngine = RuleEngine.ruleEngine();
        
        GrouperSession grouperSession = GrouperSession.staticGrouperSession();
        
        for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
          
          AttributeAssign attributeAssign = AttributeAssignFinder.findById(permissionEntry.getAttributeAssignId(), false);
          
          Role role = GroupFinder.findByUuid(grouperSession, permissionEntry.getRoleId(), false);

          Member member = MemberFinder.findByUuid(grouperSession, permissionEntry.getMemberId(), true);
          
          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(permissionEntry.getAttributeDefNameId(), false);
          
          AttributeDef attributeDef = attributeDefName.getAttributeDef();
          
          String action = permissionEntry.getAction();
          
          RulesPermissionBean rulesPermissionBean = new RulesPermissionBean(attributeAssign, 
              role, member, attributeDefName, attributeDef, action);
          
          ruleDefinition.getThen().fireRule(ruleDefinition, ruleEngine, 
              rulesPermissionBean, logDataForThisDefinition);
          
        }
      } finally {
        if (shouldLogThisDefinition) {
          String logMessage = logDataForThisDefinition.toString();
          if (StringUtils.isNotBlank(logMessage)) {
            if (LOG.isDebugEnabled()) {
              LOG.debug(logMessage);
            } else if (LOG.isInfoEnabled()) {
              LOG.info(logMessage);
            }
          }
        }
      }
    }
  },
  
  /** query daily for memberships that are enabled, but have a disabled date coming up */
  membershipDisabledDate {

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#checkKey(edu.internet2.middleware.grouper.rules.RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a group
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerGroupId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerGroupId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to group");
        }
      }
      return ruleCheck;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#addElVariables(edu.internet2.middleware.grouper.rules.RuleDefinition, java.util.Map, edu.internet2.middleware.grouper.rules.beans.RulesBean, boolean)
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition,
        Map<String, Object> variableMap, RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {

      //this should never fire normally, we will use it from a daemon
      return null;
    }

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {

      int timestampFrom = -1;
      try {
        timestampFrom = GrouperUtil.intValue(ruleCheck.getCheckArg0(), -1);      
      } catch (Exception e) {
        return "checkArg0 timestampFrom problem " + e.getMessage();
      }
      
      int timestampTo = -1;
      try {
        timestampTo = GrouperUtil.intValue(ruleCheck.getCheckArg1(), -1);      
      } catch (Exception e) {
        return "checkArg1 timestampTo problem " + e.getMessage();
      }
      
      if (timestampTo < 0 && timestampFrom < 0) {
        return "Enter checkArg0 or checkArg1 or most likely both, " +
        		"this is the days in future lower bound and upper bound";
      }
      
      return this.validate(true, ruleDefinition, ruleCheck, false, true, false, false);

    }

    /**
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#runDaemon(edu.internet2.middleware.grouper.rules.RuleDefinition)
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      boolean shouldLogThisDefinition = LOG.isDebugEnabled() || ruleDefinition.shouldLog();
      final StringBuilder logDataForThisDefinition = shouldLogThisDefinition 
        ? new StringBuilder() : null;

      try {
        Group group = RuleUtils.group(ruleDefinition.getCheck().getCheckOwnerId(),
            ruleDefinition.getCheck().getCheckOwnerName(), 
            ruleDefinition.getAttributeAssignType().getOwnerGroupId(), false, true);
        
        int daysFrom = GrouperUtil.intValue(ruleDefinition.getCheck().getCheckArg0(), -1); 
        int daysTo = GrouperUtil.intValue(ruleDefinition.getCheck().getCheckArg1(), -1); 
        
        Timestamp disabledFrom = daysFrom < 0 ? null : 
          new Timestamp(System.currentTimeMillis() + daysFrom * 24 * 60 * 60 * 1000);
        Timestamp disabledTo = daysTo < 0 ? null : 
          new Timestamp(System.currentTimeMillis() + daysTo * 24 * 60 * 60 * 1000);
        
        //lets get the memberships that are of issue.  Note, if someone has
        //two memberships which are both eligible, there will be both returned...
        //probably rare but should fix at some point
        Set<Membership> memberships = GrouperDAOFactory.getFactory()
          .getMembership().findAllMembershipsByGroupOwnerFieldDisabledRange(
              group.getId(), Group.getDefaultList(), disabledFrom, disabledTo);
        
        RuleEngine ruleEngine = RuleEngine.ruleEngine();
        
        for (Membership membership : GrouperUtil.nonNull(memberships)) {
          
          RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(membership, 
              membership.getGroup(), membership.getMember().getSubject());
          
          ruleDefinition.getThen().fireRule(ruleDefinition, ruleEngine, 
              rulesMembershipBean, logDataForThisDefinition);
          
        }
      } finally {
        if (shouldLogThisDefinition) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(logDataForThisDefinition.toString());
          } else if (LOG.isInfoEnabled()) {
            LOG.info(logDataForThisDefinition.toString());
          }
        }
      }
    }
    
  },
  
  /** if there is a membership(flattened) add of a group in a stem */
  flattenedMembershipAddInFolder{
  
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      return checkKeyForStem(ruleDefinition);
    }


    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipInFolder(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }
  }, 

  /** if there is a membership(flattened) remove of a group in a stem */
  flattenedMembershipRemoveInFolder{
  
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      return checkKeyForStem(ruleDefinition);
    }


    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipInFolder(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }
  }, 

  /** if there is a membership remove flattened */
  flattenedMembershipRemove {

    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a group
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerGroupId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerGroupId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to group");
        }
      }
      return ruleCheck;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {

      RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
      if (rulesMembershipBean != null) {
        Group group = rulesMembershipBean.getGroup();
        variableMap.put("groupId", group.getId());
        variableMap.put("groupName", group.getName());
        variableMap.put("groupDisplayName", group.getDisplayName());
        variableMap.put("groupExtension", group.getExtension());
        variableMap.put("groupDisplayExtension", group.getDisplayExtension());
        variableMap.put("groupDescription", group.getDescription());
        if (hasAccessToElApi) {
          variableMap.put("group", group);
        }
      }
      if (!StringUtils.isBlank(rulesMembershipBean.getMemberId())) {
        variableMap.put("memberId", rulesMembershipBean.getMemberId());
        if (hasAccessToElApi) {
          Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession().internal_getRootSession(), 
              rulesMembershipBean.getMemberId(), false);
          if (member != null) {
            variableMap.put("member", member);
          }
        }
      }
      Membership membership = rulesMembershipBean.getMembership();
      if (membership != null) {
        if (hasAccessToElApi) {
          variableMap.put("membership", membership);
        }
        variableMap.put("membershipId", membership.getUuid());
        if (membership.getDisabledTime() != null) {
          variableMap.put("membershipDisabledTimestamp", membership.getDisabledTime());
        }
        if (membership.getEnabledTime() != null) {
          variableMap.put("membershipEnabledTimestamp", membership.getEnabledTime());
        }
      }
      Subject subject = rulesMembershipBean.getSubject();
      if (subject != null) {
        if (hasAccessToElApi) {
          variableMap.put("subject", subject);
        }
        variableMap.put("safeSubject", new SafeSubject(subject));
      }
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, true, false, false);
    }

    
  },
  
  /** if there is a membership remove in transaction of remove */
  membershipRemove {

    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a group
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerGroupId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerGroupId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to group");
        }
      }
      return ruleCheck;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
      
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, true, false, false);
    }

    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleEngine ruleEngine = RuleEngine.ruleEngine();
      
      //lets get the if enum
      RuleIfConditionEnum ruleIfConditionEnum = ruleDefinition.getIfCondition().ifConditionEnum();
      
      switch (ruleIfConditionEnum) {
        
        case thisGroupHasImmediateEnabledMembership:
          
          //so basically, for the memberships in this group, where there is none in the other group, process them
          String thisGroupId = ruleDefinition.getAttributeAssignType().getOwnerGroupId();
          
          GrouperSession rootSession = GrouperSession.startRootSession(false);
          try {
            
            Group group = null;
            if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerId())) {
              group = GroupFinder.findByUuid(rootSession, ruleDefinition.getCheck().getCheckOwnerId(), false);
            } else if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerName())) {
              group = GroupFinder.findByName(rootSession, ruleDefinition.getCheck().getCheckOwnerName(), false);
            }

            if (group == null) {
              throw new RuntimeException("Group doesnt exist in rule! " + ruleDefinition);
            }

            //find the members which apply
            Set<Member> memberOrphans = GrouperDAOFactory.getFactory().getMembership().findAllMembersInOneGroupNotOtherAndType(thisGroupId, group.getUuid(), 
                MembershipType.IMMEDIATE.name(), null, null, true);

            for (Member member : GrouperUtil.nonNull(memberOrphans)) {

              RulesMembershipBean rulesMembershipBean = new RulesMembershipBean(member, group, member.getSubject());
              
              //fire the rule then clause
              RuleEngine.ruleFirings++;
              
              ruleDefinition.getThen().fireRule(ruleDefinition, ruleEngine, rulesMembershipBean, null);
              
            }
            
          } finally {
            GrouperSession.stopQuietly(rootSession);
          }

          break;
        default:
          if (!StringUtils.isBlank(ruleDefinition.getRunDaemon()) && ruleDefinition.isRunDaemonBoolean()) {
            throw new RuntimeException("This rule is explicitly set to run a daemon, but it is not implemented");
          }
      }
      
      
    }


  },
  
  /** if there is a membership remove in transaction of remove of a group in a stem */
  membershipRemoveInFolder {

    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      return checkKeyForStem(ruleDefinition);
    }

    /**
     * validate this check type
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipInFolder(this, ruleEngine, rulesBean);
      
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

  },
  
  /** if a group is created */
  groupCreate {
    
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      return checkKeyForStem(ruleDefinition);
    }

    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }

    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
      
      if (ruleThenEnum != RuleThenEnum.assignGroupPrivilegeToGroupId) {
        throw new RuntimeException("RuleThenEnum needs to be " + RuleThenEnum.assignGroupPrivilegeToGroupId);
      }
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      String privilegesString = ruleDefinition.getThen().getThenEnumArg1();
      
      Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(privilegesString, ",");
      
      Set<Privilege> privilegeSet = new HashSet<Privilege>();
      for (String privilegeString: privilegesStringSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        privilegeSet.add(privilege);
      }
      
      //so basically, for the memberships in this group, where there is none in the other group, process them
      String stemId = ruleDefinition.getAttributeAssignType().getOwnerStemId();
      
      Scope scope = ruleDefinition.getCheck().stemScopeEnum();
      
      for (Privilege privilege : privilegeSet) {
      
        Set<Group> groupsWhichNeedPrivs = GrouperSession.staticGrouperSession().getAccessResolver().getGroupsWhereSubjectDoesntHavePrivilege(
            stemId, scope, subject, privilege, false);
        
        for (Group group : GrouperUtil.nonNull(groupsWhichNeedPrivs)) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Daemon granting privilege: " + privilege + " to subject: " + GrouperUtil.subjectToString(subject) + " to group: " + group);
          }
          group.grantPriv(subject, privilege, false);
          
        }
        
      }
      
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      RulesGroupBean rulesGroupBean = (RulesGroupBean)rulesBean;
      
      Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
      
      //by id
      RuleCheck ruleCheck = new RuleCheck(this.name(), 
          rulesGroupBean.getGroup().getId(), rulesGroupBean.getGroup().getName(), null, null, null);
    
      ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
      
      return ruleDefinitions;
    }

    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesGroupBean rulesGroupBean = (RulesGroupBean)rulesBean;
      if (rulesGroupBean != null) {
        Group group = rulesGroupBean.getGroup();
        variableMap.put("groupId", group.getId());
        variableMap.put("groupName", group.getName());
        if (hasAccessToElApi) {
          variableMap.put("group", group);
        }
      }
    }
   
  },
  
  /** if a stem is created */
  stemCreate {
    
    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      RulesStemBean rulesStemBean = (RulesStemBean)rulesBean;
      
      Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
      
      //by id
      RuleCheck ruleCheck = new RuleCheck(this.name(), 
          rulesStemBean.getStem().getUuid(), rulesStemBean.getStem().getName(), null, null, null);
    
      ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
      
      return ruleDefinitions;
    }

    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      return checkKeyForStem(ruleDefinition);
    }

    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesStemBean rulesStemBean = (RulesStemBean)rulesBean;
      if (rulesStemBean != null) {
        Stem stem = rulesStemBean.getStem();
        variableMap.put("stemId", stem.getUuid());
        variableMap.put("stemName", stem.getName());
        if (hasAccessToElApi) {
          variableMap.put("stem", stem);
        }
      }
    }
   
    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
      
      if (ruleThenEnum != RuleThenEnum.assignStemPrivilegeToStemId) {
        throw new RuntimeException("RuleThenEnum needs to be " + RuleThenEnum.assignStemPrivilegeToStemId);
      }
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      String privilegesString = ruleDefinition.getThen().getThenEnumArg1();
      
      Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(privilegesString, ",");
      
      Set<Privilege> privilegeSet = new HashSet<Privilege>();
      for (String privilegeString: privilegesStringSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        privilegeSet.add(privilege);
      }
      
      //so basically, for the memberships in this group, where there is none in the other group, process them
      String stemId = ruleDefinition.getAttributeAssignType().getOwnerStemId();
      
      Scope scope = ruleDefinition.getCheck().stemScopeEnum();
      
      for (Privilege privilege : privilegeSet) {
      
        Set<Stem> stemsWhichNeedPrivs = GrouperSession.staticGrouperSession().getNamingResolver().getStemsWhereSubjectDoesntHavePrivilege(
            stemId, scope, subject, privilege, false);
        
        for (Stem stem : GrouperUtil.nonNull(stemsWhichNeedPrivs)) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Daemon granting privilege: " + privilege + " to subject: " + GrouperUtil.subjectToString(subject) + " to stem: " + stem);
          }
          stem.grantPriv(subject, privilege, false);
          
        }
        
      }
      
    }

  }, 
  
  /** if there is a membership add in transaction */
  membershipAdd{
  
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a group
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerGroupId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerGroupId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to group");
        }
      }
      return ruleCheck;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipAdd.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, true, false, false);
    }

  }, 
  
  /** if there is a membership remove flattened */
  flattenedMembershipAdd{
  
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a group
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerGroupId())) {

          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerGroupId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to group");
        }
      }
      return ruleCheck;
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembership(this, ruleEngine, rulesBean);
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
  
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, true, false, false);
    }

  }, 
  
  /** if there is a membership remove in transaction of remove of a group in a stem */
  membershipAddInFolder{
  
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      return checkKeyForStem(ruleDefinition);
    }

    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipInFolder(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      flattenedMembershipRemove.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }
  }, 
  
  /** if a group is created */
  attributeDefCreate{
    
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {

      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
        //if this is assigned to a stem
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {
  
          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this stem
          Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ruleDefinition.getAttributeAssignType().getOwnerStemId(), true);
          ruleCheck.setCheckOwnerName(stem.getName());
        } else {
          LOG.error("Not sure why no check owner if not assigned to stem");
        }
      }
      return ruleCheck;
    }
  
    /**
     * validate this check type
     * @param ruleDefinition
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return validate(false, ruleDefinition, ruleCheck, true, false, true, false);
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      RulesAttributeDefBean rulesAttributeDefBean = (RulesAttributeDefBean)rulesBean;
      
      Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
      
      //by id
      RuleCheck ruleCheck = new RuleCheck(this.name(), 
          rulesAttributeDefBean.getAttributeDef().getId(), rulesAttributeDefBean.getAttributeDef().getName(), null, null, null);
    
      ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
      
      return ruleDefinitions;
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesAttributeDefBean rulesAttributeDefBean = (RulesAttributeDefBean)rulesBean;
      if (rulesAttributeDefBean != null) {
        AttributeDef attributeDef = rulesAttributeDefBean.getAttributeDef();
        variableMap.put("attributeDefId", attributeDef.getId());
        variableMap.put("attributeDefName", attributeDef.getName());
        if (hasAccessToElApi) {
          variableMap.put("attributeDef", attributeDef);
        }
      }
    }
   
    /**
     * 
     */
    @Override
    public void runDaemon(RuleDefinition ruleDefinition) {
      
      RuleThenEnum ruleThenEnum = ruleDefinition.getThen().thenEnum();
      
      if (ruleThenEnum != RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) {
        throw new RuntimeException("RuleThenEnum needs to be " + RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId);
      }
      
      String subjectString = ruleDefinition.getThen().getThenEnumArg0();
      Subject subject = SubjectFinder.findByPackedSubjectString(subjectString, true);
      String privilegesString = ruleDefinition.getThen().getThenEnumArg1();
      
      Set<String> privilegesStringSet = GrouperUtil.splitTrimToSet(privilegesString, ",");
      
      Set<Privilege> privilegeSet = new HashSet<Privilege>();
      for (String privilegeString: privilegesStringSet) {
        Privilege privilege = Privilege.getInstance(privilegeString);
        privilegeSet.add(privilege);
      }
      
      //so basically, for the memberships in this group, where there is none in the other group, process them
      String stemId = ruleDefinition.getAttributeAssignType().getOwnerStemId();
      
      Scope scope = ruleDefinition.getCheck().stemScopeEnum();
      
      for (Privilege privilege : privilegeSet) {
      
        Set<AttributeDef> attributeDefsWhichNeedPrivs = GrouperSession.staticGrouperSession().getAttributeDefResolver()
          .getAttributeDefsWhereSubjectDoesntHavePrivilege(
            stemId, scope, subject, privilege, false);
        
        for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefsWhichNeedPrivs)) {
          
          if (LOG.isDebugEnabled()) {
            LOG.debug("Daemon granting privilege: " + privilege 
                + " to subject: " + GrouperUtil.subjectToString(subject) + " to attributeDef: " + attributeDef);
          }
          attributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, false);
          
        }
        
      }
      
    }
  }, 

  /**
   * if a permission is assigned to a subject
   */
  flattenedPermissionAssignToSubject {

    /**
     * @see RuleCheckType#addElVariables(RuleDefinition, Map, RulesBean, boolean)
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition,
        Map<String, Object> variableMap, RulesBean rulesBean, boolean hasAccessToElApi) {
      permissionAssignToSubject.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * @see RuleCheckType#ruleDefinitions(RuleEngine, RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      return ruleDefinitionsPermission(this, ruleEngine, rulesBean);
    }

    /**
     * @see RuleCheckType#validate(RuleDefinition, RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, false, false, true);
    }
    
  },
  
  /**
   * if a permission is assigned to a subject
   */
  flattenedPermissionRemoveFromSubject {

    /**
     * @see RuleCheckType#addElVariables(RuleDefinition, Map, RulesBean, boolean)
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition,
        Map<String, Object> variableMap, RulesBean rulesBean, boolean hasAccessToElApi) {
      permissionAssignToSubject.addElVariables(ruleDefinition, variableMap, rulesBean, hasAccessToElApi);
    }

    /**
     * @see RuleCheckType#ruleDefinitions(RuleEngine, RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      return ruleDefinitionsPermission(this, ruleEngine, rulesBean);
    }

    /**
     * @see RuleCheckType#validate(RuleDefinition, RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, false, false, true);
    }
    
  },
  
  /** if there is a permission assign in transaction to a subject, not to a role */
  permissionAssignToSubject {
  
    /**
     * @see RuleCheckType#checkKey(RuleDefinition)
     */
    @Override
    public RuleCheck checkKey(RuleDefinition ruleDefinition) {
      RuleCheck ruleCheck = ruleDefinition.getCheck();
      if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {

        //if this is assigned to an attributeDef
        if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerAttributeDefId())) {
  
          //clone so we dont edit the object
          ruleCheck = ruleCheck.clone();
          //set the owner to this group
          ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerAttributeDefId());
        } else {
          LOG.error("Not sure why no check owner if not assigned to attributeDef");
        }
      }
      return ruleCheck;
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsPermission(this, ruleEngine, rulesBean);
      
    }
  
    /**
     * 
     */
    @Override
    public void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
        RulesBean rulesBean, boolean hasAccessToElApi) {
      RulesPermissionBean rulesPermissionBean = (RulesPermissionBean)rulesBean;
      if (rulesPermissionBean != null) {
        Role role = rulesPermissionBean.getRole();
        variableMap.put("roleId", role.getId());
        variableMap.put("roleName", role.getName());
        variableMap.put("roleDisplayName", role.getDisplayName());
        variableMap.put("roleExtension", role.getExtension());
        variableMap.put("roleDisplayExtension", role.getDisplayExtension());
        variableMap.put("roleDescription", role.getDescription());
        if (hasAccessToElApi) {
          variableMap.put("role", role);
        }
      }
      if (!StringUtils.isBlank(rulesPermissionBean.getMemberId())) {
        variableMap.put("memberId", rulesPermissionBean.getMemberId());
        Member member = rulesPermissionBean.getMember();
        if (member != null) {
          if (hasAccessToElApi) {
            variableMap.put("member", member);
          }
          Subject subject = member.getSubject();
          if (subject != null) {
            if (hasAccessToElApi) {
              variableMap.put("subject", subject);
            }
            variableMap.put("safeSubject", new SafeSubject(subject));
          }
        }
        

        
      }
      String action = rulesPermissionBean.getAction();
      if (!StringUtils.isBlank(action)) {
        variableMap.put("action", rulesPermissionBean.getAction());
      }
      AttributeDef attributeDef = rulesPermissionBean.getAttributeDef();
      if (attributeDef != null) {
        variableMap.put("nameOfAttributeDef", attributeDef.getName());
        variableMap.put("attributeDefExtension", attributeDef.getExtension());
        variableMap.put("attributeDefId", attributeDef.getId());
        if (hasAccessToElApi) {
          variableMap.put("attributeDef", attributeDef);
        }
      }
      AttributeDefName attributeDefName = rulesPermissionBean.getAttributeDefName();
      if (attributeDefName != null) {
        variableMap.put("attributeDefNameName", attributeDefName.getName());
        variableMap.put("attributeDefNameId", attributeDefName.getId());
        variableMap.put("attributeDefNameExtension", attributeDefName.getExtension());
        variableMap.put("attributeDefNameDisplayName", attributeDefName.getDisplayName());
        variableMap.put("attributeDefNameDescription", attributeDefName.getDescription());
        variableMap.put("attributeDefNameDisplayExtension", attributeDefName.getDisplayExtension());
        if (hasAccessToElApi) {
          variableMap.put("attributeDefName", attributeDefName);
        }
      }
      AttributeAssign attributeAssign = rulesPermissionBean.getAttributeAssign();
      if (attributeAssign != null) {
        variableMap.put("attributeAssignId", attributeAssign.getId());
        if (attributeAssign.getDisabledTime() != null) {
          variableMap.put("permissionDisabledTimestamp", attributeAssign.getDisabledTime());
        }
        if (attributeAssign.getEnabledTime() != null) {
          variableMap.put("permissionEnabledTimestamp", attributeAssign.getEnabledTime());
        }

        if (hasAccessToElApi) {
          variableMap.put("attributeAssign", attributeAssign);
        }
      }
      
    }
  
    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#validate(RuleDefinition, edu.internet2.middleware.grouper.rules.RuleCheck)
     */
    @Override
    public String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck) {
      return this.validate(false, ruleDefinition, ruleCheck, false, false, false, true);
    }
  
  };

  /**
   * get the check key for the index
   * @param ruleDefinition
   * @return the rule check for the index
   */
  public RuleCheck checkKey(RuleDefinition ruleDefinition) {
    return ruleDefinition.getCheck();
  }
  
  /**
   * validate this check type
   * @param ruleDefinition
   * @param ruleCheck 
   * @return the error or null if valid
   */
  public abstract String validate(RuleDefinition ruleDefinition, RuleCheck ruleCheck);

  /**
   * get the check object from the rules bean
   * @param ruleEngine
   * @param rulesBean
   * @return the rules
   */
  public abstract Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean);

  /**
   * add EL variables to the substitute map
   * @param ruleDefinition 
   * @param variableMap
   * @param rulesBean 
   * @param hasAccessToElApi 
   */
  public abstract void addElVariables(RuleDefinition ruleDefinition, Map<String, Object> variableMap, 
      RulesBean rulesBean, boolean hasAccessToElApi);

  
  /**
   * validate this check type
   * @param allowArgs 
   * @param ruleDefinition
   * @param ruleCheck 
   * @param requireStemScope true to require, false to require blank
   * @param ownerIsGroup 
   * @param ownerIsStem 
   * @param ownerIsAttributeDef 
   * @return the error or null if valid
   */
  public String validate(boolean allowArgs, RuleDefinition ruleDefinition, RuleCheck ruleCheck, 
      boolean requireStemScope, boolean ownerIsGroup, boolean ownerIsStem, boolean ownerIsAttributeDef) {
    
    if (!StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && !StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
      return "Enter one and only one of checkOwnerId and checkOwnerName!";
    }
    if (requireStemScope) {
      if (StringUtils.isBlank(ruleCheck.getCheckStemScope()) && StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {
        return "Enter the checkStemScope of ALL or SUB for the folder based rule!";
      }
      try {
        Stem.Scope.valueOfIgnoreCase(ruleCheck.getCheckStemScope(), true);
      } catch (Exception e) {
        return e.getMessage();
      }
    } else {
      if (!StringUtils.isBlank(ruleCheck.getCheckStemScope())) {
        return "Cant put checkStemScope in this ruleCheckType, not allowed";
      }
    }
    
    if (ownerIsGroup) {
      String result = ruleCheck.validateOwnerGroup(ruleDefinition);
      if (!StringUtils.isBlank(result)) {
        return result;
      }
    }
    
    if (ownerIsStem) {
      String result = ruleCheck.validateOwnerStem(ruleDefinition);
      if (!StringUtils.isBlank(result)) {
        return result;
      }
    }
    
    if (ownerIsAttributeDef) {
      String result = ruleCheck.validateOwnerAttributeDef(ruleDefinition);
      if (!StringUtils.isBlank(result)) {
        return result;
      }
    }
    
    if (!allowArgs) {
      if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckArg0()) 
          || !StringUtils.isBlank(ruleDefinition.getCheck().getCheckArg1())) {
        return "Should not use checkArg0 or checkArg1";
      }
    }
    
    //if there is a stem scope, then owner is stem
    return null;
  }
  
  /**
   * for a membership remove, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsMembership(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesMembershipBean.getGroup().getId(), rulesMembershipBean.getGroup().getName(), null, null, null);
  
    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrId(ruleCheck)));
    
    return ruleDefinitions;
  }

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleCheckType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleCheckType.class, 
        string, exceptionOnNull);

  }
  
  /**
   * for a membership remove, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsMembershipInFolder(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesMembershipBean.getGroup().getId(), rulesMembershipBean.getGroup().getName(), null, null, null);

    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
    
    return ruleDefinitions;
  }

  /**
   * run the daemon to sync up the state
   * @param ruleDefinition
   */
  public void runDaemon(RuleDefinition ruleDefinition) {
    throw new RuntimeException("Not implemented daemon: " + ruleDefinition);
  }

  /**
   * for a permission assign, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsPermission(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesPermissionBean rulesPermissionBean = (RulesPermissionBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesPermissionBean.getAttributeDef().getId(), rulesPermissionBean.getAttributeDef().getName(), null, null, null);
  
    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrId(ruleCheck)));
    
    return ruleDefinitions;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleCheckType.class);

  /**
   * 
   * @param ruleDefinition
   * @return rule check
   */
  public static RuleCheck checkKeyForStem(RuleDefinition ruleDefinition) {
    
    RuleCheck ruleCheck = ruleDefinition.getCheck();
    
    if (!StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
      return ruleCheck;
    }

    //clone so we dont edit the object
    ruleCheck = ruleCheck.clone();

    if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) && StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
      //if this is assigned to a stem
      if (!StringUtils.isBlank(ruleDefinition.getAttributeAssignType().getOwnerStemId())) {
        
        ruleCheck.setCheckOwnerId(ruleDefinition.getAttributeAssignType().getOwnerStemId());
      } else {
        LOG.error("Not sure why no check owner if not assigned to stem");
        return ruleCheck;
      }
    }
    
    //we need it by name so we can do stem matches...
    String stemId = ruleCheck.getCheckOwnerId();
    ruleCheck.setCheckOwnerId(null);
    //set the owner to this stem
    Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, true);
    ruleCheck.setCheckOwnerName(stem.getName());
    
    return ruleCheck;

  }
  
}
