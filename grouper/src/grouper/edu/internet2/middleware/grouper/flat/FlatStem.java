package edu.internet2.middleware.grouper.flat;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen 
 * $Id$
 */
@SuppressWarnings("serial")
public class FlatStem extends GrouperAPI implements Hib3GrouperVersioned {
  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** stem id foreign key in grouper_stems table */
  public static final String COLUMN_STEM_ID = "stem_id";
  
  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: stemId */
  public static final String FIELD_STEM_ID = "stemId";


  /**
   * fields which are included in db version
   */
  /*
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_ID, FIELD_STEM_ID);
  */
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, 
      FIELD_STEM_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  

  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_FLAT_STEMS = "grouper_flat_stems";

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** stem id foreign key in grouper_stems table*/
  private String stemId;
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }
  
  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return id
   */
  public String getId() {
    return id;
  }
  
  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
    this.stemId = id;
  }

  /**
   * @return stem id foreign key in grouper_stems table
   */
  public String getStemId() {
    return stemId;
  }

  /**
   * Set stem id foreign key in grouper_stems table
   * @param stemId
   */
  public void setStemId(String stemId) {
    this.stemId = stemId;
  }
  
  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getFlatStem().saveOrUpdate(this);
  }
  
  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getFlatStem().delete(this);
  }
}
