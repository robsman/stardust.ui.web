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
package org.eclipse.stardust.ui.web.viewscommon.views.documentsearch;

import java.util.Date;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPickList;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;



/**
 * Filter Handler class for document search table.
 * 
 * @author Vikas.Mishra
 */
public class DocumentSearchFilterHandler extends IppFilterHandler
{
   private static final long serialVersionUID = 1L;

   public void applyFiltering(Query query, List<ITableDataFilter> filters)
   {
      if (filters.isEmpty())
      {
         return;
      }
      FilterAndTerm filter = query.getFilter().addAndTerm();

      for (ITableDataFilter tableDataFilter : filters)
      {
         if (tableDataFilter.isFilterSet())
         {

            if (DocumentSearchBean.DOCUMENT_NAME.equals(tableDataFilter.getName()))
            {
               String filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
               if (StringUtils.isNotEmpty(filterByValue))
               {
                  filter.and(DocumentQuery.NAME.like(QueryUtils.getFormattedString(filterByValue)));
               }
            }

            else if (DocumentSearchBean.DATE_CREATED.equals(tableDataFilter.getName()))
            {
               Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter).getStartValueAsDataType();
               Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter).getEndValueAsDataType();

               if (startTime != null)
                  filter.and(DocumentQuery.DATE_CREATED.greaterOrEqual(DateUtils.convertToGmt(startTime)));

               if (endTime != null)
                  filter.and(DocumentQuery.DATE_CREATED.lessOrEqual(DateUtils.convertToGmt(endTime)));

            }
            else if (DocumentSearchBean.DATE_LAST_MODIFIED.equals(tableDataFilter.getName()))
            {
               Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter).getStartValueAsDataType();
               Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter).getEndValueAsDataType();

               if (startTime != null)
                  filter.and(DocumentQuery.DATE_LAST_MODIFIED.greaterOrEqual(DateUtils.convertToGmt(startTime)));

               if (endTime != null)
                  filter.and(DocumentQuery.DATE_LAST_MODIFIED.lessOrEqual(DateUtils.convertToGmt(endTime)));

            }
            else if (DocumentSearchBean.AUTHOR.equals(tableDataFilter.getName()))
            {
               String filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
               if (StringUtils.isNotEmpty(filterByValue))
               {
                  filter.and(DocumentQuery.OWNER.like(QueryUtils.getFormattedString(filterByValue)));
               }
            }
            else if (DocumentSearchBean.FILE_TYPE.equals(tableDataFilter.getName()))
            {
               String filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
               if (StringUtils.isNotEmpty(filterByValue))
               {
                  filter.and(DocumentQuery.CONTENT_TYPE.like(QueryUtils.getFormattedString(filterByValue)));
               }
            }
            else if (DocumentSearchBean.DOCUMENT_ID.equals(tableDataFilter.getName()))
            {
               String filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
               if (StringUtils.isNotEmpty(filterByValue))
               {
                  filter.and(DocumentQuery.ID.like(QueryUtils.getFormattedString(filterByValue)));
               }
            }
            else if (DocumentSearchBean.DOCUMENT_TYPE.equals(tableDataFilter.getName()))
            {
               List<Object> filterByValues = ((TableDataFilterPickList) tableDataFilter).getSelected();
               if (!CollectionUtils.isEmpty(filterByValues))
               {
                  FilterOrTerm filterOrTerm = filter.addOrTerm();
                  for (Object object : filterByValues)
                  {
                     filterOrTerm.add(DocumentQuery.DOCUMENT_TYPE_ID.isEqual((String) object));
                  }
               }
            }
         }
      }
      query.where(filter);
   }

}
