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

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newApplicationActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newSubProcessActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.activity.BpmApplicationActivityBuilder;
import org.eclipse.stardust.model.xpdl.builder.activity.BpmSubProcessActivityBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;

/**
 * 
 * @author Marc.Gille
 * 
 */
public class ModelElementUnmarshaller
{
   private static ModelElementUnmarshaller instance;

   private Map<Class, String[]> symbolPropertiesMap;

   private Map<Class, String[]> modelElementPropertiesMap;

   private Map<Class, String[]> modelElementReferencePropertiesMap;

   /**
    * 
    * @return
    */
   public static synchronized ModelElementUnmarshaller getInstance()
   {
      if (instance == null)
      {
         instance = new ModelElementUnmarshaller();
      }

      return instance;
   }

   /**
	 * 
	 */
   public ModelElementUnmarshaller()
   {
      super();

      symbolPropertiesMap = new HashMap<Class, String[]>();
      modelElementPropertiesMap = new HashMap<Class, String[]>();
      modelElementReferencePropertiesMap = new HashMap<Class, String[]>();

      symbolPropertiesMap.put(ActivitySymbolType.class, new String[] {"x", "y"});
      modelElementPropertiesMap.put(ActivitySymbolType.class, new String[] {
            "name", "description"});

      symbolPropertiesMap.put(StartEventSymbol.class, new String[] {"x", "y"});
      modelElementPropertiesMap.put(StartEventSymbol.class, new String[] {
            "name", "description"});

      symbolPropertiesMap.put(EndEventSymbol.class, new String[] {"x", "y"});
      modelElementPropertiesMap.put(EndEventSymbol.class, new String[] {
            "name", "description"});
   }

   /**
    * 
    * @param element
    * @param json
    */
   public void populateFromJson(IModelElement element, JsonObject json)
   {
      System.out.println("Unmarshalling: " + element + " " + json);

      if (element instanceof ProcessDefinitionType)
      {
         mapDeclaredModelElementProperties(element, json,
               modelElementPropertiesMap.get(ProcessDefinitionType.class));
      }
      else if (element instanceof ActivitySymbolType)
      {
         if (((ActivitySymbolType) element).getActivity()
               .getName()
               .toLowerCase()
               .startsWith("gateway"))
         {
            System.out.println("Handling Gateway");
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

         ProcessDefinitionType subProcessDefinition = MBFacade.getProcessDefinition(
               MBFacade.getModelId(subprocessFullId),
               MBFacade.stripFullId(subprocessFullId));
         ModelType subProcessModel = ModelUtils.findContainingModel(subProcessDefinition);
         BpmSubProcessActivityBuilder subProcessActivity = newSubProcessActivity(ModelUtils.findContainingProcess(activity));

         subProcessActivity.setActivity(activity);
         subProcessActivity.setSubProcessModel(subProcessModel);
         subProcessActivity.invokingProcess(subProcessDefinition);
      }
      else if (ModelerConstants.APPLICATION_ACTIVITY.equals(extractString(activityJson,
            ModelerConstants.ACTIVITY_TYPE)))
      {
         activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);

         String applicationFullId = extractString(activityJson,
               ModelerConstants.APPLICATION_FULL_ID_PROPERTY);

         ApplicationType application = MBFacade.getApplication(
               MBFacade.getModelId(applicationFullId),
               MBFacade.stripFullId(applicationFullId));

         BpmApplicationActivityBuilder applicationActivity = newApplicationActivity(ModelUtils.findContainingProcess(activity));
         applicationActivity.setActivity(activity);
         ModelType applicationModel = ModelUtils.findContainingModel(application);

         applicationActivity.setApplicationModel(applicationModel);
         applicationActivity.invokingApplication(application);
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

   }

   /**
    * 
    * @param startEventSymbol
    * @param startEventSymbolJson
    */
   private void updateStartEventSymbol(StartEventSymbol startEventSymbol,
         JsonObject startEventSymbolJson)
   {
      mapDeclaredModelElementProperties(startEventSymbol,
            startEventSymbolJson.getAsJsonObject("modelElement"),
            modelElementPropertiesMap.get(StartEventSymbol.class));
      mapDeclaredSymbolProperties(startEventSymbol, startEventSymbolJson,
            symbolPropertiesMap.get(StartEventSymbol.class));
   }

   /**
    * 
    * @param endEventSymbol
    * @param endEventSymbolJson
    */
   private void updateEndEventSymbol(EndEventSymbol endEventSymbol,
         JsonObject endEventSymbolJson)
   {
      mapDeclaredModelElementProperties(endEventSymbol,
            endEventSymbolJson.getAsJsonObject("modelElement"),
            modelElementPropertiesMap.get(EndEventSymbol.class));
      mapDeclaredSymbolProperties(endEventSymbol, endEventSymbolJson,
            symbolPropertiesMap.get(EndEventSymbol.class));
   }

   /**
    * 
    * @param modelElement
    * @param modelElementJson
    * @param modelElementProperties
    */
   private void mapDeclaredModelElementProperties(IModelElement modelElement,
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
   private void mapProperty(IModelElement targetElement, JsonObject request,
         String property)
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
}
