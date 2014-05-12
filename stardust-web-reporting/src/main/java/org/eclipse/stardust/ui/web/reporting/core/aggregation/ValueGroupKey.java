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

public class ValueGroupKey<T> extends AbstractGroupKey<T>
{
   private StringBuffer internalKeyBuffer = new StringBuffer();
   public ValueGroupKey(T criteriaEntity)
   {
      super(criteriaEntity);
   }

   @Override
   public void addCriteria(Object criteria)
   {
      internalKeyBuffer.append(criteria);
   }

   @Override
   protected String getInternalKey()
   {
      return internalKeyBuffer.toString();
   }
}
