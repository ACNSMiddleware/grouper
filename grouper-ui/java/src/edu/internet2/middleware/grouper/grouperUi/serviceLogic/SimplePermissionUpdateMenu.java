package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionAnalyze;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntry;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.exceptions.NoSessionException;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenu;
import edu.internet2.middleware.grouper.ui.tags.menu.DhtmlxMenuItem;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.HttpContentType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * simple permission update menu
 * @author mchyzer
 */
public class SimplePermissionUpdateMenu {

  /**
   * handle a click or select from the assignment menu
   * @param httpServletRequest
   * @param httpServletResponse
   */
  @SuppressWarnings("unused")
  public void assignmentMenu(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
      
    String menuItemId = httpServletRequest.getParameter("menuItemId");
    String menuHtmlId = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.equals(menuItemId, "editAssignment")) {
      this.assignmentMenuEditAssignment();
    } else if (StringUtils.equals(menuItemId, "analyzeAssignment")) {
      this.assignmentMenuAnalyzeAssignment();
    } else {
      throw new RuntimeException("Unexpected menu id: '" + menuItemId + "'");
    }
  
    
  }

  /**
   * add an assignment on an assignment
   */
  public void assignmentMenuAddMetadataAssignment() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("assignmentMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String attributeAssignId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "assignmentMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    AttributeAssign attributeAssign = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      
      if (attributeAssign.getAttributeAssignType().isAssignmentOnAssignment()) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message(
            "simpleAttributeUpdate.assignCantAddMetadataOnAssignmentOfAssignment", false)));
        return;
        
      }
      
      AttributeUpdateRequestContainer attributeUpdateRequestContainer = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      attributeUpdateRequestContainer.setAttributeAssignType(attributeAssign.getAttributeAssignType());
      
      GuiAttributeAssign guiAttributeAssign = new GuiAttributeAssign();
      guiAttributeAssign.setAttributeAssign(attributeAssign);
      
      attributeUpdateRequestContainer.setGuiAttributeAssign(guiAttributeAssign);
      
      //the combo boxes cant be shows on a dialog, so just replace the search results with this
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#attributeAssignAssignments",
        "/WEB-INF/grouperUi/templates/simpleAttributeUpdate/simpleAttributeAssignAddMetadataAssignment.jsp"));
  
      guiResponseJs.addAction(GuiScreenAction.newScript("guiScrollTo('#attributeAssignAssignments');"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error addMetadataAssignment menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * edit the enabled disabled
   */
  public void assignmentMenuEditAssignment() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("permissionMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String guiPermissionId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "permissionMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)__(.*)$");
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
      
      String permissionTypeString = matcher.group(5);
      PermissionType permissionType = PermissionType.valueOfIgnoreCase(permissionTypeString, true);
      permissionUpdateRequestContainer.setPermissionType(permissionType);
      
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

      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);
      
      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);
            
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionEdit.jsp"));

  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error editAssignment menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * analyze assignment
   */
  public void assignmentMenuAnalyzeAssignment() {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    //lets see which subject we are dealing with:
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
  
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("permissionMenuButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    String guiPermissionId = GrouperUtil.prefixOrSuffix(menuIdOfMenuTarget, "permissionMenuButton_", false);
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
      
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)__(.*)$");
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
      
      String permissionTypeString = matcher.group(5);
      PermissionType permissionType = PermissionType.valueOfIgnoreCase(permissionTypeString, true);
      permissionUpdateRequestContainer.setPermissionType(permissionType);
      
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

      if (GrouperUtil.length(permissionEntries) == 0) {
        guiResponseJs.addAction(GuiScreenAction.newAlert(GrouperUiUtils.message("simplePermissionUpdate.analyzeNoPermissionFound", false)));
        return;
      }
      
      PermissionEntry permissionEntry = null;
      
      List<PermissionEntry> permissionEntriesList = new ArrayList<PermissionEntry>();
      
      Iterator<PermissionEntry> iterator = permissionEntries.iterator();
      while (iterator.hasNext()) {
       
        PermissionEntry current = iterator.next();
        
        //find the immediate one
        if (current.isImmediatePermission() && current.isImmediateMembership()) {
         
          if (permissionType == PermissionType.role || current.getPermissionType() == PermissionType.role_subject) {
            
            permissionEntry = current;
            iterator.remove();
            //move this to the front of the list
            permissionEntriesList.add(permissionEntry);
            break;
            
          }
        }
      }
      
      //add the rest
      permissionEntriesList.addAll(permissionEntries);
      
      GuiPermissionEntry guiPermissionEntry = new GuiPermissionEntry();
     
      if (permissionEntry == null) {
        permissionEntry = permissionEntries.iterator().next();
      }
      
      guiPermissionEntry.setPermissionEntry(permissionEntry);
      guiPermissionEntry.setPermissionType(permissionType);

      guiPermissionEntry.setRawPermissionEntries(permissionEntriesList);
      guiPermissionEntry.processRawEntries();

      permissionUpdateRequestContainer.setGuiPermissionEntry(guiPermissionEntry);
            
      GuiPermissionAnalyze guiPermissionAnalyze = new GuiPermissionAnalyze();
      permissionUpdateRequestContainer.setGuiPermissionAnalyze(guiPermissionAnalyze);
      
      guiPermissionAnalyze.analyze(permissionEntriesList);
      
      guiResponseJs.addAction(GuiScreenAction.newDialogFromJsp(
        "/WEB-INF/grouperUi/templates/simplePermissionUpdate/simplePermissionAnalyze.jsp"));
  
    } catch (ControllerDone cd) {
      throw cd;
    } catch (NoSessionException nse) {
      throw nse;
    } catch (RuntimeException re) {
      throw new RuntimeException("Error editAssignment menu item: " + menuIdOfMenuTarget 
          + ", " + re.getMessage(), re);
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }

  /**
   * make the structure of the attribute assignment
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void assignmentMenuStructure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    DhtmlxMenu dhtmlxMenu = new DhtmlxMenu();
    
    {
      DhtmlxMenuItem analyzeAssignmentMenuItem = new DhtmlxMenuItem();
      analyzeAssignmentMenuItem.setId("analyzeAssignment");
      analyzeAssignmentMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.assignMenuAnalyzeAssignment"));
      analyzeAssignmentMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.assignMenuAnalyzeAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(analyzeAssignmentMenuItem);
    }    
  
    {
      DhtmlxMenuItem addValueMenuItem = new DhtmlxMenuItem();
      addValueMenuItem.setId("editAssignment");
      addValueMenuItem.setText(TagUtils.navResourceString("simplePermissionAssign.editAssignment"));
      addValueMenuItem.setTooltip(TagUtils.navResourceString("simplePermissionAssign.editAssignmentTooltip"));
      dhtmlxMenu.addDhtmlxItem(addValueMenuItem);
    }    
  
    GrouperUiUtils.printToScreen("<?xml version=\"1.0\"?>\n" + 
        dhtmlxMenu.toXml(), HttpContentType.TEXT_XML, false, false);
  
    throw new ControllerDone();
  }
  
  
  
}
