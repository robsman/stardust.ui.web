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
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newApplicationActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newSubProcessActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.activity.BpmApplicationActivityBuilder;
import org.eclipse.stardust.model.xpdl.builder.activity.BpmSubProcessActivityBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;

import com.google.gson.JsonObject;

/**
 * 
 * @author Marc.Gille
 * 
 */
public abstract class ModelElementUnmarshaller
{
   private Map<Class<? >, String[]> symbolPropertiesMap;

   private Map<Class<? >, String[]> modelElementPropertiesMap;

   private Map<Class<? >, String[]> modelElementReferencePropertiesMap;

   protected abstract ModelManagementStrategy modelManagementStrategy();
   
   private MBFacade facade;

   /**
	 *
	 */
   public ModelElementUnmarshaller()
   {
      symbolPropertiesMap = newHashMap();
      modelElementPropertiesMap = newHashMap();
      modelElementReferencePropertiesMap = newHashMap();

      modelElementPropertiesMap.put(ProcessDefinitionType.class, new String[] {
            "name", "id"});
      symbolPropertiesMap.put(ActivitySymbolType.class, new String[] {ModelerConstants.X_PROPERTY, ModelerConstants.Y_PROPERTY});
      modelElementPropertiesMap.put(ActivitySymbolType.class, new String[] {ModelerConstants.NAME_PROPERTY});

      symbolPropertiesMap.put(StartEventSymbol.class, new String[] {ModelerConstants.X_PROPERTY, ModelerConstants.Y_PROPERTY});
      modelElementPropertiesMap.put(StartEventSymbol.class, new String[] {ModelerConstants.NAME_PROPERTY});

      symbolPropertiesMap.put(EndEventSymbol.class, new String[] {ModelerConstants.X_PROPERTY, ModelerConstants.Y_PROPERTY});
      modelElementPropertiesMap.put(EndEventSymbol.class, new String[] {ModelerConstants.NAME_PROPERTY});

      modelElementPropertiesMap.put(ApplicationType.class, new String[] {ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});

      modelElementPropertiesMap.put(TypeDeclarationType.class,
            new String[] {"name", "id"});

      modelElementPropertiesMap.put(ModelType.class, new String[] {ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});

      modelElementPropertiesMap.put(DataType.class, new String[] {ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});

      modelElementPropertiesMap.put(RoleType.class, new String[] {ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});

      modelElementPropertiesMap.put(OrganizationType.class, new String[] {ModelerConstants.NAME_PROPERTY, ModelerConstants.ID_PROPERTY});
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
         updateModelType((ModelType) element, json);
      }
      else if (element instanceof DataType)
      {
         updateDataType((DataType) element, json);
      }
      else if (element instanceof RoleType)
      {
         updateRole((RoleType) element, json);
      }
      else if (element instanceof OrganizationType)
      {
         updateOrganization((OrganizationType) element, json);
      }
      else 
      {
         System.out.println("===> Swimlane? " + element);
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
      mapDeclaredModelElementProperties(processDefinition, processDefinitionJson,
            modelElementPropertiesMap.get(ProcessDefinitionType.class));
      storeAttributes(processDefinition, processDefinitionJson);
      storeDescription(processDefinition, processDefinitionJson);

//      processDefinition.getDataPath().clear();
//
//      JsonObject dataPathes = processDefinitionJson.get(
//            ModelerConstants.DATA_PATHES_PROPERTY).getAsJsonObject();
//
//      for (Map.Entry<String, JsonElement> entry : dataPathes.entrySet())
//      {
//         String key = entry.getKey();
//         JsonObject dataPathJson = dataPathes.get(key).getAsJsonObject();
//
//         // TODO Kernel How to create?
//         DataPathType dataPath = null;
//
//         // TODO Kernel Make more elegant in Facade
//         String dataFullId = dataPathJson.get(ModelerConstants.DATA_FULL_ID_PROPERTY)
//               .getAsString();
//
//         DataType data = new MBFacade(modelManagementStrategy()).getData(
//               MBFacade.getModelId(dataFullId), MBFacade.stripFullId(dataFullId));
//
//         dataPath.setData(data);
//         dataPath.setDataPath(dataPathJson.get(ModelerConstants.DATA_PATH_PROPERTY)
//               .getAsString());
//         dataPath.setId(dataPathJson.get(ModelerConstants.ID_PROPERTY).getAsString());
//         dataPath.setName(dataPathJson.get(ModelerConstants.NAME_PROPERTY).getAsString());
//         dataPath.setDescriptor(dataPathJson.get(ModelerConstants.DESCRIPTOR_PROPERTY)
//               .getAsBoolean());
//         dataPath.setKey(dataPathJson.get(ModelerConstants.KEY_DESCRIPTOR_PROPERTY)
//               .getAsBoolean());
//
//         dataPath.setDirection(DirectionType.IN_LITERAL);
//
//         processDefinition.getDataPath().add(dataPath);
//      }
   }

   /**
    * 
    * @param activitySymbol
    * @param activitySymbolJson
    */
   private void updateActivitySymbol(ActivitySymbolType activitySymbol,
         JsonObject activitySymbolJson)
   {
      ActivityType activity = activitySymbol.getActivity();
      JsonObject activityJson = activitySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      mapDeclaredModelElementProperties(activity, activityJson,
            modelElementPropertiesMap.get(ActivitySymbolType.class));
      mapDeclaredSymbolProperties(activitySymbol, activitySymbolJson,
            symbolPropertiesMap.get(ActivitySymbolType.class));
      storeAttributes(activity, activityJson);
      storeDescription(activity, activityJson);

      if (ModelerConstants.MANUAL_ACTIVITY.equals(extractString(activityJson,
            ModelerConstants.ACTIVITY_TYPE)))
      {
         activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);
      }
      else if (ModelerConstants.SUBPROCESS_ACTIVITY.equals(extractString(activityJson,
            ModelerConstants.ACTIVITY_TYPE)))
      {
         activity.setImplementation(ActivityImplementationType.SUBPROCESS_LITERAL);

         String subprocessFullId = extractString(activityJson,
               ModelerConstants.SUBPROCESS_ID);

         ProcessDefinitionType subProcessDefinition = facade()
               .getProcessDefinition(facade().getModelId(subprocessFullId),
                     facade().stripFullId(subprocessFullId));
         
         activity.setImplementationProcess(subProcessDefinition);
      }
      else if (ModelerConstants.APPLICATION_ACTIVITY.equals(extractString(activityJson,
            ModelerConstants.ACTIVITY_TYPE)))
      {
         activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);

         String applicationFullId = extractString(activityJson,
               ModelerConstants.APPLICATION_FULL_ID_PROPERTY);

         ApplicationType application = facade().getApplication(
               facade().getModelId(applicationFullId),
               facade().stripFullId(applicationFullId));

         activity.setApplication(application);
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
      ActivityType activity = activitySymbol.getActivity();
      JsonObject activityJson = gatewaySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

      mapDeclaredModelElementProperties(activity, activityJson,
            modelElementPropertiesMap.get(ActivitySymbolType.class));
      mapDeclaredSymbolProperties(activitySymbol, gatewaySymbolJson,
            symbolPropertiesMap.get(ActivitySymbolType.class));
      storeAttributes(activity, activityJson);
      storeDescription(activity, activityJson);

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

   /**
    * 
    * @param startEventSymbol
    * @param startEventSymbolJson
    */
   private void updateStartEventSymbol(StartEventSymbol startEventSymbol,
         JsonObject startEventSymbolJson)
   {
      JsonObject startEventJson = startEventSymbolJson.getAsJsonObject("modelElement");
      
      mapDeclaredModelElementProperties(startEventSymbol.getModelElement(),
            startEventJson,
            modelElementPropertiesMap.get(StartEventSymbol.class));
      mapDeclaredSymbolProperties(startEventSymbol, startEventSymbolJson,
            symbolPropertiesMap.get(StartEventSymbol.class));
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
      
      mapDeclaredModelElementProperties(endEventSymbol.getModelElement(),
            endEventSymbolJson.getAsJsonObject("modelElement"),
            modelElementPropertiesMap.get(EndEventSymbol.class));
      mapDeclaredSymbolProperties(endEventSymbol, endEventSymbolJson,
            symbolPropertiesMap.get(EndEventSymbol.class));

      // Does not have a model element yet
      
//      storeAttributes(endEventSymbol.getModelElement(), endEventJson);
//      storeDescription(endEventSymbol.getModelElement(), endEventJson);
   }

   /**
    * @param application
    * @param applicationJson
    */
   private void updateApplication(ApplicationType application, JsonObject applicationJson)
   {
      mapDeclaredModelElementProperties(application, applicationJson,
            modelElementPropertiesMap.get(ApplicationType.class));
      storeAttributes(application, applicationJson);
      storeDescription(application, applicationJson);
   }

   /**
    * @param typeDeclaration
    * @param applicationJson
    */
   private void updateTypeDeclaration(TypeDeclarationType typeDeclaration,
         JsonObject applicationJson)
   {
      mapDeclaredModelElementProperties(typeDeclaration, applicationJson,
            modelElementPropertiesMap.get(TypeDeclarationType.class));
   }

   /**
    * @param role
    * @param roleJson
    */
   private void updateRole(RoleType role, JsonObject roleJson)
   {
      mapDeclaredModelElementProperties(role, roleJson,
            modelElementPropertiesMap.get(RoleType.class));
      storeAttributes(role, roleJson);
      storeDescription(role, roleJson);
   }

   /**
    * @param organization
    * @param organizationJson
    */
   private void updateOrganization(OrganizationType organization,
         JsonObject organizationJson)
   {
      mapDeclaredModelElementProperties(organization, organizationJson,
            modelElementPropertiesMap.get(OrganizationType.class));
      storeAttributes(organization, organizationJson);
      storeDescription(organization, organizationJson);
   }

   /**
    * @param dataType
    * @param dataJson
    */
   private void updateDataType(DataType dataType, JsonObject dataJson)
   {
      mapDeclaredModelElementProperties(dataType, dataJson,
            modelElementPropertiesMap.get(DataType.class));
   }

   /**
    * @param model
    * @param modelJson
    */
   private void updateModelType(ModelType model, JsonObject modelJson)
   {
      mapDeclaredModelElementProperties(model, modelJson,
            modelElementPropertiesMap.get(ModelType.class));
   }

   /**
    * 
    * @param modelElement
    * @param modelElementJson
    * @param modelElementProperties
    */
   private void mapDeclaredModelElementProperties(EObject modelElement,
         JsonObject modelElementJson, String[] modelElementProperties)
   {
      if (modelElement != null)
      {
         for (String property : modelElementProperties)
         {
            mapProperty(modelElement, modelElementJson, property);
         }
      }
   }

   /**
    * 
    * @param symbol
    * @param symbolJson
    * @param symbolProperties
    */
   private void mapDeclaredSymbolProperties(IModelElement symbol, JsonObject symbolJson,
         String[] symbolProperties)
   {
      if (symbol != null)
      {
         for (String property : symbolProperties)
         {
            mapProperty(symbol, symbolJson, property);
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
            String value = attributes.get(key).getAsString();

            AttributeUtil.setAttribute(element, key, value);
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
   
   private MBFacade facade()
   {
      if (facade == null)
      {
         facade = new MBFacade(modelManagementStrategy());
      }
      return facade;
   }
}
