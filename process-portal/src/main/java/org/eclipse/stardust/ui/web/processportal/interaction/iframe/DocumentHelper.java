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

package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;
import org.eclipse.stardust.ui.web.processportal.view.ActivityPanelConfigurationBean;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class DocumentHelper
{
   /**
    * 
    * @param interaction
    * @param inData
    */
   public static void initializeDocumentControllers(Interaction interaction, Map<String, ? extends Serializable> inData)
   {
      ApplicationContext context = interaction.getDefinition();

      List<DataMapping> dataMappings = context.getAllInDataMappings();

      Map<String, ManualActivityDocumentController> documentControllers = new HashMap<String, ManualActivityDocumentController>();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, ? extends Serializable> entry : inData.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               if (ModelUtils.isDocumentType(interaction.getModel(), dm))
               {
                  ManualActivityDocumentController dc;
                  if (entry.getValue() != null)
                  {
                     dc = new ManualActivityDocumentController((Document) entry.getValue(), dm, interaction);
                  }
                  else
                  {
                     dc = new ManualActivityDocumentController(null, dm, interaction);
                  }
                  documentControllers.put(dm.getId(), dc);
               }
            }
         }
      }
      interaction.setDocumentControllers(documentControllers);
   }

   /**
    * 
    * @param activityInstance
    * @param activityPanel
    */
   public static void openMappedDocuments(ActivityInstance activityInstance, View activityPanel)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(facesContext,
            InteractionRegistry.BEAN_ID);
      Interaction interaction = registry.getInteraction(Interaction.getInteractionId(activityInstance));

      if (ActivityPanelConfigurationBean.isAutoDisplayMappedDocuments())
      {
         for (Entry<String, ManualActivityDocumentController> documentController : interaction.getDocumentControllers()
               .entrySet())
         {
            PortalApplication.getInstance().setFocusView(activityPanel);
            documentController.getValue().openDocument(true);
         }
         PortalApplication.getInstance().setFocusView(activityPanel);
      }
   }

   /**
    * 
    * @param interaction
    */
   public static void transformDocuments(Interaction interaction)
   {
      Map<String, Serializable> convertedDocs = new HashMap<String, Serializable>();
      Map<String, Serializable> outData = interaction.getOutDataValues();

      if (outData == null)
      {
         return;
      }
      
      Map<String, ManualActivityDocumentController> dcs = interaction.getDocumentControllers();

      for (Entry<String, ? extends Serializable> dataEntry : outData.entrySet())
      {
         if (dcs.get(dataEntry.getKey()) != null)
         {
            // delete jcr document(s)
            DocumentHelper.deleteDocuments(dcs.get(dataEntry.getKey()).getDocsTobeDeleted());
            if (dataEntry.getValue() != null)
            {
               ManualActivityDocumentController documentController = dcs.get(dataEntry.getKey());
               if (!documentController.isJCRDocument())
               {
                  // document is uploaded, convert to jcr document
                  Document document = documentController.createJCRDocumentFromUUID((String) dataEntry.getValue());
                  // convert file system document to JCR document
                  convertedDocs.put(dataEntry.getKey(), document);
               }
            }
         }
      }

      for (Entry<String, Serializable> entry : convertedDocs.entrySet())
      {
         outData.put(entry.getKey(), entry.getValue());
      }
   }

   /**
    * 
    * @param docInteractionId
    * @param documentContentInfo
    * @param interaction
    * @return
    */
   public static boolean updateDocuments(String docInteractionId, AbstractDocumentContentInfo documentContentInfo,
         Interaction interaction)
   {
      Map<String, ManualActivityDocumentController> dcs = interaction.getDocumentControllers();
      if (docInteractionId.startsWith(interaction.getId()))
      {
         for (ManualActivityDocumentController dc : dcs.values())
         {
            if (docInteractionId.equals(dc.getDocInteractionId()))
            {
               dc.setDocument(documentContentInfo);
            }
         }
         return true;
      }
      return false;
   }
   
   /**
    * 
    * @param docInteractionId
    * @param opened
    * @param interaction
    * @return
    */
   public static void updateDocumentState(String docInteractionId, boolean opened, Interaction interaction)
   {
      if (interaction == null)
      {
         return;
      }

      Map<String, ManualActivityDocumentController> dcs = interaction.getDocumentControllers();
      if (docInteractionId.startsWith(interaction.getId()))
      {
         for (ManualActivityDocumentController dc : dcs.values())
         {
            if (docInteractionId.equals(dc.getDocInteractionId()))
            {
               dc.setOpened(opened);
            }
         }
      }
   }
   
   /**
    * 
    * @param interaction
    * @return
    */
   public static boolean isTypedDocumentOpen(Interaction interaction)
   {
      Map<String, ManualActivityDocumentController> dcs = interaction.getDocumentControllers();
      for (ManualActivityDocumentController dc : dcs.values())
      {
         if (dc.isOpened())
         {
            return true;
         }
      }
      return false;
   }

   /**
    * 
    * @param documents
    * @return
    */
   public static boolean deleteDocuments(List<AbstractDocumentContentInfo> documents)
   {
      for (AbstractDocumentContentInfo doc : documents)
      {
         if (doc instanceof JCRDocument)
         {
            DocumentMgmtUtility.deleteDocumentWithVersions(((JCRDocument) doc).getDocument());
         }
      }
      return true;
   }

}
