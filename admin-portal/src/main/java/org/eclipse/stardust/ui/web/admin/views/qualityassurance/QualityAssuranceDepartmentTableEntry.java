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
package org.eclipse.stardust.ui.web.admin.views.qualityassurance;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceDepartmentTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 4569694839690636784L;
   private Department department;
   private Activity activity;
   private String deptName;
   private String qaPercentageD;
   private boolean selectedRow;
   private boolean editOn = false;
   private boolean modified = false;
   private String qaPercentagePrev;

   /**
    * @param department
    */
   public QualityAssuranceDepartmentTableEntry(Activity activity, Department department, Integer qaPercentage)
   {
      super();
      this.department = department;
      this.activity = activity;
      this.deptName = department.getName();
      this.qaPercentageD =  QualityAssuranceUtils.getStringValueofQAProbability(qaPercentage);
      qaPercentagePrev = this.qaPercentageD;
   }

   public void setQaPercentageD(String qaPercentageNew)
   {
      this.qaPercentageD = qaPercentageNew;

      if (null == qaPercentageD && null == qaPercentagePrev)
      {
         modified = false;
      }

      else if (null != qaPercentagePrev && null != qaPercentageD && qaPercentagePrev.equals(qaPercentageD))
      {
         modified = false;
      }
      else
      {
         modified = true;
      }
   }

   public Department getDepartment()
   {
      return department;
   }

   public String getDeptName()
   {
      return deptName;
   }

   public String getQaPercentageD()
   {
      return qaPercentageD;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
   }

   public boolean isEditOn()
   {
      return editOn;
   }

   public void setEditOn(boolean editOn)
   {
      this.editOn = editOn;
   }

   public boolean isModified()
   {
      return modified;
   }

   public Activity getActivity()
   {
      return activity;
   }
}