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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.ByteConverter;
import javax.faces.convert.CharacterConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.ShortConverter;

import org.eclipse.stardust.ui.common.form.jsf.converter.PeriodConverter;
import org.eclipse.stardust.ui.common.form.jsf.utils.IceFacesHelper;
import org.eclipse.stardust.ui.web.common.util.CustomBooleanConverter;
import org.eclipse.stardust.ui.web.common.util.CustomDateConverter;
import org.eclipse.stardust.ui.web.common.util.CustomDateTimeConverter;
import org.eclipse.stardust.ui.web.common.util.CustomTimeConverter;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;

/**
 * @author Subodh.Godbole
 *
 */
public class IceFacesHelperExt extends IceFacesHelper
{
   @Override
   protected String getConverterId(Converter converter)
   {
      if (converter.getClass().equals(PriorityConverter.class))
      {
         return "ippDefaultPriorityConverter";
      }
      else if (converter.getClass().equals(CustomBooleanConverter.class))
      {
         return "ippDefaultBooleanConverter";
      }
      else if (converter.getClass().equals(ByteConverter.class))
      {
         return "ippByteConverter";
      }
      else if (converter.getClass().equals(ShortConverter.class))
      {
         return "ippDefaultShortConverter";
      }
      else if (converter.getClass().equals(IntegerConverter.class))
      {
         return "ippDefaultIntegerConverter";
      }
      else if (converter.getClass().equals(LongConverter.class))
      {
         return "ippDefaultLongConverter";
      }
      else if (converter.getClass().equals(FloatConverter.class))
      {
         return "ippDefaultFloatConverter";
      }
      else if (converter.getClass().equals(DoubleConverter.class))
      {
         return "ippDefaultDoubleConverter";
      }
      else if (converter.getClass().equals(BigDecimalConverter.class))
      {
         return "ippDefaultBigDecimalConverter";
      }
      else if (converter.getClass().equals(CharacterConverter.class))
      {
         return "ippDefaultCharacterConverter";
      }
      else if (converter.getClass().equals(CustomDateTimeConverter.class))
      {
         return "customDateTimeConverter";
      }
      else if (converter.getClass().equals(CustomDateConverter.class))
      {
         return "customDateConverter";
      }
      else if (converter.getClass().equals(CustomTimeConverter.class))
      {
         return "customTimeConverter";
      }
      else if (converter.getClass().equals(PeriodConverter.class))
      {
         return "ippDefaultPeriodConverter";
      }
      else
      {
         return null;
      }
   }
}
