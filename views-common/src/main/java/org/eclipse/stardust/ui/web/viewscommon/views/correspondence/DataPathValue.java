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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

/**
 * @author Yogesh.Manware
 * 
 */
public class DataPathValue
{
   private String dataPathId;

   private String dataPathName;

   private String dataPathValue;

   private boolean selected;

   /**
    * @param dataPathId
    * @param dataPathName
    * @param dataPathValue
    */
   public DataPathValue(String dataPathId, String dataPathName, String dataPathValue)
   {
      this.dataPathId = dataPathId;
      this.dataPathName = dataPathName;
      this.dataPathValue = dataPathValue;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object dpV)
   {
      if (dpV instanceof DataPathValue)
         return this.dataPathId.equals(((DataPathValue) dpV).getDataPathId());
      return false;
   }

   public String getDataPathId()
   {
      return dataPathId;
   }

   public void setDataPathId(String dataPathId)
   {
      this.dataPathId = dataPathId;
   }

   public String getDataPathValue()
   {
      return dataPathValue;
   }

   public void setDataPathValue(String dataPathValue)
   {
      this.dataPathValue = dataPathValue;
   }

   public String getDataPathName()
   {
      return dataPathName;
   }

   public void setDataPathName(String dataPathName)
   {
      this.dataPathName = dataPathName;
   }

   public boolean getSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }
}
