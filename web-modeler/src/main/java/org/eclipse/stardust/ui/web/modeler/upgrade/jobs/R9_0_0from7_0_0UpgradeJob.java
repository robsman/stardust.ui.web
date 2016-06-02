/*******************************************************************************
* Copyright (c) 2015 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Barry.Grotjahn (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.upgrade.jobs;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

public class R9_0_0from7_0_0UpgradeJob extends M3_1_0from1_0_0UpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R9_0_0from7_0_0UpgradeJob.class);

   /**
    * the upgrade will change version number to this version
    */
   private static final Version VERSION = Version.createFixedVersion(9, 0, 0);

   public R9_0_0from7_0_0UpgradeJob()
   {
      super();
   }

   protected Logger getLogger()
   {
      return trace;
   }

   public Version getVersion()
   {
      return VERSION;
   }
}