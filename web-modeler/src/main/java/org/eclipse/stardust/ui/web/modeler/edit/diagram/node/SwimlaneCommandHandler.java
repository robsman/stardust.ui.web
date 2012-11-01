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

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import java.util.Iterator;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;

/**
 * @author Shrikant.Gangal
 */
@CommandHandler
public class SwimlaneCommandHandler
{
   @Resource
   private ApplicationContext springContext;

   /**
    * @param parentSymbol
    * @param request
    */
   @OnCommand(commandId = "swimlaneSymbol.create")
   public void createSwimlane(ModelType model, PoolSymbol parentSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentSymbol);

      String laneName = extractString(request, ModelerConstants.NAME_PROPERTY);
      String laneId = getModelBuilderFacade().createIdFromName(laneName);
      int xPos = extractInt(request, X_PROPERTY);
      int yPos = extractInt(request, Y_PROPERTY);
      int width = extractInt(request, WIDTH_PROPERTY);
      int height = extractInt(request, HEIGHT_PROPERTY);
      String orientation = extractString(request, ModelerConstants.ORIENTATION_PROPERTY);
      String participantFullID = extractString(request,
            ModelerConstants.PARTICIPANT_FULL_ID);

      synchronized (model)
      {
         LaneSymbol laneSymbol = getModelBuilderFacade().createLane(model,
               processDefinition, participantFullID, laneId, laneName, orientation, xPos, yPos,
               width, height);

         parentSymbol.getLanes().add(laneSymbol);

         PoolSymbol containingPool = parentSymbol;
         int poolWidth = containingPool.getWidth();
         containingPool.setWidth(poolWidth + width);
      }
   }

   /**
    * @param parentSymbol
    * @param request
    */
   @OnCommand(commandId = "swimlaneSymbol.delete")
   public void deleteSwimlane(ModelType model, PoolSymbol parentSymbol, JsonObject request)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentSymbol);

      String laneId = extractString(request, ModelerConstants.ID_PROPERTY);
      LaneSymbol lane = getModelBuilderFacade().findLaneInProcess(processDefinition, laneId);

      synchronized (model)
      {
         removeLaneAndItsChildElements(lane);
         parentSymbol.getLanes().remove(lane);
         parentSymbol.getChildLanes().remove(lane);
         // Update co-ordinates of adjacent Lanes
         updateAdjacentLanes(lane, parentSymbol);
      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }

   /**
    * @param laneSymbol
    */
   private void removeLaneAndItsChildElements(LaneSymbol laneSymbol)
   {
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(laneSymbol);
      for (LaneSymbol childLaneSymbol : laneSymbol.getChildLanes())
      {
         removeLaneAndItsChildElements(childLaneSymbol);
      }

      Iterator<ActivitySymbolType> actIter = laneSymbol.getActivitySymbol().iterator();
      while (actIter.hasNext())
      {
         ActivitySymbolType activitySymbol = actIter.next();
         ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, activitySymbol);
         ModelElementEditingUtils.deleteDataMappingConnection(processDefinition,
               activitySymbol.getDataMappings().iterator());
         processDefinition.getActivity().remove(activitySymbol.getModelElement());
         processDefinition.getDiagram().get(0).getActivitySymbol().remove(activitySymbol);
         actIter.remove();
      }

      Iterator<EndEventSymbol> endIter = laneSymbol.getEndEventSymbols().iterator();
      while (endIter.hasNext())
      {
         EndEventSymbol endSymbol = endIter.next();
         ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, endSymbol);
         processDefinition.getDiagram().get(0).getEndEventSymbols().remove(endSymbol);
         endIter.remove();
      }

      Iterator<StartEventSymbol> startIter = laneSymbol.getStartEventSymbols().iterator();
      while (startIter.hasNext())
      {
         StartEventSymbol startSymbol = startIter.next();
         ModelElementEditingUtils.deleteTransitionConnectionsForSymbol(processDefinition, startSymbol);
         processDefinition.getDiagram().get(0).getStartEventSymbols().remove(startSymbol);
         startIter.remove();
      }


      Iterator<DataSymbolType> dataSymIter = laneSymbol.getDataSymbol().iterator();
      while (dataSymIter.hasNext())
      {
         DataSymbolType dataSymbol = dataSymIter.next();
         processDefinition.getDiagram().get(0).getDataSymbol().remove(dataSymbol);
         dataSymIter.remove();
      }

//      Iterator<TransitionConnectionType> connIter = laneSymbol.getTransitionConnection().iterator();
//      while (connIter.hasNext())
//      {
//         TransitionConnectionType transitionConnection = connIter.next();
//         processDefinition.getDiagram()
//               .get(0)
//               .getPoolSymbols()
//               .get(0)
//               .getTransitionConnection()
//               .remove(transitionConnection);
//
//         if (transitionConnection.getTransition() != null)
//         {
//            processDefinition.getTransition()
//                  .remove(transitionConnection.getTransition());
//         }
//         connIter.remove();
//      }
   }

   /**
    *
    * @param currentLane
    * @param poolSymbol
    */
   private void updateAdjacentLanes(LaneSymbol currentLane, PoolSymbol poolSymbol)
   {
      for (LaneSymbol laneSymbol : poolSymbol.getLanes())
      {
         // For all lanes to the right of current lane, adjust 'X'
         if (laneSymbol.getElementOid() != currentLane.getElementOid()
               && (laneSymbol.getXPos() > currentLane.getXPos()))
         {
            long xOffset = laneSymbol.getXPos() - currentLane.getXPos();
            laneSymbol.setXPos(laneSymbol.getXPos() - xOffset);
            // TODO - Implement for horizontal orientation
         }
      }
   }

}
