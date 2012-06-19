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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;


/**
 * @author anair
 * @version $Revision: $
 */
public class ServiceFactoryUtils
{
   /**
    * @return
    */
   public static ServiceFactory getServiceFactory()
   {
      SessionContext sessionContext = getSessionContext();
      return (sessionContext != null) ? sessionContext.getServiceFactory() : null;
   }
   /**
    * method to get SessionContext
    * @return
    */
   public static SessionContext getSessionContext()
   {
      return  SessionContext.findSessionContext();
   }

   /**
    * @return
    */
   public static WorkflowService getWorkflowService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getWorkflowService() : null;
   }

   /**
    * @return
    */
   public static QueryService getQueryService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getQueryService() : null;
   }
   
   /**
    * @return
    */
   public static DocumentManagementService getDocumentManagementService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getDocumentManagementService() : null;
   }

   /**
    * @return
    */
   public static AdministrationService getAdministrationService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getAdministrationService() : null;
   }
   
   /**
    * @return
    */
   public static AbstractProcessExecutionPortal getProcessExecutionPortal()
   {
      SessionContext session = SessionContext.findSessionContext();
      if (null != session)
      {
         return (AbstractProcessExecutionPortal)session.lookup(ProcessPortalConstants.WORKFLOW_FACADE);
      }
      
      return null;
   }
   
   /**
    * @return
    */
   public  static UserService getUserService()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getUserService() : null;
   }
   
   /**
    * @return WorkflowUserSessionId
    */
   public static String getWorkflowUserSessionId()
   {
      ServiceFactory serviceFactory = getServiceFactory();
      return (serviceFactory != null) ? serviceFactory.getSessionId() : null;
   }
   
   /**
    * @return QualityCheckAdminServiceFacade
    */
   public static QualityAssuranceAdminServiceFacade getQualityCheckAdminServiceFacade()
   {
      return new QualityAssuranceAdminServiceFacade(getServiceFactory());
   }
}
