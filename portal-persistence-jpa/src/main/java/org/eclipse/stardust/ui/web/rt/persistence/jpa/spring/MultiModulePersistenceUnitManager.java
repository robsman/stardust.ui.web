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

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * @author Robert.Sauer
 * @see http://jira.springframework.org/browse/SPR-2598
 */
public class MultiModulePersistenceUnitManager extends DefaultPersistenceUnitManager
{
   private String persistenceUnitNameOverride;
   
   private DataSource jtaDataSource;
   
   public String getPersistenceUnitNameOverride()
   {
      return persistenceUnitNameOverride;
   }

   public void setPersistenceUnitNameOverride(String persistenceUnitNameOverride)
   {
      this.persistenceUnitNameOverride = persistenceUnitNameOverride;
   }

   public DataSource getJtaDataSource()
   {
      return jtaDataSource;
   }

   public void setJtaDataSource(DataSource jtaDataSource)
   {
      this.jtaDataSource = jtaDataSource;
   }

   @Override
   protected void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo newPU)
   {
      super.postProcessPersistenceUnitInfo(newPU);

      if ((null != persistenceUnitNameOverride)
            && (0 < persistenceUnitNameOverride.length()))
      {
         // force all persistence units onto one well known name
         newPU.setPersistenceUnitName(persistenceUnitNameOverride);
      }

      if (null == newPU.getJtaDataSource())
      {
         newPU.setJtaDataSource(jtaDataSource);
      }

      final URL persistenceUnitRootUrl = newPU.getPersistenceUnitRootUrl();

      newPU.addJarFileUrl(persistenceUnitRootUrl);

      final String persistenceUnitName = newPU.getPersistenceUnitName();

      final MutablePersistenceUnitInfo oldPU = getPersistenceUnitInfo(persistenceUnitName);
      if (null != oldPU)
      {
         List<URL> urls = oldPU.getJarFileUrls();
         for (URL url : urls)
         {
            newPU.addJarFileUrl(url);
         }

         List<String> managedClassNames = oldPU.getManagedClassNames();
         for (String managedClassName : managedClassNames)
         {
            newPU.addManagedClassName(managedClassName);
         }

         List<String> mappingFileNames = oldPU.getMappingFileNames();
         for (String mappingFileName : mappingFileNames)
         {
            newPU.addMappingFileName(mappingFileName);
         }

         Properties oldProperties = oldPU.getProperties();
         Properties newProperties = newPU.getProperties();
         newProperties.putAll(oldProperties);
         newPU.setProperties(newProperties);
      }
   }
}
