package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.OrientationType;
import org.eclipse.stardust.model.xpdl.carnot.ParticipantType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.SubProcessModeType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.impl.ProcessDefinitionTypeImpl;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.CarnotConstants;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.DataTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalReferenceType;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParameterType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.XpdlTypeType;
import org.eclipse.stardust.modeling.repository.common.descriptors.EObjectDescriptor;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.w3c.dom.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * IPP XPDL marshaller.
 *
 * @author Marc.Gille
 * @author Robert Sauer
 */
public abstract class ModelElementMarshaller implements ModelMarshaller
{
   protected abstract EObjectUUIDMapper eObjectUUIDMapper();

   protected abstract ModelManagementStrategy modelManagementStrategy();

   private ModelBuilderFacade modelBuilderFacade;

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
         jsResult = toModelJson((ModelType) modelElement);
      }
      else if (modelElement instanceof TypeDeclarationType)
      {
         jsResult = toTypeDeclarationJson((TypeDeclarationType) modelElement);
      }
      else if (modelElement instanceof ProcessDefinitionTypeImpl)
      {
         jsResult = toProcessDefinitionJson((ProcessDefinitionType) modelElement);
      }
      else if (modelElement instanceof LaneSymbol)
      {
         jsResult = toLaneTypeJson((LaneSymbol) modelElement);
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
      else if (modelElement instanceof DataMappingType)
      {
         jsResult = toDataMappingJson((DataMappingType) modelElement);
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
      else if (modelElement instanceof ConditionalPerformerType)
      {
         jsResult = toConditionalPerformerJson((ConditionalPerformerType) modelElement);
      }
      else if (modelElement instanceof OrganizationType)
      {
         jsResult = toOrganizationJson((OrganizationType) modelElement);
      }
      else if (modelElement instanceof AccessPointType)
      {
         // Do nothing, handled via Application/Activity
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
      loadAttributes(processDefinition, processJson);

      // TODO Better way to determine whether a process provides an interface?

      if (processDefinition.getFormalParameters() != null)
      {
         processJson.addProperty(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY,
               ModelerConstants.PROVIDES_PROCESS_INTERFACE_KEY);

         JsonObject formalParametersJson = new JsonObject();

         processJson.add(ModelerConstants.FORMAL_PARAMETERS_PROPERTY,
               formalParametersJson);

         for (FormalParameterType formalParameter : processDefinition.getFormalParameters()
               .getFormalParameter())
         {
            JsonObject formalParameterJson = new JsonObject();

            formalParametersJson.add(formalParameter.getId(), formalParameterJson);
            formalParameterJson.addProperty(ModelerConstants.ID_PROPERTY,
                  formalParameter.getId());
            formalParameterJson.addProperty(ModelerConstants.NAME_PROPERTY,
                  formalParameter.getName());

            if (formalParameter.getMode().equals(ModeType.IN))
            {
               formalParameterJson.addProperty(ModelerConstants.DIRECTION_PROPERTY, ModelerConstants.IN_PARAMETER_KEY);
            }
            else if (formalParameter.getMode().equals(ModeType.INOUT))
            {
               formalParameterJson.addProperty(ModelerConstants.DIRECTION_PROPERTY, ModelerConstants.INOUT_PARAMETER_KEY);
            }
            else
            {
               formalParameterJson.addProperty(ModelerConstants.DIRECTION_PROPERTY, ModelerConstants.OUT_PARAMETER_KEY);
            }

            DataTypeType dataType = formalParameter.getDataType();
            ModelType model = ModelUtils.findContainingModel(formalParameter);

            if (dataType.getCarnotType().equals(
                  ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
            {
               formalParameterJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                     ModelerConstants.STRUCTURED_DATA_TYPE_KEY);

               String typeDeclarationId = dataType.getDeclaredType().getId();

               TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                     .getTypeDeclaration(typeDeclarationId);

               String fullId = getModelBuilderFacade().createFullId(model,
                     typeDeclaration);

               formalParameterJson.addProperty(
                     ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY, fullId);
            }
            else if (dataType.getCarnotType().equals(
                  ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
            {
               formalParameterJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                     ModelerConstants.DOCUMENT_DATA_TYPE_KEY);

               String typeDeclarationId = dataType.getDeclaredType().getId();

               TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                     .getTypeDeclaration(typeDeclarationId);

               String fullId = getModelBuilderFacade().createFullId(model,
                     typeDeclaration);

               formalParameterJson.addProperty(
                     ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY, fullId);
            }
            else if (dataType.getCarnotType().equals(
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
            {
               formalParameterJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                     ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
               String type = formalParameter.getDataType().getBasicType().getType()
                     .getLiteral();
               formalParameterJson.addProperty(
                     ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY, type);
            }
         }
      }
      else
      {
         processJson.addProperty(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY,
               ModelerConstants.NO_PROCESS_INTERFACE_KEY);
      }

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
               getModelBuilderFacade().createFullId(
                     ModelUtils.findContainingModel(dataPath), dataPath.getData()));
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
   public JsonObject toLaneTypeJson(LaneSymbol laneSymbol)
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
            getModelBuilderFacade().createFullId(
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
                     getModelBuilderFacade().createFullId(
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
            if (connectionJson.has(ModelerConstants.MODEL_ELEMENT_PROPERTY))
            {
               // ModelElement Id for dataFlow is DataId, which duplicates in case of
               // IN-OUT mapping for data, using DATA MAPPING OID
               connectionsJson.add(
                     extractInt(
                           connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                           ModelerConstants.OID_PROPERTY).toString(), connectionJson);
            }
         }

         // Transitions

         for (TransitionConnectionType transitionConnection : poolSymbol.getTransitionConnection())
         {
            JsonObject connectionJson = toTransitionConnectionJson(transitionConnection);
            if (connectionJson.has(ModelerConstants.MODEL_ELEMENT_PROPERTY))
            {
               connectionsJson.add(
                     extractString(
                           connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                           ModelerConstants.ID_PROPERTY), connectionJson);
            }
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

      if (null != activity)
      {
         activityJson.addProperty(ModelerConstants.OID_PROPERTY, activity.getElementOid());
         activityJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());
         activityJson.addProperty(ModelerConstants.NAME_PROPERTY, activity.getName());
         loadDescription(activityJson, activity);
         loadAttributes(activity, activityJson);
         if (activity.getId().toLowerCase().startsWith("gateway"))
         {
            activityJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                  ModelerConstants.ACTIVITY_KEY);
            activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE,
                  ModelerConstants.GATEWAY_ACTIVITY);

            // TODO Throw error for inconsistent Split/Join settings

            if (activity.getJoin().equals(JoinSplitType.XOR_LITERAL)
                  && activity.getSplit().equals(JoinSplitType.XOR_LITERAL))
            {
               activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                     ModelerConstants.XOR_GATEWAY_TYPE);
            }
            else if (activity.getJoin().equals(JoinSplitType.AND_LITERAL)
                  && activity.getSplit().equals(JoinSplitType.AND_LITERAL))
            {
               activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                     ModelerConstants.AND_GATEWAY_TYPE);
            }
            else
            {
               // Default behavior for incorrectly defined Gateways

               activityJson.addProperty(ModelerConstants.GATEWAY_TYPE_PROPERTY,
                     ModelerConstants.XOR_GATEWAY_TYPE);
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
                     getModelBuilderFacade().createFullId(
                           ModelUtils.findContainingModel(activity),
                           activity.getImplementationProcess()));
               if (activity.getSubProcessMode().equals(SubProcessModeType.SYNC_SEPARATE_LITERAL))
               {
                  activityJson.addProperty(ModelerConstants.SUBPROCESS_MODE_PROPERTY, ModelerConstants.SYNC_SEPARATE_KEY);
               }
               else if (activity.getSubProcessMode().equals(SubProcessModeType.SYNC_SHARED_LITERAL))
               {
                  activityJson.addProperty(ModelerConstants.SUBPROCESS_MODE_PROPERTY, ModelerConstants.SYNC_SHARED_KEY);
               }
               else if (activity.getSubProcessMode().equals(SubProcessModeType.ASYNC_SEPARATE_LITERAL))
               {
                  activityJson.addProperty(ModelerConstants.SUBPROCESS_MODE_PROPERTY, ModelerConstants.ASYNC_SEPARATE_KEY);
               }
            }
            else if ((activity.eContainer() != null && (!(activity.eContainer() instanceof ChangeDescription)))
                  && activity.getApplication() != null)
            {
               ApplicationType application = activity.getApplication();
               activityJson.addProperty(
                     ModelerConstants.APPLICATION_FULL_ID_PROPERTY,
                     getModelBuilderFacade().createFullId(
                           ModelUtils.findContainingModel(application),
                           application));
            }

            JsonObject accessPointsJson = new JsonObject();

            activityJson.add(ModelerConstants.ACCESS_POINTS_PROPERTY, accessPointsJson);

            String[] contexts = new String[] {
                  PredefinedConstants.DEFAULT_CONTEXT,
                  PredefinedConstants.APPLICATION_CONTEXT,
                  PredefinedConstants.PROCESSINTERFACE_CONTEXT,
                  PredefinedConstants.ENGINE_CONTEXT};

            System.out.println("Access Points: ");

            for (String context : contexts)
            {
               // Activity has no model as parent --> it has been deleted from the model
               if ( !(activity.eContainer() instanceof ChangeDescription))
               {
                  for (AccessPointType accessPoint : ActivityUtil.getAccessPoints(
                        activity, true, context))
                  {
                     System.out.println(accessPoint);

                     JsonObject accessPointJson = new JsonObject();

                     accessPointsJson.add(accessPoint.getId(), accessPointJson);
                     accessPointJson.addProperty(ModelerConstants.ID_PROPERTY,
                           accessPoint.getId());
                     accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
                           accessPoint.getName());
                     accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                           accessPoint.getDirection().getLiteral());
                     accessPointJson.addProperty(ModelerConstants.CONTEXT_PROPERTY,
                           context);
                     loadDescription(accessPointJson, accessPoint);
                  }

                  for (AccessPointType accessPoint : ActivityUtil.getAccessPoints(
                        activity, false, context))
                  {
                     System.out.println(accessPoint);

                     JsonObject accessPointJson = new JsonObject();

                     accessPointsJson.add(accessPoint.getId(), accessPointJson);
                     accessPointJson.addProperty(ModelerConstants.ID_PROPERTY,
                           accessPoint.getId());
                     accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
                           accessPoint.getName());
                     accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                           accessPoint.getDirection().getLiteral());
                     accessPointJson.addProperty(ModelerConstants.CONTEXT_PROPERTY,
                           context);
                     loadDescription(accessPointJson, accessPoint);
                  }
               }
            }

            /*
             * if (null != activity.getPerformer()) { act.getProps().setPerformerid(
             * activity.getPerformer().getId()); }
             */

         }
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
            activitySymbol.getXPos() + laneOffsetX);
      activitySymbolJson.addProperty(ModelerConstants.Y_PROPERTY,
            activitySymbol.getYPos() + laneOffsetY);
      activitySymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY,
            activitySymbol.getWidth());
      activitySymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY,
            activitySymbol.getHeight());

      ActivityType activity = activitySymbol.getActivity();

      if (null != activity)
      {
         activitySymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY,
               toActivityJson(activity));
         if (activity.getId().toLowerCase().startsWith("gateway"))
         {
            activitySymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                  ModelerConstants.GATEWAY_SYMBOL);

            // // TODO REVIEW There is no gateway symbol needed!!!
            //
            // // TODO Refactor
            // // Identify the gateway symbol for this activity and update the
            // // location and dimension attributes.
            // GatewaySymbol thisGatewaySymbol = null;
            // // for (GatewaySymbol gs : laneSymbol.getGatewaySymbol()) {
            // // if (gs.getActivitySymbol().getActivity().equals(activity)) {
            // // thisGatewaySymbol = gs;
            // // break;
            // // }
            // // }
            //
            // if (null != thisGatewaySymbol)
            // {
            // activitySymbolJson.remove(ModelerConstants.X_PROPERTY);
            // activitySymbolJson.addProperty(ModelerConstants.X_PROPERTY,
            // thisGatewaySymbol.getXPos() + laneOffsetX
            // + ModelerConstants.POOL_LANE_MARGIN);
            // activitySymbolJson.remove(ModelerConstants.Y_PROPERTY);
            // activitySymbolJson.addProperty(ModelerConstants.Y_PROPERTY,
            // thisGatewaySymbol.getYPos() + laneOffsetY
            // + ModelerConstants.POOL_LANE_MARGIN
            // + ModelerConstants.POOL_SWIMLANE_TOP_BOX_HEIGHT);
            // activitySymbolJson.remove(ModelerConstants.WIDTH_PROPERTY);
            // activitySymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY,
            // thisGatewaySymbol.getWidth());
            // activitySymbolJson.remove(ModelerConstants.HEIGHT_PROPERTY);
            // activitySymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY,
            // thisGatewaySymbol.getHeight());
            // }
         }
         else
         {
            activitySymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                  ModelerConstants.ACTIVITY_SYMBOL);
         }
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
      eventSymbolJson.addProperty(ModelerConstants.X_PROPERTY, startEventSymbol.getXPos()
            + laneOffsetX);
      eventSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, startEventSymbol.getYPos()
            + laneOffsetY);

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
            + laneOffsetX);
      eventSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, endEventSymbol.getYPos()
            + laneOffsetY);

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
         dataJson.addProperty(ModelerConstants.OID_PROPERTY, data.getElementOid());
         ModelType model = ModelUtils.findContainingModel(data);
         dataJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         setContainingModelIdProperty(dataJson, data);

         loadDescription(dataJson, data);
         loadAttributes(data, dataJson);

         if (data.getType().getId().equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.STRUCTURED_DATA_TYPE_KEY);
            String uri = AttributeUtil.getAttributeValue(data,
                  IConnectionManager.URI_ATTRIBUTE_NAME);
            if (null != model)
            {
               IConnectionManager manager = model.getConnectionManager();
               if (manager != null & uri != null)
               {
                  EObject eObject = manager.find(uri);
                  if (eObject instanceof EObjectDescriptor)
                  {
                     eObject = ((EObjectDescriptor) eObject).getEObject();
                  }
                  ModelType containingModel = ModelUtils.findContainingModel(eObject);

                  String fullId = getModelBuilderFacade().createFullId(containingModel,
                        eObject);

                  dataJson.addProperty(
                        ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY, fullId);
               }
               else
               {
                  String typeDeclarationId = AttributeUtil.getAttributeValue(data,
                        StructuredDataConstants.TYPE_DECLARATION_ATT);

                  if ( !StringUtils.isEmpty(typeDeclarationId))
                  {
                     TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                           .getTypeDeclaration(typeDeclarationId);

                     String fullId = getModelBuilderFacade().createFullId(model,
                           typeDeclaration);

                     dataJson.addProperty(
                           ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY, fullId);
                  }
               }
            }
         }
         else if (data.getType().getId().equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.DOCUMENT_DATA_TYPE_KEY);
            String uri = AttributeUtil.getAttributeValue(data,
                  IConnectionManager.URI_ATTRIBUTE_NAME);
            if (null != model)
            {
               IConnectionManager manager = model.getConnectionManager();
               if (manager != null & uri != null)
               {
                  EObject eObject = manager.find(uri);
                  if (eObject instanceof EObjectDescriptor)
                  {
                     eObject = ((EObjectDescriptor) eObject).getEObject();
                  }
                  ModelType containingModel = ModelUtils.findContainingModel(eObject);

                  String fullId = getModelBuilderFacade().createFullId(containingModel,
                        eObject);

                  dataJson.addProperty(
                        ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY, fullId);
               }
            }
         }
         else if (data.getType().getId().equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
            String type = AttributeUtil.getAttributeValue(data, CarnotConstants.TYPE_ATT);
            dataJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY, type);
         }
         else
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY, data.getType()
                  .getId());
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
      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (dataSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) dataSymbol.eContainer()
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

      dataSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            dataSymbol.getElementOid());
      dataSymbolJson.addProperty(ModelerConstants.X_PROPERTY, dataSymbol.getXPos()
            + laneOffsetX);
      dataSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, dataSymbol.getYPos()
            + laneOffsetY);
      dataSymbolJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(dataSymbol));
      dataSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.DATA_SYMBOL);

      // Model returned will be null in case of data delete operation

      ModelType containingModel = ModelUtils.findContainingModel(dataSymbol.getData());
      if (null != containingModel)
      {
         dataSymbolJson.addProperty(
               ModelerConstants.DATA_FULL_ID_PROPERTY,
               getModelBuilderFacade().createFullId(containingModel, dataSymbol.getData()));
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
      roleJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(role));
      ModelType model = ModelUtils.findContainingModel(role);

      if (null != model)
      {
         List<OrganizationType> parentOrgs = getModelBuilderFacade().getParentOrganizations(
               model, role);
         if (parentOrgs.size() > 0)
         {
            // TODO - add array of orgs
            OrganizationType org = parentOrgs.get(0);
            roleJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper().getUUID(org));
            if (null != org.getTeamLead() && org.getTeamLead().equals(role))
            {
               roleJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                     ModelerConstants.TEAM_LEADER_TYPE_KEY);
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
    * @param conditionalPerformer
    * @return
    */
   public JsonObject toConditionalPerformerJson(
         ConditionalPerformerType conditionalPerformer)
   {
      JsonObject conditionalPerformerJson = new JsonObject();

      conditionalPerformerJson.addProperty(ModelerConstants.ID_PROPERTY,
            conditionalPerformer.getId());
      conditionalPerformerJson.addProperty(ModelerConstants.NAME_PROPERTY,
            conditionalPerformer.getName());
      conditionalPerformerJson.addProperty(ModelerConstants.OID_PROPERTY,
            conditionalPerformer.getElementOid());
      conditionalPerformerJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY);
      conditionalPerformerJson.addProperty(
            ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY,
            getModelBuilderFacade().createFullId(
                  ModelUtils.findContainingModel(conditionalPerformer.getData()),
                  conditionalPerformer.getData()));
      conditionalPerformerJson.addProperty(ModelerConstants.BINDING_DATA_PATH_PROPERTY,
            conditionalPerformer.getDataPath());
      conditionalPerformerJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(conditionalPerformer));

      ModelType model = ModelUtils.findContainingModel(conditionalPerformer);

      if (null != model)
      {
         List<OrganizationType> parentOrgs = getModelBuilderFacade().getParentOrganizations(
               model, conditionalPerformer);
         if (parentOrgs.size() > 0)
         {
            // TODO - add array of orgs
            OrganizationType org = parentOrgs.get(0);
            conditionalPerformerJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper().getUUID(org));
         }
         conditionalPerformerJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         conditionalPerformerJson.addProperty(ModelerConstants.MODEL_ID_PROPERTY,
               model.getId());
      }

      loadDescription(conditionalPerformerJson, conditionalPerformer);
      loadAttributes(conditionalPerformer, conditionalPerformerJson);

      return conditionalPerformerJson;
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
         List<OrganizationType> parentOrgs = getModelBuilderFacade().getParentOrganizations(
               model, org);
         if (parentOrgs.size() > 0)
         {
            orgJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper().getUUID(parentOrgs.get(0)));
         }
         orgJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         orgJson.addProperty(ModelerConstants.MODEL_ID_PROPERTY, model.getId());
      }

      if (org.getTeamLead() != null)
      {
         orgJson.addProperty(
               ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY,
               getModelBuilderFacade().createFullId(
                     ModelUtils.findContainingModel(org.getTeamLead()), org.getTeamLead()));
      }
      else
      {
         orgJson.addProperty(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY, (String) null);
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

      applicationJson.addProperty(ModelerConstants.INTERACTIVE_PROPERTY, application.isInteractive());

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
         System.out.println("Access Model: " + accessPoint.getId());

         JsonObject accessPointJson = new JsonObject();

         accessPointsJson.add(accessPoint.getId(), accessPointJson);
         accessPointJson.addProperty(ModelerConstants.ID_PROPERTY, accessPoint.getId());
         accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
               accessPoint.getName());

         if (accessPoint.getType() != null)
         {
            accessPointJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                  accessPoint.getType().getName());
         }

         if (accessPoint.getDirection() != null)
         {
            accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                  accessPoint.getDirection().getLiteral());
         }

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
      JsonObject dataFlowJson = new JsonObject();

      connectionJson.addProperty(ModelerConstants.OID_PROPERTY,
            dataMappingConnection.getElementOid());

      connectionJson.addProperty(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(dataMappingConnection.getSourceAnchor()));
      connectionJson.addProperty(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(dataMappingConnection.getTargetAnchor()));

      DataType data = dataMappingConnection.getDataSymbol().getData();
      if (null != data)
      {
         ActivityType activity = dataMappingConnection.getActivitySymbol().getActivity();
         for (DataMappingType dataMapping : activity.getDataMapping())
         {
            // Update the dataFlowJson for currentData symbol
            if (dataMapping.getData().getId().equals(data.getId()))
            {
               dataFlowJson = toDataMappingJson(dataMapping);
               // TODO - Currently API always assumes connectionJson.getSourceNode will
               // be data, to set Activity in sourceNode for OUT Mapping for data below
               // code is added.
               if (dataFlowJson.get(ModelerConstants.OUT_DATA_MAPPING_PROPERTY).getAsBoolean())
               {
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
               // If user unchecks IN,OUT mapping from properties page, following will be
               // considered
               {
                  connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
                        dataMappingConnection.getDataSymbol().getElementOid());
                  connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                        ModelerConstants.DATA);
                  connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
                        dataMappingConnection.getActivitySymbol().getElementOid());
                  connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                        ModelerConstants.ACTIVITY_KEY);
               }
            }
         }

         dataFlowJson.addProperty(
               ModelerConstants.DATA_FULL_ID_PROPERTY,
               getModelBuilderFacade().createFullId(ModelUtils.findContainingModel(data),
                     data));
         dataFlowJson.addProperty(ModelerConstants.ACTIVITY_ID_PROPERTY, activity.getId());
         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, dataFlowJson);
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
      connectionJson.addProperty(ModelerConstants.OID_PROPERTY,
            transitionConnection.getElementOid());

      if (transitionConnection.getTransition() != null)
      {
         TransitionType transition = transitionConnection.getTransition();

         modelElementJson = toTransitionJson(transition);

         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);

         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(ModelerConstants.ID_PROPERTY, transition.getId());
         modelElementJson.addProperty(ModelerConstants.OID_PROPERTY,
               transition.getElementOid());

         modelElementJson.addProperty(ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY,
               transition.isForkOnTraversal());

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
         String activityId = ((ActivitySymbolType) transitionConnection.getTargetActivitySymbol()).getActivity()
               .getId();
         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);

         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(ModelerConstants.ID_PROPERTY,
               transitionConnection.getSourceNode().getElementOid() + "-" + activityId);

         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
               transitionConnection.getSourceNode().getElementOid());
         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
               ModelerConstants.EVENT_KEY);
         connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_OID,
               transitionConnection.getTargetActivitySymbol().getElementOid());
         // Added to identify the Gateway for target Symbol
         if (activityId.toLowerCase().startsWith("gateway"))
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.GATEWAY);
         }
         else
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.ACTIVITY_KEY);
         }

      }
      else if (transitionConnection.getTargetNode() instanceof EndEventSymbol)
      {
         modelElementJson = new JsonObject();
         String activityId = ((ActivitySymbolType) transitionConnection.getSourceActivitySymbol()).getActivity()
               .getId();
         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);
         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(ModelerConstants.ID_PROPERTY, activityId + "-"
               + String.valueOf(transitionConnection.getTargetNode().getElementOid()));
         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
               transitionConnection.getSourceActivitySymbol().getElementOid());
         // Added to identify the Gateway for source Symbol
         if (activityId.toLowerCase().startsWith("gateway"))
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

      if (null != transition.getCondition()
            && transition.getCondition().equals("CONDITION"))
      {
         if (null != transition.getExpression())
         {
            controlFlowJson.addProperty(ModelerConstants.CONDITION_EXPRESSION_PROPERTY,
                  (String) transition.getExpression().getMixed().getValue(0));
         }
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

   @Override
   public JsonObject toModelJson(EObject model)
   {
      return toModelJson((ModelType) model);
   }

   @Override
   public JsonObject toProcessDiagramJson(EObject model, String processId)
   {
      return toProcessDefinitionDiagram(ModelUtils.findIdentifiableElement(
            ((ModelType) model).getProcessDefinition(), processId));
   }

   /**
    *
    * @param dataMapping
    * @return
    */
   public JsonObject toDataMappingJson(DataMappingType dataMapping)
   {
      JsonObject dataFlowJson = new JsonObject();

      dataFlowJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.DATA_FLOW_LITERAL);
      dataFlowJson.addProperty(ModelerConstants.ID_PROPERTY, dataMapping.getId());
      dataFlowJson.addProperty(ModelerConstants.OID_PROPERTY, dataMapping.getElementOid());

      if (null != dataMapping.getDirection())
      {
         if (dataMapping.getDirection().equals(DirectionType.IN_LITERAL))
         {
            dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY, true);
            dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY, false);
         }
         else if (dataMapping.getDirection().equals(DirectionType.OUT_LITERAL))
         {
            dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY, false);
            dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY, true);
         }
         else if(dataMapping.getDirection().equals(DirectionType.INOUT_LITERAL))
         {
            // IN_OUT Mapping scenario
            dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY, true);
            dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY, true);
         }
         else
         {
            dataFlowJson.addProperty(ModelerConstants.IN_DATA_MAPPING_PROPERTY, false);
            dataFlowJson.addProperty(ModelerConstants.OUT_DATA_MAPPING_PROPERTY, false);
         }
      }
      return dataFlowJson;
   }


   /**
    * @param model
    * @return
    */
   public JsonObject toModelJson(ModelType model)
   {
      JsonObject modelJson = new JsonObject();

      modelJson.addProperty(ModelerConstants.ID_PROPERTY, model.getId());
      modelJson.addProperty(ModelerConstants.NAME_PROPERTY, model.getName());
      modelJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(model));
      modelJson.addProperty(ModelerConstants.FILE_NAME,
            modelManagementStrategy().getModelFileName(model));
      modelJson.addProperty(ModelerConstants.FILE_PATH,
            modelManagementStrategy().getModelFilePath(model));
      modelJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.MODEL_KEY);

      if (model.getDescription() != null)
      {
         modelJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY,
               (String) model.getDescription().getMixed().get(0).getValue());
      }
      else
      {
         modelJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, (String) null);
      }

      JsonObject processesJson = new JsonObject();

      modelJson.add("processes", processesJson);

      for (ProcessDefinitionType processDefinition : model.getProcessDefinition())
      {
         processesJson.add(processDefinition.getId(),
               toProcessDefinitionJson(processDefinition));
      }

      JsonObject participantsJson = new JsonObject();
      modelJson.add("participants", participantsJson);

      for (RoleType role : model.getRole())
      {
         if ( !hasParentParticipant(model, role))
         {
            participantsJson.add(role.getId(), toRoleJson(role));
         }
      }

      for (OrganizationType organization : model.getOrganization())
      {
         if ( !hasParentParticipant(model, organization))
         {
            JsonObject participantJson = toOrganizationJson(organization);

            participantsJson.add(organization.getId(), participantJson);

            // Adds children if any

            addChildParticipantsJson(participantJson, organization);
         }
      }

      for (ConditionalPerformerType conditionalPerformer : model.getConditionalPerformer())
      {
         if ( !hasParentParticipant(model, conditionalPerformer))
         {
            participantsJson.add(conditionalPerformer.getId(),
                  toConditionalPerformerJson(conditionalPerformer));
         }
      }

      JsonObject applicationsJson = new JsonObject();

      modelJson.add("applications", applicationsJson);

      for (ApplicationType application : model.getApplication())
      {
         applicationsJson.add(application.getId(), toApplication(application));
      }

      JsonObject dataItemsJson = new JsonObject();

      modelJson.add("dataItems", dataItemsJson);

      for (DataType data : model.getData())
      {
         dataItemsJson.add(data.getId(), toDataJson(data));
      }

      JsonObject typeDeclarationsJson = new JsonObject();

      modelJson.add("typeDeclarations", typeDeclarationsJson);

      for (TypeDeclarationType typeDeclaration : model.getTypeDeclarations().getTypeDeclaration())
      {
         typeDeclarationsJson.add(typeDeclaration.getId(),
               toTypeDeclarationJson(typeDeclaration));
      }

      return modelJson;
   }

   /**
    * @param model
    * @param participant
    * @return
    */
   private boolean hasParentParticipant(ModelType model, IModelParticipant participant)
   {
      List<OrganizationType> parentOrgs = getModelBuilderFacade().getParentOrganizations(
            model, participant);
      if (parentOrgs.size() > 0)
      {
         return true;
      }

      return false;
   }

   /**
    * @param parentJson
    * @param parent
    */
   private void addChildParticipantsJson(JsonObject parentJson, OrganizationType parent)
   {
      EList<ParticipantType> children = parent.getParticipant();
      if (children.size() > 0)
      {
         JsonArray childrenArray = new JsonArray();
         parentJson.add(ModelerConstants.CHILD_PARTICIPANTS_KEY, childrenArray);
         for (ParticipantType child : children)
         {
            IModelParticipant childParticipant = child.getParticipant();

            if (null != childParticipant)
            {
               JsonObject childJson = new JsonObject();
               childrenArray.add(childJson);

               childJson.addProperty(ModelerConstants.ID_PROPERTY,
                     childParticipant.getId());
               childJson.addProperty(ModelerConstants.NAME_PROPERTY,
                     childParticipant.getName());
               childJson.addProperty(ModelerConstants.OID_PROPERTY,
                     childParticipant.getElementOid());
               childJson.addProperty(ModelerConstants.UUID_PROPERTY,
                     eObjectUUIDMapper().getUUID(childParticipant));
               childJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                     eObjectUUIDMapper().getUUID(parent));
               loadDescription(childJson, childParticipant);

               if (childParticipant instanceof OrganizationType)
               {
                  childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                        ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY);
                  addChildParticipantsJson(childJson, (OrganizationType) childParticipant);
               }
               else if (childParticipant instanceof RoleType)
               {
                  if (null != parent.getTeamLead()
                        && parent.getTeamLead().equals(childParticipant))
                  {
                     childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                           ModelerConstants.TEAM_LEADER_TYPE_KEY);
                  }
                  else
                  {
                     childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                           ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY);
                  }
               }
               else if (childParticipant instanceof ConditionalPerformerType)
               {
                  childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                        ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY);
               }
            }
         }
      }
   }

   /**
    * @param structType
    * @return
    */
   public JsonObject toTypeDeclarationJson(TypeDeclarationType structType)
   {
      JsonObject structJson = new JsonObject();

      structJson.addProperty(ModelerConstants.ID_PROPERTY, structType.getId());
      structJson.addProperty(ModelerConstants.NAME_PROPERTY, structType.getName());
      structJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(structType));
      setContainingModelIdProperty(structJson, structType);

      // TODO: external references
      XpdlTypeType type = structType.getDataType();
      if (null != type)
      {
         structJson.add("type", toXpdlTypeJson(type));
      }
      if (null != structType.getSchema())
      {
         JsonObject schemaJson = new JsonObject();
         ModelService.loadSchemaInfo(schemaJson, structType.getSchema());
         structJson.add("schema", schemaJson);
      }
      structJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.TYPE_DECLARATION_PROPERTY);

      return structJson;
   }

   private JsonElement toXpdlTypeJson(XpdlTypeType type)
   {
      JsonObject typeJson = new JsonObject();

      String name = type.eClass().getName();
      typeJson.addProperty("classifier", name.substring(0, name.length() - 4)); // exclude "Type" suffix
      if (type instanceof ExternalReferenceType)
      {
         ExternalReferenceType ref = (ExternalReferenceType) type;
         typeJson.addProperty("location", ref.getLocation());
         if (!StringUtils.isEmpty(ref.getNamespace()))
         {
            typeJson.addProperty("namespace", ref.getNamespace());
         }
         if (!StringUtils.isEmpty(ref.getXref()))
         {
            typeJson.addProperty("xref", ref.getXref());
         }
      }

      return typeJson;
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

   private ModelBuilderFacade getModelBuilderFacade()
   {
      if (modelBuilderFacade == null)
      {
         modelBuilderFacade = new ModelBuilderFacade(modelManagementStrategy());
      }
      return modelBuilderFacade;
   }
}
