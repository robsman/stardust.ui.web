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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.plugin.resources.impl;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.plugin.support.AbstractPortalResourcesServlet;
import org.eclipse.stardust.ui.web.plugin.support.resources.ResourceLoader;
import org.eclipse.stardust.ui.web.plugin.support.resources.ServletContextResourceLoader;


/**
 * @author Subodh.Godbole
 *
 */
public class PortalDmsResourcesServlet extends AbstractPortalResourcesServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected ResourceLoader getResourceLoader(HttpServletRequest request)
   {
      ResourceLoader delegate = new ServletContextResourceLoader(getServletContext());
      ResourceLoader loader = new DmsResourceLoader(delegate);
      return loader;
   }

   @Override
   protected boolean isSupportsCaching()
   {
      // DMS resources are cached internally
      return false;
   }
}
