package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitInterface;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic for the built in amount less than some number
 * @author mchyzer
 */
public class PermissionLimitAmountLessThanEquals extends PermissionLimitBase {

  /**
   * @see PermissionLimitInterface#allowPermission(PermissionEntry, AttributeAssign, Set, Map, Set)
   */
  public boolean allowPermission(PermissionEntry permissionEntry,
      AttributeAssign limitAssignment, Set<AttributeAssignValue> limitAssignmentValues,
      Map<String, Object> limitEnvVars, Set<PermissionLimitBean> permissionLimitBeans) {
    
    return PermissionLimitAmountLessThan.amountLimitHelper(limitAssignment, limitAssignmentValues, limitEnvVars, true);
    
  }

  /**
   * @see PermissionLimitInterface#documentationKey()
   */
  public String documentationKey() {
    return "grouperPermissionAmountLessThanEquals.doc";
  }

  /**
   * @see PermissionLimitInterface#validateLimitAssignValue(AttributeAssign, Set)
   */
  public String validateLimitAssignValue(AttributeAssign limitAssign, Set<AttributeAssignValue> limitAssignmentValues) {
    
    //lets see what values we have...
    Long value = null;
    
    if (GrouperUtil.length(limitAssignmentValues) == 1) {
      value = limitAssignmentValues.iterator().next().getValueInteger();
    }
    
    if (value == null) {
      return "grouperPermissionAmountLessThanEquals.required";
    }
    
    return null;
  
  }

}
