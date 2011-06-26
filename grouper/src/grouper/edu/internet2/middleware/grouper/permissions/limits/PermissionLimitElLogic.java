package edu.internet2.middleware.grouper.permissions.limits;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in EL limit
 * @author mchyzer
 */
public class PermissionLimitElLogic extends PermissionLimitBase {

  /** if you are testing this, set it, otherwise, it will default */
  static Integer testingCacheMinutesInt = null;
  
  /** count how many times called logic for testing the cache */
  static int testingTimesCalledLogic = 0;

  /**
   * @see PermissionLimitInterface#cacheLimitValueResultMinutes()
   */
  @Override
  public int cacheLimitValueResultMinutes() {
    return testingCacheMinutesInt == null ? super.cacheLimitValueResultMinutes() : testingCacheMinutesInt;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(PermissionLimitElLogic.class);

  /**
   * @see PermissionLimitInterface#allowPermission(PermissionEntry, AttributeAssign, Set, Map)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars) {
    
    testingTimesCalledLogic++;
    
    boolean foundError = false;
    String result = null;
    RuntimeException theException = null;
    try {
    
      String attributeDefNameName = limitAssignment.getAttributeDefName().getName();
      
      String expression = null;
      if (GrouperUtil.length(limitAssignmentValues) == 1) {
        expression = limitAssignmentValues.iterator().next().getValueString();
        expression = StringUtils.trimToEmpty(expression);
      }
      
      //this should have 1 string value
      if (GrouperUtil.length(limitAssignmentValues) != 1 || StringUtils.isBlank(expression)) {
        throw new RuntimeException(attributeDefNameName + " must have 1 string value");
      }
      
      //add the curlies around if it not already there
      if (!expression.startsWith("${") && !expression.endsWith("}")) {
        expression = "${" + expression + "}";
      }
      
      limitEnvVars.put("limitElUtils", new LimitElUtils());
  
      limitEnvVars.put("limitAssignmentId", limitAssignment.getId());
      limitEnvVars.put("permissionAction", permissionEntry.getAction());
      limitEnvVars.put("permissionMemberId", permissionEntry.getMemberId());
      limitEnvVars.put("permissionRoleId", permissionEntry.getRoleId());
      limitEnvVars.put("permissionRoleName", permissionEntry.getRoleName());
      limitEnvVars.put("permissionAttributeDefNameId", permissionEntry.getAttributeDefNameId());
      limitEnvVars.put("permissionAttributeDefNameName", permissionEntry.getAttributeDefNameName());
  
      //get custom el classes to add
      Map<String, Object> customElClasses = PermissionLimitUtils.limitElClasses();
      
      limitEnvVars.putAll(GrouperUtil.nonNull(customElClasses));
      
      result = GrouperUtil.substituteExpressionLanguage(expression, limitEnvVars);
      
      return GrouperUtil.booleanObjectValue(result);
    } catch (RuntimeException re) {
      foundError = true;
      throw re;
    } finally {
      
      if (foundError || LOG.isDebugEnabled()) {
        try {
          StringBuilder logMessage = new StringBuilder();
          if (logMessage != null) {
            logMessage.append(", EL variables: ");
            for (String varName : GrouperUtil.nonNull(limitEnvVars).keySet()) {
              logMessage.append(varName);
              Object value = limitEnvVars.get(varName);
              if (value instanceof String || value instanceof Number || value instanceof Date || value == null) {
                logMessage.append("(").append(value).append(")");
              } else {
                logMessage.append("(type: ").append(value.getClass()).append(")");
              }
              logMessage.append(",");
            }
          }
          
          if (!foundError) {
            logMessage.append(", elResult: ").append(result);
          }
          
          if (foundError) {
            LOG.error(logMessage.toString(), theException);
          } else {
            LOG.debug(logMessage.toString());
          }
          
        } catch (RuntimeException re2) {
          LOG.error("loggingError", re2);
          LOG.error("originalException", theException);
        }
      }
      
      
      
    }
     
  }

  /**
   * @see PermissionLimitInterface#documentationKey()
   */
  public String documentationKey() {
    return "grouperPermissionExpressionLanguage.doc";
  }

  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign)
   */
  public String validateLimitAssignValue(AttributeAssign limitAssign) {
    String value = limitAssign.getValueDelegate().retrieveValueString();
    
    if (StringUtils.isBlank(value)) {
      return "grouperPermissionExpressionLanguage.required";
    }
    
    return null;
  }

}
