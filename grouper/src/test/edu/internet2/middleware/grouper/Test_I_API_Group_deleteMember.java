/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * @author  blair christensen.
 * @version $Id: Test_I_API_Group_deleteMember.java,v 1.3.4.1 2008-08-23 05:45:25 mchyzer Exp $
 * @since   1.2.0
 */
public class Test_I_API_Group_deleteMember extends GrouperTest {

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new Test_I_API_Group_deleteMember("test_DeleteUnresolvableSubject"));
  }

  /**
   * 
   */
  private Group           gA;
  /**
   * 
   */
  private Group gB;

  /**
   * 
   */
  private Group gC;
  
  /**
   * 
   */
  private Group gD;
  /**
   * 
   */
  private Stem            parent;
  /**
   * 
   */
  private GrouperSession  s;
  /**
   * 
   */
  private Subject         subjX;
  
  /**
   * 
   */
  private Subject subjY;



  // TESTING INFRASTRUCTURE //

  /**
   * 
   */
  public Test_I_API_Group_deleteMember() {
    super();
  }

  /**
   * @param name
   */
  public Test_I_API_Group_deleteMember(String name) {
    super(name);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  public void setUp() {
    super.setUp();
    try {    
      s       = GrouperSession.start( SubjectFinder.findRootSubject() );
      parent  = StemFinder.findRootStem(s).addChildStem("parent", "parent");
      gA      = parent.addChildGroup("child group a", "child group a");
      gB      = parent.addChildGroup("child group b", "child group b");
      gC      = parent.addChildGroup("child group c", "child group c");
      gD      = parent.addChildGroup("child group d", "child group d");
      subjX   = SubjectFinder.findById( RegistrySubject.add(s, "subjX", "person", "subjX").getId() );
      subjY   = SubjectFinder.findById( RegistrySubject.add(s, "subjY", "person", "subjY").getId() );
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  public void tearDown() {
    try {
      s.stop();
    }
    catch (Exception eShouldNotHappen) {
      throw new GrouperRuntimeException( eShouldNotHappen.getMessage(), eShouldNotHappen );
    }
    super.tearDown();
  }


  // TESTS //

  /**
   * From <a href="https://bugs.internet2.edu/jira/browse/GRP-2">jira:grouper:#2</a>.
   * <pre>
   * 1. I created a Group A with a single subject X.
   * 2. I created a Group B with a single subject Y.
   * 3. I created a Group C that is union of A and B. It has members X and Y.
   * 4. I created a Group D with a single subject Group C. Indirectly it has members X and Y.
   * 5. I removed subject X from Group A. This resulted in subject X no longer in being member
   *    of Group C as expected, but subject X remained a member of D.     
   * </pre>
   * @since   1.2.0
   */
  public void test_DeleteSubjectsRemovedFromFactorsFromWhereTheyAreEffective() {
    try {
      // (1)
      gA.addMember(subjX);
      // (2)
      gB.addMember(subjY);
      // (3)
      gC.addCompositeMember( CompositeType.UNION, gA, gB );
      // (4)
      gD.addMember( gC.toSubject() );
      // (5)
      gA.deleteMember(subjX);
    }
    catch (Exception eShouldNotHappen) {
      fail( "ERROR INITIALIZING TEST: " + ExceptionUtils.getFullStackTrace(eShouldNotHappen) );
    }
    assertFalse( "gC !has subjX", gC.hasMember(subjX) );
    assertTrue(  "gC has subjY",  gC.hasMember(subjY) );
    assertTrue(  "gD has gC",     gD.hasMember( gC.toSubject() ) ); 
    assertFalse( "gD !has subjX", gD.hasMember(subjX) ); 
    assertTrue(  "gD has subjY",  gD.hasMember(subjY) ); 
  }


  /**
   * From <a href="https://bugs.internet2.edu/jira/browse/GRP-2">jira:grouper:#2</a>.
   * <pre>
   * 1. I created a Group A with a single subject X.
   * 2. I created a Group B with a single subject Y.
   * 3. I created a Group C that is union of A and B. It has members X and Y.
   * 4. I created a Group D with a single subject Group C. Indirectly it has members X and Y.
   * 5. I removed subject X from Group A. This resulted in subject X no longer in being member
   *    of Group C as expected, but subject X remained a member of D.     
   * </pre>
   * @throws GrouperDAOException 
   * @throws MemberAddException 
   * @throws InsufficientPrivilegeException 
   * @throws SubjectNotUniqueException 
   * @throws MemberDeleteException 
   * @throws MemberNotFoundException 
   * @since   1.2.0
   */
  public void test_DeleteUnresolvableSubject() throws GrouperDAOException, MemberAddException, 
      InsufficientPrivilegeException, SubjectNotUniqueException, MemberDeleteException, MemberNotFoundException {
    gA.addMember(subjX);
    
    //RegistrySubjectDAO registrySubjectDAO = GrouperDAOFactory.getFactory().getRegistrySubject().find("subjX", "person");
    RegistrySubjectDTO registrySubjectDTO = new RegistrySubjectDTO();
    registrySubjectDTO.setId(subjX.getId());
    registrySubjectDTO.setName(subjX.getName());
    registrySubjectDTO.setType(subjX.getType().getName());
    GrouperDAOFactory.getFactory().getRegistrySubject().delete(registrySubjectDTO);

    Member memberX = MemberFinder.findBySubject(s, subjX);
    
    //clear cache
    SubjectFinder.clearCache();
    
    //lets try to resolve
    try {
      SubjectFinder.findById(subjX.getId());
      throw new RuntimeException("Why is subject: " + subjX.getId() + " found??? Probably a caching problem!");
    } catch (SubjectNotFoundException snfe) {
      //good
    }
    
    // should be able to delete an unresolvable subject
    gA.deleteMember(memberX);
    
    assertFalse("shouldnt be a member anymore", gA.hasMember(subjX));
  }

} 

