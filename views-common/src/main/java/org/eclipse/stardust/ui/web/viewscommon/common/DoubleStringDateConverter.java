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
package org.eclipse.stardust.ui.web.viewscommon.common;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


public class DoubleStringDateConverter extends javax.faces.convert.DoubleConverter
{
   protected static final Logger trace = LogManager.getLogger(DoubleStringDateConverter.class);
   
   public static final String CONVERSION_ERROR_MESSAGE_ID = "ConversionError";

   public DoubleStringDateConverter()
   {
      super();
   }

   public String getAsString(FacesContext context, UIComponent component,
       Object value) throws ConverterException 
   {
       if (value != null && value instanceof Double) 
       {
          try
          {
             return DateUtils.formatDurationAsString(((Double)value).doubleValue());
          } 
          catch (ClassCastException ce) 
          {
              throw new ConverterException("cannot cast '" + value + "' to Double");
          }
       }

       return "-";
   }
}
