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
import javax.servlet.ServletContext;

import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;
import org.eclipse.stardust.ui.web.processportal.interaction.IppDocumentController;
import org.eclipse.stardust.ui.web.processportal.view.ActivityPanelConfigurationBean;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;
import org.eclipse.stardust.ui.web.processportal.view.manual.RawDocument;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemDocument.FileSystemDocumentAttributes;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;
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
   public static void initializeDocumentControllers(Interaction interaction,
         Map<String, ? extends Serializable> inData)
   {
      ApplicationContext context = interaction.getDefinition();

      List<DataMapping> dataMappings = context.getAllInDataMappings();

      Map<String, IppDocumentController> documentControllers = new HashMap<String, IppDocumentController>();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, ? extends Serializable> entry : inData.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               if (ModelUtils.isDocumentType(interaction.getModel(), dm))
               {
                  IppDocumentController dc;
                  if (entry.getValue() != null)
                  {
                     dc = new IppDocumentController((Document) entry.getValue(), dm,
                           interaction);
                  }
                  else
                  {
                     dc = new IppDocumentController(dm, interaction);
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
   public static void openMappedDocuments(ActivityInstance activityInstance,
         View activityPanel)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
            facesContext, InteractionRegistry.BEAN_ID);
      Interaction interaction = registry.getInteraction(Interaction.getInteractionId(activityInstance));

      for (Entry<String, IppDocumentController> documentController : interaction.getDocumentControllers()
            .entrySet())
      {
         if (ActivityPanelConfigurationBean.isAutoDisplayMappedDocuments())
         {
            PortalApplication.getInstance().setFocusView(activityPanel);
            documentController.getValue().openDocument(true);
         }
      }
      PortalApplication.getInstance().setFocusView(activityPanel);
   }

   /**
    * 
    * @param doc
    * @param interaction
    * @param servletContext
    * @return
    */
   public static FileSystemJCRDocument getFileSystemDocument(
         org.eclipse.stardust.engine.api.runtime.Document doc, Interaction interaction,
         ServletContext servletContext)
   {
      if (doc instanceof RawDocument)
      {
         RawDocument rawDocument = (RawDocument) doc;

         FileSystemDocumentAttributes fileSystemDocumentAttributes = new FileSystemDocumentAttributes();
         MessagesViewsCommonBean viewBean = (MessagesViewsCommonBean) RestControllerUtils.resolveSpringBean(
               "views_common_msgPropsBean", servletContext);

         fileSystemDocumentAttributes.setResourcePath(rawDocument.getPhysicalPath());
         fileSystemDocumentAttributes.setDocumentType(doc.getDocumentType());

         MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
               "ippMimeTypesHelper", servletContext);

         MIMEType mimeType = mimeTypesHelper.detectMimeTypeI(rawDocument.getName(),
               rawDocument.getContentType());

         fileSystemDocumentAttributes.setMimeType(mimeType);
         fileSystemDocumentAttributes.setEditable(true);

         fileSystemDocumentAttributes.setDefaultAuthor(viewBean.getString("views.documentView.properties.author.default"));
         fileSystemDocumentAttributes.setDefaultAuthor(viewBean.getString("views.documentView.properties.id.default"));

         return new FileSystemJCRDocument(fileSystemDocumentAttributes, null,
               rawDocument.getDescription(), rawDocument.getComments());
      }

      return null;
   }

   /**
    * 
    * @param interaction
    */
   public static void transformDocuments(Interaction interaction)
   {
      Map<String, Serializable> convertedDocs = new HashMap<String, Serializable>();
      Map<String, Serializable> outData = interaction.getOutDataValues();

      Map<String, IppDocumentController> dcs = interaction.getDocumentControllers();

      for (Entry<String, ? extends Serializable> dataEntry : outData.entrySet())
      {
         if (dcs.get(dataEntry.getKey()) != null)
         {
            if (dataEntry.getValue() == null)
            {
               // delete document
               DocumentHelper.deleteDocuments(dcs.get(dataEntry.getKey())
                     .getDocsTobeDeleted());
            }
            else if (dataEntry.getValue() instanceof FileSystemJCRDocument)
            {
               FileSystemJCRDocument fileSystemDoc = (FileSystemJCRDocument) dataEntry.getValue();
               // set activity instance documents folder
               String parentFolder = DocumentMgmtUtility.getTypedDocumentsFolderPath(interaction.getActivityInstance()
                     .getProcessInstance());
               fileSystemDoc.setJcrParentFolder(parentFolder);

               // convert file system document to JCR document
               JCRDocument jcrDoc = (JCRDocument) fileSystemDoc.save(fileSystemDoc.retrieveContent());
               convertedDocs.put(dataEntry.getKey(), jcrDoc.getDocument());
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
