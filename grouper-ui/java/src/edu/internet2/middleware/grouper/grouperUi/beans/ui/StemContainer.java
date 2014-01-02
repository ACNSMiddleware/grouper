package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiObjectBase;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Stem.StemSearchType;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * stem container in new ui
 * @author mchyzer
 *
 */
public class StemContainer {

  /**
   * if we have a stem id to use for e.g. create group
   */
  private String objectStemId;
  
  /**
   * if we have a stem id to use for e.g. create group
   * @return object stem id
   */
  public String getObjectStemId() {
    return this.objectStemId;
  }

  /**
   * if we have a stem id to use for e.g. create group
   * @param objectStemId1
   */
  public void setObjectStemId(String objectStemId1) {
    this.objectStemId = objectStemId1;
  }

  /**
   * instructions could be for creating groups or stems or whatever
   */
  private StemSearchType stemSearchType;
  
  /**
   * instructions could be for creating groups or stems or whatever
   * @return instructions
   */
  public StemSearchType getStemSearchType() {
    return this.stemSearchType;
  }

  /**
   * instructions could be for creating groups or stems or whatever
   * @param folderSearchResultsInstructions1
   */
  public void setStemSearchType(StemSearchType folderSearchResultsInstructions1) {
    this.stemSearchType = folderSearchResultsInstructions1;
  }

  /**
   * gui paging for privileges
   */
  private GuiPaging privilegeGuiPaging;
  
  /**
   * gui paging for privileges, lazy load if null
   * @return gui paging for privs
   */
  public GuiPaging getPrivilegeGuiPaging() {
    if (this.privilegeGuiPaging == null) {
      this.privilegeGuiPaging = new GuiPaging();
    }
    return this.privilegeGuiPaging;
  }

  /**
   * gui paging for privileges
   * @param privilegeGuiPaging1
   */
  public void setPrivilegeGuiPaging(GuiPaging privilegeGuiPaging1) {
    this.privilegeGuiPaging = privilegeGuiPaging1;
  }

  /**
   * keep track of the paging on the stem screen
   */
  private GuiPaging guiPaging = null;
  
  /**
   * keep track of the paging on the stem screen
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  /**
   * filter text for privilege subjects
   */
  private String privilegeFilterText = null;
  
  /**
   * filter text for privilege subjects
   * @return filter text
   */
  public String getPrivilegeFilterText() {
    return this.privilegeFilterText;
  }

  /**
   * filter text for privilege subjects
   * @param privilegeFilterText1
   */
  public void setPrivilegeFilterText(String privilegeFilterText1) {
    this.privilegeFilterText = privilegeFilterText1;
  }

  /**
   * filter text for the stem contents
   */
  private String filterText = null;

  /**
   * filter text
   * @return filter text
   */
  public String getFilterText() {
    return this.filterText;
  }

  /**
   * filter text
   * @param filterText1
   */
  public void setFilterText(String filterText1) {
    this.filterText = filterText1;
  }

  /**
   * if the logged in user can create groups, lazy loaded
   */
  private Boolean canCreateGroups;
  
  /**
   * if the logged in user can create groups, lazy loaded
   * @return if can admin create groups
   */
  public boolean isCanCreateGroups() {
    
    if (this.canCreateGroups == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canCreateGroups = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().hasCreate(loggedInSubject);
            }
          });
      
    }
    
    return this.canCreateGroups;
  }


  
  /**
   * if the logged in user can admin privileges, lazy loaded
   */
  private Boolean canAdminPrivileges;
  
  /**
   * if the logged in user can admin privileges, lazy loaded
   * @return if can admin privileges
   */
  public boolean isCanAdminPrivileges() {
    
    if (this.canAdminPrivileges == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canAdminPrivileges = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return StemContainer.this.getGuiStem().getStem().hasStem(loggedInSubject);
            }
          });
      
    }
    
    return this.canAdminPrivileges;
  }

  /**
   * subjects and what privs they have on this stem
   */
  private Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers;

  /**
   * if filtering privileges by field
   */
  private Field privilegeField;
  
  /**
   * if filtering privileges by field
   * @return field
   */
  public Field getPrivilegeField() {
    return this.privilegeField;
  }

  /**
   * if filtering privileges by field
   * @param privilegeField1
   */
  public void setPrivilegeField(Field privilegeField1) {
    this.privilegeField = privilegeField1;
  }

  /**
   * membership type for the privilege filter
   */
  private MembershipType privilegeMembershipType;

  /**
   * membership type for the privilege filter
   * @return membership type for the privilege filter
   */
  public MembershipType getPrivilegeMembershipType() {
    return this.privilegeMembershipType;
  }

  /**
   * membership type for the privilege filter
   * @param privilegeMembershipType1
   */
  public void setPrivilegeMembershipType(MembershipType privilegeMembershipType1) {
    this.privilegeMembershipType = privilegeMembershipType1;
  }

  /**
   * subjects and what privs they have on this stem
   * @return membership subject containers
   */
  public Set<GuiMembershipSubjectContainer> getPrivilegeGuiMembershipSubjectContainers() {
    if (this.privilegeGuiMembershipSubjectContainers == null) {

      Stem stem = this.getGuiStem().getStem();
      int pageSize = this.getPrivilegeGuiPaging().getPageSize();
      int pageNumber = this.getPrivilegeGuiPaging().getPageNumber();
      QueryOptions queryOptions = new QueryOptions();
      queryOptions.paging(pageSize, pageNumber, true);
      
      MembershipFinder membershipFinder = new MembershipFinder()
        .addStemId(stem.getId()).assignCheckSecurity(true)
        .assignHasFieldForMember(true)
        .assignHasMembershipTypeForMember(true)
        .assignEnabled(true)
        .assignQueryOptionsForMember(queryOptions)
        .assignSplitScopeForMember(true);
      
      if (this.privilegeMembershipType != null) {
        membershipFinder.assignMembershipType(this.privilegeMembershipType);
      }

      if (this.privilegeField != null) {
        membershipFinder.assignField(this.privilegeField);
      }

      if (!StringUtils.isBlank(this.privilegeFilterText)) {
        membershipFinder.assignScopeForMember(this.privilegeFilterText);
      }

      //set of subjects, and what privs each subject has
      Set<MembershipSubjectContainer> results = membershipFinder
          .findMembershipResult().getMembershipSubjectContainers();

      //inherit from grouperAll or Groupersystem or privilege inheritance
      MembershipSubjectContainer.considerNamingPrivilegeInheritance(results, stem);
      
      this.privilegeGuiMembershipSubjectContainers = GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results);
      this.getPrivilegeGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());

    }
    return this.privilegeGuiMembershipSubjectContainers;
  }

  /**
   * clear this out to requery
   * @param privilegeGuiMembershipSubjectContainers1
   */
  public void setPrivilegeGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> privilegeGuiMembershipSubjectContainers1) {
    this.privilegeGuiMembershipSubjectContainers = privilegeGuiMembershipSubjectContainers1;
  }

  /**
   * groups, stems, etc in this stem which are children, only in the current page
   */
  private Set<GuiObjectBase> childGuiObjectsAbbreviated;

  /**
   * groups, stems, etc in this stem which are children, only in the current page
   * @return gui groups, stems, etc
   */
  public Set<GuiObjectBase> getChildGuiObjectsAbbreviated() {
    if (this.childGuiObjectsAbbreviated == null) {

      Stem stem = this.getGuiStem().getStem();
      int pageSize = this.getGuiPaging().getPageSize();
      int pageNumber = this.getGuiPaging().getPageNumber();
      QueryOptions queryOptions = QueryOptions.create("displayExtension", true, pageNumber, pageSize);
      
      GrouperObjectFinder grouperObjectFinder = new GrouperObjectFinder()
        .assignObjectPrivilege(ObjectPrivilege.view)
        .assignParentStemId(stem.getId())
        .assignQueryOptions(queryOptions)
        .assignSplitScope(true).assignStemScope(Scope.ONE)
        .assignSubject(GrouperSession.staticGrouperSession().getSubject());
 
      if (!StringUtils.isBlank(this.filterText)) {
        grouperObjectFinder.assignFilterText(this.filterText);
      }

      Set<GrouperObject> results = grouperObjectFinder.findGrouperObjects();
      
      this.childGuiObjectsAbbreviated = GuiObjectBase.convertFromGrouperObjects(results);
      
      this.getGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
      
      
    }
    return this.childGuiObjectsAbbreviated;
  }

  /**
   * gui stem shown on screen
   */
  private GuiStem guiStem;

  /**
   * if the stem is a favorite for the logged in user
   */
  private Boolean favorite;

  /**
   * filter text for the parent stem search
   */
  private String parentStemFilterText = null;

  /**
   * keep track of the paging on the parent stem search screen
   */
  private GuiPaging parentStemGuiPaging = null;

  /**
   * when searching for parent stems, these are the results
   */
  private Set<GuiStem> parentStemSearchResults = null;

  /**
   * when searching for parent stems, these are the results
   * @return stems
   */
  public Set<GuiStem> getParentStemSearchResults() {
    return this.parentStemSearchResults;
  }

  /**
   * when searching for parent stems, these are the results
   * @param parentStemSearchResults1
   */
  public void setParentStemSearchResults(Set<GuiStem> parentStemSearchResults1) {
    this.parentStemSearchResults = parentStemSearchResults1;
  }

  /**
   * when filtering parent stem list
   * @return
   */
  public String getParentStemFilterText() {
    return this.parentStemFilterText;
  }

  /**
   * when filtering parent stem list
   * @param parentStemFilterText1
   */
  public void setParentStemFilterText(String parentStemFilterText1) {
    this.parentStemFilterText = parentStemFilterText1;
  }

  /**
   * when paging parent stem list
   * @return parent stem gui paging
   */
  public GuiPaging getParentStemGuiPaging() {
    if (this.parentStemGuiPaging == null) {
      this.parentStemGuiPaging = new GuiPaging();
    }
    return this.parentStemGuiPaging;
  }

  /**
   * when paging parent stem list
   * @param parentStemGuiPaging1
   */
  public void setParentStemGuiPaging(GuiPaging parentStemGuiPaging1) {
    this.parentStemGuiPaging = parentStemGuiPaging1;
  }

  /**
   * gui stem shown on screen
   * @return stem
   */
  public GuiStem getGuiStem() {
    return this.guiStem;
  }

  /**
   * gui stem shown on screen
   * @param guiStem1
   */
  public void setGuiStem(GuiStem guiStem1) {
    this.guiStem = guiStem1;
  }

  /**
   * if the stem is a favorite for the logged in user
   * @return if favorite
   */
  public boolean isFavorite() {
    
    if (this.favorite == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
      this.favorite = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              
              Set<Stem> favorites = GrouperUtil.nonNull(
                  GrouperUserDataApi.favoriteStems(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject));
              return favorites.contains(StemContainer.this.getGuiStem().getStem());
                  
            }
          });
    }
    
    return this.favorite;
  }

  
  
}
