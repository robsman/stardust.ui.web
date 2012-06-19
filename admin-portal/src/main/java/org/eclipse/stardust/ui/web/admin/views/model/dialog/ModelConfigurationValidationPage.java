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

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.model.ConfigurationVariablesBean;
import org.eclipse.stardust.ui.web.admin.views.model.ModelValidationHelper;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPage;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPageEvent;



/**
 * Class represent ConfigurationPage for Model deployment wizard
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class ModelConfigurationValidationPage extends WizardPage
{
   private AdminMessagesPropertiesBean propsBean;
   private final ModelValidationHelper validationHelper;
   private List<ModelReconfigurationInfo> deploymentInfoList = null;

   public ModelConfigurationValidationPage()
   {
      super("CONFIGURATION_PAGE", "/plugins/admin-portal/views/model/_configurationVariableValidationPage.xhtml");
      propsBean = AdminMessagesPropertiesBean.getInstance();

      validationHelper = new ModelValidationHelper();
     
      validationHelper.createTreeTable();
      

   }

   @Override
   public String getTitle()
   {
      return propsBean.getString("views.configurationValidationDialog.title");
   }

   public void setDeploymentInfoList(List<ModelReconfigurationInfo> deploymentInfoList)
   {
      this.deploymentInfoList = deploymentInfoList;
   }

   public void handleEvent(WizardPageEvent event)
   {
      if (event.getType().equals(WizardPageEvent.WizardPageEventType.PAGE_ACTIVATE))
      {
         
         if (event.getFlowEvent().getOldPage() instanceof ModelDeploymentConfigurationPage)
         {
            ModelDeploymentConfigurationPage configPage = (ModelDeploymentConfigurationPage) event.getFlowEvent().getOldPage();            
            validationHelper.setDeploymentInfoList( configPage.getInfoList());
            validationHelper.createTreeTable(); 
            validationHelper.setTreeTableBean(configPage.getConfigurationVariablesBean());
            validationHelper.initialize();
         }
         
        
      }

   }

   public ModelValidationHelper getValidationHelper()
   {
      return validationHelper;
   }
   public void forceSaveConfigurationValue()
   {
    ((ConfigurationVariablesBean) validationHelper.getTreeTableBean()).forceSaveConfigurationValue();
   }
   
}
