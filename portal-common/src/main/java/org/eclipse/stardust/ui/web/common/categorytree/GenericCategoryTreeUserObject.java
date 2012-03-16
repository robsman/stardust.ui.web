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

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * @author subodh.godbole
 * 
 */
public class GenericCategoryTreeUserObject extends IceUserObject implements Serializable
{
   private static final long serialVersionUID = 1L;

   public static String ICON_FOLDER = "/plugins/common/images/icons/tree_folder_closed.gif";
   public static String ICON_FOLDER_OPEN = "/plugins/common/images/icons/tree_folder_open.gif";
   public static String ICON_DOCUMENT = "/plugins/common/images/icons/blue-document.png";
   
   private GenericCategory category;
   private GenericItem item;
   private IGenericCategoryTreeUserObjectCallback callback;

   /**
    * @param wrapper
    * @param category
    */
   public GenericCategoryTreeUserObject(DefaultMutableTreeNode wrapper,
         GenericCategory category)
   {
      super(wrapper);

      this.category = category;

      setText(category.getId());
      setLeaf(false);
   }

   /**
    * @param wrapper
    * @param item
    */
   public GenericCategoryTreeUserObject(DefaultMutableTreeNode wrapper, GenericItem item)
   {
      super(wrapper);
      this.item = item;

      setText(item.toString());
      setLeaf(true);
   }

   /* (non-Javadoc)
    * @see com.icesoft.faces.component.tree.IceUserObject#getIcon()
    */
   public String getIcon()
   {
      String icon;
      if (isReferencingCategory())
      {
         if(isExpanded())
         {
            icon = StringUtils.isNotEmpty(category.getIconBranchExpanded()) ? category
                  .getIconBranchExpanded() : ICON_FOLDER_OPEN;
         }
         else
         {
            icon = StringUtils.isNotEmpty(category.getIconBranchCollpased()) ? category
                  .getIconBranchCollpased() : ICON_FOLDER;
         }
      }
      else
      {
         icon = StringUtils.isNotEmpty(item.getIcon()) ? item.getIcon() : ICON_DOCUMENT;
      }
      
      return icon;
   }

   /**
	 * 
	 */
   public void toggleExpansion()
   {
      if (isExpanded())
      {
         if (callback != null)
         {
            callback.collapsed(this);
         }

         setExpanded(false);
         category.setExpanded(false);
      }
      else
      {
         if (callback != null)
         {
            callback.expanded(this);
         }

         setExpanded(true);
         category.setExpanded(true);
      }
   }

   /**
	 * 
	 */
   public void itemClicked()
   {
      if (callback != null)
         callback.itemClicked(this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.icesoft.faces.component.tree.IceUserObject#getLeafIcon()
    */
   public String getLeafIcon()
   {
      return getIcon();
   }

   public boolean isReferencingCategory()
   {
      return category != null;
   }

   public boolean isReferencingItem()
   {
      return item != null;
   }

   public GenericCategory getCategory()
   {
      return category;
   }

   public GenericItem getItem()
   {
      return item;
   }

   public void setCallback(IGenericCategoryTreeUserObjectCallback callback)
   {
      this.callback = callback;
   }
}
