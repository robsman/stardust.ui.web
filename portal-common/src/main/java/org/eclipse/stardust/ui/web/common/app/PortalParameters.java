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

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author Subodh.Godbole
 *
 */
public class PortalParameters implements Serializable, InitializingBean
{
   private static final long serialVersionUID = 1L;

   public static final String AUTOMATION_ENABLED = "Carnot.Client.Automation.Enabled";
   private static final Logger trace = LogManager.getLogger(PortalParameters.class);

   private boolean automationEnabled;
   
   /**
    * 
    */
   public PortalParameters()
   {
   }

   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      // Automation Related
      String autoAttr = FacesContext.getCurrentInstance().getExternalContext().getInitParameter(
            AUTOMATION_ENABLED);
      if (StringUtils.isNotEmpty(autoAttr))
      {
         automationEnabled = Boolean.valueOf(autoAttr);
      }
      
      if (trace.isDebugEnabled())
      {
         trace.debug("Automation Enabled = " + automationEnabled);
      }
   }
   
   public boolean isAutomationEnabled()
   {
      return automationEnabled;
   }
}
