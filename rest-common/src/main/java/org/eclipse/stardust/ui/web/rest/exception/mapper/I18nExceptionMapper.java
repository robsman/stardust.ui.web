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

package org.eclipse.stardust.ui.web.rest.exception.mapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.exception.ExceptionHelper;
import org.eclipse.stardust.ui.web.rest.dto.response.ErrorMessageDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;

/**
 * @author Yogesh.Manware
 *
 */
@Provider
public class I18nExceptionMapper implements ExceptionMapper<I18NException>
{
   private static final Logger trace = LogManager.getLogger(I18nExceptionMapper.class);

   @Context
   private HttpServletRequest httpRequest;

   @Resource
   ExceptionHelper exceptionHelper;

   @Override
   public Response toResponse(I18NException exception)
   {
      trace.error("", exception);
      ErrorMessageDTO errorMessage = new ErrorMessageDTO(exception.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(GsonUtils.toJson(errorMessage)).build();
   }
}