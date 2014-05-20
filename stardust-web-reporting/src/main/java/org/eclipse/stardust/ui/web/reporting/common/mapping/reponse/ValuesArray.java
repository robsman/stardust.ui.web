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
package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CompareHelper;

public class ValuesArray implements Comparable<ValuesArray>
{
   private List<Object> values;
   private Integer dimensionIndex;

   public ValuesArray()
   {
      this.values = new ArrayList<Object>();
   }

   public void setDimensionIndex(Integer dimensionIndex)
   {
      this.dimensionIndex = dimensionIndex;
   }

   public Integer getDimensionIndex()
   {
      return dimensionIndex;
   }

   public void addValue(Object value)
   {
      values.add(value);
   }

   public Object getDimensionValue()
   {
      if(dimensionIndex != null)
      {
         return values.get(dimensionIndex);
      }

      return null;
   }

   protected String getInternalKey()
   {
      StringBuffer b = new StringBuffer();
      b.append(getDimensionValue());
      return b.toString();
   }

   public List<Object> getValues()
   {
      return values;
   }

   @Override
   public int hashCode()
   {
      final String internalKey = getInternalKey();
      final int prime = 31;
      int result = 1;
      result = prime * result + ((internalKey == null) ? 0 : internalKey.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      final String internalKey = getInternalKey();

      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ValuesArray other = (ValuesArray) obj;
      final String otherInternalKey = other.getInternalKey();

      if (internalKey == null)
      {
         if (otherInternalKey != null)
            return false;
      }
      else if (!internalKey.equals(otherInternalKey))
         return false;
      return true;
   }

   @Override
   public int compareTo(ValuesArray other)
   {
      Object thisSortObject = getDimensionValue();
      Object otherSortObject = other.getDimensionValue();
      return CompareHelper.compare(thisSortObject, otherSortObject);
   }
}
