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

   // Constructor
   public DualListModel()
   {
      source = new LinkedList<SelectItemModel>();
      target = new LinkedList<SelectItemModel>();
   }

   public DualListModel(List<SelectItemModel> source, List<SelectItemModel> target)
   {
      this.source = source;
      this.target = target;
   }

   /**
     *
     */
   public void add()
   {
      List<SelectItemModel> items = getItemsByValue(getSourceSelected(), source);
      getTarget().addAll(items);
      getSource().removeAll(items);
      getSourceSelected().clear();
      getTargetSelected().clear();
   }

   /**
     *
     */
   public void addAll()
   {
      List<SelectItemModel> removableObjects = getRemovableObjects(getSource());
      getTarget().addAll(removableObjects);
      getSource().removeAll(removableObjects);
      getSourceSelected().clear();
      getTargetSelected().clear();
   }

   /**
    * 
    * @return
    */
   public List<SelectItemModel> getSource()
   {
      Collections.sort(source, SELECT_ITEM_COMPARATOR);

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
      Collections.sort(target, SELECT_ITEM_COMPARATOR);
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
      getSource().addAll(items);
      getTarget().removeAll(items);
      getSourceSelected().clear();
      getTargetSelected().clear();
   }

   /**
      *
      */
   public void removeAll()
   {
      List<SelectItemModel> removableObjects = getRemovableObjects(getTarget());
      getSource().addAll(removableObjects);
      getTarget().removeAll(removableObjects);
      getSourceSelected().clear();
      getTargetSelected().clear();
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

   public void setSource(List<SelectItemModel> source)
   {
      this.source = source;
   }

   public void setSourceSelected(List<Object> sourceSelected)
   {
      this.sourceSelected = sourceSelected;
   }

   public void setTarget(List<SelectItemModel> target)
   {
      this.target = target;
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
}

/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
class SelectItemComparator implements Comparator<SelectItemModel>
{
   public int compare(SelectItemModel arg0, SelectItemModel arg1)
   {
      if (arg0.getValue() instanceof String && arg1.getValue() instanceof String)
      {
         return ((String) arg0.getValue()).compareTo((String) arg1.getValue());
      }
      else
      {
         return 0;
      }
   }
}

