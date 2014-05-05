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
   private Integer sortIndex;

   public ValuesArray()
   {
      this.values = new ArrayList<Object>();
   }

   public void setSortIndex(Integer sortIndex)
   {
      this.sortIndex = sortIndex;
   }

   public void addValue(Object value)
   {
      values.add(value);
   }

   public Object getSortCriteria()
   {
      if(sortIndex != null)
      {
         return values.get(sortIndex);
      }

      return null;
   }

   public List<Object> getValues()
   {
      return values;
   }

   @Override
   public int compareTo(ValuesArray other)
   {
      Object thisSortObject = getSortCriteria();
      Object otherSortObject = other.getSortCriteria();
      return CompareHelper.compare(thisSortObject, otherSortObject);
   }
}
