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

import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.common.form.FormContainer;
import org.eclipse.stardust.ui.common.form.FormGenerator;
import org.eclipse.stardust.ui.common.form.FormInput;
import org.eclipse.stardust.ui.common.form.InputController;
import org.eclipse.stardust.ui.common.form.PrimitiveInputController;
import org.eclipse.stardust.ui.common.form.preferences.FormGenerationPreferences;
import org.eclipse.stardust.ui.common.introspection.Path;

/**
 * 
 * @author Ellie.Sepehri
 *
 */
public class AjaxFormGenerator extends FormGenerator {
	public static final String PREFIX = "stardust-form-";

	public AjaxFormGenerator(FormGenerationPreferences generationPreferences) {
		super(generationPreferences);
	}

	/**
	 * 
	 */
	public FormContainer createRootComponent() {
		AjaxStructureContainer rootContainer = new AjaxStructureContainer(null);

		return rootContainer;
	}

	/**
	 * 
	 */
	public void addListComponent(FormContainer parentComponent, Path path) {
//		UIComponent parentContainer = ((JsfStructureContainer) parentComponent)
//				.getRootGrid();
//		HtmlPanelGrid grid = new HtmlPanelGrid();
//
//		parentContainer.getChildren().add(grid);
//		grid.setId(JsfFormGenerator.PREFIX + "grid-" + UUID.randomUUID());
//
//		grid.setCellpadding("0");
//		grid.setCellspacing("0");
//		grid.setStyleClass("form-panel");
//		grid.setRowClasses("form-panel-item");
//
//		grid.setColumns(1);
//		grid.getChildren().clear();
//
//		HtmlPanelGroup headingGroup = new HtmlPanelGroup();
//
//		grid.getChildren().add(headingGroup);
//		headingGroup.setStyleClass("form-panel-heading");
//
//		HtmlOutputText headingText = new HtmlOutputText();
//
//		headingGroup.getChildren().add(headingText);
//		headingText.setValue(Messages.getInstance().get(path.getLabelPath()));
//		headingText.setStyleClass("form-panel-heading");
//
//		if (!path.isReadonly()) {
//			HtmlPanelGrid buttonGrid = new HtmlPanelGrid();
//
//			grid.getChildren().add(buttonGrid);
//			buttonGrid.setId(JsfFormGenerator.PREFIX + "grid-"
//					+ UUID.randomUUID());
//
//			buttonGrid.setCellpadding("0");
//			buttonGrid.setCellspacing("0");
//			buttonGrid.setColumns(2);
//			buttonGrid.setStyleClass("form-panel-table-toolbar");
//
//			HtmlCommandButton addButton = new HtmlCommandButton();
//
//			buttonGrid.getChildren().add(addButton);
//			addButton
//					.setImage("/plugins/stardust-ui-form-jsf/public/css/images/add-table-button.gif");
//			addButton.setAction(FacesContext
//					.getCurrentInstance()
//					.getApplication()
//					.createMethodBinding(
//							"#{" + getFormBinding()
//									+ ".fullPathInputControllerMap['"
//									+ path.getFullXPath() + "'].add}",
//							new Class[] {}));
//		}
//
//		HtmlDataTable dataTable = new HtmlDataTable();
//
//		dataTable.setId(JsfFormGenerator.PREFIX + "data-table-"
//				+ UUID.randomUUID());
//		grid.getChildren().add(dataTable);
//		dataTable
//				.setValueBinding(
//						"value",
//						FacesContext
//								.getCurrentInstance()
//								.getApplication()
//								.createValueBinding(
//										"#{"
//												+ getFormBinding()
//												+ ".fullPathInputControllerMap['"
//												+ path.getFullXPath()
//												+ "'].entryList}"));
//
//		dataTable.setVar("entry");
//		// dataTable.setWidth("100%");
//		dataTable.setStyleClass("form-panel-table");
//		dataTable.setColumnClasses("form-panel-table-cell");
//		dataTable.setHeaderClass("form-panel-table-header");
//
//		UIColumn column;
//		HtmlOutputText headerText;
//
//		if (!path.isReadonly()) {
//			column = new UIColumn();
//
//			column.setId(JsfFormGenerator.PREFIX + "column-"
//					+ UUID.randomUUID());
//			dataTable.getChildren().add(column);
//
//			headerText = new HtmlOutputText();
//
//			headerText.setValue("");
//			headerText.setStyleClass("form-panel-table-header-text");
//
//			column.setHeader(headerText);
//
//			HtmlCommandButton deleteButton = new HtmlCommandButton();
//
//			deleteButton.setId(JsfFormGenerator.PREFIX
//					+ "command-button-table-" + UUID.randomUUID());
//			column.getChildren().add(deleteButton);
//			deleteButton
//					.setImage("/plugins/stardust-ui-form-jsf/public/css/images/delete-table-button.gif");
//			deleteButton.setPartialSubmit(true);
//			deleteButton.setAction(FacesContext.getCurrentInstance()
//					.getApplication()
//					.createMethodBinding("#{entry.delete}", new Class[] {}));
//		}
//
//		for (Path columnPath : path.getChildPaths()) {
//			column = new UIColumn();
//
//			column.setId(JsfFormGenerator.PREFIX + "column-"
//					+ UUID.randomUUID());
//			dataTable.getChildren().add(column);
//
//			headerText = new HtmlOutputText();
//
//			headerText.setValue(Messages.getInstance().get(
//					columnPath.getLabelPath()));
//			headerText.setStyleClass("form-panel-table-header-text");
//
//			column.setHeader(headerText);
//
//			UIInput input = getPrimitiveInput(columnPath);
//
//			column.getChildren().add(input);
//			input.setValueBinding(
//					"value",
//					FacesContext
//							.getCurrentInstance()
//							.getApplication()
//							.createValueBinding(
//									"#{entry.map." + columnPath.getId() + "}"));
//		}
	}

	/**
	 * 
	 */
	public FormContainer addStructureComponent(FormContainer parentComponent,
			Path path) {
		if (parentComponent == null) {
			throw new IllegalArgumentException(
					"'parentComponent' must not be null.");
		}

		return new AjaxStructureContainer(path);
	}

	/**
	 * 
	 */
	public FormInput addEnumerationInput(FormContainer parentContainer,
			PrimitiveInputController inputController) {
//		UIComponent parentComponent = ((JsfStructureContainer) parentContainer)
//				.getPrimitiveGrid();
//		HtmlOutputLabel label;
//
//		label = new HtmlOutputLabel();
//
//		label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//
//		label.setValue(Messages.getInstance().get(
//				inputController.getPath().getLabelPath())
//				+ ":");
//		label.setStyleClass("form-panel-label");
//
//		parentComponent.getChildren().add(label);
//
//		label = new HtmlOutputLabel();
//
//		label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//		label.setStyleClass("form-panel-prefix");
//
//		String prefixKey = inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_prefixKey");
//		String prefix = inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_prefix");
//
//		if (StringUtils.isNotEmpty(prefixKey)) {
//			label.setValue(Messages.getInstance().get(prefixKey));
//		} else if (StringUtils.isNotEmpty(prefix)) {
//			label.setValue(prefix);
//		} else {
//			label.setValue("");
//		}
//
//		parentComponent.getChildren().add(label);
//
//		if (inputController.getPath().isReadonly()) {
//			HtmlOutputText outputText = new HtmlOutputText();
//			parentComponent.getChildren().add(outputText);
//
//			label = new HtmlOutputLabel();
//
//			label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//			label.setStyleClass("form-panel-suffix");
//
//			String suffixKey = inputController.getPath().getProperty(
//					InputPreferences.class.getSimpleName() + "_suffixKey");
//			String suffix = inputController.getPath().getProperty(
//					InputPreferences.class.getSimpleName() + "_suffix");
//
//			if (StringUtils.isNotEmpty(suffixKey)) {
//				label.setValue(Messages.getInstance().get(suffixKey));
//			} else if (StringUtils.isNotEmpty(suffix)) {
//				label.setValue(suffix);
//			} else {
//				label.setValue("");
//			}
//
//			parentComponent.getChildren().add(label);
//
//			outputText.setValueBinding(
//					"value",
//					FacesContext
//							.getCurrentInstance()
//							.getApplication()
//							.createValueBinding(
//									"#{"
//											+ getFormBinding()
//											+ ".fullPathInputControllerMap['"
//											+ inputController.getPath()
//													.getFullXPath()
//											+ "'].value}"));
//
//			JsfFormInput formInput = new JsfFormInput(label, outputText);
//
//			inputController.setFormInput(formInput);
//
//			return formInput;
//		} else {
//			HtmlSelectOneMenu selectOneMenu = new HtmlSelectOneMenu();
//
//			parentComponent.getChildren().add(selectOneMenu);
//
//			selectOneMenu.setId(JsfFormGenerator.PREFIX + "input-text-"
//					+ UUID.randomUUID());
//
//			UISelectItems selectItems = new UISelectItems();
//
//			for (String enumValue : inputController.getPath()
//					.getEnumerationValues()) {
//
//				UISelectItem selectItem = new UISelectItem();
//
//				selectOneMenu.getChildren().add(selectItem);
//				selectItem.setItemLabel(Messages.getInstance().get(inputController.getPath().getTypeName() + "." + enumValue.toString()));
//				selectItem.setItemValue(enumValue.toString());
//			}
//
//			selectOneMenu
//					.setValueChangeListener(FacesContext
//							.getCurrentInstance()
//							.getApplication()
//							.createMethodBinding(
//									"#{"
//											+ getFormBinding()
//											+ ".fullPathInputControllerMap['"
//											+ inputController.getPath()
//													.getFullXPath()
//											+ "'].handlers['valueChangeHandler'].valueChanged}",
//									new Class[] { ValueChangeEvent.class }));
//			selectOneMenu.setPartialSubmit(true);
//
//			label = new HtmlOutputLabel();
//
//			label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//			label.setStyleClass("form-panel-suffix");
//
//			String suffixKey = inputController.getPath().getProperty(
//					InputPreferences.class.getSimpleName() + "_suffixKey");
//			String suffix = inputController.getPath().getProperty(
//					InputPreferences.class.getSimpleName() + "_suffix");
//
//			if (StringUtils.isNotEmpty(suffixKey)) {
//				label.setValue(Messages.getInstance().get(suffixKey));
//			} else if (StringUtils.isNotEmpty(suffix)) {
//				label.setValue(suffix);
//			} else {
//				label.setValue("");
//			}
//
//			parentComponent.getChildren().add(label);
//
//			JsfFormInput formInput = new JsfFormInput(label, selectOneMenu);
//
//			inputController.getHandlers().put("valueChangeHandler",
//					new ValueChangedHandler(inputController));
//			inputController.setFormInput(formInput);
//
//			return formInput;
//		}
		
		return null;
	}

	/**
	 * 
	 */
	public FormInput addPrimitiveInput(FormContainer parentContainer,
			PrimitiveInputController inputController) {
		AjaxFormInput returnFormInput = null;

//		UIComponent parentComponent = ((JsfStructureContainer) parentContainer)
//				.getPrimitiveGrid();
//		HtmlOutputLabel label;
//
//		label = new HtmlOutputLabel();
//
//		label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//
//		label.setValue(Messages.getInstance().get(
//				inputController.getPath().getLabelPath())
//				+ ":");
//		label.setStyleClass("form-panel-label");
//
//		parentComponent.getChildren().add(label);
//
//		label = new HtmlOutputLabel();
//
//		label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//		label.setStyleClass("form-panel-prefix");
//
//		String prefix = inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_prefix");
//		String prefixKey = inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_prefixKey");
//
//		if (StringUtils.isNotEmpty(prefixKey)) {
//			label.setValue(Messages.getInstance().get(prefixKey));
//		} else if (StringUtils.isNotEmpty(prefix)) {
//			label.setValue(prefix);
//		} else {
//			label.setValue("");
//		}
//
//		parentComponent.getChildren().add(label);
//
//		if (inputController.getPath().isReadonly()) {
//		    UIComponent comp = null;
//			if ((inputController.getPath().getJavaClass() == Boolean.class || inputController
//					.getPath().getJavaClass() == Boolean.TYPE)
//					&& "CHECKBOX".equals(inputController.getPath().getProperty(
//							BooleanInputPreferences.class.getName()
//									+ ".readonlyOutputType"))) {
//				HtmlSelectBooleanCheckbox selectBox = new HtmlSelectBooleanCheckbox();
//
//				parentComponent.getChildren().add(selectBox);
//				selectBox.setId(JsfFormGenerator.PREFIX
//						+ "select-boolean-checkbox-" + UUID.randomUUID());
//				selectBox.setDisabled(true);
//				selectBox.setReadonly(true);
//				comp = selectBox;
//				
//				returnFormInput = new JsfFormInput(label, selectBox);
//			} else {
//				HtmlOutputText outputText = new HtmlOutputText();
//				parentComponent.getChildren().add(outputText);
//				comp = outputText;
//
//				returnFormInput = new JsfFormInput(label, outputText);
//			}
//			
//			comp.setValueBinding(
//                  "value",
//                  FacesContext
//                        .getCurrentInstance()
//                        .getApplication()
//                        .createValueBinding(
//                              "#{" + getFormBinding() + ".fullPathInputControllerMap['"
//                                    + inputController.getPath().getFullXPath() + "'].value}"));
//		} else {
//			UIInput input = getPrimitiveInput(inputController.getPath());
//
//			parentComponent.getChildren().add(input);
//
//			returnFormInput = new JsfFormInput(label, input);
//		}
//
//		label = new HtmlOutputLabel();
//
//		label.setId(JsfFormGenerator.PREFIX + "label-" + UUID.randomUUID());
//		label.setStyleClass("form-panel-suffix");
//
//		String suffixKey = inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_suffixKey");
//		String suffix = inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_suffix");
//
//		if (StringUtils.isNotEmpty(suffixKey)) {
//			label.setValue(Messages.getInstance().get(suffixKey));
//		} else if (StringUtils.isNotEmpty(suffix)) {
//			label.setValue(suffix);
//		} else {
//			label.setValue("");
//		}
//
//		parentComponent.getChildren().add(label);
//
//		// Set mandatory flag
//
//		if (new Boolean(inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_mandatory"))) {
//			returnFormInput.setMandatory(true);
//		}
//
//		// Readonly flag
//
//		if (new Boolean(inputController.getPath().getProperty(
//				InputPreferences.class.getSimpleName() + "_readonly"))) {
//			returnFormInput.setReadonly(true);
//		}
//
//		inputController.setFormInput(returnFormInput);

		return returnFormInput;
	}

//	protected UIInput getPrimitiveInput(Path path) {
//		UIInput input = null;
//
//		if (path.getJavaClass() == String.class) {
//			if ("TEXTAREA".equals(path.getProperty(StringInputPreferences.class
//					.getSimpleName() + "_stringInputType"))) {
//				HtmlInputTextarea inputTextArea = new HtmlInputTextarea();
//
//				inputTextArea.setId(JsfFormGenerator.PREFIX + "input-textarea-"
//						+ UUID.randomUUID());
//				inputTextArea.setPartialSubmit(true);
//				inputTextArea.setReadonly(path.isReadonly());
//
//				String property = path.getProperty(StringInputPreferences.class
//						.getSimpleName() + "_textAreaCols");
//
//				if (property != null) {
//					inputTextArea.setCols(new Integer(property));
//				}
//
//				property = path.getProperty(StringInputPreferences.class
//						.getSimpleName() + "_textAreaRows");
//
//				if (property != null) {
//					inputTextArea.setRows(new Integer(property));
//				}
//
//				inputTextArea.setStyleClass("form-panel-textarea-input");
//
//				input = inputTextArea;
//			} else {
//				HtmlInputText inputText = new HtmlInputText();
//
//				inputText.setId(JsfFormGenerator.PREFIX + "input-text-"
//						+ UUID.randomUUID());
//
//				inputText.setPartialSubmit(true);
//				inputText.setMaxlength(mapMaxLength(path.getJavaClass()));
//
//				evaluateInputTextPreferences(path, inputText);
//
//				input = inputText;
//			}
//		} else if (path.isNumber()) {
//			HtmlInputText inputText = new HtmlInputText();
//
//			inputText.setId(JsfFormGenerator.PREFIX + "input-text-"
//					+ UUID.randomUUID());
//			inputText.setPartialSubmit(true);
//
//			// Settings might be overwritten by annotations
//
//			evaluateInputTextPreferences(path, inputText);
//
//			// Max length is fix for numbers
//
//			inputText.setMaxlength(mapMaxLength(path.getJavaClass()));
//
//			if (path.getJavaClass().equals(Byte.class)
//					|| path.getJavaClass().equals(Byte.TYPE)) {
//				inputText.setConverter(new ByteConverter());
//			} else if (path.getJavaClass().equals(Short.class)
//					|| path.getJavaClass().equals(Short.TYPE)) {
//				inputText.setConverter(new ShortConverter());
//			} else if (path.getJavaClass().equals(Integer.class)
//					|| path.getJavaClass().equals(Integer.TYPE)) {
//				inputText.setConverter(new IntegerConverter());
//			} else if (path.getJavaClass().equals(Long.class)
//					|| path.getJavaClass().equals(Long.TYPE)) {
//				inputText.setConverter(new LongConverter());
//			} else if (path.getJavaClass().equals(Float.class)
//					|| path.getJavaClass().equals(Float.TYPE)) {
//				inputText.setConverter(new FloatConverter());
//			} else if (path.getJavaClass().equals(Double.class)
//					|| path.getJavaClass().equals(Double.TYPE)) {
//				inputText.setConverter(new DoubleConverter());
//			} else if (path.getJavaClass().equals(BigDecimal.class)) {
//				inputText.setConverter(new BigDecimalConverter());
//			}
//
//			input = inputText;
//		} else if (path.getJavaClass() == Date.class) {
//			SelectInputDate inputDate = new SelectInputDate();
//
//			inputDate.setId(JsfFormGenerator.PREFIX + "select-input-date-"
//					+ UUID.randomUUID());
//
//			inputDate.setRenderAsPopup(true);
//
//			Converter converter = new DateTimeConverter();
//
//			inputDate.setPartialSubmit(true);
//			inputDate.setConverter(converter);
//
//			input = inputDate;
//		} else if (path.getJavaClass() == Boolean.class) {
//			HtmlSelectBooleanCheckbox selectBox = new HtmlSelectBooleanCheckbox();
//
//			selectBox.setId(JsfFormGenerator.PREFIX
//					+ "select-boolean-checkbox-" + UUID.randomUUID());
//			selectBox.setPartialSubmit(true);
//
//			input = selectBox;
//		} else {
//           HtmlInputText inputText = new HtmlInputText();
//
//           inputText.setId(JsfFormGenerator.PREFIX + "input-text-"
//                   + UUID.randomUUID());
//
//           inputText.setPartialSubmit(true);
//           inputText.setMaxlength(mapMaxLength(path.getJavaClass()));
//
//           evaluateInputTextPreferences(path, inputText);
//
//           input = inputText;
//		}
//
//		input.setValueBinding(
//				"value",
//				FacesContext
//						.getCurrentInstance()
//						.getApplication()
//						.createValueBinding(
//								"#{" + getFormBinding()
//										+ ".fullPathInputControllerMap['"
//										+ path.getFullXPath() + "'].value}"));
//
//		return input;
//	}

	private void evaluateInputTextPreferences(Path path) {
//		inputText.setStyle(path.getProperty(InputPreferences.class
//				.getSimpleName() + "_style"));
//		inputText.setStyleClass(path.getProperty(InputPreferences.class
//				.getSimpleName() + "_styleClass"));
//		inputText.setReadonly(new Boolean(path
//				.getProperty(InputPreferences.class.getSimpleName()
//						+ "_readonly")));
//
//		boolean mandatory = new Boolean(path.getProperty(InputPreferences.class
//				.getSimpleName() + "_mandatory"));
//
//		if (mandatory) {
//			// TODO Reactivate - does not work. Bug in ICEfaces value binding
//			// for styleClass?
//			/*
//			 * inputText .setValueBinding( "styleClass", FacesContext
//			 * .getCurrentInstance() .getApplication() .createValueBinding(
//			 * "#{'form-panel-input' + " + getFormBinding() +
//			 * ".fullPathInputControllerMap['" + path.getFullXPath() +
//			 * "'].value == null || " + getFormBinding() +
//			 * ".fullPathInputControllerMap['" + path.getFullXPath() +
//			 * "'].value == '' ? '-mandatory' : ''}"));
//			 */
//			inputText.setStyleClass("form-panel-input-mandatory");
//		} else {
//			if (path.isNumber()) {
//				inputText.setStyleClass("form-panel-number-input");
//			} else {
//				inputText.setStyleClass("form-panel-input");
//			}
//		}
//
//		/*
//		 * inputText.setMaxlength(new Integer(path
//		 * .getProperty(InputPreferences.class.getSimpleName() +
//		 * "_maxLength")));
//		 */
	}

	@Override
	public void addDocumentInput(Map<String, InputController> arg0,
			FormContainer arg1, InputController arg2) {
		// TODO Auto-generated method stub
		
	}
}
