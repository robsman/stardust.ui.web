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
package org.eclipse.stardust.ui.web.viewscommon.common.notification;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class NotificationItem
{
   private String key;
   private String description;
   /**
    * 
    */
   public NotificationItem()
   {
   // TODO Auto-generated constructor stub
   }
   
   public NotificationItem(String key, String description)
   {
      super();
      this.key = key;
      this.description = description;
   }

   public String getKey()
   {
      return key;
   }
   public void setKey(String key)
   {
      this.key = key;
   }
   public String getDescription()
   {
      return description;
   }
   public void setDescription(String description)
   {
      this.description = description;
   }
   

}
