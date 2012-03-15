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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import java.util.Set;

import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;


/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class ErrorWarningUserObject extends NodeUserObject
{
   private static final long serialVersionUID = 1L;
   private static final String ERROR_IMAGE_PATH = "/plugins/views-common/images/icons/cancel.png";
   private static final String WARNING_IMAGE_PATH = "/plugins/views-common/images/icons/warning.png";
   private static final String NONE_IMAGE_PATH = "/plugins/views-common/images/t.gif";
   private ErrorWarningTreeItem treeItem;

   public ErrorWarningUserObject(TreeTable treeTable, TreeTableNode node, TreeTableBean treeBeanPointer,
         ErrorWarningTreeItem treeItem, int componenttype)
   {
      super(treeTable, node, treeBeanPointer, componenttype);

      String imagePath = null;
      this.treeItem = treeItem;

      if ((this.treeItem != null) && (this.treeItem.getType() == ErrorWarningTreeItem.Type.ERROR))
      {
         imagePath = ERROR_IMAGE_PATH;
      }
      else if ((this.treeItem != null) && (this.treeItem.getType() == ErrorWarningTreeItem.Type.WARNING))
      {
         imagePath = WARNING_IMAGE_PATH;
      }
      else
      {
         imagePath = NONE_IMAGE_PATH;
      }

      //
      if ((this.treeItem != null) && this.treeItem.getChildrens().isEmpty())
      {
         setLeaf(true);
      }
      else
      {
         setLeaf(false);
      }

      setExpanded(false);
      setBranchContractedIcon(imagePath);
      setBranchExpandedIcon(imagePath);
      setLeafIcon(imagePath);
   }

   public Set<ErrorWarningTreeItem> getChildrens()
   {
      return this.treeItem.getChildrens();
   }

   public String getElement()
   {
      return (this.treeItem != null) ? this.treeItem.getElement() : "";
   }

   @Override
   public String getLine1Text()
   {
      return this.treeItem.getMessage();
   }

   @Override
   public String getLine2Text()
   {
      return null;
   }

   public String getMessage()
   {
      return (this.treeItem != null) ? this.treeItem.getMessage() : "OooO";
   }

   public String getModelId()
   {
      return (this.treeItem != null) ? this.treeItem.getModelId() : "OooO";
   }

   public ErrorWarningTreeItem getParent()
   {
      return (this.treeItem != null) ? this.treeItem.getParent() : null;
   }

   public ErrorWarningTreeItem getTreeItem()
   {
      return treeItem;
   }

   public ErrorWarningTreeItem.Type getType()
   {
      return (this.treeItem != null) ? this.treeItem.getType() : ErrorWarningTreeItem.Type.NONE;
   }

   @Override
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      return false;
   }

   public void setTreeItem(ErrorWarningTreeItem treeItem)
   {
      this.treeItem = treeItem;
   }

}
