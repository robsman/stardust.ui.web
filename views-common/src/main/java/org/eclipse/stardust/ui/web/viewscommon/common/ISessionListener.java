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

import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

/** 
 * Implement this interface if you want to register a session listener in the {@link SessionContext}.
 * The session listener will be called if a new session is created.
 * @see org.eclipse.stardust.ui.web.viewscommon.common.listener.IBpmClientSessionListener */
public interface ISessionListener
{
   void initializeSession();
}
