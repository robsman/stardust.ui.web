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
import org.eclipse.stardust.ui.web.viewscommon.common.table.RowDeselectionListener;

public class ImplementationTableEntry extends DefaultRowModel
{

   private static final long serialVersionUID = 1L;
   private String modelName;
   private String version;
   private int modelOID;
   private String process;
   private boolean checkSelection;
   private String ModelId;
   private RowDeselectionListener rowDeselectionListener;

   public String getModelName()
   {
      return modelName;
   }

   public void setModelName(String modelName)
   {
      this.modelName = modelName;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public String getProcess()
   {
      return process;
   }

   public void setProcess(String process)
   {
      this.process = process;
   }

   @Override
   /*
    * * if active then return style class highlighted otherwise super.getStyleClass()
    */
   public String getStyleClass()
   {
      return super.getStyleClass();
   }

   public boolean isCheckSelection()
   {
      return checkSelection;
   }

   public void setCheckSelection(boolean checkSelection)
   {
      if (checkSelection && null != rowDeselectionListener)
      {
         rowDeselectionListener.rowDeselected();
      }
      this.checkSelection = checkSelection;
   }
   
   public void resetCheckSelection()
   {
      this.checkSelection = false;
   }

   public int getModelOID()
   {
      return modelOID;
   }

   public void setModelOID(int modelOID)
   {
      this.modelOID = modelOID;
   }

   /**
    * @return the modelId
    */
   public String getModelId()
   {
      return ModelId;
   }

   /**
    * @param modelId the modelId to set
    */
   public void setModelId(String modelId)
   {
      ModelId = modelId;
   }

   public void setRowDeselectionListener(RowDeselectionListener rowDeselectionListener)
   {
      this.rowDeselectionListener = rowDeselectionListener;
   }
}
