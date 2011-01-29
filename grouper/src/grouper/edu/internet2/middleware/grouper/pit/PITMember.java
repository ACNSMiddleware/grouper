package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITMember extends GrouperPIT implements Hib3GrouperVersioned {

  /** db id for this row */
  public static final String COLUMN_ID = "id";

  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** subject id */
  public static final String COLUMN_SUBJECT_ID = "subject_id";

  /** subject source */
  public static final String COLUMN_SUBJECT_SOURCE = "subject_source";
  
  /** subject type */
  public static final String COLUMN_SUBJECT_TYPE = "subject_type";
  
  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  
  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: subjectId */
  public static final String FIELD_SUBJECT_ID = "subjectId";
  
  /** constant for field name for: subjectSource */
  public static final String FIELD_SUBJECT_SOURCE = "subjectSource";
  
  /** constant for field name for: subjectType */
  public static final String FIELD_SUBJECT_TYPE = "subjectType";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID,
      FIELD_SUBJECT_ID, FIELD_SUBJECT_SOURCE, FIELD_SUBJECT_TYPE);



  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_PIT_MEMBERS = "grouper_pit_members";

  /** id of this type */
  private String id;

  /** context id ties multiple db changes */
  private String contextId;

  /** subjectId */
  private String subjectId;
  
  /** subjectSource */
  private String subjectSourceId;
  
  /** subjectType */
  private String subjectTypeId;

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return subjectId
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * Set subjectId
   * @param subjectId
   */
  public void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }
  
  /**
   * @return subjectSourceId
   */
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  /**
   * Set subjectSourceId
   * @param subjectSourceId
   */
  public void setSubjectSourceId(String subjectSourceId) {
    this.subjectSourceId = subjectSourceId;
  }
  
  /**
   * @return subjectType
   */
  public String getSubjectTypeId() {
    return subjectTypeId;
  }

  /**
   * Set subjectTypeId
   * @param subjectTypeId
   */
  public void setSubjectTypeId(String subjectTypeId) {
    this.subjectTypeId = subjectTypeId;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getPITMember().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getPITMember().delete(this);
  }
  
  /**
   * @param pitFieldId specifies the field id.  This is required.
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return Set of PITGroup
   */
  public Set<PITGroup> getGroups(String pitFieldId, String scope, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo, QueryOptions queryOptions) {
    return PITMember.getGroups(this.getId(), pitFieldId, scope, pointInTimeFrom, pointInTimeTo, queryOptions);
  }
  
  /**
   * @param pitMemberId specifies the member id.  This is required.
   * @param pitFieldId specifies the field id.  This is required.
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param pointInTimeFrom the start of the range of the point in time query.  This is optional.
   * @param pointInTimeTo the end of the range of the point in time query.  This is optional.  If this is the same as pointInTimeFrom, then the query will be done at a single point in time rather than a range.
   * @param queryOptions optional query options.
   * @return Set of PITGroup
   */
  public static Set<PITGroup> getGroups(String pitMemberId, String pitFieldId, String scope, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo, QueryOptions queryOptions) {
    
    if (pitMemberId == null) {
      throw new IllegalArgumentException("pitMemberId required.");
    }
    
    if (pitFieldId == null) {
      throw new IllegalArgumentException("pitFieldId required.");
    }
    
    return GrouperDAOFactory.getFactory().getPITGroup().getAllGroupsMembershipSecure(
        pitMemberId, pitFieldId, scope, pointInTimeFrom, pointInTimeTo, queryOptions);
  }
}
