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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.ui.web.plugin.utils.WebResource;
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
   public static final String COPYRIGHT_HEADER = "(.*)Copyright \\(c\\) (.*) SunGard CSA LLC(.*)";

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

      List<ResourceDependency> htmlResourceDependencies = ResourceDependencyUtils.discoverDependenciesAfterConcatenation(context);

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
               .append(getDependencies(resourceDependency.getLibs(), resourceDependency.getPluginId(), resourceDependency.getPluginLocation(), true));
      }

      dependencies.append("HTML5 Deps Styles");
      for (ResourceDependency resourceDependency : htmlResourceDependencies)
      {
         dependencies.append(getDependencies(resourceDependency.getStyles(), resourceDependency.getPluginId(),
               resourceDependency.getPluginLocation(), false));
      }

      dependencies.append("HTML5 Deps Scripts");
      // Scripts can be commented if not required
      for (ResourceDependency resourceDependency : htmlResourceDependencies)
      {
         dependencies.append(getDependencies(resourceDependency.getScripts(), resourceDependency.getPluginId(),
               resourceDependency.getPluginLocation(), false));
      }
      
      // For all plugins/Resources
      List<ResourceDependency> resourceDependencies = ResourceDependencyUtils.discoverAllDependencies(context);

      // Separate for loops to club all libs and styles together
      dependencies.append("Common Deps JS");
      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         if (!html5PluginIds.contains(resourceDependency.getPluginId()))// Filter HTML5 plugins
         {
            dependencies.append(getDependencies(resourceDependency.getLibs(), resourceDependency.getPluginId(),
                  resourceDependency.getPluginLocation(), true));
         }
      }
      
      dependencies.append("Common Deps CSS");      
      for (ResourceDependency resourceDependency : resourceDependencies)
      {
         if (!html5PluginIds.contains(resourceDependency.getPluginId()))// Filter HTML5 plugins
         {
            dependencies.append(getDependencies(resourceDependency.getStyles(), resourceDependency.getPluginId(),
                  resourceDependency.getPluginLocation(), false));
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
 * @param pluginId
 * @param jarLocation
 * @param versionInfo
 * @return
 */
   private String getDependencies(List<WebResource> resourceDependencyEntries, String pluginId, String jarLocation, boolean versionInfo)
   {
      StringBuilder dependencies = new StringBuilder();
      for (int i = 0; i < resourceDependencyEntries.size(); i++)
      {
         StringBuilder tempDep = new StringBuilder();
         WebResource webResource = resourceDependencyEntries.get(i);
         String reseDepEntry = webResource.webUri;
         
         tempDep.append(SEPERATOR);

         // For jarName
         String jarName = jarLocation.substring(jarLocation.lastIndexOf(DELIMITER) + 1, jarLocation.length());

         tempDep.append(jarName);

         tempDep.append(SEPERATOR);

         // For Resource Path
         reseDepEntry = reseDepEntry.replaceAll("//", "/");
         int lastIndexOf = reseDepEntry.lastIndexOf(DELIMITER);
         int ordinalIndexOf = StringUtils.ordinalIndexOf(reseDepEntry, "/", 2);
         String path = reseDepEntry.substring(ordinalIndexOf, lastIndexOf);
         
         // For Resource Name
         String name = reseDepEntry.substring(lastIndexOf + 1, reseDepEntry.length());

         String libraryname = null;
         String libraryVersion = null;

         tempDep.append(name);
         tempDep.append(SEPERATOR);
         tempDep.append(path);

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
                  tempDep.append(SEPERATOR);
                  tempDep.append(libraryname);
                  
                  tempDep.append(SEPERATOR);
                  tempDep.append(libraryVersion);
               }
               
            }
         }

         tempDep.append(NEWLINE);

         String fullFilePath = getFullFilePath(path, name, pluginId);

         if (isThirdPartyDependency(fullFilePath)) {
        	 dependencies.append(tempDep.toString());
         }

      }
      return dependencies.toString();
   }
   
	/**
	 * @param path
	 * @param name
	 * @param pluginId
	 * @return
	 */
	private String getFullFilePath(String path, String name, String pluginId) {
		String filePath = "/META-INF/" + pluginId + ".portal-plugin";
		if (pluginId != null) {
			InputStream resourceAsStream = DumpDependencies.class
					.getResourceAsStream(filePath);

			String pluginPath = null;

			if (resourceAsStream != null) {
				Scanner scanner = new Scanner(resourceAsStream);
				pluginPath = scanner.nextLine();
			}

			if (pluginPath.endsWith("/")) {
				pluginPath = pluginPath.substring(0, pluginPath.length() - 1);
			}
			return pluginPath + path + "/" + name;
		}
		return "/META-INF/webapp" + path + "/" + name;
	}

	/**
	 * @param path
	 * @return
	 */
	private boolean isThirdPartyDependency(String path) {
		InputStream resourceAsStream = DumpDependencies.class
				.getResourceAsStream(path);

		if (resourceAsStream != null) {
			int numberOfLinesScanned = 10;
			StringBuilder fileHeaderContents = new StringBuilder();
			Scanner scanner = null;
			scanner = new Scanner(resourceAsStream);
			while (scanner.hasNextLine() && numberOfLinesScanned > 0) {
				String line = scanner.nextLine();
				if (line.trim().length() > 0) {
					fileHeaderContents.append(line);
				}
				numberOfLinesScanned--;
			}

			if (fileHeaderContents.toString().matches(COPYRIGHT_HEADER)) {
				return false;
			}
		}
		return true;
	}
}
