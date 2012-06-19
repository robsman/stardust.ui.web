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
package org.eclipse.stardust.ui.client.model;

public class DescriptorFilter
{
   private String descriptor;
   private Object filterValue;
   private boolean caseSensitive;

   public DescriptorFilter(String descriptor, Object filterValue, boolean caseSensitive)
   {
      this.descriptor = descriptor;
      this.filterValue = filterValue;
      this.caseSensitive = caseSensitive;
   }

   public String getProperty()
   {
      return descriptor;
   }

   public Object getFilterValue()
   {
      return filterValue;
   }

   public boolean isCaseSensitive()
   {
      return caseSensitive;
   }
}
