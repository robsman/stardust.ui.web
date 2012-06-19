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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.SpiConstants;



public class FilterProviderUtil
{
   private static FilterProviderUtil util;
   private List<IFilterProvider> filterProviders;
   private static final Logger trace = LogManager.getLogger(FilterProviderUtil.class);
   public static final String V_WORKLIST = "worklist";
   public static final String M_WORKFLOW = "ipp-workflow-perspective";
   public static final String F_PROVIDERS = "prefs.filterProviders";

   private FilterProviderUtil()
   {
   }

   public static FilterProviderUtil getInstance()
   {
      if (util == null)
      {         
         util = new FilterProviderUtil();
         util.initializeFilterProviders();
      }
      return util;
   }

       /**
	    * 
	    */
   public void initializeFilterProviders()
   {
      filterProviders = new ArrayList<IFilterProvider>();

      boolean filtersDefined = initializeFilterProvidersFromProperties();
      if (!filtersDefined)
      {
         initializeFilterProvidersFromJcr();
         trace.info("Filter Providers are loaded from JCR");
      }
      else
      {
         trace.info("Filter Providers are loaded from properties file");
      }

      trace.info("Filter Providers loaded are: " + filterProviders);
   }

   /**
    * @return
    */
   private void initializeFilterProvidersFromJcr()
   {
      try
      {
         IFilterProvider filterProvider;
         Object filterProviderObject;

         Collection<String> classNames = getFilterProvidersFromPreferences().values();

         if (classNames != null && classNames.size() > 0)
         {
            for (String className : classNames)
            {
               try
               {
                  trace.info("Instantiating Filter Provider: " + className);
                  filterProviderObject = Class.forName(className).newInstance();

                  if (filterProviderObject instanceof IFilterProvider)
                  {
                     filterProvider = (IFilterProvider) filterProviderObject;
                     filterProviders.add(filterProvider);
                  }
                  else
                  {
                     trace.error("'" + className + "' is not an instance of IFilterProvider");
                  }
               }
               catch (Exception e)
               {
                  trace.error("Error while instantiating Filter Provider '" + className + "'", e);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Error in initializeWorklistFilterProviders", e);
      }
   }

   /**
    * @return
    */
   private Map<String, String> getFilterProvidersFromPreferences()
   {
      String value;
      String[] values;
      boolean formatCorrect;
      Map<String, String> filterProviders = new HashMap<String, String>();

      StringTokenizer tokens = new StringTokenizer(getFilterProviderPreferences(), ",");
      while (tokens.hasMoreTokens())
      {
         value = tokens.nextToken();
         if (StringUtils.isEmpty(value))
         {
            continue;
         }
         formatCorrect = false;

         if (value.indexOf("=") > 0)
         {
            values = value.split("=");
            if (values.length == 2)
            {
               filterProviders.put(values[0].trim(), values[1].trim());
               formatCorrect = true;
            }
         }

         if (!formatCorrect)
         {
            trace.error("Skipping Filter Provider, Value not in the required format - " + value);
         }
      }

      return filterProviders;
   }

   /**
    * @return
    */
   public String getFilterProviderPreferences()
   {
      return getUserPreferencesHelper().getSingleString(V_WORKLIST, F_PROVIDERS);
   }

   /**
    * @return
    */
   private UserPreferencesHelper getUserPreferencesHelper()
   {
      // Filter Providers are always saved in PARTITION and never at USER
      // scope
      return UserPreferencesHelper.getInstance(M_WORKFLOW, PreferenceScope.PARTITION);
   }

   /**
	    * 
	    */
   private boolean initializeFilterProvidersFromProperties()
   {
      boolean filtersDefined = false;
      String configuredFilterProviders = Parameters.instance().getString(SpiConstants.FILTER_PROVIDERS);
      if (!StringUtils.isEmpty(configuredFilterProviders))
      {
         filtersDefined = true;
         for (Iterator<String> i = StringUtils.split(configuredFilterProviders, ","); i.hasNext();)
         {
            String providerId = (i.next()).trim();
            if (!StringUtils.isEmpty(providerId))
            {
               Object provider = Parameters.instance().get(SpiConstants.FILTER_PROVIDER_PREFIX + providerId);
               if (provider instanceof IFilterProvider)
               {
                  ((IFilterProvider) provider).setFilterId(providerId);
                  filterProviders.add((IFilterProvider) provider);
               }
               else if (provider instanceof String)
               {
                  Object instance = Reflect.createInstance((String) provider);
                  if (instance instanceof IFilterProvider)
                  {
                     ((IFilterProvider) instance).setFilterId(providerId);
                     filterProviders.add((IFilterProvider) instance);
                  }
               }
            }
         }
      }

      /*
       * Not sure if this is relevant here filterProviders.add(new
       * DescriptorFilterProvider()); filterProviders.add(new
       * WorklistActivityStartTimeFilterProvider()); filterProviders.add(new
       * WorklistActivityNameFilterProvider()); filterProviders.add(new
       * WorklistDescriptorFilterProvider());
       */

      return filtersDefined;
   }

   public List<IFilterProvider> getFilterProviders()
   {
      return filterProviders;
   }

}
