/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.common.treetable;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * <p>
 * The <code>NodeUserObject</code> represents a nodes user object. This
 * particular IceUserobject implementation stores extra information on how many
 * times the parent node is clicked on. It is also responsible for copying and
 * deleting its self.
 * </p>
 * <p/>
 * <p>
 * In this example pay particularly close attention to the <code>wrapper</code>
 * instance variable on IceUserObject. The <code>wrapper</code> allows for
 * direct manipulations of the parent tree.
 * </p>
 * 
 * @since 1.0
 */
public abstract class NodeUserObject extends TreeTableUserObject
{
	// treebean pointer - used to store selected node
	protected TreeTableBean treeBean;

	protected String label;
	
	// message bundle references
	private static String nodeToolTip;

	// index of the component type
	protected Integer componentType = new Integer(1);

	/**
	 * Creates a new <code>NodeUserObject</code> object. Default image states
	 * are set as well as the expansion of all branch leafs.
	 * @param wrapper parent tree node which wrapps this object
	 * @param treeBeanPointer callback to parent TreeBean class.
	 */
	public NodeUserObject(TreeTable treeTable, TreeTableNode node,
			TreeTableBean treeBeanPointer, int componenttype)
	{
		super(treeTable, node);

		treeBean = treeBeanPointer;
		this.componentType = componenttype;

		setTooltip(nodeToolTip);
		label = generateLabel();
		setExpanded(false);
	}

	public abstract String getLine1Text();
	public abstract String getLine2Text();
	    
	/**
	 * Returns true if this node is the root of the tree. The root is the only
	 * node in the tree with a null parent; every tree has exactly one root.
	 * @return true if this node is the root of its tree.
	 */
	public boolean isRootNode()
	{
		return getWrapper().isRoot();
	}

	/**
	 * Deletes this not from the parent tree.
	 * @param event that fired this method
	 */
	public void deleteNode(ActionEvent event)
	{
		((DefaultMutableTreeNode) getWrapper().getParent())
				.remove(getWrapper());
	}

	/**
	 * Copies this node and adds it as a child node.
	 * @return the newly created node copy.
	 */
	public NodeUserObject copyNode()
	{
		TreeTableNode clonedWrapper = new TreeTableNode();
		NodeUserObject originalUserObject = (NodeUserObject) getWrapper()
				.getUserObject();
		NodeUserObject clonedUserObject;
		
		try
		{
			clonedUserObject = (NodeUserObject)clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
		
		clonedUserObject.setAction(originalUserObject.getAction());
		clonedUserObject.setBranchContractedIcon(originalUserObject
				.getBranchContractedIcon());
		clonedUserObject.setBranchExpandedIcon(originalUserObject
				.getBranchExpandedIcon());
		clonedUserObject.setExpanded(originalUserObject.isExpanded());
		clonedUserObject.setLeafIcon(originalUserObject.getLeafIcon());
		clonedUserObject.setTooltip(nodeToolTip);
		clonedWrapper.setUserObject(clonedUserObject);
		getWrapper().insert(clonedWrapper, 0);

		return clonedUserObject;
	}

	/**
	 * Returns the label associated with the node.
	 * @return the label of the node
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Registers a user click with this object and updates the selected node in
	 * the TreeBean.
	 * @param event that fired this method
	 */
	public void nodeClicked(ActionEvent event)
	{
		treeBean.setSelectedNodeLabel(this.label);
		treeBean.setSelectedNodeObject((NodeUserObject) this);
		this.setExpanded(!this.isExpanded());
		treeTable.rebuildList();
	}

	/**
	 * Gets the component type.
	 * @return the component type
	 */
	public Integer getComponentType()
	{
		return componentType;
	}

	/**
	 * Sets the component type.
	 * @param componentType the new component type
	 */
	public void setComponentType(Integer componentType)
	{
		this.componentType = componentType;
	}

	/**
	 * generates default names for the value attribute depending on the component type
	 * @return the string value
	 */
	public String generateValues()
	{
		return "label";
	}

	public boolean getDraggable()
	{
		return false;
	}

	/**
	 * Generates a label for the node based on an incrementing int.
	 * @return the generated label (eg. 'Node 5')
	 */
	private String generateLabel()
	{
		return "Node " + treeBean.getIncreasedLabelCount();
	}
}