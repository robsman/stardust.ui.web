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

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


/**
 * This is a workarround for a security leck in IceFaces 1.7.2,
 * Build 17, Revision 17749. <br/>
 * With the "block" servlet it is possible to fetch
 * files under WEB-INF. With the following request it is possible to
 * read the web.xml and other files under WEB-INF.
 * <code><pre>
 * http://localhost:8080/myProject/block/WEB-INF/web.xml
 * </pre></code>
 * <p/>
 * To protect your WEB-INF directory you have to regist this PhaseListener
 * for the RESTORE_VIEW Phase in the faces-config.xml. Here is an Example<br/>
 * <br/>
 * <code><pre>
 * <lifecycle>
 *      <phase-listener>
 *          org.eclipse.stardust.ui.web.common.app.BlockSaveListener
 *      </phase-listener>
 * </lifecycle>
 * </pre></code>
 * <p/>
 */
public class BlockSaveListener implements PhaseListener
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(BlockSaveListener.class);

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
    */
   public void afterPhase(PhaseEvent event)
   {}

   /* (non-Javadoc)
    * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
    */
   public void beforePhase(PhaseEvent event)
   {
      FacesContext facesContext = event.getFacesContext();
      HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
      String url = request.getRequestURL().toString();
      if (url.contains("/block/WEB-INF/"))
      {
         try
         {
            String[] s = url.split("block/WEB-INF/");
            facesContext.getExternalContext().redirect(s[0]);
         }
         catch (IOException e)
         {
            trace.error("Error Occurred while protecting WEB-INF directory", e);
            FacesContext.getCurrentInstance().responseComplete();
         }
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
