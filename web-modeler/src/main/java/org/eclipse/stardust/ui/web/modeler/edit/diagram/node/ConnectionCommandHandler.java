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

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IFlowObjectSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
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
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;

/**
 * @author Sidharth.Singh
 */
@CommandHandler
public class ConnectionCommandHandler
{

   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "connection.create")
   public void createConnection(ModelType model, IIdentifiableElement targetElement,
         JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(targetElement);

      synchronized (model)
      {
         long maxOid = XpdlModelUtils.getMaxUsedOid(model);
         DiagramType diagram = processDefinition.getDiagram().get(0);

         long fromSymbolOid = extractLong(request,
               ModelerConstants.FROM_MODEL_ELEMENT_OID);
         long toSymbolOid = extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID);

         if (ModelerConstants.ACTIVITY_KEY.equals(extractString(request,
               ModelerConstants.FROM_MODEL_ELEMENT_TYPE))
               || ModelerConstants.GATEWAY.equals(extractString(request,
                     ModelerConstants.FROM_MODEL_ELEMENT_TYPE)))
         {
            ActivitySymbolType fromActivitySymbol = getModelBuilderFacade().findActivitySymbol(
                  diagram, fromSymbolOid);

            if (ModelerConstants.ACTIVITY_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE))
                  || ModelerConstants.GATEWAY.equals(extractString(request,
                        ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               createControlFlowConnection(
                     request,
                     processDefinition,
                     fromActivitySymbol,
                     getModelBuilderFacade().findActivitySymbol(diagram, toSymbolOid),
                     maxOid);
            }
            else if (ModelerConstants.EVENT_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               StartEventSymbol startEventSymbol = getModelBuilderFacade().findStartEventSymbol(
                     diagram, toSymbolOid);
               if (null != startEventSymbol)
               {
                  // start events don't have incoming transitions, simply create an outgoing one
                  createControlFlowConnection(request, processDefinition,
                        startEventSymbol, fromActivitySymbol, maxOid);
               }
               else
               {
                  AbstractEventSymbol toEventSymbol = getModelBuilderFacade().findEndEventSymbol(
                        diagram, toSymbolOid);
                  if (null == toEventSymbol)
                  {
                     toEventSymbol = getModelBuilderFacade().findIntermediateEventSymbol(
                           diagram, toSymbolOid);
                  }
                  createControlFlowConnection(request, processDefinition,
                        fromActivitySymbol, toEventSymbol, maxOid);

               }
            }
            else if (ModelerConstants.DATA.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               createDataFlowConnection(request, processDefinition, fromActivitySymbol,
                     getModelBuilderFacade().findDataSymbol(diagram, toSymbolOid), maxOid);
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
               AbstractEventSymbol fromEventSymbol = getModelBuilderFacade().findStartEventSymbol(
                     diagram, fromSymbolOid);
               if (null == fromEventSymbol)
               {
                  fromEventSymbol = getModelBuilderFacade().findIntermediateEventSymbol(
                        diagram, fromSymbolOid);
               }
               if (null != fromEventSymbol)
               {
                  createControlFlowConnection(request, processDefinition,
                        fromEventSymbol,
                        getModelBuilderFacade().findActivitySymbol(diagram, toSymbolOid),
                        maxOid);
               }
               else
               {
                  EndEventSymbol endEventSymbol = getModelBuilderFacade().findEndEventSymbol(
                        diagram, fromSymbolOid);
                  if (null != endEventSymbol)
                  {
                     // end events don't have outgoing transitions, simply create an incoming one
                     createControlFlowConnection(
                           request,
                           processDefinition,
                           getModelBuilderFacade().findActivitySymbol(diagram,
                                 toSymbolOid), endEventSymbol, maxOid);
                  }
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
               createDataFlowConnection(request, processDefinition,
                     getModelBuilderFacade().findActivitySymbol(diagram, toSymbolOid),
                     getModelBuilderFacade().findDataSymbol(diagram, fromSymbolOid),
                     maxOid);
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + extractString(request, ModelerConstants.TO_MODEL_ELEMENT_TYPE)
                     + " for connection.");
            }
         }
         else if (ModelerConstants.ANNOTATION_SYMBOL.equals(extractString(request,
               ModelerConstants.TO_MODEL_ELEMENT_TYPE))
               || ModelerConstants.ANNOTATION_SYMBOL.equals(extractString(request,
                     ModelerConstants.FROM_MODEL_ELEMENT_TYPE)))
         {
            String typeInRequest = extractString(request,
                  ModelerConstants.FROM_MODEL_ELEMENT_TYPE);
            Long oid = fromSymbolOid;

            INodeSymbol sourceSymbol = getNodeSymbol(request, diagram, typeInRequest, oid);

            typeInRequest = extractString(request, ModelerConstants.TO_MODEL_ELEMENT_TYPE);
            oid = toSymbolOid;

            INodeSymbol targetSymbol = getNodeSymbol(request, diagram, typeInRequest, oid);

            if (null != sourceSymbol && null != targetSymbol)
            {
               // TODO: Association is not supported in 7.1 so commented the code
               // createAssociation(request, processDefinition, sourceSymbol,
               // targetSymbol,
               // maxOid);
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


   /**
    * Finds NodeSymbol in Model based on input parameters
    *
    * @param request
    *           : json Object
    * @param diagram
    * @param typeInRequest
    *           (Activity, GateWay etc)
    * @param oidInRequest
    * @return
    */
   private INodeSymbol getNodeSymbol(JsonObject request, DiagramType diagram,
         String typeInRequest, long oidInRequest)
   {
      INodeSymbol nodeSymbol = null;

      if (ModelerConstants.ACTIVITY_KEY.equals(typeInRequest))
      {
         nodeSymbol = getModelBuilderFacade().findActivitySymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.GATEWAY.equals(typeInRequest))
      {
         nodeSymbol = getModelBuilderFacade().findActivitySymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.EVENT_KEY.equals(typeInRequest))
      {
         nodeSymbol = getModelBuilderFacade().findStartEventSymbol(diagram, oidInRequest);
         if (null == nodeSymbol)
         {
            nodeSymbol = getModelBuilderFacade().findEndEventSymbol(diagram, oidInRequest);
         }
         if (null == nodeSymbol)
         {
            nodeSymbol = getModelBuilderFacade().findIntermediateEventSymbol(diagram,
                  oidInRequest);
         }
      }
      else if (ModelerConstants.DATA.equals(typeInRequest))
      {
         nodeSymbol = getModelBuilderFacade().findDataSymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.ANNOTATION_SYMBOL.equals(typeInRequest))
      {
         nodeSymbol = getModelBuilderFacade().findAnnotationSymbol(diagram, oidInRequest);
      }
      return nodeSymbol;
   }

   @OnCommand(commandId = "connection.delete")
   public void deleteConnection(ModelType model, IIdentifiableElement targetElement, JsonObject request)
   {
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
            try
            {
               DataMappingConnectionType dataMappingConnection = getModelBuilderFacade().findDataMappingConnectionByModelOid(
                     processDefinition, connectionOid);
               List<DataMappingType> dataMapping = CollectionUtils.newArrayList();
               for (DataMappingType dataMappingType : dataMappingConnection.getActivitySymbol()
                     .getActivity()
                     .getDataMapping())
               {
                  if (dataMappingType.getData()
                        .getId()
                        .equals(dataMappingConnection.getDataSymbol().getData().getId()))
                  {
                     dataMapping.add(dataMappingType);
                  }
               }
               dataMappingConnection.getActivitySymbol()
                     .getActivity()
                     .getDataMapping()
                     .removeAll(dataMapping);
               dataMappingConnection.getDataSymbol()
                     .getData()
                     .getDataMappings()
                     .removeAll(dataMapping);
               processDefinition.getDiagram()
                     .get(0)
                     .getPoolSymbols()
                     .get(0)
                     .getDataMappingConnection()
                     .remove(dataMappingConnection);
            }
            catch (Exception e)
            {
               // TODO: handle exception
            }
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

      TransitionType transition = doCreateTransition(processDefinition,
            sourceActivitySymbol.getActivity(), targetActivitySymbol.getActivity(),
            controlFlowJson, ++maxOid);

      doCreateTransitionSymbol(processDefinition, sourceActivitySymbol,
            targetActivitySymbol, transition, connectionJson, ++maxOid);
   }


   private TransitionConnectionType doCreateTransitionSymbol(
         ProcessDefinitionType processDefinition, IFlowObjectSymbol sourceActivitySymbol,
         IFlowObjectSymbol targetActivitySymbol, TransitionType transition,
         JsonObject connectionJson, long connectionOid)
   {
      TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM.createTransitionConnectionType();

      if (null != transition)
      {
         transition.getTransitionConnections().add(transitionConnection);
         transitionConnection.setTransition(transition);
      }

      transitionConnection.setElementOid(connectionOid);
      transitionConnection.setSourceNode(sourceActivitySymbol);
      transitionConnection.setTargetNode(targetActivitySymbol);
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

      return transitionConnection;
   }


   private TransitionType doCreateTransition(ProcessDefinitionType processDefinition,
         ActivityType sourceActivity, ActivityType targetActivity,
         JsonObject controlFlowJson, long transitionOid)
   {
      TransitionType transition = AbstractElementBuilder.F_CWM.createTransitionType();

      processDefinition.getTransition().add(transition);

      transition.setElementOid(transitionOid);
      transition.setFrom(sourceActivity);
      transition.setTo(targetActivity);
      String transitionId = extractString(controlFlowJson, ModelerConstants.ID_PROPERTY);
      if (isEmpty(transitionId))
      {
         transitionId = UUID.randomUUID().toString();
      }
      transition.setId(transitionId);

      // We accept an empty name here

      transition.setName(extractString(controlFlowJson, ModelerConstants.NAME_PROPERTY));

      if (controlFlowJson.has(ModelerConstants.OTHERWISE_PROPERTY)
            && extractBoolean(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY))
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
      return transition;
   }

   /**
    *
    * @param connectionJson
    * @param processDefinition
    * @param sourceEventSymbol
    * @param targetActivitySymbol
    * @param maxOid
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, AbstractEventSymbol sourceEventSymbol,
         ActivitySymbolType targetActivitySymbol, long maxOid)
   {
      TransitionType transition = null;

      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(sourceEventSymbol);
      if (null != hostActivity)
      {
         transition = doCreateTransition(processDefinition, hostActivity,
               targetActivitySymbol.getActivity(), connectionJson, maxOid++ );
      }

      doCreateTransitionSymbol(processDefinition, sourceEventSymbol, targetActivitySymbol, transition, connectionJson, ++maxOid);
   }


   /**
    * creates association between model elements and Text Annotations TODO: currently
    * Modeler API is not available
    *
    * @param connectionJson
    * @param processDefinition
    * @param sourceSymbol
    * @param targetSymbol
    * @param maxOid
    */
   private void createAssociation(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, INodeSymbol sourceSymbol,
         INodeSymbol targetSymbol, long maxOid)
   {
      TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM.createTransitionConnectionType();
      transitionConnection.setElementOid(++maxOid);
      transitionConnection.setSourceNode(sourceSymbol);
      transitionConnection.setTargetNode(targetSymbol);
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
    * @param targetEventSymbol
    * @param maxOid
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition,
         ActivitySymbolType sourceActivitySymbol, AbstractEventSymbol targetEventSymbol,
         long maxOid)
   {
      TransitionType transition = null;

      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(targetEventSymbol);
      if (null != hostActivity)
      {
         transition = doCreateTransition(processDefinition,
               sourceActivitySymbol.getActivity(), hostActivity, connectionJson, maxOid++ );
      }

      doCreateTransitionSymbol(processDefinition, sourceActivitySymbol,
            targetEventSymbol, transition, connectionJson, ++maxOid);
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

      DataMappingType dataMapping = AbstractElementBuilder.F_CWM.createDataMappingType();
      DataMappingConnectionType dataMappingConnection = AbstractElementBuilder.F_CWM.createDataMappingConnectionType();

      dataMapping.setElementOid(++maxOid);
      dataMapping.setId(data.getId());
      dataMapping.setName(data.getName());
      dataMappingConnection.setElementOid(++maxOid);

      if (connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY).has(
            ModelerConstants.INPUT_DATA_MAPPING_PROPERTY))
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
