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

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.model.ConfigurationVariablesBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardFlowEvent;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPage;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPageEvent;



/**
 * Class represent ConfigurationPage for Model deployment wizard
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class ModelDeploymentConfigurationPage extends WizardPage
{
   private AdminMessagesPropertiesBean propsBean;
   private ConfigurationVariablesBean configurationVariablesBean;

   private List<ConfigurationVariables> configurationVariablesEditList;
   private  List<ModelReconfigurationInfo> infoList;

   public ModelDeploymentConfigurationPage()
   {
      super("CONFIGURATION_PAGE", "/plugins/admin-portal/views/model/_modelDeployConfigurationVariablesPage.xhtml");
      propsBean = AdminMessagesPropertiesBean.getInstance();
      configurationVariablesBean = new ConfigurationVariablesBean(true);     

   }

   public List<ConfigurationVariables> getConfigurationVariablesEditList()
   {
      return configurationVariablesEditList;
   }

   public void setConfigurationVariablesEditList(List<ConfigurationVariables> configurationVariablesEditList)
   {
      this.configurationVariablesEditList = configurationVariablesEditList;
   }

   public ConfigurationVariablesBean getConfigurationVariablesBean()
   {
      return configurationVariablesBean;
   }

   public void handleEvent(ViewEvent event)
   {
      configurationVariablesBean.handleEvent(event);
   }

   @Override
   public String getTitle()
   {
      return propsBean.getString("views.deploymodel.title.deployContinue");
   }

   public void saveConfigurationValue(ActionEvent event)
   {
      try
      {
         configurationVariablesBean.saveConfigurationValue();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   

   public void handleEvent(WizardPageEvent event)
   {
      if (event.getType().equals(WizardPageEvent.WizardPageEventType.PAGE_ACTIVATE))
      {
         if (event.getFlowEvent().getOldPage() instanceof ModelDeploymentStatusPage)
         {
            ModelDeploymentStatusPage statusPage = (ModelDeploymentStatusPage) event.getFlowEvent().getOldPage();
            this.configurationVariablesEditList = statusPage.getConfigurationVariablesEditList();
          
            configurationVariablesBean.initializeColumnModel();
            configurationVariablesBean.initialize(statusPage.getAllConfigurationVariables());

         }
      }
      else if (event.getType().equals(WizardPageEvent.WizardPageEventType.PAGE_DEACTIVATE)
            && event.getFlowEvent().getType().equals(WizardFlowEvent.WizardFlowEventType.NEXT))
      {
         try
         {
            if(configurationVariablesBean.isValueChanged())
            {
               infoList= configurationVariablesBean.saveConfigurationValue();
            }
            else
            {
              
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }

   }

   public List<ModelReconfigurationInfo> getInfoList()
   {
      return infoList;
   }
   
   public boolean isValueChanged()
   {
      return configurationVariablesBean.isValueChanged();
   }
}
