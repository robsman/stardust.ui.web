package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.GatewaySymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.OrientationType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.impl.LaneSymbolImpl;
import org.eclipse.stardust.model.xpdl.carnot.impl.ProcessDefinitionTypeImpl;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public abstract class ModelElementMarshaller
{
   protected abstract EObjectUUIDMapper eObjectUUIDMapper();

   protected abstract ModelManagementStrategy modelManagementStrategy();

   private MBFacade facade;

   /**
    * 
    * @param modelElement
    * @return
    */
   public JsonObject toJson(EObject modelElement)
   {
      JsonObject jsResult = null;

      System.out.println("ModelElement to marshall: " + modelElement);

      // TODO generically dispatch REST generation from IModelElement

      if (modelElement instanceof ModelType)
      {
         jsResult = toModel((ModelType) modelElement);
      }
      else if (modelElement instanceof TypeDeclarationType)
      {
         jsResult = toTypeDeclarationType((TypeDeclarationType) modelElement);
      }
      else if (modelElement instanceof ProcessDefinitionTypeImpl)
      {
         jsResult = toProcessDefinitionJson((ProcessDefinitionType) modelElement);
      }
      else if (modelElement instanceof LaneSymbol)
      {
         jsResult = toLaneType((LaneSymbol) modelElement);
      }
      else if (modelElement instanceof ActivityType)
      {
         jsResult = toActivityJson((ActivityType) modelElement);
      }
      else if (modelElement instanceof TriggerType)
      {
         if ( !((TriggerType) modelElement).getSymbols().isEmpty()
               && (((TriggerType) modelElement).getSymbols().get(0) instanceof StartEventSymbol))
         {
            jsResult = toStartEventJson((StartEventSymbol) ((TriggerType) modelElement).getSymbols()
                  .get(0));
         }
      }
      else if (modelElement instanceof ActivitySymbolType)
      {
         jsResult = toActivitySymbolJson((ActivitySymbolType) modelElement);
      }
      else if (modelElement instanceof StartEventSymbol)
      {
         jsResult = toStartEventJson((StartEventSymbol) modelElement);
      }
      else if (modelElement instanceof EndEventSymbol)
      {
         jsResult = toEndEventJson((EndEventSymbol) modelElement);
      }
      else if (modelElement instanceof ApplicationType)
      {
         jsResult = toApplication((ApplicationType) modelElement);
      }
      else if (modelElement instanceof TransitionConnectionType)
      {
         jsResult = toTransitionConnectionJson((TransitionConnectionType) modelElement);
      }
      else if (modelElement instanceof TransitionType)
      {
         jsResult = toTransitionJson((TransitionType) modelElement);
      }
      else if (modelElement instanceof DataMappingConnectionType)
      {
         jsResult = toDataMappingConnectionType((DataMappingConnectionType) modelElement);
      }
      else if (modelElement instanceof DataType)
      {
         jsResult = toDataJson((DataType) modelElement);
      }
      else if (modelElement instanceof DataSymbolType)
      {
         jsResult = toDataSymbolJson((DataSymbolType) modelElement);
      }
      else if (modelElement instanceof RoleType)
      {
         jsResult = toRoleJson((RoleType) modelElement);
      }
      else if (modelElement instanceof OrganizationType)
      {
         jsResult = toOrganizationJson((OrganizationType) modelElement);
      }

      if (null == jsResult)
      {
         jsResult = new JsonObject();
         if (modelElement instanceof IModelElement)
         {
            jsResult.addProperty(ModelerConstants.OID_PROPERTY,
                  ((IModelElement) modelElement).getElementOid());
         }
         jsResult.addProperty(ModelerConstants.TYPE_PROPERTY, modelElement.getClass()
               .getName());
         jsResult.addProperty("moreContent", "TODO");
      }

      return jsResult;
   }

   /**
    * @return
    */
   public JsonObject toProcessDefinitionJson(ProcessDefinitionType processDefinition)
   {
      JsonObject processJson = new JsonObject();

      processJson.addProperty(ModelerConstants.OID_PROPERTY,
            processDefinition.getElementOid());
      processJson.addProperty(ModelerConstants.ID_PROPERTY, processDefinition.getId());
      processJson.addProperty(ModelerConstants.NAME_PROPERTY, processDefinition.getName());
      processJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(processDefinition));
      processJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.PROCESS_KEY);
      processJson.addProperty(ModelerConstants.DEFAULT_PRIORITY_PROPERTY,
            processDefinition.getDefaultPriority());

      setContainingModelIdProperty(processJson, processDefinition);

      loadDescription(processJson, processDefinition);

      JsonObject attributesJson = new JsonObject();
      processJson.add(ModelerConstants.ATTRIBUTES_PROPERTY, attributesJson);

      JsonArray dataPathesJson = new JsonArray();
      processJson.add(ModelerConstants.DATA_PATHES_PROPERTY, dataPathesJson);

      for (DataPathType dataPath : processDefinition.getDataPath())
      {
         JsonObject dataPathJson = new JsonObject();
         dataPathesJson.add(dataPathJson);

         dataPathJson.addProperty(ModelerConstants.ID_PROPERTY, dataPath.getId());
         dataPathJson.addProperty(ModelerConstants.NAME_PROPERTY, dataPath.getName());
         dataPathJson.addProperty(
               ModelerConstants.DATA_FULL_ID_PROPERTY,
               facade().createFullId(ModelUtils.findContainingModel(dataPath),
                     dataPath.getData()));
         dataPathJson.addProperty(ModelerConstants.DATA_PATH_PROPERTY,
               dataPath.getDataPath());
         dataPathJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
               dataPath.getDirection().getLiteral());
         dataPathJson.addProperty(ModelerConstants.DESCRIPTOR_PROPERTY,
               dataPath.isDescriptor());
         dataPathJson.addProperty(ModelerConstants.KEY_DESCRIPTOR_PROPERTY,
               dataPath.isKey());
      }

      JsonObject activitiesJson = new JsonObject();
      processJson.add(ModelerConstants.ACTIVITIES_PROPERTY, activitiesJson);

      for (ActivityType activity : processDefinition.getActivity())
      {
         JsonObject activityJson = new JsonObject();
         activitiesJson.add(activity.getId(), activityJson);

         activityJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());
         activityJson.addProperty(ModelerConstants.NAME_PROPERTY, activity.getName());
         loadDescription(activityJson, activity);
      }

      JsonObject gatewaysJson = new JsonObject();
      processJson.add(ModelerConstants.GATEWAYS_PROPERTY, gatewaysJson);

      JsonObject eventsJson = new JsonObject();
      processJson.add(ModelerConstants.EVENTS_PROPERTY, eventsJson);

      JsonObject controlFlowsJson = new JsonObject();
      processJson.add(ModelerConstants.CONTROL_FLOWS_PROPERTY, controlFlowsJson);

      JsonObject dataFlowsJson = new JsonObject();
      processJson.add(ModelerConstants.DATA_FLOWS_PROPERTY, dataFlowsJson);

      return processJson;
   }

   /**
    * 
    * @param laneSymbol
    * @return
    */
   public JsonObject toLaneType(LaneSymbol laneSymbol)
   {
      JsonObject laneSymbolJson = new JsonObject();

      laneSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            laneSymbol.getElementOid());
      laneSymbolJson.addProperty(ModelerConstants.ID_PROPERTY, laneSymbol.getId());
      laneSymbolJson.addProperty(ModelerConstants.NAME_PROPERTY, laneSymbol.getName());
      laneSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, laneSymbol.getWidth());
      laneSymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY, laneSymbol.getHeight());
      laneSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.SWIMLANE_SYMBOL);
      laneSymbolJson.addProperty(
            ModelerConstants.PARTICIPANT_FULL_ID,
            facade().createFullId(
                  ModelUtils.findContainingModel(laneSymbol.getParticipant()),
                  laneSymbol.getParticipant()));

      return laneSymbolJson;
   }

   /**
    * @return
    */
   public JsonObject toProcessDefinitionDiagram(ProcessDefinitionType processDefinition)
   {
      JsonObject diagramJson = new JsonObject();

      // Pools and Lanes

      JsonObject poolSymbolsJson = new JsonObject();
      diagramJson.add(ModelerConstants.POOL_SYMBOLS, poolSymbolsJson);

      for (PoolSymbol poolSymbol : processDefinition.getDiagram().get(0).getPoolSymbols())
      {
         JsonObject poolSymbolJson = new JsonObject();
         poolSymbolsJson.add(poolSymbol.getId(), poolSymbolJson);

         poolSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
               poolSymbol.getElementOid());
         poolSymbolJson.addProperty(ModelerConstants.ID_PROPERTY, poolSymbol.getId());
         poolSymbolJson.addProperty(ModelerConstants.NAME_PROPERTY, poolSymbol.getName());
         poolSymbolJson.addProperty(ModelerConstants.X_PROPERTY, poolSymbol.getXPos());
         poolSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, poolSymbol.getYPos());
         poolSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY,
               poolSymbol.getWidth());
         poolSymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY,
               poolSymbol.getHeight());

         if (poolSymbol.getOrientation().equals(OrientationType.HORIZONTAL_LITERAL))
         {
            poolSymbolJson.addProperty(ModelerConstants.ORIENTATION_PROPERTY,
                  ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL);
         }
         else
         {
            poolSymbolJson.addProperty(ModelerConstants.ORIENTATION_PROPERTY,
                  ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL);
         }

         JsonArray laneSymbols = new JsonArray();
         poolSymbolJson.add(ModelerConstants.LANE_SYMBOLS, laneSymbols);

         for (LaneSymbol laneSymbol : poolSymbol.getChildLanes())
         {
            JsonObject laneSymbolJson = new JsonObject();
            laneSymbols.add(laneSymbolJson);

            laneSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
                  laneSymbol.getElementOid());
            laneSymbolJson.addProperty(ModelerConstants.ID_PROPERTY, laneSymbol.getId());
            laneSymbolJson.addProperty(ModelerConstants.NAME_PROPERTY,
                  laneSymbol.getName());
            laneSymbolJson.addProperty(ModelerConstants.X_PROPERTY, laneSymbol.getXPos());
            laneSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, laneSymbol.getYPos());
            laneSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY,
                  laneSymbol.getWidth());
            laneSymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY,
                  laneSymbol.getHeight());

            if (laneSymbol.getParticipant() != null)
            {
               // TODO Scope handling

               laneSymbolJson.addProperty(
                     ModelerConstants.PARTICIPANT_FULL_ID,
                     facade().createFullId(
                           ModelUtils.findContainingModel(processDefinition),
                           laneSymbol.getParticipant()));
            }

            JsonObject activitySymbolsJson = new JsonObject();
            JsonObject gatewaySymbolsJson = new JsonObject();

            laneSymbolJson.add(ModelerConstants.ACTIVITY_SYMBOLS, activitySymbolsJson);
            laneSymbolJson.add(ModelerConstants.GATEWAY_SYMBOLS, gatewaySymbolsJson);

            for (ActivitySymbolType activitySymbol : laneSymbol.getActivitySymbol())
            {
               JsonObject activitySymbolJson = toActivitySymbolJson(activitySymbol);

               // TODO Hack to identify gateways
               if (activitySymbol.getActivity()
                     .getId()
                     .toLowerCase()
                     .startsWith("gateway"))
               {
                  gatewaySymbolsJson.add(activitySymbol.getActivity().getId(),
                        activitySymbolJson);
               }
               else
               {
                  activitySymbolsJson.add(activitySymbol.getActivity().getId(),
                        activitySymbolJson);
               }
            }

            JsonObject eventSymbols = new JsonObject();

            laneSymbolJson.add(ModelerConstants.EVENT_SYMBOLS, eventSymbols);

            // Start Events

            for (StartEventSymbol startEventSymbol : laneSymbol.getStartEventSymbols())
            {
               JsonObject startEventJson = toStartEventJson(startEventSymbol);
               eventSymbols.add(String.valueOf(startEventSymbol.getElementOid()),
                     startEventJson);
            }

            // End Events

            for (EndEventSymbol endEventSymbol : laneSymbol.getEndEventSymbols())
            {
               JsonObject eventSymbolJson = toEndEventJson(endEventSymbol);
               eventSymbols.add(String.valueOf(endEventSymbol.getElementOid()),
                     eventSymbolJson);
            }

            // Data

            JsonObject dataSymbolsJson = new JsonObject();

            laneSymbolJson.add(ModelerConstants.DATA_SYMBOLS, dataSymbolsJson);

            for (DataSymbolType dataSymbol : laneSymbol.getDataSymbol())
            {
               // Multiple Data Symbols can have same ID
               dataSymbolsJson.add(String.valueOf(dataSymbol.getElementOid()),
                     toDataSymbolJson(dataSymbol));
            }
         }

         JsonObject connectionsJson = new JsonObject();
         diagramJson.add(ModelerConstants.CONNECTIONS_PROPERTY, connectionsJson);

         // Data Mappings

         for (DataMappingConnectionType dataMappingConnection : poolSymbol.getDataMappingConnection())
         {
            JsonObject connectionJson = toDataMappingConnectionType(dataMappingConnection);

            connectionsJson.add(
                  extractString(
                        connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                        ModelerConstants.ID_PROPERTY), connectionJson);
         }

         // Transitions

         for (TransitionConnectionType transitionConnection : poolSymbol.getTransitionConnection())
         {
            JsonObject connectionJson = toTransitionConnectionJson(transitionConnection);
            connectionsJson.add(
                  extractString(
                        connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                        ModelerConstants.ID_PROPERTY), connectionJson);
         }
      }

      return diagramJson;
   }

   /**
    * 
    * @param activity
    * @return
    */
   public JsonObject toActivityJson(ActivityType activity)
   {
      JsonObject activityJson = new JsonObject();

      activityJson.addProperty(ModelerConstants.OID_PROPERTY, activity.getElementOid());
      activityJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());
      activityJson.addProperty(ModelerConstants.NAME_PROPERTY, activity.getName());

      loadDescription(activityJson, activity);
      loadAttributes(activity, activityJson);

      // TODO Hack to identify gateways

      if (activity.getId().toLowerCase().startsWith("gateway"))
      {
         activityJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.ACTIVITY_KEY);
         activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE,
               ModelerConstants.GATEWAY_ACTIVITY);

         // TODO Throw error for inconsistent Split/Join settings

         if (activity.getJoin().equals(JoinSplitType.XOR_LITERAL))
         {
            activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                  ModelerConstants.XOR_GATEWAY_TYPE);
         }
         else if (activity.getJoin().equals(JoinSplitType.AND_LITERAL))
         {
            activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                  ModelerConstants.AND_GATEWAY_TYPE);
         }
         else if (activity.getSplit().equals(JoinSplitType.XOR_LITERAL))
         {
            activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                  ModelerConstants.XOR_GATEWAY_TYPE);
         }
         else if (activity.getSplit().equals(JoinSplitType.AND_LITERAL))
         {
            activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                  ModelerConstants.AND_GATEWAY_TYPE);
         }
      }
      else
      {
         activityJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.ACTIVITY_KEY);
         activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE,
               activity.getImplementation().getLiteral());

         if (activity.getImplementationProcess() != null)
         {
            activityJson.addProperty(
                  ModelerConstants.SUBPROCESS_ID,
                  facade().createFullId(ModelUtils.findContainingModel(activity),
                        activity.getImplementationProcess()));
         }
         else if (activity.getApplication() != null)
         {
            activityJson.addProperty(
                  ModelerConstants.APPLICATION_FULL_ID_PROPERTY,
                  facade().createFullId(ModelUtils.findContainingModel(activity),
                        activity.getApplication()));
         }
         
         // TODO Access points need to be obtained from all
         // contexts?

         JsonObject accessPointsJson = new JsonObject();
         
         activityJson.add(ModelerConstants.ACCESS_POINTS_PROPERTY, accessPointsJson);
         
         for (AccessPointType accessPoint : ActivityUtil.getAccessPoints(
               activity, true, getDefaultDataMappingContext(activity)))
         {
            JsonObject accessPointJson = new JsonObject();

            accessPointsJson.add(accessPoint.getId(), accessPointJson);
            accessPointJson.addProperty(ModelerConstants.ID_PROPERTY, accessPoint.getId());
            accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
                  accessPoint.getName());
            accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                  accessPoint.getDirection().getLiteral());

            loadDescription(accessPointJson, accessPoint);
         }

         /*
          * if (null != activity.getPerformer()) { act.getProps().setPerformerid(
          * activity.getPerformer().getId()); }
          */

      }

      return activityJson;
   }

   /**
    * 
    * @param activitySymbol
    * @return
    */
   public JsonObject toActivitySymbolJson(ActivitySymbolType activitySymbol)
   {
      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (activitySymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) activitySymbol.eContainer()
            : null;
      while (null != container)
      {
         laneOffsetX += container.getXPos();
         laneOffsetY += container.getYPos();

         // recurse
         container = (container.eContainer() instanceof ISwimlaneSymbol)
               ? (ISwimlaneSymbol) container.eContainer()
               : null;
      }

      JsonObject activitySymbolJson = new JsonObject();

      activitySymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            activitySymbol.getElementOid());
      activitySymbolJson.addProperty(ModelerConstants.X_PROPERTY,
            activitySymbol.getXPos() + laneOffsetX + ModelerConstants.POOL_LANE_MARGIN);
      activitySymbolJson.addProperty(ModelerConstants.Y_PROPERTY,
            activitySymbol.getYPos() + laneOffsetY + ModelerConstants.POOL_LANE_MARGIN
                  + ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT);
      activitySymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY,
            activitySymbol.getWidth());
      activitySymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY,
            activitySymbol.getHeight());

      ActivityType activity = activitySymbol.getActivity();
      
      activitySymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, toActivityJson(activity));

      // TODO Hack to identify gateways

      if (activity.getId().toLowerCase().startsWith("gateway"))
      {
         activitySymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.GATEWAY_SYMBOL);

//         // TODO REVIEW There is no gateway symbol needed!!!
//
//         // TODO Refactor
//         // Identify the gateway symbol for this activity and update the
//         // location and dimension attributes.
//         GatewaySymbol thisGatewaySymbol = null;
//         // for (GatewaySymbol gs : laneSymbol.getGatewaySymbol()) {
//         // if (gs.getActivitySymbol().getActivity().equals(activity)) {
//         // thisGatewaySymbol = gs;
//         // break;
//         // }
//         // }
//
//         if (null != thisGatewaySymbol)
//         {
//            activitySymbolJson.remove(ModelerConstants.X_PROPERTY);
//            activitySymbolJson.addProperty(ModelerConstants.X_PROPERTY,
//                  thisGatewaySymbol.getXPos() + laneOffsetX
//                        + ModelerConstants.POOL_LANE_MARGIN);
//            activitySymbolJson.remove(ModelerConstants.Y_PROPERTY);
//            activitySymbolJson.addProperty(ModelerConstants.Y_PROPERTY,
//                  thisGatewaySymbol.getYPos() + laneOffsetY
//                        + ModelerConstants.POOL_LANE_MARGIN
//                        + ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT);
//            activitySymbolJson.remove(ModelerConstants.WIDTH_PROPERTY);
//            activitySymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY,
//                  thisGatewaySymbol.getWidth());
//            activitySymbolJson.remove(ModelerConstants.HEIGHT_PROPERTY);
//            activitySymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY,
//                  thisGatewaySymbol.getHeight());
//         }
      }
      else
      {
         activitySymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.ACTIVITY_SYMBOL);
      }

      return activitySymbolJson;
   }

   /**
    * 
    * @param startEventSymbol
    * @return
    */
   public JsonObject toStartEventJson(StartEventSymbol startEventSymbol)
   {
      JsonObject eventSymbolJson = new JsonObject();

      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (startEventSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) startEventSymbol.eContainer()
            : null;
      while (null != container)
      {
         laneOffsetX += container.getXPos();
         laneOffsetY += container.getYPos();

         // recurse
         container = (container.eContainer() instanceof ISwimlaneSymbol)
               ? (ISwimlaneSymbol) container.eContainer()
               : null;
      }

      eventSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            startEventSymbol.getElementOid());

      // TODO check this math
      eventSymbolJson.addProperty(ModelerConstants.X_PROPERTY,
            startEventSymbol.getXPos() + laneOffsetX + ModelerConstants.POOL_LANE_MARGIN
                  + (startEventSymbol.getWidth() / 2)
                  - ModelerConstants.START_END_SYMBOL_LEFT_OFFSET);
      eventSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, startEventSymbol.getYPos()
            + laneOffsetY + ModelerConstants.POOL_LANE_MARGIN
            + ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT);

      JsonObject eventJson = new JsonObject();
      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

      eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);
      eventJson.addProperty(ModelerConstants.EVENT_TYPE_PROPERTY,
            ModelerConstants.START_EVENT);
      eventSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.EVENT_SYMBOL);
      // eventJson.put(ID_PROPERTY,
      // String.valueOf(startEventSymbol.getModelElement().getId()));
      // loadDescription(eventJson,
      // startEventSymbol.getModelElement());
      // loadAttributes(startEventSymbol.getModelElement(),
      // eventJson);

      return eventSymbolJson;
   }

   /**
    * 
    * @param startEventSymbol
    * @return
    */
   public JsonObject toEndEventJson(EndEventSymbol endEventSymbol)
   {
      JsonObject eventSymbolJson = new JsonObject();

      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (endEventSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) endEventSymbol.eContainer()
            : null;
      while (null != container)
      {
         laneOffsetX += container.getXPos();
         laneOffsetY += container.getYPos();

         // recurse
         container = (container.eContainer() instanceof ISwimlaneSymbol)
               ? (ISwimlaneSymbol) container.eContainer()
               : null;
      }

      eventSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            endEventSymbol.getElementOid());
      eventSymbolJson.addProperty(ModelerConstants.X_PROPERTY, endEventSymbol.getXPos()
            + laneOffsetX + ModelerConstants.POOL_LANE_MARGIN
            + ModelerConstants.START_END_SYMBOL_LEFT_OFFSET);
      eventSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, endEventSymbol.getYPos()
            + laneOffsetY + ModelerConstants.POOL_LANE_MARGIN
            + ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT);

      JsonObject eventJson = new JsonObject();
      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

      eventSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.EVENT_SYMBOL);

      eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);
      eventJson.addProperty(ModelerConstants.EVENT_TYPE_PROPERTY,
            ModelerConstants.STOP_EVENT);
      // eventJson.put(ID_PROPERTY,
      // String.valueOf(endEventSymbol.getModelElement().getId()));
      // loadDescription(eventJson,
      // endEventSymbol.getModelElement());
      // loadAttributes(endEventSymbol.getModelElement(),
      // eventJson);

      return eventSymbolJson;

   }

   /**
    * 
    * @param data
    * @return
    */
   public JsonObject toDataJson(DataType data)
   {
      JsonObject dataJson = new JsonObject();

      if (null != data)
      {
         dataJson.addProperty(ModelerConstants.TYPE_PROPERTY, "data");
         dataJson.addProperty(ModelerConstants.ID_PROPERTY, data.getId());
         dataJson.addProperty(ModelerConstants.NAME_PROPERTY, data.getName());
         dataJson.addProperty(ModelerConstants.UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(data));
         ModelType model = ModelUtils.findContainingModel(data);
         dataJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         setContainingModelIdProperty(dataJson, data);

         loadDescription(dataJson, data);
         loadAttributes(data, dataJson);

         System.out.println("===> Data " + data);
         System.out.println("===> Data " + data.getType());

         if (data.getType().getId().equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.STRUCTURED_DATA_TYPE_KEY);
            // dataJson.addProperty(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID, @full
            // id@);
         }
         else if (data.getType().getId().equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.DOCUMENT_DATA_TYPE_KEY);
            // dataJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
            // @integer
            // etc.@);
         }
         else if (data.getType().getId().equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
            // dataJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
            // @integer
            // etc.@);
         }
      }

      return dataJson;
   }

   /**
    * 
    * @param startEventSymbol
    * @return
    */
   public JsonObject toDataSymbolJson(DataSymbolType dataSymbol)
   {
      JsonObject dataSymbolJson = new JsonObject();

      dataSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            dataSymbol.getElementOid());
      dataSymbolJson.addProperty(ModelerConstants.X_PROPERTY, dataSymbol.getXPos());
      dataSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, dataSymbol.getYPos());
      dataSymbolJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(dataSymbol));
      dataSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.DATA_SYMBOL);

      // TODO REVIEW This is not correct, data are loaded separately
      // dataSymbolJson.add(ModelerConstants.DATA, toDataTypeJson(dataSymbol.getData()));

      // Model returned will be null in case of data delete operation

      ModelType containingModel = ModelUtils.findContainingModel(dataSymbol.getData());
      if (null != containingModel)
      {
         dataSymbolJson.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY,
               facade().createFullId(containingModel, dataSymbol.getData()));
      }

      return dataSymbolJson;
   }

   /**
    * @param role
    * @return
    */
   public JsonObject toRoleJson(RoleType role)
   {
      JsonObject roleJson = new JsonObject();
      roleJson.addProperty(ModelerConstants.ID_PROPERTY, role.getId());
      roleJson.addProperty(ModelerConstants.NAME_PROPERTY, role.getName());
      roleJson.addProperty(ModelerConstants.OID_PROPERTY, role.getElementOid());
      roleJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY);
      roleJson.addProperty(ModelerConstants.TEAM_LEADER_KEY, "false");
      roleJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(role));
      ModelType model = ModelUtils.findContainingModel(role);

      if (null != model)
      {
         List<OrganizationType> parentOrgs = facade().getParentOrganizations(model, role);
         if (parentOrgs.size() > 0)
         {
            // TODO - add array of orgs
            OrganizationType org = parentOrgs.get(0);
            roleJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper().getUUID(org));
            if (null != org.getTeamLead() && org.getTeamLead().equals(role))
            {
               roleJson.addProperty(ModelerConstants.TEAM_LEADER_KEY, "true");
            }
         }
         roleJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         roleJson.addProperty(ModelerConstants.MODEL_ID_PROPERTY, model.getId());
      }

      loadDescription(roleJson, role);
      loadAttributes(role, roleJson);

      return roleJson;
   }

   /**
    * @param org
    * @return
    */
   public JsonObject toOrganizationJson(OrganizationType org)
   {
      JsonObject orgJson = new JsonObject();
      orgJson.addProperty(ModelerConstants.ID_PROPERTY, org.getId());
      orgJson.addProperty(ModelerConstants.NAME_PROPERTY, org.getName());
      orgJson.addProperty(ModelerConstants.OID_PROPERTY, org.getElementOid());
      orgJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY);
      orgJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(org));
      ModelType model = ModelUtils.findContainingModel(org);

      if (null != model)
      {
         List<OrganizationType> parentOrgs = facade().getParentOrganizations(model, org);
         if (parentOrgs.size() > 0)
         {
            orgJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper().getUUID(parentOrgs.get(0)));
         }
         orgJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         orgJson.addProperty(ModelerConstants.MODEL_ID_PROPERTY, model.getId());
      }

      loadDescription(orgJson, org);
      loadAttributes(org, orgJson);

      return orgJson;
   }

   /**
    * @return
    */
   public JsonObject toApplication(ApplicationType application)
   {
      JsonObject applicationJson = new JsonObject();

      applicationJson.addProperty(ModelerConstants.OID_PROPERTY,
            application.getElementOid());
      applicationJson.addProperty(ModelerConstants.ID_PROPERTY, application.getId());
      applicationJson.addProperty(ModelerConstants.NAME_PROPERTY, application.getName());
      setContainingModelIdProperty(applicationJson, application);
      applicationJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(application));
      applicationJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.APPLICATION_KEY);

      loadDescription(applicationJson, application);
      loadAttributes(application, applicationJson);

      if (application.getType() != null)
      {
         applicationJson.addProperty(ModelerConstants.APPLICATION_TYPE_PROPERTY,
               application.getType().getId());
      }
      else
      {
         // TODO - Check if needed
         // A temporary work around to identify application type, till
         // MBFacade#findApplicationTypeType is extended to
         // return application type for MessageTransformation, ExternalWebApp etc type of
         // applications.
         String applicationType = AttributeUtil.getAttributeValue(application,
               ModelerConstants.APPLICATION_TYPE_PROPERTY);
         if (StringUtils.isNotEmpty(applicationType))
         {
            applicationJson.addProperty(ModelerConstants.APPLICATION_TYPE_PROPERTY,
                  applicationType);
         }
         else
         {
            applicationJson.addProperty(ModelerConstants.APPLICATION_TYPE_PROPERTY,
                  ModelerConstants.INTERACTIVE_APPLICATION_TYPE_KEY);
         }

         JsonObject contextsJson = new JsonObject();
         applicationJson.add(ModelerConstants.CONTEXTS_PROPERTY, contextsJson);

         for (ContextType context : application.getContext())
         {
            JsonObject contextJson = new JsonObject();
            applicationJson.add(context.getType().getId(), contextJson);
         }
      }

      JsonObject accessPointsJson = new JsonObject();

      applicationJson.add(ModelerConstants.ACCESS_POINTS_PROPERTY, accessPointsJson);

      for (AccessPointType accessPoint : application.getAccessPoint())
      {
         JsonObject accessPointJson = new JsonObject();

         accessPointsJson.add(accessPoint.getId(), accessPointJson);
         accessPointJson.addProperty(ModelerConstants.ID_PROPERTY, accessPoint.getId());
         accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
               accessPoint.getName());
         accessPointJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               accessPoint.getType().getName());
         accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
               accessPoint.getDirection().getLiteral());

         loadAttributes(accessPoint, accessPointJson);
      }

      return applicationJson;
   }

   /**
    * 
    * @param dataMappingConnection
    * @return
    */
   public JsonObject toDataMappingConnectionType(
         DataMappingConnectionType dataMappingConnection)
   {
      JsonObject connectionJson = new JsonObject();

      connectionJson.addProperty(ModelerConstants.OID_PROPERTY,
            dataMappingConnection.getElementOid());

      connectionJson.addProperty(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(dataMappingConnection.getSourceAnchor()));
      connectionJson.addProperty(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(dataMappingConnection.getTargetAnchor()));

      JsonObject dataFlowJson = new JsonObject();
      connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, dataFlowJson);

      DataType data = dataMappingConnection.getDataSymbol().getData();
      if (null != data)
      {
         dataFlowJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.DATA_FLOW_LITERAL);
         dataFlowJson.addProperty(ModelerConstants.ID_PROPERTY, ""
               + dataMappingConnection.getElementOid());
         ActivityType activity = dataMappingConnection.getActivitySymbol().getActivity();
         for (DataMappingType dataMapping : activity.getDataMapping())
         {
            // Update the dataFlowJson for currentData symbol
            if (dataMapping.getData().getId() == data.getId())
            {
               if (dataMapping.getDirection() == DirectionType.IN_LITERAL)
               {
                  dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY,
                        true);
                  dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY,
                        false);
                  connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
                        dataMappingConnection.getDataSymbol().getElementOid());
                  connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                        ModelerConstants.DATA);
                  connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
                        dataMappingConnection.getActivitySymbol().getElementOid());
                  connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                        ModelerConstants.ACTIVITY_KEY);

               }
               else if (dataMapping.getDirection() == DirectionType.OUT_LITERAL)
               {
                  dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY,
                        false);
                  dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY,
                        true);
                  // TODO - Currently API always assumes connectionJson.getSourceNode will
                  // be data, to set Activity in sourceNode for OUT Mapping for data this
                  // code is placed here
                  connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
                        dataMappingConnection.getActivitySymbol().getElementOid());
                  connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                        ModelerConstants.ACTIVITY_KEY);
                  connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
                        dataMappingConnection.getDataSymbol().getElementOid());
                  connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                        ModelerConstants.DATA);
               }
               else
               {
                  dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY,
                        true);
                  dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY,
                        true);
               }
            }
         }

         dataFlowJson.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY,
               facade().createFullId(ModelUtils.findContainingModel(data), data));
         dataFlowJson.addProperty(ModelerConstants.ACTIVITY_ID_PROPERTY, activity.getId());
      }

      return connectionJson;
   }

   /**
    * 
    * @param transitionConnection
    * @return
    */
   public JsonObject toTransitionConnectionJson(
         TransitionConnectionType transitionConnection)
   {
      JsonObject connectionJson = new JsonObject();
      JsonObject modelElementJson = null;

      // Common settings

      connectionJson.addProperty(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(transitionConnection.getSourceAnchor()));
      connectionJson.addProperty(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(transitionConnection.getTargetAnchor()));

      if (transitionConnection.getTransition() != null)
      {
         connectionJson.addProperty(ModelerConstants.OID_PROPERTY,
               transitionConnection.getElementOid());

         TransitionType transition = transitionConnection.getTransition();

         modelElementJson = toTransitionJson(transition);

         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);

         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(ModelerConstants.ID_PROPERTY, transition.getId());
         modelElementJson.addProperty(ModelerConstants.OID_PROPERTY,
               transition.getElementOid());

         modelElementJson.addProperty(ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY, transition.isForkOnTraversal());

         if (transition.getCondition().equals("CONDITION"))
         {
            modelElementJson.addProperty(ModelerConstants.CONDITION_EXPRESSION_PROPERTY,
                  (String) transition.getExpression().getMixed().getValue(0));
            modelElementJson.addProperty(ModelerConstants.OTHERWISE_PROPERTY, false);
         }
         else
         {
            modelElementJson.addProperty(ModelerConstants.OTHERWISE_PROPERTY, true);
         }

         loadDescription(modelElementJson, transition);
         loadAttributes(transition, modelElementJson);

         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
               transition.getFrom().getActivitySymbols().get(0).getElementOid());

         // TODO Hack to identify gateways

         if (transition.getFrom().getId().toLowerCase().startsWith("gateway"))
         {
            connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                  ModelerConstants.GATEWAY);
         }
         else
         {
            connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                  ModelerConstants.ACTIVITY_KEY);
         }

         connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
               transition.getTo().getActivitySymbols().get(0).getElementOid());

         if (transition.getTo().getId().toLowerCase().startsWith("gateway"))
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.GATEWAY);
            connectionJson.remove(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY);
            connectionJson.addProperty(
                  ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
                  ModelerConstants.NORTH_KEY);
         }
         else
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.ACTIVITY_KEY);
         }

      }
      else if (transitionConnection.getSourceNode() instanceof StartEventSymbol)
      {
         modelElementJson = new JsonObject();

         connectionJson.addProperty(ModelerConstants.OID_PROPERTY,
               transitionConnection.getElementOid());
         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);

         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(
               ModelerConstants.ID_PROPERTY,
               transitionConnection.getSourceNode().getElementOid()
                     + "-"
                     + ((ActivitySymbolType) transitionConnection.getTargetActivitySymbol()).getActivity()
                           .getId());

         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
               transitionConnection.getSourceNode().getElementOid());
         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
               ModelerConstants.EVENT_KEY);
         connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
               transitionConnection.getTargetActivitySymbol().getElementOid());
         connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
               ModelerConstants.ACTIVITY_KEY);
      }
      else if (transitionConnection.getTargetNode() instanceof EndEventSymbol)
      {
         modelElementJson = new JsonObject();

         connectionJson.addProperty(ModelerConstants.OID_PROPERTY,
               transitionConnection.getElementOid());
         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);
         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(
               ModelerConstants.ID_PROPERTY,
               ((ActivitySymbolType) transitionConnection.getSourceActivitySymbol()).getActivity()
                     .getId()
                     + "-"
                     + String.valueOf(transitionConnection.getTargetNode()
                           .getElementOid()));
         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
               transitionConnection.getSourceActivitySymbol().getElementOid());
         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
               ModelerConstants.ACTIVITY_KEY);
         connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
               String.valueOf(transitionConnection.getTargetNode().getElementOid()));
         connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
               ModelerConstants.EVENT_KEY);

         // For end event symbol the anchorpoint orientation is set to "bottom", in
         // the eclipse modeler.
         // This causes wrong routing of the the connector.
         // Hence overriding the property with "center" / or "undefined"
         connectionJson.remove(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY);
         connectionJson.addProperty(
               ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
               ModelerConstants.UNDEFINED_ORIENTATION_KEY);
      }
      return connectionJson;
   }

   /**
    * 
    * @param transitionConnection
    * @return
    */
   public JsonObject toTransitionJson(TransitionType transition)
   {
      JsonObject controlFlowJson = new JsonObject();

      controlFlowJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.CONTROL_FLOW_LITERAL);
      controlFlowJson.addProperty(ModelerConstants.ID_PROPERTY, transition.getId());
      controlFlowJson.addProperty(ModelerConstants.OID_PROPERTY,
            transition.getElementOid());

      if (transition.getCondition().equals("CONDITION"))
      {
         controlFlowJson.addProperty(ModelerConstants.CONDITION_EXPRESSION_PROPERTY,
               (String) transition.getExpression().getMixed().getValue(0));
         controlFlowJson.addProperty(ModelerConstants.OTHERWISE_PROPERTY, false);
      }
      else
      {
         controlFlowJson.addProperty(ModelerConstants.OTHERWISE_PROPERTY, true);
      }

      loadDescription(controlFlowJson, transition);
      loadAttributes(transition, controlFlowJson);

      return controlFlowJson;
   }

   /**
    * @param model
    * @return
    */
   public JsonObject toModel(ModelType model)
   {
      JsonObject modelJson = new JsonObject();
      modelJson.addProperty(ModelerConstants.ID_PROPERTY, model.getId());
      modelJson.addProperty(ModelerConstants.NAME_PROPERTY, model.getName());
      modelJson.addProperty(ModelerConstants.OID_PROPERTY, model.getOid());
      modelJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(model));
      modelJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.MODEL_KEY);

      return modelJson;
   }

   /**
    * @param structType
    * @return
    */
   public JsonObject toTypeDeclarationType(TypeDeclarationType structType)
   {
      JsonObject structJson = new JsonObject();
      structJson.addProperty(ModelerConstants.ID_PROPERTY, structType.getId());
      structJson.addProperty(ModelerConstants.NAME_PROPERTY, structType.getName());
      structJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(structType));
      setContainingModelIdProperty(structJson, structType);
      JsonObject typeDeclarationJson = new JsonObject();
      structJson.add(ModelerConstants.TYPE_DECLARATION_PROPERTY, typeDeclarationJson);
      JsonObject childrenJson = new JsonObject();
      typeDeclarationJson.add("children", childrenJson);
      structJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.TYPE_DECLARATION_PROPERTY);

      return structJson;
   }

   /**
    * 
    * @param orientation
    * @return
    */
   private static int mapAnchorOrientation(String orientation)
   {
      if (orientation.equals("top"))
      {
         return ModelerConstants.NORTH_KEY;
      }
      else if (orientation.equals("right"))
      {
         return ModelerConstants.EAST_KEY;
      }
      else if (orientation.equals("bottom"))
      {
         return ModelerConstants.SOUTH_KEY;
      }
      else if (orientation.equals("left"))
      {
         return ModelerConstants.WEST_KEY;
      }
      else if (orientation.equals("center") || orientation == null)
      {
         return ModelerConstants.UNDEFINED_ORIENTATION_KEY;
      }

      throw new IllegalArgumentException("Illegal orientation key " + orientation + ".");
   }

   /**
    * 
    * @param modelElementJson
    * @param element
    */
   private static void loadDescription(JsonObject modelElementJson,
         IIdentifiableModelElement element)
   {
      if (null != element.getDescription())
      {
         modelElementJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY,
               (String) element.getDescription().getMixed().get(0).getValue());
      }
      else
      {
         modelElementJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "");
      }
   }

   /**
    * 
    * @param element
    * @param json
    * @throws JSONException
    */
   private static void loadAttributes(IIdentifiableModelElement element, JsonObject json)
   {
      JsonObject attributes;

      if ( !json.has(ModelerConstants.ATTRIBUTES_PROPERTY))
      {
         json.add(ModelerConstants.ATTRIBUTES_PROPERTY, attributes = new JsonObject());
      }
      else
      {
         attributes = json.getAsJsonObject(ModelerConstants.ATTRIBUTES_PROPERTY);
      }

      for (AttributeType attribute : element.getAttribute())
      {
         attributes.addProperty(attribute.getName(), attribute.getValue());
      }
   }

   /**
    * 
    * TODO From DynamicConnectionCommand. Refactor?
    * 
    * @param activity
    * @return
    */
   private static String getDefaultDataMappingContext(ActivityType activity)
   {
      if (ActivityImplementationType.ROUTE_LITERAL == activity.getImplementation())
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }

      if (ActivityImplementationType.MANUAL_LITERAL == activity.getImplementation())
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }

      if (ActivityImplementationType.APPLICATION_LITERAL == activity.getImplementation()
            && activity.getApplication() != null)
      {
         ApplicationType application = activity.getApplication();
         
         if (application.isInteractive())
         {
            if (application.getContext().size() > 0)
            {
               ContextType context = (ContextType) application.getContext().get(0);
               return context.getType().getId();
            }
            return PredefinedConstants.DEFAULT_CONTEXT;
         }
         
         return PredefinedConstants.APPLICATION_CONTEXT;
      }

      if (ActivityImplementationType.SUBPROCESS_LITERAL == activity.getImplementation()
            && activity.getImplementationProcess() != null)
      {
         ProcessDefinitionType process = activity.getImplementationProcess();
         if (process.getFormalParameters() != null)
         {
            return PredefinedConstants.PROCESSINTERFACE_CONTEXT;
         }
      }

      return PredefinedConstants.ENGINE_CONTEXT;
   }

   /**
    * @param json
    * @param obj
    */
   private void setContainingModelIdProperty(JsonObject json, EObject obj)
   {
      ModelType containingModel = ModelUtils.findContainingModel(obj);
      if (null != containingModel)
      {
         json.addProperty(ModelerConstants.MODEL_ID_PROPERTY, containingModel.getId());
      }
   }

   private MBFacade facade()
   {
      if (facade == null)
      {
         facade = new MBFacade(modelManagementStrategy());
      }
      return facade;
   }
}
