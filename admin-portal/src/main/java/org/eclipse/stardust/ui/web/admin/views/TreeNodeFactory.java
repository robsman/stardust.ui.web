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
package org.eclipse.stardust.ui.web.admin.views;

import org.eclipse.stardust.ui.web.admin.views.model.ConfigurationVariablesBean;
import org.eclipse.stardust.ui.web.admin.views.model.ModelConfigurationTreeItem;
import org.eclipse.stardust.ui.web.admin.views.model.ModelConfigurationUserObject;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementBean;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementTreeItem;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementUserObject;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.ErrorWarningTreeItem;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.ErrorWarningUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;



/**
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class TreeNodeFactory
{
    /**
     *
     * @param treeTable
     * @param modelVariablesViewBean
     * @param expanded
     * @return
     */
    public static TreeTableNode createTreeNode(TreeTable treeTable, ConfigurationVariablesBean modelVariablesViewBean,
        ModelConfigurationTreeItem treeItem, boolean expanded)
    {
        TreeTableNode node = new TreeTableNode();
        TreeTableUserObject userObject = null;

        if (treeItem != null)
        {
            userObject = new ModelConfigurationUserObject(treeTable, node, modelVariablesViewBean, treeItem, 2);
        }

        userObject.setExpanded(expanded);
        node.setUserObject(userObject);

        return node;
    }

    /**
     *
     * @param treeTable
     * @param modelVariablesViewBean
     * @param expanded
     * @return
     */
    public static TreeTableNode createTreeNode(TreeTable treeTable, ModelManagementBean modelManagementBean,
        ModelManagementTreeItem treeItem, boolean expanded)
    {
        TreeTableNode node = new TreeTableNode();
        TreeTableUserObject userObject = null;

        if (treeItem != null)
        {
            userObject = new ModelManagementUserObject(treeTable, node, modelManagementBean, treeItem, 2);
        }

        userObject.setExpanded(expanded);
        node.setUserObject(userObject);

        return node;
    }

   /**
    * 
    * @param treeTable
    * @param modelDeploymentStatusPage
    * @param treeItem
    * @param expanded
    * @return
    */
    public static TreeTableNode createTreeNode(TreeTable treeTable,
          TreeTableBean modelDeploymentStatusPage, ErrorWarningTreeItem treeItem, boolean expanded)
    {
        TreeTableNode node = new TreeTableNode();
        TreeTableUserObject userObject = null;

        if (treeItem != null)
        {
            userObject = new ErrorWarningUserObject(treeTable, node, modelDeploymentStatusPage, treeItem, 2);
        }

        userObject.setExpanded(expanded);
        node.setUserObject(userObject);

        return node;
    }    
    
}
