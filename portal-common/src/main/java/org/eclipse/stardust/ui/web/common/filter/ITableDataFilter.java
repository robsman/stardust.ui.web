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
package org.eclipse.stardust.ui.web.common.filter;

import java.io.Serializable;

import org.eclipse.stardust.ui.web.common.table.IRowModel;


/**
 * @author Subodh.Godbole
 *
 */
public interface ITableDataFilter extends Serializable
{
   static enum FilterCriteria
   {
      SEARCH,
      BETWEEN_DATE,
      BETWEEN_NUMBER,
      NUMBER,
      ONOFF,
      SELECT_ONE,
      SELECT_MANY,
      NONE
   }
   
   static enum DataType
   {
      LONG,
      INTEGER,
      DOUBLE,
      FLOAT,
      BYTE,
      SHORT,
      STRING,
      DATE,
      BOOLEAN,
      NONE
   }
   
   String getId();
   String getName();
   void setName(String name);
   String getProperty();
   void setProperty(String property);
   String getTitle();
   DataType getDataType();
   FilterCriteria getFilterCriteria();
   boolean isFilterSet();
   void resetFilter();
   boolean isVisible();
   void setVisible(boolean visible);
   boolean isFilterOut(IRowModel rowData);
   String getFilterSummaryTitle();
   boolean contains(final Object compareValue);
   ITableDataFilter getClone();
   void copyValues(ITableDataFilter dataFilterToCopy);
   
   String getValidationMessage();
}
