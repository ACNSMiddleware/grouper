/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-11-02 08:50:40 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntryActionsContainer;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;



/**
 * bean for simple attribute update.  holds all state for this module
 */
@SuppressWarnings("serial")
public class PermissionUpdateRequestContainer implements Serializable {

  /** type of permission, role or role_subject */
  private PermissionType permissionType;

  /**
   * type of permission, role or role_subject
   * @return type of permission
   */
  public PermissionType getPermissionType() {
    return this.permissionType;
  }

  /**
   * type of permission, role or role_subject
   * @param permissionType1
   */
  public void setPermissionType(PermissionType permissionType1) {
    this.permissionType = permissionType1;
  }

  /**
   * attribute assign value we are editing 
   */
  private AttributeAssignValue attributeAssignValue = null;
  
  /**
   * 
   * @return value
   */
  public AttributeAssignValue getAttributeAssignValue() {
    return this.attributeAssignValue;
  }

  /**
   * 
   * @param attributeAssignValue1
   */
  public void setAttributeAssignValue(AttributeAssignValue attributeAssignValue1) {
    this.attributeAssignValue = attributeAssignValue1;
  }

  /** gui attribute assign */
  private GuiAttributeAssign guiAttributeAssign = null;
  
  /** gui attribute assign on assignment */
  private GuiAttributeAssign guiAttributeAssignAssign = null;
  
  
  
  /**
   * gui attribute assign on assignment
   * @return gui attribute assign on assignment
   */
  public GuiAttributeAssign getGuiAttributeAssignAssign() {
    return this.guiAttributeAssignAssign;
  }

  /**
   * gui attribute assign on assignment
   * @param guiAttributeAssignAssign1
   */
  public void setGuiAttributeAssignAssign(GuiAttributeAssign guiAttributeAssignAssign1) {
    this.guiAttributeAssignAssign = guiAttributeAssignAssign1;
  }

  /**
   * enabledOnly, disabledOnly, or all (null)
   */
  private Boolean enabledDisabled = Boolean.TRUE;

  /** 
   * return enabledOnly, disabledOnly, or all (null)
   * @return enabledOnly, disabledOnly, or all (null)
   */
  public Boolean getEnabledDisabled() {
    return this.enabledDisabled;
  }

  /**
   * enabledOnly, disabledOnly, or all (null)
   * @param theEnabledDisabled
   */
  public void setEnabledDisabled(Boolean theEnabledDisabled) {
    this.enabledDisabled = theEnabledDisabled;
  }

  
  /**
   * list of sets of rows which have common actions
   */
  private List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers;
  
  
  
  /**
   * list of sets of rows which have common actions
   * @return list of sets of rows which have common actions
   */
  public List<GuiPermissionEntryActionsContainer> getGuiPermissionEntryActionsContainers() {
    return guiPermissionEntryActionsContainers;
  }

  /**
   * list of sets of rows which have common actions
   * @param guiPermissionEntryActionsContainers1
   */
  public void setGuiPermissionEntryActionsContainers(
      List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers1) {
    
    this.guiPermissionEntryActionsContainers = guiPermissionEntryActionsContainers1;
    
  }

  /** if this is a create as opposed to update */
  private boolean create;
  
  /**
   * if this is a create as opposed to update
   * @return if create
   */
  public boolean isCreate() {
    return this.create;
  }

  /**
   * if this is a create as opposed to update
   * @param create1
   */
  public void setCreate(boolean create1) {
    this.create = create1;
  }

  /**
   * store to session scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("permissionUpdateRequestContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static PermissionUpdateRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = 
      (PermissionUpdateRequestContainer)httpServletRequest.getAttribute("permissionUpdateRequestContainer");
    if (permissionUpdateRequestContainer == null) {
      permissionUpdateRequestContainer = new PermissionUpdateRequestContainer();
      permissionUpdateRequestContainer.storeToRequest();
    }
    return permissionUpdateRequestContainer;
  }

  /**
   * gui attribute assign e.g. for edit screen
   * @return gui attribute assign
   */
  public GuiAttributeAssign getGuiAttributeAssign() {
    return this.guiAttributeAssign;
  }

  /**
   * gui attribute assignment e.g. for edit screen
   * @param guiAttributeAssign1
   */
  public void setGuiAttributeAssign(GuiAttributeAssign guiAttributeAssign1) {
    this.guiAttributeAssign = guiAttributeAssign1;
  }
  


}
