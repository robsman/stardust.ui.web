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
package org.eclipse.stardust.ui.web.admin.views.criticality;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.util.PopupDialog;


public class CriticalityIconsSelectorPopup extends PopupDialog
{
   
   /**
    * @param title
    */
   public CriticalityIconsSelectorPopup(String title)
   {
      super(title);
   }

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public void showIconSelector(ActionEvent event)
   {
      setVisible(true);
   }
   
   public void closeSelectionPopup(ActionEvent event)
   {
      setVisible(false);
   }

   @Override
   public void apply()
   {
      //Do nothing
   }

   @Override
   public void reset()
   {
      //Do nothing
   }
}
