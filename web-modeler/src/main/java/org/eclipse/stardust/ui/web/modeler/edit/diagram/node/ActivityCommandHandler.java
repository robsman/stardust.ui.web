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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonObject;

/**
 * 
 * @author Sidharth.Singh
 * 
 */
public class ActivityCommandHandler implements ICommandHandler
{

   @Override
   public boolean isValidTarget(Class< ? > type)
   {
      return IIdentifiableElement.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      LaneSymbol parentLaneSymbol = (LaneSymbol) targetElement;
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      if ("activitySymbol.create".equals(commandId))
      {
         createActivity(parentLaneSymbol, model, processDefinition, request);
      }
      else if ("activitySymbol.delete".equals(commandId))
      {
         deleteActivity(parentLaneSymbol, model, processDefinition, request);
      }
   }

   /**
    *
    * @param parentLaneSymbol
    * @param model
    * @param processDefinition
    * @param request
    */
   private void createActivity(LaneSymbol parentLaneSymbol, ModelType model, ProcessDefinitionType processDefinition,
         JsonObject request)
   {

      String activityType = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.ACTIVITY_TYPE);
      String activityId = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY);

      String activityName = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.NAME_PROPERTY);
      String participantFullID = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.PARTICIPANT_FULL_ID);
      String applicationFullID = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.APPLICATION_FULL_ID_PROPERTY);
      String subProcessID = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.SUBPROCESS_ID);
      int xProperty = extractInt(request, X_PROPERTY);
      int yProperty = extractInt(request, Y_PROPERTY);
      int widthProperty = extractInt(request, WIDTH_PROPERTY);
      int heightProperty = extractInt(request, HEIGHT_PROPERTY);
      synchronized (model)
      {
         long maxOid = XpdlModelUtils.getMaxUsedOid(model);
         String modelId = model.getId();

         ActivityType activity = MBFacade.createActivity(modelId, processDefinition, activityType, participantFullID,
               activityId, activityName, applicationFullID, subProcessID, maxOid++);

         ModelService.setDescription(activity, request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));

         ActivitySymbolType activitySymbol = MBFacade.createActivitySymbol(processDefinition, parentLaneSymbol.getId(),
               xProperty, yProperty, widthProperty, heightProperty, maxOid, activity);
      }
   }

   /**
    *
    * @param parentLaneSymbol
    * @param model
    * @param processDefinition
    * @param request
    */
   private void deleteActivity(LaneSymbol parentLaneSymbol, ModelType model, ProcessDefinitionType processDefinition,
         JsonObject request)
   {
      String activityId = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY);
      ActivityType activity = MBFacade.findActivity(processDefinition, activityId);
      ActivitySymbolType activitySymbol = activity.getActivitySymbols().get(0);

      synchronized (model)
      {

         processDefinition.getActivity().remove(activity);
         processDefinition.getDiagram().get(0).getActivitySymbol().remove(activitySymbol);

         parentLaneSymbol.getActivitySymbol().remove(activitySymbol);

      }

   }

}
