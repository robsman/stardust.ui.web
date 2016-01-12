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
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.Category;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.TrafficLightViewPropertyProvider;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.springframework.stereotype.Component;

@Component
public class TrafficLightUtils
{
   private static final Logger trace = LogManager.getLogger(TrafficLightUtils.class);

   /**
    * 
    * @param processDefinition
    * @return
    */
   public List<SelectItemDTO> getCateogries(ProcessDefinition processDefinition)
   {
      trace.debug("Getting categories for process : " + processDefinition.getQualifiedId());
      List<SelectItemDTO> categoriesList = new ArrayList<SelectItemDTO>();

      if (processDefinition != null)
      {
         List<DataPath> dataPaths = processDefinition.getAllDataPaths();
         for (DataPath dataPath : dataPaths)
         {
            if (dataPath.isDescriptor())
            {
               List values = TrafficLightViewPropertyProvider.getInstance().getAllRowIDsAsList(
                     processDefinition.getQualifiedId(), dataPath.getId());
               if (!values.isEmpty())
               {
                  Category category = new Category(dataPath);
                  categoriesList.add(new SelectItemDTO(category.getId(), category.getName()));
               }
            }
         }
      }
      return categoriesList;
   }

}
