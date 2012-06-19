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
package org.eclipse.stardust.ui.web.viewscommon.common.validator;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;


public class StructuredDataValidator implements Validator, StateHolder
{
   private static final String FLOAT_TYPE = "float";

   private static final String DOUBLE_TYPE = "double";

   private static final String BYTE_TYPE = "byte";

   private static final String SHORT_TYPE = "short";

   private static final String LONG_TYPE = "long";

   private static final String INT_TYPE = "int";

   private static final String DECIMAL_TYPE = "decimal";

   public static final String DURATION_REG_EXP_PATTERN = "^[0-9]{0,3}:([0-9]|10|11):(([3][0-6][0-5])|([3][0-5][0-9])|([0-2]{0,1}[0-9]{0,2})):(([2][0-3])|([0-1]{0,1}[0-9]{0,1})):([0-5]{0,1}[0-9]{0,1}):([0-5]{0,1}[0-9]{0,1})$";

   private String type;
   
   private boolean isTransient;
   
   public StructuredDataValidator()
   {
      // constructor needed for the managed bean
   }
   
   public StructuredDataValidator(String type)
   {
      this.type = type;
   }
   
   public void validateInt(FacesContext context, UIComponent component, Object value)
   {
      validateValue(INT_TYPE, value, context, component);
   }

   public void validateLong(FacesContext context, UIComponent component, Object value)
   {
      validateValue(LONG_TYPE, value, context, component);
   }

   public void validateShort(FacesContext context, UIComponent component, Object value)
   {
      validateValue(SHORT_TYPE, value, context, component);
   }

   public void validateByte(FacesContext context, UIComponent component, Object value)
   {
      validateValue(BYTE_TYPE, value, context, component);
   }

   public void validateDouble(FacesContext context, UIComponent component, Object value)
   {
      validateValue(DOUBLE_TYPE, value, context, component);
   }

   public void validateFloat(FacesContext context, UIComponent component, Object value)
   {
      validateValue(FLOAT_TYPE, value, context, component);
   }

   public void validateDecimal(FacesContext context, UIComponent component, Object value)
   {
      validateValue(DECIMAL_TYPE, value, context, component);
   }

   private static void validateValue(final String type, Object value,
         FacesContext context, UIComponent component)
   {
      if (value instanceof String)
      {
         try
         {
            if (type.equals(LONG_TYPE))
            {
               Long.valueOf((String) value);
            }
            if (type.equals(INT_TYPE))
            {
               Integer.valueOf((String) value);
            }
            else if (type.equals(SHORT_TYPE))
            {
               Short.valueOf((String) value);
            }
            else if (type.equals(BYTE_TYPE))
            {
               Byte.valueOf((String) value);
            }
            else if (type.equals(DOUBLE_TYPE))
            {
               Double.valueOf((String) value);
            }
            else if (type.equals(FLOAT_TYPE))
            {
               Float.valueOf((String) value);
            }
            else if (type.equals(DECIMAL_TYPE))
            {
               new BigDecimal((String) value);
            }
         }
         catch (NumberFormatException e)
         {
            throw new ValidatorException(
                  new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageFormat.format(
                        Localizer.getString(LocalizerKey.INVALID_DATA_VALUE),
                        new String[] {"'"+type+"'"}), null));

         }
      }
   }

   public void validate(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      validateValue(type, value, context, component);
   }
   
   public static Validator getValidator(String type)
   {
      return new StructuredDataValidator(type); 
   }
   
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      this.type = (String)values[0];
   }

   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[1];
      values[0] = this.type;
      return values;
   }

   public boolean isTransient()
   {
      return this.isTransient;
   }

   public void setTransient(boolean isTransient)
   {
      this.isTransient = isTransient;
   }

}
