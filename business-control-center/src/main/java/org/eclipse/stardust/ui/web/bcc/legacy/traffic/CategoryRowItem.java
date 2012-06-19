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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class represents a row of a data table. Such a row
 * contains several columns containing 0..n elements.
 * 
 * @author mueller1
 * 
 */
public class CategoryRowItem implements IRowItem {

	private String id = null;
	private String name = null;
	private String processId = null;
	private String categoryValue = null;
	private Long totalCount = new Long(0);
	private List/* <ProcssInstance> */activePIs;

	private Map/* <String, IColumnItem> */columnItems;

	public CategoryRowItem(String processId, String id, String name,
			String categoryValue) {
		this.processId = processId;
		this.id = id;
		this.name = name;
		this.categoryValue = categoryValue;
		this.columnItems = new HashMap/* <String, IColumnItem> */();
	}

	public String getId() {
		return this.id;
	}

	public void addColumnItem(IColumnItem columnItem) {
		if (!columnItems.containsKey(columnItem.getId())) {
			columnItems.put(columnItem.getId(), columnItem);
		}
	}

	public Collection/* <IColumnItem> */getColumnItems() {
		return columnItems.values();
	}

	public String getName() {
		return name;
	}

	public void setActivePIs(List /* <ProcessInstance> */activePIs) {
		this.activePIs = activePIs;
	}
	
	public List /* <ProcessInstance> */ getActivePIs()
	{
		return this.activePIs;
	}

	public void calculateColumnStates() {
		for (Iterator/* <IColumnItem> */_iterator = this.columnItems.values()
				.iterator(); _iterator.hasNext();) {
			IColumnItem columnItem = (IColumnItem) _iterator.next();
			columnItem.calculateColumnState(this.processId, this.id,
					this.categoryValue);
		}
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public Long getTotalCount() {
		return totalCount != null ? totalCount : new Long(0);
	}

	public IColumnItem getColumnItem(String id) {
		return (IColumnItem) this.columnItems.get(id);
	}

	public String getCategoryValue() {
		return categoryValue;
	}

}
