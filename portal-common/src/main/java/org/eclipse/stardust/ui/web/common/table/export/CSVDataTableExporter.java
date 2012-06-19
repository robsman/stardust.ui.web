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

import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.IRowModel;

import com.icesoft.faces.component.dataexporter.CSVOutputHandler;


/**
 * @author Subodh.Godbole
 *
 */
public class CSVDataTableExporter<T extends IRowModel> extends CSVOutputHandler
{
   private DataTableExportHelper<T> dataTableExportHelper;
   private boolean used;

   /**
    * @param columnModel
    * @param dataTableExportHandler
    */
   public CSVDataTableExporter(DataTable<T> dataTable, DataTableExportHandler<T> dataTableExportHandler)
   {
      super(DataTableExportHelper.getExportFilePath(ExportType.CSV));
      dataTableExportHelper = new DataTableExportHelper<T>(dataTable, dataTableExportHandler, ExportType.CSV);
   }

   @Override
   public void writeCell(Object output, int col, int row)
   {
      super.writeCell(dataTableExportHelper.getCellText(output, col, row), col, row);
      used = true;
   }

   @Override
   public void writeHeaderCell(String text, int col)
   {
      super.writeHeaderCell(dataTableExportHelper.getHeaderCellText(text, col), col);
   }

   @Override
   public void flushFile()
   {
      dataTableExportHelper.cleanup();
      super.flushFile();
   }

   public boolean isUsed()
   {
      return used;
   }
}
