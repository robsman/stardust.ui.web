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
package org.eclipse.stardust.ui.web.viewscommon.common.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityLabelLocalizerKey;


public class PriorityConverter implements Converter
{
   // API field
   public static final String CONVERTER_ID = "org.eclipse.stardust.ui.web.viewscommon.common.converter.PriorityConverter";
    
   protected final static Logger trace = LogManager.getLogger(PriorityConverter.class);
   
   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException
   {
      if(!StringUtils.isEmpty(value))
      {
         int max = getHighestPriorityIdent();
         for (int i = getLowestPriorityIdent(); i <= max; i++)
         {
            // TODO should we cache the label? If so then we have to extend
            // the method with a new Locale paramater
            String label = Localizer.getString(new PriorityLabelLocalizerKey(i));
            if(value.equals(label))
            {
               return new Integer(i);
            }
         }
      }
      return null;
   }

   public String getAsString(FacesContext context, UIComponent component, Object value)
         throws ConverterException
   {
      if(value instanceof Number)
      {
         return getPriorityLabel(((Number)value).intValue());
      }
      if(value instanceof String)
      {
         try
         {
            return Localizer.getString(new PriorityLabelLocalizerKey(
                  Integer.parseInt((String)value)));
         }
         catch(NumberFormatException e)
         {
            
         }
      }
      return null;
   }
   
   protected int getLowestPriorityIdent()
   {
      return ProcessInstancePriority.LOW;
   }
   
   protected int getHighestPriorityIdent()
   {
      return ProcessInstancePriority.HIGH;
   }

   public static String getPriorityLabel(int priorityIdent)
   {
      return Localizer.getString(new PriorityLabelLocalizerKey(priorityIdent));
   }
}
