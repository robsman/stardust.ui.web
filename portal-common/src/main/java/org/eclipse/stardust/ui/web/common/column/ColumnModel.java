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

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;


/**
 * @author Subodh.Godbole
 *
 */
public abstract class ColumnModel implements IColumnModel
{
   private static final Logger trace = LogManager.getLogger(ColumnModel.class);
   
   private List<ColumnPreference> selectableColumns;
   protected Map<String, ColumnPreference> columnPreferences;

   private List<List<ColumnPreference>> columnGroupRows;
   private List<ColumnPreference> renderableLeafColumns;
   private List<ColumnPreference> allLeafColumns;
   
   private boolean columnGropuing;
   private boolean initialized;

   /**
    * @param columns
    */
   public ColumnModel(List<ColumnPreference> columns)
   {
      setSelectableColumns(columns);
   }

   /**
    * @param obj
    * @param property Mapped Property e.g user.firstName or user.processDef[].OID
    * @return
    * @throws Exception
    */
   public static Object resolvePropertyAndInvokeGetter(Object obj, 
         String property) throws Exception
   {
      Object targetObj;
      String targetProperty;

      if(!ColumnPreference.isComplexProperty(property)) // SIMPLE PROPERTY
      {
         targetObj = obj;
         targetProperty = property;
      }
      else // COMPLEX PROPERTY
      {
         Map<String, Object> objPropMap = FacesUtils.getObjectPropertyMapping(obj, property);

         targetObj = objPropMap.get("object");
         targetProperty = (String)objPropMap.get("property");
      }

      if (targetObj instanceof Map<?, ?>)
      {
         return ((Map<?, ?>)targetObj).get(targetProperty);
      }
      else
      {
         return ReflectionUtils.invokeGetterMethod(targetObj, targetProperty);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getColumn(java.lang.String)
    */
   public ColumnPreference getColumn(String columnName)
   {
      return columnPreferences.get(columnName);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getAllColumns()
    */
   public List<ColumnPreference> getAllColumns()
   {
     return getSelectableColumns();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getAllLeafColumns()
    */
   public List<ColumnPreference> getAllLeafColumns()
   {
      return allLeafColumns;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getRenderableColumns()
    */
   public List<ColumnPreference> getRenderableColumns()
   {
      ArrayList<ColumnPreference> renderableColumns = new ArrayList<ColumnPreference>();
      for (ColumnPreference columnPreference : selectableColumns)
      {
         if( columnPreference.isVisible() )
         {
            renderableColumns.add(columnPreference);
         }
      }

      return renderableColumns;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#initialize()
    */
   public void initialize()
   {
      allLeafColumns = new ArrayList<ColumnPreference>();
      columnPreferences = new HashMap<String, ColumnPreference>();
      addToAllleafList(getAllColumns());
      log("******** allLeafColumns = " + allLeafColumns);
      
      columnGroupRows = new ArrayList<List<ColumnPreference>>();
      renderableLeafColumns = new ArrayList<ColumnPreference>();
      addToColumnGroupRows(0, getRenderableColumns());
      columnGropuing = columnGroupRows.size() > 1 ? true : false;
      
      initialized = true;
   }
   
   /**
    * Supporter method for initialize()
    * @param index
    * @param cols
    */
   private void addToColumnGroupRows(int index, List<ColumnPreference> cols)
   {
      if(index == columnGroupRows.size())
         columnGroupRows.add(new ArrayList<ColumnPreference>());
      else if(columnGroupRows.get(index) == null)
         columnGroupRows.set(index, new ArrayList<ColumnPreference>());
      
      List<ColumnPreference> thisRow = columnGroupRows.get(index); 
      for (ColumnPreference columnPreference : cols)
      {
         thisRow.add(columnPreference);
         if(columnPreference.getChildrenCount() > 0)
         {
            addToColumnGroupRows(index+1, columnPreference.getChildren());
         }
         else
         {
            renderableLeafColumns.add(columnPreference);
         }
      }
   }
   
   /**
    * @param cols
    */
   private void addToAllleafList(List<ColumnPreference> cols)
   {
      for (ColumnPreference columnPreference : cols)
      {
         addToPreferenceMap(columnPreference);

         if(columnPreference.getChildrenCount() > 0)
         {
            addToAllleafList(columnPreference.getChildren());
         }
         else
         {
            allLeafColumns.add(columnPreference);
         }
      }
   }

   /**
    * @param columns
    */
   public void setSelectableColumns(List<ColumnPreference> columns)
   {
      this.selectableColumns = columns;
      if(this.selectableColumns == null)
      {
         this.selectableColumns = new ArrayList<ColumnPreference>();
      }
   }

   /**
    * @return
    */
   public int getColumnGroupRowsCount()
   {
      return columnGroupRows.size();
   }

   /**
    * @param columnPreference
    */
   private void addToPreferenceMap(ColumnPreference columnPreference)
   {
      if( columnPreferences.containsKey(columnPreference.getColumnName()) )
      {
         // throw new IllegalArgumentException("Duplicate Column Name: " + 
         //                                       columnPreference.getColumnName());
         // Instead of throwing exception show a message dialog
         MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getParamString(
               "common.genericDataTable.columnModel.duplicateColumn", columnPreference.getColumnName()));
      }
         
      columnPreferences.put(columnPreference.getColumnName(), columnPreference);
   }
   
   /**
    * 
    */
   private void verifyInitialize()
   {
      if(!initialized)
      {
         trace.debug("Model needs to be initialized before using it.");
         throw new IllegalStateException("Model needs to be initialized before using it.");
      }
   }

   /**
    * @param msg
    */
   protected void log(String msg)
   {
      trace.debug(msg);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.column.IColumnModel#getRenderableLeafColumns()
    */
   public List<ColumnPreference> getRenderableLeafColumns()
   {
      verifyInitialize();
      return renderableLeafColumns;
   }

   public List<ColumnPreference> getSelectableColumns()
   {
      return selectableColumns;
   }

   public boolean isColumnGropuing()
   {
      return columnGropuing;
   }

   public List<List<ColumnPreference>> getColumnGroupRows()
   {
      return columnGroupRows;
   }
}
