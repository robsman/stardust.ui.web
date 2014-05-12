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

public abstract class AbstractGroupKey<T>
{
   private T criteriaEntitiy;

   public AbstractGroupKey(T criteriaEntity)
   {
      this.criteriaEntitiy = criteriaEntity;
   }

   public T getCriteriaEntitiy()
   {
      return criteriaEntitiy;
   }

   public abstract void addCriteria(Object criteria);
   protected abstract String getInternalKey();

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getInternalKey() == null) ? 0 : getInternalKey().hashCode());
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
      AbstractGroupKey<T> other = (AbstractGroupKey<T>) obj;
      if (getInternalKey() == null)
      {
         if (other.getInternalKey() != null)
            return false;
      }
      else if (!getInternalKey().equals(other.getInternalKey()))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "GroupKey [internalKey=" + getInternalKey() + "]";
   }

}
