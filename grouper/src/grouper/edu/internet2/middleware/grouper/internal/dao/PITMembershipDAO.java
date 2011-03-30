/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITMembership;

/**
 * 
 */
public interface PITMembershipDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitMembership
   */
  public void saveOrUpdate(PITMembership pitMembership);
  
  /**
   * delete
   * @param pitMembership
   */
  public void delete(PITMembership pitMembership);
  
  /**
   * @param pitMembershipId
   * @return pit membership
   */
  public PITMembership findById(String pitMembershipId);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateId(String oldId, String newId);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
  /**
   * Get memberships by owner.
   * @param ownerId
   * @return set of pit memberships
   */
  public Set<PITMembership> findAllByOwner(String ownerId);
}
