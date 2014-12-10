/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperAtlassianConnector.db.v1.AtlassianCwdGroupV1;
import edu.internet2.middleware.grouperAtlassianConnector.db.v1.AtlassianCwdMembershipV1;
import edu.internet2.middleware.grouperAtlassianConnector.db.v1.AtlassianCwdUserV1;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianCwdGroupV2;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianCwdMembershipV2;
import edu.internet2.middleware.grouperAtlassianConnector.db.v2.AtlassianCwdUserV2;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/** 
 * which version of the database
 */
public enum AtlassianCwdVersion {

  /** v1 is the older version from jira */
  V1 {

    @Override
    public Map<String, AtlassianCwdUser> retrieveUsers() {
      return AtlassianCwdUserV1.retrieveUsers();
    }

    @Override
    public AtlassianCwdMembership newMembership() {
      return new AtlassianCwdMembershipV1();
    }

    @Override
    public AtlassianCwdGroup newGroup() {
      return new AtlassianCwdGroupV1();
    }

    @Override
    public AtlassianCwdUser newUser() {
      return new AtlassianCwdUserV1();
    }

    @Override
    public Map<String, AtlassianCwdGroup> retrieveGroups() {
      return AtlassianCwdGroupV1.retrieveGroups();
    }
    @Override
    public List<AtlassianCwdMembership> retrieveMemberships() {
      return AtlassianCwdMembershipV1.retrieveMemberships();
    }
},
  
  /** v2 is the newer version from confluence */
  V2 {

    @Override
    public Map<String, AtlassianCwdUser> retrieveUsers() {
      return AtlassianCwdUserV2.retrieveUsers();
    }

    @Override
    public AtlassianCwdMembership newMembership() {
      return new AtlassianCwdMembershipV2();
    }

    @Override
    public AtlassianCwdGroup newGroup() {
      return new AtlassianCwdGroupV2();
    }

    @Override
    public AtlassianCwdUser newUser() {
      return new AtlassianCwdUserV2();
    }

    @Override
    public Map<String, AtlassianCwdGroup> retrieveGroups() {
      return AtlassianCwdGroupV2.retrieveGroups();
    }

    @Override
    public List<AtlassianCwdMembership> retrieveMemberships() {
      return AtlassianCwdMembershipV2.retrieveMemberships();
    }
  };

  /**
   * retrieve users
   * @return the map of users
   */
  public abstract Map<String, AtlassianCwdUser> retrieveUsers();
  
  /**
   * retrieve memberships
   * @return the map of memberships
   */
  public abstract List<AtlassianCwdMembership> retrieveMemberships();
  
  /**
   * retrieve groups
   * @return the map of groups
   */
  public abstract Map<String, AtlassianCwdGroup> retrieveGroups();
  
  /**
   * new membership
   * @return new membership
   */
  public abstract AtlassianCwdMembership newMembership();
  
  /**
   * new group
   * @return new group
   */
  public abstract AtlassianCwdGroup newGroup();
  
  /**
   * new user
   * @return new user
   */
  public abstract AtlassianCwdUser newUser();
  
  /**
   * 
   * @param string
   * @param exceptionIfNotFound
   * @return the enum
   */
  public static AtlassianCwdVersion valueOfIgnoreCase(String string, boolean exceptionIfNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(AtlassianCwdVersion.class, string, exceptionIfNotFound);
  }

  /**
   * get the current configured version
   * @return the version
   */
  public static AtlassianCwdVersion currentVersion() {
    String versionFromConfig = GrouperClientConfig.retrieveConfig().propertyValueString("atlassian.cwd.version");
    return valueOfIgnoreCase(versionFromConfig, true);
  }
  
}
