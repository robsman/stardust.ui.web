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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * @author roland.stamm
 * 
 */
public class ActivitySearchUserModel
{

   private User user;

   private IActivitySearchUserSearchHandler searchHandler;

   /**
    * @param user
    * @param searchHandler
    */
   public ActivitySearchUserModel(User user,
         IActivitySearchUserSearchHandler searchHandler)
   {
      this.user = user;
      this.searchHandler = searchHandler;
   }

   /**
    * @return
    */
   public String selectAction()
   {
      select();
      return null;
   }

   /**
    * 
    */
   private void select()
   {
      searchHandler.searchWorklistFor(this.user);
   }

   public String getFirstName()
   {
      return user.getFirstName();
   }

   public String getLastName()
   {
      return user.getLastName();
   }

   /**
    * @return
    */
   public String getUserDisplayLabel()
   {
      return UserUtils.getUserDisplayLabel(user);
   }
}
