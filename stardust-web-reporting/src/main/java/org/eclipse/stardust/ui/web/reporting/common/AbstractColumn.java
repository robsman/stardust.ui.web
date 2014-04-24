/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common;

public abstract class AbstractColumn
{
   private String id;

   public AbstractColumn(String id)
   {
      this.id = id;
   }

   public String getId()
   {
      return id;
   }


}
