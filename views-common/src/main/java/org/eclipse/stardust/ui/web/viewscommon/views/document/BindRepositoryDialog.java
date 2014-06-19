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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.repository.jcr.JcrVfsRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryConfiguration;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstanceInfo;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryProviderInfo;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.RepositoryVirtualUserObject;

public class BindRepositoryDialog extends PopupUIComponentBean
{

   /**
    * 
    */
   private static final long serialVersionUID = -1142096561438831522L;
   private static final String BEAN_NAME = "bindRepositoryDialogBean";

   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

   private List<SelectItem> providerItems;

   private String repositoryId;

   private String selectedProvider;

   private RepositoryVirtualUserObject userObject;

   private Map<String, IRepositoryProviderInfo> providerMap;

   private IRepositoryProviderInfo repositoryProviderInfo;

   private IRepositoryInstanceInfo repositoryInstanceInfo;
   
   private Map<String, Serializable> attributesMap;
   
   private List<String> keyList;

   private String title;

   private boolean showProperties;

   public static BindRepositoryDialog getInstance()
   {
      BindRepositoryDialog bean = (BindRepositoryDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
      return bean;
   }

   @Override
   public void initialize()
   {
      List<IRepositoryProviderInfo> repositoryProviderInfos = DocumentMgmtUtility.getDocumentManagementService()
            .getRepositoryProviderInfos();
      providerItems = new ArrayList<SelectItem>();
      providerMap = CollectionUtils.newHashMap();
      attributesMap = new Hashtable<String, Serializable>();
      keyList = CollectionUtils.newArrayList();
      for (IRepositoryProviderInfo iRepositoryProviderInfo : repositoryProviderInfos)
      {
         providerItems.add(new SelectItem(iRepositoryProviderInfo.getProviderId(), iRepositoryProviderInfo
               .getProviderName()));
         providerMap.put(iRepositoryProviderInfo.getProviderId(), iRepositoryProviderInfo);
      }
      this.repositoryProviderInfo = repositoryProviderInfos.get(0);
      this.selectedProvider = this.repositoryProviderInfo.getProviderId();
      updateAttributeMap();
   }

   private void updateAttributeMap()
   {
      attributesMap.clear();
      Map<String, Serializable> objectMap = this.repositoryProviderInfo.getConfigurationTemplate().getAttributes();
      keyList.clear();
      for (Entry<String, Serializable> obj : objectMap.entrySet())
      {
         if (obj.getKey().equals(IRepositoryConfiguration.PROVIDER_ID)
               || obj.getKey().equals(IRepositoryConfiguration.REPOSITORY_ID))
         {
            continue;
         }
         else
         {
            keyList.add(obj.getKey());
            attributesMap.remove(obj.getKey());
            attributesMap.put(obj.getKey(), obj.getValue());
            
         }
         
      }
   }
   
   @Override
   public void openPopup()
   {
      try
      {
         if (repositoryId != null)
         {
            repositoryInstanceInfo = getRepositoryInstanceInfo(repositoryId);
            title = repositoryInstanceInfo.getRepositoryId()
                  + " " + COMMON_MESSAGE_BEAN.get("views.genericRepositoryView.treeMenuItem.repo.properties");
         }
         else
         {
            title = COMMON_MESSAGE_BEAN.getString("views.bindRepositoryDialog.title");
            initialize();
         }
         super.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException("",
               COMMON_MESSAGE_BEAN.getString("common.exception") + " : " + e.getLocalizedMessage());
      }
   }

   @Override
   public void closePopup()
   {
      showProperties = false;
      repositoryId = null;
      super.closePopup();
   }

   public void createRepository()
   {
      try
      {
         if (repositoryId == null)
         {
            ExceptionHandler.handleException(getBeanId(),
                  MessagesViewsCommonBean.getInstance().getString("views.bindRepositoryDialog.repoId.empty"));
            return;
         }
         Map<String, Serializable> attributes = CollectionUtils.newMap();
         attributes.put(IRepositoryConfiguration.PROVIDER_ID, getSelectedProvider());
         attributes.put(IRepositoryConfiguration.REPOSITORY_ID, getRepositoryId());
         for(Map.Entry<String, Serializable> entry  : attributesMap.entrySet())
         {
            if(!IRepositoryConfiguration.PROVIDER_ID.equals(entry.getKey()) || !IRepositoryConfiguration.REPOSITORY_ID.equals(entry.getKey()))
            {
               attributes.put(entry.getKey(), entry.getValue());   
            }
         }
         attributes.put(JcrVfsRepositoryConfiguration.USER_LEVEL_AUTHORIZATION, true);
         DocumentMgmtUtility.getDocumentManagementService().bindRepository(
               new JcrVfsRepositoryConfiguration(attributes));
         IRepositoryInstanceInfo repositoryInstanceInfo = getRepositoryInstanceInfo(repositoryId);
         RepositoryUtility.createRepository(userObject.getWrapper(), repositoryInstanceInfo);
         closePopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(getBeanId(),
               MessagesViewsCommonBean.getInstance().getString("views.bindRepositoryDialog.createException") + " : "
                     + e.getLocalizedMessage());
      }
   }
   
   /**
    * 
    * @param repositoryId
    * @return
    */
   private IRepositoryInstanceInfo getRepositoryInstanceInfo(String repositoryId)
   {
      List<IRepositoryInstanceInfo> repositoryInstanceInfos = DocumentMgmtUtility.getDocumentManagementService()
            .getRepositoryInstanceInfos();
      for (IRepositoryInstanceInfo instanceInfo : repositoryInstanceInfos)
      {
         if (instanceInfo.getRepositoryId().equals(repositoryId))
         {
            return instanceInfo;
         }
      }
      return null;
   }

   public void attributeChanged(ValueChangeEvent event)
   {
      String key = (String) event.getComponent().getAttributes().get("key");
      String value = (String) event.getNewValue();
      attributesMap.put(key, value);
   }
   
   public void providerChange(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
         String value = event.getNewValue().toString();
         this.repositoryProviderInfo = providerMap.get(value);
         updateAttributeMap();
   }


   public List<SelectItem> getProviderItems()
   {
      return providerItems;
   }

   public void setProviderItems(List<SelectItem> providerItems)
   {
      this.providerItems = providerItems;
   }

   public String getRepositoryId()
   {
      return repositoryId;
   }

   public void setRepositoryId(String repositoryId)
   {
      this.repositoryId = repositoryId;
   }

   public String getSelectedProvider()
   {
      return selectedProvider;
   }

   public void setSelectedProvider(String selectedProvider)
   {
      this.selectedProvider = selectedProvider;
      this.repositoryProviderInfo = providerMap.get(selectedProvider);
      updateAttributeMap();
   }

   public MessagesViewsCommonBean getCOMMON_MESSAGE_BEAN()
   {
      return COMMON_MESSAGE_BEAN;
   }

   public RepositoryVirtualUserObject getUserObject()
   {
      return userObject;
   }

   public void setUserObject(RepositoryVirtualUserObject userObject)
   {
      this.userObject = userObject;
   }

   public IRepositoryProviderInfo getRepositoryProviderInfo()
   {
      return repositoryProviderInfo;
   }
   
   public IRepositoryInstanceInfo getRepositoryInstanceInfo()
   {
      return repositoryInstanceInfo;
   }

   public void setRepositoryInstanceInfo(IRepositoryInstanceInfo repositoryInstanceInfo)
   {
      this.repositoryInstanceInfo = repositoryInstanceInfo;
   }

   public Map<String, Serializable> getAttributesMap()
   {
      return attributesMap;
   }

   public void setAttributesMap(Map<String, Serializable> attributesMap)
   {
      this.attributesMap.clear();
      this.attributesMap = attributesMap;
   }
   public boolean isShowProperties()
   {
      return showProperties;
   }

   public void setShowProperties(boolean showProperties)
   {
      this.showProperties = showProperties;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public List<String> getKeyList()
   {
      return keyList;
   }

   public void setKeyList(List<String> keyList)
   {
      this.keyList = keyList;
   }
   
}
