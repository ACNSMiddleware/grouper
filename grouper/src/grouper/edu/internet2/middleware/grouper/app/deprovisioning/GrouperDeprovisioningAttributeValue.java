/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 * bean that represents s attributes
 */
public class GrouperDeprovisioningAttributeValue {

  /**
   * set strings to defaults to reduce the number of assignment values we need
   */
  public void flattenValues() {
    
    if (!this.isAllowAddsWhileDeprovisioned()) {
      this.allowAddsWhileDeprovisionedString = null;
    }
    
    if (this.isAutoChangeLoader() == GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.autoChangeLoader", true)) {
      this.autoChangeLoaderString = null;
    }
    
    if (this.isAutoselectForRemoval()) {
      this.autoselectForRemovalString = null;
    }
    
    if (this.isDeprovision()) {
      this.deprovisionString = null;
    }

    if (!this.isDirectAssignment() ) {
      this.directAssignmentString = null;
    }

    if (!this.isSendEmail()) {
      this.sendEmailString = null;
    }
    if (this.isShowForRemoval()) {
      this.showForRemovalString = null;
    }
    
  }
  
  
  /**
   * If allows adds to group of people who are deprovisioned
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   */
  private String allowAddsWhileDeprovisionedString;

  
  /**
   * If allows adds to group of people who are deprovisioned
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   * @return the allowAddsWhileDeprovisionedString
   */
  public String getAllowAddsWhileDeprovisionedString() {
    return this.allowAddsWhileDeprovisionedString;
  }

  /**
   * if allow adds while deprovisioned
   * @return true / false
   */
  public boolean isAllowAddsWhileDeprovisioned() {
    return GrouperUtil.booleanValue(this.allowAddsWhileDeprovisionedString, false);
  }
  
  /**
   * If allows adds to group of people who are deprovisioned
   * can be: blank, true, or false.  If blank, then will not allow adds unless auto change loader is false
   * @param allowAddsWhileDeprovisionedString1 the allowAddsWhileDeprovisionedString to set
   */
  public void setAllowAddsWhileDeprovisionedString(String allowAddsWhileDeprovisionedString1) {
    this.allowAddsWhileDeprovisionedString = allowAddsWhileDeprovisionedString1;
  }

  /**
   * If this is a loader job, if being in a deprovisioned group means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   */
  private String autoChangeLoaderString;
  
  /**
   * 
   * @return true if auto change loader based on config
   */
  public boolean isAutoChangeLoader() {
    
    boolean defaultAutoChangeLoader = GrouperConfig.retrieveConfig().propertyValueBoolean("deprovisioning.autoChangeLoader", true);

    return GrouperUtil.booleanValue(this.autoChangeLoaderString, defaultAutoChangeLoader);
    
  }
  
  /**
   * If this is a loader job, if being in a deprovisioned group means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   * @return the autoChangeLoaderString
   */
  public String getAutoChangeLoaderString() {
    return this.autoChangeLoaderString;
  }
  
  /**
   * If this is a loader job, if being in a deprovisioned group means the user should not be in the loaded group.
   * can be: blank (true), or false (false)
   * @param autoChangeLoaderString the autoChangeLoaderString to set
   */
  public void setAutoChangeLoaderString(String autoChangeLoaderString) {
    this.autoChangeLoaderString = autoChangeLoaderString;
  }

  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   */
  private String autoselectForRemovalString;
  
  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   * @return the autoselectForRemoval
   */
  public String getAutoselectForRemovalString() {
    return this.autoselectForRemovalString;
  }
  
  /**
   * 
   * @return if autoselect for removal
   */
  public boolean isAutoselectForRemoval() {
    return GrouperUtil.booleanValue(this.autoselectForRemovalString, true);
  }
  
  /**
   * If the deprovisioning screen should autoselect this object as an object to deprovision
   * can be: blank, true, or false.  If blank, then will autoselect unless deprovisioningAutoChangeLoader is false
   * @param autoselectForRemoval1 the autoselectForRemoval to set
   */
  public void setAutoselectForRemovalString(String autoselectForRemoval1) {
    this.autoselectForRemovalString = autoselectForRemoval1;
  }


  /**
   * true|false, true to deprovision, false to not deprovision (default to true). 
   * Note, if this is set on a daemon job, then it will not deprovision any group 
   * in the loader job (they will be marked as such)
   */
  private String deprovisionString;
  

  /**
   * true|false, true to deprovision, false to not deprovision (default to true). 
   * Note, if this is set on a daemon job, then it will not deprovision any group 
   * in the loader job (they will be marked as such)
   * @return the deprovisionString
   */
  public String getDeprovisionString() {
    return this.deprovisionString;
  }

  /**
   * 
   * @return true if deprovision
   */
  public boolean isDeprovision() {
    return GrouperUtil.booleanValue(this.deprovisionString, true);
  }

  /**
   * true|false, true to deprovision, false to not deprovision (default to true). 
   * Note, if this is set on a daemon job, then it will not deprovision any group 
   * in the loader job (they will be marked as such)
   * @param deprovisionString1 the deprovisionString to set
   */
  public void setDeprovisionString(String deprovisionString1) {
    this.deprovisionString = deprovisionString1;
  }

  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   */
  private String directAssignmentString;
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * @return the directAssignmentString
   */
  public String getDirectAssignmentString() {
    return this.directAssignmentString;
  }
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * @return the directAssignmentString
   */
  public boolean isDirectAssignment() {
    return GrouperUtil.booleanValue(this.directAssignmentString, false);
  }
  
  /**
   * If deprovisioning configuration is directly assigned to the group or folder or inherited from parent
   * @param directAssignmentString1 the directAssignmentString to set
   */
  public void setDirectAssignmentString(String directAssignmentString1) {
    this.directAssignmentString = directAssignmentString1;
  }


  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   */
  private String emailAddressesString;


  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   * @return the emailAddressesString
   */
  public String getEmailAddressesString() {
    return this.emailAddressesString;
  }


  
  /**
   * Email addresses to send deprovisioning messages.
   * If blank, then send to group managers, or comma separated email addresses (mutually exclusive with deprovisioningMailToGroup)
   * @param emailAddressesString1 the emailAddressesString to set
   */
  public void setEmailAddressesString(String emailAddressesString1) {
    this.emailAddressesString = emailAddressesString1;
  }

  /**
   * custom email body for emails, if blank use the default configured body. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  private String emailBodyString;
  
  /**
   * custom email body for emails, if blank use the default configured body. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @return the emailBodyString
   */
  public String getEmailBodyString() {
    return this.emailBodyString;
  }
  
  /**
   * custom email body for emails, if blank use the default configured body. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @param emailBodyString the emailBodyString to set
   */
  public void setEmailBodyString(String emailBodyString) {
    this.emailBodyString = emailBodyString;
  }

  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   */
  private String inheritedFromFolderIdString;

  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   * @return inherited from folder id
   */
  public String getInheritedFromFolderIdString() {
    return this.inheritedFromFolderIdString;
  }


  /**
   * Stem ID of the folder where the configuration is inherited from.  This is blank if this is a 
   * direct assignment and not inherited
   * @param inheritedFromFolderIdString1
   */
  public void setInheritedFromFolderIdString(String inheritedFromFolderIdString1) {
    this.inheritedFromFolderIdString = inheritedFromFolderIdString1;
  }

  /**
   * custom subject for emails, if blank use the default configured subject. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   */
  private String emailSubjectString;
  

  
  /**
   * custom subject for emails, if blank use the default configured subject. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @return the emailSubjectString
   */
  public String getEmailSubjectString() {
    return this.emailSubjectString;
  }


  
  /**
   * custom subject for emails, if blank use the default configured subject. Note there are template variables 
   * $$name$$ $$netId$$ $$userSubjectId$$ $$userEmailAddress$$ $$userDescription$$
   * @param emailSubjectString1 the emailSubjectString to set
   */
  public void setEmailSubjectString(String emailSubjectString1) {
    this.emailSubjectString = emailSubjectString1;
  }


  /**
   * Group ID which holds people to email members of that group to send deprovisioning messages (mutually exclusive with deprovisioningEmailAddresses)
   */
  private String mailToGroupString;
  
  
  /**
   * @return the mailToGroupString
   */
  public String getMailToGroupString() {
    return this.mailToGroupString;
  }


  
  /**
   * @param mailToGroupString the mailToGroupString to set
   */
  public void setMailToGroupString(String mailToGroupString) {
    this.mailToGroupString = mailToGroupString;
  }

  /**
   * Affiliation configured in the grouper.properties
   */
  private String affiliationString;
  
  
  /**
   * @return the affiliationString
   */
  public String getAffiliationString() {
    return this.affiliationString;
  }


  
  /**
   * @param affiliationString the affiliationString to set
   */
  public void setAffiliationString(String affiliationString) {
    this.affiliationString = affiliationString;
  }

  /**
   * true|false, default to false. Set this to true for objects where the system of record is outside of grouper or where manual removal is preferred
   */
  private String sendEmailString;
  
  /**
   * 
   * @return true if send email
   */
  public boolean isSendEmail() {
    return GrouperUtil.booleanValue(this.sendEmailString, false);
  }
  
  /**
   * 
   * @return true if show for removal
   */
  public boolean isShowForRemoval() {
    return GrouperUtil.booleanValue(this.showForRemovalString, true);
  }
  
  /**
   * @return the sendEmailString
   */
  public String getSendEmailString() {
    return this.sendEmailString;
  }


  
  /**
   * @param sendEmailString the sendEmailString to set
   */
  public void setSendEmailString(String sendEmailString) {
    this.sendEmailString = sendEmailString;
  }


  /**
   * If the deprovisioning screen should show this object if the user as an assignment.
   * can be: blank, true, or false.  If blank, will default to true unless auto change loader is false.
   */
  private String showForRemovalString;
  

  
  /**
   * @return the showForRemovalString
   */
  public String getShowForRemovalString() {
    return this.showForRemovalString;
  }


  
  /**
   * @param showForRemovalString the showForRemovalString to set
   */
  public void setShowForRemovalString(String showForRemovalString) {
    this.showForRemovalString = showForRemovalString;
  }


  /**
   * one|sub, if in folder only or in folder and all subfolders (default to sub)
   */
  private String stemScopeString;

  
  /**
   * @return the stemScopeString
   */
  public String getStemScopeString() {
    return this.stemScopeString;
  }

  /**
   * get the stem scope if assigned to a stem
   * @return the scope
   */
  public Scope getStemScope() {
    if (StringUtils.isBlank(this.stemScopeString)) {
      return Scope.SUB;
    }
    return Scope.valueOfIgnoreCase(this.stemScopeString, true);
  }
  
  /**
   * @param stemScopeString the stemScopeString to set
   */
  public void setStemScopeString(String stemScopeString) {
    this.stemScopeString = stemScopeString;
  }
  
}
