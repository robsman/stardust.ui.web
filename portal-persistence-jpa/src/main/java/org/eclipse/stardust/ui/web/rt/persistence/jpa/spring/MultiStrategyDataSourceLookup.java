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

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException;

/**
 * @author Robert.Sauer
 */
public class MultiStrategyDataSourceLookup implements DataSourceLookup
{
   List<DataSourceLookup> strategies;

   public void setStrategies(List<DataSourceLookup> strategies)
   {
      this.strategies = strategies;
   }

   public DataSource getDataSource(String dataSourceName)
         throws DataSourceLookupFailureException
   {
      DataSource ds = null;

      if (null != strategies)
      {
         for (DataSourceLookup strategy : strategies)
         {
            ds = strategy.getDataSource(dataSourceName);
            if (null != ds)
            {
               break;
            }
         }
      }

      return ds;
   }

}
