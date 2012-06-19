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
package org.eclipse.stardust.ui.web.processportal.common;

import java.util.Set;

import org.eclipse.stardust.engine.api.query.WorklistQuery;


/**
 * @author Shrikant.Gangal
 * 
 * This is an extension of the default implementation of assembly line contract
 * to additionally provide ordering by activity criticality.
 * 
 */
public class CriticalityAwareAssemblyLineActivityProvider extends DefaultAssemblyLineActivityProvider
{
   @Override
   protected WorklistQuery createWorklistQuery(Set participantIds, boolean outline)
   {
      //Get the default worklist query.
      WorklistQuery query = super.createWorklistQuery(participantIds, outline);
      
      /* Add the following 'order by' criteria to default worklist query
         Order by
           Descending activity criticality (first and main criterion)
           Descending priority of corresponding process (second criterion)
           Descending time elapsed since activity creation (third criterion)*/
      query.orderBy(WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY, false);
      query.orderBy(WorklistQuery.PROCESS_INSTANCE_PRIORITY, false);
      query.orderBy(WorklistQuery.START_TIME, true);
      
      return query;
   }
}
