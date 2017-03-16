/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import net.redhogs.cronparser.CronExpressionDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.ldap.GrouperLoaderLdapServer;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapElUtils;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiHib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.ldap.LdapHandler;
import edu.internet2.middleware.grouper.ldap.LdapHandlerBean;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.SearchFilter;


/**
 *
 */
public class GrouperLoaderContainer {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderContainer.class);

  /**
   * 
   */
  public GrouperLoaderContainer() {
  }

  /**
   * 
   * @return number of rows
   */
  public int getNumberOfRows() {
    return GrouperUiConfig.retrieveConfig().propertyValueInt("uiV2.loader.logs.maxSize", 400);
  }
  
  /**
   * 
   * @return true if this job has subjobs
   */
  public boolean isHasSubjobs() {
    
    GrouperLoaderType grouperLoaderType = this.getGrouperLoaderType();
    
    if (grouperLoaderType != null && 
        (grouperLoaderType == GrouperLoaderType.LDAP_GROUP_LIST 
          || grouperLoaderType == GrouperLoaderType.LDAP_GROUPS_FROM_ATTRIBUTES 
          || grouperLoaderType == GrouperLoaderType.SQL_GROUP_LIST)) {
      return true;
    }
    
    return false;
  }
  
  /**
   * 
   */
  private List<GuiHib3GrouperLoaderLog> guiHib3GrouperLoaderLogs;

  //private static Pattern groupIdFromJobNamePattern = Pattern.compile(".*__([^_]+)$");
  /**
   * pattern to get group id from job name
   * SQL_GROUP_LIST__penn:community:emplo__yee:affiliationPrimaryConfig__fa9dca910f9a4accb8529dd040dc1198
   */
  private static Pattern groupNameFromJobNamePattern = Pattern.compile("^.*?__(.*)__.*$");
  
  /**
   * group name from subjob name
   */
  private static Pattern groupNameFromSubjobNamePattern = Pattern.compile("^subjobFor_(.*)$");
  
  /**
   * retrieve group name from job name
   * @param jobName
   * @return group id
   */
  public static String retrieveGroupNameFromJobName(String jobName) {
    
    if (StringUtils.isBlank(jobName)) {
      return null;
    }
    
    //try normal job
    Matcher matcher = groupNameFromJobNamePattern.matcher(jobName);
    
    if (matcher.matches()) {
      return matcher.group(1);
    }

    //try subjob
    matcher = groupNameFromSubjobNamePattern.matcher(jobName);
      
    if (matcher.matches()) {
      return matcher.group(1);
    }
    return null;
    
  }
  
  /**
   * hib3 loader logs
   * @return the list of logs
   */
  public List<GuiHib3GrouperLoaderLog> getGuiHib3GrouperLoaderLogs() {

    return this.guiHib3GrouperLoaderLogs;

  }

  /**
   * @param guiHib3GrouperLoaderLogs1 the guiHib3GrouperLoaderLogs to set
   */
  public void setGuiHib3GrouperLoaderLogs(List<GuiHib3GrouperLoaderLog> guiHib3GrouperLoaderLogs1) {
    this.guiHib3GrouperLoaderLogs = guiHib3GrouperLoaderLogs1;
  }

  /**
   * 
   * @return the sql group query
   */
  public String getSqlGroupQuery() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderGroupQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_QUERY);
    
    return grouperLoaderGroupQuery;
    
  }
  
  /**
   * 
   * @return sql groups like
   */
  public String getSqlGroupsLike() {

    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderGroupsLike = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUPS_LIKE);
    
    return grouperLoaderGroupsLike;

  }
  
  /**
   * @return sql group types
   */
  public String getSqlGroupTypes() {

    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderGroupsLike = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_GROUP_TYPES);
    
    return grouperLoaderGroupsLike;

  }
  
  /**
   * 
   * @return database name
   */
  public String getSqlDatabaseName() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderDbName = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_DB_NAME);
    
    return grouperLoaderDbName;
    
  }

  /**
   * 
   * @return database name
   */
  public String getSqlDatabaseNameUrl() {
    
    String databaseName = this.getSqlDatabaseName();

    if (StringUtils.isBlank(databaseName)) {
      return null;
    }
    
    if (StringUtils.equals("grouper", databaseName)) {
      
      return GrouperHibernateConfig.retrieveConfig().propertyValueString("hibernate.connection.url");
      
    }
    
    String databaseUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("db." + databaseName + ".url");
    return databaseUrl;

  }

  /**
   * @return ldap server id url or a message that says not found
   */
  public String getSqlDatabaseNameUrlText() {
    
    String databaseNameUrl = this.getSqlDatabaseNameUrl();
    if (!StringUtils.isBlank(databaseNameUrl)) {
      return databaseNameUrl;
    }
    
    return TextContainer.retrieveFromRequest().getText().get("grouperLoaderDatabaseNameNotFound");

  }


  /**
   * 
   * @return scheduling priority
   */
  public String getSqlPriority() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String priority = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_PRIORITY);
    
    return priority;
    
  }
  
  /**
   * 
   * @return database name
   */
  public int getSqlPriorityInt() {
    
    String priority = this.getSqlPriority();
    
    if (!StringUtils.isBlank(priority)) {
      
      try {
        
        return GrouperUtil.intValue(priority);
        
      } catch (Exception e) {
        LOG.error("Cant parse priority: '" + priority + "'", e);
        return -200;
      }
      
    }
    
    return 5;
    
  }
  
  /**
   * 
   * @return scheduling priority
   */
  public String getLdapPriority() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapPriorityName());

  }
  
  /**
   * 
   * @return groups like
   */
  public String getLdapGroupsLike() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupsLikeName());

  }
  
  /**
   * 
   * @return extra attributes
   */
  public String getLdapExtraAttributes() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapExtraAttributesName());

  }

  
  /**
   * 
   * @return extra attributes
   */
  public String getLdapAttributeFilterExpression() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapAttributeFilterExpressionName());

  }

  /**
   * 
   * @return ldap group description expression
   */
  public String getLdapGroupDescriptionExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupDescriptionExpressionName());

  }

  /**
   * 
   * @return ldap group display name expression
   */
  public String getLdapGroupDisplayNameExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupDisplayNameExpressionName());

  }
  
  /**
   * 
   * @return ldap group name expression
   */
  public String getLdapGroupNameExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupNameExpressionName());

  }
  
  /**
   * 
   * @return ldap subject expression
   */
  public String getLdapSubjectExpression() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSubjectExpressionName());

  }
  
  /**
   * 
   * @return ldap group types
   */
  public String getLdapGroupTypes() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupTypesName());

  }
  
  /**
   * 
   * @return ldap readers
   */
  public String getLdapReaders() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapReadersName());

  }
  
  /**
   * 
   * @return ldap readers
   */
  public String getLdapAttrReaders() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupAttrReadersName());

  }
  
  /**
   * 
   * @return ldap viewers
   */
  public String getLdapViewers() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapViewersName());

  }
  
  
  
  /**
   * 
   * @return ldap viewers
   */
  public String getLdapAdmins() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapAdminsName());

  }
  
  /**
   * 
   * @return ldap updaters
   */
  public String getLdapUpdaters() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapUpdatersName());

  }
  
  /**
   * 
   * @return ldap attr updaters
   */
  public String getLdapAttrUpdaters() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupAttrUpdatersName());

  }
  
  /**
   * 
   * @return ldap optins
   */
  public String getLdapOptins() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapOptinsName());

  }
  
  /**
   * 
   * @return ldap optouts
   */
  public String getLdapOptouts() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapOptoutsName());

  }
  
  /**
   * 
   * @return database name
   */
  public int getLdapPriorityInt() {
    
    String priority = this.getLdapPriority();
    
    if (!StringUtils.isBlank(priority)) {
      
      try {
        
        return GrouperUtil.intValue(priority);
        
      } catch (Exception e) {
        LOG.error("Cant parse priority: '" + priority + "'", e);
        return -200;
      }
      
    }
    
    return 5;
    
  }
  
  /**
   * 
   * @return sql query
   */
  public String getSqlQuery() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderSqlQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUERY);
    
    return grouperLoaderSqlQuery;

  }

  /**
   * 
   * @return sql query
   */
  public String getSqlAndGroups() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderAndGroups = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_AND_GROUPS);
    return grouperLoaderAndGroups;

  }

  /**
   * 
   * @return list of gui groups
   */
  public List<GuiGroup> getSqlAndGuiGroups() {

    final List<String> andGroupsStringList = getSqlAndGroupsStringList();
    
    final List<GuiGroup> guiGroups = new ArrayList<GuiGroup>();
    
    if (GrouperUtil.length(andGroupsStringList) > 0) {

      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          for (String andGroupString : andGroupsStringList) {
            
            Group group = GroupFinder.findByUuid(grouperSession, andGroupString, false);
            group = group != null ? group : GroupFinder.findByName(grouperSession, andGroupString, false);
            guiGroups.add(new GuiGroup(group));
            
          }
          
          return null;
        }
      });
    }    
    return guiGroups;
  }

  /**
   * convert and groups to string
   * @return the list of strings
   */
  private List<String> getSqlAndGroupsStringList() {
    String andGroupsString = this.getSqlAndGroups();
    
    if (StringUtils.isBlank(andGroupsString)) {
      return null;
    }
    
    final List<String> andGroupsStringList = GrouperUtil.splitTrimToList(andGroupsString, ",");
    return andGroupsStringList;
  }

  /**
   * 
   * @return sql query
   */
  public String getLdapAndGroups() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapAndGroupsName());

  }

  /**
   * 
   * @return list of gui groups
   */
  public List<GuiGroup> getLdapAndGuiGroups() {

    final List<String> andGroupsStringList = getLdapAndGroupsStringList();

    final List<GuiGroup> guiGroups = new ArrayList<GuiGroup>();

    if (GrouperUtil.length(andGroupsStringList) > 0) {

      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          for (String andGroupString : andGroupsStringList) {
            
            Group group = GroupFinder.findByUuid(grouperSession, andGroupString, false);
            group = group != null ? group : GroupFinder.findByName(grouperSession, andGroupString, false);
            guiGroups.add(new GuiGroup(group));
            
          }
          
          return null;
        }
      });
    }
    
    return guiGroups;
  }

  /**
   * convert and groups to string
   * @return the list of strings
   */
  private List<String> getLdapAndGroupsStringList() {
    String andGroupsString = this.getLdapAndGroups();
    
    if (StringUtils.isBlank(andGroupsString)) {
      return null;
    }
    
    final List<String> andGroupsStringList = GrouperUtil.splitTrimToList(andGroupsString, ",");
    return andGroupsStringList;
  }


  /**
   * 
   * @return sql cron
   */
  public String getSqlCron() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderQuartzCron = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
    
    return grouperLoaderQuartzCron;

  }

  /**
   * 
   * @return ldap cron
   */
  public String getLdapCron() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapQuartzCronName());

  }

  /**
   * 
   * @return sql schedule type
   */
  public String getSqlScheduleType() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderScheduleType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE);
    
    return grouperLoaderScheduleType;

  }

  
  /**
   * 
   * @return sql loader type
   */
  public String getSqlLoaderType() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_TYPE);
    
    return grouperLoaderType;

  }

  /**
   * 
   */
  private AttributeAssign attributeAssign = null;
  
  /**
   * 
   * @return the grouper loader type
   */
  public GrouperLoaderType getGrouperLoaderType() {
    String grouperLoaderTypeString = null;
    
    if (this.isGrouperSqlLoader()) {
      grouperLoaderTypeString = this.getSqlLoaderType();
    } else if (this.isGrouperLdapLoader()) {
      grouperLoaderTypeString = this.getLdapLoaderType();
    }
    
    if (StringUtils.isBlank(grouperLoaderTypeString)) {
      return null;
    }
    
    GrouperLoaderType grouperLoaderType = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderTypeString, true);
    
    return grouperLoaderType;

  }

  /**
   * 
   * @return job name
   */
  public String getJobName() {
    GrouperLoaderType grouperLoaderType = this.getGrouperLoaderType();
    
    if (grouperLoaderType == null) {
      return null;
    }

    Group group = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();

    return grouperLoaderType.name() + "__" + group.getName() + "__" + group.getUuid();

  }
  
  /**
   * 
   * @return is SQL loader
   */
  public boolean isGrouperSqlLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasAttrDefNameGrouperLoader();
  }
  
  /**
   * 
   * @return is LDAP loader
   */
  public boolean isGrouperLdapLoader() {
    return GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().isHasAttrDefNameGrouperLoaderLdap();
  }
  
  /**
   * get the ldap attribute assign for this group
   * @return attribute assign
   */
  private AttributeAssign getLdapAttributeAssign() {
    
    if (this.attributeAssign == null) {

      Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
      
      AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);
      
      this.attributeAssign = jobGroup.getAttributeDelegate().retrieveAssignment(AttributeDef.ACTION_DEFAULT, attributeDefName, false, true);
      
    }
    
    return this.attributeAssign;

  }

  /**
   * 
   * @param nameOfAttributeDefName
   * @return the value of the attribute
   */
  private String retrieveLdapAttributeValue(String nameOfAttributeDefName) {
    AttributeAssign theAttributeAssign = this.getLdapAttributeAssign();
    if (theAttributeAssign == null) { 
      return null;
    }
    return theAttributeAssign.getAttributeValueDelegate().retrieveValueString(nameOfAttributeDefName);
  }
  
  /**
   * 
   * @return ldap loader type
   */
  public String getLdapLoaderType() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapTypeName());

  }

  /**
   * 
   * @return ldap server id
   */
  public String getLdapServerId() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapServerIdName());

  }

  /**
   * 
   * @return ldap filter
   */
  public String getLdapLoaderFilter() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapFilterName());

  }

  /**
   * 
   * @return ldap subject attribute name
   */
  public String getLdapSubjectAttributeName() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSubjectAttributeName());

  }

  /**
   * 
   * @return ldap group attribute name
   */
  public String getLdapGroupAttributeName() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapGroupAttributeName());

  }

  /**
   * 
   * @return ldap group attribute name
   */
  public String getLdapSearchDn() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSearchDnName());

  }
    
  /**
   * 
   * @return ldap server id
   */
  public String getLdapServerIdUrl() {
    
    String ldapServerId = this.getLdapServerId();

    if (StringUtils.isBlank(ldapServerId)) {
      return null;
    }
    String ldapUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".url");
    return ldapUrl;
  }

  /**
   * @return ldap server id url or a message that says not found
   */
  public String getLdapServerIdUrlText() {
    
    String ldapUrl = this.getLdapServerIdUrl();
    if (!StringUtils.isBlank(ldapUrl)) {
      return ldapUrl;
    }
    
    return TextContainer.retrieveFromRequest().getText().get("grouperLoaderLdapServerIdNotFound");

  }

  

  
  /**
   * 
   * @return sql schedule interval
   */
  public String getSqlScheduleInterval() {
    
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_INTERVAL_SECONDS);
    
    return grouperLoaderType;

  }

  /**
   * 
   * @return sql schedule interval
   */
  public String getSqlScheduleIntervalHumanReadable() {

    return GrouperUiUtils.convertSecondsToString(this.getSqlScheduleIntervalSecondsTotal());

  }

  
  
  /**
   * 
   * @return sql schedule interval seconds
   */
  public int getSqlScheduleIntervalSecondsTotal() {
    
    String interval = this.getSqlScheduleInterval();
    
    if (StringUtils.isBlank(interval)) {
      return -1;
    }

    try {
      int intervalInt = GrouperUtil.intValue(interval);
      return intervalInt;
    } catch (Exception e) {
      LOG.error("Cant parse interval: '" + interval + "'", e);
      return -2;
    }
  }

  /**
   * 
   * @return the sql cron description
   */
  public String getSqlCronDescription() {
    Group jobGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup().getGroup();
    String grouperLoaderQuartzCron = GrouperLoaderType.attributeValueOrDefaultOrNull(jobGroup, GrouperLoader.GROUPER_LOADER_QUARTZ_CRON);
    
    if (!StringUtils.isBlank(grouperLoaderQuartzCron)) {
      try {
        return CronExpressionDescriptor.getDescription(grouperLoaderQuartzCron);
      } catch (Exception e) {
        
        LOG.error("Cant parse cron string:" + grouperLoaderQuartzCron, e);
        
        return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError");
      }
    }
    return "";
  }
  
  /**
   * 
   * @return the sql cron description
   */
  public String getLdapCronDescription() {
    String grouperLoaderQuartzCron = this.getLdapCron();
    
    if (!StringUtils.isBlank(grouperLoaderQuartzCron)) {
      try {
        return CronExpressionDescriptor.getDescription(grouperLoaderQuartzCron);
      } catch (Exception e) {
        
        LOG.error("Cant parse cron string:" + grouperLoaderQuartzCron, e);
        
        return TextContainer.retrieveFromRequest().getText().get("grouperLoaderSqlCronDescriptionError");
      }
    }
    return "";
  }
  
  /**
   * 
   * @return source ID
   */
  public String getLdapSourceId() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSourceIdName());

  }
  
  /**
   * 
   * @return subject lookup type
   */
  public String getLdapSubjectLookupType() {
    
    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSubjectIdTypeName());

  }
  
  /**
   * 
   * @return ldap search scope
   */
  public String getLdapSearchScope() {

    return retrieveLdapAttributeValue(LoaderLdapUtils.grouperLoaderLdapSearchScopeName());

  }
  
  /**
   * 
   * @return if loader group
   */
  public boolean isLoaderGroup() {

    GuiGroup guiGroup = GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().getGuiGroup();

    if (guiGroup.isHasAttrDefNameGrouperLoader() || guiGroup.isHasAttrDefNameGrouperLoaderLdap()) {
      return true;
    }
    
    return false;
    
  }
  
  /**
   * show if grouper admin or loader group
   * @return true if shouldl show the loader menu item
   */
  public boolean isCanSeeLoader() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    String error = GrouperUiFilter.requireUiGroup("uiV2.loader.must.be.in.group", loggedInSubject);
    //null error means allow
    if (error == null) {
      return true;
    }
    
    if (GrouperUiConfig.retrieveConfig().propertyValueBoolean("uiV2.loaderTab.view.by.group.admins", true)) {
      if (GrouperRequestContainer.retrieveFromRequestOrCreate().getGroupContainer().isCanAdmin()) {
        return true;
      }
    }
    
    return false;
  }
  
}
