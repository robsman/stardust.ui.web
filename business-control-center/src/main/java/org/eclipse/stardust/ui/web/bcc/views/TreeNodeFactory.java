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
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.bcc.jsf.PriorityOverviewEntry;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ModelTreeItem;
import org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ActivityCriticalityManagerBean;
import org.eclipse.stardust.ui.web.bcc.views.criticalityManager.CriticalityOverviewNodeObject;
import org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class TreeNodeFactory
{
   /**
    * @param treeTable
    * @param procHistoryTable
    * @param procInstance
    * @param expanded
    * @return
    */
   public static TreeTableNode createTreeNode(TreeTable treeTable, BusinessProcessManagerBean busProcManagerBean,
         PriorityOverviewEntry prioOverviewEntry, boolean expanded)
   {
      TreeTableNode node = new TreeTableNode();
      TreeTableUserObject userObject = null;

      if (prioOverviewEntry != null)
      {
         userObject = new PriorityOverviewUserObject(treeTable, node, busProcManagerBean, 2, prioOverviewEntry);
         userObject.setLeaf(prioOverviewEntry.getChildren() != null && !prioOverviewEntry.getChildren().isEmpty()
               ? false
               : true);
         userObject.setExpanded(expanded);
         node.setUserObject(userObject);
      }

      return node;
   }

   public static TreeTableNode createModelTreeNode(TreeTable treeTable, GanttChartManagerBean ganttChartManagerBean,
         ModelTreeItem modelTreeEntry, boolean expanded)
   {
      TreeTableNode node = new TreeTableNode();
      TreeTableUserObject userObject = null;

      if (modelTreeEntry != null)
      {
         userObject = new ModelTreeItemUserObject(treeTable, node, ganttChartManagerBean, 2, modelTreeEntry);
         userObject.setLeaf(modelTreeEntry.getChildren() != null && !modelTreeEntry.getChildren().isEmpty()
               ? false
               : true);
         userObject.setExpanded(expanded);
         node.setUserObject(userObject);
      }

      return node;
   }

   public static TreeTableNode createTreeNode(TreeTable treeTable, ActivityCriticalityManagerBean treeTableBean,
         ICriticalityMgrTableEntry criticalityOverviewEntry, boolean expanded)
   {
      TreeTableNode node = new TreeTableNode();
      TreeTableUserObject userObject = null;

      if (criticalityOverviewEntry != null)
      {
         userObject = new CriticalityOverviewNodeObject(treeTable, node, treeTableBean, 2, criticalityOverviewEntry);
         userObject.setLeaf(criticalityOverviewEntry.getChildren() != null && !criticalityOverviewEntry.getChildren().isEmpty()
               ? false
               : true);
         userObject.setExpanded(expanded);
         node.setUserObject(userObject);
      }

      return node;
   }
}
