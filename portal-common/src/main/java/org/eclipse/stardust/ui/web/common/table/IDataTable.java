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
package org.eclipse.stardust.ui.web.common.table;

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;


/**
 * @author Subodh.Godbole
 *
 * @param <T>
 */
public interface IDataTable<T extends IRowModel> extends Serializable
{
   String getId();
   void initialize();

   List<T> getList();
   void setList(List<T> list);
   
   int getRowCount();
   
   IColumnModel getColumnModel();
   void setColumnModel(IColumnModel columnModel);
   
   TableDataFilters getDataFilters();
   TableDataFilterPopup getDataFilterPopup();
   TableColumnSelectorPopup getColumnSelectorPopup();
   
   /**
    * Enable/Disable auto filter feature
    * @return
    */
   void setAutoFilter(boolean autoFilter);
   boolean isShowFilterAtColumns();
   public void setRowSelector(DataTableRowSelector rowSelector);
}
