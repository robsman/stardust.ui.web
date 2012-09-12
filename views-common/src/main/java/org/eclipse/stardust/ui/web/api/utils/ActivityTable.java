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

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Subodh.Godbole
 *
 */
public final class ActivityTable
{
   private ActivityTableHelper activityTableHelper;
   private SearchHandler searchHandler;

   /**
    * @param aiQuery
    * @param preferenceModuleId
    * @param preferenceViewId
    */
   public ActivityTable(ActivityInstanceQuery aiQuery, String preferenceModuleId, String preferenceViewId)
   {
      this.searchHandler = new SearchHandler(aiQuery);

      activityTableHelper = new ActivityTableHelper();
      activityTableHelper.initActivityTable(preferenceModuleId, preferenceViewId);
      activityTableHelper.getActivityTable().setISearchHandler(searchHandler);

      initialize();
   }

   /**
    * 
    */
   public void initialize()
   {
      activityTableHelper.getActivityTable().initialize();
   }

   /**
    * 
    */
   public void refresh()
   {
      activityTableHelper.getActivityTable().refresh();
   }

   /**
    * @param keepPageIndex
    */
   public void refresh(boolean keepPageIndex)
   {
      activityTableHelper.getActivityTable().refresh(keepPageIndex);
   }

   /**
    * @return
    */
   public IColumnModel getColumnModel()
   {
      return activityTableHelper.getActivityTable().getColumnModel();
   }

   public ActivityTableHelper getActivityTableHelper()
   {
      return activityTableHelper;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static class SearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private static final long serialVersionUID = 1L;

      private ActivityInstanceQuery aiQuery;

      /**
       * @param query
       */
      public SearchHandler(ActivityInstanceQuery aiQuery)
      {
         this.aiQuery = aiQuery;
      }

      @Override
      public Query createQuery()
      {
         return aiQuery;
      }

      @Override
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         return ServiceFactoryUtils.getQueryService().getAllActivityInstances((ActivityInstanceQuery) query);
      }
   }
}
