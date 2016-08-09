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
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.upgrade.UpgradeJob;

public class R9_2_0from9_0_0UpgradeJob extends UpgradeJob
{
   private static final Logger trace = LogManager.getLogger(R9_2_0from9_0_0UpgradeJob.class);

   /**
    * the upgrade will change version number to this version
    */
   private static final Version VERSION = Version.createFixedVersion(9, 2, 0);

   public R9_2_0from9_0_0UpgradeJob()
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

   @Override
   public ModelType upgradeModel(ModelType model)
   {
      DataType data = (DataType) ModelUtils.findIdentifiableElement(model.getData(), PredefinedConstants.BUSINESS_DATE);
      if (data != null)
      {
         AttributeType attribute = AttributeUtil.getAttribute(data, PredefinedConstants.TYPE_ATT);
         if (attribute != null && !Type.Calendar.getId().equals(attribute.getValue()))
         {
            attribute.setValue(Type.Calendar.getId());
         }
      }
      return model;
   }
}