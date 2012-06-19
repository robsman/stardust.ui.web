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
import org.eclipse.stardust.ui.web.common.spi.env.RuntimeEnvironmentInfoProvider;
import org.eclipse.stardust.ui.web.common.spi.env.Version;

/**
 * @author Subodh.Godbole
 *
 */
public class IppRuntimeEnvironmentInfoProvider implements RuntimeEnvironmentInfoProvider
{
   private static final long serialVersionUID = 1L;

   private IppVersion version;
   private CopyrightInfo copyrightInfo;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.env.RuntimeEnvironmentProvider#getVersion()
    */
   public Version getVersion()
   {
      if (null == version)
      {
         version = new IppVersion();
      }

      return version;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.spi.env.RuntimeEnvironmentInfoProvider#getCopyrightInfo()
    */
   public CopyrightInfo getCopyrightInfo()
   {
      if (null == copyrightInfo)
      {
         copyrightInfo = new IppCopyrightInfo();
      }
      return copyrightInfo;
   }
}
