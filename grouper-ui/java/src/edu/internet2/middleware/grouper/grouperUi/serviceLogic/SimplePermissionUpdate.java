/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdate.java,v 1.7 2009-11-13 14:56:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntryActionsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionRoleDelegate;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * main ajax methods for simple attribute update module
 */
public class SimplePermissionUpdate {
  
  /**
   * index page of application
   * @param request
   * @param response
   */
  public void assignInit(HttpServletRequest request, HttpServletResponse response) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#topDiv", 
      "/WEB-INF/grouperUi/templates/common/commonTop.jsp"));

    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#bodyDiv", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAssignInit.jsp"));
  
  }

  /**
   * assign a permission
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignPermission(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
      
      if (permissionType == null) {
        return;
      }
      
      //get the permission name to assign
      String permissionAssignAttributeName = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAttributeName"));

      AttributeDefName attributeDefName = null;
      if (!StringUtils.isBlank(permissionAssignAttributeName)) {
        attributeDefName = AttributeDefNameFinder.findById(permissionAssignAttributeName, false);
      }
  
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickPermissionResource", false)));
        return;
      }

      String permissionAssignRoleId = null;
      String attributeAssignMemberId = null;
  
      attributeAssignMemberId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignMemberId"));

      Member member = null;
      
      //member is not required, could assign a permission to a role
      if (!StringUtils.isBlank(attributeAssignMemberId)) {
        Subject subject = GrouperUiUtils.findSubject(attributeAssignMemberId, false);
        member = subject == null ? null : MemberFinder.findBySubject(grouperSession, subject, true);
        if (member == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickSubject", false)));
          return;
        }            
      }
      
      //why would subject be submitted?  something is wrong
      if (permissionType == PermissionType.role && member != null) {
        throw new RuntimeException("Why is member here if a role assignment????");
      }
      
      //member is required if assigning to a member
      if (permissionType == PermissionType.role_subject && member == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickSubjectForRoleSubject", false)));
        return;
      }
      
      permissionAssignRoleId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignRoleId"));
      Role role = null;
      if (!StringUtils.isBlank(permissionAssignRoleId)) {
        
        role = GroupFinder.findByUuid(grouperSession, permissionAssignRoleId, false);
      }
      
      //role is required
      if (role == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickRole", false)));
        return;
      }            
      
      String action = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAction"));
      
      Set<String> allowedActions = attributeDefName.getAttributeDef().getAttributeDefActionDelegate().allowedActionStrings();
      
      //if there is one action, thats ok, just use that one if not specified
      if (StringUtils.isBlank(action) && allowedActions.size() == 1) {
        action = allowedActions.iterator().next();
      }
        
      if (StringUtils.isBlank(action) || !allowedActions.contains(action)) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignErrorPickAction", false)));
        return;
      }
      
      AttributeAssignResult attributeAssignResult = null;
      
      //see if assigning to role
      if (permissionType == PermissionType.role) {
        
        PermissionRoleDelegate permissionRoleDelegate = role.getPermissionRoleDelegate();

        attributeAssignResult = permissionRoleDelegate.assignRolePermission(action, attributeDefName);
        
      } else if (permissionType == PermissionType.role_subject) {

        Set<Membership> memberships = GrouperUtil.nonNull(((Group)role).getMemberships(Group.getDefaultList(), GrouperUtil.toSet(member)));
        
        //we just need one
        if (GrouperUtil.length(memberships) == 0) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorMembershipRequired", false)));
          return;
        }
        
        PermissionRoleDelegate permissionRoleDelegate = role.getPermissionRoleDelegate();
        
        attributeAssignResult = permissionRoleDelegate.assignSubjectRolePermission(action, attributeDefName, member);
        
      } else {
        throw new RuntimeException("Permission type not expected: " + permissionType);
      }
      
      if (attributeAssignResult.isChanged()) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.assignSuccess", false)));
        
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simplePermissionUpdate.errorAssignedAlready", false)));
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    assignFilter(httpServletRequest, httpServletResponse);

  
  }

  /**
   * the owner type drop down was changed
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignSelectOwnerType(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
    
    if (retrievePermissionType(httpServletRequest, guiResponseJs,
        permissionUpdateRequestContainer) == null) {
      return;
    }

    //put in the generic panel that filters on attribute definitions
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#permissionAssignFilter", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAssignFilter.jsp"));
  }

  /**
   * get the permission type out of the request
   * @param httpServletRequest
   * @param guiResponseJs
   * @param permissionUpdateRequestContainer
   * @return permissiontype if ok, null if not
   */
  private PermissionType retrievePermissionType(HttpServletRequest httpServletRequest,
      GuiResponseJs guiResponseJs,
      PermissionUpdateRequestContainer permissionUpdateRequestContainer) {

    String permissionAssignTypeString = httpServletRequest.getParameter("permissionAssignType");
    
    //clear out the assignments panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#permissionAssignAssignments", ""));
    
    if (StringUtils.isBlank(permissionAssignTypeString)) {
      guiResponseJs.addAction(GuiScreenAction.newAlert(TagUtils.navResourceString("simplePermissionAssign.requiredOwnerType")));

      //clear out the filter panels for generic and specific
      guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#permissionAssignFilter", ""));
      
      return null;
    }
    
    PermissionType permissionAssignType = PermissionType.valueOfIgnoreCase(permissionAssignTypeString, true);
    
    permissionUpdateRequestContainer.setPermissionType(permissionAssignType);
    return permissionAssignType;
  }
  
  /**
   * filter permission assignments and display the results
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
  
      if (permissionType == null) {
        return;
      }
      
      //get the permission name to assign
      String permissionAssignAttributeName = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAttributeName"));
      
      //attribute def name is not required
      AttributeDefName attributeDefName = null;
      if (!StringUtils.isBlank(permissionAssignAttributeName)) {

        attributeDefName = AttributeDefNameFinder.findById(permissionAssignAttributeName, false);
        
        //if cant find, but submitted
        if (attributeDefName == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickPermissionResource", false)));
        }
        
      }
  
      //get the subject id
      String attributeAssignMemberId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignMemberId"));

      Member member = null;
      
      //member is not required, could assign a permission to a role
      if (!StringUtils.isBlank(attributeAssignMemberId)) {
        Subject subject = GrouperUiUtils.findSubject(attributeAssignMemberId, false);
        member = subject == null ? null : MemberFinder.findBySubject(grouperSession, subject, true);
        
        //this is required if a value was submitted
        if (member == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickSubject", false)));
          return;
        }            
      }


      String permissionAssignAttributeDefId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAttributeDef"));
      
      AttributeDef attributeDef = null;
      
      if (!StringUtils.isBlank(permissionAssignAttributeDefId)) {
        attributeDef = AttributeDefFinder.findById(permissionAssignAttributeDefId, false);
        if (attributeDef == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickPermissionDefinition", false)));
          return;
        }
      }

      String permissionAssignRoleId = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignRoleId"));

      Role role = null;
      if (!StringUtils.isBlank(permissionAssignRoleId)) {
        
        role = GroupFinder.findByUuid(grouperSession, permissionAssignRoleId, false);

        //role is required if a string was submitted
        if (role == null) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
              "simplePermissionUpdate.assignErrorPickRole", false)));
          return;
        }            

      }
      
      String action = StringUtils.trimToNull(httpServletRequest.getParameter("permissionAssignAction"));
      
      //enabled / disabled
      String enabledDisabledString = httpServletRequest.getParameter("enabledDisabled");
      Boolean enabledDisabledBoolean = true;
      if (!StringUtils.isBlank(enabledDisabledString)) {
        
        if (StringUtils.equals(enabledDisabledString, "enabledOnly")) {
          enabledDisabledBoolean = true;
        } else if (StringUtils.equals(enabledDisabledString, "disabledOnly")) {
          enabledDisabledBoolean = false;
        } else if (StringUtils.equals(enabledDisabledString, "all")) {
          enabledDisabledBoolean = null;
        } else {
          throw new RuntimeException("Not expecting enabledDisabled: " + enabledDisabledString);
        }
        permissionUpdateRequestContainer.setEnabledDisabled(enabledDisabledBoolean);
      }
      
      Set<PermissionEntry> permissionEntriesFromDb = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
          permissionAssignAttributeDefId,  permissionAssignAttributeName, permissionAssignRoleId, 
          member == null ? null : member.getUuid(), action, enabledDisabledBoolean);
      
      List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers = new ArrayList<GuiPermissionEntryActionsContainer>();
      
      //lets link the set of actions (alphabetized), to a GuiPermissionEntryActionsContainer
      Map<MultiKey, GuiPermissionEntryActionsContainer> actionsToPermissionsEntryActionsContainer 
        = new HashMap<MultiKey, GuiPermissionEntryActionsContainer>();
      
      //lets link the attribute def id to a GuiPermissionEntryActionsContainer
      Map<String, GuiPermissionEntryActionsContainer> attributeDefIdToPermissionsEntryActionsContainer 
        = new HashMap<String, GuiPermissionEntryActionsContainer>();

      Set<String> allActionsSet = new TreeSet<String>();
      
      if (!StringUtils.isBlank(action)) {
        allActionsSet.add(action);
      }
      
      //process the permissions to group up the GuiPermissionEntryActionsContainers
      for (PermissionEntry permissionEntry : permissionEntriesFromDb) {
        String attributeDefId = permissionEntry.getAttributeDefId();
        
        //see if we are all set
        GuiPermissionEntryActionsContainer guiPermissionEntryActionsContainer 
          = attributeDefIdToPermissionsEntryActionsContainer.get(attributeDefId);
        
        if (guiPermissionEntryActionsContainer == null) {
          //if we havent done this id yet
          //see if we have the actions taken care of
          AttributeDef currentAttributeDef = permissionEntry.getAttributeDef();
          
          List<String> actions = null;
          if (StringUtils.isBlank(action)) {
            actions = new ArrayList<String>(currentAttributeDef.getAttributeDefActionDelegate().allowedActionStrings());
  
            Collections.sort(actions);
            
            allActionsSet.addAll(actions);
          } else {
            actions = GrouperUtil.toList(action);
          }
          Object[] actionsArray = actions.toArray();
          
          MultiKey actionsKey = new MultiKey(actionsArray);
          
          //lets see if these actions are there
          guiPermissionEntryActionsContainer = actionsToPermissionsEntryActionsContainer.get(actionsKey);
          
          if (guiPermissionEntryActionsContainer == null) {
            
            guiPermissionEntryActionsContainer = new GuiPermissionEntryActionsContainer();
            guiPermissionEntryActionsContainer.setPermissionType(permissionType);
            guiPermissionEntryActionsContainer.setRawPermissionEntries(new ArrayList<PermissionEntry>());
            
            guiPermissionEntryActionsContainer.setActions(actions);
            
            guiPermissionEntryActionsContainers.add(guiPermissionEntryActionsContainer);
            
            actionsToPermissionsEntryActionsContainer.put(actionsKey, guiPermissionEntryActionsContainer);
            
          }
          attributeDefIdToPermissionsEntryActionsContainer.put(attributeDefId, guiPermissionEntryActionsContainer);
        }
        guiPermissionEntryActionsContainer.getRawPermissionEntries().add(permissionEntry);
      }
      
      List<String> allActionsList = new ArrayList<String>(allActionsSet);
      permissionUpdateRequestContainer.setAllActions(allActionsList);
      
      for (GuiPermissionEntryActionsContainer guiPermissionEntryActionsContainer : guiPermissionEntryActionsContainers) {
        
        guiPermissionEntryActionsContainer.processRawEntries();
        
      }
      
      permissionUpdateRequestContainer.setGuiPermissionEntryActionsContainers(guiPermissionEntryActionsContainers);
      
      //set the permissions panel
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#permissionAssignAssignments", 
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAssignments.jsp"));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#permissionAssignAssignments');"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  

  /**
   * privilege image button was pressed on the privilege edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void permissionPanelImageClick(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String guiPermissionId = httpServletRequest.getParameter("guiPermissionId");
      
      if (StringUtils.isBlank(guiPermissionId)) {
        throw new RuntimeException("Why is guiPermissionId blank????");
      }

      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
      if (permissionType == null) {
        //this should be an error...
        return;
      }
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)$");
      Matcher matcher = pattern.matcher(guiPermissionId);
      if (!matcher.matches()) {
        throw new RuntimeException("Why does guiPermissionId not match? " + guiPermissionId);
      }

      //get current state
      Role role = null;
      {
        String roleId = matcher.group(1);
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasAdmin(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
          return;
        }
      }
      Member member = null;
      { 
        if (permissionType == PermissionType.role_subject) {
          String memberId = matcher.group(2);
          member = MemberFinder.findByUuid(grouperSession, memberId, true);
        }
      }
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = matcher.group(3);
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!attributeDef.getPrivilegeDelegate().canAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
          return;
        }
        
      }
      
      String action = matcher.group(4);
      
      String allowString = httpServletRequest.getParameter("allow");

      if (StringUtils.isBlank(allowString)) {
        throw new RuntimeException("Why is allow blank????");
      }
      boolean allow = GrouperUtil.booleanValue(allowString);
      
      StringBuilder alert = new StringBuilder();
      
      GuiMember guiMember = new GuiMember(member);
      String subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();

      if (allow) {
        
        if (permissionType == PermissionType.role) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(action, attributeDefName);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionAllowRole = Success: Role: {0} can now perform action: {1} on permission resource: {2}
            permissionUpdateRequestContainer.setAssignmentStatusMessage(
                GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRole", false, true, 
                    new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()}));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
        } else if (permissionType == PermissionType.role_subject) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignSubjectRolePermission(action, attributeDefName, member);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
            permissionUpdateRequestContainer.setAssignmentStatusMessage(
                GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRoleSubject", false, true, 
                    new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()}));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
          
        } else {
          throw new RuntimeException("Not expecting permission type: " + permissionType);
        }
      } else {

        if (permissionType == PermissionType.role) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeRolePermission(action, attributeDefName);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionRevokeRole = Success: Role: {0} can no longer perform action: {1} on permission resource: {2}
            permissionUpdateRequestContainer.setAssignmentStatusMessage(
                GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRole", false, true, 
                    new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()}));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
          
        } else if (permissionType == PermissionType.role_subject) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeSubjectRolePermission(action, attributeDefName, member);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
            permissionUpdateRequestContainer.setAssignmentStatusMessage(
                GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRoleSubject", false, true, 
                    new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()}));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
          
        } else {
          throw new RuntimeException("Not expecting permission type: " + permissionType);
        }

      }
      
      guiResponseJs.addAction(GuiScreenAction.newAlert(alert.toString()));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
    assignFilter(httpServletRequest, httpServletResponse);
    
  }

  /**
   * cancel permission button was pressed on the permission edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void permissionCancel(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

    //set the privilege panel
    guiResponseJs.addAction(GuiScreenAction.newInnerHtml("#permissionAssignAssignments", ""));
    
  }
      
  /**
   * submit permissions button was pressed on the permission edit panel
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void permissionPanelSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
      if (permissionType == null) {
        //this should be an error...
        return;
      }
      
      StringBuilder alert = new StringBuilder();

      //lets see what to do
      //<input  name="previousState__${guiPermissionId}"
      //  type="hidden" value="${guiPermissionEntryChecked ? 'true' : 'false'}" />
      //<input  style="margin-right: -3px" name="permissionCheckbox__${guiPermissionId}" value="true"
      //  type="checkbox" ${guiPermissionEntryChecked ? 'checked="checked"' : '' } 
      
      Pattern pattern = Pattern.compile("^previousState__(.*)__(.*)__(.*)__(.*)$");
      Enumeration<?> enumeration = httpServletRequest.getParameterNames();

      //process all params submitted
      while (enumeration != null && enumeration.hasMoreElements()) {
        String paramName = (String)enumeration.nextElement();
        Matcher matcher = pattern.matcher(paramName);
        if (matcher.matches()) {
          
          //lets get the previous state
          boolean previousChecked = GrouperUtil.booleanValue(httpServletRequest.getParameter(paramName));
          
          //get current state
          String roleId = matcher.group(1);
          String memberId = matcher.group(2);
          String attributeDefNameId = matcher.group(3);
          String action = matcher.group(4);

          String currentStateString = httpServletRequest.getParameter("permissionCheckbox__" + roleId + "__" + memberId + "__" + attributeDefNameId + "__" + action);
          boolean currentChecked = GrouperUtil.booleanValue(currentStateString, false);
          
          //if they dont match, do something about it
          if (previousChecked != currentChecked) {

            if (alert.length() > 0) {
              alert.append("<br />");
            }

            //get current state
            Role role = null;
            {
              role = GroupFinder.findByUuid(grouperSession, roleId, true);
              if (!((Group)role).hasAdmin(loggedInSubject)) {
                guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
                return;
              }
            }
            Member member = null;
            { 
              if (permissionType == PermissionType.role_subject) {
                member = MemberFinder.findByUuid(grouperSession, memberId, true);
              }
            }
            AttributeDefName attributeDefName = null;
            {
              attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
              attributeDef = attributeDefName.getAttributeDef();
              if (!attributeDef.getPrivilegeDelegate().canAttrUpdate(loggedInSubject)) {
                guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
                return;
              }
              
            }
            
            GuiMember guiMember = new GuiMember(member);
            String subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();

            if (currentChecked) {
              
              if (permissionType == PermissionType.role) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(action, attributeDefName);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionAllowRole = Success: Role: {0} can now perform action: {1} on permission resource: {2}
                  alert.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRole", false, true, 
                          new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
              } else if (permissionType == PermissionType.role_subject) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignSubjectRolePermission(action, attributeDefName, member);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
                  alert.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRoleSubject", false, true, 
                          new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
                
              } else {
                throw new RuntimeException("Not expecting permission type: " + permissionType);
              }
            } else {

              if (permissionType == PermissionType.role) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeRolePermission(action, attributeDefName);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionRevokeRole = Success: Role: {0} can no longer perform action: {1} on permission resource: {2}
                  alert.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRole", false, true, 
                          new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
                
              } else if (permissionType == PermissionType.role_subject) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeSubjectRolePermission(action, attributeDefName, member);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
                  alert.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRoleSubject", false, true, 
                          new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
                
              } else {
                throw new RuntimeException("Not expecting permission type: " + permissionType);
              }

            }

            
          }
        }
      }

      
      if (alert.length() > 0) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(alert.toString()));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.noPermissionChangesDetected", false)));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    } 
    
    assignFilter(httpServletRequest, httpServletResponse);
    
  }



  /**
   * submit the assign edit screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignEditSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      PermissionType permissionType = retrievePermissionType(httpServletRequest, guiResponseJs, permissionUpdateRequestContainer);
      
      if (permissionType == null) {
        return;
      }
      
      String guiPermissionId = httpServletRequest.getParameter("guiPermissionId");
      
      if (StringUtils.isBlank(guiPermissionId)) {
        throw new RuntimeException("Why is guiPermissionId blank???");
      }
  
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)$");
      Matcher matcher = pattern.matcher(guiPermissionId);
      if (!matcher.matches()) {
        throw new RuntimeException("Why does guiPermissionId not match? " + guiPermissionId);
      }

      //get current state
      Role role = null;
      {
        String roleId = matcher.group(1);
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasAdmin(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantManageRole", false)));
          return;
        }
      }
      
      Member member = null;
      { 
        if (permissionType == PermissionType.role_subject) {
          String memberId = matcher.group(2);
          member = MemberFinder.findByUuid(grouperSession, memberId, true);
        }
      }
      AttributeDef attributeDef = null;
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = matcher.group(3);
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!attributeDef.getPrivilegeDelegate().canAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.errorCantEditAttributeDef", false)));
          return;
        }
        
      }
      
      String action = matcher.group(4);

      //get all the assignments
      Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory()
        .getPermissionEntry().findPermissions(null, attributeDefName.getId(), role.getId(), member.getUuid(), action, null);

      PermissionEntry permissionEntry = null;
      for (PermissionEntry current : permissionEntries) {
       
        //find the immediate one
        if (current.isImmediatePermission() && current.isImmediateMembership()) {
         
          if (permissionType == PermissionType.role || current.getPermissionType() == PermissionType.role_subject) {
            
            permissionEntry = current;
            break;
            
          }
        }
      }
      
      if (permissionEntry == null) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.noImmediatePermissionFound", false)));
        return;
      }
      AttributeAssign attributeAssign = permissionEntry.getAttributeAssign();
      {
        String enabledDate = httpServletRequest.getParameter("enabledDate");
        
        if (StringUtils.isBlank(enabledDate) ) {
          attributeAssign.setEnabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp enabledTimestamp = GrouperUtil.toTimestamp(enabledDate);
          attributeAssign.setEnabledTime(enabledTimestamp);
        }
      }
      
      {
        String disabledDate = httpServletRequest.getParameter("disabledDate");
  
        if (StringUtils.isBlank(disabledDate) ) {
          attributeAssign.setDisabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp disabledTimestamp = GrouperUtil.toTimestamp(disabledDate);
          attributeAssign.setDisabledTime(disabledTimestamp);
        }
      }
      
      attributeAssign.saveOrUpdate();
      
      //close the modal dialog
      guiResponseJs.addAction(GuiScreenAction.newCloseModal());
  
      String successMessage = TagUtils.navResourceString("simplePermissionUpdate.assignEditSuccess");
      successMessage = GrouperUiUtils.escapeHtml(successMessage, true);
      guiResponseJs.addAction(GuiScreenAction.newAlert(successMessage));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
    assignFilter(httpServletRequest, httpServletResponse);
    
  }



  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(SimplePermissionUpdate.class);
}
