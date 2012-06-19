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

import javax.faces.FacesException;

public class InvalidServiceException extends FacesException
{
   private String summary;

   public InvalidServiceException(String message)
   {
      super(message);
   }

   public InvalidServiceException(String summary, String detail)
   {
      super(detail);
      this.summary = summary;
   }

   public String getSummary()
   {
      return summary;
   }

   public String getDetail()
   {
      return getMessage();
   }

   public void setSummary(String summary)
   {
      this.summary = summary;
   }
}
