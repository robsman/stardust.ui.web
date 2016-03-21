/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.component.exception;

import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;

/**
 * @author Subodh.Godbole
 *
 */
public class NotificationMapException extends Exception
{
   private static final long serialVersionUID = 1L;
   
   private NotificationMap notificationMap;

   /**
    * @param notificationMap
    */
   public NotificationMapException(NotificationMap notificationMap)
   {
      this(notificationMap, null);
   }

   /**
    * @param notificationMap
    * @param t
    */
   public NotificationMapException(NotificationMap notificationMap, Throwable t)
   {
      super(t);
      this.notificationMap = notificationMap;
   }
   
   public NotificationMap getNotificationMap()
   {
      return notificationMap;
   }
}
