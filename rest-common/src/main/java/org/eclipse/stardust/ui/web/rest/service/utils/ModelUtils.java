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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Models;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ModelUtils
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;
   
   public DeployedModel getModel(int oid)
   {
      return serviceFactoryUtils.getQueryService().getModel(oid);
   }

   /**
    * @return
    */
   public Models getActiveModels()
   {
      Models deployedModelDescriptions = serviceFactoryUtils.getQueryService().getModels(
            DeployedModelQuery.findActive());

      return deployedModelDescriptions;
   }

}
