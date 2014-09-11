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
package org.eclipse.stardust.ui.web.bcc;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;

public class ActivitySearchHandler extends IppSearchHandler<ActivityInstance>
{
   private static final long serialVersionUID = 1L;
   private Set<Long> oids;

   @Override
   public Query createQuery()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      FilterTerm filter = query.getFilter();
      if (oids != null)
      {
         if (!oids.isEmpty())
         {
            FilterTerm orTerm = filter.addOrTerm();
            for (Iterator iter = oids.iterator(); iter.hasNext();)
            {
               Long oid = (Long) iter.next();
               orTerm.add(ActivityInstanceQuery.OID.isEqual(oid.longValue()));
            }
         }
         else
         {
            filter.add(ActivityInstanceQuery.OID.isNull());
         }
      }

      return query;
   }

   public Set<Long> getOids()
   {
      return oids;
   }

   public void setOids(Set<Long> oids)
   {
      this.oids = oids;
   }

   @Override
   public QueryResult<ActivityInstance> performSearch(Query query)
   {
      QueryResult<ActivityInstance> result = WorkflowFacade.getWorkflowFacade().getAllActivityInstances(
            (ActivityInstanceQuery) query);
      return result;
   }
}
