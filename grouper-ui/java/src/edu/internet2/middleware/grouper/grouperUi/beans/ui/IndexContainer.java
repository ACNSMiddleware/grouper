/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * index page and common functions beans
 * @author mchyzer
 *
 */
public class IndexContainer {

  /**
   * recent activity
   */
  private Set<GuiAuditEntry> guiAuditEntriesRecentActivity;
  
  /**
   * recent activity
   * @return audits
   */
  public Set<GuiAuditEntry> getGuiAuditEntriesRecentActivity() {
    
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    try {
      if (this.guiAuditEntriesRecentActivity == null) {
        if (LOG.isDebugEnabled()) {
          debugLog.put("inittingRecentActivity", true);
        }
        GrouperSession grouperSession = GrouperSession.staticGrouperSession();
        Subject subject = grouperSession.getSubject();

        if (LOG.isDebugEnabled()) {
          debugLog.put("userName", subject == null ? null : subject.getId() + " - " + subject.getName());
        }
        
        Member member = MemberFinder.findBySubject(grouperSession, subject, true);

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.paging(6, 1, false);

        Set<AuditEntry> auditEntries = GrouperDAOFactory.getFactory().getAuditEntry().findByActingUser(member.getUuid(), queryOptions);
  
        if (LOG.isDebugEnabled()) {
          debugLog.put("resultsFromDb", GrouperUtil.length(auditEntries));
        }
        
        this.guiAuditEntriesRecentActivity = GuiAuditEntry.convertFromAuditEntries(auditEntries);
  
      } else {
        if (LOG.isDebugEnabled()) {
          debugLog.put("inittingRecentActivity", false);
        }
      }

      if (LOG.isDebugEnabled()) {
        debugLog.put("recentActivitySize", GrouperUtil.length(this.guiAuditEntriesRecentActivity));
      }
      
      return this.guiAuditEntriesRecentActivity;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugLog));
      }
    }
  }

  /**
   * for index page, this is a short list of groups the user manages
   */
  private Set<GuiGroup> guiGroupsUserManagesAbbreviated;

  /**
   * for the index page, this is a short list of stems the user manages
   */
  private Set<GuiStem> guiStemsUserManagesAbbreviated;
  
  /**
   * get the stems the user manages, size 10 for front screen
   * @return the stems
   */
  public Set<GuiStem> getGuiStemsUserManagesAbbreviated() {

    if (this.guiGroupsUserManagesAbbreviated == null) {
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Set<Stem> stems = new StemFinder().assignSubject(grouperSession.getSubject())
          .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().paging(
              GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10),
              1, false)).findStems();

      this.guiStemsUserManagesAbbreviated = GuiStem.convertFromStems(stems);
            
    }

    return this.guiStemsUserManagesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefs favorites
   */
  private Set<GuiAttributeDef> guiAttributeDefsMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of attributeDefNames favorites
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of groups the user manages, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsUserManagesAbbreviated() {
    
    if (this.guiGroupsUserManagesAbbreviated == null) {
      
      Set<Group> groups = new GroupFinder()
          .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().paging(GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false)).findGroups();

      this.guiGroupsUserManagesAbbreviated = GuiGroup.convertFromGroups(groups);
            
    }
    
    return this.guiGroupsUserManagesAbbreviated;
  }

  /**
   * for index page, this is a short list of groups the user is a member of, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsMyMembershipsAbbreviated() {
    
    if (this.guiGroupsMyMembershipsAbbreviated == null) {
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Set<Group> groups = new GroupFinder()
          .assignSubject(grouperSession.getSubject())
          .assignField(Group.getDefaultList())
          .assignPrivileges(AccessPrivilege.READ_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().paging(
              GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false)).findGroups();

      this.guiGroupsMyMembershipsAbbreviated = GuiGroup.convertFromGroups(groups);
            
    }
    
    return this.guiGroupsMyMembershipsAbbreviated;
  }

  /**
   * for index page, this is a short list of groups the user has favorited
   */
  private Set<GuiGroup> guiGroupsMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of groups the user is a member of
   */
  private Set<GuiGroup> guiGroupsMyMembershipsAbbreviated;

  /**
   * for index page, this is a short list of subjects the user has favorited
   */
  private Set<GuiMember> guiMembersMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of groups the user has favorited, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsMyFavoritesAbbreviated() {
    
    if (this.guiGroupsMyFavoritesAbbreviated == null) {
      
      Set<Group> groups = GrouperUserDataApi.favoriteGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiGroupsMyFavoritesAbbreviated = GuiGroup.convertFromGroups(groups, "uiV2.index.maxFavoritesEachType", 5);
    }
    
    return this.guiGroupsMyFavoritesAbbreviated;
  }

  /**
   * 
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesMyServices;
  
  /**
   * for index page, this is a short list of stems the user has favorited
   */
  private Set<GuiStem> guiStemsMyFavoritesAbbreviated;

  /**
   * for index page, this is a short list of attributeDefNames RecentlyUsed
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of attributeDefs RecentlyUsed
   */
  private Set<GuiAttributeDef> guiAttributeDefsRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of groups the user has RecentlyUsed
   */
  private Set<GuiGroup> guiGroupsRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of subjects the user has RecentlyUsed
   */
  private Set<GuiMember> guiMembersRecentlyUsedAbbreviated;

  /**
   * for index page, this is a short list of stems the user has RecentlyUsed
   */
  private Set<GuiStem> guiStemsRecentlyUsedAbbreviated;

  /** logger */
  protected static final Log LOG = LogFactory.getLog(IndexContainer.class);

  /**
   * for index page, this is a short list of user's services, lazy load if null
   * @return the list of services
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyServices() {
    
    if (this.guiAttributeDefNamesMyServices == null) {
      
      GrouperSession grouperSessionOuter = GrouperSession.staticGrouperSession();
      final Subject subject = grouperSessionOuter.getSubject();
      
      GrouperSession.callbackGrouperSession(grouperSessionOuter.internal_getRootSession(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignAnyRole(true)
              .assignSubject(subject).assignQueryOptions(new QueryOptions().paging(GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.index.numberOfObjectsInSectionDefault", 10), 1, false))
              .findAttributeNames();
              
          IndexContainer.this.guiAttributeDefNamesMyServices = GuiAttributeDefName.convertFromAttributeDefNames(attributeDefNames);

          return null;
        }
      });
      
      
      
    }
    
    return this.guiAttributeDefNamesMyServices;
  }

  /**
   * for index page, this is a short list of stems the user has favorited, lazy load if null
   * @return the list of stems
   */
  public Set<GuiStem> getGuiStemsMyFavoritesAbbreviated() {
    
    if (this.guiStemsMyFavoritesAbbreviated == null) {
      
      Set<Stem> stems = GrouperUserDataApi.favoriteStems(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          GrouperSession.staticGrouperSession().getSubject());
      
      this.guiStemsMyFavoritesAbbreviated = GuiStem.convertFromStems(stems, "uiV2.index.maxFavoritesEachType", 5);
      
    }
    
    return this.guiStemsMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of members the user has favorited, lazy load if null
   * @return the list of members
   */
  public Set<GuiMember> getGuiMembersMyFavoritesAbbreviated() {
    
    if (this.guiMembersMyFavoritesAbbreviated == null) {
      
      Set<Member> members = GrouperUserDataApi.favoriteMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiMembersMyFavoritesAbbreviated = GuiMember.convertFromMembers(members, "uiV2.index.maxFavoritesEachType", 5);
    }
    
    return this.guiMembersMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefNames the user has favorited, lazy load if null
   * @return the list of attributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyFavoritesAbbreviated() {
    
    if (this.guiAttributeDefNamesMyFavoritesAbbreviated == null) {
      
      Set<AttributeDefName> attributeDefNames = GrouperUserDataApi.favoriteAttributeDefNames(
          GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiAttributeDefNamesMyFavoritesAbbreviated = GuiAttributeDefName.convertFromAttributeDefNames(
          attributeDefNames, "uiV2.index.maxFavoritesEachType", 5);
    }
    
    return this.guiAttributeDefNamesMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefs the user has favorited, lazy load if null
   * @return the list of attributeDefs
   */
  public Set<GuiAttributeDef> getGuiAttributeDefsMyFavoritesAbbreviated() {
    
    if (this.guiAttributeDefsMyFavoritesAbbreviated == null) {
      
      Set<AttributeDef> attributeDefs = GrouperUserDataApi.favoriteAttributeDefs(
          GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiAttributeDefsMyFavoritesAbbreviated = GuiAttributeDef.convertFromAttributeDefs(
          attributeDefs, "uiV2.index.maxFavoritesEachType", 5);
    }
    
    return this.guiAttributeDefsMyFavoritesAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefNames the user has RecentlyUsed, lazy load if null
   * @return the list of attributeDefNames
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesRecentlyUsedAbbreviated() {
    
    if (this.guiAttributeDefNamesRecentlyUsedAbbreviated == null) {
      
      Set<AttributeDefName> attributeDefNames = GrouperUserDataApi.recentlyUsedAttributeDefNames(
          GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiAttributeDefNamesRecentlyUsedAbbreviated = GuiAttributeDefName.convertFromAttributeDefNames(
          attributeDefNames, "uiV2.index.maxRecentlyUsedEachType", 5);
    }
    
    return this.guiAttributeDefNamesRecentlyUsedAbbreviated;
  }

  /**
   * for index page, this is a short list of attributeDefs the user has RecentlyUsed, lazy load if null
   * @return the list of attributeDefs
   */
  public Set<GuiAttributeDef> getGuiAttributeDefsRecentlyUsedAbbreviated() {
    
    if (this.guiAttributeDefsRecentlyUsedAbbreviated == null) {
      
      Set<AttributeDef> attributeDefs = GrouperUserDataApi.recentlyUsedAttributeDefs(
          GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiAttributeDefsRecentlyUsedAbbreviated = GuiAttributeDef.convertFromAttributeDefs(
          attributeDefs, "uiV2.index.maxRecentlyUsedEachType", 5);
    }
    
    return this.guiAttributeDefsRecentlyUsedAbbreviated;
  }

  /**
   * for index page, this is a short list of groups the user has RecentlyUsed, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsRecentlyUsedAbbreviated() {
    
    if (this.guiGroupsRecentlyUsedAbbreviated == null) {
      
      Set<Group> groups = GrouperUserDataApi.recentlyUsedGroups(GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiGroupsRecentlyUsedAbbreviated = GuiGroup.convertFromGroups(groups, "uiV2.index.maxRecentlyUsedEachType", 5);
    }
    
    return this.guiGroupsRecentlyUsedAbbreviated;
  }

  /**
   * for index page, this is a short list of members the user has RecentlyUsed, lazy load if null
   * @return the list of members
   */
  public Set<GuiMember> getGuiMembersRecentlyUsedAbbreviated() {
    
    if (this.guiMembersRecentlyUsedAbbreviated == null) {
      
      Set<Member> members = GrouperUserDataApi.recentlyUsedMembers(GrouperUiUserData.grouperUiGroupNameForUserData(), GrouperSession.staticGrouperSession().getSubject());
      
      this.guiMembersRecentlyUsedAbbreviated = GuiMember.convertFromMembers(members, "uiV2.index.maxRecentlyUsedEachType", 5);
    }
    
    return this.guiMembersRecentlyUsedAbbreviated;
  }

  /**
   * for index page, this is a short list of stems the user has RecentlyUsed, lazy load if null
   * @return the list of stems
   */
  public Set<GuiStem> getGuiStemsRecentlyUsedAbbreviated() {
    
    if (this.guiStemsRecentlyUsedAbbreviated == null) {
      
      Set<Stem> stems = GrouperUserDataApi.recentlyUsedStems(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          GrouperSession.staticGrouperSession().getSubject());
      
      this.guiStemsRecentlyUsedAbbreviated = GuiStem.convertFromStems(stems, "uiV2.index.maxRecentlyUsedEachType", 5);
      
    }
    
    return this.guiStemsRecentlyUsedAbbreviated;
  }

  
  
  
}
