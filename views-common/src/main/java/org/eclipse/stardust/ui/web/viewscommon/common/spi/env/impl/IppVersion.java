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

import org.eclipse.stardust.ui.web.common.spi.env.Version;


/**
 * @author Subodh.Godbole
 *
 */
public class IppVersion implements Version
{
   private static final long serialVersionUID = 1L;

   private org.eclipse.stardust.common.config.Version currentVersion;
   
   /**
    * 
    */
   public IppVersion()
   {
      currentVersion = CurrentVersion.getBuildVersion();
   }

   public String getShortString()
   {
      return currentVersion.toShortString();
   }

   public String getCompleteString()
   {
      if (null != currentVersion)
      {
         return currentVersion.toCompleteString();
      }
      else
      {
         // By default returning 'dev' mode
         return "dev";
      }
   }
}