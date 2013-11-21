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

package org.eclipse.stardust.ui.web.processportal.service.rest;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.TYPE_ATT;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.core.pojo.data.QNameConstants;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.ui.web.common.util.DateUtils;

/**
 * Copied required functions from project stardust-engine-ws-cxf
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class DataFlowUtils
{
   public final static QName NOTES_NAMESPACE = new QName("http://eclipse.org/stardust/ws/v2012a/api", "Note");

   private static final Logger trace = LogManager.getLogger(DataFlowUtils.class);

   public static Type unmarshalPrimitiveType(QName type)
   {
      if ((null == type) || (QNameConstants.QN_STRING.equals(type)))
      {
         return Type.String;
      }
      else if (QNameConstants.QN_LONG.equals(type))
      {
         return Type.Long;
      }
      else if (QNameConstants.QN_INT.equals(type))
      {
         return Type.Integer;
      }
      else if (QNameConstants.QN_SHORT.equals(type))
      {
         return Type.Short;
      }
      else if (QNameConstants.QN_BYTE.equals(type))
      {
         return Type.Byte;
      }
      else if (QNameConstants.QN_DOUBLE.equals(type))
      {
         return Type.Double;
      }
      else if (QNameConstants.QN_FLOAT.equals(type))
      {
         return Type.Float;
      }
      else if (QNameConstants.QN_BOOLEAN.equals(type))
      {
         return Type.Boolean;
      }
      else if (QNameConstants.QN_DATETIME.equals(type))
      {
         return Type.Timestamp;
      }
      else if (QNameConstants.QN_CHAR.equals(type))
      {
         return Type.Char;
      }
      else if (QNameConstants.QN_BASE64BINARY.equals(type))
      {
         return Type.Calendar;
      }
      else
      {
         trace.warn("Unsupported primitive type code " + type);

         return null;
      }
   }

   public static QName marshalSimpleTypeXsdType(Class< ? > value)
   {
      QName ret = null;
      if (String.class.equals(value))
      {
         ret = QNameConstants.QN_STRING;
      }
      else if (Long.class.equals(value))
      {
         ret = QNameConstants.QN_LONG;
      }
      else if (Integer.class.equals(value))
      {
         ret = QNameConstants.QN_INT;
      }
      else if (Short.class.equals(value))
      {
         ret = QNameConstants.QN_SHORT;
      }
      else if (Byte.class.equals(value))
      {
         ret = QNameConstants.QN_BYTE;
      }
      else if (Double.class.equals(value))
      {
         ret = QNameConstants.QN_DOUBLE;
      }
      else if (Float.class.equals(value))
      {
         ret = QNameConstants.QN_FLOAT;
      }
      else if (Boolean.class.equals(value))
      {
         ret = QNameConstants.QN_BOOLEAN;
      }
      else if (Date.class.equals(value))
      {
         ret = QNameConstants.QN_DATETIME;
      }
      else if (Calendar.class.equals(value) || Money.class.equals(value))
      {
         ret = QNameConstants.QN_BASE64BINARY;
      }
      else if (Character.class.equals(value))
      {
         ret = QNameConstants.QN_CHAR;
      }
      return ret;
   }

   public static Serializable unmarshalPrimitiveValue(Model model, Data data,
         Class< ? > mappedType, String value)
   {
      Serializable result = null;

      if (mappedType == null)
      {
         Type primitiveType = (Type) data.getAttribute(TYPE_ATT);

         result = unmarshalPrimitiveValue(primitiveType, value);
      }
      else
      {
         // check on internal type (mappedType) instead of external QName
         result = unmarshalPrimitiveValue(marshalSimpleTypeXsdType(mappedType), value);
      }

      return result;
   }

   public static Serializable unmarshalPrimitiveValue(Model model, DataMapping dm,
         String value)
   {
      Data data = model.getData(dm.getDataId());

      return unmarshalPrimitiveValue(model, data, dm.getMappedType(), value);
   }

   public static Serializable unmarshalPrimitiveValue(QName type, String value)
   {
      Type targetType = unmarshalPrimitiveType(type);

      return unmarshalPrimitiveValue(targetType, value);
   }

   public static Serializable unmarshalPrimitiveValue(Type targetType, String value)
   {
      // TODO consider type codes
      if (StringUtils.isEmpty(value))
      {
         return null;
      }
      else if ((null == targetType) || (Type.String == targetType))
      {
         return value;
      }
      else if (Type.Long == targetType)
      {
         return Long.parseLong(value);
      }
      else if (Type.Integer == targetType)
      {
         return Integer.parseInt(value);
      }
      else if (Type.Short == targetType)
      {
         return Short.parseShort(value);
      }
      else if (Type.Byte == targetType)
      {
         return Byte.parseByte(new String(value));
      }
      else if (Type.Double == targetType)
      {
         return Double.parseDouble(value);
      }
      else if (Type.Float == targetType)
      {
         return Float.parseFloat(value);
      }
      else if (Type.Boolean == targetType)
      {
         return Boolean.parseBoolean(value);
      }
      else if (Type.Timestamp == targetType)
      {
         Date date = DateUtils.parseDateTime(value, "yyyy-MM-dd HH:mm:ss", Locale.getDefault(), TimeZone.getDefault());
         return date;
      }
      else if (Type.Char == targetType)
      {
         return value == null ? null : Character.valueOf(new String(value.getBytes()).charAt(0));
      }
      else if (Type.Calendar == targetType)
      {
         Date date = DateUtils.parseDateTime(value, "yyyy-MM-dd HH:mm:ss", Locale.getDefault(), TimeZone.getDefault());
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         return cal;
      }
      else
      {
         trace.warn("Ignoring primitive type code " + targetType);
      }
      return value;
   }
}
