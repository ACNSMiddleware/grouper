/*--
$Id: TreeNodeImpl.java,v 1.16 2007-05-23 19:15:20 ddonn Exp $
$Date: 2007-05-23 19:15:20 $
 
Copyright 2006 Internet2, Stanford University

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
package edu.internet2.middleware.signet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;

public class TreeNodeImpl extends EntityImpl implements TreeNode, Comparable
{
  private TreeImpl tree;
  private String   treeId;
  private Set      parents;
  private Set      children;
  private boolean  parentsAlreadyFetched  = false;
  private boolean  childrenAlreadyFetched = false;

  /**
   * 
   */
  public TreeNodeImpl()
  {
    super();
    this.parents = new HashSet();
    this.children = new HashSet();
    this.setStatus(Status.PENDING);
  }

  public TreeNodeImpl(Signet signet, Tree tree, String id, String name)
  {
    super(signet, id, name, Status.ACTIVE);
    this.tree = (TreeImpl) tree;
    this.treeId = this.tree.getId();
    this.parents = new HashSet();
    this.children = new HashSet();
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#getParents()
   */
  /**
   * TODO - Hibernate requires that getters and setters for collections
   * return the EXACT SAME collection, not just an identical one. Failure
   * to do this makes Hibernate think that the collection has been modified,
   * and causes the entire collection to be re-persisted in the database.
   * 
   * I need to find some way to tell Hibernate to use a specific non-public
   * getter, so that the public getter can resume returning a non-modifiable
   * copy of the collection. 
   */
  public Set getParents()
  {
    if ( !parentsAlreadyFetched)
    {
    	Signet mySignet = getSignet();
    	if (null != mySignet)
    	{
    		HibernateDB hibr = mySignet.getPersistentDB();
    		if (null != hibr)
    		{
    			parents = hibr.getParents(this);
    			parentsAlreadyFetched = true;
    		}
    	}
    }

    return (parents);
    // return UnmodifiableSet.decorate(this.parents);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#getChildren()
   */
  public Set getChildren()
  {
    return (getChildren(null));
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#getChildren()
   */
  /**
   * TODO - Hibernate requires that getters and setters for collections
   * return the EXACT SAME collection, not just an identical one. Failure
   * to do this makes Hibernate think that the collection has been modified,
   * and causes the entire collection to be re-persisted in the database.
   * 
   * I need to find some way to tell Hibernate to use a specific non-public
   * getter, so that the public getter can resume returning a non-modifiable
   * copy of the collection. 
   */
  public Set getChildren(Session hs)
  {
    if ( !childrenAlreadyFetched)
    {
      children = getSignet().getPersistentDB().getChildren(hs, this);
      childrenAlreadyFetched = true;
    }

    return (children);
    // return UnmodifiableSet.decorate(this.children);
  }

  /**
   * @return Returns the tree.
   */
  public Tree getTree()
  {
    if ((tree == null) && (treeId != null) && (getSignet() != null))
    {
      try
      {
		HibernateDB hibr = getSignet().getPersistentDB();
		Session hs = hibr.openSession();
		tree = (TreeImpl)(hibr.getTree(hs, treeId));
		hibr.closeSession(hs);
      }
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
    }

    return this.tree;
  }

  public String getTreeId()
  {
    return this.treeId;
  }

  /**
   * @param tree The tree to set.
   */
  void setTree(Tree tree)
  {
    this.tree = (TreeImpl) tree;
    this.treeId = tree.getId();
  }

  void setTreeId(String treeId) throws ObjectNotFoundException
  {
    this.treeId = treeId;

    Signet signet;
    if (null != (signet = getSignet()))
    {
    	HibernateDB hibr = signet.getPersistentDB();
    	Session hs = hibr.openSession();
      tree = (TreeImpl)(hibr.getTree(hs, treeId));
      	hibr.closeSession(hs);
    }
  }

  /**
   * @param children The children to set.
   */
  void setChildren(Set children)
  {
    this.children = children;

    Iterator childrenIterator = children.iterator();
    while (childrenIterator.hasNext())
    {
      TreeNode child = (TreeNode) (childrenIterator.next());
      // Now, save the parent-child relationship info in the db.
      this.addChild(child);
    }
  }

  /**
   * @param parents The parents to set.
   */
  void setParents(Set parents)
  {
    this.parents = parents;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#addChild(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public void addChild(TreeNode childNode)
  {
    this.children.add(childNode);
    ((TreeNodeImpl) childNode).parents.add(this);
    saveRelationship(childNode, this);
  }

  private void saveRelationship(TreeNode childNode, TreeNode parentNode)
  {
    TreeNodeRelationship tnr = new TreeNodeRelationship(
    		childNode.getTree().getId(),
    		childNode.getId(),
    		parentNode.getId());

    HibernateDB hibr = getSignet().getPersistentDB();
    Session hs = hibr.openSession();
    Transaction tx = hs.beginTransaction();
    hibr.save(hs, tnr);
    tx.commit();
    hibr.closeSession(hs);
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#isAncestorOf(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public boolean isAncestorOf(TreeNode possibleDescendant)
  {
    // If we can find the possibleDescendant in this node's subtree, then
    // this node is an ancestor of that possibleDescendant.

    boolean foundDescendant = false;

    if (possibleDescendant == null)
    {
      // No one can be an ancestor of a NULL node.
      throw new IllegalArgumentException(
          "It is illegal to inquire whether a NULL TreeNode"
              + " is the descendant of some other node.");
    }

    if (getChildren().contains(possibleDescendant))
    {
      foundDescendant = true;
    }
    else
    {
      Iterator childrenIterator = children.iterator();

      while (childrenIterator.hasNext() && ( !foundDescendant))
      {
    	  TreeNodeImpl child = (TreeNodeImpl)childrenIterator.next();
        foundDescendant = child.isAncestorOf(possibleDescendant);
      }
    }

    return foundDescendant;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#isAncestorOf(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public boolean isAncestorOf(Session hs, TreeNode possibleDescendant)
  {
    // If we can find the possibleDescendant in this node's subtree, then
    // this node is an ancestor of that possibleDescendant.

    boolean foundDescendant = false;

    if (possibleDescendant == null)
    {
      // No one can be an ancestor of a NULL node.
      throw new IllegalArgumentException(
          "It is illegal to inquire whether a NULL TreeNode"
              + " is the descendant of some other node.");
    }

    if (getChildren(hs).contains(possibleDescendant))
    {
      foundDescendant = true;
    }
    else
    {
      Iterator childrenIterator = children.iterator();

      while (childrenIterator.hasNext() && ( !foundDescendant))
      {
    	  TreeNodeImpl child = (TreeNodeImpl)childrenIterator.next();
		  foundDescendant = child.isAncestorOf(hs, possibleDescendant);
      }
    }

    return foundDescendant;
  }


  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.tree.TreeNode#isDescendantOf(edu.internet2.middleware.signet.tree.TreeNode)
   */
  public boolean isDescendantOf(TreeNode possibleAncestor)
  {
    return possibleAncestor.isAncestorOf(this);
  }

  public boolean equals(Object o)
  {
    if (!(o instanceof TreeNodeImpl))
    {
      return false;
    }

    TreeNodeImpl rhs = (TreeNodeImpl) o;
    return new EqualsBuilder().append(this.getTreeId(), rhs.getTreeId())
        .append(this.getId(), rhs.getId()).isEquals();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    // you pick a hard-coded, randomly chosen, non-zero, odd number
    // ideally different for each class
    return new HashCodeBuilder(17, 37).append(this.getTreeId()).append(
        this.getId()).toHashCode();
  }

  public String toString()
  {
    return this.getTree().getAdapter().getClass().getName()
           + SignetFactory.SCOPE_PART_DELIMITER + this.getTreeId()
           + SignetFactory.SCOPE_PART_DELIMITER + this.getId();
  }

  /**
   * @param treeNodes A Set of TreeNodes
   * @return True if this node is an ancestor of all nodes in the set
   */
  public boolean isAncestorOfAll(Set treeNodes)
  {
	boolean retval = true; // assume success

	if ((null == treeNodes) || (0 >= treeNodes.size()))
		retval = false;
	else
	{
		for (Iterator treeNodesIterator = treeNodes.iterator();
				treeNodesIterator.hasNext() && retval; )
		{
			TreeNodeImpl otherTreeNode = (TreeNodeImpl)treeNodesIterator.next();
			retval = this.getTreeId().equals(otherTreeNode.getTreeId()) &&
					this.isAncestorOf(otherTreeNode);
		}
	}

    return (retval);
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o)
  {
    String thisName = null;
    String otherName = null;

    thisName = this.getName();
    otherName = ((TreeNode) o).getName();

    return thisName.compareToIgnoreCase(otherName);
  }

  /* This method exists only for use by Hibernate.
   */
  public TreeNodeFullyQualifiedId getFullyQualifiedId()
  {
    return new TreeNodeFullyQualifiedId(this.getTreeId(), this.getId());
  }

  /*
   * This method exists only for use by Hibernate.
   */
  void setFullyQualifiedId(TreeNodeFullyQualifiedId tnfqId) throws ObjectNotFoundException
  {
    setTreeId(tnfqId.getTreeId());
    setId(tnfqId.getTreeNodeId());
  }
  
  public String getId()
  {
    return super.getStringId();
  }
  
  // This method is only for use by Hibernate.
  private void setId(String id)
  {
    super.setStringId(id);
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.Entity#inactivate()
   */
  public void inactivate()
  {
    throw new UnsupportedOperationException
      ("This method is not yet implemented");
  }

}
