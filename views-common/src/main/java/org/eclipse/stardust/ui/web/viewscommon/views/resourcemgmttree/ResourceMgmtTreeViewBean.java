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
package org.eclipse.stardust.ui.web.viewscommon.views.resourcemgmttree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean.RepositoryMode;


/**
 * @author yogesh.manware
 * @version $Revision: $
 */
public class ResourceMgmtTreeViewBean extends UIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private GenericRepositoryTreeViewBean genericRepositoryTree;

   /**
    * default constructor - creates my documents tree using GenericRepositoryTree
    */
   public ResourceMgmtTreeViewBean()
   {
      super(ResourcePaths.VID_RESOURCE_MGMT);
      GenericRepositoryTreeViewBean genericRepositoryTree = GenericRepositoryTreeViewBean.getInstance();
      genericRepositoryTree.setRepositoryMode(RepositoryMode.RESOURCE_MANAGEMENT);
      genericRepositoryTree.initialize();
      this.genericRepositoryTree = genericRepositoryTree;
   }

   @Override
   public void initialize()
   {}

   public void handleEvent(ViewEvent event)
   {
   // TODO Reason
   }

   /**
    * refreshes the complete document tree
    */
   public void update()
   {
      if (genericRepositoryTree.isEditingModeOff())
      {
         DefaultMutableTreeNode virtualNode = (DefaultMutableTreeNode) this.genericRepositoryTree.getModel().getRoot();
         int count = virtualNode.getChildCount();
         DefaultMutableTreeNode tempNode;
         for (int i = 0; i < count; i++)
         {
            tempNode = (DefaultMutableTreeNode) virtualNode.getChildAt(i);
            RepositoryUtility.refreshNode(tempNode);
         }
      }
   }
}
