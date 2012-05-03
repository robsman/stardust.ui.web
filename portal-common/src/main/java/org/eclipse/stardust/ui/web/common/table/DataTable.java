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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.faces.component.UIData;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.table.export.CSVDataTableExporter;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExcelDataTableExporter;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * Class representing Table with Data Model and Data Filters
 * @author Subodh.Godbole
 *
 */
public class DataTable<T extends IRowModel> implements IDataTable<T>, ITableDataFilterListener
{
   private static String EXCEL_SUPPORT_LIB = "jxl.Workbook";

   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(DataTable.class);

   private static Boolean hasExcelClassesInClassPath;

   protected List<T> list = null;
   protected List<T> originalList = null;
   
   protected String id;
   protected boolean autoFilter = true;
   
   // Filters
   protected IColumnModel columnModel;
   protected TableDataFilters dataFilters;

   protected TableDataFilterPopup dataFilterPopup;
   protected TableColumnSelectorPopup columnSelectorPopup;

   private boolean showFilterAtColumns = true;
   
   private UIData tableUI;

   protected boolean allRowsSelected = false;
   
   private HtmlDataTable tableBinding;
   
   private DataTableRowSelector rowSelector;

   private DataTableExportHandler<T> dataTableExportHandler;
   
 
   
   /**
    * @param list
    * @param columnSelectorPopup
    * @param dataFilterPopup
    */
   public DataTable(List<T> list, TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilterPopup dataFilterPopup)
   {
      if(list != null)
         setList(list);

      this.columnSelectorPopup = columnSelectorPopup;
      this.dataFilterPopup = dataFilterPopup;
      
      if(columnSelectorPopup != null)
         columnModel = columnSelectorPopup.getColumnModel();

      if(dataFilterPopup != null)
         dataFilters = dataFilterPopup.getDataFilters();
   }

   /**
    * @param list
    * @param columnModel
    * @param dataFilters
    */
   public DataTable(List<T> list, IColumnModel columnModel, TableDataFilters dataFilters)
   {
      if(list != null)
         setList(list);

      this.columnModel = columnModel;
      this.dataFilters = dataFilters;
   }

   /**
    * @param list
    * @param columnSelectorPopup
    * @param dataFilters
    */
   public DataTable(List<T> list, TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilters dataFilters)
   {
      if(list != null)
         setList(list);

      this.columnSelectorPopup = columnSelectorPopup;
      this.dataFilters = dataFilters;
      
      if(columnSelectorPopup != null)
         columnModel = columnSelectorPopup.getColumnModel();
   }

   /**
    * @param columnSelectorPopup
    * @param dataFilters
    */
   public DataTable(TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilters dataFilters)
   {
      this(null, columnSelectorPopup, dataFilters);
   }

   /**
    * @param columnSelectorPopup
    * @param dataFilterPopup
    */
   public DataTable(TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilterPopup dataFilterPopup)
   {
      this(null, columnSelectorPopup, dataFilterPopup);
   }

   /**
    * @param list
    * @param columnModel
    * @param dataFilters
    */
   public DataTable(IColumnModel columnModel, TableDataFilters dataFilters)
   {
      this(null, columnModel, dataFilters);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.IDataTable#initialize()
    */
   public void initialize()
   {
      if(columnModel == null)
         throw new IllegalArgumentException("Column Model can not be null");

      if (trace.isDebugEnabled())
      {
         trace.debug("autoFilter = " + autoFilter);
      }

      boolean useColumnFilters = (dataFilterPopup == null || dataFilters == null);
      if(useColumnFilters && dataFilters == null)
      {
         dataFilters = new TableDataFilters();
      }

      columnModel.initialize();
      if(columnSelectorPopup != null)
         columnSelectorPopup.initialize();

      // No Filters directly set at Table.
      if(useColumnFilters) 
      {
         trace.debug("Using Column Level Data Filters");

         showFilterAtColumns = true;

         TableDataFilterPopup filterPopup;
         for (ColumnPreference columnPreference : columnModel.getAllLeafColumns())
         {
            filterPopup = columnPreference.getColumnDataFilterPopup();
            if (trace.isDebugEnabled())
            {
               trace.debug("Filter for Col = " + 
                     columnPreference.getColumnContentUrl() + " -> " + filterPopup);
            }

            if(filterPopup != null)
            {
               dataFilters.addDataFilters(filterPopup.getDataFilters());
               
               String filterLabel = MessagePropertiesBean.getInstance().getString(
                     "common.filterPopup.dataFilterByLabel")
                     + " ";
               filterPopup.setTitle(filterLabel + columnPreference.getColumnTitle());

               if(autoFilter)
                  filterPopup.setListener(this);
            }
         }
         dataFilters.resetFilters();
      }
      else // Table has Filters set
      {
         trace.debug("Using Table Level Data Filters");

         showFilterAtColumns = false;

         ColumnPreference columnPreference;
         for (ITableDataFilter tableDataFilter : dataFilterPopup.getDataFilters().getList())
         {
            columnPreference = columnModel.getColumn(tableDataFilter.getName());
            if(columnPreference != null)
            {
               tableDataFilter.setProperty(columnPreference.getColumnProperty());
            }
         }

         if(autoFilter)
            dataFilterPopup.setListener(this);
      }

      // If any Filters set Initially Apply them
      applyFilter(dataFilters);

      allRowsSelected = false;
      
      if (null != getRowSelector())
      {
         getRowSelector().resetSelectedRow(list);
      }
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.table.IDataTable#getRowCount()
    */
   public int getRowCount()
   {
      if(getList() != null)
      {
         return getList().size();
      }
      
      return 0;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener#applyFilter(org.eclipse.stardust.ui.web.common.filter.TableDataFilters)
    */
   public void applyFilter(TableDataFilters tableDataFilters)
   {
      if(originalList != null)
      {
         list = new ArrayList<T>();
         
         try
         {
            for (T t : originalList)
            {
               boolean filterOut = dataFilters.isFilterOut(t);
               if(!filterOut)
               {
                  list.add(t);
               }
            }
         }
         catch(Exception e)
         {
            trace.error(e);
         }
      }
   }
   
   /**
    * @return
    */
   public String getColumnWidths()
   {
      String DEFAULT_COL_WIDTH = "80px";
      StringBuffer sb = new StringBuffer();
      
      ColumnPreference colPref;
      List<ColumnPreference> cols = columnModel.getRenderableLeafColumns();
      int size = cols.size();
      for(int i = 0; i < size ; i++)
      {
         colPref = cols.get(i);
         if(StringUtils.isEmpty(colPref.getWidth()))
         {
            sb.append(DEFAULT_COL_WIDTH);
         }
         else
         {
            sb.append(colPref.getWidth());
         }
         
         if(i < size - 1)
            sb.append(", ");
      }
      
      return sb.toString();
   }
   
   
   /**
    * 
    */
   public void selectAllRows()
   {
      allRowsSelected = true;
      selectAllRows(true);
   }
   
   /**
    * 
    */
   public void unselectAllRows()
   {
      allRowsSelected = false;
      selectAllRows(false);
   }
   
   private void selectAllRows(boolean all)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();

      String selectColumnName = (String) params.get("selectColumnName");
      
      String selectColumnProperty = columnModel.getColumn(selectColumnName).getColumnProperty();
      if(StringUtils.isNotEmpty(selectColumnProperty))
      {
         try
         {
            for (T t : getList())
            {
               Map<String, Object> objectPropertyMap = FacesUtils.getObjectPropertyMapping(t, selectColumnProperty);
               String selectSetterMethod = "set"
                     + ReflectionUtils.toSentenseCase((String) objectPropertyMap
                           .get("property"));

               ReflectionUtils.invokeMethod(objectPropertyMap.get("object"),
                     selectSetterMethod, new Object[]{all}, new Class[]{boolean.class});
            }
         }
         catch(Exception e)
         {
            MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance()
                  .getString("common.unknownError"), e);
         }
      }
      else
      {
         MessageDialog.addMessage(MessageType.ERROR, MessagePropertiesBean.getInstance().getParamString(
               "common.genericDataTable.emptyProperty"), selectColumnName);
      }
   }

   /**
    * @return
    */
   public String getId()
   {
      if(StringUtils.isEmpty(id))
      {
         Random o = new Random();
         id = "DT" + o.nextInt(10000);
      }

      return id;
   }

   /**
    * @return
    */
   public String getExportableColumns()
   {
      StringBuffer columns = new StringBuffer();
      int i = 0;
      for (ColumnPreference column : columnModel.getRenderableLeafColumns())
      {
         if (column.isVisible() && column.isExportable())
         {
            columns.append(i).append(",");
         }
         i++;
      }
      
      String exportCols = columns.toString();
      if (exportCols.length() > 0)
      {
         exportCols = exportCols.substring(0, exportCols.length() - 1);
      }
      
      return exportCols;
   }
   
   /**
    * Method to validate any Filter Column visible , used for aligning Non Filterable
    * column Header
    * 
    * @return
    */
   public boolean isAtleastOneFilterVisible()
   {
      if (null != columnModel)
      {
         List<ColumnPreference> colPref = columnModel.getRenderableLeafColumns();
         for (ColumnPreference pref : colPref)
         {
            if (pref.isVisible() && pref.getColumnDataFilterPopup() != null)
               return true;
         }
      }
      return false;
   }

   public List<T> getList()
   {
      return list;
   }

   public void setList(List<T> list)
   {
      this.list = list;
      this.originalList = this.list;
   }

   public IColumnModel getColumnModel()
   {
      return columnModel;
   }

   public void setColumnModel(IColumnModel columnModel)
   {
      this.columnModel = columnModel;
   }

   public TableDataFilters getDataFilters()
   {
      return dataFilters;
   }

   public TableDataFilterPopup getDataFilterPopup()
   {
      return dataFilterPopup;
   }

   public TableColumnSelectorPopup getColumnSelectorPopup()
   {
      return columnSelectorPopup;
   }
   
   public void setColumnSelectorPopup(TableColumnSelectorPopup columnSelectorPopup)
   {
      this.columnSelectorPopup = columnSelectorPopup;
   }

   public void setAutoFilter(boolean autoFilter)
   {
      this.autoFilter = autoFilter;
   }

   public boolean isShowFilterAtColumns()
   {
      return showFilterAtColumns;
   }

   public UIData getTableUI()
   {
      return tableUI;
   }

   public void setTableUI(UIData tableUI)
   {
      this.tableUI = tableUI;
   }
   
   public boolean isAllRowsSelected()
   {
      return allRowsSelected;
   }

   public HtmlDataTable getTableBinding()
   {
      return tableBinding;
   }

   public void setTableBinding(HtmlDataTable tableBinding)
   {
      this.tableBinding = tableBinding;
   }

   public DataTableRowSelector getRowSelector()
   {
      return rowSelector;
   }

   public void setRowSelector(DataTableRowSelector rowSelector)
   {
      this.rowSelector = rowSelector;
   }

   public ExcelDataTableExporter<T> getExcelDataTableExporter()
   {
      return new ExcelDataTableExporter<T>(this, dataTableExportHandler);
   }
   
   public CSVDataTableExporter<T> getCsvDataTableExporter()
   {
      return new CSVDataTableExporter<T>(this, dataTableExportHandler);
   }

   public DataTableExportHandler<T> getDataTableExportHandler()
   {
      return dataTableExportHandler;
   }

   public void setDataTableExportHandler(DataTableExportHandler<T> dataTableExportHandler)
   {
      this.dataTableExportHandler = dataTableExportHandler;
   }
   
   /**
    * if excel export libraries available in classPath then return true otherwise false.
    * jar:jxl.jar etc.
    * 
    * @return
    */
   public boolean isSupportExportToExcel()
   {
      if (null == hasExcelClassesInClassPath)
      {
         hasExcelClassesInClassPath = ReflectionUtils.isClassInClassPath(EXCEL_SUPPORT_LIB);
      }
      return hasExcelClassesInClassPath;
   }
}
