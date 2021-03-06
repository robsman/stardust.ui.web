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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;



/**
 * 
 * @author roland.stamm
 * 
 */
public class ActivitySearchModel implements IQueryBuilder
{
   private IQueryBuilder queryBuilder;

   private String name;

   private String id;
   
   private Map<String,Object> params;

   
   /**
    * @param id
    * @param name
    * @param queryBuilder
    */
   public ActivitySearchModel(String id, String name, IQueryBuilder queryBuilder)
   {
      super();
      this.queryBuilder = queryBuilder;
      this.name = name;
      this.id = id;
   }
   /**
    * @param id
    * @param name
    * @param queryBuilder
    */
   public ActivitySearchModel(String id, String name, IQueryBuilder queryBuilder, Map<String,Object> params)
   {
      super();
      this.queryBuilder = queryBuilder;
      this.name = name;
      this.id = id;
      this.params = params;
   }

   /**
    * @return
    */
   public String select()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put(Query.class.getName(), this.createQuery());
      params.put("id", getId());
      params.put("name", getName());
      if(ProcessPortalConstants.ID_ALL_RESUBMISSION_ACTIVITY_INSTANCES.equals(getId()))
      {
         params.put("showResubmitLink", true);
         params.put("showResubmissionTime", true);
      }
      PPUtils.openWorklistView("id=" + getId(), params);

      PPUtils.selectWorklist(null);
      return null;
   }
   
   /**
    */
   public String selectHTML5()
   {
      Map<String, Object> params = getParams();
      PPUtils.openWorklistViewHTML5("id=" + getId(), params);
      PPUtils.selectWorklist(null);
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.processportal.launchpad.IQueryBuilder#createQuery()
    */
   public Query createQuery()
   {
      return queryBuilder.createQuery();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.processportal.launchpad.IQueryBuilder#executeCountQuery()
    */
   public QueryResult executeCountQuery()
   {
      return queryBuilder.executeCountQuery();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.processportal.launchpad.IQueryBuilder#getCountQueryResult()
    */
   public QueryResult getCountQueryResult()
   {
      return queryBuilder.getCountQueryResult();
   }

   public String getTotalCount()
   {
      return Long.toString(queryBuilder.getCountQueryResult().getTotalCount());
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }
   
   public Map<String, Object> getParams()
   {
      return params;
   }
}
