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
package org.eclipse.stardust.ui.web.common.util;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


/**
 * @author Subodh.Godbole
 *
 */
public class LogPhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger trace = LogManager.getLogger(LogPhaseListener.class);

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#getPhaseId()
    */
   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
    */
   public void beforePhase(PhaseEvent arg0)
   {
      trace.debug("[LogPhaseListener] Before Phase " + arg0.getPhaseId());
   }

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
    */
   public void afterPhase(PhaseEvent arg0)
   {
      trace.debug("[LogPhaseListener] After Phase " + arg0.getPhaseId());
   }
}
