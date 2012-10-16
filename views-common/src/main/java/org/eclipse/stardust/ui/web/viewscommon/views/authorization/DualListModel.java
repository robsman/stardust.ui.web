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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;

/**
 * 
 * @author Vikas.Mishra
 * @author Yogesh.Manware
 * 
 * @version $Revision: $
 */
public class DualListModel implements Serializable
{
   private static final long serialVersionUID = 1L;
   private static final SelectItemComparator SELECT_ITEM_COMPARATOR = new SelectItemComparator();
   private List<SelectItemModel> source;
   private List<Object> sourceSelected = new LinkedList<Object>();
   private List<SelectItemModel> target;
   private List<Object> targetSelected = new LinkedList<Object>();

   private List<SelectItemModel> filteredSource = new LinkedList<SelectItemModel>();
   private List<SelectItemModel> filteredTarget = new LinkedList<SelectItemModel>();

   // Constructor
   public DualListModel()
   {
      source = new LinkedList<SelectItemModel>();
      target = new LinkedList<SelectItemModel>();
   }

   public void clear()
   {
      source.clear();
      target.clear();
      filteredSource.clear();
      filteredTarget.clear();
      sourceSelected.clear();
      targetSelected.clear();
   }

   /**
     *
     */
   public void add()
   {
      List<SelectItemModel> items = getItemsByValue(getSourceSelected(), source);
      target.addAll(items);
      source.removeAll(items);
      filteredTarget.addAll(items);
      filteredSource.removeAll(items);
      sourceSelected.clear();
      targetSelected.clear();
   }

   /**
     *
     */
   public void addAll()
   {
      List<SelectItemModel> removableObjects = getRemovableObjects(getSource());
      target.addAll(removableObjects);
      source.removeAll(removableObjects);
      filteredTarget.addAll(removableObjects);
      filteredSource.removeAll(removableObjects);
      sourceSelected.clear();
      targetSelected.clear();
   }

   /**
    * 
    * @return
    */
   public List<SelectItemModel> getSource()
   {
      return source;
   }

   /**
    * 
    * @return
    */
   public List<Object> getSourceSelected()
   {
      return sourceSelected;
   }

   /**
    * 
    * @return
    */
   public List<SelectItemModel> getTarget()
   {
      return target;
   }

   /**
    * 
    * @return
    */
   public List<Object> getTargetSelected()
   {
      return targetSelected;
   }

   /**
      *
      */
   public void remove()
   {
      List<SelectItemModel> items = getItemsByValue(getTargetSelected(), target);
      source.addAll(items);
      target.removeAll(items);
      filteredSource.addAll(items);
      filteredTarget.removeAll(items);
      sourceSelected.clear();
      targetSelected.clear();
   }

   /**
      *
      */
   public void removeAll()
   {
      List<SelectItemModel> removableObjects = getRemovableObjects(getTarget());
      source.addAll(removableObjects);
      target.removeAll(removableObjects);
      filteredSource.addAll(removableObjects);
      filteredTarget.removeAll(removableObjects);
      sourceSelected.clear();
      targetSelected.clear();
   }

   /**
    * 
    * @param list
    * @return
    */
   private List<SelectItemModel> getRemovableObjects(List<SelectItemModel> list)
   {
      List<SelectItemModel> removableObjects = CollectionUtils.newArrayList();

      for (SelectItemModel model : list)
      {
         if (!model.isDisable())
         {
            removableObjects.add(model);
         }
      }
      return removableObjects;
   }

   public void setSourceSelected(List<Object> sourceSelected)
   {
      this.sourceSelected = sourceSelected;
   }

   public void setTargetSelected(List<Object> targetSelected)
   {
      this.targetSelected = targetSelected;
   }

   /**
    * 
    * @param ids
    * @param fromItems
    * @return
    */
   private List<SelectItemModel> getItemsByValue(List<Object> ids, List<SelectItemModel> fromItems)
   {
      List<SelectItemModel> items = new ArrayList<SelectItemModel>();

      for (SelectItemModel item : fromItems)
      {
         for (Object id : ids)
         {
            if (id.equals(item.getValue()))
            {
               items.add(item);

               break;
            }
         }
      }

      return items;
   }

   public List<SelectItemModel> getFilteredSource()
   {
      Collections.sort(filteredSource, SELECT_ITEM_COMPARATOR);
      return filteredSource;
   }

   public List<SelectItemModel> getFilteredTarget()
   {
      Collections.sort(filteredTarget, SELECT_ITEM_COMPARATOR);
      return filteredTarget;
   }

   public void setFilteredSource(List<SelectItemModel> filteredSource)
   {
      this.filteredSource = filteredSource;
   }

   public void setFilteredTarget(List<SelectItemModel> filteredTarget)
   {
      this.filteredTarget = filteredTarget;
   }
}

/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
class SelectItemComparator implements Comparator<SelectItemModel>
{
   private static final String GE = "GE-";
   private static final String UI = "UI-";

   public int compare(SelectItemModel arg0, SelectItemModel arg1)
   {
      if (arg0.getLabel() instanceof String && arg1.getLabel() instanceof String)
      {
         String permissionId0 = (String) arg0.getValue();
         String permissionId1 = (String) arg1.getValue();
         String label0 = arg0.getLabel();
         String label1 = arg1.getLabel();

         if (UiPermissionUtils.isGeneralPermissionId(permissionId0))
         {
            label0 = GE + label0;
         }
         else
         {
            label0 = UI + label0;
         }

         if (UiPermissionUtils.isGeneralPermissionId(permissionId1))
         {
            label1 = GE + label1;
         }
         else
         {
            label1 = UI + label1;
         }

         return label0.compareTo(label1);
      }
      else
      {
         return 0;
      }
   }
}
