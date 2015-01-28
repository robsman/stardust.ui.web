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

import org.eclipse.stardust.ui.web.html5.utils.ResourceDependency;
import org.eclipse.stardust.ui.web.html5.utils.ResourceDependencyUtils;
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

   /**
    * @param args
    *           csv filename If csv filename is not provided then csv with default
    *           filename(DependencyList.csv) would be created
    */
   public static void main(String[] args)
   {

      String outputFileName = "DependencyList.csv";
      if (args != null && args.length == 1 && args[0] != "")
      {
         outputFileName = args[0];
      }

      /**
       * Empty configLocations specified so as all the deployed jars in classpath would be
       * scanned for dependencies.
       */
      String[] configLocations = {};

      ApplicationContext context = new ClassPathXmlApplicationContext(configLocations);

      List<ResourceDependency> resourceDependencies = ResourceDependencyUtils.discoverDependencies(context);

      ArrayList<String> allScripts = new ArrayList<String>();
      ArrayList<String> allStyles = new ArrayList<String>();

      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         allScripts.addAll(resourceDependency.getLibs());
         allStyles.addAll(resourceDependency.getStyles());
      }
      StringBuilder dependencies = new StringBuilder();
      dependencies.append("PluginId");
      dependencies.append(SEPERATOR);
      dependencies.append("Name");
      dependencies.append(SEPERATOR);
      dependencies.append("Path");
      dependencies.append(NEWLINE);

      dependencies.append(getDependencies(allScripts));

      dependencies.append(NEWLINE);

      dependencies.append(getDependencies(allStyles));

      FileWriter writer = null;
      try
      {
         writer = new FileWriter(outputFileName);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      try
      {
         writer.append(dependencies);
         writer.flush();
         writer.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private static String getDependencies(List<String> resourceDependencyEntries)
   {
      StringBuilder dependencies = new StringBuilder();
      for (String reseDepEntry : resourceDependencyEntries)
      {
         int pluginIdIndex = reseDepEntry.indexOf(DELIMITER, PLUGINS_PREFIX.length() + 1);
         dependencies.append(reseDepEntry.substring(PLUGINS_PREFIX.length() + 1, pluginIdIndex));
         dependencies.append(SEPERATOR);
         int lastIndexOf = reseDepEntry.lastIndexOf(DELIMITER);
         String path = reseDepEntry.substring(0, lastIndexOf);
         String name = reseDepEntry.substring(lastIndexOf + 1, reseDepEntry.length());
         dependencies.append(name);
         dependencies.append(SEPERATOR);
         dependencies.append(path);
         dependencies.append(NEWLINE);
      }
      return dependencies.toString();
   }
}
