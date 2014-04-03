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

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newRouteActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author Sidharth.Singh
 */
@CommandHandler
public class GatewayCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "gateSymbol.create")
   public void createGateway(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      synchronized (model)
      {
         EObjectUUIDMapper mapper = modelService().uuidMapper();
         // encode Gateway as Route Activity (default configuration)
         String name = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.NAME_PROPERTY);
         if(StringUtils.isEmpty(name))
         {
            name = "gateway"; //$NON-NLS-1$
         }
         
         ActivityType gateway = newRouteActivity(processDefinition) //
               .withIdAndName(null, name)
               .usingControlFlow(JoinSplitType.XOR_LITERAL, JoinSplitType.XOR_LITERAL).build();
         gateway.setName(""); //$NON-NLS-1$
         mapper.map(gateway);
         // add gateway to model
         processDefinition.getActivity().add(gateway);

         // apply any non-default settings
         ModelBinding<ModelType> modelBinding = modelService().findModelBinding(model);
         modelBinding.updateModelElement(gateway, request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));

         // create node symbol
         ActivitySymbolType gatewaySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();
         mapper.map(gatewaySymbol);
         // connect symbol with model element
         gatewaySymbol.setActivity(gateway);

         // add symbol to lane
         parentLaneSymbol.getActivitySymbol().add(gatewaySymbol);

         // apply any non-default settings
         modelBinding.updateModelElement(gatewaySymbol, request);
      }
   }

   /**
    * @param parentLaneSymbol
    * @param request
    */
   @OnCommand(commandId = "gateSymbol.delete")
   public void deleteGateway(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      String gatewayId = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY);
      ActivityType gateway = getModelBuilderFacade().findActivity(processDefinition, gatewayId);
      ActivitySymbolType gatewaySymbol = gateway.getActivitySymbols().get(0);
      synchronized (model)
      {
         ModelElementEditingUtils.deleteTransitionConnections(gatewaySymbol);

         processDefinition.getActivity().remove(gateway);
         processDefinition.getDiagram().get(0).getActivitySymbol().remove(gatewaySymbol);

         parentLaneSymbol.getActivitySymbol().remove(gatewaySymbol);

      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }
}
