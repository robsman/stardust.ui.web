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
package org.eclipse.stardust.ui.web.common.app;

import java.io.Serializable;

import org.eclipse.stardust.ui.web.common.util.FacesUtils;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class InternalErrorHandler implements Serializable
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "ippInternalErrorHandler";
   private boolean displayLoginUrl = false;
   private Exception exception = null;

   public static InternalErrorHandler getInstance()
   {
      return (InternalErrorHandler) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public boolean isDisplayLoginUrl()
   {
      return displayLoginUrl;
   }

   public void setDisplayLoginUrl(boolean displayLoginUrl)
   {
      this.displayLoginUrl = displayLoginUrl;
   }

   public Exception getException()
   {
      return exception;
   }

   public void setException(Exception exception)
   {
      this.exception = exception;
   }
}
