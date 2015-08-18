/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.exception;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ErrorMessageDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;

/**
 * 
 * @author Yogesh.Manware
 * 
 *         For new code from Portal Perspective user PortalException and relevant Mapper
 * 
 */
@Provider
public class PortalExceptionMapper implements ExceptionMapper<PortalException>
{
   private static final Logger trace = LogManager.getLogger(PortalExceptionMapper.class);

   @Context
   private HttpServletRequest httpRequest;

   @Resource
   ExceptionHelper exceptionHelper;

   @Override
   public Response toResponse(PortalException exception)
   {
      trace.error("", exception);
      ErrorMessageDTO errorMessage = exceptionHelper.getMessageFromProvider(exception, httpRequest.getLocale());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(GsonUtils.toJson(errorMessage)).build();
   }
}