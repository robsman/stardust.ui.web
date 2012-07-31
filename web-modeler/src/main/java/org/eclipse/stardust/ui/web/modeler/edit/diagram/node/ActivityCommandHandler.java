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

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 *
 * @author Sidharth.Singh
 *
 */
@CommandHandler
public class ActivityCommandHandler
{
   /**
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "activitySymbol.create")
   public void createActivity(LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

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
      //TODO -Remove the adjustment and pass correct co-ordinates for symbols.
      int xProperty = extractInt(request, X_PROPERTY) - ModelerConstants.POOL_LANE_MARGIN;
      int yProperty = extractInt(request, Y_PROPERTY) - ModelerConstants.POOL_LANE_MARGIN
            - ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT;
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
    * @param request
    */
   @OnCommand(commandId = "activitySymbol.delete")
   public void deleteActivity(LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

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
