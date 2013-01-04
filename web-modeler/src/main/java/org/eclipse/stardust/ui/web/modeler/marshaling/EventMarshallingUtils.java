package org.eclipse.stardust.ui.web.modeler.marshaling;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.List;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.EventConditionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.IAttributeCategory;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IntermediateEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;

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

   public static String encodeEventHandlerType(EventConditionTypeType conditionType)
   {
      if (null == conditionType)
      {
         return null;
      }

      if (PredefinedConstants.TIMER_CONDITION.equals(conditionType.getId()))
      {
         return ModelerConstants.TIMER_EVENT_CLASS_KEY;
      }
      else if (PredefinedConstants.EXCEPTION_CONDITION.equals(conditionType.getId()))
      {
         return ModelerConstants.ERROR_EVENT_CLASS_KEY;
      }
      else
      {
         // TODO more event types, ideally per pluggable SPI
         return null;
      }
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

   public static Boolean encodeIsInterruptingEvent(EventConditionTypeType conditionType)
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

   public static EventConditionTypeType decodeEventHandlerType(String eventClass, ModelType model)
   {
      String conditionTypeId = null;
      if (ModelerConstants.TIMER_EVENT_CLASS_KEY.equals(eventClass))
      {
         conditionTypeId = PredefinedConstants.TIMER_CONDITION;
      }
      else if (ModelerConstants.ERROR_EVENT_CLASS_KEY.equals(eventClass))
      {
         conditionTypeId = PredefinedConstants.EXCEPTION_CONDITION;
      }
      // TODO more event types, ideally per pluggable SPI

      return !isEmpty(conditionTypeId) //
            ? ModelUtils.findIdentifiableElement(model.getEventConditionType(),
                  conditionTypeId) //
            : null;
   }

   private EventMarshallingUtils()
   {
      // utility class
   }
}
