/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.beans.spring.portal;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.User;

/**
 * @author Yogesh.Manware
 * 
 */
public interface IUserService extends ISearchHandler
{
   /**
    * @param searchValue
    * @param onlyActive
    * @param maxMatches
    * @return
    */
   public List<User> searchUsers(String searchValue, boolean onlyActive, int maxMatches);

   /**
    * @param user
    * @return
    */
   public String getUserDisplayLabel(User user);
}
