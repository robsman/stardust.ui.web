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

import org.eclipse.stardust.common.IntKey;

public class Authorization extends IntKey
{
   public static final int FULL = 1;
   public static final int READONLY = 2;
   public static final int NONE = 3;
   
   public static final Authorization full = new Authorization(FULL, "full");
   public static final Authorization readOnly = new Authorization(READONLY, "readOnly");
   public static final Authorization none = new Authorization(NONE, "none");
   
   private Authorization(int key, String name)
   {
      super(key, name);
   }
   
   public static Authorization getAuthorization(int value)
   {
      return (Authorization)getKey(Authorization.class, value);
   }
}
