/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author Barry.Grotjahn
 *
 */
@CommandHandler
public class QualityAssuranceCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "qualityAssuranceCode.create")
   public void createQualityAssuranceCode(ModelType model, JsonObject request)
   {
      Code code = null;
      String qaId = extractString(request, ModelerConstants.ID_PROPERTY);      
      String qaName = extractString(request, ModelerConstants.NAME_PROPERTY);
      String qaDescription = extractString(request, ModelerConstants.DESCRIPTION_PROPERTY);

      synchronized (model)
      {      
         QualityControlType qualityControl = model.getQualityControl();
         if(qualityControl == null)
         {
            qualityControl = CarnotWorkflowModelFactory.eINSTANCE.createQualityControlType();
            model.setQualityControl(qualityControl);         
         }
         
         code = CarnotWorkflowModelFactory.eINSTANCE.createCode();
         code.setCode(qaId);
         code.setName(qaName);
         if(!StringUtils.isEmpty(qaDescription))
         {
            code.setValue(qaDescription);
         }
         qualityControl.getCode().add(code);
      }      
      
      EObjectUUIDMapper mapper = modelService().getModelBuilderFacade().getModelManagementStrategy().uuidMapper();
      mapper.map(code);      
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "qualityAssuranceCode.delete")
   public void deleteQualityAssuranceCode(ModelType model, JsonObject request)
   {
      String uuid = extractString(request, ModelerConstants.UUID_PROPERTY);
      if(!StringUtils.isEmpty(uuid))
      {
         Code code = (Code) modelService().getModelBuilderFacade().getModelManagementStrategy().uuidMapper().getEObject(uuid);
         if(code != null)
         {
            synchronized (model)
            {            
               model.getQualityControl().getCode().remove(code);
            }
         }      
      }
   }
      
   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }
}