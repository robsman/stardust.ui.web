/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.dto.LogEntryDetails;
import org.eclipse.stardust.engine.api.query.LogEntryQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.LogEntry;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.LogEntryDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.springframework.stereotype.Component;

@Component
public class OverviewUtils
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public QueryResultDTO getAllLogEntries(DataTableOptionsDTO options)
   {
      List<LogEntryDTO> logEntries = new ArrayList<LogEntryDTO>();

      LogEntryQuery query = new LogEntryQuery();

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      query.setPolicy(subsetPolicy);

      applySorting(query, options);

      QueryResult<LogEntry> queryResult = serviceFactoryUtils.getQueryService().getAllLogEntries((LogEntryQuery) query);

      for (LogEntry logEntry : queryResult)
      {
         LogEntryDetails logEntryDetails = (LogEntryDetails) logEntry;
         LogEntryDTO logEntryDTO = new LogEntryDTO(logEntryDetails);
         logEntries.add(logEntryDTO);
      }

      QueryResultDTO queryResultDTO = new QueryResultDTO();
      queryResultDTO.list = logEntries;
      queryResultDTO.totalCount = queryResult.getTotalCount();

      return queryResultDTO;
   }

   public void applySorting(Query query, DataTableOptionsDTO options)
   {
      if ("timeStamp".equals(options.orderBy))
      {
         query.orderBy(LogEntryQuery.STAMP, options.asc);
      }

   }

}
