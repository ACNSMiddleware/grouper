/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author mchyzer
 * $Id: AttributeDefFinder.java,v 1.2 2009-09-28 20:30:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.finder;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * finder methods for attribute assign
 */
public class AttributeAssignFinder {

  /**
   * if should retrieve values
   */
  private boolean retrieveValues;
  
  /**
   * if should retrieve values
   * @param theRetrieveValues
   * @return this for chaining
   */
  public AttributeAssignFinder assignRetrieveValues(boolean theRetrieveValues) {
    this.retrieveValues = theRetrieveValues;
    return this;
  }
  
  /**
   * queryOptions for calls
   */
  private QueryOptions queryOptions;
  
  /**
   * query options paging and sorting
   * @param theQueryOptions
   * @return this for chaining
   */
  public AttributeAssignFinder assignQueryOptions(QueryOptions theQueryOptions) {
    this.queryOptions = theQueryOptions;
    return this;
  }
  
  /**
   * id of attribute def name that there is an assignment on assignment of with a value or values (optional)
   */
  private String idOfAttributeDefNameOnAssignment0;
  
  /**
   * id of attribute def name that there is an assignment on assignment of with a value or values (optional)
   * @param theIdOfAttributeDefNameOnAssignment0
   * @return this for chaining
   */
  public AttributeAssignFinder assignIdOfAttributeDefNameOnAssignment0(String theIdOfAttributeDefNameOnAssignment0) {
    this.idOfAttributeDefNameOnAssignment0 = theIdOfAttributeDefNameOnAssignment0;
    return this;
  }
  
  /**
   * values that the attribute def name on assignment of assignment has
   */
  private Set<Object> attributeValuesOnAssignment0;

  /**
   * values that the attribute def name on assignment of assignment has
   * @param theAttributeValuesOnAssignment0
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeValuesOnAssignment0(Set<Object> theAttributeValuesOnAssignment0) {
    this.attributeValuesOnAssignment0 = theAttributeValuesOnAssignment0;
    return this;
  }

  /**
   * second id of attribute def name that there is an assignment on assignment of with a value
   */
  private String idOfAttributeDefNameOnAssignment1;

  /**
   * id of second attribute def name that there is an assignment on assignment of with a value or values (optional)
   * @param theIdOfAttributeDefNameOnAssignment1
   * @return this for chaining
   */
  public AttributeAssignFinder assignIdOfAttributeDefNameOnAssignment1(String theIdOfAttributeDefNameOnAssignment1) {
    this.idOfAttributeDefNameOnAssignment1 = theIdOfAttributeDefNameOnAssignment1;
    return this;
  }

  /**
   * second values that the attribute def name on assignment of assignment has
   */
  private Set<Object> attributeValuesOnAssignment1;

  /**
   * values that the second attribute def name on assignment of assignment has
   * @param theAttributeValuesOnAssignment1
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeValuesOnAssignment1(Set<Object> theAttributeValuesOnAssignment1) {
    this.attributeValuesOnAssignment1 = theAttributeValuesOnAssignment1;
    return this;
  }

  /**
   * if check attribute read on owner if applicable
   */
  private Boolean checkAttributeReadOnOwner;
  
  /**
   * use security around attribute def?  default is true
   */
  private Boolean attributeCheckReadOnAttributeDef = null;
  
  /**
   * use security around attribute def?  default is true
   * @param theAttributeDefNameUseSecurity
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeCheckReadOnAttributeDef(boolean theAttributeDefNameUseSecurity) {
    this.attributeCheckReadOnAttributeDef = theAttributeDefNameUseSecurity;
    return this;
  }
  
  /**
   * query these attribute assign types (if querying by attribute)
   */
  private AttributeAssignType attributeAssignType;
  
  /**
   * assign the attribute assign type for querying by attribute
   * @param theAttributeAssignType
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeAssignType(AttributeAssignType theAttributeAssignType) {
    this.attributeAssignType = theAttributeAssignType;
    return this;
  }
  
  /**
   * attribute def names ids
   */
  private Collection<String> attributeDefNameIds;
  
  /**
   * attribute def ids
   */
  private Collection<String> attributeDefIds;
  
  /**
   * attribute def name id to find
   * @param attributeDefNameId
   * @return this for chaining
   */
  public AttributeAssignFinder addAttributeDefNameId(String attributeDefNameId) {
    if (this.attributeDefNameIds == null) {
      this.attributeDefNameIds = new LinkedHashSet<String>();
    }
    this.attributeDefNameIds.add(attributeDefNameId);
    return this;
  }
  
  /**
   * attribute def id to find
   * @param attributeDefId
   * @return this for chaining
   */
  public AttributeAssignFinder addAttributeDefId(String attributeDefId) {
    if (this.attributeDefIds == null) {
      this.attributeDefIds = new LinkedHashSet<String>();
    }
    this.attributeDefIds.add(attributeDefId);
    return this;
  }
  
  /**
   * attribute def name ids to find
   * @param theAttributeDefNameIds
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeDefNameIds(Collection<String> theAttributeDefNameIds) {
    this.attributeDefNameIds = theAttributeDefNameIds;
    return this;
  }
  
  /**
   * attribute def ids to find
   * @param theAttributeDefIds
   * @return this for chaining
   */
  public AttributeAssignFinder assignAttributeDefIds(Collection<String> theAttributeDefIds) {
    this.attributeDefIds = theAttributeDefIds;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerGroupIds;
  
  /**
   * add owner group id
   * @param ownerGroupId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerGroupId(String ownerGroupId) {
    if (this.ownerGroupIds == null) {
      this.ownerGroupIds = new LinkedHashSet<String>();
    }
    this.ownerGroupIds.add(ownerGroupId);
    return this;
  }
  
  /**
   * add owner group id
   * @param ownerGroupIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerGroupIds(Collection<String> ownerGroupIds1) {
    this.ownerGroupIds = ownerGroupIds1;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerAttributeAssignIds;
  
  /**
   * add owner assign id
   * @param ownerAttributeAssignId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerAttributeAssignId(String ownerAttributeAssignId) {
    if (this.ownerAttributeAssignIds == null) {
      this.ownerAttributeAssignIds = new LinkedHashSet<String>();
    }
    this.ownerAttributeAssignIds.add(ownerAttributeAssignId);
    return this;
  }
  
  /**
   * add owner assign id
   * @param ownerAttributeAssignIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerAttributeAssignIds(Collection<String> ownerAttributeAssignIds1) {
    this.ownerAttributeAssignIds = ownerAttributeAssignIds1;
    return this;
  }
  
  /**
   * 
   */
  private Collection<String> ownerStemIds;
  
  /**
   * add owner stem id
   * @param ownerStemId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerStemId(String ownerStemId) {
    if (this.ownerStemIds == null) {
      this.ownerStemIds = new LinkedHashSet<String>();
    }
    this.ownerStemIds.add(ownerStemId);
    return this;
  }
  
  /**
   * add owner stem id
   * @param ownerStemIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerStemIds(Collection<String> ownerStemIds1) {
    this.ownerStemIds = ownerStemIds1;
    return this;
  }
  
  /**
   * if assignments on assignments should also be included
   */
  private boolean includeAssignmentsOnAssignments = false;
  /**
   * 
   */
  private Collection<String> ownerAttributeDefIds;
  
  /**
   * if assignments on assignments should also be included
   * @param theIncludeAssignAssignmentsOnAssignments
   * @return this for chaining
   */
  public AttributeAssignFinder assignIncludeAssignmentsOnAssignments(boolean theIncludeAssignAssignmentsOnAssignments) {
    this.includeAssignmentsOnAssignments = theIncludeAssignAssignmentsOnAssignments;
    return this;
  }

  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public AttributeAssignFinderResults findAttributeAssignFinderResults() {

    if (GrouperUtil.length(this.ownerGroupIds) > 0 || GrouperUtil.length(this.ownerStemIds) > 0
      || GrouperUtil.length(this.ownerAttributeDefIds) > 0 
      || GrouperUtil.length(this.ownerAttributeAssignIds) > 0 || this.attributeAssignType == null) {
      throw new RuntimeException("Invalid Query");
    }
    
    if (this.attributeAssignType == AttributeAssignType.group) {
      AttributeAssignFinderResults attributeAssignFinderResults = new AttributeAssignFinderResults();
      
      Set<Object[]> results = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignmentsByAttribute(this.attributeDefIds, attributeDefNameIds, 
          null, true, this.checkAttributeReadOnOwner, this.queryOptions, this.retrieveValues);
      
            
      attributeAssignFinderResults.setResultObjects(results);
      
      return attributeAssignFinderResults;
    }
    
    throw new RuntimeException("Not supported");
    
  }
  
  /**
   * find all the attribute assigns
   * @return the set of groups or the empty set if none found
   */
  public Set<AttributeAssign> findAttributeAssigns() {
  
    if (this.retrieveValues) {
      throw new RuntimeException("retrieveValues not supported in this call");
    }

    if (this.queryOptions != null) {
      throw new RuntimeException("queryOptions not supported in this call");
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.emptySetOfLookupsReturnsNoResults", true)) {
  
      // if passed in empty set of group ids and no names, then no groups found
      if (this.ownerGroupIds != null && this.ownerGroupIds.size() == 0) {
        return new HashSet<AttributeAssign>();
      }
      
    }

    int ownerCount = 0;
    
    if (GrouperUtil.length(this.ownerGroupIds) > 0) {
      ownerCount++;
    }
    if (GrouperUtil.length(this.ownerStemIds) > 0) {
      ownerCount++;
    }
    if (GrouperUtil.length(this.ownerAttributeDefIds) > 0) {
      ownerCount++;
    }
    if (GrouperUtil.length(this.ownerAttributeAssignIds) > 0) {
      ownerCount++;
    }
    if (this.attributeAssignType != null) {
      ownerCount++;
    }
    if (ownerCount > 1) {
      throw new RuntimeException("Can only pass one type of owner: groups, stems, attributeDefs, attributeAssigns, attributeAssignType, but has " + ownerCount + " types");
    }

    if (this.ownerGroupIds != null) {
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, true);
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findGroupAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerGroupIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef, 
              this.idOfAttributeDefNameOnAssignment0, this.attributeValuesOnAssignment0, this.idOfAttributeDefNameOnAssignment1, this.attributeValuesOnAssignment1);

    }
    
    if (this.ownerStemIds != null) {
      
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, true);
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findStemAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerStemIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef,
              this.idOfAttributeDefNameOnAssignment0, this.attributeValuesOnAssignment0, this.idOfAttributeDefNameOnAssignment1, this.attributeValuesOnAssignment1);

    }
    
    if (this.ownerAttributeDefIds != null) {
      
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, true);
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findAttributeDefAttributeAssignments(null, null, this.attributeDefNameIds, this.ownerAttributeDefIds, null, true, 
              this.includeAssignmentsOnAssignments, null, null, null, this.attributeCheckReadOnAttributeDef,
              this.idOfAttributeDefNameOnAssignment0, this.attributeValuesOnAssignment0, this.idOfAttributeDefNameOnAssignment1, this.attributeValuesOnAssignment1);

    }
    
    if (this.ownerAttributeAssignIds != null) {
      
      this.attributeCheckReadOnAttributeDef = GrouperUtil.booleanValue(this.attributeCheckReadOnAttributeDef, false);
      if (this.attributeCheckReadOnAttributeDef) {
        throw new RuntimeException("Invalid query: attributeCheckReadOnAttributeDef");
      }

      if (this.includeAssignmentsOnAssignments) {
        throw new RuntimeException("Invalid query: includeAssignmentsOnAssignments");
      }
      
      if (GrouperUtil.length(this.attributeDefNameIds) > 0) {
        throw new RuntimeException("Invalid query: attributeDefNameIds");
      }
      
      return GrouperDAOFactory.getFactory().getAttributeAssign()
          .findAssignmentsFromAssignmentsByIds(this.attributeDefNameIds, null, null, true);

    }
    
    if (this.attributeAssignType != null) {
      if (GrouperUtil.length(this.attributeDefNameIds) == 0 ) {
        throw new RuntimeException("You need to pass in attributeDefNameIds if you are querying by attribute");
      }
      throw new RuntimeException("This query is not yet supported");
    }
    
    throw new RuntimeException("Bad query");
  }

  /**
   * add owner AttributeDef id
   * @param ownerAttributeDefId
   * @return this for chaining
   */
  public AttributeAssignFinder addOwnerAttributeDefId(String ownerAttributeDefId) {
    if (this.ownerAttributeDefIds == null) {
      this.ownerAttributeDefIds = new LinkedHashSet<String>();
    }
    this.ownerAttributeDefIds.add(ownerAttributeDefId);
    return this;
  }

  /**
   * add owner AttributeDef id
   * @param ownerAttributeDefIds1
   * @return this for chaining
   */
  public AttributeAssignFinder assignOwnerAttributeDefIds(Collection<String> ownerAttributeDefIds1) {
    this.ownerAttributeDefIds = ownerAttributeDefIds1;
    return this;
  }

  /**
   * find an attributeAssign by id.  This is a secure method, a GrouperSession must be open
   * @param id of attributeAssign
   * @param exceptionIfNull true if exception should be thrown if null
   * @return the attribute assign or null
   * @throws AttributeAssignNotFoundException
   */
  public static AttributeAssign findById(String id, boolean exceptionIfNull) {
    
    AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(id, exceptionIfNull);
    
    //at this point no exception should be thrown
    if (attributeAssign == null) {
      return null;
    }
    
    //now we need to check security
    if (PrivilegeHelper.canViewAttributeAssign(GrouperSession.staticGrouperSession(), attributeAssign, true)) {
      return attributeAssign;
    }
    if (exceptionIfNull) {
      throw new AttributeAssignNotFoundException("Not allowed to view attribute assign by id: " + id);
    }
    return null;
  }  

}
