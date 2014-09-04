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
package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.ProcessDefinitionDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessDefinitionUtils;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ProcessDefinitionService
{

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;

   /**
    * @return
    */
   public List<ProcessDefinitionDTO> getStartableProcesses()
   {
      List<ProcessDefinition> startableProcesses = processDefinitionUtils
            .getStartableProcesses();

      List<ProcessDefinitionDTO> startableProcessesDTO = ProcessDefinitionDTOBuilder
            .build(startableProcesses);

      return startableProcessesDTO;
   }

}
