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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;



/**
 * @author Subodh.Godbole
 *
 */
public class DefaultColumnModel extends ColumnModel
{
   private static final Logger trace = LogManager.getLogger(DefaultColumnModel.class);
   
   private static final long serialVersionUID = -4852240131774176913L;

   private List<ColumnPreference> fixedBeforeColumns;
   private List<ColumnPreference> fixedAfterColumns;

   private Map<String, ColumnPreference> fixedColumnPreferences;
   
   private String moduleId;
   private String viewId;
   private IColumnModelListener listener;
   
   private List<ColumnPreference> orgColumns;
   
   private PreferenceScope preferenceScope = PreferenceScope.USER;
   
   private IColumnPreferenceHandler columnPreferenceHandler;
   private boolean lock;

   /**
    * @param columns
    * @param moduleId
    * @param viewId
    */
   public DefaultColumnModel(List<ColumnPreference> columns, String moduleId, String viewId)
   {
      this(columns, null, null, moduleId, viewId, null);
   }

   /**
    * @param columns
    * @param moduleId
    * @param viewId
    * @param listener
    */
   public DefaultColumnModel(List<ColumnPreference> columns, String moduleId,
         String viewId, IColumnModelListener listener)
   {
      this(columns, null, null, moduleId, viewId, listener);
   }

   /**
    * @param columns
    * @param fixedBeforeColumns
    * @param fixedAfterColumns
    * @param moduleId
    * @param viewId
    */
   public DefaultColumnModel(List<ColumnPreference> columns,
         List<ColumnPreference> fixedBeforeColumns,
         List<ColumnPreference> fixedAfterColumns, String moduleId, String viewId)
   {
      this(columns, fixedBeforeColumns, fixedAfterColumns, moduleId, viewId, null);
   }

   /**
    * @param columns
    * @param fixedBeforeColumns
    * @param fixedAfterColumns
    * @param moduleId
    * @param viewId
    * @param listener
    */
   public DefaultColumnModel(List<ColumnPreference> columns,
         List<ColumnPreference> fixedBeforeColumns,
         List<ColumnPreference> fixedAfterColumns, String moduleId, String viewId,
         IColumnModelListener listener)
   {
      super(columns);
      this.moduleId = moduleId;
      this.viewId = viewId;
      this.fixedBeforeColumns = (fixedBeforeColumns == null) ? new ArrayList<ColumnPreference>() : fixedBeforeColumns;
      this.fixedAfterColumns = (fixedAfterColumns == null) ? new ArrayList<ColumnPreference>() : fixedAfterColumns;
      this.listener = listener;

      // Create Preference Map for Fixed Cols
      List<ColumnPreference> allFixedCols = new ArrayList<ColumnPreference>();
      allFixedCols.addAll(this.fixedBeforeColumns);
      allFixedCols.addAll(this.fixedAfterColumns);
      fixedColumnPreferences = new HashMap<String, ColumnPreference>(allFixedCols.size());
      
      for (ColumnPreference columnPreference : allFixedCols)
      {
         if( fixedColumnPreferences.containsKey(columnPreference.getColumnName()) )
         {
            throw new IllegalArgumentException("Duplicate Fixed Column Name: " + 
                                                   columnPreference.getColumnName());
         }
            
         fixedColumnPreferences.put(columnPreference.getColumnName(), columnPreference);
      }
      
      orgColumns = getClone(getSelectableColumns());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#initialize()
    */
   public void initialize()
   {
      super.initialize();
      this.reInitialize();
   }
   
   /**
    * 
    */
   protected void reInitialize()
   {
      // Get Saved state and Reorder Columns
      setSelectableColumns(orderAndSelectAsPerSavedState(getSelectableColumns(), preferenceScope));
      notifyListeners(); // Notify

      // Columns may have changed so again ReInitialize
      super.initialize();
   }
   
   @Override
   public ColumnPreference getColumn(String columnName)
   {
      ColumnPreference columnPreference = super.getColumn(columnName);
      if(columnPreference == null)
      {
         columnPreference = fixedColumnPreferences.get(columnName);
      }

      return columnPreference;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getAllColumns()
    */
   public List<ColumnPreference> getAllColumns()
   {
      List<ColumnPreference> allCols = new ArrayList<ColumnPreference>();
      allCols.addAll(fixedBeforeColumns);
      allCols.addAll(super.getAllColumns());
      allCols.addAll(fixedAfterColumns);

      return allCols;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#setDefaultSelectableColumns(java.util.List)
    */
   public void setDefaultSelectableColumns(List<ColumnPreference> columns)
   {
      setSelectableColumns(columns);
      orgColumns = getClone(getSelectableColumns());
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#saveSelectableColumns()
    */
   public void saveSelectableColumns(PreferenceScope prefScope)
   {
      saveSelectableColumns(prefScope, getSelectableColumns());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#saveSelectableColumns()
    */
   public void saveSelectableColumns(PreferenceScope prefScope, List<ColumnPreference> cols)
   {
      ArrayList<String> colsToBeSaved = getColsToBeSaved(cols);
      this.preferenceScope = prefScope;
      if (null == columnPreferenceHandler)
      {
         UserPreferencesHelper userPreferences = UserPreferencesHelper.getInstance(moduleId, prefScope);
         userPreferences.setSelectedColumns(viewId, colsToBeSaved);
      }
      else
      {
         columnPreferenceHandler.savePreferences(prefScope, colsToBeSaved, lock);
      }
      
      log("[saveSelectableColumns]-> Plattenbau Storing List for '" + prefScope + "' = " + colsToBeSaved);
      
      this.initialize();
      
      // Notify
      //notifyListeners();
   }
   
   public ArrayList<String> getColsToBeSaved(List<ColumnPreference> cols)
   {
      ArrayList<String> colsToBeSaved = new ArrayList<String>();

      for (ColumnPreference columnPreference : cols)
      {
         if (columnPreference.isVisible())
         {
            colsToBeSaved.add(columnPreference.getColumnName());
         }
      }
      return colsToBeSaved;
   }
   
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#resetSelectableColumns()
    */
   public void resetSelectableColumns(PreferenceScope prefScope)
   {
      this.preferenceScope = prefScope;
      
      if (null == columnPreferenceHandler)
      {
         UserPreferencesHelper userPreferences = UserPreferencesHelper.getInstance(moduleId, prefScope);
         userPreferences.resetSelectedColumns(viewId);
      }
      else
      {
         columnPreferenceHandler.resetPreferences(prefScope);
      }
      
      log("[resetSelectableColumns]-> Plattenbau resetted Columns for '" + prefScope);
      
      setSelectableColumns(getClone(orgColumns));
      this.initialize();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getSelectableColumnsForPreferenceScope(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public List<ColumnPreference> getSelectableColumnsForPreferenceScope(PreferenceScope prefScope)
   {
      return orderAndSelectAsPerSavedState(getClone(orgColumns), prefScope);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.ColumnModel#getRenderableColumns()
    */
   public List<ColumnPreference> getRenderableColumns()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.addAll(getVisibleColumns(fixedBeforeColumns));
      cols.addAll(super.getRenderableColumns());
      cols.addAll(getVisibleColumns(fixedAfterColumns));
      
      return cols;
   }
   
   /**
    * @param cols
    * @return
    */
   private List<ColumnPreference> getVisibleColumns(List<ColumnPreference> cols)
   {
      List<ColumnPreference> visibleCols = new ArrayList<ColumnPreference>();
      for (ColumnPreference col : cols)
      {
         if (col.isVisible())
         {
            visibleCols.add(col);
         }
      }
      
      return visibleCols;
   }

   /**
    * Mark Stored columns as Visible i.e. Selected and others as not selected
    * Also sort Selected as per Saved Order 
    * @return
    */
   private List<ColumnPreference> orderAndSelectAsPerSavedState(List<ColumnPreference> cols, PreferenceScope pScope)
   {
      List<String> storedList = null;
      if (null == columnPreferenceHandler)
      {
         UserPreferencesHelper userPreferences = UserPreferencesHelper.getInstance(moduleId, pScope);
         storedList = userPreferences.getSelectedColumns(viewId);
      }
      else
      {
         columnPreferenceHandler.fetchPreferences(pScope);
         storedList = columnPreferenceHandler.getPreferences();
         this.lock = columnPreferenceHandler.isLock();
      }
      
      log("[DefaultColumnModel]-> For '" + pScope + "' Got Plattenbau Stored List = " + storedList);
      log("[DefaultColumnModel]-> cols = " + cols);

      if(storedList == null)
      {
         return cols;
      }
      else
      {
         ColumnPreference colPreference;
         ArrayList<ColumnPreference> selectedOrderedList = new ArrayList<ColumnPreference>();
         Map<String, ColumnPreference> colsMap = getColumnsAsMap(cols); 
         for (String colName : storedList)
         {
            colPreference = colsMap.get(colName);
            if(colPreference != null)
            {
               colPreference.setVisible(true);
               selectedOrderedList.add(colPreference);
            }
         }
         
         for (ColumnPreference columnPreference : cols)
         {
            if( !selectedOrderedList.contains(columnPreference) )
            {
               columnPreference.setVisible(false);
               selectedOrderedList.add(columnPreference);
            }
         }

         return selectedOrderedList;
      }
   }

   /**
    * @param cols
    * @return
    */
   private Map<String, ColumnPreference> getColumnsAsMap(List<ColumnPreference> cols)
   {
      Map<String, ColumnPreference> colsPref = new HashMap<String, ColumnPreference>();
      for (ColumnPreference col : cols)
      {
         colsPref.put(col.getColumnName(), col);
      }
      return colsPref;
   }

   /**
    * @param columns
    * @return
    */
   private List<ColumnPreference> getClone(List<ColumnPreference> columns)
   {
      List<ColumnPreference> cloneColumns = new ArrayList<ColumnPreference>(columns.size());
      for (ColumnPreference columnPreference : columns)
      {
         cloneColumns.add(columnPreference.getClone());
      }
      
      return cloneColumns;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#notifyListeners()
    */
   public void notifyListeners()
   {
      if(listener != null)
         listener.columnsRearranged(this);
      else
         trace.debug("DefaultColumnModel: Listener is NULL can not notify columnsRearranged");
   }

   public boolean isLock()
   {
      return lock;
   }

   public void setLock(boolean lock)
   {
      this.lock = lock;
   }

   public void setColumnPreferenceHandler(IColumnPreferenceHandler columnPreferenceHandler)
   {
      this.columnPreferenceHandler = columnPreferenceHandler;
   }
}
