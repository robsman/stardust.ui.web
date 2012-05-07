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

import java.util.Calendar;
import java.util.Date;

import javax.faces.convert.Converter;

import org.eclipse.stardust.ui.common.form.Indent;
import org.eclipse.stardust.ui.common.form.jsf.ILabelProvider;
import org.eclipse.stardust.ui.common.form.jsf.JsfFormGenerator;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.common.introspection.Path;
import org.eclipse.stardust.ui.web.common.util.CustomBooleanConverter;
import org.eclipse.stardust.ui.web.common.util.CustomDateConverter;
import org.eclipse.stardust.ui.web.common.util.CustomDateTimeConverter;
import org.eclipse.stardust.ui.web.common.util.CustomTimeConverter;
import org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter;

import com.icesoft.faces.component.ext.HtmlPanelGrid;

/**
 * @author Subodh.Godbole
 *
 */
public class IppJsfFormGenerator extends JsfFormGenerator
{
   /**
    * @param generationPreferences
    * @param formBinding
    */
   public IppJsfFormGenerator(FormGenerationPreferences generationPreferences, String formBinding)
   {
      super(generationPreferences, formBinding);
   }

   /**
    * @param generationPreferences
    * @param formBinding
    * @param labelProvider
    */
   public IppJsfFormGenerator(FormGenerationPreferences generationPreferences, String formBinding,
         ILabelProvider labelProvider)
   {
      super(generationPreferences, formBinding, labelProvider);
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
    * @param indent
    * @return
    */
   public String generateMarkup(HtmlPanelGrid grid, Indent indent)
   {
      return new IceFacesHelperExt().generateMarkup(grid, null == indent ? new Indent() : indent);
   }
}
