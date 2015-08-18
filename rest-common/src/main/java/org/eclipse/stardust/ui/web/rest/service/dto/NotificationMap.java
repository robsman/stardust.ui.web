/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yogesh.Manware
 *
 */

public class NotificationMap extends HashMap<String, List<NotificationMap.NotificationDTO>>
{
   private static final String SUCCESS = "success";
   private static final String FAILURE = "failure";
   /**
    * 
    */
   private static final long serialVersionUID = -2306889075495761029L;

   public NotificationMap()
   {
      super();
      this.put(SUCCESS, new ArrayList<NotificationDTO>());
      this.put(FAILURE, new ArrayList<NotificationDTO>());
   }

   public void addSuccess(NotificationDTO dto)
   {
      this.get(SUCCESS).add(dto);
   }

   public void addFailure(NotificationDTO dto)
   {
      this.get(FAILURE).add(dto);
   }

   public void addSuccess(List<NotificationDTO> notificatios)
   {
      this.get(SUCCESS).addAll(notificatios);
   }

   public void addFailures(List<NotificationDTO> notificatios)
   {
      this.get(FAILURE).addAll(notificatios);
   }

   public void addAll(NotificationMap map)
   {
      this.get(SUCCESS).addAll(map.get(SUCCESS));
      this.get(FAILURE).addAll(map.get(FAILURE));
   }

   public static class NotificationDTO
   {
      private String name;
      private Long OID;
      private String message;

      public NotificationDTO(Long oID, String name, String description)
      {
         this.name = name;
         OID = oID;
         this.message = description;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public Long getOID()
      {
         return OID;
      }

      public void setOID(Long oID)
      {
         OID = oID;
      }

      public String getMessage()
      {
         return message;
      }

      public void setMessage(String message)
      {
         this.message = message;
      }
   }

}
