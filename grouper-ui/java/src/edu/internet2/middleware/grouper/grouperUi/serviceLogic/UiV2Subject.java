package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiSubject;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.SubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipSubjectContainer;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUserData;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * logic involving subjects
 * @author mchyzer
 *
 */
public class UiV2Subject {

  /**
   * modal search form results for add this subject to a group
   * @param request
   * @param response
   */
  public void addGroupSearch(HttpServletRequest request, HttpServletResponse response) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    GrouperSession grouperSession = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
  
      Subject subject = retrieveSubjectHelper(request);
  
      if (subject == null) {
        return;
      }
  
      SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();
  
      String searchString = request.getParameter("addMemberGroupSearch");
      
      boolean searchOk = GrouperUiUtils.searchStringValid(searchString);
      if (!searchOk) {
        
        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("subjectViewAddToGroupNotEnoughChars")));
        return;
      }

      String matchExactIdString = request.getParameter("matchExactId[]");
      boolean matchExactId = GrouperUtil.booleanValue(matchExactIdString, false);
      
      Set<Group> groups = null;
    
    
      GroupFinder groupFinder = new GroupFinder().assignPrivileges(AccessPrivilege.UPDATE_PRIVILEGES)
        .assignScope(searchString).assignSplitScope(true);
      
      if (matchExactId) {
        groupFinder.assignFindByUuidOrName(true);
      }
      
      groups = groupFinder.findGroups();
      
      
      if (GrouperUtil.length(groups) == 0) {

        guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#addGroupResults", 
            TextContainer.retrieveFromRequest().getText().get("subjectViewAddMemberNoSubjectsFound")));
        return;
      }
      
      Set<GuiGroup> guiGroups = GuiGroup.convertFromGroups(groups, "uiV2.subjectSearchResults", 30);
      
      subjectContainer.setGuiGroupsAddMember(guiGroups);
  
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#addGroupResults", 
          "/WEB-INF/grouperUi2/subject/addGroupResults.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * combobox results for add this subject to a group
   * @param request
   * @param response
   */
  public void addToGroupFilter(HttpServletRequest request, HttpServletResponse response) {
    new UiV2Group().groupUpdateFilter(request, response);
  }
  
  /**
   * get the subject from the request
   * @param request
   * @return the subject or null if not found
   */
  private static Subject retrieveSubjectHelper(HttpServletRequest request) {
    return retrieveSubjectHelper(request, true);
  }

  /**
   * get the subject from the request
   * @param request
   * @return the subject or null if not found
   */
  public static Subject retrieveSubjectHelper(HttpServletRequest request, boolean displayErrorIfProblem) {
  
    //initialize the bean
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    
    SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();

    Subject subject = null;
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    String sourceId = request.getParameter("sourceId");
    String subjectId = request.getParameter("subjectId");
    String subjectIdentifier = request.getParameter("subjectIdentifier");
    String subjectIdOrIdentifier = request.getParameter("subjectIdOrIdentifier");
    String memberId = request.getParameter("memberId");
    
    boolean addedError = false;
    
    if (StringUtils.isBlank(subjectId) && StringUtils.isBlank(subjectIdentifier)
        && StringUtils.isBlank(subjectIdOrIdentifier) && StringUtils.isBlank(memberId)) {
      if (!displayErrorIfProblem) {
        return null;
      }
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
          TextContainer.retrieveFromRequest().getText().get("subjectCantFindSubjectId")));
      addedError = true;
    }
    
    SubjectFinder subjectFinder = addedError ? null : new SubjectFinder().assignSourceId(sourceId)
        .assignSubjectId(subjectId).assignSubjectIdentifier(subjectIdentifier)
        .assignSubjectIdOrIdentifier(subjectIdOrIdentifier).assignMemberId(memberId);

    subject = subjectFinder.findSubject();
    
    if (subject != null) {
      subjectContainer.setGuiSubject(new GuiSubject(subject));      

    } else {
      
      if (!addedError) {
        if (!displayErrorIfProblem) {
          return null;
        }
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, 
            TextContainer.retrieveFromRequest().getText().get("subjectCantFindSubject")));
        addedError = true;
      }
      
    }
  
    //go back to the main screen, cant find group
    if (addedError) {
      if (!displayErrorIfProblem) {
        guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
            "/WEB-INF/grouperUi2/index/indexMain.jsp"));
      }
    }

    return subject;
  }

  /**
   * view subject
   * @param request
   * @param response
   */
  public void viewSubject(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;

    Subject subject = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      subject = retrieveSubjectHelper(request);

      if (subject == null) {
        return;
      }

      //if viewing a subject, and that subject is a group, just show the group screen
      if (GrouperSourceAdapter.groupSourceId().equals(subject.getSourceId())) {
        //hmmm, should we change to the group url?  i guess not... hmmm
        new UiV2Group().viewGroup(request, response);
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/subject/viewSubject.jsp"));

      filterHelper(request, response, subject);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }

  /**
   * the filter button was pressed, or paging or sorting, or view Subject or something
   * @param request
   * @param response
   * @param subject
   */
  private void filterHelper(HttpServletRequest request, HttpServletResponse response, Subject subject) {
    
    GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    String filterText = request.getParameter("filterText");
    SubjectContainer subjectContainer = grouperRequestContainer.getSubjectContainer();
    
    //if filtering by subjects that have a certain type
    String membershipTypeString = request.getParameter("membershipType");
    MembershipType membershipType = null;
    if (!StringUtils.isBlank(membershipTypeString)) {
      membershipType = MembershipType.valueOfIgnoreCase(membershipTypeString, true);
    }
  
    String pageSizeString = request.getParameter("pagingTagPageSize");
    int pageSize = -1;
    if (!StringUtils.isBlank(pageSizeString)) {
      pageSize = GrouperUtil.intValue(pageSizeString);
    } else {
      pageSize = GrouperUiConfig.retrieveConfig().propertyValueInt("pager.pagesize.default", 50);
    }
    subjectContainer.getGuiPaging().setPageSize(pageSize);
    
    //1 indexed
    String pageNumberString = request.getParameter("pagingTagPageNumber");
    
    int pageNumber = 1;
    if (!StringUtils.isBlank(pageNumberString)) {
      pageNumber = GrouperUtil.intValue(pageNumberString);
    }

    subjectContainer.getGuiPaging().setPageNumber(pageNumber);

    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(pageSize, pageNumber, true);

    MembershipFinder membershipFinder = new MembershipFinder()
      .addSubject(subject).assignCheckSecurity(true)
      .assignHasFieldForGroup(true)
      .assignEnabled(true)
      .assignHasMembershipTypeForGroup(true)
      .assignQueryOptionsForGroup(queryOptions)
      .assignSplitScopeForGroup(true);

    if (membershipType != null) {
      membershipFinder.assignMembershipType(membershipType);
    }
  
    if (!StringUtils.isBlank(filterText)) {
      membershipFinder.assignScopeForGroup(filterText);
    }
  
    //set of subjects, and what memberships each subject has
    Set<MembershipSubjectContainer> results = membershipFinder
        .findMembershipResult().getMembershipSubjectContainers();
  
    subjectContainer.setGuiMembershipSubjectContainers(GuiMembershipSubjectContainer.convertFromMembershipSubjectContainers(results));
    
    subjectContainer.getGuiPaging().setTotalRecordCount(queryOptions.getQueryPaging().getTotalRecordCount());
    
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectFilterResultsId", 
        "/WEB-INF/grouperUi2/subject/subjectContents.jsp"));
  
  }

  /**
   * 
   * @param request
   * @param response
   */
  public void addToMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteMemberAdd(GrouperUiUserData.grouperUiGroupNameForUserData(), 
          loggedInSubject, subject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSuccessAddedToMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/subject/subjectMoreActionsButtonContents.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  /**
   * ajax logic to remove from my favorites
   * @param request
   * @param response
   */
  public void removeFromMyFavorites(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Subject subject = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      subject = retrieveSubjectHelper(request);
      
      if (subject == null) {
        return;
      }
  
      GrouperUserDataApi.favoriteMemberRemove(GrouperUiUserData.grouperUiGroupNameForUserData(), loggedInSubject, subject);
  
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("subjectSuccessRemovedFromMyFavorites")));
  
      //redisplay so the button will change
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#subjectMoreActionsButtonContentsDivId", 
          "/WEB-INF/grouperUi2/subject/subjectMoreActionsButtonContents.jsp"));
  
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }

  
}
