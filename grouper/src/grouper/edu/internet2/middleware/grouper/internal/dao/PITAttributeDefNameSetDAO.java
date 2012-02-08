/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;

/**
 * 
 */
public interface PITAttributeDefNameSetDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitAttributeDefNameSet
   */
  public void saveOrUpdate(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * insert or update
   * @param pitAttributeDefNameSets
   */
  public void saveOrUpdate(Set<PITAttributeDefNameSet> pitAttributeDefNameSets);
  
  /**
   * delete
   * @param pitAttributeDefNameSet
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findById(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * @param pitAttributeDefNameSet
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findImmediateChildren(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * @param id
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findAllSelfPITAttributeDefNameSetsByPITAttributeDefNameId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByPITAttributeDefNameId(String id);
  
  /**
   * @param id
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findByThenHasPITAttributeDefNameId(String id);
  
  
  /**
   * @return active attribute def name sets that are missing in point in time
   */
  public Set<AttributeDefNameSet> findMissingActivePITAttributeDefNameSets();
  
  /**
   * @return active point in time attribute def name sets that should be inactive
   */
  public Set<PITAttributeDefNameSet> findMissingInactivePITAttributeDefNameSets();
}
