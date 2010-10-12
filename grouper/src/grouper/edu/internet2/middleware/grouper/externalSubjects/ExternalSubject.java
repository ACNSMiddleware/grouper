package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Date;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * database object for external subject
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class ExternalSubject extends GrouperAPI implements GrouperHasContext, 
   Hib3GrouperVersioned {

  /** uuid for row */
  private String uuid;
  
  /** the thing that the subject uses to login */
  private String identifier;
  
  /** name of subject */
  private String name;

  /** description, which is generated from other attributes */
  private String description;

  /** email address */
  private String email;

  /** institution where the user is from */
  private String institution;

  /** search string to find a subject, in all lower case */
  private String searchStringLower;
  
  /** contextId links to audit tables */
  private String contextId;

  /** time created */
  private long createTime = System.currentTimeMillis();

  /** who created this */
  private String creatorMemberId;

  /** who last modified this */
  private String modifierMemberId;

  /** when last modified */
  private long modifyTime = System.currentTimeMillis(); 

  /** when this was disabled, or when it will be disabled */
  private Long disabledTime = null; 

  /** is this is currently enabled */
  private boolean enabled = true;
  
  /**
   * when this was disabled, or when it will be disabled
   * @return the millis from 1970
   */
  public Long getDisabledTimeDb() {
    return this.disabledTime;
  }


  /**
   * when this was disabled, or when it will be disabled
   * @param disabledTime1
   */
  public void setDisabledTimeDb(Long disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /**
   * when this was disabled, or when it will be disabled, millis from 1970
   * @return disabled time
   */
  public Date getDisabledTime() {
    return this.disabledTime == null ? null : new Date(this.disabledTime);
  }

  /**
   * when this was disabled, or when it will be disabled, millis from 1970
   * @param theDisabledTime1
   */
  public void setDisabledTime(Date theDisabledTime1) {
    this.disabledTime = theDisabledTime1 == null ? null : theDisabledTime1.getTime();
  }
  
  /**
   * if this is enabled
   * @return true if enabled
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * if this is enabled
   * @return T or F
   */
  public String getEnabledDb() {
    return this.enabled ? "T" : "F";
  }

  /**
   * if this is enabled
   * @param enabled1
   */
  public void setEnabled(boolean enabled1) {
    this.enabled = enabled1;
  }

  /**
   * if this is enabled, T or F
   * @param enabled1
   */
  public void setEnabledDb(String enabled1) {
    this.enabled = GrouperUtil.booleanValue(enabled1);
  }

  /** table name for external subjects */
  public static final String TABLE_GROUPER_EXT_SUBJ = "grouper_ext_subj";
  
  /** column name for context id */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** column name for description */
  public static final String COLUMN_DESCRIPTION = "description";
  
  /** column name for disabled time */
  public static final String COLUMN_DISABLED_TIME = "disabled_time";
  
  /** column name for email */
  public static final String COLUMN_EMAIL = "email";
  
  /** column name for enabled */
  public static final String COLUMN_ENABLED = "enabled";
  
  /** column name for identifier */
  public static final String COLUMN_IDENTIFIER = "identifier";
  
  /** column name for institution */
  public static final String COLUMN_INSTITUTION = "institution";
  
  /** column name for name */
  public static final String COLUMN_NAME = "name";
  
  /** column name for searchStringLower */
  public static final String COLUMN_SEARCH_STRING_LOWER = "search_string_lower";
  
  /** column name for uuid */
  public static final String COLUMN_UUID = "uuid";
  
  /** column name for create time */
  public static final String COLUMN_CREATE_TIME = "create_time";
  
  /** column name for creator member id */
  public static final String COLUMN_CREATOR_MEMBER_ID = "creator_member_id";
  
  /** column name for modify time */
  public static final String COLUMN_MODIFY_TIME = "modify_time";
  
  /** column name for modifier member id */
  public static final String COLUMN_MODIFIER_MEMBER_ID = "modifier_member_id";
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorMemberId */
  public static final String FIELD_CREATOR_MEMBER_ID = "creatorMemberId";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: disabledTime */
  public static final String FIELD_DISABLED_TIME = "disabledTime";

  /** constant for field name for: email */
  public static final String FIELD_EMAIL = "email";

  /** constant for field name for: enabled */
  public static final String FIELD_ENABLED = "enabled";

  /** constant for field name for: identifier */
  public static final String FIELD_IDENTIFIER = "identifier";

  /** constant for field name for: institution */
  public static final String FIELD_INSTITUTION = "institution";

  /** constant for field name for: modifierMemberId */
  public static final String FIELD_MODIFIER_MEMBER_ID = "modifierMemberId";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: searchStringLower */
  public static final String FIELD_SEARCH_STRING_LOWER = "searchStringLower";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_MEMBER_ID, FIELD_DESCRIPTION, 
      FIELD_DISABLED_TIME, FIELD_EMAIL, FIELD_ENABLED, FIELD_IDENTIFIER, 
      FIELD_INSTITUTION, FIELD_MODIFIER_MEMBER_ID, FIELD_MODIFY_TIME, FIELD_NAME, 
      FIELD_SEARCH_STRING_LOWER, FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_MEMBER_ID, FIELD_DESCRIPTION, 
      FIELD_DISABLED_TIME, FIELD_EMAIL, FIELD_ENABLED, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_IDENTIFIER, FIELD_INSTITUTION, FIELD_MODIFIER_MEMBER_ID, FIELD_MODIFY_TIME, 
      FIELD_NAME, FIELD_SEARCH_STRING_LOWER, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /**
   * contextId links to audit tables
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }


  /**
   * search string to find a subject, in all lower case
   * @return search string lower
   */
  public String getSearchStringLower() {
    return searchStringLower;
  }


  /**
   * search string to find a subject, in all lower case
   * @param searchStringLower1
   */
  public void setSearchStringLower(String searchStringLower1) {
    this.searchStringLower = searchStringLower1;
  }

  /**
   * uuid for row
   * @return uuid
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * uuid for row
   * @param uuid1
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * the thing that the subject uses to login
   * @return identifier
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * the thing that the subject uses to login
   * @param identifier1
   */
  public void setIdentifier(String identifier1) {
    this.identifier = identifier1;
  }

  /**
   * name of subject
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * name of subject
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * description, which is generated from other attributes
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * description, which is generated from other attributes
   * @param description1
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * email address
   * @return email
   */
  public String getEmail() {
    return email;
  }

  /**
   * email address
   * @param email1
   */
  public void setEmail(String email1) {
    this.email = email1;
  }

  /**
   * institution where the user is from
   * @return institution
   */
  public String getInstitution() {
    return institution;
  }

  /**
   * institution where the user is from
   * @param institution1
   */
  public void setInstitution(String institution1) {
    this.institution = institution1;
  }


  /**
   * deep clone the fields in this object
   */
  @Override
  public ExternalSubject clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


  /**
   * contextId links to audit tables
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }


  /**
   * Get creation time for this subject.
   * @return  {@link Date} that this subject was created.
   */
  public Date getCreateTime() {
    return new Date(this.getCreateTimeDb());
  }


  /**
   * @return create time
   */
  public long getCreateTimeDb() {
    return this.createTime;
  }


  /**
   * @return creator
   */
  public String getCreatorMemberId() {
    return this.creatorMemberId;
  }

  /**
   * 
   * @return the modifier member id
   */
  public String getModifierMemberId() {
    return this.modifierMemberId;
  }
  
  /**
   * create time
   * @param createTime1 
   */
  public void setCreateTimeDb(long createTime1) {
    this.createTime = createTime1;
  
  }


  /**
   * member id of creator
   * @param creatorMemberId1
   */
  public void setCreatorMemberId(String creatorMemberId1) {
    this.creatorMemberId = creatorMemberId1;
  
  }


  /**
   * member id of modifier
   * @param modifierMemberId1
   */
  public void setModifierMemberId(String modifierMemberId1) {
    this.modifierMemberId = modifierMemberId1;
  
  }


  /**
   * last time modified
   * @param modifyTime1 
   */
  public void setModifyTimeDb(long modifyTime1) {
    this.modifyTime = modifyTime1;
  
  }


  /**
   * Get modify time for this subject.
   * @return  {@link Date} that this subject was created.
   */
  public Date getModifyTime() {
    return new Date(this.getModifyTimeDb());
  }


  /**
   * @return modify time
   */
  public long getModifyTimeDb() {
    return this.modifyTime;
  }

  /**
   * cache if someone can edit external subjects
   */
  private static GrouperCache<MultiKey, Boolean> subjectCanEditExternalUser = 
    new GrouperCache(ExternalSubject.class.getName(), 200, false, 60, 60, false);
  
  /**
   * see if someone is allowed to edit, cache for 1 minute
   * @param subject
   * @return true if allowed to edit
   */
  public static boolean subjectCanEditExternalUser(final Subject subject) {
    
    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
    
    Boolean result = subjectCanEditExternalUser.get(multiKey);
    
    if (result != null) {
      return result;
    }
    
    //figure it out
    boolean wheelOrRootCanEdit = GrouperConfig.getPropertyBoolean("externalSubjects.wheelOrRootCanEdit", true);
    final String groupAllowedForEdit = GrouperConfig.getProperty("externalSubjects.groupAllowedForEdit");
    
    if (wheelOrRootCanEdit) {
      if (PrivilegeHelper.isWheelOrRoot(subject)) {
        result = true;
      }
    }
    
    if (result == null || !result) {
      if (!StringUtils.isBlank(groupAllowedForEdit)) {
        
        //use root since the current user might not be able to read the group of allowed users
        GrouperSession rootSession = GrouperSession.staticGrouperSession().internal_getRootSession();
        result = (Boolean)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {
          
          public Object callback(GrouperSession theRootSession) throws GrouperSessionException {
            
            Group theGroupAllowedForEdit = GroupFinder.findByName(theRootSession, groupAllowedForEdit, true);
            
            return theGroupAllowedForEdit.hasMember(subject);
            
          }
        });
      }
      
    }
    
    //just cache the positives...
    if (result != null && result) {
      subjectCanEditExternalUser.put(multiKey, result);
    }
    return result != null ? result : false;
  }
  
  /**
   * @see GrouperAPI#onPreSave(HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    if (StringUtils.isBlank(this.getUuid())) {
      this.setUuid(GrouperUuid.getUuid());
    }
    
    this.setModifierMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeDb( System.currentTimeMillis() );

    this.setCreatorMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setCreateTimeDb( System.currentTimeMillis() );
  }


  /**
   * @see GrouperAPI#onPreUpdate(HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    this.setModifierMemberId( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeDb( System.currentTimeMillis() );

  }


  /**
   * store this object to the DB.
   */
  public void store() {    
    
    Subject currentSubject = GrouperSession.staticGrouperSession().getSubject();
    
    if (!subjectCanEditExternalUser(currentSubject)) {
      throw new RuntimeException("Subject cannot edit external users (per grouper.properties): " + GrouperUtil.subjectToString(currentSubject));
    }
    
    HibernateSession.callbackHibernateSession(
      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
      new HibernateHandler() {

        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

          GrouperDAOFactory.getFactory().getExternalSubject().saveOrUpdate( ExternalSubject.this );
            
          return null;
        }
      });
    
  }

}
