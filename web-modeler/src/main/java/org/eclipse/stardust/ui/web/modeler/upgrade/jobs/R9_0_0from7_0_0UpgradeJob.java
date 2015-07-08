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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.model.beans.XMLConstants;
import org.eclipse.stardust.model.xpdl.builder.defaults.DefaultElementsInitializer;
import org.eclipse.stardust.model.xpdl.builder.spi.ModelInitializer;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.upgrade.UpgradeJob;

public class R9_0_0from7_0_0UpgradeJob extends UpgradeJob
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

   /**
    * on upgrade, check for each single element if a change is needed, to prevent duplicates
    */
   public ModelType upgradeModel(ModelType model)
   {
      String vendor = model.getVendor();
      if(vendor != null && !vendor.equals(XMLConstants.VENDOR_NAME))
      {
         model.setVendor(XMLConstants.VENDOR_NAME);
      }
            
      ModelInitializer initializer = new DefaultElementsInitializer();
      initializer.initializeModel(model);
      
      return model;
   }
}