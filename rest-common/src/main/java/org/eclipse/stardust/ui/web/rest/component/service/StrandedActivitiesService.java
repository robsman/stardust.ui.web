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
package org.eclipse.stardust.ui.web.rest.component.service;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.component.util.StrandedActivitiesUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.springframework.stereotype.Component;

@Component
public class StrandedActivitiesService
{

   @Resource
   private StrandedActivitiesUtils strandedActivitiesUtil;
   
   public QueryResultDTO getStrandedActivities(DataTableOptionsDTO options)
   {
      QueryResult<ActivityInstance> queryResult =  strandedActivitiesUtil.getStrandedActivities(options);
      return ActivityTableUtils.buildTableResult(queryResult,MODE.ACTIVITY_TABLE);
   }

}
