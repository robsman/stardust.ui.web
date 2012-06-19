/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *  This code was copied from myfaces-trinidad-api.
 *  The original class is org.apache.myfaces.trinidad.webapp.ResourceServlet
 */
package org.eclipse.stardust.ui.web.plugin.support;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.plugin.support.resources.DirectoryResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.PluginResourceUtils;
import org.eclipse.stardust.ui.web.plugin.support.resources.PortalPluginResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.ResourceLoader;


/**
 * A Servlet which serves up web application resources (images, style sheets, JavaScript
 * libraries).
 * 
 * The servlet path at which this servlet is registered is used to lookup the class name
 * of the resource loader implementation. For example, if this servlet is registered with
 * name "resources" and URL pattern "/images/*", then its servlet path is "/images". This
 * is used to construct the class loader lookup for the text file
 * "/META-INF/servlets/resources/images.resources" which contains a single line entry with
 * the class name of the resource loader to use. This technique is very similar to
 * "/META-INF/services" lookup that allows the implementation object to implement an
 * interface in the public API and be used by the public API but reside in a private
 * implementation JAR.
 */
public class PortalPluginResourcesServlet extends AbstractPortalResourcesServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected ResourceLoader getResourceLoader(HttpServletRequest request)
   {
      ResourceLoader loader = null;

      if (PluginResourceUtils.isPluginPath(getResourcePath(request)))
      {
         ServletContext context = getServletContext();
         File tempdir = (File) context.getAttribute("javax.servlet.context.tempdir");
         ResourceLoader delegate = new DirectoryResourceLoader(tempdir);
         loader = new PortalPluginResourceLoader(delegate);
      }

      return loader;
   }

   @Override
   protected boolean isSupportsCaching()
   {
      return true;
   }
}
