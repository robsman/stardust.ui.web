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
package org.eclipse.stardust.ui.web.viewscommon.wizard;

public class WizardPageEvent
{
   private final WizardPageEventType type;
   private boolean vetoed;
   private final WizardFlowEvent flowEvent;

   public WizardPageEvent(WizardPageEventType type, WizardFlowEvent flowEvent)
   {
      this.type = type;
      this.flowEvent = flowEvent;
   }
   public WizardPageEvent(WizardPageEventType type)
   {
      this.type = type;
      this.flowEvent = null;
   }
   
   public static enum WizardPageEventType {
      PAGE_ACTIVATE, PAGE_DEACTIVATE,PAGE_ONLOAD;
   }

   public boolean isVetoed()
   {
      return vetoed;
   }

   public void setVetoed(boolean vetoed)
   {
      this.vetoed = vetoed;
   }

   public WizardPageEventType getType()
   {
      return type;
   }

   public WizardFlowEvent getFlowEvent()
   {
      return flowEvent;
   }

}
