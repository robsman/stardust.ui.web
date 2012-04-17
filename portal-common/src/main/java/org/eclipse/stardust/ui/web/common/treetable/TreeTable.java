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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.swing.tree.TreeModel;

import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.table.DataTable;



/**
 *
 */
public class TreeTable extends DataTable<TreeTableUserObject>
{
   private static final long serialVersionUID = 1L;
   private TreeModel treeModel;
   private boolean filterRootNode = true;
   private boolean hideRootNode;
   private String tooltipURL;

   // **************************** CONSTRUCTORS ****************************

   /**
    * @param treeModel
    * @param columnSelectorPopup
    * @param dataFilterPopup
    */
   public TreeTable(TreeModel treeModel, TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilterPopup dataFilterPopup)
   {
      super(columnSelectorPopup, dataFilterPopup);
      this.treeModel = treeModel;
   }

 	/**
 	 * @param treeModel
 	 * @param columnModel
 	 * @param dataFilters
 	 */
 	public TreeTable(TreeModel treeModel, IColumnModel columnModel, TableDataFilters dataFilters)
	{
	   super(columnModel, dataFilters);
	   this.treeModel = treeModel;
	}

    /**
    * @param treeModel
    * @param columnSelectorPopup
    * @param dataFilters
    */
   public TreeTable(TreeModel treeModel, TableColumnSelectorPopup columnSelectorPopup,
          TableDataFilters dataFilters)
   {
      super(columnSelectorPopup, dataFilters);
      this.treeModel = treeModel;
   }

   public TreeTable(TreeModel treeModel, TableColumnSelectorPopup columnSelectorPopup)
   {
      super(columnSelectorPopup, (TableDataFilterPopup)null);
      this.treeModel = treeModel;
   }

	/**
     * @param model
     * @param dataFilters
     */
    public TreeTable(TreeModel model, TableDataFilters dataFilters)
    {
       this(model, (IColumnModel)null, dataFilters);
    }

	// **************************** PUBLIC METHODS ****************************
    
    /* (non-Javadoc)
     * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener#applyFilter(org.eclipse.stardust.ui.web.common.filter.TableDataFilters)
     */
    public void applyFilter(TableDataFilters tableDataFilters)
    {
       rebuildList();
    }

   /**
    * Event for rebuilding the tree table list after the treeModel has been changed.
    * This event should be raised whenever the treeModel changes. (must be called as an
    * event to force Seam to uncache the previous list value)
    */
	public void rebuildList()
	{
		if (list == null)
			list = new ArrayList<TreeTableUserObject>();
		list.clear();
		
		resetExportFiles();

		TreeTableUserObject ttuo = ((TreeTableNode) treeModel.getRoot()).getUserObject();
		if(ttuo != null)
		{
		   boolean filterOut = false;
		   if(!hideRootNode && filterRootNode)
		   {
         	   if(autoFilter && dataFilters != null)
         	      filterOut = dataFilters.isFilterOut(ttuo);
         	   else
         	      filterOut = ttuo.isFilterOut(dataFilters);
		   }

		   if(!filterOut)
           {
      	      boolean expandNode = ttuo.isExpanded();
      	      if (!hideRootNode)
      	      {
      	         list.add(ttuo);
      	      }
      	      else
      	      {
      	         expandNode = true;
      	      }

      	      if (expandNode)
      	      {
      	         addChildrenToList((TreeTableNode) treeModel.getRoot());
      	      }
           }
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.stardust.ui.web.common.treetable.ITreeTable#expandAll(javax.faces.event.ActionEvent)
	 */
	public void expandAll(ActionEvent event)
	{
		expandCollapseAll( (TreeTableNode)treeModel.getRoot(), true );
		rebuildList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.stardust.ui.web.common.treetable.ITreeTable#collapseAll(javax.faces.event.ActionEvent)
	 */
	public void collapseAll(ActionEvent event)
	{
		expandCollapseAll( (TreeTableNode)treeModel.getRoot(), false );
		rebuildList();
	}

	// **************************** PRIVATE METHODS ****************************

	/**
	 * @param node
	 */
	private void addChildrenToList(TreeTableNode node)
	{
		Enumeration enumer = node.children();
		while (enumer.hasMoreElements())
		{
			TreeTableNode child = (TreeTableNode) enumer.nextElement();
			TreeTableUserObject childObject = child.getUserObject();
			
		     boolean filterOut;
		     if(autoFilter)
		        filterOut = dataFilters.isFilterOut(childObject);
		     else
		        filterOut = childObject.isFilterOut(dataFilters);

			if(!filterOut)
			{
				list.add(childObject);
				if (childObject.isExpanded())
				{
					addChildrenToList(child);
				}
			}
		}
	}

	/**
	 * @param root
	 */
	private void expandCollapseAll(TreeTableNode node, boolean expand)
	{
	   if(node != null && node.getUserObject() != null)
   	   {
      		node.getUserObject().setExpanded(expand);
      
      		Enumeration enumer = node.children();
      		while( enumer.hasMoreElements() )
      		{
      			TreeTableNode child = (TreeTableNode) enumer.nextElement();
      			TreeTableUserObject childObject = child.getUserObject();
      			childObject.setExpanded(expand);
      			
      			expandCollapseAll(child, expand);
      		}
	   }
	}

	// **************************** DEFAULT GETTER SETTER METHODS ****************************
	
	@Override
   public List<TreeTableUserObject> getList()
   {
      return list;
   }

   /**
	 * @return The rendered state of the tree table (only shows the currently
	 *         displayed treeTable nodes)
	 * @deprecated - Instead use getList()
	 */
	public List<TreeTableUserObject> getLinearizedTree()
	{
		return getList();
	}
	
   public boolean isFilterRootNode()
   {
      return filterRootNode;
   }

   public void setFilterRootNode(boolean filterRootNode)
   {
      this.filterRootNode = filterRootNode;
   }

   public boolean isHideRootNode()
   {
      return hideRootNode;
   }

   public void setHideRootNode(boolean hideRootNode)
   {
      this.hideRootNode = hideRootNode;
   }

   public String getTooltipURL()
   {
      return tooltipURL;
   }

   public void setTooltipURL(String tooltipURL)
   {
      this.tooltipURL = tooltipURL;
   }
   
}
