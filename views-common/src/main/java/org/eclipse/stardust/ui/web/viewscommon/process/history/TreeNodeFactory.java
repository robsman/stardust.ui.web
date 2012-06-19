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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;

/**
 * @author Subodh.Godbole
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
   public static TreeTableNode createActivityTreeNode(TreeTable treeTable, ActivityTreeTable activityTreeTable,
         IProcessHistoryTableEntry tableEntry, boolean expanded)
   {
      TreeTableNode node = new TreeTableNode();
      TreeTableUserObject userObject = null;

      if (tableEntry != null)
      {
         userObject = new ActivityTableEntryUserObject(treeTable, node, activityTreeTable, 2, tableEntry);
         userObject.setLeaf(((tableEntry.getChildren() != null) && !tableEntry.getChildren().isEmpty()) ? false : true);
         userObject.setExpanded(expanded);
         node.setUserObject(userObject);
      }
      return node;
   }

   /**
    * @param treeTable
    * @param procHistoryTable
    * @param procInstance
    * @param expanded
    * @return
    */
   public static TreeTableNode createProcessTreeNode(TreeTable treeTable, ProcessTreeTable processTreeTable,
         ProcessInstanceHistoryItem tableEntry, boolean expanded)
   {
      TreeTableNode node = new TreeTableNode();
      TreeTableUserObject userObject = null;

      if (tableEntry != null)
      {
         userObject = new ProcessTableEntryUserObject(treeTable, node, processTreeTable, 2, tableEntry);
         userObject.setLeaf(((tableEntry.getChildren() != null) && !tableEntry.getChildren().isEmpty()) ? false : true);
         userObject.setExpanded(expanded);
         node.setUserObject(userObject);
      }
      return node;
   }

}
