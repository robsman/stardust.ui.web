package org.eclipse.stardust.ui.web.modeler.xpdl.marshalling;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingModel;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.extensions.actions.abort.AbortActivityEventAction;
import org.eclipse.stardust.engine.core.extensions.actions.complete.CompleteActivityEventAction;
import org.eclipse.stardust.engine.core.extensions.actions.excludeuser.ExcludeUserAction;
import org.eclipse.stardust.engine.core.extensions.conditions.assignment.AssignmentCondition;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionCondition;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionConditionAccessPointProvider;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionConditionValidator;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimeStampBinder;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimeStampCondition;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimeStampEmitter;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimerAccessPointProvider;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimerValidator;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.model.BpmPackageBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.EventActionType;
import org.eclipse.stardust.model.xpdl.carnot.EventActionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventConditionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.IAttributeCategory;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.ImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.IntermediateEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.NameIdUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;

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
      activity.setHibernateOnCreation(true);
   }

   public static void unTagAsIntermediateEventHost(ActivityType activity)
   {
      AttributeUtil.clearExcept(activity, new String[]{TAG_INTERMEDIATE_EVENT_HOST});
      activity.setHibernateOnCreation(false);
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
      String id = PREFIX_HOSTED_EVENT + ":"
            + Long.toString(eventSymbol.getElementOid());

      AttributeUtil.setAttribute(activity, id, null);
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
      else if (PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION.equals(conditionTypeId))
      {
         conditionType = newConditionType(conditionTypeId, "On Assignment", false, true,
            ImplementationType.ENGINE_LITERAL, new String[][] {
            {"carnot:engine:condition", AssignmentCondition.class.getName()},
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
      else if (PredefinedConstants.COMPLETE_ACTIVITY_ACTION.equals(actionTypeId))
      {
         actionType = newActionType(actionTypeId, "Complete Activity", false, true,
            "timer, exception", "bind", new String[][] {
            {"carnot:engine:action", CompleteActivityEventAction.class.getName()}
         });
      }
      else if (PredefinedConstants.EXCLUDE_USER_ACTION.equals(actionTypeId))
      {
         actionType = newActionType(actionTypeId, "Exclude User", false, true,
               "onAssignment", "bind", new String[][] {
            {"carnot:engine:action", ExcludeUserAction.class.getName()}
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

   private static EventConditionTypeType newConditionType(String id, String name,
         boolean isProcessCondition, boolean isActivityCondition,
         ImplementationType implementation, String[][] attributes)
   {
      EventConditionTypeType conditionType = BpmPackageBuilder.F_CWM.createEventConditionTypeType();
      conditionType.setId(id);
      conditionType.setName(name);
      conditionType.setIsPredefined(true);
      conditionType.setProcessCondition(isProcessCondition);
      conditionType.setActivityCondition(isActivityCondition);
      conditionType.setImplementation(implementation);
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
         // (fh) enable automatic binding by default on all PULL events.
         eventHandler.setAutoBind(ImplementationType.PULL_LITERAL == conditionType.getImplementation());

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

         String eventType = null;

         Boolean interrupting = extractBoolean(eventJson, ModelerConstants.INTERRUPTING_PROPERTY);
         if (interrupting == null || interrupting) // null means default value which is "true"
         {
            eventType = "Interrupting";

            // there should be exactly one abort action with scope sub hierarchy
            EventActionType action = setEventAction(eventHandler,
                  PredefinedConstants.ABORT_ACTIVITY_ACTION,
                  ModelElementUnmarshaller.ABORT_ACTIVITY_NAME);

            String scope = AttributeUtil.getAttributeValue(action, "carnot:engine:abort:scope");
            if (!AbortScope.SUB_HIERARCHY.equals(scope))
            {
               AttributeUtil.setAttribute(action, "carnot:engine:abort:scope", AbortScope.SUB_HIERARCHY);
            }
         }
         else
         {
            if (EventMarshallingUtils.isIntermediateEventHost(hostActivity))
            {
               // non-interrupting non-boundary events have just one complete action.
               setEventAction(eventHandler,
                     PredefinedConstants.COMPLETE_ACTIVITY_ACTION,
                     ModelElementUnmarshaller.COMPLETE_ACTIVITY_NAME);
            }
            else
            {
               eventType = "Non-interrupting";

               // non-interrupting boundary events have no actions.
               eventHandler.getEventAction().clear();
            }
         }

         AttributeUtil.setAttribute(eventHandler, "carnot:engine:event:boundaryEventType", eventType);
      }

      hostingConfig.addProperty(PRP_EVENT_HANDLER_ID, eventHandler.getId());
   }

   public static EventActionType setEventAction(EventHandlerType eventHandler, String typeId, String defaultName)
   {
      EventActionType action = null;

      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i.hasNext();)
      {
         EventActionType a = i.next();
         if (action != null || a.getType() == null || !typeId.equals(a.getType().getId()))
         {
            i.remove();
         }
         else
         {
            action = a;
         }
      }

      if (action == null)
      {
         EventActionTypeType actionType = decodeEventActionType(typeId, findContainingModel(eventHandler));
         if (actionType != null)
         {
            action = newEventAction(actionType);
            action.setId(NameIdUtils.createIdFromName(defaultName));
            action.setName(defaultName);
            eventHandler.getEventAction().add(action);
         }
      }
      return action;
   }

   public static ActivityType createHostActivity(ProcessDefinitionType processDefinition, String name)
   {
      ActivityType hostActivity = BpmModelBuilder.newRouteActivity(processDefinition)
            .withIdAndName("event_" + UUID.randomUUID(), name)
            .build();
      processDefinition.getActivity().add(hostActivity);
      return hostActivity;
   }

   public static void createExcludeUserAction(ActivityType activity, JsonObject activityJson)
   {
      JsonObject euJson = activityJson.getAsJsonObject(ModelerConstants.EU_EXCLUDE_USER);

      String dataFullID =  euJson.get(ModelerConstants.EU_EXCLUDE_PERFORMER_DATA).getAsString();

      String dataID = dataFullID;

      if (dataFullID.split(":").length > 1)
      {
         dataFullID = dataFullID.split(":")[1];
      }

      String dataPath = GsonUtils.extractAsString(euJson, ModelerConstants.EU_EXCLUDE_PERFORMER_DATA_PATH);

      if (dataPath != null)
      {
         dataPath = euJson.get(ModelerConstants.EU_EXCLUDE_PERFORMER_DATA_PATH).getAsString();
      }

      ModelType model = ModelUtils.findContainingModel(activity);
      EventConditionTypeType conditionType = EventMarshallingUtils.decodeEventHandlerType(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, model);

      EventHandlerType eventHandler = EventMarshallingUtils.findExcludeUserEventHandler(activity);

      if (eventHandler == null)
      {
         eventHandler = EventMarshallingUtils.newEventHandler(conditionType);
      }

      activity.getEventHandler().add(eventHandler);
      EventActionType action = EventMarshallingUtils.setEventAction(eventHandler,PredefinedConstants.EXCLUDE_USER_ACTION, PredefinedConstants.EXCLUDE_USER_ACTION);
      AttributeUtil.setAttribute(action, PredefinedConstants.EXCLUDED_PERFORMER_DATA, dataID);
      AttributeUtil.setAttribute(action, PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH, dataPath);
      eventHandler.setType(conditionType);
      eventHandler.setId(ModelerConstants.EU_EXCLUDE_USER_INTERNAL);
      eventHandler.setName("Exclude User");
   }

   public static void removeExcludeUserAction(ActivityType activity)
   {
      EventHandlerType tobeRemoved = null;
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         if (eventHandler.getType().getId().equals(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION) && eventHandler.getId().equals(ModelerConstants.EU_EXCLUDE_USER_INTERNAL))
         {
            tobeRemoved = eventHandler;
         }
      }
      if (tobeRemoved != null)
      {
         activity.getEventHandler().remove(tobeRemoved);
      }
   }

   public static EventHandlerType findExcludeUserEventHandler(ActivityType activity)
   {
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         if (eventHandler.getType().getId()
               .equals(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION)
               && eventHandler.getId().equals(ModelerConstants.EU_EXCLUDE_USER_INTERNAL))
         {
            return eventHandler;
         }
      }
      return null;
   }

   public static JsonObject getOnAssignmentHandlers(ActivityType activity)
   {
      JsonObject oaJson = new JsonObject();
      JsonArray eventHandlersJson = new JsonArray();
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         if (eventHandler.getType().getId()
               .equals(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
         {
            JsonObject eventHandlerJson = new JsonObject();
            JsonArray eventActionsJson = new JsonArray();
            for (Iterator<EventActionType> j = eventHandler.getEventAction().iterator(); j
                  .hasNext();)
            {
               EventActionType eventActionType = j.next();
               if (eventActionType.getType().getId().equals("excludeUser"))
               {
                  JsonObject eventActionJson = new JsonObject();
                  eventActionJson.addProperty("id", eventActionType.getId());
                  eventActionJson.addProperty("name", eventActionType.getName());
                  eventActionJson.addProperty(ModelerConstants.EU_EXCLUDE_PERFORMER_DATA,
                        AttributeUtil.getAttributeValue(eventActionType,
                              PredefinedConstants.EXCLUDED_PERFORMER_DATA));
                  eventActionJson.addProperty(
                        ModelerConstants.EU_EXCLUDE_PERFORMER_DATA_PATH, AttributeUtil
                              .getAttributeValue(eventActionType,
                                    PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH));
                  eventActionsJson.add(eventActionJson);
               }
            }
            eventHandlerJson.addProperty("id", eventHandler.getId());
            eventHandlerJson.addProperty("name", eventHandler.getName());
            eventHandlerJson.addProperty("consumeOnMatch",
                  eventHandler.isConsumeOnMatch());
            eventHandlerJson.addProperty("logHandler", eventHandler.isLogHandler());
            eventHandlerJson.add("eventAction", eventActionsJson);
            eventHandlersJson.add(eventHandlerJson);
         }
         oaJson.add("eventHandler", eventHandlersJson);

      }
      return oaJson;
   }


}
