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

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.ArtifactType;
import org.eclipse.stardust.ui.web.rest.service.dto.ArtifactTypeDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.springframework.stereotype.Component;

@Component
public class ArtifactTypeService
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public List<ArtifactTypeDTO> getRuntimeArtifactTypes()
   {

      List<ArtifactType> types = serviceFactoryUtils.getAdministrationService().getSupportedRuntimeArtifactTypes();

      List<ArtifactTypeDTO> artifactTypes = DTOBuilder.buildList(types, ArtifactTypeDTO.class);

      return artifactTypes;
   }
}
