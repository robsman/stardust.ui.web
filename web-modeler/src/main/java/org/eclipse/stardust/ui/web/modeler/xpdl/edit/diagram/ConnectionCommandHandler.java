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

package org.eclipse.stardust.ui.web.modeler.xpdl.edit.diagram;

import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findIdentifiableElement;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMap;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XPDLFinderUtils;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.xpdl.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.EventMarshallingUtils;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
            ActivitySymbolType fromActivitySymbol = XPDLFinderUtils.findActivitySymbol(
                  diagram, fromSymbolOid);

            ActivitySymbolType toActivitySymbol = XPDLFinderUtils.findActivitySymbol(
                  diagram, toSymbolOid);


            if (ModelerConstants.ACTIVITY_KEY.equals(targetType)
                  || ModelerConstants.GATEWAY.equals(targetType))
            {
               JsonObject controlFlowJson = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

               TransitionType transition = createTransition(controlFlowJson, processDefinition,
                     fromActivitySymbol.getActivity(), toActivitySymbol.getActivity());

               TransitionConnectionType transitionConnectionType = getModelBuilderFacade()
                     .createTransitionSymbol(
                           processDefinition,
                           fromActivitySymbol,
                           toActivitySymbol,
                           transition,
                           mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
                           mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

               mapper.map(transitionConnectionType);
            }
            else if (ModelerConstants.EVENT_KEY.equals(targetType))
            {
               StartEventSymbol startEventSymbol = XPDLFinderUtils.findStartEventSymbol(
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
                  AbstractEventSymbol toEventSymbol = XPDLFinderUtils.findEndEventSymbol(
                        diagram, toSymbolOid);
                  if (null == toEventSymbol)
                  {
                     toEventSymbol = XPDLFinderUtils.findIntermediateEventSymbol(
                           diagram, toSymbolOid);
                  }
                  createControlFlowConnection(request, processDefinition,
                        fromActivitySymbol, toEventSymbol, mapper);

               }
            }
            else if (ModelerConstants.DATA.equals(targetType))
            {
               /*JsonObject controlFlowJson = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
               JsonArray dataMappingsJson = controlFlowJson.getAsJsonArray(ModelerConstants.DATAMAPPINGS_PROPERTY);
               String direction = dataMappingsJson.get(0).getAsJsonObject().get(ModelerConstants.DIRECTION_PROPERTY).getAsString();*/

               String direction = getDirection(request);

               DataMappingConnectionType dataConnectionType = getModelBuilderFacade().createDataFlowConnection(
                     processDefinition,
                     fromActivitySymbol,
                     XPDLFinderUtils.findDataSymbol(diagram, toSymbolOid),
                     direction.equals(ModelerConstants.DATAMAPPING_IN)
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
               AbstractEventSymbol fromEventSymbol = XPDLFinderUtils.findStartEventSymbol(diagram, fromSymbolOid);
               if (null == fromEventSymbol)
               {
                  fromEventSymbol = XPDLFinderUtils.findIntermediateEventSymbol(diagram, fromSymbolOid);
               }
               if (null != fromEventSymbol)
               {
                  createControlFlowConnection(request, processDefinition,
                        fromEventSymbol,
                        XPDLFinderUtils.findActivitySymbol(diagram, toSymbolOid), mapper);
               }
               else
               {
                  EndEventSymbol endEventSymbol = XPDLFinderUtils.findEndEventSymbol(
                        diagram, fromSymbolOid);
                  if (null != endEventSymbol)
                  {
                     // end events don't have outgoing transitions, simply create an
                     // incoming one
                     createControlFlowConnection(
                           request,
                           processDefinition,
                           XPDLFinderUtils.findActivitySymbol(diagram,
                                 toSymbolOid), endEventSymbol, mapper);
                  }
               }
            }
            else if (ModelerConstants.EVENT_KEY.equals(targetType))
            {
               AbstractEventSymbol fromEventSymbol = XPDLFinderUtils.findStartEventSymbol(
                     diagram, fromSymbolOid);

               AbstractEventSymbol toEventSymbol = XPDLFinderUtils.findIntermediateEventSymbol(
                     diagram, toSymbolOid);

               if (null == fromEventSymbol)
               {
                  fromEventSymbol = XPDLFinderUtils.findIntermediateEventSymbol(
                        diagram, fromSymbolOid);

                  //Intermediate event can connect to End event directly
                  if (null == toEventSymbol && null != fromEventSymbol)
                  {
                     toEventSymbol = XPDLFinderUtils.findEndEventSymbol(diagram,
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

               String direction = getDirection(request);

               DataMappingConnectionType dataConnectionType = getModelBuilderFacade().createDataFlowConnection(
                     processDefinition,
                     XPDLFinderUtils.findActivitySymbol(diagram, toSymbolOid),
                     XPDLFinderUtils.findDataSymbol(diagram, fromSymbolOid),
                           direction.equals(ModelerConstants.DATAMAPPING_IN)
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


   private String getDirection(JsonObject request)
   {
      JsonObject controlFlowJson = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
      JsonArray dataMappingsJson = controlFlowJson.getAsJsonArray(ModelerConstants.DATAMAPPINGS_PROPERTY);
      String direction = null;
      if (dataMappingsJson != null)
      {
         direction = dataMappingsJson.get(0).getAsJsonObject()
               .get(ModelerConstants.DIRECTION_PROPERTY).getAsString();
      }
      else
      {
         direction = controlFlowJson.get(ModelerConstants.DIRECTION_PROPERTY)
               .getAsString();
      }
      return direction;
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
         nodeSymbol = XPDLFinderUtils.findActivitySymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.GATEWAY.equals(typeInRequest))
      {
         nodeSymbol = XPDLFinderUtils.findActivitySymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.EVENT_KEY.equals(typeInRequest))
      {
         nodeSymbol = XPDLFinderUtils.findStartEventSymbol(diagram, oidInRequest);
         if (null == nodeSymbol)
         {
            nodeSymbol = XPDLFinderUtils.findEndEventSymbol(diagram, oidInRequest);
         }
         if (null == nodeSymbol)
         {
            nodeSymbol = XPDLFinderUtils.findIntermediateEventSymbol(diagram, oidInRequest);
         }
      }
      else if (ModelerConstants.DATA.equals(typeInRequest))
      {
         nodeSymbol = XPDLFinderUtils.findDataSymbol(diagram, oidInRequest);
      }
      else if (ModelerConstants.ANNOTATION_SYMBOL.equals(typeInRequest))
      {
         nodeSymbol = XPDLFinderUtils.findAnnotationSymbol(diagram, oidInRequest);
      }
      return nodeSymbol;
   }

   @OnCommand(commandId = "connection.delete")
   public void deleteConnection(ModelType model, IIdentifiableElement targetElement,
         JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils
            .findContainingProcess(targetElement);

      Long connectionOid = extractLong(request, ModelerConstants.OID_PROPERTY);
      synchronized (model)
      {
         DiagramType defaultDiagram = processDefinition.getDiagram().get(0);
         PoolSymbol defaultPool = defaultDiagram.getPoolSymbols().get(0);

         TransitionConnectionType transitionConnection = XPDLFinderUtils
               .findTransitionConnectionByModelOid(processDefinition, connectionOid);
         if (transitionConnection != null)
         {
            defaultPool.getTransitionConnection().remove(transitionConnection);
            transitionConnection.getSourceActivitySymbol().getOutTransitions()
                  .remove(transitionConnection);
            transitionConnection.getTargetActivitySymbol().getInTransitions()
                  .remove(transitionConnection);
            if (transitionConnection.getTransition() != null)
            {
               TransitionType transitionType = transitionConnection.getTransition();
               processDefinition.getTransition().remove(transitionType);
               transitionType.getFrom().getOutTransitions().remove(transitionType);
               transitionType.getTo().getInTransitions().remove(transitionType);
            }
         }
         else
         {
            DataMappingConnectionType dataMappingConnection = XPDLFinderUtils
                  .findDataMappingConnectionByModelOid(processDefinition, connectionOid);
            List<DataMappingType> dataMapping = CollectionUtils.newArrayList();
            for (DataMappingType dataMappingType : dataMappingConnection
                  .getActivitySymbol().getActivity().getDataMapping())
            {
               if (dataMappingType.getData().getId()
                     .equals(dataMappingConnection.getDataSymbol().getData().getId()))
               {
                  dataMapping.add(dataMappingType);
               }
            }
            dataMappingConnection.getActivitySymbol().getActivity().getDataMapping()
                  .removeAll(dataMapping);
            dataMappingConnection.getDataSymbol().getData().getDataMappings()
                  .removeAll(dataMapping);
            defaultPool.getDataMappingConnection().remove(dataMappingConnection);
         }
      }

   }


   @OnCommand(commandId = "datamapping.create")
   public void createDatamapping(ModelType model, DataMappingConnectionType dataFlowConnection,
         JsonObject request)
   {
      DataMappingType dataMapping = createDataMapping(dataFlowConnection, request);
      modelService().uuidMapper().map(dataMapping);
   }

   @OnCommand(commandId = "datamapping.delete")
   public void deleteDatamapping(ModelType model,
         DataMappingConnectionType dataFlowConnection, JsonObject request)
   {
      if (request.has(ModelerConstants.UUID_PROPERTY))
      {
         JsonArray uuidArray = request.get(ModelerConstants.UUID_PROPERTY)
               .getAsJsonArray();
         if (uuidArray != null)
         {
            for (Iterator<JsonElement> i = uuidArray.iterator(); i.hasNext();)
            {
               JsonElement element = i.next();
               String uuid = element.getAsString();
               DataMappingType dataMapping = (DataMappingType) modelService()
                     .uuidMapper().getEObject(uuid);
               ActivityType activity = ModelUtils.findContainingActivity(dataMapping);
               activity.getDataMapping().remove(dataMapping);
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

         JsonObject controlFlowJson = connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
         transition = createTransition(controlFlowJson, processDefinition, hostActivity,
               targetActivitySymbol.getActivity());

         if(!StringUtils.isEmpty(condition))
         {
            transition.setCondition(ModelerConstants.CONDITION_KEY);
            if(transition.getExpression() == null)
            {
               XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();
               transition.setExpression(expression);
            }
            FeatureMap mixedNode = transition.getExpression().getMixed();
            ModelUtils.setCDataString(mixedNode, condition, true);
         }

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
         JsonObject controlFlowJson = connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

         transition = createTransition(controlFlowJson, processDefinition, sourceActivitySymbol.getActivity(),
               hostActivity);

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
         JsonObject controlFlowJson = connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

         transition = createTransition(controlFlowJson, processDefinition, sourceHostActivity, targetHostActivity);
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
    * @param controlFlowJson
    * @param processDefinition
    * @param sourceActivity
    * @param targetActivity
    * @return
    */
   private TransitionType createTransition(JsonObject controlFlowJson, ProcessDefinitionType processDefinition,
         ActivityType sourceActivity, ActivityType targetActivity)
   {
      TransitionType transition = getModelBuilderFacade().createTransition(
            processDefinition,
            sourceActivity,
            targetActivity,
            extractString(controlFlowJson, ModelerConstants.ID_PROPERTY),
            extractString(controlFlowJson, ModelerConstants.NAME_PROPERTY),
            extractString(controlFlowJson, ModelerConstants.DESCRIPTION_PROPERTY),
            hasNotJsonNull(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY)
                  && extractBoolean(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY),
            extractString(controlFlowJson, ModelerConstants.CONDITION_EXPRESSION_PROPERTY));

      transition.setForkOnTraversal(hasNotJsonNull(controlFlowJson, ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY)
            && extractBoolean(controlFlowJson, ModelerConstants.FORK_ON_TRAVERSAL_PROPERTY));

      // update comments
      storeAttributes(transition, controlFlowJson);

      return transition;
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
  private DataMappingType createDataMapping(DataMappingConnectionType dataFlowConnection, JsonObject dataMappingJson)
  {
     DataMappingType dataMapping = AbstractElementBuilder.F_CWM.createDataMappingType();
     DataType data = dataFlowConnection.getDataSymbol().getData();
     ActivityType activity = dataFlowConnection.getActivitySymbol().getActivity();

     if (hasNotJsonNull(dataMappingJson, ModelerConstants.ID_PROPERTY))
     {
        dataMapping.setId(dataMappingJson.get(ModelerConstants.ID_PROPERTY)
              .getAsString());
     }
     else
     {
           dataMapping.setId(data.getId());
           dataMapping.setName(data.getId());

     }

     if (hasNotJsonNull(dataMappingJson, ModelerConstants.NAME_PROPERTY))
     {
        dataMapping.setName(dataMappingJson.get(ModelerConstants.NAME_PROPERTY)
              .getAsString());
     }
     else
     {
        dataMapping.setId(data.getId());
        dataMapping.setName(data.getId());
     }

     String direction = dataMappingJson.get(ModelerConstants.DIRECTION_PROPERTY).getAsString();
     DirectionType directionType = direction.equals(ModelerConstants.DATAMAPPING_IN) ? DirectionType.IN_LITERAL : DirectionType.OUT_LITERAL;

     dataMapping.setDirection(directionType);

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
        dataMapping.setContext(getModelBuilderFacade().getDefaultContext(activity,
              directionType));
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
    * @param element
    * @param json
    */
   private void storeAttributes(EObject element, JsonObject json)
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