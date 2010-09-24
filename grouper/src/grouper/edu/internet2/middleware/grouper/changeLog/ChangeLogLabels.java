/*
 * @author mchyzer
 * $Id: ChangeLogLabels.java,v 1.5 2009-10-31 17:46:47 shilen Exp $
 */
package edu.internet2.middleware.grouper.changeLog;


/**
 *
 */
public class ChangeLogLabels {

  /**
   * label for group type add
   */
  public static enum GROUP_TYPE_ADD implements ChangeLogLabel {

    /** id of the group type */
    id, 
    
    /** name */
    name;
  };
  
  /** labels for a group type update */
  public static enum GROUP_TYPE_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,

    /** new value of the property */
    propertyNewValue;
  };
      
  /**
   * labels for group type delete
   */
  public static enum GROUP_TYPE_DELETE implements ChangeLogLabel {
    
    /** id of the group type */
    id,
    
    /** name */
    name;
  };

  /**
   * label for group add
   */
  public static enum GROUP_ADD implements ChangeLogLabel {

    /** id of the group */
    id, 
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** description */
    description;
  };
  
  /** labels for a group update */
  public static enum GROUP_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** display extension (not stored in col, but yes stored in diffs on update */
    displayExtension,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };
      
  /**
   * labels for group delete
   */
  public static enum GROUP_DELETE implements ChangeLogLabel {
    
    /** id of the group */
    id,
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId, 

    /** display name */
    displayName, 
    
    /** description */
    description;
  };
  
  /**
   * label for group field add
   */
  public static enum GROUP_FIELD_ADD implements ChangeLogLabel {

    /** id of the group type */
    id, 
    
    /** name */
    name,
    
    /** group type id */
    groupTypeId,
    
    /** group type name */
    groupTypeName, 
    
    /** type */
    type;
  };
  
  /** labels for a group field update */
  public static enum GROUP_FIELD_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** groupTypeId */
    groupTypeId,
    
    /** groupTypeName */
    groupTypeName, 
    
    /** type */
    type,
    
    /** readPrivilege */
    readPrivilege,
    
    /** writePrivilege */
    writePrivilege,
    
    /** isNullable */
    isNullable,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,

    /** new value of the property */
    propertyNewValue;
  };
  
  /**
   * labels for group field delete
   */
  public static enum GROUP_FIELD_DELETE implements ChangeLogLabel {
    
    /** id of the group type */
    id,
    
    /** name */
    name,
    
    /** groupTypeId */
    groupTypeId,
    
    /** groupTypeName */
    groupTypeName,
    
    /** type */
    type;
  
  };

  /**
   * label for attribute def add
   */
  public static enum ATTRIBUTE_DEF_ADD implements ChangeLogLabel {
  
    /** id of the attribute def */
    id, 
    
    /** name */
    name,
    
    /** stem id */
    stemId,
    
    /** description */
    description;
  }

  /** labels for a attribute def update */
  public static enum ATTRIBUTE_DEF_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** stem id */
    stemId,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };

  /**
   * labels for attribute def delete
   */
  public static enum ATTRIBUTE_DEF_DELETE implements ChangeLogLabel {
    
    /** id of the attribute def */
    id,
    
    /** name */
    name,
    
    /** stem id */
    stemId,
    
    /** description */
    description;
  };
  
  /**
   * label for stem add
   */
  public static enum STEM_ADD implements ChangeLogLabel {
  
    /** id of the stem */
    id, 
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** description */
    description;
  }

  /** labels for a stem update */
  public static enum STEM_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** name */
    name, 
    
    /** parent stem id */
    parentStemId,
    
    /** display name */
    displayName,
    
    /** display extension (not stored in col, but yes stored in diffs on update */
    displayExtension,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };

  /**
   * labels for stem delete
   */
  public static enum STEM_DELETE implements ChangeLogLabel {
    
    /** id of the stem */
    id,
    
    /** name */
    name,
    
    /** parent stem id */
    parentStemId, 
  
    /** display name */
    displayName, 
    
    /** description */
    description;
  };

  /**
   * 
   * label for membership add
   */
  public static enum MEMBERSHIP_ADD implements ChangeLogLabel {
  
    /** id of the stem */
    id, 
    
    /** fieldName */
    fieldName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** membershipType */
    membershipType,

    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId;
  };

  /** labels for a membership update */
  public static enum MEMBERSHIP_UPDATE implements ChangeLogLabel {
    
    /** id of the stem */
    id, 
    
    /** fieldName */
    fieldName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** membershipType */
    membershipType,

    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };

  /**
   * labels for membership delete
   */
  public static enum MEMBERSHIP_DELETE implements ChangeLogLabel {
    
    /** id of the stem */
    id, 
    
    /** fieldName */
    fieldName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** membershipType */
    membershipType,

    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId;
  };
  
  /**
   * labels for privilege add
   */
  public static enum PRIVILEGE_ADD implements ChangeLogLabel {
    
    /** id of membership */
    id, 
    
    /** privilegeName */
    privilegeName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** privilegeType */
    privilegeType,
    
    /** ownerType */
    ownerType,

    /** ownerId */
    ownerId,
    
    /** ownerName */
    ownerName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId,
    
    /** membershipType */
    membershipType;
  };
  
  /**
   * labels for privilege update
   */
  public static enum PRIVILEGE_UPDATE implements ChangeLogLabel {
    
    /** id of membership */
    id, 
    
    /** privilegeName */
    privilegeName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** privilegeType */
    privilegeType,
    
    /** ownerType */
    ownerType,

    /** ownerId */
    ownerId,
    
    /** ownerName */
    ownerName,
    
    /** membershipType */
    membershipType;
  };
  
  /**
   * labels for privilege delete
   */
  public static enum PRIVILEGE_DELETE implements ChangeLogLabel {
    
    /** id of membership */
    id, 
    
    /** privilegeName */
    privilegeName,
    
    /** subjectId */
    subjectId,
    
    /** sourceId */
    sourceId,
    
    /** privilegeType */
    privilegeType,
    
    /** ownerType */
    ownerType,

    /** ownerId */
    ownerId,
    
    /** ownerName */
    ownerName,
    
    /** member id */
    memberId,
    
    /** field id */
    fieldId,
    
    /** membershipType */
    membershipType;
  };
  
  /**
   * labels for member add
   */
  public static enum MEMBER_ADD implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** subjectId */
    subjectId, 
    
    /** subjectSourceId */
    subjectSourceId,
    
    /** subjectTypeId */
    subjectTypeId;
  };
  
  /**
   * labels for member delete
   */
  public static enum MEMBER_DELETE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** subjectId */
    subjectId, 
    
    /** subjectSourceId */
    subjectSourceId,
    
    /** subjectTypeId */
    subjectTypeId;
  };
  
  /**
   * labels for member update
   */
  public static enum MEMBER_UPDATE implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** subjectId */
    subjectId, 
    
    /** subjectSourceId */
    subjectSourceId,
    
    /** subjectTypeId */
    subjectTypeId,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  };
  
  /**
   * labels for group type assign
   */
  public static enum GROUP_TYPE_ASSIGN implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** typeId */
    typeId,
    
    /** typeName */
    typeName;
  };
  
  /**
   * labels for group type unassign
   */
  public static enum GROUP_TYPE_UNASSIGN implements ChangeLogLabel {
    
    /** id */
    id, 
    
    /** groupId */
    groupId,
    
    /** groupName */
    groupName,
    
    /** typeId */
    typeId,
    
    /** typeName */
    typeName;
  };
  
  /**
   * label for attribute assign action add
   */
  public static enum ATTRIBUTE_ASSIGN_ACTION_ADD implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** name */
    name,
    
    /** attribute def id */
    attributeDefId;
  }
  
  /**
   * label for attribute assign action update
   */
  public static enum ATTRIBUTE_ASSIGN_ACTION_UPDATE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** name */
    name,
    
    /** attribute def id */
    attributeDefId,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  }
  
  /**
   * label for attribute assign action delete
   */
  public static enum ATTRIBUTE_ASSIGN_ACTION_DELETE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** name */
    name,
    
    /** attribute def id */
    attributeDefId;
  }
  
  /**
   * label for attribute assign action set add
   */
  public static enum ATTRIBUTE_ASSIGN_ACTION_SET_ADD implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** type */
    type,
    
    /** ifHasAttrAssnActionId */
    ifHasAttrAssnActionId,
    
    /** thenHasAttrAssnActionId */
    thenHasAttrAssnActionId;
  }
  
  /**
   * label for attribute assign action set delete
   */
  public static enum ATTRIBUTE_ASSIGN_ACTION_SET_DELETE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** type */
    type,
    
    /** ifHasAttrAssnActionId */
    ifHasAttrAssnActionId,
    
    /** thenHasAttrAssnActionId */
    thenHasAttrAssnActionId;
  }
  
  /**
   * label for attribute def name set add
   */
  public static enum ATTRIBUTE_DEF_NAME_SET_ADD implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** type */
    type,
    
    /** ifHasAttributeDefNameId */
    ifHasAttributeDefNameId,
    
    /** thenHasAttributeDefNameId */
    thenHasAttributeDefNameId;
  }
  
  /**
   * label for attribute def name set delete
   */
  public static enum ATTRIBUTE_DEF_NAME_SET_DELETE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** type */
    type,
    
    /** ifHasAttributeDefNameId */
    ifHasAttributeDefNameId,
    
    /** thenHasAttributeDefNameId */
    thenHasAttributeDefNameId;
  }
  
  /**
   * label for role set add
   */
  public static enum ROLE_SET_ADD implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** type */
    type,
    
    /** ifHasRoleId */
    ifHasRoleId,
    
    /** thenHasRoleId */
    thenHasRoleId;
  }
  
  /**
   * label for role set delete
   */
  public static enum ROLE_SET_DELETE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** type */
    type,
    
    /** ifHasRoleId */
    ifHasRoleId,
    
    /** thenHasRoleId */
    thenHasRoleId;
  }
  
  /**
   * label for attribute def name add
   */
  public static enum ATTRIBUTE_DEF_NAME_ADD implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** attributeDefId */
    attributeDefId,
    
    /** name */
    name,
    
    /** stemId */
    stemId,
    
    /** description */
    description;
  }
  
  /**
   * label for attribute def name update
   */
  public static enum ATTRIBUTE_DEF_NAME_UPDATE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** attributeDefId */
    attributeDefId,
    
    /** name */
    name,
    
    /** stemId */
    stemId,
    
    /** description */
    description,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  }
  
  /**
   * label for attribute def name delete
   */
  public static enum ATTRIBUTE_DEF_NAME_DELETE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** attributeDefId */
    attributeDefId,
    
    /** name */
    name,
    
    /** stemId */
    stemId,
    
    /** description */
    description;
  }
  
  /**
   * label for attribute assign add
   */
  public static enum ATTRIBUTE_ASSIGN_ADD implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** attributeDefNameId */
    attributeDefNameId,
    
    /** attributeAssignActionId */
    attributeAssignActionId,
    
    /** assignType */
    assignType,
    
    /** ownerId1 */
    ownerId1,
    
    /** ownerId2 - if the assignType is any_mem, then ownerId1 is the groupId and ownerId2 is the memberId */
    ownerId2;
  }
  
  /**
   * label for attribute assign delete
   */
  public static enum ATTRIBUTE_ASSIGN_DELETE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** attributeDefNameId */
    attributeDefNameId,
    
    /** attributeAssignActionId */
    attributeAssignActionId,
    
    /** assignType */
    assignType,
    
    /** ownerId1 */
    ownerId1,
    
    /** ownerId2 - if the assignType is any_mem, then ownerId1 is the groupId and ownerId2 is the memberId */
    ownerId2;
  }
  
  /**
   * label for attribute assign update
   */
  public static enum ATTRIBUTE_ASSIGN_UPDATE implements ChangeLogLabel {
  
    /** id */
    id, 
    
    /** attributeDefNameId */
    attributeDefNameId,
    
    /** attributeAssignActionId */
    attributeAssignActionId,
    
    /** assignType */
    assignType,
    
    /** ownerId1 */
    ownerId1,
    
    /** ownerId2 - if the assignType is any_mem, then ownerId1 is the groupId and ownerId2 is the memberId */
    ownerId2,
    
    /** property which changed */
    propertyChanged, 
    
    /** old value of the property */
    propertyOldValue,
    
    /** new value of the property */
    propertyNewValue;
  }
}
