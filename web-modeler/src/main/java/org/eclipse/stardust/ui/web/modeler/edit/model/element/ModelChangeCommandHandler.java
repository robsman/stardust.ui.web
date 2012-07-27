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
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newBpmModel;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementHelper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;

/**
 * @author Shrikant.Gangal
 * 
 */
@Component
@Scope("prototype")
public class ModelChangeCommandHandler implements ICommandHandler
{
   private ModelType model;

   private JsonObject response;

   @Resource
   private ApplicationContext springContext;

   @Override
   public boolean isValidTarget(Class<? > type)
   {
      return IIdentifiableElement.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      if ("model.create".equals(commandId))
      {
         createModel(commandId, request);
      }
      else if ("model.delete".equals(commandId))
      {
         deleteModel(commandId, request);
      }
   }

   /**
    * @return
    */
   public JsonObject getResponseJSON()
   {
      return response;
   }

   /**
    * 
    * @param parentSymbol
    * @param model
    * @param processDefinition
    * @param request
    */
   private void createModel(String commandId, JsonObject request)
   {
      model = newBpmModel().withIdAndName(
            request.get(ModelerConstants.ID_PROPERTY).getAsString(),
            request.get(ModelerConstants.NAME_PROPERTY).getAsString()).build();
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      AttributeUtil.setAttribute(model, PredefinedConstants.VERSION_ATT, "1");

      RoleType admin = AbstractElementBuilder.F_CWM.createRoleType();
      admin.setName(ADMINISTRATOR_ROLE);
      admin.setId(ADMINISTRATOR_ROLE);
      long adminOid = ++maxOid;
      admin.setElementOid(adminOid);

      model.getRole().add(admin);

      ModelManagementHelper.getInstance()
            .getModelManagementStrategy()
            .getModels()
            .put(model.getId(), model);

      JsonArray added = new JsonArray();
      JsonObject addedModel = springContext.getBean(ModelElementMarshaller.class).toModel(model);
      added.add(addedModel);
      generateResponse(commandId, null, added, null);
   }

   /**
    * 
    * @param parentSymbol
    * @param model
    * @param processDefinition
    * @param request
    */
   private void deleteModel(String commandId, JsonObject request)
   {
      // TODO
   }

   /**
    * @param commandId
    * @param modified
    * @param added
    * @param removed
    */
   private void generateResponse(String commandId, JsonArray modified, JsonArray added,
         JsonArray removed)
   {
      response = new JsonObject();

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
   }

   /**
    * @return
    */
   public JsonObject getResponse()
   {
      return response;
   }
}
