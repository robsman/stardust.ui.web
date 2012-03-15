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
/**
 * 
 */
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ModelTreeItem;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ProgressStatus;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;



/**
 * @author Ankita.Patel
 * 
 */
public class ModelTreeItemUserObject extends NodeUserObject
{

   private ModelTreeItem modelTreeItem;

   /**
    * @param treeTable
    * @param node
    * @param treeBeanPointer
    * @param componenttype
    */
   public ModelTreeItemUserObject(TreeTable treeTable, TreeTableNode node,
         TreeTableBean treeBeanPointer, int componenttype, ModelTreeItem modelTreeItem)
   {
      super(treeTable, node, treeBeanPointer, componenttype);
      this.modelTreeItem = modelTreeItem;
      String imagePath = "/plugins/views-common/images/icons/process.png";

      setLeafIcon(imagePath);
      setBranchContractedIcon(imagePath);
      setBranchExpandedIcon(imagePath);
      setExpanded(false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.treetable.NodeUserObject#getLine1Text()
    */
   @Override
   public String getLine1Text()
   {
      return getName();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.treetable.NodeUserObject#getLine2Text()
    */
   @Override
   public String getLine2Text()
   {
      return null;
   }

   public String getFilterType()
   {
      return "";
   }

   public String getName()
   {
      return modelTreeItem.getName();
   }

   public ProgressStatus getProgressStatus()
   {
      return modelTreeItem.getProgressStatus();
   }

   @Override
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      return false;
   }

}
