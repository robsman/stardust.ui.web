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
package org.eclipse.stardust.ui.web.viewscommon.processContextExplorer;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.RuntimeObject;


public class DescriptorItem implements Serializable
{

   private RuntimeObject runtimeObject;

   private String name;

   private String value;

   public DescriptorItem(RuntimeObject runtimeObject, String name, Object value)
   {
      this.name = name;
      this.value = value != null ? value.toString() : "-";
      this.runtimeObject = runtimeObject;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getValue()
   {
      return value != null ? value.toString() : "-";
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public RuntimeObject getRuntimeObject()
   {
      return runtimeObject;
   }
}
