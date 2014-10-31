/**
 * 
 */
package edu.internet2.middleware.grouperVoot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperVoot.beans.VootGetGroupsResponse;
import edu.internet2.middleware.grouperVoot.beans.VootGetMembersResponse;
import edu.internet2.middleware.grouperVoot.beans.VootGroup;
import edu.internet2.middleware.grouperVoot.beans.VootPerson;
import edu.internet2.middleware.subject.Subject;


/**
 * business logic for voot
 * @author mchyzer
 * @author Andrea Biancini <andrea.biancini@gmail.com>
 *
 */
public class VootLogic {

  /**
   * Helper for find by approximate name queries
   * 
   * This isnt in 2.0.0
   * @param name
   * @param scope
   * @param currentNames
   * @param alternateNames
   * @param queryOptions 
   * @param typeOfGroups
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   */
  private static Set<Group> findAllByApproximateNameSecureHelper(final String name, final String scope,
      final boolean currentNames, final boolean alternateNames, final QueryOptions queryOptions, final Set<TypeOfGroup> typeOfGroups)
      throws GrouperDAOException {
    @SuppressWarnings("unchecked")
	Set<Group> resultGroups = (Set<Group>) HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            StringBuilder hql = new StringBuilder("select distinct theGroup from Group theGroup ");
      
            ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                grouperSession.getSubject(), byHqlStatic, 
                hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
          
            if (!changedQuery) {
              hql.append(" where ");
            } else {
              hql.append(" and ");
            }
            String lowerName = StringUtils.defaultString(name).toLowerCase();
            hql.append(" ( ");
            if (currentNames) {
              hql.append(" lower(theGroup.nameDb) like :theName or lower(theGroup.displayNameDb) like :theDisplayName ");
              byHqlStatic.setString("theName", "%" + lowerName + "%");
              byHqlStatic.setString("theDisplayName", "%" + lowerName + "%");
            } 
  
            if (alternateNames) {
              if (currentNames) {
                hql.append(" or ");
              }
              hql.append(" theGroup.alternateNameDb like :theAlternateName ");
              byHqlStatic.setString("theAlternateName", "%" + lowerName + "%");
            }
            
            hql.append(" ) ");
            
            if (scope != null) {
              hql.append(" and theGroup.nameDb like :theStemScope ");
              byHqlStatic.setString("theStemScope", scope + "%");
            }

            //add in the typeOfGroups part
            appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
            
            byHqlStatic.setCacheable(false);

            //reset sorting
            if (queryOptions != null) {
                            
              byHqlStatic.options(queryOptions);
            }
            
            byHqlStatic.createQuery(hql.toString());
            Set<Group> groups = byHqlStatic.listSet(Group.class);
            
            return groups;
          }
    });
    return resultGroups;
  }

  /**
   * note: this isnt in 2.0.0
   * @param groupAlias is the alias in the group hql query e.g. theGroup
   * @param typeOfGroups the set of TypeOfGroup or null for all
   * @param hql query so far
   * @param hqlQuery object to append the stored params to
   */
  private static void appendHqlQuery(String groupAlias, Set<TypeOfGroup> typeOfGroups, StringBuilder hql, HqlQuery hqlQuery) {
    if (GrouperUtil.length(typeOfGroups) > 0) {
      if (hql.indexOf(" where ") > 0) {
        hql.append(" and ");
      } else {
        hql.append(" where ");
      }
      hql.append(groupAlias).append(".typeOfGroupDb in ( ");
      Set<String> typeOfGroupStrings = new LinkedHashSet<String>();
      for (TypeOfGroup typeOfGroup : typeOfGroups) {
        typeOfGroupStrings.add(typeOfGroup.name());
      }
      hql.append(HibUtils.convertToInClause(typeOfGroupStrings, hqlQuery));
      hql.append(" ) ");
    }

  }
  
  /**
   * get the members for a group based on a group
   * @param subject
   * @param vootGroup
   * @return the response
   */
  public static VootGetMembersResponse getMembers(Subject subject, VootGroup vootGroup, String sortBy, int start, int count) {
	GrouperSession session = GrouperSession.staticGrouperSession();
	// Non using session created by subject because findByName will only retrieve
	// groups where the subject is administrator
	//if (!GrouperSession.staticGrouperSession().getSubject().equals(subject)) {
	//  session = GrouperSession.start(subject, false);
	//}
	  
	//note the name is the id
    String groupName = vootGroup.getId();
    
    //throws exception if the group is not found
    Group group = GroupFinder.findByName(session, groupName, true);
    
    Set<Subject> memberSubjects = new HashSet<Subject>();
    
    Set<Member> members = group.getMembers();
    for (Member member : members) {
      memberSubjects.add(member.getSubject());
    }
      
    Set<Subject> admins = group.getAdmins();
    Set<Subject> updaters = group.getUpdaters();
    
    //lets keep track of the subjects
    //since subjects have a composite key, then keep track with multikey
    Map<MultiKey, Subject> multiKeyToSubject = new HashMap<MultiKey, Subject>();

    //member, admin, manager
    Map<MultiKey, String> memberToRole = new HashMap<MultiKey, String>();
    
    boolean subjectInGroup = false;
    for (Subject curSubject : memberSubjects) {
      if (curSubject.getSourceId().equals(subject.getSourceId()) && curSubject.getId().equals(subject.getId())) subjectInGroup = true;
      MultiKey subjectMultiKey = new MultiKey(curSubject.getSourceId(), curSubject.getId());
      multiKeyToSubject.put(subjectMultiKey, curSubject);
      memberToRole.put(subjectMultiKey, "admin");
    }
    for (Subject curSubject : updaters) {
      if (curSubject.getSourceId().equals(subject.getSourceId()) && curSubject.getId().equals(subject.getId())) subjectInGroup = true;
      MultiKey subjectMultiKey = new MultiKey(subject.getSourceId(), curSubject.getId());
      multiKeyToSubject.put(subjectMultiKey, curSubject);
      memberToRole.put(subjectMultiKey, "manager");
    }
    for (Subject curSubject : admins) {
      if (curSubject.getSourceId().equals(subject.getSourceId()) && curSubject.getId().equals(subject.getId())) subjectInGroup = true;
      MultiKey subjectMultiKey = new MultiKey(curSubject.getSourceId(), curSubject.getId());
      multiKeyToSubject.put(subjectMultiKey, curSubject);
      memberToRole.put(subjectMultiKey, "member");
    }
    
    if (!GrouperSession.staticGrouperSession().getSubject().equals(subject) && !subjectInGroup) {
    	throw new GroupNotFoundException(E.GROUP_NOTFOUND + " by name: " + groupName);
    }
    
    VootGetMembersResponse vootGetMembersResponse = new VootGetMembersResponse();
    VootPerson[] result = new VootPerson[memberToRole.size()];    

    //lets put them all back and make the person subjects
    int index = 0;
    for (MultiKey multiKey : memberToRole.keySet()) {
      Subject curSubject = multiKeyToSubject.get(multiKey);
      String role = memberToRole.get(multiKey);
      VootPerson vootPerson = new VootPerson(curSubject);
      vootPerson.setVoot_membership_role(role);
      result[index] = vootPerson;
      
      index++;
    }

    result = VootGetMembersResponse.sort(result, sortBy);
    vootGetMembersResponse.paginate(result, start, count);
    vootGetMembersResponse.setEntry(result, start, count);
    return vootGetMembersResponse;
  }
  
  /**
   * get the groups that a person is in
   * @param subject
   * @return the groups
   */
  public static VootGetGroupsResponse getGroups(Subject subject, String sortBy, int start, int count) {
	GrouperSession session = GrouperSession.staticGrouperSession();
	if (!GrouperSession.staticGrouperSession().getSubject().equals(subject)) {
	  session = GrouperSession.start(subject, false);
	}
    
    Member member = MemberFinder.findBySubject(session, subject, false);
    
    VootGetGroupsResponse vootGetGroupsResponse = new VootGetGroupsResponse();
    
    if (member == null) {
      vootGetGroupsResponse.paginate(null, start, count);
      return vootGetGroupsResponse;
    }
    
    //member, admin, manager
    Set<Group> groups = member.getGroups();
    Set<Group> admins = member.getGroups(FieldFinder.find("admins", true));
    Set<Group> updaters = member.getGroups(FieldFinder.find("updaters", true));
    
    Map<Group, String> groupToRole = new TreeMap<Group, String>();
    
    //if you are a member, and not an admin or updater, then you are a member
    for (Group group : GrouperUtil.nonNull(groups)) {
      groupToRole.put(group, "member");
    }

    //if you are an updater and not an admin, then you are a manager
    for (Group group : GrouperUtil.nonNull(updaters)) {
      groupToRole.put(group, "manager");
    }
    
    //if you are an admin, then you are an admin
    for (Group group : GrouperUtil.nonNull(admins)) {
      groupToRole.put(group, "admin");
    }
    
    
    if (groupToRole.size() == 0) {
      vootGetGroupsResponse.paginate(null, start, count);
      return vootGetGroupsResponse;
    }
    
    VootGroup[] result = new VootGroup[groupToRole.size()];
    
    int index = 0;
    for (Group group : groupToRole.keySet()) {
      
      VootGroup vootGroup = new VootGroup(group);
      vootGroup.setVoot_membership_role(groupToRole.get(group));
      
      result[index] = vootGroup;
      
      index++;
    }
    
    result = VootGetGroupsResponse.sort(result, sortBy);
    vootGetGroupsResponse.paginate(result, start, count);
    vootGetGroupsResponse.setEntry(result, start, count);
    return vootGetGroupsResponse;
  }
  
  /**
   * Get the groups that a person is in.
   * @return the groups
   */
  public static VootGetGroupsResponse getGroups(String sortBy, int start, int count) {
	  return getGroups((String) null, sortBy, start, count);
  }
  
  /**
   * Get the groups that a person is in, searching by their name.
   * @param search the search term to be searched in group name 
   * @return the groups
   */
  public static VootGetGroupsResponse getGroups(String search, String sortBy, int start, int count) {
    VootGetGroupsResponse vootGetGroupsResponse = new VootGetGroupsResponse();
    
    //this isnt in 2.0.0
    String searchString = "%";
    if (search != null) {
    	searchString = "%" + search + "%";
    }
    Set<Group> groups = findAllByApproximateNameSecureHelper(searchString, null, true, true, null, null);
    
    if (GrouperUtil.length(groups) == 0) {
      vootGetGroupsResponse.paginate(null, start, count);
      return vootGetGroupsResponse;
    }
    
    VootGroup[] result = new VootGroup[groups.size()];
    
    int index = 0;
    for (Group group : groups) {
      VootGroup vootGroup = new VootGroup(group);
      result[index] = vootGroup;
      index++;
    }
    
    result = VootGetGroupsResponse.sort(result, sortBy);
    vootGetGroupsResponse.paginate(result, start, count);
    vootGetGroupsResponse.setEntry(result, start, count);
    return vootGetGroupsResponse;
  }
}
