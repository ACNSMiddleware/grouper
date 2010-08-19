/*
 * @author mchyzer
 * $Id: AuditTypeBuiltin.java,v 1.10 2009-07-17 03:00:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;



/**
 *
 */
public enum AuditTypeBuiltin implements AuditTypeIdentifier {

  /**
   * add group type
   */
  GROUP_TYPE_ADD(new AuditType("groupType", "addGroupType", null, "id", "name")),
  
  /**
   * update group type
   */
  GROUP_TYPE_UPDATE(new AuditType("groupType", "updateGroupType", null, "id", "name")),
  
  /**
   * delete group type
   */
  GROUP_TYPE_DELETE(new AuditType("groupType", "deleteGroupType", null, "id", "name")),
  
  /**
   * add group field
   */
  GROUP_FIELD_ADD(new AuditType("groupField", "addGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * update group field
   */
  GROUP_FIELD_UPDATE(new AuditType("groupField", "updateGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * delete group field
   */
  GROUP_FIELD_DELETE(new AuditType("groupField", "deleteGroupField", null, "id", "name", 
      "groupTypeId", "groupTypeName", "type")),
  
  /**
   * add group attribute
   */
  GROUP_ATTRIBUTE_ADD(new AuditType("groupAttribute", "addGroupAttribute", null, "id", 
      "groupId", "groupName", "fieldId", "fieldName", "value")),
  
  /**
   * update group attribute
   */
  GROUP_ATTRIBUTE_UPDATE(new AuditType("groupAttribute", "updateGroupAttribute", null, "id", 
      "groupId", "groupName", "fieldId", "fieldName", "value", "oldValue")),

  /**
   * delete group attribute
   */
  GROUP_ATTRIBUTE_DELETE(new AuditType("groupAttribute", "deleteGroupAttribute", null, "id", 
      "groupId", "groupName", "fieldId", "fieldName", "value")),

  /**
   * add group composite
   */
  GROUP_COMPOSITE_ADD(new AuditType("groupComposite", "addGroupComposite", null, "id", "ownerId", 
      "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * update group composite
   */
  GROUP_COMPOSITE_UPDATE(new AuditType("groupComposite", "updateGroupComposite", null, "id", "ownerId", 
      "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * delete group composite
   */
  GROUP_COMPOSITE_DELETE(new AuditType("groupComposite", "deleteGroupComposite", null, "id", "ownerId", 
      "ownerName", "leftFactorId", "leftFactorName", "rightFactorId", "rightFactorName", "type")),
  
  /**
   * assign group type
   */
  GROUP_TYPE_ASSIGN(new AuditType("groupTypeAssignment", "assignGroupType", null, "id", "groupId", 
      "groupName", "typeId", "typeName")),
  
  /**
   * unassign group type
   */
  GROUP_TYPE_UNASSIGN(new AuditType("groupTypeAssignment", "unassignGroupType", null, "id", "groupId", 
      "groupName", "typeId", "typeName")),

  /**
   * add group membership
   */
  MEMBERSHIP_GROUP_ADD(new AuditType("membership", "addGroupMembership", null, "id", 
      "fieldId", "fieldName", "memberId", "membershipType", "groupId", "groupName")),
  
  /**
   * update group membership
   */
  MEMBERSHIP_GROUP_UPDATE(new AuditType("membership", "updateGroupMembership", null, "id", 
      "fieldId", "fieldName", "memberId", "membershipType", "groupId", "groupName")),

  /**
   * delete group membership
   */
  MEMBERSHIP_GROUP_DELETE(new AuditType("membership", "deleteGroupMembership", null, "id", 
      "fieldId", "fieldName", "memberId", "membershipType", "groupId", "groupName")),

  /**
   * add group privilege
   */
  PRIVILEGE_GROUP_ADD(new AuditType("privilege", "addGroupPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "groupId", "groupName")),
  
  /**
   * update group privilege
   */
  PRIVILEGE_GROUP_UPDATE(new AuditType("privilege", "updateGroupPrivilege", null,"privilegeName", "memberId", 
      "privilegeType", "groupId", "groupName")),

  /**
   * delete group privilege
   */
  PRIVILEGE_GROUP_DELETE(new AuditType("privilege", "deleteGroupPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "groupId", "groupName")),

  /**
   * add stem privilege
   */
  PRIVILEGE_STEM_ADD(new AuditType("privilege", "addStemPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "stemId", "stemName")),
  
  /**
   * update stem privilege
   */
  PRIVILEGE_STEM_UPDATE(new AuditType("privilege", "updateStemPrivilege", null,"privilegeName", "memberId", 
      "privilegeType", "stemId", "stemName")),

  /**
   * delete stem privilege
   */
  PRIVILEGE_STEM_DELETE(new AuditType("privilege", "deleteStemPrivilege", null, "privilegeName", "memberId", 
      "privilegeType", "stemId", "stemName")),

  /**
   * add attribute def
   */
  ATTRIBUTE_DEF_ADD(new AuditType("attributeDef", "addAttributeDef", null, 
      "id", "name", "description", "parentStemId")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_DEF_UPDATE(new AuditType("attributeDef", "updateAttributeDef", null, "id", "name", 
      "description", "parentStemId")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_DEF_DELETE(new AuditType("attributeDef", "deleteAttributeDef", null, "id", "name", 
      "description", "parentStemId")),

  /**
   * 
   */
  ATTRIBUTE_DEF_NAME_ADD(new AuditType("attributeDefName", "addAttributeDefName", null, 
      "id", "name", "displayName", "description", "parentStemId", "parentAttributeDefId", "parentAttributeDefName")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_DEF_NAME_UPDATE(new AuditType("attributeDefName", "updateAttributeDefName", null, "id", "name", "displayName", 
      "description", "parentStemId", "parentAttributeDefId", "parentAttributeDefName")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_DEF_NAME_DELETE(new AuditType("attributeDefName", "deleteAttributeDefName", null, "id", "name", "displayName", 
      "description", "parentStemId", "parentAttributeDefId", "parentAttributeDefName")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_GROUP_ADD(new AuditType("attributeAssignGroup", "addAttributeAssignGroup", null, 
      "id", "ownerGroupName", "ownerGroupId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update group
   */
  ATTRIBUTE_ASSIGN_GROUP_UPDATE(new AuditType("attributeAssignGroup", "updateAttributeAssignGroup", null, 
      "id", "ownerGroupName", "ownerGroupId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete group
   */
  ATTRIBUTE_ASSIGN_GROUP_DELETE(new AuditType("attributeAssignGroup", "deleteAttributeAssignGroup", null, 
      "id", "ownerGroupName", "ownerGroupId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_STEM_ADD(new AuditType("attributeAssignStem", "addAttributeAssignStem", null, 
      "id", "ownerStemName", "ownerStemId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update stem
   */
  ATTRIBUTE_ASSIGN_STEM_UPDATE(new AuditType("attributeAssignStem", "updateAttributeAssignStem", null, 
      "id", "ownerStemName", "ownerStemId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete stem
   */
  ATTRIBUTE_ASSIGN_STEM_DELETE(new AuditType("attributeAssignStem", "deleteAttributeAssignStem", null, 
      "id", "ownerStemName", "ownerStemId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_MEMBER_ADD(new AuditType("attributeAssignMember", "addAttributeAssignMember", null, 
      "id", "ownerSourceId", "ownerSubjectId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update member
   */
  ATTRIBUTE_ASSIGN_MEMBER_UPDATE(new AuditType("attributeAssignMember", "updateAttributeAssignMember", null, 
      "id", "ownerSourceId", "ownerSubjectId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete member
   */
  ATTRIBUTE_ASSIGN_MEMBER_DELETE(new AuditType("attributeAssignMember", "deleteAttributeAssignMember", null, 
      "id", "ownerSourceId", "ownerSubjectId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_IMMMSHIP_ADD(new AuditType("attributeAssignImmMship", "addAttributeAssignImmMship", null, 
      "id", "ownerMembershipId", "ownerOwnerId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update imm mship
   */
  ATTRIBUTE_ASSIGN_IMMMSHIP_UPDATE(new AuditType("attributeAssignImmMship", "updateAttributeAssignImmMship", null, 
      "id", "ownerMembershipId", "ownerOwnerId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete imm mship
   */
  ATTRIBUTE_ASSIGN_IMMMSHIP_DELETE(new AuditType("attributeAssignImmMship", "deleteAttributeAssignImmMship", null, 
      "id", "ownerMembershipId", "ownerOwnerId", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * 
   */
  ATTRIBUTE_ASSIGN_ANYMSHIP_ADD(new AuditType("attributeAssignAnyMship", "addAttributeAssignAnyMship", null, 
      "id", "ownerGroupId", "ownerGroupName", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update any mship
   */
  ATTRIBUTE_ASSIGN_ANYMSHIP_UPDATE(new AuditType("attributeAssignAnyMship", "updateAttributeAssignAnyMship", null, 
      "id", "ownerGroupId", "ownerGroupName", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete any mship
   */
  ATTRIBUTE_ASSIGN_ANYMSHIP_DELETE(new AuditType("attributeAssignAnyMship", "deleteAttributeAssignAnyMship", null, 
      "id", "ownerGroupId", "ownerGroupName", "ownerMemberId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * insert attribute def
   */
  ATTRIBUTE_ASSIGN_ATTRDEF_ADD(new AuditType("attributeAssignAttrDef", "addAttributeAssignAttrDef", null, 
      "id", "ownerAttributeDefId", "ownerAttributeDefName", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update attribute def
   */
  ATTRIBUTE_ASSIGN_ATTRDEF_UPDATE(new AuditType("attributeAssignAttrDef", "updateAttributeAssignAttrDef", null, 
      "id", "ownerAttributeDefId", "ownerAttributeDefName", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete attribute def
   */
  ATTRIBUTE_ASSIGN_ATTRDEF_DELETE(new AuditType("attributeAssignAttrDef", "deleteAttributeAssignAttrDef", null, 
      "id", "ownerAttributeDefId", "ownerAttributeDefName", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * insert attribute assign
   */
  ATTRIBUTE_ASSIGN_ASSIGN_ADD(new AuditType("attributeAssignAssign", "addAttributeAssignAssign", null, 
      "id", "ownerAttributeAssignId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),
  
  /**
   * update attribute assign
   */
  ATTRIBUTE_ASSIGN_ASSIGN_UPDATE(new AuditType("attributeAssignAssign", "updateAttributeAssignAssign", null, 
      "id", "ownerAttributeAssignId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * delete attribute assign
   */
  ATTRIBUTE_ASSIGN_ASSIGN_DELETE(new AuditType("attributeAssignAssign", "deleteAttributeAssignAssign", null, 
      "id", "ownerAttributeAssignId", "attributeDefNameName", "attributeDefNameId", "action", "attributeDefId")),

  /**
   * add group
   */
  GROUP_ADD(new AuditType("group", "addGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * update group
   */
  GROUP_UPDATE(new AuditType("group", "updateGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * delete group
   */
  GROUP_DELETE(new AuditType("group", "deleteGroup", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * stem add
   */
  STEM_ADD(new AuditType("stem", "addStem", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * stem update
   */
  STEM_UPDATE(new AuditType("stem", "updateStem", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * stem delete
   */
  STEM_DELETE(new AuditType("stem", "deleteStem", null, "id", "name", "parentStemId", 
      "displayName", "description")),
  
  /**
   * member change subject
   */
  MEMBER_CHANGE_SUBJECT(new AuditType("member", "changeSubject", null, "oldMemberId", 
      "oldSubjectId", "oldSourceId", "newMemberId", "newSubjectId", "newSourceId", "deleteOldMember", "memberIdChanged")),
  
  /**
   * import from xml
   */
  XML_IMPORT(new AuditType("importExport", "import", null, "fileName", "subjectId")),
  
  /**
   * copy a group to another stem
   */
  GROUP_COPY(new AuditType("group", "copy", "attributes", "oldGroupId", "oldGroupName", "newGroupId", "newGroupName", 
      "privilegesOfGroup", "groupAsPrivilege", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a group to another stem
   */
  GROUP_MOVE(new AuditType("group", "move", null, "groupId", "oldGroupName", "newGroupName", "newStemId", 
      "assignAlternateName")),
  
  /**
   * copy a stem to another stem
   */
  STEM_COPY(new AuditType("stem", "copy", "attributes", "oldStemId", "oldStemName", "newStemName", "newStemId", 
      "privilegesOfStem", "privilegesOfGroup", "listMembersOfGroup", "listGroupAsMember")),
  
  /**
   * move a stem to another stem
   */
  STEM_MOVE(new AuditType("stem", "move", null, "stemId", "oldStemName", "newStemName", "newParentStemId", 
      "assignAlternateName"));
  
  /**
   * defaults for audit type, though doesnt hold the id
   */
  private AuditType internalAuditTypeDefault;
  
  /**
   * construct
   * @param theInternalAuditTypeDefault 
   */
  private AuditTypeBuiltin(AuditType theInternalAuditTypeDefault) {
    this.internalAuditTypeDefault = theInternalAuditTypeDefault;
  }
  
  /**
   * get the audit type from the enum
   * @return the audit type
   */
  public AuditType getAuditType() {
    return AuditTypeFinder.find(this.internalAuditTypeDefault.getAuditCategory(), 
        this.internalAuditTypeDefault.getActionName(), true);
  }

  /**
   * get the defaults, but not the id
   * @return the defaults
   */
  public AuditType internal_auditTypeDefault() {
    return this.internalAuditTypeDefault;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getAuditCategory()
   */
  public String getAuditCategory() {
    return this.getAuditType().getAuditCategory();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.audit.AuditTypeIdentifier#getActionName()
   */
  public String getActionName() {
    return this.getAuditType().getActionName();
  }
  
  /**
   * 
   */
  public static void internal_clearCache() {
    
    //set this to -1 so it will be an insert next time
    for (AuditTypeBuiltin auditTypeBuiltin : AuditTypeBuiltin.values()) {
      auditTypeBuiltin.internalAuditTypeDefault.setHibernateVersionNumber(-1l);
    }
  }
}
