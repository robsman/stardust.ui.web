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
package org.eclipse.stardust.ui.web.bcc.jsf;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractSortHandler;


public class ActivityInstanceWithPrioSortHandler extends AbstractSortHandler
{
   public ActivityInstanceWithPrioSortHandler()
   {
      addSortableAttribute("activityOID", 
            ActivityInstanceQuery.OID);
      addSortableAttribute("processOID", 
            ActivityInstanceQuery.PROCESS_INSTANCE_OID);
      addSortableAttribute("stateName", 
            ActivityInstanceQuery.STATE);
      addSortableAttribute("priority", 
            ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY);
      addSortableAttribute("startTime", 
            ActivityInstanceQuery.START_TIME);
      addSortableAttribute("lastModificationTime", 
            ActivityInstanceQuery.LAST_MODIFICATION_TIME);
   }
}
