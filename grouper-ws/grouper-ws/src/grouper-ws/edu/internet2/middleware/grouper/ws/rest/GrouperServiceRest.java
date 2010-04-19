/*
 * @author mchyzer $Id: GrouperServiceRest.java,v 1.16 2009/12/29 07:39:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributesRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.group.WsRestHasMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestGetMembersRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.member.WsRestMemberChangeSubjectRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.membership.WsRestGetMembershipsRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestFindStemsRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemDeleteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.stem.WsRestStemSaveRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsLiteRequest;
import edu.internet2.middleware.grouper.ws.rest.subject.WsRestGetSubjectsRequest;
import edu.internet2.middleware.grouper.ws.soap.GrouperService;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAddMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesLiteResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignAttributesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsAssignGrouperPrivilegesResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignPermissionsLiteResults;
import edu.internet2.middleware.grouper.ws.soap.WsAssignPermissionsResults;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsDeleteMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsFindStemsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetGroupsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembershipsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetPermissionAssignmentsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsGroupSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsHasMemberResults;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemDeleteResults;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveLiteResult;
import edu.internet2.middleware.grouper.ws.soap.WsStemSaveResults;
import edu.internet2.middleware.grouper.ws.soap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * consolidated static list of of rest web services (only web service methods here
 * to have clean javadoc).  the method name corresponds to the url and request method.
 * e.g. "GET /groups/a:b:c/members" will correspond to groupMembersGet()
 */
public class GrouperServiceRest {

  /**
   * <pre>
   * based on a group query, get the groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindGroupsRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindGroupsResults findGroups(GrouperWsVersion clientVersion,
      WsRestFindGroupsRequest wsRestFindGroupsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindGroupsRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindGroupsResults wsFindGroupsResults = new GrouperService(false).findGroups(
        clientVersionString, wsRestFindGroupsRequest.getWsQueryFilter(),
        wsRestFindGroupsRequest.getActAsSubjectLookup(), wsRestFindGroupsRequest
            .getIncludeGroupDetail(), wsRestFindGroupsRequest.getParams(), 
            wsRestFindGroupsRequest.getWsGroupLookups());

    //return result
    return wsFindGroupsResults;
  }

  /**
   * <pre>
   * based on a group query, get the groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindGroupsLiteRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindGroupsResults findGroupsLite(GrouperWsVersion clientVersion,
      WsRestFindGroupsLiteRequest wsRestFindGroupsLiteRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindGroupsLiteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindGroupsResults wsFindGroupsResults = new GrouperService(false).findGroupsLite(
        clientVersionString, wsRestFindGroupsLiteRequest.getQueryFilterType(),
        wsRestFindGroupsLiteRequest.getGroupName(), wsRestFindGroupsLiteRequest
            .getStemName(), wsRestFindGroupsLiteRequest.getStemNameScope(),
        wsRestFindGroupsLiteRequest.getGroupUuid(), wsRestFindGroupsLiteRequest
            .getGroupAttributeName(), wsRestFindGroupsLiteRequest
            .getGroupAttributeValue(), wsRestFindGroupsLiteRequest.getGroupTypeName(),
        wsRestFindGroupsLiteRequest.getActAsSubjectId(), wsRestFindGroupsLiteRequest
            .getActAsSubjectSourceId(), wsRestFindGroupsLiteRequest
            .getActAsSubjectIdentifier(), wsRestFindGroupsLiteRequest
            .getIncludeGroupDetail(), wsRestFindGroupsLiteRequest.getParamName0(),
        wsRestFindGroupsLiteRequest.getParamValue0(), wsRestFindGroupsLiteRequest
            .getParamName1(), wsRestFindGroupsLiteRequest.getParamValue1());

    //return result
    return wsFindGroupsResults;
  }

  /**
   * <pre>
   * based on a group name, get the members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestGetMembersLiteRequest is the request body converted to an object
   * @return the results
   */
  public static WsGetMembersLiteResult getMembersLite(GrouperWsVersion clientVersion,
      String groupName, WsRestGetMembersLiteRequest wsRestGetMembersLiteRequest) {

    //make sure not null
    wsRestGetMembersLiteRequest = wsRestGetMembersLiteRequest == null ? new WsRestGetMembersLiteRequest()
        : wsRestGetMembersLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetMembersLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGetMembersLiteRequest
        .getGroupName(), false, "groupName");

    //get the results
    WsGetMembersLiteResult wsGetMembersLiteResult = new GrouperService(false).getMembersLite(
        clientVersionString, groupName, wsRestGetMembersLiteRequest.getGroupUuid(),
        wsRestGetMembersLiteRequest.getMemberFilter(), wsRestGetMembersLiteRequest
            .getActAsSubjectId(), wsRestGetMembersLiteRequest.getActAsSubjectSourceId(),
        wsRestGetMembersLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetMembersLiteRequest.getFieldName(), wsRestGetMembersLiteRequest
            .getIncludeGroupDetail(), wsRestGetMembersLiteRequest
            .getRetrieveSubjectDetail(), wsRestGetMembersLiteRequest
            .getSubjectAttributeNames(), wsRestGetMembersLiteRequest.getParamName0(),
        wsRestGetMembersLiteRequest.getParamValue0(), wsRestGetMembersLiteRequest
            .getParamName1(), wsRestGetMembersLiteRequest.getParamValue1(), 
            wsRestGetMembersLiteRequest.getSourceIds());

    //return result
    return wsGetMembersLiteResult;

  }

  /**
   * <pre>
   * based on a group name, put the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestAddMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberLiteResult addMemberLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestAddMemberLiteRequest wsRestAddMemberLiteRequest) {

    //make sure not null
    wsRestAddMemberLiteRequest = wsRestAddMemberLiteRequest == null ? new WsRestAddMemberLiteRequest()
        : wsRestAddMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAddMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestAddMemberLiteRequest
        .getGroupName(), true, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestAddMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestAddMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsAddMemberLiteResult wsAddMemberLiteResult = new GrouperService(false).addMemberLite(
        clientVersionString, groupName, wsRestAddMemberLiteRequest.getGroupUuid(),
        subjectId, sourceId, wsRestAddMemberLiteRequest.getSubjectIdentifier(),
        wsRestAddMemberLiteRequest.getActAsSubjectId(), wsRestAddMemberLiteRequest
            .getActAsSubjectSourceId(), wsRestAddMemberLiteRequest
            .getActAsSubjectIdentifier(), wsRestAddMemberLiteRequest.getFieldName(),
        wsRestAddMemberLiteRequest.getIncludeGroupDetail(), wsRestAddMemberLiteRequest
            .getIncludeSubjectDetail(), wsRestAddMemberLiteRequest
            .getSubjectAttributeNames(), wsRestAddMemberLiteRequest.getParamName0(),
        wsRestAddMemberLiteRequest.getParamValue0(), wsRestAddMemberLiteRequest
            .getParamName1(), wsRestAddMemberLiteRequest.getParamValue1());

    //return result
    return wsAddMemberLiteResult;

  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestAddMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsAddMemberResults addMember(GrouperWsVersion clientVersion,
      String groupName, WsRestAddMemberRequest wsRestAddMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestAddMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestAddMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAddMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestAddMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        true, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsAddMemberResults wsAddMemberResults = new GrouperService(false).addMember(
        clientVersionString, wsGroupLookup, wsRestAddMembersRequest.getSubjectLookups(),
        wsRestAddMembersRequest.getReplaceAllExisting(), wsRestAddMembersRequest
            .getActAsSubjectLookup(), wsRestAddMembersRequest.getFieldName(),
        wsRestAddMembersRequest.getTxType(), wsRestAddMembersRequest
            .getIncludeGroupDetail(), wsRestAddMembersRequest.getIncludeSubjectDetail(),
        wsRestAddMembersRequest.getSubjectAttributeNames(), wsRestAddMembersRequest
            .getParams());

    //return result
    return wsAddMemberResults;

  }

  /**
   * <pre>
   * assign privileges.  e.g. url:
   * /v1_3_000/grouperPrivileges
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestAssignGrouperPrivilegeRequest is the request body converted to an object
   * @return the result
   */
  public static WsAssignGrouperPrivilegesResults assignGrouperPrivileges(GrouperWsVersion clientVersion,
      WsRestAssignGrouperPrivilegesRequest wsRestAssignGrouperPrivilegeRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestAssignGrouperPrivilegeRequest != null,
        "Body of request must contain an instance of "
            + WsRestAssignGrouperPrivilegesRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAssignGrouperPrivilegeRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsAssignGrouperPrivilegesResults wsAssignGrouperPrivilegesResults = new GrouperService(false).assignGrouperPrivileges(
        clientVersionString, wsRestAssignGrouperPrivilegeRequest.getWsSubjectLookups(), wsRestAssignGrouperPrivilegeRequest.getWsGroupLookup(), 
        wsRestAssignGrouperPrivilegeRequest.getWsStemLookup(), wsRestAssignGrouperPrivilegeRequest.getPrivilegeType(), wsRestAssignGrouperPrivilegeRequest.getPrivilegeNames(),
        wsRestAssignGrouperPrivilegeRequest.getAllowed(), wsRestAssignGrouperPrivilegeRequest.getReplaceAllExisting(), wsRestAssignGrouperPrivilegeRequest.getTxType(), wsRestAssignGrouperPrivilegeRequest
            .getActAsSubjectLookup(), wsRestAssignGrouperPrivilegeRequest.getIncludeSubjectDetail(),
            wsRestAssignGrouperPrivilegeRequest.getSubjectAttributeNames(), wsRestAssignGrouperPrivilegeRequest
            .getIncludeGroupDetail(), wsRestAssignGrouperPrivilegeRequest.getParams());

    //return result
    return wsAssignGrouperPrivilegesResults;

  }

  /**
   * <pre>
   * based on a group name, delete the member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestDeleteMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsDeleteMemberLiteResult deleteMemberLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestDeleteMemberLiteRequest wsRestDeleteMemberLiteRequest) {

    //make sure not null
    wsRestDeleteMemberLiteRequest = wsRestDeleteMemberLiteRequest == null ? new WsRestDeleteMemberLiteRequest()
        : wsRestDeleteMemberLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestDeleteMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestDeleteMemberLiteRequest
        .getGroupName(), true, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestDeleteMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestDeleteMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsDeleteMemberLiteResult wsDeleteMemberLiteResult = new GrouperService(false)
        .deleteMemberLite(clientVersionString, groupName, wsRestDeleteMemberLiteRequest
            .getGroupUuid(), subjectId, sourceId, wsRestDeleteMemberLiteRequest
            .getSubjectIdentifier(), wsRestDeleteMemberLiteRequest.getActAsSubjectId(),
            wsRestDeleteMemberLiteRequest.getActAsSubjectSourceId(),
            wsRestDeleteMemberLiteRequest.getActAsSubjectIdentifier(),
            wsRestDeleteMemberLiteRequest.getFieldName(), wsRestDeleteMemberLiteRequest
                .getIncludeGroupDetail(), wsRestDeleteMemberLiteRequest
                .getIncludeSubjectDetail(), wsRestDeleteMemberLiteRequest
                .getSubjectAttributeNames(), wsRestDeleteMemberLiteRequest
                .getParamName0(), wsRestDeleteMemberLiteRequest.getParamValue0(),
            wsRestDeleteMemberLiteRequest.getParamName1(), wsRestDeleteMemberLiteRequest
                .getParamValue1());

    //return result
    return wsDeleteMemberLiteResult;

  }

  /**
   * <pre>
   * based on a group name, put multiple members, or all members.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestDeleteMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsDeleteMemberResults deleteMember(GrouperWsVersion clientVersion,
      String groupName, WsRestDeleteMemberRequest wsRestDeleteMembersRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestDeleteMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestDeleteMemberRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestDeleteMembersRequest.getClientVersion(), false, "clientVersion");
  
    WsGroupLookup wsGroupLookup = wsRestDeleteMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }
  
    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        true, "groupName");
    wsGroupLookup.setGroupName(groupName);
  
    //get the results
    WsDeleteMemberResults wsDeleteMemberResults = new GrouperService(false).deleteMember(
        clientVersionString, wsGroupLookup, wsRestDeleteMembersRequest
            .getSubjectLookups(), wsRestDeleteMembersRequest.getActAsSubjectLookup(),
        wsRestDeleteMembersRequest.getFieldName(),
        wsRestDeleteMembersRequest.getTxType(), wsRestDeleteMembersRequest
            .getIncludeGroupDetail(), wsRestDeleteMembersRequest
            .getIncludeSubjectDetail(), wsRestDeleteMembersRequest
            .getSubjectAttributeNames(), wsRestDeleteMembersRequest.getParams());
  
    //return result
    return wsDeleteMemberResults;
  
  }

  /**
   * <pre>
   * based on a group name, and multiple subjects, see if they are members .  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestHasMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsHasMemberResults hasMember(GrouperWsVersion clientVersion,
      String groupName, WsRestHasMemberRequest wsRestHasMembersRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestHasMembersRequest != null,
        "Body of request must contain an instance of "
            + WsRestHasMemberRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestHasMembersRequest.getClientVersion(), false, "clientVersion");

    WsGroupLookup wsGroupLookup = wsRestHasMembersRequest.getWsGroupLookup();
    if (wsGroupLookup == null) {
      wsGroupLookup = new WsGroupLookup();
    }

    groupName = GrouperServiceUtils.pickOne(groupName, wsGroupLookup.getGroupName(),
        true, "groupName");
    wsGroupLookup.setGroupName(groupName);

    //get the results
    WsHasMemberResults wsHasMemberResults = new GrouperService(false).hasMember(
        clientVersionString, wsGroupLookup, wsRestHasMembersRequest.getSubjectLookups(),
        wsRestHasMembersRequest.getMemberFilter(), wsRestHasMembersRequest
            .getActAsSubjectLookup(), wsRestHasMembersRequest.getFieldName(),
        wsRestHasMembersRequest.getIncludeGroupDetail(), wsRestHasMembersRequest
            .getIncludeSubjectDetail(), wsRestHasMembersRequest
            .getSubjectAttributeNames(), wsRestHasMembersRequest.getParams());

    //return result
    return wsHasMemberResults;

  }

  /**
   * <pre>
   * based on a group name, get members .  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param wsRestGetMembersRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetMembersResults getMembers(GrouperWsVersion clientVersion,
      WsRestGetMembersRequest wsRestGetMembersRequest) {

    //cant be null
    GrouperUtil
        .assertion(wsRestGetMembersRequest != null,
            "Body of request must contain an instance of "
                + WsRestGetMembersRequest.class.getSimpleName()
                + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetMembersRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsGetMembersResults wsGetMembersResults = new GrouperService(false).getMembers(
        clientVersionString, wsRestGetMembersRequest.getWsGroupLookups(),
        wsRestGetMembersRequest.getMemberFilter(), wsRestGetMembersRequest
            .getActAsSubjectLookup(), wsRestGetMembersRequest.getFieldName(),
        wsRestGetMembersRequest.getIncludeGroupDetail(), wsRestGetMembersRequest
            .getIncludeSubjectDetail(), wsRestGetMembersRequest
            .getSubjectAttributeNames(), wsRestGetMembersRequest.getParams(),
            wsRestGetMembersRequest.getSourceIds());

    //return result
    return wsGetMembersResults;

  }

  /**
   * <pre>
   * based on a group name, and a subject, see if member
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestHasMemberLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsHasMemberLiteResult hasMemberLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId,
      WsRestHasMemberLiteRequest wsRestHasMemberLiteRequest) {
  
    //make sure not null
    wsRestHasMemberLiteRequest = wsRestHasMemberLiteRequest == null ? new WsRestHasMemberLiteRequest()
        : wsRestHasMemberLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestHasMemberLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestHasMemberLiteRequest
        .getGroupName(), false, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestHasMemberLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestHasMemberLiteRequest
        .getSubjectSourceId(), true, "sourceId");
  
    //get the results
    WsHasMemberLiteResult wsHasMemberLiteResult = new GrouperService(false).hasMemberLite(
        clientVersionString, groupName, wsRestHasMemberLiteRequest.getGroupUuid(),
        subjectId, sourceId, wsRestHasMemberLiteRequest.getSubjectIdentifier(),
        wsRestHasMemberLiteRequest.getMemberFilter(), wsRestHasMemberLiteRequest
            .getActAsSubjectId(), wsRestHasMemberLiteRequest.getActAsSubjectSourceId(),
        wsRestHasMemberLiteRequest.getActAsSubjectIdentifier(),
        wsRestHasMemberLiteRequest.getFieldName(), wsRestHasMemberLiteRequest
            .getIncludeGroupDetail(), wsRestHasMemberLiteRequest
            .getIncludeSubjectDetail(), wsRestHasMemberLiteRequest
            .getSubjectAttributeNames(), wsRestHasMemberLiteRequest.getParamName0(),
        wsRestHasMemberLiteRequest.getParamValue0(), wsRestHasMemberLiteRequest
            .getParamName1(), wsRestHasMemberLiteRequest.getParamValue1());
  
    //return result
    return wsHasMemberLiteResult;
  
  }

  /**
   * <pre>
   * based on a member, change the subject  e.g. url:
   * /v1_3_000/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param oldSubjectId from url if applicable
   * @param oldSubjectSourceId from url is applicable
   * @param wsRestMemberChangeSubjectRequest is the request body converted to an object
   * @return the result
   */
  public static WsMemberChangeSubjectResults memberChangeSubject(GrouperWsVersion clientVersion,
      WsRestMemberChangeSubjectRequest wsRestMemberChangeSubjectRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestMemberChangeSubjectRequest != null,
        "Body of request must contain an instance of "
            + WsRestDeleteMemberRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestMemberChangeSubjectRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsMemberChangeSubjectResults wsMemberChangeSubjectResults = new GrouperService(false).memberChangeSubject(
        clientVersionString, wsRestMemberChangeSubjectRequest.getWsMemberChangeSubjects(), 
          wsRestMemberChangeSubjectRequest.getActAsSubjectLookup(),
            wsRestMemberChangeSubjectRequest.getTxType(), wsRestMemberChangeSubjectRequest
            .getIncludeSubjectDetail(), wsRestMemberChangeSubjectRequest
            .getSubjectAttributeNames(), wsRestMemberChangeSubjectRequest.getParams());

    //return result
    return wsMemberChangeSubjectResults;

  }

  /**
   * <pre>
   * based on a member, change the subject
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param oldSubjectId from url, e.g. /v1_3_000/members/subjectId/123412345
   * @param oldSubjectSourceId from url (optional) e.g.
   * /v1_3_000/members/sourceId/someSource/subjectId/123412345/sourceId/12342
   * @param wsRestMemberChangeSubjectLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsMemberChangeSubjectLiteResult memberChangeSubjectLite(GrouperWsVersion clientVersion,
      String oldSubjectId, String oldSubjectSourceId, WsRestMemberChangeSubjectLiteRequest wsRestMemberChangeSubjectLiteRequest) {

    //make sure not null
    wsRestMemberChangeSubjectLiteRequest = wsRestMemberChangeSubjectLiteRequest == null ? new WsRestMemberChangeSubjectLiteRequest()
        : wsRestMemberChangeSubjectLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestMemberChangeSubjectLiteRequest.getClientVersion(), false, "clientVersion");
    oldSubjectId = GrouperServiceUtils.pickOne(oldSubjectId, wsRestMemberChangeSubjectLiteRequest
        .getOldSubjectId(), false, "oldSubjectId");
    oldSubjectSourceId = GrouperServiceUtils.pickOne(oldSubjectSourceId, wsRestMemberChangeSubjectLiteRequest
        .getOldSubjectSourceId(), true, "oldSubjectSourceId");

    //get the results
    WsMemberChangeSubjectLiteResult wsMemberChangeSubjectLiteResult = new GrouperService(false).memberChangeSubjectLite(
        clientVersionString,oldSubjectId, oldSubjectSourceId, wsRestMemberChangeSubjectLiteRequest.getOldSubjectIdentifier(),
        wsRestMemberChangeSubjectLiteRequest.getNewSubjectId(),
        wsRestMemberChangeSubjectLiteRequest.getNewSubjectSourceId(),
        wsRestMemberChangeSubjectLiteRequest.getNewSubjectIdentifier(),
        wsRestMemberChangeSubjectLiteRequest
            .getActAsSubjectId(), wsRestMemberChangeSubjectLiteRequest.getActAsSubjectSourceId(),
            wsRestMemberChangeSubjectLiteRequest.getActAsSubjectIdentifier(),
            wsRestMemberChangeSubjectLiteRequest.getDeleteOldMember(), 
            wsRestMemberChangeSubjectLiteRequest.getIncludeSubjectDetail(),
            wsRestMemberChangeSubjectLiteRequest
            .getSubjectAttributeNames(), wsRestMemberChangeSubjectLiteRequest.getParamName0(),
            wsRestMemberChangeSubjectLiteRequest.getParamValue0(), wsRestMemberChangeSubjectLiteRequest
            .getParamName1(), wsRestMemberChangeSubjectLiteRequest.getParamValue1());

    //return result
    return wsMemberChangeSubjectLiteResult;

  }

  /**
   * <pre>
   * get privileges for a group or stem and subject
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestGetGrouperPrivilegesLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGrouperPrivilegesLiteResult getGrouperPrivilegesLite(GrouperWsVersion clientVersion,
      WsRestGetGrouperPrivilegesLiteRequest wsRestGetGrouperPrivilegesLiteRequest) {
  
    //make sure not null
    wsRestGetGrouperPrivilegesLiteRequest = wsRestGetGrouperPrivilegesLiteRequest == null ? new WsRestGetGrouperPrivilegesLiteRequest()
        : wsRestGetGrouperPrivilegesLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetGrouperPrivilegesLiteRequest.getClientVersion(), false, "clientVersion");
  
    //get the results
    WsGetGrouperPrivilegesLiteResult wsGetGrouperPrivilegesLiteResult = new GrouperService(false).getGrouperPrivilegesLite(
        clientVersionString,
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectId(),
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectSourceId(),
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectIdentifier(), 
        wsRestGetGrouperPrivilegesLiteRequest.getGroupName(), 
        wsRestGetGrouperPrivilegesLiteRequest.getGroupUuid(), 
        wsRestGetGrouperPrivilegesLiteRequest.getStemName(), 
        wsRestGetGrouperPrivilegesLiteRequest.getStemUuid(), 
        wsRestGetGrouperPrivilegesLiteRequest.getPrivilegeType(), 
        wsRestGetGrouperPrivilegesLiteRequest.getPrivilegeName(),
        wsRestGetGrouperPrivilegesLiteRequest.getActAsSubjectId(), 
        wsRestGetGrouperPrivilegesLiteRequest.getActAsSubjectSourceId(),
        wsRestGetGrouperPrivilegesLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetGrouperPrivilegesLiteRequest.getIncludeSubjectDetail(),
        wsRestGetGrouperPrivilegesLiteRequest.getSubjectAttributeNames(), 
        wsRestGetGrouperPrivilegesLiteRequest.getIncludeGroupDetail(),
        wsRestGetGrouperPrivilegesLiteRequest.getParamName0(),
        wsRestGetGrouperPrivilegesLiteRequest.getParamValue0(), 
        wsRestGetGrouperPrivilegesLiteRequest.getParamName1(), 
        wsRestGetGrouperPrivilegesLiteRequest.getParamValue1());
  
    //return result
    return wsGetGrouperPrivilegesLiteResult;
  
  }

  /**
   * <pre>
   * assign/revoke privileges for a group or stem and subject
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestAssignGrouperPrivilegesLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsAssignGrouperPrivilegesLiteResult assignGrouperPrivilegesLite(GrouperWsVersion clientVersion,
      WsRestAssignGrouperPrivilegesLiteRequest wsRestAssignGrouperPrivilegesLiteRequest) {

    //make sure not null
    wsRestAssignGrouperPrivilegesLiteRequest = wsRestAssignGrouperPrivilegesLiteRequest == null 
      ? new WsRestAssignGrouperPrivilegesLiteRequest()
        : wsRestAssignGrouperPrivilegesLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAssignGrouperPrivilegesLiteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = new GrouperService(false)
          .assignGrouperPrivilegesLite(
        clientVersionString,
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectId(),
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectSourceId(),
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectIdentifier(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getGroupName(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getGroupUuid(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getStemName(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getStemUuid(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getPrivilegeType(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getPrivilegeName(),
        wsRestAssignGrouperPrivilegesLiteRequest.getAllowed(),
        wsRestAssignGrouperPrivilegesLiteRequest.getActAsSubjectId(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getActAsSubjectSourceId(),
        wsRestAssignGrouperPrivilegesLiteRequest.getActAsSubjectIdentifier(),
        wsRestAssignGrouperPrivilegesLiteRequest.getIncludeSubjectDetail(),
        wsRestAssignGrouperPrivilegesLiteRequest.getSubjectAttributeNames(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getIncludeGroupDetail(),
        wsRestAssignGrouperPrivilegesLiteRequest.getParamName0(),
        wsRestAssignGrouperPrivilegesLiteRequest.getParamValue0(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getParamName1(), 
        wsRestAssignGrouperPrivilegesLiteRequest.getParamValue1());

    //return result
    return wsAssignGrouperPrivilegesLiteResult;

  }

  /**
   * <pre>
   * based a subject object of type WsRestGetGroupsRequest, get the groups
   * /v1_3_000/subjects/123/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param sourceId is the source of the service
   * @param subjectId is the subject to search for groups
   * @param wsRestGetGroupsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGroupsResults getGroups(GrouperWsVersion clientVersion,
      String subjectId, String sourceId, WsRestGetGroupsRequest wsRestGetGroupsRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestGetGroupsRequest != null,
        "Body of request must contain an instance of "
            + WsRestGetGroupsRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetGroupsRequest.getClientVersion(), false, "clientVersion");

    Set<WsSubjectLookup> subjectLookups = null;
    WsSubjectLookup[] subjectLookupArray = wsRestGetGroupsRequest.getSubjectLookups();
    if (!StringUtils.isBlank(subjectId)) {
      
      if (GrouperUtil.length(subjectLookupArray) == 0) {
        subjectLookups = new HashSet<WsSubjectLookup>();
      } else {
        subjectLookups = GrouperUtil.toSet(subjectLookupArray);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, null);
      subjectLookups.add(wsSubjectLookup);
      subjectLookupArray = GrouperUtil.toArray(subjectLookups, WsSubjectLookup.class);
    }

    //get the results
    WsGetGroupsResults wsGetGroupsResults = new GrouperService(false).getGroups(
        clientVersionString, subjectLookupArray,
        wsRestGetGroupsRequest.getMemberFilter(), wsRestGetGroupsRequest
            .getActAsSubjectLookup(), wsRestGetGroupsRequest.getIncludeGroupDetail(),
        wsRestGetGroupsRequest.getIncludeSubjectDetail(), wsRestGetGroupsRequest
            .getSubjectAttributeNames(), wsRestGetGroupsRequest.getParams(), wsRestGetGroupsRequest.getFieldName(),
            wsRestGetGroupsRequest.getScope(),
            wsRestGetGroupsRequest.getWsStemLookup(), wsRestGetGroupsRequest.getStemScope(), wsRestGetGroupsRequest.getEnabled(),
            wsRestGetGroupsRequest.getPageSize(), wsRestGetGroupsRequest.getPageNumber(), wsRestGetGroupsRequest.getSortString(), 
            wsRestGetGroupsRequest.getAscending());

    //return result
    return wsGetGroupsResults;

  }

  /**
   * <pre>
   * based on a subject, get the groups associated
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group including stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestGetGroupsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetGroupsLiteResult getGroupsLite(GrouperWsVersion clientVersion,
      String subjectId, String sourceId,
      WsRestGetGroupsLiteRequest wsRestGetGroupsLiteRequest) {

    //make sure not null
    wsRestGetGroupsLiteRequest = wsRestGetGroupsLiteRequest == null ? new WsRestGetGroupsLiteRequest()
        : wsRestGetGroupsLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetGroupsLiteRequest.getClientVersion(), false, "clientVersion");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestGetGroupsLiteRequest
        .getSubjectId(), false, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestGetGroupsLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsGetGroupsLiteResult wsGetGroupsLiteResult = new GrouperService(false).getGroupsLite(
        clientVersionString, subjectId, sourceId, wsRestGetGroupsLiteRequest
            .getSubjectIdentifier(), wsRestGetGroupsLiteRequest.getMemberFilter(),
        wsRestGetGroupsLiteRequest.getActAsSubjectId(), wsRestGetGroupsLiteRequest
            .getActAsSubjectSourceId(), wsRestGetGroupsLiteRequest
            .getActAsSubjectIdentifier(), wsRestGetGroupsLiteRequest
            .getIncludeGroupDetail(), wsRestGetGroupsLiteRequest
            .getIncludeSubjectDetail(), wsRestGetGroupsLiteRequest
            .getSubjectAttributeNames(), wsRestGetGroupsLiteRequest.getParamName0(),
        wsRestGetGroupsLiteRequest.getParamValue0(), wsRestGetGroupsLiteRequest
            .getParamName1(), wsRestGetGroupsLiteRequest.getParamValue1(), wsRestGetGroupsLiteRequest.getFieldName(),
            wsRestGetGroupsLiteRequest.getScope(), wsRestGetGroupsLiteRequest.getStemName(), 
            wsRestGetGroupsLiteRequest.getStemUuid(), wsRestGetGroupsLiteRequest.getStemScope(),
            wsRestGetGroupsLiteRequest.getEnabled(), wsRestGetGroupsLiteRequest.getPageSize(),
            wsRestGetGroupsLiteRequest.getPageNumber(), wsRestGetGroupsLiteRequest.getSortString(), 
            wsRestGetGroupsLiteRequest.getAscending());

    //return result
    return wsGetGroupsLiteResult;

  }

  /**
   * <pre>
   * based on a stem query, get the stems
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindStemsRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindStemsResults findStems(GrouperWsVersion clientVersion,
      WsRestFindStemsRequest wsRestFindStemsRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindStemsRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindStemsResults wsFindStemsResults = new GrouperService(false).findStems(
        clientVersionString, wsRestFindStemsRequest.getWsStemQueryFilter(),
        wsRestFindStemsRequest.getActAsSubjectLookup(), wsRestFindStemsRequest
            .getParams(), wsRestFindStemsRequest.getWsStemLookups());

    //return result
    return wsFindStemsResults;
  }

  /**
   * <pre>
   * based on a stem query, get the stems
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestFindStemsLiteRequest is the request body converted to an object
   * @return the results
   */
  public static WsFindStemsResults findStemsLite(GrouperWsVersion clientVersion,
      WsRestFindStemsLiteRequest wsRestFindStemsLiteRequest) {

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestFindStemsLiteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsFindStemsResults wsFindStemsResults = new GrouperService(false).findStemsLite(
        clientVersionString, wsRestFindStemsLiteRequest.getStemQueryFilterType(),
        wsRestFindStemsLiteRequest.getStemName(), wsRestFindStemsLiteRequest
            .getParentStemName(), wsRestFindStemsLiteRequest.getParentStemNameScope(),
        wsRestFindStemsLiteRequest.getStemUuid(), wsRestFindStemsLiteRequest
            .getStemAttributeName(), wsRestFindStemsLiteRequest.getStemAttributeValue(),
        wsRestFindStemsLiteRequest.getActAsSubjectId(), wsRestFindStemsLiteRequest
            .getActAsSubjectSourceId(), wsRestFindStemsLiteRequest
            .getActAsSubjectIdentifier(), wsRestFindStemsLiteRequest.getParamName0(),
        wsRestFindStemsLiteRequest.getParamValue0(), wsRestFindStemsLiteRequest
            .getParamName1(), wsRestFindStemsLiteRequest.getParamValue1());

    //return result
    return wsFindStemsResults;
  }

  /**
   * <pre>
   * based on a submitted object of type WsRestStemSaveRequest, save stems.  e.g. url:
   * /v1_3_000/stems
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestStemSaveRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemSaveResults stemSave(GrouperWsVersion clientVersion,
      WsRestStemSaveRequest wsRestStemSaveRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestStemSaveRequest != null,
        "Body of request must contain an instance of "
            + WsRestStemSaveRequest.class.getSimpleName() + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemSaveRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsStemSaveResults wsStemSaveResults = new GrouperService(false).stemSave(
        clientVersionString, wsRestStemSaveRequest.getWsStemToSaves(),
        wsRestStemSaveRequest.getActAsSubjectLookup(), wsRestStemSaveRequest.getTxType(),
        wsRestStemSaveRequest.getParams());

    //return result
    return wsStemSaveResults;

  }

  /**
   * <pre>
   * based on a stem name and submitted object type WsRestStemSaveLiteRequest,
   * save a stem.  url e.g. /v1_3_000/stems/aStem:aStem2
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param stemLookupName is the name of the stem to lookup and save (old name if changing) including parent stems, e.g. a:b:c
   * @param wsRestStemSaveLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemSaveLiteResult stemSaveLite(GrouperWsVersion clientVersion,
      String stemLookupName, WsRestStemSaveLiteRequest wsRestStemSaveLiteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestStemSaveLiteRequest != null,
        "Body of request must contain an instance of "
            + WsRestStemSaveLiteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemSaveLiteRequest.getClientVersion(), false, "clientVersion");

    stemLookupName = GrouperServiceUtils.pickOne(stemLookupName,
        wsRestStemSaveLiteRequest.getStemLookupName(), false, "stemLookupName");

    //get the results
    WsStemSaveLiteResult wsStemSaveLiteResult = new GrouperService(false).stemSaveLite(
        clientVersionString, wsRestStemSaveLiteRequest.getStemLookupUuid(),
        stemLookupName, wsRestStemSaveLiteRequest.getStemUuid(), wsRestStemSaveLiteRequest.getStemName(),
        wsRestStemSaveLiteRequest.getDisplayExtension(),wsRestStemSaveLiteRequest
            .getDescription(), 
        wsRestStemSaveLiteRequest.getSaveMode(), wsRestStemSaveLiteRequest
            .getActAsSubjectId(), wsRestStemSaveLiteRequest.getActAsSubjectSourceId(),
        wsRestStemSaveLiteRequest.getActAsSubjectIdentifier(), wsRestStemSaveLiteRequest
            .getParamName0(), wsRestStemSaveLiteRequest.getParamValue0(),
        wsRestStemSaveLiteRequest.getParamName1(), wsRestStemSaveLiteRequest
            .getParamValue1());

    //return result
    return wsStemSaveLiteResult;

  }

  /**
   * <pre>
   * based on submitted object of type WsRestStemDeleteRequest, delete stems
   * /v1_3_000/groups/aStem:aGroup/members
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestStemDeleteRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemDeleteResults stemDelete(GrouperWsVersion clientVersion,
      WsRestStemDeleteRequest wsRestStemDeleteRequest) {

    //cant be null
    GrouperUtil
        .assertion(wsRestStemDeleteRequest != null,
            "Body of request must contain an instance of "
                + WsRestStemDeleteRequest.class.getSimpleName()
                + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemDeleteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsStemDeleteResults wsStemDeleteResults = new GrouperService(false).stemDelete(
        clientVersionString, wsRestStemDeleteRequest.getWsStemLookups(),
        wsRestStemDeleteRequest.getActAsSubjectLookup(), wsRestStemDeleteRequest
            .getTxType(), wsRestStemDeleteRequest.getParams());

    //return result
    return wsStemDeleteResults;

  }

  /**
   * <pre>
   * based on a stem name, delete the stem url e.g. /v1_3_000/stems/aStem:aStem2
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param stemName is the name of the stem to delete including parent stems, e.g. a:b:c
   * @param wsRestStemDeleteLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsStemDeleteLiteResult stemDeleteLite(GrouperWsVersion clientVersion,
      String stemName, WsRestStemDeleteLiteRequest wsRestStemDeleteLiteRequest) {

    //make sure not null
    wsRestStemDeleteLiteRequest = wsRestStemDeleteLiteRequest == null ? new WsRestStemDeleteLiteRequest()
        : wsRestStemDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestStemDeleteLiteRequest.getClientVersion(), false, "clientVersion");

    stemName = GrouperServiceUtils.pickOne(stemName, wsRestStemDeleteLiteRequest
        .getStemName(), false, "stemName");

    //get the results
    WsStemDeleteLiteResult wsStemDeleteLiteResult = new GrouperService(false).stemDeleteLite(
        clientVersionString, stemName, wsRestStemDeleteLiteRequest.getStemUuid(),
        wsRestStemDeleteLiteRequest.getActAsSubjectId(), wsRestStemDeleteLiteRequest
            .getActAsSubjectSourceId(), wsRestStemDeleteLiteRequest
            .getActAsSubjectIdentifier(), wsRestStemDeleteLiteRequest.getParamName0(),
        wsRestStemDeleteLiteRequest.getParamValue0(), wsRestStemDeleteLiteRequest
            .getParamName1(), wsRestStemDeleteLiteRequest.getParamValue1());

    //return result
    return wsStemDeleteLiteResult;

  }

  /**
   * <pre>
   * based on a submitted object of type WsRestGroupDeleteRequest, delete the groups.  e.g. url:
   * /v1_3_000/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestGroupDeleteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupDeleteResults groupDelete(GrouperWsVersion clientVersion,
      WsRestGroupDeleteRequest wsRestGroupDeleteRequest) {

    //cant be null
    GrouperUtil.assertion(wsRestGroupDeleteRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupDeleteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupDeleteRequest.getClientVersion(), false, "clientVersion");

    //get the results
    WsGroupDeleteResults wsGroupDeleteResults = new GrouperService(false).groupDelete(
        clientVersionString, wsRestGroupDeleteRequest.getWsGroupLookups(),
        wsRestGroupDeleteRequest.getActAsSubjectLookup(), wsRestGroupDeleteRequest
            .getTxType(), wsRestGroupDeleteRequest.getIncludeGroupDetail(), 
            wsRestGroupDeleteRequest.getParams());

    //return result
    return wsGroupDeleteResults;

  }

  /**
   * <pre>
   * based on a group name, delete a group
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group to delete including parent stems, e.g. a:b:c
   * @param subjectId from url, e.g. /v1_3_000/groups/aStem:aGroup/members/123412345
   * @param sourceId from url (optional) e.g.
   * /v1_3_000/groups/aStem:aGroup/members/sourceId/someSource/subjectId/123412345
   * @param wsRestGroupDeleteLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupDeleteLiteResult groupDeleteLite(GrouperWsVersion clientVersion,
      String groupName, WsRestGroupDeleteLiteRequest wsRestGroupDeleteLiteRequest) {

    //make sure not null
    wsRestGroupDeleteLiteRequest = wsRestGroupDeleteLiteRequest == null ? new WsRestGroupDeleteLiteRequest()
        : wsRestGroupDeleteLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupDeleteLiteRequest.getClientVersion(), false, "clientVersion");

    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGroupDeleteLiteRequest
        .getGroupName(), false, "groupName");

    //get the results
    WsGroupDeleteLiteResult wsGroupDeleteLiteResult = new GrouperService(false)
        .groupDeleteLite(clientVersionString, groupName, wsRestGroupDeleteLiteRequest
            .getGroupUuid(), wsRestGroupDeleteLiteRequest.getActAsSubjectId(),
            wsRestGroupDeleteLiteRequest.getActAsSubjectSourceId(),
            wsRestGroupDeleteLiteRequest.getActAsSubjectIdentifier(),
            wsRestGroupDeleteLiteRequest.getIncludeGroupDetail(),
            wsRestGroupDeleteLiteRequest.getParamName0(), wsRestGroupDeleteLiteRequest
                .getParamValue0(), wsRestGroupDeleteLiteRequest.getParamName1(),
            wsRestGroupDeleteLiteRequest.getParamValue1());

    //return result
    return wsGroupDeleteLiteResult;

  }

  /**
   * <pre>
   * based on a submitted object of type WsRestGroupSaveRequest, save groups.  e.g. url:
   * /v1_3_000/groups
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param wsRestGroupSaveRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupSaveResults groupSave(GrouperWsVersion clientVersion,
      WsRestGroupSaveRequest wsRestGroupSaveRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestGroupSaveRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupSaveRequest.class.getSimpleName() + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupSaveRequest.getClientVersion(), false, "clientVersion");
  
    //get the results
    WsGroupSaveResults wsGroupSaveResults = new GrouperService(false).groupSave(
        clientVersionString, wsRestGroupSaveRequest.getWsGroupToSaves(),
        wsRestGroupSaveRequest.getActAsSubjectLookup(), wsRestGroupSaveRequest.getTxType(),
        wsRestGroupSaveRequest.getIncludeGroupDetail(),
        wsRestGroupSaveRequest.getParams());
  
    //return result
    return wsGroupSaveResults;
  
  }

  /**
   * <pre>
   * based on a group name and submitted object type WsRestGroupSaveLiteRequest,
   * save a group.  url e.g. /v1_3_000/groups/aStem:aGroup2
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupLookupName is the name of the group to lookup and save (old name if changing) including parent groups, e.g. a:b:c
   * @param wsRestGroupSaveLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGroupSaveLiteResult groupSaveLite(GrouperWsVersion clientVersion,
      String groupLookupName, WsRestGroupSaveLiteRequest wsRestGroupSaveLiteRequest) {
  
    //cant be null
    GrouperUtil.assertion(wsRestGroupSaveLiteRequest != null,
        "Body of request must contain an instance of "
            + WsRestGroupSaveLiteRequest.class.getSimpleName()
            + " in xml, xhtml, json, etc");
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGroupSaveLiteRequest.getClientVersion(), false, "clientVersion");
  
    groupLookupName = GrouperServiceUtils.pickOne(groupLookupName,
        wsRestGroupSaveLiteRequest.getGroupLookupName(), false, "groupLookupName");
  
    //get the results
    WsGroupSaveLiteResult wsGroupSaveLiteResult = new GrouperService(false).groupSaveLite(
        clientVersionString, wsRestGroupSaveLiteRequest.getGroupLookupUuid(),
        groupLookupName, wsRestGroupSaveLiteRequest.getGroupUuid(), wsRestGroupSaveLiteRequest.getGroupName(),
        wsRestGroupSaveLiteRequest.getDisplayExtension(),wsRestGroupSaveLiteRequest
            .getDescription(), 
        wsRestGroupSaveLiteRequest.getSaveMode(), wsRestGroupSaveLiteRequest
            .getActAsSubjectId(), wsRestGroupSaveLiteRequest.getActAsSubjectSourceId(),
        wsRestGroupSaveLiteRequest.getActAsSubjectIdentifier(), 
        wsRestGroupSaveLiteRequest.getIncludeGroupDetail(), wsRestGroupSaveLiteRequest
            .getParamName0(), wsRestGroupSaveLiteRequest.getParamValue0(),
        wsRestGroupSaveLiteRequest.getParamName1(), wsRestGroupSaveLiteRequest
            .getParamValue1());
  
    //return result
    return wsGroupSaveLiteResult;
  
  }

  /**
   * <pre>
   * based on a group name, get memberships.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/memberships
   * /v1_3_000/subjects/12345678/memberships
   * /v1_3_000/memberships
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group (optional)
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetMembershipsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetMembershipsResults getMemberships(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId, 
      WsRestGetMembershipsRequest wsRestGetMembershipsRequest) {
  
    //cant be null
    wsRestGetMembershipsRequest = wsRestGetMembershipsRequest == null ? new WsRestGetMembershipsRequest()
      : wsRestGetMembershipsRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetMembershipsRequest.getClientVersion(), false, "clientVersion");
  
    Set<WsGroupLookup> groupLookups = null;
    WsGroupLookup[] groupLookupArray = wsRestGetMembershipsRequest.getWsGroupLookups();
    //add a lookup
    if (!StringUtils.isBlank(groupName)) {
      if (GrouperUtil.length(groupLookupArray) == 0) {
        groupLookups = new HashSet<WsGroupLookup>();
      } else {
        groupLookups = GrouperUtil.toSet(groupLookupArray);
      }
      
      WsGroupLookup wsGroupLookup = new WsGroupLookup(groupName, null);
      groupLookups.add(wsGroupLookup);
      groupLookupArray = GrouperUtil.toArray(groupLookups, WsGroupLookup.class);
    }

    Set<WsSubjectLookup> subjectLookups = null;
    WsSubjectLookup[] subjectLookupArray = wsRestGetMembershipsRequest.getWsSubjectLookups();
    if (!StringUtils.isBlank(subjectId)) {
      
      if (GrouperUtil.length(subjectLookupArray) == 0) {
        subjectLookups = new HashSet<WsSubjectLookup>();
      } else {
        subjectLookups = GrouperUtil.toSet(subjectLookupArray);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, null);
      subjectLookups.add(wsSubjectLookup);
      subjectLookupArray = GrouperUtil.toArray(subjectLookups, WsSubjectLookup.class);
    }
        
    //get the results
    WsGetMembershipsResults wsGetMembershipsResults = new GrouperService(false).getMemberships(
        clientVersionString, groupLookupArray, subjectLookupArray,
        wsRestGetMembershipsRequest.getMemberFilter(), wsRestGetMembershipsRequest
            .getActAsSubjectLookup(), wsRestGetMembershipsRequest.getFieldName(), wsRestGetMembershipsRequest.getIncludeSubjectDetail(), 
            wsRestGetMembershipsRequest.getSubjectAttributeNames(),
        wsRestGetMembershipsRequest.getIncludeGroupDetail(), wsRestGetMembershipsRequest.getParams(),
            wsRestGetMembershipsRequest.getSourceIds(), wsRestGetMembershipsRequest.getScope(), 
            wsRestGetMembershipsRequest.getWsStemLookup(), wsRestGetMembershipsRequest.getStemScope(), wsRestGetMembershipsRequest.getEnabled(), wsRestGetMembershipsRequest.getMembershipIds() );
  
    //return result
    return wsGetMembershipsResults;
  
  }

  /**
   * <pre>
   * based on a group name, get memberships.  e.g. url:
   * /v1_3_000/groups/aStem:aGroup/memberships
   * /v1_3_000/subjects/12345678/memberships
   * /v1_3_000/memberships
   * </pre>
   * @param clientVersion version of client, e.g. v1_3_000
   * @param groupName is the name of the group (optional)
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetMembershipsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetMembershipsResults getMembershipsLite(GrouperWsVersion clientVersion,
      String groupName, String subjectId, String sourceId, WsRestGetMembershipsLiteRequest wsRestGetMembershipsLiteRequest) {
  
    //make sure not null
    wsRestGetMembershipsLiteRequest = wsRestGetMembershipsLiteRequest == null ? new WsRestGetMembershipsLiteRequest()
        : wsRestGetMembershipsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetMembershipsLiteRequest.getClientVersion(), false, "clientVersion");
    groupName = GrouperServiceUtils.pickOne(groupName, wsRestGetMembershipsLiteRequest
        .getGroupName(), true, "groupName");
    subjectId = GrouperServiceUtils.pickOne(subjectId, wsRestGetMembershipsLiteRequest
        .getSubjectId(), true, "subjectId");
    sourceId = GrouperServiceUtils.pickOne(sourceId, wsRestGetMembershipsLiteRequest
        .getSubjectSourceId(), true, "sourceId");

    //get the results
    WsGetMembershipsResults wsGetMembershipsResults = new GrouperService(false).getMembershipsLite(
        clientVersionString, groupName, wsRestGetMembershipsLiteRequest.getGroupUuid(), subjectId, sourceId, wsRestGetMembershipsLiteRequest.getSubjectIdentifier(),
        wsRestGetMembershipsLiteRequest.getMemberFilter(), wsRestGetMembershipsLiteRequest.getIncludeSubjectDetail(), wsRestGetMembershipsLiteRequest
            .getActAsSubjectId(), wsRestGetMembershipsLiteRequest.getActAsSubjectSourceId(),
        wsRestGetMembershipsLiteRequest.getActAsSubjectIdentifier(),
        wsRestGetMembershipsLiteRequest.getFieldName(), wsRestGetMembershipsLiteRequest
            .getSubjectAttributeNames(), wsRestGetMembershipsLiteRequest.getIncludeGroupDetail(), wsRestGetMembershipsLiteRequest.getParamName0(),
        wsRestGetMembershipsLiteRequest.getParamValue0(), wsRestGetMembershipsLiteRequest
            .getParamName1(), wsRestGetMembershipsLiteRequest.getParamValue1(), 
            wsRestGetMembershipsLiteRequest.getSourceIds(), wsRestGetMembershipsLiteRequest.getScope(), wsRestGetMembershipsLiteRequest.getStemName(),
            wsRestGetMembershipsLiteRequest.getStemUuid(), wsRestGetMembershipsLiteRequest.getStemScope(), 
            wsRestGetMembershipsLiteRequest.getEnabled(), wsRestGetMembershipsLiteRequest.getMembershipIds());
  
    //return result
    return wsGetMembershipsResults;
  
  }

  /**
   * <pre>
   * find subjects by id or search string.  e.g. url:
   * /v1_6_000/subjects/12345678
   * /v1_6_000/subjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_6_000
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetSubjectsRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetSubjectsResults getSubjects(GrouperWsVersion clientVersion,
      String subjectId, String sourceId, 
      WsRestGetSubjectsRequest wsRestGetSubjectsRequest) {
  
    //cant be null
    wsRestGetSubjectsRequest = wsRestGetSubjectsRequest == null ? new WsRestGetSubjectsRequest()
      : wsRestGetSubjectsRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetSubjectsRequest.getClientVersion(), false, "clientVersion");
  
    Set<WsSubjectLookup> subjectLookups = null;
    WsSubjectLookup[] subjectLookupArray = wsRestGetSubjectsRequest.getWsSubjectLookups();
    if (!StringUtils.isBlank(subjectId)) {
      
      if (GrouperUtil.length(subjectLookupArray) == 0) {
        subjectLookups = new HashSet<WsSubjectLookup>();
      } else {
        subjectLookups = GrouperUtil.toSet(subjectLookupArray);
      }
      
      WsSubjectLookup wsSubjectLookup = new WsSubjectLookup(subjectId, sourceId, null);
      subjectLookups.add(wsSubjectLookup);
      subjectLookupArray = GrouperUtil.toArray(subjectLookups, WsSubjectLookup.class);
    }
        
    //get the results
    WsGetSubjectsResults wsGetSubjectsResults = new GrouperService(false).getSubjects(
        clientVersionString, subjectLookupArray, wsRestGetSubjectsRequest.getSearchString(),
        wsRestGetSubjectsRequest.getIncludeSubjectDetail(), wsRestGetSubjectsRequest.getSubjectAttributeNames(),
        wsRestGetSubjectsRequest.getActAsSubjectLookup(), wsRestGetSubjectsRequest.getSourceIds(), 
        wsRestGetSubjectsRequest.getWsGroupLookup(), wsRestGetSubjectsRequest.getMemberFilter(),
        wsRestGetSubjectsRequest.getFieldName(), wsRestGetSubjectsRequest.getIncludeGroupDetail(), 
        wsRestGetSubjectsRequest.getParams());
  
    //return result
    return wsGetSubjectsResults;
  
  }

  /**
   * <pre>
   * find subjects by id or search string.  e.g. url:
   * /v1_6_000/subjects/12345678
   * /v1_6_000/subjects
   * </pre>
   * @param clientVersion version of client, e.g. v1_6_000
   * @param subjectId is the subjectId (optional)
   * @param sourceId is the source id of the subject to search for (optional)
   * @param wsRestGetSubjectsLiteRequest is the request body converted to an object
   * @return the result
   */
  public static WsGetSubjectsResults getSubjectsLite(GrouperWsVersion clientVersion,
      String subjectId, String sourceId, 
      WsRestGetSubjectsLiteRequest wsRestGetSubjectsLiteRequest) {
  
    //cant be null
    wsRestGetSubjectsLiteRequest = wsRestGetSubjectsLiteRequest == null ? new WsRestGetSubjectsLiteRequest()
      : wsRestGetSubjectsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetSubjectsLiteRequest.getClientVersion(), false, "clientVersion");
    
    String theSubjectId = wsRestGetSubjectsLiteRequest.getSubjectId();
    
    if (!StringUtils.isBlank(subjectId)) {
      if (!StringUtils.isBlank(theSubjectId) 
          && !StringUtils.equals(subjectId, theSubjectId)) {
        throw new WsInvalidQueryException("subjectId in url '" + subjectId 
            + "' is different than in XML submission '" + theSubjectId + "'");
      }
      theSubjectId = subjectId;
    }
    
    String theSourceId = wsRestGetSubjectsLiteRequest.getSubjectSourceId();
    
    if (!StringUtils.isBlank(sourceId)) {
      if (!StringUtils.isBlank(theSourceId) 
          && !StringUtils.equals(sourceId, theSourceId)) {
        throw new WsInvalidQueryException("sourceId in url '" + sourceId 
            + "' is different than in XML submission '" + theSourceId + "'");
      }
      theSourceId = sourceId;
    }
    
    //get the results
    WsGetSubjectsResults wsGetSubjectsResults = new GrouperService(false).getSubjectsLite(
        clientVersionString, theSubjectId, theSourceId, 
        wsRestGetSubjectsLiteRequest.getSubjectIdentifier(), wsRestGetSubjectsLiteRequest.getSearchString(),
        wsRestGetSubjectsLiteRequest.getIncludeSubjectDetail(), wsRestGetSubjectsLiteRequest.getSubjectAttributeNames(),
        wsRestGetSubjectsLiteRequest.getActAsSubjectId(), wsRestGetSubjectsLiteRequest.getActAsSubjectSourceId(),
        wsRestGetSubjectsLiteRequest.getActAsSubjectIdentifier(), wsRestGetSubjectsLiteRequest.getSourceIds(),
        wsRestGetSubjectsLiteRequest.getGroupName(), wsRestGetSubjectsLiteRequest.getGroupUuid(),
        wsRestGetSubjectsLiteRequest.getMemberFilter(), wsRestGetSubjectsLiteRequest.getFieldName(),
        wsRestGetSubjectsLiteRequest.getIncludeGroupDetail(), wsRestGetSubjectsLiteRequest.getParamName0(),
        wsRestGetSubjectsLiteRequest.getParamValue0(), wsRestGetSubjectsLiteRequest.getParamName1(),
        wsRestGetSubjectsLiteRequest.getParamValue1());

    //return result
    return wsGetSubjectsResults;
  
  }
  
  /**
   * get attribute assignments rest
   * @param clientVersion
   * @param wsRestGetAttributesRequest
   * @return the result
   */
  public static WsGetAttributeAssignmentsResults getAttributeAssignments(GrouperWsVersion clientVersion,
      WsRestGetAttributeAssignmentsRequest wsRestGetAttributesRequest) {
    //cant be null
    wsRestGetAttributesRequest = wsRestGetAttributesRequest == null ? new WsRestGetAttributeAssignmentsRequest()
      : wsRestGetAttributesRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetAttributesRequest.getClientVersion(), false, "clientVersion");

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new GrouperService(false).getAttributeAssignments(
        clientVersionString, wsRestGetAttributesRequest.getAttributeAssignType(), 
        wsRestGetAttributesRequest.getWsAttributeAssignLookups(), wsRestGetAttributesRequest.getWsAttributeDefLookups(), 
        wsRestGetAttributesRequest.getWsAttributeDefNameLookups(), wsRestGetAttributesRequest.getWsOwnerGroupLookups(), 
        wsRestGetAttributesRequest.getWsOwnerStemLookups(), wsRestGetAttributesRequest.getWsOwnerSubjectLookups(), 
        wsRestGetAttributesRequest.getWsOwnerMembershipLookups(), wsRestGetAttributesRequest.getWsOwnerMembershipAnyLookups(), 
        wsRestGetAttributesRequest.getWsOwnerAttributeDefLookups(), wsRestGetAttributesRequest.getActions(), 
        wsRestGetAttributesRequest.getIncludeAssignmentsOnAssignments(), wsRestGetAttributesRequest.getActAsSubjectLookup(), 
        wsRestGetAttributesRequest.getIncludeSubjectDetail(), wsRestGetAttributesRequest.getSubjectAttributeNames(), 
        wsRestGetAttributesRequest.getIncludeGroupDetail(), wsRestGetAttributesRequest.getParams(), 
        wsRestGetAttributesRequest.getEnabled());
    
    return wsGetAttributeAssignmentsResults;
  }
  
  /**
   * get attribute assignments rest for one owner (lite)
   * @param clientVersion
   * @param wsRestGetAttributesLiteRequest
   * @return the results object
   */
  public static WsGetAttributeAssignmentsResults getAttributeAssignmentsLite(GrouperWsVersion clientVersion,
      WsRestGetAttributeAssignmentsLiteRequest wsRestGetAttributesLiteRequest) {

    //cant be null
    wsRestGetAttributesLiteRequest = wsRestGetAttributesLiteRequest == null ? new WsRestGetAttributeAssignmentsLiteRequest() : wsRestGetAttributesLiteRequest;

    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetAttributesLiteRequest.getClientVersion(), false, "clientVersion");

    WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = new GrouperService(false).getAttributeAssignmentsLite(
        clientVersionString, wsRestGetAttributesLiteRequest.getAttributeAssignType(), wsRestGetAttributesLiteRequest.getAttributeAssignId(), wsRestGetAttributesLiteRequest.getWsAttributeDefName(), 
        wsRestGetAttributesLiteRequest.getWsAttributeDefId(), wsRestGetAttributesLiteRequest.getWsAttributeDefNameName(), wsRestGetAttributesLiteRequest.getWsAttributeDefNameId(), wsRestGetAttributesLiteRequest.getWsOwnerGroupName(), 
        wsRestGetAttributesLiteRequest.getWsOwnerGroupId(), wsRestGetAttributesLiteRequest.getWsOwnerStemName(), wsRestGetAttributesLiteRequest.getWsOwnerStemId(), wsRestGetAttributesLiteRequest.getWsOwnerSubjectId(), 
        wsRestGetAttributesLiteRequest.getWsOwnerSubjectSourceId(), wsRestGetAttributesLiteRequest.getWsOwnerSubjectIdentifier(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipId(), 
        wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnyGroupName(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnyGroupId(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnySubjectId(), 
        wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnySubjectSourceId(), wsRestGetAttributesLiteRequest.getWsOwnerMembershipAnySubjectIdentifier(), 
        wsRestGetAttributesLiteRequest.getWsOwnerAttributeDefName(), wsRestGetAttributesLiteRequest.getWsOwnerAttributeDefId(), wsRestGetAttributesLiteRequest.getAction(), 
        wsRestGetAttributesLiteRequest.getIncludeAssignmentsOnAssignments(), wsRestGetAttributesLiteRequest.getActAsSubjectId(), wsRestGetAttributesLiteRequest.getActAsSubjectSourceId(), wsRestGetAttributesLiteRequest.getActAsSubjectIdentifier(), 
        wsRestGetAttributesLiteRequest.getIncludeSubjectDetail(), 
        wsRestGetAttributesLiteRequest.getSubjectAttributeNames(), wsRestGetAttributesLiteRequest.getIncludeGroupDetail(), wsRestGetAttributesLiteRequest.getParamName0(), 
        wsRestGetAttributesLiteRequest.getParamValue0(), wsRestGetAttributesLiteRequest.getParamName1(), wsRestGetAttributesLiteRequest.getParamValue1(), wsRestGetAttributesLiteRequest.getEnabled());
    
    return wsGetAttributeAssignmentsResults;
    
  }

  /**
   * assign attributes rest
   * @param clientVersion
   * @param wsRestAssignAttributesRequest
   * @return the result
   */
  public static WsAssignAttributesResults assignAttributes(GrouperWsVersion clientVersion,
      WsRestAssignAttributesRequest wsRestAssignAttributesRequest) {
    //cant be null
    wsRestAssignAttributesRequest = wsRestAssignAttributesRequest == null ? new WsRestAssignAttributesRequest()
      : wsRestAssignAttributesRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAssignAttributesRequest.getClientVersion(), false, "clientVersion");
  
    WsAssignAttributesResults wsAssignAttributesResults = new GrouperService(false).assignAttributes(
        clientVersionString, wsRestAssignAttributesRequest.getAttributeAssignType(), 
        wsRestAssignAttributesRequest.getWsAttributeDefNameLookups(), wsRestAssignAttributesRequest.getAttributeAssignOperation(),
        wsRestAssignAttributesRequest.getValues(), wsRestAssignAttributesRequest.getAssignmentNotes(), 
        wsRestAssignAttributesRequest.getAssignmentEnabledTime(), wsRestAssignAttributesRequest.getAssignmentDisabledTime(),
        wsRestAssignAttributesRequest.getDelegatable(), wsRestAssignAttributesRequest.getAttributeAssignValueOperation(),
        wsRestAssignAttributesRequest.getWsAttributeAssignLookups(),
        wsRestAssignAttributesRequest.getWsOwnerGroupLookups(), 
        wsRestAssignAttributesRequest.getWsOwnerStemLookups(), wsRestAssignAttributesRequest.getWsOwnerSubjectLookups(), 
        wsRestAssignAttributesRequest.getWsOwnerMembershipLookups(), wsRestAssignAttributesRequest.getWsOwnerMembershipAnyLookups(), 
        wsRestAssignAttributesRequest.getWsOwnerAttributeDefLookups(), wsRestAssignAttributesRequest.getWsOwnerAttributeAssignLookups(),
        wsRestAssignAttributesRequest.getActions(), 
        wsRestAssignAttributesRequest.getActAsSubjectLookup(), 
        wsRestAssignAttributesRequest.getIncludeSubjectDetail(), wsRestAssignAttributesRequest.getSubjectAttributeNames(), 
        wsRestAssignAttributesRequest.getIncludeGroupDetail(), wsRestAssignAttributesRequest.getParams());
    
    return wsAssignAttributesResults;
  }

  /**
   * assign attributes rest for one owner (lite)
   * @param clientVersion
   * @param wsRestAssignAttributesLiteRequest
   * @return the results object
   */
  public static WsAssignAttributesLiteResults assignAttributesLite(GrouperWsVersion clientVersion,
      WsRestAssignAttributesLiteRequest wsRestAssignAttributesLiteRequest) {
  
    //cant be null
    wsRestAssignAttributesLiteRequest = wsRestAssignAttributesLiteRequest == null ? new WsRestAssignAttributesLiteRequest() : wsRestAssignAttributesLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAssignAttributesLiteRequest.getClientVersion(), false, "clientVersion");
  
    WsAssignAttributesLiteResults wsAssignAttributesLiteResults = new GrouperService(false).assignAttributesLite(
        clientVersionString, wsRestAssignAttributesLiteRequest.getAttributeAssignType(), 
        wsRestAssignAttributesLiteRequest.getWsAttributeDefNameName(), wsRestAssignAttributesLiteRequest.getWsAttributeDefNameId(), 
        wsRestAssignAttributesLiteRequest.getAttributeAssignOperation(), wsRestAssignAttributesLiteRequest.getValueId(),
        wsRestAssignAttributesLiteRequest.getValueSystem(), wsRestAssignAttributesLiteRequest.getValueFormatted(),
        wsRestAssignAttributesLiteRequest.getAssignmentNotes(), wsRestAssignAttributesLiteRequest.getAssignmentEnabledTime(),
        wsRestAssignAttributesLiteRequest.getAssignmentDisabledTime(), wsRestAssignAttributesLiteRequest.getDelegatable(), 
        wsRestAssignAttributesLiteRequest.getAttributeAssignValueOperation(), wsRestAssignAttributesLiteRequest.getAttributeAssignId(),
        wsRestAssignAttributesLiteRequest.getWsOwnerGroupName(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerGroupId(), wsRestAssignAttributesLiteRequest.getWsOwnerStemName(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerStemId(), wsRestAssignAttributesLiteRequest.getWsOwnerSubjectId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerSubjectSourceId(), wsRestAssignAttributesLiteRequest.getWsOwnerSubjectIdentifier(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnyGroupName(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnyGroupId(), wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnySubjectId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnySubjectSourceId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerMembershipAnySubjectIdentifier(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerAttributeDefName(), wsRestAssignAttributesLiteRequest.getWsOwnerAttributeDefId(), 
        wsRestAssignAttributesLiteRequest.getWsOwnerAttributeAssignId(), wsRestAssignAttributesLiteRequest.getAction(), 
        wsRestAssignAttributesLiteRequest.getActAsSubjectId(), 
        wsRestAssignAttributesLiteRequest.getActAsSubjectSourceId(), wsRestAssignAttributesLiteRequest.getActAsSubjectIdentifier(), 
        wsRestAssignAttributesLiteRequest.getIncludeSubjectDetail(), 
        wsRestAssignAttributesLiteRequest.getSubjectAttributeNames(), wsRestAssignAttributesLiteRequest.getIncludeGroupDetail(), 
        wsRestAssignAttributesLiteRequest.getParamName0(), 
        wsRestAssignAttributesLiteRequest.getParamValue0(), wsRestAssignAttributesLiteRequest.getParamName1(), 
        wsRestAssignAttributesLiteRequest.getParamValue1());
    
    return wsAssignAttributesLiteResults;
  }

  /**
   * get permission assignments rest
   * @param clientVersion
   * @param wsRestGetPermissionAssignmentsRequest
   * @return the result
   */
  public static WsGetPermissionAssignmentsResults getPermissionAssignments(GrouperWsVersion clientVersion,
      WsRestGetPermissionAssignmentsRequest wsRestGetPermissionAssignmentsRequest) {
    //cant be null
    wsRestGetPermissionAssignmentsRequest = wsRestGetPermissionAssignmentsRequest == null ? new WsRestGetPermissionAssignmentsRequest()
      : wsRestGetPermissionAssignmentsRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetPermissionAssignmentsRequest.getClientVersion(), false, "clientVersion");
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new GrouperService(false).getPermissionAssignments(
        clientVersionString, wsRestGetPermissionAssignmentsRequest.getWsAttributeDefLookups(), 
        wsRestGetPermissionAssignmentsRequest.getWsAttributeDefNameLookups(), wsRestGetPermissionAssignmentsRequest.getRoleLookups(), 
        wsRestGetPermissionAssignmentsRequest.getWsSubjectLookups(), wsRestGetPermissionAssignmentsRequest.getActions(), 
        wsRestGetPermissionAssignmentsRequest.getIncludePermissionAssignDetail(), wsRestGetPermissionAssignmentsRequest.getIncludeAttributeDefNames(),
        wsRestGetPermissionAssignmentsRequest.getIncludeAttributeAssignments(),
        wsRestGetPermissionAssignmentsRequest.getIncludeAssignmentsOnAssignments(), wsRestGetPermissionAssignmentsRequest.getActAsSubjectLookup(), 
        wsRestGetPermissionAssignmentsRequest.getIncludeSubjectDetail(), wsRestGetPermissionAssignmentsRequest.getSubjectAttributeNames(), 
        wsRestGetPermissionAssignmentsRequest.getIncludeGroupDetail(), wsRestGetPermissionAssignmentsRequest.getParams(), 
        wsRestGetPermissionAssignmentsRequest.getEnabled());
    
    return wsGetPermissionAssignmentsResults;
  }

  /**
   * get permission assignments rest for one owner (lite)
   * @param clientVersion
   * @param wsRestGetPermissionAssignmentsLiteRequest
   * @return the results object
   */
  public static WsGetPermissionAssignmentsResults getPermissionAssignmentsLite(GrouperWsVersion clientVersion,
      WsRestGetPermissionAssignmentsLiteRequest wsRestGetPermissionAssignmentsLiteRequest) {
  
    //cant be null
    wsRestGetPermissionAssignmentsLiteRequest = wsRestGetPermissionAssignmentsLiteRequest == null ? new WsRestGetPermissionAssignmentsLiteRequest() : wsRestGetPermissionAssignmentsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestGetPermissionAssignmentsLiteRequest.getClientVersion(), false, "clientVersion");
  
    WsGetPermissionAssignmentsResults wsGetPermissionAssignmentsResults = new GrouperService(false).getPermissionAssignmentsLite(
        clientVersionString, wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefName(), 
        wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefId(), wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefNameName(), 
        wsRestGetPermissionAssignmentsLiteRequest.getWsAttributeDefNameId(), wsRestGetPermissionAssignmentsLiteRequest.getRoleName(), 
        wsRestGetPermissionAssignmentsLiteRequest.getRoleId(), wsRestGetPermissionAssignmentsLiteRequest.getWsSubjectId(), 
        wsRestGetPermissionAssignmentsLiteRequest.getWsSubjectSourceId(), wsRestGetPermissionAssignmentsLiteRequest.getWsSubjectIdentifier(), 
        wsRestGetPermissionAssignmentsLiteRequest.getAction(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludePermissionAssignDetail(), wsRestGetPermissionAssignmentsLiteRequest.getIncludeAttributeDefNames(),
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeAttributeAssignments(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeAssignmentsOnAssignments(), wsRestGetPermissionAssignmentsLiteRequest.getActAsSubjectId(), 
        wsRestGetPermissionAssignmentsLiteRequest.getActAsSubjectSourceId(), wsRestGetPermissionAssignmentsLiteRequest.getActAsSubjectIdentifier(), 
        wsRestGetPermissionAssignmentsLiteRequest.getIncludeSubjectDetail(), 
        wsRestGetPermissionAssignmentsLiteRequest.getSubjectAttributeNames(), wsRestGetPermissionAssignmentsLiteRequest.getIncludeGroupDetail(), 
        wsRestGetPermissionAssignmentsLiteRequest.getParamName0(), 
        wsRestGetPermissionAssignmentsLiteRequest.getParamValue0(), wsRestGetPermissionAssignmentsLiteRequest.getParamName1(), 
        wsRestGetPermissionAssignmentsLiteRequest.getParamValue1(), wsRestGetPermissionAssignmentsLiteRequest.getEnabled());
    
    return wsGetPermissionAssignmentsResults;
    
  }

  /**
   * assign permissions rest
   * @param clientVersion
   * @param wsRestAssignPermissionsRequest
   * @return the result
   */
  public static WsAssignPermissionsResults assignPermissions(GrouperWsVersion clientVersion,
      WsRestAssignPermissionsRequest wsRestAssignPermissionsRequest) {
    //cant be null
    wsRestAssignPermissionsRequest = wsRestAssignPermissionsRequest == null ? new WsRestAssignPermissionsRequest()
      : wsRestAssignPermissionsRequest;
    
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAssignPermissionsRequest.getClientVersion(), false, "clientVersion");
  
    WsAssignPermissionsResults wsAssignPermissionsResults = new GrouperService(false).assignPermissions(
        clientVersionString, wsRestAssignPermissionsRequest.getPermissionType(),
        wsRestAssignPermissionsRequest.getPermissionDefNameLookups(), wsRestAssignPermissionsRequest.getPermissionAssignOperation(),
        wsRestAssignPermissionsRequest.getAssignmentNotes(), 
        wsRestAssignPermissionsRequest.getAssignmentEnabledTime(), wsRestAssignPermissionsRequest.getAssignmentDisabledTime(),
        wsRestAssignPermissionsRequest.getDelegatable(), 
        wsRestAssignPermissionsRequest.getWsAttributeAssignLookups(),
        wsRestAssignPermissionsRequest.getRoleLookups(), 
        wsRestAssignPermissionsRequest.getSubjectRoleLookups(),
        wsRestAssignPermissionsRequest.getActions(), 
        wsRestAssignPermissionsRequest.getActAsSubjectLookup(), 
        wsRestAssignPermissionsRequest.getIncludeSubjectDetail(), wsRestAssignPermissionsRequest.getSubjectAttributeNames(), 
        wsRestAssignPermissionsRequest.getIncludeGroupDetail(), wsRestAssignPermissionsRequest.getParams());
    
    return wsAssignPermissionsResults;
  }

  /**
   * assign attributes rest for one owner (lite)
   * @param clientVersion
   * @param wsRestAssignPermissionsLiteRequest
   * @return the results object
   */
  public static WsAssignPermissionsLiteResults assignPermissionsLite(GrouperWsVersion clientVersion,
      WsRestAssignPermissionsLiteRequest wsRestAssignPermissionsLiteRequest) {
  
    //cant be null
    wsRestAssignPermissionsLiteRequest = wsRestAssignPermissionsLiteRequest == null ? new WsRestAssignPermissionsLiteRequest() : wsRestAssignPermissionsLiteRequest;
  
    String clientVersionString = GrouperServiceUtils.pickOne(clientVersion.name(),
        wsRestAssignPermissionsLiteRequest.getClientVersion(), false, "clientVersion");
  
    WsAssignPermissionsLiteResults wsAssignPermissionsLiteResults = new GrouperService(false).assignPermissionsLite(
        clientVersionString, wsRestAssignPermissionsLiteRequest.getPermissionType(), 
        wsRestAssignPermissionsLiteRequest.getPermissionDefNameName(), wsRestAssignPermissionsLiteRequest.getPermissionDefNameId(), 
        wsRestAssignPermissionsLiteRequest.getPermissionAssignOperation(), 
        wsRestAssignPermissionsLiteRequest.getAssignmentNotes(), wsRestAssignPermissionsLiteRequest.getAssignmentEnabledTime(),
        wsRestAssignPermissionsLiteRequest.getAssignmentDisabledTime(), wsRestAssignPermissionsLiteRequest.getDelegatable(), 
        wsRestAssignPermissionsLiteRequest.getAttributeAssignId(),
        wsRestAssignPermissionsLiteRequest.getRoleName(), 
        wsRestAssignPermissionsLiteRequest.getRoleId(),  
        wsRestAssignPermissionsLiteRequest.getSubjectRoleName(), 
        wsRestAssignPermissionsLiteRequest.getSubjectRoleId(), wsRestAssignPermissionsLiteRequest.getSubjectRoleSubjectId(), 
        wsRestAssignPermissionsLiteRequest.getSubjectRoleSubjectSourceId(), 
        wsRestAssignPermissionsLiteRequest.getSubjectRoleSubjectIdentifier(), 
        wsRestAssignPermissionsLiteRequest.getAction(), 
        wsRestAssignPermissionsLiteRequest.getActAsSubjectId(), 
        wsRestAssignPermissionsLiteRequest.getActAsSubjectSourceId(), wsRestAssignPermissionsLiteRequest.getActAsSubjectIdentifier(), 
        wsRestAssignPermissionsLiteRequest.getIncludeSubjectDetail(), 
        wsRestAssignPermissionsLiteRequest.getSubjectAttributeNames(), wsRestAssignPermissionsLiteRequest.getIncludeGroupDetail(), 
        wsRestAssignPermissionsLiteRequest.getParamName0(), 
        wsRestAssignPermissionsLiteRequest.getParamValue0(), wsRestAssignPermissionsLiteRequest.getParamName1(), 
        wsRestAssignPermissionsLiteRequest.getParamValue1());
    
    return wsAssignPermissionsLiteResults;
  }
}
