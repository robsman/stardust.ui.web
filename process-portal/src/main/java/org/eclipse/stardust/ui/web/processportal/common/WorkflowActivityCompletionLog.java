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

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;

/**
 * @author Subodh.Godbole
 *
 */
public class WorkflowActivityCompletionLog implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   final private ActivityInstance completedActivity;
   final private ActivityInstance nextActivity;
   final private boolean success;
   final private boolean closeViewAndProceed;
   final private boolean delayViewClose;

   /**
    * @param completedActivity
    * @param nextActivity
    * @param success
    * @param closeViewAndProceed
    */
   public WorkflowActivityCompletionLog(ActivityInstance completedActivity, ActivityInstance nextActivity,
         boolean success, boolean closeViewAndProceed, boolean delayViewClose)
   {
      super();
      this.completedActivity = completedActivity;
      this.nextActivity = nextActivity;
      this.success = success;
      this.closeViewAndProceed = closeViewAndProceed;
      this.delayViewClose = delayViewClose;
   }

   public ActivityInstance getCompletedActivity()
   {
      return completedActivity;
   }

   public ActivityInstance getNextActivity()
   {
      return nextActivity;
   }

   public boolean isSuccess()
   {
      return success;
   }

   public boolean isCloseViewAndProceed()
   {
      return closeViewAndProceed;
   }

   public boolean isDelayViewClose()
   {
      return delayViewClose;
   }
   
}
