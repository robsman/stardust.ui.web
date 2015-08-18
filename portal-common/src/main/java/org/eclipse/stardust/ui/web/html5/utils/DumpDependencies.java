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
package org.eclipse.stardust.ui.web.html5.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Aditya.Gaikwad
 *
 */
public class DumpDependencies
{

   public static final String SEPERATOR = ",";
   public static final String DELIMITER = "/";
   public static final String NEWLINE = "\n";
   public static final String PLUGINS_PREFIX = "plugins";

   private StringBuilder dependencies = new StringBuilder();

   /**
    * @param args
    *           csv filename(optional)
    */
   public static void main(String[] args)
   {
      DumpDependencies dumpDependencies = new DumpDependencies();
      dumpDependencies.discover();
      dumpDependencies.persist(args);
   }

   /**
    * Scans the invoking JVM's classpath for dependencies
    */
   public void discover()
   {
      /**
       * Empty configLocations specified so as all the deployed jars in classpath would be
       * scanned for dependencies.
       */
      String[] configLocations = {};
      List<String> html5PluginIds = new ArrayList<String>();

      ApplicationContext context = new ClassPathXmlApplicationContext(configLocations);

      List<ResourceDependency> htmlResourceDependencies = ResourceDependencyUtils.discoverDependencies(context);

      dependencies.append("Dependency Type");
      dependencies.append(SEPERATOR);
      dependencies.append("Jar Name");
      dependencies.append(SEPERATOR);
      dependencies.append("Resource Name");
      dependencies.append(SEPERATOR);
      dependencies.append("Resource Path");
      dependencies.append(SEPERATOR);
      dependencies.append("Library Name");
      dependencies.append(SEPERATOR);
      dependencies.append("Library Version");
      dependencies.append(NEWLINE);

      // Separate for loops to club all libs, styles and scripts together
      dependencies.append("HTML5 Deps Libs");
      for (ResourceDependency resourceDependency : htmlResourceDependencies)
      {
         
         html5PluginIds.add(resourceDependency.getPluginId());
         dependencies
               .append(getDependencies(resourceDependency.getLibs(), resourceDependency.getPluginLocation(), true));
      }

      dependencies.append("HTML5 Deps Styles");
      for (ResourceDependency resourceDependency : htmlResourceDependencies)
      {
         dependencies.append(getDependencies(resourceDependency.getStyles(), resourceDependency.getPluginLocation(),
               false));
      }

      dependencies.append("HTML5 Deps Scripts");
      // Scripts can be commented if not required
      for (ResourceDependency resourceDependency : htmlResourceDependencies)
      {
         dependencies.append(getDependencies(resourceDependency.getScripts(), resourceDependency.getPluginLocation(),
               false));
      }
      
      // For all plugins/Resources
      List<ResourceDependency> resourceDependencies = ResourceDependencyUtils.discoverAllDependencies(context);

      // Separate for loops to club all libs and styles together
      dependencies.append("Common Deps JS");
      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         if (!html5PluginIds.contains(resourceDependency.getPluginId()))// Filter HTML5 plugins
         {
            dependencies.append(getDependencies(resourceDependency.getLibs(), resourceDependency.getPluginLocation(),
                  true));
         }
      }
      
      dependencies.append("Common Deps CSS");      
      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         if (!html5PluginIds.contains(resourceDependency.getPluginId()))// Filter HTML5 plugins
         {
            dependencies.append(getDependencies(resourceDependency.getStyles(), resourceDependency.getPluginLocation(),
                  false));
         }
      }

   }

   /**
    * @param args
    *           csv filename. If csv filename is not provided then csv with default
    *           filename(DependencyList.csv) would be created
    */
   public void persist(String[] args)
   {
      String outputFileName = "DependencyList.csv";
      if (args != null && args.length == 1 && args[0] != "")
      {
         outputFileName = args[0];
      }

      FileWriter writer = null;
      try
      {
         writer = new FileWriter(outputFileName);
         writer.append(dependencies);
         writer.flush();
         writer.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * @param resourceDependencyEntries
    * @param jarLocation
    * @param versionInfo
    * @return Formatted output string containing dependencies
    */
   private String getDependencies(List<String> resourceDependencyEntries, String jarLocation, boolean versionInfo)
   {
      StringBuilder dependencies = new StringBuilder();
      for (int i = 0; i < resourceDependencyEntries.size(); i++)
      {
         String reseDepEntry = resourceDependencyEntries.get(i);
         
         dependencies.append(SEPERATOR);

         // For jarName
         String jarName = jarLocation.substring(jarLocation.lastIndexOf(DELIMITER) + 1, jarLocation.length());
         dependencies.append(jarName);

         dependencies.append(SEPERATOR);

         // For Resource Path
         reseDepEntry = reseDepEntry.replaceAll("//", "/");
         int lastIndexOf = reseDepEntry.lastIndexOf(DELIMITER);
         int ordinalIndexOf = StringUtils.ordinalIndexOf(reseDepEntry, "/", 2);
         String path = reseDepEntry.substring(ordinalIndexOf, lastIndexOf);
         
         // For Resource Name
         String name = reseDepEntry.substring(lastIndexOf + 1, reseDepEntry.length());

         String libraryname = null;
         String libraryVersion = null;

         dependencies.append(name);
         dependencies.append(SEPERATOR);
         dependencies.append(path);

         if (versionInfo)
         {
            String[] pathTokens = path.split(DELIMITER);
            if (pathTokens.length > 2)
            {
               // sample path-token "plugins/html5-common/libs/datatables/1.9.4/plugins"
               // 2nd last token token would be version and 3rd last token library name
               libraryVersion = pathTokens[pathTokens.length - 2];
               libraryname = pathTokens[pathTokens.length - 3];
               
               if (libraryVersion.matches("[0-9,.]+"))
               {
                  dependencies.append(SEPERATOR);
                  dependencies.append(libraryname);
                  
                  dependencies.append(SEPERATOR);
                  dependencies.append(libraryVersion);
               }
               
            }
         }

         dependencies.append(NEWLINE);
      }
      return dependencies.toString();
   }
}
