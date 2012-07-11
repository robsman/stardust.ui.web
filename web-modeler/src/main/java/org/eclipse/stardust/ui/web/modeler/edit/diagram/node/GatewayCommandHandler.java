/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.diagram.node;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
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

import com.google.gson.JsonObject;

public class GatewayCommandHandler implements ICommandHandler
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
      if ("gateSymbol.create".equals(commandId))
      {
         createGateway(parentLaneSymbol, model, processDefinition, request);
      }
   }

   private void createGateway(LaneSymbol parentLaneSymbol, ModelType model, ProcessDefinitionType processDefinition,
         JsonObject request)
   {
      synchronized (model)
      {
         long maxOid = XpdlModelUtils.getMaxUsedOid(model);
         ActivityType gateway = null;

         // TODO Should be Route
         gateway = newManualActivity(processDefinition)
               .withIdAndName(
                     extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY),
                     extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.NAME_PROPERTY))
               .havingDefaultPerformer(ADMINISTRATOR_ROLE).build();

         processDefinition.getActivity().add(gateway);

         ActivitySymbolType gatewaySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

         gatewaySymbol.setElementOid(++maxOid);

         gatewaySymbol.setXPos(extractInt(request, X_PROPERTY) - parentLaneSymbol.getXPos());
         gatewaySymbol.setYPos(extractInt(request, Y_PROPERTY) - parentLaneSymbol.getYPos());
         gatewaySymbol.setActivity(gateway);

         gateway.getActivitySymbols().add(gatewaySymbol);
         processDefinition.getDiagram().get(0).getActivitySymbol().add(gatewaySymbol);
         parentLaneSymbol.getActivitySymbol().add(gatewaySymbol);
      }
   }

}
