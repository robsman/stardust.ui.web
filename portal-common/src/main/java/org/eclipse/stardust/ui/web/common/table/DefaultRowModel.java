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
package org.eclipse.stardust.ui.web.common.table;

import java.io.Serializable;

/**
 * @author Subodh.Godbole
 * 
 */
public abstract class DefaultRowModel implements IRowModel, Serializable
{
   private static final long serialVersionUID = 1739220451264846586L;
   // in case if exception,we can keep exception info here
   private Throwable cause;

   // Default will be true. Implies this User Object is loaded/created successfully
   private boolean loaded = true;

   public Throwable getCause()
   {
      return cause;
   }

   public String getStyleClass()
   {
      return "";
   }

   public boolean isLoaded()
   {
      return loaded;
   }

   public void setCause(Throwable cause)
   {
      this.cause = cause;
   }

   public void setLoaded(boolean loaded)
   {
      this.loaded = loaded;
   }
}
