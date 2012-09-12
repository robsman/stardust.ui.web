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
package org.eclipse.stardust.ui.web.plugin.support;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class ServiceLoaderUtils
{
   private static final Logger trace = LogManager.getLogger(ServiceLoaderUtils.class);

   private static boolean useIpp = false;

   static
   {
      try
      {
         Class.forName("org.eclipse.stardust.common.config.ExtensionProviderUtils");
         useIpp = true;
         trace.info("Using IPP for Loading Services...");
      }
      catch(Exception e)
      {
         useIpp = false;
         trace.info("Using JSE1.6 for Loading Services...");
      }
   }

   /**
    * @param <S>
    * @param clazz
    * @return
    */
   public static <S> Iterator<S> searchProviders(Class<S> clazz)
   {
      Iterator<S> servicesIterator = null;

      try
      {
         if (useIpp)
         {
            servicesIterator = searchProvidersUsingIpp(clazz);
         }
         else
         {
            servicesIterator = searchProvidersUsingJSE16(clazz);
         }
      }
      catch(Exception e)
      {
         trace.error("searchProviders: Error in searching providers -> " + e);
      }

      return servicesIterator;
   }
   
   /**
    * @param <S>
    * @param clazz
    * @return
    */
   private static <S> Iterator<S> searchProvidersUsingJSE16(Class<S> clazz)
   {
      try
      {
         trace.info("searchProviders: Searching providers using JSE1.6 = " + clazz.getName());
         ServiceLoader<S> serviceLoader = ServiceLoader.load(clazz);
         Iterator<S> servicesIterator = serviceLoader.iterator();
         trace.info("searchProviders: Found provider using JSE1.6");

         return servicesIterator;
      }
      catch(Exception e)
      {
         trace.error("searchProviders: Error in searching providers using JSE1.6 -> " + e);
      }
      
      return null;
   }

   /**
    * @param <S>
    * @param clazz
    * @return
    */
   @SuppressWarnings("unchecked")
   private static <S> Iterator<S> searchProvidersUsingIpp(Class<S> clazz)
   {
      try
      {
         trace.info("searchProviders: Searching providers using IPP = " + clazz.getName());
         Object services = ReflectionUtils.invokeStaticMethod("org.eclipse.stardust.common.config.ExtensionProviderUtils",
               "getExtensionProviders(java.lang.Class)", clazz);
         List<S> servicesList = (List<S>)services;
         trace.info("searchProviders: Found provider using IPP -> " + servicesList);

         return servicesList.iterator();
      }
      catch(Exception e)
      {
         trace.error("searchProviders: Error in searching providers using IPP -> " + e);
      }
      
      return null;
   }
}
