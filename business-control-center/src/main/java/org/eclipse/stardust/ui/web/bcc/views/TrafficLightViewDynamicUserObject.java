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
package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

public class TrafficLightViewDynamicUserObject extends DefaultRowModel {
	
   private static final long serialVersionUID = 1L;

   private String processActivityId;
   private String processActivityQualifiedId;

	private String completed;

	private String symbolUrl;

	private String symbolName;

	private String completedIcon;

	private boolean activePIs;

	private String categoryValue;

	/**
	 * @param processActivityId
	 * @param Completed
	 * @param SymbolUrl
	 * @param symbolName
	 * @param CompletedIcon
	 * @param activePIs
	 * @param categoryValue
	 */
	public TrafficLightViewDynamicUserObject(String processActivityId,String processActivityQualifiedId,
			String Completed, String SymbolUrl, String symbolName,
			String CompletedIcon, boolean activePIs, String categoryValue) {
		super();
		this.processActivityId = processActivityId;
		this.processActivityQualifiedId = processActivityQualifiedId;
		this.completed = Completed;
		this.symbolUrl = SymbolUrl;
		this.symbolName = symbolName;
		this.completedIcon = CompletedIcon;
		this.activePIs = activePIs;
		this.categoryValue = categoryValue;
	}

	public String getProcessActivityId() {
		return processActivityId;
	}

	public void setProcessActivityId(String processActivityId) {
		this.processActivityId = processActivityId;
	}

	public String getCompleted() {
		return completed;
	}

	public void setCompleted(String completed) {
		this.completed = completed;
	}

	public String getSymbolUrl() {
		return symbolUrl;
	}

	public void setSymbolUrl(String symbolUrl) {
		this.symbolUrl = symbolUrl;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public void setSymbolName(String symbolName) {
		this.symbolName = symbolName;
	}

	public String getCompletedIcon() {
		return completedIcon;
	}

	public void setCompletedIcon(String completedIcon) {
		this.completedIcon = completedIcon;
	}

	public boolean isActivePIs() {
		return activePIs;
	}

	public void setActivePIs(boolean activePIs) {
		this.activePIs = activePIs;
	}

	public String getCategoryValue() {
		return categoryValue;
	}

	public void setCategoryValue(String categoryValue) {
		this.categoryValue = categoryValue;
	}

	public String getProcessActivityQualifiedId() {
      return processActivityQualifiedId;
	}
	

}
