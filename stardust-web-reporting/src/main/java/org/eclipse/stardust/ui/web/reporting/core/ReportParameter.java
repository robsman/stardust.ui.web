/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class ReportParameter
{
   private String id;
   private TreeSet<String> values;

   public ReportParameter(String id, TreeSet<String> values)
   {
      this.id = id;
      this.values = values;
   }

   public String getId()
   {
      return id;
   }

   private String getSingleValue()
   {
      if(values != null && !values.isEmpty())
      {
         return values.last();
      }

      return null;
   }

   public boolean hasSingleValue()
   {
      return (values.size() == 1);
   }

   public boolean hasMultipleValues()
   {
      return (values.size() > 1);
   }

   public Collection<String> getAllValues()
   {
      return values;
   }

   public Double getDoubleValue()
   {
      String s = getSingleValue();
      return Double.parseDouble(s);
   }

   public Long getLongValue()
   {
      String s = getSingleValue();
      return Long.parseLong(s);
   }

   public List<Long> getLongValues()
   {
      List<Long> longValues = new ArrayList<Long>();
      for(String s : values)
      {
         longValues.add(Long.parseLong(s));
      }

      return longValues;
   }

   public String getFirstValue()
   {
      return values.first();
   }

   public String getLastValue()
   {
      return values.last();
   }

   public int getValuesSize()
   {
      return values.size();
   }
}
