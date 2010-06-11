/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class AttributeSecurityFromTypeHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeSecurityFromTypeHookTest("testAttributeSecurity"));
  }
  
  /**
   * @param name
   */
  public AttributeSecurityFromTypeHookTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testAttributeSecurity() {
    //set some grouper properties
    ApiConfig.testConfig.put("grouperIncludeExclude.requireGroups.use", "false");
    ApiConfig.testConfig.put("security.types.posixGroup.wheelOnly", "true");

    GroupTypeSecurityHook.resetCacheSettings();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    final GroupType groupType = GroupType.createType(grouperSession, "posixGroup", true);
    
    groupType.addAttribute(grouperSession, "gidNumber", 
        AccessPrivilege.READ, AccessPrivilege.ADMIN, false, true);
    
    Stem stem = new StemSave(grouperSession).assignName("aStem").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject = SubjectTestHelper.SUBJ0;
    
    stem.grantPriv(subject, NamingPrivilege.CREATE, true);
    
    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(subject);
    final Group group = stem.addChildGroup("aGroup", "aGroup");
    
    try {
      group.addType(groupType);
      fail("Shouldnt get here");
    } catch (Exception e) {
      //ok
    }
    
    assertTrue(!group.getTypes().contains(groupType));
    
    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        try {
          //should work here
          group.addType(groupType);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        try {
          group.setAttribute("gidNumber", "2");
        } catch (Exception e) {
          fail("Shouldnt get here: " + ExceptionUtils.getFullStackTrace(e));
        }

        return null;
      }
    });
    
    
  }
  
}
