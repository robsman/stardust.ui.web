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

import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findIdentifiableElement;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

/**
 * @author Sidharth.Singh
 */
@CommandHandler
public class ConnectionCommandHandler
{
   private static final JsonMarshaller jsonIo = new JsonMarshaller();

   @Resource
   private ApplicationContext springContext;

   @OnCommand(commandId = "connection.create")
   public void createConnection(ModelType model, IIdentifiableElement targetElement,
         JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(targetElement);
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      synchronized (model)
      {
         DiagramType diagram = processDefinition.getDiagram().get(0);

         long fromSymbolOid = extractLong(request, ModelerConstants.FROM_MODEL_ELEMENT_OID);
         long toSymbolOid = extractLong(request, ModelerConstants.TO_MODEL_ELEMENT_OID);

         String sourceType = extractString(request, ModelerConstants.FROM_MODEL_ELEMENT_TYPE);
         String targetType = extractString(request, ModelerConstants.TO_MODEL_ELEMENT_TYPE);
         if (ModelerConstants.ACTIVITY_KEY.equals(sourceType)
               || ModelerConstants.GATEWAY.equals(sourceType))
         {
            ActivitySymbolType fromActivitySymbol = ModelBuilderFacade.findActivitySymbol(
                  diagram, fromSymbolOid);

            if (ModelerConstants.ACTIVITY_KEY.equals(targetType)
                  || ModelerConstants.GATEWAY.equals(targetType))
            {
               JsonObject controlFlowJson = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

               TransitionConnectionType transitionConnectionType = getModelBuilderFacade().createControlFlowConnection(
                     processDefinition, fromActivitySymbol,
                     ModelBuilderFacade.findActivitySymbol(diagram, toSymbolOid),
                     extractString(controlFlowJson, ModelerConstants.ID_PROPERTY),
                     extractString(controlFlowJson, ModelerConstants.NAME_PROPERTY),
                     extractString(controlFlowJson, ModelerConstants.DESCRIPTION_PROPERTY),
                     hasNotJsonNull(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY)
                        && extractBoolean(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY),
                     "",
                     mapAnchorOrientation(extractInt(request, ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
                     mapAnchorOrientation(extractInt(request, ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
               mapper.map(transitionConnectionType);
            }
            else if (ModelerConstants.EVENT_KEY.equals(targetType))
            {
               StartEventSymbol startEventSymbol = ModelBuilderFacade.findStartEventSymbol(
                     diagram, toSymbolOid);
               if (null != startEventSymbol)
               {
                  // start events don't have incoming transitions, simply create an
                  // outgoing one
                  createControlFlowConnection(request, processDefinition,
                        startEventSymbol, fromActivitySymbol, mapper);
               }
               else
               {
                  AbstractEventSymbol toEventSymbol = ModelBuilderFacade.findEndEventSymbol(
                        diagram, toSymbolOid);
                  if (null == toEventSymbol)
                  {
                     toEventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(
                           diagram, toSymbolOid);
                  }
                  createControlFlowConnection(request, processDefinition,
                        fromActivitySymbol, toEventSymbol, mapper);

               }
            }
            else if (ModelerConstants.DATA.equals(targetType))
            {
               DataMappingConnectionType dataConnectionType = getModelBuilderFacade().createDataFlowConnection(
                     processDefinition,
                     fromActivitySymbol,
                     getModelBuilderFacade().findDataSymbol(diagram, toSymbolOid),
                           hasNotJsonNull(request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                                 ModelerConstants.INPUT_DATA_MAPPING_PROPERTY)
                           ? DirectionType.IN_LITERAL
                           : DirectionType.OUT_LITERAL,
                           mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
                           mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)),
                           PredefinedConstants.DEFAULT_CONTEXT, null);
               mapper.map(dataConnectionType);
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + targetType
                     + " for connection.");
            }
         }
         else if (ModelerConstants.EVENT_KEY.equals(sourceType))
         {
            if (ModelerConstants.ACTIVITY_KEY.equals(targetType))
            {
               AbstractEventSymbol fromEventSymbol = ModelBuilderFacade.findStartEventSymbol(diagram, fromSymbolOid);
               if (null == fromEventSymbol)
               {
                  fromEventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(diagram, fromSymbolOid);
               }
               if (null != fromEventSymbol)
               {
                  createControlFlowConnection(request, processDefinition,
                        fromEventSymbol,
                        ModelBuilderFacade.findActivitySymbol(diagram, toSymbolOid), mapper);
               }
               else
               {
                  EndEventSymbol endEventSymbol = ModelBuilderFacade.findEndEventSymbol(
                        diagram, fromSymbolOid);
                  if (null != endEventSymbol)
                  {
                     // end events don't have outgoing transitions, simply create an
                     // incoming one
                     createControlFlowConnection(
                           request,
                           processDefinition,
                           ModelBuilderFacade.findActivitySymbol(diagram,
                                 toSymbolOid), endEventSymbol, mapper);
                  }
               }
            }
            else if (ModelerConstants.EVENT_KEY.equals(targetType))
            {
               AbstractEventSymbol fromEventSymbol = ModelBuilderFacade.findStartEventSymbol(
                     diagram, fromSymbolOid);

               AbstractEventSymbol toEventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(
                     diagram, toSymbolOid);

               if (null == fromEventSymbol)
               {
                  fromEventSymbol = ModelBuilderFacade.findIntermediateEventSymbol(
                        diagram, fromSymbolOid);

                  //Intermediate event can connect to End event directly
                  if (null == toEventSymbol && null != fromEventSymbol)
                  {
                     toEventSymbol = ModelBuilderFacade.findEndEventSymbol(diagram,
                           toSymbolOid);
                  }
               }

               if (null != fromEventSymbol && null != toEventSymbol)
               {
                  createControlFlowConnection(request, processDefinition,
                        fromEventSymbol, toEventSymbol, mapper);
               }
               else
               {
                  throw new IllegalArgumentException("invalid source and/or target symbol type. "
                        + "target type: " + targetType + " source type: " + sourceType);
               }
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + targetType
                     + " for connection.");
            }
         }
         else if (ModelerConstants.DATA.equals(sourceType))
         {
            if (ModelerConstants.ACTIVITY_KEY.equals(targetType))
            {
               DataMappingConnectionType dataConnectionType = getModelBuilderFacade().createDataFlowConnection(
                     processDefinition,
                     ModelBuilderFacade.findActivitySymbol(diagram, toSymbolOid),
                     getModelBuilderFacade().findDataSymbol(diagram, fromSymbolOid),
                           hasNotJsonNull(request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                                 ModelerConstants.INPUT_DATA_MAPPING_PROPERTY)
                           ? DirectionType.IN_LITERAL
                           : DirectionType.OUT_LITERAL,
                           mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
                           mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)),
                           PredefinedConstants.DEFAULT_CONTEXT, null);
               
               mapper.map(dataConnectionType);
            }
            else
            {
               throw new IllegalArgumentException("Unknown target symbol type "
                     + targetType
                     + " for connection.");
            }
         }
         else if (ModelerConstants.ANNOTATION_SYMBOL.equals(targetType)
               || ModelerConstants.ANNOTATION_SYMBOL.equals(sourceType))
         {
            String typeInRequest = sourceType;
            Long oid = fromSymbolOid;

            INodeSymbol sourceSymbol = getNodeSymbol(request, diagram, typeInRequest, oid);

            typeInRequest = targetType;
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
                  + sourceType
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
      // TODO: refactor, makes no sense (fh)

      INodeSymbol nodeSymbol = null;

      if (ModelerConstants.ACTIVITY_KEY.equals(typeInRequest))
      {
         nodeSymbol = ModelBuilderFacade.findActivitySymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.GATEWAY.equals(typeInRequest))
      {
         nodeSymbol = ModelBuilderFacade.findActivitySymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.EVENT_KEY.equals(typeInRequest))
      {
         nodeSymbol = ModelBuilderFacade.findStartEventSymbol(diagram, oidInRequest);
         if (null == nodeSymbol)
         {
            nodeSymbol = ModelBuilderFacade.findEndEventSymbol(diagram, oidInRequest);
         }
         if (null == nodeSymbol)
         {
            nodeSymbol = ModelBuilderFacade.findIntermediateEventSymbol(diagram, oidInRequest);
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
   public void deleteConnection(ModelType model, IIdentifiableElement targetElement,
         JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(targetElement);

      Long connectionOid = extractLong(request, ModelerConstants.OID_PROPERTY);
      synchronized (model)
      {
         DiagramType defaultDiagram = processDefinition.getDiagram().get(0);
         PoolSymbol defaultPool = defaultDiagram.getPoolSymbols().get(0);
         try
         {
            TransitionConnectionType transitionConnection = ModelBuilderFacade.findTransitionConnectionByModelOid(
                  processDefinition, connectionOid);

            defaultPool
                  .getTransitionConnection()
                  .remove(transitionConnection);

            if (transitionConnection.getTransition() != null)
            {
               TransitionType transitionType = transitionConnection.getTransition();
               processDefinition.getTransition().remove(transitionType);
               transitionType.getFrom().getOutTransitions().remove(transitionType);
               transitionType.getTo().getInTransitions().remove(transitionType);
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
               defaultPool
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
    * @param connectionJson
    * @param processDefinition
    * @param sourceEventSymbol
    * @param targetActivitySymbol
    * @param maxOid
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, AbstractEventSymbol sourceEventSymbol,
         ActivitySymbolType targetActivitySymbol, EObjectUUIDMapper mapper)
   {
      TransitionType transition = null;
      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(sourceEventSymbol);

      if (hostActivity != null)
      {
         String condition = "";
         if (sourceEventSymbol instanceof IntermediateEventSymbol &&
               !EventMarshallingUtils.isIntermediateEventHost(hostActivity))
         {
            JsonObject hostingConfig = EventMarshallingUtils.getEventHostingConfig(
                  hostActivity, (IntermediateEventSymbol) sourceEventSymbol, jsonIo);
            if (hostingConfig != null && hasNotJsonNull(hostingConfig, EventMarshallingUtils.PRP_EVENT_HANDLER_ID))
            {
               String eventHandlerId = extractAsString(hostingConfig, EventMarshallingUtils.PRP_EVENT_HANDLER_ID);
               EventHandlerType eventHandler = findIdentifiableElement(hostActivity.getEventHandler(), eventHandlerId);
               if (eventHandler != null)
               {
                  condition = "ON_BOUNDARY_EVENT(" + eventHandlerId + ")";
               }
            }
         }
         transition = getModelBuilderFacade().createTransition(
               processDefinition,
               hostActivity,
               targetActivitySymbol.getActivity(),
               extractString(connectionJson, ModelerConstants.ID_PROPERTY),
               extractString(connectionJson, ModelerConstants.NAME_PROPERTY),
               extractString(connectionJson, ModelerConstants.DESCRIPTION_PROPERTY),
               hasNotJsonNull(connectionJson, ModelerConstants.OTHERWISE_PROPERTY)
                     && extractBoolean(connectionJson,
                           ModelerConstants.OTHERWISE_PROPERTY),
               condition);
         mapper.map(transition);
      }

      TransitionConnectionType transitionConnectionType = getModelBuilderFacade().createTransitionSymbol(
            processDefinition,
            sourceEventSymbol,
            targetActivitySymbol,
            transition,
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      
      mapper.map(transitionConnectionType);
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
         ActivitySymbolType sourceActivitySymbol, AbstractEventSymbol targetEventSymbol, EObjectUUIDMapper mapper)
   {
      TransitionType transition = null;
      
      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(targetEventSymbol);

      if (null != hostActivity)
      {
         transition = getModelBuilderFacade().createTransition(
               processDefinition,
               sourceActivitySymbol.getActivity(),
               hostActivity,
               extractString(connectionJson, ModelerConstants.ID_PROPERTY),
               extractString(connectionJson, ModelerConstants.NAME_PROPERTY),
               extractString(connectionJson, ModelerConstants.DESCRIPTION_PROPERTY),
               hasNotJsonNull(connectionJson, ModelerConstants.OTHERWISE_PROPERTY)
                     && extractBoolean(connectionJson,
                           ModelerConstants.OTHERWISE_PROPERTY), "");
         mapper.map(transition);
      }

      TransitionConnectionType transitionConnectionType = getModelBuilderFacade().createTransitionSymbol(
            processDefinition,
            sourceActivitySymbol,
            targetEventSymbol,
            transition,
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      
      mapper.map(transitionConnectionType);
   }

   /**
    * @param connectionJson
    * @param processDefinition
    * @param sourceEventSymbol
    * @param targetEventSymbol
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, AbstractEventSymbol sourceEventSymbol,
         AbstractEventSymbol targetEventSymbol, EObjectUUIDMapper mapper)
   {
      TransitionType transition = null;
      
      ActivityType targetHostActivity = EventMarshallingUtils.resolveHostActivity(targetEventSymbol);
      ActivityType sourceHostActivity = EventMarshallingUtils.resolveHostActivity(sourceEventSymbol);

      if (null != targetHostActivity && null != sourceHostActivity)
      {
         transition = getModelBuilderFacade().createTransition(
               processDefinition,
               sourceHostActivity,
               targetHostActivity,
               extractString(connectionJson, ModelerConstants.ID_PROPERTY),
               extractString(connectionJson, ModelerConstants.NAME_PROPERTY),
               extractString(connectionJson, ModelerConstants.DESCRIPTION_PROPERTY),
               hasNotJsonNull(connectionJson, ModelerConstants.OTHERWISE_PROPERTY)
                     && extractBoolean(connectionJson,
                           ModelerConstants.OTHERWISE_PROPERTY), "");
         mapper.map(transition);

         TransitionConnectionType transitionConnectionType = getModelBuilderFacade().createTransitionSymbol(
               processDefinition,
               sourceEventSymbol,
               targetEventSymbol,
               transition,
               mapAnchorOrientation(extractInt(connectionJson,
                     ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
               mapAnchorOrientation(extractInt(connectionJson,
                     ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
         mapper.map(transitionConnectionType);
      }
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

   /**
    *
    * @return
    */
   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }
   
}
