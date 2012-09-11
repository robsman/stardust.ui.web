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

package org.eclipse.stardust.ui.web.modeler.edit.diagram.node;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.XmlTextNode;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;

/**
 * @author Sidharth.Singh
 */
@CommandHandler
public class ConnectionCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "connection.create")
   public void createConnection(IIdentifiableElement targetElement, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(targetElement);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(targetElement);

      synchronized (model)
      {
         long maxOid = XpdlModelUtils.getMaxUsedOid(model);
         if (ModelerConstants.ACTIVITY_KEY.equals(extractString(request,
               ModelerConstants.FROM_MODEL_ELEMENT_TYPE))
               || ModelerConstants.GATEWAY.equals(extractString(request,
                     ModelerConstants.FROM_MODEL_ELEMENT_TYPE)))
         {
            if (ModelerConstants.ACTIVITY_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE))
                  || ModelerConstants.GATEWAY.equals(extractString(request,
                        ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               createControlFlowConnection(
                     request,
                     processDefinition,
                     getModelBuilderFacade().findActivitySymbol(processDefinition.getDiagram().get(0),
                           extractLong(request, ModelerConstants.FROM_MODEL_ELEMENT_OID)),
                     getModelBuilderFacade().findActivitySymbol(processDefinition.getDiagram().get(0),
                           extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID)),
                     maxOid);
            }
            else if (ModelerConstants.EVENT_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               StartEventSymbol startEventSymbol = getModelBuilderFacade().findStartEventSymbol(
                     processDefinition.getDiagram().get(0),
                     extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID));

               if (null != startEventSymbol)
               {
                  createControlFlowConnection(request, processDefinition,
                        startEventSymbol, getModelBuilderFacade().findActivitySymbol(
                              processDefinition.getDiagram().get(0),
                              extractLong(request,
                                    ModelerConstants.FROM_MODEL_ELEMENT_OID)), maxOid);
               }
               else
               {
                  EndEventSymbol endEventSymbol = getModelBuilderFacade().findEndEventSymbol(
                        processDefinition.getDiagram().get(0),
                        extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID));
                  createControlFlowConnection(request, processDefinition,
                        getModelBuilderFacade().findActivitySymbol(
                              processDefinition.getDiagram().get(0),
                              extractLong(request,
                                    ModelerConstants.FROM_MODEL_ELEMENT_OID)),
                        endEventSymbol, maxOid);

               }
            }
            else if (ModelerConstants.DATA.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               createDataFlowConnection(
                     request,
                     processDefinition,
                     getModelBuilderFacade().findActivitySymbol(processDefinition.getDiagram().get(0),
                           extractLong(request, ModelerConstants.FROM_MODEL_ELEMENT_OID)),
                     getModelBuilderFacade().findDataSymbol(processDefinition.getDiagram().get(0),
                           extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID)),
                     maxOid);
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + extractString(request, ModelerConstants.TO_MODEL_ELEMENT_TYPE)
                     + " for connection.");
            }
         }
         else if (ModelerConstants.EVENT_KEY.equals(extractString(request,
               ModelerConstants.FROM_MODEL_ELEMENT_TYPE)))
         {
            if (ModelerConstants.ACTIVITY_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               try
               {
                  StartEventSymbol startEventSymbol = getModelBuilderFacade().findStartEventSymbol(
                        processDefinition.getDiagram().get(0),
                        extractLong(request, ModelerConstants.FROM_MODEL_ELEMENT_OID));

                  createControlFlowConnection(
                        request,
                        processDefinition,
                        startEventSymbol,
                        getModelBuilderFacade().findActivitySymbol(
                              processDefinition.getDiagram().get(0),
                              extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID)),
                        maxOid);
               }
               catch (ObjectNotFoundException x)
               {
                  EndEventSymbol endEventSymbol = getModelBuilderFacade().findEndEventSymbol(
                        processDefinition.getDiagram().get(0),
                        extractLong(request, ModelerConstants.FROM_MODEL_ELEMENT_OID));
                  createControlFlowConnection(
                        request,
                        processDefinition,
                        getModelBuilderFacade().findActivitySymbol(
                              processDefinition.getDiagram().get(0),
                              extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID)),
                        endEventSymbol, maxOid);
               }
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + extractString(request, ModelerConstants.TO_MODEL_ELEMENT_TYPE)
                     + " for connection.");
            }
         }
         else if (ModelerConstants.DATA.equals(extractString(request,
               ModelerConstants.FROM_MODEL_ELEMENT_TYPE)))
         {
            if (ModelerConstants.ACTIVITY_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               createDataFlowConnection(
                     request,
                     processDefinition,
                     getModelBuilderFacade().findActivitySymbol(processDefinition.getDiagram().get(0),
                           extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID)),
                     getModelBuilderFacade().findDataSymbol(processDefinition.getDiagram().get(0),
                           extractLong(request, ModelerConstants.FROM_MODEL_ELEMENT_OID)),
                     maxOid);
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + extractString(request, ModelerConstants.TO_MODEL_ELEMENT_TYPE)
                     + " for connection.");
            }
         }
         else
         {
            throw new IllegalArgumentException("Unsupported source symbol type "
                  + extractString(request, ModelerConstants.FROM_MODEL_ELEMENT_TYPE)
                  + " for connection.");
         }
      }
   }

   @OnCommand(commandId = "connection.delete")
   public void deleteConnection(IIdentifiableElement targetElement, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(targetElement);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(targetElement);

      Long connectionOid = extractLong(request, ModelerConstants.OID_PROPERTY);
      synchronized (model)
      {
         try
         {
            TransitionConnectionType transitionConnection = getModelBuilderFacade().findTransitionConnectionByModelOid(
                  processDefinition, connectionOid);

            processDefinition.getDiagram()
                  .get(0)
                  .getPoolSymbols()
                  .get(0)
                  .getTransitionConnection()
                  .remove(transitionConnection);

            if (transitionConnection.getTransition() != null)
            {
               processDefinition.getTransition().remove(
                     transitionConnection.getTransition());
            }
         }
         catch (ObjectNotFoundException x)
         {
            DataMappingConnectionType dataMappingConnection = getModelBuilderFacade().findDataMappingConnectionByModelOid(
                  processDefinition, connectionOid);

            processDefinition.getDiagram()
                  .get(0)
                  .getPoolSymbols()
                  .get(0)
                  .getDataMappingConnection()
                  .remove(dataMappingConnection);
         }
      }
   }

   /**
    *
    * @param sourceActivitySymbol
    * @param targetActivitySymbol
    * @throws JSONException
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition,
         ActivitySymbolType sourceActivitySymbol,
         ActivitySymbolType targetActivitySymbol, long maxOid)
   {
      JsonObject controlFlowJson = connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
      TransitionType transition = AbstractElementBuilder.F_CWM.createTransitionType();

      processDefinition.getTransition().add(transition);

      transition.setElementOid(++maxOid);
      transition.setFrom(sourceActivitySymbol.getActivity());
      transition.setTo(targetActivitySymbol.getActivity());
      transition.setId(extractString(controlFlowJson, ModelerConstants.ID_PROPERTY));

      if (extractBoolean(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY))
      {
         transition.setCondition(ModelerConstants.OTHERWISE_KEY);
      }
      else
      {
         transition.setCondition(ModelerConstants.CONDITION_KEY);

         XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();
         ModelUtils.setCDataString(expression.getMixed(), "true", true);
         transition.setExpression(expression);

      }

      // setDescription(transition,
      // controlFlowJson.getString(DESCRIPTION_PROPERTY));

      TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM.createTransitionConnectionType();

      transition.getTransitionConnections().add(transitionConnection);
      transitionConnection.setTransition(transition);

      transitionConnection.setElementOid(++maxOid);
      transitionConnection.setSourceActivitySymbol(sourceActivitySymbol);
      transitionConnection.setTargetActivitySymbol(targetActivitySymbol);
      transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

      // TODO Obtain pool from call

      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getTransitionConnection()
            .add(transitionConnection);
   }

   /**
    *
    * @param connectionJson
    * @param processDefinition
    * @param sourceActivitySymbol
    * @param targetActivitySymbol
    * @param maxOid
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, StartEventSymbol startEventSymbol,
         ActivitySymbolType targetActivitySymbol, long maxOid)
   {
      TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM.createTransitionConnectionType();

      transitionConnection.setElementOid(++maxOid);
      transitionConnection.setSourceNode(startEventSymbol);
      transitionConnection.setTargetNode(targetActivitySymbol);
      transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getTransitionConnection()
            .add(transitionConnection);
   }

   /**
    *
    * @param connectionJson
    * @param processDefinition
    * @param sourceActivitySymbol
    * @param targetActivitySymbol
    * @param maxOid
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition,
         ActivitySymbolType sourceActivitySymbol, EndEventSymbol endEventSymbol,
         long maxOid)
   {
      TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM.createTransitionConnectionType();
      transitionConnection.setElementOid(++maxOid);
      transitionConnection.setSourceNode(sourceActivitySymbol);
      transitionConnection.setTargetNode(endEventSymbol);
      transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      // TODO Obtain pool from call

      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getTransitionConnection()
            .add(transitionConnection);
   }

   /**
    *
    * @param connectionJson
    * @param processDefinition
    * @param sourceActivitySymbol
    * @param dataSymbol
    * @param maxOid
    */
   private void createDataFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, ActivitySymbolType activitySymbol,
         DataSymbolType dataSymbol, long maxOid)
   {

      System.out.println("Create data flow connection");

      DataType data = dataSymbol.getData();
      ActivityType activity = activitySymbol.getActivity();
      boolean inDataMapping = extractBoolean(
            connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            ModelerConstants.IN_DATA_MAPPING_PROPERTY);

      DataMappingType dataMapping = AbstractElementBuilder.F_CWM.createDataMappingType();
      DataMappingConnectionType dataMappingConnection = AbstractElementBuilder.F_CWM.createDataMappingConnectionType();

      dataMapping.setElementOid(++maxOid);
      dataMapping.setId(data.getId());
      dataMapping.setName(data.getName());

      if (inDataMapping)
      {
         dataMapping.setDirection(DirectionType.get(DirectionType.IN));
      }
      else
      {
         dataMapping.setDirection(DirectionType.get(DirectionType.OUT));
      }

      dataMapping.setData(data);
      // TODO Incomplete

      // if (activity.getImplementation().getLiteral().equals("Application"))
      // {
      // dataMapping.setContext(PredefinedConstants.APPLICATION_CONTEXT);
      // dataMapping.setApplicationAccessPoint(element.getProps().getEnds()
      // .getAccesspoint());
      // }
      // else
      // {
      dataMapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
      // }

      activity.getDataMapping().add(dataMapping);

      // TODO Obtain pool from call

      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getDataMappingConnection()
            .add(dataMappingConnection);

      dataMappingConnection.setElementOid(++maxOid);
      dataMappingConnection.setActivitySymbol(activitySymbol);
      dataMappingConnection.setDataSymbol(dataSymbol);
      activitySymbol.getDataMappings().add(dataMappingConnection);
      dataSymbol.getDataMappings().add(dataMappingConnection);
      dataMappingConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      dataMappingConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
            connectionJson, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

   }

   /**
    *
    * @param orientation
    * @return
    */
   public static String mapAnchorOrientation(int orientation)
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

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }

}
