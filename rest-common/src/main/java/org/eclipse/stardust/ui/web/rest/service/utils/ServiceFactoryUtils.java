/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.utils;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ServiceFactoryUtils
{
   @Resource
   private SessionContext sessionContext;

   /**
    * @return
    */
   public ServiceFactory getServiceFactory()
   {
      SessionContext sessionContext = getSessionContext();
      return (sessionContext != null) ? sessionContext.getServiceFactory() : null;
   }

   /**
    * @return
    */
   public WorkflowService getWorkflowService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getWorkflowService() : null;
   }

   /**
    * @return
    */
   public QueryService getQueryService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getQueryService() : null;
   }

   /**
    * @return
    */
   public DocumentManagementService getDocumentManagementService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null)
            ? serviceFactory.getDocumentManagementService()
            : null;
   }

   /**
    * @return
    */
   public AdministrationService getAdministrationService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getAdministrationService() : null;
   }

   /**
    * @return
    */
   public UserService getUserService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getUserService() : null;
   }

   /**
    * @return the sessionContext
    */
   public SessionContext getSessionContext()
   {
      return sessionContext;
   }
}
