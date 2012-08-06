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
package org.eclipse.stardust.ui.web.viewscommon.views.documentsearch;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Vikas.Mishra
 * 
 */
public class DocumentProcessesDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;

   public DocumentProcessesDialog()
   {
      super("documentSearchView");
   }

   @Override
   public void initialize()
   {

   }

   @Override
   public void closePopup()
   {
      super.closePopup();
   }
   
   public void openProcess(ActionEvent event)
   {
      ProcessInstance processInstance = (ProcessInstance) event.getComponent().getAttributes().get("processInstance");     
      if (processInstance != null )
      {
         ProcessInstanceUtils.openProcessContextExplorer(processInstance);
         super.closePopup();
      }
   }

}
