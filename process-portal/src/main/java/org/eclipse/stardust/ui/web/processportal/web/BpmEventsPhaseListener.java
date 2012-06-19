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
package org.eclipse.stardust.ui.web.processportal.web;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.processportal.view.BpmEventsBridge;



public class BpmEventsPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1L;

   public void beforePhase(PhaseEvent pe)
   {
      // ignore
   }

   public void afterPhase(PhaseEvent pe)
   {
      if (PhaseId.RESTORE_VIEW == pe.getPhaseId())
      {
         // make sure the BPM events bridge is initialized in request scope
         ManagedBeanUtils.getManagedBean(pe.getFacesContext(), BpmEventsBridge.BEAN_NAME);
      }
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.RESTORE_VIEW;
   }
}
