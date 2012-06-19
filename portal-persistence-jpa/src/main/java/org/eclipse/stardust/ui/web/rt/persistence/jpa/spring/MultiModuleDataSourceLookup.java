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
package org.eclipse.stardust.ui.web.rt.persistence.jpa.spring;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * @author Robert.Sauer
 */
public class MultiModuleDataSourceLookup
      implements DataSourceLookup, ApplicationContextAware
{
   private DataSource defaultDataSource;

   private ApplicationContext applicationContext;

   public DataSource getDefaultDataSource()
   {
      return defaultDataSource;
   }

   public void setDefaultDataSource(DataSource defaultDataSource)
   {
      this.defaultDataSource = defaultDataSource;
   }

   public DataSource getDataSource(String dataSourceName)
         throws DataSourceLookupFailureException
   {
      DataSource ds = null;

      @SuppressWarnings("unchecked")
      Map<String, DataSourceMapping> mappings = applicationContext.getBeansOfType(DataSourceMapping.class);
      if (null != mappings)
      {
         for (DataSourceMapping mapping : mappings.values())
         {
            if (dataSourceName.equals(mapping.getDataSourceName()))
            {
               ds = mapping.getDataSource();
               if (null != ds)
               {
                  break;
               }
            }
         }
      }

      return (null != ds) ? ds : defaultDataSource;
   }

   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.applicationContext = applicationContext;
   }
}
