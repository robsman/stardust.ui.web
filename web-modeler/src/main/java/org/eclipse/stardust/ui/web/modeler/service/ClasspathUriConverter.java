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
package org.eclipse.stardust.ui.web.modeler.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.URIHandler;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * Supports URLs with scheme "classpath:/". Searches for resources in CLASSPATH
 */
public class ClasspathUriConverter implements URIConverter
{
   private static final Logger trace = LogManager.getLogger(ClasspathUriConverter.class);

   public static final String CLASSPATH_SCHEME = "classpath";

   public OutputStream createOutputStream(URI uri) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }

   public Map<URI, URI> getURIMap()
   {
      return URIConverter.URI_MAP;
   }

   public URI normalize(URI uri)
   {
      // no normalization implemented
      return uri;
   }

   public Map<String, ? > contentDescription(URI arg0, Map< ? , ? > arg1) throws IOException
   {
      return null;
   }

   public InputStream createInputStream(URI uri) throws IOException
   {
      return createInputStream(uri, null);
   }

   public InputStream createInputStream(URI uri, Map< ? , ? > arg1) throws IOException
   {
      URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(uri.path());
      if (resourceUrl == null)
      {
         resourceUrl = ClasspathUriConverter.class.getClassLoader().getResource(uri.path());
         if (resourceUrl == null)
         {
            resourceUrl = ClasspathUriConverter.class.getResource(uri.path());
            if (resourceUrl == null)
            {
               throw new PublicException("Could not find XSD '" + uri.path() + "' in CLASSPATH");
            }
         }
      }
      if (trace.isDebugEnabled())
      {
         trace.debug("Resolved '" + uri + "' to '" + resourceUrl + "'.");
      }
      return resourceUrl.openStream();
   }

   public OutputStream createOutputStream(URI arg0, Map< ? , ? > arg1) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }

   public void delete(URI arg0, Map< ? , ? > arg1) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }

   public boolean exists(URI arg0, Map< ? , ? > arg1)
   {
      throw new RuntimeException("Not supported.");
   }

   public Map<String, ? > getAttributes(URI arg0, Map< ? , ? > arg1)
   {
      throw new RuntimeException("Not supported.");
   }

   public EList<ContentHandler> getContentHandlers()
   {
      throw new RuntimeException("Not supported.");
   }

   public URIHandler getURIHandler(URI arg0)
   {
      throw new RuntimeException("Not supported.");
   }

   public EList<URIHandler> getURIHandlers()
   {
      throw new RuntimeException("Not supported.");
   }

   public void setAttributes(URI arg0, Map<String, ? > arg1, Map< ? , ? > arg2) throws IOException
   {
      throw new RuntimeException("Not supported.");
   }
}
