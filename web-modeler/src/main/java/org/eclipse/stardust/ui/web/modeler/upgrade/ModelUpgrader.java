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

package org.eclipse.stardust.ui.web.modeler.upgrade;

import java.util.List;

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;

/**
 * The upgrader to upgrade a model.
 *
 * @author Barry.Grotjahn
 */
public class ModelUpgrader
{
   private List<UpgradeJob> jobs;
   private ModelType model;
   private Version version;

   public ModelUpgrader(ModelType model)
   {
      this(model, ModelJobs.getModelJobs());
   }

   private ModelUpgrader(ModelType model, List<UpgradeJob> modelJobs)
   {
      this.model = model;
      jobs = modelJobs;

      version = Version.createModelVersion(model.getCarnotVersion(), model.getVendor());
   }

   public boolean upgradeNeeded()
   {
      for(UpgradeJob job : jobs)
      {
         if(job.matches(version))
         {
            return true;
         }
      }

      return false;
   }

   public ModelType doUpgradeModel()
   {
      for(UpgradeJob job : jobs)
      {
         if(job.matches(version))
         {
            ModelType upgraded = job.upgradeModel(model);
            if(upgraded != null)
            {
               model = upgraded;
            }
            model = job.upgradeVersion(model);
         }
      }

      return model;
   }
}