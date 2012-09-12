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
package org.eclipse.stardust.ui.web.admin;

import java.io.Serializable;

import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractBpmJsfClientSessionListener;


/**
 * @author rsauer
 * @version $Revision: 26037 $
 */
public class AdminPortalSessionListener extends AbstractBpmJsfClientSessionListener implements Serializable
{
   private static final long serialVersionUID = -9049732250528133086L;

   public void intializeSession(ServiceFactory serviceFactory) throws LoginFailedException
   {
      initializeBeans();

      WorkflowFacade workflowFacade = new WorkflowFacade();
      workflowFacade.setServiceFactory(serviceFactory);

      SessionContext ctx = SessionContext.findSessionContext();
      if (null != ctx)
      {
         ctx.bind(AdminportalConstants.WORKFLOW_FACADE, workflowFacade);
      }
   }
}
