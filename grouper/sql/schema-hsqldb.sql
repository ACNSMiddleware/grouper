-- 
-- Create required Grouper tables
-- $Id: schema-hsqldb.sql,v 1.6 2004-08-06 18:22:40 blair Exp $
-- 

-- XXX Right types?
-- XXX Do I need a unique constraint on groupField?
CREATE TABLE grouper_fields (
  groupField  VARCHAR(255) NOT NULL PRIMARY KEY,
  readPriv    VARCHAR(255),
  writePriv   VARCHAR(255),
  isList      VARCHAR(255)
);

CREATE TABLE grouper_group (
  groupKey      VARCHAR(255) NOT NULL PRIMARY KEY,
  compoundExpr  VARCHAR(255),
  flattenedExpr VARCHAR(255),
  CONSTRAINT    uniq_gk UNIQUE (groupKey)
);

CREATE TABLE grouper_members (
  memberID        VARCHAR(255) NOT NULL PRIMARY KEY,
  presentationID  VARCHAR(255),
  CONSTRAINT      uniq_mi  UNIQUE (memberID),
  CONSTRAINT      uniq_pi  UNIQUE (presentationID)
);

CREATE TABLE grouper_session (
  sessionID  VARCHAR(255) NOT NULL PRIMARY KEY,
  cred       VARCHAR(255),
  startTime  BIGINT,
  CONSTRAINT uniq_si  UNIQUE (sessionID)
);

CREATE TABLE grouper_typeDefs (
  -- TOOD Foreign Key
  groupType   INTEGER NOT NULL,
  -- TOOD Foreign Key
  groupField  VARCHAR(255) NOT NULL,
  CONSTRAINT  uniq_gt_gf  UNIQUE (groupType, groupField)
);

CREATE TABLE grouper_types (
  groupType   INTEGER NOT NULL PRIMARY KEY,
  CONSTRAINT  uniq_gt UNIQUE (groupType)
);

