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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

public class ProcessDescriptor extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   
   String id; 
   String key;
   String value = null;
   boolean isLink = false;

   public ProcessDescriptor(String id, String key, String value)
   {
      this.id = id;
      this.key = key;
      this.value = value;
   }
   
   public ProcessDescriptor(String id, String key, String value, boolean isLink)
   {
      this.id = id;
      this.key = key;
      this.value = value;
      this.isLink = isLink;
   }

   public String getId()
   {
      return id;
   }

   public String getKey()
   {
      return key;
   }

   public String getValue()
   {
      return value;
   }

   public boolean isLink()
   {
      return isLink;
   }

}
