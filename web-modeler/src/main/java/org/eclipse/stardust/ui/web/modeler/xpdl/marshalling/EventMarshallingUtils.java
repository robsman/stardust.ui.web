package org.eclipse.stardust.ui.web.modeler.xpdl.marshalling;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingModel;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.extensions.actions.abort.AbortActivityEventAction;
import org.eclipse.stardust.engine.core.extensions.actions.complete.CompleteActivityEventAction;
import org.eclipse.stardust.engine.core.extensions.actions.delegate.DelegateEventAction;
import org.eclipse.stardust.engine.core.extensions.actions.excludeuser.ExcludeUserAction;
import org.eclipse.stardust.engine.core.extensions.actions.schedule.ScheduleEventAction;
import org.eclipse.stardust.engine.core.extensions.actions.setdata.SetDataAction;
import org.eclipse.stardust.engine.core.extensions.conditions.assignment.AssignmentCondition;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionCondition;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionConditionAccessPointProvider;
import org.eclipse.stardust.engine.core.extensions.conditions.exception.ExceptionConditionValidator;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.*;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.engine.extensions.events.signal.SignalEventCondition;
import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.model.BpmPackageBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IdFactory;
import org.eclipse.stardust.model.xpdl.util.NameIdUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.ModelElementEditingUtils;

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
      else if ("signal".equals(conditionTypeId))
      {
         conditionType = newConditionType(conditionTypeId, "Catch Signal", false, true,
            ImplementationType.ENGINE_LITERAL, new String[][] {
            // TODO access point provider
            {"carnot:engine:condition", SignalEventCondition.class.getName()}
            // TODO validator
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
      else if (PredefinedConstants.SET_DATA_ACTION.equals(actionTypeId))
      {
         actionType = newActionType(actionTypeId, "Set Data", false, true,
               "exception", "bind", new String[][] {
            {"carnot:engine:action", SetDataAction.class.getName()}
         });
      }
      else if (PredefinedConstants.SCHEDULE_ACTIVITY_ACTION.equals(actionTypeId))
      {
         actionType = newActionType(actionTypeId, "Schedule Activity", false, true,
               "timer, exception, statechange", "bind", new String[][] {
            {"carnot:engine:action", ScheduleEventAction.class.getName()}
         });
      }
      else if (PredefinedConstants.DELEGATE_ACTIVITY_ACTION.equals(actionTypeId))
      {
         actionType = newActionType(actionTypeId, "Delegate Activity", false, true,
               "timer, exception, statechange", "bind", new String[][] {
            {"carnot:engine:action", DelegateEventAction.class.getName()}
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

   public static BindActionType newBindAction(EventActionTypeType type)
   {
      BindActionType action = BpmPackageBuilder.F_CWM.createBindActionType();
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
      if (eventJson.has(ModelerConstants.CONSUME_ON_MATCH_PROPERTY))
      {
         eventHandler.setConsumeOnMatch(extractBoolean(eventJson, ModelerConstants.CONSUME_ON_MATCH_PROPERTY));
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
                  ModelElementUnmarshaller.ABORT_ACTIVITY_NAME, false);

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
                     ModelElementUnmarshaller.COMPLETE_ACTIVITY_NAME, false);
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

      ModelType model = ModelUtils.findContainingModel(hostActivity);
      if(model != null)
      {
         String supportedConditions = null;
         for(EventConditionTypeType type : model.getEventConditionType())
         {
            if(supportedConditions == null)
            {
               supportedConditions = type.getId();
            }
            else
            {
               supportedConditions += ", ";
               supportedConditions += type.getId();
            }
         }
         for(EventActionTypeType type : model.getEventActionType())
         {
            type.setSupportedConditionTypes(supportedConditions);
         }
      }

      hostingConfig.addProperty(PRP_EVENT_HANDLER_ID, eventHandler.getId());
   }

   public static EventActionType setEventAction(EventHandlerType eventHandler, String typeId, String defaultName, boolean keepOtherActions)
   {
      EventActionType action = null;

      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i.hasNext();)
      {
         EventActionType a = i.next();
         if (action != null || a.getType() == null || !typeId.equals(a.getType().getId()))
         {
            if (!keepOtherActions)
            {
               i.remove();
            }
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

   public static BindActionType setBindAction(EventHandlerType eventHandler, String typeId, String defaultName, boolean keepOtherActions)
   {
      BindActionType action = null;

      for (Iterator<BindActionType> i = eventHandler.getBindAction().iterator(); i.hasNext();)
      {
         BindActionType a = i.next();
         if (action != null || a.getType() == null || !typeId.equals(a.getType().getId()))
         {
            if (!keepOtherActions)
            {
               i.remove();
            }
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
            action = newBindAction(actionType);
            action.setId(NameIdUtils.createIdFromName(defaultName));
            action.setName(defaultName);
            eventHandler.getBindAction().add(action);
         }
      }
      return action;
   }

   public static EventActionType addEventAction(EventHandlerType eventHandler,
         String typeId, String defaultName, boolean keepOtherActions)
   {
      EventActionType action = null;
      EventActionTypeType actionType = decodeEventActionType(typeId,
            findContainingModel(eventHandler));
      if (actionType != null)
      {
         action = newEventAction(actionType);
         IdFactory idFactory = null;
         if (defaultName == null)
         {
            idFactory = new IdFactory("Exclude User", "Exclude User");
            idFactory.computeNames(eventHandler.getEventAction(), false);
            action.setId(idFactory.getId());
            action.setName(idFactory.getName());
         }
         else
         {
            idFactory = new IdFactory(defaultName, defaultName);
            idFactory.computeNames(eventHandler.getEventAction(), false);
            action.setId(idFactory.getId());
            action.setName(defaultName);
         }
         eventHandler.getEventAction().add(action);
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

   public static void updateResubmissionHandler(EventHandlerType eventHandler,
         JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(eventHandler);

      boolean defaultPerformer = false;
      String delayValue = null;
      String delayUnit = null;

      boolean useData = AttributeUtil.getBooleanValue(eventHandler, "carnot:engine:useData");

      String dataFullID = null;
      if (AttributeUtil.getAttributeValue(eventHandler, "carnot:engine:data") != null)
      {
         dataFullID =  model.getId() + ":" + AttributeUtil.getAttributeValue(eventHandler, "carnot:engine:data");
      }

      String dataPath = AttributeUtil.getAttributeValue(eventHandler, PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT);

      String period = AttributeUtil.getAttributeValue(eventHandler,"carnot:engine:period");
      if (period != null)
      {
         delayValue = ModelElementEditingUtils.getDelayUnit(period).split(":")[0];
         delayUnit = ModelElementEditingUtils.getDelayUnit(period).split(":")[1];
      }

      //AttributeUtil.setAttribute(eventHandler, "carnot:engine:data", null);

      EventActionType delegateAction = getFirstResubmissionDelegateAction(eventHandler);

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_DELAY_VALUE))
      {
         delayValue = request.get(ModelerConstants.RS_DELAY_VALUE).getAsString();
         setUseConstant(eventHandler, delayValue, delayUnit);
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_DELAY_UNIT))
      {
         delayUnit = request.get(ModelerConstants.RS_DELAY_UNIT).getAsString();
         setUseConstant(eventHandler, delayValue, delayUnit);
      }

      //Change useData / useConstant
      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_USEDATA))
      {
         useData = request.get(ModelerConstants.RS_USEDATA).getAsBoolean();
         if (!useData)
         {
            setUseConstant(eventHandler, delayValue, delayUnit);
         }
         else
         {
            setUseData(eventHandler, dataFullID, dataPath);
         }
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.DATA_FULL_ID_PROPERTY))
      {
         dataFullID = request.get(ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();
         setUseData(eventHandler, dataFullID, dataPath);
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.DATA_PATH_PROPERTY))
      {
         dataPath = request.get(ModelerConstants.DATA_PATH_PROPERTY).getAsString();
         setUseData(eventHandler, dataFullID, dataPath);
      }

      //Changed delegate to default performer
      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_DELEGATE_TO_DEFAULT_PERFORMER))
      {
         defaultPerformer = request.get(ModelerConstants.RS_DELEGATE_TO_DEFAULT_PERFORMER).getAsBoolean();
         if (defaultPerformer)
         {
            if (delegateAction == null)
            {
               createDelegateAction(eventHandler);
            }
         } else
         {
            eventHandler.getEventAction().remove(delegateAction);
         }
      }
   }

   public static void createResubmissionHandler(ActivityType activity, JsonObject request, EObjectUUIDMapper uuidMapper)
   {
      ModelType model = ModelUtils.findContainingModel(activity);
      EventConditionTypeType conditionType = EventMarshallingUtils.decodeEventHandlerType(PredefinedConstants.TIMER_CONDITION, model);

      boolean useData = false;
      boolean defaultPerformer = false;
      String dataFullID = null;
      String dataPath = null;
      String delayValue = null;
      String delayUnit = null;

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_USEDATA))
      {
         useData = request.get(ModelerConstants.RS_USEDATA).getAsBoolean();
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.DATA_FULL_ID_PROPERTY))
      {
         dataFullID = request.get(ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.DATA_PATH_PROPERTY))
      {
         dataPath = request.get(ModelerConstants.DATA_PATH_PROPERTY).getAsString();
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_DELAY_VALUE))
      {
         delayValue = request.get(ModelerConstants.RS_DELAY_VALUE).getAsString();
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_DELAY_UNIT))
      {
         delayUnit = request.get(ModelerConstants.RS_DELAY_UNIT).getAsString();
      }

      if (GsonUtils.hasNotJsonNull(request, ModelerConstants.RS_DELEGATE_TO_DEFAULT_PERFORMER))
      {
         defaultPerformer = request.get(ModelerConstants.RS_DELEGATE_TO_DEFAULT_PERFORMER).getAsBoolean();
      }

      EventHandlerType eventHandler = EventMarshallingUtils.newEventHandler(conditionType);
      eventHandler.setType(conditionType);
      eventHandler.setId(ModelerConstants.RS_RESUBMISSION);
      eventHandler.setName(ModelerConstants.RS_RESUBMISSION);

      AttributeUtil.setBooleanAttribute(eventHandler, "carnot:engine:useData", useData);


      if (!useData)
      {
         setUseConstant(eventHandler, delayValue, delayUnit);
      } else
      {
         setUseData(eventHandler, dataFullID, dataPath);
      }

      activity.getEventHandler().add(eventHandler);
      uuidMapper.map(eventHandler);

      BindActionType bindAction = EventMarshallingUtils.setBindAction(eventHandler,PredefinedConstants.SCHEDULE_ACTIVITY_ACTION, "Bind Action", true);
      AttributeUtil.setAttribute(bindAction, PredefinedConstants.TARGET_STATE_ATT, "org.eclipse.stardust.engine.api.runtime.ActivityInstanceState", "7");

      EventActionType eventAction = EventMarshallingUtils.setEventAction(eventHandler,PredefinedConstants.SCHEDULE_ACTIVITY_ACTION, "Event Action", true);
      AttributeUtil.setAttribute(eventAction, PredefinedConstants.TARGET_STATE_ATT, "org.eclipse.stardust.engine.api.runtime.ActivityInstanceState", "5");

      if (defaultPerformer)
      {
         createDelegateAction(eventHandler);
      }
      
      eventHandler.setAutoBind(true);
   }

   private static void setUseConstant(EventHandlerType eventHandler, String delayValue, String delayUnit)
   {
      AttributeUtil.setBooleanAttribute(eventHandler, "carnot:engine:useData", false);
      AttributeUtil.setAttribute(eventHandler, "carnot:engine:data", null);
      AttributeUtil.setAttribute(eventHandler, PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, null);
      ModelElementEditingUtils.setPeriodAttribute(eventHandler, delayValue, delayUnit);
   }

   private static void setUseData(EventHandlerType eventHandler, String dataFullID,
         String dataPath)
   {
      AttributeUtil.setBooleanAttribute(eventHandler, "carnot:engine:useData", true);
      AttributeUtil.setAttribute(eventHandler, "carnot:engine:period", null);
      if (dataFullID != null)
      {
         String dataID = dataFullID;
         if (dataFullID.split(":").length > 1)
         {
            dataID = dataFullID.split(":")[1];
            AttributeUtil.setAttribute(eventHandler, "carnot:engine:data", dataID);
         }
      }
      else
      {
         AttributeUtil.setAttribute(eventHandler, "carnot:engine:data", null);
      }
      AttributeUtil.setAttribute(eventHandler,
            ModelerConstants.SD_SET_DATA_ACTION_DATA_PATH, dataPath);
   }

   private static void createDelegateAction(EventHandlerType eventHandler)
   {
      EventActionType delegateAction = EventMarshallingUtils.setEventAction(eventHandler,PredefinedConstants.DELEGATE_ACTIVITY_ACTION, "Delegate Action", true);
      AttributeUtil.setAttribute(delegateAction, PredefinedConstants.TARGET_WORKLIST_ATT, "org.eclipse.stardust.engine.core.extensions.actions.delegate.TargetWorklist", ModelerConstants.RS_DEFAULT_PERFORMER);
   }

   public static EventActionType createExcludeUserAction(ActivityType activity, JsonObject euJson, EObjectUUIDMapper uuidMapper)
   {
      String dataFullID = "";
      if (GsonUtils.hasNotJsonNull(euJson, ModelerConstants.EU_EXCLUDE_PERFORMER_DATA))
      {
         dataFullID = euJson.get(ModelerConstants.EU_EXCLUDE_PERFORMER_DATA).getAsString();
      }

      String actionName = null;

      if (GsonUtils.hasNotJsonNull(euJson, ModelerConstants.NAME_PROPERTY))
      {
         actionName = euJson.get(ModelerConstants.NAME_PROPERTY).getAsString();
      }

      String dataID = dataFullID;

      if (dataFullID.split(":").length > 1)
      {
         dataID = dataFullID.split(":")[1];
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
         eventHandler.setType(conditionType);
         eventHandler.setId(ModelerConstants.EU_EXCLUDE_USER_INTERNAL);
         eventHandler.setName(ModelerConstants.EU_EXCLUDE_USER_INTERNAL);
         activity.getEventHandler().add(eventHandler);
         uuidMapper.map(eventHandler);
      }

      EventActionType action = EventMarshallingUtils.addEventAction(eventHandler,PredefinedConstants.EXCLUDE_USER_ACTION, actionName, true);
      AttributeUtil.setAttribute(action, PredefinedConstants.EXCLUDED_PERFORMER_DATA, dataID);
      AttributeUtil.setAttribute(action, PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH, dataPath);
      uuidMapper.map(action);
      return action;
   }

   public static void addResubmissionToJson(EventHandlerType eventHandler,
         JsonObject rsJson)
   {
      ModelType model = ModelUtils.findContainingModel(eventHandler);

      // Check if everything is in place for Resubmission (additional actions are
      // ignored!)
      BindActionType bindAction = getFirstResubmissionBindAction(eventHandler);
      EventActionType eventAction = getFirstResubmissionEventAction(eventHandler);
      EventActionType delegateAction = getFirstResubmissionDelegateAction(eventHandler);

      if (null == bindAction || null == eventAction)
      {
         return;
      }

      // Data / Datapath
      String data = AttributeUtil.getAttributeValue(eventHandler, "carnot:engine:data");
      if (data != null)
      {
         rsJson.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY, model.getId() + ":"
               + data);
      }

      String dataPath = AttributeUtil.getAttributeValue(eventHandler,
            ModelerConstants.SD_SET_DATA_ACTION_DATA_PATH);
      if (data != null)
      {
         rsJson.addProperty(ModelerConstants.DATA_PATH_PROPERTY, dataPath);
      }

      // Period
      String period = AttributeUtil.getAttributeValue(eventHandler,
            "carnot:engine:period");
      if (period != null)
      {
         rsJson.addProperty(ModelerConstants.RS_DELAY_VALUE, ModelElementEditingUtils
               .getDelayUnit(period).split(":")[0]);
         rsJson.addProperty(ModelerConstants.RS_DELAY_UNIT, ModelElementEditingUtils
               .getDelayUnit(period).split(":")[1]);
      }

      boolean useData = AttributeUtil.getBooleanValue(eventHandler,
            "carnot:engine:useData");
      rsJson.addProperty(ModelerConstants.RS_USEDATA, useData);

      rsJson.addProperty(ModelerConstants.RS_DEFAULT_PERFORMER, delegateAction != null);

   }


   private static BindActionType getFirstResubmissionBindAction(
         EventHandlerType eventHandler)
   {
      for (Iterator<BindActionType> i = eventHandler.getBindAction().iterator(); i
            .hasNext();)
      {
         BindActionType bindAction = i.next();
         String targetState = AttributeUtil.getAttributeValue(bindAction,
               "carnot:engine:targetState");
         if (targetState != null && targetState.equalsIgnoreCase("7"))
         {
            EventActionTypeType actionType = bindAction.getType();
            if (actionType != null
                  && actionType.getId().equalsIgnoreCase(
                        PredefinedConstants.SCHEDULE_ACTIVITY_ACTION))
            {
               return bindAction;
            }
         }
      }
      return null;
   }

   private static EventActionType getFirstResubmissionEventAction(EventHandlerType eventHandler) {
      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType eventAction = i.next();
         String targetState = AttributeUtil.getAttributeValue(eventAction,
               "carnot:engine:targetState");
         if (targetState != null && targetState.equalsIgnoreCase("5"))
         {
            EventActionTypeType actionType = eventAction.getType();
            if (actionType != null
                  && actionType.getId().equalsIgnoreCase(
                        PredefinedConstants.SCHEDULE_ACTIVITY_ACTION))
            {
               return eventAction;
            }
         }
      }
      return null;
   }

   /*private static EventActionType getResubmissionEventHandler(ActivityType activity) {
      for (Iterator<EventHandlerType> i = activity.get.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType eventAction = i.next();
         String targetState = AttributeUtil.getAttributeValue(eventAction,
               "carnot:engine:targetState");
         if (targetState != null && targetState.equalsIgnoreCase("5"))
         {
            EventActionTypeType actionType = eventAction.getType();
            if (actionType != null
                  && actionType.getId().equalsIgnoreCase(
                        PredefinedConstants.SCHEDULE_ACTIVITY_ACTION))
            {
               return eventAction;
            }
         }
      }
      return null;
   }*/

   private static EventActionType getFirstResubmissionDelegateAction(EventHandlerType eventHandler) {
      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType delegateAction = i.next();
         EventActionTypeType actionType = delegateAction.getType();
         if (actionType != null
               && actionType.getId().equalsIgnoreCase(
                     PredefinedConstants.DELEGATE_ACTIVITY_ACTION))
         {
            return delegateAction;
         }
      }
      return null;
   }



   public static void addExcludeUserActions(EventHandlerType eventHandler,
         JsonObject eventJson, EObjectUUIDMapper uuidMapper)
   {
      JsonArray excludeUserActionsJson = new JsonArray();
      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType action = i.next();
         JsonObject euJson = new JsonObject();
         euJson.addProperty(ModelerConstants.NAME_PROPERTY, action.getName());
         euJson.addProperty(ModelerConstants.UUID_PROPERTY, uuidMapper.getUUID(action));
         euJson.addProperty(ModelerConstants.EU_EXCLUDE_PERFORMER_DATA, AttributeUtil
               .getAttribute(action, PredefinedConstants.EXCLUDED_PERFORMER_DATA)
               .getValue());
         euJson.addProperty(
               ModelerConstants.EU_EXCLUDE_PERFORMER_DATA_PATH,
               AttributeUtil.getAttribute(action,
                     PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH).getValue());
         excludeUserActionsJson.add(euJson);
      }
      eventJson.add("userExclusions", excludeUserActionsJson);
   }

   public static void createSetDataAction(EventHandlerType eventHandler, JsonObject eventJson)
   {
      JsonObject sdJson = eventJson.getAsJsonObject(ModelerConstants.SD_SET_DATA_ACTION);

      String dataFullID =  sdJson.get(ModelerConstants.SD_SET_DATA_ACTION_DATA_ID).getAsString();

      String dataID = dataFullID;

      if (dataFullID.split(":").length > 1)
      {
         dataID = dataFullID.split(":")[1];
      }

      String dataPath = GsonUtils.extractAsString(sdJson, ModelerConstants.SD_SET_DATA_ACTION_DATA_PATH);

      if (dataPath != null)
      {
         dataPath = sdJson.get(ModelerConstants.SD_SET_DATA_ACTION_DATA_PATH).getAsString();
      }

      EventActionType action = EventMarshallingUtils.setEventAction(eventHandler,PredefinedConstants.SET_DATA_ACTION, PredefinedConstants.SET_DATA_ACTION, true);
      AttributeUtil.setAttribute(action, PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, dataID);
      AttributeUtil.setAttribute(action, PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, dataPath);
      AttributeUtil.setAttribute(action, PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, "carnot:engine:exception");
      AttributeUtil.setAttribute(action, PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, "getMessage()");
      action.setId(ModelerConstants.SD_SET_DATA_ACTION_INTERNAL);
      action.setName(ModelerConstants.SD_SET_DATA_ACTION_INTERNAL);
   }

   public static void removeSetDataAction(EventHandlerType eventHandler)
   {
      EventActionType toBeRemoved = null;
      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType action = i.next();
         if (action.getId().equals(ModelerConstants.SD_SET_DATA_ACTION_INTERNAL))
         {
            toBeRemoved = action;
         }

      }
      if (toBeRemoved != null)
      {
         eventHandler.getEventAction().remove(toBeRemoved);
      }

   }

   public static void removeExcludeUserAction(EventActionType action)
   {
      if (action != null)
      {
         if (action.eContainer() instanceof EventHandlerType)
         {
            EventHandlerType handler = (EventHandlerType) action.eContainer();
            handler.getEventAction().remove(action);
         }
      }
   }

   public static EventHandlerType findExcludeUserEventHandler(ActivityType activity)
   {
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         if (eventHandler.getType().getId()
               .equals(PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION))
         {
            return eventHandler;
         }
      }
      return null;
   }

   public static EventHandlerType findResubmissionEventHandler(ActivityType activity)
   {
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         if (eventHandler.getId().equals(ModelerConstants.RS_RESUBMISSION))
         {
            return eventHandler;
         }
      }
      return null;
   }

   public static EventActionType findSetDataEventAction(EventHandlerType eventHandler)
   {
      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType eventAction = i.next();
         if (eventAction.getId().equals(ModelerConstants.SD_SET_DATA_ACTION_INTERNAL))
         {
            return eventAction;
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

   public static Object getJsonAttribute(JsonObject object, String name)
   {
      if (!object.has(ModelerConstants.ATTRIBUTES_PROPERTY))
      {
         return null;
      }

      JsonObject attributes = object.getAsJsonObject(ModelerConstants.ATTRIBUTES_PROPERTY);
      if (attributes != null)
      {
         for (Map.Entry<String, ? > entry : attributes.entrySet())
         {
            String key = entry.getKey();
            if(key.equals(name))
            {
               JsonElement jsonValue = attributes.get(key);
               if (jsonValue.isJsonNull())
               {
                  return null;
               }
               else if (jsonValue.getAsJsonPrimitive().isBoolean())
               {
                  return jsonValue.getAsBoolean();
               }
               else
               {
                  return jsonValue.getAsString();
               }
            }
         }
      }

      return null;
   }



   /*public static void updateResubmission(ActivityType activity,
         JsonObject resubmissionJson)
   {
      if (resubmissionJson.has("enabled"))
      {
         boolean enabled = resubmissionJson.get("enabled").getAsBoolean();
         if (enabled)
         {
            EventHandlerType handler = XPDLFinderUtils.findEventHandler(activity,
                  ModelerConstants.RS_RESUBMISSION);
            if (handler == null)
            {
               EventMarshallingUtils.createResubmissionEvent(activity, resubmissionJson);
            }
            else
            {
               EventMarshallingUtils.updateResubmission(handler, resubmissionJson);
            }
         }
      }
   }*/




}
