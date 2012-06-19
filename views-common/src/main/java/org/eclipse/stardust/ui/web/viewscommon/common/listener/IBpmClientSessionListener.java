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
package org.eclipse.stardust.ui.web.viewscommon.common.listener;

import org.eclipse.stardust.common.security.authentication.LoginFailedException;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;


/**
 * Beans that should be created/called if a new session is created have to implement this interface.
 * Furthermore you have to define the bean names as a context param <code>carnot.SESSION_LISTENERS</code> 
 * in the <strong>web.xml</strong>. If you have more than one bean seperate the bean names with commas.
 * @author rsauer
 * @version $Revision$
 */
public interface IBpmClientSessionListener
{
   /** Method is called when a new session is created.
    * @param serviceFactory ServiceFactory that was created by the login credentials
    * @throws LoginFailedException If you want to prevent the login process
    */
   void intializeSession(ServiceFactory serviceFactory) throws LoginFailedException;
}
