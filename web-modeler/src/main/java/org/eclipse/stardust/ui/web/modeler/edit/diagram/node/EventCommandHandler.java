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

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IntermediateEventSymbol;
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
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;

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
         if (START_EVENT.equals(extractString(request,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY)))
         {
            StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();
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
                  .accessibleTo(LaneParticipantUtil.getParticipant(parentLaneSymbol))
                  .build();
            manualTrigger.setName("");
            startEventSymbol.setTrigger(manualTrigger);
         }
         else if (ModelerConstants.INTERMEDIATE_EVENT.equals(extractString(request,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY)))
         {
            IntermediateEventSymbol eventSymbol = AbstractElementBuilder.F_CWM.createIntermediateEventSymbol();
            // TODO - Pass correct x,y co-ordinates rather than adjustment at server
            eventSymbol.setXPos(extractInt(request, X_PROPERTY)
                  - parentLaneSymbol.getXPos());
            eventSymbol.setYPos(extractInt(request, Y_PROPERTY)
                  - parentLaneSymbol.getYPos());
            eventSymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
            eventSymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));

            processDefinition.getDiagram()
                  .get(0)
                  .getIntermediateEventSymbols()
                  .add(eventSymbol);
            parentLaneSymbol.getIntermediateEventSymbols().add(eventSymbol);

            // add a host activity
            ActivityType hostActivity  = null;
            JsonObject modelElement = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
            if (modelElement.has(ModelerConstants.BINDING_ACTIVITY_UUID))
            {
               hostActivity = ModelUtils.findIdentifiableElement(
                     processDefinition.getActivity(),
                     extractAsString(modelElement, ModelerConstants.BINDING_ACTIVITY_UUID));
            }

            if (null == hostActivity)
            {
               hostActivity = BpmModelBuilder.newRouteActivity(processDefinition)
                     .withIdAndName("event_" + UUID.randomUUID(), "Intermediate Event")
                     .build();
               processDefinition.getActivity().add(hostActivity);
            }

            EventMarshallingUtils.tagAsIntermediateEventHost(hostActivity);

            EventMarshallingUtils.updateEventHostingConfig(hostActivity, eventSymbol,
                  new JsonObject());

            // TODO evaluate other properties
         }
         else
         {
            EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();

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
         }else
         {
            EndEventSymbol endEventSymbol = getModelBuilderFacade().findEndEventSymbol(parentLaneSymbol,
                  eventOId);
            if(endEventSymbol != null)
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

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}