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

import java.util.List;

import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.query.AbstractDataFilter;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceFilter;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.BinaryOperatorFilter;
import org.eclipse.stardust.engine.api.query.CurrentPartitionFilter;
import org.eclipse.stardust.engine.api.query.DocumentFilter;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.FilterEvaluationVisitor;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.PerformedByUserFilter;
import org.eclipse.stardust.engine.api.query.PerformingOnBehalfOfFilter;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceHierarchyFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceLinkFilter;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.api.query.StartingUserFilter;
import org.eclipse.stardust.engine.api.query.TernaryOperatorFilter;
import org.eclipse.stardust.engine.api.query.UnaryOperatorFilter;
import org.eclipse.stardust.engine.api.query.UserStateFilter;
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

   public static class BinaryOperatorFilterValueExtractor extends AbstractExtractorVisitor
   {
      private final String attribute;

      private final Binary operator;

      public BinaryOperatorFilterValueExtractor(String attribute, Binary operator)
      {
         this.attribute = attribute;
         this.operator = operator;
      }

      public Object visit(BinaryOperatorFilter filter, Object context)
      {
         if (operator.equals(filter.getOperator())
               && attribute.equals(filter.getAttribute()))
         {
            return filter.getValue();
         }
         return null;
      }
   }

   private static class AbstractExtractorVisitor implements FilterEvaluationVisitor
   {
      public Object visit(FilterTerm filter, Object context)
      {
         Object result = null;
         List<FilterCriterion> parts = filter.getParts();
         for (FilterCriterion criterion : parts)
         {
            Object value = criterion.accept(this, context);
            if (value != null)
            {
               result = value;
            }
         }
         // no parts means no restrictions
         return result;
      }

      public Object visit(UnaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(BinaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(TernaryOperatorFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessDefinitionFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceFilter filter, Object context)
      {
         return null;
      }

      public Object visit(StartingUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityInstanceFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ActivityStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingParticipantFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformingOnBehalfOfFilter filter, Object context)
      {
         return null;
      }

      public Object visit(PerformedByUserFilter filter, Object context)
      {
         return null;
      }

      public Object visit(AbstractDataFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ParticipantAssociationFilter filter, Object context)
      {
         return null;
      }

      public Object visit(CurrentPartitionFilter filter, Object context)
      {
         return null;
      }

      public Object visit(UserStateFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceLinkFilter filter, Object context)
      {
         return null;
      }

      public Object visit(ProcessInstanceHierarchyFilter filter, Object context)
      {
         return null;
      }

      public Object visit(DocumentFilter filter, Object context)
      {
         return null;
      }
   }
}
