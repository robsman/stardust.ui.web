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
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
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
               JsonObject controlFlowJson = request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

               getModelBuilderFacade().createControlFlowConnection(processDefinition,
                     fromActivitySymbol,
                     getModelBuilderFacade().findActivitySymbol(diagram, toSymbolOid), extractString(controlFlowJson, ModelerConstants.ID_PROPERTY),
                     extractString(controlFlowJson, ModelerConstants.NAME_PROPERTY), extractString(controlFlowJson, ModelerConstants.DESCRIPTION_PROPERTY),
                     hasNotJsonNull(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY)
                     && extractBoolean(controlFlowJson, ModelerConstants.OTHERWISE_PROPERTY), "", mapAnchorOrientation(extractInt(request,
                           ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)), mapAnchorOrientation(extractInt(request,
                                 ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
            }
            else if (ModelerConstants.EVENT_KEY.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               StartEventSymbol startEventSymbol = getModelBuilderFacade().findStartEventSymbol(
                     diagram, toSymbolOid);
               if (null != startEventSymbol)
               {
                  // start events don't have incoming transitions, simply create an
                  // outgoing one
                  createControlFlowConnection(request, processDefinition,
                        startEventSymbol, fromActivitySymbol);
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
                        fromActivitySymbol, toEventSymbol);

               }
            }
            else if (ModelerConstants.DATA.equals(extractString(request,
                  ModelerConstants.TO_MODEL_ELEMENT_TYPE)))
            {
               getModelBuilderFacade().createDataFlowConnection(
                     processDefinition,
                     fromActivitySymbol,
                     getModelBuilderFacade().findDataSymbol(diagram, toSymbolOid),
                           hasNotJsonNull(request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                                 ModelerConstants.INPUT_DATA_MAPPING_PROPERTY)
                           ? DirectionType.IN_LITERAL
                           : DirectionType.OUT_LITERAL, "left", "right", PredefinedConstants.DEFAULT_CONTEXT, null);
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
                        getModelBuilderFacade().findActivitySymbol(diagram, toSymbolOid));
               }
               else
               {
                  EndEventSymbol endEventSymbol = getModelBuilderFacade().findEndEventSymbol(
                        diagram, fromSymbolOid);
                  if (null != endEventSymbol)
                  {
                     // end events don't have outgoing transitions, simply create an
                     // incoming one
                     createControlFlowConnection(
                           request,
                           processDefinition,
                           getModelBuilderFacade().findActivitySymbol(diagram,
                                 toSymbolOid), endEventSymbol);
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
               getModelBuilderFacade().createDataFlowConnection(
                     processDefinition,
                     getModelBuilderFacade().findActivitySymbol(diagram, toSymbolOid),
                     getModelBuilderFacade().findDataSymbol(diagram, fromSymbolOid),
                           hasNotJsonNull(request.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
                                 ModelerConstants.INPUT_DATA_MAPPING_PROPERTY)
                           ? DirectionType.IN_LITERAL
                           : DirectionType.OUT_LITERAL, "left", "right", PredefinedConstants.DEFAULT_CONTEXT, null);

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
   public void deleteConnection(ModelType model, IIdentifiableElement targetElement,
         JsonObject request)
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
    * @param connectionJson
    * @param processDefinition
    * @param sourceEventSymbol
    * @param targetActivitySymbol
    * @param maxOid
    */
   private void createControlFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, AbstractEventSymbol sourceEventSymbol,
         ActivitySymbolType targetActivitySymbol)
   {
      TransitionType transition = null;

      ActivityType hostActivity = EventMarshallingUtils.resolveHostActivity(sourceEventSymbol);

      if (null != hostActivity)
      {
         transition = getModelBuilderFacade().createTransition(
               processDefinition,
               hostActivity,
               targetActivitySymbol.getActivity(),
               extractString(connectionJson, ModelerConstants.ID_PROPERTY),
               extractString(connectionJson, ModelerConstants.NAME_PROPERTY),
               extractString(connectionJson, ModelerConstants.DESCRIPTION_PROPERTY),
               hasNotJsonNull(connectionJson, ModelerConstants.OTHERWISE_PROPERTY)
                     && extractBoolean(connectionJson,
                           ModelerConstants.OTHERWISE_PROPERTY), "");
      }

      getModelBuilderFacade().createTransitionSymbol(
            processDefinition,
            sourceEventSymbol,
            targetActivitySymbol,
            transition,
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
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
         ActivitySymbolType sourceActivitySymbol, AbstractEventSymbol targetEventSymbol)
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
      }

      getModelBuilderFacade().createTransitionSymbol(
            processDefinition,
            sourceActivitySymbol,
            targetEventSymbol,
            transition,
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)),
            mapAnchorOrientation(extractInt(connectionJson,
                  ModelerConstants.TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
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

}
