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

package org.eclipse.stardust.ui.web.processportal.service.rest;

import java.util.Map;

/**
 * @author Subodh.Godbole
 *
 */
public class DataException extends Exception
{
   private static final long serialVersionUID = 1L;

   private Map<String, Throwable> errors;

   /**
    * @param errors
    */
   public DataException(Map<String, Throwable> errors)
   {
      this.errors = errors;
   }

   public Map<String, Throwable> getErrors()
   {
      return errors;
   }
}
