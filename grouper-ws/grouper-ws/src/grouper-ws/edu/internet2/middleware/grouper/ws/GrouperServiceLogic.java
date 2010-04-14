/*nerer
 * @author mchyzer $Id: GrouperServiceLogic.java,v 1.41 2009/12/30 07:07:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignOperation;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueOperation;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.filter.QueryFilter;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.GrouperPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.NamingResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeType;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WebServiceDoneException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.member.WsMemberFilter;
import edu.internet2.middleware.grouper.ws.query.StemScope;
import edu.internet2.middleware.grouper.ws.query.WsQueryFilterType;
import edu.internet2.middleware.grouper.ws.query.WsStemQueryFilterType;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsAssignAttributeLogic;
import edu.internet2.middleware.grouper.ws.rest.subject.TooManyResultsWhenFilteringByGroupException;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributeResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeAssignValue;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAttributeDefNameLookup;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupToSave;
import edu.internet2.middleware.grouper.ws.soap.WsGrouperPrivilegeResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubject;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResult;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipAnyLookup;
import edu.internet2.middleware.grouper.ws.soap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.soap.WsParam;
import edu.internet2.middleware.grouper.ws.soap.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsStem;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.soap.WsStemQueryFilter;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemToSave;
import edu.internet2.middleware.grouper.ws.soap.WsSubject;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResult.WsAddMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults.WsAddMemberResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesResults.WsAssignAttributesResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResult.WsAssignGrouperPrivilegesResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResults.WsAssignGrouperPrivilegesResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResult.WsDeleteMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults.WsFindGroupsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults.WsFindStemsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults.WsGetAttributeAssignmentsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsGetGrouperPrivilegesLiteResult.WsGetGrouperPrivilegesLiteResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults.WsGetMembershipsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsGetSubjectsResults.WsGetSubjectsResultsCode;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult.WsGroupDeleteResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup.GroupFindResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResult.WsGroupSaveResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResult.WsHasMemberResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResult.WsMemberChangeSubjectResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResult.WsStemDeleteResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsStemLookup.StemFindResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResult.WsStemSaveResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup.MemberFindResult;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Meant to be delegate from GrouperService which has the same params (and names)
 * with enums translated (for Simple objects like Field) for each Javadoc viewing.
 */
public class GrouperServiceLogic {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(GrouperServiceLogic.class);

  /**
   * add member to a group (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   *            group to add the members to
   * @param subjectLookups
   *            subjects to be added to the group
   * @param replaceAllExisting
   *            optional: T or F (default), if the existing groups should be
   *            replaced
   * @param actAsSubjectLookup
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list).  by fieldName
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params
   *            optional: reserved for future use
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @return the results.  return the subject lookup only if there are problems retrieving the subject.
   * @see GrouperWsVersion
   */
  @SuppressWarnings("unchecked")
  public static WsAddMemberResults addMember(final GrouperWsVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final boolean replaceAllExisting, final WsSubjectLookup actAsSubjectLookup,
      final Field fieldName, GrouperTransactionType txType,
      final boolean includeGroupDetail, final boolean includeSubjectDetail,
      final String[] subjectAttributeNames, final WsParam[] params) {
    final WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();

    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 100) + "\n, replaceAllExisting: "
          + replaceAllExisting + ", actAsSubject: " + actAsSubjectLookup
          + ", fieldName: " + GrouperServiceUtils.fieldName(fieldName) + ", txType: "
          + txType + ", includeGroupDetail: " + includeGroupDetail
          + ", includeSubjectDetail: " + includeSubjectDetail
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames, 50) + "\n, params: "
          + GrouperUtil.toStringForLog(params, 100);

      final String THE_SUMMARY = theSummary;
      
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      final GrouperSession SESSION = session;

      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);

              int subjectLength = GrouperUtil.length(subjectLookups);

              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");

              String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
                  .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

              wsAddMemberResults
                  .setSubjectAttributeNames(subjectAttributeNamesToRetrieve);

              //assign the group to the result to be descriptive
              wsAddMemberResults
                  .setWsGroupAssigned(new WsGroup(group, wsGroupLookup, includeGroupDetail));

              int resultIndex = 0;

              Set<MultiKey> newSubjects = new HashSet<MultiKey>();
              
              if (subjectLength > 0) {
                wsAddMemberResults.setResults(new WsAddMemberResult[subjectLength]);
              }

              //get existing members if replacing
              Set<Member> members = null;
              if (replaceAllExisting) {
                try {
                  // see who is there
                  members = fieldName == null ? group.getImmediateMembers() : group
                      .getImmediateMembers(fieldName);
                } catch (SchemaException se) {
                  throw new WsInvalidQueryException(
                      "Problem with getting existing members: " + fieldName + ".  "
                          + ExceptionUtils.getFullStackTrace(se));
                }
              }

              for (WsSubjectLookup wsSubjectLookup : GrouperUtil.nonNull(subjectLookups, WsSubjectLookup.class)) {
                WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
                wsAddMemberResults.getResults()[resultIndex++] = wsAddMemberResult;
                try {

                  Subject subject = wsSubjectLookup.retrieveSubject();

                  wsAddMemberResult.processSubject(wsSubjectLookup,
                      subjectAttributeNamesToRetrieve);

                  if (subject == null) {
                    continue;
                  }

                  // keep track
                  if (replaceAllExisting) {
                    newSubjects.add(new MultiKey(subject.getId(), subject.getSource().getId()));
                  }

                  try {
                    boolean didntAlreadyExist = false;
                    if (fieldName == null) {
                      // dont fail if already a direct member
                      didntAlreadyExist = group.addMember(subject, false);
                    } else {
                      didntAlreadyExist = group.addMember(subject, fieldName, false);
                    }
                    
                    wsAddMemberResult.assignResultCode(clientVersion.addMemberSuccessResultCode(didntAlreadyExist));

                  } catch (InsufficientPrivilegeException ipe) {
                    wsAddMemberResult
                        .assignResultCode(WsAddMemberResultCode.INSUFFICIENT_PRIVILEGES);
                  }
                } catch (Exception e) {
                  wsAddMemberResult.assignResultCodeException(e, wsSubjectLookup);
                }
              }

              // after adding all these, see if we are removing:
              if (replaceAllExisting) {

                for (Member member : members) {
                  Subject subject = null;
                  try {
                    subject = member.getSubject();

                    if (!newSubjects.contains(new MultiKey(subject.getId(), subject.getSource().getId()))) {
                      if (fieldName == null) {
                        group.deleteMember(subject);
                      } else {
                        group.deleteMember(subject, fieldName);
                      }
                    }
                  } catch (Exception e) {
                    String theError = "Error deleting subject: " + subject
                        + " from group: " + group + ", field: "
                        + GrouperServiceUtils.fieldName(fieldName) + ", " + e + ".  ";
                    wsAddMemberResults.assignResultCodeException(
                        WsAddMemberResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
                  }
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsAddMemberResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }

              return wsAddMemberResults;

            }

          });
    } catch (Exception e) {
      wsAddMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }

    //this should be the first and only return, or else it is exiting too early
    return wsAddMemberResults;
  }

  /**
   * add member to a group (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to add (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to add (mutually exclusive with subjectId)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   *  of the group (certain list)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsAddMemberLiteResult addMemberLite(
      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      Field fieldName, boolean includeGroupDetail, boolean includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {

    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);

    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);


    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsAddMemberResults wsAddMemberResults = addMember(clientVersion, wsGroupLookup,
        subjectLookups, false, actAsSubjectLookup, fieldName, null, includeGroupDetail,
        includeSubjectDetail, subjectAttributeArray, params);

    WsAddMemberLiteResult wsAddMemberLiteResult = new WsAddMemberLiteResult(
        wsAddMemberResults);
    return wsAddMemberLiteResult;
  }

  /**
   * remove member(s) from a group (if not already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   * @param subjectLookups
   *            subjects to be deleted to the group
   * @param actAsSubjectLookup
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsDeleteMemberResults deleteMember(final GrouperWsVersion clientVersion,
      final WsGroupLookup wsGroupLookup, final WsSubjectLookup[] subjectLookups,
      final WsSubjectLookup actAsSubjectLookup, final Field fieldName,
      GrouperTransactionType txType, final boolean includeGroupDetail, 
      final boolean includeSubjectDetail, String[] subjectAttributeNames, 
      final WsParam[] params) {
  
    final WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);
      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 100) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName + ", txType: " + txType
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
      
      final String THE_SUMMARY = theSummary;
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsDeleteMemberResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  subjectLookups, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000, "subjectLookups");
  
              Group group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
  
              //assign the group to the result to be descriptive
              wsDeleteMemberResults.setWsGroup(new WsGroup(group, wsGroupLookup,
                  includeGroupDetail));
  
              wsDeleteMemberResults.setResults(new WsDeleteMemberResult[subjectLength]);
  
              int resultIndex = 0;
  
              //loop through all subjects and do the delete
              for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
                WsDeleteMemberResult wsDeleteMemberResult = new WsDeleteMemberResult();
                wsDeleteMemberResults.getResults()[resultIndex++] = wsDeleteMemberResult;
                try {
  
                  Subject subject = wsSubjectLookup.retrieveSubject();
                  wsDeleteMemberResult.processSubject(wsSubjectLookup, subjectAttributeNamesToRetrieve);
  
                  if (subject == null) {
                    continue;
                  }
  
                  try {
  
                    boolean hasImmediate = false;
                    boolean hasEffective = false;
                    if (fieldName == null) {
                      // dont fail if already a direct member
                      hasEffective = group.hasEffectiveMember(subject);
                      hasImmediate = group.hasImmediateMember(subject);
                      if (hasImmediate) {
                        group.deleteMember(subject);
                      }
                    } else {
                      // dont fail if already a direct member
                      hasEffective = group.hasEffectiveMember(subject, fieldName);
                      hasImmediate = group.hasImmediateMember(subject, fieldName);
                      if (hasImmediate) {
                        group.deleteMember(subject, fieldName);
                      }
                      }
                    if (LOG.isDebugEnabled()) {
                      LOG.debug("deleteMember: " + group.getName() + ", " + subject.getId() + ", eff? " + hasEffective + ", imm? " + hasImmediate);
                    }
  
                    //assign one of 4 success codes
                    wsDeleteMemberResult.assignResultCodeSuccess(hasImmediate,
                        hasEffective);
  
                  } catch (InsufficientPrivilegeException ipe) {
                    wsDeleteMemberResult
                        .assignResultCode(WsDeleteMemberResultCode.INSUFFICIENT_PRIVILEGES);
                  }
                } catch (Exception e) {
                  wsDeleteMemberResult.assignResultCodeException(e, wsSubjectLookup);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsDeleteMemberResults
                  .tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsDeleteMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsDeleteMemberResults;
  }

  /**
   * delete member to a group (if not already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to lookup the subject (mutually exclusive with
   *            subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to lookup the subject (mutually exclusive with subjectId)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member delete
   */
  public static WsDeleteMemberLiteResult deleteMemberLite(final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, String subjectId, String subjectSourceId,
      String subjectIdentifier, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, final Field fieldName,
      final boolean includeGroupDetail, boolean includeSubjectDetail,
      String subjectAttributeNames, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsDeleteMemberResults wsDeleteMemberResults = deleteMember(clientVersion,
        wsGroupLookup, subjectLookups, actAsSubjectLookup, fieldName, null,
        includeGroupDetail, includeSubjectDetail, subjectAttributeArray, params);
  
    return new WsDeleteMemberLiteResult(wsDeleteMemberResults);
  }

  /**
   * find a group or groups
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsQueryFilter is the filter properties that can search by
   * name, uuid, attribute, type, and can do group math on multiple operations, etc
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @param wsGroupLookups if you want to just pass in a list of uuids and/or names.  Note the groups are returned
   * in alphabetical order
   * @return the groups, or no groups if none found
   */
  @SuppressWarnings("unchecked")
  public static WsFindGroupsResults findGroups(final GrouperWsVersion clientVersion,
      WsQueryFilter wsQueryFilter, 
      WsSubjectLookup actAsSubjectLookup, boolean includeGroupDetail, WsParam[] params, WsGroupLookup[] wsGroupLookups) {
  
    final WsFindGroupsResults wsFindGroupsResults = new WsFindGroupsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      theSummary = "clientVersion: " + clientVersion + ", wsQueryFilter: "
          + wsQueryFilter + "\n, includeGroupDetail: " + includeGroupDetail
          + ", actAsSubject: " + actAsSubjectLookup + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n, wsGroupLookups: " + GrouperUtil.toStringForLog(wsGroupLookups, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      Set<Group> groups = new TreeSet<Group>();
      
      if (wsQueryFilter != null) {
        wsQueryFilter.assignGrouperSession(session);
    
        //make sure filter is ok to use
        wsQueryFilter.validate();
    
        //run the query
        QueryFilter queryFilter = wsQueryFilter.retrieveQueryFilter();
        GrouperQuery grouperQuery = GrouperQuery.createQuery(session, queryFilter);
        groups.addAll(grouperQuery.getGroups());
      }
      
      //we could do this in fewer queries if we like...
      for (WsGroupLookup wsGroupLookup : GrouperUtil.nonNull(wsGroupLookups, WsGroupLookup.class)) {
        wsGroupLookup.retrieveGroupIfNeeded(session);
        Group group = wsGroupLookup.retrieveGroup();
        if (group != null) {
          groups.add(group);
        }
      }
      
      wsFindGroupsResults.assignGroupResult(groups, includeGroupDetail);
  
      wsFindGroupsResults.assignResultCode(WsFindGroupsResultsCode.SUCCESS);
      wsFindGroupsResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindGroupsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsFindGroupsResults;
  }

  /**
   * find a group or groups
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param queryFilterType findGroupType is the WsQueryFilterType enum for which 
   * type of find is happening:  e.g.
   * FIND_BY_GROUP_UUID, FIND_BY_GROUP_NAME_EXACT, FIND_BY_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE,  FIND_BY_GROUP_NAME_APPROXIMATE,
   * FIND_BY_TYPE, AND, OR, MINUS;
   * @param groupName search by group name (context in query type)
   * @param stemName
   *            will return groups in this stem.  can be used with various query types
   * @param stemNameScope
   *            if searching by stem, ONE_LEVEL is for one level,
   *            ALL_IN_SUBTREE will return all in sub tree. Required if
   *            searching by stem
   * @param groupUuid
   *            search by group uuid (must match exactly), cannot use other
   *            params with this
   * @param groupAttributeName if searching by attribute, this is name,
   * or null for all attributes
   * @param groupAttributeValue if searching by attribute, this is the value
   * @param groupTypeName if searching by type, this is the type.  not yet implemented
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId
   *            optional to narrow the act as subject search to a particular source 
   * @param includeGroupDetail T or F as for if group detail should be included
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the groups, or no groups if none found
   */
  public static WsFindGroupsResults findGroupsLite(final GrouperWsVersion clientVersion,
      WsQueryFilterType queryFilterType, String groupName, String stemName, StemScope stemNameScope,
      String groupUuid, String groupAttributeName, String groupAttributeValue,
      GroupType groupTypeName, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsQueryFilter wsQueryFilter = new WsQueryFilter();
    wsQueryFilter.setQueryFilterType(queryFilterType == null ? null : queryFilterType.name());
    wsQueryFilter.setGroupName(groupName);
    wsQueryFilter.setStemName(stemName);
    wsQueryFilter.setStemNameScope(stemNameScope == null ? null : stemNameScope.name());
    wsQueryFilter.setGroupUuid(groupUuid);
    wsQueryFilter.setGroupAttributeName(groupAttributeName);
    wsQueryFilter.setGroupAttributeValue(groupAttributeValue);
    wsQueryFilter.setGroupTypeName(groupTypeName == null ? null : groupTypeName.getName());
  
    // pass through to the more comprehensive method
    WsFindGroupsResults wsFindGroupsResults = findGroups(clientVersion, wsQueryFilter,
        actAsSubjectLookup, includeGroupDetail, params, null);
  
    return wsFindGroupsResults;
  }

  /**
   * find a stem or stems
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsStemQueryFilter is the filter properties that can search by
   * name, uuid, approximate attribute, and can do group math on multiple operations, etc
   * @param includeStemDetail T or F as to if the stem detail should be
   * included (defaults to F)
   * @param actAsSubjectLookup
   * @param params optional: reserved for future use
   * @param wsStemLookups to pass in a list of uuids or names to lookup.  Note the stems are returned
   * in alphabetical order
   * @return the stems, or no stems if none found
   */
  @SuppressWarnings("unchecked")
  public static WsFindStemsResults findStems(final GrouperWsVersion clientVersion,
      WsStemQueryFilter wsStemQueryFilter, WsSubjectLookup actAsSubjectLookup,
      WsParam[] params, WsStemLookup[] wsStemLookups) {
  
    final WsFindStemsResults wsFindStemsResults = new WsFindStemsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      theSummary = "clientVersion: " + clientVersion + ", wsStemQueryFilter: "
          + wsStemQueryFilter + ", actAsSubject: " + actAsSubjectLookup
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n, wsStemLookups: " + GrouperUtil.toStringForLog(wsStemLookups, 100);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      //keep these ordered for testing
      Set<Stem> stems = new TreeSet<Stem>();
      
      if (wsStemQueryFilter != null) {

        wsStemQueryFilter.assignGrouperSession(session);
        
        //make sure filter is ok to use
        wsStemQueryFilter.validate();
    
        //run the query
        QueryFilter queryFilter = wsStemQueryFilter.retrieveQueryFilter();
        GrouperQuery grouperQuery = GrouperQuery.createQuery(session, queryFilter);
        stems.addAll(grouperQuery.getStems());
        
      }

      //we could do this in fewer queries if we like...
      for (WsStemLookup wsStemLookup : GrouperUtil.nonNull(wsStemLookups, WsStemLookup.class)) {
        wsStemLookup.retrieveStemIfNeeded(session, false);
        Stem stem = wsStemLookup.retrieveStem();
        stems.add(stem);
      }

      wsFindStemsResults.assignStemResult(stems);
  
      wsFindStemsResults.assignResultCode(WsFindStemsResultsCode.SUCCESS);
      wsFindStemsResults.getResultMetadata().appendResultMessage(
          "Success for: " + theSummary);
  
    } catch (Exception e) {
      wsFindStemsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsFindStemsResults;
  }

  /**
   * find a stem or stems
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemQueryFilterType findStemType is the WsFindStemType enum for which 
   * type of find is happening:  e.g.
   * FIND_BY_STEM_UUID, FIND_BY_STEM_NAME_EXACT, FIND_BY_PARENT_STEM_NAME, 
   * FIND_BY_APPROXIMATE_ATTRIBUTE, 
   * AND, OR, MINUS;
   * @param stemName search by stem name (must match exactly), cannot use other
   *            params with this
   * @param parentStemName
   *            will return stems in this stem.  can be used with various query types
   * @param parentStemNameScope
   *            if searching by stem, ONE_LEVEL is for one level,
   *            ALL_IN_SUBTREE will return all in sub tree. Required if
   *            searching by stem
   * @param stemUuid
   *            search by stem uuid (must match exactly), cannot use other
   *            params with this
   * @param stemAttributeName if searching by attribute, this is name,
   * or null for all attributes
   * @param stemAttributeValue if searching by attribute, this is the value
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId
   *            optional to narrow the act as subject search to a particular source 
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the stems, or no stems if none found
   */
  public static WsFindStemsResults findStemsLite(final GrouperWsVersion clientVersion,
      WsStemQueryFilterType stemQueryFilterType, String stemName, String parentStemName,
      StemScope parentStemNameScope, String stemUuid, String stemAttributeName,
      String stemAttributeValue, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, 
        paramName1, paramValue1);
  
    WsStemQueryFilter wsStemQueryFilter = new WsStemQueryFilter();
    wsStemQueryFilter.setStemQueryFilterType(stemQueryFilterType == null ? null : stemQueryFilterType.name());
    wsStemQueryFilter.setParentStemName(parentStemName);
    wsStemQueryFilter.setParentStemNameScope(parentStemNameScope == null ? null : parentStemNameScope.name());
    wsStemQueryFilter.setStemAttributeName(stemAttributeName);
    wsStemQueryFilter.setStemAttributeValue(stemAttributeValue);
    wsStemQueryFilter.setStemName(stemName);
    wsStemQueryFilter.setStemUuid(stemUuid);
  
    // pass through to the more comprehensive method
    WsFindStemsResults wsFindStemsResults = findStems(clientVersion, wsStemQueryFilter,
        actAsSubjectLookup, params, null);
  
    return wsFindStemsResults;
  }

  /**
   * get groups from members based on filter (accepts batch of members)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectLookup
   * @param subjectLookups
   *            subjects to be examined to see if in group
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @param fieldName is field name (list name) to search or blank for default list
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param wsStemLookup is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param enabled is A for all, T or null for enabled only, F for disabled
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetGroupsResults getGroups(final GrouperWsVersion clientVersion,
      WsSubjectLookup[] subjectLookups, WsMemberFilter memberFilter, 
      WsSubjectLookup actAsSubjectLookup, boolean includeGroupDetail,
      boolean includeSubjectDetail, 
      String[] subjectAttributeNames, WsParam[] params, String fieldName, String scope, 
      WsStemLookup wsStemLookup, StemScope stemScope, String enabled, 
      Integer pageSize, Integer pageNumber, String sortString, Boolean ascending) {
  
    final WsGetGroupsResults wsGetGroupsResults = new WsGetGroupsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      theSummary = "clientVersion: " + clientVersion + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 200) 
          + "\nmemberFilter: " + memberFilter + ", includeGroupDetail: "
          + includeGroupDetail + ", actAsSubject: " + actAsSubjectLookup
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100)
          + "\n fieldName1: " + fieldName + "\n, scope: " + scope
          + ", wsStemLookup: " + wsStemLookup + "\n, stemScope: " + stemScope + ", enabled: " + enabled
          + ", pageSize: " + pageSize + ", pageNumber: " + pageNumber + ", sortString: " + sortString
          + ", ascending: " + ascending;
  
      subjectAttributeNames = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

      wsGetGroupsResults.setSubjectAttributeNames(subjectAttributeNames);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(
          subjectLookups, GrouperWsConfig.WS_GET_GROUPS_SUBJECTS_MAX, 1000000, "subjectLookups");

      int resultIndex = 0;

      Boolean enabledBoolean = null;
      if (StringUtils.equalsIgnoreCase("A", enabled)) {
        enabledBoolean = null;
      } else {
        enabledBoolean = GrouperServiceUtils.booleanValue(enabled, true, "enabled");
      }
      
      wsGetGroupsResults.setResults(new WsGetGroupsResult[subjectLength]);

      //convert the options to a map for easy access, and validate them
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(params);
      if (StringUtils.isBlank(fieldName)) {
        fieldName = paramMap.get("fieldName");
      }
      Field field = null;
      if (!StringUtils.isBlank(fieldName)) {
        field = GrouperServiceUtils.retrieveField(fieldName);
        theSummary += ", field: " + field.getName();
      }
  
      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
        WsGetGroupsResult wsGetGroupsResult = new WsGetGroupsResult();
        wsGetGroupsResults.getResults()[resultIndex++] = wsGetGroupsResult;
        
        try {
          //init in case error
          wsGetGroupsResult.setWsSubject(new WsSubject(wsSubjectLookup));
          Subject subject = wsSubjectLookup.retrieveSubject("subjectLookup");
          wsGetGroupsResult.setWsSubject(new WsSubject(subject, subjectAttributeNames, wsSubjectLookup));
          Member member = MemberFinder.internal_findBySubject(subject, null, false);
          Set<Group> groups = null;
          if (member == null) {
            groups = new HashSet<Group>();
          } else {
            if (field == null) {
              field = Group.getDefaultList();
            }
            
            Stem stem = null;
            if (wsStemLookup != null) {
              wsStemLookup.retrieveStemIfNeeded(session, true);
              stem = wsStemLookup.retrieveStem();
            }

            //if supposed to have stem but cant find, then dont get any groups
            if (wsStemLookup == null || stem != null ) {
              QueryOptions queryOptions = null;
              
              if (pageSize != null || pageNumber != null || !StringUtils.isBlank(sortString) || ascending != null) {
                queryOptions = new QueryOptions();
                if ((pageSize == null) != (pageNumber == null)) {
                  throw new RuntimeException("If you pass page size, you must pass page number and vice versa");
                }
                if (pageSize != null) {
                  queryOptions.paging(new QueryPaging(pageSize, pageNumber, false));
                }
                if (!StringUtils.isBlank(sortString)) {
                  if (ascending == null) {
                    ascending = true;
                  }
                  queryOptions.sort(new QuerySort(sortString, ascending));
                }
              }
              
              Scope stemDotScope = null;
              if (stemScope != null) {
                stemDotScope = stemScope.convertToScope();
              }
              
              groups = memberFilter.getGroups(member, field, scope, stem, stemDotScope, queryOptions, enabledBoolean);
            }
          }
          wsGetGroupsResult.assignGroupResult(groups, includeGroupDetail);
        } catch (Exception e) {
          wsGetGroupsResult.assignResultCodeException(null, null,wsSubjectLookup,  e);
        }
        
      }
  
      wsGetGroupsResults.tallyResults(theSummary);
      
    } catch (Exception e) {
      wsGetGroupsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetGroupsResults;
  
  }

  /**
   * get groups for a subject based on filter
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectId
   *            to add (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param subjectIdentifier
   *            to add (mutually exclusive with subjectId)
   * @param includeGroupDetail T or F as to if the group detail should be
   * included (defaults to F)
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param subjectIdentifier
   *            to query (mutually exclusive with subjectId)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param fieldName is field name (list name) to search or blank for default list
   * @param scope is a DB pattern that will have % appended to it, or null for all.  e.g. school:whatever:parent:
   * @param stemName is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemUuid is the stem to check in, or null if all.  If has stem, must have stemScope
   * @param stemScope is if in this stem, or in any stem underneath.  You must pass stemScope if you pass a stem
   * @param enabled is A for all, T or null for enabled only, F for disabled
   * @param pageSize page size if paging
   * @param pageNumber page number 1 indexed if paging
   * @param sortString must be an hql query field, e.g. can sort on name, displayName, extension, displayExtension
   * @param ascending or null for ascending, false for descending.  If you pass true or false, must pass a sort string
   * @return the result of one member add
   */
  public static WsGetGroupsLiteResult getGroupsLite(final GrouperWsVersion clientVersion, String subjectId,
      String subjectSourceId, String subjectIdentifier, WsMemberFilter memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, 
      boolean includeSubjectDetail, 
      String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String fieldName, String scope, 
      String stemName, String stemUuid, StemScope stemScope, String enabled, 
      Integer pageSize, Integer pageNumber, String sortString, Boolean ascending) {
  
    // setup the subject lookup
    WsSubjectLookup subjectLookup = new WsSubjectLookup(subjectId, subjectSourceId,
        subjectIdentifier);
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[]{subjectLookup};
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    
    WsGetGroupsResults wsGetGroupsResults = getGroups(clientVersion, subjectLookups,
        memberFilter, actAsSubjectLookup, includeGroupDetail, includeSubjectDetail,
        subjectAttributeArray, params, fieldName, scope, wsStemLookup, stemScope, enabled, pageSize, pageNumber, sortString, ascending);
  
    return new WsGetGroupsLiteResult(wsGetGroupsResults);
  
  }

  /**
   * get members from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups are groups to check members for
   * @param memberFilter
   *            must be one of All, Effective, Immediate, Composite
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param sourceIds are source ids of members to retrieve
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetMembersResults getMembers(
      final GrouperWsVersion clientVersion,
      WsGroupLookup[] wsGroupLookups, WsMemberFilter memberFilter,
      WsSubjectLookup actAsSubjectLookup, final Field fieldName,
      boolean includeGroupDetail, 
      boolean includeSubjectDetail, String[] subjectAttributeNames,
      WsParam[] params, String[] sourceIds) {
  
    WsGetMembersResults wsGetMembersResults = new WsGetMembersResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      boolean hasSources = GrouperUtil.length(sourceIds) > 0;

      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups,200) + "\n, memberFilter: " 
          + memberFilter
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + 
          "\n, sourceIds: " + GrouperUtil.toStringForLog(sourceIds);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);

      int resultIndex = 0;
      Set<Source> sources = null;
      if (hasSources) {
        sources = GrouperUtil.convertSources(sourceIds);
      }
      
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
      wsGetMembersResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
      
      int groupLookupsLength = GrouperUtil.length(wsGroupLookups);
      wsGetMembersResults.setResults(new WsGetMembersResult[groupLookupsLength]);
      
      for (WsGroupLookup wsGroupLookup : GrouperUtil.nonNull(wsGroupLookups, WsGroupLookup.class)) {
        WsGetMembersResult wsGetMembersResult = new WsGetMembersResult();
        wsGetMembersResults.getResults()[resultIndex++] = wsGetMembersResult;
        
        try {
          Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");

          //init in case error
          wsGetMembersResult.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
          
          if (group == null) {

            wsGetMembersResult
                .assignResultCode(GroupFindResult
                    .convertToGetMembersCodeStatic(wsGroupLookup
                        .retrieveGroupFindResult()));
            wsGetMembersResult.getResultMetadata().setResultMessage(
                "Problem with group: '" + wsGroupLookup + "'.  ");
            //should we short circuit if transactional?
            continue;
          }
          
          // lets get the members, cant be null
          Set<Member> members = memberFilter.getMembers(group, fieldName, sources);
      
          wsGetMembersResult.assignSubjectResult(members, subjectAttributeNamesToRetrieve, includeSubjectDetail);
      
        } catch (Exception e) {
          wsGetMembersResult.assignResultCodeException(null, null, wsGroupLookup, e);
        }
        
      }
  
      wsGetMembersResults.tallyResults(theSummary);
      
      
    } catch (Exception e) {
      wsGetMembersResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetMembersResults;
  }

  /**
   * get memberships from groups and or subjects based on a filter (all, immediate only,
   * effective only, composite, nonimmediate).
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups are groups to look in
   * @param wsSubjectLookups are subjects to look in
   * @param wsMemberFilter
   *            must be one of All, Effective, Immediate, Composite, NonImmediate
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param fieldName is if the memberships should be retrieved from a certain field membership
   * of the group (certain list)
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param sourceIds are sources to look in for memberships, or null if all
   * @param scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @param wsStemLookup is the stem to look in for memberships
   * @param stemScope is StemScope to search only in one stem or in substems: ONE_LEVEL, ALL_IN_SUBTREE
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @param membershipIds are the ids to search for if they are known
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetMembershipsResults getMemberships(final GrouperWsVersion clientVersion,
      WsGroupLookup[] wsGroupLookups, WsSubjectLookup[] wsSubjectLookups, WsMemberFilter wsMemberFilter,
      WsSubjectLookup actAsSubjectLookup, Field fieldName, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params, 
      String[] sourceIds, String scope, 
      WsStemLookup wsStemLookup, StemScope stemScope, String enabled, String[] membershipIds) {  

    WsGetMembershipsResults wsGetMembershipsResults = new WsGetMembershipsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups, 200) + ", wsMemberFilter: " + wsMemberFilter
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup + ", fieldName: " + fieldName
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsSubjectLookups: "
          + GrouperUtil.toStringForLog(wsSubjectLookups, 200) + "\n, sourceIds: " + GrouperUtil.toStringForLog(sourceIds, 100)
          + "\n, scope: " + scope + ", wsStemLookup: " + wsStemLookup + ", stemScope: " + stemScope + ", enabled: " + enabled
          + "\n, membershipIds: " + GrouperUtil.toStringForLog(membershipIds, 200);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //TODO lookup all groups and subjects before and after
      
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
      
      MembershipType membershipType = null;
      if (wsMemberFilter != null) {
        membershipType = wsMemberFilter.getMembershipType();
      }
      
      //get all the groups
      //we could probably batch these to get better performance.  And we dont even have to lookup uuids
      Set<String> groupIds = null;
      if (GrouperUtil.length(wsGroupLookups) > 0) {
        
        groupIds = new LinkedHashSet<String>();
        for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
          
          if (wsGroupLookup == null) {
            continue;
          }
          wsGroupLookup.retrieveGroupIfNeeded(session);
          Group group = wsGroupLookup.retrieveGroup();
          groupIds.add(group.getUuid());
          
        }
        
      }

      //get all the members
      Set<String> memberIds = null;
      if (GrouperUtil.length(wsSubjectLookups) > 0) {
        
        memberIds = new LinkedHashSet<String>();
        for (WsSubjectLookup wsSubjectLookup : wsSubjectLookups) {
          if (wsSubjectLookup == null) {
            continue;
          }
          Member member = wsSubjectLookup.retrieveMember();
          if (member == null) {
            //cant find, thats ok
            if (MemberFindResult.MEMBER_NOT_FOUND.equals(wsSubjectLookup.retrieveMemberFindResult())) {
              continue;
            }
            //problem
            throw new RuntimeException("Problem with subject: " + wsSubjectLookup + ", " + wsSubjectLookup.retrieveMemberFindResult());
          }
          memberIds.add(member.getUuid());
        }
      }

      Boolean enabledBoolean = true;
      if (!StringUtils.isBlank(enabled)) {
        if (StringUtils.equalsIgnoreCase("A", enabled)) {
          enabledBoolean = null;
        } else {
          enabledBoolean = GrouperUtil.booleanValue(enabled);
        }
      }
      
      Stem stem = null;
      
      if (wsStemLookup != null) {
        wsStemLookup.retrieveStemIfNeeded(session, true);
        stem = wsStemLookup.retrieveStem();

      }
      
      //if filtering by stem, and stem not found, then dont find any memberships
      if (wsStemLookup == null || stem != null) {
        Set<Source> sources = GrouperUtil.convertSources(sourceIds);
        
        Set<String> membershipIdSet = null;
        if (GrouperUtil.length(membershipIds) > 0) {
          membershipIdSet = GrouperUtil.toSet(membershipIds);
        }
        
        // lets get the members, cant be null
        Set<Object[]> membershipObjects = MembershipFinder.findMemberships(groupIds, memberIds, membershipIdSet, 
            membershipType, fieldName, sources, scope, stem, stemScope == null ? null : stemScope.convertToScope(), enabledBoolean);
        
        //calculate and return the results
        wsGetMembershipsResults.assignResult(membershipObjects, includeGroupDetail, includeSubjectDetail, subjectAttributeNames);
      }
      wsGetMembershipsResults.assignResultCode(WsGetMembershipsResultsCode.SUCCESS);
      
      wsGetMembershipsResults.getResultMetadata().setResultMessage(
          "Found " + GrouperUtil.length(wsGetMembershipsResults.getWsMemberships()) 
          + " results involving " + GrouperUtil.length(wsGetMembershipsResults.getWsGroups())
          + " groups and " + GrouperUtil.length(wsGetMembershipsResults.getWsSubjects()) + " subjects");

        
    } catch (Exception e) {
      wsGetMembershipsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetMembershipsResults;
  
  }

  /**
   * get memberships from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId to search for memberships in or null to not restrict
   * @param sourceId of subject to search for memberships, or null to not restrict
   * @param subjectIdentifier of subject to search for memberships, or null to not restrict
   * @param wsMemberFilter
   *            must be one of All, Effective, Immediate, Composite
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param sourceIds are comma separated sourceIds
   * @param scope is a sql like string which will have a percent % concatenated to the end for group
   * names to search in (or stem names)
   * @param stemName to limit the search to a stem (in or under)
   * @param stemUuid to limit the search to a stem (in or under)
   * @param stemScope to specify if we are searching in or under the stem
   * @param enabled A for all, null or T for enabled only, F for disabled only
   * @param membershipIds comma separated list of membershipIds to retrieve
   * @return the memberships, or none if none found
   */
  public static WsGetMembershipsResults getMembershipsLite(final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, String subjectId, String sourceId, String subjectIdentifier, 
      WsMemberFilter wsMemberFilter,
      boolean includeSubjectDetail, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, Field fieldName, String subjectAttributeNames,
      boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds, String scope, String stemName, 
      String stemUuid, StemScope stemScope, String enabled, String membershipIds) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = null;
    
    if (StringUtils.isNotBlank(groupName) || StringUtils.isNotBlank(groupUuid)) {
      wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    }
  
    WsSubjectLookup wsSubjectLookup = WsSubjectLookup.createIfNeeded(subjectId, sourceId, subjectIdentifier);
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // pass through to the more comprehensive method
    WsGroupLookup[] wsGroupLookups = wsGroupLookup == null ? null : new WsGroupLookup[]{wsGroupLookup};
    WsSubjectLookup[] wsSubjectLookups = wsSubjectLookup == null ? null : new WsSubjectLookup[]{wsSubjectLookup};
    
    String[] sourceIdArray = GrouperUtil.splitTrim(sourceIds, ",");

    String[] membershipIdArray = GrouperUtil.splitTrim(membershipIds, ",");

    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    
    WsGetMembershipsResults wsGetMembershipsResults = getMemberships(clientVersion,
        wsGroupLookups, wsSubjectLookups, wsMemberFilter, actAsSubjectLookup, fieldName,
        includeSubjectDetail, subjectAttributeArray, includeGroupDetail,
        params, sourceIdArray, scope, wsStemLookup, stemScope, enabled, membershipIdArray);
  
    return wsGetMembershipsResults;
  }

  /**
   * get members from a group based on a filter (all, immediate only,
   * effective only, composite)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param memberFilter
   *            must be one of All, Effective, Immediate, Composite
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is the source to use to lookup the subject (if applicable) 
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName is if the member should be added to a certain field membership
   * of the group (certain list)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
   * if multiple
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param sourceIds comma separated of sources to get members from
   * @return the members, or no members if none found
   */
  public static WsGetMembersLiteResult getMembersLite(
      final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, WsMemberFilter memberFilter, 
      String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier, 
      final Field fieldName,
      boolean includeGroupDetail, 
      boolean includeSubjectDetail, String subjectAttributeNames,
      String paramName0, String paramValue0,
      String paramName1, String paramValue1, String sourceIds) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] {wsGroupLookup};
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    String[] sourceIdArray = GrouperUtil.splitTrim(sourceIds, ",");
    
    // pass through to the more comprehensive method
    WsGetMembersResults wsGetMembersResults = getMembers(clientVersion, wsGroupLookups,
        memberFilter, actAsSubjectLookup, fieldName, 
        includeGroupDetail, includeSubjectDetail,
        subjectAttributeArray, params, sourceIdArray);
  
    return new WsGetMembersLiteResult(wsGetMembersResults);
  }

  /**
   * delete a group or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookups
   *            groups to delete
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGroupDeleteResults groupDelete(final GrouperWsVersion clientVersion,
      final WsGroupLookup[] wsGroupLookups, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final boolean includeGroupDetail, final WsParam[] params) {
  
    final WsGroupDeleteResults wsGroupDeleteResults = new WsGroupDeleteResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookups: "
          + GrouperUtil.toStringForLog(wsGroupLookups, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", includeGroupDetail: "
          + includeGroupDetail + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int groupsSize = GrouperServiceUtils.arrayLengthAtLeastOne(wsGroupLookups,
                  GrouperWsConfig.WS_GROUP_DELETE_MAX, 1000000, "groupLookups");
  
              wsGroupDeleteResults.setResults(new WsGroupDeleteResult[groupsSize]);
  
              int resultIndex = 0;
  
              //loop through all groups and do the delete
              for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
  
                WsGroupDeleteResult wsGroupDeleteResult = new WsGroupDeleteResult(
                    wsGroupLookup);
                wsGroupDeleteResults.getResults()[resultIndex++] = wsGroupDeleteResult;
  
                wsGroupLookup.retrieveGroupIfNeeded(SESSION);
                Group group = wsGroupLookup.retrieveGroup();
  
                if (group == null) {
  
                  wsGroupDeleteResult
                      .assignResultCode(GroupFindResult
                          .convertToGroupDeleteCodeStatic(wsGroupLookup
                              .retrieveGroupFindResult()));
                  wsGroupDeleteResult.getResultMetadata().setResultMessage(
                      "Cant find group: '" + wsGroupLookup + "'.  ");
                  //should we short circuit if transactional?
                  continue;
                }
  
                //make each group failsafe
                try {
                  wsGroupDeleteResult.assignGroup(group, wsGroupLookup, includeGroupDetail);
  
                  //if there was already a problem, then dont continue
                  if (!GrouperUtil.booleanValue(wsGroupDeleteResult.getResultMetadata()
                      .getSuccess(), true)) {
                    continue;
                  }
  
                  group.delete();
  
                  wsGroupDeleteResult.assignResultCode(WsGroupDeleteResultCode.SUCCESS);
                  wsGroupDeleteResult.getResultMetadata().setResultMessage(
                      "Group '" + group.getName() + "' was deleted.");
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsGroupDeleteResult
                      .assignResultCode(WsGroupDeleteResultCode.INSUFFICIENT_PRIVILEGES);
                } catch (Exception e) {
                  wsGroupDeleteResult.assignResultCodeException(e, wsGroupLookup);
                }
              }
  
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsGroupDeleteResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsGroupDeleteResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsGroupDeleteResults;
  
  }

  /**
   * delete a group or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to delete the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to delete the group (mutually exclusive with groupName)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsGroupDeleteLiteResult groupDeleteLite(final GrouperWsVersion clientVersion,
      String groupName, String groupUuid, String actAsSubjectId,
      String actAsSubjectSourceId, String actAsSubjectIdentifier,
      final boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGroupDeleteResults wsGroupDeleteResults = groupDelete(clientVersion,
        wsGroupLookups, actAsSubjectLookup, null, includeGroupDetail, params);
  
    return new WsGroupDeleteLiteResult(wsGroupDeleteResults);
  }

  /**
   * save a group or many (insert or update).  Note, you cannot rename an existing group.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param wsGroupToSaves
   *            groups to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGroupSaveResults groupSave(final GrouperWsVersion clientVersion,
      final WsGroupToSave[] wsGroupToSaves, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final boolean includeGroupDetail,  final WsParam[] params) {

    final WsGroupSaveResults wsGroupSaveResults = new WsGroupSaveResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsGroupToSaves: "
          + GrouperUtil.toStringForLog(wsGroupToSaves, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int wsGroupsLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsGroupToSaves, GrouperWsConfig.WS_STEM_SAVE_MAX, 1000000, "groupsToSave");
  
              wsGroupSaveResults.setResults(new WsGroupSaveResult[wsGroupsLength]);
  
              int resultIndex = 0;
  
              //loop through all stems and do the save
              for (WsGroupToSave wsGroupToSave : wsGroupToSaves) {
                final WsGroupSaveResult wsGroupSaveResult = new WsGroupSaveResult(wsGroupToSave.getWsGroupLookup());
                wsGroupSaveResults.getResults()[resultIndex++] = wsGroupSaveResult;
                final WsGroupToSave WS_GROUP_TO_SAVE = wsGroupToSave;
                try {
                  //this should be autonomous, so that within one group, it is transactional
                  HibernateSession.callbackHibernateSession(
                      GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

                    public Object callback(HibernateHandlerBean hibernateHandlerBean)
                        throws GrouperDAOException {
                      //make sure everything is in order
                      WS_GROUP_TO_SAVE.validate();
                      Group group = WS_GROUP_TO_SAVE.save(SESSION);
                      SaveResultType saveResultType = WS_GROUP_TO_SAVE.saveResultType();
                      wsGroupSaveResult.setWsGroup(new WsGroup(group, 
                          WS_GROUP_TO_SAVE.getWsGroupLookup(), includeGroupDetail));
                      
                      if (saveResultType == SaveResultType.INSERT) {
                        wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.SUCCESS_INSERTED, clientVersion);
                      } else if (saveResultType == SaveResultType.UPDATE) {
                        wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.SUCCESS_UPDATED, clientVersion);
                      } else if (saveResultType == SaveResultType.NO_CHANGE) {
                        wsGroupSaveResult.assignResultCode(WsGroupSaveResultCode.SUCCESS_NO_CHANGES_NEEDED, clientVersion);
                      } else {
                        throw new RuntimeException("Invalid saveType: " + saveResultType);
                      }

                      return null;
                    }

                  });
  
                } catch (Exception e) {
                  wsGroupSaveResult.assignResultCodeException(e, wsGroupToSave, clientVersion);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsGroupSaveResults.tallyResults(TX_TYPE, THE_SUMMARY, clientVersion)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsGroupSaveResults.assignResultCodeException(null, theSummary, e, clientVersion);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsGroupSaveResults;
  }

  /**
   * see if a group has members based on filter (accepts batch of members)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsGroupLookup
   *            for the group to see if the members are in there
   * @param subjectLookups
   *            subjects to be examined to see if in group
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param fieldName
   *            is if the Group.hasMember() method with field is to be called
   *            (e.g. admins, optouts, optins, etc from Field table in DB)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsHasMemberResults hasMember(final GrouperWsVersion clientVersion,
      WsGroupLookup wsGroupLookup, WsSubjectLookup[] subjectLookups,
      WsMemberFilter memberFilter,
      WsSubjectLookup actAsSubjectLookup, Field fieldName,
      final boolean includeGroupDetail, boolean includeSubjectDetail, 
      String[] subjectAttributeNames, WsParam[] params) {
  
    WsHasMemberResults wsHasMemberResults = new WsHasMemberResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      theSummary = "clientVersion: " + clientVersion + ", wsGroupLookup: "
          + wsGroupLookup + ", subjectLookups: "
          + GrouperUtil.toStringForLog(subjectLookups, 200)
          + "\n memberFilter: "
          + memberFilter + ", actAsSubject: " + actAsSubjectLookup + ", fieldName: "
          + fieldName + ", includeGroupDetail: " + includeGroupDetail 
          + ", includeSubjectDetail: " + includeSubjectDetail
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n," +
          		"params: " + GrouperUtil.toStringForLog(params, 100) + "\n";
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
  
      Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
  
      //assign the group to the result to be descriptive
      wsHasMemberResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
  
      String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsHasMemberResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      int subjectLength = GrouperServiceUtils.arrayLengthAtLeastOne(subjectLookups,
          GrouperWsConfig.WS_HAS_MEMBER_SUBJECTS_MAX, 1000000, "subjectLookups");
  
      wsHasMemberResults.setResults(new WsHasMemberResult[subjectLength]);
  
      int resultIndex = 0;
  
      for (WsSubjectLookup wsSubjectLookup : subjectLookups) {
        WsHasMemberResult wsHasMemberResult = null;
        try {
          wsHasMemberResult = new WsHasMemberResult(wsSubjectLookup,
              subjectAttributeNamesToRetrieve);
          wsHasMemberResults.getResults()[resultIndex++] = wsHasMemberResult;
  
          //see if subject found
          if (!GrouperUtil.booleanValue(wsHasMemberResult.getResultMetadata()
              .getSuccess(), true)) {
            continue;
          }
  
          boolean hasMember = memberFilter.hasMember(group, wsSubjectLookup
              .retrieveSubject(), fieldName);
          wsHasMemberResult.assignResultCode(hasMember ? WsHasMemberResultCode.IS_MEMBER
              : WsHasMemberResultCode.IS_NOT_MEMBER);
  
        } catch (Exception e) {
          wsHasMemberResult.assignResultCodeException(e, wsSubjectLookup);
        }
      }
  
      //see if all success
      wsHasMemberResults.tallyResults(theSummary);
  
    } catch (Exception e) {
      wsHasMemberResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsHasMemberResults;
  
  }

  /**
   * see if a group has a member (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param groupName
   *            to lookup the group (mutually exclusive with groupUuid)
   * @param groupUuid
   *            to lookup the group (mutually exclusive with groupName)
   * @param subjectId
   *            to query (mutually exclusive with subjectIdentifier)
   * @param subjectSourceId is source of subject to narrow the result and prevent
   * duplicates
   * @param memberFilter
   *            can be All, Effective (non immediate), Immediate (direct),
   *            Composite (if composite group with group math (union, minus,
   *            etc)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param fieldName
   *            is if the Group.hasMember() method with field is to be called
   *            (e.g. admins, optouts, optins, etc from Field table in DB)
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param subjectIdentifier
   *            to query (mutually exclusive with subjectId)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member query
   */
  public static WsHasMemberLiteResult hasMemberLite(final GrouperWsVersion clientVersion, String groupName,
      String groupUuid, String subjectId, String subjectSourceId, String subjectIdentifier,
      WsMemberFilter memberFilter,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      Field fieldName, final boolean includeGroupDetail, boolean includeSubjectDetail, String subjectAttributeNames, 
      String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
  
    // setup the subject lookup
    WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
    subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsHasMemberResults wsHasMemberResults = hasMember(clientVersion, wsGroupLookup,
        subjectLookups, memberFilter,
        actAsSubjectLookup, fieldName, includeGroupDetail, 
        includeSubjectDetail, subjectAttributeArray, params);
  
    return new WsHasMemberLiteResult(wsHasMemberResults);
  }

  /**
   * see if a group has a member (if already a direct member, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param oldSubjectId subject id of old member object.  This is the preferred way to look up the 
   * old subject, but subjectIdentifier could also be used
   * @param oldSubjectSourceId source id of old member object (optional)
   * @param oldSubjectIdentifier subject identifier of old member object.  It is preferred to lookup the 
   * old subject by id, but if identifier is used, that is ok instead (as long as subject is resolvable).
   * @param newSubjectId preferred way to identify the new subject id
   * @param newSubjectSourceId preferres way to identify the new subject id
   * @param newSubjectIdentifier subjectId is the preferred way to lookup the new subject, but identifier is
   * ok to use instead
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param deleteOldMember T or F as to whether the old member should be deleted (if new member does exist).
   * This defaults to T if it is blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member query
   */
  public static WsMemberChangeSubjectLiteResult memberChangeSubjectLite(final GrouperWsVersion clientVersion, 
      String oldSubjectId, String oldSubjectSourceId, String oldSubjectIdentifier,
      String newSubjectId, String newSubjectSourceId, String newSubjectIdentifier,      
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      boolean deleteOldMember, 
      boolean includeSubjectDetail, String subjectAttributeNames, 
      String paramName0,
      String paramValue0, String paramName1, String paramValue1) {
  
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsMemberChangeSubject wsMemberChangeSubject = new WsMemberChangeSubject();
    
    WsSubjectLookup oldSubjectLookup = new WsSubjectLookup(oldSubjectId, oldSubjectSourceId, oldSubjectIdentifier);
    WsSubjectLookup newSubjectLookup = new WsSubjectLookup(newSubjectId, newSubjectSourceId, newSubjectIdentifier);
    
    wsMemberChangeSubject.assignDeleteOldMemberBoolean(deleteOldMember);
    wsMemberChangeSubject.setOldSubjectLookup(oldSubjectLookup);
    wsMemberChangeSubject.setNewSubjectLookup(newSubjectLookup);
    
    WsMemberChangeSubject[] wsMemberChangeSubjects = {wsMemberChangeSubject};
    
    // setup the subject lookup
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsMemberChangeSubjectResults wsMemberChangeSubjectResults = memberChangeSubject(clientVersion, 
        wsMemberChangeSubjects,
        actAsSubjectLookup, null, includeSubjectDetail, subjectAttributeArray, params);
  
    return new WsMemberChangeSubjectLiteResult(wsMemberChangeSubjectResults);
  }

  /**
   * change the subject in a member or some members
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsMemberChangeSubjects objects that describe one member renaming
   * @param actAsSubjectLookup
   *            to act as a different user than the logged in user
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param params optional: reserved for future use
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsMemberChangeSubjectResults memberChangeSubject(final GrouperWsVersion clientVersion,
      final WsMemberChangeSubject[] wsMemberChangeSubjects,
      final WsSubjectLookup actAsSubjectLookup, GrouperTransactionType txType, 
      final boolean includeSubjectDetail, 
      final String[] subjectAttributeNames, final WsParam[] params) {
    final WsMemberChangeSubjectResults wsMemberChangeSubjectResults = new WsMemberChangeSubjectResults();
    
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsMemberChangeSubject: "
          + GrouperUtil.toStringForLog(wsMemberChangeSubjects, 500) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
      
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
      wsMemberChangeSubjectResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int membersToChangeLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsMemberChangeSubjects, GrouperWsConfig.WS_ADD_MEMBER_SUBJECTS_MAX, 1000000, "subjectLookups");
  
              wsMemberChangeSubjectResults.setResults(new WsMemberChangeSubjectResult[membersToChangeLength]);
  
              int resultIndex = 0;
  
              //loop through all subjects and do the delete
              for (WsMemberChangeSubject wsMemberChangeSubject : wsMemberChangeSubjects) {
                WsMemberChangeSubjectResult wsMemberChangeSubjectResult = new WsMemberChangeSubjectResult();
                wsMemberChangeSubjectResults.getResults()[resultIndex++] = wsMemberChangeSubjectResult;
                try {
  
                  Member oldMember = wsMemberChangeSubject.getOldSubjectLookup().retrieveMember();
                  wsMemberChangeSubjectResult.processMemberOld(wsMemberChangeSubject.getOldSubjectLookup(), 
                      subjectAttributeNamesToRetrieve, includeSubjectDetail);
                  if (oldMember == null) {
                    continue;
                  }
                  Subject newSubject = wsMemberChangeSubject.getNewSubjectLookup().retrieveSubject();
                  
                  wsMemberChangeSubjectResult.processSubjectNew(wsMemberChangeSubject.getNewSubjectLookup(), 
                      subjectAttributeNamesToRetrieve);
  
                  //make sure we have the right data, if not, then keep going
                  if (newSubject == null) {
                    continue;
                  }

                  boolean deleteOldMember = wsMemberChangeSubject.retrieveDeleteOldMemberBoolean();
                  
                  oldMember.changeSubject(newSubject, deleteOldMember);
                  
                  //assign one of 4 success codes
                  wsMemberChangeSubjectResult.assignResultCode(WsMemberChangeSubjectResultCode.SUCCESS);
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsMemberChangeSubjectResult
                      .assignResultCode(WsMemberChangeSubjectResultCode.INSUFFICIENT_PRIVILEGES);
                } catch (Exception e) {
                  wsMemberChangeSubjectResult.assignResultCodeException(e, wsMemberChangeSubject);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsMemberChangeSubjectResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsMemberChangeSubjectResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsMemberChangeSubjectResults;

  }
  
  /**
   * delete a stem or many (if doesnt exist, ignore)
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemName name of stem to delete (mutually exclusive with uuid)
   * @param stemUuid uuid of stem to delete (mutually exclusive with name)
   * 
   * @param wsStemLookups stem lookups of stems to delete (specify name or uuid)
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsStemDeleteResults stemDelete(final GrouperWsVersion clientVersion,
      final WsStemLookup[] wsStemLookups, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsStemDeleteResults wsStemDeleteResults = new WsStemDeleteResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsStemLookups: "
          + GrouperUtil.toStringForLog(wsStemLookups, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int stemsSize = GrouperServiceUtils.arrayLengthAtLeastOne(wsStemLookups,
                  GrouperWsConfig.WS_STEM_DELETE_MAX, 1000000, "stemLookups");
  
              wsStemDeleteResults.setResults(new WsStemDeleteResult[stemsSize]);
  
              int resultIndex = 0;
  
              //loop through all groups and do the delete
              for (WsStemLookup wsStemLookup : wsStemLookups) {
  
                WsStemDeleteResult wsStemDeleteResult = new WsStemDeleteResult(
                    wsStemLookup);
                wsStemDeleteResults.getResults()[resultIndex++] = wsStemDeleteResult;
  
                wsStemLookup.retrieveStemIfNeeded(SESSION, true);
                Stem stem = wsStemLookup.retrieveStem();
  
                if (stem == null) {
                  wsStemDeleteResult.assignResultCode(StemFindResult
                      .convertToDeleteCodeStatic(wsStemLookup.retrieveStemFindResult()));
                  wsStemDeleteResult.getResultMetadata().setResultMessage(
                      "Cant find stem: '" + wsStemLookup + "'.  ");
                  continue;
                }
  
                //make each stem failsafe
                try {
                  wsStemDeleteResult.setWsStem(new WsStem(stem));
                  stem.delete();
  
                  wsStemDeleteResult.assignResultCode(WsStemDeleteResultCode.SUCCESS);
                  wsStemDeleteResult.getResultMetadata().setResultMessage(
                      "Stem '" + stem.getName() + "' was deleted.");
  
                } catch (InsufficientPrivilegeException ipe) {
                  wsStemDeleteResult
                      .assignResultCode(WsStemDeleteResultCode.INSUFFICIENT_PRIVILEGES);
                  wsStemDeleteResult.getResultMetadata().setResultMessage(
                      "Error: insufficient privileges to delete stem '" + stem.getName()
                          + "'");
                } catch (Exception e) {
                  wsStemDeleteResult.assignResultCodeException(e, wsStemLookup);
                }
  
              }
  
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsStemDeleteResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsStemDeleteResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsStemDeleteResults;
  
  }

  /**
   * delete a stem or many (if doesnt exist, ignore)
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param stemName
   *            to delete the stem (mutually exclusive with stemUuid)
   * @param stemUuid
   *            to delete the stem (mutually exclusive with stemName)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsStemDeleteLiteResult stemDeleteLite(final GrouperWsVersion clientVersion,
      String stemName, String stemUuid, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the stem lookup
    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);
    WsStemLookup[] wsStemLookups = new WsStemLookup[] { wsStemLookup };
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemDeleteResults wsStemDeleteResults = stemDelete(clientVersion, wsStemLookups,
        actAsSubjectLookup, null, params);
  
    return new WsStemDeleteLiteResult(wsStemDeleteResults);
  }

  /**
   * save a stem or many (insert or update).  Note, you cannot rename an existing stem.
   * 
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param wsStemToSaves
   *            stems to save
   * @param actAsSubjectLookup
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsStemSaveResults stemSave(final GrouperWsVersion clientVersion,
      final WsStemToSave[] wsStemToSaves, final WsSubjectLookup actAsSubjectLookup,
      GrouperTransactionType txType, final WsParam[] params) {
  
    final WsStemSaveResults wsStemSaveResults = new WsStemSaveResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
      GrouperWsVersion.assignCurrentClientVersion(clientVersion);

      txType = GrouperUtil.defaultIfNull(txType, GrouperTransactionType.NONE);
      final GrouperTransactionType TX_TYPE = txType;
      
      theSummary = "clientVersion: " + clientVersion + ", wsStemToSaves: "
          + GrouperUtil.toStringForLog(wsStemToSaves, 200) + "\n, actAsSubject: "
          + actAsSubjectLookup + ", txType: " + txType + ", paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
  
      final String THE_SUMMARY = theSummary;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final GrouperSession SESSION = session;
  
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {
  
            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {
  
              //convert the options to a map for easy access, and validate them
              @SuppressWarnings("unused")
              Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
                  params);
  
              int wsStemsLength = GrouperServiceUtils.arrayLengthAtLeastOne(
                  wsStemToSaves, GrouperWsConfig.WS_STEM_SAVE_MAX, 1000000, "stemsToSave");
  
              wsStemSaveResults.setResults(new WsStemSaveResult[wsStemsLength]);
  
              int resultIndex = 0;
  
              //loop through all stems and do the save
              for (WsStemToSave wsStemToSave : wsStemToSaves) {
                WsStemSaveResult wsStemSaveResult = new WsStemSaveResult(wsStemToSave.getWsStemLookup());
                wsStemSaveResults.getResults()[resultIndex++] = wsStemSaveResult;
  
                try {
                  //make sure everything is in order
                  wsStemToSave.validate();
                  Stem stem = wsStemToSave.save(SESSION);
  
                  wsStemSaveResult.setWsStem(new WsStem(stem));
  
                  SaveResultType saveResultType = wsStemToSave.saveResultType();
                  
                  if (saveResultType == SaveResultType.INSERT) {
                    wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS_INSERTED, clientVersion);
                  } else if (saveResultType == SaveResultType.UPDATE) {
                    wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS_UPDATED, clientVersion);
                  } else if (saveResultType == SaveResultType.NO_CHANGE) {
                    wsStemSaveResult.assignResultCode(WsStemSaveResultCode.SUCCESS_NO_CHANGES_NEEDED, clientVersion);
                  } else {
                    throw new RuntimeException("Invalid saveResultType: " + saveResultType);
                  }
                    
                } catch (Exception e) {
                  wsStemSaveResult.assignResultCodeException(e, wsStemToSave, clientVersion);
                }
              }
              //see if any inner failures cause the whole tx to fail, and/or change the outer status
              if (!wsStemSaveResults.tallyResults(TX_TYPE, THE_SUMMARY, clientVersion)) {
                grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
              }
  
              return null;
            }
          });
    } catch (Exception e) {
      wsStemSaveResults.assignResultCodeException(null, theSummary, e, clientVersion);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    //this should be the first and only return, or else it is exiting too early
    return wsStemSaveResults;
  }

  /**
   * save a stem (insert or update).  Note you cannot rename an existing stem.
   * 
   * @param stemLookupUuid the uuid of the stem to save (mutually exclusive with stemLookupName), null for insert
   * @param stemLookupName the name of the stam to save (mutually exclusive with stemLookupUuid), null for insert
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Stem#saveStem(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param stemName data of stem to save
   * @param stemUuid uuid data of stem to save
   * @param description of the stem
   * @param displayExtension of the stem
   * @param saveMode if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsStemSaveLiteResult stemSaveLite(final GrouperWsVersion clientVersion,
      String stemLookupUuid, String stemLookupName, String stemUuid, String stemName, 
      String displayExtension, String description, SaveMode saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the stem lookup
    WsStemToSave wsStemToSave = new WsStemToSave();
  
    WsStem wsStem = new WsStem();
    wsStem.setDescription(description);
    wsStem.setDisplayExtension(displayExtension);
    wsStem.setName(stemName);
    wsStem.setUuid(stemUuid);
  
    wsStemToSave.setWsStem(wsStem);
  
    WsStemLookup wsStemLookup = new WsStemLookup(stemLookupName, stemLookupUuid);
    wsStemToSave.setWsStemLookup(wsStemLookup);
  
    wsStemToSave.setSaveMode(saveMode == null ? null : saveMode.name());
  
    WsStemToSave[] wsStemsToSave = new WsStemToSave[] { wsStemToSave };
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsStemSaveResults wsStemSaveResults = stemSave(clientVersion, wsStemsToSave,
        actAsSubjectLookup, null, params);
  
    return new WsStemSaveLiteResult(wsStemSaveResults);
  }

  /**
   * save a group (insert or update).  Note you cannot rename an existing group.
   * 
   * @param groupLookupUuid the uuid of the group to save (mutually exclusive with groupLookupName), null for insert
   * @param groupLookupName the name of the stam to save (mutually exclusive with groupLookupUuid), null for insert
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @see {@link Group#saveGroup(GrouperSession, String, String, String, String, boolean, boolean, boolean)}
   * @param groupName data of group to save
   * @param groupUuid uuid data of group to save
   * @param description of the group
   * @param displayExtension of the group
   * @param saveMode if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default)
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member add
   */
  public static WsGroupSaveLiteResult groupSaveLite(final GrouperWsVersion clientVersion,
      String groupLookupUuid, String groupLookupName, String groupUuid, String groupName, 
      String displayExtension, String description, SaveMode saveMode,
      String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {
  
    // setup the group lookup
    WsGroupToSave wsGroupToSave = new WsGroupToSave();
  
    WsGroup wsGroup = new WsGroup();
    wsGroup.setDescription(description);
    wsGroup.setDisplayExtension(displayExtension);
    wsGroup.setName(groupName);
    wsGroup.setUuid(groupUuid);
  
    wsGroupToSave.setWsGroup(wsGroup);
  
    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupLookupName, groupLookupUuid);
    wsGroupToSave.setWsGroupLookup(wsGroupLookup);
  
    wsGroupToSave.setSaveMode(saveMode == null ? null : saveMode.name());
  
    WsGroupToSave[] wsGroupsToSave = new WsGroupToSave[] { wsGroupToSave };
  
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);

  
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  
    WsGroupSaveResults wsGroupSaveResults = groupSave(clientVersion, wsGroupsToSave,
        actAsSubjectLookup, null, includeGroupDetail, params);
  
    return new WsGroupSaveLiteResult(wsGroupSaveResults);
  }

//  /**
//   * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
//   * just view.  
//   * 
//   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
//   * @param wsGroupLookups
//   *            groups to save
//   * @param wsAttributeEdits are the attributes to change or delete
//   * @param actAsSubjectLookup
//   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
//   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
//   * are NONE (or blank), and READ_WRITE_NEW.
//   * @param params optional: reserved for future use
//   * @return the results
//   */
//  @SuppressWarnings("unchecked")
//  public static WsViewOrEditAttributesResults viewOrEditAttributes(final GrouperWsVersion clientVersion,
//      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
//      final WsSubjectLookup actAsSubjectLookup, final GrouperTransactionType txType,
//      final WsParam[] params) {
//  
//    GrouperSession session = null;
//    int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;
//  
//    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();
//  
//    //convert the options to a map for easy access, and validate them
//    @SuppressWarnings("unused")
//    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
//        params);
//  
//    // see if greater than the max (or default)
//    int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
//        GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
//    if (groupsSize > maxAttributeGroup) {
//      wsViewOrEditAttributesResults
//          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
//      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
//          "Number of groups must be less than max: " + maxAttributeGroup + " (sent in "
//              + groupsSize + ")");
//      return wsViewOrEditAttributesResults;
//    }
//  
//    // TODO make sure size of params and values the same
//  
//    //lets validate the attribute edits
//    boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
//    if (!readOnly) {
//      for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
//        String errorMessage = wsAttributeEdit.validate();
//        if (errorMessage != null) {
//          wsViewOrEditAttributesResults
//              .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
//          wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
//              errorMessage + ", " + wsAttributeEdit);
//        }
//      }
//    }
//  
//    // assume success
//    wsViewOrEditAttributesResults
//        .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
//    Subject actAsSubject = null;
//    // TODO have common try/catch
//    try {
//      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
//  
//      if (actAsSubject == null) {
//        // TODO make this a result code
//        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
//      }
//  
//      // use this to be the user connected, or the user act-as
//      try {
//        session = GrouperSession.start(actAsSubject);
//      } catch (SessionException se) {
//        // TODO make this a result code
//        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
//            se);
//      }
//  
//      int resultIndex = 0;
//  
//      wsViewOrEditAttributesResults
//          .setResults(new WsViewOrEditAttributesResult[groupsSize]);
//      GROUP_LOOP: for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
//        WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
//        wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
//        Group group = null;
//  
//        try {
//          wsViewOrEditAttributesResult.setGroupName(wsGroupLookup.getGroupName());
//          wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
//  
//          //get the group
//          wsGroupLookup.retrieveGroupIfNeeded(session);
//          group = wsGroupLookup.retrieveGroup();
//          if (group == null) {
//            wsViewOrEditAttributesResult
//                .assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
//            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
//                "Cant find group: '" + wsGroupLookup + "'.  ");
//            continue;
//          }
//  
//          group = wsGroupLookup.retrieveGroup();
//  
//          // these will probably match, but just in case
//          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
//            wsViewOrEditAttributesResult.setGroupName(group.getName());
//          }
//          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
//            wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
//          }
//  
//          //lets read them
//          Map<String, String> attributeMap = GrouperUtil.nonNull(group.getAttributes());
//  
//          //see if we are updating
//          if (!readOnly) {
//            for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
//              String attributeName = wsAttributeEdit.getName();
//              try {
//                //lets see if delete
//                if (wsAttributeEdit.deleteBoolean()) {
//                  //if its not there, dont bother
//                  if (attributeMap.containsKey(attributeName)) {
//                    group.deleteAttribute(attributeName);
//                    //update map
//                    attributeMap.remove(attributeName);
//                  }
//                } else {
//                  String attributeValue = wsAttributeEdit.getValue();
//                  //make sure it is different
//                  if (!StringUtils
//                      .equals(attributeValue, attributeMap.get(attributeName))) {
//                    //it is update
//                    group.setAttribute(attributeName, wsAttributeEdit.getValue());
//                    attributeMap.put(attributeName, attributeValue);
//                  }
//                }
//              } catch (AttributeNotFoundException anfe) {
//                wsViewOrEditAttributesResult
//                    .assignResultCode(WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
//                wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
//                    "Cant find attribute: " + attributeName);
//                //go to next group
//                continue GROUP_LOOP;
//  
//              }
//            }
//          }
//          //now take the attributes and put them in the result
//          if (attributeMap.size() > 0) {
//            int attributeIndex = 0;
//            WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
//            wsViewOrEditAttributesResult.setAttributes(attributes);
//            //lookup each from map and return
//            for (String key : attributeMap.keySet()) {
//              WsAttribute wsAttribute = new WsAttribute();
//              attributes[attributeIndex++] = wsAttribute;
//              wsAttribute.setName(key);
//              wsAttribute.setValue(attributeMap.get(key));
//            }
//          }
//          wsViewOrEditAttributesResult.getResultMetadata().assignSuccess("T");
//          wsViewOrEditAttributesResult.getResultMetadata().assignResultCode("SUCCESS");
//          if (readOnly) {
//            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
//                "Group '" + group.getName() + "' was queried.");
//          } else {
//            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
//                "Group '" + group.getName() + "' had attributes edited.");
//          }
//        } catch (InsufficientPrivilegeException ipe) {
//          wsViewOrEditAttributesResult
//              .assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
//          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
//              "Error: insufficient privileges to view/edit attributes '"
//                  + wsGroupLookup.getGroupName() + "'");
//        } catch (Exception e) {
//          // lump the rest in there, group_add_exception, etc
//          wsViewOrEditAttributesResult
//              .assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
//          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
//              ExceptionUtils.getFullStackTrace(e));
//          LOG.error(wsGroupLookup + ", " + e, e);
//        }
//      }
//  
//    } catch (RuntimeException re) {
//      wsViewOrEditAttributesResults
//          .assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
//      String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
//          + GrouperUtil.toStringForLog(wsGroupLookups) + ", attributeEdits: "
//          + GrouperUtil.toStringForLog(wsAttributeEdits) + ", actAsSubject: "
//          + actAsSubject + ".  \n" + "";
//      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(theError);
//      // this is sent back to the caller anyway, so just log, and not send
//      // back again
//      LOG.error(theError + ", wsViewOrEditAttributesResults: "
//          + GrouperUtil.toStringForLog(wsViewOrEditAttributesResults), re);
//    } finally {
//      if (session != null) {
//        try {
//          session.stop();
//        } catch (Exception e) {
//          LOG.error(e.getMessage(), e);
//        }
//      }
//    }
//  
//    if (wsViewOrEditAttributesResults.getResults() != null) {
//      // check all entries
//      int successes = 0;
//      int failures = 0;
//      for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
//          .getResults()) {
//        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
//            : wsGroupSaveResult.getResultMetadata().getSuccess());
//        if (success) {
//          successes++;
//        } else {
//          failures++;
//        }
//      }
//      if (failures > 0) {
//        wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
//            "There were " + successes + " successes and " + failures
//                + " failures of viewing/editing group attribues.   ");
//        wsViewOrEditAttributesResults
//            .assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
//      } else {
//        wsViewOrEditAttributesResults
//            .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
//      }
//    }
//    if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getResultMetadata()
//        .getSuccess())) {
//  
//      LOG.error(wsViewOrEditAttributesResults.getResultMetadata().getResultMessage());
//    }
//    return wsViewOrEditAttributesResults;
//  }
//
//  /**
//   * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
//   * just view.  
//   * 
//   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
//   * @param groupName
//   *            to delete the group (mutually exclusive with groupUuid)
//   * @param groupUuid
//   *            to delete the group (mutually exclusive with groupName)
//   * @param attributeName0 name of first attribute (optional)
//   * @param attributeValue0 value of first attribute (optional)
//   * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
//   * @param attributeName1 name of second attribute (optional)
//   * @param attributeValue1 value of second attribute (optional)
//   * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
//   * @param attributeName2 name of third attribute (optional)
//   * @param attributeValue2 value of third attribute (optional)
//   * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
//   * @param actAsSubjectId
//   *            optional: is the subject id of subject to act as (if
//   *            proxying). Only pass one of actAsSubjectId or
//   *            actAsSubjectIdentifer
//   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
//   * duplicates
//   * @param actAsSubjectIdentifier
//   *            optional: is the subject identifier of subject to act as (if
//   *            proxying). Only pass one of actAsSubjectId or
//   *            actAsSubjectIdentifer
//   * @param paramName0
//   *            reserved for future use
//   * @param paramValue0
//   *            reserved for future use
//   * @param paramName1
//   *            reserved for future use
//   * @param paramValue1
//   *            reserved for future use
//   * @return the result of one member add
//   */
//  public static WsViewOrEditAttributesResults viewOrEditAttributesLite(
//      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
//      String attributeName0, String attributeValue0, String attributeDelete0,
//      String attributeName1, String attributeValue1, String attributeDelete1,
//      String attributeName2, String attributeValue2, String attributeDelete2,
//      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
//      String paramName0, String paramValue0, String paramName1, String paramValue1) {
//  
//    // setup the group lookup
//    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
//    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
//  
//    //setup attributes
//    List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
//    if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
//        || !StringUtils.isBlank(attributeDelete0)) {
//      attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0,
//          attributeDelete0));
//    }
//    if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
//        || !StringUtils.isBlank(attributeDelete1)) {
//      attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1,
//          attributeDelete1));
//    }
//    if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
//        || !StringUtils.isBlank(attributeDelete2)) {
//      attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2,
//          attributeDelete2));
//    }
//    //convert to array
//    WsAttributeEdit[] wsAttributeEdits = GrouperUtil.toArray(attributeEditList,
//        WsAttributeEdit.class);
//    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
//        actAsSubjectSourceId, actAsSubjectIdentifier);
//  
//    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
//  
//    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(
//        clientVersion, wsGroupLookups, wsAttributeEdits, actAsSubjectLookup, null,
//        params);
//  
//    return wsViewOrEditAttributesResults;
//  }
//
  
  /**
   * <pre>
   * see if a group has a member (if already a direct member, ignore)
   * e.g. /grouperPrivileges/subjects/1234567/groups/aStem:aGroup/types/access/names/update
   * e.g. /grouperPrivileges/subjects/sources/someSource/subjectId/1234567/stems/aStem1:aStem2/
   * </pre>
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param subjectId subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
   * @param subjectSourceId source id of subject object (optional)
   * @param subjectIdentifier subject identifier of subject.  Mutuallyexclusive with subjectId
   * @param groupName if this is a group privilege.  mutually exclusive with groupUuid
   * @param groupUuid if this is a group privilege.  mutually exclusive with groupName
   * @param stemName if this is a stem privilege.  mutually exclusive with stemUuid
   * @param stemUuid if this is a stem privilege.  mutually exclusive with stemName
   * @param actAsSubjectId
   *            optional: is the subject id of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
   * duplicates
   * @param actAsSubjectIdentifier
   *            optional: is the subject identifier of subject to act as (if
   *            proxying). Only pass one of actAsSubjectId or
   *            actAsSubjectIdentifer
   * @param privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeName (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param includeGroupDetail T or F as for if group detail should be included
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the result of one member query
   */
  @SuppressWarnings({ "cast", "unchecked" })
  public static WsGetGrouperPrivilegesLiteResult getGrouperPrivilegesLite(final GrouperWsVersion clientVersion, 
      String subjectId, String subjectSourceId, String subjectIdentifier,
      String groupName, String groupUuid, 
      String stemName, String stemUuid, 
      PrivilegeType privilegeType, Privilege privilegeName,
      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
      boolean includeSubjectDetail, String subjectAttributeNames, 
      boolean includeGroupDetail, String paramName0,
      String paramValue0, String paramName1, String paramValue1) {

    GrouperWsVersion.assignCurrentClientVersion(clientVersion);

    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

    WsSubjectLookup subjectLookup = new WsSubjectLookup(subjectId, 
        subjectSourceId, subjectIdentifier);

    WsStemLookup wsStemLookup = new WsStemLookup(stemName, stemUuid);

    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    
    // setup the subject lookup
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);


    WsParam[] params = GrouperServiceUtils.params(paramName0, 
        paramValue0, paramValue1, paramValue1);

    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = 
      new WsGetGrouperPrivilegesLiteResult();
      
    GrouperSession session = null;
    String theSummary = null;
    
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsSubject: "
          + subjectLookup + ", group: " +  wsGroupLookup + ", stem: " + wsStemLookup 
          + ", privilege: " + (privilegeType == null ? null : privilegeType.getPrivilegeName()) 
          + "-" + (privilegeName == null ? null : privilegeName.getName())
          + ", actAsSubject: "
          + actAsSubjectLookup 
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
        
      subjectAttributeArray = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeArray, includeSubjectDetail);

      wsGetGrouperPrivilegesLiteResult.setSubjectAttributeNames(subjectAttributeArray);
      
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      boolean hasGroup = wsGroupLookup.hasData();
      boolean hasStem = wsStemLookup.hasData();

      //cant have both
      if (hasGroup && hasStem) {
        throw new WsInvalidQueryException("Cant pass both group and stem.  Pass one or the other");
      }
      
    
      boolean hasSubject = !subjectLookup.blank();
      boolean hasPrivilege = privilegeName != null;
      
      //lets try to assign privilege type if not assigned
      if (privilegeType == null && hasPrivilege) {
        if (Privilege.isAccess(privilegeName)) {
          privilegeType = PrivilegeType.ACCESS;
        } else if (Privilege.isNaming(privilegeName)) {
          privilegeType = PrivilegeType.NAMING;
        } else {
          throw new RuntimeException("Unexpected privilege, cant find type: " + privilegeName);
        }
      }
      
      if (privilegeType == null) {
        if (hasGroup) {
          privilegeType = PrivilegeType.ACCESS;
        } else if (hasStem) {
          privilegeType = PrivilegeType.NAMING;
        }
      }
      
      boolean groupPrivilege = PrivilegeType.ACCESS.equals(privilegeType);
      boolean stemPrivilege = PrivilegeType.NAMING.equals(privilegeType);

      //make sure the privilege type matches
      if (privilegeType != null) {
          
        if (hasGroup && !groupPrivilege) {
            throw new WsInvalidQueryException("If you are querying a group, you need to pass in an " +
              "access privilege type: '" + privilegeType + "', e.g. admin|view|read|optin|optout|update");
          }

        if (hasStem && !stemPrivilege) {
          throw new WsInvalidQueryException("If you are querying a stem, you need to pass in a " +
              "naming privilege type: '" + privilegeType + "', e.g. stem|create");
        }
      }

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
    
      
      Subject subject = null;
      
      if (hasSubject) {
        subject = subjectLookup.retrieveSubject();
        //need to check to see status      
        wsGetGrouperPrivilegesLiteResult.processSubject(subjectLookup, subjectAttributeArray);
        
        if (subject == null) {
          throw new WebServiceDoneException();
        }
      } else {
        if (!hasGroup && !hasStem) {
          //what are we filtering by???
          throw new RuntimeException("Not enough information in the query, pass in at least a subject or group or stem");
        }
      }
      
      AccessResolver accessResolver = session.getAccessResolver();
      NamingResolver namingResolver = session.getNamingResolver();
      
      TreeSet<GrouperPrivilege> privileges = new TreeSet<GrouperPrivilege>();
      if (hasGroup) {
        
        Group group = wsGroupLookup.retrieveGroupIfNeeded(session, "wsGroupLookup");
        
        //handle bad stuff
        if (group == null) {
          GroupFindResult groupFindResult = wsGroupLookup.retrieveGroupFindResult();
          if (groupFindResult == GroupFindResult.GROUP_NOT_FOUND) {
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.GROUP_NOT_FOUND);
            throw new WebServiceDoneException();
          }
          throw new RuntimeException(groupFindResult == null ? null : groupFindResult.toString());
        }

        if (subject != null) {

          privileges.addAll(GrouperUtil.nonNull(group.getPrivs(subject)));
        } else {
          Set<Subject> subjects = new HashSet<Subject>();
          if (privilegeName == null || AccessPrivilege.ADMIN.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.ADMIN)));
          } 
          if (privilegeName == null || AccessPrivilege.OPTIN.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.OPTIN)));
          } 
          if (privilegeName == null || AccessPrivilege.OPTOUT.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.OPTOUT)));
          } 
          if (privilegeName == null || AccessPrivilege.READ.equals(privilegeName)) {
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.READ)));
          } 
          if (privilegeName == null || AccessPrivilege.UPDATE.equals(privilegeName)) {
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.UPDATE)));
          } 
          if (privilegeName == null || AccessPrivilege.VIEW.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(accessResolver.getSubjectsWithPrivilege(group, AccessPrivilege.VIEW)));
          } 
          //make it a little more efficient, note subjects dont have equals and hashcode, cant just use set
          SubjectHelper.removeDuplicates(subjects);
          //add privs
          for (Subject current : subjects) {
            privileges.addAll(accessResolver.getPrivileges(group, current));
          }
        }
      } else if (hasStem) {

        wsStemLookup.retrieveStemIfNeeded(session, true);
        Stem stem = wsStemLookup.retrieveStem();
        //handle bad stuff
        if (stem == null) {
          StemFindResult stemFindResult = wsStemLookup.retrieveStemFindResult();
          if (stemFindResult == StemFindResult.STEM_NOT_FOUND) {
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.STEM_NOT_FOUND);
            throw new WebServiceDoneException();
          }
          throw new RuntimeException(stemFindResult == null ? null : stemFindResult.toString());
        }

        if (subject != null) {

          privileges.addAll(GrouperUtil.nonNull(stem.getPrivs(subject)));
        } else {
          Set<Subject> subjects = new HashSet<Subject>();
          if (privilegeName == null || NamingPrivilege.CREATE.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(namingResolver.getSubjectsWithPrivilege(stem, NamingPrivilege.CREATE)));
          } 
          if (privilegeName == null || NamingPrivilege.STEM.equals(privilegeName)) { 
            subjects.addAll(GrouperUtil.nonNull(namingResolver.getSubjectsWithPrivilege(stem, NamingPrivilege.STEM)));
          } 
          //make it a little more efficient, note subjects dont have equals and hashcode, cant just use set
          SubjectHelper.removeDuplicates(subjects);
          //add privs
          for (Subject current : subjects) {
            privileges.addAll(namingResolver.getPrivileges(stem, current));
          }
        }
      } else {

        //if group privilege, then 
        Member member = subjectLookup.retrieveMember();
        //if there is no member record, then there is nothing
        if (member != null) {
          //this means no group or stem, but has subject
          if (groupPrivilege || privilegeType == null) {

            Set<Group> groups = new HashSet<Group>();
            if (privilegeName == null || AccessPrivilege.ADMIN.equals(privilegeName)) { 
              groups.addAll(member.hasAdmin());
            } 
            if (privilegeName == null || AccessPrivilege.OPTIN.equals(privilegeName)) { 
              groups.addAll(member.hasOptin());
            } 
            if (privilegeName == null || AccessPrivilege.OPTOUT.equals(privilegeName)) { 
              groups.addAll(member.hasOptout());
            } 
            if (privilegeName == null || AccessPrivilege.READ.equals(privilegeName)) { 
              groups.addAll(member.hasRead());
            } 
            if (privilegeName == null || AccessPrivilege.UPDATE.equals(privilegeName)) { 
              groups.addAll(member.hasUpdate());
            } 
            if (privilegeName == null || AccessPrivilege.VIEW.equals(privilegeName)) { 
              groups.addAll(member.hasView());
            } 
            //from there lets get the privilege
            for (Group group : groups) {
              privileges.addAll(GrouperUtil.nonNull(group.getPrivs(subject)));
            }
          }
          //this means no group or stem, but has subject
          if (stemPrivilege || privilegeType == null) {

            Set<Stem> stems = new HashSet<Stem>();
            if (privilegeName == null || NamingPrivilege.CREATE.equals(privilegeName)) { 
              stems.addAll(member.hasCreate());
            } 
            if (privilegeName == null || NamingPrivilege.STEM.equals(privilegeName)) { 
              stems.addAll(member.hasStem());
            } 
            //from there lets get the privilege
            for (Stem stem : stems) {
              privileges.addAll(GrouperUtil.nonNull(stem.getPrivs(subject)));
            }
          }

        }
        }

        //see if we need to remove, if specifying privs, and this doesnt match
        Iterator<? extends GrouperPrivilege> iterator = privileges.iterator();
        while (iterator.hasNext()) {
          GrouperPrivilege current = iterator.next();
          if (privilegeName != null && !StringUtils.equals(privilegeName.getName(), current.getName())){
            iterator.remove();
          }          
        }

        WsGrouperPrivilegeResult[] privilegeResults = new WsGrouperPrivilegeResult[privileges.size()];
        if (privileges.size() > 0) {
          
          wsGetGrouperPrivilegesLiteResult.setPrivilegeResults(privilegeResults);
        }
        
        int i=0;
        for (GrouperPrivilege grouperPrivilege : privileges) {
          
          WsGrouperPrivilegeResult wsGrouperPrivilegeResult = new WsGrouperPrivilegeResult();
          privilegeResults[i] = wsGrouperPrivilegeResult;
          
          wsGrouperPrivilegeResult.setAllowed("T");
          Subject owner = grouperPrivilege.getOwner();
          wsGrouperPrivilegeResult.setOwnerSubject(owner == null ? null : new WsSubject(owner, subjectAttributeArray, null));

          String thePrivilegeName = grouperPrivilege.getName();
          wsGrouperPrivilegeResult.setPrivilegeName(thePrivilegeName);
          wsGrouperPrivilegeResult.setPrivilegeType(grouperPrivilege.getType());
          
          wsGrouperPrivilegeResult.setRevokable(grouperPrivilege.isRevokable() ? "T" : "F");

          GrouperAPI groupOrStem = grouperPrivilege.getGrouperApi();
          if (groupOrStem instanceof Group) {
            Group group = (Group)groupOrStem;
            wsGrouperPrivilegeResult.setWsGroup(new WsGroup(group, null, includeGroupDetail));
          } else if (groupOrStem instanceof Stem) {
            Stem stem = (Stem)groupOrStem;
            wsGrouperPrivilegeResult.setWsStem(new WsStem(stem));
          }

          //note, if there is a subejct lookup, it should match here
          //make sure they match
          Subject privilegeSubject = grouperPrivilege.getSubject(); 
          
          //(granted we should check source too, but it might not be available
        if (subject != null) {
          if (!StringUtils.equals(privilegeSubject.getId(), subject.getId())) {
            throw new RuntimeException("These subjects should be equal: " 
                + GrouperUtil.subjectToString(privilegeSubject) + ", " 
                + GrouperUtil.subjectToString(subject));
          }
          }

        //only pass in the subject lookup if it wasnt null
        wsGrouperPrivilegeResult.setWsSubject(new WsSubject(privilegeSubject, subjectAttributeArray, 
            subject != null ? subjectLookup : null));
          
          i++;
        }

        //if the privilege was queried, and group/stem and subject... then it should be one alswer
        if (privilegeName != null && (wsGroupLookup.hasData() ^ wsStemLookup.hasData()) &&
            subject != null && (privileges.size() == 1 || privileges.size() == 0)) {
          
          if (privileges.size() == 1) {
            
            //assign one of 2 success codes
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUCCESS_ALLOWED);
          } else {
            
            //assign one of 2 success codes
            wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUCCESS_NOT_ALLOWED);
          }
          
          
        } else {
        
          //assign success and the real privs are in the XML
          wsGetGrouperPrivilegesLiteResult.assignResultCode(WsGetGrouperPrivilegesLiteResultCode.SUCCESS);
        }
      
    } catch (WebServiceDoneException wsde) {
      //ignore this
    } catch (InsufficientPrivilegeException ipe) {
      wsGetGrouperPrivilegesLiteResult
          .assignResultCode(WsGetGrouperPrivilegesLiteResultCode.INSUFFICIENT_PRIVILEGES);
    } catch (Exception e) {
      wsGetGrouperPrivilegesLiteResult.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetGrouperPrivilegesLiteResult;

    
  }

  //  /**
  //   * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
  //   * just view.  
  //   * 
  //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
  //   * @param wsGroupLookups
  //   *            groups to save
  //   * @param wsAttributeEdits are the attributes to change or delete
  //   * @param actAsSubjectLookup
  //   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
  //   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
  //   * are NONE (or blank), and READ_WRITE_NEW.
  //   * @param params optional: reserved for future use
  //   * @return the results
  //   */
  //  @SuppressWarnings("unchecked")
  //  public static WsViewOrEditAttributesResults viewOrEditAttributes(final GrouperWsVersion clientVersion,
  //      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
  //      final WsSubjectLookup actAsSubjectLookup, final GrouperTransactionType txType,
  //      final WsParam[] params) {
  //  
  //    GrouperSession session = null;
  //    int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;
  //  
  //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();
  //  
  //    //convert the options to a map for easy access, and validate them
  //    @SuppressWarnings("unused")
  //    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
  //        params);
  //  
  //    // see if greater than the max (or default)
  //    int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
  //        GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
  //    if (groupsSize > maxAttributeGroup) {
  //      wsViewOrEditAttributesResults
  //          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
  //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
  //          "Number of groups must be less than max: " + maxAttributeGroup + " (sent in "
  //              + groupsSize + ")");
  //      return wsViewOrEditAttributesResults;
  //    }
  //  
  //    // TODO make sure size of params and values the same
  //  
  //    //lets validate the attribute edits
  //    boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
  //    if (!readOnly) {
  //      for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
  //        String errorMessage = wsAttributeEdit.validate();
  //        if (errorMessage != null) {
  //          wsViewOrEditAttributesResults
  //              .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
  //          wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
  //              errorMessage + ", " + wsAttributeEdit);
  //        }
  //      }
  //    }
  //  
  //    // assume success
  //    wsViewOrEditAttributesResults
  //        .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
  //    Subject actAsSubject = null;
  //    // TODO have common try/catch
  //    try {
  //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
  //  
  //      if (actAsSubject == null) {
  //        // TODO make this a result code
  //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
  //      }
  //  
  //      // use this to be the user connected, or the user act-as
  //      try {
  //        session = GrouperSession.start(actAsSubject);
  //      } catch (SessionException se) {
  //        // TODO make this a result code
  //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
  //            se);
  //      }
  //  
  //      int resultIndex = 0;
  //  
  //      wsViewOrEditAttributesResults
  //          .setResults(new WsViewOrEditAttributesResult[groupsSize]);
  //      GROUP_LOOP: for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
  //        WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
  //        wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
  //        Group group = null;
  //  
  //        try {
  //          wsViewOrEditAttributesResult.setGroupName(wsGroupLookup.getGroupName());
  //          wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
  //  
  //          //get the group
  //          wsGroupLookup.retrieveGroupIfNeeded(session);
  //          group = wsGroupLookup.retrieveGroup();
  //          if (group == null) {
  //            wsViewOrEditAttributesResult
  //                .assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
  //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                "Cant find group: '" + wsGroupLookup + "'.  ");
  //            continue;
  //          }
  //  
  //          group = wsGroupLookup.retrieveGroup();
  //  
  //          // these will probably match, but just in case
  //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
  //            wsViewOrEditAttributesResult.setGroupName(group.getName());
  //          }
  //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
  //            wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
  //          }
  //  
  //          //lets read them
  //          Map<String, String> attributeMap = GrouperUtil.nonNull(group.getAttributes());
  //  
  //          //see if we are updating
  //          if (!readOnly) {
  //            for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
  //              String attributeName = wsAttributeEdit.getName();
  //              try {
  //                //lets see if delete
  //                if (wsAttributeEdit.deleteBoolean()) {
  //                  //if its not there, dont bother
  //                  if (attributeMap.containsKey(attributeName)) {
  //                    group.deleteAttribute(attributeName);
  //                    //update map
  //                    attributeMap.remove(attributeName);
  //                  }
  //                } else {
  //                  String attributeValue = wsAttributeEdit.getValue();
  //                  //make sure it is different
  //                  if (!StringUtils
  //                      .equals(attributeValue, attributeMap.get(attributeName))) {
  //                    //it is update
  //                    group.setAttribute(attributeName, wsAttributeEdit.getValue());
  //                    attributeMap.put(attributeName, attributeValue);
  //                  }
  //                }
  //              } catch (AttributeNotFoundException anfe) {
  //                wsViewOrEditAttributesResult
  //                    .assignResultCode(WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
  //                wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                    "Cant find attribute: " + attributeName);
  //                //go to next group
  //                continue GROUP_LOOP;
  //  
  //              }
  //            }
  //          }
  //          //now take the attributes and put them in the result
  //          if (attributeMap.size() > 0) {
  //            int attributeIndex = 0;
  //            WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
  //            wsViewOrEditAttributesResult.setAttributes(attributes);
  //            //lookup each from map and return
  //            for (String key : attributeMap.keySet()) {
  //              WsAttribute wsAttribute = new WsAttribute();
  //              attributes[attributeIndex++] = wsAttribute;
  //              wsAttribute.setName(key);
  //              wsAttribute.setValue(attributeMap.get(key));
  //            }
  //          }
  //          wsViewOrEditAttributesResult.getResultMetadata().assignSuccess("T");
  //          wsViewOrEditAttributesResult.getResultMetadata().assignResultCode("SUCCESS");
  //          if (readOnly) {
  //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                "Group '" + group.getName() + "' was queried.");
  //          } else {
  //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //                "Group '" + group.getName() + "' had attributes edited.");
  //          }
  //        } catch (InsufficientPrivilegeException ipe) {
  //          wsViewOrEditAttributesResult
  //              .assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
  //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //              "Error: insufficient privileges to view/edit attributes '"
  //                  + wsGroupLookup.getGroupName() + "'");
  //        } catch (Exception e) {
  //          // lump the rest in there, group_add_exception, etc
  //          wsViewOrEditAttributesResult
  //              .assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
  //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
  //              ExceptionUtils.getFullStackTrace(e));
  //          LOG.error(wsGroupLookup + ", " + e, e);
  //        }
  //      }
  //  
  //    } catch (RuntimeException re) {
  //      wsViewOrEditAttributesResults
  //          .assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
  //      String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
  //          + GrouperUtil.toStringForLog(wsGroupLookups) + ", attributeEdits: "
  //          + GrouperUtil.toStringForLog(wsAttributeEdits) + ", actAsSubject: "
  //          + actAsSubject + ".  \n" + "";
  //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(theError);
  //      // this is sent back to the caller anyway, so just log, and not send
  //      // back again
  //      LOG.error(theError + ", wsViewOrEditAttributesResults: "
  //          + GrouperUtil.toStringForLog(wsViewOrEditAttributesResults), re);
  //    } finally {
  //      if (session != null) {
  //        try {
  //          session.stop();
  //        } catch (Exception e) {
  //          LOG.error(e.getMessage(), e);
  //        }
  //      }
  //    }
  //  
  //    if (wsViewOrEditAttributesResults.getResults() != null) {
  //      // check all entries
  //      int successes = 0;
  //      int failures = 0;
  //      for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
  //          .getResults()) {
  //        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
  //            : wsGroupSaveResult.getResultMetadata().getSuccess());
  //        if (success) {
  //          successes++;
  //        } else {
  //          failures++;
  //        }
  //      }
  //      if (failures > 0) {
  //        wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
  //            "There were " + successes + " successes and " + failures
  //                + " failures of viewing/editing group attribues.   ");
  //        wsViewOrEditAttributesResults
  //            .assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
  //      } else {
  //        wsViewOrEditAttributesResults
  //            .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
  //      }
  //    }
  //    if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getResultMetadata()
  //        .getSuccess())) {
  //  
  //      LOG.error(wsViewOrEditAttributesResults.getResultMetadata().getResultMessage());
  //    }
  //    return wsViewOrEditAttributesResults;
  //  }
  //
  //  /**
  //   * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
  //   * just view.  
  //   * 
  //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
  //   * @param groupName
  //   *            to delete the group (mutually exclusive with groupUuid)
  //   * @param groupUuid
  //   *            to delete the group (mutually exclusive with groupName)
  //   * @param attributeName0 name of first attribute (optional)
  //   * @param attributeValue0 value of first attribute (optional)
  //   * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
  //   * @param attributeName1 name of second attribute (optional)
  //   * @param attributeValue1 value of second attribute (optional)
  //   * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
  //   * @param attributeName2 name of third attribute (optional)
  //   * @param attributeValue2 value of third attribute (optional)
  //   * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
  //   * @param actAsSubjectId
  //   *            optional: is the subject id of subject to act as (if
  //   *            proxying). Only pass one of actAsSubjectId or
  //   *            actAsSubjectIdentifer
  //   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
  //   * duplicates
  //   * @param actAsSubjectIdentifier
  //   *            optional: is the subject identifier of subject to act as (if
  //   *            proxying). Only pass one of actAsSubjectId or
  //   *            actAsSubjectIdentifer
  //   * @param paramName0
  //   *            reserved for future use
  //   * @param paramValue0
  //   *            reserved for future use
  //   * @param paramName1
  //   *            reserved for future use
  //   * @param paramValue1
  //   *            reserved for future use
  //   * @return the result of one member add
  //   */
  //  public static WsViewOrEditAttributesResults viewOrEditAttributesLite(
  //      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
  //      String attributeName0, String attributeValue0, String attributeDelete0,
  //      String attributeName1, String attributeValue1, String attributeDelete1,
  //      String attributeName2, String attributeValue2, String attributeDelete2,
  //      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
  //      String paramName0, String paramValue0, String paramName1, String paramValue1) {
  //  
  //    // setup the group lookup
  //    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
  //    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
  //  
  //    //setup attributes
  //    List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
  //    if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
  //        || !StringUtils.isBlank(attributeDelete0)) {
  //      attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0,
  //          attributeDelete0));
  //    }
  //    if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
  //        || !StringUtils.isBlank(attributeDelete1)) {
  //      attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1,
  //          attributeDelete1));
  //    }
  //    if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
  //        || !StringUtils.isBlank(attributeDelete2)) {
  //      attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2,
  //          attributeDelete2));
  //    }
  //    //convert to array
  //    WsAttributeEdit[] wsAttributeEdits = GrouperUtil.toArray(attributeEditList,
  //        WsAttributeEdit.class);
  //    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
  //        actAsSubjectSourceId, actAsSubjectIdentifier);
  //  
  //    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
  //  
  //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(
  //        clientVersion, wsGroupLookups, wsAttributeEdits, actAsSubjectLookup, null,
  //        params);
  //  
  //    return wsViewOrEditAttributesResults;
  //  }
  //
    
    /**
     * <pre>
     * assign a privilege for a user/group/type/name combo
     * e.g. /grouperPrivileges/subjects/1234567/groups/aStem:aGroup/types/access/names/update
     * e.g. /grouperPrivileges/subjects/sources/someSource/subjectId/1234567/stems/aStem1:aStem2/
     * </pre>
     * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param subjectId subject id of subject to search for privileges.  Mutually exclusive with subjectIdentifier
     * @param subjectSourceId source id of subject object (optional)
     * @param subjectIdentifier subject identifier of subject.  Mutuallyexclusive with subjectId
     * @param groupName if this is a group privilege.  mutually exclusive with groupUuid
     * @param groupUuid if this is a group privilege.  mutually exclusive with groupName
     * @param stemName if this is a stem privilege.  mutually exclusive with stemUuid
     * @param stemUuid if this is a stem privilege.  mutually exclusive with stemName
     * @param actAsSubjectId
     *            optional: is the subject id of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
     * duplicates
     * @param actAsSubjectIdentifier
     *            optional: is the subject identifier of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param privilegeType (e.g. "access" for groups and "naming" for stems)
     * @param privilegeName (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
     * stem, create)
     * @param allowed is T to allow this privilege, F to deny this privilege
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
     * @param includeGroupDetail T or F as for if group detail should be included
     * @param paramName0
     *            reserved for future use
     * @param paramValue0
     *            reserved for future use
     * @param paramName1
     *            reserved for future use
     * @param paramValue1
     *            reserved for future use
     * @return the result of one member query
     */
    public static WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLite(
        final GrouperWsVersion clientVersion, 
        String subjectId, String subjectSourceId, String subjectIdentifier,
        String groupName, String groupUuid, 
        String stemName, String stemUuid, 
        PrivilegeType privilegeType, Privilege privilegeName,
        boolean allowed,
        String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
        boolean includeSubjectDetail, String subjectAttributeNames, 
        boolean includeGroupDetail, String paramName0,
        String paramValue0, String paramName1, String paramValue1) {

      // setup the group lookup
      WsGroupLookup wsGroupLookup = null;
      
      if (!StringUtils.isBlank(groupName) || !StringUtils.isBlank(groupUuid)) {
        wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
      }

      WsStemLookup wsStemLookup = null;
      
      if (!StringUtils.isBlank(stemName) || !StringUtils.isBlank(stemUuid)) {
        wsStemLookup = new WsStemLookup(stemName, stemUuid);
      }

      // setup the subject lookup
      WsSubjectLookup[] subjectLookups = new WsSubjectLookup[1];
      subjectLookups[0] = new WsSubjectLookup(subjectId, subjectSourceId, subjectIdentifier);
      WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
          actAsSubjectSourceId, actAsSubjectIdentifier);


      WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

      Privilege[] privileges = new Privilege[]{privilegeName};
      
      String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");

      WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = assignGrouperPrivileges(clientVersion, 
          subjectLookups, wsGroupLookup, wsStemLookup, privilegeType, privileges, allowed, false, null, actAsSubjectLookup, 
          includeSubjectDetail, subjectAttributeArray, includeGroupDetail, params);

      WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = new WsAssignGrouperPrivilegesLiteResult(
          wsAssignGrouperPrivilegesResults);

      return wsAssignGrouperPrivilegesLiteResult;
    }

    /**
     * get subjects from searching by id or identifier or search string.  Can filter by subjects which
     * are members in a group.
     * 
     * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param wsSubjectLookups are subjects to look in
     * @param searchString free form string query to find a list of subjects (exact behavior depends on source)
     * @param wsMemberFilter
     *            must be one of All, Effective, Immediate, Composite, NonImmediate
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param actAsSubjectLookup
     * @param fieldName is if the memberships should be retrieved from a certain field membership
     * of the group (certain list)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent
     * @param includeGroupDetail T or F as to if the group detail should be returned
     * @param params optional: reserved for future use
     * @param sourceIds are sources to look in for memberships, or null if all
     * @param wsGroupLookup specify a group if the subjects must be in the group (limit of number of subjects
     * found in list is much lower e.g. 1000)
     * @return the results
     */
    @SuppressWarnings("unchecked")
    public static WsGetSubjectsResults getSubjects(final GrouperWsVersion clientVersion,
        WsSubjectLookup[] wsSubjectLookups, String searchString, boolean includeSubjectDetail,
        String[] subjectAttributeNames, WsSubjectLookup actAsSubjectLookup, 
        String[] sourceIds, WsGroupLookup wsGroupLookup, WsMemberFilter wsMemberFilter,
        Field fieldName, boolean includeGroupDetail, final WsParam[] params) {  
    
      WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
    
      GrouperSession session = null;
      String theSummary = null;
      try {
    
        theSummary = "clientVersion: " + clientVersion + ", wsSubjectLookups: "
            + GrouperUtil.toStringForLog(wsSubjectLookups, 200) + ", searchString: '" + searchString + "'" 
            + ", wsMemberFilter: " + wsMemberFilter
            + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
            + actAsSubjectLookup + ", fieldName: " + fieldName  + ", wsGroupLookup: " + wsGroupLookup
            + ", subjectAttributeNames: "
            + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
            + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsSubjectLookups: "
            + GrouperUtil.toStringForLog(wsSubjectLookups, 200) + "\n, sourceIds: " + GrouperUtil.toStringForLog(sourceIds, 100);
    
        //start session based on logged in user or the actAs passed in
        session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
    
        //convert the options to a map for easy access, and validate them
        @SuppressWarnings("unused")
        Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
            params);
        
        MembershipType membershipType = null;
        if (wsMemberFilter != null) {
          membershipType = wsMemberFilter.getMembershipType();
        }
        
        Group group = null;
        if (wsGroupLookup != null && wsGroupLookup.hasData()) {
          wsGroupLookup.retrieveGroupIfNeeded(session, "getSubjects group is not valid");
          group = wsGroupLookup.retrieveGroup();            
          wsGetSubjectsResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
        }
        
        boolean filteringByGroup = group != null;
        
        //get all the members
        Set<Subject> resultSubjects = new HashSet<Subject>();
        Set<WsSubject> resultWsSubjects = new TreeSet<WsSubject>();

        String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
          .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

        wsGetSubjectsResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);

        //we need to keep track of the lookups if doing group filtering and specifying the users by id or identifier
        //multikey of source id to subject id
        Map<MultiKey, WsSubjectLookup> subjectLookupMap = null;
        
        //find members by id or identifier
        if (GrouperUtil.length(wsSubjectLookups) > 0 && !wsSubjectLookups[0].blank()) {
          
          subjectLookupMap = new HashMap<MultiKey, WsSubjectLookup>();
          
          for (WsSubjectLookup wsSubjectLookup : wsSubjectLookups) {
            if (wsSubjectLookup == null) {
              continue;
            }
            
            Subject subject = wsSubjectLookup.retrieveSubject();
            
            //normally we will keep the subjects not found, but not if filtering by group, no subject, or result
            if (subject == null && filteringByGroup) {
              continue; 
            }
            
            if (subject != null) {
              subjectLookupMap.put(SubjectHelper.convertToMultiKey(subject), wsSubjectLookup);
            }
            
            //keep track here if not filtering by group
            if (!filteringByGroup) {
              WsSubject wsSubject = new WsSubject(subject, subjectAttributeNamesToRetrieve, wsSubjectLookup);
              
              resultWsSubjects.add(wsSubject);
            }
            
            if (subject == null) {

              continue;
            }
            resultSubjects.add(subject);
          }
        }
        
        //free form search
        if (!StringUtils.isBlank(searchString)) {
          
          //if filtering by stem, and stem not found, then dont find any memberships
          Set<Source> sources = GrouperUtil.convertSources(sourceIds);
          
          Set<Subject> subjects = SubjectFinder.findAll(searchString, sources);
          
          for (Subject subject: GrouperUtil.nonNull(subjects)) {
            resultSubjects.add(subject);
            
            //keep track here if not filtering by group
            if (!filteringByGroup) {
              resultWsSubjects.add(new WsSubject(subject, subjectAttributeNamesToRetrieve, null));
            }
              
          }
          
        }
        
        int resultSubjectsLengthPreGroup = GrouperUtil.length(resultSubjects);
        if (filteringByGroup && resultSubjectsLengthPreGroup > 0) {
          //we have a list of subjects, lets see if they are too large
          if (resultSubjectsLengthPreGroup > GrouperWsConfig.getPropertyInt("ws.get.subjects.max.filter.by.group", 1000)) {
            throw new TooManyResultsWhenFilteringByGroupException();
          }
          
          //lets filter by group
          Set<Member> members = MemberFinder.findBySubjectsInGroup(session, resultSubjects, group, fieldName, membershipType);
          
          resultSubjects = null;
          
          if (GrouperUtil.length(members) > 0) {
            resultSubjects = new HashSet<Subject>();
            for (Member member : members) {
              Subject subject = member.getSubject();
              
              WsSubjectLookup wsSubjectLookup = null;
              if (subjectLookupMap != null) {
                
                wsSubjectLookup = subjectLookupMap.get(SubjectHelper.convertToMultiKey(subject));
                
              }
              
              WsSubject wsSubject = new WsSubject(subject, subjectAttributeNamesToRetrieve, wsSubjectLookup);
              resultWsSubjects.add(wsSubject);
            }
          }
        }
        
        //calculate and return the results
        if (GrouperUtil.length(resultWsSubjects) > 0) {
          
          wsGetSubjectsResults.setWsSubjects(GrouperUtil.toArray(resultWsSubjects, WsSubject.class));
        }
        
        wsGetSubjectsResults.assignResultCode(WsGetSubjectsResultsCode.SUCCESS);
        
        wsGetSubjectsResults.getResultMetadata().setResultMessage(
            "Queried " + GrouperUtil.length(wsGetSubjectsResults.getWsSubjects()) + " subjects");
          
      } catch (Exception e) {
        wsGetSubjectsResults.assignResultCodeException(null, theSummary, e);
      } finally {
        GrouperSession.stopQuietly(session);
      }

      return wsGetSubjectsResults;
    
    }

    /**
     * get subjects from searching by id or identifier or search string.  Can filter by subjects which
     * are members in a group.
     * 
     * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
     * @param wsSubjectLookups are subjects to look in
     * @param subjectId to find a subject by id
     * @param sourceId to find a subject by id or identifier
     * @param subjectIdentifier to find a subject by identifier
     * @param searchString free form string query to find a list of subjects (exact behavior depends on source)
     * @param wsMemberFilter
     *            must be one of All, Effective, Immediate, Composite, NonImmediate or null (all)
     * @param includeSubjectDetail
     *            T|F, for if the extended subject information should be
     *            returned (anything more than just the id)
     * @param actAsSubjectId
     *            optional: is the subject id of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
     * duplicates
     * @param actAsSubjectIdentifier
     *            optional: is the subject identifier of subject to act as (if
     *            proxying). Only pass one of actAsSubjectId or
     *            actAsSubjectIdentifer
     * @param fieldName is if the memberships should be retrieved from a certain field membership
     * of the group (certain list)
     * @param subjectAttributeNames are the additional subject attributes (data) to return.
     * If blank, whatever is configured in the grouper-ws.properties will be sent.  Comma-separate
     * if multiple
     * @param includeGroupDetail T or F as to if the group detail should be returned
     * @param paramName0
     *            reserved for future use
     * @param paramValue0
     *            reserved for future use
     * @param paramName1
     *            reserved for future use
     * @param paramValue1
     *            reserved for future use
     * @param sourceIds are comma separated sourceIds for a searchString
     * @param groupName specify a group if the subjects must be in the group (limit of number of subjects
     * found in list is much lower e.g. 1000)
     * @param groupUuid specify a group if the subjects must be in the group (limit of number of subjects
     * found in list is much lower e.g. 1000)
     * @return the results or none if none found
     */
    public static WsGetSubjectsResults getSubjectsLite(final GrouperWsVersion clientVersion,
        String subjectId, String sourceId, String subjectIdentifier, String searchString,
        boolean includeSubjectDetail, String subjectAttributeNames,
        String actAsSubjectId, String actAsSubjectSourceId,
        String actAsSubjectIdentifier, String sourceIds,
        String groupName, String groupUuid, WsMemberFilter wsMemberFilter,
        Field fieldName, boolean includeGroupDetail, String paramName0, String paramValue0,
        String paramName1, String paramValue1) {
    
      // setup the group lookup
      WsGroupLookup wsGroupLookup = null;
      
      if (StringUtils.isNotBlank(groupName) || StringUtils.isNotBlank(groupUuid)) {
        wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
      }
    
      WsSubjectLookup wsSubjectLookup = WsSubjectLookup.createIfNeeded(subjectId, sourceId, subjectIdentifier);
      
      WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
          actAsSubjectSourceId, actAsSubjectIdentifier);
    
      WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
    
      String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
      // pass through to the more comprehensive method
      WsSubjectLookup[] wsSubjectLookups = wsSubjectLookup == null ? null : new WsSubjectLookup[]{wsSubjectLookup};
      
      String[] sourceIdArray = GrouperUtil.splitTrim(sourceIds, ",");
      
      WsGetSubjectsResults wsGetSubjectsResults = getSubjects(clientVersion,
          wsSubjectLookups, searchString, includeSubjectDetail, subjectAttributeArray, actAsSubjectLookup, sourceIdArray, wsGroupLookup, wsMemberFilter, fieldName,
          includeGroupDetail,
          params);
    
      return wsGetSubjectsResults;
    }

    //  /**
    //   * view or edit attributes for groups.  pass in attribute names and values (and if delete), if they are null, then 
    //   * just view.  
    //   * 
    //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
    //   * @param wsGroupLookups
    //   *            groups to save
    //   * @param wsAttributeEdits are the attributes to change or delete
    //   * @param actAsSubjectLookup
    //   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
    //   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
    //   * are NONE (or blank), and READ_WRITE_NEW.
    //   * @param params optional: reserved for future use
    //   * @return the results
    //   */
    //  @SuppressWarnings("unchecked")
    //  public static WsViewOrEditAttributesResults viewOrEditAttributes(final GrouperWsVersion clientVersion,
    //      final WsGroupLookup[] wsGroupLookups, final WsAttributeEdit[] wsAttributeEdits,
    //      final WsSubjectLookup actAsSubjectLookup, final GrouperTransactionType txType,
    //      final WsParam[] params) {
    //  
    //    GrouperSession session = null;
    //    int groupsSize = wsGroupLookups == null ? 0 : wsGroupLookups.length;
    //  
    //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = new WsViewOrEditAttributesResults();
    //  
    //    //convert the options to a map for easy access, and validate them
    //    @SuppressWarnings("unused")
    //    Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
    //        params);
    //  
    //    // see if greater than the max (or default)
    //    int maxAttributeGroup = GrouperWsConfig.getPropertyInt(
    //        GrouperWsConfig.WS_GROUP_ATTRIBUTE_MAX, 1000000);
    //    if (groupsSize > maxAttributeGroup) {
    //      wsViewOrEditAttributesResults
    //          .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
    //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
    //          "Number of groups must be less than max: " + maxAttributeGroup + " (sent in "
    //              + groupsSize + ")");
    //      return wsViewOrEditAttributesResults;
    //    }
    //  
    //    // TODO make sure size of params and values the same
    //  
    //    //lets validate the attribute edits
    //    boolean readOnly = wsAttributeEdits == null || wsAttributeEdits.length == 0;
    //    if (!readOnly) {
    //      for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
    //        String errorMessage = wsAttributeEdit.validate();
    //        if (errorMessage != null) {
    //          wsViewOrEditAttributesResults
    //              .assignResultCode(WsViewOrEditAttributesResultsCode.INVALID_QUERY);
    //          wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
    //              errorMessage + ", " + wsAttributeEdit);
    //        }
    //      }
    //    }
    //  
    //    // assume success
    //    wsViewOrEditAttributesResults
    //        .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
    //    Subject actAsSubject = null;
    //    // TODO have common try/catch
    //    try {
    //      actAsSubject = GrouperServiceJ2ee.retrieveSubjectActAs(actAsSubjectLookup);
    //  
    //      if (actAsSubject == null) {
    //        // TODO make this a result code
    //        throw new RuntimeException("Cant find actAs user: " + actAsSubjectLookup);
    //      }
    //  
    //      // use this to be the user connected, or the user act-as
    //      try {
    //        session = GrouperSession.start(actAsSubject);
    //      } catch (SessionException se) {
    //        // TODO make this a result code
    //        throw new RuntimeException("Problem with session for subject: " + actAsSubject,
    //            se);
    //      }
    //  
    //      int resultIndex = 0;
    //  
    //      wsViewOrEditAttributesResults
    //          .setResults(new WsViewOrEditAttributesResult[groupsSize]);
    //      GROUP_LOOP: for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
    //        WsViewOrEditAttributesResult wsViewOrEditAttributesResult = new WsViewOrEditAttributesResult();
    //        wsViewOrEditAttributesResults.getResults()[resultIndex++] = wsViewOrEditAttributesResult;
    //        Group group = null;
    //  
    //        try {
    //          wsViewOrEditAttributesResult.setGroupName(wsGroupLookup.getGroupName());
    //          wsViewOrEditAttributesResult.setGroupUuid(wsGroupLookup.getUuid());
    //  
    //          //get the group
    //          wsGroupLookup.retrieveGroupIfNeeded(session);
    //          group = wsGroupLookup.retrieveGroup();
    //          if (group == null) {
    //            wsViewOrEditAttributesResult
    //                .assignResultCode(WsViewOrEditAttributesResultCode.GROUP_NOT_FOUND);
    //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                "Cant find group: '" + wsGroupLookup + "'.  ");
    //            continue;
    //          }
    //  
    //          group = wsGroupLookup.retrieveGroup();
    //  
    //          // these will probably match, but just in case
    //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupName())) {
    //            wsViewOrEditAttributesResult.setGroupName(group.getName());
    //          }
    //          if (StringUtils.isBlank(wsViewOrEditAttributesResult.getGroupUuid())) {
    //            wsViewOrEditAttributesResult.setGroupUuid(group.getUuid());
    //          }
    //  
    //          //lets read them
    //          Map<String, String> attributeMap = GrouperUtil.nonNull(group.getAttributes());
    //  
    //          //see if we are updating
    //          if (!readOnly) {
    //            for (WsAttributeEdit wsAttributeEdit : wsAttributeEdits) {
    //              String attributeName = wsAttributeEdit.getName();
    //              try {
    //                //lets see if delete
    //                if (wsAttributeEdit.deleteBoolean()) {
    //                  //if its not there, dont bother
    //                  if (attributeMap.containsKey(attributeName)) {
    //                    group.deleteAttribute(attributeName);
    //                    //update map
    //                    attributeMap.remove(attributeName);
    //                  }
    //                } else {
    //                  String attributeValue = wsAttributeEdit.getValue();
    //                  //make sure it is different
    //                  if (!StringUtils
    //                      .equals(attributeValue, attributeMap.get(attributeName))) {
    //                    //it is update
    //                    group.setAttribute(attributeName, wsAttributeEdit.getValue());
    //                    attributeMap.put(attributeName, attributeValue);
    //                  }
    //                }
    //              } catch (AttributeNotFoundException anfe) {
    //                wsViewOrEditAttributesResult
    //                    .assignResultCode(WsViewOrEditAttributesResultCode.ATTRIBUTE_NOT_FOUND);
    //                wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                    "Cant find attribute: " + attributeName);
    //                //go to next group
    //                continue GROUP_LOOP;
    //  
    //              }
    //            }
    //          }
    //          //now take the attributes and put them in the result
    //          if (attributeMap.size() > 0) {
    //            int attributeIndex = 0;
    //            WsAttribute[] attributes = new WsAttribute[attributeMap.size()];
    //            wsViewOrEditAttributesResult.setAttributes(attributes);
    //            //lookup each from map and return
    //            for (String key : attributeMap.keySet()) {
    //              WsAttribute wsAttribute = new WsAttribute();
    //              attributes[attributeIndex++] = wsAttribute;
    //              wsAttribute.setName(key);
    //              wsAttribute.setValue(attributeMap.get(key));
    //            }
    //          }
    //          wsViewOrEditAttributesResult.getResultMetadata().assignSuccess("T");
    //          wsViewOrEditAttributesResult.getResultMetadata().assignResultCode("SUCCESS");
    //          if (readOnly) {
    //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                "Group '" + group.getName() + "' was queried.");
    //          } else {
    //            wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //                "Group '" + group.getName() + "' had attributes edited.");
    //          }
    //        } catch (InsufficientPrivilegeException ipe) {
    //          wsViewOrEditAttributesResult
    //              .assignResultCode(WsViewOrEditAttributesResultCode.INSUFFICIENT_PRIVILEGES);
    //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //              "Error: insufficient privileges to view/edit attributes '"
    //                  + wsGroupLookup.getGroupName() + "'");
    //        } catch (Exception e) {
    //          // lump the rest in there, group_add_exception, etc
    //          wsViewOrEditAttributesResult
    //              .assignResultCode(WsViewOrEditAttributesResultCode.EXCEPTION);
    //          wsViewOrEditAttributesResult.getResultMetadata().setResultMessage(
    //              ExceptionUtils.getFullStackTrace(e));
    //          LOG.error(wsGroupLookup + ", " + e, e);
    //        }
    //      }
    //  
    //    } catch (RuntimeException re) {
    //      wsViewOrEditAttributesResults
    //          .assignResultCode(WsViewOrEditAttributesResultsCode.EXCEPTION);
    //      String theError = "Problem view/edit attributes for groups: wsGroupLookup: "
    //          + GrouperUtil.toStringForLog(wsGroupLookups) + ", attributeEdits: "
    //          + GrouperUtil.toStringForLog(wsAttributeEdits) + ", actAsSubject: "
    //          + actAsSubject + ".  \n" + "";
    //      wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(theError);
    //      // this is sent back to the caller anyway, so just log, and not send
    //      // back again
    //      LOG.error(theError + ", wsViewOrEditAttributesResults: "
    //          + GrouperUtil.toStringForLog(wsViewOrEditAttributesResults), re);
    //    } finally {
    //      if (session != null) {
    //        try {
    //          session.stop();
    //        } catch (Exception e) {
    //          LOG.error(e.getMessage(), e);
    //        }
    //      }
    //    }
    //  
    //    if (wsViewOrEditAttributesResults.getResults() != null) {
    //      // check all entries
    //      int successes = 0;
    //      int failures = 0;
    //      for (WsViewOrEditAttributesResult wsGroupSaveResult : wsViewOrEditAttributesResults
    //          .getResults()) {
    //        boolean success = "T".equalsIgnoreCase(wsGroupSaveResult == null ? null
    //            : wsGroupSaveResult.getResultMetadata().getSuccess());
    //        if (success) {
    //          successes++;
    //        } else {
    //          failures++;
    //        }
    //      }
    //      if (failures > 0) {
    //        wsViewOrEditAttributesResults.getResultMetadata().appendResultMessage(
    //            "There were " + successes + " successes and " + failures
    //                + " failures of viewing/editing group attribues.   ");
    //        wsViewOrEditAttributesResults
    //            .assignResultCode(WsViewOrEditAttributesResultsCode.PROBLEM_WITH_GROUPS);
    //      } else {
    //        wsViewOrEditAttributesResults
    //            .assignResultCode(WsViewOrEditAttributesResultsCode.SUCCESS);
    //      }
    //    }
    //    if (!"T".equalsIgnoreCase(wsViewOrEditAttributesResults.getResultMetadata()
    //        .getSuccess())) {
    //  
    //      LOG.error(wsViewOrEditAttributesResults.getResultMetadata().getResultMessage());
    //    }
    //    return wsViewOrEditAttributesResults;
    //  }
    //
    //  /**
    //   * view or edit attributes for group.  pass in attribute names and values (and if delete), if they are null, then 
    //   * just view.  
    //   * 
    //   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
    //   * @param groupName
    //   *            to delete the group (mutually exclusive with groupUuid)
    //   * @param groupUuid
    //   *            to delete the group (mutually exclusive with groupName)
    //   * @param attributeName0 name of first attribute (optional)
    //   * @param attributeValue0 value of first attribute (optional)
    //   * @param attributeDelete0 if first attribute should be deleted (T|F) (optional)
    //   * @param attributeName1 name of second attribute (optional)
    //   * @param attributeValue1 value of second attribute (optional)
    //   * @param attributeDelete1 if second attribute should be deleted (T|F) (optional)
    //   * @param attributeName2 name of third attribute (optional)
    //   * @param attributeValue2 value of third attribute (optional)
    //   * @param attributeDelete2 if third attribute should be deleted (T|F) (optional)
    //   * @param actAsSubjectId
    //   *            optional: is the subject id of subject to act as (if
    //   *            proxying). Only pass one of actAsSubjectId or
    //   *            actAsSubjectIdentifer
    //   * @param actAsSubjectSourceId is source of act as subject to narrow the result and prevent
    //   * duplicates
    //   * @param actAsSubjectIdentifier
    //   *            optional: is the subject identifier of subject to act as (if
    //   *            proxying). Only pass one of actAsSubjectId or
    //   *            actAsSubjectIdentifer
    //   * @param paramName0
    //   *            reserved for future use
    //   * @param paramValue0
    //   *            reserved for future use
    //   * @param paramName1
    //   *            reserved for future use
    //   * @param paramValue1
    //   *            reserved for future use
    //   * @return the result of one member add
    //   */
    //  public static WsViewOrEditAttributesResults viewOrEditAttributesLite(
    //      final GrouperWsVersion clientVersion, String groupName, String groupUuid,
    //      String attributeName0, String attributeValue0, String attributeDelete0,
    //      String attributeName1, String attributeValue1, String attributeDelete1,
    //      String attributeName2, String attributeValue2, String attributeDelete2,
    //      String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier,
    //      String paramName0, String paramValue0, String paramName1, String paramValue1) {
    //  
    //    // setup the group lookup
    //    WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, groupUuid);
    //    WsGroupLookup[] wsGroupLookups = new WsGroupLookup[] { wsGroupLookup };
    //  
    //    //setup attributes
    //    List<WsAttributeEdit> attributeEditList = new ArrayList<WsAttributeEdit>();
    //    if (!StringUtils.isBlank(attributeName0) || !StringUtils.isBlank(attributeValue0)
    //        || !StringUtils.isBlank(attributeDelete0)) {
    //      attributeEditList.add(new WsAttributeEdit(attributeName0, attributeValue0,
    //          attributeDelete0));
    //    }
    //    if (!StringUtils.isBlank(attributeName1) || !StringUtils.isBlank(attributeValue1)
    //        || !StringUtils.isBlank(attributeDelete1)) {
    //      attributeEditList.add(new WsAttributeEdit(attributeName1, attributeValue1,
    //          attributeDelete1));
    //    }
    //    if (!StringUtils.isBlank(attributeName2) || !StringUtils.isBlank(attributeValue2)
    //        || !StringUtils.isBlank(attributeDelete2)) {
    //      attributeEditList.add(new WsAttributeEdit(attributeName2, attributeValue2,
    //          attributeDelete2));
    //    }
    //    //convert to array
    //    WsAttributeEdit[] wsAttributeEdits = GrouperUtil.toArray(attributeEditList,
    //        WsAttributeEdit.class);
    //    WsSubjectLookup actAsSubjectLookup = new WsSubjectLookup(actAsSubjectId,
    //        actAsSubjectSourceId, actAsSubjectIdentifier);
    //  
    //    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramValue1, paramValue1);
    //  
    //    WsViewOrEditAttributesResults wsViewOrEditAttributesResults = viewOrEditAttributes(
    //        clientVersion, wsGroupLookups, wsAttributeEdits, actAsSubjectLookup, null,
    //        params);
    //  
    //    return wsViewOrEditAttributesResults;
    //  }
    //
      
  /**
   * <pre>
   * assign a privilege for a user/group/type/name combo
   * e.g. POST /grouperPrivileges
   * </pre>
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsSubjectLookups are the subjects to assign the privileges to, looked up by subjectId or identifier
   * @param wsGroupLookup if this is a group privilege, this is the group
   * @param wsStemLookup if this is a stem privilege, this is the stem
   * @param replaceAllExisting
   *            optional: T or F (default), if the existing privilege assignments for this object should be
   *            replaced
   * @param actAsSubjectLookup optional: is the subject to act as (if proxying).
   * @param privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeNames (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param allowed is T to allow this privilege, F to deny this privilege
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent (comma separated)
   * @param includeGroupDetail T or F as for if group detail should be included
   * @param txType is the GrouperTransactionType for the request.  If blank, defaults to
   * NONE (will finish as much as possible).  Generally the only values for this param that make sense
   * are NONE (or blank), and READ_WRITE_NEW.
   * @param params
   *            optional: reserved for future use
   * @return the result of one member query
   */
  public static WsAssignGrouperPrivilegesResults assignGrouperPrivileges(
      final GrouperWsVersion clientVersion, 
      final WsSubjectLookup[] wsSubjectLookups,
      final WsGroupLookup wsGroupLookup,
      final WsStemLookup wsStemLookup,
      final PrivilegeType privilegeType, final Privilege[] privilegeNames,
      final boolean allowed,
      final boolean replaceAllExisting, GrouperTransactionType txType,
      final WsSubjectLookup actAsSubjectLookup,
      final boolean includeSubjectDetail, final String[] subjectAttributeNames, 
      final boolean includeGroupDetail,  final WsParam[] params) {

    GrouperWsVersion.assignCurrentClientVersion(clientVersion);
    
    final WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = 
      new WsAssignGrouperPrivilegesResults();

    GrouperSession session = null;
    String theSummary = null;
    
    try {
  
      theSummary = "clientVersion: " + clientVersion + ", wsSubjects: " + GrouperUtil.toStringForLog(wsSubjectLookups, 100)
          + ", group: " +  wsGroupLookup + ", stem: " + wsStemLookup 
          + ", privilege: " + privilegeType.name() + "-" + GrouperUtil.toStringForLog(privilegeNames)
          + ", allowed? " + allowed + ", actAsSubject: "
          + actAsSubjectLookup + ", replaceAllExisting: " + replaceAllExisting
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100);
        
      final String[] subjectAttributeArray = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);

      wsAssignGrouperPrivilegesResults.setSubjectAttributeNames(subjectAttributeArray);
        
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);

      if (wsGroupLookup != null && wsGroupLookup.hasData() && wsStemLookup != null && wsStemLookup.hasData()) {
        throw new WsInvalidQueryException("Cant pass both group and stem.  Pass one or the other");
      }
      if ((wsGroupLookup == null || !wsGroupLookup.hasData()) && (wsStemLookup == null || !wsStemLookup.hasData())) {
        throw new WsInvalidQueryException("Cant pass neither group nor stem.  Pass one or the other");
      }
      if (GrouperUtil.length(privilegeNames) == 0) {
        throw new WsInvalidQueryException("Need to pass in a privilege name");
      }
      
      
      if (txType == null) {
        txType = GrouperTransactionType.NONE;
      }
      
      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
    
      final GrouperSession SESSION = session;
      final GrouperTransactionType TX_TYPE = txType;
      
      final String THE_SUMMARY = theSummary;
      
      final List<WsAssignGrouperPrivilegesResult> wsAssignGrouperPrivilegesResultList = new ArrayList<WsAssignGrouperPrivilegesResult>();
      
      //start a transaction (or not if none)
      GrouperTransaction.callbackGrouperTransaction(txType,
          new GrouperTransactionHandler() {

            public Object callback(GrouperTransaction grouperTransaction)
                throws GrouperDAOException {

              Group group = null;
              
              //see if group and retrieve
              if (wsGroupLookup != null && wsGroupLookup.hasData()) {
                
                if (!privilegeType.equals(PrivilegeType.ACCESS)) {
                  throw new WsInvalidQueryException("If you are querying a group, you need to pass in an " +
                      "access privilege type: '" + privilegeType + "'");
                }
      
                group = wsGroupLookup.retrieveGroupIfNeeded(SESSION, "wsGroupLookup");
                
                wsAssignGrouperPrivilegesResults.setWsGroup(new WsGroup(group, wsGroupLookup, includeGroupDetail));
              }
              
              Stem stem = null;
              
              //see if stem and retrieve
              if (wsStemLookup != null && wsStemLookup.hasData()) {
                  
                wsStemLookup.retrieveStemIfNeeded(SESSION, true);
                stem = wsStemLookup.retrieveStem();
                if (stem != null) {
                  wsAssignGrouperPrivilegesResults.setWsStem(new WsStem(stem));
                } else {
                  wsAssignGrouperPrivilegesResults.setWsStem(new WsStem(wsStemLookup));
                }
              }
              
              //loop through all the privileges
              for (Privilege privilege : privilegeNames) {
                
                if (privilege == null) {
                  throw new WsInvalidQueryException("privilege cannot be null");
                }
                
                //get existing members if replacing
                Map<MultiKey, Subject> existingSubjectMap = null;
                if (replaceAllExisting) {
                  existingSubjectMap = new HashMap<MultiKey, Subject>();
                  Set<Subject> subjects = null;
                  try {
                    
                    if (group != null) {
                      
                      subjects = GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(group, privilege);
                    } else if (stem != null) {
                      subjects = GrouperSession.staticGrouperSession().getNamingResolver().getSubjectsWithPrivilege(stem, privilege);
                    } 
                    //add to map, note, might not be revokable
                    for (Subject subject : GrouperUtil.nonNull(subjects)) {
                      existingSubjectMap.put(SubjectHelper.convertToMultiKey(subject), subject);
                    }

                  } catch (SchemaException se) {
                    throw new WsInvalidQueryException(
                        "Problem with getting existing subjects", se);
                  }
                }

                Set<MultiKey> newSubjects = replaceAllExisting ? new HashSet<MultiKey>() : null;

                for (WsSubjectLookup subjectLookup : wsSubjectLookups) {
                
                  Subject subject = subjectLookup.retrieveSubject();
                  
                  WsAssignGrouperPrivilegesResult wsAssignGrouperPrivilegesResult = new WsAssignGrouperPrivilegesResult();
                  wsAssignGrouperPrivilegesResultList.add(wsAssignGrouperPrivilegesResult);
                  wsAssignGrouperPrivilegesResult.processSubject(subjectLookup, subjectAttributeArray);
            
                  //need to check to see status
            
                  if (subject != null) {
            
                    // keep track
                    if (replaceAllExisting) {
                      newSubjects.add(SubjectHelper.convertToMultiKey(subject));
                    }

                    boolean privilegeDidntAlreadyExist = false;
                    boolean privilegeStillExists = false;
                    
                    //handle group privileges
                    if (group != null) {
                                            
                      if (allowed) {
                        privilegeDidntAlreadyExist = group.grantPriv(subject, privilege, false);
                      } else {
                        privilegeDidntAlreadyExist = group.revokePriv(subject, privilege, false);
                        Set<AccessPrivilege> privileges = group.getPrivs(subject);
                        
                        for (AccessPrivilege accessPrivilege : GrouperUtil.nonNull(privileges)) {
                          if (StringUtils.equals(accessPrivilege.getName(), privilege.getName())) {
                            privilegeStillExists = true;
                          }
                        }
                      }
                      
                    } else if (stem != null) {
            
                      if (allowed) {
                        privilegeDidntAlreadyExist = stem.grantPriv(subject, privilege, false);
                      } else {
                        privilegeDidntAlreadyExist = stem.revokePriv(subject, privilege, false);
                        Set<NamingPrivilege> privileges = stem.getPrivs(subject);
                        
                        for (NamingPrivilege namingPrivilege : GrouperUtil.nonNull(privileges)) {
                          if (StringUtils.equals(namingPrivilege.getName(), privilege.getName())) {
                            privilegeStillExists = true;
                          }
                        }
                      }
                      
                    }
                    
                    String thePrivilegeName = privilege.getName();
                    wsAssignGrouperPrivilegesResult.setPrivilegeName(thePrivilegeName);
                    wsAssignGrouperPrivilegesResult.setPrivilegeType(privilegeType.getPrivilegeName());
                    
                    wsAssignGrouperPrivilegesResult.setWsSubject(new WsSubject(subject, subjectAttributeArray, subjectLookup));
                      
                    //assign one of 6 success codes
                    //setup the resultcode
                    if (allowed) {
                      if (!privilegeDidntAlreadyExist) {
                        wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_ALLOWED_ALREADY_EXISTED);
                      } else {
                        wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_ALLOWED);
                      }
                    } else {
                      if (!privilegeDidntAlreadyExist) {
                        if (privilegeStillExists) {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED_DIDNT_EXIST_BUT_EXISTS_EFFECTIVE);
                        } else {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED_DIDNT_EXIST);
                        }
                      } else {
                        if (privilegeStillExists) {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED_EXISTS_EFFECTIVE);
                        } else {
                          wsAssignGrouperPrivilegesResult.assignResultCode(WsAssignGrouperPrivilegesResultCode.SUCCESS_NOT_ALLOWED);
                        }
                      }
                    }
                  }
                }
                
                //remove ones not added
                if (replaceAllExisting) {
                  for (MultiKey subjectKey : existingSubjectMap.keySet()) {
                    if (newSubjects.contains(subjectKey)) {
                      continue;
                    }
                    Subject subject = existingSubjectMap.get(subjectKey);
                    try {
                      //note, no exception if already revoked since might not be immediate
                      if (group != null) {
                        group.revokePriv(subject, privilege, false);
                      } else if (stem != null) {
                        stem.revokePriv(subject, privilege, false);
                      }
                    } catch (Exception e) {
                      String theError = "Error removing subject: " + subject
                          + " owner: " + (group == null ? stem : group) + ", privilege: "
                          + privilege + ", " + e + ".  ";
                      wsAssignGrouperPrivilegesResults.assignResultCodeException(
                          WsAssignGrouperPrivilegesResultsCode.PROBLEM_DELETING_MEMBERS, theError, e);
                    }

                  }
                  
                }
                
                //see if any inner failures cause the whole tx to fail, and/or change the outer status
                if (!wsAssignGrouperPrivilegesResults.tallyResults(TX_TYPE, THE_SUMMARY)) {
                  grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
                }

              }
              return null;
            }
      });
            
      //assign results
      wsAssignGrouperPrivilegesResults.setResults(GrouperUtil.toArray(wsAssignGrouperPrivilegesResultList, WsAssignGrouperPrivilegesResult.class));
      
    } catch (InsufficientPrivilegeException ipe) {
      wsAssignGrouperPrivilegesResults
          .assignResultCode(WsAssignGrouperPrivilegesResults.WsAssignGrouperPrivilegesResultsCode.INSUFFICIENT_PRIVILEGES);
    } catch (Exception e) {
      wsAssignGrouperPrivilegesResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperWsVersion.assignCurrentClientVersion(null, true);
      GrouperSession.stopQuietly(session);
    }
  
    return wsAssignGrouperPrivilegesResults;
  }
  
  /**
   * get attributeAssignments from groups etc based on inputs
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, NOT: group_asgn, NOT: mem_asgn, 
   * NOT: stem_asgn, NOT: any_mem_asgn, NOT: imm_mem_asgn, NOT: attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeAssignLookups if you know the assign ids you want, put them here
   * @param wsOwnerGroupLookups are groups to look in
   * @param wsOwnerSubjectLookups are subjects to look in
   * @param wsAttributeDefLookups find assignments in these attribute defs (optional)
   * @param wsAttributeDefNameLookups find assignments in these attribute def names (optional)
   * @param wsOwnerStemLookups are stems to look in
   * @param wsOwnerMembershipLookups to query attributes on immediate memberships
   * @param wsOwnerMembershipAnyLookups to query attributes in "any" memberships which are on immediate or effective memberships
   * @param wsOwnerAttributeDefLookups to query attributes assigned on attribute defs
   * @param actions to query, or none to query all actions
   * @param includeAssignmentsOnAssignments if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetAttributeAssignmentsResults getAttributeAssignments(
      final GrouperWsVersion clientVersion, AttributeAssignType attributeAssignType,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsAttributeDefLookup[] wsAttributeDefLookups, WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups, WsSubjectLookup[] wsOwnerSubjectLookups, 
      WsMembershipLookup[] wsOwnerMembershipLookups, WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups, 
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups, 
      String[] actions, 
      boolean includeAssignmentsOnAssignments, WsSubjectLookup actAsSubjectLookup, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params, 
      String enabled) {  

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new WsGetAttributeAssignmentsResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion+ ", attributeAssignType: " + attributeAssignType 
          + ", wsAttributeDefLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefLookups, 200) 
          + ", wsAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsAttributeAssignLookups, 200)
          + ", wsAttributeDefNameLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameLookups, 200) + ", wsOwnerStemLookups: "
          + GrouperUtil.toStringForLog(wsOwnerStemLookups, 200) + ", wsOwnerGroupLookups: "
          + GrouperUtil.toStringForLog(wsOwnerGroupLookups, 200) + ", wsOwnerMembershipLookups: "
          + GrouperUtil.toStringForLog(wsOwnerMembershipLookups, 200) 
          + ", wsOwnerMembershipAnyLookups: " + GrouperUtil.toStringForLog(wsOwnerMembershipAnyLookups, 200)
          + ", wsOwnerAttributeDefLookups: " + GrouperUtil.toStringForLog(wsOwnerAttributeDefLookups, 200)
          + ", actions: " + GrouperUtil.toStringForLog(actions, 200)
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsOwnerSubjectLookups: "
          + GrouperUtil.toStringForLog(wsOwnerSubjectLookups, 200) 
          + ", enabled: " + enabled;
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsGetAttributeAssignmentsResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);


      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(
          params);
      
      //this is for error checking
      
      int[] lookupCount = new int[]{0};

      StringBuilder errorMessage = new StringBuilder();

      if (attributeAssignType == null) {
        throw new WsInvalidQueryException("You need to pass in an attributeAssignType");
      }
      
      //get the attributeAssignids to retrieve
      Set<String> attributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsAttributeAssignLookups, errorMessage);
      
      //get the attributedefs to retrieve
      Set<String> attributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsAttributeDefLookups, errorMessage);
      
      //get the attributeDefNames to retrieve
      Set<String> attributeDefNameIds = WsAttributeDefNameLookup.convertToAttributeDefNameIds(session, wsAttributeDefNameLookups, errorMessage);
      
      //get all the owner groups
      Set<String> ownerGroupIds = WsGroupLookup.convertToGroupIds(session, wsOwnerGroupLookups, errorMessage, lookupCount);
      
      //get all the owner stems
      Set<String> ownerStemIds = WsStemLookup.convertToStemIds(session, wsOwnerStemLookups, errorMessage, lookupCount);
      
      //get all the owner member ids
      Set<String> ownerMemberIds = WsSubjectLookup.convertToMemberIds(session, wsOwnerSubjectLookups, errorMessage, lookupCount);
      
      //get all the owner membership ids
      Set<String> ownerMembershipIds = WsMembershipLookup.convertToMembershipIds(session, wsOwnerMembershipLookups, errorMessage, lookupCount);
      
      //get all the owner membership any ids
      Set<MultiKey> ownerGroupMemberIds = WsMembershipAnyLookup.convertToGroupMemberIds(session, wsOwnerMembershipAnyLookups, errorMessage, lookupCount);
      
      //get all the owner attributeDef ids
      Set<String> ownerAttributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsOwnerAttributeDefLookups, errorMessage, lookupCount);
      
      if (lookupCount[0] > 1) {
        throw new WsInvalidQueryException("Why is there more than one type of lookup?  ");
      }
      
      Set<AttributeAssign> results = null;
      
      Boolean enabledBoolean = true;
      if (!StringUtils.isBlank(enabled)) {
        if (StringUtils.equalsIgnoreCase("A", enabled)) {
          enabledBoolean = null;
        } else {
          enabledBoolean = GrouperUtil.booleanValue(enabled);
        }
      }
      
      Collection<String> actionsCollection = GrouperUtil.toSet(actions);
      
      if (actionsCollection == null || actionsCollection.size() == 0 
          || (actionsCollection.size() == 1 && StringUtils.isBlank(actionsCollection.iterator().next()))) {
        actionsCollection = null;
      }
      
      switch(attributeAssignType) {
        case group:
          
          //if there is a lookup and its not about groups, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerGroupLookups) == 0) {
            throw new WsInvalidQueryException("Group calls can only have group owner lookups.  ");
          }
          
          results = GrouperDAOFactory.getFactory().getAttributeAssign().findGroupAttributeAssignments(
              attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerGroupIds, actionsCollection, enabledBoolean, includeAssignmentsOnAssignments);
          
          break;  
        case stem:
          
          //if there is a lookup and its not about stems, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerStemLookups) == 0) {
            throw new WsInvalidQueryException("Stem calls can only have stem owner lookups.  ");
          }
          
          results = GrouperDAOFactory.getFactory().getAttributeAssign().findStemAttributeAssignments(
              attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerStemIds, actionsCollection, enabledBoolean, includeAssignmentsOnAssignments);
          
          break;  
        case member:
          
          //if there is a lookup and its not about subjects, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerSubjectLookups) == 0) {
            throw new WsInvalidQueryException("Subject calls can only have subject owner lookups.  ");
          }
          
          results = GrouperDAOFactory.getFactory().getAttributeAssign().findMemberAttributeAssignments(
              attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerMemberIds, actionsCollection, enabledBoolean, includeAssignmentsOnAssignments);
          
          break;  
        case imm_mem:
          
          //if there is a lookup and its not about memberships, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerMembershipLookups) == 0) {
            throw new WsInvalidQueryException("Membership calls can only have membership owner lookups.  ");
          }
          
          results = GrouperDAOFactory.getFactory().getAttributeAssign().findMembershipAttributeAssignments(
              attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerMembershipIds, actionsCollection, enabledBoolean, includeAssignmentsOnAssignments);
          
          break;  
        case any_mem:
          
          //if there is a lookup and its not about memberships, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerMembershipAnyLookups) == 0) {
            throw new WsInvalidQueryException("MembershipAny calls can only have membershipAny owner lookups.  ");
          }
          
          results = GrouperDAOFactory.getFactory().getAttributeAssign().findAnyMembershipAttributeAssignments(
              attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerGroupMemberIds, actionsCollection, enabledBoolean, includeAssignmentsOnAssignments);
          
          break;  
        case attr_def:
          
          //if there is a lookup and its not about attr def, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerAttributeDefLookups) == 0) {
            throw new WsInvalidQueryException("attributeDef calls can only have attributeDef owner lookups.  ");
          }
          
          results = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeDefAttributeAssignments(
              attributeAssignIds, attributeDefIds, attributeDefNameIds, ownerAttributeDefIds, actionsCollection, enabledBoolean, includeAssignmentsOnAssignments);
          
          break;  
        default: 
          throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
      }
      
      wsGetAttributeAssignmentsResults.assignResult(results, subjectAttributeNames);
      
      wsGetAttributeAssignmentsResults.fillInAttributeDefNames(attributeDefNameIds);
      wsGetAttributeAssignmentsResults.fillInAttributeDefs(attributeDefIds);
      
      Set<String> allGroupIds = new HashSet<String>(GrouperUtil.nonNull(ownerGroupIds));
      Set<String> extraMemberIds = new HashSet<String>();
      for (MultiKey multiKey : GrouperUtil.nonNull(ownerGroupMemberIds)) {
        allGroupIds.add((String)multiKey.getKey(0));
        extraMemberIds.add((String)multiKey.getKey(1));
      }
      
      
      wsGetAttributeAssignmentsResults.fillInGroups(ownerGroupIds, includeGroupDetail);
      wsGetAttributeAssignmentsResults.fillInStems(ownerStemIds);
      wsGetAttributeAssignmentsResults.fillInSubjects(wsOwnerSubjectLookups, extraMemberIds, 
          includeSubjectDetail, subjectAttributeNamesToRetrieve);
      wsGetAttributeAssignmentsResults.fillInMemberships(ownerMembershipIds);
      
      //sort after all the data is there
      wsGetAttributeAssignmentsResults.sortResults();
      
      if (errorMessage.length() > 0) {
        wsGetAttributeAssignmentsResults.assignResultCode(WsGetAttributeAssignmentsResultsCode.INVALID_QUERY);
        wsGetAttributeAssignmentsResults.getResultMetadata().appendResultMessage(errorMessage.toString());
      } else {
        wsGetAttributeAssignmentsResults.assignResultCode(WsGetAttributeAssignmentsResultsCode.SUCCESS);
      }
      
      wsGetAttributeAssignmentsResults.getResultMetadata().appendResultMessage(
          ", Found " + GrouperUtil.length(wsGetAttributeAssignmentsResults.getWsAttributeAssigns())
          + " results.  ");

        
    } catch (Exception e) {
      wsGetAttributeAssignmentsResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsGetAttributeAssignmentsResults; 
  }

    
  /**
   * get attributeAssignments from group and or subject based on inputs
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, NOT: group_asgn, NOT: mem_asgn, 
   * NOT: stem_asgn, NOT: any_mem_asgn, NOT: imm_mem_asgn, NOT: attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param attributeAssignId if you know the assign id you want, put it here
   * @param wsAttributeDefName find assignments in this attribute def (optional)
   * @param wsAttributeDefId find assignments in this attribute def (optional)
   * @param wsAttributeDefNameName find assignments in this attribute def name (optional)
   * @param wsAttributeDefNameId find assignments in this attribute def name (optional)
   * @param wsOwnerGroupName is group name to look in
   * @param wsOwnerGroupId is group id to look in
   * @param wsOwnerStemName is stem to look in
   * @param wsOwnerStemId is stem to look in
   * @param wsOwnerSubjectId is subject to look in
   * @param wsOwnerSubjectSourceId is subject to look in
   * @param wsOwnerSubjectIdentifier is subject to look in
   * @param wsOwnerMembershipId to query attributes on immediate membership
   * @param wsOwnerMembershipAnyGroupName to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupId  to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectId to query attributes in "any" membership which is on immediate or effective membership 
   * @param wsOwnerMembershipAnySubjectSourceId to query attributes in "any" membership which is on immediate or effective membership 
   * @param wsOwnerMembershipAnySubjectIdentifier to query attributes in "any" membership which is on immediate or effective membership 
   * @param wsOwnerAttributeDefName to query attributes assigned on attribute def
   * @param wsOwnerAttributeDefId to query attributes assigned on attribute def
   * @param action to query, or none to query all actions
   * @param includeAssignmentsOnAssignments if this is not querying assignments on assignments directly, but the assignments
   * and assignments on those assignments should be returned, enter true.  default to false.
   * @param actAsSubjectId act as this subject
   * @param actAsSubjectSourceId act as this subject
   * @param actAsSubjectIdentifier act as this subject
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param subjectAttributeNames are the additional subject attributes (data) to return (comma separated)
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @param enabled is A for all, T or null for enabled only, F for disabled 
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsGetAttributeAssignmentsResults getAttributeAssignmentsLite(
      final GrouperWsVersion clientVersion, AttributeAssignType attributeAssignType,
      String attributeAssignId,
      String wsAttributeDefName, String wsAttributeDefId, String wsAttributeDefNameName, String wsAttributeDefNameId,
      String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, 
      String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier,
      String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId,
      String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier, 
      String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, 
      String action, 
      boolean includeAssignmentsOnAssignments, String actAsSubjectId, String actAsSubjectSourceId,
      String actAsSubjectIdentifier, boolean includeSubjectDetail,
      String subjectAttributeNames, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1, 
      String enabled) {  

    WsAttributeAssignLookup[] attributeAssignLookups = null;
    
    if (!StringUtils.isBlank(attributeAssignId)) {
      attributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(attributeAssignId)};
    }
    
    WsAttributeDefLookup[] wsAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefName) || !StringUtils.isBlank(wsAttributeDefId)) {
      wsAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsAttributeDefName, wsAttributeDefId)};
    }
    
    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefNameName) || !StringUtils.isBlank(wsAttributeDefNameId)) {
      wsAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId )};
    }
    
    WsGroupLookup[] wsOwnerGroupLookups = null;
    if (!StringUtils.isBlank(wsOwnerGroupName) || !StringUtils.isBlank(wsOwnerGroupId)) {
      wsOwnerGroupLookups = new WsGroupLookup[]{new WsGroupLookup(wsOwnerGroupName, wsOwnerGroupId)};
    }
    
    WsStemLookup[] wsOwnerStemLookups = null;
    if (!StringUtils.isBlank(wsOwnerStemName) || !StringUtils.isBlank(wsOwnerStemId)) {
      wsOwnerStemLookups = new WsStemLookup[]{new WsStemLookup(wsOwnerStemName, wsOwnerStemId)};
    }
    
    WsSubjectLookup[] wsOwnerSubjectLookups = null;
    if (!StringUtils.isBlank(wsOwnerSubjectId) || !StringUtils.isBlank(wsOwnerSubjectSourceId) || !StringUtils.isBlank(wsOwnerSubjectIdentifier)) {
      wsOwnerSubjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(wsOwnerSubjectId, wsOwnerSubjectSourceId, wsOwnerSubjectIdentifier)};
    }
    
    WsMembershipLookup[] wsOwnerMembershipLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipId)) {
      wsOwnerMembershipLookups = new WsMembershipLookup[]{new WsMembershipLookup(wsOwnerMembershipId)};
    }
    
    WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipAnyGroupName) || !StringUtils.isBlank(wsOwnerMembershipAnyGroupId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectId) || !StringUtils.isBlank(wsOwnerMembershipAnySubjectSourceId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectIdentifier)) {
      wsOwnerMembershipAnyLookups = new WsMembershipAnyLookup[]{
          new WsMembershipAnyLookup(new WsGroupLookup(wsOwnerMembershipAnyGroupName,wsOwnerMembershipAnyGroupId ),
              new WsSubjectLookup(wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, wsOwnerMembershipAnySubjectIdentifier))};
    }
    
    WsAttributeDefLookup[] wsOwnerAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsOwnerAttributeDefName) || !StringUtils.isBlank(wsOwnerAttributeDefId)) {
      wsOwnerAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsOwnerAttributeDefName, wsOwnerAttributeDefId)}; 
    }
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
    
    String[] actions = null;
    if (!StringUtils.isBlank(action)) {
      actions = new String[]{action};
    }
    
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = getAttributeAssignments(clientVersion, attributeAssignType, 
        attributeAssignLookups, wsAttributeDefLookups, wsAttributeDefNameLookups, wsOwnerGroupLookups, wsOwnerStemLookups, 
        wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups, wsOwnerAttributeDefLookups, actions, 
        includeAssignmentsOnAssignments, actAsSubjectLookup, includeSubjectDetail, subjectAttributeArray, includeGroupDetail, 
        params, enabled );
    
    return wsGetAttributeAssignmentsResults; 
  
  }

  /**
   * assign attributes and values to owner objects (groups, stems, etc)
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeAssignLookups if you know the assign ids you want, put them here
   * @param wsOwnerGroupLookups are groups to look in
   * @param wsOwnerSubjectLookups are subjects to look in
   * @param wsAttributeDefNameLookups attribute def names to assign to the owners
   * @param attributeAssignOperation operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param values are the values to assign, replace, remove, etc.  If removing, and id is specified, will
   * only remove values with that id.
   * @param assignmentNotes notes on the assignment (optional)
   * @param assignmentEnabledTime enabled time, or null for enabled now
   * @param assignmentDisabledTime disabled time, or null for not disabled
   * @param delegatable really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param attributeAssignValueOperation operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param wsOwnerStemLookups are stems to look in
   * @param wsOwnerMembershipLookups to query attributes on immediate memberships
   * @param wsOwnerMembershipAnyLookups to query attributes in "any" memberships which are on immediate or effective memberships
   * @param wsOwnerAttributeDefLookups to query attributes assigned on attribute defs
   * @param wsOwnerAttributeAssignLookups for assignment on assignment
   * @param actions to assign, or "assign" is the default if blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectLookup
   * @param wsAttributeAssignLookups lookups to remove etc
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param params optional: reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignAttributesResults assignAttributes(
      final GrouperWsVersion clientVersion, AttributeAssignType attributeAssignType,
      WsAttributeDefNameLookup[] wsAttributeDefNameLookups,
      AttributeAssignOperation attributeAssignOperation,
      WsAttributeAssignValue[] values,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation,
      WsAttributeAssignLookup[] wsAttributeAssignLookups,
      WsGroupLookup[] wsOwnerGroupLookups, WsStemLookup[] wsOwnerStemLookups, WsSubjectLookup[] wsOwnerSubjectLookups, 
      WsMembershipLookup[] wsOwnerMembershipLookups, WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups, 
      WsAttributeDefLookup[] wsOwnerAttributeDefLookups, WsAttributeAssignLookup[] wsOwnerAttributeAssignLookups,
      String[] actions, WsSubjectLookup actAsSubjectLookup, boolean includeSubjectDetail,
      String[] subjectAttributeNames, boolean includeGroupDetail, final WsParam[] params) {  

    WsAssignAttributesResults wsAssignAttributesResults = new WsAssignAttributesResults();
  
    GrouperSession session = null;
    String theSummary = null;
    try {
  
      theSummary = "clientVersion: " + clientVersion+ ", attributeAssignType: " + attributeAssignType 
          + ", attributeAssignOperation: " + attributeAssignOperation
          + ", attributeAssignValues: " + GrouperUtil.toStringForLog(values, 200)
          + ", attributeAssignValueOperation: " + attributeAssignValueOperation
          + ", wsOwnerAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsOwnerAttributeAssignLookups, 200)
          + ", wsAttributeAssignLookups: " + GrouperUtil.toStringForLog(wsAttributeAssignLookups, 200)
          + ", wsAttributeDefNameLookups: "
          + GrouperUtil.toStringForLog(wsAttributeDefNameLookups, 200) + ", wsOwnerStemLookups: "
          + GrouperUtil.toStringForLog(wsOwnerStemLookups, 200) + ", wsOwnerGroupLookups: "
          + GrouperUtil.toStringForLog(wsOwnerGroupLookups, 200) + ", wsOwnerMembershipLookups: "
          + GrouperUtil.toStringForLog(wsOwnerMembershipLookups, 200) 
          + ", wsOwnerMembershipAnyLookups: " + GrouperUtil.toStringForLog(wsOwnerMembershipAnyLookups, 200)
          + ", wsOwnerAttributeDefLookups: " + GrouperUtil.toStringForLog(wsOwnerAttributeDefLookups, 200)
          + ", actions: " + GrouperUtil.toStringForLog(actions, 200)
          + ", includeSubjectDetail: " + includeSubjectDetail + ", actAsSubject: "
          + actAsSubjectLookup 
          + ", subjectAttributeNames: "
          + GrouperUtil.toStringForLog(subjectAttributeNames) + "\n, paramNames: "
          + "\n, params: " + GrouperUtil.toStringForLog(params, 100) + "\n, wsOwnerSubjectLookups: "
          + GrouperUtil.toStringForLog(wsOwnerSubjectLookups, 200);
  
      //start session based on logged in user or the actAs passed in
      session = GrouperServiceUtils.retrieveGrouperSession(actAsSubjectLookup);
  
      final String[] subjectAttributeNamesToRetrieve = GrouperServiceUtils
        .calculateSubjectAttributes(subjectAttributeNames, includeSubjectDetail);
  
      wsAssignAttributesResults.setSubjectAttributeNames(subjectAttributeNamesToRetrieve);

      //convert the options to a map for easy access, and validate them
      @SuppressWarnings("unused")
      Map<String, String> paramMap = GrouperServiceUtils.convertParamsToMap(params);
      
      //this is for error checking
      
      int[] lookupCount = new int[]{0};

      StringBuilder errorMessage = new StringBuilder();

      if (attributeAssignType == null) {
        throw new WsInvalidQueryException("You need to pass in an attributeAssignType.  ");
      }
      
      if (attributeAssignOperation == null) {
        throw new WsInvalidQueryException("You need to pass in an attributeAssignOperation.  ");
      }
      
      if (!GrouperServiceUtils.nullArray(wsAttributeAssignLookups)) {
        if (!GrouperServiceUtils.nullArray(wsAttributeDefNameLookups)) {
          throw new WsInvalidQueryException("If you are passing in assign lookup ids to query, you cant specify attribute def names.  ");
        }
        
        if (attributeAssignOperation != AttributeAssignOperation.assign_attr 
            && attributeAssignOperation != AttributeAssignOperation.remove_attr) {
          throw new WsInvalidQueryException("If you are passing in assign lookup ids to query, " +
          		"attributeAssignOperation must be assign_attr or remove_attr.  ");
        }
        
      }
      
      //get the attributeAssignids to retrieve.  but shouldnt have owner as well as this...
      Set<String> attributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsAttributeAssignLookups, errorMessage, lookupCount);
      
      //get the owner attributeAssignids to retrieve
      Set<String> ownerAttributeAssignIds = WsAttributeAssignLookup.convertToAttributeAssignIds(session, wsOwnerAttributeAssignLookups, errorMessage, lookupCount);
      
      //get the attributeDefNames to retrieve
      Set<String> attributeDefNameIds = WsAttributeDefNameLookup.convertToAttributeDefNameIds(session, wsAttributeDefNameLookups, errorMessage);
      
      //get all the owner groups
      Set<String> ownerGroupIds = WsGroupLookup.convertToGroupIds(session, wsOwnerGroupLookups, errorMessage, lookupCount);
      
      //get all the owner stems
      Set<String> ownerStemIds = WsStemLookup.convertToStemIds(session, wsOwnerStemLookups, errorMessage, lookupCount);
      
      //get all the owner member ids
      Set<String> ownerMemberIds = WsSubjectLookup.convertToMemberIds(session, wsOwnerSubjectLookups, errorMessage, lookupCount);
      
      //get all the owner membership ids
      Set<String> ownerMembershipIds = WsMembershipLookup.convertToMembershipIds(session, wsOwnerMembershipLookups, errorMessage, lookupCount);
      
      //get all the owner membership any ids
      Set<MultiKey> ownerGroupMemberIds = WsMembershipAnyLookup.convertToGroupMemberIds(session, wsOwnerMembershipAnyLookups, errorMessage, lookupCount);
      
      //get all the owner attributeDef ids
      Set<String> ownerAttributeDefIds = WsAttributeDefLookup.convertToAttributeDefIds(session, wsOwnerAttributeDefLookups, errorMessage, lookupCount);
      
      List<WsAssignAttributeResult> wsAssignAttributeResultList = new ArrayList<WsAssignAttributeResult>();
      
      if (lookupCount[0] > 1) {
        throw new WsInvalidQueryException("Why is there more than one type of lookup?  ");
      }

      //cant delete and do anything with values
      if (attributeAssignOperation == AttributeAssignOperation.remove_attr) {
        if (!GrouperServiceUtils.nullArray(values)) {
          throw new WsInvalidQueryException("Cant pass in values when deleting attributes.  ");
        }
        if (!StringUtils.isBlank(assignmentNotes)) {
          throw new WsInvalidQueryException("Cant pass in assignmentNotes when deleting attributes.  ");
        }
        if (assignmentEnabledTime != null) {
          throw new WsInvalidQueryException("Cant pass in assignmentEnabledTime when deleting attributes.  ");
        }
        if (assignmentDisabledTime != null) {
          throw new WsInvalidQueryException("Cant pass in assignmentDisabledTime when deleting attributes.  ");
        }
        if (delegatable != null) {
          throw new WsInvalidQueryException("Cant pass in delegatable when deleting attributes.  ");
        }
        if (attributeAssignValueOperation != null) {
          throw new WsInvalidQueryException("Cant pass in attributeAssignValueOperation when deleting attributes.  ");
        }
          
      }

      if (GrouperUtil.length(attributeAssignIds) > 0) {
        for (WsAttributeAssignLookup wsAttributeAssignLookup : GrouperUtil.nonNull(wsAttributeAssignLookups, WsAttributeAssignLookup.class)) {
          AttributeAssign attributeAssign = wsAttributeAssignLookup.retrieveAttributeAssign();
          if (attributeAssign.getAttributeAssignType() != attributeAssignType) {
            throw new WsInvalidQueryException("attributeAssign " + attributeAssign.getId() 
                + " has attributeAssignType: " + attributeAssign.getAttributeAssignType() 
                + " but this operation was passed attributeAssignType: " + attributeAssignType + ".  ");
          }
        }
        
        //dont pass in an action
        if (!GrouperServiceUtils.nullArray(actions)) {
          throw new WsInvalidQueryException("Cant pass in actions when using attribute assign id lookup.  ");
        }
        
        //move to results
        for (WsAttributeAssignLookup wsAttributeAssignLookup : GrouperUtil.nonNull(wsAttributeAssignLookups, WsAttributeAssignLookup.class)) {
          
          AttributeAssign attributeAssign = wsAttributeAssignLookup.retrieveAttributeAssign();
          
          //if its null the error is handled above
          if (attributeAssign != null) {
            WsAssignAttributeResult wsAssignAttributeResult = new WsAssignAttributeResult();
            wsAssignAttributeResult.setChanged("F");
            wsAssignAttributeResult.setValuesChanged("F");
            
            switch(attributeAssignOperation) {
              
              case assign_attr:
                
                WsAssignAttributeLogic.assignmentMetadataAndValues(wsAssignAttributeResult, 
                    attributeAssign, values, assignmentNotes, assignmentEnabledTime, 
                    assignmentDisabledTime, delegatable, attributeAssignValueOperation);
                
                //TODO edit and assign values
                break;
              case remove_attr:
                attributeAssign.delete();
                wsAssignAttributeResult.setChanged("T");
                break;
              default:
                throw new WsInvalidQueryException("Invalid attributeAssignOperation: " + attributeAssignOperation + ".  ");
              
            }
            WsAttributeAssign wsAttributeAssign = new WsAttributeAssign(attributeAssign);
            wsAssignAttributeResult.setWsAttributeAssigns(new WsAttributeAssign[]{wsAttributeAssign});
            wsAssignAttributeResultList.add(wsAssignAttributeResult);
            
          }
          
        }
        
      } else {
        
        //else not going by id
        
        //we are looping through actions, so dont have a null array
        if (GrouperServiceUtils.nullArray(actions)) {
          actions = new String[]{AttributeDef.ACTION_DEFAULT};
        }
      
        List<AttributeAssignable> attributeAssignableList = new ArrayList<AttributeAssignable>();
        
        switch(attributeAssignType) {
        case group:
          
          //if there is a lookup and its not about groups, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerGroupLookups) == 0) {
            throw new WsInvalidQueryException("Group calls can only have group owner lookups.  ");
          }
          
          Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByUuids(ownerGroupIds, true);
          attributeAssignableList.addAll(groups);
          
          break;  
        case stem:
          
          //if there is a lookup and its not about stems, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerStemLookups) == 0) {
            throw new WsInvalidQueryException("Stem calls can only have stem owner lookups.  ");
          }
          
          for (String stemId : GrouperUtil.nonNull(ownerStemIds)) {
            Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(stemId, true);
            attributeAssignableList.add(stem);
          }
                      
          break;  
        case member:
          
          //if there is a lookup and its not about subjects, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerSubjectLookups) == 0) {
            throw new WsInvalidQueryException("Subject calls can only have subject owner lookups.  ");
          }
          
          for (String memberId : GrouperUtil.nonNull(ownerMemberIds)) {
            Member member = GrouperDAOFactory.getFactory().getMember().findByUuid(memberId, true);
            attributeAssignableList.add(member);
          }
          
          
          break;  
        case imm_mem:
          
          //if there is a lookup and its not about memberships, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerMembershipLookups) == 0) {
            throw new WsInvalidQueryException("Membership calls can only have membership owner lookups.  ");
          }
          
          for (String membershipId : GrouperUtil.nonNull(ownerMembershipIds)) {
            Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(membershipId, true, false);
            attributeAssignableList.add(membership);
          }
          
          
          break;  
        case any_mem:
          
          //if there is a lookup and its not about memberships, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerMembershipAnyLookups) == 0) {
            throw new WsInvalidQueryException("MembershipAny calls can only have membershipAny owner lookups.  ");
          }
          
          for (MultiKey groupMemberId : GrouperUtil.nonNull(ownerGroupMemberIds)) {
            Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid((String)groupMemberId.getKey(0), true);
            Member member = GrouperDAOFactory.getFactory().getMember().findByUuid((String)groupMemberId.getKey(1), true);
            GroupMember groupMember = new GroupMember(group, member);
            attributeAssignableList.add(groupMember);
          }
          
          
          break;  
        case attr_def:
          
          //if there is a lookup and its not about attr def, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerAttributeDefLookups) == 0) {
            throw new WsInvalidQueryException("attributeDef calls can only have attributeDef owner lookups.  ");
          }

          for (String attributeDefId : GrouperUtil.nonNull(ownerAttributeDefIds)) {
            AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(attributeDefId, true);
            attributeAssignableList.add(attributeDef);
          }

          break;  
        case any_mem_asgn:
        case attr_def_asgn:
        case group_asgn:
        case imm_mem_asgn:
        case mem_asgn:
        case stem_asgn:
          
          //if there is a lookup and its not about attr assign, then there is a problem
          if (lookupCount[0] == 1 && GrouperUtil.length(wsOwnerAttributeAssignLookups) == 0) {
            throw new WsInvalidQueryException("attributeAssign calls can only have attributeAssign owner lookups.  ");
          }

          for (String attributeAssignId : GrouperUtil.nonNull(ownerAttributeAssignIds)) {
            AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, true);
            attributeAssignableList.add(attributeAssign);
          }
          
          break;
        default: 
          throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
        }
        
        //loop through the assignables
        for (AttributeAssignable attributeAssignable : attributeAssignableList) {
          
          for (String attributeDefNameId : attributeDefNameIds) {
            
            AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByUuidOrName(attributeDefNameId, null, true); 

            for (String action : actions) {
              
              try {
                WsAssignAttributeResult wsAssignAttributeResult = new WsAssignAttributeResult();
                wsAssignAttributeResult.setChanged("F");
                wsAssignAttributeResult.setValuesChanged("F");
                AttributeAssign[] attributeAssigns = null;
                AttributeAssignResult attributeAssignResult = null;
                switch (attributeAssignOperation) {
                  case add_attr:
                    attributeAssignResult = attributeAssignable.getAttributeDelegate().addAttribute(action, attributeDefName);
                    attributeAssigns = new AttributeAssign[]{attributeAssignResult.getAttributeAssign()};
                    WsAssignAttributeLogic.assignmentMetadataAndValues(wsAssignAttributeResult, 
                        attributeAssigns[0], values, assignmentNotes, assignmentEnabledTime, assignmentDisabledTime, 
                        delegatable, attributeAssignValueOperation);
                    break;
                  case assign_attr:
                    attributeAssignResult = attributeAssignable.getAttributeDelegate().assignAttribute(action, attributeDefName);
                    attributeAssigns = new AttributeAssign[]{attributeAssignResult.getAttributeAssign()};
                    WsAssignAttributeLogic.assignmentMetadataAndValues(wsAssignAttributeResult, 
                        attributeAssigns[0], values, assignmentNotes, assignmentEnabledTime, assignmentDisabledTime, 
                        delegatable, attributeAssignValueOperation);
                    
                    break;
                    
                  case remove_attr:
                    attributeAssignResult = attributeAssignable.getAttributeDelegate().removeAttribute(action, attributeDefName);
                    attributeAssigns = GrouperUtil.toArray(attributeAssignResult.getAttributeAssigns(), AttributeAssign.class);
                    
                    break;
                    
                  default: 
                    throw new RuntimeException("Not expecting AttributeAssignOperation: " + attributeAssignOperation);
                }
                
                //convert the attribute assigns to ws attribute assigns
                int attributeAssignsLength = GrouperUtil.length(attributeAssigns);
                WsAttributeAssign[] wsAttributeAssigns = attributeAssignsLength == 0 ? null : new WsAttributeAssign[attributeAssignsLength];
                int i=0;
                for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns, AttributeAssign.class)) {
                  wsAttributeAssigns[i] = new WsAttributeAssign(attributeAssign);
                  i++;
                }
                wsAssignAttributeResult.setWsAttributeAssigns(wsAttributeAssigns);
                //the result knows if it is changed or not
                if (StringUtils.equals("F", wsAssignAttributeResult.getChanged())) {
                  wsAssignAttributeResult.setChanged(attributeAssignResult.isChanged() ? "T" : "F");
                }
                wsAssignAttributeResultList.add(wsAssignAttributeResult);
              } catch (Exception e) {
                //add to error and keep going
                errorMessage.append(
                    "Problem with " + attributeDefName + ", action: " + action 
                    + ", owner: " + attributeAssignable + ", " + ExceptionUtils.getFullStackTrace(e) + ".  ");
              }
              
              
            }
            
          }
        }
        
        
      }


      Set<String> allGroupIds = new HashSet<String>(GrouperUtil.nonNull(ownerGroupIds));
      Set<String> extraMemberIds = new HashSet<String>();
      for (MultiKey multiKey : GrouperUtil.nonNull(ownerGroupMemberIds)) {
        allGroupIds.add((String)multiKey.getKey(0));
        extraMemberIds.add((String)multiKey.getKey(1));
      }
      
      wsAssignAttributesResults.assignResult(wsAssignAttributeResultList, subjectAttributeNames);
      
      wsAssignAttributesResults.fillInAttributeDefNames(attributeDefNameIds);
      wsAssignAttributesResults.fillInAttributeDefs(ownerAttributeDefIds);
      
      wsAssignAttributesResults.fillInGroups(ownerGroupIds, includeGroupDetail);
      wsAssignAttributesResults.fillInStems(ownerStemIds);
      wsAssignAttributesResults.fillInSubjects(wsOwnerSubjectLookups, extraMemberIds, 
          includeSubjectDetail, subjectAttributeNamesToRetrieve);
      wsAssignAttributesResults.fillInMemberships(ownerMembershipIds);
      
      wsAssignAttributesResults.sortResults();
      
      if (errorMessage.length() > 0) {
        wsAssignAttributesResults.assignResultCode(WsAssignAttributesResultsCode.INVALID_QUERY);
        wsAssignAttributesResults.getResultMetadata().appendResultMessage(errorMessage.toString());
      } else {
        wsAssignAttributesResults.assignResultCode(WsAssignAttributesResultsCode.SUCCESS);
      }
      
      wsAssignAttributesResults.getResultMetadata().appendResultMessage(
          ", Found " + GrouperUtil.length(wsAssignAttributesResults.getWsAttributeAssignResults())
          + " results.  ");

        
    } catch (Exception e) {
      wsAssignAttributesResults.assignResultCodeException(null, theSummary, e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  
    return wsAssignAttributesResults; 
  
  }

  /**
   * assign attributes and values to owner objects (groups, stems, etc)
   * @param attributeAssignType Type of owner, from enum AttributeAssignType, e.g.
   * group, member, stem, any_mem, imm_mem, attr_def, group_asgn, mem_asgn, 
   * stem_asgn, any_mem_asgn, imm_mem_asgn, attr_def_asgn  
   * @param clientVersion is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param wsAttributeAssignId if you know the assign id you want, put id here
   * @param wsOwnerGroupName is group to look in
   * @param wsOwnerGroupId is group to look in
   * @param wsOwnerSubjectId is subject to look in
   * @param wsOwnerSubjectSourceId is subject to look in
   * @param wsOwnerSubjectIdentifier is subject to look in
   * @param wsAttributeDefNameName attribute def name to assign to the owner
   * @param wsAttributeDefNameId attribute def name to assign to the owner
   * @param attributeAssignOperation operation to perform for attribute on owners, from enum AttributeAssignOperation
   * assign_attr, add_attr, remove_attr
   * @param valueId If removing, and id is specified, will
   * only remove values with that id.
   * @param valueSystem is value to add, assign, remove, etc
   * @param valueFormatted is value to add, assign, remove, etc though not implemented yet
   * @param assignmentNotes notes on the assignment (optional)
   * @param assignmentEnabledTime enabled time, or null for enabled now
   * @param assignmentDisabledTime disabled time, or null for not disabled
   * @param delegatable really only for permissions, if the assignee can delegate to someone else.  TRUE|FALSE|GRANT
   * @param attributeAssignValueOperation operation to perform for attribute value on attribute
   * assignments: assign_value, add_value, remove_value, replace_values
   * @param wsOwnerStemName is stem to look in
   * @param wsOwnerStemId is stem to look in
   * @param wsOwnerMembershipId to query attributes on immediate membership
   * @param wsOwnerMembershipAnyGroupName to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnyGroupId to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectId to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectSourceId to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerMembershipAnySubjectIdentifier to query attributes in "any" membership which is on immediate or effective membership
   * @param wsOwnerAttributeDefName to query attributes assigned on attribute def
   * @param wsOwnerAttributeDefId to query attributes assigned on attribute def
   * @param wsOwnerAttributeAssignId for assignment on assignment
   * @param action to assign, or "assign" is the default if blank
   * @param includeSubjectDetail
   *            T|F, for if the extended subject information should be
   *            returned (anything more than just the id)
   * @param actAsSubjectId act as this subject
   * @param actAsSubjectSourceId act as this subject
   * @param actAsSubjectIdentifier act as this subject
   * @param wsAttributeAssignLookups lookups to remove etc
   * @param subjectAttributeNames are the additional subject attributes (data) to return.
   * If blank, whatever is configured in the grouper-ws.properties will be sent
   * @param includeGroupDetail T or F as to if the group detail should be returned
   * @param paramName0
   *            reserved for future use
   * @param paramValue0
   *            reserved for future use
   * @param paramName1
   *            reserved for future use
   * @param paramValue1
   *            reserved for future use
   * @return the results
   */
  @SuppressWarnings("unchecked")
  public static WsAssignAttributesLiteResults assignAttributesLite(
      GrouperWsVersion clientVersion, AttributeAssignType attributeAssignType,
      String wsAttributeDefNameName, String wsAttributeDefNameId,
      AttributeAssignOperation attributeAssignOperation,
      String valueId, String valueSystem, String valueFormatted,
      String assignmentNotes, Timestamp assignmentEnabledTime,
      Timestamp assignmentDisabledTime, AttributeAssignDelegatable delegatable,
      AttributeAssignValueOperation attributeAssignValueOperation,
      String wsAttributeAssignId,
      String wsOwnerGroupName, String wsOwnerGroupId, String wsOwnerStemName, String wsOwnerStemId, 
      String wsOwnerSubjectId, String wsOwnerSubjectSourceId, String wsOwnerSubjectIdentifier,
      String wsOwnerMembershipId, String wsOwnerMembershipAnyGroupName, String wsOwnerMembershipAnyGroupId,
      String wsOwnerMembershipAnySubjectId, String wsOwnerMembershipAnySubjectSourceId, String wsOwnerMembershipAnySubjectIdentifier,
      String wsOwnerAttributeDefName, String wsOwnerAttributeDefId, String wsOwnerAttributeAssignId,
      String action, String actAsSubjectId, String actAsSubjectSourceId, String actAsSubjectIdentifier, boolean includeSubjectDetail,
      String subjectAttributeNames, boolean includeGroupDetail, String paramName0, String paramValue0,
      String paramName1, String paramValue1) {  
  
    WsAttributeDefNameLookup[] wsAttributeDefNameLookups = null;
    if (!StringUtils.isBlank(wsAttributeDefNameName) || !StringUtils.isBlank(wsAttributeDefNameId)) {
      wsAttributeDefNameLookups = new WsAttributeDefNameLookup[]{new WsAttributeDefNameLookup(wsAttributeDefNameName,wsAttributeDefNameId )};
    }
    
    
    WsAttributeAssignValue[] wsAttributeAssignValues = null;
    if (!StringUtils.isBlank(valueId) || !StringUtils.isBlank(valueSystem) || !StringUtils.isBlank(valueFormatted)) {
      WsAttributeAssignValue wsAttributeAssignValue = new WsAttributeAssignValue();
      wsAttributeAssignValue.setId(valueId);
      wsAttributeAssignValue.setValueSystem(valueSystem);
      wsAttributeAssignValue.setValueFormatted(valueFormatted);
      wsAttributeAssignValues = new WsAttributeAssignValue[]{wsAttributeAssignValue};
    }
    
    WsAttributeAssignLookup[] attributeAssignLookups = null;
    
    if (!StringUtils.isBlank(wsAttributeAssignId)) {
      attributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(wsAttributeAssignId)};
    }
    
    WsGroupLookup[] wsOwnerGroupLookups = null;
    if (!StringUtils.isBlank(wsOwnerGroupName) || !StringUtils.isBlank(wsOwnerGroupId)) {
      wsOwnerGroupLookups = new WsGroupLookup[]{new WsGroupLookup(wsOwnerGroupName, wsOwnerGroupId)};
    }
    
    WsStemLookup[] wsOwnerStemLookups = null;
    if (!StringUtils.isBlank(wsOwnerStemName) || !StringUtils.isBlank(wsOwnerStemId)) {
      wsOwnerStemLookups = new WsStemLookup[]{new WsStemLookup(wsOwnerStemName, wsOwnerStemId)};
    }
    
    WsSubjectLookup[] wsOwnerSubjectLookups = null;
    if (!StringUtils.isBlank(wsOwnerSubjectId) || !StringUtils.isBlank(wsOwnerSubjectSourceId) || !StringUtils.isBlank(wsOwnerSubjectIdentifier)) {
      wsOwnerSubjectLookups = new WsSubjectLookup[]{new WsSubjectLookup(wsOwnerSubjectId, wsOwnerSubjectSourceId, wsOwnerSubjectIdentifier)};
    }
    
    WsMembershipLookup[] wsOwnerMembershipLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipId)) {
      wsOwnerMembershipLookups = new WsMembershipLookup[]{new WsMembershipLookup(wsOwnerMembershipId)};
    }
    
    WsMembershipAnyLookup[] wsOwnerMembershipAnyLookups = null;
    if (!StringUtils.isBlank(wsOwnerMembershipAnyGroupName) || !StringUtils.isBlank(wsOwnerMembershipAnyGroupId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectId) || !StringUtils.isBlank(wsOwnerMembershipAnySubjectSourceId)
        || !StringUtils.isBlank(wsOwnerMembershipAnySubjectIdentifier)) {
      wsOwnerMembershipAnyLookups = new WsMembershipAnyLookup[]{
          new WsMembershipAnyLookup(new WsGroupLookup(wsOwnerMembershipAnyGroupName,wsOwnerMembershipAnyGroupId ),
              new WsSubjectLookup(wsOwnerMembershipAnySubjectId, wsOwnerMembershipAnySubjectSourceId, wsOwnerMembershipAnySubjectIdentifier))};
    }
    
    WsAttributeDefLookup[] wsOwnerAttributeDefLookups = null;
    if (!StringUtils.isBlank(wsOwnerAttributeDefName) || !StringUtils.isBlank(wsOwnerAttributeDefId)) {
      wsOwnerAttributeDefLookups = new WsAttributeDefLookup[]{new WsAttributeDefLookup(wsOwnerAttributeDefName, wsOwnerAttributeDefId)}; 
    }
    
    WsAttributeAssignLookup[] ownerAttributeAssignLookups = null;
    if (!StringUtils.isBlank(wsOwnerAttributeAssignId)) {
      ownerAttributeAssignLookups = new WsAttributeAssignLookup[]{new WsAttributeAssignLookup(wsOwnerAttributeAssignId)};
    }
    
    WsSubjectLookup actAsSubjectLookup = WsSubjectLookup.createIfNeeded(actAsSubjectId,
        actAsSubjectSourceId, actAsSubjectIdentifier);
    
    String[] actions = null;
    if (!StringUtils.isBlank(action)) {
      actions = new String[]{action};
    }
    
    String[] subjectAttributeArray = GrouperUtil.splitTrim(subjectAttributeNames, ",");
    
    WsParam[] params = GrouperServiceUtils.params(paramName0, paramValue0, paramName0, paramName1);

    WsAssignAttributesResults wsAssignAttributesResults = assignAttributes(clientVersion, attributeAssignType, 
        wsAttributeDefNameLookups, attributeAssignOperation, wsAttributeAssignValues, assignmentNotes, assignmentEnabledTime,
        assignmentDisabledTime, delegatable, attributeAssignValueOperation, attributeAssignLookups, wsOwnerGroupLookups, wsOwnerStemLookups, 
        wsOwnerSubjectLookups, wsOwnerMembershipLookups, wsOwnerMembershipAnyLookups, wsOwnerAttributeDefLookups, ownerAttributeAssignLookups, actions, 
        actAsSubjectLookup, includeSubjectDetail, subjectAttributeArray, includeGroupDetail, 
        params );
    
    WsAssignAttributesLiteResults wsAssignAttributesLiteResults = new WsAssignAttributesLiteResults(wsAssignAttributesResults);
    
    return wsAssignAttributesLiteResults; 

  }
}
