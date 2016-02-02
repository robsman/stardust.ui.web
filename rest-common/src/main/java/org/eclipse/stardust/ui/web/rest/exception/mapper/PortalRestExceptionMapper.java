/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.exception.mapper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.dto.response.ErrorMessageDTO;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
@Provider
public class PortalRestExceptionMapper implements ExceptionMapper<PortalRestException>
{
   private static final Logger trace = LogManager.getLogger(PortalRestExceptionMapper.class);

   @Context
   private HttpServletRequest httpRequest;

   /*
    * (non-Javadoc)
    * 
    * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
    */
   @Override
   public Response toResponse(PortalRestException exception)
   {
      trace.error("", exception);
      ErrorMessageDTO errorMessage = new ErrorMessageDTO(exception.getMessage(httpRequest.getLocale()));
      return Response.status(exception.getHttpStatus()).entity(GsonUtils.toJson(errorMessage)).build();
   }
}