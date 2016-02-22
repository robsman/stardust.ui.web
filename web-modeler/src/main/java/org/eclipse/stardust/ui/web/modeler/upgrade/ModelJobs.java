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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.ui.web.modeler.upgrade.jobs.M3_1_0from1_0_0UpgradeJob;
import org.eclipse.stardust.ui.web.modeler.upgrade.jobs.R9_0_0from7_0_0UpgradeJob;

/**
 * @author Barry.Grotjahn
 */
public class ModelJobs
{
   private static List<UpgradeJob> jobs;

   /*
    *
    */
   public static List<UpgradeJob> getModelJobs()
   {
      if (jobs == null)
      {
         jobs = new LinkedList<UpgradeJob>();

         // TODO: refactor this out of Stardust code
         if (CurrentVersion.getProductName().matches(".*[Ee]clipse.*"))
         {
            jobs.add(new M3_1_0from1_0_0UpgradeJob());
         }
         else
         {
            jobs.add(new R9_0_0from7_0_0UpgradeJob());
         }
      }

      return jobs;
   }
}