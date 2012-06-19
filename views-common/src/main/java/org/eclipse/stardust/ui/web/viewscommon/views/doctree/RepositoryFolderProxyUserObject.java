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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RepositoryFolderProxyUserObject extends RepositoryVirtualUserObject
{
   private static final long serialVersionUID = 1L;
   private String resourceId;
  
   /**
    * constructor - set default properties
    * 
    * @param defaultMutableTreeNode
    */
   public RepositoryFolderProxyUserObject(DefaultMutableTreeNode defaultMutableTreeNode, String resourceId)
   {
      super(defaultMutableTreeNode);
      this.setLeaf(false);
      this.setCanCreateFile(true);
      this.setCanUploadFile(true);
      this.setCanCreateFolder(false);
      this.resourceId = resourceId;
   }

   @Override
   public RepositoryResourceUserObject createTextDocument(String fileType)
   {
      RepositoryResourceUserObject resourceObject = getResourceUserObject();
      if (null != resourceObject)
      {
         return resourceObject.createTextDocument(fileType);
      }
      
      return null;
   }

   @Override
   public void upload()
   {
      RepositoryResourceUserObject resourceObject = getResourceUserObject();
      if (null != resourceObject)
      {
         resourceObject.upload();
      }
   }

   @Override
   public void drop(DefaultMutableTreeNode valueNode)
   {
      RepositoryResourceUserObject resourceObject = getResourceUserObject();
      if (null != resourceObject)
      {
         resourceObject.drop(valueNode);
      }
   }

   private RepositoryResourceUserObject getResourceUserObject()
   {
      DefaultMutableTreeNode node = RepositoryUtility.replaceProxyNode(this.wrapper);
      if (null != node)
      {
         return (RepositoryResourceUserObject) node.getUserObject();
      }
      else
      {
         return null;
      }
   }
   
   @Override
   public RepositoryResourceUserObject createSubfolder()
   {
      RepositoryResourceUserObject resourceObject = getResourceUserObject();
      if (null != resourceObject)
      {
         return resourceObject.createSubfolder();
      }
      return null;
   }
   
   public String getResourceId()
   {
      return resourceId;
   }
}