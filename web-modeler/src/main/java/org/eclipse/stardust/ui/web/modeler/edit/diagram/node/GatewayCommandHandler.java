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

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import javax.annotation.Resource;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;

/**
 * @author Sidharth.Singh
 */
@CommandHandler
public class GatewayCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "gateSymbol.create")
   public void createGateway(LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

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

         gateway.setElementOid(maxOid);
         gateway.setImplementation(ActivityImplementationType.ROUTE_LITERAL);

         processDefinition.getActivity().add(gateway);

         ActivitySymbolType gatewaySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

         gatewaySymbol.setElementOid(++maxOid);
         // TODO - Pass correct x,y co-ordinates rather than adjustment at server
         gatewaySymbol.setXPos(extractInt(request, X_PROPERTY)
               - parentLaneSymbol.getXPos() - ModelerConstants.POOL_LANE_MARGIN);
         gatewaySymbol.setYPos(extractInt(request, Y_PROPERTY)
               - parentLaneSymbol.getYPos() - ModelerConstants.POOL_LANE_MARGIN
               - ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT);
         gatewaySymbol.setActivity(gateway);
         gatewaySymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
         gatewaySymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));

         gateway.getActivitySymbols().add(gatewaySymbol);
         processDefinition.getDiagram().get(0).getActivitySymbol().add(gatewaySymbol);
         parentLaneSymbol.getActivitySymbol().add(gatewaySymbol);
      }
   }

   /**
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "gateSymbol.delete")
   public void deleteGateway(LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      String gatewayId = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY);
      ActivityType gateway = getModelBuilderFacade().findActivity(processDefinition, gatewayId);
      ActivitySymbolType gatewaySymbol = gateway.getActivitySymbols().get(0);
      synchronized (model)
      {
         processDefinition.getActivity().remove(gateway);
         processDefinition.getDiagram().get(0).getActivitySymbol().remove(gatewaySymbol);

         parentLaneSymbol.getActivitySymbol().remove(gatewaySymbol);

      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return new ModelBuilderFacade(springContext.getBean(ModelService.class)
            .getModelManagementStrategy());
   }

}
