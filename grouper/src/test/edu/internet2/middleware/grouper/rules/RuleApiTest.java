/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.subject.Subject;


/**
 * test rule api
 * @author mchyzer
 */
public class RuleApiTest extends GrouperTest {

  /**
   * @param name
   */
  public RuleApiTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleApiTest("testInheritFolderPrivilegesDaemon"));
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");

  }

  /**
   * 
   */
  public void testGroupIntersection() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);

    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);

    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(subject9, groupA, groupB);
    
    groupB.addMember(subject0);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(subject0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));

    //lets someone to A
    groupA.addMember(subject1);
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //should not be in A anymore
    assertFalse(groupA.hasMember(subject1));
    
    
  }

  /**
   * 
   */
  public void testRuleApiEmailTemplate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}"); // ${subjectEmail}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "template: testTemplateSubject"); //${groupId}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "template: testTemplateBody"); 
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  
  /**
   * 
   */
  public void testGroupIntersectionDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);

    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);
  
    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(subject9, groupA, groupB, 5);
    
    groupB.addMember(subject0);
  
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(subject0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //should have a disabled date in group A
    assertTrue(groupA.hasMember(subject0));

    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, true);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 4 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 6 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));

    groupA.addMember(subject1);

    membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);

    assertNull(membership.getDisabledTime());

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);

    assertNotNull(membership.getDisabledTime());
    disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 4 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 6 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
  
  }
  
  /**
   * 
   */
  public void testRuleVeto() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group mustBeInGroup = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    Subject actAsSubject = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
    
    RuleApi.vetoMembershipIfNotInGroup(actAsSubject, ruleGroup, mustBeInGroup, 
        "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    try {
      ruleGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of stem:a if not a member of stem:b"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    mustBeInGroup.addMember(subject0);
    ruleGroup.addMember(subject0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

  }
  
  /**
   * 
   */
  public void testInheritGroupPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();

    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));

    long initialFirings = RuleEngine.ruleFirings;
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //make sure allowed
    assertTrue(groupB.hasUpdate(subject0));
    assertTrue(groupB.hasRead(subject0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
    
    
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(groupC.hasRead(subject0));
    assertTrue(groupC.hasUpdate(subject0));

  }

  /**
   * 
   */
  public void testInheritFolderPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));
  
    long initialFirings = RuleEngine.ruleFirings;
    
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasCreate(subject0));
    assertTrue(stemB.hasStem(subject0));
    
    
    Stem stemD = new StemSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(stemD.hasCreate(subject0));
    assertFalse(stemD.hasStem(subject0));
    
    
    Stem stemC = new StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    //fires for the sub stem and c stem
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    assertTrue(stemC.hasCreate(subject0));
    assertTrue(stemC.hasStem(subject0));
  
  
  }

  /**
   * 
   */
  public void testGroupIntersectionFolder() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stem = StemFinder.findByName(grouperSession, "stem2", true);
    
    RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Scope.SUB);
    
    long initialFirings = RuleEngine.ruleFirings;
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    groupB.addMember(subject0);
  
    //count rule firings
    
    //doesnt do anything
    groupB.deleteMember(subject0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    
    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    
    groupC.addMember(subject0);
    
    //doesnt do anything
    groupC.deleteMember(subject0);
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    groupC.addMember(subject0);
    groupA.addMember(subject0);
  
    groupC.deleteMember(subject0);
  
    //should fire from ancestor
    assertFalse(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
  }

  
  /**
   * 
   */
  public void testInheritAttributeDefPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
  
    groupA.addMember(subject0);
    
    RuleApi.inheritAttributeDefPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("attrRead, attrUpdate"));
  
    long initialFirings = RuleEngine.ruleFirings;
     
    
    AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(subject0));
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(subject0));
    
    
    AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(subject0));
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrRead(subject0));
    
    
    AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(subject0));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(subject0));
  
  }
  
  /**
   * 
   */
  public void testPermissionAssignment() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    //assign a user to a role
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    payrollUser.addMember(subject0, false);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    payrollGuest.addMember(subject1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee);

    groupEmployee.addMember(subject0);
    groupEmployee.addMember(subject1);
    groupEmployee.addMember(subject2);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupEmployee.deleteMember(subject2);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupEmployee.deleteMember(subject0);
    
    //should come out of groupA
    assertFalse(payrollUser.hasMember(subject0));

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupEmployee.deleteMember(subject1);

    assertTrue(payrollGuest.hasMember(subject1));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
  }

  /**
   * 
   */
  public void testPermissionAssignmentIntersectFolder() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
  
    Stem itEmployee = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    RuleApi.permissionFolderIntersection(SubjectFinder.findRootSubject(), permissionDef, itEmployee, Stem.Scope.SUB);
    
    groupProgrammers.addMember(subject0, false);
    groupSysadmins.addMember(subject0, false);
    groupProgrammers.addMember(subject1, false);
    groupSysadmins.addMember(subject1, false);
    groupProgrammers.addMember(subject2, false);
    groupSysadmins.addMember(subject2, false);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupProgrammers.deleteMember(subject2);
    groupSysadmins.deleteMember(subject2);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupProgrammers.deleteMember(subject0);

    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupSysadmins.deleteMember(subject0);

    //should come out of groupA
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(payrollUser.hasMember(subject0));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupSysadmins.deleteMember(subject1);

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    groupProgrammers.deleteMember(subject1);

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(payrollGuest.hasMember(subject1));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
  }

  /**
   * 
   */
  public void testPermissionAssignmentDisabledDate() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    //assign a user to a role
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);

    groupEmployee.addMember(subject0);
    groupEmployee.addMember(subject1);
    groupEmployee.addMember(subject2);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupEmployee.deleteMember(subject2);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupEmployee.deleteMember(subject0);
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    //should come out of groupA in 7 days
    Membership membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupEmployee.deleteMember(subject1);

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(payrollGuest.hasMember(subject1));

    //should come out of groupA in 7 days
    membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
    
    assertNull(membership.getDisabledTime());

    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    Timestamp timestamp = permissions.iterator().next().getDisabledTime();

    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

  }

  /**
   * 
   */
  public void testRuleVetoInOrg() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
    
    Stem mustBeInStem = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    RuleApi.vetoMembershipIfNotInGroupInFolder(SubjectFinder.findRootSubject(), ruleGroup, mustBeInStem, Stem.Scope.SUB, 
        "rule.entity.must.be.in.IT.employee.to.be.in.group", "Entity cannot be a member of group if not in the IT department org");
    
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    try {
      ruleGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of group if not in the IT department org"));
    }
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    groupProgrammers.addMember(subject0);
    ruleGroup.addMember(subject0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(ruleGroup.hasMember(subject0));

    ruleGroup.deleteMember(subject0);
    groupProgrammers.deleteMember(subject0);
    groupSysadmins.addMember(subject0);
 
    ruleGroup.addMember(subject0);

    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(ruleGroup.hasMember(subject0));

  }

  /**
   * 
   */
  public void testRuleVetoPermissions() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //assign a user to a role
    payrollUser.addMember(subject0, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
       
    RuleApi.vetoPermissionIfNotInGroup(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, "rule.entity.must.be.an.employee", "Entity cannot be assigned these permissions unless they are an employee");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    

    try {
      //assign the permission to another user directly, not due to a role
      payrollUser.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be assigned these permissions unless they are an employee"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //see that not have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    groupEmployee.addMember(subject0);
    
    payrollUser.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);

    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
  }

  /**
   * 
   */
  public void testRuleEmailFlattenedRemove() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
    Group groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();

    groupEmployee.addMember(groupProgrammer.toSubject());
    groupEmployee.addMember(groupResearcher.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //subject0 is an employee by two paths
    groupProgrammer.addMember(subject0, false);
    groupResearcher.addMember(subject0, false);

    RuleApi.emailOnFlattenedMembershipRemove(SubjectFinder.findRootSubject(), groupEmployee, "a@b.c, ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension}", "template: testEmailGroupBodyFlattenedRemove");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    //doesnt do anything, still in the group by another path
    groupProgrammer.deleteMember(subject0);
  
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

    assertEquals(initialFirings, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount, GrouperEmail.testingEmailCount);
  
    groupResearcher.deleteMember(subject0);

    //run the change log to change log temp and rules consumer
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);
    
    //should send an email...
    
  }

  /**
   * 
   */
  public void testRuleEmailFlattenedAddFromStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
    Group groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());
    groupEmployee.addMember(groupResearcher.toSubject());
  
    Stem stem = StemFinder.findByName(grouperSession, "stem", true);
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    RuleApi.emailOnFlattenedMembershipAddFromStem(SubjectFinder.findRootSubject(), stem, Stem.Scope.SUB, "a@b.c, ${safeSubject.emailAddress}", "template: testEmailGroupSubjectFlattenedAddInFolder", "Hello ${safeSubject.name},\n\nJust letting you know you were removed from group ${groupDisplayExtension} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
    
    //run at first so the consumer is initted
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    //subject0 is an employee by two paths
    groupProgrammer.addMember(subject0, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);

    groupResearcher.addMember(subject0, false);

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+3, GrouperEmail.testingEmailCount);
    
    groupEmployee.addMember(subject0);
  
    //run the change log to change log temp and rules consumer
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    //should not send an email...
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+3, GrouperEmail.testingEmailCount);
  
    
  }

  /**
   * 
   */
  public void testRuleEmailDisabledDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    RuleApi.emailOnFlattenedDisabledDate(SubjectFinder.findRootSubject(), groupEmployee, 6, 8, GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will be removed from group ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
    
    //count rule firings
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    groupEmployee.addMember(subject0, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");

    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    
    Membership membership = groupEmployee.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    //set disabled 7 days in the future
    membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);

    groupProgrammer.addMember(subject0);

    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);

  }

  /**
   * 
   */
  public void testRuleEmailFlattenedPermissionAssign() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    //run at first so the consumer is initted
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
    
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser")
      .assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest")
      .assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    
    payrollGuest.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef)
      .assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    RuleApi.emailOnFlattenedPermissionAssign(SubjectFinder.findRootSubject(), permissionDef, 
        "a@b.c, ${safeSubject.emailAddress}", 
        "You were assigned permission: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}", 
        "Hello ${safeSubject.name},\n\nJust letting you know you were assigned permission ${attributeDefNameDisplayExtension} in the central Groups/Permissions management system.  Please do not respond to this email.\n\nRegards.");
      
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    long initialEmailCount = GrouperEmail.testingEmailCount;

    //assign a permission
    payrollUser.addMember(subject0, false);

    //should fire and send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);

    //assign by a different path
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);
    
    //shouldnt fire or send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);

    //assign a new user directly
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    
    //should fire and send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);

    //assign by a different path
    payrollUser.addMember(subject1, false);

    //shouldnt fire or send email
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);
    
  }
  
  /**
   * 
   */
  public void testRuleEmailPermissionsDisabledDate() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    RuleApi.emailOnFlattenedPermissionDisabledDate(SubjectFinder.findRootSubject(), permissionDef, 6, 8, GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}", "You will have this permission unassigned: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will have this permission removed ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups / Permissions management system.  Please do not respond to this email.\n\nRegards.");

    //count rule firings
    long initialEmailCount = GrouperEmail.testingEmailCount;

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    //subject 1,2 is just more data in the mix
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    

    payrollUser.addMember(subject1, false);
    payrollGuest.addMember(subject0, false);
    payrollGuest.addMember(subject2, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);

    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject2);

    try {
      GrouperDAOFactory.getFactory().getPermissionEntry().findPermissionsByAttributeDefDisabledRange(permissionDef.getId(),
          null, null);

      fail("should need either disabled from or to");
    } catch (Exception e) {
      //good
    }

    AttributeAssign attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0).getAttributeAssign();

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount, GrouperEmail.testingEmailCount);

    //set disabled 7 days in the future
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);
  
    payrollUser.addMember(subject0, false);
  
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);
  
  }

  /**
   * 
   */
  public void testGroupIntersectionDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);
    
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);
  
    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(subject9, groupA, groupB);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    groupA.addMember(subject0, false);
    groupA.addMember(subject1, false);
    groupB.addMember(subject1, false);
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    assertTrue(status.toLowerCase().contains("success"));
    
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);

    assertFalse(groupA.hasMember(subject0));
    assertTrue(groupA.hasMember(subject1));
    
  }

  /**
   * 
   */
  public void testGroupIntersectionDateDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    Subject subject2 = SubjectFinder.findById("test.subject.2", true);
  
    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);

    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);

    RuleApi.groupIntersection(subject9, groupA, groupB, 7);

    groupA.addMember(subject0);
    groupA.addMember(subject1);
    groupB.addMember(subject1);
    groupA.addMember(subject2);
    
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member2, true, true);
    
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));

    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));

    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);

    //should have a disabled date in group A
    assertTrue(groupA.hasMember(subject0));
    assertTrue(groupA.hasMember(subject1));
    assertTrue(groupA.hasMember(subject2));
  
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));
  
    //should have member1, with no disabled date
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);
    
    assertNull(membership.getDisabledTime());
    
    //should have member2 with original disabled date
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member2, true, true);
    
    assertNotNull(membership.getDisabledTime());
    
    disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
  }

  /**
   * 
   */
  public void testGroupIntersectionFolderDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    
    Stem stem = StemFinder.findByName(grouperSession, "stem2", true);
    
    RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Scope.SUB);
    
    long initialFirings = RuleEngine.ruleFirings;
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    groupA.addMember(subject0);
    groupA.addMember(subject1);
    groupC.addMember(subject1);
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
        
    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));
    assertTrue(groupA.hasMember(subject1));
  
  
  }

  
  /**
   * 
   */
  public void testInheritAttributeDefPrivilegesDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:c").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem2:d").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefE = new AttributeDefSave(grouperSession).assignName("stem3:e").assignCreateParentStemsIfNotExist(true).save();

    //almost has it
    attributeDefC.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
    attributeDefC.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_READ, false);

    //has it
    attributeDefD.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDefD.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_READ, false);
    
    RuleApi.inheritAttributeDefPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("attrRead, attrUpdate"));
  
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //count rule firings
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));
    
    //make sure allowed
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrOptin(groupA.toSubject()));
  
    //make sure allowed
    assertTrue(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertTrue(attributeDefD.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));

    assertFalse(attributeDefE.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertFalse(attributeDefE.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));

  }

  /**
   * 
   */
  public void testInheritGroupPrivilegesDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:c").assignCreateParentStemsIfNotExist(true).save();
    Group groupD = new GroupSave(grouperSession).assignName("stem2:d").assignCreateParentStemsIfNotExist(true).save();
    Group groupE = new GroupSave(grouperSession).assignName("stem3:e").assignCreateParentStemsIfNotExist(true).save();

    groupC.grantPriv(groupA.toSubject(), AccessPrivilege.OPTIN, false);
    groupC.grantPriv(groupA.toSubject(), AccessPrivilege.READ, false);
    
    groupD.grantPriv(groupA.toSubject(), AccessPrivilege.UPDATE, false);
    groupD.grantPriv(groupA.toSubject(), AccessPrivilege.READ, false);

    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));
  
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //count rule firings
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(groupB.hasUpdate(groupA.toSubject()));
    assertTrue(groupB.hasRead(groupA.toSubject()));
    
    assertTrue(groupC.hasUpdate(groupA.toSubject()));
    assertTrue(groupC.hasOptin(groupA.toSubject()));
    assertTrue(groupC.hasRead(groupA.toSubject()));

    assertTrue(groupD.hasUpdate(groupA.toSubject()));
    assertTrue(groupD.hasRead(groupA.toSubject()));
    
    assertFalse(groupE.hasUpdate(groupA.toSubject()));
    assertFalse(groupE.hasRead(groupA.toSubject()));
      
  }

  /**
   * 
   */
  public void testInheritFolderPrivilegesDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Stem stemC = new StemSave(grouperSession).assignName("stem2:c").assignCreateParentStemsIfNotExist(true).save();
    Stem stemD = new StemSave(grouperSession).assignName("stem2:d").assignCreateParentStemsIfNotExist(true).save();
    Stem stemE = new StemSave(grouperSession).assignName("stem3:e").assignCreateParentStemsIfNotExist(true).save();

    //stemC can stem and create
    stemC.grantPriv(groupA.toSubject(), NamingPrivilege.STEM, false);
    stemC.grantPriv(groupA.toSubject(), NamingPrivilege.CREATE, false);
    
    //stemD can stem but not create
    stemD.grantPriv(groupA.toSubject(), NamingPrivilege.STEM, false);
    
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));
  
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //count rule firings
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasCreate(subject0));
    assertTrue(stemB.hasStem(subject0));

    assertTrue(stemC.hasCreate(subject0));
    assertTrue(stemC.hasStem(subject0));

    assertTrue(stemD.hasCreate(subject0));
    assertTrue(stemD.hasStem(subject0));

    assertFalse(stemE.hasCreate(subject0));
    assertFalse(stemE.hasStem(subject0));
    
  
  }

  /**
   * 
   */
  public void testPermissionAssignmentDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    payrollUser.addMember(subject0, false);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    payrollGuest.addMember(subject1, false);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    payrollUser.addMember(subject3, false);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    payrollGuest.addMember(subject4, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, true);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee);
  
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    groupEmployee.addMember(subject3);
    groupEmployee.addMember(subject4);
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));

    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
  
    //should come out of groupA
    assertFalse(payrollUser.hasMember(subject0));

    assertTrue(payrollUser.hasMember(subject3));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollGuest.hasMember(subject4));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    

  }

  /**
   * 
   */
  public void testPermissionAssignmentDisabledDateDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    Subject subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "jdbc", true);

    //assign a user to a role
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    //subject 3 and 4 are on the up and up
    payrollUser.addMember(subject3, false);
    payrollGuest.addMember(subject4, false);
    //subject 5 and 6 have disabled dates, shouldnt be changed
    payrollUser.addMember(subject5, false);
    payrollGuest.addMember(subject6, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);

    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
    
    AttributeAssign attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject6).getAttributeAssign();
    
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate();
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, false);
    Member member5 = MemberFinder.findBySubject(grouperSession, subject5, false);
    Member member6 = MemberFinder.findBySubject(grouperSession, subject6, false);

    Membership membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    

    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  
    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);
  
    groupEmployee.addMember(subject2);
    groupEmployee.addMember(subject3);
    groupEmployee.addMember(subject4);

    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
    
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));

    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    
    //should come out of groupA in 7 days
    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member3, true, true);
    
    assertNull(membership.getDisabledTime());

    //member 5 already had a disabled date
    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
    
    assertNotNull(membership.getDisabledTime());
    disabledTime = membership.getDisabledTime().getTime();

    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    assertTrue(payrollUser.hasMember(subject0));
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollUser.hasMember(subject3));
    assertTrue(payrollGuest.hasMember(subject4));
    assertTrue(payrollUser.hasMember(subject5));
    assertTrue(payrollGuest.hasMember(subject6));
  
    //should not care about guest disabled date
    membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
    assertNull(membership.getDisabledTime());

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());

    Timestamp timestamp = permissions.iterator().next().getImmediateMshipDisabledTime();
    
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  
    timestamp = permissions.iterator().next().getDisabledTime();
  
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    timestamp = permissions.iterator().next().getDisabledTime();
    
    assertNull(timestamp);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    timestamp = permissions.iterator().next().getDisabledTime();
    
    assertNull(timestamp);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member5.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    timestamp = permissions.iterator().next().getImmediateMshipDisabledTime();
    
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member6.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  
    timestamp = permissions.iterator().next().getDisabledTime();
  
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));


  }

  /**
   * 
   */
  public void testPermissionAssignmentIntersectFolderDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
  
    Stem itEmployee = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    //subject2 is ok
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    groupProgrammers.addMember(subject2, false);
    groupSysadmins.addMember(subject3, false);
  
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    payrollUser.addMember(subject2, false);
    payrollGuest.addMember(subject3, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject3);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    RuleApi.permissionFolderIntersection(SubjectFinder.findRootSubject(), permissionDef, itEmployee, Stem.Scope.SUB);
        
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
  
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
      
    assertFalse(payrollUser.hasMember(subject0));
    assertTrue(payrollUser.hasMember(subject2));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollGuest.hasMember(subject3));
  
  }

  /**
   * 
   */
  public void testRuleVetoDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group mustBeInGroup = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    Subject actAsSubject = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    
    //add these before the rule
    ruleGroup.addMember(subject0);
    ruleGroup.addMember(subject1);
    mustBeInGroup.addMember(subject1);
    
    RuleApi.vetoMembershipIfNotInGroup(actAsSubject, ruleGroup, mustBeInGroup, 
        "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
  
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(ruleGroup.hasMember(subject0));
    assertTrue(ruleGroup.hasMember(subject1));
  
  }

  /**
   * 
   */
  public void testRuleVetoInOrgDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    
    Stem mustBeInStem = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);

    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);

    ruleGroup.addMember(subject0);
    ruleGroup.addMember(subject1);
    groupProgrammers.addMember(subject1);

    RuleApi.vetoMembershipIfNotInGroupInFolder(SubjectFinder.findRootSubject(), ruleGroup, mustBeInStem, Stem.Scope.SUB, 
        "rule.entity.must.be.in.IT.employee.to.be.in.group", "Entity cannot be a member of group if not in the IT department org");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(ruleGroup.hasMember(subject0));
    assertTrue(ruleGroup.hasMember(subject1));

  }

  /**
   * 
   */
  public void testRuleVetoPermissionsDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);

    //assign a user to a role
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    payrollUser.addMember(subject2, false);
    payrollGuest.addMember(subject3, false);

    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();

    //assign the permission to another user directly, not due to a role
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject3);

    //these are legit
    groupEmployee.addMember(subject2);
    groupEmployee.addMember(subject3);
    
    RuleApi.vetoPermissionIfNotInGroup(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, "rule.entity.must.be.an.employee", "Entity cannot be assigned these permissions unless they are an employee");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));


    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //see that not have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    assertFalse(payrollUser.hasMember(subject0));
    assertTrue(payrollGuest.hasMember(subject1));

  }

  /**
   * 
   */
  public void testInheritGroupPrivilegesPattern() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"), "stem2:%someGroup");
  
    long initialFirings = RuleEngine.ruleFirings;
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    //make sure not allowed
    assertFalse(groupB.hasUpdate(subject0));
    assertFalse(groupB.hasRead(subject0));
    
    //allowed
    Group someGroup = new GroupSave(grouperSession).assignName("stem2:b:w_someGroup").assignCreateParentStemsIfNotExist(true).save();
    
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(someGroup.hasUpdate(subject0));
    assertTrue(someGroup.hasRead(subject0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
    
    
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(groupC.hasRead(subject0));
    assertFalse(groupC.hasUpdate(subject0));
  
  }

  /**
   * 
   */
  public void testInheritGroupPrivilegesPatternDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    //allowed
    Group someGroup = new GroupSave(grouperSession).assignName("stem2:b:w_someGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
    
    assertFalse(someGroup.hasUpdate(subject0));
    assertFalse(someGroup.hasRead(subject0));
  
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    

    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"), "stem2:%someGroup");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    //count rule firings, one for the one group for two privs
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //make sure not allowed
    assertFalse(groupB.hasUpdate(subject0));
    assertFalse(groupB.hasRead(subject0));
  
    //make sure allowed
    assertTrue(someGroup.hasUpdate(subject0));
    assertTrue(someGroup.hasRead(subject0));
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
  
    assertFalse(groupC.hasRead(subject0));
    assertFalse(groupC.hasUpdate(subject0));
  
    //run the daemon
    status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    //not much changed
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //make sure not allowed
    assertFalse(groupB.hasUpdate(subject0));
    assertFalse(groupB.hasRead(subject0));
  
    //make sure allowed
    assertTrue(someGroup.hasUpdate(subject0));
    assertTrue(someGroup.hasRead(subject0));
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
  
    assertFalse(groupC.hasRead(subject0));
    assertFalse(groupC.hasUpdate(subject0));
  
  }

}
