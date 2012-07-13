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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ICommandHandler;

import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 *
 */
public class SwimlaneCommandHandler implements ICommandHandler {

	@Override
	public boolean isValidTarget(Class<?> type) {
		return IIdentifiableElement.class.isAssignableFrom(type);
	}

	@Override
   public void handleCommand(String commandId, EObject targetElement, JsonObject request)
   {
		PoolSymbol parentSymbol = (PoolSymbol) targetElement;
		ModelType model = ModelUtils.findContainingModel(parentSymbol);
		ProcessDefinitionType processDefinition = ModelUtils
				.findContainingProcess(parentSymbol);
		if ("swimlaneSymbol.create".equals(commandId)) {
			createSwimlane(parentSymbol, model, processDefinition, request);
		} else if ("swimlaneSymbol.delete".equals(commandId)) {
			deleteSwimlane(parentSymbol, model, processDefinition, request);
		}
	}

	/**
	 *
	 * @param parentSymbol
	 * @param model
	 * @param processDefinition
	 * @param request
	 */
	private void createSwimlane(PoolSymbol parentSymbol, ModelType model,
			ProcessDefinitionType processDefinition, JsonObject request) {
		String laneId = extractString(request, ModelerConstants.ID_PROPERTY);
		String laneName = extractString(request, ModelerConstants.NAME_PROPERTY);
		int xPos = extractInt(request, X_PROPERTY);
		int yPos = extractInt(request, Y_PROPERTY);
		int width = extractInt(request, WIDTH_PROPERTY);
		int height = extractInt(request, HEIGHT_PROPERTY);
		String orientation = extractString(request,
				ModelerConstants.ORIENTATION_PROPERTY);
		String participantFullID = extractString(request,
				ModelerConstants.PARTICIPANT_FULL_ID);

		synchronized (model) {
			LaneSymbol laneSymbol = MBFacade.createLane(model.getId(), model,
					processDefinition, laneId, laneName, xPos, yPos, width,
					height, orientation, participantFullID);
			parentSymbol.getLanes().add(laneSymbol);

		}
	}

	/**
	 *
	 * @param parentSymbol
	 * @param model
	 * @param processDefinition
	 * @param request
	 */
	private void deleteSwimlane(PoolSymbol parentSymbol, ModelType model,
			ProcessDefinitionType processDefinition, JsonObject request) {
		String laneId = extractString(request, ModelerConstants.ID_PROPERTY);
		LaneSymbol lane = MBFacade.findLaneInProcess(processDefinition, laneId);

		synchronized (model) {
			parentSymbol.getLanes().remove(lane);
		}
	}

}
