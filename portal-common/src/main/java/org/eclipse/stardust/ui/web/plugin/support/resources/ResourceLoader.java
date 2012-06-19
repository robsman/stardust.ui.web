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
 *  The original class is org.apache.myfaces.trinidad.resource.ResourceLoader
 */
package org.eclipse.stardust.ui.web.plugin.support.resources;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class for resource loaders.  Resource loaders can lookup resources 
 * as URLs from arbitrary locations, including JAR files.
 *
 */
public class ResourceLoader
{
  /**
   * Returns the shared resource loader that always returns null.
   * 
   * @return null for any resource path
   */
  public static ResourceLoader getNullResourceLoader()
  {
    return _NULL_RESOURCE_LOADER;
  }
  
  /**
   * Finds the resource with the given name.  A resource is some data
   * (images, audio, text, etc) that can be accessed by class code in a way
   * that is independent of the location of the code.
   *
   * <p> The name of a resource is a '<tt>/</tt>'-separated path name that
   * identifies the resource.
   *
   * <p> This method will first search the this resource loader for the
   * resource.  That failing, this method will invoke 
   * {@link #findResource(HttpServletRequest, String)} to on the parent resource loader to
   * find the resource.  </p>
   *
   * @param request the servlet request
   * @param  name  the resource name
   *
   * @return  A <tt>URL</tt> object for reading the resource, or
   *          <tt>null</tt> if the resource could not be found or the invoker
   *          doesn't have adequate privileges to get the resource.
   */
  public URL getResource(
    HttpServletRequest request, String name
    ) throws IOException
  {
    URL url = findResource(request, name);
    
    if (url == null && _parent != null) 
    {
      url = _parent.getResource(request, name);
    }
    
    return url;
  }

  /**
   * Finds the resource with the given name. Resource loader implementations
   * should override this method to specify where to find resources.  </p>
   *
   * @param request the servlet request
   * @param  name
   *         The resource name
   *
   * @return  A <tt>URL</tt> object for reading the resource, or
   *          <tt>null</tt> if the resource could not be found
   */
  protected URL findResource(
    HttpServletRequest request, String name
    ) throws IOException
  {
   return null;
  }
  
  /**
   * Returns the content type of this URL connection.
   * 
   * @return  the content type
   */
  protected String getContentType(
    URLConnection conn)
  {
    return conn.getContentType();
  }
  
  /**
   * Returns the parent resource loader, or null if this is a root 
   * resource loader.
   * 
   * @return  the parent resource loader
   */
  protected ResourceLoader getParent()
  {
    return _parent;
  }
  
  /**
   * Constructs a new resource loader with specified parent resource loader.
   * 
   * @param parent  the parent resource loader
   */
  protected ResourceLoader(
    ResourceLoader parent)
  {
    _parent = parent;
  }

  /**
   * Constructs a new root resource loader.
   */
  protected ResourceLoader()
  {
    this(null);
  }

  private static final ResourceLoader _NULL_RESOURCE_LOADER = 
                                                      new ResourceLoader();

  private final ResourceLoader _parent;

}
