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

public final class ReportParameter
{
   private String id;
   private TreeSet<String> uniqueValues;

   public ReportParameter(String id, String[] values)
   {
      uniqueValues = new TreeSet<String>();
      for(String v: values)
      {
         uniqueValues.add(v);
      }
   }

   public String getId()
   {
      return id;
   }

   private String getSingleValue()
   {
      if(uniqueValues != null && !uniqueValues.isEmpty())
      {
         return uniqueValues.last();
      }

      return null;
   }

   public boolean hasSingleValue()
   {
      return (uniqueValues.size() == 1);
   }

   public boolean hasMultipleValues()
   {
      return (uniqueValues.size() > 1);
   }

   public Collection<String> getAllValues()
   {
      return uniqueValues;
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
      for(String s : uniqueValues)
      {
         longValues.add(Long.parseLong(s));
      }

      return longValues;
   }

   public String getFirstValue()
   {
      return uniqueValues.first();
   }

   public String getLastValue()
   {
      return uniqueValues.last();
   }

   public int getValuesSize()
   {
      return uniqueValues.size();
   }
}
