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

import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;

/**
 * The base class for a concrete update job (on each new version). 
 * 
 * @author Barry.Grotjahn
 */
public abstract class UpgradeJob
{
   public abstract Version getVersion();

   public ModelType upgradeModel(ModelType model)
   {
      return null;
   }

   public ModelType upgradeVersion(ModelType model)
   {
      model.setCarnotVersion(getVersion().toCompleteString());
      return model;
   }
      
   public boolean matches(Version version)
   {
      if (getVersion().compareTo(version) > 0)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
}