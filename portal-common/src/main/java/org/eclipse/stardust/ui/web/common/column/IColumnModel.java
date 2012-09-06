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
package org.eclipse.stardust.ui.web.common.column;

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;


/**
 * @author Subodh.Godbole
 *
 */
public interface IColumnModel extends Serializable
{
   /**
    * Returns the columns that are available to choose and order
    * @return
    */
   List<ColumnPreference> getSelectableColumns();
   void setSelectableColumns(List<ColumnPreference> columns);
   void setDefaultSelectableColumns(List<ColumnPreference> columns);
   
   /**
    * Returns the list of columns to be rendered
    * This includes the selected columns as well default/fixed columns
    * @return
    */
   List<ColumnPreference> getRenderableColumns();
   List<ColumnPreference> getRenderableLeafColumns();
   
   /**
    * Return All Columns
    * @return
    */
   List<ColumnPreference> getAllColumns();
   List<ColumnPreference> getAllLeafColumns();
   
   /**
    * Saves the user selection
    */
   void saveSelectableColumns(PreferenceScope prefScope);
   void resetSelectableColumns(PreferenceScope prefScope);
   List<ColumnPreference> getSelectableColumnsForPreferenceScope(PreferenceScope prefScope);
   
   ColumnPreference getColumn(String columnName);
   
   /**
    * Returns the 2 dimension List for rendering grouped headers
    * @return
    */
   List<List<ColumnPreference>> getColumnGroupRows();
   int getColumnGroupRowsCount();
   boolean isColumnGropuing();
   
   /**
    * Initialized would be called after creating model so that model can configure itself
    */
   void initialize();
   void notifyListeners();
   
   void setStoredList(List<String> storedList);
   void setLock(boolean lock);
   boolean isLock();
   
}
