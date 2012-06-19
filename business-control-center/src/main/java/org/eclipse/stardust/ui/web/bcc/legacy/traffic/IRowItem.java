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
import java.util.List;

public interface IRowItem {

	public String getId();

	public String getName();

	public void addColumnItem(IColumnItem columnItem);

	public Collection/* <IColumnItem> */getColumnItems();

	public IColumnItem getColumnItem(String id);

	public void calculateColumnStates();

	public void setTotalCount(Long totalCount);

	public Long getTotalCount();

	public String getCategoryValue();

	public void setActivePIs(List /* <ProcessInstance> */activePIs);

	public List /* <ProcessInstance> */getActivePIs();
}
