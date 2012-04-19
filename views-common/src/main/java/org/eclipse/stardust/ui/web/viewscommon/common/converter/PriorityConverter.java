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

import java.util.LinkedHashMap;
import java.util.Map;

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
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class PriorityConverter implements Converter
{
   // API field
   public static final String CONVERTER_ID = "org.eclipse.stardust.ui.web.processportal.view.manual.PriorityConverter";
    
   protected final static Logger trace = LogManager.getLogger(PriorityConverter.class);
   
   /* (non-Javadoc)
    * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException
   {
      if(!StringUtils.isEmpty(value))
      {
         int max = getHighestPriorityIdent();
         for (int i = getLowestPriorityIdent(); i <= max; i++)
         {
            String label = getPriorityLabel(i);
            if(value.equals(label))
            {
               return new Integer(i);
            }
         }
      }
      return null;
   }

   /* (non-Javadoc)
    * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
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
            return getPriorityLabel(Integer.parseInt((String)value));
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
      return ProcessInstanceUtils.getPriorityLabel(priorityIdent);
   }

   /**
    * @return
    */
   public static Map<String, String> getPossibleValues()
   {
      Map<String, String> values = new LinkedHashMap<String, String>();
      values.put(String.valueOf(ProcessInstancePriority.LOW), getPriorityLabel(ProcessInstancePriority.LOW));
      values.put(String.valueOf(ProcessInstancePriority.NORMAL), getPriorityLabel(ProcessInstancePriority.NORMAL));
      values.put(String.valueOf(ProcessInstancePriority.HIGH), getPriorityLabel(ProcessInstancePriority.HIGH));

      return values;
   }
}
