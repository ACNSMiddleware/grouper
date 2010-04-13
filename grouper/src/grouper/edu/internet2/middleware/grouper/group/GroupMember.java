/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.group;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignEffMshipDelegate;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;


/**
 * holds a group and a member
 */
public class GroupMember implements AttributeAssignable {
  
  /**
   * @param group1
   * @param member1
   */
  public GroupMember(Group group1, Member member1) {
    super();
    this.group = group1;
    this.member = member1;
  }

  /**
   * 
   */
  public GroupMember() {
    //empty
  }
  
  /** group */
  private Group group;
  
  /** member */
  private Member member;

  /**
   * delegate for effective memberships
   * @return the delegate
   */
  public AttributeAssignEffMshipDelegate getAttributeDelegate() {
    return new AttributeAssignEffMshipDelegate(this.group, this.member);
  }
  

  
  /**
   * group
   * @return group
   */
  public Group getGroup() {
    return this.group;
  }

  /**
   * group
   * @param group1
   */
  public void setGroup(Group group1) {
    this.group = group1;
  }

  /**
   * member
   * @return subject
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * member
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }
  
  /**
   * this delegate works on attributes and values at the same time
   * @return the delegate
   */
  public AttributeValueDelegate getAttributeValueDelegate() {
    if (this.attributeValueDelegate == null) {
      this.attributeValueDelegate = new AttributeValueDelegate(this.getAttributeDelegate());
    }
    return this.attributeValueDelegate;
  }

  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeValueDelegate attributeValueDelegate;
  

}
