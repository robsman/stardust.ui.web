/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC 

 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.DataType;

public class DescriptorColumnUtils
{

   /**
    * @param mappedType
    * @return
    */
	public static ColumnDataType determineColumnType(Class< ? > mappedType)
	{
		if (Boolean.class.equals(mappedType))
		{
			return ColumnDataType.BOOLEAN;
		}
		if (Date.class.equals(mappedType))
		{
			return ColumnDataType.DATE;
		}
		else if (determineNumberDataType(mappedType) != null)
		{
			return ColumnDataType.NUMBER;
		}
		else if(Document.class.equals(mappedType))
		{
			return ColumnDataType.DOCUMENT;
		}
		else if(List.class.equals(mappedType))
		{
			return ColumnDataType.LIST;
		}
		else
		{
			return ColumnDataType.STRING;
		}
	}

	/**
    * @param mappedType
    * @return
    */
   private static DataType determineNumberDataType(Class< ? > mappedType)
   {
      if (Byte.class.equals(mappedType))
      {
         return DataType.BYTE;
      }
      else if (Short.class.equals(mappedType))
      {
         return DataType.SHORT;
      }
      else if (Integer.class.equals(mappedType))
      {
         return DataType.INTEGER;
      }
      else if (Long.class.equals(mappedType))
      {
         return DataType.LONG;
      }
      else if (Float.class.equals(mappedType))
      {
         return DataType.FLOAT;
      }
      else if (Double.class.equals(mappedType))
      {
         return DataType.DOUBLE;
      }
      return null;
   }
   
   public static enum ColumnDataType
   {
      STRING,
      NUMBER,
      DATE,
      BOOLEAN,
      DOCUMENT,
      LIST,
      NONE
   }

}
