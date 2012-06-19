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
package org.eclipse.stardust.ui.web.plugin.support.resources.spi;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Subodh.Godbole
 * 
 */
public interface ResourceRequestProcessor
{
   void handleRequest(final String requestedPath, HttpServletRequest request, HttpServletResponse response,
         ServletContext servletContext) throws ServletException, IOException;

   interface Factory
   {
      ResourceRequestProcessor getRequestProcessor(String requestedPath);
   }
}
