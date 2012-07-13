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
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.EVENT_TYPE_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.HEIGHT_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.START_EVENT;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.WIDTH_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.X_PROPERTY;
import static org.eclipse.stardust.ui.web.modeler.service.ModelService.Y_PROPERTY;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;

import com.google.gson.JsonObject;

/**
 * 
 * @author Sidharth.Singh
 *
 */
public class EventCommandHandler implements ICommandHandler
{

   @Override
   public boolean isValidTarget(Class< ? > type)
   {
      return LaneSymbol.class.isAssignableFrom(type);
   }

   @Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
      if ("eventSymbol.create".equals(commandId))
      {
         createEvent(targetElement, request);
      }
      else if ("eventSymbol.delete".equals(commandId))
      {
         //TODO : add impl code
      }
   }

   private void createEvent(EObject targetElement, JsonObject request)
   {
      LaneSymbol parentLaneSymbol = (LaneSymbol) targetElement;
      ModelType model = ModelUtils.findContainingModel(parentLaneSymbol);
      ProcessDefinitionType processDefinition = ModelUtils.findContainingProcess(parentLaneSymbol);
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);

      if (START_EVENT.equals(extractString(request, ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY)))
      {
         StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();
         startEventSymbol.setElementOid(++maxOid);

         startEventSymbol.setXPos(extractInt(request, X_PROPERTY) - parentLaneSymbol.getXPos());
         startEventSymbol.setYPos(extractInt(request, Y_PROPERTY) - parentLaneSymbol.getYPos());
         startEventSymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
         startEventSymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));

         // TODO evaluate other properties

         processDefinition.getDiagram().get(0).getStartEventSymbols().add(startEventSymbol);
         parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);
      }
      else
      {
         EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();
         endEventSymbol.setElementOid(++maxOid);

         endEventSymbol.setXPos(extractInt(request, X_PROPERTY) - parentLaneSymbol.getXPos());
         endEventSymbol.setYPos(extractInt(request, Y_PROPERTY) - parentLaneSymbol.getYPos());
         endEventSymbol.setWidth(extractInt(request, WIDTH_PROPERTY));
         endEventSymbol.setHeight(extractInt(request, HEIGHT_PROPERTY));

         processDefinition.getDiagram().get(0).getEndEventSymbols().add(endEventSymbol);

         parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);
      }
   }

}
