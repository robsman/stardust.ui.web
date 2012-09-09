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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DescriptionType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.SubProcessModeType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.XmlTextNode;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;

/**
 *
 * @author Marc.Gille
 *
 */
public abstract class ModelElementUnmarshaller
{
   private Map<Class<? >, String[]> propertiesMap;

   protected abstract ModelManagementStrategy modelManagementStrategy();

   private ModelBuilderFacade modelBuilderFacade;

   /**
	 *
	 */
   public ModelElementUnmarshaller()
   {
      propertiesMap = newHashMap();

      propertiesMap.put(ProcessDefinitionType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY,
            ModelerConstants.DEFAULT_PRIORITY_PROPERTY});
      propertiesMap.put(ActivitySymbolType.class, new String[] {
            ModelerConstants.X_PROPERTY, ModelerConstants.Y_PROPERTY});
      propertiesMap.put(ActivityType.class, new String[] {ModelerConstants.NAME_PROPERTY});
      propertiesMap.put(StartEventSymbol.class, new String[] {
            ModelerConstants.X_PROPERTY, ModelerConstants.Y_PROPERTY});
      // propertiesMap.put(EventSymbol.class,
      // new String[] {ModelerConstants.NAME_PROPERTY});
      propertiesMap.put(EndEventSymbol.class, new String[] {
            ModelerConstants.X_PROPERTY, ModelerConstants.Y_PROPERTY});
      propertiesMap.put(LaneSymbol.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      // propertiesMap.put(EndEventSymbol.class,
      // new String[] {ModelerConstants.NAME_PROPERTY});
      propertiesMap.put(ApplicationType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(TypeDeclarationType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(ModelType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(DataType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(RoleType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(OrganizationType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
      propertiesMap.put(ConditionalPerformerType.class, new String[] {
            ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY,
            ModelerConstants.BINDING_DATA_PATH_PROPERTY});
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
      mapDeclaredProperties(activity, activityJson, propertiesMap.get(ActivityType.class));
      storeAttributes(activity, activityJson);
      storeDescription(activity, activityJson);

      if (activity.getId().toLowerCase().startsWith("gateway"))
      {
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
         if (ModelerConstants.MANUAL_ACTIVITY.equals(extractString(activityJson,
               ModelerConstants.ACTIVITY_TYPE)))
         {
            activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);
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

               ProcessDefinitionType subProcessDefinition = getModelBuilderFacade().getProcessDefinition(
                     getModelBuilderFacade().getModelId(subprocessFullId),
                     getModelBuilderFacade().stripFullId(subprocessFullId));

               activity.setImplementationProcess(subProcessDefinition);
               
               if (activityJson.has(ModelerConstants.SUBPROCESS_MODE_PROPERTY))
               {
                  if (activityJson.get(ModelerConstants.SUBPROCESS_MODE_PROPERTY).getAsString().equals(ModelerConstants.ASYNC_SEPARATE_KEY))
                  {
                     activity.setSubProcessMode(SubProcessModeType.ASYNC_SEPARATE_LITERAL);
                  }
                  else if (activityJson.get(ModelerConstants.SUBPROCESS_MODE_PROPERTY).getAsString().equals(ModelerConstants.SYNC_SEPARATE_KEY))
                  {
                     activity.setSubProcessMode(SubProcessModeType.SYNC_SEPARATE_LITERAL);
                  }
                  else if (activityJson.get(ModelerConstants.SUBPROCESS_MODE_PROPERTY).getAsString().equals(ModelerConstants.SYNC_SHARED_KEY))
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

               ApplicationType application = getModelBuilderFacade().getApplication(
                     getModelBuilderFacade().getModelId(applicationFullId),
                     getModelBuilderFacade().stripFullId(applicationFullId));

               activity.setApplication(application);
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
         }
         else
         {
            transition.setCondition(ModelerConstants.CONDITION_KEY);
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
      if (controlFlowConnectionJson.has(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         controlFlowConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
               controlFlowConnectionJson,
               ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }
      if (controlFlowConnectionJson.has(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         controlFlowConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
               controlFlowConnectionJson,
               ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
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
      JsonObject dataFlowJson = dataFlowConnectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
      if (dataFlowConnectionJson.has(ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         dataFlowConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
               dataFlowConnectionJson,
               ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }
      if (dataFlowConnectionJson.has(ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY))
      {
         dataFlowConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
               dataFlowConnectionJson,
               ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      }

      if (dataFlowJson.has(ModelerConstants.IN_DATA_MAPPING_PROPERTY)
            || dataFlowJson.has(ModelerConstants.OUT_DATA_MAPPING_PROPERTY))
      {
         for (DataMappingType dataMapping : dataFlowConnection.getActivitySymbol()
               .getActivity()
               .getDataMapping())
         {
            if (dataMapping.getId().equals(
                  dataFlowConnection.getDataSymbol().getData().getId()))
            {
             updateDataMapping(dataFlowJson, dataMapping);
            }
         }
      }
   }

   /**
    *
    * @param element
    * @param json
    */
   private void updateSwimlane(LaneSymbol swimlaneSymbol, JsonObject swimlaneSymbolJson)
   {
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
   }

   /**
    *
    * @param processDefinition
    * @param processDefinitionJson
    */
   private void updateProcessDefinition(ProcessDefinitionType processDefinition,
         JsonObject processDefinitionJson)
   {
      mapDeclaredProperties(processDefinition, processDefinitionJson,
            propertiesMap.get(ProcessDefinitionType.class));
      storeAttributes(processDefinition, processDefinitionJson);
      storeDescription(processDefinition, processDefinitionJson);

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
            if (processDefinitionJson.has(ModelerConstants.FORMAL_PARAMETERS_PROPERTY))
            {
               processDefinition.getFormalParameters().getFormalParameter().clear();

               for (Map.Entry<String, ? > entry : processDefinitionJson.get(
                     ModelerConstants.FORMAL_PARAMETERS_PROPERTY)
                     .getAsJsonObject()
                     .entrySet())
               {
                  String key = entry.getKey();
                  JsonObject formalParameter = processDefinitionJson.get(
                        ModelerConstants.FORMAL_PARAMETERS_PROPERTY)
                        .getAsJsonObject()
                        .get(key)
                        .getAsJsonObject();

                  if (formalParameter.get(ModelerConstants.DATA_TYPE_PROPERTY).equals(
                        ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
                  {
                     // getModelBuilderFacade().setFormalParameter(processDefinition,
                     // formalParameter.get(ModelerConstants.ID_PROPERTY).getAsString(),
                     // getModelBuilderFacade().createPrimitiveParameter(processDefinition,
                     // localPrimitive,
                     // formalParameter.get(ModelerConstants.ID_PROPERTY).getAsString(),
                     // formalParameter.get(ModelerConstants.NAME_PROPERTY).getAsString(),
                     // ModeType.IN));
                  }
                  else if (formalParameter.get(ModelerConstants.DATA_TYPE_PROPERTY)
                        .equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
                  {
                     // getModelBuilderFacade().setFormalParameter(processDefinition,
                     // formalParameter.get(ModelerConstants.ID_PROPERTY).getAsString(),
                     // getModelBuilderFacade().createPrimitiveParameter(processDefinition,
                     // localPrimitive,
                     // formalParameter.get(ModelerConstants.ID_PROPERTY).getAsString(),
                     // formalParameter.get(ModelerConstants.NAME_PROPERTY).getAsString(),
                     // ModeType.IN));
                  }
               }
            }
         }
         else if (processDefinitionJson.get(
               ModelerConstants.PROCESS_INTERFACE_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.IMPLEMENTS_PROCESS_INTERFACE_KEY))
         {
            // TODO
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

               DataType data = getModelBuilderFacade().findData(dataFullId);

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
   }

   /**
    *
    * @param activitySymbol
    * @param activitySymbolJson
    */
   private void updateActivitySymbol(ActivitySymbolType activitySymbol,
         JsonObject activitySymbolJson)
   {
      mapDeclaredProperties(activitySymbol, activitySymbolJson,
            propertiesMap.get(ActivitySymbolType.class));

      ActivityType activity = activitySymbol.getActivity();
      JsonObject activityJson = activitySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      updateActivity(activity, activityJson);
   }

   /**
    *
    * @param activitySymbol
    * @param gatewaySymbolJson
    */
   private void updateGatewaySymbol(ActivitySymbolType activitySymbol,
         JsonObject gatewaySymbolJson)
   {
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
      JsonObject startEventJson = startEventSymbolJson.getAsJsonObject("modelElement");

      mapDeclaredProperties(startEventSymbol.getModelElement(), startEventJson,
            propertiesMap.get(StartEventSymbol.class));
      storeAttributes(startEventSymbol.getModelElement(), startEventJson);
      storeDescription(startEventSymbol.getModelElement(), startEventJson);
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

      mapDeclaredProperties(endEventSymbol.getModelElement(),
            endEventSymbolJson.getAsJsonObject("modelElement"),
            propertiesMap.get(EndEventSymbol.class));

      // Does not have a model element yet

      // storeAttributes(endEventSymbol.getModelElement(), endEventJson);
      // storeDescription(endEventSymbol.getModelElement(), endEventJson);
   }

   /**
    * @param application
    * @param applicationJson
    */
   private void updateApplication(ApplicationType application, JsonObject applicationJson)
   {
      mapDeclaredProperties(application, applicationJson,
            propertiesMap.get(ApplicationType.class));
      storeAttributes(application, applicationJson);
      storeDescription(application, applicationJson);

      if (applicationJson.has(ModelerConstants.ACCESS_POINTS_PROPERTY))
      {
         application.getAccessPoint().clear();

         for (Map.Entry<String, ? > entry : applicationJson.get(
               ModelerConstants.ACCESS_POINTS_PROPERTY)
               .getAsJsonObject()
               .entrySet())
         {
            String key = entry.getKey();
            JsonObject accessPointJson = applicationJson.get(
                  ModelerConstants.ACCESS_POINTS_PROPERTY)
                  .getAsJsonObject()
                  .get(key)
                  .getAsJsonObject();

            System.out.println("Access point " + accessPointJson);

            AccessPointType accessPoint = AbstractElementBuilder.F_CWM.createAccessPointType();

            application.getAccessPoint().add(accessPoint);

            accessPoint.setId(accessPointJson.get(ModelerConstants.ID_PROPERTY)
                  .getAsString());
            accessPoint.setName(accessPointJson.get(ModelerConstants.NAME_PROPERTY)
                  .getAsString());

            // if (accessPointJson.get(ModelerConstants.ID_PROPERTY)
            // .getAsString()
            // .equals(DirectionType.IN_LITERAL))
            // {
            // accessPoint.setDirection(DirectionType.IN_LITERAL);
            // }
            // else
            // {
            // accessPoint.setDirection(DirectionType.OUT_LITERAL);
            // }

            if (accessPointJson.has(ModelerConstants.DATA_TYPE_PROPERTY))
            {
               String dataType = accessPointJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
                     .getAsString();

               if (dataType.equals(ModelerConstants.PRIMITIVE_DATA_TYPE_KEY))
               {
                  // accessPoint.setType(@accessPointJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY));
               }
               else if (dataType.equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
               {
                  // accessPoint.setType(getModelBuilderFacade().findDataType(accessPointJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID).getAsString()));
               }
               else if (dataType.equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
               {
                  // accessPoint.setType(getModelBuilderFacade().findDataType(accessPointJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID).getAsString()));
               }
               else
               {
               }
            }

            storeDescription(accessPoint, accessPointJson);
         }
      }
   }

   /**
    * @param typeDeclaration
    * @param applicationJson
    */
   private void updateTypeDeclaration(TypeDeclarationType typeDeclaration,
         JsonObject applicationJson)
   {
      mapDeclaredProperties(typeDeclaration, applicationJson,
            propertiesMap.get(TypeDeclarationType.class));
   }

   /**
    * @param role
    * @param roleJson
    */
   private void updateRole(RoleType role, JsonObject roleJson)
   {
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
      mapDeclaredProperties(conditionalPerformer, conditionalPerformerJson,
            propertiesMap.get(ConditionalPerformerType.class));

      if (conditionalPerformerJson.has(ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY))
      {
         conditionalPerformer.setData(getModelBuilderFacade().findData(
               conditionalPerformerJson.get(
                     ModelerConstants.BINDING_DATA_FULL_ID_PROPERTY).getAsString()));
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
      mapDeclaredProperties(organization, organizationJson,
            propertiesMap.get(OrganizationType.class));
      storeAttributes(organization, organizationJson);
      storeDescription(organization, organizationJson);

      if (organizationJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY))
      {
      organization.setTeamLead((RoleType)getModelBuilderFacade().findParticipant(organizationJson.get(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY).getAsString()));
      }
   }

   /**
    * @param data
    * @param dataJson
    */
   private void updateData(DataType data, JsonObject dataJson)
   {
      System.out.println("Data Json " + dataJson);

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

            getModelBuilderFacade().convertDataType(data, ModelerConstants.PRIMITIVE_DATA_TYPE_KEY);
            getModelBuilderFacade().updatePrimitiveData(data, dataJson.get(ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY).getAsString());

            System.out.println("Primitive Type: " + data.getType());
         }
         else if (dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.STRUCTURED_DATA_TYPE_KEY))
         {
            getModelBuilderFacade().convertDataType(data, ModelerConstants.STRUCTURED_DATA_TYPE_KEY);
            getModelBuilderFacade().updateStructuredDataType(data, dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY).getAsString());

            System.out.println("Structured Type: " + data.getType());
         }
         else if (dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY)
               .getAsString()
               .equals(ModelerConstants.DOCUMENT_DATA_TYPE_KEY))
         {
            getModelBuilderFacade().convertDataType(data, ModelerConstants.DOCUMENT_DATA_TYPE_KEY);
            getModelBuilderFacade().updateDocumentDataType(data, dataJson.get(ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY).getAsString());

            System.out.println("Document Type: " + data.getType());
         }
         else
         {
            System.out.println("Other type "
                  + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
            System.out.println("Other type " + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
            System.out.println("Other type " + dataJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString());
         }
      }
   }

   /**
    * @param model
    * @param modelJson
    */
   private void updateModel(ModelType model, JsonObject modelJson)
   {
      mapDeclaredProperties(model, modelJson, propertiesMap.get(ModelType.class));
      // storeAttributes(model, modelJson);
      // storeDescription(model, modelJson);
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
      if (element != null)
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
   private void storeAttributes(IIdentifiableModelElement element, JsonObject json)
   {
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

               AttributeUtil.setAttribute(element, key, null);
            }
            else
            {
               System.out.println("Setting extended attribute " + key + " to " + attributes.get(key).getAsString());

               AttributeUtil.setAttribute(element, key, attributes.get(key).getAsString());
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

      if (StringUtils.isNotEmpty(description))
      {
         DescriptionType dt = AbstractElementBuilder.F_CWM.createDescriptionType();
         dt.getMixed().add(FeatureMapUtil.createRawTextEntry(description));
         element.setDescription(dt);
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
         modelBuilderFacade = new ModelBuilderFacade(modelManagementStrategy());
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
    * @param dataFlowJson
    * @param dataMapping
    */
   private void updateDataMapping(JsonObject dataFlowJson, DataMappingType dataMapping)
   {
      // If both IN-OUT mapping is present
      if (dataFlowJson.has(ModelerConstants.IN_DATA_MAPPING_PROPERTY)
            && dataFlowJson.has(ModelerConstants.OUT_DATA_MAPPING_PROPERTY))
      {
         if (dataFlowJson.get(ModelerConstants.IN_DATA_MAPPING_PROPERTY).getAsBoolean()
               && dataFlowJson.get(ModelerConstants.OUT_DATA_MAPPING_PROPERTY)
                     .getAsBoolean())
         {
            dataMapping.setDirection(DirectionType.INOUT_LITERAL);
         }
      }
      // IN data mapping is updates
      else if (dataFlowJson.has(ModelerConstants.IN_DATA_MAPPING_PROPERTY))
      {
         if (dataFlowJson.get(ModelerConstants.IN_DATA_MAPPING_PROPERTY).getAsBoolean())
         {
            //If OUT mapping was already set , update to IN-OUT mapping
            if (dataMapping.getDirection().equals(DirectionType.OUT_LITERAL))
            {
               dataMapping.setDirection(DirectionType.INOUT_LITERAL);
            }
            else
            {
               dataMapping.setDirection(DirectionType.IN_LITERAL);
            }
         }
         else
         {
            if (dataMapping.getDirection().equals(DirectionType.INOUT_LITERAL))
            {
               dataMapping.setDirection(DirectionType.OUT_LITERAL);
            }
         }
      }
      // OUT data mapping is updates
      else if (dataFlowJson.has(ModelerConstants.OUT_DATA_MAPPING_PROPERTY))
      {
         if (dataFlowJson.get(ModelerConstants.OUT_DATA_MAPPING_PROPERTY).getAsBoolean())
         {
          //If IN mapping was already set , update to IN-OUT mapping
            if (dataMapping.getDirection().equals(DirectionType.IN_LITERAL))
            {
               dataMapping.setDirection(DirectionType.INOUT_LITERAL);
            }
            else
            {
               dataMapping.setDirection(DirectionType.OUT_LITERAL);
            }
         }
         else
         {
            if (dataMapping.getDirection().equals(DirectionType.INOUT_LITERAL))
            {
               dataMapping.setDirection(DirectionType.IN_LITERAL);
            }
         }
      }
   }

}
