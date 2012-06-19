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
package org.eclipse.stardust.ui.web.processportal.legacy;

import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractBpmJsfClientSessionListener;
import org.eclipse.stardust.ui.web.viewscommon.common.JSFProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;


public class RADProcessPortalSessionInitializer extends AbstractBpmJsfClientSessionListener
{
   public void intializeSession(ServiceFactory serviceFactory) throws LoginFailedException
   {
      initializeBeans();
      
      JSFProcessExecutionPortal portal = new JSFProcessExecutionPortal();

      SessionContext session = SessionContext.findSessionContext();
      if (null != session)
      {
         session.bind(ProcessPortalConstants.WORKFLOW_FACADE, portal);
      }
   }
}
