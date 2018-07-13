/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperDeprovisioningLogic {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperDeprovisioningLogic.class);

  /**
   * cache deprovisioning stuff
   */
  private static class GrouperDeprovisioningCache {
    
    /**
     * key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins, sourceId, subjectId 
     * if affiliation is null and deprovisioningAdmins is second key then its overall admins
     */
    private Set<MultiKey> deprovisionedSubjectSet = null;

    /**
     * key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins
     * if affiliation is null and deprovisioningAdmins is second key then its overall admins
     */
    private Map<MultiKey, Set<Subject>> deprovisionedSubjectSetMap = null;

    
    /**
     * key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins, sourceId, subjectId 
     * if affiliation is null and deprovisioningAdmins is second key then its overall admins
     * @return the deprovisionedSubjectSet
     */
    public Set<MultiKey> getDeprovisionedSubjectSet() {
      return this.deprovisionedSubjectSet;
    }

    
    /**
     * key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins, sourceId, subjectId 
     * if affiliation is null and deprovisioningAdmins is second key then its overall admins
     * @param deprovisionedSubjectSet1 the depovisionedSubjectSet to set
     */
    public void setDeprovisionedSubjectSet(Set<MultiKey> deprovisionedSubjectSet1) {
      this.deprovisionedSubjectSet = deprovisionedSubjectSet1;
    }

    
    /**
     * key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins
     * if affiliation is null and deprovisioningAdmins is second key then its overall admins
     * @return the deprovisionedSubjectSetMap
     */
    public Map<MultiKey, Set<Subject>> getDeprovisionedSubjectSetMap() {
      return this.deprovisionedSubjectSetMap;
    }

    
    /**
     * key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins
     * if affiliation is null and deprovisioningAdmins is second key then its overall admins
     * @param deprovisionedSubjectSetMap1 the deprovisionedSubjectSetMap to set
     */
    public void setDeprovisionedSubjectSetMap(
        Map<MultiKey, Set<Subject>> deprovisionedSubjectSetMap1) {
      this.deprovisionedSubjectSetMap = deprovisionedSubjectSetMap1;
    }
    
    
  }
  
  /** Multikey can be:
   * sourceId, subjectId
   * sourceId, subjectId, affiliation
   */
  private static ExpirableCache<Boolean, GrouperDeprovisioningCache> deprovisionedSubjectCache = null;

  /**
   * nanos this was last retrieved
   */
  private static long deprovisionedSubjectCacheLastRetrievedNanos = -1L;
  
  /** 
   * if it takes too long to build the cache, just return the previous one
   */
  private static GrouperDeprovisioningCache grouperDeprovisioningCacheFailsafe = null;

  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<Boolean, GrouperDeprovisioningCache> deprovisionedSubjectCache() {
    //  # number of minutes to cache deprovisioned members / admins
    //  deprovisioning.cacheMembersForMinutes = 5
    if (deprovisionedSubjectCache == null) {
      int cacheMembersForMinutes = GrouperConfig.retrieveConfig().propertyValueInt("deprovisioning.cacheMembersForMinutes", 5);
      deprovisionedSubjectCache = new ExpirableCache<Boolean, GrouperDeprovisioningCache>(cacheMembersForMinutes);
    }
    
    return deprovisionedSubjectCache;
  }
  
  /**
   * get the cache.  if we arent using the cache, then get it again
   * @param useCache
   * @return the cache
   */
  private static GrouperDeprovisioningCache grouperDeprovisioningCache(final boolean useCache) {
    
    GrouperDeprovisioningCache grouperDeprovisioningCache = deprovisionedSubjectCache().get(Boolean.TRUE);
    
    if (!useCache || grouperDeprovisioningCache == null) {
      
      if (useCache && grouperDeprovisioningCacheFailsafe != null) {
        
        final GrouperDeprovisioningCache[] GROUPER_DEPROVISIONING_CACHE = new GrouperDeprovisioningCache[]{null};
        
        GROUPER_DEPROVISIONING_CACHE[0] = grouperDeprovisioningCacheFailsafe;
        
        Thread thread = new Thread(new Runnable() {

          public void run() {
            GrouperSession grouperSession = GrouperSession.startRootSession();
            try {
              GROUPER_DEPROVISIONING_CACHE[0] = grouperDeprovisioningCacheHelperAsRoot(useCache);
            } catch (RuntimeException re) {
              LOG.error("Error refreshing deprovisioning cache", re);
            } finally {
              GrouperSession.stopQuietly(grouperSession);
            }
          }
          
        });
        
        //run job
        thread.start();

        // maybe we should only wait for a little while...
        if (GROUPER_DEPROVISIONING_CACHE[0] != null) {

          //  # number of seconds to wait for refresh before giving up and using failsafe (if caching)
          //  deprovisioning.cacheFailsafeSeconds = 10 
          int cacheFailsafeSeconds = GrouperConfig.retrieveConfig().propertyValueInt("deprovisioning.cacheFailsafeSeconds", 10);
          GrouperUtil.threadJoin(thread, cacheFailsafeSeconds*1000);
          
        }
        
      } else {

        grouperDeprovisioningCache = (GrouperDeprovisioningCache)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            return grouperDeprovisioningCacheHelperAsRoot(useCache);
          }
        });
      }
      
      
    }
    return grouperDeprovisioningCache;
  }

  /**
   * 
   * @param useCache
   * @return the cache
   */
  private static GrouperDeprovisioningCache grouperDeprovisioningCacheHelperAsRoot(final boolean useCache) {
    
    final long lastRetrievedNanos = System.nanoTime();
    
    synchronized(GrouperDeprovisioningLogic.class) {
      GrouperDeprovisioningCache grouperDeprovisioningCache = deprovisionedSubjectCache().get(Boolean.TRUE);
      boolean hasCache = grouperDeprovisioningCache != null;
      boolean newEnoughCache = deprovisionedSubjectCacheLastRetrievedNanos > lastRetrievedNanos;
      
      if (!hasCache || !newEnoughCache) {

        grouperDeprovisioningCache = new GrouperDeprovisioningCache();
        grouperDeprovisioningCache.setDeprovisionedSubjectSet(new HashSet<MultiKey>());
        grouperDeprovisioningCache.setDeprovisionedSubjectSetMap(new HashMap<MultiKey, Set<Subject>>());

        MembershipFinder membershipFinder = new MembershipFinder();
        
        Map<String, MultiKey> mapGroupNameFirstTwoKeys = new HashMap<String, MultiKey>();
        
        // add overall managers group
        String deprovisioningAdminGroupName = GrouperDeprovisioningSettings.retrieveDeprovisioningAdminGroupName();
        
        membershipFinder.addGroup(deprovisioningAdminGroupName);
        
        
        //  key is affiliation, deprovisionedGroup|inAffiliationGroup|deprovisioningAdmins, sourceId, subjectId 
        //  if affiliation is null and deprovisioningAdmins is second key then its overall admins
        
        mapGroupNameFirstTwoKeys.put(deprovisioningAdminGroupName, multiKeyMapDeprovisioningAdmins());
        
        //add affiliation groups
        for (GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().values()) {
          
          membershipFinder.addGroup(grouperDeprovisioningAffiliation.getManagersGroupName());
          mapGroupNameFirstTwoKeys.put(grouperDeprovisioningAffiliation.getManagersGroupName(), 
              multiKeyMapAffiliationAdmins(grouperDeprovisioningAffiliation.getLabel()));

          membershipFinder.addGroup(grouperDeprovisioningAffiliation.getUsersWhoHaveBeenDeprovisionedGroupName());
          mapGroupNameFirstTwoKeys.put(grouperDeprovisioningAffiliation.getUsersWhoHaveBeenDeprovisionedGroupName(), 
              multiKeyMapAffiliationDeprovisionedGroup(grouperDeprovisioningAffiliation.getLabel()));
          
          String groupNameMeansInAffiliation = grouperDeprovisioningAffiliation.getGroupNameMeansInAffiliation();
          
          if (!StringUtils.isBlank(groupNameMeansInAffiliation)) {

            membershipFinder.addGroup(groupNameMeansInAffiliation);
            mapGroupNameFirstTwoKeys.put(groupNameMeansInAffiliation, 
                multiKeyMapInAffiliationGroup(grouperDeprovisioningAffiliation));
          }
          
        }
        
        MembershipResult membershipResult = membershipFinder.assignField(Group.getDefaultList()).findMembershipResult();

        for (Object[] membershipOwnerMember : membershipResult.getMembershipsOwnersMembers()) {
          Group group = (Group)membershipOwnerMember[1];
          String groupName = group.getName();
          MultiKey firstTwoKeys = mapGroupNameFirstTwoKeys.get(groupName);
          Member member = (Member)membershipOwnerMember[2];
          grouperDeprovisioningCache.getDeprovisionedSubjectSet().add(new MultiKey(firstTwoKeys.getKey(0), 
              firstTwoKeys.getKey(1), member.getSubjectSourceId(), member.getSubjectId()));
          Set<Subject> subjects = grouperDeprovisioningCache.getDeprovisionedSubjectSetMap().get(firstTwoKeys);
          if (subjects == null) {
            subjects = new HashSet<Subject>();
            grouperDeprovisioningCache.getDeprovisionedSubjectSetMap().put(firstTwoKeys, subjects);
          }
          subjects.add(member.getSubject());
        }
        
        deprovisionedSubjectCache.put(Boolean.TRUE, grouperDeprovisioningCache);
        grouperDeprovisioningCacheFailsafe = grouperDeprovisioningCache;
        deprovisionedSubjectCacheLastRetrievedNanos = System.nanoTime();
      }
      return grouperDeprovisioningCache;
    }
  }

  /**
   * @param grouperDeprovisioningAffiliation
   * @return the multikey
   */
  private static MultiKey multiKeyMapInAffiliationGroup(
      GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation) {
    return new MultiKey(grouperDeprovisioningAffiliation.getLabel(), "inAffiliationGroup");
  }

  /**
   * @param grouperDeprovisioningAffiliation
   * @return the multikey
   */
  private static MultiKey multiKeyMapAffiliationDeprovisionedGroup(
      String grouperDeprovisioningAffiliation) {
    return new MultiKey(grouperDeprovisioningAffiliation, "deprovisionedGroup");
  }

  /**
   * @param grouperDeprovisioningAffiliation
   * @return the multikey
   */
  private static MultiKey multiKeyMapAffiliationAdmins(
      String grouperDeprovisioningAffiliation) {
    return new MultiKey(grouperDeprovisioningAffiliation, "deprovisioningAdmins");
  }

  /**
   * @param grouperDeprovisioningAffiliation
   * @param subject
   * @return the multikey
   */
  private static MultiKey multiKeySetAffiliationAdmins(
      String grouperDeprovisioningAffiliation, Subject subject) {
    return new MultiKey(grouperDeprovisioningAffiliation, "deprovisioningAdmins", subject.getSourceId(), subject.getId());
  }

  /**
   * @return the multikey for global admin
   */
  private static MultiKey multiKeyMapDeprovisioningAdmins() {
    return new MultiKey(null, "deprovisioningAdmins");
  }

  /**
   * 
   * @param affiliation
   * @param useCache
   * @return if the subject is deprovisioned
   */
  public static Set<Subject> deprovisionedSubjectsForAffiliation(String affiliation, boolean useCache) {
    
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return new HashSet<Subject>();
    }

    GrouperDeprovisioningCache grouperDeprovisioningCache = grouperDeprovisioningCache(useCache);

    MultiKey multiKey = multiKeyMapAffiliationDeprovisionedGroup(affiliation);

    return GrouperUtil.nonNull(grouperDeprovisioningCache.getDeprovisionedSubjectSetMap().get(multiKey));

  }

  /**
   * 
   * @param subject
   * @param affiliation
   * @param useCache
   * @return if the subject is deprovisioned
   */
  public static boolean deprovisionedSubject(Subject subject, String affiliation, boolean useCache) {

    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }

    GrouperDeprovisioningCache grouperDeprovisioningCache = grouperDeprovisioningCache(useCache);
    
    MultiKey multiKey = multiKeySetAffiliationDeprovisionedGroup(affiliation, subject);
    
    return grouperDeprovisioningCache.getDeprovisionedSubjectSet().contains(multiKey);
    
  }
  
  
  /**
   * 
   * @param subject
   * @param useCache
   * @return if the subject is deprovisioned
   */
  public static boolean deprovisionedSubject(Subject subject, boolean useCache) {
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }

    for (GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().values()) {

      if (deprovisionedSubject(subject, grouperDeprovisioningAffiliation.getLabel(), useCache)) {
        return true;
      }

    }
    return false;
  }
  
  /**
   * 
   */
  public GrouperDeprovisioningLogic() {
  }

  /**
   * update last certified date to now
   * @param grouperObject
   * @param lastCertifiedDate or null to remove
   */
  public static void updateLastCertifiedDate(GrouperObject grouperObject, Date lastCertifiedDate) {
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);

    for (String affiliation : grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().keySet()) {

      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);
      
      if (grouperDeprovisioningConfiguration.isHasDatabaseConfiguration() && grouperDeprovisioningConfiguration.getOriginalConfig().isDeprovision()) {
        
        grouperDeprovisioningConfiguration.getNewConfig().setCertifiedDate(lastCertifiedDate);
        grouperDeprovisioningConfiguration.storeConfiguration();
      }
      
    }
    
  }
  
  /**
   * @param membership
   */
  public static void removeAccess(final Membership membership) {
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {

        Subject subject =  membership.getMember().getSubject();

        Group ownerGroup = membership.getOwnerGroupId() != null ? membership.getOwnerGroup(): null;
        if (ownerGroup != null) {
          removeAccess(ownerGroup, subject);
        }

        AttributeDef ownerAttributeDef = membership.getOwnerAttrDefId() != null ? membership.getOwnerAttributeDef(): null;

        if (ownerAttributeDef != null) {
          removeAccess(ownerAttributeDef, subject);
        }

        Stem ownerStem = membership.getOwnerStemId() != null ? membership.getOwnerStem(): null;
        if (ownerStem != null) {
          removeAccess(ownerStem, subject);
        }
        return null;
      }
    });

  }

  /**
   * @param membership
   */
  public static void removeAccess(final Group group, final Subject subject) {
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        group.deleteMember(subject, false);
        
        for (Privilege priv: AccessPrivilege.ALL_PRIVILEGES) {
          group.revokePriv(subject, priv, false);
        }
          
        return null;
      }
    });
    
  }
  /**
   * @param attributeDef
   * @param subject
   */
  public static void removeAccess(final AttributeDef attributeDef, final Subject subject) {
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        for (Privilege priv: AttributeDefPrivilege.ALL_PRIVILEGES) {
          attributeDef.getPrivilegeDelegate().revokePriv(subject, priv, false);
        }

        return null;
      }
    });
    
  }


  /**
   * @param membership
   */
  public static void removeAccess(final Stem stem, final Subject subject) {
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
        
        for (Privilege priv: NamingPrivilege.ALL_PRIVILEGES) {
          stem.revokePriv(subject, priv, false); 
        }
        return null;
      }
    });
    
  }

  /**
   * if user is allowed to deprovision
   * @param subject
   * @return true if allowed
   */
  public static boolean allowedToDeprovision(Subject subject) {
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }

    Map<String, GrouperDeprovisioningAffiliation> map =  GrouperDeprovisioningAffiliation.retrieveAffiliationsForUserManager(subject);

    return GrouperUtil.length(map) > 0;
  }
  
  /**
   * @param grouperObject 
   */
  public static void updateDeprovisioningMetadataForSingleObject(GrouperObject grouperObject) {
    
    // dont do this now
    if (GrouperCheckConfig.isInCheckConfig()) {
      return;
    }
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(grouperObject);
    updateDeprovisioningMetadataForSingleObject(grouperObject, grouperDeprovisioningOverallConfiguration);
  }

  /**
   * @param grouperObject 
   * @param grouperDeprovisioningOverallConfiguration 
   */
  public static void updateDeprovisioningMetadataForSingleObject(GrouperObject grouperObject, GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration) {

    for (String affiliation : GrouperDeprovisioningAffiliation.retrieveAllAffiliations().keySet()) {
      
      GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().get(affiliation);

      // we good
      GrouperDeprovisioningAttributeValue originalConfig = grouperDeprovisioningConfiguration.getOriginalConfig();
      if (originalConfig != null && originalConfig.isDirectAssignment()) {
        continue;
      }
      
      grouperDeprovisioningOverallConfiguration.calculateInheritedConfig();
      
      GrouperDeprovisioningConfiguration inheritedConfiguration = grouperDeprovisioningConfiguration.getInheritedConfig();

      if (inheritedConfiguration != null && inheritedConfiguration.getOriginalConfig() != null) {

        GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = grouperDeprovisioningConfiguration.getNewConfig();
        GrouperDeprovisioningAttributeValue inheritedAttributeValue = inheritedConfiguration.getOriginalConfig();

        grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString(inheritedAttributeValue.getAllowAddsWhileDeprovisionedString());
        grouperDeprovisioningAttributeValue.setAutoChangeLoaderString(inheritedAttributeValue.getAutoChangeLoaderString());
        grouperDeprovisioningAttributeValue.setAutoselectForRemovalString(inheritedAttributeValue.getAutoselectForRemovalString());
        // dont set certified date
        grouperDeprovisioningAttributeValue.setDeprovisionString(inheritedAttributeValue.getDeprovisionString());
        grouperDeprovisioningAttributeValue.setDirectAssignment(false);
        grouperDeprovisioningAttributeValue.setEmailAddressesString(inheritedAttributeValue.getEmailAddressesString());
        grouperDeprovisioningAttributeValue.setEmailBodyString(inheritedAttributeValue.getEmailBodyString());
        grouperDeprovisioningAttributeValue.setInheritedFromFolderIdString(inheritedAttributeValue.getGrouperDeprovisioningConfiguration().getAttributeAssignBase().getOwnerStemId());
        // dont set last emailed
        grouperDeprovisioningAttributeValue.setMailToGroupString(inheritedAttributeValue.getMailToGroupString());
        grouperDeprovisioningAttributeValue.setAffiliationString(inheritedAttributeValue.getAffiliationString());
        grouperDeprovisioningAttributeValue.setSendEmailString(inheritedAttributeValue.getSendEmailString());
        grouperDeprovisioningAttributeValue.setShowForRemovalString(inheritedAttributeValue.getShowForRemovalString());
        grouperDeprovisioningAttributeValue.setStemScopeString(inheritedAttributeValue.getStemScopeString());
        
      } else {

        // there is no local config or inherited config, delete it all (well most of it)
        grouperDeprovisioningConfiguration.clearOutConfigurationButLeaveMetadata();

      }
      grouperDeprovisioningConfiguration.storeConfiguration();
      
    }

  }
  
  /**
   * go through groups and folders marked with deprovisioning metadata and make sure its up to date with inheritance
   * @param stem 
   */
  public static void updateDeprovisioningMetadata(Stem stem) {

    Map<GrouperObject, GrouperDeprovisioningOverallConfiguration> grouperDeprovisioningOverallConfigurationMap 
      = GrouperDeprovisioningOverallConfiguration.retrieveConfigurationForStem(stem, true);

    for (GrouperObject grouperObject: grouperDeprovisioningOverallConfigurationMap.keySet()) {
      GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = grouperDeprovisioningOverallConfigurationMap.get(grouperObject);
      updateDeprovisioningMetadataForSingleObject(grouperObject, grouperDeprovisioningOverallConfiguration);
    }
    
  }

  /**
   * @param grouperDeprovisioningAffiliation
   * @param subject
   * @return the multikey
   */
  private static MultiKey multiKeySetAffiliationDeprovisionedGroup(
      String grouperDeprovisioningAffiliation, Subject subject) {
    return new MultiKey(grouperDeprovisioningAffiliation, "deprovisionedGroup", subject.getSourceId(), subject.getId());
  }

  /**
   * @param subject
   * @return the multikey for global admin
   */
  private static MultiKey multiKeySetDeprovisioningAdmins(Subject subject) {
    return new MultiKey(null, "deprovisioningAdmins", subject.getSourceId(), subject.getId());
  }

  /**
   * @param grouperDeprovisioningAffiliation
   * @param subject
   * @return the multikey
   */
  private static MultiKey multiKeySetInAffiliationGroup(
      String grouperDeprovisioningAffiliation, Subject subject) {
    return new MultiKey(grouperDeprovisioningAffiliation, "inAffiliationGroup", subject.getSourceId(), subject.getId());
  }

  /**
   * 
   * @param subject
   * @param affiliation
   * @param checkOverallAdmins 
   * @param checkRootUsers 
   * @param useCache
   * @return if the subject is an affiliation admin
   */
  public static boolean affiliationAdmin(Subject subject, String affiliation, boolean checkOverallAdmins, boolean checkRootUsers, boolean useCache) {
  
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }
  
    GrouperDeprovisioningCache grouperDeprovisioningCache = grouperDeprovisioningCache(useCache);
    
    MultiKey multiKey = multiKeySetAffiliationAdmins(affiliation, subject);
    
    if (grouperDeprovisioningCache.getDeprovisionedSubjectSet().contains(multiKey)) {
      return true;
    }
    
    if (checkOverallAdmins) {
      multiKey = multiKeySetDeprovisioningAdmins(subject);
      
      if (grouperDeprovisioningCache.getDeprovisionedSubjectSet().contains(multiKey)) {
        return true;
      }
    }
    
    if (checkRootUsers) {
      if (PrivilegeHelper.isWheelOrRoot(subject)) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * get the affiliations to deprovision
   * @param owner
   * @return the affilations
   */
  public static Set<String> affiliationsToDeprovision(GrouperObject owner) {
    Set<String> affiliationsToDeprovision = new TreeSet<String>();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = 
      GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(owner);
    
    for (GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration : grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().values()) {
      
      if (grouperDeprovisioningConfiguration.getOriginalConfig() != null && grouperDeprovisioningConfiguration.getOriginalConfig().isDeprovision()) {
        affiliationsToDeprovision.add(grouperDeprovisioningConfiguration.getOriginalConfig().getAffiliationString());
      }
      
    }
    return affiliationsToDeprovision;
  }

  /**
   * subjects who are deprovisioned, on affiliations on the owner which are deprovisioning, 
   * and which are not in affilation groups of other deprovisionable groups
   * @param owner
   * @param useCache 
   * @return the subjects to query
   */
  public static Set<Subject> subjectsWhoAreDeprovisionedInRelationToOwner(GrouperObject owner, boolean useCache) {
    Set<Subject> result = new HashSet<Subject>();
    for (DeprovisionedSubject deprovisionedSubject : subjectsWhoAreDeprovisionedInRelationToOwnerWithAffiliations(owner, useCache)) {
      result.add(deprovisionedSubject.getSubject());
    }
    return result;
  }

  /**
   * subjects who are deprovisioned, on affiliations on the owner which are deprovisioning, 
   * and which are not in affilation groups of other deprovisionable groups
   * @param owner
   * @param useCache 
   * @return the subjects to query
   */
  public static Set<DeprovisionedSubject> subjectsWhoAreDeprovisionedInRelationToOwnerWithAffiliations(GrouperObject owner, boolean useCache) {
    Set<String> affiliationsToDeprovision = GrouperDeprovisioningLogic.affiliationsToDeprovision(owner);

    //get all the users who are deprovisioned
    Map<Subject, DeprovisionedSubject> subjectsWhoAreDeprovisioned = new HashMap<Subject, DeprovisionedSubject>();

    for (String affiliation : GrouperUtil.nonNull(affiliationsToDeprovision)) {

      for (Subject subject : GrouperDeprovisioningLogic.deprovisionedSubjectsForAffiliation(affiliation, useCache)) {
        
        DeprovisionedSubject deprovisionedSubject = subjectsWhoAreDeprovisioned.get(subject);
        
        if (deprovisionedSubject == null) {
          deprovisionedSubject = new DeprovisionedSubject();
          deprovisionedSubject.setSubject(subject);
          deprovisionedSubject.setAffiliations(new TreeSet<String>());
          subjectsWhoAreDeprovisioned.put(subject, deprovisionedSubject);
        }
        deprovisionedSubject.getAffiliations().add(affiliation);
      }

    }
    
    //see if any of these subjects are in affiliation groups
    Iterator<Map.Entry<Subject, DeprovisionedSubject>> iterator = subjectsWhoAreDeprovisioned.entrySet().iterator();
    while (iterator.hasNext()) {

      Map.Entry<Subject, DeprovisionedSubject> entry = iterator.next();
      Subject subject = entry.getKey();
      
      //go through affiliations
      for (String affiliation : GrouperUtil.nonNull(affiliationsToDeprovision)) {
        
        // if deprovisioning this user, then deprovision
        if (!GrouperDeprovisioningLogic.deprovisionedSubject(subject, affiliation, useCache)) {
          
          // if in an affiliation group of an affiliation which is deprovisioned, then dont remove the user
          if (GrouperDeprovisioningLogic.inAffiliationGroup(subject, affiliation, useCache)) {
            iterator.remove();
          }
        }
      }
    }
    
    return new HashSet<DeprovisionedSubject>(subjectsWhoAreDeprovisioned.values());
    
  }
  
  /**
   * 
   * @param subject
   * @param affiliation
   * @param useCache
   * @return if the subject is deprovisioned
   */
  public static boolean inAffiliationGroup(Subject subject, String affiliation, boolean useCache) {
  
    if (!GrouperDeprovisioningSettings.deprovisioningEnabled()) {
      return false;
    }
  
    GrouperDeprovisioningCache grouperDeprovisioningCache = grouperDeprovisioningCache(useCache);
    
    MultiKey multiKey = multiKeySetInAffiliationGroup(affiliation, subject);
    
    return grouperDeprovisioningCache.getDeprovisionedSubjectSet().contains(multiKey);
    
  }
  
  /**
   * 
   * @param grouperSession
   * @param loaderGroup
   * @param subject
   * @return true if the subject has not been deprovisioned or autoChangeLoader is set to false
   *  or some other affiliation's groupNameMeansInAffiliation contains this subject
   */
  public static boolean shouldAddSubject(GrouperSession grouperSession, Group loaderGroup, Subject subject) {
    
    Map<String, GrouperDeprovisioningAffiliation> allAffiliations = GrouperDeprovisioningAffiliation.retrieveAllAffiliations();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(loaderGroup);
    
    Set<String> affiliationsToDeprovision = GrouperDeprovisioningLogic.affiliationsToDeprovision(loaderGroup);
    
    for (String affiliation: affiliationsToDeprovision) {
      
      Set<Subject> subjectsForAffiliation = GrouperDeprovisioningLogic.deprovisionedSubjectsForAffiliation(affiliation, true);
      
      if (subjectsForAffiliation.contains(subject)) {
        boolean autoChangeLoader = grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration()
            .get(affiliation).getOriginalConfig().isAutoChangeLoader();
        
        if (!autoChangeLoader) {
          return true;
        }
        
        boolean subjectInAnotherGroupMeansInAffiliation = false;
        
        // go through the rest of the affiliations and if subject is member of groupNameMeansInAffiliation 
        // then subject should still be added
        Set<String> affiliationsToDeprovisionRest = GrouperDeprovisioningLogic.affiliationsToDeprovision(loaderGroup);
        affiliationsToDeprovisionRest.remove(affiliation);
        
        for (String affiliationToCheck: affiliationsToDeprovisionRest) {
          GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation = allAffiliations.get(affiliationToCheck);
          if (StringUtils.isNotBlank(grouperDeprovisioningAffiliation.getGroupNameMeansInAffiliation())) {
            String groupNameMeansInAffiliation = grouperDeprovisioningAffiliation.getGroupNameMeansInAffiliation();
            Group groupMeansInAffiliation = GroupFinder.findByName(grouperSession, groupNameMeansInAffiliation, false);
            if (groupMeansInAffiliation != null && groupMeansInAffiliation.hasMember(subject)) {
              subjectInAnotherGroupMeansInAffiliation = true;
            }
          }
        }
        
        if (!subjectInAnotherGroupMeansInAffiliation) {
          return false;
        }
      }
      
    }
    
    return true;

  }

}
