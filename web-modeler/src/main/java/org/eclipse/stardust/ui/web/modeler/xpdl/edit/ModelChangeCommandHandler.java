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

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;

import java.util.*;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.exception.ModelerErrorClass;
import org.eclipse.stardust.model.xpdl.builder.exception.ModelerException;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ExternalReferenceUtils;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.SchemaLocatorAdapter;
import org.eclipse.stardust.model.xpdl.util.NameIdUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ModelCommandsHandler;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

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
      AttributeUtil.setAttribute(model, PredefinedConstants.MODEL_ELEMENT_UUID, modelUUID);

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

      if (request.has("createBusinessDate"))
      {
         facade.createPrimitiveData(model, "BusinessDate", "Business Date",
               ModelerConstants.DATE_PRIMITIVE_DATA_TYPE);
      }
      
      model.setId(preventDuplicateFilenames(model.getId()));
      
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
   private ModificationDescriptor cloneModel(String commandId, ModelType orgModel, JsonObject request)
   {
      ModelBuilderFacade facade = new ModelBuilderFacade(modelService.getModelManagementStrategy());
      
      ModelType model = performFilebasedClone(orgModel);
            
      Collection<ModelType> models = facade.getModelManagementStrategy().getModels().values();      
      List<ModelType> list = new ArrayList<ModelType>(models);
      
      model.setId(orgModel.getId() + "CLONE");
      model.setName("CLONE - " + orgModel.getName());
      
      String id = NameIdUtils.createIdFromName(list, model);
      model.setId(id);

      //This is a unique model UUID used to identify references
      String modelUUID = UUID.randomUUID().toString();
      AttributeUtil.setAttribute(model, PredefinedConstants.MODEL_ELEMENT_UUID, modelUUID);

      modelService.getModelBuilderFacade().setModified(model, model.getCreated());
      EObjectUUIDMapper mapper = modelService.uuidMapper();
      mapper.map(model);

      //Add all model elements to the uuid map and additionally re-generate the carnot:model:uuid attribute.
      for (Iterator<EObject> i = model.eAllContents(); i.hasNext();)
      {
         EObject obj = i.next();
         if (obj instanceof IExtensibleElement)
         {
            IExtensibleElement extObj = (IExtensibleElement) obj;
            modelUUID = UUID.randomUUID().toString();
            AttributeUtil.setAttribute(extObj, PredefinedConstants.MODEL_ELEMENT_UUID, modelUUID);
         }   
         mapper.map(obj);
      }
      
      model.setId(preventDuplicateFilenames(model.getId()));     
      modelService.getModelManagementStrategy()
            .getModels()
            .put(model.getId(), model);
      modelService.getModelManagementStrategy().saveModel(model);
      model.eResource().eAdapters().add(new SchemaLocatorAdapter());

      ModificationDescriptor changes = new ModificationDescriptor();
      changes.added.add(modelService.currentSession().xpdlMarshaller().toModelJson(model));
      return changes;
   }

   private ModelType performFilebasedClone(ModelType orgModel)
   {
      byte[] orgModelBytes = XpdlModelIoUtils.saveModel(orgModel);     
      ModelType model = XpdlModelIoUtils.loadModel(orgModelBytes, modelService.getModelManagementStrategy());
      return model;
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
            //This happens if a inconsistent / broken model is deleted
            JsonObject removeInfo = new JsonObject();            
            removeInfo.addProperty(ModelerConstants.TYPE_PROPERTY, "model");
            removeInfo.addProperty(ModelerConstants.ID_PROPERTY, model.getId());                            
            removeInfo.addProperty(ModelerConstants.UUID_PROPERTY, modelService.uuidMapper().getUUID(model));
            changes.removed.add(removeInfo);
         }
         modelMgtStrategy.deleteModel(model);
         
         // Remove pending elements from EObjectUUIDMapper and purge them
         for (Iterator<EObject> i = model.eAllContents(); i.hasNext();)
         {
            EObject element = i.next();
            modelService.currentSession().uuidMapper().unmap(element, true);
         }
         modelService.currentSession().uuidMapper().unmap(model, false);
         modelService.currentSession().uuidMapper().cleanup();         
      }
      return changes;
   }
   
   private String preventDuplicateFilenames(String modelID) 
   {
      for (Iterator<ModelType> i = modelService.getModelManagementStrategy()
            .getModels().values().iterator(); i.hasNext();)
      {
         ModelType modelType = i.next();
         if (modelType != null)
         {
            if ((modelID + ".xpdl").equals(modelService
                  .getModelManagementStrategy().getModelFileName(modelType)))
            {
               modelID = modelID + "1";
               return preventDuplicateFilenames(modelID);
            }
         }
      }
      return modelID;
   }
}