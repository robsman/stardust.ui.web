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
package org.eclipse.stardust.ui.web.plugin.utils;

import org.springframework.core.io.Resource;

/**
 * @author Subodh.Godbole
 *
 */
public class WebResource
{
   public String webUri;
   public Resource resource;

   /**
    * @param webUri
    * @param resource
    */
   public WebResource(String webUri, Resource resource)
   {
      super();
      this.webUri = webUri;
      this.resource = resource;
   }
}
