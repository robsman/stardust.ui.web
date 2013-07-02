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
package org.eclipse.stardust.ui.web.common.app;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * @author Subodh.Godbole
 *
 */
public class PortalApplicationPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1L;

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
    */
   public void afterPhase(PhaseEvent event)
   {
   }

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
    */
   public void beforePhase(PhaseEvent event)
   {
      if (PhaseId.RESTORE_VIEW == event.getPhaseId())
      {
         PortalApplicationEventScript.getInstance().cleanEventScripts();
      }
   }

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#getPhaseId()
    */
   public PhaseId getPhaseId()
   {
      return PhaseId.RESTORE_VIEW;
   }
}
