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
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.EVENT_TYPE_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.START_EVENT;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import java.util.UUID;

import javax.annotation.Resource;

import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementUnmarshaller;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

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
         String eventType = extractString(request,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY);
         if (START_EVENT.equals(eventType))
         {
            StartEventSymbol startEventSymbol = updateAndAddSymbol(parentLaneSymbol, request,
                  AbstractElementBuilder.F_CWM.createStartEventSymbol());

            // TODO evaluate other properties

            //Add a manual trigger by default
            TriggerType manualTrigger = newManualTrigger(processDefinition) //
                  .accessibleTo(LaneParticipantUtil.getParticipant(parentLaneSymbol))
                  .build();
            manualTrigger.setName("");
            startEventSymbol.setTrigger(manualTrigger);
         }
         else if (ModelerConstants.INTERMEDIATE_EVENT.equals(eventType))
         {
            IntermediateEventSymbol eventSymbol = updateAndAddSymbol(parentLaneSymbol, request,
                  AbstractElementBuilder.F_CWM.createIntermediateEventSymbol());

            // add a host activity
            ActivityType hostActivity  = null;
            JsonObject eventJson = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
            if (eventJson.has(ModelerConstants.BINDING_ACTIVITY_UUID))
            {
               hostActivity = ModelUtils.findIdentifiableElement(
                     processDefinition.getActivity(),
                     extractAsString(eventJson, ModelerConstants.BINDING_ACTIVITY_UUID));
            }

            if (null == hostActivity)
            {
               hostActivity = BpmModelBuilder.newRouteActivity(processDefinition)
                     .withIdAndName("event_" + UUID.randomUUID(), "Intermediate Event")
                     .build();
               processDefinition.getActivity().add(hostActivity);

               EventMarshallingUtils.tagAsIntermediateEventHost(hostActivity);
            }

            JsonObject hostingConfig = new JsonObject();

            String eventClass = extractAsString(eventJson, ModelerConstants.EVENT_CLASS_PROPERTY);
            EventHandlerType eventHandler = EventMarshallingUtils.createEventHandler(eventSymbol,
                  hostActivity, hostingConfig, eventClass);
            if (eventHandler != null)
            {
               ModelElementUnmarshaller.updateEventHandler(eventHandler, hostActivity, hostingConfig, eventJson);
            }

            EventMarshallingUtils.updateEventHostingConfig(hostActivity, eventSymbol, hostingConfig );
         }
         else
         {
            EndEventSymbol endEventSymbol = updateAndAddSymbol(parentLaneSymbol, request,
                  AbstractElementBuilder.F_CWM.createEndEventSymbol());

            //TODO: hasNotJsonNull required here?
            String eventName = request.getAsJsonObject(
                  ModelerConstants.MODEL_ELEMENT_PROPERTY).has(
                  ModelerConstants.NAME_PROPERTY)
                  ? extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
                        ModelerConstants.NAME_PROPERTY) : "End Event";
            // add a host activity
            ActivityType hostActivity = BpmModelBuilder.newRouteActivity(processDefinition)
                  .withIdAndName("event_" + UUID.randomUUID(), eventName)
                  .build();
            EventMarshallingUtils.tagAsEndEventHost(hostActivity);

            processDefinition.getActivity().add(hostActivity);

            EventMarshallingUtils.updateEventHostingConfig(hostActivity, endEventSymbol,
                  new JsonObject());

            // TODO evaluate other properties
         }
      }
   }

   private <T extends AbstractEventSymbol> T updateAndAddSymbol(LaneSymbol parentLaneSymbol,
         JsonObject request, T symbol)
   {
      // TODO - Pass correct x,y co-ordinates rather than adjustment at server
      symbol.setXPos(extractInt(request, X_PROPERTY)
            - parentLaneSymbol.getXPos());
      symbol.setYPos(extractInt(request, Y_PROPERTY)
            - parentLaneSymbol.getYPos());
      symbol.setWidth(extractInt(request, WIDTH_PROPERTY));
      symbol.setHeight(extractInt(request, HEIGHT_PROPERTY));
      
      addSymbol(ModelUtils.findContainingProcess(parentLaneSymbol).getDiagram().get(0), symbol);
      addSymbol(parentLaneSymbol, symbol);

      return symbol;
   }

   private void addSymbol(ISymbolContainer container, AbstractEventSymbol symbol)
   {
      if (symbol instanceof StartEventSymbol)
      {
         container.getStartEventSymbols().add((StartEventSymbol) symbol);
      }
      else if (symbol instanceof IntermediateEventSymbol)
      {
         container.getIntermediateEventSymbols().add((IntermediateEventSymbol) symbol);
      }
      else if (symbol instanceof EndEventSymbol)
      {
         container.getEndEventSymbols().add((EndEventSymbol) symbol);
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
            StartEventSymbol startEventSymbol = ModelBuilderFacade.findStartEventSymbol(
                  parentLaneSymbol, eventOId);

            // Delete the associated trigger too, if it exists
            // TODO - should be moved to ModelBuilderFacade?
            TriggerType trigger = startEventSymbol.getTrigger();
            if (null != trigger)
            {
               processDefinition.getTrigger().remove(trigger);
            }

            ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, startEventSymbol);
            processDefinition.getDiagram()
                  .get(0)
                  .getStartEventSymbols()
                  .remove(startEventSymbol);
            parentLaneSymbol.getStartEventSymbols().remove(startEventSymbol);
         }
         if (ModelerConstants.INTERMEDIATE_EVENT.equals(extractString(request,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY)))
         {
            IntermediateEventSymbol intEventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(
                  parentLaneSymbol, eventOId);
            if(intEventSymbol != null)
            {
               ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, intEventSymbol);
               processDefinition.getDiagram()
                     .get(0)
                     .getIntermediateEventSymbols()
                     .remove(intEventSymbol);

               ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(intEventSymbol);

               //delete associated activity
               if (null != hostActivity)
               {
                  if (ActivityImplementationType.ROUTE_LITERAL.equals(hostActivity.getImplementation()))
                  {
                     processDefinition.getActivity().remove(hostActivity);
                  }
                  // unbind the event from activity
                  else if (ActivityImplementationType.MANUAL_LITERAL.equals(hostActivity.getImplementation()))
                  {
                     EventMarshallingUtils.unTagAsIntermediateEventHost(hostActivity);
                     EventMarshallingUtils.deleteEventHostingConfig(hostActivity,
                           intEventSymbol);
                  }
               }
               parentLaneSymbol.getIntermediateEventSymbols().remove(intEventSymbol);

            }
         }
         else
         {
            EndEventSymbol endEventSymbol = ModelBuilderFacade.findEndEventSymbol(parentLaneSymbol,
                  eventOId);
            if (endEventSymbol != null)
            {
               ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, endEventSymbol);
               processDefinition.getDiagram()
                     .get(0)
                     .getEndEventSymbols()
                     .remove(endEventSymbol);

               ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(endEventSymbol);
               processDefinition.getActivity().remove(hostActivity);
               parentLaneSymbol.getEndEventSymbols().remove(endEventSymbol);

            }
         }
      }
   }
/*
   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }*/
}