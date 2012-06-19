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

import java.util.Date;

import org.eclipse.stardust.ui.web.common.util.CustomDateTimeConverter;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;


/**
 * @author yogesh.manware
 * 
 */
public class TableDataFilterDate extends TableDataFilterBetween
{
   private static final long serialVersionUID = 1L;

   /**
    * @param name
    * @param title
    * @param dataType
    * @param visible
    * @param startValue
    * @param endValue
    */
   public TableDataFilterDate(String name, String title, DataType dataType, boolean visible, Object startValue,
         Object endValue)
   {
      super(name, title, dataType, FilterCriteria.BETWEEN_DATE, visible, startValue, endValue);
   }

   /**
    * @param dataType
    */
   public TableDataFilterDate(DataType dataType)
   {
      super("", "", dataType, FilterCriteria.BETWEEN_DATE, true, "", "");
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
         return true;

      if (!(compareValue instanceof Date))
         throw new RuntimeException("Can not compare Non Date values");

      Date compareDate = (Date) compareValue;
      Date startDate = (Date) getStartValueAsDataType();
      Date endDate = (Date) getEndValueAsDataType();

      if (compareDate.equals(startDate) || compareDate.equals(startDate))
         return true;

      if (startDate != null && endDate != null)
      {
         return compareDate.after(startDate) && compareDate.before(endDate);
      }
      else if (startDate != null)
      {
         return compareDate.after(startDate);
      }
      else
      // if(endDate != null)
      {
         return compareDate.before(endDate);
      }

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

      return new TableDataFilterDate(getName(), getTitle(), getDataType(), isVisible(), start, end);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween#getConverter()
    */
   public Object getConverter()
   {
      return new CustomDateTimeConverter();
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
         Date startDate = (Date) startVal;
         Date endDate = (Date) endVal;

         if (startDate.after(endDate))
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
      if (value != null)
      {
         if (asClone)
         {
            return ((Date) value).clone();
         }
      }
      return value;
   }

   /**
    * @param value
    * @return
    */
   protected String getFormatedValue(final Object value)
   {
      Object returnValue = getReturnValue(value, false);
      if (returnValue != null)
      {
         return DateUtils.formatDateTime((Date) returnValue);
      }
      else
      {
         return "";
      }
   }
}
