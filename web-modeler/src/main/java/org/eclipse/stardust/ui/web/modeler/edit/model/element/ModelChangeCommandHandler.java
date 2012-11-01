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

package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;

import java.util.Date;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 * @author Shrikant.Gangal
 *
 */
@Component
@Scope("singleton")
public class ModelChangeCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   public boolean isValidTarget(Class<? > type)
   {
      return IIdentifiableElement.class.isAssignableFrom(type);
   }

   public JsonObject handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      if ("model.create".equals(commandId))
      {
         return createModel(commandId, request);
      }
      else if ("model.update".equals(commandId))
      {
         return updateModel(commandId, targetElement, request);
      }
      else if ("model.delete".equals(commandId))
      {
         return deleteModel(commandId, targetElement, request);
      }

      return null;
   }

   /**
    * @param commandId
    * @param request
    */
   private JsonObject createModel(String commandId, JsonObject request)
   {
      ModelBuilderFacade facade = new ModelBuilderFacade(modelService().getModelManagementStrategy());
      String modelName = request.get(ModelerConstants.NAME_PROPERTY).getAsString();
      String modelID = generateId(modelName);
      ModelType model = facade.createModel(modelID, modelName);
      modelService().getModelBuilderFacade().setModified(model, model.getCreated());
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(model);

      //Assign UUID to default data
      if (null != model.getData()) {
         for (DataType data : model.getData())
         {
            mapper.map(data);
         }
      }
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      AttributeUtil.setAttribute(model, PredefinedConstants.VERSION_ATT, "1");

      RoleType admin = AbstractElementBuilder.F_CWM.createRoleType();
      admin.setName(ADMINISTRATOR_ROLE);
      admin.setId(ADMINISTRATOR_ROLE);
      long adminOid = ++maxOid;
      admin.setElementOid(adminOid);

      model.getRole().add(admin);
      mapper.map(admin);

      modelService().getModelManagementStrategy()
            .getModels()
            .put(model.getId(), model);

      JsonArray added = new JsonArray();
      JsonObject addedModel = modelService().modelElementMarshaller().toModelJson(model);
      added.add(addedModel);
      return generateResponse(commandId, null, added, null);
   }

   /**
    * @param commandId
    * @param obj
    * @param request
    * @return
    */
   private JsonObject deleteModel(String commandId, EObject obj, JsonObject request)
   {
      if (null != obj && obj instanceof ModelType) {
         ModelType model = (ModelType) obj;

         ModelManagementStrategy modelMgtStrategy = modelService(). getModelManagementStrategy();
         modelMgtStrategy.deleteModel(model);
         JsonArray deleted = new JsonArray();
         JsonObject deletedModel = modelService().modelElementMarshaller().toModelJson(model);
         deleted.add(deletedModel);

         return generateResponse(commandId, null, null, deleted);
      }

      return generateResponse(commandId, null, null, null);
   }

   /**
    * @param commandId
    * @param obj
    * @param request
    * @return
    */
   private JsonObject updateModel(String commandId, EObject obj, JsonObject request)
   {
      if (null != obj && obj instanceof ModelType) {
         ModelType model = (ModelType) obj;
         //Delete old model xpdl
         ModelManagementStrategy modelMgtStrategy = springContext.getBean(ModelService.class).getModelManagementStrategy();
         modelMgtStrategy.deleteModel(model);

         modelService().currentSession().modelElementUnmarshaller().populateFromJson(model, request);

         model.setId(generateId(model.getName()));
         modelMgtStrategy.getModels().put(model.getId(), model);
         modelService().getModelBuilderFacade().setModified(model, new Date());
         modelMgtStrategy.saveModel(model);

         JsonArray modified = new JsonArray();
         JsonObject modifiedModel = modelService().modelElementMarshaller().toModelJson(model);
         modified.add(modifiedModel);
         return generateResponse(commandId, modified, null, null);
      }

      return generateResponse(commandId, null, null, null);
   }

   /**
    * @param commandId
    * @param modified
    * @param added
    * @param removed
    */
   private JsonObject generateResponse(String commandId, JsonArray modified, JsonArray added,
         JsonArray removed)
   {
      JsonObject response = new JsonObject();

      response.addProperty("id", System.currentTimeMillis());
      response.addProperty("account", "sheldor"); // TODO Robert add!
      response.addProperty("timestamp", System.currentTimeMillis());

      response.addProperty("commandId", commandId);

      JsonObject jsChanges = new JsonObject();
      response.add("changes", jsChanges);

      JsonArray jsModified = modified;
      if (null == jsModified)
      {
         jsModified = new JsonArray();
      }
      jsChanges.add("modified", jsModified);

      JsonArray jsAdded = added;
      if (null == jsAdded)
      {
         jsAdded = new JsonArray();
      }
      jsChanges.add("added", jsAdded);

      JsonArray jsRemoved = removed;
      if (null == jsRemoved)
      {
         jsRemoved = new JsonArray();
      }
      jsChanges.add("removed", jsRemoved);

      return response;
   }

   /**
    * @return
    */
   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   /**
    * @param modelName
    * @return
    */
   private String generateId(String modelName)
   {
      String id = modelService().getModelBuilderFacade().createIdFromName(modelName);
      String newId = id;
      int postFix = 1;
      while (null != modelService().getModelBuilderFacade().findModel(newId))
      {
         newId = id + "_" + postFix++ ;
      }

      return newId;
   }
}

