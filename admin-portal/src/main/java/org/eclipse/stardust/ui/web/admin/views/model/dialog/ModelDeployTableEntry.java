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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;

public class ModelDeployTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private String fileName;
   private String filePath;

   // private String modelId;
   private int deploymentAction;
   private int overwriteVersion;

   public int getDeploymentAction()
   {
      return deploymentAction;
   }

   public String getFileName()
   {
      return fileName;
   }

   public String getFilePath()
   {
      return filePath;
   }

   public int getOverwriteVersion()
   {
      return overwriteVersion;
   }

   public void setDeploymentAction(int deploymentAction)
   {
      this.deploymentAction = deploymentAction;
   }

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   public void setFilePath(String filePath)
   {
      this.filePath = filePath;
   }

   public void setOverwriteVersion(int overwriteVersion)
   {
      this.overwriteVersion = overwriteVersion;
   }
}
