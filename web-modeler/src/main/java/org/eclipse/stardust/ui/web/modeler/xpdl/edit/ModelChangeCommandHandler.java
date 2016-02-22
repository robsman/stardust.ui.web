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

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.UUID;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ExternalReferenceUtils;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.SchemaLocatorAdapter;
import org.eclipse.stardust.ui.web.modeler.common.exception.ModelerErrorClass;
import org.eclipse.stardust.ui.web.modeler.common.exception.ModelerException;
import org.eclipse.stardust.ui.web.modeler.edit.model.ModelConversionService;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ModelCommandsHandler;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 * @author Robert.Sauer
 *
 */
@CommandHandler
public class ModelChangeCommandHandler implements ModelCommandsHandler
{
   @Resource
   private ApplicationContext springContext;

   @Resource
   private ModelService modelService;

   @Override
   public boolean handlesModel(String formatId)
   {
      return "xpdl".equalsIgnoreCase(formatId);
   }

   @Override
   public ModificationDescriptor handleCommand(String commandId, EObject context, JsonObject request)
   {
      if ("model.create".equals(commandId))
      {
         return createModel(commandId, request);
      }
      else
      {
         ModelType model = (ModelType) context;
         if (modelService.getModelBuilderFacade().isReadOnly(model))
         {
            // TODO bad request?
            return null;
         }
         if ("model.clone".equals(commandId))
         {
            return cloneModel(commandId, model, request);
         }
         else if ("model.delete".equals(commandId))
         {
            return deleteModel(commandId, model, request);
         }
      }

      return null;
   }

   /**
    * @param commandId
    * @param request
    */
   private ModificationDescriptor createModel(String commandId, JsonObject request)
   {
      ModelBuilderFacade facade = new ModelBuilderFacade(modelService.getModelManagementStrategy());
      String modelName = request.get(ModelerConstants.NAME_PROPERTY).getAsString();
      String id = null != request.get(ModelerConstants.ID_PROPERTY) ? request.get(
            ModelerConstants.ID_PROPERTY).getAsString() : null;
      ModelType model = facade.createModel(id, modelName);

      //This is a unique model UUID used to identify references
      String modelUUID = UUID.randomUUID().toString();
      AttributeUtil.setAttribute(model, "carnot:model:uuid", modelUUID);

      modelService.getModelBuilderFacade().setModified(model, model.getCreated());
      EObjectUUIDMapper mapper = modelService.uuidMapper();
      mapper.map(model);

      //Assign UUID to default data
      if (null != model.getData()) {
         for (DataType data : model.getData())
         {
            mapper.map(data);
         }
      }
      AttributeUtil.setAttribute(model, PredefinedConstants.VERSION_ATT, "1");

      RoleType admin = AbstractElementBuilder.F_CWM.createRoleType();
      admin.setName(ADMINISTRATOR_ROLE);
      admin.setId(ADMINISTRATOR_ROLE);

      model.getRole().add(admin);
      mapper.map(admin);

      if (request.has("createBusinessDate")) {
         DataType businessDate = facade.createPrimitiveData(model, "BusinessDate", "Business Date",
               ModelerConstants.DATE_PRIMITIVE_DATA_TYPE);
      }

      modelService.getModelManagementStrategy()
            .getModels()
            .put(model.getId(), model);
      modelService.getModelManagementStrategy().saveModel(model);
      model.eResource().eAdapters().add(new SchemaLocatorAdapter());

      ModificationDescriptor changes = new ModificationDescriptor();
      changes.added.add(modelService.currentSession().xpdlMarshaller().toModelJson(model));
      return changes;
   }

   /**
    * @param commandId
    * @param request
    */
   private ModificationDescriptor cloneModel(String commandId, ModelType model, JsonObject request)
   {
      ModelConversionService conversionService = springContext
            .getBean(ModelConversionService.class);

      String targetFormat = extractString(request, "targetFormat");
      if (isEmpty(targetFormat))
      {
         targetFormat = "bpmn2";
      }
      EObject modelCopy = conversionService.convertModel(model, targetFormat);

      ModificationDescriptor changes = new ModificationDescriptor();
      ModelBinding<EObject> modelBinding = modelService.currentSession()
            .modelRepository().getModelBinding(modelCopy);
      changes.added.add(modelBinding.getMarshaller().toModelJson(modelCopy));
      return changes;
   }

   /**
    * @param commandId
    * @param obj
    * @param request
    * @return
    */
   private ModificationDescriptor deleteModel(String commandId, ModelType model, JsonObject request)
   {
      ModificationDescriptor changes = new ModificationDescriptor();

      if (ExternalReferenceUtils.isModelReferenced(model, modelService.currentSession()
            .modelManagementStrategy().getModels().values()))
      {
         throw new ModelerException(ModelerErrorClass.UNABLE_TO_DELETE_REFERENCED_MODEL);
      }

      if (null != model)
      {
         ModelManagementStrategy modelMgtStrategy = modelService
               .getModelManagementStrategy();
         try
         {
            changes.removed.add(modelService.currentSession().xpdlMarshaller()
                  .toModelJson(model));
         }
         catch (Exception e)
         {
         }
         modelMgtStrategy.deleteModel(model);
      }
      return changes;
   }
}