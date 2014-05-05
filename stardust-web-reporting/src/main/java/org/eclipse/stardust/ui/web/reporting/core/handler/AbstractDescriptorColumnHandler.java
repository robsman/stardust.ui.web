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
package org.eclipse.stardust.ui.web.reporting.core.handler;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.Serialization;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterCriterion;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilter.OperatorType;
import org.eclipse.stardust.ui.web.reporting.common.mapping.request.ReportFilterMetaData;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public abstract class AbstractDescriptorColumnHandler<U extends IDescriptorProvider, V extends Query>
   extends AbstractColumnHandler<Object, U, V> implements IFactValueProvider<U>
{
   @Override
   public void applyFilter(V query, ReportFilter filter)
   {
      ReportFilterMetaData metadata = filter.getMetadata();
      String dimension = filter.getDimension();

      // split the full qualified activity namedimension in format
      // FullQualifiedProcessId:FullQualifiedActivityId
      StringTokenizer st = new StringTokenizer(dimension, ":");
      st.nextToken();
      String dataId = st.nextToken();
      String xPath = metadata.getXPath();
      boolean isStructuredType = metadata.isStructuredType();
      String javaType = metadata.getJavaType();

      String operator = filter.getOperator();
      OperatorType operatorType = OperatorType.valueOf(operator);
      JsonElement rawFilterValue = filter.getValue();

      final FilterCriterion filterCriterion;
      final Serializable dataValue;
      final Collection<Serializable> dataValues;
      switch (operatorType)
      {
         case E:
            dataValue = getDataFilterValue(rawFilterValue, javaType);
            filterCriterion = isStructuredType
                  ? DataFilter.isEqual(dataId, xPath, dataValue)
                  : DataFilter.isEqual(dataId, dataValue);
            break;
         case NE:
            dataValue = getDataFilterValue(rawFilterValue, javaType);
            filterCriterion = isStructuredType
                  ? DataFilter.notEqual(dataId, xPath, dataValue)
                  : DataFilter.notEqual(dataId, dataValue);
            break;
         case LE:
            dataValue = getDataFilterValue(rawFilterValue, javaType);
            filterCriterion = isStructuredType
                  ? DataFilter.lessOrEqual(dataId, xPath, dataValue)
                  : DataFilter.lessOrEqual(dataId, dataValue);
            break;
         case GE:
            dataValue = getDataFilterValue(rawFilterValue, javaType);
            filterCriterion = isStructuredType
                  ? DataFilter.greaterOrEqual(dataId, xPath, dataValue)
                  : DataFilter.greaterOrEqual(dataId, dataValue);
            break;
         case B:
            JsonPrimitive fromPrimitive = JsonUtil.getPrimitiveProperty(
                  filter.getValue(), "From");
            Serializable lowerBound = getDataFilterValue(fromPrimitive, javaType);

            JsonPrimitive toPrimitive = JsonUtil.getPrimitiveProperty(filter.getValue(),
                  "To");
            Serializable upperBound = getDataFilterValue(toPrimitive, javaType);

            filterCriterion = isStructuredType
                  ? DataFilter.between(dataId, xPath, lowerBound, upperBound)
                  : DataFilter.between(dataId, lowerBound, upperBound) ;
            break;
         case I:
            dataValues = getDataFilterValues(rawFilterValue, javaType);
            filterCriterion = isStructuredType
                  ? DataFilter.in(dataId, xPath, dataValues)
                  : DataFilter.in(dataId, dataValues);
            break;
          case NI:
             dataValues = getDataFilterValues(rawFilterValue, javaType);
             filterCriterion = isStructuredType
                   ? DataFilter.notIn(dataId, xPath, dataValues)
                   : DataFilter.notIn(dataId, dataValues);
          break;
         case L:
            dataValue = getDataFilterValue(rawFilterValue, javaType);
            filterCriterion = isStructuredType
                  ? DataFilter.like(dataId, xPath, (String) dataValue)
                  : DataFilter.like(dataId, (String) dataValue);
            break;
         default:
            throw new RuntimeException("Unsupported Operator Type? " + operator);
      }

      query.where(filterCriterion);
   }

   private Collection<Serializable> getDataFilterValues(JsonElement jsonElement,
         String javaType)
   {
      Collection<Serializable> filterValues = new ArrayList<Serializable>();
      JsonArray jsonArray = jsonElement.getAsJsonArray();
      for(JsonElement element: jsonArray)
      {
         filterValues.add(element.getAsString());
      }

      return filterValues;
   }

   private Serializable getDataFilterValue(JsonElement jsonElement, String javaType)
   {
      try
      {
         Class< ? > javaTypeClass = Class.forName(javaType);

         if (javaTypeClass.isAssignableFrom(String.class))
         {
            return jsonElement.getAsString();
         }
         else if (javaTypeClass.isAssignableFrom(Short.class))
         {
            return jsonElement.getAsShort();
         }
         else if (javaTypeClass.isAssignableFrom(Integer.class))
         {
            return jsonElement.getAsInt();
         }
         else if (javaTypeClass.isAssignableFrom(Long.class))
         {
            return jsonElement.getAsLong();
         }
         else if (javaTypeClass.isAssignableFrom(Byte.class))
         {
            return jsonElement.getAsByte();
         }
         else if (javaTypeClass.isAssignableFrom(Boolean.class))
         {
            return jsonElement.getAsBoolean();
         }
         else if (javaTypeClass.isAssignableFrom(Date.class))
         {
            return JsonUtil.getPrimitiveValueAsDate(jsonElement);
         }
         else if (javaTypeClass.isAssignableFrom(Float.class))
         {
            return jsonElement.getAsFloat();
         }
         else if (javaTypeClass.isAssignableFrom(Double.class))
         {
            return jsonElement.getAsDouble();
         }
         else if (javaTypeClass.isAssignableFrom(Character.class))
         {
            return jsonElement.getAsCharacter();
         }
         else if (javaTypeClass.isAssignableFrom(Money.class))
         {
            return new Money(jsonElement.getAsString());
         }
         else if (javaTypeClass.isAssignableFrom(BigDecimal.class))
         {
            return jsonElement.getAsBigDecimal();
         }
         else if (javaTypeClass.isAssignableFrom(Serializable.class))
         {
            try
            {
               return Serialization.deserializeObject(jsonElement.getAsString()
                     .getBytes());
            }
            catch (IOException e)
            {
               throw new RuntimeException("Could not deserialize value: "
                     + jsonElement.getAsString());
            }
         }

         throw new ClassNotFoundException();
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException("Unsupported java type: " + javaType, e);
      }
   }

   protected void raiseUnsupportedDescriptorTypeException(HandlerContext context)
   {
      RequestColumn column = context.getColumn();
      StringBuffer errorMsg = new StringBuffer();
      errorMsg.append("Descriptor: "+column.getId());
      errorMsg.append(" is not of numeric type");
      throw new RuntimeException(errorMsg.toString());
   }

   @Override
   public boolean canFilter(V query, ReportFilter filter)
   {
      return filter.isDescriptor();
   }

   @Override
   public Number provideFactValue(HandlerContext context, U t)
   {
      Object descriptorValue = provideObjectValue(context, t);
      if(descriptorValue != null )
      {
         if(descriptorValue instanceof Number)
         {
            return (Number) descriptorValue;
         }
         else
         {
            raiseUnsupportedDescriptorTypeException(context);
         }
      }

      return 0;
   }

   @Override
   public Object provideObjectValue(HandlerContext context, U t)
   {
      String descriptorId = context.getColumn().getId();
      return t.getDescriptorValue(descriptorId);
   }
}
