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

package org.eclipse.stardust.mobile.form;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.common.form.Form;
import org.eclipse.stardust.ui.common.form.FormGenerator;
import org.eclipse.stardust.ui.common.form.FormInput;
import org.eclipse.stardust.ui.common.form.PrimitiveInputController;
import org.eclipse.stardust.ui.common.introspection.java.JavaPath;
import org.eclipse.stardust.ui.common.introspection.xsd.XsdPath;

import com.infinity.bpm.rt.impl.api.ws.DataFlowUtils;

/**
 * Represents the form generated for a Manual Activity
 * 
 * @author Ellie.Sepehri
 * 
 */
public class ManualActivityForm extends Form {
	private WorkflowService workflowService;
	private ApplicationContext applicationContext;

	public ManualActivityForm(FormGenerator formGenerator,
			WorkflowService workflowService,
			ApplicationContext applicationContext) {
		super();
		this.workflowService = workflowService;
		this.applicationContext = applicationContext;

		generateForm(formGenerator);
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public WorkflowService getWorkflowService() {
		return workflowService;
	}

	public Model getModel() {
		// TODO Consider multi-version
		return getWorkflowService().getModel();
	}

	/**
	 * 
	 * @param activityInstance
	 */
	public void setData(ActivityInstance activityInstance) {
		for (Object object : getApplicationContext().getAllDataMappings()) {
			DataMapping dataMapping = (DataMapping) object;

			if (dataMapping.getDirection().equals(Direction.IN)
					|| dataMapping.getDirection().equals(Direction.IN_OUT)) {
				System.out.println("Set data for mapping "
						+ dataMapping.getId());
				setValue(
						dataMapping.getId(),
						getWorkflowService().getInDataValue(
								activityInstance.getOID(),
								getApplicationContext().getId(),
								dataMapping.getId()));
			}
		}
	}

	/**
	 * 
	 * @param activityInstance
	 * @return
	 */
	public Map<String, Object> retrieveData(ActivityInstance activityInstance) {
		Map<String, Object> map = new HashMap<String, Object>();

		for (Object object : getApplicationContext().getAllDataMappings()) {
			DataMapping dataMapping = (DataMapping) object;

			if (dataMapping.getDirection().equals(Direction.OUT)
					|| dataMapping.getDirection().equals(Direction.IN_OUT)) {
				map.put(dataMapping.getId(), getValue(dataMapping.getId()));
			}
		}

		return map;
	}

	/*
	 * Generates the top level panel for all Data Mappings of the Activity.
	 */
	public void generateForm(FormGenerator formGenerator) {
		setRootContainer(formGenerator.createRootComponent());

		Map<String, DataMapping> dataMappingMap = new HashMap<String, DataMapping>();

		for (Object object : getApplicationContext().getAllDataMappings()) {
			DataMapping dataMapping = (DataMapping) object;
			Data data = getModel().getData(dataMapping.getDataId());

			System.out.println("Data Mapping: " + dataMapping.getId());

			if (dataMappingMap.containsKey(dataMapping.getId())) {
				continue;
			}

			dataMappingMap.put(dataMapping.getId(), dataMapping);

			// TODO Lists, Enumerations, Java Classes
			if (ModelUtils.isStructuredType(getModel(), dataMapping)) {
				Set<TypedXPath> xpaths = DataFlowUtils.getXPaths(getModel(),
						dataMapping);

				System.out.println("Create structured for "
						+ dataMapping.getId());

				for (TypedXPath path : xpaths) {
					if (path.getParentXPath() == null) {
						getTopLevelInputControllerMap().put(
								dataMapping.getId(),
								formGenerator.generateStructurePanel(
										getFullPathInputControllerMap(),
										getRootContainer(), new XsdPath(
												null, path,
												false/*
													 * dataMapping
													 * .getDirection().equals(
													 * Direction.IN)
													 */)));
					}
				}
			} else if (ModelUtils.isPrimitiveType(getModel(), dataMapping)) {
				System.out.println("Create primitive input for "
						+ dataMapping.getId());

				PrimitiveInputController inputController = new PrimitiveInputController(JavaPath.createFromClass(
						dataMapping.getId(),
						dataMapping.getMappedType(), false/*
														 * dataMapping
														 * .getDirection
														 * ().equals(
														 * Direction.IN)
														 */));
				FormInput input = formGenerator.addPrimitiveInput(
						getRootContainer(), inputController);
				getRootContainer().getInputs().add(input);
				getTopLevelInputControllerMap().put(dataMapping.getId(),
						inputController);
			} else if (ModelUtils.isDMSType(getModel(), dataMapping)) {
				Data documentData = getModel().getData(dataMapping.getDataId());
				String metaDataTypeId = (String) documentData
						.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
				TypeDeclaration typeDeclaration = getModel()
						.getTypeDeclaration(metaDataTypeId);
				Set<TypedXPath> allXPaths = StructuredTypeRtUtils.getAllXPaths(
						getModel(), typeDeclaration);

				for (TypedXPath path : allXPaths) {
					if (path.getParentXPath() == null) {
						getTopLevelInputControllerMap().put(
								dataMapping.getId(),
								formGenerator.generateStructurePanel(
										getFullPathInputControllerMap(),
										getRootContainer(), new XsdPath(
												null, path,
												false/*
													 * dataMapping
													 * .getDirection().equals(
													 * Direction.IN)
													 */)));
					}
				}
			} else {
				getTopLevelInputControllerMap().put(
						dataMapping.getId(),
						formGenerator.generateStructurePanel(
								getFullPathInputControllerMap(),
								getRootContainer(), JavaPath.createFromClass(
										dataMapping.getMappedType().getName(),
										dataMapping.getMappedType(), false/*
																		 * dataMapping
																		 * .
																		 * getDirection
																		 * (
																		 * ).equals
																		 * (
																		 * Direction
																		 * .IN)
																		 */)));
			}
		}
	}
}
