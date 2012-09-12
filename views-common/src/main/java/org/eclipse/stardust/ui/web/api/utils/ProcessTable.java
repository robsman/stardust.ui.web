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
package org.eclipse.stardust.ui.web.api.utils;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Subodh.Godbole
 *
 */
public final class ProcessTable
{
   private ProcessTableHelper processTableHelper;
   private SearchHandler searchHandler;

   /**
    * @param piQuery
    * @param preferenceModuleId
    * @param preferenceViewId
    */
   public ProcessTable(ProcessInstanceQuery piQuery, String preferenceModuleId, String preferenceViewId)
   {
      this.searchHandler = new SearchHandler(piQuery);

      processTableHelper = new ProcessTableHelper();
      processTableHelper.initializeProcessTable(preferenceModuleId, preferenceViewId);
      processTableHelper.getProcessTable().setISearchHandler(searchHandler);

      initialize();
   }

   /**
    * 
    */
   public void initialize()
   {
      processTableHelper.getProcessTable().initialize();
   }

   /**
    * 
    */
   public void refresh()
   {
      processTableHelper.getProcessTable().refresh();
   }

   /**
    * @param keepPageIndex
    */
   public void refresh(boolean keepPageIndex)
   {
      processTableHelper.getProcessTable().refresh(keepPageIndex);
   }

   /**
    * @return
    */
   public IColumnModel getColumnModel()
   {
      return processTableHelper.getProcessTable().getColumnModel();
   }

   public ProcessTableHelper getProcessTableHelper()
   {
      return processTableHelper;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static class SearchHandler extends IppSearchHandler<ProcessInstance>
   {
      private static final long serialVersionUID = 1L;

      private ProcessInstanceQuery piQuery;

      /**
       * @param query
       */
      public SearchHandler(ProcessInstanceQuery piQuery)
      {
         this.piQuery = piQuery;
      }

      @Override
      public Query createQuery()
      {
         return piQuery;
      }

      @Override
      public QueryResult<ProcessInstance> performSearch(Query query)
      {
         return ServiceFactoryUtils.getQueryService().getAllProcessInstances((ProcessInstanceQuery) query);
      }
   }
}
