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

import javax.sql.DataSource;

/**
 * @author Robert.Sauer
 */
public class DataSourceMapping
{
   private String dataSourceName;

   private DataSource dataSource;

   public String getDataSourceName()
   {
      return dataSourceName;
   }

   public void setDataSourceName(String dataSourceName)
   {
      this.dataSourceName = dataSourceName;
   }

   public DataSource getDataSource()
   {
      return dataSource;
   }

   public void setDataSource(DataSource dataSource)
   {
      this.dataSource = dataSource;
   }
}
