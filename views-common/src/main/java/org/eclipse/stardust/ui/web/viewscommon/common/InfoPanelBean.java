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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;

/**
 *
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class InfoPanelBean implements Serializable
{
   /**
	 *
	 */
   private static final long serialVersionUID = 6708354182884102732L;
   private String notificationMsg;

   public InfoPanelBean()
   {
      notificationMsg = null;
   }

   public String getNotificationMsg()
   {
      return notificationMsg;
   }

   public void setNotificationMsg(String notificationMsg)
   {
      this.notificationMsg = notificationMsg;
   }

   /**
    * @param reset
    * @return
    */
   public void reset()
   {
      this.notificationMsg = null;
   }
}
