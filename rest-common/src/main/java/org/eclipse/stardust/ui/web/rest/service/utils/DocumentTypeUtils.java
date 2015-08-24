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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.stereotype.Component;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class DocumentTypeUtils
{

   @Resource
   private ModelUtils modelUtils;

   /**
    * @return
    */
   public List<DocumentType> getDocumentTypes()
   {
      List<DocumentType> allDocumentTypes = new ArrayList<DocumentType>();

      // Get all "active" models
      Models deployedModelDescriptions = modelUtils.getActiveModels();

      List<DeployedModel> deployedModels = new ArrayList<DeployedModel>();
      for (DeployedModelDescription deployedModelDescription : deployedModelDescriptions)
      {
         deployedModels.add(modelUtils.getModel(deployedModelDescription.getModelOID()));
      }

      // Get Document Types in each "active" model
      for (DeployedModel deployedModel : deployedModels)
      {
         allDocumentTypes
               .addAll(org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils
                     .getDeclaredDocumentTypes(deployedModel));
      }

      return allDocumentTypes;
   }
   
   /**
    * @param processInstance
    * @param dataPathId
    * @return
    */
   public static DocumentType getDocumentTypeForDataPath(ProcessInstance processInstance, String dataPathId)
   {
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      ModelCache modelCache = ModelCache.findModelCache();
      String data = processDefinition.getDataPath(dataPathId).getData();
      Model model = modelCache.getModel(processInstance.getModelOID());
      return org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils.getDocumentTypeFromData(model,
            model.getData(data));
   }
}
