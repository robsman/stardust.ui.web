/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.springframework.stereotype.Component;

@Component
public class SchemaDefinitionService
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   public byte[] getSchemaDefinition(long modelOID, String typeDeclarationId)
   {
      try
      {
         return serviceFactoryUtils.getQueryService().getSchemaDefinition(modelOID, typeDeclarationId);
      }
      catch (ObjectNotFoundException e)
      {
         throw new I18NException(restCommonClientMessages.getParamString("typeDeclaration.schema.notFound",
               typeDeclarationId));
      }
      catch (Exception e)
      {
         throw new I18NException(restCommonClientMessages.getParamString("typeDeclaration.schema.error",
               typeDeclarationId));
      }
   }
}
