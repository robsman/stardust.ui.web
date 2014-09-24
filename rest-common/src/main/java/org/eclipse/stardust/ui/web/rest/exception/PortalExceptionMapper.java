/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.exception;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
@Provider
public class PortalExceptionMapper implements ExceptionMapper<PortalException>
{
   private static final Logger trace = LogManager.getLogger(PortalExceptionMapper.class);

   @Context
   private HttpServletRequest httpRequest;

   /*
    * (non-Javadoc)
    * 
    * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
    */
   @Override
   public Response toResponse(PortalException exception)
   {
      trace.error(exception);
      return Response.status(exception.getHttpStatus()).entity(exception.getMessage(httpRequest.getLocale())).build();
   }
}