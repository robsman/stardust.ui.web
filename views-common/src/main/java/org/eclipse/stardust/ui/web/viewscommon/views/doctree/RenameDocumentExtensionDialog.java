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

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RenameDocumentExtensionDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "renameDocumentExtensionDialog";
   private RepositoryDocumentUserObject repositoryDocumentUserObject;

   /**
    * default constructor
    */
   public RenameDocumentExtensionDialog()
   {
      super();
   }

   /**
    * returns current instance
    * 
    * @return
    */
   public static RenameDocumentExtensionDialog getCurrent()
   {
      return (RenameDocumentExtensionDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void initialize()
   {

   }
   
   /**
    * saves the file name
    */
   public void save()
   {
      this.repositoryDocumentUserObject.saveRename();
      closePopup();
   }

   /**
    * opens the dialog/popup
    * 
    * @param documentUserObject
    */
   public void open(RepositoryDocumentUserObject documentUserObject)
   {
      setRepositoryDocumentUserObject(documentUserObject);
      openPopup();
   }

   private void setRepositoryDocumentUserObject(RepositoryDocumentUserObject repositoryDocumentUserObject)
   {
      this.repositoryDocumentUserObject = repositoryDocumentUserObject;
   }
}
