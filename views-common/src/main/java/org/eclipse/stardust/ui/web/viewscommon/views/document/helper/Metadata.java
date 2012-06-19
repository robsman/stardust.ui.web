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
package org.eclipse.stardust.ui.web.viewscommon.views.document.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * provides basic type conversion methods
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public abstract class Metadata
{
   private Map<String, Object> properties;

   /**
    * sets default properties if not set already
    * 
    * @param document
    */
   protected Metadata(Map<String, Object> properties)
   {
      this.properties = properties;
      if (null == this.properties)
      {
         this.properties = new HashMap<String, Object>();
      }
   }

   /**
    * Override this method to provide required properties to this class for conversion
    * 
    * @return
    */
   protected Map<String, Object> getProperties()
   {
      return this.properties;
   }

   protected boolean getBooleanValue(String name)
   {
      return ((Boolean) getProperties().get(name)).booleanValue();
   }

   protected void setBooleanValue(String name, boolean value)
   {
      getProperties().put(name, new Boolean(value));
   }

   protected String getStringValue(String name)
   {
      return (String) getProperties().get(name);
   }

   protected void setStringValue(String name, String value)
   {
      getProperties().put(name, value);
   }

   protected Date getDateValue(String name)
   {
      return (Date) getProperties().get(name);
   }

   protected void setDateValue(String name, Date value)
   {
      getProperties().put(name, value);
   }
}