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
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.plugin.utils.PluginUtils;
import org.eclipse.stardust.ui.web.plugin.utils.WebResource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.gson.JsonObject;

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
         "{\"portal-plugins\" : [], \"libs\" : [\"all-resources.js\"], \"scripts\" : [\"all-resources.js\"], \"styles\" : [\"all-resources.css\"]\n}");

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
      boolean success = false;
      String[] configLocations = {};
      ApplicationContext context = new ClassPathXmlApplicationContext(configLocations);

      // This will have 2 entries, one portal-common and one from current folder
      List<ResourceDependency> pluginDeps = ResourceDependencyUtils.discoverDependencies(context);

      Iterator<ResourceDependency> it = pluginDeps.iterator();
      while (it.hasNext())
      {
         ResourceDependency dep = it.next();
         if(dep.getDescriptorResource().getURL().getProtocol().equals("jar"))
         {
            // we're not interested in plugins which resides in JARs
            it.remove();
         }
      }
      if (pluginDeps.size() == 1)
      {
         ResourceDependency resDep = pluginDeps.get(0);

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
            PluginUtils.writeResource(libFile, concatAllResources(resDep.getLibs()));
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
            PluginUtils.writeResource(scriptFile, concatAllResources(resDep.getScripts()));
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
            PluginUtils.writeResource(styleFile, concatAllResources(resDep.getStyles()));
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
         success = true;
      }
      else
      {
         System.out.println("No dependencies found...");
         success = (pluginDeps.size() == 0);
      }
           
      if (!success)
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
    * @return
    */
   private String concatAllResources(List<WebResource> list)
   {
      StringBuffer sb = new StringBuffer();
      for (WebResource webResource : list)
      {
         try
         {
            sb.append("/** START " + webResource.webUri + " **/\n\n");
            sb.append(PluginUtils.readResource(webResource.resource).trim());
            sb.append("\n\n/** END " + webResource.webUri + " **/\n\n\n");
         }
         catch (IOException e)
         {
            System.err.println("Unable to read dependency resource: " + webResource.webUri);
         }
      }

      return sb.toString().trim();
   }
}
