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
package org.eclipse.stardust.ui.web.common.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.DateTimeConverter;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;

import com.icesoft.faces.component.selectinputdate.SelectInputDate;

/**
 * @author Giridhara.G
 * @version
 */
public class CustomDateTimeConverter extends DateTimeConverter implements Serializable
{
   public static final String USE_SERVER_TIME_ZONE = "useServerTimeZone";
   
   /**
    * 
    */
   public CustomDateTimeConverter()
   {
      super();
      try
      {
         setTimeZone(PortalApplication.getInstance().getTimeZone());
         setPattern(DateUtils.getDateTimeFormat());
         setLocale(getLocale());
      }
      catch (Throwable t)
      {
         // Ignore
      }
   }

   /* (non-Javadoc)
    * @see javax.faces.convert.DateTimeConverter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
         throws ConverterException
   {
      try
      {
         return super.getAsObject(arg0, arg1, arg2);
      }
      catch (Exception e)
      {
         SimpleDateFormat sdf = new SimpleDateFormat(getPattern());

         FacesContext.getCurrentInstance().addMessage(
               arg1.getClientId(FacesContext.getCurrentInstance()),
               new FacesMessage(MessageFormat.format(
                     MessagePropertiesBean.getInstance().getString("common.converter.date.errorMsg"), new Object[] {
                           arg2, sdf.format(new Date())})));

         // If error occurs return the earlier valid input
         if(arg1 instanceof SelectInputDate)
            return ((SelectInputDate)arg1).getValue();
      }

      return null;
   }

   /* (non-Javadoc)
    * @see javax.faces.convert.DateTimeConverter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
         throws ConverterException
   {
      if(arg2 instanceof Date)
      {
         return super.getAsString(arg0, arg1, arg2);
      }
      else if (arg2 instanceof Calendar)
      {
         Object useServerTimeZoneObj = arg1.getAttributes().get(USE_SERVER_TIME_ZONE);
         if (useServerTimeZoneObj != null)
         {
            Boolean useServerTimeZone = (Boolean) useServerTimeZoneObj;
            if (useServerTimeZone.equals(true))
            {
               setTimeZone(((Calendar) arg2).getTimeZone());
            }
         }
         
         return super.getAsString(arg0, arg1, ((Calendar) arg2).getTime());
      }
      return null;
   }

}