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

package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingActivity;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findIdentifiableElement;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ElementCopier;
import org.eclipse.stardust.model.xpdl.builder.utils.LaneParticipantUtil;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.NameIdUtilsExtension;
import org.eclipse.stardust.model.xpdl.builder.utils.PepperIconFactory;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerConnectionManager;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.AnnotationSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationTypeType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DescriptionType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.EventConditionTypeType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.IAccessPointOwner;
import org.eclipse.stardust.model.xpdl.carnot.IExtensibleElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ISwimlaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IntermediateEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.OrientationType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.SubProcessModeType;
import org.eclipse.stardust.model.xpdl.carnot.TextType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.XmlTextNode;
import org.eclipse.stardust.model.xpdl.carnot.util.AccessPointUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.CarnotConstants;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.util.StructuredTypeUtils;
import org.eclipse.stardust.model.xpdl.util.IdFactory;
import org.eclipse.stardust.model.xpdl.xpdl2.*;
import org.eclipse.stardust.model.xpdl.xpdl2.extensions.ExtensionFactory;
import org.eclipse.stardust.model.xpdl.xpdl2.extensions.LoopDataRefType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.util.XpdlUtil;
import org.eclipse.stardust.modeling.repository.common.descriptors.ReplaceModelElementDescriptor;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.service.WebServiceApplicationUtils;
import org.eclipse.stardust.ui.web.modeler.service.XsdSchemaUtils;
import org.eclipse.stardust.ui.web.modeler.spi.ModelFormat;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

/**
 *
 * @author Marc.Gille
 *
 */
@Service
@ModelFormat(ModelFormat.XPDL)
@ModelingSessionScoped
public class ModelElementUnmarshaller implements ModelUnmarshaller
{
   static final String ABORT_ACTIVITY_NAME = "Abort Activity";

   static final String COMPLETE_ACTIVITY_NAME = "Complete Activity";

   private Map<Class<? >, String[]> propertiesMap;

   @Resource
   private ModelingSession modelingSession;

   // TODO For documentation creation
   private static final String MODEL_DOCUMENTATION_TEMPLATES_FOLDER = "/documents/templates/modeling/";

   private static final String MODELING_DOCUMENTS_DIR = "/process-modeling-documents/";

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   private ModelBuilderFacade modelBuilderFacade;

   @Resource
   private JsonMarshaller jsonIo;

   private static final Logger logger = LogManager.getLogger(ModelElementUnmarshaller.class);

   public ModelElementUnmarshaller()
   {
      propertiesMap = newHashMap();

      propertiesMap.put(ProcessDefinitionType.class,
            new String[] {ModelerConstants.DEFAULT_PRIORITY_PROPERTY});
      propertiesMap.put(ActivityType.class, new String[] {});
      // propertiesMap.put(EventSymbol.class,
      // new String[] {ModelerConstants.NAME_PROPERTY});
      propertiesMap.put(LaneSymbol.class, new String[] {});
      // propertiesMap.put(EndEventSymbol.class,
      // new String[] {ModelerConstants.NAME_PROPERTY});

      propertiesMap.put(ApplicationType.class, new String[] {});

      // propertiesMap.put(TypeDeclarationType.class, new String[] {
      // ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(ModelType.class, new String[] {});
      propertiesMap.put(DataType.class, new String[] {});
      propertiesMap.put(RoleType.class, new String[] {});
      propertiesMap.put(OrganizationType.class, new String[] {});
      propertiesMap.put(ConditionalPerformerType.class,
            new String[] {ModelerConstants.BINDING_DATA_PATH_PROPERTY});
      propertiesMap.put(TransitionType.class,
            new String[] {ModelerConstants.NAME_PROPERTY});
   }

   /**
    *
    * @param element
    * @param json
    */
   public void populateFromJson(EObject element, JsonObject json)
   {
      logger.debug("Unmarshalling: " + element + " " + json);

      if (element instanceof ProcessDefinitionType)
      {
         updateProcessDefinition((ProcessDefinitionType) element, json);
      }
      else if (element instanceof ActivitySymbolType)
      {
         if (((ActivitySymbolType) element).getActivity()
               .getName()
               .toLowerCase()
               .startsWith("gateway"))
         {
            updateGatewaySymbol((ActivitySymbolType) element, json);
         }
         else
         {
            updateActivitySymbol((ActivitySymbolType) element, json);
         }
      }
      else if (element instanceof ActivityType)
      {
         updateActivity((ActivityType) element, json);
      }
      else if (element instanceof StartEventSymbol)
      {
         updateStartEventSymbol((StartEventSymbol) element, json);
      }
      else if (element instanceof IntermediateEventSymbol)
      {
         updateIntermediateEventSymbol((IntermediateEventSymbol) element, json);
      }
      else if (element instanceof EndEventSymbol)
      {
         updateEndEventSymbol((EndEventSymbol) element, json);
      }
      else if (element instanceof AnnotationSymbolType)
      {
         updateAnnotationSymbol((AnnotationSymbolType) element, json);
      }
      else if (element instanceof ApplicationType)
      {
         updateApplication((ApplicationType) element, json);
      }
      else if (element instanceof TypeDeclarationType)
      {
         updateTypeDeclaration((TypeDeclarationType) element, json);
      }
      else if (element instanceof ModelType)
      {
         updateModel((ModelType) element, json);
      }
      else if (element instanceof DataType)
      {
         updateData((DataType) element, json);
      }
      else if (element instanceof DataSymbolType)
      {
         updateDataSymbol((DataSymbolType) element, json);
      }
      else if (element instanceof RoleType)
      {
         updateRole((RoleType) element, json);
      }
      else if (element instanceof ConditionalPerformerType)
      {
         updateConditionalPerformer((ConditionalPerformerType) element, json);
      }
      else if (element instanceof OrganizationType)
      {
         updateOrganization((OrganizationType) element, json);
      }
      else if (element instanceof LaneSymbol)
      {
         updateSwimlane((LaneSymbol) element, json);
      }
      else if (element instanceof DiagramType)
      {
         updateDiagram((DiagramType) element, json);
      }
      else if (element instanceof TransitionConnectionType)
      {
         updateControlFlowConnection((TransitionConnectionType) element, json);
      }
      else if (element instanceof DataMappingConnectionType)
      {
         updateDataFlowConnection((DataMappingConnectionType) element, json);
      }
      else
      {
         logger.warn("===> Unsupported Symbol " + element);
      }
   }

   /**
    *
    * @param element
    * @param json
    */
   private void updateActivity(ActivityType activity, JsonObject activityJson)
   {
      if (null == activityJson)
      {
         return;
      }

      // Detect gateways early to be able to fix accidental ID changes

      final boolean isGateway = activity.getId().toLowerCase().startsWith("gateway");

      updateIdentifiableElement(activity, activityJson);
      mapDeclaredProperties(activity, activityJson, propertiesMap.get(ActivityType.class));
      storeAttributes(activity, activityJson);
      storeDescription(activity, activityJson);

      if (isGateway)
      {
         if (hasNotJsonNull(activityJson, ModelerConstants.GATEWAY_TYPE_PROPERTY))
         {
            if (activityJson.get(ModelerConstants.GATEWAY_TYPE_PROPERTY)
                  .getAsString()
                  .equals(ModelerConstants.XOR_GATEWAY_TYPE))
            {
               activity.setJoin(JoinSplitType.XOR_LITERAL);
               activity.setSplit(JoinSplitType.XOR_LITERAL);
            }
            else if (activityJson.get(ModelerConstants.GATEWAY_TYPE_PROPERTY)
                  .getAsString()
                  .equals(ModelerConstants.AND_GATEWAY_TYPE))
            {
               activity.setJoin(JoinSplitType.AND_LITERAL);
               activity.setSplit(JoinSplitType.AND_LITERAL);
            }
            else if (activityJson.get(ModelerConstants.GATEWAY_TYPE_PROPERTY)
                  .getAsString()
                  .equals(ModelerConstants.OR_GATEWAY_TYPE))
            {
               activity.setJoin(JoinSplitType.OR_LITERAL);
               activity.setSplit(JoinSplitType.OR_LITERAL);
            }
         }
      }
      else
      {
         if (hasNotJsonNull(activityJson, ModelerConstants.ACTIVITY_IS_ABORTABLE_BY_PERFORMER))
         {
            activity.setAllowsAbortByPerformer(activityJson.get(
                  ModelerConstants.ACTIVITY_IS_ABORTABLE_BY_PERFORMER).getAsBoolean());
         }

         if (hasNotJsonNull(activityJson, ModelerConstants.ACTIVITY_IS_HIBERNATED_ON_CREATION))
         {
            activity.setHibernateOnCreation(activityJson.get(
                  ModelerConstants.ACTIVITY_IS_HIBERNATED_ON_CREATION).getAsBoolean());
         }

         JsonElement loopJson = activityJson.get("loop");
         if (loopJson != null)
         {
            updateLoop(activity, loopJson);
         }

         if (hasNotJsonNull(activityJson, ModelerConstants.TASK_TYPE))
         {
            String taskType = activityJson.get(ModelerConstants.TASK_TYPE).getAsString();
            ModelBuilderFacade.setAttribute(activity, ModelerConstants.TASK_TYPE,
                  taskType);
            ApplicationType rulesApp = null;
            if (activity.getApplication() != null
                  && activity.getApplication().getType() != null)
            {
               if (activity.getApplication()
                     .getType()
                     .getId()
                     .equals(ModelerConstants.DROOLS_APPLICATION_TYPE_ID))
               {
                  rulesApp = activity.getApplication();
               }
            }

            if (taskType.equals(ModelerConstants.NONE_TASK_KEY))
            {
               activity.setImplementation(ActivityImplementationType.ROUTE_LITERAL);
               activity.setApplication(null);
            }
            else if (taskType.equals(ModelerConstants.MANUAL_TASK_KEY))
            {
               activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);
               activity.setApplication(null);
            }
            else if (taskType.equals(ModelerConstants.RULE_TASK_KEY))
            {
               if (activity.getApplication() == null) {
                  ModelType model = ModelUtils.findContainingModel(activity);
                  activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);
                  ApplicationType application = getModelBuilderFacade().createApplication(model, "droolsApplication",
                        "droolsApplication", ModelerConstants.DROOLS_APPLICATION_TYPE_ID);
                  activity.setApplication(application);
               }
            }
            else
            {
               activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);
            }
            //Remove the "hidden" drools application if not needed anymore
            if (rulesApp != null
                  && (activity.getApplication() == null || activity.getApplication()
                        .equals(rulesApp)))
            {
               ModelType model = ModelUtils.findContainingModel(activity);
               model.getApplication().remove(rulesApp);
            }
         }

         if (hasNotJsonNull(activityJson, ModelerConstants.APPLICATION_FULL_ID_PROPERTY))
         {
            String applicationFullId = extractString(activityJson,
                  ModelerConstants.APPLICATION_FULL_ID_PROPERTY);

            getModelBuilderFacade().setApplication(activity, applicationFullId);
         }

         if (ModelerConstants.SUBPROCESS_ACTIVITY.equals(extractString(activityJson,
               ModelerConstants.ACTIVITY_TYPE)))
         {
            activity.setImplementation(ActivityImplementationType.SUBPROCESS_LITERAL);

            if (hasNotJsonNull(activityJson, ModelerConstants.SUBPROCESS_ID))
            {
               String subprocessFullId = extractString(activityJson,
                     ModelerConstants.SUBPROCESS_ID);

               getModelBuilderFacade().setSubProcess(activity, subprocessFullId);

               if (hasNotJsonNull(activityJson, ModelerConstants.SUBPROCESS_MODE_PROPERTY))
               {
                  if (activityJson.get(ModelerConstants.SUBPROCESS_MODE_PROPERTY)
                        .getAsString()
                        .equals(ModelerConstants.ASYNC_SEPARATE_KEY))
                  {
                     activity.setSubProcessMode(SubProcessModeType.ASYNC_SEPARATE_LITERAL);
                  }
                  else if (activityJson.get(ModelerConstants.SUBPROCESS_MODE_PROPERTY)
                        .getAsString()
                        .equals(ModelerConstants.SYNC_SEPARATE_KEY))
                  {
                     activity.setSubProcessMode(SubProcessModeType.SYNC_SEPARATE_LITERAL);
                  }
                  else if (activityJson.get(ModelerConstants.SUBPROCESS_MODE_PROPERTY)
                        .getAsString()
                        .equals(ModelerConstants.SYNC_SHARED_KEY))
                  {
                     activity.setSubProcessMode(SubProcessModeType.SYNC_SHARED_LITERAL);
                  }
               }
            }
         }

         JsonElement participantIdJson = activityJson.get(ModelerConstants.PARTICIPANT_FULL_ID);
         if (participantIdJson != null)
         {
            if (participantIdJson.isJsonNull())
            {
               activity.setPerformer(null);
            }
            else if (participantIdJson.isJsonPrimitive() && participantIdJson.getAsJsonPrimitive().isString())
            {
               String participantFullId = participantIdJson.getAsString();
               IModelParticipant performer = getModelBuilderFacade().findParticipant(participantFullId);
               activity.setPerformer(performer);
            }
         }
      }
   }

   private void updateLoop(ActivityType activity, JsonElement loopJson)
   {
      if (loopJson.isJsonNull())
      {
         activity.setLoop(null);
      }
      else if (loopJson.isJsonObject())
      {
         JsonObject json = (JsonObject) loopJson;
         LoopType loop = activity.getLoop();
         String type = GsonUtils.safeGetAsString(json, "type");
         if ("multi".equals(type))
         {
            LoopMultiInstanceType multiLoop = XpdlUtil.getOrCreateLoopMulti(loop);
            if (json.has("sequential"))
            {
               multiLoop.setMIOrdering(GsonUtils.safeGetBool(json, "sequential") ? MIOrderingType.SEQUENTIAL : MIOrderingType.PARALLEL);
            }
            LoopDataRefType loopDataRef = multiLoop.getLoopDataRef();
            LoopDataRefType dataRef = loopDataRef;

            boolean hasInputId = json.has("inputId");
            boolean hasOutputId = json.has("outputId");
            boolean hasIndexId = json.has("indexId");
            if (hasInputId || hasOutputId || hasIndexId)
            {
               if (dataRef == null)
               {
                  dataRef = ExtensionFactory.eINSTANCE.createLoopDataRefType();
               }
               if (hasInputId)
               {
                  dataRef.setInputItemRef(GsonUtils.safeGetAsString(json, "inputId"));
               }
               if (hasOutputId)
               {
                  dataRef.setOutputItemRef(GsonUtils.safeGetAsString(json, "outputId"));
               }
               if (hasIndexId)
               {
                  dataRef.setLoopCounterRef(GsonUtils.safeGetAsString(json, "indexId"));
               }
            }
            if (dataRef != null)
            {
               if (loopDataRef != null
                     && dataRef.getInputItemRef() == null
                     && dataRef.getOutputItemRef() == null
                     && dataRef.getLoopCounterRef() == null)
               {
                  multiLoop.setLoopDataRef(null);
               }
               else if (loopDataRef == null)
               {
                  multiLoop.setLoopDataRef(dataRef);
               }
            }
            if (loop == null)
            {
               activity.setLoop((LoopType) multiLoop.eContainer());
            }
         }
      }
   }

   /**
    *
    * @param element
    * @param controlFlowJson
    */
   private void updateControlFlowConnection(
         TransitionConnectionType controlFlowConnection,
         JsonObject controlFlowConnectionJson)
   {
      TransitionType transition = controlFlowConnection.getTransition();
      JsonObject controlFlowJson = controlFlowConnectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      mapDeclaredProperties(transition, controlFlowJson,
            propertiesMap.get(TransitionType.class));
      storeDescription(transition, controlFlowJson);
      storeAttributes(transition, controlFlowJson);

      if (hasNotJsonNull(controlFlowJson, ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY))
      {
         transition.setForkOnTraversal(controlFlowJson.get(
               ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY).getAsBoolean());
      }

      if (hasNotJsonNull(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY))
      {
         if (controlFlowJson.get(ModelerConstants.OTHERWISE_PROPERTY).getAsBoolean())
         {
            transition.setCondition(ModelerConstants.OTHERWISE_KEY);

            // Sets condition expression to empty for default flow
            XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();
            ModelUtils.setCDataString(expression.getMixed(), "", true);
            transition.setExpression(expression);
         }
         else
         {
            transition.setCondition(ModelerConstants.CONDITION_KEY);

            // Sets condition expression to true when default flow is unchecked
            XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();
            ModelUtils.setCDataString(expression.getMixed(), "true", true);
            transition.setExpression(expression);
         }
      }

      if (hasNotJsonNull(controlFlowJson, ModelerConstants.CONDITION_EXPRESSION_PROPERTY))
      {
         transition.setCondition(ModelerConstants.CONDITION_KEY);

         XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();

         String expressionValue = controlFlowJson.get(
               ModelerConstants.CONDITION_EXPRESSION_PROPERTY).getAsString();
         if (StringUtils.isEmpty(expressionValue))
         {
            expressionValue = "true";
         }

         ModelUtils.setCDataString(expression.getMixed(), expressionValue, true);
         transition.setExpression(expression);
      }

      // While routing , anchor point orientation changes
      if (hasNotJsonNull(controlFlowJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         controlFlowConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
               controlFlowJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }
      if (hasNotJsonNull(controlFlowJson, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         controlFlowConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
               controlFlowJson, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }
   }

   /**
    *
    * @param dataFlowConnection
    * @param dataFlowConnectionJson
    */
   private void updateDataFlowConnection(DataMappingConnectionType dataFlowConnection,
         JsonObject dataFlowConnectionJson)
   {
      // dataFlowConnectionJson is the diagram element; dataFlowJson is the model element

      JsonObject dataFlowJson = dataFlowConnectionJson
            .getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      if (hasNotJsonNull(dataFlowJson,
            ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         dataFlowConnection.setSourceAnchor(mapAnchorOrientation(extractInt(dataFlowJson,
               ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }

      if (hasNotJsonNull(dataFlowJson,
            ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         dataFlowConnection.setTargetAnchor(mapAnchorOrientation(extractInt(dataFlowJson,
               ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }

      // Mapping should be updated,if JSON contains mapping element
      if (dataFlowJson.has(ModelerConstants.INPUT_DATA_MAPPING_PROPERTY)
            || dataFlowJson.has(ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY))
      {
         // Collect all data mappings between the activity and the data

         List<DataMappingType> dataMappings = new ArrayList<DataMappingType>();

         for (DataMappingType dataMapping : dataFlowConnection.getActivitySymbol()
               .getActivity().getDataMapping())
         {
            if (dataMapping.getData().getId()
                  .equals(dataFlowConnection.getDataSymbol().getData().getId()))
            {
               dataMappings.add(dataMapping);
            }
         }

         // Delete all data mappings between the activity and the data

         for (DataMappingType dataMapping : dataMappings)
         {
            dataFlowConnection.getActivitySymbol().getActivity().getDataMapping()
                  .remove(dataMapping);
            dataFlowConnection.getDataSymbol().getData().getDataMappings()
                  .remove(dataMapping);
         }

         // dataFlowJson holds an input and/or an output dataMappingJson; data mappings
         // have to be created for both

         // Create input mapping
         JsonObject inJson = null;
         JsonObject outJson = null;

         if (hasNotJsonNull(dataFlowJson, ModelerConstants.INPUT_DATA_MAPPING_PROPERTY))
         {
            inJson = dataFlowJson
                  .getAsJsonObject(ModelerConstants.INPUT_DATA_MAPPING_PROPERTY);

         }
         if (hasNotJsonNull(dataFlowJson, ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY))
         {
            outJson = dataFlowJson
                  .getAsJsonObject(ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY);
         }

         boolean hasInAccessPoints = hasAccessPoints(dataFlowConnection
               .getActivitySymbol().getActivity(), DirectionType.IN_LITERAL);
         boolean hasOutAccessPoints = hasAccessPoints(dataFlowConnection
               .getActivitySymbol().getActivity(), DirectionType.OUT_LITERAL);

         if ((hasInAccessPoints && hasOutAccessPoints)
               || (!hasInAccessPoints && !hasOutAccessPoints))
         {
            if (inJson != null)
            {
               createDataMapping(dataFlowConnection.getActivitySymbol().getActivity(),
                     dataFlowConnection.getDataSymbol().getData(), dataFlowJson,
                     DirectionType.IN_LITERAL, inJson);
            }

            // Create output mapping
            if (outJson != null)
            {
               createDataMapping(dataFlowConnection.getActivitySymbol().getActivity(),
                     dataFlowConnection.getDataSymbol().getData(), dataFlowJson,
                     DirectionType.OUT_LITERAL, outJson);
            }
         }

         if (hasInAccessPoints && !hasOutAccessPoints)
         {
            if (inJson != null)
            {
               createDataMapping(dataFlowConnection.getActivitySymbol().getActivity(),
                     dataFlowConnection.getDataSymbol().getData(), dataFlowJson,
                     DirectionType.IN_LITERAL, inJson);
            }

            // Create output mapping
            if (outJson != null)
            {
               createDataMapping(dataFlowConnection.getActivitySymbol().getActivity(),
                     dataFlowConnection.getDataSymbol().getData(), dataFlowJson,
                     DirectionType.OUT_LITERAL, inJson);
            }
         }

         if (!hasInAccessPoints && hasOutAccessPoints)
         {
            if (inJson != null)
            {
               createDataMapping(dataFlowConnection.getActivitySymbol().getActivity(),
                     dataFlowConnection.getDataSymbol().getData(), dataFlowJson,
                     DirectionType.IN_LITERAL, outJson);
            }

            // Create output mapping
            if (outJson != null)
            {
               createDataMapping(dataFlowConnection.getActivitySymbol().getActivity(),
                     dataFlowConnection.getDataSymbol().getData(), dataFlowJson,
                     DirectionType.OUT_LITERAL, outJson);
            }
         }

      }

      // Following condition is added to handle the scenario where only name property is
      // modified
      if (dataFlowJson.has(ModelerConstants.NAME_PROPERTY))
      {
         for (DataMappingType dataMapping : dataFlowConnection.getActivitySymbol()
               .getActivity().getDataMapping())
         {
            if (dataMapping.getData().getId()
                  .equals(dataFlowConnection.getDataSymbol().getData().getId()))
            {
               dataMapping.setName(extractAsString(dataFlowJson,
                     ModelerConstants.NAME_PROPERTY));
               dataMapping.setId(extractAsString(dataFlowJson,
                     ModelerConstants.NAME_PROPERTY));
            }
         }
      }
   }

   private boolean hasAccessPoints(ActivityType activity, DirectionType direction)
   {
      if (activity.getImplementation().getLiteral().equals("Subprocess"))
      {
         return true;
      }
      return !getAccessPoints(activity, direction).isEmpty();
   }

   private List<AccessPointType> getAccessPoints(ActivityType activity,
         DirectionType direction)
   {
      List<AccessPointType> emptyList = new ArrayList<AccessPointType>();
      if (activity.getImplementation().getLiteral().equals("Application"))
      {
         if (direction.equals(DirectionType.IN_LITERAL))
         {
            List<AccessPointType> accessPoints = AccessPointUtil
                  .getInAccessPonts(activity.getApplication());
            if (!accessPoints.isEmpty())
            {
               return accessPoints;
            }
            if (activity.getApplication().getContext() != null
                  && !activity.getApplication().getContext().isEmpty())
            {
               return AccessPointUtil.getInAccessPonts(activity.getApplication()
                     .getContext().get(0));
            }
         }
         if (direction.equals(DirectionType.OUT_LITERAL))
         {
            List<AccessPointType> accessPoints = AccessPointUtil
                  .getOutAccessPonts(activity.getApplication());
            if (!accessPoints.isEmpty())
            {
               return accessPoints;
            }
            if (activity.getApplication().getContext() != null
                  && !activity.getApplication().getContext().isEmpty())
            {
               return AccessPointUtil.getOutAccessPonts(activity.getApplication()
                     .getContext().get(0));
            }
         }
      }
      return emptyList;
   }


   /**
    *
    * @param activity
    * @param data
    * @param direction
    * @param id
    * @param name
    * @return
    */
   private DataMappingType createDataMapping(ActivityType activity, DataType data,
         JsonObject dataFlowJson, DirectionType direction, JsonObject dataMappingJson)
   {
      DataMappingType dataMapping = AbstractElementBuilder.F_CWM.createDataMappingType();

      dataMapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);

      if (hasNotJsonNull(dataFlowJson, ModelerConstants.ID_PROPERTY))
      {
         dataMapping.setId(dataFlowJson.get(ModelerConstants.ID_PROPERTY).getAsString());
      }
      else
      {
         dataMapping.setId(data.getId());
      }

      if (hasNotJsonNull(dataFlowJson, ModelerConstants.NAME_PROPERTY))
      {
         dataMapping.setName(dataFlowJson.get(ModelerConstants.NAME_PROPERTY)
               .getAsString());
      }
      else
      {
         dataMapping.setName(data.getName());
      }

      dataMapping.setDirection(direction);

      if (hasNotJsonNull(dataMappingJson, ModelerConstants.ACCESS_POINT_ID_PROPERTY))
      {

         dataMapping.setApplicationAccessPoint(dataMappingJson.get(
               ModelerConstants.ACCESS_POINT_ID_PROPERTY).getAsString());
         {
            dataMapping.setContext(dataMappingJson.get(
                  ModelerConstants.ACCESS_POINT_CONTEXT_PROPERTY).getAsString());
         }
      }
      if (dataMappingJson.has(ModelerConstants.ACCESS_POINT_PATH_PROPERTY))
      {
         if (dataMappingJson.get(ModelerConstants.ACCESS_POINT_PATH_PROPERTY)
               .isJsonNull())
         {
            dataMapping.setApplicationPath(null);
         }
         else
         {
            dataMapping.setApplicationPath(dataMappingJson.get(
                  ModelerConstants.ACCESS_POINT_PATH_PROPERTY).getAsString());
         }
      }

      if (StringUtils.isEmpty(dataMapping.getContext()))
      {
         dataMapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
      }

      if (hasNotJsonNull(dataMappingJson, ModelerConstants.DATA_PATH_PROPERTY))
      {
         dataMapping.setDataPath(dataMappingJson.get(ModelerConstants.DATA_PATH_PROPERTY)
               .getAsString());
      }

      dataMapping.setData(data);
      activity.getDataMapping().add(dataMapping);
      data.getDataMappings().add(dataMapping);

      return dataMapping;
   }


   /**
    *
    * @param element
    * @param json
    */
   private void updateSwimlane(LaneSymbol swimlaneSymbol, JsonObject swimlaneSymbolJson)
   {
      updateIdentifiableElement(swimlaneSymbol, swimlaneSymbolJson);

      // update orientation
      String orientation = extractString(swimlaneSymbolJson,
            ModelerConstants.ORIENTATION_PROPERTY);
      if (StringUtils.isNotEmpty(orientation))
      {
         if (ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL.equals(orientation))
         {
            swimlaneSymbol.setOrientation(OrientationType.HORIZONTAL_LITERAL);
            setDiagramOrientationType(swimlaneSymbol, OrientationType.HORIZONTAL_LITERAL);
         }
         else
         {
            swimlaneSymbol.setOrientation(OrientationType.VERTICAL_LITERAL);
            setDiagramOrientationType(swimlaneSymbol, OrientationType.VERTICAL_LITERAL);
         }
      }

      updateNodeSymbol(swimlaneSymbol, swimlaneSymbolJson);

      mapDeclaredProperties(swimlaneSymbol, swimlaneSymbolJson,
            propertiesMap.get(LaneSymbol.class));

      if (hasNotJsonNull(swimlaneSymbolJson, ModelerConstants.PARTICIPANT_FULL_ID))
      {
         String participantFullId = swimlaneSymbolJson.get(
               ModelerConstants.PARTICIPANT_FULL_ID).getAsString();

         if (ModelerConstants.NONE_LITERAL.equals(participantFullId))
         {
            LaneParticipantUtil.setParticipant(swimlaneSymbol, null);
         }
         else
         {
            ModelType model = ModelUtils.findContainingModel(swimlaneSymbol);
            String participantModelID = getModelBuilderFacade().getModelId(
                  participantFullId);
            ModelType participantModel = getModelBuilderFacade().findModel(
                  participantModelID);
            String participantId = getModelBuilderFacade().stripFullId(participantFullId);

            IModelParticipant findParticipant = getModelBuilderFacade().findParticipant(
                  participantModel, participantId);

            if ( !participantModelID.equals(model.getId()))
            {
               String fileConnectionId = WebModelerConnectionManager.createFileConnection(
                     model, participantModel);

               String bundleId = CarnotConstants.DIAGRAM_PLUGIN_ID;
               URI uri = URI.createURI("cnx://" + fileConnectionId + "/");

               ModelType loadModel = getModelBuilderFacade().getModelManagementStrategy()
                     .loadModel(participantModelID);

               IModelParticipant participantCopy = null;

               try
               {
                  participantCopy = getModelBuilderFacade().findParticipant(loadModel, participantId);
               }
               catch (ObjectNotFoundException e)
               {
               }

               if (participantCopy == null)
               {
                  ElementCopier copier = new ElementCopier(loadModel, null);
                  participantCopy = (IModelParticipant) copier.copy(findParticipant);
               }

               ReplaceModelElementDescriptor descriptor = new ReplaceModelElementDescriptor(
                     uri, participantCopy, bundleId, null, true);
               PepperIconFactory iconFactory = new PepperIconFactory();
               descriptor.importElements(iconFactory, model, true);
               findParticipant = getModelBuilderFacade().findParticipant(model,
                     participantId);
            }
            LaneParticipantUtil.setParticipant(swimlaneSymbol, findParticipant);
         }
      }

      storeAttributes(swimlaneSymbol, swimlaneSymbolJson);
   }

   /**
    * @param diagram
    * @param poolSymbolJson
    */
   private void updateDiagram(DiagramType diagram, JsonObject poolSymbolJson)
   {
      String orientation = extractString(poolSymbolJson,
            ModelerConstants.ORIENTATION_PROPERTY);
      if (StringUtils.isNotEmpty(orientation))
      {
         if (ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL.equals(orientation))
         {
            diagram.setOrientation(OrientationType.HORIZONTAL_LITERAL);
         }
         else
         {
            diagram.setOrientation(OrientationType.VERTICAL_LITERAL);
         }
      }
   }

   /**
    *
    * @param element
    * @param elementJson
    */
   public static void updateIdentifiableElement(IIdentifiableElement element,
         JsonObject elementJson)
   {
      updateElementNameAndId(element,
            CarnotWorkflowModelPackage.eINSTANCE.getIIdentifiableElement_Id(),
            CarnotWorkflowModelPackage.eINSTANCE.getIIdentifiableElement_Name(),
            elementJson);
   }

   /**
    * @param element
    * @param elementJson
    */
   private static boolean updateElementNameAndId(EObject element, EStructuralFeature eFtrId,
         EStructuralFeature eFtrName, JsonObject elementJson)
   {
      boolean wasModified = false;
      boolean isGateway = false;
      String newId = null;

      if (element instanceof ActivityType)
      {
         isGateway = ((IIdentifiableElement) element).getId()
               .toLowerCase()
               .startsWith("gateway");
      }

      if (hasNotJsonNull(elementJson, ModelerConstants.ID_PROPERTY))
      {
         // provided ID has precedence over generated ID
         newId = extractString(elementJson, ModelerConstants.ID_PROPERTY);
      }

      if (hasNotJsonNull(elementJson, ModelerConstants.NAME_PROPERTY))
      {
         String newName = extractString(elementJson, ModelerConstants.NAME_PROPERTY);
         String base = null;
         if (isGateway && !newName.toLowerCase().startsWith("gateway"))
         {
            base = "gateway_" + newName;
         }

         if ( !element.eIsSet(eFtrName) || !element.eGet(eFtrName).equals(newName))
         {
            wasModified = true;
            element.eSet(eFtrName, newName);

            if (isEmpty(newId))
            {
               if (element instanceof IIdentifiableElement)
               {
                  newId = NameIdUtilsExtension.createIdFromName(null,
                        (IIdentifiableElement) element, base);
               }
               else if (element instanceof TypeDeclarationType)
               {
                  newId = NameIdUtilsExtension.createIdFromName(null,
                        (TypeDeclarationType) element);
               }
               else
               {
                  newId = NameIdUtilsExtension.createIdFromName(newName);
               }
            }
         }
      }

      if ( !isEmpty(newId) && !element.eGet(eFtrId).equals(newId))
      {
         wasModified = true;
         element.eSet(eFtrId, newId);
      }

      return wasModified;
   }

   /**
    *
    * @param processDefinition
    * @param processDefinitionJson
    */
   private void updateProcessDefinition(ProcessDefinitionType processDefinition,
         JsonObject processDefinitionJson)
   {
      updateIdentifiableElement(processDefinition, processDefinitionJson);

      mapDeclaredProperties(processDefinition, processDefinitionJson,
            propertiesMap.get(ProcessDefinitionType.class));
      storeAttributes(processDefinition, processDefinitionJson);
      storeDescription(processDefinition, processDefinitionJson);

      if (hasNotJsonNull(processDefinitionJson, ModelerConstants.FORMAL_PARAMETERS_PROPERTY))
      {
         // Make sure that formal parameters are never empty
         // TODO Code should be at a central place for Process Definitions
         if (processDefinition.getFormalParameters() == null)
         {
            processDefinition.setFormalParameters(XpdlFactory.eINSTANCE.createFormalParametersType());
         }

         if (processDefinition.getFormalParameters().getFormalParameter() != null)
         {
            //processDefinition.getFormalParameters().getFormalParameter().clear();
         }

         List<FormalParameterType> newParameters = new ArrayList<FormalParameterType>();

         processDefinition.setFormalParameterMappings(null);

         JsonArray formalParametersJson = processDefinitionJson.get(
               ModelerConstants.FORMAL_PARAMETERS_PROPERTY).getAsJsonArray();

         for (int n = 0; n < formalParametersJson.size(); ++n)
         {
            JsonObject formalParameterJson = formalParametersJson.get(n)
                  .getAsJsonObject();

            ModeType mode = null;

            if (formalParameterJson.get(ModelerConstants.DIRECTION_PROPERTY)
                  .getAsString()
                  .equals(DirectionType.IN_LITERAL.getLiteral()))
            {
               mode = ModeType.IN;
            }
            else if (formalParameterJson.get(ModelerConstants.DIRECTION_PROPERTY)
                  .getAsString()
                  .equals(DirectionType.OUT_LITERAL.getLiteral()))
            {
               mode = ModeType.OUT;
            }
            else
            {
               mode = ModeType.INOUT;
            }

            DataType data = null;
            String dataFullID = null;

            if (hasNotJsonNull(formalParameterJson, ModelerConstants.DATA_FULL_ID_PROPERTY))
            {
               dataFullID = formalParameterJson.get(
                     ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();
               ModelType model = ModelUtils.findContainingModel(processDefinition);
               data = getModelBuilderFacade().importData(model, dataFullID);
            }

            String formalParameterName = GsonUtils.safeGetAsString(formalParameterJson, ModelerConstants.NAME_PROPERTY);
            String formalParameterId = GsonUtils.safeGetAsString(formalParameterJson, ModelerConstants.ID_PROPERTY);
            String dataTypeId = GsonUtils.safeGetAsString(formalParameterJson, ModelerConstants.DATA_TYPE_PROPERTY);

            if (ModelerConstants.PRIMITIVE_DATA_TYPE_KEY.equals(dataTypeId))
            {
               String primitiveDataType = GsonUtils.safeGetAsString(formalParameterJson,
                     ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY);
               FormalParameterType parameterType = null;
               if (formalParameterJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
               {
                  String structuredDataTypeFullId = GsonUtils.safeGetAsString(formalParameterJson,
                        ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
                  TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(
                        structuredDataTypeFullId);
                  // For Java bound ENUM's create primitive else structured Params
                  if (!getModelBuilderFacade().isEnumerationJavaBound(typeDeclaration))
                  {
                     parameterType = getModelBuilderFacade().createStructuredParameter(processDefinition, data,
                           formalParameterId, formalParameterName, structuredDataTypeFullId, mode);
                  }
                  else
                  {
                     parameterType = getModelBuilderFacade().createPrimitiveParameter(processDefinition, data,
                           formalParameterId, formalParameterName,
                           primitiveDataType == null ? "String" : primitiveDataType, mode, structuredDataTypeFullId); //$NON-NLS-1$
                  }
                  newParameters.add(parameterType);
               }
               else
               {
                  parameterType = getModelBuilderFacade().createPrimitiveParameter(processDefinition, data,
                        formalParameterId, formalParameterName,
                        primitiveDataType == null ? "String" : primitiveDataType, mode); //$NON-NLS-1$
                  newParameters.add(parameterType);
               }
            }
            else if (ModelerConstants.STRUCTURED_DATA_TYPE_KEY.equals(dataTypeId))
            {
               String structuredDataTypeFullId = GsonUtils.safeGetAsString(
                     formalParameterJson,
                     ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
               FormalParameterType parameterType = getModelBuilderFacade().createStructuredParameter(
                     processDefinition, data, formalParameterId, formalParameterName,
                     structuredDataTypeFullId, mode);
               newParameters.add(parameterType);
            }
            else if (dataTypeId.equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
            {
               String structuredDataTypeFullId = null;

               if (hasNotJsonNull(formalParameterJson,
                     ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
               {
                  structuredDataTypeFullId = formalParameterJson.get(
                        ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                        .getAsString();
               }

               FormalParameterType parameterType = getModelBuilderFacade().createDocumentParameter(
                     processDefinition, data, formalParameterId, formalParameterName,
                     structuredDataTypeFullId, mode);
               newParameters.add(parameterType);
            }
         }
         processDefinition.getFormalParameters().getFormalParameter().clear();
         processDefinition.getFormalParameters().getFormalParameter().addAll(newParameters);
      }

      if (hasNotJsonNull(processDefinitionJson, ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY))
      {
         if (processDefinitionJson.get(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.NO_PROCESS_INTERFACE_KEY))
         {
            processDefinition.setFormalParameters(null);
            processDefinition.setFormalParameterMappings(null);
            processDefinition.setExternalRef(null);
         }
         else if (processDefinitionJson.get(
               ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.PROVIDES_PROCESS_INTERFACE_KEY))
         {
            //TODO: hasNotJsonNull required here?
            if ( !hasNotJsonNull(processDefinitionJson, ModelerConstants.FORMAL_PARAMETERS_PROPERTY))
            {
               if (processDefinition.getFormalParameters() == null)
               {
                  processDefinition.setFormalParameters(XpdlFactory.eINSTANCE.createFormalParametersType());
               }

               if (processDefinition.getFormalParameters().getFormalParameter() != null)
               {
                  processDefinition.getFormalParameters().getFormalParameter().clear();
               }
               processDefinition.setFormalParameterMappings(null);
            }
         }
         else if (processDefinitionJson.get(
               ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.IMPLEMENTS_PROCESS_INTERFACE_KEY))
         {
         }
      }

      if (hasNotJsonNull(processDefinitionJson, ModelerConstants.DATA_PATHES_PROPERTY))
      {
         List<DataPathType> newDataPaths = new ArrayList<DataPathType>();

         JsonArray dataPathes = processDefinitionJson.get(
               ModelerConstants.DATA_PATHES_PROPERTY).getAsJsonArray();

         for (int n = 0; n < dataPathes.size(); ++n)
         {
            ModelType model = ModelUtils.findContainingModel(processDefinition);
            JsonObject dataPathJson = dataPathes.get(n).getAsJsonObject();
            String dataPathID = dataPathJson.get(ModelerConstants.ID_PROPERTY)
                  .getAsString();
            String dataPathName = dataPathJson.get(ModelerConstants.NAME_PROPERTY)
                  .getAsString();

            DataPathType dataPathType = getModelBuilderFacade().createDataPath();

            if (dataPathID.equals("New_1"))
            {
               IdFactory idFactory = new IdFactory("dataPath", "DataPath_");
               idFactory.computeNames(processDefinition.getDataPath(), true);
               dataPathID = idFactory.getId();
               dataPathName = idFactory.getName();
            }

            if (dataPathID.equals("PROCESS_ATTACHMENTS"))
            {
               dataPathType.setId("PROCESS_ATTACHMENTS");
               dataPathType.setName("PROCESS_ATTACHMENTS");
            }

            if (StringUtils.isNotEmpty(dataPathName)
                  && !dataPathName.equals(dataPathType.getName())
                  && !dataPathID.equals("PROCESS_ATTACHMENTS"))
            {
               dataPathID = (NameIdUtilsExtension.createIdFromName(null,
                     (IIdentifiableElement) dataPathType, dataPathName));
            }

            dataPathType.setId(dataPathID);
            dataPathType.setName(dataPathName);

            if (hasNotJsonNull(dataPathJson, ModelerConstants.DATA_FULL_ID_PROPERTY))
            {
               String dataFullId = dataPathJson.get(
                     ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();

               DataType data = getModelBuilderFacade().importData(model, dataFullId);
               dataPathType.setData(data);
            }

            if (hasNotJsonNull(dataPathJson, ModelerConstants.DATA_PATH_PROPERTY))
            {
               dataPathType.setDataPath(dataPathJson.get(ModelerConstants.DATA_PATH_PROPERTY)
                     .getAsString());
            }

            dataPathType.setDescriptor(dataPathJson.get(ModelerConstants.DESCRIPTOR_PROPERTY)
                  .getAsBoolean());
            dataPathType.setKey(dataPathJson.get(ModelerConstants.KEY_DESCRIPTOR_PROPERTY)
                  .getAsBoolean());

            if (dataPathJson.get(ModelerConstants.DIRECTION_PROPERTY)
                  .getAsString()
                  .equals(DirectionType.IN_LITERAL.getLiteral()))
            {
               dataPathType.setDirection(DirectionType.IN_LITERAL);
            }
            else
            {
               dataPathType.setDirection(DirectionType.OUT_LITERAL);
            }

            newDataPaths.add(dataPathType);
         }
         processDefinition.getDataPath().clear();
         processDefinition.getDataPath().addAll(newDataPaths);
      }

      if (hasNotJsonNull(processDefinitionJson, ModelerConstants.DEFAULT_PRIORITY_PROPERTY))
      {
         processDefinition.setDefaultPriority(processDefinitionJson.get(
               ModelerConstants.DEFAULT_PRIORITY_PROPERTY).getAsInt());
      }
   }

   /**
    *
    * @param activitySymbol
    * @param activitySymbolJson
    */
   private void updateActivitySymbol(ActivitySymbolType activitySymbol,
         JsonObject activitySymbolJson)
   {
      updateNodeSymbol(activitySymbol, activitySymbolJson);

      mapDeclaredProperties(activitySymbol, activitySymbolJson,
            propertiesMap.get(ActivitySymbolType.class));

      ActivityType activity = activitySymbol.getActivity();
      JsonObject activityJson = activitySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      updateActivity(activity, activityJson);
   }

   /**
    * @param dataSymbol
    * @param dataSymbolJson
    */
   private void updateDataSymbol(DataSymbolType dataSymbol, JsonObject dataSymbolJson)
   {
      updateNodeSymbol(dataSymbol, dataSymbolJson);

      mapDeclaredProperties(dataSymbol, dataSymbolJson,
            propertiesMap.get(DataSymbolType.class));

      DataType data = dataSymbol.getData();
      JsonObject activityJson = dataSymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      if (null != activityJson)
      {
         updateData(data, activityJson);
      }
   }

   /**
    *
    * @param activitySymbol
    * @param activitySymbolJson
    */
   private void updateNodeSymbol(INodeSymbol nodeSymbol, JsonObject nodeSymbolJto)
   {
      LaneSymbol newParentSymbol = null;
      String parentID = extractString(nodeSymbolJto,
            ModelerConstants.PARENT_SYMBOL_ID_PROPERTY);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(nodeSymbol);
      if ( !(nodeSymbol instanceof LaneSymbol) && parentID != null)
      {
         newParentSymbol = getModelBuilderFacade().findLaneSymbolById(
               processDefinition, parentID);
      }

      if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.X_PROPERTY)
            && hasNotJsonNull(nodeSymbolJto, ModelerConstants.Y_PROPERTY))
      {
         int x = extractInt(nodeSymbolJto, ModelerConstants.X_PROPERTY);
         int y = extractInt(nodeSymbolJto, ModelerConstants.Y_PROPERTY);

         // adjust coordinates from global to local
         int laneOffsetX = 0;
         int laneOffsetY = 0;

         if (!(nodeSymbol instanceof LaneSymbol))
         {
            newParentSymbol = getModelBuilderFacade().findLaneSymbolById(
                  processDefinition, parentID);

            if (null != newParentSymbol)
            {
               laneOffsetX = new Long(newParentSymbol.getXPos()).intValue();
               laneOffsetY = new Long(newParentSymbol.getYPos()).intValue();
            }

         }

         nodeSymbol.setXPos(x - laneOffsetX);
         nodeSymbol.setYPos(y - laneOffsetY);
      }
      if (nodeSymbol instanceof LaneSymbol
            && (hasNotJsonNull(nodeSymbolJto, ModelerConstants.WIDTH_PROPERTY) || hasNotJsonNull(
                  nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY)))
      {
         int xOffset = 0, yOffset = 0;
         int height = 0;
         int heightOffset = 0;
         PoolSymbol poolSymbol = (PoolSymbol) nodeSymbol.eContainer();
         // Update the width of current Lane.
         int width = extractInt(nodeSymbolJto, ModelerConstants.WIDTH_PROPERTY);
         // Calculate widthOffset required to adjust other swimlanes
         int widthOffset = width - nodeSymbol.getWidth();
         nodeSymbol.setWidth(width);

         // Update the height of current Lane.
         if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY))
         {
            height = extractInt(nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY);
            heightOffset = height - nodeSymbol.getHeight();
            nodeSymbol.setHeight(height);
         }

         // Update the child symbol co-ordinates wrt parent(lane)
         if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.X_OFFSET))
            xOffset = nodeSymbolJto.get(ModelerConstants.X_OFFSET).getAsInt();
         if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.Y_OFFSET))
            yOffset = nodeSymbolJto.get(ModelerConstants.Y_OFFSET).getAsInt();

         if (xOffset != 0)
         {
            updateChildSymbolCoordinates((LaneSymbol) nodeSymbol, xOffset, 0);
         }
         if (yOffset != 0)
         {
            updateChildSymbolCoordinates((LaneSymbol) nodeSymbol, 0, yOffset);
         }

         // Update other swimlane width/height
         OrientationType orientation = getDiagramOrientationType(nodeSymbol);

         for (LaneSymbol lane : poolSymbol.getLanes())
         {
            if (nodeSymbol.getElementOid() != lane.getElementOid())
            {
               if (orientation.equals(OrientationType.VERTICAL_LITERAL))
               {
                  if (lane.getXPos() > nodeSymbol.getXPos() && widthOffset != 0)
                  {
                     lane.setXPos(lane.getXPos() + widthOffset);
                  }
                  if (heightOffset != 0)
                  {
                     lane.setHeight(height);
                     // if symbol on currentLane(nodeSymbol) is moved , adjustment on
                     // other lane symbol is required
                     updateChildSymbolCoordinates(lane, 0, yOffset);
                  }
               }
               else
               {
                  if (lane.getYPos() > nodeSymbol.getYPos() && heightOffset != 0)
                  {
                     lane.setYPos(lane.getYPos() + heightOffset);
                  }
                  if (widthOffset != 0)
                  {
                     lane.setWidth(width);
                     // if symbol on currentLane(nodeSymbol) is moved , adjustment on
                     // other lane symbol is required
                     updateChildSymbolCoordinates(lane, xOffset, 0);
                  }
               }
            }
         }
         // Update pool dimensions
         poolSymbol.setWidth(poolSymbol.getWidth() + widthOffset);
         poolSymbol.setHeight(poolSymbol.getHeight() + heightOffset);
      }
      else
      {
         if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.WIDTH_PROPERTY))
         {
            int width = extractInt(nodeSymbolJto, ModelerConstants.WIDTH_PROPERTY);
            nodeSymbol.setWidth(width);
         }
         if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY))
         {
            int height = extractInt(nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY);
            nodeSymbol.setHeight(height);
         }
      }
      // Type property is used to identify the symbol type, used while changing
      // parentSymbol on move from one lane to another.
      if (hasNotJsonNull(nodeSymbolJto, ModelerConstants.TYPE_PROPERTY))
      {
         String symbolType = nodeSymbolJto.get(ModelerConstants.TYPE_PROPERTY)
               .getAsString();
         if (null != symbolType && null != newParentSymbol)
         {
            updateParentSymbolForSymbol(nodeSymbol, newParentSymbol, symbolType);
         }
      }
   }

   /**
    * assist updating diagram - orientation
    *
    * @param nodeSymbol
    * @param orientation
    */
   private void setDiagramOrientationType(INodeSymbol nodeSymbol,
         OrientationType orientation)
   {
      ISwimlaneSymbol container = (nodeSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) nodeSymbol.eContainer()
            : null;

      if (null != container)
      {
         DiagramType diagram = (container.eContainer() instanceof DiagramType)
               ? (DiagramType) container.eContainer()
               : null;

         if (null != diagram)
         {
            diagram.setOrientation(orientation);
         }
      }
   }

   /**
    * assist retrieving diagram - orientation
    *
    * @param nodeSymbol
    * @return
    */
   private OrientationType getDiagramOrientationType(INodeSymbol nodeSymbol)
   {
      ISwimlaneSymbol container = (nodeSymbol.eContainer() instanceof ISwimlaneSymbol)
            ? (ISwimlaneSymbol) nodeSymbol.eContainer()
            : null;

      if (null != container)
      {
         DiagramType diagram = (container.eContainer() instanceof DiagramType)
               ? (DiagramType) container.eContainer()
               : null;

         if (null != diagram)
         {
            return diagram.getOrientation();
         }
      }

      return OrientationType.VERTICAL_LITERAL;
   }

   /**
    * Update the x,y co-ordinates of symbols contained in the lane
    *
    * @param laneSymbol
    * @param xOffset
    * @param yOffset
    */
   private void updateChildSymbolCoordinates(LaneSymbol laneSymbol, int xOffset,
         int yOffset)
   {
      for (ActivitySymbolType activitySymbol : laneSymbol.getActivitySymbol())
      {
         activitySymbol.setXPos(activitySymbol.getXPos() + xOffset);
         activitySymbol.setYPos(activitySymbol.getYPos() + yOffset);
      }
      for (StartEventSymbol startSymbol : laneSymbol.getStartEventSymbols())
      {
         startSymbol.setXPos(startSymbol.getXPos() + xOffset);
         startSymbol.setYPos(startSymbol.getYPos() + yOffset);
      }
      for (EndEventSymbol endSymbol : laneSymbol.getEndEventSymbols())
      {
         endSymbol.setXPos(endSymbol.getXPos() + xOffset);
         endSymbol.setYPos(endSymbol.getYPos() + yOffset);
      }
      for (DataSymbolType dataSymbol : laneSymbol.getDataSymbol())
      {
         dataSymbol.setXPos(dataSymbol.getXPos() + xOffset);
         dataSymbol.setYPos(dataSymbol.getYPos() + yOffset);
      }
   }

   /**
    * remove the association from existing lane and add symbol to new Lane
    *
    * @param nodeSymbol
    * @param newParentSymbol
    * @param symbolType
    */
   private void updateParentSymbolForSymbol(INodeSymbol nodeSymbol,
         LaneSymbol newParentSymbol, String symbolType)
   {
      LaneSymbol parentLane = (LaneSymbol) nodeSymbol.eContainer();
      if (symbolType.equals(ModelerConstants.ACTIVITY_SYMBOL)
            || symbolType.equals(ModelerConstants.GATEWAY_SYMBOL))
      {
         if (parentLane.getElementOid() != newParentSymbol.getElementOid())
         {
            // If the parent is changed, remove reference from old parent
            parentLane.getActivitySymbol().remove(nodeSymbol);
            ActivitySymbolType activitySymbol = (ActivitySymbolType) nodeSymbol;
            // Set the Performer for Activ
            if (null != activitySymbol.getActivity().getPerformer())
            {
               activitySymbol.getActivity()
                     .setPerformer(newParentSymbol.getParticipant());
            }
            newParentSymbol.getActivitySymbol().add((ActivitySymbolType) nodeSymbol);
         }
      }
      else if (symbolType.equals(ModelerConstants.EVENT_SYMBOL))
      {
         StartEventSymbol startSymbol = ModelBuilderFacade.findStartEventSymbol(
               parentLane, nodeSymbol.getElementOid());

         EndEventSymbol endEventSymbol = ModelBuilderFacade.findEndEventSymbol(
               parentLane, nodeSymbol.getElementOid());

         IntermediateEventSymbol intermediateEventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(
               parentLane, nodeSymbol.getElementOid());

         if (null != startSymbol)
         {
            if (parentLane.getElementOid() != newParentSymbol.getElementOid())
            {
               parentLane.getStartEventSymbols().remove(nodeSymbol);

               newParentSymbol.getStartEventSymbols().add((StartEventSymbol) nodeSymbol);
            }
         }
         else if (endEventSymbol != null)
         {
            if (parentLane.getElementOid() != newParentSymbol.getElementOid())
            {
               parentLane.getEndEventSymbols().remove(nodeSymbol);

               newParentSymbol.getEndEventSymbols().add((EndEventSymbol) nodeSymbol);
            }
         }
         else if (intermediateEventSymbol != null)
         {
            if (parentLane.getElementOid() != newParentSymbol.getElementOid())
            {
               parentLane.getIntermediateEventSymbols().remove(nodeSymbol);
               newParentSymbol.getIntermediateEventSymbols().add(
                     (IntermediateEventSymbol) nodeSymbol);
            }
         }
      }
      else if (symbolType.equals(ModelerConstants.DATA_SYMBOL))
      {
         if (parentLane.getElementOid() != newParentSymbol.getElementOid())
         {
            parentLane.getDataSymbol().remove(nodeSymbol);

            newParentSymbol.getDataSymbol().add((DataSymbolType) nodeSymbol);
         }
      }
   }

   /**
    *
    * @param activitySymbol
    * @param gatewaySymbolJson
    */
   private void updateGatewaySymbol(ActivitySymbolType activitySymbol,
         JsonObject gatewaySymbolJson)
   {
      updateNodeSymbol(activitySymbol, gatewaySymbolJson);

      mapDeclaredProperties(activitySymbol, gatewaySymbolJson,
            propertiesMap.get(ActivitySymbolType.class));

      ActivityType gateway = activitySymbol.getActivity();
      JsonObject gatewayJson = gatewaySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      updateActivity(gateway, gatewayJson);
   }

   /**
    *
    * @param startEventSymbol
    * @param startEventSymbolJson
    */
   private void updateStartEventSymbol(StartEventSymbol startEventSymbol,
         JsonObject startEventSymbolJson)
   {
      JsonObject startEventJson = startEventSymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      updateNodeSymbol(startEventSymbol, startEventSymbolJson);

      if (null == startEventJson)
      {
         return;
      }

      // If no implementation is set for a None Event, the event symbol does not have a
      // Trigger

      if (hasNotJsonNull(startEventJson, ModelerConstants.EVENT_CLASS_PROPERTY)
            && startEventJson.get(ModelerConstants.EVENT_CLASS_PROPERTY)
                  .getAsString()
                  .equals(ModelerConstants.NONE_EVENT_CLASS_KEY)
            && hasNotJsonNull(startEventJson, ModelerConstants.IMPLEMENTATION_PROPERTY)
            && startEventJson.get(ModelerConstants.IMPLEMENTATION_PROPERTY)
                  .getAsString()
                  .equals("none"))
      {
         if (startEventSymbol.getTrigger() != null)
         {
            TriggerType trigger = startEventSymbol.getTrigger();
            ProcessDefinitionType process = ModelUtils.findContainingProcess(trigger);
            process.getTrigger().remove(trigger);
         }
         startEventSymbol.setTrigger(null);
      }
      else
      {
         TriggerType trigger = startEventSymbol.getTrigger();

         if (trigger == null)
         {
            // Trigger type does not really matter as it will be changed in the
            // updateTrigger method

            trigger = newManualTrigger(ModelUtils.findContainingProcess(startEventSymbol)) //
            .accessibleTo(
                  LaneParticipantUtil.getParticipant((LaneSymbol) startEventSymbol.eContainer()))
                  .build();

            // We do not have an indication for a name - generate from Type/Implementation?

            trigger.setName("");

            startEventSymbol.setTrigger(trigger);
         }

         updateTrigger(trigger, startEventJson);
      }
   }

   private void updateIntermediateEventSymbol(IntermediateEventSymbol eventSymbol,
         JsonObject eventSymbolJson)
   {
      JsonObject eventJson = eventSymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      updateNodeSymbol(eventSymbol, eventSymbolJson);

      if (null == eventJson)
      {
         return;
      }

      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(eventSymbol);
      JsonObject hostingConfig = null;
      if (hostActivity != null)
      {
         hostingConfig = EventMarshallingUtils.getEventHostingConfig(
               hostActivity, eventSymbol, jsonIo);
      }

      if (null == hostingConfig)
      {
         hostingConfig = new JsonObject();
      }

      EventHandlerType eventHandler = null;
      //TODO: hasNotJsonNull required here?
      if (hostActivity != null && hasNotJsonNull(hostingConfig, EventMarshallingUtils.PRP_EVENT_HANDLER_ID))
      {
         String eventHandlerId = extractAsString(hostingConfig, EventMarshallingUtils.PRP_EVENT_HANDLER_ID);
         eventHandler = findIdentifiableElement(hostActivity.getEventHandler(), eventHandlerId);
      }

      if (eventJson.has(ModelerConstants.BINDING_ACTIVITY_UUID))
      {
         // potentially detach from old host
         ProcessDefinitionType containingProcess = ModelUtils.findContainingProcess(eventSymbol);
         if (null != containingProcess)
         {
            ActivityType newHostActivity = ModelUtils.findIdentifiableElement(
                  containingProcess.getActivity(),
                  extractAsString(eventJson, ModelerConstants.BINDING_ACTIVITY_UUID));
            boolean isBoundary = false;

            if (hostActivity != newHostActivity)
            {
               if (hostActivity != null)
               {
                  EventMarshallingUtils.updateEventHostingConfig(hostActivity, eventSymbol, null);

                  if (EventMarshallingUtils.isIntermediateEventHost(hostActivity)
                       && EventMarshallingUtils.resolveHostedEvents(hostActivity).isEmpty())
                  {
                     // delete incoming transition connections
                     ModelElementEditingUtils.deleteIdentifiables(hostActivity.getInTransitions());

                     // delete associated activity
                     if (ActivityImplementationType.ROUTE_LITERAL.equals(hostActivity.getImplementation()))
                     {
                        if (newHostActivity != null)
                        {
                           ModelElementEditingUtils.deleteIdentifiables(CollectionUtils.intersect(
                                 hostActivity.getOutTransitions(), newHostActivity.getInTransitions()));
                        }
                        containingProcess.getActivity().remove(hostActivity);
                     }
                     else if (ActivityImplementationType.MANUAL_LITERAL.equals(hostActivity.getImplementation()))
                     {
                        EventMarshallingUtils.unTagAsIntermediateEventHost(hostActivity);
                        EventMarshallingUtils.deleteEventHostingConfig(hostActivity, eventSymbol);
                     }
                     isBoundary = true;
                  }
               }

               if (newHostActivity == null)
               {
                  if (eventHandler != null)
                  {
                     // TODO: (fh) this block wouldn't be necessary if we reuse the event handler
                     EventConditionTypeType type = eventHandler.getType();
                     if (type != null && PredefinedConstants.EXCEPTION_CONDITION.equals(type.getId()))
                     {
                        // (fh) error events not allowed as intermediate, so reset to timer
                        eventJson.addProperty(ModelerConstants.EVENT_CLASS_PROPERTY, PredefinedConstants.TIMER_CONDITION);

                        // (fh) preserve logHandler and interrupting properties
                        eventJson.addProperty(ModelerConstants.LOG_HANDLER_PROPERTY, eventHandler.isLogHandler());
                        eventJson.addProperty(ModelerConstants.INTERRUPTING_PROPERTY,
                              EventMarshallingUtils.encodeIsInterruptingEvent(eventHandler));
                     }
                  }

                  newHostActivity = EventMarshallingUtils.createHostActivity(containingProcess, "Intermediate Event");
                  EventMarshallingUtils.tagAsIntermediateEventHost(newHostActivity);
               }

               if (eventHandler != null)
               {
                  newHostActivity.getEventHandler().add(eventHandler);
               }

               for (TransitionConnectionType connection : eventSymbol.getOutTransitions())
               {
                  TransitionType transition = connection.getTransition();
                  if (transition != null)
                  {
                     transition.setFrom(newHostActivity);
                     if (isBoundary)
                     {
                        FeatureMap mixedNode = transition.getExpression().getMixed();
                        ModelUtils.setCDataString(mixedNode, "ON_BOUNDARY_EVENT("
                              + eventHandler.getId() + ')', true);
                     }
                     else
                     {
                        FeatureMap mixedNode = transition.getExpression().getMixed();
                        String expression = ModelUtils.getCDataString(mixedNode);
                        if (expression.startsWith("ON_BOUNDARY_EVENT"))
                        {
                           ModelUtils.setCDataString(mixedNode, "true", true);
                        }
                     }
                  }
               }

               EventMarshallingUtils.updateEventHostingConfig(newHostActivity,
                     eventSymbol, hostingConfig);

               hostActivity = newHostActivity;
            }
         }
         // attach to new host activity
      }

      // store model element state
      if (null != hostActivity)
      {
         String eventClass = extractAsString(eventJson, ModelerConstants.EVENT_CLASS_PROPERTY);
         if (null != eventHandler)
         {
            // verify handler still matches the given event class
            String currentEventClass = EventMarshallingUtils.encodeEventHandlerType(eventHandler.getType());
            if ((findContainingActivity(eventHandler) != hostActivity)
                  || (hasNotJsonNull(eventJson, ModelerConstants.EVENT_CLASS_PROPERTY) && !eventClass.equals(
                        currentEventClass)))
            {
               // TODO: (fh) try to reuse event handler instead of recreating

               // dispose current handler if it is out of sync, but carry over crucial
               // attributes
               mergeUndefinedProperty(eventHandler.getName(), eventJson,
                     ModelerConstants.NAME_PROPERTY);
               mergeUndefinedProperty(
                     ModelUtils.getDescriptionText(eventHandler.getDescription()),
                     eventJson, ModelerConstants.DESCRIPTION_PROPERTY);

               // TODO attributes

               hostingConfig.remove(EventMarshallingUtils.PRP_EVENT_HANDLER_ID);
               findContainingActivity(eventHandler).getEventHandler().remove(eventHandler);
               eventHandler = null;
            }
         }

         if (null == eventHandler)
         {
            // if possible, create an event handler defined by the event
            eventHandler = EventMarshallingUtils.createEventHandler(eventSymbol, hostActivity, hostingConfig, eventClass);
         }

         if (null != eventHandler)
         {

            EventMarshallingUtils.updateEventHandler(eventHandler, hostActivity, hostingConfig, eventJson);

            storeAttributes(eventHandler, eventJson, "carnot:engine:delayUnit");

            hostingConfig.remove(ModelerConstants.EVENT_CLASS_PROPERTY);
            hostingConfig.remove(ModelerConstants.THROWING_PROPERTY);
            hostingConfig.remove(ModelerConstants.INTERRUPTING_PROPERTY);
         }
         else
         {
            mergeProperty(eventJson, hostingConfig, ModelerConstants.NAME_PROPERTY);
            mergeProperty(eventJson, hostingConfig, ModelerConstants.DESCRIPTION_PROPERTY);
            mergeProperty(eventJson, hostingConfig, ModelerConstants.EVENT_CLASS_PROPERTY);
            mergeProperty(eventJson, hostingConfig, ModelerConstants.THROWING_PROPERTY);
            mergeProperty(eventJson, hostingConfig, ModelerConstants.INTERRUPTING_PROPERTY);
         }

         EventMarshallingUtils.updateEventHostingConfig(hostActivity, eventSymbol, hostingConfig);
         if (null != eventHandler)
         {
            EventMarshallingUtils.bindEvent(eventHandler, eventSymbol);
         }
      }
   }

   private void mergeProperty(JsonObject source, JsonObject target, String propertyName)
   {
      if (source.has(propertyName))
      {
         target.add(propertyName, source.get(propertyName));
      }
   }

   private void mergeUndefinedProperty(String sourceValue, JsonObject target,
         String propertyName)
   {
      if ( !target.has(propertyName))
      {
         target.addProperty(propertyName, sourceValue);
      }
   }

   /**
    *
    * @param endEventSymbol
    * @param endEventSymbolJson
    */
   private void updateEndEventSymbol(EndEventSymbol endEventSymbol,
         JsonObject endEventSymbolJson)
   {
      JsonObject endEventJson = endEventSymbolJson.getAsJsonObject("modelElement");

      updateNodeSymbol(endEventSymbol, endEventSymbolJson);

      mapDeclaredProperties(endEventSymbol.getModelElement(),
            endEventSymbolJson.getAsJsonObject("modelElement"),
            propertiesMap.get(EndEventSymbol.class));

      if (null != endEventJson)
      {
         // store model element state
         ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(endEventSymbol);
         if (null != hostActivity)
         {
            updateIdentifiableElement(hostActivity, endEventJson);
            storeDescription(hostActivity, endEventJson);
            storeAttributes(hostActivity, endEventJson);
         }
      }
   }

   /**
    *
    * @param trigger
    * @param triggerJson
    */
   private void updateTrigger(TriggerType trigger, JsonObject triggerJson)
   {
      if (hasNotJsonNull(triggerJson, ModelerConstants.NAME_PROPERTY))
      {
         trigger.setName(triggerJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      if (hasNotJsonNull(triggerJson, ModelerConstants.IMPLEMENTATION_PROPERTY))
      {
         logger.debug("===> Implementation: "
               + triggerJson.get(ModelerConstants.IMPLEMENTATION_PROPERTY).getAsString());
         trigger.setType(ModelBuilderFacade.findTriggerType(
               ModelUtils.findContainingModel(trigger),
               triggerJson.get(ModelerConstants.IMPLEMENTATION_PROPERTY).getAsString()));
         logger.debug("===> Implementation: " + trigger.getType());
      }

      if (isUserTrigger(trigger))
      {
         storeAttributes(trigger, triggerJson);
      }
      else
      // remove participant if set
      {
         AttributeUtil.setReference(trigger, PredefinedConstants.PARTICIPANT_ATT, null);
         storeAttributes(trigger, triggerJson, PredefinedConstants.PARTICIPANT_ATT);
      }

      storeDescription(trigger, triggerJson);

      // A few BPMN properties

      if (hasNotJsonNull(triggerJson, ModelerConstants.EVENT_CLASS_PROPERTY))
      {
         ModelBuilderFacade.setAttribute(trigger, "eventClass",
               triggerJson.get(ModelerConstants.EVENT_CLASS_PROPERTY).getAsString());

         if (triggerJson.get(ModelerConstants.EVENT_CLASS_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.NONE_EVENT_CLASS_KEY))
         {
            trigger.setType(ModelBuilderFacade.findTriggerType(
                  ModelUtils.findContainingModel(trigger), "manual"));
         }
      }

      if (hasNotJsonNull(triggerJson, ModelerConstants.THROWING_PROPERTY))
      {
         ModelBuilderFacade.setBooleanAttribute(trigger, "throwing",
               triggerJson.get(ModelerConstants.THROWING_PROPERTY).getAsBoolean());
      }

      if (hasNotJsonNull(triggerJson, ModelerConstants.INTERRUPTING_PROPERTY))
      {
         ModelBuilderFacade.setBooleanAttribute(trigger, "interrupting",
               triggerJson.get(ModelerConstants.INTERRUPTING_PROPERTY).getAsBoolean());
      }

      if (triggerJson.has(ModelerConstants.PARTICIPANT_FULL_ID))
      {
         String participantFullId = extractString(triggerJson,
               ModelerConstants.PARTICIPANT_FULL_ID);

         if (participantFullId != null && isUserTrigger(trigger))
         {
            IModelParticipant performer = getModelBuilderFacade().findParticipant(participantFullId);
            ModelType model = ModelUtils.findContainingModel(trigger);
            if (model != ModelUtils.findContainingModel(performer))
            {
               List<? extends IModelParticipant> localParticipants = performer instanceof OrganizationType ? model.getOrganization() : model.getRole();
               performer = ModelUtils.findIdentifiableElement(localParticipants, performer.getId());
            }
            AttributeUtil.setReference(trigger, PredefinedConstants.PARTICIPANT_ATT, performer);
         }
      }

      if (hasNotJsonNull(triggerJson, ModelerConstants.PARAMETER_MAPPINGS_PROPERTY))
      {
         JsonArray parameterMappings = triggerJson.get(
               ModelerConstants.PARAMETER_MAPPINGS_PROPERTY).getAsJsonArray();

         trigger.getAccessPoint().clear();
         trigger.getParameterMapping().clear();

         for (int n = 0; n < parameterMappings.size(); ++n)
         {
            JsonObject parameterMappingJson = parameterMappings.get(n).getAsJsonObject();

            String name = parameterMappingJson.get(ModelerConstants.NAME_PROPERTY)
                  .getAsString();

            String direction = parameterMappingJson.get(
                  ModelerConstants.DIRECTION_PROPERTY).getAsString();

            AccessPointType accessPoint = null;

            if (hasNotJsonNull(parameterMappingJson, ModelerConstants.DATA_TYPE_PROPERTY))
            {
               String dataType = parameterMappingJson.get(
                     ModelerConstants.DATA_TYPE_PROPERTY).getAsString();
               if (dataType.equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
               {
                  String primitiveDataType = parameterMappingJson.get(
                        ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY).getAsString();

                  // ID is set to null to enforce ID generation at server side
                  accessPoint = getModelBuilderFacade().createPrimitiveAccessPoint(
                        trigger, null, name, primitiveDataType, direction);
               }
               else if (dataType.equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
               {
                  String structuredDataFullId = null;

                  if (hasNotJsonNull(parameterMappingJson, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                  {
                     structuredDataFullId = parameterMappingJson.get(
                           ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                           .getAsString();
                  }

                  // ID is set to null to enforce ID generation at server side
                  accessPoint = getModelBuilderFacade().createStructuredAccessPoint(
                        trigger, null, name, structuredDataFullId, direction);
               }
               else if (dataType.equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
               {

               // ID is set to null to enforce ID generation at server side
                  accessPoint = getModelBuilderFacade().createDocumentAccessPoint(
                        trigger, null, name, direction);

                  String structuredDataFullId = null;

                  if (hasNotJsonNull(parameterMappingJson, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                  {
                     structuredDataFullId = parameterMappingJson.get(
                           ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                           .getAsString();

                     if(!ModelerConstants.TO_BE_DEFINED.equals(structuredDataFullId))
                     {
                        TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(
                              structuredDataFullId);

                        if (typeDeclaration != null)
                        {
                           StructuredTypeUtils.setStructuredAccessPointAttributes(
                                 accessPoint, typeDeclaration);
                        }
                     }
                  }
               }
            }

            // TODO Attributes storage missing?

            storeDescription(accessPoint, parameterMappingJson);

            if (hasNotJsonNull(parameterMappingJson, ModelerConstants.DATA_FULL_ID_PROPERTY))
            {
               String dataPath = null;
               String dataFullID = parameterMappingJson.get(
                     ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();
               if (hasNotJsonNull(parameterMappingJson, ModelerConstants.DATA_PATH_PROPERTY))
               {
                  dataPath = parameterMappingJson.get(ModelerConstants.DATA_PATH_PROPERTY)
                        .getAsString();
               }

               getModelBuilderFacade().createParameterMapping(trigger,
                     accessPoint.getId(), dataFullID, dataPath);
            }
         }
      }
   }

   /**
    *
    * @param trigger
    * @return
    */
   private boolean isUserTrigger(TriggerType trigger)
   {
      return trigger.getType() == null
            || PredefinedConstants.MANUAL_TRIGGER.equals(trigger.getType().getId())
            || PredefinedConstants.SCAN_TRIGGER.equals(trigger.getType().getId());
   }

   /**
    *
    * @param annotationSymbol
    * @param annotationSymbolJson
    */
   private void updateAnnotationSymbol(AnnotationSymbolType annotationSymbol,
         JsonObject annotationSymbolJson)
   {
      updateNodeSymbol(annotationSymbol, annotationSymbolJson);

      String content = null;

      if (annotationSymbolJson.has(ModelerConstants.CONTENT_PROPERTY))
      {
         content = extractString(annotationSymbolJson, ModelerConstants.CONTENT_PROPERTY);
      }

      if (StringUtils.isNotEmpty(content))
      {
         TextType text = AbstractElementBuilder.F_CWM.createTextType();

         text.getMixed().add(FeatureMapUtil.createRawTextEntry(content));
         annotationSymbol.setText(text);
      }
   }

   /**
    * @param application
    * @param applicationJson
    */
   private void updateApplication(ApplicationType application, JsonObject applicationJson)
   {
      updateIdentifiableElement(application, applicationJson);

      mapDeclaredProperties(application, applicationJson,
            propertiesMap.get(ApplicationType.class));

      if (hasNotJsonNull(applicationJson, ModelerConstants.TYPE_PROPERTY))
      {
         JsonPrimitive typeJson = applicationJson.get(ModelerConstants.TYPE_PROPERTY).getAsJsonPrimitive();

         if (!application.getType().getId().equals(typeJson.getAsString()))
      {
            ModelType modelType = ModelUtils.findContainingModel(application);
            ApplicationTypeType type = getModelBuilderFacade()
               .findApplicationTypeType(modelType, typeJson.getAsString());

            if (type != null)
            {
               application.setType(type);
            }
      }
      }

      // (fh) must update before changing the attributes so we can compare with old values.
      if (WebServiceApplicationUtils.isWebServiceApplication(application))
      {
         WebServiceApplicationUtils.updateWebServiceApplication(modelingSession.uuidMapper(),
               application, applicationJson);
      }

      storeAttributes(application, applicationJson);
      storeDescription(application, applicationJson);

      if (hasNotJsonNull(applicationJson, ModelerConstants.CONTEXTS_PROPERTY))
      {
         // TODO This is too invasive as client may ship incomplete context(s)

         application.getContext().clear();
         application.getAccessPoint().clear();

         JsonObject contextsJson = applicationJson.get(ModelerConstants.CONTEXTS_PROPERTY)
               .getAsJsonObject();

         for (Map.Entry<String, ? > entry : contextsJson.entrySet())
         {
            String contextId = entry.getKey();

            IAccessPointOwner context = application;

            if ( !ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY.equals(contextId))
            {
               context = getModelBuilderFacade().createApplicationContext(application,
                     contextId);
            }

            JsonObject contextJson = contextsJson.get(contextId).getAsJsonObject();

            storeAttributes(context, contextJson);

            if (hasNotJsonNull(contextJson, ModelerConstants.ACCESS_POINTS_PROPERTY))
            {
               JsonArray accessPointsJson = contextJson.get(
                     ModelerConstants.ACCESS_POINTS_PROPERTY).getAsJsonArray();

               List<EObject> accessPoints = new ArrayList<EObject>();
               for (int n = 0; n < accessPointsJson.size(); ++n)
               {
                  JsonObject accessPointJson = accessPointsJson.get(n).getAsJsonObject();

                  boolean predefined = false;

                  if (hasNotJsonNull(accessPointJson, ModelerConstants.ATTRIBUTES_PROPERTY))
                  {
                     JsonObject attributeJson = accessPointJson.get(ModelerConstants.ATTRIBUTES_PROPERTY).getAsJsonObject();
                     if (hasNotJsonNull(attributeJson, "stardust:predefined"))
                     {
                     // TODO : create ModelerConstans entry
                     predefined = attributeJson.get("stardust:predefined").getAsBoolean();
                     }
                  }

                  String id = null;

                  if (predefined)
                  {
                     id = accessPointJson.get(ModelerConstants.ID_PROPERTY).getAsString();
                  }

                  String name = accessPointJson.get(ModelerConstants.NAME_PROPERTY)
                        .getAsString();
                  String direction = accessPointJson.get(
                        ModelerConstants.DIRECTION_PROPERTY).getAsString();

                  logger.debug("Direction: " + direction);

                  AccessPointType accessPoint = null;

                  if (hasNotJsonNull(accessPointJson, ModelerConstants.DATA_TYPE_PROPERTY))
                  {
                     String dataType = accessPointJson.get(
                           ModelerConstants.DATA_TYPE_PROPERTY).getAsString();

                     if (dataType.equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
                     {
                        String primitiveDataType = accessPointJson.get(
                              ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY)
                              .getAsString();
                        String structuredDataFullId = null;
                     if (hasNotJsonNull(accessPointJson, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                        {
                           structuredDataFullId = accessPointJson.get(
                                 ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                                 .getAsString();
                           accessPoint = getModelBuilderFacade().createStructuredAccessPoint(
                                   context, id, name, structuredDataFullId, direction);

                             TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(
                                   structuredDataFullId);

                             if (typeDeclaration != null)
                             {
                                StructuredTypeUtils.setStructuredAccessPointAttributes(
                                      accessPoint, typeDeclaration);
                             }
                        }
                     else
                     {
                        accessPoint = getModelBuilderFacade().createPrimitiveAccessPoint(
                                    context, id, name, primitiveDataType, direction);
                     }
                     }
                     else if (dataType.equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
                     {
                        String structuredDataFullId = null;

                        if (hasNotJsonNull(accessPointJson, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                        {
                           structuredDataFullId = accessPointJson.get(
                                 ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                                 .getAsString();
                        }

                        accessPoint = getModelBuilderFacade().createStructuredAccessPoint(
                              context, id, name, structuredDataFullId, direction);

                        TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(
                              structuredDataFullId);

                        if (typeDeclaration != null)
                        {
                           StructuredTypeUtils.setStructuredAccessPointAttributes(
                                 accessPoint, typeDeclaration);
                        }
                     }
                     else if (dataType.equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
                     {
                        accessPoint = getModelBuilderFacade().createDocumentAccessPoint(
                              context, id, name, direction);

                        String structuredDataFullId = null;

                        if (hasNotJsonNull(accessPointJson, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                        {
                           structuredDataFullId = accessPointJson.get(
                                 ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                                 .getAsString();

                           TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(
                                 structuredDataFullId);

                           if (typeDeclaration != null)
                           {
                              StructuredTypeUtils.setStructuredAccessPointAttributes(
                                    accessPoint, typeDeclaration);
                           }
                        }
                     }
                     accessPoints.add(accessPoint);
                  }

                  storeAttributes(accessPoint, accessPointJson);
                  storeDescription(accessPoint, accessPointJson);
               }
            }
         }
      }
   }

   /**
    * @param typeDeclaration
    * @param json
    */
   private void updateTypeDeclaration(TypeDeclarationType typeDeclaration, JsonObject json)
   {
      storeAttributes(typeDeclaration, json);
      storeDescription(typeDeclaration, json);

      String oldId = typeDeclaration.getId();
      if (updateElementNameAndId(typeDeclaration,
            XpdlPackage.eINSTANCE.getTypeDeclarationType_Id(),
            XpdlPackage.eINSTANCE.getTypeDeclarationType_Name(), json))
      {
         // propagate ID change
         if (null != typeDeclaration.getSchemaType())
         {
            XSDSchema schema = typeDeclaration.getSchemaType().getSchema();

            // update target namespace
            String oldTargetNs = TypeDeclarationUtils.computeTargetNamespace(
                  ModelUtils.findContainingModel(typeDeclaration), oldId);
            String newTargetNs = TypeDeclarationUtils.computeTargetNamespace(
                  ModelUtils.findContainingModel(typeDeclaration),
                  typeDeclaration.getId());

            if (schema.getTargetNamespace().equals(oldTargetNs))
            {
               schema.setTargetNamespace(newTargetNs);

               while (schema.getQNamePrefixToNamespaceMap().containsValue(oldTargetNs))
               {
                  for (String nsPrefix : schema.getQNamePrefixToNamespaceMap().keySet())
                  {
                     if (schema.getQNamePrefixToNamespaceMap()
                           .get(nsPrefix)
                           .equals(oldTargetNs))
                     {
                        schema.getQNamePrefixToNamespaceMap().remove(nsPrefix);
                        // restart iteration after edit
                        break;
                     }
                  }
               }
               String newNsPrefix = TypeDeclarationUtils.computePrefix(
                     typeDeclaration.getId().toLowerCase(),
                     schema.getQNamePrefixToNamespaceMap().keySet());
               schema.getQNamePrefixToNamespaceMap().put(newNsPrefix, newTargetNs);
            }

            // update location, if internal
            if (TypeDeclarationUtils.isInternalSchema(typeDeclaration))
            {
               schema.setSchemaLocation(StructuredDataConstants.URN_INTERNAL_PREFIX
                     + typeDeclaration.getId());
               if (null != typeDeclaration.getExternalReference())
               {
                  ExternalReferenceType schemaRef = typeDeclaration.getExternalReference();
                  schemaRef.setLocation(schema.getSchemaLocation());
               }
            }

            // update "main" element end type
            List<XSDElementDeclaration> changedElements = newArrayList();
            for (XSDElementDeclaration elementDeclaration : schema.getElementDeclarations())
            {
               if (elementDeclaration.getName().equals(oldId))
               {
                  // file for later change to avoid concurrent modification exceptions
                  changedElements.add(elementDeclaration);
               }
            }
            for (XSDElementDeclaration elementDeclaration : changedElements)
            {
               if ((null != elementDeclaration.getType())
                     && elementDeclaration.getTypeDefinition().getName().equals(oldId))
               {
                  elementDeclaration.getTypeDefinition().setName(typeDeclaration.getId());
               }

               elementDeclaration.setName(typeDeclaration.getId());
            }

            List<XSDTypeDefinition> renamedTypes = newArrayList();
            for (XSDTypeDefinition typeDefinition : schema.getTypeDefinitions())
            {
               if (typeDefinition.getName().equals(oldId))
               {
                  // file for later change to avoid concurrent modification exceptions
                  renamedTypes.add(typeDefinition);
               }
            }
            for (XSDTypeDefinition typeDefinition : renamedTypes)
            {
               typeDefinition.setName(typeDeclaration.getId());
            }

            // TODO adjust cross references

            // adjust underlying DOM
            schema.updateElement(true);

            TypeDeclarationsType container = (TypeDeclarationsType) typeDeclaration.eContainer();
            for (TypeDeclarationType decl : container.getTypeDeclaration())
            {
               if ( !decl.equals(typeDeclaration))
               {
                  XpdlTypeType type = decl.getDataType();
                  if (TypeDeclarationUtils.fixImport(decl, typeDeclaration.getId(), oldId)
                        && type instanceof SchemaTypeType)
                  {
                     TypeDeclarationUtils.updateTypeDefinition(decl,
                           typeDeclaration.getId(), oldId);
                  }
               }
            }
         }

         // TODO
         // move this into a new ChangePostprocessor and send notification to the client
         if (!typeDeclaration.getId().equals(oldId))
         {
            ModelType model = ModelUtils.findContainingModel(typeDeclaration);
            for (ProcessDefinitionType process : model.getProcessDefinition())
            {
               FormalParametersType parametersType = process.getFormalParameters();
               if (parametersType != null)
               {
                  for (FormalParameterType type : parametersType.getFormalParameter())
                  {
                     DataTypeType dataType = type.getDataType();
                     DeclaredTypeType declaredType = dataType.getDeclaredType();
                     if (declaredType != null)
                     {
                        String declaredTypeId = declaredType.getId();
                        if ( !StringUtils.isEmpty(declaredTypeId)
                              && declaredTypeId.equals(oldId))
                        {
                           declaredType.setId(typeDeclaration.getId());
                        }
                     }
                  }
               }
            }
         }
      }

      JsonObject declarationJson = json.getAsJsonObject("typeDeclaration");
      JsonObject typeJson = (null != declarationJson)
            ? declarationJson.getAsJsonObject("type")
            : null;
      if ((null != typeJson)
            && "SchemaType".equals(typeJson.getAsJsonPrimitive("classifier")
                  .getAsString()))
      {
         if (hasNotJsonNull(json, ModelerConstants.ATTRIBUTES_PROPERTY))
         {
             JsonObject attributeJson = json.get(ModelerConstants.ATTRIBUTES_PROPERTY).getAsJsonObject();
             if (hasNotJsonNull(attributeJson, PredefinedConstants.CLASS_NAME_ATT))
             {
                String className=attributeJson.get(PredefinedConstants.CLASS_NAME_ATT).getAsString();
                JsonArray facets = loadEnumForStructuredType(className);
                if (null != facets)
                {
                   JsonObject schemaJson = declarationJson.getAsJsonObject("schema");
                   JsonArray types = GsonUtils.safeGetAsJsonArray(schemaJson, "types");
                   JsonArray elements = GsonUtils.safeGetAsJsonArray(schemaJson, "elements");
                   for (JsonElement entry : types)
                   {
                      if (entry instanceof JsonObject)
                      {
                         JsonObject defJson = (JsonObject) entry;
                         defJson.add("facets", facets);
                      }
                   }
                   for (JsonElement entry : elements)
                   {
                      if (entry instanceof JsonObject)
                      {
                         JsonObject defJson = (JsonObject) entry;
                         defJson.add("facets", facets);
                      }
                   }
                }
             }
         }
         XsdSchemaUtils.updateXSDSchemaType(getModelBuilderFacade(),
               typeDeclaration.getSchemaType(), declarationJson.getAsJsonObject("schema"));
      }

      // ExternalReference ?
   }

   /**
    * @param role
    * @param roleJson
    */
   private void updateRole(RoleType role, JsonObject roleJson)
   {

      if (hasNotJsonNull(roleJson, ModelerConstants.CARDINALITY))
      {
         int cardinality = 0;
         try
         {
            if (StringUtils.isEmpty(roleJson.get(ModelerConstants.CARDINALITY)
                  .getAsString())
                  || (Integer.parseInt(roleJson.get(ModelerConstants.CARDINALITY)
                        .getAsString()) <= 0))
            {
               cardinality = 0;
            }
            else
            {
               cardinality = roleJson.get(ModelerConstants.CARDINALITY).getAsInt();
            }
         }
         catch (NumberFormatException e)
         {
            // Do nothing
         }
         role.setCardinality(cardinality);
      }

      updateIdentifiableElement(role, roleJson);

      mapDeclaredProperties(role, roleJson, propertiesMap.get(RoleType.class));
      storeAttributes(role, roleJson);
      storeDescription(role, roleJson);
   }

   /**
    * @param conditionalPerformer
    * @param conditionalPerformerJson
    */
   private void updateConditionalPerformer(ConditionalPerformerType conditionalPerformer,
         JsonObject conditionalPerformerJson)
   {
      updateIdentifiableElement(conditionalPerformer, conditionalPerformerJson);

      mapDeclaredProperties(conditionalPerformer, conditionalPerformerJson,
            propertiesMap.get(ConditionalPerformerType.class));

      if (hasNotJsonNull(conditionalPerformerJson, ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY)
            && !(conditionalPerformerJson.get(
                  ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY).getAsString().equals(ModelerConstants.TO_BE_DEFINED)))
      {
         ModelType model = ModelUtils.findContainingModel(conditionalPerformer);
         DataType data = getModelBuilderFacade().importData(
               model,
               conditionalPerformerJson.get(
                     ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY).getAsString());
         conditionalPerformer.setData(data);
      }

      storeAttributes(conditionalPerformer, conditionalPerformerJson);
      storeDescription(conditionalPerformer, conditionalPerformerJson);
   }

   /**
    * @param organization
    * @param organizationJson
    */
   private void updateOrganization(OrganizationType organization,
         JsonObject organizationJson)
   {
      updateIdentifiableElement(organization, organizationJson);

      mapDeclaredProperties(organization, organizationJson,
            propertiesMap.get(OrganizationType.class));

      // import bound data if from external model
      JsonElement jsonAttributes = organizationJson.get(ModelerConstants.ATTRIBUTES_PROPERTY);
      if (jsonAttributes instanceof JsonObject)
      {
         JsonElement jsonDataId = ((JsonObject) jsonAttributes).get(PredefinedConstants.BINDING_DATA_ID_ATT);
         if (jsonDataId instanceof JsonPrimitive)
         {
            String dataId = jsonDataId.getAsString();
            int ix = dataId.indexOf(':');
            if (ix > 0)
            {
               ModelType model = ModelUtils.findContainingModel(organization);
               if (dataId.substring(0, ix).equals(model.getId()))
               {
                  // same model, no need to qualify
                  dataId = dataId.substring(ix + 1);
               }
               else
               {
                  DataType data = getModelBuilderFacade().importData(model, dataId);
                  if (data != null)
                  {
                     dataId = data.getId();
                  }
               }
               ((JsonObject) jsonAttributes).addProperty(PredefinedConstants.BINDING_DATA_ID_ATT, dataId);
            }
         }
      }

      storeAttributes(organization, organizationJson);
      storeDescription(organization, organizationJson);

      if (hasNotJsonNull(organizationJson, ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY))
      {
         getModelBuilderFacade().updateTeamLead(
               organization,
               organizationJson.get(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY)
                     .getAsString());
      }
   }

   /**
    * @param data
    * @param dataJson
    */
   private void updateData(DataType data, JsonObject dataJson)
   {
      updateIdentifiableElement(data, dataJson);

      mapDeclaredProperties(data, dataJson, propertiesMap.get(DataType.class));
      storeAttributes(data, dataJson);
      storeDescription(data, dataJson);

      if (hasNotJsonNull(dataJson, ModelerConstants.DATA_TYPE_PROPERTY))
      {
         if (dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
         {
            if (dataJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
            {
               String fullTypeId = dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY).getAsString();
               TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(fullTypeId);
               // For Java bound ENUM's create primitive else structured Data
               if (getModelBuilderFacade().isEnumerationJavaBound(typeDeclaration))
               {
                  getModelBuilderFacade().convertDataType(data, ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
                  String typeFullID = dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY).getAsString();
                  getModelBuilderFacade().updateTypeForPrimitive(data, typeFullID);
                  getModelBuilderFacade().updatePrimitiveData(data, ModelerConstants.ENUM_PRIMITIVE_DATA_TYPE);
               }
               else
               {
                  getModelBuilderFacade().convertDataType(data, ModelerConstants.STRUCTURED_DATA_TYPE_KEY);
                  getModelBuilderFacade().updateStructuredDataType(data,
                        dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY).getAsString());
               }
            }
            else
            {
               getModelBuilderFacade().convertDataType(data, ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
               getModelBuilderFacade().updatePrimitiveData(data,
                     dataJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY).getAsString());
            }
         }
         else if (dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
         {
            getModelBuilderFacade().convertDataType(data,
                  ModelerConstants.STRUCTURED_DATA_TYPE_KEY);
            getModelBuilderFacade().updateStructuredDataType(
                  data,
                  dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                        .getAsString());
         }
         else if (dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
         {
            getModelBuilderFacade().convertDataType(data,
                  ModelerConstants.DOCUMENT_DATA_TYPE_KEY);
            getModelBuilderFacade().updateDocumentDataType(
                  data,
                  dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                        .getAsString());
         }
         else
         {
            logger.debug("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
            logger.debug("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
            logger.debug("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
         }

         // Reset default value attribute in case of a data type change / primitive type
         // change
         // as the previously set default may be invalid.
         ModelBuilderFacade.setAttribute(data, "carnot:engine:defaultValue", "");
      }
   }

   /**
    *
    * @param className
    * @return
    */
   private JsonArray loadEnumForStructuredType(String className)
   {
      if (StringUtils.isNotEmpty(className))
      {
         JsonArray facets = new JsonArray();
         Class< ? > clsTarget = null;
         try
         {
            clsTarget = modelingSession.classLoaderProvider().classLoader().loadClass(className);
            if (null != clsTarget)
            {
               Object[] consts = clsTarget.getEnumConstants();
               for (Object obj : consts)
               {
                  // Create facets object reading the ENUM values
                  JsonObject enumObj = new JsonObject();
                  enumObj.addProperty("name", obj.toString());
                  enumObj.addProperty("classifier", "enumeration");
                  facets.add(enumObj);
               }
               return facets;
            }
         }
         catch (Exception e)
         {
            // TODO: handle exception
         }
      }
      return null;
   }

   /**
    * @param model
    * @param modelJson
    */
   private void updateModel(ModelType model, JsonObject modelJson)
   {
      updateIdentifiableElement(model, modelJson);

      mapDeclaredProperties(model, modelJson, propertiesMap.get(ModelType.class));
      storeAttributes(model, modelJson);
      // Store model description
      if (modelJson.has(ModelerConstants.DESCRIPTION_PROPERTY))
      {
         String description = extractString(modelJson,
               ModelerConstants.DESCRIPTION_PROPERTY);
         if (null != description)
         {
            DescriptionType dt = AbstractElementBuilder.F_CWM.createDescriptionType();
            dt.getMixed().add(FeatureMapUtil.createRawTextEntry(description));
            model.setDescription(dt);
         }
      }
   }


   /**
    *
    * @param element
    * @param elementJson
    * @param elementProperties
    */
   private void mapDeclaredProperties(EObject element, JsonObject elementJson,
         String[] elementProperties)
   {
      if ((element != null) && !isEmpty(elementProperties))
      {
         for (String property : elementProperties)
         {
            if (elementJson.has(property))
            {
               mapProperty(element, elementJson, property);
            }
         }
      }
   }

   /**
    *
    * @param targetElement
    * @param request
    * @param property
    */
   private void mapProperty(EObject targetElement, JsonObject request, String property)
   {
      if (request.has(property))
      {
         logger.debug("Setting property " + property + " of value "
               + request.get(property) + " on object " + targetElement);

         try
         {
            // TODO Boolean

            Method getter = targetElement.getClass()
                  .getMethod(
                        "get" + ("" + property.charAt(0)).toUpperCase()
                              + property.substring(1), new Class[] {});
            Method setter = targetElement.getClass()
                  .getMethod(
                        "set" + ("" + property.charAt(0)).toUpperCase()
                              + property.substring(1), getter.getReturnType());

            // TODO Consider other types, possibly even keys

            if (String.class.isAssignableFrom(getter.getReturnType()))
            {
               if (hasNotJsonNull(request, property))
               {
                  logger.debug("Invoking " + setter.getName()
                        + " with property value " + request.get(property).getAsString());
                  setter.invoke(targetElement, request.get(property).getAsString());
               }
               else
               {
                  logger.debug("Invoking " + setter.getName() + " with null");
                  setter.invoke(targetElement, new Object[] {null});
               }
            }
         }
         catch (SecurityException e)
         {
            throw new RuntimeException(e);
         }
         catch (NoSuchMethodException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalArgumentException e)
         {
            throw new RuntimeException(e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException(e);
         }
         catch (InvocationTargetException e)
         {
            throw new RuntimeException(e);
         }
      }
      else
      {
         logger.warn("No value for property " + property);
      }
   }

   /**
    *
    * @param json
    * @param element
    * @throws JSONException
    */
   private void storeAttributes(EObject element, JsonObject json, String ... excludedKeys)
   {
      // Extract JSON elements which are stored in Extended Attributes

      if (json.has(ModelerConstants.COMMENTS_PROPERTY))
      {
         JsonArray commentsJson = json.getAsJsonArray(ModelerConstants.COMMENTS_PROPERTY);
         JsonObject holderJson = new JsonObject();

         holderJson.add(ModelerConstants.COMMENTS_PROPERTY, commentsJson);

         ModelBuilderFacade.setAttribute(element, "documentation:comments",
               jsonIo.writeJsonObject(holderJson));
      }

      if ( !json.has(ModelerConstants.ATTRIBUTES_PROPERTY))
      {
         return;
      }

      JsonObject attributes = json.getAsJsonObject(ModelerConstants.ATTRIBUTES_PROPERTY);

      if (attributes != null)
      {
         List<String> excluded = Arrays.asList(excludedKeys);
         for (Map.Entry<String, ? > entry : attributes.entrySet())
         {
            String key = entry.getKey();
            if (excluded.contains(key))
            {
               continue;
            }

            JsonElement jsonValue = attributes.get(key);

            //Infer the ruleSetId into the "hidden" drools application
            if (key.equals(ModelerConstants.RULE_SET_ID))
            {
               if (element instanceof ActivityType)
               {
                  ActivityType activity = (ActivityType) element;
                  if (activity.getApplication() != null)
                  {
                     ApplicationType application = (ApplicationType) activity.getApplication();
                     ModelBuilderFacade.setAttribute(application,
                           ModelerConstants.RULE_SET_ID, jsonValue.getAsString());
                  }
               }
               //continue;
            }

            if (jsonValue.isJsonNull())
            {
               logger.debug("Setting extended attribute " + key + " to null.");
               ModelBuilderFacade.setAttribute(element, key, null);
            }
            else if (jsonValue.getAsJsonPrimitive().isBoolean())
            {
               ModelBuilderFacade.setBooleanAttribute(element, key,
                     jsonValue.getAsBoolean());
            }
            else
            {
               String stringValue = jsonValue.getAsString();

               // TODO Trick to create document

               if (key.equals("documentation:externalDocumentUrl")
                     && jsonValue.getAsString().equals("@CREATE"))
               {
                  ModelBuilderFacade.setAttribute(element, key,
                        createModelElementDocumentation(json));
               }
               else if (key.equals("carnot:engine:delay"))
               {
                  ModelElementEditingUtils.setPeriodAttribute((IExtensibleElement) element, stringValue,
                        GsonUtils.safeGetAsString(attributes, "carnot:engine:delayUnit"));
               }
               else if (key.equals(PredefinedConstants.VALID_FROM_ATT))
               {
                  ModelBuilderFacade.setTimestampAttribute(
                        (IExtensibleElement) element, key, stringValue);
               }
               else
               {
                  logger.debug("Setting extended attribute " + key + " to " + stringValue);
                  ModelBuilderFacade.setAttribute(element, key, stringValue);
               }
            }
         }
      }
   }

   /**
    *
    * @param modelElementJson
    * @param element
    */
   public static void storeDescription(IIdentifiableModelElement element,
         JsonObject modelElementJson)
   {
      String description = null;

      if (modelElementJson.has(ModelerConstants.DESCRIPTION_PROPERTY))
      {
         description = extractString(modelElementJson,
               ModelerConstants.DESCRIPTION_PROPERTY);
      }

      if (null != description)
      {
         DescriptionType dt = AbstractElementBuilder.F_CWM.createDescriptionType();
         dt.getMixed().add(FeatureMapUtil.createRawTextEntry(description));
         element.setDescription(dt);
      }
   }

   /**
    *
    * @param modelElementJson
    * @param element
    */
   private void storeDescription(TypeDeclarationType element, JsonObject modelElementJson)
   {
      String description = null;
      if (modelElementJson.has(ModelerConstants.DESCRIPTION_PROPERTY))
      {
         description = extractString(modelElementJson,
               ModelerConstants.DESCRIPTION_PROPERTY);

         if ( !isEmpty(description))
         {
            element.setDescription(description);
         }
         else
         {
            element.setDescription(null);
         }
      }
   }

   /**
    *
    * @param orientation
    * @return
    */
   private String mapAnchorOrientation(int orientation)
   {
      if (orientation == ModelerConstants.NORTH_KEY)
      {
         return "top";
      }
      else if (orientation == ModelerConstants.EAST_KEY)
      {
         return "right";
      }
      else if (orientation == ModelerConstants.SOUTH_KEY)
      {
         return "bottom";
      }
      else if (orientation == ModelerConstants.WEST_KEY)
      {
         return "left";
      }

      throw new IllegalArgumentException("Illegal orientation key " + orientation + ".");
   }

   /**
    *
    * @return
    */
   private ModelBuilderFacade getModelBuilderFacade()
   {
      if (modelBuilderFacade == null)
      {
         modelBuilderFacade = new ModelBuilderFacade(modelingSession.modelManagementStrategy());
      }

      return modelBuilderFacade;
   }

   /**
    *
    * @param json
    * @param memberName
    * @return
    */
   private static String extractString(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);

      return (null != member) && member.isJsonPrimitive()
            && member.getAsJsonPrimitive().isString()
            ? member.getAsString()
            : (String) null;
   }

   /**
   *
   */
   private String createModelElementDocumentation(JsonObject json)
   {
      // TODO Make folder structure

      String fileName = extractString(json, ModelerConstants.TYPE_PROPERTY) + "-"
            + extractString(json, ModelerConstants.ID_PROPERTY) + ".html";

      DocumentInfo documentInfo = DmsUtils.createDocumentInfo(fileName);
      documentInfo.setOwner(getServiceFactory().getWorkflowService()
            .getUser()
            .getAccount());
      documentInfo.setContentType(MediaType.TEXT_HTML_VALUE);
      Document document = getDocumentManagementService().getDocument(
            MODELING_DOCUMENTS_DIR + fileName);

      if (null == document)
      {
         // TODO Obtain element type
         document = getDocumentManagementService().createDocument(MODELING_DOCUMENTS_DIR,
               documentInfo,
               replaceProperties("", json, getTemplateContent("activity")).getBytes(),
               null);

         // getDocumentManagementService().versionDocument(document.getId(), null);
      }

      return document.getId();
   }

   /**
    *
    */
   private String replaceProperties(String path, JsonObject json, String content)
   {
      if (path.length() > 0)
      {
         path += ".";
      }

      for (Map.Entry<String, JsonElement> entry : json.entrySet())
      {
         String key = entry.getKey();
         JsonElement value = entry.getValue();

         if (value != null)
         {
            if (value.isJsonObject())
            {
               content = replaceProperties(path + key, value.getAsJsonObject(), content);
            }
            else
            {
               content = content.replace("#{" + path + key + "}", value.toString());
            }
         }
      }

      return content;
   }

   /**
    *
    * @param elementType
    * @return
    */
   private String getTemplateContent(String elementType)
   {
      Document document = getDocumentManagementService().getDocument(
            MODEL_DOCUMENTATION_TEMPLATES_FOLDER + elementType + "-template.html");

      // Try extension ".htm"

      if (document == null)
      {
         getDocumentManagementService().getDocument(
               MODEL_DOCUMENTATION_TEMPLATES_FOLDER + elementType + "-template.html");
      }

      if (document != null)
      {
         return new String(getDocumentManagementService().retrieveDocumentContent(
               document.getId()));
      }

      return "";
   }

   /**
    *
    * @return
    */
   private DocumentManagementService getDocumentManagementService()
   {
      if (documentManagementService == null)
      {
         documentManagementService = getServiceFactory().getDocumentManagementService();
      }

      return documentManagementService;
   }

   /**
    *
    * @return
    */
   private ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         // TODO Bind against user!

         serviceFactory = ServiceFactoryLocator.get("motu", "motu");
      }

      return serviceFactory;
   }
}