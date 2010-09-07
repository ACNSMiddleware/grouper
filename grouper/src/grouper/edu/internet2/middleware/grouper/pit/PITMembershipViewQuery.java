package edu.internet2.middleware.grouper.pit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;


/**
 * Point in time membership query
 * 
 * @author shilen
 * $Id$
 */
public class PITMembershipViewQuery {
  
  /**
   * query for memberships that started after this date
   */
  private Date startDateAfter = null;
  
  /**
   * query for memberships that started before this date
   */
  private Date startDateBefore = null;

  /**
   * query for memberships that ended after this date or have not ended yet
   */
  private Date endDateAfter = null;
  
  /**
   * query for memberships that ended before this date
   */
  private Date endDateBefore = null;
  
  /**
   * owner id
   */
  private String ownerId = null;

  /**
   * member id
   */
  private String memberId = null;
  
  /**
   * field id
   */
  private String fieldId = null;
  
  /**
   * query options
   */
  private QueryOptions queryOptions = null;

  /**
   * extra criteria
   */
  private Criterion extraCriterion;
  
  /**
   * extra criteria
   * @param extraCriterion
   * @return this for chaining
   */
  public PITMembershipViewQuery setExtraCriterion(Criterion extraCriterion) {
    this.extraCriterion = extraCriterion;
    return this;
  }

  /**
   * query for memberships that started after this date
   * @param startDateAfter
   * @return this for chaining
   */
  public PITMembershipViewQuery setStartDateAfter(Date startDateAfter) {
    this.startDateAfter = startDateAfter;
    return this;
  }
  
  /**
   * query for memberships that started before this date
   * @param startDateBefore
   * @return this for chaining
   */
  public PITMembershipViewQuery setStartDateBefore(Date startDateBefore) {
    this.startDateBefore = startDateBefore;
    return this;
  }

  /**
   * query for memberships that ended after this date or have not ended yet
   * @param endDateAfter
   * @return this for chaining
   */
  public PITMembershipViewQuery setEndDateAfter(Date endDateAfter) {
    this.endDateAfter = endDateAfter;
    return this;
  }
  
  /**
   * query for memberships that ended before this date
   * @param endDateBefore
   * @return this for chaining
   */
  public PITMembershipViewQuery setEndDateBefore(Date endDateBefore) {
    this.endDateBefore = endDateBefore;
    return this;
  }

  /**
   * query for memberships that were active at any point in the specified date range
   * @param fromDate
   * @param toDate
   * @return this for chaining
   */
  public PITMembershipViewQuery setActiveDateRange(Date fromDate, Date toDate) {
    this.startDateBefore = toDate;
    this.endDateAfter = fromDate;
    return this;
  }

  /**
   * query options
   * @return query options
   */
  public QueryOptions getQueryOptions() {
    return this.queryOptions;
  }

  /**
   * query options
   * @param queryOptions
   * @return this for chaining
   */
  public PITMembershipViewQuery setQueryOptions(QueryOptions queryOptions) {
    this.queryOptions = queryOptions;
    return this;
  }
  
  /**
   * query for memberships with this ownerId
   * @param ownerId
   * @return this for chaining
   */
  public PITMembershipViewQuery setOwnerId(String ownerId) {
    this.ownerId = ownerId;
    return this;
  }

  /**
   * query for memberships with this memberId
   * @param memberId
   * @return this for chaining
   */
  public PITMembershipViewQuery setMemberId(String memberId) {
    this.memberId = memberId;
    return this;
  }
  
  /**
   * query for memberships with this fieldId
   * @param fieldId
   * @return this for chaining
   */
  public PITMembershipViewQuery setFieldId(String fieldId) {
    this.fieldId = fieldId;
    return this;
  }

  /**
   * 
   * @return set of PITMembershipView objects
   */
  public Set<PITMembershipView> execute() {
    
    List<Criterion> criterionList = new ArrayList<Criterion>();
    
    if (this.ownerId != null) {
      criterionList.add(Restrictions.eq(PITMembershipView.FIELD_OWNER_ID, this.ownerId));
    }
    
    if (this.memberId != null) {
      criterionList.add(Restrictions.eq(PITMembershipView.FIELD_MEMBER_ID, this.memberId));
    }
    
    if (this.fieldId != null) {
      criterionList.add(Restrictions.eq(PITMembershipView.FIELD_FIELD_ID, this.fieldId));
    }
    
    if (this.startDateAfter != null) {
      criterionList.add(Restrictions.or(
          Expression.ge(PITMembershipView.FIELD_MEMBERSHIP_START_TIME_DB, this.startDateAfter.getTime() * 1000), 
          Expression.ge(PITMembershipView.FIELD_GROUP_SET_START_TIME_DB, this.startDateAfter.getTime() * 1000)));
    }
    
    if (this.startDateBefore != null) {
      criterionList.add(Expression.le(PITMembershipView.FIELD_MEMBERSHIP_START_TIME_DB, this.startDateBefore.getTime() * 1000));
      criterionList.add(Expression.le(PITMembershipView.FIELD_GROUP_SET_START_TIME_DB, this.startDateBefore.getTime() * 1000));
    }
    
    if (this.endDateAfter != null) {
      criterionList.add(Restrictions.or(
          Expression.isNull(PITMembershipView.FIELD_MEMBERSHIP_END_TIME_DB),
          Expression.ge(PITMembershipView.FIELD_MEMBERSHIP_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
      
      criterionList.add(Restrictions.or(
          Expression.isNull(PITMembershipView.FIELD_GROUP_SET_END_TIME_DB),
          Expression.ge(PITMembershipView.FIELD_GROUP_SET_END_TIME_DB, this.endDateAfter.getTime() * 1000)));
    }
    
    if (this.endDateBefore != null) {
      criterionList.add(Restrictions.or(
          Expression.le(PITMembershipView.FIELD_MEMBERSHIP_END_TIME_DB, this.endDateBefore.getTime() * 1000), 
          Expression.le(PITMembershipView.FIELD_GROUP_SET_END_TIME_DB, this.endDateBefore.getTime() * 1000)));
    }
    
    if (this.extraCriterion != null) {
      criterionList.add(this.extraCriterion);
    }
    
    Criterion allCriteria = HibUtils.listCrit(criterionList);
    
    Set<PITMembershipView> results = HibernateSession.byCriteriaStatic()
      .options(this.queryOptions).listSet(PITMembershipView.class, allCriteria);
    return results;
  }
}
