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
package org.eclipse.stardust.ui.web.common.table.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.column.ColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.IRowModel;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;

import com.icesoft.faces.util.CoreUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class DataTableExportHelper<T extends IRowModel>
{
   private final Logger trace = LogManager.getLogger(DataTableExportHelper.class);

   private final static String EXPORT_FOLDER = "export";
   private final static String EXPORT_FILE_PREFIX = "export_";

   private DataTable<T> dataTable;
   private DataTableExportHandler<T> dataTableExportHandler;
   private ExportType exportType;

   private List<String> exportableCols;

   /**
    * @param columnModel
    * @param dataTableExportHandler
    */
   public DataTableExportHelper(DataTable<T> dataTable, DataTableExportHandler<T> dataTableExportHandler,
         ExportType exportType)
   {
      this.dataTable = dataTable;
      this.dataTableExportHandler = dataTableExportHandler;
      this.exportType = exportType;
   }

   /**
    * @param output
    * @param col
    * @param row
    * @return
    */
   public Object getCellText(Object output, int col, int row)
   {
      ColumnPreference columnPreference = getColumnAtIndex(col);
      T rowObject = dataTable.getList().get(row);
      return getCellText(columnPreference, rowObject, output);
   }

   /**
    * @param text
    * @param col
    * @return
    */
   public String getHeaderCellText(String text, int col)
   {
      ColumnPreference columnPreference = getColumnAtIndex(col);
      return getHeaderCellText(columnPreference, text);
   }

   /**
    * 
    */
   public void cleanup()
   {
      exportableCols = null;
   }

   /**
    * @param index
    * @return
    */
   private ColumnPreference getColumnAtIndex(int index)
   {
      return dataTable.getColumnModel().getRenderableLeafColumns().get(getExportableColumnsAtIndex(index));
   }

   /**
    * ICEfaces is not returning correct index when "includeColumns" is specified
    * If includeColumns="1,2,3" Then ICEfaces calls writeCell or writeHeaderCell with "0,1,2" as columns
    * To work around this we need this function
    * @param index
    * @return
    */
   private int getExportableColumnsAtIndex(int index)
   {
      if (null == exportableCols)
      {
         String cols = dataTable.getExportableColumns();
         exportableCols = new ArrayList<String>(StringUtils.splitAndKeepOrder(cols, ","));
      }

      return Integer.parseInt(exportableCols.get(index));
   }

   /**
    * @param column
    * @param text
    * @return
    */
   private String getHeaderCellText(ColumnPreference column, String text)
   {
      // Default Header Text
      String headerText = column.getColumnTitle();

      if (null != dataTableExportHandler)
      {
         headerText = dataTableExportHandler.handleHeaderCellExport(exportType, column, headerText);
      }

      return headerText;
   }

   /**
    * @param column
    * @param row
    * @param output
    * @return
    */
   private Object getCellText(ColumnPreference column, T row, Object output)
   {
      Object value = null;

      // With ICEfaces EE JARs it's observed that "output" is always blank
      // But with Community Edition JARs "output" has value as seen on UI
      // So to cover this issue with EE JARs invoke property mapping and get value directly for all columns
      try
      {
         Object object = ColumnModel.resolvePropertyAndInvokeGetter(row, column.getColumnProperty());
         if (null == object)
         {
            value = "";
         }
         // Added for I18N of boolean descriptors
         else if (object instanceof Boolean)
         {
            MessagePropertiesBean props = MessagePropertiesBean.getInstance();
            value = (Boolean) object ? props.getString("common.true") : props.getString("common.false");
         }
         else if (object instanceof Date)
         {
            PortalApplication portalApp = PortalApplication.getInstance();
            value = DateUtils.format((Date) object, column.getColumnFormatter(), portalApp.getLocaleObject(),
                  portalApp.getTimeZone());
         }
         else
         {
            value = object;
         }
         
         // Convert to String Representation as Required by ICEfaces
         value = null != value ? value.toString() : "";
      }
      catch (Exception e)
      {
         trace.error("Unable to export Column = " + column.getColumnName(), e);
      }

      // If Handler is register, invoke the same and use value returned by it
      if (null != dataTableExportHandler)
      {
         value = dataTableExportHandler.handleCellExport(exportType, column, row, value);
      }
      
      return value;
   }

   /**
    * @param exportType
    * @return
    */
   public static String getExportFilePath(ExportType exportType)
   {
      String exportDirPath = CoreUtils.getRealPath(FacesContext.getCurrentInstance(), (File.separator + EXPORT_FOLDER));
      File exportDir = new File(exportDirPath);
      if (!exportDir.exists())
      {
         exportDir.mkdirs();
      }

      String exportFilePath = exportDir + File.separator + EXPORT_FILE_PREFIX + new Date().getTime()
            + exportType.getExtension();
      return exportFilePath;
   }
}
