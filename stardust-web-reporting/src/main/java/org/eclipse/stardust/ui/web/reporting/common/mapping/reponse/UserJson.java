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

package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

/**
 * @author Yogesh.Manware
 */
public class UserJson extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String id;
   private String name;

   /**
    * @param user
    * @param displayLabel
    */
   public UserJson(User user, String displayLabel)
   {
      this.name = displayLabel;
      this.id = user.getAccount();
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

}
