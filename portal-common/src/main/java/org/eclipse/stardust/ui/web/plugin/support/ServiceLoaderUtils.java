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

   private static boolean useJSE16 = false;

   static
   {
      try
      {
         Class.forName("java.util.ServiceLoader");
         useJSE16 = true;
         trace.info("Using JSE1.6 for Loading Services...");
      }
      catch(Exception e)
      {
         useJSE16 = false;
         trace.info("Using IPP for Loading Services...");
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
         if (useJSE16)
         {
            servicesIterator = searchProvidersUsingJSE16(clazz);
         }
         else
         {
            servicesIterator = searchProvidersUsingIpp(clazz);
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
   @SuppressWarnings("unchecked")
   private static <S> Iterator<S> searchProvidersUsingJSE16(Class<S> clazz)
   {
      try
      {
         trace.info("searchProviders: Searching providers usng JSE1.6 = " + clazz.getName());
         Object serviceLoader = ReflectionUtils.invokeStaticMethod("java.util.ServiceLoader", "load(java.lang.Class)",
               clazz);
         Object servicesIterator = ReflectionUtils.invokeMethod(serviceLoader, "iterator");
         trace.info("searchProviders: Found provider usng JSE1.6");

         return (Iterator<S>) servicesIterator;
      }
      catch(Exception e)
      {
         trace.error("searchProviders: Error in searching providers usng JSE1.6 -> " + e);
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
         trace.info("searchProviders: Searching providers usng IPP = " + clazz.getName());
         Object services = ReflectionUtils.invokeStaticMethod("org.eclipse.stardust.common.config.ExtensionProviderUtils",
               "getExtensionProviders(java.lang.Class)", clazz);
         List<S> servicesList = (List<S>)services;
         trace.info("searchProviders: Found provider usng IPP -> " + servicesList);

         return servicesList.iterator();
      }
      catch(Exception e)
      {
         trace.error("searchProviders: Error in searching providers using IPP -> " + e);
      }
      
      return null;
   }
}
