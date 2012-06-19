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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.table.IRowModel;

import com.icesoft.faces.component.dragdrop.DropEvent;
import com.icesoft.faces.component.tree.IceUserObject;

/**
 * 
 */
public abstract class TreeTableUserObject extends IceUserObject  implements IRowModel, Serializable
{
	private static final long serialVersionUID = 5767271638995730787L;

	protected TreeTable treeTable;

	/**
	 * @param treeTable
	 * @param node
	 */
	public TreeTableUserObject(TreeTable treeTable, TreeTableNode node)
	{
		super(node);
		this.treeTable = treeTable;
		init(node);
		setExpanded(false);
	}

   /**
    * Implement this iff you want to implement your own filtering mechanism
    * I.e. when Auto Filtering if Data Table is false 
    * @param dataFilters
    * @return true if this use entry i.e. row is filtered out
    */
    public abstract boolean isFilterOut(TableDataFilters dataFilters);
    
	@Override
	public TreeTableNode getWrapper()
	{
		return (TreeTableNode) this.wrapper;
	}

	/**
	 * @return
	 */
	public List<Object> getLevelList()
	{
		List<Object> list = new ArrayList<Object>();
		
		for (int i = 0; i < this.getWrapper().getLevel(); i++)
		{
			list.add(new Object());
		}

		return list;
	}

	public String toggleRow()
	{
		if (!this.leaf)
		{
			this.setExpanded(!expanded);
			treeTable.rebuildList();
		}
	
		return "";
	}

	public void nodeMoved(DropEvent evt)
	{
		TreeTableUserObject target = (TreeTableUserObject) evt
				.getTargetDragValue();
		if (null == target || this.getWrapper().isNodeAncestor(target.getWrapper()))
		{
			return;
		}

		target.getWrapper().removeFromParent();
		this.getWrapper().add(target.getWrapper());
		this.setExpanded(true);

		treeTable.rebuildList();
	}

	/**
	 * @param node
	 */
	private void init(DefaultMutableTreeNode node)
	{
		setLeafIcon("tree_node.gif");
		setBranchContractedIcon("tree_folder_closed.gif");
		setBranchExpandedIcon("tree_folder_open.gif");
		node.setUserObject(this);
	}

	public String getStyle()
	{
		return "background-color: #ffffff";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.stardust.ui.web.common.table.IRowModel#getStyleClass()
	 */
	public String getStyleClass()
	{
	   return "";
	}
	    
	public void setTreeTable(TreeTable treeTable)
	{
		this.treeTable = treeTable;
	}
}
