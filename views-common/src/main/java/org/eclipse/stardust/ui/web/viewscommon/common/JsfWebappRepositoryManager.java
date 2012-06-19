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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.repository.AbstractDocumentServiceRepositoryManager;
import org.eclipse.stardust.engine.core.repository.IRepositoryManager;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class JsfWebappRepositoryManager extends AbstractDocumentServiceRepositoryManager implements IRepositoryManager.Factory
{

   protected DocumentManagementService getDocumentService()
   {
      ServiceFactory sFactory = SessionContext.findSessionContext().getServiceFactory();
      return sFactory != null ? sFactory.getDocumentManagementService() : null;
   }

   protected String getPartitionId()
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();
      User user = sessionCtx.isSessionInitialized() ? sessionCtx.getUser() : null;
      return user != null ? user.getPartitionId() : null;
   }

   public IRepositoryManager getRepositoryManager()
   {
      return instance;
   }
   
   private final static IRepositoryManager instance = new JsfWebappRepositoryManager();
}
