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

import javax.annotation.Resource;

import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 */
@CommandHandler
public class SwimlaneCommandHandler
{
   @Resource
   private ApplicationContext springContext;
   private MBFacade facade;

   /**
    * @param parentSymbol
    * @param request
    */
   @OnCommand(commandId = "swimlaneSymbol.create")
   public void createSwimlane(PoolSymbol parentSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentSymbol);

      String laneId = extractString(request, ModelerConstants.ID_PROPERTY);
      String laneName = extractString(request, ModelerConstants.NAME_PROPERTY);
      int xPos = extractInt(request, X_PROPERTY);
      int yPos = extractInt(request, Y_PROPERTY);
      int width = extractInt(request, WIDTH_PROPERTY);
      int height = extractInt(request, HEIGHT_PROPERTY);
      String orientation = extractString(request, ModelerConstants.ORIENTATION_PROPERTY);
      String participantFullID = extractString(request,
            ModelerConstants.PARTICIPANT_FULL_ID);

      synchronized (model)
      {
         LaneSymbol laneSymbol = facade().createLane(model,
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
   public void deleteSwimlane(PoolSymbol parentSymbol, JsonObject request)
   {
      ModelType model = ModelUtils.findContainingModel(parentSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentSymbol);

      String laneId = extractString(request, ModelerConstants.ID_PROPERTY);
      LaneSymbol lane = facade().findLaneInProcess(processDefinition, laneId);

      synchronized (model)
      {
         parentSymbol.getLanes().remove(lane);
         parentSymbol.getChildLanes().remove(lane);
      }
   }
   
   private MBFacade facade()
   {
      if (facade == null)
      {
         facade = new MBFacade(springContext.getBean(ModelService.class)
               .getModelManagementStrategy());
      }
      return facade;
   }
}
