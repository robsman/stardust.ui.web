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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl;

import org.eclipse.stardust.ui.web.common.spi.env.CopyrightInfo;

/**
 * @author Subodh.Godbole
 *
 */
public class IppCopyrightInfo implements CopyrightInfo
{
   private static final long serialVersionUID = 1L;

   private String message;

   /**
    * 
    */
   public IppCopyrightInfo()
   {
      message = CurrentVersion.getCopyrightMessage();
   }
   
   public String getMessage()
   {
      return message;
   }
}
