/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

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
   * delete
   * @param pitAttributeDefNameSet
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet);
  
  /**
   * @param id
   * @return PITAttributeDefNameSet
   */
  public PITAttributeDefNameSet findById(String id);
  
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
  public Set<PITAttributeDefNameSet> findAllSelfAttributeDefNameSetsByAttributeDefNameId(String id);
  
  /**
   * @param id
   */
  public void deleteSelfByAttributeDefNameId(String id);
  
  /**
   * @param id
   * @return pit attribute def name sets
   */
  public Set<PITAttributeDefNameSet> findByThenHasAttributeDefNameId(String id);
}
