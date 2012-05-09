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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * 
 * This is an extension to ConfirmationDialog and can be used as a separate instance as we
 * use ConfirmationDialog
 * 
 * @author Yogesh.Manware
 * 
 */
public class ConfirmationDialogWithOptionsBean extends ConfirmationDialog
{
   private static final long serialVersionUID = -3080491591080966564L;
   private static final String BEAN_NAME = "confirmationDialogWithOptionsBean";

   private SelectItem[] options;
   private String selectedOption;

   public ConfirmationDialogWithOptionsBean()
   {
      super(DialogContentType.NONE, DialogActionType.OK_CANCEL, null);
   }

   public static ConfirmationDialogWithOptionsBean getInstance()
   {
      return (ConfirmationDialogWithOptionsBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public SelectItem[] getOptions()
   {
      return options;
   }

   public void setOptions(SelectItem[] options)
   {
      this.options = options;
   }

   public String getSelectedOption()
   {
      return selectedOption;
   }

   public void setSelectedOption(String selectedOption)
   {
      this.selectedOption = selectedOption;
   }
}
