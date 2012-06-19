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

import java.io.Serializable;
import java.util.List;

import javax.faces.event.ActionEvent;

public interface PriorityOverviewEntry
{
   
   String getName();

   int getThresholdState();
   
   Priorities getPriorities();
   Priorities getCriticalPriorities();
   
   String getDefaultPerformerName();
   
   List getChildren();
   
   String getType();

   void doPriorityAction(ActionEvent event);

   public static class Priorities implements Serializable
   {
      private final static long serialVersionUID = 1l;
      
      private long lowPriority;
      private long highPriority;
      private long normalPriority;
      public long getLowPriority()
      {
         return lowPriority;
      }
      public void setLowPriority(long lowPriority)
      {
         this.lowPriority = lowPriority > 0 ? lowPriority : 0;
      }
      public long getHighPriority()
      {
         return highPriority;
      }
      public void setHighPriority(long highPriority)
      {
         this.highPriority = highPriority > 0 ? highPriority : 0;
      }
      public long getNormalPriority()
      {
         return normalPriority;
      }
      public void setNormalPriority(long normalPriority)
      {
         this.normalPriority = normalPriority > 0 ? normalPriority : 0;
      }
      public long getTotalPriority()
      {
         return highPriority + normalPriority + lowPriority;
      }
   }
   
}
