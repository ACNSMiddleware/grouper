package edu.internet2.middleware.grouper.rules;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * definition for the subject act as for a rule
 * @author mchyzer
 *
 */
public class RuleSubjectActAs {

  /**
   * 
   * @param subjectId
   * @param sourceId
   * @param subjectIdentifier
   */
  public RuleSubjectActAs(String subjectId, String sourceId, String subjectIdentifier) {
    super();
    this.subjectId = subjectId;
    this.sourceId = sourceId;
    this.subjectIdentifier = subjectIdentifier;
  }

  /**
   * 
   */
  public RuleSubjectActAs() {

  }
  
  /** subject id to act as */
  private String subjectId;
  
  /** source id to act as */
  private String sourceId;
  
  /** subject identifier to act as */
  private String subjectIdentifier;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleSubjectActAs.class);

  /**
   * subject id to act as
   * @return subject id to act as
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id to act as
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source id to act as
   * @return source id to act as
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * source id to act as
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * subject identifier to act as
   * @return subject id to act as
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }

  /**
   * subject identifier to act as
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    toStringHelper(result);
    return result.toString();
  }
  
  /**
   * 
   * @param result
   */
  void toStringHelper(StringBuilder result) {
    if (!StringUtils.isBlank(this.sourceId)) {
      result.append("sourceId: ").append(this.sourceId).append(", ");
    }
    if (!StringUtils.isBlank(this.subjectId)) {
      result.append("subjectId: ").append(this.subjectId).append(", ");
    }
    if (!StringUtils.isBlank(this.subjectIdentifier)) {
      result.append("subjectIdentifier: ").append(this.subjectIdentifier).append(", ");
    }
  }
  
  /**
   * resolve the subject
   * @param exceptionIfNotFound true if exception if not found
   * @return the subject
   */
  public Subject subject(boolean exceptionIfNotFound) {
    
    if (!StringUtils.isBlank(this.sourceId)) {
      
      if (!StringUtils.isBlank(this.subjectId)) {
        return SubjectFinder.findByIdAndSource(this.subjectId, this.sourceId, exceptionIfNotFound);
      }
      
      if (!StringUtils.isBlank(this.subjectIdentifier)) {
        return SubjectFinder.findByIdentifierAndSource(this.subjectIdentifier, this.sourceId, exceptionIfNotFound);
      }
      
    }
    //no source
    if (!StringUtils.isBlank(this.subjectId)) {
      return SubjectFinder.findById(this.subjectId, exceptionIfNotFound);
    }
    
    if (!StringUtils.isBlank(this.subjectIdentifier)) {
      return SubjectFinder.findByIdentifier(this.subjectIdentifier, exceptionIfNotFound);
    }
    
    if (exceptionIfNotFound) {
      throw new RuntimeException("Cant find subject: " + this);
    }
    return null;
  }
  
  /**
   * validate this 
   * @return error or null if ok
   */
  public String validate() {
    if (StringUtils.isBlank(this.subjectId) ==  StringUtils.isBlank(this.subjectIdentifier)) {
      return "Enter one and only one of actAsSubjectId and actAsSubjectIdentifier!";
    }
    //lets see what the subject is
    Subject subject = this.subject(false);
    if (subject == null) {
      return "Cant find subject: " + this;
    }
    
    //make sure can act as
    if (!allowedToActAs(GrouperSession.staticGrouperSession().getSubject(), subject)) {
      return "Subject: " 
        + GrouperUtil.subjectToString(GrouperSession.staticGrouperSession().getSubject())
        + " cannot act as subject: " + GrouperUtil.subjectToString(subject) + " based on grouper.properties: "
        + " rules.act.as.group";
    }
    
    return null;
  }
  
  /**
   * @return act as cache minutes
   */
  private static int actAsCacheMinutes() {
    int actAsTimeoutMinutes = GrouperConfig.getPropertyInt(
        "rules.act.as.cache.minutes", 30);
    return actAsTimeoutMinutes;
  }


  /** cache the actAs */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache = null;
  
  /**
   * get the subjectAllowedCache, and init if not initted
   * @return the subjectAllowedCache
   */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache() {
    if (subjectAllowedCache == null) {
      
      subjectAllowedCache = new GrouperCache<MultiKey, Boolean>(
          RuleSubjectActAs.class.getName() + "subjectAllowedCache", 1000, false, 
          60*60*24, actAsCacheMinutes()*60, false);
  
    }
    return subjectAllowedCache;
  }
  
  /** when packing things in a single param, this is the separator */
  public static final String ACT_AS_SEPARATOR = "::::";


  /**
   * see if a subject can act as another subject
   * @param subject
   * @param subjectToActAs
   * @return true if a subject can act as another subject
   */
  public static boolean allowedToActAs(final Subject subject, final Subject subjectToActAs) {

    if (subject == null || subjectToActAs == null) {
      throw new RuntimeException("Need to pass in subject and subjectToActAs");
    }
  
    //if they equal each other, its ok
    if (SubjectHelper.eq(subject, subjectToActAs)) {
      return true;
    }

    //if we are GrouperSystem, let that go through
    if (SubjectHelper.eq(SubjectFinder.findRootSubject(), subject)) {
      return true;
    }

    
    //lets see if in cache    
    //cache key to get or set if a user can act as another

    GrouperSession staticGrouperSession = GrouperSession.staticGrouperSession();
    GrouperSession internalRootSession = staticGrouperSession.internal_getRootSession();
    return (Boolean)GrouperSession.callbackGrouperSession(internalRootSession, new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        //make sure allowed
        String actAsGroupName = GrouperConfig.getProperty("rules.act.as.group");
        
        if (StringUtils.isBlank(actAsGroupName)) {
          return false;
        }

        MultiKey cacheKey = new MultiKey(subject.getId(), subject.getSourceId(), 
            subjectToActAs.getId(), subjectToActAs.getSourceId());
      
        Boolean inCache = null;
      
        if (actAsCacheMinutes() > 0) {
          inCache = subjectAllowedCache().get(cacheKey);
        } else {
          inCache = false;
        }
      
        if (inCache != null && Boolean.TRUE.equals(inCache)) {
          //if in cache and true, then allow
          return true;
        }

        //first separate by comma
        String[] groupEntries = GrouperUtil.splitTrim(actAsGroupName, ",");
    
        //see if all throw exceptions
        int countNoExceptions = 0;
    
        //we could also cache which entries the user is in...  not sure how many entries will be here
        for (String groupEntry : groupEntries) {
    
          //each entry should be failsafe
          try {
            //now see if it is a multi input
            if (StringUtils.contains(groupEntry, ACT_AS_SEPARATOR)) {
    
              //it is the group the user is in, and the group the act as has to be in
              String[] groupEntryArray = GrouperUtil.splitTrim(groupEntry,
                  ACT_AS_SEPARATOR);
              String userMustBeInGroupName = groupEntryArray[0];
              String actAsMustBeInGroupName = groupEntryArray[1];
    
              Group userMustBeInGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(),
                  userMustBeInGroupName, true);
              Group actAsMustBeInGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(),
                  actAsMustBeInGroupName, true);
    
              if (userMustBeInGroup.hasMember(subject)
                  && actAsMustBeInGroup.hasMember(subjectToActAs)) {
                //its ok, lets add to cache
                subjectAllowedCache().put(cacheKey, Boolean.TRUE);
                return true;
              }
    
            } else {
              //else this is a straightforward rule where the logged in user just has to be in a group and
              //can act as anyone
              Group actAsGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupEntry, true);
    
              // if the logged in user is a member of the actAs group, then allow
              // the actAs
              if (actAsGroup.hasMember(subject)) {
                //its ok, lets add to cache
                subjectAllowedCache().put(cacheKey, Boolean.TRUE);
                // this is the subject the rule wants to use
                return true;
              }
            }
            countNoExceptions++;
          } catch (Exception e) {
            //just log and dont act since other entries could be fine
            LOG.error("Problem with groupEntry: " + groupEntry + ", subject: "
                + subject + ", actAsSubject: " + subjectToActAs, e);
          }

        }

        if (countNoExceptions == 0) {
          return false;
        }
        // if not an effective member
        LOG.error(
            "A rule is specifying an actAsUser, but the groups specified in "
                + " rules.act.as.group in the grouper.properties "
                + " does not have a valid rule for member: '" + GrouperUtil.subjectToString(subject)
                + "', and actAs: '" + GrouperUtil.subjectToString(subjectToActAs) + "'");
        
        return false;
      }
    });
  

  }
}
