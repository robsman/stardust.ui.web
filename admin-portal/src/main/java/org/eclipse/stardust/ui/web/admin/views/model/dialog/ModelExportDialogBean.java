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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementUserObject;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;


import com.icesoft.faces.context.ByteArrayResource;
import com.icesoft.faces.context.Resource;

/**
 * Managed bean for Model Export popup
 * 
 * @author Vikas.Mishra
 * 
 */
public class ModelExportDialogBean extends PopupUIComponentBean
{
   private final static String SINGLE_MODEL = "1";
   private final static String MODEL_WITH_REFERENCE = "2";

   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "modelExportDialogBean";
   private AdminMessagesPropertiesBean propsBean;
   private ModelManagementUserObject userObject;
   private String selectedOption;
   private Resource fileResource;
   private String fileName;

   public ModelExportDialogBean()
   {
      propsBean = AdminMessagesPropertiesBean.getInstance();
   }

   /**
    * Gets the option items for model export selection .
    * 
    * @return array of model export items
    */
   public SelectItem[] getModelExportItems()
   {
      SelectItem[] exportItems = new SelectItem[2];
      exportItems[0] = new SelectItem(SINGLE_MODEL, propsBean.getString("views.modelExportDialog.option.onlyModel"));

      exportItems[1] = new SelectItem(MODEL_WITH_REFERENCE, propsBean
            .getString("views.modelExportDialog.option.modelAndReferences"));

      return exportItems;
   }

   /**
    * @return
    */
   public static ModelExportDialogBean getCurrent()
   {
      ModelExportDialogBean bean = (ModelExportDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
      bean.initialize();
      return bean;
   }

   @Override
   public void initialize()
   {
      fileResource = null;
      userObject = null;
      selectedOption = SINGLE_MODEL;
   }

   public String getSelectedOption()
   {
      return selectedOption;
   }

   public void setSelectedOption(String selectedOption)
   {
      this.selectedOption = selectedOption;
   }

   public ModelManagementUserObject getUserObject()
   {
      return userObject;
   }

   public void setUserObject(ModelManagementUserObject userObject)
   {
      this.userObject = userObject;
   }

   public Resource getFileResource()
   {
      return fileResource;
   }

   public void setFileResource(Resource fileResource)
   {
      this.fileResource = fileResource;
   }

   public void modelSelectionListener(ValueChangeEvent event)
   {
      String selectedOption = event.getNewValue().toString();
      if (SINGLE_MODEL.equals(selectedOption))
      {
         QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
         String xmlData = queryService.getModelAsXML(userObject.getOid());
         fileResource = new ByteArrayResource(xmlData.getBytes());
         fileName = userObject.getParent().getLabel() + FileUtils.XPDL_FILE;
      
      
      }
      else
      {

         if (userObject.getModelDescription() != null)
         {
            QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
            String xmlData = queryService.getModelAsXML(userObject.getOid());
            Map<String, String> dataMap = new HashMap<String, String>();

            dataMap.put(fileName, xmlData);

            if (MODEL_WITH_REFERENCE.equals(selectedOption))
            {
               List<Long> providerModels = userObject.getModelDescription().getProviderModels();
               if (providerModels != null)
               {
                  for (Long providerId : providerModels)
                  {
                     Model model = ModelCache.findModelCache().getModel(providerId);
                     String content = queryService.getModelAsXML(providerId);
                     dataMap.put(model.getName() + FileUtils.XPDL_FILE, content);
                  }
               }
               List<Long> consumerModels = userObject.getModelDescription().getConsumerModels();
               if (consumerModels != null)
               {
                  for (Long consumerId : consumerModels)
                  {
                     Model model = ModelCache.findModelCache().getModel(consumerId);
                     String content = queryService.getModelAsXML(consumerId);
                     dataMap.put(model.getName() + FileUtils.XPDL_FILE, content);
                  }
               }
            }

            byte[] zipContent = FileUtils.doZip(dataMap);
            fileResource = new ByteArrayResource(zipContent);
            fileName = userObject.getParent().getLabel() + FileUtils.ZIP_FILE;

         }
      }

   }

   public String getFileName()
   {
      return fileName;
   }

   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   @Override
   public void openPopup()
   {
      QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      String xmlData = queryService.getModelAsXML(userObject.getOid());
      fileResource = new ByteArrayResource(xmlData.getBytes());
      fileName = userObject.getParent().getLabel() + FileUtils.XPDL_FILE;
      selectedOption = SINGLE_MODEL;
      super.openPopup();
   }

}
