/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: LoaderMemberWrapper.java,v 1.3 2009-08-12 04:52:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class LoaderMemberWrapper {

  /**
   * 
   */
  private String subjectId;
  
  /**
   * 
   */
  private String sourceId;
  
  /**
   * 
   */
  private Member member;

  /**
   * 
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * 
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * 
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * 
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * 
   * @return member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * 
   * @return member
   */
  public Member findOrGetMember() {
    if (this.member == null) {
      try {
        this.member = edu.internet2.middleware.grouper.misc.GrouperDAOFactory.getFactory().getMember().findBySubject(this.subjectId, this.sourceId, true);
      } catch (Exception e) {
        throw new RuntimeException("Problem with loader member wrapper: " + this.subjectId, e);
      }
    }
    return this.member;
  }

  /**
   * 
   * @return subject
   */
  public Subject findOrGetSubject() {
    try {
      if (this.member == null) {
          Subject subject = SubjectFinder.getSource(this.sourceId).getSubject(this.subjectId, true);
          return subject;
      }
      return this.member.getSubject();
    } catch (Exception e) {
      throw new RuntimeException("Problem with loader member wrapper: " + this.subjectId, e);
    }
  }

  /**
   * 
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }

  /**
   * @param subjectId
   * @param sourceId
   */
  public LoaderMemberWrapper(String subjectId, String sourceId) {
    this.subjectId = subjectId;
    this.sourceId = sourceId;
  }

  /**
   * @param member
   */
  public LoaderMemberWrapper(Member member) {
    this.member = member;
    this.subjectId = member.getSubjectIdDb();
    this.sourceId = member.getSubjectSourceIdDb();
  }
  
  
  
}
