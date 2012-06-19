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
package org.eclipse.stardust.ui.web.common.spi.user;

import java.io.Serializable;

/**
 * @author Subodh.Godbole
 *
 */
public interface UserProvider extends Serializable
{
   /**
    * Returns the logged-in or context user
    * @return
    */
   public User getUser();

   /**
    * Returns true if using external authentication 
    * @return
    */
   public boolean isExternalAuthentication();
   
   public boolean isExternalAuthorization();
}
