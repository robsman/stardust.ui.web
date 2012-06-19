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

import java.util.List;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


public class TrafficLightViewUserObject extends DefaultRowModel {
	private static final long serialVersionUID = 1L;

	private String categoryName;

	private String totalCount;

	private List<TrafficLightViewDynamicUserObject> trafficLightViewDynamicUserObjectList;

	/**
	 * @param categoryName
	 * @param totalCount
	 * @param trafficLightViewDynamicUserObjectList
	 */
	public TrafficLightViewUserObject(
			String categoryName,
			String totalCount,
			List<TrafficLightViewDynamicUserObject> trafficLightViewDynamicUserObjectList) {
		super();
		this.categoryName = categoryName;
		this.totalCount = totalCount;
		this.trafficLightViewDynamicUserObjectList = trafficLightViewDynamicUserObjectList;

	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(String totalCount) {
		this.totalCount = totalCount;
	}

	public List<TrafficLightViewDynamicUserObject> getTrafficLightViewDynamicUserObjectList() {
		return trafficLightViewDynamicUserObjectList;
	}

	public void setTrafficLightViewDynamicUserObjectList(
			List<TrafficLightViewDynamicUserObject> trafficLightViewDynamicUserObjectList) {
		this.trafficLightViewDynamicUserObjectList = trafficLightViewDynamicUserObjectList;
	}
}
