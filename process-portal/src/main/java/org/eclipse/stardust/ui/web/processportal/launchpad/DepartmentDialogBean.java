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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;


/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class DepartmentDialogBean extends PopupUIComponentBean
{
   private static final String BEAN_NAME = "startDepartmentBean";

   private DefaultTreeModel model;

   /**
    * 
    */
   public DepartmentDialogBean()
   {

   }

   public static DepartmentDialogBean getCurrent()
   {
      return (DepartmentDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * @param name
    */
   public DepartmentDialogBean(String name)
   {
      super(name);

   }

   @Override
   public void initialize()
   {

   }

   public DefaultTreeModel getModel()
   {
      return model;
   }

   public void setModel(DefaultTreeModel model)
   {
      this.model = model;
   }

}
