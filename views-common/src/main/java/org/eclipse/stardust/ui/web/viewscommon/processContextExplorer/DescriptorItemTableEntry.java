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

import java.util.Date;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


/**
 * @author Yogesh.Manware
 * 
 */
public class DescriptorItemTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   String id;
   String name;
   Object value;
   String type;
   Class mappedType;
   boolean editable = false;
   boolean hasError;
   Date lastModified;
   String modifiedBy;
   String linkText;
   boolean hideTime;
   boolean useServerTimeZone;

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

   public DescriptorItemTableEntry(String name, Object value, String id, String type, Class mappedType, boolean editable)
   {
      this(name, value);
      this.id = id;
      this.type = type;
      this.mappedType = mappedType;
      this.editable = editable;
   }
   
   public DescriptorItemTableEntry(String name, Object value, String id, String type, Class mappedType, boolean editable, String linkText)
   {
      this(name, value, id, type, mappedType, editable);
      this.linkText = linkText;
   }
   
   public String getId()
   {
      return id;
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

   public String getType()
   {
      return type;
   }

   public Class getMappedType()
   {
      return mappedType;
   }

   public boolean isEditable()
   {
      return editable;
   }

   public boolean isHasError()
   {
      return hasError;
   }

   public void setHasError(boolean hasError)
   {
      this.hasError = hasError;
   }

   public Date getLastModified()
   {
      return lastModified;
   }

   public void setLastModified(Date lastModified)
   {
      this.lastModified = lastModified;
   }

   public String getModifiedBy()
   {
      return modifiedBy;
   }

   public void setModifiedBy(String modifiedBy)
   {
      this.modifiedBy = modifiedBy;
   }

   public String getLinkText()
   {
      return linkText;
   }

   public void setLinkText(String linkText)
   {
      this.linkText = linkText;
   }
   
   public boolean isHideTime()
   {
      return hideTime;
   }

   public void setHideTime(boolean hideTime)
   {
      this.hideTime = hideTime;
   }

   public boolean isUseServerTimeZone()
   {
      return useServerTimeZone;
   }

   public void setUseServerTimeZone(boolean useServerTimeZone)
   {
      this.useServerTimeZone = useServerTimeZone;
   }

}
