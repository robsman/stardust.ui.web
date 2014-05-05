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
package org.eclipse.stardust.ui.web.reporting.core.aggregation;

public class ValueGroupKey<T>
{
   private StringBuffer internalKey = new StringBuffer();
   private T criteriaEntitiy;

   public ValueGroupKey(T criteriaEntitiy)
   {
      this.criteriaEntitiy = criteriaEntitiy;
   }

   public void addKeyCriteria(Object criteria)
   {
      internalKey.append(criteria);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((internalKey == null) ? 0 : internalKey.toString().hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      @SuppressWarnings("unchecked")
      ValueGroupKey<T> other = (ValueGroupKey<T>) obj;
      if (internalKey == null)
      {
         if (other.internalKey != null)
            return false;
      }
      else if (!internalKey.toString().equals(other.internalKey.toString()))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ValueGroupKey [internalKey=" + internalKey.toString() + "]";
   }

   public T getCriteriaEntitiy()
   {
      return criteriaEntitiy;
   }
}
