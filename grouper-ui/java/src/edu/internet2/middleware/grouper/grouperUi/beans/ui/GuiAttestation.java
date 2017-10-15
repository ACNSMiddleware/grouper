/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author vsachdeva
 *
 */
public class GuiAttestation {
  
  private AttributeAssignable attributeAssignable;
  
  private Boolean grouperAttestationSendEmail = true;
  
  private String grouperAttestationEmailAddresses; // list of comma separated emails
  
  private String grouperAttestationDaysUntilRecertify;
  
  /** days before attestation needed */
  private Integer grouperAttestationDaysLeftUntilRecertify;

  /**
   * gui group associated with the group the attestation is on if applicable 
   */
  private GuiGroup guiGroup;

  /**
   * gui stem associates with the stem the attestation is on if applicable
   */
  private GuiStem guiStem;

  /**
   * gui stem associated with the stem the attestation is on if applicable 
   * @return gui stem
   */
  public GuiStem getGuiStem() {
    if (this.guiStem == null) {
      if (this.attributeAssignable instanceof Stem) {
        this.guiStem = new GuiStem((Stem)this.attributeAssignable);
      }
    }
    return this.guiStem;
  }
   

  /**
   * gui group associated with the group the attestation is on if applicable 
   * @return gui group
   */
  public GuiGroup getGuiGroup() {
    if (this.guiGroup == null) {
      if (this.attributeAssignable instanceof Group) {
        this.guiGroup = new GuiGroup((Group)this.attributeAssignable);
      }
    }
    return this.guiGroup;
  }
   
  /**
   * days before attestation needed
   * @return days left before recertify
   */
  public Integer getGrouperAttestationDaysLeftUntilRecertify() {
    return this.grouperAttestationDaysLeftUntilRecertify;
  }

  /**
   * days before attestation needed
   * @param grouperAttestationDaysLeftUntilRecertify1
   */
  public void setGrouperAttestationDaysLeftUntilRecertify(
      Integer grouperAttestationDaysLeftUntilRecertify1) {
    this.grouperAttestationDaysLeftUntilRecertify = grouperAttestationDaysLeftUntilRecertify1;
  }

  private String grouperAttestationLastEmailedDate;
  
  private String grouperAttestationDaysBeforeToRemind;
  
  private String grouperAttestationStemScope;
  
  private String grouperAttestationDateCertified;
  
  private Boolean grouperAttestationDirectAssignment = false;
 
  private Mode mode;
  
  private Type type;
  
  public enum Mode {
    EDIT, ADD
  }
  
  public enum Type {
    DIRECT, INDIRECT
  }
  
  public GuiAttestation(AttributeAssignable attributeAssignable, Type type) {
    this.mode = Mode.ADD;
    this.attributeAssignable = attributeAssignable;
    this.type = type;
  }
  
  
  public GuiAttestation(AttributeAssignable attributeAssignable, Boolean grouperAttestationSendEmail,
      String grouperAttestationEmailAddresses,
      String grouperAttestationDaysUntilRecertify,
      String grouperAttestationLastEmailedDate,
      String grouperAttestationDaysBeforeToRemind, String grouperAttestationStemScope,
      String grouperAttestationDateCertified, Boolean grouperAttestationDirectAssignment, Type type, Integer daysLeftUntilRecertify) {
    
    super();
    this.mode = Mode.EDIT;
    this.attributeAssignable = attributeAssignable;
    this.grouperAttestationSendEmail = grouperAttestationSendEmail;
    this.grouperAttestationEmailAddresses = grouperAttestationEmailAddresses;
    this.grouperAttestationDaysUntilRecertify = grouperAttestationDaysUntilRecertify;
    this.grouperAttestationLastEmailedDate = grouperAttestationLastEmailedDate;
    this.grouperAttestationDaysBeforeToRemind = grouperAttestationDaysBeforeToRemind;
    this.grouperAttestationStemScope = grouperAttestationStemScope;
    this.grouperAttestationDateCertified = grouperAttestationDateCertified;
    this.grouperAttestationDirectAssignment = grouperAttestationDirectAssignment;
    this.type = type;
    this.grouperAttestationDaysLeftUntilRecertify = daysLeftUntilRecertify;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Group.class);

  /**
   * if needs recertify
   * @return if needs recertify
   */
  public boolean isNeedsRecertify() {
    return needsRecertifyHelper(0);
  }

  /**
   * @param daysBuffer is 0 for needs recertify now, or more than that for buffer
   * @return if needs recertify
   */
  public boolean needsRecertifyHelper(int daysBuffer) {
    int daysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (! StringUtils.isBlank(this.grouperAttestationDaysUntilRecertify)) {
      try {
        daysUntilRecertify = GrouperUtil.intValue(this.grouperAttestationDaysUntilRecertify);
      } catch (Exception e) {
        //swallow
      }
      
    }
    
    boolean needsRecertify = false;
    if (StringUtils.isBlank(this.grouperAttestationDateCertified)) {
      needsRecertify = true;
    } else {
      // find the difference between today's date and last certified date
      try {
        Date lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(this.grouperAttestationDateCertified);
        long diff = new Date().getTime() - lastCertifiedDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (diffInDays+daysBuffer > daysUntilRecertify) {
          needsRecertify = true;
        }
      } catch (ParseException e) {
        LOG.error("Could not convert "+this.grouperAttestationDateCertified+" to date. Attribute assign id is: "
            +this.getAttributeAssignable().getAttributeDelegate().getAttributeAssigns().iterator().next().getId(), e);
      }
    }
    return needsRecertify;
  }
  
  /**
   * if needs recertify soon
   * @return if the group needs recertify soon
   */
  public boolean isNeedsRecertifySoon() {
    
    int daysBeforeNeeds = GrouperConfig.retrieveConfig().propertyValueInt("attestation.daysBeforeNeedsAttestationToShowButton", 14);
    return needsRecertifyHelper(daysBeforeNeeds);
  }
  
  /**
   * date this group needs recertify
   * @return the date
   */
  public String getGrouperAttestationDateNeedsCertify() {
    int daysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    if (! StringUtils.isBlank(this.grouperAttestationDaysUntilRecertify)) {
      try {
        daysUntilRecertify = GrouperUtil.intValue(this.grouperAttestationDaysUntilRecertify);
      } catch (Exception e) {
        //swallow
      }
      
    }

    Date dateNeedsCertify = null;
    if (StringUtils.isBlank(this.grouperAttestationDateCertified)) {
      //now
      dateNeedsCertify = new Date();
    } else {
      // find the difference between today's date and last certified date
      try {
        Date lastCertifiedDate = new SimpleDateFormat("yyyy/MM/dd").parse(this.grouperAttestationDateCertified);
        
        Calendar lastCertifiedCalendar = new GregorianCalendar();
        lastCertifiedCalendar.setTime(lastCertifiedDate);
        
        lastCertifiedCalendar.add(Calendar.DAY_OF_YEAR, daysUntilRecertify);
        
        dateNeedsCertify = new Date(lastCertifiedCalendar.getTimeInMillis());
        
      } catch (ParseException e) {
        LOG.error("Could not convert "+this.grouperAttestationDateCertified+" to date. Attribute assign id is: "
            +this.getAttributeAssignable().getAttributeDelegate().getAttributeAssigns().iterator().next().getId(), e);
      }
    }
    return new SimpleDateFormat("yyyy/MM/dd").format(dateNeedsCertify);
  }
  
  /**
   * return the gui folder with settings
   * @return gui stem
   */
  public GuiStem getGuiFolderWithSettings() {
    AttributeAssignable attributeAssignable = this.getAttributeAssignable();
    if (attributeAssignable == null) {
      return null;
    }
    Stem stem = attributeAssignable.getAttributeDelegate().getAttributeAssigns().iterator().next().getOwnerStemFailsafe();
    if (stem == null) {
      return null;
    }
    return new GuiStem(stem);
  }
  
  public AttributeAssignable getAttributeAssignable() {
    return attributeAssignable;
  }

  public Boolean getGrouperAttestationSendEmail() {
    return grouperAttestationSendEmail;
  }
  
  public void setGrouperAttestationSendEmail(Boolean grouperAttestationSendEmail) {
    this.grouperAttestationSendEmail = grouperAttestationSendEmail;
  }

  public Boolean getGrouperAttestationDirectAssignment() {
    return grouperAttestationDirectAssignment;
  }

  public String getGrouperAttestationEmailAddresses() {
    return grouperAttestationEmailAddresses;
  }
  
  public void setGrouperAttestationEmailAddresses(String grouperAttestationEmailAddresses) {
    this.grouperAttestationEmailAddresses = grouperAttestationEmailAddresses;
  }


  public String getGrouperAttestationDaysUntilRecertify() {
    return grouperAttestationDaysUntilRecertify;
  }

  public void setGrouperAttestationDaysUntilRecertify(String grouperAttestationDaysUntilRecertify) {
    this.grouperAttestationDaysUntilRecertify = grouperAttestationDaysUntilRecertify;
  }

  public String getGrouperAttestationLastEmailedDate() {
    return grouperAttestationLastEmailedDate;
  }
  
  
  public String getGrouperAttestationDaysBeforeToRemind() {
    return grouperAttestationDaysBeforeToRemind;
  }
  
  
  public void setGrouperAttestationDaysBeforeToRemind(String grouperAttestationDaysBeforeToRemind) {
    this.grouperAttestationDaysBeforeToRemind = grouperAttestationDaysBeforeToRemind;
  }


  public String getGrouperAttestationStemScope() {
    return grouperAttestationStemScope;
  }
  
  public String getGrouperAttestationDateCertified() {
    return grouperAttestationDateCertified;
  }

  public Mode getMode() {
    return mode;
  }

  public Type getType() {
    return type;
  }
  
}
