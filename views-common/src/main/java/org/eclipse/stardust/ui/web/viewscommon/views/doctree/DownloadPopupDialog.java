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
public class DownloadPopupDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "downloadPopupDialog";
   private OutputResource outputResource;

   /**
    * default constructor
    */
   public DownloadPopupDialog()
   {
      super("myDocumentsTreeView");
   }

   /**
    * returns current instance
    * 
    * @return
    */
   public static DownloadPopupDialog getCurrent()
   {
      return (DownloadPopupDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void initialize()
   {}

   /**
    * Opens file download dialog for a specified file
    * 
    * @param userObject
    */
   public void open(OutputResource resource)
   {

      this.outputResource = resource;
      super.openPopup();

   }

   /**
    * closes the popup
    */
   public void closePopup()
   {
      try
      {
         this.outputResource = null;
         super.closePopup();
      }
      catch (Exception e)
      {
         // do nothing
      }
   }

   public OutputResource getOutputResource()
   {
      return outputResource;
   }
}
