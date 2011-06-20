package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignActionDAO extends Hib3DAO implements PITAttributeAssignActionDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignActionDAO.class.getName();

  /**
   *  @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignAction)
   */
  public void saveOrUpdate(PITAttributeAssignAction pitAttributeAssignAction) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignAction);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeAssignAction> pitAttributeAssignActions) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignActions);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignAction)
   */
  public void delete(PITAttributeAssignAction pitAttributeAssignAction) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignAction);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignAction where id not in (select action.id from AttributeAssignAction as action)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findById(java.lang.String)
   */
  public PITAttributeAssignAction findById(String id) {
    PITAttributeAssignAction pitAttributeAssignAction = HibernateSession
      .byHqlStatic()
      .createQuery("select action from PITAttributeAssignAction as action where action.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignAction.class);
    
    return pitAttributeAssignAction;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssignAction where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findByAttributeDefId(java.lang.String)
   */
  public Set<PITAttributeAssignAction> findByAttributeDefId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select action from PITAttributeAssignAction as action where action.attributeDefId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByAttributeDefId")
        .setString("id", id)
        .listSet(PITAttributeAssignAction.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findMissingActivePITAttributeAssignActions()
   */
  public Set<AttributeAssignAction> findMissingActivePITAttributeAssignActions() {

    Set<AttributeAssignAction> actions = HibernateSession
      .byHqlStatic()
      .createQuery("select a from AttributeAssignAction a where " +
          "not exists (select 1 from PITAttributeAssignAction pit where a.id = pit.id and (a.nameDb = pit.nameDb or (a.nameDb is null and pit.nameDb is null))) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = a.id " +
          "    and type.actionName='addAttributeAssignAction' and type.changeLogCategory='attributeAssignAction' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = a.id " +
          "    and type.actionName='updateAttributeAssignAction' and type.changeLogCategory='attributeAssignAction' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeAssignActions")
      .listSet(AttributeAssignAction.class);
    
    return actions;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findMissingInactivePITAttributeAssignActions()
   */
  public Set<PITAttributeAssignAction> findMissingInactivePITAttributeAssignActions() {

    Set<PITAttributeAssignAction> actions = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignAction pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeAssignAction a where a.id = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteAttributeAssignAction' and type.changeLogCategory='attributeAssignAction' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssignActions")
      .listSet(PITAttributeAssignAction.class);
    
    return actions;
  }
}
