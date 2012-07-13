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

import org.eclipse.stardust.ui.common.form.FormInput;
import org.eclipse.stardust.ui.common.form.Indent;

/**
 * 
 * @author Ellie.Sepehri
 *
 */
public class AjaxFormInput implements FormInput {
	public static final String INPUT_TEXT = "INPUT_TEXT";
	public static final String INPUT_CHECKBOX = "INPUT_HECKBOX";
	public static final String INPUT_TEXTAREA = "INPUT_TEXTAREA";

	private String label;
	private String type;
	private Object value;
	private boolean mandatory;
	private boolean readonly;

	protected AjaxFormInput(String label, String type) {
		this.label = label;
		this.type = type;
	}

	public Object getValue() {
			return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public String generateMarkupCode(Indent indent) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(indent);

		buffer.append("<div data-role=\"fieldcontain\">");
		buffer.append("<label for=\"firstNameInput\">First Name</label>");
		buffer.append("<input type=\"text\" name=\"firstNameInput\" id=\"firstNameInput\" value=\"\" data-mini=\"true\" />");
		buffer.append("</div>");
		buffer.append("<ice:outputLabel id=\"");
		
//		buffer.append(getLabel().getId());
//		buffer.append("\" value=\"");
//		buffer.append(getLabel().getValue());
//		buffer.append("\"/>\n");
//
//		if (getInput() != null) {
//			if (getInput() instanceof HtmlSelectOneMenu) {
//				HtmlSelectOneMenu selectOneMenu = (HtmlSelectOneMenu) getInput();
//
//				buffer.append(indent);
//				buffer.append("<ice:selectOneMenu id=\"");
//				buffer.append(selectOneMenu.getId());
//				buffer.append("\" partialSubmit=\"");
//				buffer.append(selectOneMenu.getPartialSubmit());
//				buffer.append("\" readonly=\"");
//				buffer.append(selectOneMenu.isReadonly());
//                buffer.append("\" value=\"");
//                buffer.append(selectOneMenu.getValueBinding("value"));
//				buffer.append("\">\n");
//
//				indent.increment();
//
//				for (Object component : getInput().getChildren()) {
//					if (component instanceof UISelectItem) {
//						UISelectItem selectItem = (UISelectItem) component;
//
//						buffer.append(indent);
//						buffer.append("<ice:selectItem label=\"");
//						buffer.append(selectItem.getItemLabel());
//						buffer.append("\" value=\"");
//						buffer.append(selectItem.getItemValue());
//						buffer.append("\"/>\n");
//					}
//				}
//
//				indent.decrement();
//
//				buffer.append(indent);
//				buffer.append("</ice:selectOneMenu>\n");
//			} else if (getInput() instanceof HtmlSelectBooleanCheckbox) {
//
//				HtmlSelectBooleanCheckbox selectBox = (HtmlSelectBooleanCheckbox) getInput();
//
//				buffer.append(indent);
//				buffer.append("<ice:selectBooleanCheckbox id=\"");
//				buffer.append(selectBox.getId());
//				buffer.append("\" partialSubmit=\"");
//				buffer.append(selectBox.getPartialSubmit());
//				buffer.append("\" readonly=\"");
//				buffer.append(selectBox.isReadonly());
//                buffer.append("\" value=\"");
//                buffer.append(selectBox.getValueBinding("value"));
//				buffer.append("\"/>\n");
//			} else if (getInput() instanceof SelectInputDate) {
//				SelectInputDate inputDate = (SelectInputDate) getInput();
//
//				buffer.append(indent);
//				buffer.append("<ice:selectInputDate id=\"");
//				buffer.append(inputDate.getId());
//				buffer.append("\" partialSubmit=\"");
//				buffer.append(inputDate.getPartialSubmit());
//				buffer.append("\" renderAsPopup=\"");
//				buffer.append(inputDate.isRenderAsPopup());
//				buffer.append("\" converter=\"");
//				buffer.append(inputDate.getConverter());
//				buffer.append("\" readonly=\"");
//				buffer.append(inputDate.isReadonly());
//                buffer.append("\" value=\"");
//                buffer.append(inputDate.getValueBinding("value"));
//				buffer.append("\"/>\n");
//			} else if (getInput() instanceof HtmlInputText) {
//				HtmlInputText inputText = (HtmlInputText) getInput();
//
//				buffer.append(indent);
//				buffer.append("<ice:inputText id=\"");
//				buffer.append(getInput().getId());
//				buffer.append("\" partialSubmit=\"");
//				buffer.append(inputText.getPartialSubmit());
//				buffer.append("\" readonly=\"");
//				buffer.append(inputText.isReadonly());
//				buffer.append("\" maxlength=\"");
//				buffer.append(inputText.getMaxlength());
//				buffer.append("\" styleClass=\"");
//				buffer.append(inputText.getStyleClass());
//				buffer.append("\" style=\"");
//				buffer.append(inputText.getStyle());
//                buffer.append("\" value=\"");
//                buffer.append(inputText.getValueBinding("value"));
//				buffer.append("\"/>\n");
//			}
//		} else if (getInput() instanceof HtmlInputTextarea) {
//			HtmlInputTextarea inputTextarea = (HtmlInputTextarea) getInput();
//
//			buffer.append(indent);
//			buffer.append("<ice:inputText id=\"");
//			buffer.append(getInput().getId());
//			buffer.append("\" partialSubmit=\"");
//			buffer.append(inputTextarea.getPartialSubmit());
//			buffer.append("\" readonly=\"");
//			buffer.append(inputTextarea.isReadonly());
//			buffer.append("\" cols=\"");
//			buffer.append(inputTextarea.getCols());
//			buffer.append("\" rows=\"");
//			buffer.append(inputTextarea.getRows());
//			buffer.append("\" styleClass=\"");
//			buffer.append(inputTextarea.getStyleClass());
//			buffer.append("\" style=\"");
//			buffer.append(inputTextarea.getStyle());
//            buffer.append("\" value=\"");
//            buffer.append(inputTextarea.getValueBinding("value"));
//			buffer.append("\"/>\n");
//		} else {
//			buffer.append(indent);
//			buffer.append("<ice:outputText value=\"");
//			buffer.append(getOutputText().getValueBinding("value"));
//			buffer.append("\"/>\n");
//		}

		return buffer.toString();
	}
}