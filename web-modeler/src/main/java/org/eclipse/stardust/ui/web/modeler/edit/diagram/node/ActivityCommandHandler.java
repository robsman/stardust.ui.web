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

package org.eclipse.stardust.ui.web.modeler.edit.diagram.node;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 * 
 * @author Sidharth.Singh
 * 
 */
@CommandHandler
public class ActivityCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "activitySymbol.create")
   public void createActivity(ModelType model, LaneSymbol parentLaneSymbol,
         JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      String activityType = extractString(request,
            ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ACTIVITY_TYPE);
      String taskType = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.TASK_TYPE);
      String activityName = extractString(request,
            ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.NAME_PROPERTY);
      String participantFullID = extractString(request,
            ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.PARTICIPANT_FULL_ID);
      String applicationFullID = extractString(request,
            ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.APPLICATION_FULL_ID_PROPERTY);
      String subProcessID = extractString(request,
            ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.SUBPROCESS_ID);
      // TODO -Remove the adjustment and pass correct co-ordinates for symbols.
      int xProperty = extractInt(request, X_PROPERTY);
      int yProperty = extractInt(request, Y_PROPERTY);
      int widthProperty = extractInt(request, WIDTH_PROPERTY);
      int heightProperty = extractInt(request, HEIGHT_PROPERTY);
      synchronized (model)
      {
         ActivityType activity = getModelBuilderFacade().createActivity(model,
               processDefinition, activityType, taskType, null, activityName, participantFullID,
               applicationFullID, subProcessID);

         ModelService.setDescription(activity,
               request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));

         ActivitySymbolType activitySymbol = getModelBuilderFacade().createActivitySymbol(
               model, activity, processDefinition, parentLaneSymbol.getId(), xProperty,
               yProperty, widthProperty, heightProperty);
      }
   }

   /**
    * 
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "activitySymbol.delete")
   public void deleteActivity(ModelType model, LaneSymbol parentLaneSymbol,
         JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      String activityId = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.ID_PROPERTY);
      ActivityType activity = getModelBuilderFacade().findActivity(processDefinition,
            activityId);
      ActivitySymbolType activitySymbol = activity.getActivitySymbols().get(0);

      synchronized (model)
      {
         ModelElementEditingUtils.deleteEventSymbols(activity, parentLaneSymbol);
         
         ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition,
               activitySymbol);
         ModelElementEditingUtils.deleteDataMappingConnection(processDefinition,
               activitySymbol.getDataMappings().iterator());

         processDefinition.getActivity().remove(activity);
         processDefinition.getDiagram().get(0).getActivitySymbol().remove(activitySymbol);

         parentLaneSymbol.getActivitySymbol().remove(activitySymbol);
      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}