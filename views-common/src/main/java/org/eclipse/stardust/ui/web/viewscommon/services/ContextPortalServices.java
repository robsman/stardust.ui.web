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
package org.eclipse.stardust.ui.web.viewscommon.services;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;


public class ContextPortalServices
{
   public static WorkflowService getWorkflowService()
   {
      SessionContext sessioncontext = SessionContext.findSessionContext();
      if (sessioncontext.isSessionInitialized())
         return sessioncontext.getServiceFactory().getWorkflowService();
      else
         return null;
   }

   public static DocumentManagementService getDocumentManagementService()
   {
      SessionContext sessioncontext = SessionContext.findSessionContext();
      if (sessioncontext.isSessionInitialized())
         return sessioncontext.getServiceFactory().getDocumentManagementService();
      else
         return null;
   }

   public static QueryService getQueryService()
   {
      SessionContext sessioncontext = SessionContext.findSessionContext();
      if (sessioncontext.isSessionInitialized())
         return sessioncontext.getServiceFactory().getQueryService();
      else
         return null;
   }

   
   
   public static User getUser()
   {
      return getWorkflowService().getUser();
   }
}
