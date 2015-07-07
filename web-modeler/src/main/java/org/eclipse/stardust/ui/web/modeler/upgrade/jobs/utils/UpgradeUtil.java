/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.upgrade.jobs.utils;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

public class UpgradeUtil
{      
   public static ModelService modelService()
   {
      return (ModelService) ManagedBeanUtils.getManagedBean("modelService");
   }
   
   public static ModelBuilderFacade getModelBuilderFacade()
   {      
      ModelService service = (ModelService) ManagedBeanUtils.getManagedBean("modelService");
      return service.getModelBuilderFacade();
   }   
}