package edu.internet2.middleware.grouper.permissions.limits.impl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllPermissionsLimitsImplTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.attr");
    //$JUnit-BEGIN$
    suite.addTestSuite(PermissionLimitElLogicTest.class);
    suite.addTestSuite(PermissionLimitLabelsContainTest.class);
    suite.addTestSuite(PermissionLimitTimeLogicTest.class);
    suite.addTestSuite(PermissionLimitAmountLogicTest.class);
    suite.addTestSuite(PermissionLimitIpNetworkLogicTest.class);
    //$JUnit-END$
    return suite;
  }

}
