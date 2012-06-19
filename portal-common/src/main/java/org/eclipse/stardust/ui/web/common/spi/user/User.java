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
public interface User extends Serializable
{
   /**
    * @returns the unique id of the user. The id must be unique among tenants. Cannot be
    *          null.
    */
   public String getUID();

   /**
    * Important: users of this interface should assume that LoginName COULD change during
    * the lifetime of the user. Login name may not be unique among tenants.
    * 
    * @returns the name used at login time for this user. Cannot be null.
    */
   public String getLoginName();

   /**
    * Returns the first name of the user. Cannot be null.
    * 
    * @return
    */
   public String getFirstName();

   /**
    * Returns the last name of the user. If null, the full name is assumed to be returned
    * by getFirstName.
    */
   public String getLastName();

   /**
    * Returns true if the user is in the role specified.
    */
   public boolean isInRole(String role);

   /**
    * Returns true if the user is an administrator
    */
   public boolean isAdministrator();
   
   /**
    * Returns the user name in a format one desires for their application.
    */
   public String getDisplayName();

}