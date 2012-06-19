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
package org.eclipse.stardust.ui.web.common.categorytree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author subodh.godbole
 *
 */
public class GenericCategory implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String id;
   private String label;
   private Object categoryObject;

   private List<GenericCategory> subCategories;
   private List<GenericItem> items;
   
   private boolean expanded; 
   private String iconBranchCollpased; 
   private String iconBranchExpanded;

   /**
    * @param id
    * @param categoryObject
    * @param messageBean
    */
   public GenericCategory(String id, String label, Object categoryObject)
   {
      this.id = id;
      this.label = label;
      this.categoryObject = categoryObject;
      subCategories = new ArrayList<GenericCategory>();
      items = new ArrayList<GenericItem>();
   }

   /**
    * @param id
    */
   public GenericCategory(String id, String label)
   {
      this(id, label, null);
   }

   /**
    * @param id
    * @return
    */
   public GenericCategory addSubCategory(String id, String label)
   {
      GenericCategory category = new GenericCategory(id, label);
      subCategories.add(category);
      setExpanded(true);
      return category;
   }
   
   /**
    * @param id
    * @param label
    * @param categoryObject
    * @return
    */
   public GenericCategory addSubCategory(String id, String label, Object categoryObject)
   {
      GenericCategory category = new GenericCategory(id, label, categoryObject);
      subCategories.add(category);
      setExpanded(true);
      return category;
   }

   /**
    * @param id
    * @param label
    */
   public void addItem(String id, String label)
   {
      items.add(new GenericItem(id, label));
   }

   /**
    * @param id
    * @param label
    * @param item
    * @return
    */
   public GenericItem addItem(String id, String label, Object item)
   {
      GenericItem genericItem = new GenericItem(id, label, item);
      items.add(genericItem);
      return genericItem;
   }
   
   /**
    * Additional parameter icon set the icon for the node 
    * 
    * @param id
    * @param label
    * @param item
    * @param icon
    * @return
    */
   public GenericItem addItem(String id, String label, Object item, String icon)
   {
      GenericItem genericItem = addItem(id, label, item);
      genericItem.setIcon(icon);
      return genericItem;
   }
   
   /**
    * Sets both icons to same
    * @param icon
    */
   public void setIcon(String icon)
   {
      setIconBranchExpanded(icon);
      setIconBranchCollpased(icon);
   }
   
   /**
    * @return
    */
   public String getLabel()
   {
      return label;
   }
   
   public List<GenericItem> getItems()
   {
      return items;
   }

   public String getId()
   {
      return id;
   }

   public List<GenericCategory> getSubCategories()
   {
      return subCategories;
   }

   public Object getCategoryObject()
   {
      return categoryObject;
   }

   public void setCategoryObject(Object categoryObject)
   {
      this.categoryObject = categoryObject;
   }

   public boolean isExpanded()
   {
      return expanded;
   }

   public void setExpanded(boolean expanded)
   {
      this.expanded = expanded;
   }

   public String getIconBranchCollpased()
   {
      return iconBranchCollpased;
   }

   public void setIconBranchCollpased(String iconBranchCollpased)
   {
      this.iconBranchCollpased = iconBranchCollpased;
   }

   public String getIconBranchExpanded()
   {
      return iconBranchExpanded;
   }

   public void setIconBranchExpanded(String iconBranchExpanded)
   {
      this.iconBranchExpanded = iconBranchExpanded;
   }
}
