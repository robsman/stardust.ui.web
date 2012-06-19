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
package org.eclipse.stardust.ui.web.common.filter;

import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.ShortConverter;

import org.eclipse.stardust.ui.web.common.util.ByteConverter;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author yogesh.manware
 * 
 */
public class TableDataFilterNumber extends TableDataFilterBetween
{
   private static final long serialVersionUID = 4276094614258630061L;

   /**
    * @param name
    * @param title
    * @param dataType
    * @param visible
    * @param startValue
    * @param endValue
    */
   public TableDataFilterNumber(String name, String title, DataType dataType, boolean visible, Object startValue,
         Object endValue)
   {
      super(name, title, dataType, determineFilterCriteria(dataType), visible, startValue, endValue);
   }

   /**
    * @param dataType
    */
   public TableDataFilterNumber(DataType dataType)
   {
      this("", "", dataType, true, "", "");
   }

   /**
    * @param supportsRange
    *           the supportsRange to set
    */
   public static FilterCriteria determineFilterCriteria(DataType dataType)
   {
      if (DataType.DOUBLE.equals(dataType) || DataType.FLOAT.equals(dataType))
      {
         return FilterCriteria.NUMBER;
      }
      else
      {
         return FilterCriteria.BETWEEN_NUMBER;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#contains(java.lang.Object)
    */
   public boolean contains(final Object compareValue)
   {
      if (!isFilterSet() || compareValue == null)
      {
         return true;
      }

      if (isLong())
      {
         if (!(compareValue instanceof Long))
         {
            throw new RuntimeException("Can not compare Non Long values");
         }

         Long compareNumber = (Long) compareValue;
         Long startNumber = (Long) getStartValueAsDataType();
         Long endNumber = (Long) getEndValueAsDataType();

         if (startNumber != null && endNumber != null)
         {
            return compareNumber >= startNumber && compareNumber <= endNumber;
         }
         else if (startNumber != null)
         {
            return compareNumber >= startNumber;
         }
         else
         // if (endNumber != null)
         {
            return compareNumber <= endNumber;
         }
      }
      else if (isInteger())
      {
         if (!(compareValue instanceof Integer))
         {
            throw new RuntimeException("Can not compare Non Integer values");
         }

         Integer compareNumber = (Integer) compareValue;
         Integer startNumber = (Integer) getStartValueAsDataType();
         Integer endNumber = (Integer) getEndValueAsDataType();

         if (startNumber != null && endNumber != null)
         {
            return compareNumber >= startNumber && compareNumber <= endNumber;
         }
         else if (startNumber != null)
         {
            return compareNumber >= startNumber;
         }
         else
         // if (endNumber != null)
         {
            return compareNumber <= endNumber;
         }
      }
      else if (isDouble())
      {
         if (!(compareValue instanceof Double))
         {
            throw new RuntimeException("Can not compare Non Double values");
         }

         Double compareNumber = (Double) compareValue;
         Double startNumber = (Double) getStartValueAsDataType();
         Double endNumber = (Double) getEndValueAsDataType();

         if (startNumber != null && endNumber != null)
         {
            return compareNumber >= startNumber && compareNumber <= endNumber;
         }
         else if (startNumber != null)
         {
            return compareNumber >= startNumber;
         }
         else
         // if (endNumber != null)
         {
            return compareNumber <= endNumber;
         }
      }
      else if (isFloat())
      {
         if (!(compareValue instanceof Float))
         {
            throw new RuntimeException("Can not compare Non Float values");
         }

         Float compareNumber = (Float) compareValue;
         Float startNumber = (Float) getStartValueAsDataType();
         Float endNumber = (Float) getEndValueAsDataType();

         if (startNumber != null && endNumber != null)
         {
            return compareNumber >= startNumber && compareNumber <= endNumber;
         }
         else if (startNumber != null)
         {
            return compareNumber >= startNumber;
         }
         else
         // if (endNumber != null)
         {
            return compareNumber <= endNumber;
         }
      }
      else if (isShort())
      {
         if (!(compareValue instanceof Short))
         {
            throw new RuntimeException("Can not compare Non Short values");
         }

         Short compareNumber = (Short) compareValue;
         Short startNumber = (Short) getStartValueAsDataType();
         Short endNumber = (Short) getEndValueAsDataType();

         if (startNumber != null && endNumber != null)
         {
            return compareNumber >= startNumber && compareNumber <= endNumber;
         }
         else if (startNumber != null)
         {
            return compareNumber >= startNumber;
         }
         else
         // if (endNumber != null)
         {
            return compareNumber <= endNumber;
         }
      }
      else if (isByte())
      {
         if (!(compareValue instanceof Byte))
         {
            throw new RuntimeException("Can not compare Non Byte values");
         }

         Byte compareNumber = (Byte) compareValue;
         Byte startNumber = (Byte) getStartValueAsDataType();
         Byte endNumber = (Byte) getEndValueAsDataType();

         if (startNumber != null && endNumber != null)
         {
            return compareNumber >= startNumber && compareNumber <= endNumber;
         }
         else if (startNumber != null)
         {
            return compareNumber >= startNumber;
         }
         else
         // if (endNumber != null)
         {
            return compareNumber <= endNumber;
         }
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getClone()
    */
   public ITableDataFilter getClone()
   {
      Object start = getReturnValue(getStartValue(), true);
      Object end = getReturnValue(getEndValue(), true);

      return new TableDataFilterNumber(getName(), getTitle(), getDataType(), isVisible(), start, end);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween#getConverter()
    */
   public Object getConverter()
   {
      if (isLong())
      {
         return new LongConverter();
      }
      else if (isInteger())
      {
         return new IntegerConverter();
      }
      else if (isDouble())
      {
         return new DoubleConverter();
      }
      else if (isFloat())
      {
         return new FloatConverter();
      }
      else if (isShort())
      {
         return new ShortConverter();
      }
      else if (isByte())
      {
         return new ByteConverter();
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getValidationMessage()
    */
   public String getValidationMessage()
   {
      String validationMessage = "";
      Object startVal = getStartValueAsDataType();
      Object endVal = getEndValueAsDataType();

      if (startVal != null && endVal != null)
      {
         boolean invalidRange = false;
         if (isLong())
         {
            Long startNumber = (Long) startVal;
            Long endNumber = (Long) endVal;

            if (startNumber > endNumber)
            {
               invalidRange = true;
            }
         }
         else if (isInteger())
         {
            Integer startNumber = (Integer) startVal;
            Integer endNumber = (Integer) endVal;

            if (startNumber > endNumber)
            {
               invalidRange = true;
            }
         }
         else if (isDouble())
         {
            Double startNumber = (Double) startVal;
            Double endNumber = (Double) endVal;

            if (startNumber > endNumber)
            {
               invalidRange = true;
            }
         }
         else if (isFloat())
         {
            Float startNumber = (Float) startVal;
            Float endNumber = (Float) endVal;

            if (startNumber > endNumber)
            {
               invalidRange = true;
            }
         }
         else if (isShort())
         {
            Short startNumber = (Short) startVal;
            Short endNumber = (Short) endVal;

            if (startNumber > endNumber)
            {
               invalidRange = true;
            }
         }
         else if (isByte())
         {
            Byte startNumber = (Byte) startVal;
            Byte endNumber = (Byte) endVal;

            if (startNumber > endNumber)
            {
               invalidRange = true;
            }
         }

         if (invalidRange)
         {
            validationMessage = MessagePropertiesBean.getInstance().getString(
                  "common.filterPopup.betweenFilter.message.rangeNotValid");
         }
      }
      return validationMessage;
   }

   /**
    * @param value
    * @param asClone
    * @return
    */
   protected Object getReturnValue(final Object value, boolean asClone)
   {
      Object retValue = null;
      String strValue = null;
      if (value != null)
      {
         if (value instanceof String && !StringUtils.isEmpty((String) value))
         {
            strValue = (String) value;
            if (isLong())
            {
               retValue = Long.valueOf(strValue);
            }
            else if (isInteger())
            {
               retValue = Integer.valueOf((String) value);
            }
            else if (isDouble())
            {
               retValue = Double.valueOf((String) value);
            }
            else if (isFloat())
            {
               retValue = Float.valueOf((String) value);
            }
            else if (isShort())
            {
               retValue = Short.valueOf((String) value);
            }
            else if (isByte())
            {
               retValue = Byte.valueOf((String) value);
            }
         }
         else
         {
            return value;
         }
      }
      return retValue;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.filter.TableDataFilterBetween#getFormatedValue(java
    * .lang.Object)
    */
   protected String getFormatedValue(final Object value)
   {
      Object returnValue = getReturnValue(value, false);
      if (returnValue != null)
      {
         return String.valueOf(returnValue);
      }
      else
      {
         return "";
      }
   }

   private boolean isLong()
   {
      return getDataType() == DataType.LONG;
   }

   private boolean isInteger()
   {
      return getDataType() == DataType.INTEGER;
   }

   private boolean isDouble()
   {
      return getDataType() == DataType.DOUBLE;
   }

   private boolean isFloat()
   {
      return getDataType() == DataType.FLOAT;
   }

   private boolean isShort()
   {
      return getDataType() == DataType.SHORT;
   }

   private boolean isByte()
   {
      return getDataType() == DataType.BYTE;
   }
}