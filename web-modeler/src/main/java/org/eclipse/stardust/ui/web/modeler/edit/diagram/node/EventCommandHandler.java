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
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findIdentifiableElement;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

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
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
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

   private JsonMarshaller jsonIo = new JsonMarshaller();

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
               String eventName = request.getAsJsonObject(
                     ModelerConstants.MODEL_ELEMENT_PROPERTY).has(
                     ModelerConstants.NAME_PROPERTY)
                     ? extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
                           ModelerConstants.NAME_PROPERTY) : "Intermediate Event";
               hostActivity = EventMarshallingUtils.createHostActivity(processDefinition, eventName);
               EventMarshallingUtils.tagAsIntermediateEventHost(hostActivity);
            }

            JsonObject hostingConfig = new JsonObject();

            String eventClass = extractAsString(eventJson, ModelerConstants.EVENT_CLASS_PROPERTY);
            EventHandlerType eventHandler = EventMarshallingUtils.createEventHandler(eventSymbol,
                  hostActivity, hostingConfig, eventClass);
            if (eventHandler != null)
            {
               EventMarshallingUtils.updateEventHandler(eventHandler, hostActivity, hostingConfig, eventJson);
            }

            EventMarshallingUtils.updateEventHostingConfig(hostActivity, eventSymbol, hostingConfig);
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
            ActivityType hostActivity = EventMarshallingUtils.createHostActivity(processDefinition, eventName);
            EventMarshallingUtils.tagAsEndEventHost(hostActivity);

            // TODO evaluate other properties

            EventMarshallingUtils.updateEventHostingConfig(hostActivity, endEventSymbol,
                  new JsonObject());
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
      String eventType = extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY,
            EVENT_TYPE_PROPERTY);
      synchronized (model)
      {
         if (START_EVENT.equals(eventType))
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

            ModelElementEditingUtils.deleteTransitionConnections(startEventSymbol);
            processDefinition.getDiagram()
                  .get(0)
                  .getStartEventSymbols()
                  .remove(startEventSymbol);
            parentLaneSymbol.getStartEventSymbols().remove(startEventSymbol);
         }
         else if (ModelerConstants.INTERMEDIATE_EVENT.equals(eventType))
         {
            IntermediateEventSymbol eventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(
                  parentLaneSymbol, eventOId);
            if (eventSymbol != null)
            {
               // TODO: (fh) refactor code, smells.
               ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(eventSymbol);
               
               if (hostActivity != null && !EventMarshallingUtils.isIntermediateEventHost(hostActivity))
               {
                  JsonObject hostingConfig = EventMarshallingUtils.getEventHostingConfig(
                        hostActivity, eventSymbol, jsonIo);
                  if (hostingConfig != null)
                  {
                     String eventHandlerId = extractAsString(hostingConfig, EventMarshallingUtils.PRP_EVENT_HANDLER_ID);
                     EventHandlerType eventHandler = findIdentifiableElement(hostActivity.getEventHandler(), eventHandlerId);
                     if (eventHandler != null)
                     {
                        // delete matching transitions
                        String match = "ON_BOUNDARY_EVENT(" + eventHandlerId + ")";
                        for (TransitionType transition : hostActivity.getOutTransitions())
                        {
                           String expression = getExpression(transition);
                           if (expression != null && match.equals(expression))
                           {
                              // this deletes corresponding transition connections too
                              ModelElementEditingUtils.deleteIdentifiable(transition);
                              break;
                           }
                        }
                        // now remove the event handler
                        hostActivity.getEventHandler().remove(eventHandler);
                     }
                     EventMarshallingUtils.deleteEventHostingConfig(hostActivity, eventSymbol);
                  }
               }
               else
               {
                  ModelElementEditingUtils.deleteTransitionConnections(eventSymbol);
                  processDefinition.getDiagram()
                        .get(0)
                        .getIntermediateEventSymbols()
                        .remove(eventSymbol);
   
                     //delete associated activity
                  if (null != hostActivity)
                  {
                     List<TransitionType> delete = new ArrayList<TransitionType>();
                     for (TransitionType transition : hostActivity.getOutTransitions())
                     {
                        delete.add(transition);
                     }
                     for (TransitionType transition : hostActivity.getInTransitions())
                     {
                        delete.add(transition);
                     }
                     for (TransitionType transition : delete)
                     {                     
                        ModelElementEditingUtils.deleteIdentifiable(transition);
                     }
                     
                     if (ActivityImplementationType.ROUTE_LITERAL.equals(hostActivity.getImplementation()))
                     {
                        processDefinition.getActivity().remove(hostActivity);
                     }
                     // unbind the event from activity
                     else if (ActivityImplementationType.MANUAL_LITERAL.equals(hostActivity.getImplementation()))
                     {
                        EventMarshallingUtils.unTagAsIntermediateEventHost(hostActivity);
                        EventMarshallingUtils.deleteEventHostingConfig(hostActivity, eventSymbol);
                     }
                  }
               }
               ModelElementEditingUtils.deleteTransitionConnections(eventSymbol);
               parentLaneSymbol.getIntermediateEventSymbols().remove(eventSymbol);
            }
         }
         else
         {
            EndEventSymbol endEventSymbol = ModelBuilderFacade.findEndEventSymbol(parentLaneSymbol,
                  eventOId);
            if (endEventSymbol != null)
            {
               ModelElementEditingUtils.deleteTransitionConnections(endEventSymbol);
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
      
   private String getExpression(TransitionType transition)
   {
      XmlTextNode type = transition.getExpression();
      String expression = type == null ? null : ModelUtils.getCDataString(transition.getExpression().getMixed());
      return expression;
   }
}