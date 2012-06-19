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
package org.eclipse.stardust.ui.web.viewscommon.descriptors;

import java.util.Collections;
import java.util.Set;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.AbstractFilterProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterModel;


public class DescriptorFilterProvider extends AbstractFilterProvider
{
   private static final long serialVersionUID = 1L;

   protected static final Logger trace = LogManager.getLogger(DescriptorFilterProvider.class);
   
   public final static String FILTER_ID = "descriptors";

   private boolean refresh;

   private String dynamicFiltersURL;

   private boolean dynamicFiltersURLRefresh;
   
   private FilterTerm filterTerm;

   public DescriptorFilterProvider()
   {
      setFilterId(FILTER_ID);
   }
   
   public void init()
   {
      refresh = false;
      dynamicFiltersURL = null;
      dynamicFiltersURLRefresh = false;
   }

   public Set<String> getProcessDomain(IFilterModel abstractFilterModel)
   {
      Set<String> result = null;
      
      GenericDescriptorFilterModel filterModel = (abstractFilterModel instanceof GenericDescriptorFilterModel)
            ? (GenericDescriptorFilterModel) abstractFilterModel
            : null;
      
      if ((null != filterModel) && filterModel.isFilterEnabled())
      {
         result = Collections.singleton(filterModel.getProcessId());
      }
      return result;
   }
   
   public boolean isGlobalWorklistFilter()
   {
      return true;
   }

   public void applyFilter(Query query)
   {
      GenericDescriptorFilterModel filterModel = (getFilterModel() instanceof GenericDescriptorFilterModel)
            ? (GenericDescriptorFilterModel) getFilterModel() : null;

      if ((null != filterModel) && filterModel.isFilterEnabled())
      {
         FilterTerm predicate;
         if ( !StringUtils.isEmpty(filterModel.getProcessId()))
         {
            predicate = query.getFilter().addAndTerm();
            
            predicate.add(new ProcessDefinitionFilter(filterModel.getProcessId(), false));
         }
         else if (null != filterTerm)
         {
            predicate = filterTerm;
         }
         else
         {
            predicate = query.getFilter();
         }

         DescriptorFilterUtils.applyDescriptorDataFilters(predicate, filterModel,
               query.getClass(), Constants.isSearchCaseSensitive());
      }
   }

   public void update()
   {
      refresh = false;
   }

   public boolean getRefresh()
   {
      boolean tmp = refresh;
      refresh = false;
      return tmp;
   }

   public void setRefresh(boolean refresh)
   {
      this.refresh = refresh;
   }

   public String getDynamicFiltersURL()
   {
      return dynamicFiltersURL;
   }

   public void setDynamicFiltersURL(String dynamicFiltersURL)
   {
      this.dynamicFiltersURL = dynamicFiltersURL;
   }

   public boolean getDynamicFiltersURLRefresh()
   {
      return dynamicFiltersURLRefresh;
   }

   public void setDynamicFiltersURLRefresh(boolean dynamicFiltersURLRefresh)
   {
      this.dynamicFiltersURLRefresh = dynamicFiltersURLRefresh;
   }   

   public FilterTerm getFilterTerm()
   {
      return filterTerm;
   }

   public void setFilterTerm(FilterTerm filterTerm)
   {
      this.filterTerm = filterTerm;
   }  
   
   
}
