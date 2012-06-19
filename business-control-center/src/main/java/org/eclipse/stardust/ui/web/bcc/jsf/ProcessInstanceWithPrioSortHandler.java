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

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractSortHandler;


public class ProcessInstanceWithPrioSortHandler extends AbstractSortHandler
{
   public ProcessInstanceWithPrioSortHandler()
   {
      addSortableAttribute("processInstanceRootOID", 
            ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID);
      addSortableAttribute("processInstanceOID", 
            ProcessInstanceQuery.OID);
      addSortableAttribute("priority", 
            ProcessInstanceQuery.PRIORITY);
      addSortableAttribute("state", 
            ProcessInstanceQuery.STATE);
      addSortableAttribute("processInstanceStartTime", 
            ProcessInstanceQuery.START_TIME);
      addSortableAttribute("terminationTime", 
            ProcessInstanceQuery.TERMINATION_TIME);
      
   }
}
