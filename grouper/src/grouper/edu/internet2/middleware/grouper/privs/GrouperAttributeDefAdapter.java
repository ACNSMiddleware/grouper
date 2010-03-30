/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.privs;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HqlQuery;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * <pre> 
 * Grouper Attribute Definition Access Privilege interface.
 * <p>
 * Unless you are implementing a new implementation of this interface,
 * you should not need to directly use these methods as they are all
 * wrapped by methods in the {@link AttributeDef} class.
 * </p>
 * This access adapter affects the HQL queries to give better performance
 * 
 * </pre>
 * @author  blair christensen.
 * @version $Id: GrouperAttributeDefAdapter.java,v 1.1 2009-09-21 06:14:26 mchyzer Exp $
 */
public class GrouperAttributeDefAdapter extends GrouperNonDbAttrDefAdapter {

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.BaseAttrDefAdapter#hqlFilterAttrDefsWhereClause(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.hibernate.HqlQuery, java.lang.StringBuilder, java.lang.StringBuilder, java.lang.String, java.util.Set)
   */
  @Override
  public boolean hqlFilterAttrDefsWhereClause(GrouperSession grouperSession,
      Subject subject, HqlQuery hqlQuery, StringBuilder hqlTables, StringBuilder hqlWhereClause, String attributeDefColumn,
      Set<Privilege> privInSet) {
    //no privs no filter
    if (GrouperUtil.length(privInSet) == 0) {
      return false;
    }
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    Member allMember = MemberFinder.internal_findAllMember();

    Collection<String> attrDefPrivs = GrouperPrivilegeAdapter.fieldIdSet(priv2list, privInSet); 
    String attrDefInClause = HibUtils.convertToInClause(attrDefPrivs, hqlQuery);
    
    String columnAlias = "__attrDefMembership" + GrouperUtil.uniqueId();
    
    //if not, we need an in clause
    hqlTables.append( ", MembershipEntry " + columnAlias);
    if (hqlWhereClause.length() != 0) {
      hqlWhereClause.append(" and ");
    }
    hqlWhereClause.append(columnAlias + ".ownerAttrDefId = " + attributeDefColumn
        + " and " + columnAlias + ".fieldId in (");
    hqlWhereClause.append(attrDefInClause).append(") and " + columnAlias + ".memberUuid in (");
    Set<String> memberIds = GrouperUtil.toSet(allMember.getUuid());
    if (member != null) {
      memberIds.add(member.getUuid());
    }
    String memberInClause = HibUtils.convertToInClause(memberIds, hqlQuery);
    hqlWhereClause.append(memberInClause).append(")");

    // don't return disabled memberships
    hqlWhereClause.append(" and " + columnAlias + ".enabledDb = 'T'");
    return true;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#postHqlFilterAttributeAssigns(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set)
   */
  @Override
  public Set<AttributeAssign> postHqlFilterAttributeAssigns(GrouperSession grouperSession,
      Subject subject, Set<AttributeAssign> attributeAssigns) {
    return attributeAssigns;
  }

}

