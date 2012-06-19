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
package org.eclipse.stardust.ui.web.admin.views.model;

import java.util.Date;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementTreeItem.Type;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Vikas.Mishra 
 * NodeUserObject for ModelManagement table
 */
public class ModelManagementUserObject extends NodeUserObject
{
   private static final long serialVersionUID = 1L;
   private ModelManagementTreeItem treeItem = null;

   // private String value;

   private boolean popupVisible;

   public ModelManagementUserObject(TreeTable treeTable, TreeTableNode node, TreeTableBean treeBeanPointer,
         ModelManagementTreeItem treeItem, int componenttype)
   {
      super(treeTable, node, treeBeanPointer, componenttype);
      this.treeItem = treeItem;

      String imagePath = treeItem.getType().getImage();

      setExpanded(false);
      setBranchContractedIcon(imagePath);
      setBranchExpandedIcon(imagePath);
      setLeafIcon(imagePath);
      setTooltip(treeItem.getVersionLabel());
      if ((this.treeItem.getType() == ModelManagementTreeItem.Type.MODEL)
            || (this.treeItem.getType() == ModelManagementTreeItem.Type.NONE)
            || (this.treeItem.getType() == ModelManagementTreeItem.Type.MODEL_VERSION))
      {
         setLeaf(false);
      }
      else
      {
         setLeaf(true);
      }
   }

   public String getComment()
   {
      return (this.treeItem.getType() == ModelManagementTreeItem.Type.MODEL_VERSION) ? treeItem.getComment() : null;
   }

   // TODO delete this method after new tag-lib implementation
   public String getCommentTrim()
   {
      String comment = getComment();

      if ((comment != null) && (comment.length() > 30))
      {
         comment = comment.substring(0, 30);

         return comment + "...";
      }

      return comment;
   }

   public String getFilterType()
   {
      return "";
   }

   public String getLabel()
   {
      return treeItem.getLabel();
   }

   @Override
   public String getLine1Text()
   {
      return treeItem.getLabel();
   }

   @Override
   public String getLine2Text()
   {
      return null;
   }

   public DeployedModelDescription getModelDescription()
   {
      return treeItem.getModelDescription();
   }

   public Integer getOid()
   {
      return (this.treeItem.getType() == ModelManagementTreeItem.Type.MODEL) ? null : treeItem.getOid();
   }

   public ModelManagementTreeItem getParent()
   {
      return treeItem.getParent();
   }

   public ModelManagementTreeItem getTreeItem()
   {
      return treeItem;
   }

   public Type getType()
   {
      return treeItem.getType();
   }

   public Date getValidFrom()
   {
      return treeItem.getValidFrom();
   }

   public String getVersion()
   {
      return (this.treeItem.getType() == ModelManagementTreeItem.Type.MODEL) ? "" : treeItem.getVersion();
   }

   

   /**
    * method to filter out node on specific condition these are conditions to filter out:
    * 1)if type is MODEL_VERSION then filter out=false for topmost version and rest will
    * be filter out=true 2)if type is CONSUMER_MODEL then filter out PROVIDER_MODEL AND
    * PRIMARY IMPLEMENTATION MODEL 3)if type is PROVIDER_MODEL then filter out
    * CONSUMER_MODEL AND PRIMARY IMPLEMENTATION MODEL 4)if type is PRIMARY IMPLEMENTATION
    * MODEL then filter out CONSUMER_MODEL AND PROVIDER_MODEL
    */
   @Override
   public boolean isFilterOut(TableDataFilters dataFilters)
   {
      for (ITableDataFilter tableDataFilters : dataFilters.getList())
      {
         ITableDataFilterOnOff onOffFilter = ((ITableDataFilterOnOff) tableDataFilters);

         if (onOffFilter.isOn())
         {
            if (onOffFilter.getName().equals(getType().name())
                  && !ModelManagementTreeItem.Type.MODEL_VERSION.equals(getType()))
            {
               return true;
            }
            else if (onOffFilter.getName().equals(getType().name()) && !treeItem.isActiveVersion()
                  && ModelManagementTreeItem.Type.MODEL_VERSION.equals(getType()))
            {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isPopupVisible()
   {
      return popupVisible;
   }

   public void setComment(String comment)
   {
      treeItem.setComment(comment);
   }   

   public void setPopupVisible(boolean popupVisible)
   {
      this.popupVisible = popupVisible;
   }

   public void setTreeItem(ModelManagementTreeItem treeItem)
   {
      this.treeItem = treeItem;
   }

   public void setValidFrom(Date validFrom)
   {
      treeItem.setValidFrom(validFrom);
   }

   public boolean isActionAllowed()
   {
      return !PredefinedConstants.PREDEFINED_MODEL_ID.equals(treeItem.getModelDescription().getId());
   }

}
