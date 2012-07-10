/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.diagram.node;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

import com.google.gson.JsonObject;

public class CreateActivityCommandHandler implements ICommandHandler
{

   @Override
   public boolean isValidTarget(Class< ? > type)
   {
      return LaneSymbol.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, IModelElement targetElement, JsonObject request)
   {
      LaneSymbol parentLaneSymbol = (LaneSymbol) targetElement;
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      String activityType = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            ModelerConstants.ACTIVITY_TYPE);
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
               modelId, model.getName(), applicationFullID, subProcessID, maxOid);

         ModelService.setDescription(activity, request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));

         ActivitySymbolType activitySymbol = MBFacade.createActivitySymbol(processDefinition, parentLaneSymbol.getId(),
               xProperty, yProperty, widthProperty, heightProperty, maxOid, activity);
      }
   }

}
