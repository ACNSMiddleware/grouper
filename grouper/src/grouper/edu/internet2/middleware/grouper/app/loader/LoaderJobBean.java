/*
 * @author mchyzer
 * $Id: LoaderJobBean.java,v 1.2 2009-04-28 20:08:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class LoaderJobBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: andGroups */
  public static final String FIELD_AND_GROUPS = "andGroups";

  /** constant for field name for: attributeDefName */
  public static final String FIELD_ATTRIBUTE_DEF_NAME = "attributeDefName";

  /** constant for field name for: attributeLoaderAttrQuery */
  public static final String FIELD_ATTRIBUTE_LOADER_ATTR_QUERY = "attributeLoaderAttrQuery";

  /** constant for field name for: attributeLoaderAttrSetQuery */
  public static final String FIELD_ATTRIBUTE_LOADER_ATTR_SET_QUERY = "attributeLoaderAttrSetQuery";

  /** constant for field name for: attributeLoaderAttrsLike */
  public static final String FIELD_ATTRIBUTE_LOADER_ATTRS_LIKE = "attributeLoaderAttrsLike";

  /** constant for field name for: groupLikeString */
  public static final String FIELD_GROUP_LIKE_STRING = "groupLikeString";

  /** constant for field name for: groupNameOverall */
  public static final String FIELD_GROUP_NAME_OVERALL = "groupNameOverall";

  /** constant for field name for: groupQuery */
  public static final String FIELD_GROUP_QUERY = "groupQuery";

  /** constant for field name for: groupTypes */
  public static final String FIELD_GROUP_TYPES = "groupTypes";

  /** constant for field name for: grouperLoaderDb */
  public static final String FIELD_GROUPER_LOADER_DB = "grouperLoaderDb";

  /** constant for field name for: grouperLoaderType */
  public static final String FIELD_GROUPER_LOADER_TYPE = "grouperLoaderType";

  /** constant for field name for: grouperSession */
  public static final String FIELD_GROUPER_SESSION = "grouperSession";

  /** constant for field name for: hib3GrouploaderLogOverall */
  public static final String FIELD_HIB3_GROUPLOADER_LOG_OVERALL = "hib3GrouploaderLogOverall";

  /** constant for field name for: query */
  public static final String FIELD_QUERY = "query";

  /** constant for field name for: startTime */
  public static final String FIELD_START_TIME = "startTime";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_AND_GROUPS, FIELD_ATTRIBUTE_DEF_NAME, FIELD_ATTRIBUTE_LOADER_ATTR_QUERY, FIELD_ATTRIBUTE_LOADER_ATTR_SET_QUERY, 
      FIELD_ATTRIBUTE_LOADER_ATTRS_LIKE, FIELD_GROUP_LIKE_STRING, FIELD_GROUP_NAME_OVERALL, FIELD_GROUP_QUERY, 
      FIELD_GROUP_TYPES, FIELD_GROUPER_LOADER_DB, FIELD_GROUPER_LOADER_TYPE, FIELD_GROUPER_SESSION, 
      FIELD_HIB3_GROUPLOADER_LOG_OVERALL, FIELD_QUERY, FIELD_START_TIME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  
  /**
   * start time of job
   */
  private long startTime;
  
  /**
   * type of loader
   */
  private GrouperLoaderType grouperLoaderType;

  /**
   * group name for the job.  If this is a group list, then this is the overall group name
   */
  private String groupNameOverall;
  
  /**
   * attributeDef name for the job
   */
  private String attributeDefName;
  
  /**
   * attributeDef name for the job
   * @return attributeDef name
   */
  public String getAttributeDefName() {
    return attributeDefName;
  }

  /**
   * attributeDef name for the job
   * @param attributeDefName1
   */
  public void setAttributeDefName(String attributeDefName1) {
    this.attributeDefName = attributeDefName1;
  }

  /**
   * database this job runs against
   */
  private GrouperLoaderDb grouperLoaderDb;
  
  /**
   * quert for the job
   */
  private String query;
  
  /**
   * log
   */
  private Hib3GrouperLoaderLog hib3GrouploaderLogOverall;
  
  /**
   * grouper session for the job, probably a root session
   */
  private GrouperSession grouperSession;
  
  /**
   * members must be in these groups to be in the overall group
   */
  private List<Group> andGroups;
  
  /**
   * group types to add to loader managed group
   */
  private List<GroupType> groupTypes;
  
  /**
   * groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   */
  private String groupLikeString; 
  
  /**
   * query for the job
   */
  private String groupQuery;
  
  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   */
  private String attributeLoaderAttrsLike;
  
  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   */
  private String attributeLoaderAttrQuery;
  
  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   */
  private String attributeLoaderAttrSetQuery;
  
  /** SQL query with at least the following column: action_name */
  private String attributeLoaderActionQuery;

  /** SQL query with at least the following columns: if_has_action_name, then_has_action_name */
  private String attributeLoaderActionSetQuery;
  
  
  /**
   * SQL query with at least the following column: action_name
   * @return query
   */
  public String getAttributeLoaderActionQuery() {
    return attributeLoaderActionQuery;
  }

  /**
   * SQL query with at least the following column: action_name
   * @param attributeLoaderActionQuery1
   */
  public void setAttributeLoaderActionQuery(String attributeLoaderActionQuery1) {
    this.attributeLoaderActionQuery = attributeLoaderActionQuery1;
  }

  /**
   * SQL query with at least the following columns: if_has_action_name, then_has_action_name
   * @return query
   */
  public String getAttributeLoaderActionSetQuery() {
    return attributeLoaderActionSetQuery;
  }

  /**
   * SQL query with at least the following columns: if_has_action_name, then_has_action_name
   * @param attributeLoaderActionSetQuery1
   */
  public void setAttributeLoaderActionSetQuery(String attributeLoaderActionSetQuery1) {
    this.attributeLoaderActionSetQuery = attributeLoaderActionSetQuery1;
  }

  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   * @return attrs like
   */
  public String getAttributeLoaderAttrsLike() {
    return attributeLoaderAttrsLike;
  }

  /**
   * If empty, then orphans will be left alone (for attributeDefName and attributeDefNameSets).  If %, then all orphans deleted.  If a SQL like string, then only ones in that like string not in loader will be deleted
   * @param attributeLoaderAttrsLike1
   */
  public void setAttributeLoaderAttrsLike(String attributeLoaderAttrsLike1) {
    this.attributeLoaderAttrsLike = attributeLoaderAttrsLike1;
  }

  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   * @return query
   */
  public String getAttributeLoaderAttrQuery() {
    return attributeLoaderAttrQuery;
  }

  /**
   * SQL query with at least some of the following columns: attr_name, attr_display_name, attr_description
   * @param attributeLoaderAttrQuery1
   */
  public void setAttributeLoaderAttrQuery(String attributeLoaderAttrQuery1) {
    this.attributeLoaderAttrQuery = attributeLoaderAttrQuery1;
  }

  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   * @return sql query
   */
  public String getAttributeLoaderAttrSetQuery() {
    return attributeLoaderAttrSetQuery;
  }

  /**
   * SQL query with at least the following columns: if_has_attr_name, then_has_attr_name
   * @param attributeLoaderAttrSetQuery1
   */
  public void setAttributeLoaderAttrSetQuery(String attributeLoaderAttrSetQuery1) {
    this.attributeLoaderAttrSetQuery = attributeLoaderAttrSetQuery1;
  }

  /**
   * 
   */
  public LoaderJobBean() {
    super();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public LoaderJobBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * type of job, e.g. group list, or sql simple
   * @return type
   */
  public GrouperLoaderType getGrouperLoaderType() {
    return this.grouperLoaderType;
  }

  /**
   * overall group name (if a group list job, then overall, if sql simple, then the group)
   * @return group name overall
   */
  public String getGroupNameOverall() {
    return this.groupNameOverall;
  }

  /**
   * database this runs against
   * @return loader db
   */
  public GrouperLoaderDb getGrouperLoaderDb() {
    return this.grouperLoaderDb;
  }

  /**
   * query for the job
   * @return query
   */
  public String getQuery() {
    return this.query;
  }

  /**
   * log entry for the job
   * @return log
   */
  public Hib3GrouperLoaderLog getHib3GrouploaderLogOverall() {
    return this.hib3GrouploaderLogOverall;
  }

  /**
   * grouper session (probably a root session)
   * @return session
   */
  public GrouperSession getGrouperSession() {
    return this.grouperSession;
  }

  /**
   * members must be in these groups also to be in the overall group
   * @return and groups
   */
  public List<Group> getAndGroups() {
    return this.andGroups;
  }

  /**
   * group types to add to loader managed group
   * @return group types
   */
  public List<GroupType> getGroupTypes() {
    return this.groupTypes;
  }

  /**
   * 
   * @return group like string
   */
  public String getGroupLikeString() {
    return this.groupLikeString;
  }

  /**
   * group query
   * @return group query
   */
  public String getGroupQuery() {
    return this.groupQuery;
  }

  /**
   * @param grouperLoaderType1
   * @param groupNameOverall1
   * @param grouperLoaderDb1
   * @param query1
   * @param hib3GrouploaderLogOverall1
   * @param grouperSession1
   * @param andGroups1
   * @param groupTypes1
   * @param groupLikeString1 groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupQuery1
   * @param startTime1 
   */
  public LoaderJobBean(GrouperLoaderType grouperLoaderType1,
      String groupNameOverall1, GrouperLoaderDb grouperLoaderDb1, String query1,
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1,
      GrouperSession grouperSession1, List<Group> andGroups1,
      List<GroupType> groupTypes1, String groupLikeString1, String groupQuery1, long startTime1) {
    this.grouperLoaderType = grouperLoaderType1;
    this.groupNameOverall = groupNameOverall1;
    this.grouperLoaderDb = grouperLoaderDb1;
    this.query = query1;
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
    this.grouperSession = grouperSession1;
    this.andGroups = andGroups1;
    this.groupTypes = groupTypes1;
    this.groupLikeString = groupLikeString1;
    this.groupQuery = groupQuery1;
    this.startTime = startTime1;
  }

  /**
   * @param grouperLoaderType1
   * @param attributeDefName 
   * @param groupNameOverall1
   * @param grouperLoaderDb1
   * @param query1
   * @param hib3GrouploaderLogOverall1
   * @param grouperSession1
   * @param andGroups1
   * @param groupTypes1
   * @param groupLikeString1 groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupQuery1
   * @param startTime1 
   * @param attributeLoaderAttrQuery1
   * @param attributeLoaderAttrSetQuery1
   * @param attributeLoaderAttrsLike1
   * @param attributeLoaderActionQuery1 
   * @param attributeLoaderActionSetQuery1 
   */
  public LoaderJobBean(GrouperLoaderType grouperLoaderType1, String attributeDefName,
      GrouperLoaderDb grouperLoaderDb1, 
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1,
      GrouperSession grouperSession1, String attributeLoaderAttrQuery1, 
      String attributeLoaderAttrSetQuery1, String attributeLoaderAttrsLike1,
      String attributeLoaderActionQuery1, String attributeLoaderActionSetQuery1, long startTime1  ) {
    this.attributeDefName = attributeDefName;
    this.grouperLoaderType = grouperLoaderType1;
    this.grouperLoaderDb = grouperLoaderDb1;
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
    this.grouperSession = grouperSession1;
    this.attributeLoaderAttrQuery = attributeLoaderAttrQuery1;
    this.attributeLoaderAttrSetQuery = attributeLoaderAttrSetQuery1;
    this.attributeLoaderAttrsLike = attributeLoaderAttrsLike1;
    this.attributeLoaderActionQuery = attributeLoaderActionQuery1;
    this.attributeLoaderActionSetQuery = attributeLoaderActionSetQuery1;
    this.startTime = startTime1;
  }

  /**
   * type of job, e.g. sql simple or group list
   * @param grouperLoaderType
   */
  public void setGrouperLoaderType(GrouperLoaderType grouperLoaderType) {
    this.grouperLoaderType = grouperLoaderType;
  }

  /**
   * group name for job (if group list, this is the overall name)
   * @param groupNameOverall
   */
  public void setGroupNameOverall(String groupNameOverall) {
    this.groupNameOverall = groupNameOverall;
  }

  /**
   * db this job runs against
   * @param grouperLoaderDb
   */
  public void setGrouperLoaderDb(GrouperLoaderDb grouperLoaderDb) {
    this.grouperLoaderDb = grouperLoaderDb;
  }

  /**
   * query for this job (if runs against query)
   * @param query1
   */
  public void setQuery(String query1) {
    this.query = query1;
  }

  /**
   * 
   * @param hib3GrouploaderLogOverall1
   */
  public void setHib3GrouploaderLogOverall(
      Hib3GrouperLoaderLog hib3GrouploaderLogOverall1) {
    this.hib3GrouploaderLogOverall = hib3GrouploaderLogOverall1;
  }

  /**
   * grouper session, probably a root session
   * @param grouperSession1
   */
  public void setGrouperSession(GrouperSession grouperSession1) {
    this.grouperSession = grouperSession1;
  }

  /**
   * members must be in these groups also to be in the overall group
   * @param andGroups1
   */
  public void setAndGroups(List<Group> andGroups1) {
    this.andGroups = andGroups1;
  }

  /**
   * group types to add to loader managed group
   * @param groupTypes
   */
  public void setGroupTypes(List<GroupType> groupTypes) {
    this.groupTypes = groupTypes;
  }

  /**
   * groups with this like DB sql string are managed by the loader.
   * Any group in this list with no memberships and not in the group
   * metadata query will be emptied and if configured deleted
   * @param groupLikeString
   */
  public void setGroupLikeString(String groupLikeString) {
    this.groupLikeString = groupLikeString;
  }

  /**
   * 
   * @param groupQuery1
   */
  public void setGroupQuery(String groupQuery1) {
    this.groupQuery = groupQuery1;
  }

  /**
   * start time of job
   * @return start time
   */
  public long getStartTime() {
    return this.startTime;
  }

  /**
   * start time of job
   * @param startTime1
   */
  public void setStartTime(long startTime1) {
    this.startTime = startTime1;
  }

}
