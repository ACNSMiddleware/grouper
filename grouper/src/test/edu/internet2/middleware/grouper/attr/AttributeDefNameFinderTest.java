/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeDefNameFinderTest extends GrouperTest {

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameFinderTest("testFinder"));
  }
  
  /**
   * 
   */
  public AttributeDefNameFinderTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefNameFinderTest(String name) {
    super(name);
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testFinder() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDef attributeDef1 = new AttributeDefSave(grouperSession).assignName("test:attributeDef1")
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
      .assignToGroup(true).save();
    
    AttributeDef attributeDef2 = new AttributeDefSave(grouperSession).assignName("test:attributeDef2")
      .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
      .assignToGroup(true).save();
  
    AttributeDef serviceDef1 = new AttributeDefSave(grouperSession).assignName("test:serviceDef1")
      .assignAttributeDefType(AttributeDefType.service).assignCreateParentStemsIfNotExist(true)
      .assignToGroup(true).save();

    AttributeDefName serviceDefName1_1_7 = new AttributeDefNameSave(grouperSession, serviceDef1)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:serviceDefName1_7").save();
    
    AttributeDefName serviceDefName1_1_8 = new AttributeDefNameSave(grouperSession, serviceDef1)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:serviceDefName1_8").save();
    
    AttributeDefName serviceDefName1_1_9 = new AttributeDefNameSave(grouperSession, serviceDef1)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:serviceDefName1_9").save();
  
    
    AttributeDefName attributeDefName1_1_1 = new AttributeDefNameSave(grouperSession, attributeDef1)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:attributeDefName1_1").save();
    AttributeDefName attributeDefName1_1_2 = new AttributeDefNameSave(grouperSession, attributeDef1)
    .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:attributeDefName1_2").save();
    
    AttributeDefName attributeDefName1_2_1 = new AttributeDefNameSave(grouperSession, attributeDef2)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:attributeDefName2_1").save();
    AttributeDefName attributeDefName1_2_2 = new AttributeDefNameSave(grouperSession, attributeDef2)
      .assignCreateParentStemsIfNotExist(true)
      .assignName("test1:attributeDefName2_2").save();
  
    attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef1.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);

    //subj7 is a member of service 7
    Group someGroup1 = new GroupSave(grouperSession).assignName("test1:someGroup1")
      .assignCreateParentStemsIfNotExist(true).save();
  
    someGroup1.getAttributeDelegate().assignAttribute(serviceDefName1_1_7);
    
    someGroup1.addMember(SubjectTestHelper.SUBJ7);
    
    //subj8 is an admin of service 8
    Group someGroup2 = new GroupSave(grouperSession).assignName("test1:someGroup2")
      .assignCreateParentStemsIfNotExist(true).save();

    someGroup2.grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.ADMIN);

    someGroup2.getAttributeDelegate().assignAttribute(serviceDefName1_1_8);
    
    //grouperAll is a reader of service 9
    Group someGroup3 = new GroupSave(grouperSession).assignName("test1:someGroup3")
      .assignCreateParentStemsIfNotExist(true).save();
  
    someGroup3.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);
  
    someGroup3.getAttributeDelegate().assignAttribute(serviceDefName1_1_9);
    
    Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder()
      .addPrivilege(AttributeDefPrivilege.ATTR_ADMIN).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(0, attributeDefNames.size());

    attributeDefNames = new AttributeDefNameFinder().assignScope("test")
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 2, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_1));
    assertTrue(attributeDefNames.contains(attributeDefName1_1_2));
    
    //this will have 1_1 in there some where
    attributeDefNames = new AttributeDefNameFinder().assignScope("%1_1")
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_1));

    //starts with 1_1 attr
    attributeDefNames = new AttributeDefNameFinder().assignScope("1_1 attr")
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 0, attributeDefNames.size());
    
    //starts with 1_1 attr split scope
    attributeDefNames = new AttributeDefNameFinder().assignScope("1_1 ATtr").assignSplitScope(true)
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_1));

    //starts with 1_1 attr split scope attribute assign type correct
    attributeDefNames = new AttributeDefNameFinder().assignScope("1_1 ATtr").assignSplitScope(true)
      .assignAttributeAssignType(AttributeAssignType.group)
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_1));

    //starts with 1_1 attr split scope attribute assign type wrong
    attributeDefNames = new AttributeDefNameFinder().assignScope("1_1 ATtr").assignSplitScope(true)
      .assignAttributeAssignType(AttributeAssignType.any_mem)
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 0, attributeDefNames.size());
    
    //starts with 1_1 attr split scope attribute wrong attribute def 
    attributeDefNames = new AttributeDefNameFinder().assignScope("1_1 ATtr").assignSplitScope(true)
      .assignAttributeDefId(attributeDef2.getId())
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 0, attributeDefNames.size());
    
    //starts with 1_1 attr split scope attribute assign type correct
    attributeDefNames = new AttributeDefNameFinder().assignScope("1_1 ATtr").assignSplitScope(true)
      .assignAttributeDefId(attributeDef1.getId())
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_1));

    //test paging
    attributeDefNames = new AttributeDefNameFinder().assignScope("test")
      .assignQueryOptions(QueryOptions.create(null, null, 1, 1))
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_1));

    //test sorting
    attributeDefNames = new AttributeDefNameFinder().assignScope("test")
      .assignQueryOptions(QueryOptions.create("extension", false, 1, 1))
      .addPrivilege(AttributeDefPrivilege.ATTR_UPDATE).assignSubject(SubjectTestHelper.SUBJ0).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(attributeDefName1_1_2));

    //which services can the user see?  subj6 can see the public one... service 9
    attributeDefNames = new AttributeDefNameFinder().assignScope("test1:").assignSubject(SubjectTestHelper.SUBJ6)
      .assignFilterByServicesCanView(true).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 1, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(serviceDefName1_1_9));
    
    //which services can the user see?  subj7 can see the public one... service 9, and 7
    attributeDefNames = new AttributeDefNameFinder().assignScope("test1:").assignSubject(SubjectTestHelper.SUBJ7)
      .assignFilterByServicesCanView(true).findAttributeNames();
    
    assertEquals(GrouperUtil.toStringForLog(attributeDefNames), 2, attributeDefNames.size());
    assertTrue(attributeDefNames.contains(serviceDefName1_1_9));
    assertTrue(attributeDefNames.contains(serviceDefName1_1_7));
    
    
  }


  
}
