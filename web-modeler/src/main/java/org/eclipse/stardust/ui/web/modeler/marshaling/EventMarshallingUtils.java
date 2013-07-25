package org.eclipse.stardust.ui.web.modeler.marshaling;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingModel;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.extensions.actions.abort.AbortActivityEventAction;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionCondition;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionConditionAccessPointProvider;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionConditionValidator;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.*;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.model.BpmPackageBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.NameIdUtils;

import com.google.gson.JsonObject;

public class EventMarshallingUtils
{
   public static final String TAG_INTERMEDIATE_EVENT_HOST = "stardust:bpmnIntermediateEventHost";

   public static final String TAG_END_EVENT_HOST = "stardust:bpmnEndEventHost";

   public static final String PREFIX_HOSTED_EVENT = "stardust:bpmnEvent";

   public static final String PRP_EVENT_HANDLER_ID = "eventHandlerUuid";

   /**
    * Tags an activity as carrier for an intermediate event. Due to intermediate events
    * not being a first class type in the Stardust meta model they have to be encoded as
    * specially tagged activities.
    *
    * @param activity
    * @see #isIntermediateEventHost(ActivityType)
    * @see #tagAsEndEventHost(ActivityType)
    */
   public static void tagAsIntermediateEventHost(ActivityType activity)
   {
      AttributeUtil.setBooleanAttribute(activity, TAG_INTERMEDIATE_EVENT_HOST, true);
   }

   public static void unTagAsIntermediateEventHost(ActivityType activity)
   {
      AttributeUtil.clearExcept(activity, new String[]{TAG_INTERMEDIATE_EVENT_HOST});
   }

   /**
    * Determines if the host activity is just an artificial carrier for an intermediate
    * event.
    *
    * @param activity
    *           the activity in question
    * @return <code>true</code> if the activity is tagged as a carrier for an intermediate
    *         event
    * @see #tagAsIntermediateEventHost(ActivityType)
    * @see #isEndEventHost(ActivityType)
    */
   public static boolean isIntermediateEventHost(ActivityType activity)
   {
      return AttributeUtil.getBooleanValue(activity, TAG_INTERMEDIATE_EVENT_HOST);
   }

   /**
    * Tags an activity as carrier for an end event. Due to end events not being a first
    * class type in the Stardust meta model they have to be encoded as specially tagged
    * activities.
    *
    * @param activity
    * @see #isEndEventHost(ActivityType)
    * @see #tagAsIntermediateEventHost(ActivityType)
    */
   public static void tagAsEndEventHost(ActivityType activity)
   {
      AttributeUtil.setBooleanAttribute(activity, TAG_END_EVENT_HOST, true);
   }

   /**
    * Determines if the host activity is just an artificial carrier for an end event.
    *
    * @param activity
    *           the activity in question
    * @return <code>true</code> if the activity is tagged as a carrier for an end event
    * @see #tagAsEndEventHost(ActivityType)
    * @see #isIntermediateEventHost(ActivityType)
    */
   public static boolean isEndEventHost(ActivityType activity)
   {
      return AttributeUtil.getBooleanValue(activity, TAG_END_EVENT_HOST);
   }

   public static ActivityType resolveHostActivity(AbstractEventSymbol eventSymbol)
   {
      DiagramType diagram = ModelUtils.findContainingDiagram(eventSymbol);
      if (null != diagram)
      {
         ProcessDefinitionType containingProcess = ModelUtils.findContainingProcess(diagram);
         if (null != containingProcess)
         {
            for (ActivityType activity : containingProcess.getActivity())
            {
               List<Long> hostedEvents = resolveHostedEvents(activity);
               if (hostedEvents.contains(eventSymbol.getElementOid()))
               {
                  return activity;
               }
            }
         }
      }

      return null;
   }

   public static List<Long> resolveHostedEvents(ActivityType activity)
   {
      List<Long> result = null;

      IAttributeCategory eventsCategory = AttributeUtil.createAttributeCategory(activity, PREFIX_HOSTED_EVENT);
      for (AttributeType attr : eventsCategory.getAttributes())
      {
         if (null == result)
         {
            result = newArrayList();
         }
         result.add(Long.valueOf(attr.getName().substring(eventsCategory.getFullId().length() + 1)));
      }

      if (null == result)
      {
         result = emptyList();
      }
      return result;
   }

   public static Long resolveHostedEvent(EventHandlerType eventHandler)
   {
      Long result = null;

      IAttributeCategory eventsCategory = AttributeUtil.createAttributeCategory(eventHandler, PREFIX_HOSTED_EVENT);
      for (AttributeType attr : eventsCategory.getAttributes())
      {
         result = Long.valueOf(attr.getName().substring(eventsCategory.getFullId().length() + 1));
         break;
      }

      return result;
   }

   public static JsonObject getEventHostingConfig(ActivityType activity, IntermediateEventSymbol eventSymbol, JsonMarshaller jsonIo)
   {
      return doGetEventHostingConfig(activity, eventSymbol, jsonIo);
   }

   public static JsonObject getEventHostingConfig(ActivityType activity, EndEventSymbol eventSymbol, JsonMarshaller jsonIo)
   {
      return doGetEventHostingConfig(activity, eventSymbol, jsonIo);
   }

   private static JsonObject doGetEventHostingConfig(ActivityType activity, AbstractEventSymbol eventSymbol, JsonMarshaller jsonIo)
   {
      String configValue = AttributeUtil.getCDataAttribute(activity, PREFIX_HOSTED_EVENT
            + ":" + Long.toString(eventSymbol.getElementOid()));

      JsonObject config = null;
      if (null != configValue)
      {
         config = jsonIo.readJsonObject(configValue);
      }
      return config;
   }

   public static void updateEventHostingConfig(ActivityType activity, IntermediateEventSymbol eventSymbol, JsonObject config)
   {
      doUpdateEventHostingConfig(activity, eventSymbol, config);
   }

   public static void updateEventHostingConfig(ActivityType activity, EndEventSymbol eventSymbol, JsonObject config)
   {
      doUpdateEventHostingConfig(activity, eventSymbol, config);
   }

   public static void bindEvent(EventHandlerType eventHandler, AbstractEventSymbol eventSymbol)
   {
      doUpdateEventHostingConfig(eventHandler, eventSymbol, new JsonObject());
   }

   private static void doUpdateEventHostingConfig(IExtensibleElement activity,
         AbstractEventSymbol eventSymbol, JsonObject config)
   {
      AttributeUtil.setCDataAttribute(activity,
            PREFIX_HOSTED_EVENT + ":" + Long.toString(eventSymbol.getElementOid()),
            (null != config) //
                  ? config.toString()
                  : null);
   }

   /**
    * @param activity
    * @param eventSymbol
    */
   public static void deleteEventHostingConfig(IExtensibleElement activity,
         AbstractEventSymbol eventSymbol)
   {
      String ids[] = new String[] {PREFIX_HOSTED_EVENT + ":"
            + Long.toString(eventSymbol.getElementOid())};
      AttributeUtil.clearExcept(activity, ids);
   }

   public static String encodeEventHandlerType(EventConditionTypeType conditionType)
   {
      return conditionType == null ? null : conditionType.getId();
   }

   public static Boolean encodeIsThrowingEvent(EventConditionTypeType conditionType)
   {
      if (null == conditionType)
      {
         return null;
      }

      if (PredefinedConstants.TIMER_CONDITION.equals(conditionType.getId()))
      {
         return true;
      }
      else if (PredefinedConstants.EXCEPTION_CONDITION.equals(conditionType.getId()))
      {
         return false;
      }
      else
      {
         // TODO more event types, ideally per pluggable SPI
         return null;
      }
   }

   public static Boolean encodeIsInterruptingEvent(EventHandlerType eventHandler)
   {
      if (null == eventHandler)
      {
         return null;
      }
      
      for (EventActionType action : eventHandler.getEventAction())
      {
         EventActionTypeType type = action.getType();
         if (type != null && PredefinedConstants.ABORT_ACTIVITY_ACTION.equals(type.getId()))
         {
            return true;
         }
      }
      return false;
   }

   public static EventConditionTypeType decodeEventHandlerType(String conditionTypeId, ModelType model)
   {
      if (conditionTypeId != null)
      {
         EventConditionTypeType conditionType = ModelUtils.findIdentifiableElement(
               model.getEventConditionType(), conditionTypeId);
         if (conditionType == null)
         {
            conditionType = injectPredefinedConditionType(model, conditionTypeId);
         }
         return conditionType;
      }
      return null;
   }

   public static EventActionTypeType decodeEventActionType(String actionTypeId, ModelType model)
   {
      if (actionTypeId != null)
      {
         EventActionTypeType actionType = ModelUtils.findIdentifiableElement(
               model.getEventActionType(), actionTypeId);
         if (actionType == null)
         {
            actionType = injectPredefinedActionType(model, actionTypeId);
         }
         return actionType;
      }
      return null;
   }
   
   private static EventConditionTypeType injectPredefinedConditionType(ModelType model, String conditionTypeId)
   {
      EventConditionTypeType conditionType = null;
      if (PredefinedConstants.TIMER_CONDITION.equals(conditionTypeId))
      {
         conditionType = newConditionType(conditionTypeId, "Timer", true, true,
            ImplementationType.PULL_LITERAL, new String[][] {
            {"carnot:engine:accessPointProvider", TimerAccessPointProvider.class.getName()},
            {"carnot:engine:binder", TimeStampBinder.class.getName()},
            {"carnot:engine:condition", TimeStampCondition.class.getName()},
            {"carnot:engine:pullEventEmitter", TimeStampEmitter.class.getName()},
            {"carnot:engine:validator", TimerValidator.class.getName()}
         });
      }
      else if (PredefinedConstants.EXCEPTION_CONDITION.equals(conditionTypeId))
      {
         conditionType = newConditionType(conditionTypeId, "On Exception", false, true,
            ImplementationType.ENGINE_LITERAL, new String[][] {
            {"carnot:engine:accessPointProvider", ExceptionConditionAccessPointProvider.class.getName()},
            {"carnot:engine:condition", ExceptionCondition.class.getName()},
            {"carnot:engine:validator", ExceptionConditionValidator.class.getName()}
         });
      }
      model.getEventConditionType().add(conditionType);
      return conditionType;
   }

   private static EventActionTypeType injectPredefinedActionType(ModelType model, String actionTypeId)
   {
      EventActionTypeType actionType = null;
      if (PredefinedConstants.ABORT_ACTIVITY_ACTION.equals(actionTypeId))
      {
         actionType = newActionType(actionTypeId, "Abort Activity", false, true,
            "timer, exception", "bind, unbind", new String[][] {
            {"carnot:engine:action", AbortActivityEventAction.class.getName()}
         });
      }
      model.getEventActionType().add(actionType);
      return actionType;
   }

   private static EventActionTypeType newActionType(String id, String name, boolean isProcessAction, boolean isActivityAction,
         String supportedConditionTypes, String unsupportedContexts, String[][] attributes)
   {
      EventActionTypeType actionType = BpmPackageBuilder.F_CWM.createEventActionTypeType();
      actionType.setId(id);
      actionType.setName(name);
      actionType.setIsPredefined(true);
      actionType.setProcessAction(isProcessAction);
      actionType.setActivityAction(isActivityAction);
      actionType.setSupportedConditionTypes(supportedConditionTypes);
      actionType.setUnsupportedContexts(unsupportedContexts);
      for (String[] attribute : attributes)
      {
         AttributeUtil.setAttribute(actionType, attribute[0], attribute[1]);
      }
      return actionType;
   }

   private static EventConditionTypeType newConditionType(String id, String name, boolean isProcessCondition, boolean isActivityCondition,
         ImplementationType engineLiteral, String[][] attributes)
   {
      EventConditionTypeType conditionType = BpmPackageBuilder.F_CWM.createEventConditionTypeType();
      conditionType.setId(id);
      conditionType.setName(name);
      conditionType.setIsPredefined(true);
      conditionType.setProcessCondition(isProcessCondition);
      conditionType.setActivityCondition(isActivityCondition);
      conditionType.setImplementation(ImplementationType.ENGINE_LITERAL);
      for (String[] attribute : attributes)
      {
         AttributeUtil.setAttribute(conditionType, attribute[0], attribute[1]);
      }
      return conditionType;
   }

   public static EventHandlerType newEventHandler(EventConditionTypeType type)
   {
      EventHandlerType eventHandler = BpmPackageBuilder.F_CWM.createEventHandlerType();
      eventHandler.setId(UUID.randomUUID().toString());
      eventHandler.setType(type);
      return eventHandler;
   }

   public static EventActionType newEventAction(EventActionTypeType type)
   {
      EventActionType action = BpmPackageBuilder.F_CWM.createEventActionType();
      action.setId(UUID.randomUUID().toString());
      action.setType(type);
      return action;
   }

   private EventMarshallingUtils()
   {
      // utility class
   }

   public static EventHandlerType createEventHandler(IntermediateEventSymbol eventSymbol, ActivityType hostActivity,
         JsonObject hostingConfig, String eventClass)
   {
      EventHandlerType eventHandler = null;
      EventConditionTypeType conditionType = decodeEventHandlerType(
            eventClass, findContainingModel(hostActivity));
      if (null != conditionType)
      {
         eventHandler = newEventHandler(conditionType);
         
         bindEvent(eventHandler, eventSymbol);
         hostActivity.getEventHandler().add(eventHandler);
   
         hostingConfig.addProperty(PRP_EVENT_HANDLER_ID, eventHandler.getId());
      }
      return eventHandler;
   }

   public static void updateEventHandler(EventHandlerType eventHandler, ActivityType hostActivity, JsonObject hostingConfig,
         JsonObject eventJson)
   {
      ModelElementUnmarshaller.updateIdentifiableElement(eventHandler, eventJson);
      ModelElementUnmarshaller.storeDescription(eventHandler, eventJson);
      
      if (eventJson.has(ModelerConstants.LOG_HANDLER_PROPERTY))
      {
         eventHandler.setLogHandler(extractBoolean(eventJson, ModelerConstants.LOG_HANDLER_PROPERTY));
      }
      
      if (eventJson.has(ModelerConstants.INTERRUPTING_PROPERTY))
      {
         
         // no bind or unbind actions supported.
         eventHandler.getBindAction().clear();
         eventHandler.getUnbindAction().clear();
         Boolean interrupting = extractBoolean(eventJson, ModelerConstants.INTERRUPTING_PROPERTY);
         if (interrupting == null || interrupting) // null means default value which is "true"
         {
            AttributeUtil.setAttribute(eventHandler, "carnot:engine:event:boundaryEventType", "Interrupting");
            
            // there should be exactly one abort action with scope sub hierarchy
            boolean found = false;
            for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i.hasNext();)
            {
               EventActionType action = i.next();
               if (found || action.getType() == null
                     || !PredefinedConstants.ABORT_ACTIVITY_ACTION.equals(action.getType().getId()))
               {
                  i.remove();
               }
               else
               {
                  found = true;
                  String scope = AttributeUtil.getAttributeValue(action, "carnot:engine:abort:scope");
                  if (!AbortScope.SUB_HIERARCHY.equals(scope))
                  {
                     AttributeUtil.setAttribute(action, "carnot:engine:abort:scope", AbortScope.SUB_HIERARCHY);
                  }
               }
            }
            if (!found)
            {
               EventActionTypeType actionType = decodeEventActionType(
                     PredefinedConstants.ABORT_ACTIVITY_ACTION, findContainingModel(hostActivity));
               if (actionType != null)
               {
                  EventActionType action = newEventAction(actionType);
                  action.setId(NameIdUtils.createIdFromName(ModelElementUnmarshaller.ABORT_ACTIVITY_NAME));
                  action.setName(ModelElementUnmarshaller.ABORT_ACTIVITY_NAME);
                  AttributeUtil.setAttribute(action, "carnot:engine:abort:scope", AbortScope.SUB_HIERARCHY);
                  eventHandler.getEventAction().add(action);
               }
            }
         }
         else
         {
            AttributeUtil.setAttribute(eventHandler, "carnot:engine:event:boundaryEventType", "Non-interrupting");
            
            // non-interrupting events have no actions.
            eventHandler.getEventAction().clear();
         }
      }
   
      hostingConfig.addProperty(PRP_EVENT_HANDLER_ID, eventHandler.getId());
   }

   public static ActivityType createHostActivity(ProcessDefinitionType processDefinition, String name)
   {
      ActivityType hostActivity = BpmModelBuilder.newRouteActivity(processDefinition)
            .withIdAndName("event_" + UUID.randomUUID(), name)
            .build();
      processDefinition.getActivity().add(hostActivity);
      return hostActivity;
   }
}
