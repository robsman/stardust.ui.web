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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Shrikant.Gangal
 * 
 */
public class ImageViewerStampsBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = -4188431712256680032L;
   private static final String STAMPS_FOLDER = "/stamps";
   private static final String STANDARD_STAMPS_FOLDER = "/documents" + STAMPS_FOLDER;
   private static final String BEAN_NAME = "imageViewerStampsBean";
   private static String MY_STAMPS_FOLDER;
   private MessagesViewsCommonBean messageBean;
   private Map<String, Map<String, List<StampData>>> stamps;
   private Map<String, List<StampData>> standardStamps = new LinkedHashMap<String, List<StampData>>();;
   private Map<String, List<StampData>> myStamps = new LinkedHashMap<String, List<StampData>>();;
   private String selectedStampId;
   private byte[] selectedStampContent;
   private String standardStampsHeading;
   private String myStampsHeading;

   /**
    * 
    */
   public ImageViewerStampsBean()
   {
      initialize();
   }

   /**
    * 
    */
   public void initialize()
   {
      messageBean = MessagesViewsCommonBean.getInstance();
      standardStampsHeading = messageBean.getString("views.imageViewerConfig.stamps.standardStamps.heading");
      myStampsHeading = messageBean.getString("views.imageViewerConfig.stamps.myStamps.heading");
      MY_STAMPS_FOLDER = DocumentMgmtUtility.getMyDocumentsPath() + STAMPS_FOLDER;
      initializeStamps();
   }

   /**
    * @return
    */
   public static ImageViewerStampsBean getCurrent()
   {
      return (ImageViewerStampsBean) org.eclipse.stardust.ui.web.common.util.FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * @param event
    */
   public void selectStamp(ActionEvent event)
   {
      StampData stampData = (StampData) event.getComponent().getAttributes().get("selectedStamp");
      selectedStampId = stampData.getDocId();
      selectedStampContent = stampData.getDocContent();
      closePopup();
   }

   /**
    * @return
    */
   public Map<String, Map<String, List<StampData>>> getStamps()
   {
      return stamps;
   }

   /**
    * @return
    */
   public Map<String, List<StampData>> getStandardStamps()
   {
      return standardStamps;
   }

   /**
    * @return
    */
   public Map<String, List<StampData>> getMyStamps()
   {
      return myStamps;
   }

   /**
    * @return
    */
   public String getSelectedStampId()
   {
      return selectedStampId;
   }

   /**
    * @param stampId
    */
   public void setSelectedStampId(String stampId)
   {
      selectedStampId = stampId;
      setSelectedStampContent(DocumentMgmtUtility.getFileContent(stampId));
   }

   /**
    * @return
    */
   public byte[] getSelectedStampContent()
   {
      return selectedStampContent;
   }

   /**
    * @param stampsContent
    */
   public void setSelectedStampContent(byte[] stampsContent)
   {
      selectedStampContent = stampsContent;
   }
   
   /**
    * @return
    */
   public boolean isStampSelected()
   {
      if(null != selectedStampContent && selectedStampContent.length > 0)
      {
         return true;
      }
      
      return false;
   }

   /**
    * @param stampsMap
    * @param parentFolder
    */
   private void addDocumentsInSubFolders(Map<String, List<StampData>> stampsMap, Folder parentFolder)
   {
      List<Folder> subFolders = parentFolder.getFolders();
      for (Folder folder : subFolders)
      {
         stampsMap.put(folder.getName(), getDocumentDownloadTokens(folder.getDocuments()));
         addDocumentsInSubFolders(stampsMap, folder);
      }
   }

   /**
    * @param docs
    * @return
    */
   private List<StampData> getDocumentDownloadTokens(List<Document> docs)
   {
      List<StampData> stamps = new ArrayList<StampData>(docs.size());
      for (Document doc : docs)
      {
         stamps.add(new StampData(doc.getId(), DocumentMgmtUtility.getFileContent(doc.getId())));
      }

      return stamps;
   }

   /**
    * 
    */
   private void initializeStamps()
   {
      initializeStampsMap(STANDARD_STAMPS_FOLDER, standardStamps);
      initializeStampsMap(MY_STAMPS_FOLDER, myStamps);
      stamps = new LinkedHashMap<String, Map<String, List<StampData>>>();
      stamps.put(standardStampsHeading, standardStamps);
      stamps.put(myStampsHeading, myStamps);
   }

   /**
    * @param stampsFolder
    * @param stampsMap
    */
   private void initializeStampsMap(String stampsFolder, Map<String, List<StampData>> stampsMap)
   {
      Folder folder = DocumentMgmtUtility.getDocumentManagementService().getFolder(stampsFolder,
            Folder.LOD_LIST_MEMBERS_OF_MEMBERS);
      if (null != folder)
      {
         stampsMap.put(messageBean.getString("views.imageViewerConfig.stamps.uncategorized"), getDocumentDownloadTokens(folder.getDocuments()));
         addDocumentsInSubFolders(stampsMap, folder);
      }
   }

   /**
    * @author Shrikant.Gangal
    * 
    */
   public class StampData
   {
      private String docId;
      private byte[] docContent;

      public StampData(String docId, byte[] docContent)
      {
         this.docId = docId;
         this.docContent = docContent;
      }

      public String getDocId()
      {
         return docId;
      }

      public void setDocId(String docId)
      {
         this.docId = docId;
      }

      public byte[] getDocContent()
      {
         return docContent;
      }

      public void setDocContent(byte[] docContent)
      {
         this.docContent = docContent;
      }
   }
}
