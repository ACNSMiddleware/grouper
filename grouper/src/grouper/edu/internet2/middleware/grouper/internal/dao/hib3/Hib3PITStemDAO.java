package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITStemDAO;
import edu.internet2.middleware.grouper.pit.PITStem;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITStemDAO extends Hib3DAO implements PITStemDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITStemDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void saveOrUpdate(PITStem pitStem) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitStem);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#delete(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void delete(PITStem pitStem) {
    HibernateSession.byObjectStatic().delete(pitStem);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<PITStem> pitStems) {
    HibernateSession.byObjectStatic().saveBatch(pitStems);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys    
    hibernateSession.byHql().createQuery("update PITStem set parentStemId = null where id not in (select s.uuid from Stem as s)").executeUpdate();

    hibernateSession.byHql().createQuery("delete from PITStem where id not in (select s.uuid from Stem as s)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findById(java.lang.String)
   */
  public PITStem findById(String pitStemId) {
    PITStem pitStem = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitStemId)
      .uniqueResult(PITStem.class);
    
    return pitStem;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys    
    HibernateSession.byHqlStatic()
      .createQuery("update PITStem set parentStemId = null where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
      
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITStem where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findByParentStemId(java.lang.String)
   */
  public Set<PITStem> findByParentStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitStem from PITStem as pitStem where pitStem.parentStemId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByParentStemId")
        .setString("id", id)
        .listSet(PITStem.class);
  }

  public Set<PITStem> findByName(String stemName, boolean orderByStartTime) {
    String sql = "select pitStem from PITStem as pitStem where pitStem.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITStem> pitStems = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", stemName)
      .listSet(PITStem.class);
    
    return pitStems;
  }
}

