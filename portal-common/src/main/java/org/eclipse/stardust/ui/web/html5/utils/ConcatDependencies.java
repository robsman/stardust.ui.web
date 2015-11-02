/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.html5.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.WebResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Subodh.Godbole
 *
 */
public class ConcatDependencies
{
   private String resourceDir;
   private String targetDir;
   private String fileSeparator;

   private JsonObject defaultDescriptor = GsonUtils.readJsonObject(
         "{\"portal-plugins\" : [], \"libs\" : [], \"scripts\" : [], \"styles\" : []\n}");

   /**
    * @param args
    */
   public static void main(String[] args) throws Exception
   {
      String resourceDir;
      String targetDir;
      
      if(args.length == 1)
      {
         resourceDir = targetDir = args[0];
      }
      else
      {
         resourceDir = args[0];
         targetDir = args[1];
      }
      ConcatDependencies deps = new ConcatDependencies(resourceDir, targetDir);
      deps.discover();
   }

   /**
    * @param relativeResourceDir
    * @param relativeTargetDir
    */
   private ConcatDependencies(String resourceDir, String targetDir)
   {
      this.fileSeparator = System.getProperty("file.separator");
      this.resourceDir = resourceDir;
      this.targetDir = targetDir;

      System.out.println("Resource Directory = " + resourceDir);
      System.out.println("Target Directory = " + targetDir);
   }
   
   /**
    * 
    */
   private void discover() throws Exception
   {
      String[] configLocations = {};
      ApplicationContext context = new ClassPathXmlApplicationContext(configLocations);

      // This will have 2 entries, one portal-common and one from current folder
      List<ResourceDependency> pluginDeps = ResourceDependencyUtils.discoverDependencies(context);

      System.out.println("Discovered Dependencies:");
      Iterator<ResourceDependency> it = pluginDeps.iterator();
      while (it.hasNext())
      {
         ResourceDependency dep = it.next();
         if(dep.getDescriptorResource().getURL().getProtocol().equals("jar"))
         {
            // we're not interested in plugins which resides in JARs
            it.remove();
         }
         else
         {
            System.out.println("\tDiscovered Dependency: " + dep.getPluginLocation());
         }
      }

      // When running this in DEV workspace, 2 deps will be detected.
      // One of them will be portal-common and it will not be from JAR, so get rid of it.
      if (pluginDeps.size() == 2)
      {
         it = pluginDeps.iterator();
         while (it.hasNext())
         {
            ResourceDependency dep = it.next();
            if(dep.getPluginId().equals("common"))
            {
               System.out.println("Ignoring Dependency: " + dep.getPluginLocation());
               it.remove();
               break;
            }
         }
      }

      if (pluginDeps.size() == 1)
      {
         ResourceDependency resDep = pluginDeps.get(0);
         System.out.println("\nProcessing Dependency: " + resDep.getPluginLocation());

         String descriptorFileDirPath = resDep.getDescriptorResource().getFile().getParentFile().getAbsolutePath();
         System.out.println("descriptorFileDirPath = " + descriptorFileDirPath);
         descriptorFileDirPath = descriptorFileDirPath.replace(resourceDir, targetDir);
         System.out.println("descriptorFileDirPath = " + descriptorFileDirPath);

         if (resDep.getLibs().size() > 0)
         {
            System.out.println("Concatinating libs...");
            File libDir = new File(descriptorFileDirPath + fileSeparator + "libs");
            libDir.mkdirs();
            File libFile = new File(libDir, "all-resources.js");
            PluginUtils.writeResource(libFile, concatAllResources(resDep.getLibs(), 
                  resDep.getConcatSkip().get("libs-before"), resDep.getConcatSkip().get("libs")));

            addToDescriptor(defaultDescriptor.getAsJsonArray("libs"), resDep.getConcatSkip().get("libs-before"),
                  resDep.getConcatSkip().get("libs"), "all-resources.js");
         }
         else
         {
            defaultDescriptor.remove("libs");
         }

         if (resDep.getScripts().size() > 0)
         {
            System.out.println("Concatinating scripts...");
            File scriptDir = new File(descriptorFileDirPath + fileSeparator + "scripts");
            scriptDir.mkdirs();
            File scriptFile = new File(scriptDir, "all-resources.js");
            PluginUtils.writeResource(scriptFile, concatAllResources(resDep.getScripts(),
                  resDep.getConcatSkip().get("scripts-before"), resDep.getConcatSkip().get("scripts")));

            addToDescriptor(defaultDescriptor.getAsJsonArray("scripts"), resDep.getConcatSkip().get("scripts-before"),
                  resDep.getConcatSkip().get("scripts"), "all-resources.js");
         }
         else
         {
            defaultDescriptor.remove("scripts");
         }

         if (resDep.getStyles().size() > 0)
         {
            System.out.println("Concatinating styles...");
            File styleDir = new File(descriptorFileDirPath + fileSeparator + "styles");
            styleDir.mkdirs();
            File styleFile = new File(styleDir, "all-resources.css");
            PluginUtils.writeResource(styleFile, concatAllResources(resDep.getStyles(),
                  resDep.getConcatSkip().get("styles-before"), resDep.getConcatSkip().get("styles")));
            
            addToDescriptor(defaultDescriptor.getAsJsonArray("styles"), resDep.getConcatSkip().get("styles-before"),
                  resDep.getConcatSkip().get("styles"), "all-resources.css");
         }
         else
         {
            defaultDescriptor.remove("styles");
         }
         
         String jsonDescriptor = PluginUtils.readResource(resDep.getDescriptorResource());
         File orgDescriptorFile = new File(descriptorFileDirPath, "portal-plugin-dependencies-org.json");
         PluginUtils.writeResource(orgDescriptorFile, jsonDescriptor);

         JsonObject descriptor = GsonUtils.readJsonObject(jsonDescriptor);
         defaultDescriptor.add("portal-plugins", descriptor.get("portal-plugins"));
         File descriptorFile = new File(descriptorFileDirPath, "portal-plugin-dependencies.json");
         PluginUtils.writeResource(descriptorFile, defaultDescriptor.toString());

         System.out.println("Concatination Completed...");
      }
      else if (pluginDeps.size() == 0)
      {
         System.out.println("No dependencies found...");
      }
      else
      {
         System.err.println("Incorrect number of dependencies found: " + pluginDeps.size());
         for (ResourceDependency resDep : pluginDeps)
         {
            System.out.println("\t" + resDep.getPluginLocation());
         }
         System.exit(1);
      }
   }

   /**
    * @param list
    * @param skipConcatBefore
    * @param skipConcatAfter
    * @return
    */
   private String concatAllResources(List<WebResource> list, List<String> skipConcatBefore, List<String> skipConcatAfter)
   {
      List<String> skipConcat = new ArrayList<String>();
      if (null != skipConcatBefore)
      {
         skipConcat.addAll(skipConcatBefore);
      }
      if (null != skipConcatAfter)
      {
         skipConcat.addAll(skipConcatAfter);
      }

      StringBuffer sb = new StringBuffer();

      for (WebResource webResource : list)
      {
         try
         {
            if (!contains(webResource.webUri, skipConcat))
            {
               sb.append("/** START " + webResource.webUri + " **/\n\n");
               sb.append(PluginUtils.readResource(webResource.resource).trim());
               sb.append("\n\n/** END " + webResource.webUri + " **/\n\n\n");
            }
         }
         catch (IOException e)
         {
            System.err.println("Unable to read dependency resource: " + webResource.webUri);
         }
      }

      return sb.toString().trim();
   }

   /**
    * @param str
    * @param skipConcat
    * @return
    */
   private static boolean contains(String str, List<String> skipConcat)
   {
      if (CollectionUtils.isNotEmpty(skipConcat))
      {
         for(String file : skipConcat)
         {
            if (str.endsWith(file)) // TODO: Better matching rule?
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * @param entry
    * @param before
    * @param after
    * @param current
    */
   private void addToDescriptor(JsonArray entry, List<String> before, List<String> after, String current)
   {
      if (null != before)
      {
         for (String file : before)
         {
            entry.add(new JsonPrimitive(file));
         }
      }

      entry.add(new JsonPrimitive(current));

      if (null != after)
      {
         for (String file : after)
         {
            entry.add(new JsonPrimitive(file));
         }
      }      
   }
}
