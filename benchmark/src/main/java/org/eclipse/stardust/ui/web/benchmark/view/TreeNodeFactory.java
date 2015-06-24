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
package org.eclipse.stardust.ui.web.benchmark.view;

import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;



/**
 * @author Aditya.Gaikwad
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
    public static TreeTableNode createTreeNode(TreeTable treeTable, BenchmarkConfigurationTableBean modelVariablesViewBean,
        BenchmarkModelConfigurationTreeItem treeItem, boolean expanded)
    {
        TreeTableNode node = new TreeTableNode();
        TreeTableUserObject userObject = null;

        if (treeItem != null)
        {
            userObject = new BenchmarkModelConfigurationUserObject(treeTable, node, modelVariablesViewBean, treeItem, 2);
        }

        userObject.setExpanded(expanded);
        node.setUserObject(userObject);

        return node;
    }

}
