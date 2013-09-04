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
package org.eclipse.stardust.ui.web.viewscommon.login.dialogs;

import java.io.Serializable;

import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.ui.web.viewscommon.login.util.LoginUtils;

public class InitializeSessionBean implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 7084643287916287831L;
   private boolean initSuccess;

   /**
    * @param event
    */
   public void initUserSession(ValueChangeEvent event)
   {
      LoginUtils.initialize();
      initSuccess = true;
   }

   public boolean isInitSuccess()
   {
      return initSuccess;
   }

}
