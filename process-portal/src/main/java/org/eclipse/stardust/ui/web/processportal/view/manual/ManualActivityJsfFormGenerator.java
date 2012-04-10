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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.util.Calendar;
import java.util.Date;

import javax.faces.convert.BigDecimalConverter;
import javax.faces.convert.ByteConverter;
import javax.faces.convert.CharacterConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.convert.ShortConverter;

import org.eclipse.stardust.ui.common.form.Indent;
import org.eclipse.stardust.ui.common.form.jsf.JsfFormGenerator;
import org.eclipse.stardust.ui.common.form.jsf.converter.PeriodConverter;
import org.eclipse.stardust.ui.common.form.jsf.utils.IceFacesHelper;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.web.common.util.CustomBooleanConverter;
import org.eclipse.stardust.ui.web.common.util.CustomDateConverter;
import org.eclipse.stardust.ui.web.common.util.CustomDateTimeConverter;
import org.eclipse.stardust.ui.web.common.util.CustomTimeConverter;

import com.icesoft.faces.component.ext.HtmlPanelGrid;

/**
 * @author Subodh.Godbole
 *
 */
public class ManualActivityJsfFormGenerator extends JsfFormGenerator
{
   /**
    * @param generationPreferences
    * @param formBinding
    */
   public ManualActivityJsfFormGenerator(FormGenerationPreferences generationPreferences, String formBinding)
   {
      super(generationPreferences, formBinding);
   }

   @Override
   protected Converter getConverter(Path path)
   {
      Converter converter = getCustomConverter(path);
      if (null != converter)
      {
         return converter;
      }
      else
      {
         return super.getConverter(path);
      }
   }
   
   /**
    * @param path
    * @return
    */
   private static Converter getCustomConverter(Path path)
   {
      Converter converter = null;

      if ("PROCESS_PRIORITY".equals(path.getId()))
      {
         converter = new PriorityConverter();
      }
      else if (path.getJavaClass().equals(Boolean.class) || path.getJavaClass().equals(Boolean.TYPE))
      {
         converter = new CustomBooleanConverter();
      }
      else if (path.getJavaClass().equals(Date.class) || path.getJavaClass().equals(Calendar.class))
      {
         if ("date".equals(path.getTypeName()))
         {
            return new CustomDateConverter();
         }
         else if ("time".equals(path.getTypeName()))
         {
            return new CustomTimeConverter();
         }
         else // if ("dateTime".equals(path.getTypeName()))
         {
            return new CustomDateTimeConverter();
         }
      }

      return converter;
   }

   /**
    * @param grid
    * @return
    */
   public String generateMarkup(HtmlPanelGrid grid)
   {
      return new IceFacesHelperExt().generateMarkup(grid, new Indent());
   }
   
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
}
