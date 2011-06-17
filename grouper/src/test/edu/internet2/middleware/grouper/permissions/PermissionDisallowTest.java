/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PermissionDisallowTest extends GrouperTest {
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionDisallowTest("testVariousRoleAssignments"));
  }

  /**
   * 
   */
  public PermissionDisallowTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionDisallowTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  private Stem root;

  /** top stem */
  private Stem top;

  /** admin role */
  private Role admin;

  /** senior admin */
  private Role seniorAdmin;

  /** user */
  private Role user;
  
  /** attribute def */
  private AttributeDef permissionDef;
  
  /** english */
  private AttributeDefName english;
  
  /** english */
  private AttributeDefName math;
  
  /** english */
  private AttributeDefName electricalEngineering;
  
  /** english */
  private AttributeDefName chemicalEngineering;
  
  /** english */
  private AttributeDefName artsAndSciences;
  
  /** english */
  private AttributeDefName engineering;
  
  /** english */
  private AttributeDefName all;

  /** read */
  private String readString = "read";
  
  /** write */
  private String writeString = "write";
  
  /** readWrite */
  private String readWriteString = "readWrite";
  
  /** admin */
  private String adminString = "admin";
  
  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");

    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");

    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    this.admin = this.top.addChildRole("admin", "admin");
    
    this.seniorAdmin = this.top.addChildRole("seniorAdmin", "seniorAdmin");
    
    //senior admin inherits from admin
    this.seniorAdmin.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.admin);
    
    this.user = this.top.addChildRole("user", "user");
    
    this.permissionDef = this.top.addChildAttributeDef("permissionDef", AttributeDefType.perm);
    this.permissionDef.setAssignToEffMembership(true);
    this.permissionDef.setAssignToGroup(true);
    this.permissionDef.store();
    this.english = this.top.addChildAttributeDefName(this.permissionDef, "english", "English");
    this.math = this.top.addChildAttributeDefName(this.permissionDef, "math", "Math");
    this.electricalEngineering = this.top.addChildAttributeDefName(this.permissionDef, "electricalEngineering", "Electrical Engineering");
    this.chemicalEngineering = this.top.addChildAttributeDefName(this.permissionDef, "chemicalEngineering", "Chemical Engineering");
    this.artsAndSciences = this.top.addChildAttributeDefName(this.permissionDef, "artsAndSciences", "Arts and Sciences");
    this.engineering = this.top.addChildAttributeDefName(this.permissionDef, "engineering", "Engineering");
    this.all = this.top.addChildAttributeDefName(this.permissionDef, "all", "All");

    this.all.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.engineering);
    this.all.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.artsAndSciences);
    this.artsAndSciences.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.english);
    this.artsAndSciences.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.math);
    this.engineering.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.math);
    this.engineering.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.electricalEngineering);
    this.engineering.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.chemicalEngineering);

    this.permissionDef.getAttributeDefActionDelegate().configureActionList("read, write, readWrite, admin");
    
    
    AttributeAssignAction read = this.permissionDef.getAttributeDefActionDelegate().findAction(this.readString, true);
    AttributeAssignAction write = this.permissionDef.getAttributeDefActionDelegate().findAction(this.writeString, true);
    AttributeAssignAction readWrite = this.permissionDef.getAttributeDefActionDelegate().findAction(this.readWriteString, true);
    AttributeAssignAction admin = this.permissionDef.getAttributeDefActionDelegate().findAction(this.adminString, true);
    
    readWrite.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(read);
    readWrite.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(write);
    admin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(readWrite);
   
    
    
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }
  

  /** 
   * test various role assignments
   */
  public void testVariousRoleAssignments() {
    
    //Role<Admin> allows: Action<Read> of Resource<Arts and sciences>
    this.admin.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.ALLOWED);
    
    //
    //Role<User> denies: Action<Read> of Resource<Arts and sciences>
    this.user.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.ALLOWED);

    //
    //User subj0 is assigned Role<Admin> and Role<User>
    this.admin.addMember(SubjectTestHelper.SUBJ0, true);
    this.user.addMember(SubjectTestHelper.SUBJ0, true);
    
    //
    //Result:
    //
    //Overall, jsmith is allowed Arts and sciences since if a user is allowed in any role, they are allowed.
    //
    //If the application supports users acting as a certain role instead of flattening all permissions into one 
    //permissions set (i.e. ability to elevate permissions), then as a User, jsmith cannot Read Arts and Sciences, 
    //but as an Admin, jsmith can Read Arts and Sciences
    
    assertFalse("wrong subject", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ1, this.admin, this.english, this.readString));
    assertTrue("inherits from arts and sciences", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.english, this.readString));
    assertTrue("inherits from arts and sciences", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.admin, this.english, this.readString));
    assertTrue("inherits from arts and sciences", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.user, this.english, this.readString));
    assertFalse("wrong role", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.seniorAdmin, this.english, this.readString));
    assertTrue("is arts and sciences", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.admin, this.artsAndSciences, this.readString));
    assertFalse("wrong permission", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.admin, this.electricalEngineering, this.readString));
    assertFalse("wrong action", PermissionFinder.hasPermission(SubjectTestHelper.SUBJ0, this.admin, this.english, this.readWriteString));
    
    
//    Set<PermissionEntry> permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
//        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);
//    List<PermissionEntry> permissionEntriesList = new ArrayList<PermissionEntry>(permissionEntriesSet);
//    Collections.sort(permissionEntriesList);
//    System.out.println("\n\n");
//    for (PermissionEntry permissionEntry : permissionEntriesList) {
//      System.out.println("    permissionEntry = PermissionEntry.collectionFindFirst(permissionEntriesList, \"" 
//          + permissionEntry.getRoleName() + "\", \"" + permissionEntry.getAttributeDefNameName() + "\", \"" 
//          + permissionEntry.getAction() + "\", \"" + permissionEntry.getSubjectSourceId() + "\", \""
//          + permissionEntry.getSubjectId() + "\", \"" + permissionEntry.getPermissionTypeDb() + "\");");
//      System.out.println("    assertPermission(permissionEntry, \"" 
//          + permissionEntry.getPermissionTypeDb() + "\", " + permissionEntry.isImmediateMembership() + ", " 
//          + permissionEntry.isImmediatePermission() + ", " + permissionEntry.getMembershipDepth() + ", " 
//          + permissionEntry.getRoleSetDepth() + ", " + permissionEntry.getAttributeAssignActionSetDepth()
//          + ", " + permissionEntry.getAttributeDefNameSetDepth() + ");\n");
//      
//    }
//    permissionEntry = PermissionEntry.collectionFindFirst(permissionEntriesList, "top:roleChild", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.3", "role");
//    assertPermission(permissionEntry, "role", true, false, 0, 1, 1, 1);

    
    
  }
  
  /**
   * 
   */
  public void testNotChangeDisallow() {

    //Role<Admin> allows: Action<Read> of Resource<Arts and sciences>
    AttributeAssign attributeAssign = this.admin.getPermissionRoleDelegate()
      .assignRolePermission("read", this.artsAndSciences, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    //prove that we can do saves and updates
    attributeAssign.setDisabledTime(GrouperUtil.toTimestamp("2010/01/01"));
    attributeAssign.saveOrUpdate();
    
    attributeAssign.setDisabledTime(null);
    attributeAssign.saveOrUpdate();
    
    //try to change the disallow flag
    attributeAssign.setDisallowed(true);
    
    try {
      attributeAssign.saveOrUpdate();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //good
    }
    

  }
  
}
