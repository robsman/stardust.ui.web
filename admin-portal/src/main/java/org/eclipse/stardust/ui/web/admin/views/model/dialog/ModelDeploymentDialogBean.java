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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.wizard.Wizard;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardFlowEvent;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPage;


/**
 * 
 * @author Vikas.Mishra
 * 
 */
public class ModelDeploymentDialogBean extends Wizard implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "modelDeploymentDialogBean";
   private final List<String> fileTypes;
   private ModelDeploymentConfigurationPage configurationPage;
   private ModelDeploymentPage deploymentPage;
   private ModelDeploymentStatusPage deploymentStatusPage;
   private ModelConfigurationValidationPage configurationValidationPage;
   private int modelOID;
   private String modelName;
   private boolean overwrite = false;
   private String version;
   private final AdminMessagesPropertiesBean propBean;
   private boolean allowBrowse = true;

   // //////////////////////////////////////
   public ModelDeploymentDialogBean(List<String> supportedFileTypes)
   {
      super();
      this.fileTypes = supportedFileTypes;
      propBean=AdminMessagesPropertiesBean.getInstance();

   }

   /**
    * @return
    */
   public static ModelDeploymentDialogBean getCurrent()
   {
      return (ModelDeploymentDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void closePopup()
   {
      super.closePopup();
   }

   public ModelDeploymentConfigurationPage getConfigurationPage()
   {
      return configurationPage;
   }

   public ModelDeploymentPage getDeploymentPage()
   {
      return deploymentPage;
   }

   public ModelDeploymentStatusPage getDeploymentStatusPage()
   {
      return deploymentStatusPage;
   }   

   //
   // /**
   // *
   // */
   public void handleEvent(ViewEvent event)
   {}

   @Override
   public void initialize()
   {}

   public boolean isOverwrite()
   {
      return overwrite;
   }

   /**
    * Last action of wizard It save configuration variable changes and close popup
    * 
    * @param event
    */

   public void finish(ActionEvent event)
   {
      configurationPage.saveConfigurationValue(event);
      closePopup();

   }

   @Override
   public void openPopup()
   {
      List<WizardPage> pages = configurePages();
      initializePages(pages);
      super.openPopup();
   }

   private List<WizardPage> configurePages()
   {
      List<WizardPage> pages = new ArrayList<WizardPage>();
      deploymentPage = new ModelDeploymentPage(fileTypes, overwrite, modelOID, modelName, version);
      deploymentStatusPage = new ModelDeploymentStatusPage();
      configurationPage = new ModelDeploymentConfigurationPage();
      configurationValidationPage=new ModelConfigurationValidationPage();

      pages.add(deploymentPage);
      pages.add(deploymentStatusPage);
      pages.add(configurationPage);
      pages.add(configurationValidationPage);
      return pages;
   }

   public void setOverwriteModel(int modelOID, String modelName, String version)
   {
      this.overwrite = true;
      this.modelOID = modelOID;
      this.modelName = modelName;
      this.version = version;
   }

   public void setOverwrite(boolean overwrite)
   {
      this.overwrite = overwrite;
   }

   public String getVersion()
   {
      return version;
   }

   @Override
   public void flowEvent(WizardFlowEvent event)
   {
     
      if (event.getType().equals(WizardFlowEvent.WizardFlowEventType.PREVIOUS)
            && event.getNewPage() instanceof ModelDeploymentPage)
      {
         event.setVetoed(true);
      }
      else if (event.getType().equals(WizardFlowEvent.WizardFlowEventType.NEXT)
               && event.getOldPage() instanceof ModelDeploymentConfigurationPage)
         {
           if(! ((ModelDeploymentConfigurationPage)event.getOldPage()).isValueChanged())
           {
              closePopup();
           }
         }
      else if (event.getType().equals(WizardFlowEvent.WizardFlowEventType.FINISH)
            && event.getOldPage() instanceof ModelConfigurationValidationPage)
      {
        ((ModelConfigurationValidationPage) event.getOldPage()).forceSaveConfigurationValue();
      }
   }

   @Override
   public boolean isPreviousRender()
   {
      return (getCurrentPage() instanceof ModelDeploymentStatusPage) ? false : super.isPreviousRender();
   }
  
   @Override
   public boolean isNextRender()
   {
      if(getCurrentPage() instanceof ModelDeploymentConfigurationPage)
      {
         return  true;
      }
      return super.isNextRender();
   }

   @Override
   public boolean isNextEnable()
   {
      if ((getCurrentPage() instanceof ModelDeploymentStatusPage))
      {
         ModelDeploymentStatusPage statusPage = (ModelDeploymentStatusPage) getCurrentPage();

         if (statusPage.isContainsErrors() || statusPage.isDeploymentException() )
         {
            return false;
         }

         return statusPage.isContainsConfigurationValues();
      }
      else if ((getCurrentPage() instanceof ModelDeploymentPage))
      {
         ModelDeploymentPage deploymentPage = (ModelDeploymentPage) getCurrentPage();
         return deploymentPage.isModelUploaded();
      }
      return super.isNextEnable();
   }

   public boolean isFinishRender()
   {
      if(getCurrentPage() instanceof ModelDeploymentConfigurationPage)
      {
         return  false;
      }
      return super.isFinishRender();
   }
   @Override
   public String getNextLabel()
   {
      if(getCurrentPage() instanceof ModelDeploymentPage)
      {
        return propBean.getString("views.deployModelView.deployment.button.deploy");
      }
      else  if(getCurrentPage() instanceof ModelDeploymentPage)
      {
         return  super.getFinishLabel();
       }
      else  if(getCurrentPage() instanceof ModelDeploymentConfigurationPage)
      {
         return  super.getFinishLabel();
       }
      return super.getNextLabel();
   }
   
   @Override
   public String getFinishLabel()
   {
      if(getCurrentPage() instanceof ModelDeploymentPage)
      {
        return propBean.getString("views.deployModelView.deployment.button.cancel");
      }
      return super.getFinishLabel();
   }

   public void openDeployModelDialog(ActionEvent event)
   {  
      Map requestParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      if (null != requestParams.get("allowBrowse"))
      {
         setAllowBrowse(Boolean.valueOf((String) requestParams.get("allowBrowse")));
      }
      setOverwrite(false);
      openPopup();
      ModelDeployTableEntry modelTableEntry = new ModelDeployTableEntry();
      modelTableEntry.setFileName((String) requestParams.get("fileName"));
      modelTableEntry.setFilePath((String) requestParams.get("filePath"));
      modelTableEntry.setDeploymentAction(overwrite ? 2 : 1);
      deploymentPage.addModelToModelList(modelTableEntry);
      deploymentPage.initialize();
   }

   public void setAllowBrowse(boolean allowBrowse)
   {
      this.allowBrowse = allowBrowse;
   }

   public boolean isAllowBrowse()
   {
      return allowBrowse;
   }
}
