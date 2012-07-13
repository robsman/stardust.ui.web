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

import java.util.UUID;

import org.eclipse.stardust.ui.common.form.AbstractFormContainer;
import org.eclipse.stardust.ui.common.form.Indent;
import org.eclipse.stardust.ui.common.introspection.Path;

/**
 * 
 * @author Ellie.Sepehri
 *
 */
public class AjaxStructureContainer extends AbstractFormContainer {
	protected AjaxStructureContainer(Path path) {
//		rootGrid = new HtmlPanelGrid();
//
//		rootGrid.setId(JsfFormGenerator.PREFIX + "grid-" + UUID.randomUUID());
//
//		rootGrid.setCellpadding("0");
//		rootGrid.setCellspacing("0");
//
//		rootGrid.setColumns(1);
//		rootGrid.getChildren().clear();
//
//		if (parentGrid != null && path != null) {
//			parentGrid.getChildren().add(rootGrid);
//
//			rootGrid.setStyleClass("form-panel");
//			rootGrid.setRowClasses("form-panel-item");
//
//			HtmlPanelGroup headingGroup = new HtmlPanelGroup();
//
//			rootGrid.getChildren().add(headingGroup);
//			headingGroup.setStyleClass("form-panel-heading");
//
//			HtmlOutputText headingText = new HtmlOutputText();
//
//			headingGroup.getChildren().add(headingText);
//			headingText.setValue(Messages.getInstance().get(
//			      StringUtils.isNotEmpty(path.getLabelPath()) ? path.getLabelPath() : "Anonymous"));
//			headingText.setStyleClass("form-panel-heading");
//
//			if (new Boolean(path.getProperty(InputPreferences.class
//					.getSimpleName() + "_showDescription"))) {
//				HtmlPanelGroup descriptionGroup = new HtmlPanelGroup();
//
//				rootGrid.getChildren().add(descriptionGroup);
//				descriptionGroup.setStyleClass("form-panel-description");
//
//				HtmlOutputText descriptionText = new HtmlOutputText();
//
//				descriptionGroup.getChildren().add(descriptionText);
//				descriptionText.setValue(Messages.getInstance().get(
//						path.getLabelPath() + "._description"));
//				descriptionText.setStyleClass("form-panel-description");
//			}
//		}	
//
//		primitiveGridContainer = new HtmlPanelGrid();
//
//		rootGrid.getChildren().add(primitiveGridContainer);
//		primitiveGridContainer.setId(JsfFormGenerator.PREFIX + "grid-"
//				+ UUID.randomUUID());
//
//		primitiveGridContainer.setCellpadding("0");
//		primitiveGridContainer.setCellspacing("0");
//		primitiveGridContainer.setColumns(10);
//		primitiveGridContainer.setColumnClasses("form-panel-label-field-column");
//		primitiveGridContainer.setRowClasses("form-panel-label-field-row");
//		primitiveGridContainer.getChildren().clear();
//
//		swapPrimitiveGrid();
	}

	public String generateMarkupCode(Indent indent) {
		StringBuffer buffer = new StringBuffer();

//		buffer.append(indent);
//		buffer.append("<ice:panelGrid id=\"");
//		buffer.append(getRootGrid().getId());
//		buffer.append("\" columns=\"");
//		buffer.append(getRootGrid().getColumns());
//		buffer.append("\" cellpadding=\"");
//		buffer.append(getRootGrid().getCellpadding());
//		buffer.append("\" cellspacing=\"");
//		buffer.append(getRootGrid().getCellpadding());
//		buffer.append("\" styleClass=\"");
//		buffer.append(getRootGrid().getStyleClass());
//		buffer.append("\" rowClass=\"");
//		buffer.append(getRootGrid().getRowClasses());
//		buffer.append("\">\n");
//
//		indent.increment();
//
//		buffer.append(indent);
//		buffer.append("<ice:panelGrid id=\"");
//		buffer.append(getPrimitiveGrid().getId());
//		buffer.append("\" columns=\"");
//		buffer.append(getPrimitiveGrid().getColumns());
//		buffer.append("\" cellpadding=\"");
//		buffer.append(getPrimitiveGrid().getCellpadding());
//		buffer.append("\" cellspacing=\"");
//		buffer.append(getPrimitiveGrid().getCellpadding());
//		buffer.append("\" styleClass=\"");
//		buffer.append(getPrimitiveGrid().getStyleClass());
//		buffer.append("\" rowClass=\"");
//		buffer.append(getPrimitiveGrid().getRowClasses());
//		buffer.append("\">\n");
//
//		indent.increment();
//
//		for (FormInput input : getInputs()) {
//			buffer.append(((JsfFormInput) input).generateMarkupCode(indent));
//		}
//
//		indent.decrement();
//
//		buffer.append(indent);
//		buffer.append("</ice:panelGrid>\n");
//
//		indent.increment();
//
//		for (FormContainer childContainer : getChildContainers()) {
//			buffer.append(((JsfStructureContainer) childContainer)
//					.generateMarkupCode(indent));
//		}
//
//		indent.decrement();
//		indent.decrement();
//
//		buffer.append(indent);
//		buffer.append("</ice:panelGrid>\n");

		return buffer.toString();
	}
}
