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
package org.eclipse.stardust.ui.web.rest.service.dto.builder;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class ProcessDefinitionDTOBuilder
{

   /**
    * @param processDefinition
    * @return
    */
   public static ProcessDefinitionDTO build(ProcessDefinition processDefinition)
   {
      ProcessDefinitionDTO processDefinitionDTO = new ProcessDefinitionDTO();

      if (processDefinition != null)
      {
         processDefinitionDTO.setId(processDefinition.getId());
         processDefinitionDTO.setName(processDefinition.getName());
         processDefinitionDTO.setDescription(processDefinition.getDescription());
         processDefinitionDTO.setModelOid(processDefinition.getModelOID());
         
//         ProcessDefinitionUtils.getSpecificDocuments(processDefinition);
//         processDefinitionDTO.setSpecificDocuments(specificDocuments);

      }

      return processDefinitionDTO;
   }

   /**
    * @param processDefinitions
    * @return
    */
   public static List<ProcessDefinitionDTO> build(
         List<ProcessDefinition> processDefinitions)
   {
      List<ProcessDefinitionDTO> processDefinitionsDTO = CollectionUtils.newArrayList();

      for (ProcessDefinition processDefinition : processDefinitions)
      {
         processDefinitionsDTO.add(build(processDefinition));
      }

      return processDefinitionsDTO;
   }

   /**
    * Prevent instantiation
    */
   private ProcessDefinitionDTOBuilder()
   {

   }
}
