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

import org.eclipse.stardust.ui.web.common.column.ColumnPreference;

/**
 * @author Subodh.Godbole
 *
 */
public interface DataTableExportHandler<T>
{
   /**
    * Called for getting Header Cell Text.
    * @param exportType Type of Export
    * @param column Object representing current column
    * @param text Framework calculated Header Text. This comes from 'column.getColumnTitle()'
    * @return Header Text in case if it's different from 'text', otherwise return 'text' itself.
    *         Text returned from this function will be used as Cell Header Text
    */
   String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text);

   /**
    * @param exportType Type of Export
    * @param column Object representing current column
    * @param row Object representing current row
    * @param value Framework calculated Column Value. This needs to be changed when column value is not 'literal'
    * @return Column Value in case if it's different from value, otherwise return 'value' itself.
    *         Value returned from this function will be used in Cell.
    */
   Object handleCellExport(ExportType exportType, ColumnPreference column, T row, Object value);
}
