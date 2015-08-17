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

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;


/**
 * @author Yogesh.Manware
 * 
 */
public class DescriptorItemTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   String name;
   Object value;
   boolean editable = false;
   boolean hasError;

   /**
    * @param name
    * @param value
    */
   public DescriptorItemTableEntry(String name, Object value)
   {
      super();
      this.name = name;
      this.value = value;
   }

   public DescriptorItemTableEntry(String name, Object value, boolean editable)
   {
      this(name, value);
      this.editable = editable;
   }
   
   public String getName()
   {
      return this.name;
   }

   public Object getValue()
   {
      return this.value;
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   public boolean isEditable()
   {
      return editable;
   }

   public void setEditable(boolean editable)
   {
      this.editable = editable;
   }

   public boolean isHasError()
   {
      return hasError;
   }

   public void setHasError(boolean hasError)
   {
      this.hasError = hasError;
   }

}
