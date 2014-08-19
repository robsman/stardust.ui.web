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
package org.eclipse.stardust.ui.web.business_object_management.service;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.AbstractDataFilter;
import org.eclipse.stardust.engine.api.query.FilterEvaluationVisitor;
import org.eclipse.stardust.engine.core.persistence.Operator.Binary;
import org.eclipse.stardust.engine.core.runtime.beans.ModelAwareQueryPredicate;

public class BusinessObjectQueryEvaluator extends ModelAwareQueryPredicate<IData>
{
   private Object pkValue;

   public BusinessObjectQueryEvaluator(BusinessObjectQuery query)
   {
      super(query);

      pkValue = query.getFilter().accept(new BinaryOperatorFilterValueExtractor(
            BusinessObjectQuery.PK_ATTRIBUTE, Binary.IS_EQUAL), null);
   }

   public Object getPkValue()
   {
      return pkValue;
   }

   @Override
   public boolean accept(IData data)
   {
      return BusinessObjectUtils.hasBusinessObject(data) && super.accept(data);
   }

   @Override
   protected FilterEvaluationVisitor createFilterEvaluationVisitor()
   {
      return new AbstractEvaluationVisitor()
      {
         @Override
         public Object visit(AbstractDataFilter filter, Object context)
         {
            return null;
         }
      };
   }

   public Object getValue(IData data, String attribute, Object expected)
   {
      if (BusinessObjectQuery.ID_ATTRIBUTE.equals(attribute))
      {
         return data.getId();
      }
      if (BusinessObjectQuery.PK_ATTRIBUTE.equals(attribute))
      {
         // (fh) Not filtered by pk.
         return expected;
      }
      return super.getValue(data, attribute, expected);
   }
}
