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

import org.eclipse.stardust.ui.web.bcc.jsf.PriorityOverviewEntry.Priorities;


public class ThresholdProvider implements IThresholdProvider
{

   private int getThreshold(Priorities priorities)
   {
      int thresholdState = priorities.getLowPriority() + 
         priorities.getNormalPriority() > 0 ? 
            WARNING_THRESHOLD_STATE : NO_THRESHOLD_STATE;
      return priorities.getHighPriority() > 0 ?
         CRITICAL_THRESHOLD_STATE : thresholdState;
   }
   
   public int getActivityThreshold(ActivityDefinitionWithPrio adwp)
   {
      return getThreshold(adwp.getCriticalPriorities());
   }

   public int getProcessThreshold(ProcessDefinitionWithPrio pdwp)
   {
      return getThreshold(pdwp.getCriticalPriorities());
   }

}
