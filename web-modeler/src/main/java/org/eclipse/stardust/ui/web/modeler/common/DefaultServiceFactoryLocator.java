/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.common;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

public class DefaultServiceFactoryLocator implements ServiceFactoryLocator
{
   /**
    *
    */
   @Resource
   private SessionContext sessionContext;

   @Override
   public ServiceFactory get()
   {
      return sessionContext.getServiceFactory();
   }
}
