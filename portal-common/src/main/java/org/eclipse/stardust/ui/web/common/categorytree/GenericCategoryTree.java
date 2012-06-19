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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.ui.web.common.UIComponentBean;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * @author subodh.godbole
 *
 */
public class GenericCategoryTree extends UIComponentBean
{
   private static final long serialVersionUID = 1L;

   private DefaultTreeModel model;
   private GenericCategory rootCategory;
   private IGenericCategoryTreeUserObjectCallback callback;

   /**
    * @param root
    * @param label
    * @param callback
    */
   public GenericCategoryTree(String root, String label,
         IGenericCategoryTreeUserObjectCallback callback)
   {
      this.callback = callback; 
      rootCategory = new GenericCategory(root, label);
   }

   @Override
   public void initialize()
   {
   }

   /**
    * 
    */
   public void refreshTreeModel()
   {
      DefaultMutableTreeNode rootTreeNode = addFolder(null, rootCategory);
      expandFolder(rootTreeNode, rootCategory);
      model = new DefaultTreeModel(rootTreeNode);
   }
   
   /**
    * @param node
    * @param category
    */
   private void expandFolder(DefaultMutableTreeNode node, GenericCategory category)
   {
      node.removeAllChildren();

      for (int n = 0; n < category.getSubCategories().size(); ++n)
      {
         if(category.isExpanded())
         {
            expandFolder(addFolder(node, (GenericCategory) category.getSubCategories().get(n)),
                  (GenericCategory) category.getSubCategories().get(n));
         }
      }

      addLeafNodes(node, category.getItems());
      
      GenericCategoryTreeUserObject userObj = (GenericCategoryTreeUserObject)node.getUserObject();
      if(userObj.getCategory().isExpanded())
      {
         ((IceUserObject)node.getUserObject()).setExpanded(true);
      }
   }

   /**
    * @param parentNode
    * @param category
    * @return
    */
   private DefaultMutableTreeNode addFolder(DefaultMutableTreeNode parentNode,
         GenericCategory category)
   {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode();
      GenericCategoryTreeUserObject branchObject = new GenericCategoryTreeUserObject(node, category);
      branchObject.setCallback(callback);
      node.setUserObject(branchObject);

      if (parentNode != null)
      {
         parentNode.add(node);
         // Set Expanded only if it has children
         ((IceUserObject)parentNode.getUserObject()).setExpanded(true);
      }

      return node;
   }

   /**
    * @param parentNode
    * @param views
    */
   private void addLeafNodes(DefaultMutableTreeNode parentNode, List<GenericItem> views)
   {
      for (int n = 0; n < views.size(); ++n)
      {
         DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode();
         GenericCategoryTreeUserObject branchObject = new GenericCategoryTreeUserObject(
               leafNode, views.get(n));
         branchObject.setCallback(callback);

         leafNode.setUserObject(branchObject);
         parentNode.add(leafNode);
      }
   }

   public DefaultTreeModel getModel()
   {
      return model;
   }
   
   public GenericCategory getRootCategory()
   {
      return rootCategory;
   }
}