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

public class IdentityGroupKey<T> extends AbstractGroupKey<T>
{
   public IdentityGroupKey(T criteriaEntity)
   {
      super(criteriaEntity);
   }

   @Override
   public void addCriteria(Object criteria)
   {

   }

   @Override
   protected String getInternalKey()
   {
      T criteriaEntitiy = getCriteriaEntitiy();
      if(criteriaEntitiy != null)
      {
         return new Integer(criteriaEntitiy.hashCode()).toString();
      }

      return null;
   }
}
