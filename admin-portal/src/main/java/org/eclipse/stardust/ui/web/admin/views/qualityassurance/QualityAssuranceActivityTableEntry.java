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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementLocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceActivityTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 5119025123905804704L;
   private static final String ICON_ORGANIZATION_UNSCOPED = "/plugins/views-common/images/icons/chart_organisation.png";
   private static final String ICON_ORGANIZATION_SCOPED_EXPLICIT = "/plugins/views-common/images/icons/organization_scoped.png";
   private static final String ICON_ORGANIZATION_SCOPED_IMPLICIT = "/plugins/views-common/images/icons/organization_scoped.png";
   private static final String ICON_ROLE_UNSCOPED = "/plugins/views-common/images/icons/role.png";
   private static final String ICON_ROLE_SCOPED = "/plugins/views-common/images/icons/role_scoped.png";

   private Activity activity;
   private String modelName;
   private String processName;
   private String activityName;
   private String defaultPerformer;
   private String qaPercentage;
   private String qaPercentagePrev;
   private boolean selectedRow;
   private boolean editOn = false;
   private boolean modified = false;
   private String performerIconPath;
   private String modelDescription;
   private boolean oldModel;

   /**
    * @param activity
    */
   public QualityAssuranceActivityTableEntry(Activity activity, Integer qaPercentage)
   {
      super();
      this.activity = activity;
      DeployedModelDescription model = ModelUtils.getModel(activity.getModelOID());
      this.modelName = I18nUtils.getModelName(model);
      this.processName = I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(activity
            .getProcessDefinitionId()));
      this.activityName = I18nUtils.getActivityName(activity);
      this.defaultPerformer = I18nUtils.getParticipantName(activity.getDefaultPerformer());
      this.qaPercentage = QualityAssuranceUtils.getStringValueofQAProbability(qaPercentage);
      qaPercentagePrev = this.qaPercentage;
      modelDescription = I18nUtils.getDescriptionAsHtml(model, model.getDescription());
      setIconPath();
   }

   public void setQaPercentage(String qaPercentageNew)
   {
      this.qaPercentage = qaPercentageNew;

      if (null == qaPercentage && null == qaPercentagePrev)
      {
         modified = false;
      }
      else if (null != qaPercentagePrev && null != qaPercentage && qaPercentagePrev.equals(qaPercentage))
      {
         modified = false;
      }
      else
      {
         modified = true;
      }
   }

   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
      if (this.selectedRow)
      {
         QualityAssuranceManagementBean qaManagementBean = QualityAssuranceManagementBean.getInstance();
         qaManagementBean.displayDepartmentTable(this.activity);
      }
   }

   /**
    * set Performer Icon path
    */
   private void setIconPath()
   {
      QualifiedModelParticipantInfo modelParticipantInfo = activity.getDefaultPerformer();
      if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.definesDepartmentScope())
      {
         performerIconPath = ICON_ORGANIZATION_SCOPED_EXPLICIT;
      }
      else if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.isDepartmentScoped()
            && !modelParticipantInfo.definesDepartmentScope())
      {
         performerIconPath = ICON_ORGANIZATION_SCOPED_IMPLICIT;
      }
      else if ((modelParticipantInfo instanceof RoleInfo) && modelParticipantInfo.isDepartmentScoped())
      {
         performerIconPath = ICON_ROLE_SCOPED;
      }
      else if (modelParticipantInfo instanceof OrganizationInfo)
      {
         performerIconPath = ICON_ORGANIZATION_UNSCOPED;
      }
      else if (modelParticipantInfo instanceof RoleInfo)
      {
         performerIconPath = ICON_ROLE_UNSCOPED;
      }
   }

   public void setModelStatus(boolean active)
   {
      if (!active)
      {
         oldModel = true;
      }
   }

   public Activity getActivity()
   {
      return activity;
   }

   public String getProcessName()
   {
      return processName;
   }

   public String getActivityName()
   {
      return activityName;
   }

   public String getDefaultPerformer()
   {
      return defaultPerformer;
   }

   public String getQaPercentage()
   {
      return qaPercentage;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public String getModelName()
   {
      return modelName;
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

   public String getPerformerIconPath()
   {
      return performerIconPath;
   }

   public String getModelDescription()
   {
      return modelDescription;
   }

   public boolean isOldModel()
   {
      return oldModel;
   }
}
