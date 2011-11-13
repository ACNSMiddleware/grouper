package edu.internet2.middleware.grouper.subj;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * filter out subjects not in the collaboration group
 * @author mchyzer
 *
 */
public class SubjectCustomizerForDecoratorTesting2 extends SubjectCustomizerBase {

  /** student (protected data) group name */
  private static final String COLLAB_STEM_NAME = "collaboration:collabGroups";

  /** privileged employee group name */
  private static final String PRIVILEGED_ADMIN_GROUP_NAME = "collaboration:etc:privilegedAdmin";

  /** source id we care about */
  private static final String SOURCE_ID = "jdbc";

  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Set, String)
   */
  @Override
  public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects, String findSubjectsInStemName) {
    
    //nothing to do if no results
    if (GrouperUtil.length(subjects) == 0) {
      return subjects;
    }
    
    Stem stem = StemFinder.findByName(grouperSession.internal_getRootSession(), COLLAB_STEM_NAME, true);
    
    //get results in one query
    MembershipResult groupMembershipResult = new MembershipFinder().assignCheckSecurity(false).addGroup(PRIVILEGED_ADMIN_GROUP_NAME)
      .addSubjects(subjects).addSubject(grouperSession.getSubject()).assignStem(stem).assignStemScope(Scope.SUB)
      .findMembershipResult(); 
        
    //see if the user is privileged
    boolean grouperSessionIsPrivileged = groupMembershipResult.hasGroupMembership(PRIVILEGED_ADMIN_GROUP_NAME, grouperSession.getSubject());
    
    //if so, we are done, they can see stuff
    if (grouperSessionIsPrivileged) {
      return subjects;
    }
    
    //get the group names that the grouper session subject is in
    Set<String> grouperSessionGroupNames = groupMembershipResult.groupNamesInStem(grouperSession.getSubject(), COLLAB_STEM_NAME);
    
    //loop through the subjects and see which collab groups the users are in
    Set<Subject> results = new LinkedHashSet<Subject>();
    for (Subject subject : subjects) {
      //only protect one source
      if (!StringUtils.equals(SOURCE_ID, subject.getSourceId())) {
        results.add(subject);
      } else {
        Set<String> subjectGroupNames = groupMembershipResult.groupNamesInStem(subject, COLLAB_STEM_NAME);
        if (CollectionUtils.containsAny(grouperSessionGroupNames, subjectGroupNames)) {
          results.add(subject);
        }
      }
    }
    return results;
  }

  
  
}
