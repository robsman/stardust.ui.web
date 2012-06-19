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
package org.eclipse.stardust.ui.web.viewscommon.common.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRangeChangeListener;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IDescriptorFilterModel;


/**
 * @author rsauer
 * @version $Revision$
 */
public abstract class AbstractDescriptorFilterModel implements IDescriptorFilterModel
{
   private boolean enabled;

   private List/* <GenericDataMapping> */filterableData;

   private Map filterValues = new HashMap();
   
   private DateRangeChangeListener dateRangeChangeListener;

   public boolean isFilterEnabled()
   {
      return enabled && (null != filterableData) && !filterableData.isEmpty();
   }

   public void setFilterEnabled(boolean isEnabled)
   {
      this.enabled = isEnabled;
   }

   public List/* <GenericDataMapping> */getFilterableData()
   {
      return filterableData;
   }

   public GenericDataMapping getFilterableData(String id)
   {
      GenericDataMapping result = null;

      if (null != filterableData)
      {
         for (int i = 0; i < filterableData.size(); i++ )
         {
            GenericDataMapping mapping = ((GenericDataMapping) filterableData.get(i));
            if (id.equals(mapping.getId()))
            {
               result = mapping;
               break;
            }
         }
      }
      return result;
   }

   public void setFilterableData(List filterableData)
   {
      this.filterableData = Collections.unmodifiableList(filterableData);
   }

   public Map getFilterValues()
   {
      return filterValues;
   }

   public Serializable getFilterValue(String id)
   {
      Object value = filterValues.get(id);
      return (value instanceof Serializable) ? (Serializable) value : null;
   }

   public void resetFilterValues()
   {
      filterValues.clear();
   }

   public void setFilterValue(String id, Serializable value)
   {
      if ((value instanceof String) && StringUtils.isEmpty((String) value))
      {
         value = null;
      }

      if (null != value)
      {
         filterValues.put(id, value);
      }
      else
      {
         filterValues.remove(id);
      }
   }
   
   public DateRangeChangeListener getDateRangeChangeListener()
   {
      return dateRangeChangeListener;
   }

   public void addDateRangeChangeListener(DateRangeChangeListener listener)
   {
      this.dateRangeChangeListener = listener;
   }

}
