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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;

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
}
