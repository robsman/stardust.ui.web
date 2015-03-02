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

package org.eclipse.stardust.ui.web.rest.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

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

/**
 * 
 * @author Yogesh.Manware
 * 
 */
@Provider
public class GenericThrowableMapper implements ExceptionMapper<Throwable>
{
   private static final Logger trace = LogManager.getLogger(GenericThrowableMapper.class);

   @Context
   private HttpServletRequest httpRequest;

   @Resource
   ExceptionHelper exceptionHelper;

   /*
    * (non-Javadoc)
    * 
    * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
    */
   @Override
   public Response toResponse(Throwable exception)
   {
      //TODO: remove later
      exception.printStackTrace();
      trace.error(exception);
      ErrorMessageDTO errorMessage = exceptionHelper.getMessageFromProvider(exception, httpRequest.getLocale());
      // errorMessage.setStacktrace(getStackTrace(exception));
      errorMessage.setDetailedMessage(exception.toString());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(GsonUtils.toJson(errorMessage)).build();
   }

   /**
    * @param throwable
    * @return
    */
   public static String getStackTrace(final Throwable throwable)
   {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw, true);
      throwable.printStackTrace(pw);
      return sw.getBuffer().toString();
   }
}