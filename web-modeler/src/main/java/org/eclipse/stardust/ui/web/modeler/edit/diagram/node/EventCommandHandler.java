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

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.EVENT_TYPE_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.START_EVENT;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;

/**
 * @author Sidharth.Singh
 */
@CommandHandler
public class EventCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "eventSymbol.create")
   public void createEvent(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      synchronized (model)
      {
         long maxOid = XpdlModelUtils.getMaxUsedOid(model);

         if (START_EVENT.equals(extractString(request,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY)))
         {
            StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();
            startEventSymbol.setElementOid(++maxOid);
            // TODO - Pass correct x,y co-ordinates rather than adjustment at server
            startEventSymbol.setXPos(extractInt(request, X_PROPERTY)
                  - parentLaneSymbol.getXPos());
            startEventSymbol.setYPos(extractInt(request, Y_PROPERTY)
                  - parentLaneSymbol.getYPos());
            startEventSymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
            startEventSymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));

            // TODO evaluate other properties

            processDefinition.getDiagram()
                  .get(0)
                  .getStartEventSymbols()
                  .add(startEventSymbol);
            parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

            //Add a manual trigger by default
            TriggerType manualTrigger = newManualTrigger(processDefinition) //
                  .accessibleTo(parentLaneSymbol.getParticipant())
                  .build();
            manualTrigger.setElementOid(++maxOid);
            startEventSymbol.setTrigger(manualTrigger);
         }
         else
         {
            EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();
            endEventSymbol.setElementOid(++maxOid);

            endEventSymbol.setXPos(extractInt(request, X_PROPERTY)
                  - parentLaneSymbol.getXPos());
            endEventSymbol.setYPos(extractInt(request, Y_PROPERTY)
                  - parentLaneSymbol.getYPos());
            endEventSymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
            endEventSymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));

            processDefinition.getDiagram()
                  .get(0)
                  .getEndEventSymbols()
                  .add(endEventSymbol);

            parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);
         }
      }
   }

   @OnCommand(commandId = "eventSymbol.delete")
   public void deleteEvent(ModelType model, LaneSymbol parentLaneSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);

      Long eventOId = extractLong(request, ModelerConstants.OID_PROPERTY);
      synchronized (model)
      {
         if (START_EVENT.equals(extractString(request,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY)))
         {
            StartEventSymbol startEventSymbol = getModelBuilderFacade().findStartEventSymbol(
                  parentLaneSymbol, eventOId);
            ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, startEventSymbol);
            processDefinition.getDiagram()
                  .get(0)
                  .getStartEventSymbols()
                  .remove(startEventSymbol);
            parentLaneSymbol.getStartEventSymbols().remove(startEventSymbol);
         }
         else
         {
            EndEventSymbol endEventSymbol = getModelBuilderFacade().findEndEventSymbol(parentLaneSymbol,
                  eventOId);
            ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, endEventSymbol);
            processDefinition.getDiagram()
                  .get(0)
                  .getEndEventSymbols()
                  .remove(endEventSymbol);
            parentLaneSymbol.getEndEventSymbols().remove(endEventSymbol);
         }
      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }

}
