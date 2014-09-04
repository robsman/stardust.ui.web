/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class ActivityInstanceDTO
{
   private long oid;

   private String start;

   private ActivityDTO activity;

   /**
    * 
    */
   public ActivityInstanceDTO()
   {

   }

   /**
    * @return the oid
    */
   public long getOid()
   {
      return oid;
   }

   /**
    * @param oid
    *           the oid to set
    */
   public void setOid(long oid)
   {
      this.oid = oid;
   }

   /**
    * @return the start
    */
   public String getStart()
   {
      return start;
   }

   /**
    * @param start
    *           the start to set
    */
   public void setStart(String start)
   {
      this.start = start;
   }

   /**
    * @return the activity
    */
   public ActivityDTO getActivity()
   {
      return activity;
   }

   /**
    * @param activity
    *           the activity to set
    */
   public void setActivity(ActivityDTO activity)
   {
      this.activity = activity;
   }

}
