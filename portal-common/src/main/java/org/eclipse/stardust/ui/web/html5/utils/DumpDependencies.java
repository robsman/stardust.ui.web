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
import java.util.List;

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
    *  Scans the invoking JVM's classpath for dependencies
    */
   public void discover()
   {
      /**
       * Empty configLocations specified so as all the deployed jars in classpath would be
       * scanned for dependencies.
       */
      String[] configLocations = {};

      ApplicationContext context = new ClassPathXmlApplicationContext(configLocations);

      List<ResourceDependency> resourceDependencies = ResourceDependencyUtils.discoverDependencies(context);

      dependencies.append("Jar Name");
      dependencies.append(SEPERATOR);
      dependencies.append("Resource Name");
      dependencies.append(SEPERATOR);
      dependencies.append("Resource Path");
      dependencies.append(NEWLINE);

      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         dependencies.append(getDependencies(resourceDependency.getLibs(), resourceDependency.getPluginLocation()));
      }

      dependencies.append(NEWLINE);

      // Separate for loops to club all libs and styles together
      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         dependencies.append(getDependencies(resourceDependency.getStyles(), resourceDependency.getPluginLocation()));
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
    * @return Formatted output string containing dependencies
    */
   private String getDependencies(List<String> resourceDependencyEntries, String jarLocation)
   {
      StringBuilder dependencies = new StringBuilder();
      for (int i = 0; i < resourceDependencyEntries.size(); i++)
      {
         String reseDepEntry = resourceDependencyEntries.get(i);

         // For jarName
         String jarName = jarLocation.substring(jarLocation.lastIndexOf(DELIMITER) + 1, jarLocation.length());
         dependencies.append(jarName);

         dependencies.append(SEPERATOR);

         // For Resource Path
         int lastIndexOf = reseDepEntry.lastIndexOf(DELIMITER);
         String path = reseDepEntry.substring(0, lastIndexOf);

         // For Resource Name
         String name = reseDepEntry.substring(lastIndexOf + 1, reseDepEntry.length());

         dependencies.append(name);
         dependencies.append(SEPERATOR);
         dependencies.append(path);
         dependencies.append(NEWLINE);
      }
      return dependencies.toString();
   }
}
