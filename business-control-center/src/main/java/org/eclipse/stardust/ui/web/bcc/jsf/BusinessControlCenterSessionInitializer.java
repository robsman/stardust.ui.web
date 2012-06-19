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
package org.eclipse.stardust.ui.web.bcc.jsf;

import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractBpmJsfClientSessionListener;


public class BusinessControlCenterSessionInitializer
      extends AbstractBpmJsfClientSessionListener
{
   
   public void intializeSession(ServiceFactory service) throws LoginFailedException
   {
      // Initialization moved to Perspective Event Handler
      // This class can be removed. In that case web.xml also needs to be modified
   }
}
