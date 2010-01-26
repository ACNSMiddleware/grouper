/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeDefScopeTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefScopeTest("testXmlDifferentUpdateProperties"));
  }
  
  /**
   * 
   */
  public AttributeDefScopeTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefScopeTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }
  
  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setId(GrouperUuid.getUuid());
    attributeDefScope.setAttributeDefId(attributeDef.getId());
    attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.nameLike);
    attributeDefScope.setScopeString("top:%");
    attributeDefScope.saveOrUpdate();
    
  }

  /**
   * make an example AttributeDefScope for testing
   * @return an example AttributeDefScope
   */
  public static AttributeDefScope exampleAttributeDefScope() {
    AttributeDefScope attributeDefScope = new AttributeDefScope();
    attributeDefScope.setAttributeDefId("attributeDefId");
    attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
    attributeDefScope.setContextId("contextId");
    attributeDefScope.setCreatedOnDb(new Long(4L));
    attributeDefScope.setHibernateVersionNumber(3L);
    attributeDefScope.setLastUpdatedDb(new Long(7L));
    attributeDefScope.setId("id");
    attributeDefScope.setScopeString("scopeString");
    attributeDefScope.setScopeString2("scopeString2");
    return attributeDefScope;
  }
  
  /**
   * make an example AttributeDefScope from db for testing
   * @return an example AttributeDefScope
   */
  public static AttributeDefScope exampleAttributeDefScopeDb() {
    AttributeDef attributeDef = AttributeDefTest.exampleAttributeDefDb("test", "attributeDefScope");
    
    Set<AttributeDefScope> attributeDefScopes = attributeDef
      .getAttributeDefScopeDelegate().retrieveAttributeDefScopes();
    
    if (GrouperUtil.length(attributeDefScopes) > 0) {
      return attributeDefScopes.iterator().next();
    }
    
    AttributeDefScope attributeDefScope = attributeDef.getAttributeDefScopeDelegate()
      .newScope(AttributeDefScopeType.attributeDefNameIdAssigned, "scopeString", null);
    attributeDefScope.saveOrUpdate();
    return attributeDefScope;
  }

  
  /**
   * retrieve example AttributeDefScope from db for testing
   * @return an example AttributeDefScope
   */
  public static AttributeDefScope exampleRetrieveAttributeDefScopeDb() {
    AttributeDef attributeDef =  AttributeDefFinder.findByName("test:attributeDefScope", true);
    AttributeDefScope attributeDefScope = GrouperDAOFactory.getFactory().getAttributeDefScope()
      .findByUuidOrKey(null, null, attributeDef.getId(), AttributeDefScopeType.attributeDefNameIdAssigned.name(), true, null);
    return attributeDefScope;
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testRetrieveMultiple() {
    
    AttributeDef attributeDef = AttributeDefTest.exampleAttributeDefDb("test", "attributeDefScopeMultiple");
    
    AttributeDefScope attributeDefScope1 = attributeDef.getAttributeDefScopeDelegate()
      .newScope(AttributeDefScopeType.attributeDefNameIdAssigned, "scopeString1", null);
    attributeDefScope1.saveOrUpdate();

    AttributeDefScope attributeDefScope2 = attributeDef.getAttributeDefScopeDelegate()
      .newScope(AttributeDefScopeType.attributeDefNameIdAssigned, "scopeString2", null);
    attributeDefScope2.saveOrUpdate();

    AttributeDefScope attributeDefScope3 = attributeDef.getAttributeDefScopeDelegate()
      .newScope(AttributeDefScopeType.inStem, "scopeString1", null);
    attributeDefScope3.saveOrUpdate();
    
    //get by id
    AttributeDefScope attributeDefScope = attributeDefScope1.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeDefScope1.getId(), attributeDefScope.getId());

    attributeDefScope = attributeDefScope2.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeDefScope2.getId(), attributeDefScope.getId());

    attributeDefScope = attributeDefScope3.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeDefScope3.getId(), attributeDefScope.getId());

    AttributeDefScope attributeDefScope4 = new AttributeDefScope();
    attributeDefScope4.setId("abc");
    attributeDefScope4.setAttributeDefScopeType(AttributeDefScopeType.attributeDefNameIdAssigned);
    attributeDefScope4.setScopeString("scopeString1");
    attributeDefScope4.setAttributeDefId(attributeDef.getId());

    attributeDefScope = attributeDefScope4.xmlRetrieveByIdOrKey(null);
    assertEquals(attributeDefScope1.getId(), attributeDefScope.getId());
    
    
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Group groupOriginal = new GroupSave(GrouperSession.staticGrouperSession()).assignGroupNameToEdit("test:groupInsert")
      .assignName("test:groupInsert").assignCreateParentStemsIfNotExist(true).save();
    
    //not sure why I need to sleep, but the last membership update gets messed up...
    GrouperUtil.sleep(1000);
    
    //do this because last membership update isnt there, only in db
    groupOriginal = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    Group groupCopy = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    Group groupCopy2 = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    groupCopy.delete();
    
    //lets insert the original
    groupCopy2.xmlSaveBusinessProperties(null);
    groupCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    groupCopy = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupOriginal.getUuid(), true, null);
    
    assertFalse(groupCopy == groupOriginal);
    assertFalse(groupCopy.xmlDifferentBusinessProperties(groupOriginal));
    assertFalse(groupCopy.xmlDifferentUpdateProperties(groupOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDefScope attributeDefScope = null;
    AttributeDefScope exampleAttributeDefScope = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      attributeDefScope.setContextId("abc");
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertTrue(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setContextId(exampleAttributeDefScope.getContextId());
      attributeDefScope.xmlSaveUpdateProperties();

      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
      
    }
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setCreatedOnDb(99L);
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertTrue(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setCreatedOnDb(exampleAttributeDefScope.getCreatedOnDb());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    }
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setLastUpdatedDb(99L);
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertTrue(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setLastUpdatedDb(exampleAttributeDefScope.getLastUpdatedDb());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

    }

    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setHibernateVersionNumber(99L);
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertTrue(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setHibernateVersionNumber(exampleAttributeDefScope.getHibernateVersionNumber());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setAttributeDefId("abc");
      
      assertTrue(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setAttributeDefId(exampleAttributeDefScope.getAttributeDefId());
      attributeDefScope.xmlSaveBusinessProperties(exampleAttributeDefScope.clone());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    
    }
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setAttributeDefScopeType(AttributeDefScopeType.inStem);
      
      assertTrue(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setAttributeDefScopeType(exampleAttributeDefScope.getAttributeDefScopeType());
      attributeDefScope.xmlSaveBusinessProperties(exampleAttributeDefScope.clone());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    
    }
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setId("abc");
      
      assertTrue(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setId(exampleAttributeDefScope.getId());
      attributeDefScope.xmlSaveBusinessProperties(exampleAttributeDefScope.clone());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    
    }
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setScopeString("abc");
      
      assertTrue(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setScopeString(exampleAttributeDefScope.getScopeString());
      attributeDefScope.xmlSaveBusinessProperties(exampleAttributeDefScope.clone());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    
    }
    
    {
      attributeDefScope = exampleAttributeDefScopeDb();
      exampleAttributeDefScope = exampleRetrieveAttributeDefScopeDb();

      attributeDefScope.setScopeString2("abc");
      
      assertTrue(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));

      attributeDefScope.setScopeString2(exampleAttributeDefScope.getScopeString2());
      attributeDefScope.xmlSaveBusinessProperties(exampleAttributeDefScope.clone());
      attributeDefScope.xmlSaveUpdateProperties();
      
      attributeDefScope = exampleRetrieveAttributeDefScopeDb();
      
      assertFalse(attributeDefScope.xmlDifferentBusinessProperties(exampleAttributeDefScope));
      assertFalse(attributeDefScope.xmlDifferentUpdateProperties(exampleAttributeDefScope));
    
    }
    
  }


  
}
