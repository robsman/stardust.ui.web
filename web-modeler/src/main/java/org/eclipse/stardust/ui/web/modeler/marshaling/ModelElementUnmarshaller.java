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
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.AnnotationSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
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
import org.eclipse.stardust.model.xpdl.carnot.IAccessPointOwner;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
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
import org.eclipse.stardust.model.xpdl.carnot.SubProcessModeType;
import org.eclipse.stardust.model.xpdl.carnot.TextType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.XmlTextNode;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalReferenceType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.XpdlPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDCompositor;
import org.eclipse.xsd.XSDConstrainingFacet;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.impl.XSDSchemaImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Marc.Gille
 *
 */
public abstract class ModelElementUnmarshaller implements ModelUnmarshaller
{
   private static final Logger trace = LogManager.getLogger(ModelElementUnmarshaller.class);

   private Map<Class<? >, String[]> propertiesMap;

   protected abstract ModelManagementStrategy modelManagementStrategy();

   // TODO For documentation creation
   private static final String MODEL_DOCUMENTATION_TEMPLATES_FOLDER = "/documents/templates/modeling/";

   private static final String MODELING_DOCUMENTS_DIR = "/process-modeling-documents/";

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   private ModelBuilderFacade modelBuilderFacade;

   private JsonMarshaller jsonIo = new JsonMarshaller();

   public static String deriveElementIdFromName(String name)
   {
      StringBuilder idBuilder = new StringBuilder(name.length());
      boolean firstWord = true;
      boolean newWord = true;
      for (int i = 0; i < name.length(); ++i)
      {
         char nameChar = name.charAt(i);
         if (Character.isLetterOrDigit(nameChar))
         {
            if (newWord && !firstWord)
            {
               // append underscore for each first illegal character
               idBuilder.append('_');
            }
            idBuilder.append(Character.toUpperCase(nameChar));
            firstWord &= false;
            newWord = false;
         }
         else
         {
            newWord = true;
         }
      }

      return idBuilder.toString();
   }

   /**
	 *
	 */
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
      System.out.println("Unmarshalling: " + element + " " + json);

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
         System.out.println("===> Unsupported Symbol " + element);
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
         if ( !activity.getId().toLowerCase().startsWith("gateway"))
         {
            // fix accidental ID change
            activity.setId("gateway_" + activity.getId());
         }

         if (activityJson.has(ModelerConstants.GATEWAY_TYPE_PROPERTY))
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
               // TODO OR Support

               activity.setJoin(JoinSplitType.XOR_LITERAL);
               activity.setSplit(JoinSplitType.XOR_LITERAL);
            }
         }
      }
      else
      {
         if (activityJson.has(ModelerConstants.ACTIVITY_IS_ABORTABLE_BY_PERFORMER))
         {
            activity.setAllowsAbortByPerformer(activityJson.get(
                  ModelerConstants.ACTIVITY_IS_ABORTABLE_BY_PERFORMER).getAsBoolean());
         }
         if (activityJson.has(ModelerConstants.ACTIVITY_IS_HIBERNATED_ON_CREATION))
         {
            activity.setHibernateOnCreation(activityJson.get(
                  ModelerConstants.ACTIVITY_IS_HIBERNATED_ON_CREATION).getAsBoolean());
         }

         if (ModelerConstants.MANUAL_ACTIVITY.equals(extractString(activityJson,
               ModelerConstants.ACTIVITY_TYPE)))
         {
            activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);

            if (activityJson.has(ModelerConstants.PARTICIPANT_FULL_ID)
                  && !activityJson.get(ModelerConstants.PARTICIPANT_FULL_ID).isJsonNull())
            {
               String participantFullId = extractString(activityJson,
                     ModelerConstants.PARTICIPANT_FULL_ID);

               IModelParticipant performer = getModelBuilderFacade().findParticipant(
                     participantFullId);
               activity.setPerformer(performer);
            }
         }
         else if (ModelerConstants.SUBPROCESS_ACTIVITY.equals(extractString(activityJson,
               ModelerConstants.ACTIVITY_TYPE)))
         {
            activity.setImplementation(ActivityImplementationType.SUBPROCESS_LITERAL);

            if (activityJson.has(ModelerConstants.SUBPROCESS_ID)
                  && !activityJson.get(ModelerConstants.SUBPROCESS_ID).isJsonNull())
            {
               String subprocessFullId = extractString(activityJson,
                     ModelerConstants.SUBPROCESS_ID);

               getModelBuilderFacade().setSubProcess(activity, subprocessFullId);

               if (activityJson.has(ModelerConstants.SUBPROCESS_MODE_PROPERTY))
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
         else if (ModelerConstants.APPLICATION_ACTIVITY.equals(extractString(
               activityJson, ModelerConstants.ACTIVITY_TYPE)))
         {
            activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);

            if (activityJson.has(ModelerConstants.APPLICATION_FULL_ID_PROPERTY)
                  && !activityJson.get(ModelerConstants.APPLICATION_FULL_ID_PROPERTY)
                        .isJsonNull())
            {
               String applicationFullId = extractString(activityJson,
                     ModelerConstants.APPLICATION_FULL_ID_PROPERTY);

               getModelBuilderFacade().setApplication(activity, applicationFullId);
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

      if (controlFlowJson.has(ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY))
      {
         transition.setForkOnTraversal(controlFlowJson.get(
               ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY).getAsBoolean());
      }

      if (controlFlowJson.has(ModelerConstants.OTHERWISE_PROPERTY))
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

      if (controlFlowJson.has(ModelerConstants.CONDITION_EXPRESSION_PROPERTY))
      {
         transition.setCondition(ModelerConstants.CONDITION_KEY);

         XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();

         ModelUtils.setCDataString(expression.getMixed(),
               controlFlowJson.get(ModelerConstants.CONDITION_EXPRESSION_PROPERTY)
                     .getAsString(), true);
         transition.setExpression(expression);
      }

      // While routing , anchor point orientation changes
      if (controlFlowJson.has(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         controlFlowConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
               controlFlowJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }
      if (controlFlowJson.has(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY))
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

      JsonObject dataFlowJson = dataFlowConnectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      if (dataFlowJson.has(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         dataFlowConnection.setSourceAnchor(mapAnchorOrientation(extractInt(dataFlowJson,
               ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }

      if (dataFlowJson.has(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY))
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
               .getActivity()
               .getDataMapping())
         {
            if (dataMapping.getData()
                  .getId()
                  .equals(dataFlowConnection.getDataSymbol().getData().getId()))
            {
               dataMappings.add(dataMapping);
            }
         }

         // Delete all data mappings between the activity and the data

         for (DataMappingType dataMapping : dataMappings)
         {
            dataFlowConnection.getActivitySymbol()
                  .getActivity()
                  .getDataMapping()
                  .remove(dataMapping);
            dataFlowConnection.getDataSymbol()
                  .getData()
                  .getDataMappings()
                  .remove(dataMapping);
         }

         // dataFlowJson holds an input and/or an output dataMappingJson; data mappings
         // have to be created for both

         // Create input mapping

         if (dataFlowJson.has(ModelerConstants.INPUT_DATA_MAPPING_PROPERTY))
         {
            createDataMapping(
                  dataFlowConnection.getActivitySymbol().getActivity(),
                  dataFlowConnection.getDataSymbol().getData(),
                  dataFlowJson,
                  DirectionType.IN_LITERAL,
                  dataFlowJson.getAsJsonObject(ModelerConstants.INPUT_DATA_MAPPING_PROPERTY));
         }

         // Create output mapping

         if (dataFlowJson.has(ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY))
         {
            createDataMapping(
                  dataFlowConnection.getActivitySymbol().getActivity(),
                  dataFlowConnection.getDataSymbol().getData(),
                  dataFlowJson,
                  DirectionType.OUT_LITERAL,
                  dataFlowJson.getAsJsonObject(ModelerConstants.OUTPUT_DATA_MAPPING_PROPERTY));
         }
      }
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

      long maxOid = XpdlModelUtils.getMaxUsedOid(ModelUtils.findContainingModel(activity));

      dataMapping.setElementOid(++maxOid);

      if (dataFlowJson.has(ModelerConstants.ID_PROPERTY))
      {
         dataMapping.setId(dataFlowJson.get(ModelerConstants.ID_PROPERTY).getAsString());
      }
      else
      {
         dataMapping.setId(data.getId());
      }

      if (dataFlowJson.has(ModelerConstants.NAME_PROPERTY))
      {
         dataMapping.setName(dataFlowJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }
      else
      {
         dataMapping.setName(data.getName());
      }

      dataMapping.setDirection(direction);

      if (dataMappingJson.has(ModelerConstants.ACCESS_POINT_ID_PROPERTY)
            && !dataMappingJson.get(ModelerConstants.ACCESS_POINT_ID_PROPERTY)
                  .isJsonNull())
      {
         dataMapping.setApplicationAccessPoint(dataMappingJson.get(
               ModelerConstants.ACCESS_POINT_ID_PROPERTY).getAsString());
         dataMapping.setContext(dataMappingJson.get(
               ModelerConstants.ACCESS_POINT_CONTEXT_PROPERTY).getAsString());
      }
      else
      {
         // TODO Review

         dataMapping.setApplicationAccessPoint(null);
         dataMapping.setContext(ModelerConstants.DEFAULT_LITERAL);
      }

      if (dataMappingJson.has(ModelerConstants.DATA_PATH_PROPERTY)
            && !dataMappingJson.get(ModelerConstants.DATA_PATH_PROPERTY).isJsonNull())
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
         }
         else
         {
            swimlaneSymbol.setOrientation(OrientationType.VERTICAL_LITERAL);
         }
      }

      updateNodeSymbol(swimlaneSymbol, swimlaneSymbolJson);

      mapDeclaredProperties(swimlaneSymbol, swimlaneSymbolJson,
            propertiesMap.get(LaneSymbol.class));

      if (swimlaneSymbolJson.has(ModelerConstants.PARTICIPANT_FULL_ID))
      {
         String participantFullId = swimlaneSymbolJson.get(
               ModelerConstants.PARTICIPANT_FULL_ID).getAsString();

         swimlaneSymbol.setParticipant(getModelBuilderFacade().findParticipant(
               getModelBuilderFacade().findModel(
                     getModelBuilderFacade().getModelId(participantFullId)),
               getModelBuilderFacade().stripFullId(participantFullId)));
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
   private void updateIdentifiableElement(IIdentifiableElement element,
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
   private boolean updateElementNameAndId(EObject element, EStructuralFeature eFtrId,
         EStructuralFeature eFtrName, JsonObject elementJson)
   {
      boolean wasModified = false;
      String newId = null;

      if (elementJson.has(ModelerConstants.ID_PROPERTY))
      {
         // provided ID has precedence over generated ID
         newId = extractString(elementJson, ModelerConstants.ID_PROPERTY);
      }

      if (elementJson.has(ModelerConstants.NAME_PROPERTY))
      {
         String newName = extractString(elementJson, ModelerConstants.NAME_PROPERTY);
         if ( !element.eGet(eFtrName).equals(newName))
         {
            wasModified = true;
            element.eSet(eFtrName, newName);

            if (isEmpty(newId))
            {
               // compute ID from name
               String generatedId = deriveElementIdFromName(newName);
               if ((null != element.eContainer())
                     && element.eContainingFeature().isMany())
               {
                  newId = generatedId;
                  int counter = 0;
                  while (true)
                  {
                     @SuppressWarnings("unchecked")
                     List<? extends EObject> domain = (List<? extends EObject>) element.eContainer()
                           .eGet(element.eContainingFeature());

                     boolean isConflict = false;
                     for (EObject peer : domain)
                     {
                        if ((peer != element) && (newId.equals(peer.eGet(eFtrId))))
                        {
                           isConflict = true;
                           break;
                        }
                     }

                     if (isConflict)
                     {
                        // there is a conflict, resolve by appending a counter
                        newId = generatedId + "_" + (++counter);
                        continue;
                     }
                     else
                     {
                        break;
                     }
                  }
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

      if (processDefinitionJson.has(ModelerConstants.FORMAL_PARAMETERS_PROPERTY))
      {
         if (processDefinition.getFormalParameters() != null
               && processDefinition.getFormalParameters().getFormalParameter() != null)
         {
            processDefinition.getFormalParameters().getFormalParameter().clear();
         }

         JsonArray formalParametersJson = processDefinitionJson.get(
               ModelerConstants.FORMAL_PARAMETERS_PROPERTY).getAsJsonArray();

         for (int n = 0; n < formalParametersJson.size(); ++n)
         {
            JsonObject formalParameterJson = formalParametersJson.get(n)
                  .getAsJsonObject();

            System.out.println("Formal parameter: " + formalParameterJson);

            ModeType mode = null;

            if (formalParameterJson.get(ModelerConstants.DIRECTION_PROPERTY)
                  .getAsString()
                  .equals(DirectionType.IN_LITERAL.getLiteral()))
            {
               mode = ModeType.IN;
            }
            else
            {
               mode = ModeType.OUT;
            }

            DataType data = null;

            if (formalParameterJson.has(ModelerConstants.DATA_FULL_ID_PROPERTY)
                  && !formalParameterJson.get(ModelerConstants.DATA_FULL_ID_PROPERTY)
                        .isJsonNull())
            {
               data = getModelBuilderFacade().findData(
                     formalParameterJson.get(ModelerConstants.DATA_FULL_ID_PROPERTY)
                           .getAsString());
            }

            if (formalParameterJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
                  .getAsString()
                  .equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
            {
               getModelBuilderFacade().createPrimitiveParameter(
                     processDefinition,
                     data,
                     getModelBuilderFacade().createIdFromName(
                           formalParameterJson.get(ModelerConstants.NAME_PROPERTY)
                                 .getAsString()),
                     formalParameterJson.get(ModelerConstants.NAME_PROPERTY)
                           .getAsString(),
                     formalParameterJson.get(
                           ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY).getAsString(),
                     mode);
            }
            else if (formalParameterJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
                  .getAsString()
                  .equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
            {
               String structuredDataTypeFullId = null;

               if (formalParameterJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
               {
                  structuredDataTypeFullId = formalParameterJson.get(
                        ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                        .getAsString();
               }

               getModelBuilderFacade().createStructuredParameter(
                     processDefinition,
                     data,
                     getModelBuilderFacade().createIdFromName(
                           formalParameterJson.get(ModelerConstants.NAME_PROPERTY)
                                 .getAsString()),
                     formalParameterJson.get(ModelerConstants.NAME_PROPERTY)
                           .getAsString(), structuredDataTypeFullId, mode);
            }
         }
      }

      if (processDefinitionJson.has(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY))
      {
         if (processDefinitionJson.get(ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.NO_PROCESS_INTERFACE_KEY))
         {
         }
         else if (processDefinitionJson.get(
               ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.PROVIDES_PROCESS_INTERFACE_KEY))
         {
         }
         else if (processDefinitionJson.get(
               ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.IMPLEMENTS_PROCESS_INTERFACE_KEY))
         {
         }
      }

      if (processDefinitionJson.has(ModelerConstants.DATA_PATHES_PROPERTY))
      {
         processDefinition.getDataPath().clear();

         JsonArray dataPathes = processDefinitionJson.get(
               ModelerConstants.DATA_PATHES_PROPERTY).getAsJsonArray();

         for (int n = 0; n < dataPathes.size(); ++n)
         {
            JsonObject dataPathJson = dataPathes.get(n).getAsJsonObject();
            DataPathType dataPath = getModelBuilderFacade().createDataPath();
            ModelType model = ModelUtils.findContainingModel(processDefinition);
            long maxOID = XpdlModelUtils.getMaxUsedOid(model);
            dataPath.setElementOid(maxOID);

            dataPath.setId(getModelBuilderFacade().createIdFromName(
                  dataPathJson.get(ModelerConstants.NAME_PROPERTY).getAsString()));
            dataPath.setName(dataPathJson.get(ModelerConstants.NAME_PROPERTY)
                  .getAsString());

            if (dataPathJson.has(ModelerConstants.DATA_FULL_ID_PROPERTY)
                  && !dataPathJson.get(ModelerConstants.DATA_FULL_ID_PROPERTY)
                        .isJsonNull())
            {
               String dataFullId = dataPathJson.get(
                     ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();

               DataType data = getModelBuilderFacade().importData(model, dataFullId);

               dataPath.setData(data);
            }

            if (dataPathJson.has(ModelerConstants.DATA_PATH_PROPERTY)
                  && !dataPathJson.get(ModelerConstants.DATA_PATH_PROPERTY).isJsonNull())
            {
               dataPath.setDataPath(dataPathJson.get(ModelerConstants.DATA_PATH_PROPERTY)
                     .getAsString());
            }

            dataPath.setDescriptor(dataPathJson.get(ModelerConstants.DESCRIPTOR_PROPERTY)
                  .getAsBoolean());
            dataPath.setKey(dataPathJson.get(ModelerConstants.KEY_DESCRIPTOR_PROPERTY)
                  .getAsBoolean());

            if (dataPathJson.get(ModelerConstants.DIRECTION_PROPERTY)
                  .getAsString()
                  .equals(DirectionType.IN_LITERAL.getLiteral()))
            {
               dataPath.setDirection(DirectionType.IN_LITERAL);
            }
            else
            {
               dataPath.setDirection(DirectionType.OUT_LITERAL);
            }

            processDefinition.getDataPath().add(dataPath);
         }
      }

      if (processDefinitionJson.has(ModelerConstants.DEFAULT_PRIORITY_PROPERTY))
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
    *
    * @param activitySymbol
    * @param activitySymbolJson
    */
   private void updateNodeSymbol(INodeSymbol nodeSymbol, JsonObject nodeSymbolJto)
   {
      if (nodeSymbolJto.has(ModelerConstants.X_PROPERTY)
            && nodeSymbolJto.has(ModelerConstants.Y_PROPERTY))
      {
         int x = extractInt(nodeSymbolJto, ModelerConstants.X_PROPERTY);
         int y = extractInt(nodeSymbolJto, ModelerConstants.Y_PROPERTY);

         // adjust coordinates from global to local
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

         nodeSymbol.setXPos(x - laneOffsetX);
         nodeSymbol.setYPos(y - laneOffsetY);

         if (nodeSymbol instanceof LaneSymbol
               && (nodeSymbolJto.has(ModelerConstants.WIDTH_PROPERTY) || nodeSymbolJto.has(ModelerConstants.HEIGHT_PROPERTY)))
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
            if (nodeSymbolJto.has(ModelerConstants.HEIGHT_PROPERTY))
            {
               height = extractInt(nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY);
               heightOffset = height - nodeSymbol.getHeight();
               nodeSymbol.setHeight(height);
            }

            // Update the child symbol co-ordinates wrt parent(lane)
            if (nodeSymbolJto.has(ModelerConstants.X_OFFSET))
               xOffset = nodeSymbolJto.get(ModelerConstants.X_OFFSET).getAsInt();
            if (nodeSymbolJto.has(ModelerConstants.Y_OFFSET))
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

            if(orientation.equals(OrientationType.VERTICAL_LITERAL))
            {
               for (LaneSymbol lane : poolSymbol.getLanes())
               {
                  if (nodeSymbol.getElementOid() != lane.getElementOid())
                  {
                     if ((lane.getXPos() > nodeSymbol.getXPos() && widthOffset != 0))
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
               }
            }
            else{
               for (LaneSymbol lane : poolSymbol.getLanes())
               {
                  if (nodeSymbol.getElementOid() != lane.getElementOid())
                  {
                     if ((lane.getYPos() > nodeSymbol.getYPos() && heightOffset != 0))
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
         }
         else
         {
            if (nodeSymbolJto.has(ModelerConstants.WIDTH_PROPERTY))
            {
               int width = extractInt(nodeSymbolJto, ModelerConstants.WIDTH_PROPERTY);
               nodeSymbol.setWidth(width);
            }
            if (nodeSymbolJto.has(ModelerConstants.HEIGHT_PROPERTY))
            {
               int height = extractInt(nodeSymbolJto, ModelerConstants.HEIGHT_PROPERTY);
               nodeSymbol.setHeight(height);
            }
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

      TriggerType trigger = startEventSymbol.getTrigger();

      if (trigger != null)
      {
         updateTrigger(trigger, startEventJson);
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

//      TriggerType trigger = endEventSymbol.getTrigger();
//
//      if (trigger == null)
//      {
//         TriggerType manualTrigger = newManualTrigger(endEventSymbol.get)
//         .build();
//
//         manualTrigger.setElementOid(++maxOid);
//         endEventSymbol.setTrigger(manualTrigger);
//      }
//
//      updateTrigger(trigger, endEventJson);
   }

   /**
    *
    * @param trigger
    * @param triggerJson
    */
   private void updateTrigger(TriggerType trigger, JsonObject triggerJson)
   {
      if (triggerJson.has(ModelerConstants.NAME_PROPERTY))
      {
         trigger.setName(triggerJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
      }

      storeAttributes(trigger, triggerJson);
      storeDescription(trigger, triggerJson);

      if (triggerJson.has(ModelerConstants.EVENT_CLASS_PROPERTY))
      {
         getModelBuilderFacade().setAttribute(trigger,
               "stardust::engine:eventClass",
               triggerJson.get(ModelerConstants.EVENT_CLASS_PROPERTY).getAsString());
      }

      if (triggerJson.has(ModelerConstants.PARAMETER_MAPPINGS_PROPERTY))
      {
         JsonArray parameterMappings = triggerJson.get(
               ModelerConstants.PARAMETER_MAPPINGS_PROPERTY).getAsJsonArray();

         trigger.getAccessPoint().clear();
         trigger.getParameterMapping().clear();

         for (int n = 0; n < parameterMappings.size(); ++n)
         {
            JsonObject parameterMappingJson = parameterMappings.get(n)
                  .getAsJsonObject();
            String id = parameterMappingJson.get(ModelerConstants.ID_PROPERTY)
                  .getAsString();
            String name = parameterMappingJson.get(ModelerConstants.NAME_PROPERTY)
                  .getAsString();
            String direction = parameterMappingJson.get(
                  ModelerConstants.DIRECTION_PROPERTY).getAsString();

            AccessPointType accessPoint = null;

            if (parameterMappingJson.has(ModelerConstants.DATA_TYPE_PROPERTY))
            {
               String dataType = parameterMappingJson.get(
                     ModelerConstants.DATA_TYPE_PROPERTY).getAsString();
               if (dataType.equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
               {
                  String primitiveDataType = parameterMappingJson.get(
                        ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY).getAsString();
                  accessPoint = getModelBuilderFacade().createPrimitiveAccessPoint(
                        trigger, id, name, primitiveDataType, direction);
               }
               else if (dataType.equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
               {
                  String structuredDataFullId = null;

                  if (parameterMappingJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                  {
                     structuredDataFullId = parameterMappingJson.get(
                           ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                           .getAsString();
                  }

                  accessPoint = getModelBuilderFacade().createStructuredAccessPoint(
                        trigger, id, name, structuredDataFullId, direction);
               }
               else if (dataType.equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
               {
                  // accessPoint.setType(getModelBuilderFacade().findDataType(accessPointJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID).getAsString()));
               }
            }

            // TODO Attributes storage missing?

            storeDescription(accessPoint, parameterMappingJson);

            if (parameterMappingJson.has(ModelerConstants.DATA_FULL_ID_PROPERTY))
            {
               String dataPath = null;
               String dataFullID = parameterMappingJson.get(
                     ModelerConstants.DATA_FULL_ID_PROPERTY).getAsString();
               if (parameterMappingJson.has(ModelerConstants.DATA_PATH_PROPERTY))
               {
                  dataPath = parameterMappingJson.get(
                        ModelerConstants.DATA_PATH_PROPERTY).getAsString();
               }
               getModelBuilderFacade().createParameterMapping(trigger, dataFullID,
                     dataPath);
            }
         }
      }
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
      storeAttributes(application, applicationJson);
      storeDescription(application, applicationJson);

      if (applicationJson.has(ModelerConstants.CONTEXTS_PROPERTY))
      {
         application.getContext().clear();
         application.getAccessPoint().clear();

         JsonObject contextsJson = applicationJson.get(ModelerConstants.CONTEXTS_PROPERTY)
               .getAsJsonObject();

         for (Map.Entry<String, ? > entry : contextsJson.entrySet())
         {
            String contextId = entry.getKey();

            System.out.println("Context: " + contextId);

            IAccessPointOwner context = application;

            if ( !ModelerConstants.APPLICATION_CONTEXT_TYPE_KEY.equals(contextId))
            {
               context = getModelBuilderFacade().createApplicationContext(application,
                     contextId);
            }

            JsonObject contextJson = contextsJson.get(contextId).getAsJsonObject();
            JsonArray accessPointsJson = contextJson.get(
                  ModelerConstants.ACCESS_POINTS_PROPERTY).getAsJsonArray();

            for (int n = 0; n < accessPointsJson.size(); ++n)
            {
               JsonObject accessPointJson = accessPointsJson.get(n).getAsJsonObject();
               String id = accessPointJson.get(ModelerConstants.ID_PROPERTY)
                     .getAsString();
               String name = accessPointJson.get(ModelerConstants.NAME_PROPERTY)
                     .getAsString();
               String direction = accessPointJson.get(ModelerConstants.DIRECTION_PROPERTY)
                     .getAsString();

               AccessPointType accessPoint = null;

               System.out.println("Access Point JSON: " + accessPointJson);

               if (accessPointJson.has(ModelerConstants.DATA_TYPE_PROPERTY))
               {
                  String dataType = accessPointJson.get(
                        ModelerConstants.DATA_TYPE_PROPERTY).getAsString();

                  if (dataType.equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
                  {
                     String primitiveDataType = accessPointJson.get(
                           ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY).getAsString();
                     accessPoint = getModelBuilderFacade().createPrimitiveAccessPoint(
                           context, id, name, primitiveDataType, direction);
                  }
                  else if (dataType.equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
                  {
                     String structuredDataFullId = null;

                     if (accessPointJson.has(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
                     {
                        structuredDataFullId = accessPointJson.get(
                              ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY)
                              .getAsString();
                     }

                     accessPoint = getModelBuilderFacade().createStructuredAccessPoint(
                           context, id, name, structuredDataFullId, direction);

                     System.out.println("Created Access Point: " + accessPoint);
                  }
                  else if (dataType.equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
                  {
                     // accessPoint.setType(getModelBuilderFacade().findDataType(accessPointJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID).getAsString()));
                  }
               }

               storeDescription(accessPoint, accessPointJson);
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
         updateXSDSchemaType(typeDeclaration.getSchemaType(),
               declarationJson.getAsJsonObject("schema"));
      }

      // ExternalReference ?
   }

   private void updateXSDSchemaType(SchemaTypeType schemaType, JsonObject schemaJson)
   {
      XSDSchema schema = schemaType.getSchema();

      if (schemaJson.has("targetNamespace"))
      {
         schema.setTargetNamespace(schemaJson.getAsJsonPrimitive("targetNamespace")
               .getAsString());
      }

      if (schemaJson.has("types"))
      {
         updateXSDTypeDefinitions(schema, schemaJson.getAsJsonArray("types"));
      }

      if (schemaJson.has("elements"))
      {
         updateElementDeclarations(schema, schemaJson.getAsJsonArray("elements"));
      }
   }

   private void updateXSDTypeDefinitions(XSDSchema schema, JsonArray json)
   {
      Map<String, XSDTypeDefinition> typesIndex = newHashMap();
      Set<XSDTypeDefinition> updatedTypes = newHashSet();

      for (XSDTypeDefinition def : schema.getTypeDefinitions())
      {
         typesIndex.put(def.getName(), def);
      }

      for (JsonElement entry : json)
      {
         if ( !(entry instanceof JsonObject))
         {
            trace.warn("Expected object, but received " + entry);
            continue;
         }
         JsonObject defJson = (JsonObject) entry;
         String typeName = extractAsString((JsonObject) entry,
               ModelerConstants.NAME_PROPERTY);
         XSDTypeDefinition def = typesIndex.get(typeName);
         boolean isComplexType = defJson.has("body");

         int contentsIdx = schema.getContents().size();
         int typeIdx = schema.getTypeDefinitions().size();
         if ((isComplexType && (def instanceof XSDSimpleTypeDefinition))
               || ( !isComplexType && (def instanceof XSDComplexTypeDefinition)))
         {
            // coerce between complex/simple type (insert as same position as before)
            contentsIdx = schema.getContents().indexOf(def);
            typeIdx = schema.getTypeDefinitions().indexOf(def);
            schema.getContents().remove(contentsIdx);
            typesIndex.remove(typeName);
            def = null;
         }

         if (def == null)
         {
            def = isComplexType
                  ? XSDFactory.eINSTANCE.createXSDComplexTypeDefinition()
                  : XSDFactory.eINSTANCE.createXSDSimpleTypeDefinition();
            schema.getContents().add(contentsIdx, def);
            schema.getTypeDefinitions().move(typeIdx, def);

            typesIndex.put(typeName, def);
         }

         def.setName(defJson.getAsJsonPrimitive("name").getAsString());

         if (isComplexType)
         {
            updateXSDComplexTypeDefinition((XSDComplexTypeDefinition) def, defJson);
         }
         else
         {
            updateXSDSimpleTypeDefinition((XSDSimpleTypeDefinition) def, defJson);
         }

         updatedTypes.add(def);
      }

      // remove types not present in JSON anymore
      for (Iterator<XSDTypeDefinition> i = schema.getTypeDefinitions().iterator(); i.hasNext();)
      {
         XSDTypeDefinition typeDefinition = i.next();
         if ( !updatedTypes.contains(typeDefinition))
         {
            i.remove();
         }
      }
   }

   /**
    *
    * @param def
    * @param simpleTypeJson
    */
   private void updateXSDSimpleTypeDefinition(XSDSimpleTypeDefinition def,
         JsonObject simpleTypeJson)
   {
      List<XSDConstrainingFacet> facets = def.getFacetContents();

      if (simpleTypeJson.has(ModelerConstants.TYPE_PROPERTY))
      {
         String baseTypeName = extractAsString(simpleTypeJson, ModelerConstants.TYPE_PROPERTY);

         String nsPrefix = null;
         if (0 <= baseTypeName.indexOf(':'))
         {
            nsPrefix = baseTypeName.substring(0, baseTypeName.indexOf(':'));
            baseTypeName = baseTypeName.substring(baseTypeName.indexOf(':') + 1);
         }
         String baseTypeNamespace = def.getSchema().getQNamePrefixToNamespaceMap().get(nsPrefix);

         XSDSimpleTypeDefinition baseType = def.resolveSimpleTypeDefinition(baseTypeNamespace, baseTypeName);
         if (null != baseType.eContainer())
         {
            def.setBaseTypeDefinition(baseType);
         }
      }

      facets.clear();

      if (simpleTypeJson.has("facets"))
      {
         JsonArray facetsJson = simpleTypeJson.getAsJsonArray("facets");
         for (JsonElement entry : facetsJson)
         {
            if ( !(entry instanceof JsonObject))
            {
               trace.warn("Expected object, but received " + entry);
               continue;
            }
            JsonObject facetJson = (JsonObject) entry;
            String classifier = facetJson.getAsJsonPrimitive("classifier").getAsString();
            XSDConstrainingFacet facet = SupportedXSDConstrainingFacets.valueOf(
                  classifier).create();
            facet.setLexicalValue(facetJson.getAsJsonPrimitive("name").getAsString());
            facets.add(facet);
         }
      }
   }

   /**
    *
    *
    *
    */
   private static enum SupportedXSDConstrainingFacets
   {
      // (fh) Only added what is supported by the eclipse modeler. Should be all of them.
      enumeration, pattern, maxLength, minLength;

      XSDConstrainingFacet create()
      {
         switch (this)
         {
         case enumeration:
            return XSDFactory.eINSTANCE.createXSDEnumerationFacet();
         case pattern:
            return XSDFactory.eINSTANCE.createXSDPatternFacet();
         case maxLength:
            return XSDFactory.eINSTANCE.createXSDMaxLengthFacet();
         case minLength:
            return XSDFactory.eINSTANCE.createXSDMinLengthFacet();
         }
         return null; // (fh) unreachable
      }
   }

   /**
    *
    * @param def
    * @param json
    */
   private void updateXSDComplexTypeDefinition(XSDComplexTypeDefinition def,
         JsonObject json)
   {
      JsonObject bodyJson = json.getAsJsonObject("body");
      XSDComplexTypeContent content = def.getContent();

      if (null == content)
      {
         content = XSDFactory.eINSTANCE.createXSDParticle();
         ((XSDParticle) content).setContent(XSDFactory.eINSTANCE.createXSDModelGroup());
         def.setContent(content);
      }

      if (content instanceof XSDParticle)
      {
         XSDParticle particle = (XSDParticle) content;
         XSDTerm term = particle.getTerm();

         if (term instanceof XSDModelGroup)
         {
            XSDModelGroup group = (XSDModelGroup) term;
            String classifier = bodyJson.getAsJsonPrimitive("classifier").getAsString();
            group.setCompositor(XSDCompositor.get(classifier));
            List<XSDParticle> particles = group.getContents();
            particles.clear();

            if (bodyJson.has("elements"))
            {
               JsonArray elements = bodyJson.getAsJsonArray("elements");
               for (JsonElement entry : elements)
               {
                  if ( !(entry instanceof JsonObject))
                  {
                     trace.warn("Expected object, but received " + entry);
                     continue;
                  }
                  JsonObject elementJson = (JsonObject) entry;
                  XSDParticle p = XSDFactory.eINSTANCE.createXSDParticle();
                  ParticleCardinality.get(
                        elementJson.getAsJsonPrimitive("cardinality").getAsString())
                        .update(p);
                  XSDElementDeclaration decl = XSDFactory.eINSTANCE.createXSDElementDeclaration();
                  p.setContent(decl);
                  decl.setName(elementJson.getAsJsonPrimitive("name").getAsString());
                  String type = elementJson.getAsJsonPrimitive("type").getAsString();

                  String namespace = null;
                  String nsPrefix = null;
                  if (type.startsWith("{"))
                  {
                     // a type QName
                     QName typeQName = QName.valueOf(type);
                     type = typeQName.getLocalPart();
                     namespace = typeQName.getNamespaceURI();
                     if ( !def.getSchema()
                           .getQNamePrefixToNamespaceMap()
                           .containsValue(typeQName.getNamespaceURI()))
                     {
                        nsPrefix = typeQName.getPrefix();
                        if (isEmpty(nsPrefix))
                        {
                           nsPrefix = TypeDeclarationUtils.computePrefix(
                                 typeQName.getLocalPart().toLowerCase(), def.getSchema()
                                       .getQNamePrefixToNamespaceMap()
                                       .keySet());
                        }
                        def.getSchema()
                              .getQNamePrefixToNamespaceMap()
                              .put(nsPrefix, namespace);
                        // propagate ns-prefix mappings to DOM
                        def.getSchema().updateElement(true);
                     }

                     Collection<XSDSchema> targetSchemas = ((XSDSchemaImpl) def.getSchema()).resolveSchema(namespace);
                     if (targetSchemas.isEmpty())
                     {
                        // find target schema
                        ModelType scopeModel = ModelUtils.findContainingModel(def);
                        for (TypeDeclarationType typeDeclaration : scopeModel.getTypeDeclarations()
                              .getTypeDeclaration())
                        {
                           if (null != typeDeclaration.getSchema())
                           {
                              XSDTypeDefinition targetType = typeDeclaration.getSchema()
                                    .resolveTypeDefinition(namespace, type);
                              if ((null != targetType)
                                    && (null != targetType.eContainer()))
                              {
                                 XSDImport schemaImport = XSDFactory.eINSTANCE.createXSDImport();
                                 schemaImport.setNamespace(namespace);
                                 schemaImport.setSchemaLocation(typeDeclaration.getSchema()
                                       .getSchemaLocation());
                                 schemaImport.setResolvedSchema(typeDeclaration.getSchema());
                                 def.getSchema().getContents().add(0, schemaImport);
                                 break;
                              }
                           }
                        }
                     }
                  }
                  else
                  {
                     int ix = type.indexOf(':');
                     if (ix > 0)
                     {
                        nsPrefix = type.substring(0, ix);
                        type = type.substring(ix + 1);
                     }
                     namespace = def.getSchema()
                           .getQNamePrefixToNamespaceMap()
                           .get(nsPrefix);
                  }
                  XSDTypeDefinition typeDefinition = def.resolveTypeDefinition(namespace,
                        type);
                  if ((null == typeDefinition) || (null == typeDefinition.eContainer()))
                  {
                     typeDefinition = def.resolveComplexTypeDefinition(namespace, type);
                  }
                  decl.setTypeDefinition(typeDefinition);
                  particles.add(p);
               }
            }
         }
         // else unsupported wildcard and element declaration
      }
      // else unsupported simple & complex content
   }

   /**
    *
    */
   private static enum ParticleCardinality
   {
      required, optional, many, atLeastOne;

      void update(XSDParticle particle)
      {
         switch (this)
         {
         case required:
            particle.unsetMinOccurs();
            particle.unsetMaxOccurs();
            break;
         case optional:
            particle.setMinOccurs(0);
            particle.unsetMaxOccurs();
            break;
         case many:
            particle.setMinOccurs(0);
            particle.setMaxOccurs(XSDParticle.UNBOUNDED);
            break;
         case atLeastOne:
            particle.unsetMinOccurs();
            particle.setMaxOccurs(XSDParticle.UNBOUNDED);
            break;
         }
      }

      static ParticleCardinality get(String name)
      {
         if ("at least one".equals(name))
         {
            return atLeastOne;
         }
         return valueOf(name);
      }
   }

   /**
    *
    * @param schema
    * @param json
    */
   private void updateElementDeclarations(XSDSchema schema, JsonArray json)
   {
      // TODO Auto-generated method stub
   }

   /**
    * @param role
    * @param roleJson
    */
   private void updateRole(RoleType role, JsonObject roleJson)
   {

      if (roleJson.has(ModelerConstants.CARDINALITY))
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

      if (conditionalPerformerJson.has(ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY))
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
      storeAttributes(organization, organizationJson);
      storeDescription(organization, organizationJson);

      if (organizationJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY))
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
      System.out.println("Data Json " + dataJson);

      updateIdentifiableElement(data, dataJson);

      mapDeclaredProperties(data, dataJson, propertiesMap.get(DataType.class));
      storeAttributes(data, dataJson);
      storeDescription(data, dataJson);

      if (dataJson.has(ModelerConstants.DATA_TYPE_PROPERTY))
      {
         System.out.println("Has property "
               + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());

         if (dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
         {
            System.out.println("Creating Primitive Type: "
                  + dataJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY));

            getModelBuilderFacade().convertDataType(data,
                  ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
            getModelBuilderFacade().updatePrimitiveData(
                  data,
                  dataJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY)
                        .getAsString());

            System.out.println("Primitive Type: " + data.getType());
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

            System.out.println("Structured Type: " + data.getType());
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

            System.out.println("Document Type: " + data.getType());
         }
         else
         {
            System.out.println("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
            System.out.println("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
            System.out.println("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
         }

         // Reset default value attribute in case of a data type change / primitive type
         // change
         // as the previously set default may be invalid.
         getModelBuilderFacade().setAttribute(data, "carnot:engine:defaultValue", "");
      }
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
            mapProperty(element, elementJson, property);
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
         System.out.println("Setting property " + property + " of value "
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
               if (request.get(property) != null)
               {
                  System.out.println("Invoking " + setter.getName()
                        + " with property value " + request.get(property).getAsString());
                  setter.invoke(targetElement, request.get(property).getAsString());
               }
               else
               {
                  System.out.println("Invoking " + setter.getName() + " with null");
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
         System.out.println("No value for property " + property);
      }
   }

   /**
    *
    * @param json
    * @param element
    * @throws JSONException
    */
   private void storeAttributes(EObject element, JsonObject json)
   {
      // Extract JSON elements which are stored in Extended Attributes

      if (json.has(ModelerConstants.COMMENTS_PROPERTY))
      {
         JsonArray commentsJson = json.getAsJsonArray(ModelerConstants.COMMENTS_PROPERTY);
         JsonObject holderJson = new JsonObject();

         holderJson.add(ModelerConstants.COMMENTS_PROPERTY, commentsJson);

         getModelBuilderFacade().setAttribute(element, "documentation:comments",
               jsonIo.writeJsonObject(holderJson));
      }

      if ( !json.has(ModelerConstants.ATTRIBUTES_PROPERTY))
      {
         return;
      }

      JsonObject attributes = json.getAsJsonObject(ModelerConstants.ATTRIBUTES_PROPERTY);

      if (attributes != null)
      {
         for (Map.Entry<String, ? > entry : attributes.entrySet())
         {
            String key = entry.getKey();

            if (attributes.get(key).isJsonNull())
            {
               System.out.println("Setting extended attribute " + key + " to null.");
               getModelBuilderFacade().setAttribute(element, key, null);
            }
            else if (attributes.get(key).getAsJsonPrimitive().isBoolean())
            {
               getModelBuilderFacade().setBooleanAttribute(element, key,
                     attributes.get(key).getAsBoolean());
            }
            else
            {
               // TODO Trick to create document

               if (key.equals("documentation:externalDocumentUrl")
                     && attributes.get(key).getAsString().equals("@CREATE"))
               {
                  getModelBuilderFacade().setAttribute(element, key,
                        createModelElementDocumentation(json));
               }
               else
               {
                  System.out.println("Setting extended attribute " + key + " to "
                        + attributes.get(key).getAsString());
                  getModelBuilderFacade().setAttribute(element, key,
                        attributes.get(key).getAsString());
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
   private void storeDescription(IIdentifiableModelElement element,
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
      return new ModelBuilderFacade(modelManagementStrategy());
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
      documentInfo.setContentType(MimeTypesHelper.HTML.getType());
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
