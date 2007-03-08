--
-- This is the HSQL DDL for the Signet database
--
-- $Header: /home/hagleyj/i2mi/signet/sql/hsql.sql,v 1.41 2007-03-08 07:10:01 lmcrae Exp $
--

-- Tree tables
drop table signet_treeNodeRelationship if exists;
drop table signet_treeNode if exists;
drop table signet_tree if exists;
-- ChoiceSet tables
drop table signet_choice if exists;
drop table signet_choiceSet if exists;
-- Assignment tables
drop table signet_assignmentLimit if exists;
drop table signet_assignment if exists;
drop table signet_assignmentLimit_history if exists;
drop table signet_assignment_history if exists;
drop table signet_proxy if exists;
drop table signet_proxy_history if exists;
-- Subsystem tables
drop table signet_permission_limit if exists;
drop table signet_function_permission if exists;
drop table signet_category if exists;
drop table signet_function if exists;
drop table signet_permission if exists;
drop table signet_limit if exists;
drop table signet_subsystem if exists;
-- Signet Subject table
drop table signet_subjectAttribute if exists;
drop table signet_subject if exists;
-- Local Source Subject tables (optional)
drop table SubjectAttribute if exists;
drop table Subject if exists;
drop table SubjectType if exists;
--
-- Subsystem tables
create table signet_subsystem
(
subsystemID         varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            varchar(2000)       NOT NULL,
scopeTreeID         varchar(64)         NULL,
modifyDatetime      datetime            NOT NULL,
primary key (subsystemID)
)
;
create table signet_category
(
categoryKey         int                 NOT NULL IDENTITY,
subsystemID         varchar(64)         NOT NULL,
categoryID          varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (categoryKey),
unique (subsystemID, categoryID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_function
(
functionKey         int                 NOT NULL IDENTITY,
subsystemID         varchar(64)         NOT NULL,
functionID          varchar(64)         NOT NULL,
categoryKey         int                 NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            varchar(2000)       NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (functionKey),
unique (subsystemID, functionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_permission
(
permissionKey		int                 NOT NULL IDENTITY,
subsystemID         varchar(64)         NOT NULL,
permissionID        varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (permissionKey),
unique (subsystemID, permissionID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_limit
(
limitKey			int                 NOT NULL IDENTITY,
subsystemID         varchar(64)         NOT NULL,
limitID             varchar(64)         NOT NULL,
status              varchar(16)         NOT NULL,
limitType           varchar(16)         NOT NULL,
limitTypeID         varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
helpText            varchar(2000)       NULL,
dataType            varchar(32)         NOT NULL,
valueType           varchar(32)         NOT NULL,
displayOrder        smallint            NOT NULL,
renderer            varchar(255)        NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (limitKey),
unique (subsystemID, limitID),
foreign key (subsystemID) references signet_subsystem (subsystemID)
)
;
create table signet_function_permission
(
functionKey         int                 NOT NULL,
permissionKey       int                 NOT NULL,
primary key (functionKey, permissionKey),
foreign key (functionKey) references signet_function (functionKey),
foreign key (permissionKey) references signet_permission (permissionKey)
)
;
create table signet_permission_limit
(
permissionKey       int                 NOT NULL,
limitKey            int                 NOT NULL,
defaultLimitValueValue  varchar(64)     NULL,
primary key (permissionKey, limitKey),
foreign key (permissionKey) references signet_permission (permissionKey),
foreign key (limitKey) references signet_limit (limitKey)
)
;
--
-- Signet Subject tables
--
create table signet_subject (
subjectKey          bigint              NOT NULL IDENTITY,
sourceID            varchar(64)         NOT NULL,
subjectID           varchar(64)         NOT NULL,
type                varchar(32)         NOT NULL,
name                varchar(255)        NOT NULL,
modifyDatetime      datetime            NOT NULL,
syncDatetime        datetime            NOT NULL,
primary key (subjectKey),
unique (sourceID, subjectID)
)
;
create table signet_subjectAttribute (
subjectAttributeKey  bigint             NOT NULL,
subjectKey           bigint             NOT NULL,
attributeName        varchar(31)        NOT NULL,
attributeSequence    integer            NOT NULL,
attributeValue       varchar(255)       NOT NULL,
attributeType        varchar(31)        DEFAULT 'string',
modifyDatetime       timestamp          NOT NULL,
primary key (subjectAttributeKey),
foreign key (subjectKey)
    references signet_subject(subjectKey) ON DELETE CASCADE,
unique (subjectKey, attributeName, attributeSequence)
)
;

-- create sequence subjectAttrValueSerial START 1;

create table signet_subjectAttrValue (
subjectAttrValueKey		bigint			NOT NULL,
subjectAttrKey			bigint			NOT NULL,
sequence				bigint			NOT NULL,
value					varchar(255)	NOT NULL,
type					varchar(255)	DEFAULT 'string',
primary key (subjectAttrValueKey),
foreign key (subjectAttrKey)
references signet_subjectAttribute(subjectAttrKey) ON DELETE CASCADE,
unique (subjectAttrKey, sequence)
)
;

--
-- Tree tables
--
create table signet_tree
(
treeID              varchar(64)         NOT NULL,
name                varchar(120)        NOT NULL,
adapterClass        varchar(255)        NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (treeID)
)
;
create table signet_treeNode
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
nodeType            varchar(32)         NOT NULL,
status              varchar(16)         NOT NULL,
name                varchar(120)        NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (treeID, nodeID),
foreign key (treeID) references signet_tree(treeID)
)
;
create table signet_treeNodeRelationship
(
treeID              varchar(64)         NOT NULL,
nodeID              varchar(64)         NOT NULL,
parentNodeID        varchar(64)         NOT NULL,
primary key (treeID, nodeID, parentNodeID),
foreign key (treeID) references signet_tree (treeID),
foreign key (treeID, nodeID) references signet_treeNode (treeID, nodeID),
foreign key (treeID, parentNodeID) references signet_treeNode(treeID, nodeID)
)
;
--
-- ChoiceSet tables
--
create table signet_choiceSet
(
choiceSetKey		int          NOT NULL IDENTITY,
choiceSetID         varchar(64)  NOT NULL,
adapterClass        varchar(255) NOT NULL,
subsystemID         varchar(64)  NULL,
modifyDatetime      datetime     NOT NULL,
primary key (choiceSetKey),
unique (choiceSetId, subsystemID)
)
;
create table signet_choice
(
choiceKey			int          NOT NULL IDENTITY,
choiceSetKey        int          NOT NULL,
value               varchar(32)  NOT NULL,
label               varchar(64)  NOT NULL,
rank                smallint     NOT NULL,
displayOrder        smallint     NOT NULL,
modifyDatetime      datetime     NOT NULL,
primary key (choiceKey),
unique (choiceSetKey, value),
foreign key (choiceSetKey) references signet_choiceSet (choiceSetKey)
)
;
--
-- Assignment tables
--
create table signet_assignment
(
assignmentID        int                 NOT NULL IDENTITY,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
functionKey         int                 NOT NULL,
grantorKey          int                 NOT NULL,
granteeKey          int                 NOT NULL,
proxyKey            int                 NULL,
revokerKey          int                 NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canUse              bit                 NOT NULL,
canGrant            bit                 NOT NULL,
effectiveDate       datetime            NOT NULL,
expirationDate      datetime            NULL,
modifyDatetime      datetime            NOT NULL,
primary key (assignmentID),
foreign key (grantorKey) references signet_subject (subjectKey),
foreign key (granteeKey) references signet_subject (subjectKey),
foreign key (proxyKey) references signet_subject (subjectKey),
foreign key (revokerKey) references signet_subject (subjectKey),
foreign key (functionKey) references signet_function (functionKey)
)
;
create index signet_assignment_1
on signet_assignment (
  grantorKey
)
;
create index signet_assignment_2
on signet_assignment (
  granteeKey
)
;
create index signet_assignment_3
on signet_assignment (
  effectiveDate
)
;
create index signet_assignment_4
on signet_assignment (
  expirationDate
)
;
create table signet_assignmentLimit
(
assignmentID        int                 NOT NULL,
limitKey    		int                 NOT NULL,
value               varchar(32)         NOT NULL,
unique (assignmentID, limitKey, value),
foreign key (assignmentID) references signet_assignment (assignmentID),
foreign key (limitKey) references signet_limit (limitKey)
)
;
create table signet_assignment_history
(
historyID           int                 NOT NULL IDENTITY,
assignmentID        int                 NOT NULL,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
functionKey         int                 NOT NULL,
grantorKey          int                 NOT NULL,
granteeKey          int                 NOT NULL,
proxyKey            int                 NULL,
revokerKey          int                 NULL,
scopeID             varchar(64)         NULL,
scopeNodeID         varchar(64)         NULL,
canUse              bit                 NOT NULL,
canGrant            bit                 NOT NULL,
effectiveDate       datetime            NOT NULL,
expirationDate      datetime            NULL,
historyDatetime     datetime            NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (historyID),
unique (assignmentID, instanceNumber),
foreign key (grantorKey) references signet_subject (subjectKey),
foreign key (granteeKey) references signet_subject (subjectKey),
foreign key (proxyKey) references signet_subject (subjectKey),
foreign key (revokerKey) references signet_subject (subjectKey)
)
;
create index signet_assignment_history_1
on signet_assignment_history (
  grantorKey
)
;
create index signet_assignment_history_2
on signet_assignment_history (
  granteeKey
)
;
create table signet_assignmentLimit_history
(
  assignment_historyID int          NOT NULL,
  limitKey             int          NOT NULL,
  value                varchar(32)  NOT NULL,
  unique      (assignment_historyID, limitKey, value),
  foreign key (assignment_historyID)
    references signet_assignment_history
      (historyID),
  foreign key (limitKey)
    references signet_limit
      (limitKey)
)
;
create table signet_proxy
(
proxyID             int                 NOT NULL IDENTITY,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NULL,
grantorKey          int                 NOT NULL,
granteeKey          int                 NOT NULL,
proxySubjectKey     int                 NULL,
canUse              bit                 NOT NULL,
canExtend           bit                 NOT NULL,
effectiveDate       datetime            NOT NULL,
expirationDate      datetime            NULL,
revokerKey          int                 NULL,
modifyDatetime      datetime            NOT NULL,
primary key (proxyID),
foreign key (grantorKey) references signet_subject (subjectKey),
foreign key (granteeKey) references signet_subject (subjectKey),
foreign key (proxySubjectKey) references signet_subject (subjectKey),
foreign key (revokerKey) references signet_subject (subjectKey)
)
;
create table signet_proxy_history
(
historyID           int                 NOT NULL IDENTITY,
proxyID             int                 NOT NULL,
instanceNumber      int                 NOT NULL,
status              varchar(16)         NOT NULL,
subsystemID         varchar(64)         NULL,
grantorKey          int                 NOT NULL,
granteeKey          int                 NOT NULL,
proxySubjectKey		int                 NULL,
canUse              bit                 NOT NULL,
canExtend           bit                 NOT NULL,
effectiveDate       datetime            NOT NULL,
expirationDate      datetime            NULL,
revokerKey          int                 NULL,
historyDatetime     datetime            NOT NULL,
modifyDatetime      datetime            NOT NULL,
primary key (historyID),
unique (proxyID, instanceNumber),
foreign key (grantorKey) references signet_subject (subjectKey),
foreign key (granteeKey) references signet_subject (subjectKey),
foreign key (proxySubjectKey) references signet_subject (subjectKey),
foreign key (revokerKey) references signet_subject (subjectKey)
)
;
--
-- Subject tables (optional, for local subject tables)
--
create table Subject (
  subjectTypeID     varchar(32)     NOT NULL,
  subjectID         varchar(64)     NOT NULL,
  name              varchar(120)    NOT NULL,
  description       varchar(255)    NOT NULL,
  displayId         varchar(64)     NOT NULL,
  modifyDatetime    datetime        NOT NULL,
  primary key (subjectTypeID, subjectID)
)
;
create table SubjectAttribute (
  subjectTypeID     varchar(32)     NOT NULL,
  subjectID         varchar(64)     NOT NULL,
  name              varchar(32)     NOT NULL,
  instance          smallint        NOT NULL,
  value             varchar(255)    NOT NULL,
  searchValue       varchar(255)    NOT NULL,
  modifyDatetime    datetime        NOT NULL,
  primary key (subjectTypeID, subjectID, name, instance),
  foreign key (subjectTypeID, subjectID)
    references Subject (subjectTypeID, subjectID)
)
;
create index SubjectAttribute_1
on SubjectAttribute (
  subjectID,
  name,
  value
)
;
