package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.eclipse.emf.common.util.ECollections.sort;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingDiagram;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.xsd.XSDSchema;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.extensions.FormalParameterMappingsType;
import org.eclipse.stardust.model.xpdl.carnot.impl.ProcessDefinitionTypeImpl;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.CarnotConstants;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.util.IConnectionManager;
import org.eclipse.stardust.model.xpdl.xpdl2.*;
import org.eclipse.stardust.model.xpdl.xpdl2.DataTypeType;
import org.eclipse.stardust.modeling.repository.common.descriptors.EObjectDescriptor;
import org.eclipse.stardust.ui.web.modeler.service.XsdSchemaUtils;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController.ChangeDescriptionJto;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController.CommandJto;

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

   protected abstract ClassLoaderProvider classLoaderProvider();

   private ModelBuilderFacade modelBuilderFacade;

   private JsonMarshaller jsonIo = new JsonMarshaller();

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
      else if (modelElement instanceof IntermediateEventSymbol)
      {
         jsResult = toIntermediateEventJson((IntermediateEventSymbol) modelElement);
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
         // Do nothing - handled via DataMappingConnectionType
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
      else if (modelElement instanceof AnnotationSymbolType)
      {
         jsResult = toAnnotationSymbolJson((AnnotationSymbolType) modelElement);
      }
      else if (modelElement instanceof AccessPointType)
      {
         // Do nothing, handled via Application/Activity
      }
      else if (modelElement instanceof EventHandlerType)
      {
         jsResult = toEventJson((EventHandlerType) modelElement, new JsonObject());         
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

         if (modelElement instanceof IExtensibleElement)
         {
            loadAttributes(modelElement, jsResult);
         }
         jsResult.addProperty("moreContent", "TODO");
      }

      return jsResult;
   }

   /**
    * @return
    */
   public JsonObject toProcessDefinitionJson(ProcessDefinitionType processDefinition)
   {
      List<ChangeDescriptionJto> changeDescriptions = null;
      CommandJto commandJto = ModelerSessionRestController.getCommandJto();
      if (commandJto != null)
      {
         changeDescriptions = commandJto.changeDescriptions;
      }

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

      if (null != processDefinition.getFormalParameters()
            && null != processDefinition.getFormalParameters().getFormalParameter())
      {
         processJson.addProperty(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY,
               ModelerConstants.PROVIDES_PROCESS_INTERFACE_KEY);

         if (processDefinition.getExternalRef() != null)
         {
            processJson.addProperty(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY,
                  ModelerConstants.IMPLEMENTS_PROCESS_INTERFACE_KEY);
         }

         processJson.add(ModelerConstants.FORMAL_PARAMETERS_PROPERTY,
               getFormalParametersJson(processDefinition, changeDescriptions));
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
         // JsonObject activityJson = new JsonObject();
         // activitiesJson.add(activity.getId(), activityJson);
         //
         // activityJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());
         // activityJson.addProperty(ModelerConstants.NAME_PROPERTY, activity.getName());
         // loadDescription(activityJson, activity);
         activitiesJson.add(activity.getId(), toActivityJson(activity));
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

   private JsonArray getFormalParametersJson(ProcessDefinitionType processDefinition,
         List<ChangeDescriptionJto> changeDescriptions)
   {
      JsonArray formalParametersJson = new JsonArray();
      FormalParametersType formalParameters = processDefinition.getFormalParameters();
      if (formalParameters != null)
      {
         for (FormalParameterType formalParameter : formalParameters.getFormalParameter())
         {
            formalParametersJson.add(getFormalParameterJson(formalParameter, changeDescriptions));
         }
      }
      return formalParametersJson;
   }

   private JsonObject getFormalParameterJson(FormalParameterType formalParameter,
         List<ChangeDescriptionJto> changeDescriptions)
   {
      JsonObject formalParameterJson = new JsonObject();

      formalParameterJson.addProperty(ModelerConstants.ID_PROPERTY, formalParameter.getId());
      formalParameterJson.addProperty(ModelerConstants.NAME_PROPERTY, formalParameter.getName());

      ModeType mode = formalParameter.getMode();
      if (mode != null)
      {
         formalParameterJson.addProperty(ModelerConstants.DIRECTION_PROPERTY, mode.getLiteral());
      }

      DataTypeType dataType = formalParameter.getDataType();
      final ModelType model = ModelUtils.findContainingModel(formalParameter);
      if (model != null)
      {
         if (dataType.getCarnotType().equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
         {
            formalParameterJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.STRUCTURED_DATA_TYPE_KEY);

            ModelType typeModel = model;
            XpdlTypeType xpdlType = dataType.getDataType();
            String typeDeclarationId = null;
            if (xpdlType instanceof DeclaredTypeType)
            {
               typeDeclarationId = ((DeclaredTypeType) xpdlType).getId();
            }
            else if (xpdlType instanceof ExternalReferenceType)
            {
               String modelId = ((ExternalReferenceType) xpdlType).getLocation();
               typeModel = getModelBuilderFacade().findModel(modelId);
               typeDeclarationId = ((ExternalReferenceType) xpdlType).getXref();
            }

            TypeDeclarationType typeDeclaration = typeModel.getTypeDeclarations()
                  .getTypeDeclaration(typeDeclarationId);

            String fullId = getModelBuilderFacade().createFullId(typeModel, typeDeclaration);

            formalParameterJson.addProperty(
                  ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY, fullId);
         }
         else if (dataType.getCarnotType().equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
         {
            formalParameterJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.DOCUMENT_DATA_TYPE_KEY);

            String typeDeclarationId = dataType.getDeclaredType().getId();

            TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                  .getTypeDeclaration(typeDeclarationId);

            formalParameterJson.addProperty(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY,
                  getModelBuilderFacade().createFullId(model, typeDeclaration));
         }
         else if (dataType.getCarnotType().equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
         {
            formalParameterJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);

            String type = getPrimitiveType(formalParameter, changeDescriptions);
            if (type != null)
            {
               formalParameterJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY, type);
            }
         }

         FormalParameterMappingsType mappingsType = getFormalParameterMappings(formalParameter);
         if (mappingsType != null)
         {
            DataType data = mappingsType.getMappedData(formalParameter);
            setDataFullID(formalParameterJson, model, data);
         }
      }
      return formalParameterJson;
   }

   private FormalParameterMappingsType getFormalParameterMappings(FormalParameterType formalParameter)
   {
      ProcessDefinitionType process = ModelUtils.findContainingProcess(formalParameter);
      return process.getFormalParameterMappings();
   }

   private String getPrimitiveType(FormalParameterType formalParameter,
         List<ChangeDescriptionJto> changeDescriptions)
   {
      String type = changeDescriptions == null ? null :
         findInChangeDescriptions(changeDescriptions, formalParameter.getId());
      if (null == type)
      {
         FormalParameterMappingsType mappingsType = getFormalParameterMappings(formalParameter);
         if (mappingsType != null)
         {
            DataType data = mappingsType.getMappedData(formalParameter);
            if (data != null)
            {
               type = AttributeUtil.getAttributeValue(data, "carnot:engine:type");
            }
         }
      }
      return type;
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
      laneSymbolJson.addProperty(ModelerConstants.X_PROPERTY, laneSymbol.getXPos());
      laneSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, laneSymbol.getYPos());
      laneSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, laneSymbol.getWidth());
      laneSymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY, laneSymbol.getHeight());
      laneSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.SWIMLANE_SYMBOL);

      IModelParticipant participant = LaneParticipantUtil.getParticipant(laneSymbol);
      if (null != participant)
      {
         if (getModelBuilderFacade().isExternalReference(participant))
         {
            ModelType model = ModelUtils.findContainingModel(laneSymbol);
            URI proxyUri = ((InternalEObject) participant).eProxyURI();
            ModelType referencedModel = getModelByProxyURI(model, proxyUri);

            if (referencedModel != null)
            {
               String roleId = getModelBuilderFacade().createFullId(referencedModel,
                     LaneParticipantUtil.getParticipant(laneSymbol));
               laneSymbolJson.addProperty(ModelerConstants.PARTICIPANT_FULL_ID, roleId);
            }
         }
         else
         {
            laneSymbolJson.addProperty(
                  ModelerConstants.PARTICIPANT_FULL_ID,
                  getModelBuilderFacade().createFullId(
                        ModelUtils.findContainingModel(LaneParticipantUtil.getParticipant(laneSymbol)),
                        LaneParticipantUtil.getParticipant(laneSymbol)));
            loadAttributes(laneSymbol, laneSymbolJson);
         }
      }

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

      diagramJson.addProperty(ModelerConstants.OID_PROPERTY,
            processDefinition.getDiagram().get(0).getElementOid());

      if (processDefinition.getDiagram()
            .get(0)
            .getOrientation()
            .equals(OrientationType.HORIZONTAL_LITERAL))
      {
         diagramJson.addProperty(ModelerConstants.ORIENTATION_PROPERTY,
               ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL);
      }
      else
      {
         diagramJson.addProperty(ModelerConstants.ORIENTATION_PROPERTY,
               ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL);
      }

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

         // Sort the lane Symbols based on 'X' co-ordinates
         sort(poolSymbol.getChildLanes(), new Comparator<LaneSymbol>()
         {
            @Override
            public int compare(LaneSymbol o1, LaneSymbol o2)
            {
               return (int) ((int) o1.getXPos() - (int) o2.getXPos());
            }
         });

         for (LaneSymbol laneSymbol : poolSymbol.getChildLanes())
         {
            JsonObject laneSymbolJson = toLaneTypeJson(laneSymbol);
            laneSymbols.add(laneSymbolJson);

            JsonObject activitySymbolsJson = new JsonObject();
            JsonObject gatewaySymbolsJson = new JsonObject();

            laneSymbolJson.add(ModelerConstants.ACTIVITY_SYMBOLS, activitySymbolsJson);
            laneSymbolJson.add(ModelerConstants.GATEWAY_SYMBOLS, gatewaySymbolsJson);

            List<ActivitySymbolType> boundaryEvents = newArrayList();
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

               // generate boundary events, if appropriate
               if ( !activitySymbol.getActivity().getEventHandler().isEmpty())
               {
                  boundaryEvents.add(activitySymbol);
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

            // Intermediate Events
            for (IntermediateEventSymbol eventSymbol : laneSymbol.getIntermediateEventSymbols())
            {
               JsonObject eventSymbolJson = toIntermediateEventJson(eventSymbol);
               eventSymbols.add(String.valueOf(eventSymbol.getElementOid()),
                     eventSymbolJson);
            }
            // Boundary Events
            for (ActivitySymbolType boundaryEventHostSymbol : boundaryEvents)
            {
               ActivityType hostActivity = boundaryEventHostSymbol.getActivity();
               for (EventHandlerType handler : hostActivity.getEventHandler())
               {
                  // be sure to avoid marshalling event handlers twice (in case there is
                  // an explicit intermediate event symbol)
                  if ((null == EventMarshallingUtils.resolveHostedEvent(handler))
                        && !isEmpty(EventMarshallingUtils.encodeEventHandlerType(handler.getType())))
                  {
                     JsonObject boundaryEventJson = toBoundaryEventJson(handler,
                           boundaryEventHostSymbol);
                     eventSymbols.add(
                           boundaryEventJson.get(ModelerConstants.OID_PROPERTY)
                                 .getAsString(), boundaryEventJson);
                  }
               }
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

            // Annotations

            JsonObject annotationSymbolsJson = new JsonObject();

            laneSymbolJson.add(ModelerConstants.ANNOTATION_SYMBOLS, annotationSymbolsJson);

            for (AnnotationSymbolType annotationSymbol : laneSymbol.getAnnotationSymbol())
            {
               annotationSymbolsJson.add(
                     String.valueOf(annotationSymbol.getElementOid()),
                     toAnnotationSymbolJson(annotationSymbol));
            }
         }

         JsonObject connectionsJson = new JsonObject();
         diagramJson.add(ModelerConstants.CONNECTIONS_PROPERTY, connectionsJson);

         // Data Mappings

         for (DataMappingConnectionType dataMappingConnection : poolSymbol.getDataMappingConnection())
         {
            JsonObject connectionJson = toDataMappingConnectionType(dataMappingConnection);
            if (hasNotJsonNull(connectionJson, ModelerConstants.MODEL_ELEMENT_PROPERTY))
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
            if (hasNotJsonNull(connectionJson, ModelerConstants.MODEL_ELEMENT_PROPERTY))
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

            if ( !activity.getImplementation().equals(
                  ActivityImplementationType.SUBPROCESS_LITERAL))
            {
               activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE,
                     ModelerConstants.TASK_ACTIVITY);
               mapTaskType(activity, activityJson);
            }
            else
            {
               activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE,
                     activity.getImplementation().getLiteral());
            }

            activityJson.addProperty(ModelerConstants.ACTIVITY_IS_ABORTABLE_BY_PERFORMER,
                  activity.isAllowsAbortByPerformer());
            activityJson.addProperty(ModelerConstants.ACTIVITY_IS_HIBERNATED_ON_CREATION,
                  activity.isHibernateOnCreation());

            activityJson.addProperty(
                  ModelerConstants.PARTICIPANT_FULL_ID,
                  getModelBuilderFacade().createFullId(
                        ModelUtils.findContainingModel(activity), activity.getPerformer()));

            ProcessDefinitionType implementationProcess = getImplementationProcess(activity);
            if (implementationProcess != null)
            {
               activityJson.addProperty(
                     ModelerConstants.SUBPROCESS_ID,
                     getModelBuilderFacade().createFullId(
                           ModelUtils.findContainingModel(implementationProcess),
                           implementationProcess));

               String mode = null;
               switch (activity.getSubProcessMode())
               {
               case SYNC_SHARED_LITERAL:
                  mode = ModelerConstants.SYNC_SHARED_KEY;
                  break;
               case SYNC_SEPARATE_LITERAL:
                  mode = ModelerConstants.SYNC_SEPARATE_KEY;
                  break;
               case ASYNC_SEPARATE_LITERAL:
                  mode = ModelerConstants.ASYNC_SEPARATE_KEY;
                  break;
               default:
                  mode = ModelerConstants.SYNC_SHARED_KEY;
               }
               activityJson.addProperty(ModelerConstants.SUBPROCESS_MODE_PROPERTY, mode);
            }
            else
            {
               ApplicationType application = getApplication(activity);
               if (application != null)
               {
                  activityJson.addProperty(
                        ModelerConstants.APPLICATION_FULL_ID_PROPERTY,
                        getModelBuilderFacade().createFullId(
                              ModelUtils.findContainingModel(application), application));
               }
            }
         }
      }
      return activityJson;
   }

   /**
    *
    * @param activity
    * @param activityJson
    */
   public void mapTaskType(ActivityType activity, JsonObject activityJson)
   {
      ModelBuilderFacade builder = getModelBuilderFacade();
      Object taskTypeAttribute = builder.getAttribute(activity, ModelerConstants.TASK_TYPE);
      String taskType = builder.getAttributeValue(taskTypeAttribute);

      if (taskType == null)
      {
         taskType = ModelerConstants.SERVICE_TASK_KEY;
         switch (activity.getImplementation())
         {
         case ROUTE_LITERAL:
            taskType = ModelerConstants.NONE_TASK_KEY;
            break;
         case MANUAL_LITERAL:
            taskType = ModelerConstants.MANUAL_TASK_KEY;
            break;
         case SUBPROCESS_LITERAL:
            taskType = ModelerConstants.SUBPROCESS_TASK_KEY;
            break;
         case APPLICATION_LITERAL:
            ApplicationType application = getApplication(activity);
            if (application != null)
            {
               if (application.isInteractive())
               {
                  taskType = ModelerConstants.USER_TASK_KEY;
               }
               else
               {
                  ApplicationTypeType applicationType = application.getType();
                  String typeId = applicationType.getId();
                  if (typeId.equals("messageTransformationBean")
               		   || typeId.equals("camelSpringProducerApplication"))
                  {
                     taskType = ModelerConstants.SCRIPT_TASK_KEY;
                  }
                  else if (typeId.equals("rulesEngineBean"))
                  {
                     taskType = ModelerConstants.RULE_TASK_KEY;
                  }
                  else if (typeId.equals("jms")
                        && !applicationType.isSynchronous())
                  {
                     taskType = ModelerConstants.RECEIVE_TASK_KEY;
                  }
               }
            }
            break;
         }
      }
      activityJson.addProperty(ModelerConstants.TASK_TYPE, taskType);
   }

   private ProcessDefinitionType getImplementationProcess(ActivityType activity)
   {
      if (activity.getImplementation().equals(
            ActivityImplementationType.SUBPROCESS_LITERAL)) {
         if (ModelUtils.findContainingModel(activity) == null)
         {
            // (fh) special case if activity was deleted
            IdRef ref = activity.getExternalRef();
            if (ref != null)
            {
               ExternalPackage pack = ref.getPackageRef();
               if (pack != null)
               {
                  ModelBuilderFacade builder = getModelBuilderFacade();
                  ModelType model = builder.findModel(pack.getHref());
                  return builder.findProcessDefinition(model, ref.getRef());
               }
            }
         }
         return activity.getImplementationProcess();
      }

      return null;
   }

   private ApplicationType getApplication(ActivityType activity)
   {
      if (ModelUtils.findContainingModel(activity) == null)
      {
         // (fh) special case if activity was deleted
         IdRef ref = activity.getExternalRef();
         if (ref != null)
         {
            ExternalPackage pack = ref.getPackageRef();
            if (pack != null)
            {
               ModelBuilderFacade builder = getModelBuilderFacade();
               ModelType model = builder.findModel(pack.getHref());
               return builder.findApplication(model, ref.getRef());
            }
         }
      }
      return activity.getApplication();
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

      ActivityType activity = activitySymbol.getActivity();

      if (null != activity)
      {
         // set default height and width if not defined
         int width = activitySymbol.getWidth();
         int height = activitySymbol.getHeight();
         if ( -1 == width)
         {
            if (activity.getId().toLowerCase().startsWith("gateway"))
            {
               width = ModelerConstants.GATEWAY_SYMBOL_DEFAULT_WIDTH;
            }
            else
            {
               width = ModelerConstants.ACTIVITY_SYMBOL_DEFAULT_WIDTH;
            }
         }
         if ( -1 == height)
         {
            if (activity.getId().toLowerCase().startsWith("gateway"))
            {
               height = ModelerConstants.GATEWAY_SYMBOL_DEFAULT_HEIGHT;
            }
            else
            {
               height = ModelerConstants.ACTIVITY_SYMBOL_DEFAULT_HEIGHT;
            }
         }

         activitySymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, width);
         activitySymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY, height);

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

      eventSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.EVENT_SYMBOL);
      eventSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            startEventSymbol.getElementOid());
      // TODO check this math
      eventSymbolJson.addProperty(ModelerConstants.X_PROPERTY, startEventSymbol.getXPos()
            + laneOffsetX);
      eventSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, startEventSymbol.getYPos()
            + laneOffsetY);

      // set default height and width if not defined

      int width = startEventSymbol.getWidth();

      if ( -1 == width)
      {
         width = ModelerConstants.EVENT_ICON_WIDTH;
      }

      eventSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, width);

      JsonObject eventJson = null;
      TriggerType trigger = (TriggerType) startEventSymbol.getModelElement();

      if (trigger != null)
      {
         eventJson = toEventJson(trigger);
      }
      else
      {
         eventJson = new JsonObject();

         eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);
         eventJson.addProperty(ModelerConstants.IMPLEMENTATION_PROPERTY, "none");
         eventJson.add(ModelerConstants.ATTRIBUTES_PROPERTY, new JsonObject());
      }

      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);
      eventJson.addProperty(ModelerConstants.EVENT_TYPE_PROPERTY,
            ModelerConstants.START_EVENT);
      eventJson.addProperty(ModelerConstants.INTERRUPTING_PROPERTY, true);

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

      /*int laneOffsetX = 0;
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
      }*/

      eventSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            endEventSymbol.getElementOid());

      setNodeSymbolCoordinates(eventSymbolJson, endEventSymbol);

      // set default height and width if not defined
      int width = endEventSymbol.getWidth();
      if ( -1 == width)
      {
         width = ModelerConstants.EVENT_ICON_WIDTH;
      }

      eventSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, width);

      JsonObject eventJson = new JsonObject();
      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

      eventSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.EVENT_SYMBOL);

      eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);
      eventJson.addProperty(ModelerConstants.EVENT_TYPE_PROPERTY,
            ModelerConstants.STOP_EVENT);

      eventJson.addProperty(ModelerConstants.THROWING_PROPERTY, true);
      eventJson.addProperty(ModelerConstants.INTERRUPTING_PROPERTY, true);

      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(endEventSymbol);
      if (null != hostActivity)
      {
         eventJson.addProperty(ModelerConstants.ID_PROPERTY, hostActivity.getId());
         eventJson.addProperty(ModelerConstants.NAME_PROPERTY, hostActivity.getName());
         loadDescription(eventJson, hostActivity);
         loadAttributes(hostActivity, eventJson);
      }

      return eventSymbolJson;
   }

   /**
    * Generates a transfer object based on an explicit intermediate event symbol. Knows
    * how to handle both intermediate and boundary events.
    *
    * @param eventSymbol
    *           the defining intermediate event symbol
    * @return the transfer object
    */
   public JsonObject toIntermediateEventJson(IntermediateEventSymbol eventSymbol)
   {
      JsonObject eventSymbolJson = new JsonObject();

      eventSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.EVENT_SYMBOL);
      eventSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            eventSymbol.getElementOid());

      // set default height and width if not defined
      int width = eventSymbol.getWidth();
      if ( -1 == width)
      {
         width = ModelerConstants.EVENT_ICON_WIDTH;
      }
      eventSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, width);

      setNodeSymbolCoordinates(eventSymbolJson, eventSymbol);

      // TODO more attributes
      JsonObject eventJson = new JsonObject();
      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(eventSymbol);
      EventHandlerType eventHandler = null;
      if (null != hostActivity)
      {
         JsonObject config = EventMarshallingUtils.getEventHostingConfig(hostActivity,
               eventSymbol, jsonIo);
         if (null != config)
         {
            eventJson = config;
         }

         if (hasNotJsonNull(eventJson, EventMarshallingUtils.PRP_EVENT_HANDLER_ID))
         {
            // marshal properties from defining event handler, if possible
            eventHandler = ModelUtils.findIdentifiableElement(
                  hostActivity.getEventHandler(),
                  extractString(eventJson, EventMarshallingUtils.PRP_EVENT_HANDLER_ID));
            if (null != eventHandler)
            {
               toEventJson(eventHandler, eventJson);
            }

            eventJson.remove(EventMarshallingUtils.PRP_EVENT_HANDLER_ID);
         }

         eventJson.addProperty(ModelerConstants.EVENT_TYPE_PROPERTY,
               ModelerConstants.INTERMEDIATE_EVENT);
         if (!EventMarshallingUtils.isIntermediateEventHost(hostActivity))
         {
            // actually a boundary event
            eventJson.addProperty(ModelerConstants.BINDING_ACTIVITY_UUID,
                  hostActivity.getId());
         }
      }

      eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);

      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

      return eventSymbolJson;
   }

   /**
    * Generates a transfer object for a event handler that has no explicit intermediate
    * event symbol, guessing a reasonable location of the made up symbol.
    *
    * @param eventHandler
    *           the defining event handler
    * @param hostActivitySymbol
    *           the symbol of the hosting activity, giving hint to where to locate the
    *           made up event symbol
    * @return the transfer object
    */
   public JsonObject toBoundaryEventJson(EventHandlerType eventHandler,
         ActivitySymbolType hostActivitySymbol)
   {
      JsonObject eventSymbolJson = new JsonObject();

      eventSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.EVENT_SYMBOL);
      // HACK use event handler element OID for symbol, ensure for the element itself a
      // UUID is being used
      eventSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            eventHandler.getElementOid());

      // guess coordinates relative to the hosting activity's symbol
      // TODO handle multiple events per activity, avoid collisions with explicit
      // intermediate event symbols
      eventSymbolJson.addProperty(ModelerConstants.X_PROPERTY,
            hostActivitySymbol.getXPos() + (hostActivitySymbol.getWidth() - 24));
      eventSymbolJson.addProperty(ModelerConstants.Y_PROPERTY,
            hostActivitySymbol.getYPos() + (hostActivitySymbol.getHeight() - 12));
      eventSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, 24);
      eventSymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY, 24);

      JsonObject eventJson = new JsonObject();
      toEventJson(eventHandler, eventJson);
      eventJson.addProperty(ModelerConstants.EVENT_TYPE_PROPERTY,
            ModelerConstants.INTERMEDIATE_EVENT);
      eventJson.addProperty(ModelerConstants.BINDING_ACTIVITY_UUID,
            hostActivitySymbol.getActivity().getId());

      eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

      return eventSymbolJson;
   }

   public JsonObject toEventJson(EventHandlerType eventHandler, JsonObject eventJson)
   {
      eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);

      eventJson.addProperty(ModelerConstants.ID_PROPERTY, eventHandler.getId());
      eventJson.addProperty(ModelerConstants.NAME_PROPERTY, eventHandler.getName());
      eventJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(eventHandler));
      eventJson.addProperty(ModelerConstants.OID_PROPERTY, eventHandler.getElementOid());
      ModelType model = ModelUtils.findContainingModel(eventHandler);
      eventJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(model));
      setContainingModelIdProperty(eventJson, eventHandler);

      // TODO This may changes

      loadDescription(eventJson, eventHandler);
      loadAttributes(eventHandler, eventJson);

      eventJson.addProperty(ModelerConstants.LOG_HANDLER_PROPERTY,
            eventHandler.isLogHandler());
      eventJson.addProperty(ModelerConstants.EVENT_CLASS_PROPERTY,
            EventMarshallingUtils.encodeEventHandlerType(eventHandler.getType()));
      //eventJson.addProperty(ModelerConstants.THROWING_PROPERTY,
      //      EventMarshallingUtils.encodeIsThrowingEvent(eventHandler.getType()));
      eventJson.addProperty(ModelerConstants.INTERRUPTING_PROPERTY,
            EventMarshallingUtils.encodeIsInterruptingEvent(eventHandler));

      JsonArray parameterMappingsJson = new JsonArray();

      eventJson.add(ModelerConstants.PARAMETER_MAPPINGS_PROPERTY, parameterMappingsJson);

      for (AccessPointType accessPoint : eventHandler.getAccessPoint())
      {
         JsonObject parameterMappingJson = new JsonObject();

         parameterMappingsJson.add(parameterMappingJson);
         parameterMappingJson.addProperty(ModelerConstants.ID_PROPERTY,
               accessPoint.getId());
         parameterMappingJson.addProperty(ModelerConstants.NAME_PROPERTY,
               accessPoint.getName());

         if (accessPoint.getType() != null)
         {
            parameterMappingJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  accessPoint.getType().getId());
         }

         if (accessPoint.getDirection() != null)
         {
            parameterMappingJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                  accessPoint.getDirection().getLiteral());
         }

         loadAttributes(accessPoint, parameterMappingJson);
      }
      
      return eventJson;
   }

   /**
    *
    * @param event
    * @return
    */
   public JsonObject toEventJson(TriggerType event)
   {
      JsonObject eventJson = new JsonObject();

      eventJson.addProperty(ModelerConstants.ID_PROPERTY, event.getId());
      eventJson.addProperty(ModelerConstants.NAME_PROPERTY, event.getName());
      eventJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(event));
      eventJson.addProperty(ModelerConstants.OID_PROPERTY, event.getElementOid());
      ModelType model = ModelUtils.findContainingModel(event);
      eventJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(model));
      setContainingModelIdProperty(eventJson, event);

      eventJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.EVENT_KEY);

      // TODO This may changes

      loadDescription(eventJson, event);
      loadAttributes(event, eventJson);

      if (event.getType() != null)
      {
         eventJson.addProperty(ModelerConstants.IMPLEMENTATION_PROPERTY, event.getType()
               .getId());

         if (event.getType().getId().equals("manual"))
         {
            eventJson.get(ModelerConstants.ATTRIBUTES_PROPERTY)
                  .getAsJsonObject()
                  .addProperty("carnot:engine:integration::overlay", "manualTrigger");
         }
         else if (event.getType().getId().equals("scan"))
         {
            eventJson.get(ModelerConstants.ATTRIBUTES_PROPERTY)
                  .getAsJsonObject()
                  .addProperty("carnot:engine:integration::overlay", "scanEvent");
         }
      }

      String participantFullID = null;
      try
      {
         if (model != null)
         {
            participantFullID = getModelBuilderFacade().createFullId(
                  model,
                  getModelBuilderFacade().findParticipant(
                        model,
                        getModelBuilderFacade().getAttributeValue(
                              getModelBuilderFacade().getAttribute(event,
                                    PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT))));
         }
      }
      catch (ObjectNotFoundException ex)
      {
         // No participant found - FULL ID stays null
      }

      eventJson.addProperty(ModelerConstants.PARTICIPANT_FULL_ID, participantFullID);

      // Load starting Participant
      // TODO The code below is wrong as full references are not loaded
      // TODO May be only loaded for None Start Events

      // eventJson.addProperty(ModelerConstants.PARTICIPANT_FULL_ID,
      // getModelBuilderFacade().getAttributeValue(getModelBuilderFacade().getAttribute(event,
      // PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT)));

      // Load BPMN attributes

      Object attribute = getModelBuilderFacade().getAttribute(event, "eventClass");

      // TODO We need a better convenience function to access attributes

      if (attribute != null)
      {
         eventJson.addProperty(ModelerConstants.EVENT_CLASS_PROPERTY,
               getModelBuilderFacade().getAttributeValue(attribute));
      }
      else
      {
         if (event.getType().getId().equals("scan"))
         {
            eventJson.addProperty(ModelerConstants.EVENT_CLASS_PROPERTY,
                  ModelerConstants.MESSAGE_EVENT_CLASS_KEY);
         }
         else
         {
            eventJson.addProperty(ModelerConstants.EVENT_CLASS_PROPERTY,
                  ModelerConstants.NONE_EVENT_CLASS_KEY);
         }
      }

      // TODO Validate defaults (e.g. Start Events cannot be throwing
      eventJson.addProperty(ModelerConstants.THROWING_PROPERTY,
            getModelBuilderFacade().getBooleanAttribute(event, "throwing"));
      eventJson.addProperty(ModelerConstants.INTERRUPTING_PROPERTY,
            getModelBuilderFacade().getBooleanAttribute(event, "interrupting"));

      JsonArray parameterMappingsJson = new JsonArray();

      eventJson.add(ModelerConstants.PARAMETER_MAPPINGS_PROPERTY, parameterMappingsJson);

      for (AccessPointType accessPoint : event.getAccessPoint())
      {
         JsonObject parameterMappingJson = new JsonObject();

         parameterMappingsJson.add(parameterMappingJson);
         parameterMappingJson.addProperty(ModelerConstants.ID_PROPERTY,
               accessPoint.getId());
         parameterMappingJson.addProperty(ModelerConstants.NAME_PROPERTY,
               accessPoint.getName());

         if (accessPoint.getType() != null)
         {
            parameterMappingJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  accessPoint.getType().getId());
         }

         if (accessPoint.getDirection() != null)
         {
            parameterMappingJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                  accessPoint.getDirection().getLiteral());
         }

         loadAttributes(accessPoint, parameterMappingJson);

         for (ParameterMappingType parameterMapping : event.getParameterMapping())
         {
            if (accessPoint.getId().equals(parameterMapping.getParameter()))
            {
               String fullID = getDataFullID(model, parameterMapping.getData());
               parameterMappingJson.addProperty(
                     ModelerConstants.DATA_FULL_ID_PROPERTY,
                     fullID);
               parameterMappingJson.addProperty(ModelerConstants.DATA_PATH_PROPERTY,
                     parameterMapping.getDataPath());

               break;
            }
         }
      }

      return eventJson;
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
         dataJson.addProperty(ModelerConstants.ID_PROPERTY, data.getId());
         dataJson.addProperty(ModelerConstants.TYPE_PROPERTY, "data");
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

         dataJson.addProperty(ModelerConstants.EXTERNAL_REFERENCE_PROPERTY,
               this.getModelBuilderFacade().isExternalReference(data));

         if (getModelBuilderFacade().isExternalReference(data))
         {
            ModelType referencedModel = null;
            URI proxyUri = ((InternalEObject) data).eProxyURI();
            referencedModel = getModelByProxyURI(model, proxyUri);
            if (referencedModel != null)
            {
               String dataId = getModelBuilderFacade().createFullId(referencedModel, data);
               dataJson.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY, dataId);
            }
         }

         if (null != data.getType()
               && data.getType()
                     .getId()
                     .equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
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
         else if (null != data.getType()
               && data.getType().getId().equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
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
               else
               {
                  String typeDeclarationId = AttributeUtil.getAttributeValue(data,
                        "carnot:engine:dms:resourceMetadataSchema");

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
         else if (null != data.getType()
               && data.getType().getId().equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
            String type = AttributeUtil.getAttributeValue(data, CarnotConstants.TYPE_ATT);
            dataJson.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY, type);
         }
         else if (null != data.getType())
         {
            dataJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY, data.getType()
                  .getId());
         }
      }

      return dataJson;
   }

   private ModelType getModelByProxyURI(ModelType model, URI proxyUri)
   {
      ModelType referencedModel = null;
      if (model != null && model.getConnectionManager() != null)
      {
         EObject connectionObject = model.getConnectionManager().find(
               proxyUri.scheme() + "://" + proxyUri.authority() + "/");
         if (connectionObject != null)
         {
            referencedModel = (ModelType) Reflect.getFieldValue(connectionObject,
                  "eObject");
         }
      }
      return referencedModel;
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

      if (role.getCardinality() > 0)
      {
         roleJson.addProperty(ModelerConstants.CARDINALITY, role.getCardinality());
      }
      else
      {
         roleJson.addProperty(ModelerConstants.CARDINALITY, "");
      }

      if (model != null)
      {
         List<OrganizationType> parentOrgs = ModelBuilderFacade.getParentOrganizations(
               model, role);
         if (parentOrgs.size() > 0)
         {
            // TODO - add array of orgs?
            OrganizationType org = parentOrgs.get(0);
            roleJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper().getUUID(org));
         }
         else
         {
            OrganizationType parentOrg = getOrganizationForTeamLeader(role);
            if (null != parentOrg)
            {
               roleJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                     eObjectUUIDMapper().getUUID(parentOrg));
               roleJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                     ModelerConstants.TEAM_LEADER_TYPE_KEY);
            }
         }

         roleJson.addProperty(ModelerConstants.MODEL_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(model));
         roleJson.addProperty(ModelerConstants.MODEL_ID_PROPERTY, model.getId());
      }

      roleJson.addProperty(ModelerConstants.EXTERNAL_REFERENCE_PROPERTY,
            this.getModelBuilderFacade().isExternalReference(role));

      if (getModelBuilderFacade().isExternalReference(role))
      {
         URI proxyUri = ((InternalEObject) role).eProxyURI();
         ModelType referencedModel = getModelByProxyURI(model, proxyUri);

         if (referencedModel != null)
         {
            String roleId = getModelBuilderFacade().createFullId(referencedModel, role);
            roleJson.addProperty(ModelerConstants.PARTICIPANT_FULL_ID, roleId);
         }
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
      String bindingDataFullID = getDataFullID(
            ModelUtils.findContainingModel(conditionalPerformer.getData()),
            conditionalPerformer.getData());
      if (null != bindingDataFullID)
      {
         conditionalPerformerJson.addProperty(
               ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY, bindingDataFullID);
      }

      conditionalPerformerJson.addProperty(ModelerConstants.BINDING_DATA_PATH_PROPERTY,
            conditionalPerformer.getDataPath());
      conditionalPerformerJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper().getUUID(conditionalPerformer));

      ModelType model = ModelUtils.findContainingModel(conditionalPerformer);

      if (null != model)
      {
         List<OrganizationType> parentOrgs = ModelBuilderFacade.getParentOrganizations(
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

      conditionalPerformerJson.addProperty(ModelerConstants.EXTERNAL_REFERENCE_PROPERTY,
            this.getModelBuilderFacade().isExternalReference(conditionalPerformer));

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
         List<OrganizationType> parentOrgs = ModelBuilderFacade.getParentOrganizations(
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
         orgJson.addProperty(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY,
               ModelerConstants.TO_BE_DEFINED);
      }

      orgJson.addProperty(ModelerConstants.EXTERNAL_REFERENCE_PROPERTY,
            this.getModelBuilderFacade().isExternalReference(org));

      loadDescription(orgJson, org);
      loadAttributes(org, orgJson);

      return orgJson;
   }

   /**
    * For testing population of implicit Access Points for Java-typed applications.
    *
    * TODO Remove
    *
    * @param serializabe
    * @return
    */
   public String test(Serializable serializabe)
   {
      return null;
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

      applicationJson.addProperty(ModelerConstants.INTERACTIVE_PROPERTY,
            application.isInteractive());

      JsonObject contextsJson = new JsonObject();

      applicationJson.add(ModelerConstants.CONTEXTS_PROPERTY, contextsJson);

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

         for (ContextType context : application.getContext())
         {
            JsonObject contextJson = new JsonObject();

            contextsJson.add(context.getType().getId(), contextJson);

            loadAttributes(context, contextJson);

            JsonArray accessPointsJson = new JsonArray();

            contextJson.add(ModelerConstants.ACCESS_POINTS_PROPERTY, accessPointsJson);

            for (AccessPointType accessPoint : context.getAccessPoint())
            {
               JsonObject accessPointJson = new JsonObject();

               accessPointsJson.add(accessPointJson);
               accessPointJson.addProperty(ModelerConstants.ID_PROPERTY,
                     accessPoint.getId());
               accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
                     accessPoint.getName());

               if (accessPoint.getType() != null)
               {
                  accessPointJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                        accessPoint.getType().getId());
               }

               if (accessPoint.getDirection() != null)
               {
                  accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                        accessPoint.getDirection().getLiteral());
               }

               loadAttributes(accessPoint, accessPointJson);
            }
         }
      }

      // Add top level access points to context "application"

      JsonObject applicationContextJson = null;
      JsonArray accessPointsJson = null;

      if (hasNotJsonNull(contextsJson, ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY))
      {
         applicationContextJson = contextsJson.get(
               ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY).getAsJsonObject();
      }
      else
      {
         applicationContextJson = new JsonObject();

         contextsJson.add(ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY,
               applicationContextJson);

         accessPointsJson = new JsonArray();

         applicationContextJson.add(ModelerConstants.ACCESS_POINTS_PROPERTY,
               accessPointsJson);
      }

      for (AccessPointType accessPoint : application.getAccessPoint())
      {
         JsonObject accessPointJson = new JsonObject();

         accessPointsJson.add(accessPointJson);
         accessPointJson.addProperty(ModelerConstants.ID_PROPERTY, accessPoint.getId());
         accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
               accessPoint.getName());

         if (accessPoint.getType() != null)
         {
            accessPointJson.addProperty(ModelerConstants.DATA_TYPE_PROPERTY,
                  accessPoint.getType().getId());
         }

         if (accessPoint.getDirection() != null)
         {
            accessPointJson.addProperty(ModelerConstants.DIRECTION_PROPERTY,
                  accessPoint.getDirection().getLiteral());
         }

         loadAttributes(accessPoint, accessPointJson);
      }

      // Retrieve implicit Java Access Points

      if (isSupportedJavaApplicationType(application))
      {
         if (hasNotJsonNull(contextsJson, ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY))
         {
            applicationContextJson = contextsJson.get(
                  ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY).getAsJsonObject();
         }
         else
         {
            applicationContextJson = new JsonObject();

            contextsJson.add(ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY,
                  applicationContextJson);

            accessPointsJson = new JsonArray();

            applicationContextJson.add(ModelerConstants.ACCESS_POINTS_PROPERTY,
                  accessPointsJson);
         }
         
         // TODO: (fh) constructor ?

         String methodName = getModelBuilderFacade().getAttributeValue(
               getModelBuilderFacade().getAttribute(application,
                     PredefinedConstants.METHOD_NAME_ATT));
         String className = null;

         if (application.getType()
               .getId()
               .equals(PredefinedConstants.SESSIONBEAN_APPLICATION))
         {
         // TODO: Which is the relevant attribute here?
            className = getModelBuilderFacade().getAttributeValue(
                  getModelBuilderFacade().getAttribute(application,
                        PredefinedConstants.CLASS_NAME_ATT));

         }
         else
         {
            className = getModelBuilderFacade().getAttributeValue(
                  getModelBuilderFacade().getAttribute(application,
                        PredefinedConstants.CLASS_NAME_ATT));
         }

         Method method = ClassesHelper.getMethodBySignature(
               classLoaderProvider().classLoader(), className, methodName);

         ClassesHelper.addParameterAccessPoints(accessPointsJson, method);
         ClassesHelper.addReturnTypeAccessPoint(accessPointsJson, method);

      }
      return applicationJson;
   }

   private static List<String> pojoTypes = Arrays.asList(new String[] {
         PredefinedConstants.PLAINJAVA_APPLICATION,
         PredefinedConstants.SESSIONBEAN_APPLICATION,
         PredefinedConstants.SPRINGBEAN_APPLICATION
   });
   
   private boolean isSupportedJavaApplicationType(ApplicationType application)
   {
      ApplicationTypeType type = application.getType();
      return type != null && pojoTypes.contains(type.getId());
   }

   /**
    *
    * @param annotationSymbol
    * @return
    */
   public JsonObject toAnnotationSymbolJson(AnnotationSymbolType annotationSymbol)
   {
      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (annotationSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) annotationSymbol.eContainer()
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

      JsonObject annotationSymbolJson = new JsonObject();

      annotationSymbolJson.addProperty(ModelerConstants.OID_PROPERTY,
            annotationSymbol.getElementOid());
      annotationSymbolJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.ANNOTATION_SYMBOL);
      annotationSymbolJson.addProperty(ModelerConstants.X_PROPERTY,
            annotationSymbol.getXPos() + laneOffsetX);
      annotationSymbolJson.addProperty(ModelerConstants.Y_PROPERTY,
            annotationSymbol.getYPos() + laneOffsetY);

      // set default height and width if not defined
      int width = annotationSymbol.getWidth();
      int height = annotationSymbol.getHeight();
      if ( -1 == width)
      {
         width = ModelerConstants.ANNOTATION_SYMBOL_DEFAULT_WIDTH;
      }
      if ( -1 == height)
      {
         height = ModelerConstants.ANNOTATION_SYMBOL_DEFAULT_HEIGHT;
      }
      annotationSymbolJson.addProperty(ModelerConstants.WIDTH_PROPERTY, width);
      annotationSymbolJson.addProperty(ModelerConstants.HEIGHT_PROPERTY, height);

      if (null != annotationSymbol.getText())
      {
         annotationSymbolJson.addProperty(ModelerConstants.CONTENT_PROPERTY,
               (String) annotationSymbol.getText().getMixed().get(0).getValue());
      }
      else
      {
         annotationSymbolJson.addProperty(ModelerConstants.CONTENT_PROPERTY, "");
      }

      return annotationSymbolJson;
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
      connectionJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.DATA_FLOW_CONNECTION_LITERAL);

      connectionJson.addProperty(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(dataMappingConnection.getSourceAnchor()));
      connectionJson.addProperty(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
            mapAnchorOrientation(dataMappingConnection.getTargetAnchor()));

      JsonObject dataFlowJson = new JsonObject();

      connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, dataFlowJson);

      DataType data = null != dataMappingConnection.getDataSymbol()
            ? dataMappingConnection.getDataSymbol().getData()
            : null;

      if (null != data && null != dataMappingConnection.getActivitySymbol())
      {
         ActivityType activity = dataMappingConnection.getActivitySymbol().getActivity();

         // Find all data mappings between the data and the activity connected by the
         // connection

         for (DataMappingType dataMapping : activity.getDataMapping())
         {
            if (dataMapping.getData().getId().equals(data.getId()))
            {
               if (hasNotJsonNull(dataFlowJson, ModelerConstants.ID_PROPERTY))
               {
                  if ( !dataFlowJson.get(ModelerConstants.ID_PROPERTY)
                        .getAsString()
                        .equals(dataMapping.getId()))
                  {
                     // TODO Other data mapping
                     continue;
                  }
               }
               else
               {
                  // Set ID etc. for first data mapping between activity and data found

                  dataFlowJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                        ModelerConstants.DATA_FLOW_LITERAL);
                  dataFlowJson.addProperty(ModelerConstants.ID_PROPERTY,
                        dataMapping.getId());
                  dataFlowJson.addProperty(ModelerConstants.NAME_PROPERTY,
                        dataMapping.getName());
                  dataFlowJson.addProperty(ModelerConstants.OID_PROPERTY,
                        dataMapping.getElementOid());
               }

               if (dataMapping.getDirection().equals(DirectionType.IN_LITERAL))
               {
                  dataFlowJson.add(ModelerConstants.INPUT_DATA_MAPPING_PROPERTY,
                        toDataMappingJson(dataMapping));
               }
               else
               {
                  dataFlowJson.add(ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY,
                        toDataMappingJson(dataMapping));
               }
            }
         }

         // TODO Review

         if (dataFlowJson != null)
         {
            if (hasNotJsonNull(dataFlowJson,
                  ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY))
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

            if (hasNotJsonNull(dataFlowJson, ModelerConstants.INPUT_DATA_MAPPING_PROPERTY))
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

            dataFlowJson.addProperty(
                  ModelerConstants.DATA_FULL_ID_PROPERTY,
                  getModelBuilderFacade().createFullId(
                        ModelUtils.findContainingModel(data), data));
            dataFlowJson.addProperty(ModelerConstants.ACTIVITY_ID_PROPERTY,
                  activity.getId());
            connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, dataFlowJson);
         }
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
      connectionJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.CONTROL_FLOW_CONNECTION_LITERAL);

      IFlowObjectSymbol sourceActivitySymbol = transitionConnection.getSourceActivitySymbol();
      IFlowObjectSymbol targetActivitySymbol = transitionConnection.getTargetActivitySymbol();

      if (transitionConnection.getTransition() != null)
      {
         TransitionType transition = transitionConnection.getTransition();

         modelElementJson = toTransitionJson(transition);

         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);


         DiagramType containingDiagram = findContainingDiagram(transitionConnection);

         if (null != containingDiagram)
         {
            connectionJson.addProperty(
                  ModelerConstants.FROM_MODEL_ELEMENT_OID,
                  /*resolveSymbolAssociatedWithActivity(transition.getFrom(), containingDiagram)*/
                  sourceActivitySymbol.getElementOid());
         }

         // TODO Hack to identify gateways

         if (isGatwayHost(transition.getFrom()))
         {
            connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                  ModelerConstants.GATEWAY);
         }
         else if (/*EventMarshallingUtils.isIntermediateEventHost(transition.getFrom())
               || EventMarshallingUtils.isEndEventHost(transition.getFrom())*/
               sourceActivitySymbol instanceof IntermediateEventSymbol
               || sourceActivitySymbol instanceof EndEventSymbol)
         {
            connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                  ModelerConstants.EVENT_KEY);
         }
         else
         {
            connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_TYPE,
                  ModelerConstants.ACTIVITY_KEY);
         }


         if (containingDiagram != null)
         {
            connectionJson.addProperty(
                  ModelerConstants.TO_MODEL_ELEMENT_OID,
                  /*resolveSymbolAssociatedWithActivity(transition.getTo(), containingDiagram)*/
                  targetActivitySymbol.getElementOid());
         }

         if (isGatwayHost(transition.getTo()))
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.GATEWAY);
            // TODO - Is this code required. Causes issue while changing anchor Point
            /*
             * connectionJson.remove(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)
             * ; connectionJson.addProperty(
             * ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
             * ModelerConstants.NORTH_KEY);
             */
         }
         else if (/*EventMarshallingUtils.isIntermediateEventHost(transition.getTo())
               || EventMarshallingUtils.isEndEventHost(transition.getTo())*/
               targetActivitySymbol instanceof IntermediateEventSymbol
               || targetActivitySymbol instanceof EndEventSymbol)
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.EVENT_KEY);
         }
         else
         {
            connectionJson.addProperty(ModelerConstants.TO_MODEL_ELEMENT_TYPE,
                  ModelerConstants.ACTIVITY_KEY);
         }

      }
      else if (transitionConnection.getSourceNode() instanceof StartEventSymbol
            && targetActivitySymbol instanceof ActivitySymbolType)
      {
         modelElementJson = new JsonObject();
         String activityId = ((ActivitySymbolType) targetActivitySymbol).getActivity()
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
               targetActivitySymbol.getElementOid());
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
      else if (transitionConnection.getTargetNode() instanceof EndEventSymbol
            && sourceActivitySymbol instanceof ActivitySymbolType)
      {
         modelElementJson = new JsonObject();
         String activityId = ((ActivitySymbolType) sourceActivitySymbol).getActivity().getId();
         connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);
         modelElementJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONTROL_FLOW_LITERAL);
         modelElementJson.addProperty(ModelerConstants.ID_PROPERTY, activityId + "-"
               + String.valueOf(transitionConnection.getTargetNode().getElementOid()));
         connectionJson.addProperty(ModelerConstants.FROM_MODEL_ELEMENT_OID,
               sourceActivitySymbol.getElementOid());
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

   private static boolean isGatwayHost(ActivityType activity)
   {
      return activity != null && activity.getId().toLowerCase().startsWith("gateway");
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
      controlFlowJson.addProperty(ModelerConstants.NAME_PROPERTY, transition.getName());

      if (null != transition.getCondition()
            && transition.getCondition().equals("CONDITION"))
      {
         String expression = transition.getExpression() == null ? null
               : ModelUtils.getCDataString(transition.getExpression().getMixed());
         // filter out boundary event conditions
         if (expression != null && expression.trim().startsWith("ON_BOUNDARY_EVENT"))
         {
            expression = null;
         }
         controlFlowJson.addProperty(ModelerConstants.OTHERWISE_PROPERTY, false);
         controlFlowJson.addProperty(ModelerConstants.CONDITION_EXPRESSION_PROPERTY, expression == null ? "" : expression);
      }
      else
      {
         controlFlowJson.addProperty(ModelerConstants.OTHERWISE_PROPERTY, true);
         controlFlowJson.addProperty(ModelerConstants.CONDITION_EXPRESSION_PROPERTY, "");
      }

      controlFlowJson.addProperty(ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY,
            transition.isForkOnTraversal());

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
    * @param dataMapping
    * @return
    */
   public JsonObject toDataMappingJson(DataMappingType dataMapping)
   {
      JsonObject dataMappingJson = new JsonObject();

      if (dataMapping.getApplicationAccessPoint() != null)
      {
         dataMappingJson.addProperty(ModelerConstants.ACCESS_POINT_ID_PROPERTY,
               dataMapping.getApplicationAccessPoint());
         dataMappingJson.addProperty(ModelerConstants.ACCESS_POINT_CONTEXT_PROPERTY,
               dataMapping.getContext());
      }

      dataMappingJson.addProperty(ModelerConstants.DATA_PATH_PROPERTY,
            dataMapping.getDataPath());

      return dataMappingJson;
   }


   /**
    * Returns Models infromation only and skips info about its elements
    * @param model
    * @return
    */
   public JsonObject toModelOnlyJson(ModelType model)
   {
	      JsonObject modelJson = new JsonObject();

	      modelJson.addProperty(ModelerConstants.ID_PROPERTY, model.getId());
	      modelJson.addProperty(ModelerConstants.NAME_PROPERTY, model.getName());
	      modelJson.addProperty(ModelerConstants.UUID_PROPERTY,
	            eObjectUUIDMapper().getUUID(model));
	      modelJson.addProperty(ModelerConstants.FILE_NAME,
	            getModelBuilderFacade().getModelManagementStrategy().getModelFileName(model));
	      modelJson.addProperty(ModelerConstants.FILE_PATH,
	            getModelBuilderFacade().getModelManagementStrategy().getModelFilePath(model));
	      modelJson.addProperty(ModelerConstants.TYPE_PROPERTY, ModelerConstants.MODEL_KEY);
	      modelJson.addProperty(ModelerConstants.DATE_OF_CREATION,
	            getModelBuilderFacade().convertDate(model.getCreated()));
	      modelJson.addProperty(ModelerConstants.DATE_OF_MODIFICATION,
	            getModelBuilderFacade().getModified(model));

	      // Model description
	      if (null != model.getDescription() && model.getDescription().getMixed().size() > 0)
	      {
	         modelJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY,
	               (String) model.getDescription().getMixed().get(0).getValue());
	      }
	      else
	      {
	         modelJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, "");
	      }

	      loadAttributes(model, modelJson);

	      if (model.getDescription() != null)
	      {
	         modelJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY,
	               (String) model.getDescription().getMixed().get(0).getValue());
	      }
	      else
	      {
	         modelJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY, (String) null);
	      }

	      return modelJson;
   }

   /**
    * @param model
    * @return
    */
   public JsonObject toModelJson(ModelType model)
   {
      JsonObject modelJson = toModelOnlyJson(model);

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
         // Role is not in an organisation hierarchy nor a team leader.
         if ( !hasParentParticipant(model, role)
               && (null == getOrganizationForTeamLeader(role)))
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

      for (TypeDeclarationType typeDeclaration : model.getTypeDeclarations()
            .getTypeDeclaration())
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
      List<OrganizationType> parentOrgs = ModelBuilderFacade.getParentOrganizations(
            model, participant);
      if (parentOrgs.size() > 0)
      {
         return true;
      }

      return false;
   }

   /**
    * TODO - is there a better way to do this?
    *
    * Returns the organisation for which the role is a team leader, null otherwise returns
    * null if role is not a team leader in the first place
    *
    * @param participant
    * @return
    */
   private OrganizationType getOrganizationForTeamLeader(IModelParticipant participant)
   {
      ModelType model = ModelUtils.findContainingModel(participant);
      if (null != model)
      {
         EList<OrganizationType> orgs = model.getOrganization();
         for (OrganizationType org : orgs)
         {
            if (null != org.getTeamLead() && org.getTeamLead().equals(participant))
            {
               return org;
            }
         }
      }

      return null;
   }

   /**
    * @param parentJson
    * @param parent
    */
   private void addChildParticipantsJson(JsonObject parentJson, OrganizationType parent)
   {
      EList<ParticipantType> children = parent.getParticipant();
      boolean teamLeadAdded = false;
      JsonArray childrenArray = null;
      if (children.size() > 0)
      {
         childrenArray = new JsonArray();
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
                  childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                        ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY);
               }
               else if (childParticipant instanceof ConditionalPerformerType)
               {
                  childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                        ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY);
               }

               loadDescription(childJson, childParticipant);
               loadAttributes(childParticipant, childJson);
            }
         }
      }

      // Add teamlead as a child role so that it can be shown
      // in the org hierarchy
      if (null != parent.getTeamLead() && !teamLeadAdded)
      {
         JsonObject childJson = new JsonObject();
         if (null == childrenArray)
         {
            childrenArray = new JsonArray();
         }
         childrenArray.add(childJson);
         IModelParticipant childParticipant = parent.getTeamLead();
         childJson.addProperty(ModelerConstants.ID_PROPERTY, childParticipant.getId());
         childJson.addProperty(ModelerConstants.NAME_PROPERTY, childParticipant.getName());
         childJson.addProperty(ModelerConstants.OID_PROPERTY,
               childParticipant.getElementOid());
         childJson.addProperty(ModelerConstants.UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(childParticipant));
         childJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
               eObjectUUIDMapper().getUUID(parent));
         childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.TEAM_LEADER_TYPE_KEY);
         loadDescription(childJson, childParticipant);
         loadAttributes(childParticipant, childJson);
      }

      parentJson.add(ModelerConstants.CHILD_PARTICIPANTS_KEY, childrenArray);
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

      loadAttributes(structType, structJson);

      structJson.addProperty(ModelerConstants.DESCRIPTION_PROPERTY,
            (null != structType.getDescription()) ? structType.getDescription() : "");

      JsonObject typeDeclarationJson = new JsonObject();

      structJson.add("typeDeclaration", typeDeclarationJson);

      XpdlTypeType type = structType.getDataType();
      if (null != type)
      {
         typeDeclarationJson.add("type", toXpdlTypeJson(type));
      }

      XSDSchema schema = structType.getSchema();
      if (null != schema)
      {
         String componentId = null;
         if (type instanceof SchemaTypeType)
         {
            componentId = structType.getId();
         }
         else if (type instanceof ExternalReferenceType)
         {
            componentId = ((ExternalReferenceType) type).getXref();
         }
         typeDeclarationJson.add("schema",
               XsdSchemaUtils.toSchemaJson(schema, componentId));
      }

      structJson.addProperty(ModelerConstants.TYPE_PROPERTY,
            ModelerConstants.TYPE_DECLARATION_PROPERTY);

      return structJson;
   }

   private JsonElement toXpdlTypeJson(XpdlTypeType type)
   {
      JsonObject typeJson = new JsonObject();

      String name = type.eClass().getName();
      typeJson.addProperty("classifier", name.substring(0, name.length() - 4)); // exclude
                                                                                // "Type"
                                                                                // suffix
      if (type instanceof ExternalReferenceType)
      {
         ExternalReferenceType ref = (ExternalReferenceType) type;
         typeJson.addProperty("location", ref.getLocation());
         if ( !StringUtils.isEmpty(ref.getNamespace()))
         {
            typeJson.addProperty("namespace", ref.getNamespace());
         }
         if ( !StringUtils.isEmpty(ref.getXref()))
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
      if (null != element.getDescription()
            && element.getDescription().getMixed().size() > 0)
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
   private void loadAttributes(EObject element, JsonObject json)
   {
      JsonObject attributes;

      if (!hasNotJsonNull(json, ModelerConstants.ATTRIBUTES_PROPERTY))
      {
         json.add(ModelerConstants.ATTRIBUTES_PROPERTY, attributes = new JsonObject());
      }
      else
      {
         attributes = json.getAsJsonObject(ModelerConstants.ATTRIBUTES_PROPERTY);
      }

      for (Object attribute : getModelBuilderFacade().getAttributes(element))
      {
         String attributeName = getModelBuilderFacade().getAttributeName(attribute);
         String attributeValue = getModelBuilderFacade().getAttributeValue(attribute);
         if (attributeName.equals(
               "carnot:engine:period"))
         {
            Period period = new Period(attributeValue);
            String units = "YMDhms";
            int delay = 0;
            String unit = units.substring(Period.SECONDS);
            for (int i = Period.YEARS; i <= Period.SECONDS; i++)
            {
               delay = period.get(i);
               if (delay > 0)
               {
                  unit = units.substring(i, i + 1);
                  break;
               }
            }
            attributes.addProperty("carnot:engine:delay", delay);
            attributes.addProperty("carnot:engine:delayUnit", unit);
         }
         else
         {
            if (attributeName.equals(
                  "documentation:comments"))
            {
               json.add(
                     ModelerConstants.COMMENTS_PROPERTY,
                     jsonIo.readJsonObject(
                           attributeValue)
                           .get(ModelerConstants.COMMENTS_PROPERTY)
                           .getAsJsonArray());
            }
            else if (attributeName.equals(
                  "carnot:engine:type"))
            {
               // For Access Points

               // TODO Very ugly storage

               json.addProperty(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY,
                     attributeValue);
            }
            else if (attributeName.equals(
                  ModelerConstants.DATA_TYPE))
            {
               // For Access Points

               // TODO Very ugly storage

               String encodedId = attributeValue;
               String structuredDataFullId = null;

               if (encodedId.indexOf("typeDeclaration") == 0)
               {
                  String parts[] = encodedId.split("\\{")[1].split("\\}");

                  structuredDataFullId = parts[0] + ":" + parts[1];
               }
               else
               {
                  ModelType model = ModelUtils.findContainingModel(element);
                  if (null != model)
                  {
                     structuredDataFullId = model.getId() + ":" + encodedId;
                  }
               }

               json.addProperty(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY,
                     structuredDataFullId);
            }
            else if (getModelBuilderFacade().isBooleanAttribute(attribute))
            {
               attributes.addProperty(
                     attributeName,
                     Boolean.parseBoolean(attributeValue));
            }
            else
            {
               attributes.addProperty(attributeName,
                     attributeValue);
            }
         }
      }

      if ( !hasNotJsonNull(json, ModelerConstants.COMMENTS_PROPERTY))
      {
         json.add(ModelerConstants.COMMENTS_PROPERTY, new JsonArray());
      }
   }

   protected void setNodeSymbolCoordinates(JsonObject nodeSymbolJson,
         INodeSymbol nodeSymbol)
   {
      int laneOffsetX = 0;
      int laneOffsetY = 0;
      ISwimlaneSymbol container = (nodeSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) nodeSymbol.eContainer()
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

      nodeSymbolJson.addProperty(ModelerConstants.X_PROPERTY, nodeSymbol.getXPos()
            + laneOffsetX);
      nodeSymbolJson.addProperty(ModelerConstants.Y_PROPERTY, nodeSymbol.getYPos()
            + laneOffsetY);
   }

   /**
    *
    * TODO From DynamicConnectionCommand. Refactor?
    *
    * @param activity
    * @return
    */
   /*private static String getDefaultDataMappingContext(ActivityType activity)
   {
      if (ActivityImplementationType.ROUTE_LITERAL == activity.getImplementation())
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }

      if (ActivityImplementationType.MANUAL_LITERAL == activity.getImplementation())
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }

      if (ActivityImplementationType.APPLICATION_LITERAL == activity.getImplementation())
      {
         ApplicationType application = getApplication(activity);

         if (application != null)
         {
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
   }*/

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

   /**
    * @param data
    * @param model
    * @param jsonObj
    */
   private void setDataFullID(JsonObject jsonObj, ModelType model, DataType data)
   {
      String fullID = getDataFullID(model, data);
      if (null != fullID)
      {
         jsonObj.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY, fullID);
      }
   }

   /**
    * @param data
    * @param model
    * @param jsonObj
    */
   private String getDataFullID(ModelType model, DataType data)
   {
      if (null != data)
      {
         if (data.eIsProxy() && model != null)
         {
            URI proxyUri = ((InternalEObject) data).eProxyURI();
            ModelType referencedModel = null;
            referencedModel = getModelByProxyURI(model, proxyUri);
            if (referencedModel != null)
            {
               return getModelBuilderFacade().createFullId(referencedModel, data);
            }
         }
         else
         {
            return getModelBuilderFacade().createFullId(model, data);
         }
      }

      return null;
   }

   String findInChangeDescriptions(List<ChangeDescriptionJto> changeDescriptions,
         String id)
   {
      for (ChangeDescriptionJto description : changeDescriptions)
      {
         JsonObject changes = description.changes;

         if (hasNotJsonNull(changes, ModelerConstants.FORMAL_PARAMETERS_PROPERTY))
         {
            JsonArray formalParametersJson = changes.get(
                  ModelerConstants.FORMAL_PARAMETERS_PROPERTY).getAsJsonArray();
            for (int n = 0; n < formalParametersJson.size(); ++n)
            {
               JsonObject formalParameterJson = formalParametersJson.get(n)
                     .getAsJsonObject();
               if (formalParameterJson.get(ModelerConstants.ID_PROPERTY)
                     .getAsString()
                     .equals(id))
               {
                  if (formalParameterJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
                        .getAsString()
                        .equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
                  {
                     String primitiveDataType = null;
                     JsonElement jsonElementType = formalParameterJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY);
                     if (jsonElementType != null)
                     {
                        primitiveDataType = jsonElementType.getAsString();
                     }

                     return primitiveDataType;
                  }
               }
            }
         }
      }

      return null;
   }
}