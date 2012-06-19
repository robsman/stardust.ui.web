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
package org.eclipse.stardust.ui.web.viewscommon.common.spi;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.viewscommon.common.DateRangeChangeListener;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface IDescriptorFilterModel extends IFilterModel
{
   Serializable getFilterValue(String id);

   void setFilterValue(String id, Serializable value);

   Map/*<String, Serializable>*/ getFilterValues();

   List/*<GenericDataMapping>*/ getFilterableData();
   
   GenericDataMapping getFilterableData(String id);
   
   void resetFilterValues();

   void addDateRangeChangeListener(DateRangeChangeListener listener);

   DateRangeChangeListener getDateRangeChangeListener();
}
