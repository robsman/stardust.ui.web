/*
 * $Id$
 * (C) 2000 - 2012 CARNOT AG
 */
package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newProcessDefinition;
import static org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants.ID_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.ACTIVITIES_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.ATTRIBUTES_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.CONTROL_FLOWS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.DATA_FLOWS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.EVENTS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.GATEWAYS_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.MODEL_ID_PROPERTY;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.DiagramModeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrientationType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 *
 */
@Component
@Scope("prototype")
public class CreateProcessCommandHandler implements ICommandHandler
{
   @Override
   public boolean isValidTarget(Class< ? > type)
   {
      return ModelType.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      ModelType model = (ModelType) targetElement;
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String id = MBFacade.createIdFromName(name);
      ProcessDefinitionType processDefinition = newProcessDefinition(model).withIdAndName(id, name).build();
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);
      processDefinition.setElementOid(++maxOid);
      // Create diagram bits too

      DiagramType diagram = AbstractElementBuilder.F_CWM.createDiagramType();
      diagram.setMode(DiagramModeType.MODE_450_LITERAL);
      diagram.setOrientation(OrientationType.VERTICAL_LITERAL);
      diagram.setElementOid(++maxOid);
      diagram.setName("Diagram 1");

      PoolSymbol poolSymbol = AbstractElementBuilder.F_CWM.createPoolSymbol();

      diagram.getPoolSymbols().add(poolSymbol);

      poolSymbol.setElementOid(++maxOid);
      poolSymbol.setXPos(0);
      poolSymbol.setYPos(0);
      poolSymbol.setWidth(500);
      poolSymbol.setHeight(600);
      poolSymbol.setName("DEFAULT_POOL");
      poolSymbol.setId("DEFAULT_POOL");
      poolSymbol.setOrientation(OrientationType.VERTICAL_LITERAL);

      LaneSymbol laneSymbol = AbstractElementBuilder.F_CWM.createLaneSymbol();

      poolSymbol.getChildLanes().add(laneSymbol);
      laneSymbol.setParentPool(poolSymbol);

      laneSymbol.setElementOid(++maxOid);
      laneSymbol.setId(ModelerConstants.DEF_LANE_ID);
      laneSymbol.setName(ModelerConstants.DEF_LANE_NAME);
      laneSymbol.setXPos(10);
      laneSymbol.setYPos(10);
      laneSymbol.setWidth(480);
      laneSymbol.setHeight(580);
      laneSymbol.setOrientation(OrientationType.VERTICAL_LITERAL);

      processDefinition.getDiagram().add(diagram);

      newManualTrigger(processDefinition).accessibleTo(ADMINISTRATOR_ROLE).build();

      JsonObject processDefinitionJson = new JsonObject();

      processDefinitionJson.addProperty(ModelerConstants.TYPE_PROPERTY, "process");
      processDefinitionJson.addProperty(ID_PROPERTY, id);
      processDefinitionJson.addProperty(ModelerConstants.NAME_PROPERTY, name);
      processDefinitionJson.addProperty(MODEL_ID_PROPERTY, model.getId());
      processDefinitionJson.addProperty(ModelerConstants.TYPE_PROPERTY, "process");
      processDefinitionJson.add(ATTRIBUTES_PROPERTY, new JsonObject());
      processDefinitionJson.add(ACTIVITIES_PROPERTY, new JsonObject());
      processDefinitionJson.add(GATEWAYS_PROPERTY, new JsonObject());
      processDefinitionJson.add(EVENTS_PROPERTY, new JsonObject());
      processDefinitionJson.add(DATA_FLOWS_PROPERTY, new JsonObject());
      processDefinitionJson.add(CONTROL_FLOWS_PROPERTY, new JsonObject());
   }
}
