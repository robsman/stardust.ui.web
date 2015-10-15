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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.PortalTimestampProvider;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRVersionTracker;
import org.eclipse.stardust.ui.web.viewscommon.views.documentsearch.DocumentSearchProvider;

import com.icesoft.faces.component.inputfile.FileInfo;



/**
 * contains UI independent utility methods for document management
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentMgmtUtility
{
   public static final String DOCUMENTS = "/documents";
   public static final String PROCESS_ATTACHMENTS = "/process-attachments";
   public static final String SPECIFIC_DOCUMENTS = "/specific-documents";
   public static final String CORRESPONDENCE = "/correspondence";
   public static final String CORRESPONDENCE_OUT = "/correspondence-out-";
   
   
   private static final String YYYYMMDD_FORMAT = "yyyyMMdd";
   private static final String DATE_TIME_SECONDS = "MM/dd/yy hh:mm:ss a";
   private static final String REALMS_FOLDER = "realms/";
   private static final String REPORT_DESIGNS = "/designs";
   private static final String ARCHIVED_REPORTS = "/reports/archived";
   private static final Logger trace = LogManager.getLogger(DocumentMgmtUtility.class); 
   private static final String UNVERSIONED = "UNVERSIONED";
   private static final String SAVED_REPORTS = "/saved-reports";
   private static final String AD_HOC = "/ad-hoc";
   private static final String REPORTS_ROOT_FOLDER = "/reports";
   
   private static final String CONTENT_TYPE = "text/plain";
   private static final String SPECIAL_CHARACTER_SET = "[\\\\/:*?\"<>|\\[\\]]";
   private static final String VALID_FILENAME_PATTERN = "[^\\\\/:*?\"<>|\\[\\]]*";

   /**
    * creates blank document with default file name
    * 
    * @param targetId
    * @return
    */
   public static Document createBlankDocument(String targetId, String contentType, String fileName)
   {
      if (StringUtils.isEmpty(fileName))
      {
         fileName = getNewDocumentName(contentType);
      }
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);
      docInfo.setOwner(getUser().getAccount());
      
      if (StringUtils.isEmpty(contentType))
      {
         docInfo.setContentType(CONTENT_TYPE);
      }
      else
      {
         docInfo.setContentType(contentType);
      }
      Document document = getDocumentManagementService().createDocument(targetId, docInfo);
      // Workaround code added for some Repo, if versioning fails,send original doc
      Document documentVersioned = getDocumentManagementService().versionDocument(document.getId(), "", null);
      
      return documentVersioned != null ? documentVersioned : document;
   }

   /**
    * creates new folder based on the input parameters This function creates a single
    * folder on the provided path which must be valid
    * 
    * @param folderPath
    * @param folderName
    * @return
    */
   public static Folder createFolder(String folderPath, String folderName)
   {
      if (null != folderName)
      {
         // append
         folderPath = folderPath + "/" + folderName;
      }
      return createFolderIfNotExists(folderPath);
   }

   /**
    * @param targetId
    * @param fileInfo
    * @param description
    * @param comments
    * @param documentType
    * @return
    * @throws IOException
    */
   public static Document createDocument(String targetId, FileInfo fileInfo, String description, String comments,
         DocumentType documentType) throws IOException
   {
      String fileName = fileInfo.getFileName();
      Document doc = null;
      byte[] contents = getFileSystemDocumentContent(fileInfo.getPhysicalPath());
      if (null != contents)
      {
         doc = createDocument(targetId, fileName, contents, documentType, fileInfo.getContentType(), description,
               comments, null, null);
      }
      return doc;
   }
   
   /**
    * @param targetId
    * @param fileName
    * @param byteContents
    * @param documentType
    * @param contentType
    * @param description
    * @param comments
    * @param annotation
    * @return
    */
   public static Document createDocument(String targetId, String fileName, byte[] byteContents,
         DocumentType documentType, String contentType, String description, String comments,
         DocumentAnnotations annotation, Map<String, Object> metaDataProperties)
   {
      Document doc = null;
      if (null != byteContents)
      {
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);
         docInfo.setOwner(getUser().getAccount());
         docInfo.setContentType(contentType);
         docInfo.setDocumentAnnotations(annotation);
         docInfo.setDocumentType(documentType);
         docInfo.setDescription(description);
         Map properties = docInfo.getProperties();
         if (null == properties)
         {
            properties = new HashMap();
         }
         
         if (CollectionUtils.isNotEmpty(metaDataProperties))
         {
            properties.putAll(metaDataProperties);
         }
         
         doc = getDocumentManagementService().createDocument(targetId, docInfo, byteContents, null);
         doc = getDocumentManagementService().versionDocument(doc.getId(), comments, null);
      }
      return doc;
   }

   /**
    * creates revision - new copy of the document
    * 
    * @param existingDocument
    * @param content
    * @param description
    * @param comments
    * @return
    */
   public static Document updateDocument(Document existingDocument, byte[] content, String description, String comments)
   {
      return updateDocument(existingDocument, content, description, comments, false);
   }

   /**
    * creates document revision or overwrites it based on the input parameter, updates document owner as
    * well.
    * 
    * @param existingDocument
    * @param content
    * @param description
    * @param comments
    * @param overwrite
    * @return
    */
   public static Document updateDocument(Document existingDocument, byte[] content, String description,
         String comments, boolean overwrite)
   {
      Document doc = null;

      existingDocument.setDescription(description);
      existingDocument.setOwner(getUser().getAccount());

      if (!overwrite && !isDocumentVersioned(existingDocument))
      {
         getDocumentManagementService().versionDocument(existingDocument.getId(), "", null);
      }

      if (null != content)
      {
         doc = getDocumentManagementService().updateDocument(existingDocument, content, "", !overwrite, comments, null,
               false);
      }
      return doc;
   }
   
   /**
    * returns true if the folder(having name as input parameter 'name') already exist
    * 
    * @param parentFolder
    * @param name
    * @return
    */
   public static boolean isFolderPresent(String path, String name)
   {
      Folder parentFolder = getFolder(path);
      if (null != parentFolder)
      {
         name = stripOffSpecialCharacters(name);
         Folder finalFolder = getDocumentManagementService().getFolder(parentFolder.getId());
         List<Folder> folders = finalFolder.getFolders();
         
         for (Folder folder : folders)
         {
            if (folder.getName().equalsIgnoreCase(name))
            {
               return true;
            }
         }         
      }
      return false;
   }

   /**
    * returns document if the document(having name as input parameter 'name') already exist in the
    * folder
    * 
    * @param parentFolder path
    * @param name
    * @return
    */
   public static Document getDocument(String path, String name)
   {
      Folder parentFolder = getFolder(path);
      if (null != parentFolder)
      {
         name = stripOffSpecialCharacters(name);
         Folder folder = getDocumentManagementService().getFolder(parentFolder.getId());
         List<Document> documents = folder.getDocuments();
         for (Document document : documents)
         {
            if (document.getName().equalsIgnoreCase(name))
            {
               return document;
            }
         }
      }
      return null;
   }

   /**
    * returns true if the folder/file(having name as input parameter 'name') already exist
    * 
    * @param path
    * @param name
    * @return
    */
   public static boolean isExistingResource(String path, String name)
   {
      return (isFolderPresent(path, name) || null != getDocument(path, name));
   }

   /**
    * returns true if the extension of document changed
    * 
    * @param newDocName
    * @param oldDocName
    * @return
    */
   public static boolean isDocumentExtensionChanged(String newDocName, String oldDocName)
   {
      return !org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(newDocName, ".").equalsIgnoreCase(
            org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(oldDocName, "."));
   }

   /**
    * strips off the special characters
    * 
    * @param inputString
    * @return
    */
   public static String stripOffSpecialCharacters(String inputString)
   {
      String outputString = inputString.trim();
      outputString = outputString.replaceAll(SPECIAL_CHARACTER_SET, "");
      return outputString;
   }

   /**
    * validates filename
    * 
    * @param fileName
    * @return
    */
   public static boolean validateFileName(String fileName)
   {
      return fileName.trim().matches(VALID_FILENAME_PATTERN);
   }

   /**
    * returns DocumentManagementService
    * 
    * @return DocumentManagementService
    */
   public static DocumentManagementService getDocumentManagementService()
   {
      return SessionContext.findSessionContext().getServiceFactory().getDocumentManagementService();
   }

   /**
    * returns current user
    * 
    * @return
    */
   public static User getUser()
   {
      return SessionContext.findSessionContext().getUser();
   }

   /**
    * returns latest version history of the resource
    * @param document
    * @return
    */
   @SuppressWarnings("rawtypes")
   public static List getDocumentVersions(Document document)
   {
      if (isDocumentVersioned(document))
      {
         return getDocumentManagementService().getDocumentVersions(document.getId());
      }
      else
      {
         return null;
      }
   }

   /**
    * @param srcDoc
    * @param targetFolderPath
    * @param overWrite
    * @return
    */
   public static Document copyDocumentTo(Document srcDoc, String targetFolderPath, boolean overWrite)
   {
      Document document = getDocument(targetFolderPath, srcDoc.getName());
      if (overWrite && document != null)
      {
         DocumentManagementService dms = getDocumentManagementService();
         document = updateDocument(document, dms.retrieveDocumentContent(srcDoc.getId()), null, null, overWrite);
      }
      else
      {
         document = copyDocumentTo(srcDoc, targetFolderPath);
      }
      return document;
   }

   /**
    * creates copy of the provided document
    * 
    * @param srcDoc
    * @param targetFolderPath
    * @return Document
    */
   public static Document copyDocumentTo(Document srcDoc, String targetFolderPath)
   {
      DocumentManagementService dms = getDocumentManagementService();
      DocumentInfo docInfo = DmsUtils.createDocumentInfo(srcDoc.getName());
      docInfo.setOwner(getUser().getAccount());
      docInfo.setContentType(srcDoc.getContentType());
      docInfo.setProperties(srcDoc.getProperties());
      docInfo.setDocumentType(srcDoc.getDocumentType());
      docInfo.setDocumentAnnotations(srcDoc.getDocumentAnnotations());
      docInfo.setDescription(srcDoc.getDescription());
      Document document = dms.createDocument(targetFolderPath, docInfo, dms.retrieveDocumentContent(srcDoc.getId()),
            null);
      document = getDocumentManagementService().versionDocument(document.getId(),"", null);
      return document;
   }

   /**
    * makes a downloadable zip file of the input folder contents
    * 
    * @param rootFolder
    * @param out
    * @return
    */
   public static byte[] backupToZipFile(String resourceId, DocumentManagementService dms)
   {
      byte[] moon = "moon".getBytes();
      ZipOutputStream out = null;
      Folder rootFolder = dms.getFolder(resourceId);
      try
      {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         out = new ZipOutputStream((OutputStream) bos);

         out.setComment("Configuration backup " + "', created on " + new Date() + ", root folder '"
               + rootFolder.getPath() + "'");

         backupFolder(rootFolder, out, "", dms);

         // Complete the ZIP file
         out.close();
         moon = bos.toByteArray();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      return moon;
   }

   /**
    * returns the folder on the specified path
    * @param path
    * @return
    */
   public static Folder getFolder(String path)
   {
      return getDocumentManagementService().getFolder(path);
   }

   /**
    * Returns the folder if exist otherwise create new folder
    * 
    * @param folderPath
    * @return
    */
   public static Folder createFolderIfNotExists(String folderPath)
   {
      return createFolderIfNotExists(folderPath, null);
   }

   /**
    * @param folderPath
    * @param folderInfo
    * @return
    */
   public static Folder createFolderIfNotExists(String folderPath, FolderInfo folderInfo)
   {
      Folder folder = getDocumentManagementService().getFolder(folderPath, Folder.LOD_NO_MEMBERS);

      if (null == folder)
      {
         // folder does not exist yet, create it
         String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
         String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

         if (StringUtils.isEmpty(parentPath))
         {
            // top-level reached
            if (folderInfo == null)
            {
               folderInfo = DmsUtils.createFolderInfo(childName);
            }
            else
            {
               folderInfo.setName(childName);
            }

            return getDocumentManagementService().createFolder("/", folderInfo);
         }
         else
         {
            Folder parentFolder = createFolderIfNotExists(parentPath);
            if (folderInfo == null)
            {
               folderInfo = DmsUtils.createFolderInfo(childName);
            }
            else
            {
               folderInfo.setName(childName);
            }
            return getDocumentManagementService().createFolder(parentFolder.getId(), folderInfo);
         }
      }
      else
      {
         return folder;
      }
   }
   
   /**
    * returns documents attached to provided process instance
    * 
    * @param processInstance
    * @return
    */
   public static List<Document> getProcesInstanceDocuments(ProcessInstance processInstance)
   {
      List<Document> processAttachments = null;

      boolean supportsProcessAttachments = existsProcessAttachmentsDataPath(processInstance);
      if (supportsProcessAttachments)
      {
         WorkflowService ws = ServiceFactoryUtils.getWorkflowService();
         
         Model model = ModelCache.findModelCache().getModel(processInstance.getModelOID());
         ProcessDefinition pd = model.getProcessDefinition(processInstance.getProcessID());
         String dataId = pd.getDataPath(DmsConstants.PATH_ID_ATTACHMENTS).getData();

         if (StringUtils.isEmpty(dataId))
         {
            dataId = DmsConstants.PATH_ID_ATTACHMENTS;
         }

         DataDetails data = (DataDetails) model.getData(dataId);
         
         Object o = ws.getInDataPath(processInstance.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);
         
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(data.getTypeId()))
         {
            processAttachments = (List<Document>) o;
         }

         if (processAttachments == null)
         {
            processAttachments = new ArrayList<Document>();
         }
         else
         {
               processAttachments = filterWithReadAccess(processAttachments);
         }
      }
      return processAttachments;
   }
   
   /**
    * Filter the documents for which user do not have READ access
    * 
    * @param documentsList
    * @return
    */
  private static List<Document> filterWithReadAccess(List<Document> documentsList)
   {
      List<Document> updatedDocumentList = CollectionUtils.newArrayList();
      for (Document doc : documentsList)
      {
         if (null != getDocumentManagementService().getDocument(doc.getId()))
         {
            updatedDocumentList.add(doc);
         }
      }
      return updatedDocumentList;
   }

   /**
    * reads data from inputstream
    * 
    * @param stream
    * @return
    * @throws Exception
    */
   private static byte[] readEntryData(ZipInputStream stream) throws Exception
   {
      // create a buffer to improve performance
      byte[] buffer = new byte[2048];

      // Once we get the entry from the stream, the stream is
      // positioned read to read the raw data, and we keep
      // reading until read returns 0 or less.
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try
      {
         int len = 0;
         while ((len = stream.read(buffer)) > 0)
         {
            output.write(buffer, 0, len);
         }
         return output.toByteArray();
      }
      finally
      {
         // must always close the output file
         if (output != null)
         {
            output.close();
         }
      }
   }

   /**
    * return true if the provided process instance have attached documents
    * 
    * @param processInstance
    * @return
    */
   private static boolean existsProcessAttachmentsDataPath(ProcessInstance processInstance)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Model model = modelCache.getModel(processInstance.getModelOID());
      ProcessDefinition pd = model != null ? model.getProcessDefinition(processInstance.getProcessID()) : null;
      List<DataPath> dataPaths = pd.getAllDataPaths();

      for (DataPath dataPath : dataPaths)
      {
         if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId()) && dataPath.getDirection().equals(Direction.IN))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * recursive function to backup the folders
    * 
    * @param parentFolder
    * @param out
    * @param subfolder
    * @throws Exception
    */
   private static void backupFolder(Folder parentFolder, ZipOutputStream out, String subfolder,
         DocumentManagementService dms) throws Exception
   {
      List<Document> documents = parentFolder.getDocuments();
      int documentCount = documents.size();
      for (int i = 0; i < documentCount; i++)
      {
         Document document = documents.get(i);
         // Add ZIP entry to output stream.
         String entryName = (subfolder + document.getName()).replace('/', File.separatorChar);
         ZipEntry zipEntry = new ZipEntry(entryName);
         zipEntry.setSize(document.getSize());
         zipEntry.setTime(document.getDateLastModified().getTime());
         out.putNextEntry(zipEntry);
         out.write(dms.retrieveDocumentContent(document.getId()));
         // Complete the entry
         out.closeEntry();
      }

      List<Folder> folders = parentFolder.getFolders();
      int folderCount = folders.size();
      for (int i = 0; i < folderCount; i++)
      {
         Folder folder = folders.get(i);
         // re-get the folder with the subfolders
         folder = dms.getFolder(folder.getId(), Folder.LOD_LIST_MEMBERS);
         // recurse
         backupFolder(folder, out, subfolder + folder.getName() + File.separator, dms);
      }

      if (documents.size() == 0 && folders.size() == 0)
      {
         // create an entry for an empty folder
         String entryName = subfolder.replace('/', File.separatorChar);
         out.putNextEntry(new ZipEntry(entryName));
         out.closeEntry();
      }
   }

   public static byte[] getFileSystemDocumentContent(String physicalPath)
   {
      FileInputStream is = null;
      try
      {
         is = new FileInputStream(physicalPath);
         return getFileContent(is);
      }
      catch (FileNotFoundException e)
      {
         ExceptionHandler.handleException(e);
      }
      catch (IOException e)
      {
         ExceptionHandler.handleException(e);
      }
      return null;
   }
   
   /**
    * converts input stream into byte array
    * 
    * @param is
    * @return
    * @throws IOException
    */
   public static byte[] getFileContent(InputStream is) throws IOException
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try
      {
         byte[] content = new byte[1024];
         int r = -1;
         while ((r = is.read(content)) != -1)
         {
            out.write(content, 0, r);
         }
         out.flush();
         return out.toByteArray();
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
            }
         }

      }
   }
   
   /**
    * Retrieves the byte array for the document identified by document id
    * 
    * @param is
    * @return
    * @throws IOException
    */
   public static byte[] getFileContent(String docId)
   {
      return getDocumentManagementService().retrieveDocumentContent(docId);
   }

   /**
    * deletes the existing child folders and documents
    * 
    * @param parentFolder
    */
   private static void cleanupFolder(Folder parentFolder)
   {
      getDocumentManagementService().removeFolder(parentFolder.getId(), true);
   }

   /**
    * Recursive function to load/create folders based on the zip file information
    * 
    * @param partitionFolderPath
    * @param sourceFile
    * @throws Exception
    */
   public static void loadFromZipFile(String partitionFolderPath, File sourceFile) throws Exception
   {
      // open the zip file stream
      ZipInputStream stream = new ZipInputStream(new FileInputStream(sourceFile));
      cleanupFolder(getFolder(partitionFolderPath));
      partitionFolderPath = partitionFolderPath + "/";
      try
      {
         // now iterate through each item in the stream. The get next
         // entry call will return a ZipEntry for each file in the
         // stream
         ZipEntry entry;
         while ((entry = stream.getNextEntry()) != null)
         {
            // take care of Windows paths
            String relativeEntryPath = entry.getName().replace('\\', '/');

            // TODO (ab) how else we can see that this is a folder and not a file
            // (determining this
            // based on the size is dangerous because files also can be empty!
            if ((relativeEntryPath.endsWith("/") || relativeEntryPath.endsWith("\\")))
            {
               // this is only an empty folder, create it
               relativeEntryPath = relativeEntryPath.substring(0, relativeEntryPath.length() - 1);
               createFolderIfNotExists(partitionFolderPath + relativeEntryPath);
            }
            else
            {
            	// this is a file, put it as a document
               if (relativeEntryPath.contains("/"))
               {
                  String folderPath = partitionFolderPath + relativeEntryPath.substring(0, relativeEntryPath.lastIndexOf('/'));
                  String documentName = relativeEntryPath.substring(relativeEntryPath.lastIndexOf('/') + 1);
                  Folder folder = createFolderIfNotExists(folderPath);
                  // TODO (CRNT-10654) can not use upload servlet here if content size
                  // exceeds threshold
                  // (carnot.properties "Carnot.Configuration.ContentStreamingThreshold")
                  // since the base url of the dms-content servlet is unknown
                  byte[] documentContent = readEntryData(stream);
                  
                  //set contentType
                  DocumentInfo docInfo = DmsUtils.createDocumentInfo(documentName);
                  docInfo.setContentType(MimeTypesHelper.detectMimeType(documentName, null).getType());
                  
                  // use default encoding, should not be a problem
                  getDocumentManagementService().createDocument(folder.getId(), docInfo, documentContent, null);
               }
            }
         }
      }
      finally
      {
         // we must always close the zip file.
         stream.close();
      }
   }

   /**
    * @return
    */
   public static String generateUniqueId(String anyString)
   {
      Random o = new Random();
      return anyString + o.nextInt(10000);
   }

   /**
    * @param document
    * @return
    */
   public static boolean isDocumentVersioned(Document document)
   {
      if (UNVERSIONED.equals(document.getRevisionId()))
      {
         return false;
      }
      return true;
   }

   /**
    * @return
    */
   public static String getTimeStampString()
   {
      return DMSUtils.replaceAllSpecialChars(DateUtils.format(new Date(PortalTimestampProvider.getTimeStampValue()), DATE_TIME_SECONDS));
   }

   /**
    * @return
    */
   public static String getNewDocumentName(String contentType)
   {
      StringBuilder builder = new StringBuilder(MessagesViewsCommonBean.getInstance().getString(
            "views.genericRepositoryView.newFile.name"));
      builder.append(" ").append(getTimeStampString()).append(".").append(MimeTypesHelper.getExtension(contentType));
      return builder.toString();
   }

   /**
    * append a document name with timestamp
    * 
    * @param documentName
    * @return
    */
   public static String appendTimeStamp(String documentName)
   {
      String part1 = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringBeforeLast(documentName, ".");
      String part2 = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(documentName, ".");
      return part1 + " " + getTimeStampString() + "." + part2;
   }
   
   /**
    * @return
    */
   public static String getNewFolderName()
   {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, getLocale());
      return MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.newFolder.name")
            + DMSUtils.replaceAllSpecialChars(format.format(new Date(PortalTimestampProvider.getTimeStampValue())));
   }

   /**
    * @return
    */
   public static String getNewReportName(String reportName)
   {
      StringBuffer reportNameB = new StringBuffer(org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils
            .substringBeforeLast(reportName, "."));
      return reportNameB.append("_").append(DateUtils.format(new Date(PortalTimestampProvider.getTimeStampValue()), YYYYMMDD_FORMAT)).append(".pdf")
            .toString();
   }

   /**
    * @param documentId
    * @return
    * @throws ResourceNotFoundException
    */
   public static Document getDocument(String documentId) throws ResourceNotFoundException
   {
      Document document = getDocumentManagementService().getDocument(documentId);
      if (null == document)
      {
         throw new I18NException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.documentNotFound"));
      }
      return document;
   }
   
   /**
    * @param folderId
    * @return
    * @throws ResourceNotFoundException
    */
   public static Folder getFolderById(String folderId) throws ResourceNotFoundException
   {
      Folder folder = getDocumentManagementService().getFolder(folderId);
      if (null == folder)
      {
         throw new ResourceNotFoundException(MessagesViewsCommonBean.getInstance().getString(
               "views.myDocumentsTreeView.folderNotFound"));
      }
      return folder;
   }

   /**
    * @param documentId
    * @param message
    * @param t
    * @return
    */
   public static boolean verifyExistanceOfDocumentAndShowMessage(String documentId, String message, Exception e)
   {
      try
      {
         DocumentMgmtUtility.getDocument(documentId);
         if (StringUtils.isNotEmpty(message) || null != e)
         {
            ExceptionHandler.handleException(e, message);
            return false;
         }
      }
      catch (ResourceNotFoundException dnfe)
      {
         ExceptionHandler.handleException(e,
               MessagesViewsCommonBean.getInstance().getString("views.myDocumentsTreeView.documentNotFound"));
         return false;
      }
      return true;
   }
   

   /**
    * @param folderId
    * @param message
    * @param t
    * @return
    */
   public static boolean verifyExistenceOfFolderAndShowMessage(String folderId, String message, Throwable t)
   {
      try
      {
         DocumentMgmtUtility.getFolderById(folderId);
         if (StringUtils.isNotEmpty(message) || null != t)
         {
            trace.error("Error in verifyExistanceOfFolderAndShowMessage()", t);
            MessageDialog.addErrorMessage(message + "\n" + (null != t ? t.getMessage() : ""));
            return false;
         }
      }
      catch(ResourceNotFoundException dnfe)
      {
         ExceptionHandler.handleException(dnfe);
         return false;
      }

      return true;
   }
   
   /**
    * return process attachment folder path
    * 
    * @param pi
    * @return
    */
   public static String getProcessAttachmentsFolderPath(ProcessInstance pi)
   {
      return DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) + PROCESS_ATTACHMENTS;
   }
   
   
   /**
    * return Typed Documents folder path
    * 
    * @param pi
    * @return
    */
   public static String getTypedDocumentsFolderPath(ProcessInstance pi)
   {
      return DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) + SPECIFIC_DOCUMENTS;
   }
   
   /**
    * @param processInstance
    * @return
    */
   public static String getCorrespondenceFolderPath(ProcessInstance processInstance)
   {
      if (processInstance.getOID() != processInstance.getScopeProcessInstanceOID())
      {
         processInstance = ProcessInstanceUtils.getProcessInstance(processInstance.getScopeProcessInstanceOID());
      }
      return DmsUtils.composeDefaultPath(processInstance.getOID(), processInstance.getStartTime()) + CORRESPONDENCE;
   }

   /**
    * @param ai
    * @return
    */
   public static String getCorrespondenceOutFolderPath(ActivityInstance ai)
   {
      return getCorrespondenceFolderPath(ai.getProcessInstance()) + CORRESPONDENCE_OUT + ai.getOID();
   }
   
   /**
    * adds documents to process instance
    * 
    * @param processInstance
    * @param documentList
    * @return
    */
   public static void addDocumentsToProcessInstance(ProcessInstance processInstance, List<Document> documentList)
   {
      String processAttachmentPath = getProcessAttachmentsFolderPath(processInstance);
      createFolderIfNotExists(processAttachmentPath);
      for (Document document : documentList)
      {
         document = copyDocumentTo(document, processAttachmentPath);
         DMSHelper.addAndSaveProcessAttachment(processInstance, document);
      }
   }

   /**
    * This method checks if process instance is active and process attachments are allowed
    * to process instance
    * 
    * @param processInstance
    * @return
    */
   public static boolean isProcessAttachmentAllowed(ProcessInstance processInstance)
   {
      boolean isProcessValid = false;
      if (null != processInstance)
      {
         boolean supportsProcessAttachments = existsProcessAttachmentsDataPath(processInstance);
         ProcessInstanceState state = processInstance.getState();
         if (supportsProcessAttachments && state.getValue() == ProcessInstanceState.ACTIVE)
         {
            isProcessValid = true;
         }
      }
      return isProcessValid;
   }
   /**
    * This method checks if process defination supports attachment
    * to process instance
    * 
    * @param processDefinationFQID
    * @return
    */

   public static boolean isProcessAttachmentAllowed(DeployedModel model, String processDefinationId)
   {
      ProcessDefinition pd = model.getProcessDefinition(processDefinationId);
      if (pd == null)
      {
         trace.error("No Process found with processDefinationId: " + processDefinationId);
         return false;
      }
      List<DataPath> dataPaths = pd.getAllDataPaths();
      for (DataPath dataPath : dataPaths)
      {
         if (DmsConstants.PATH_ID_ATTACHMENTS.equals(dataPath.getId()) && dataPath.getDirection().equals(Direction.IN))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Returns the list of duplicate documents, documents are compared by name
    * 
    * @param path
    * @param documentList
    * @return
    */
   public static List<Document> getDuplicateDocuments(ProcessInstance processInstance, List<Document> documentList)
   {
      List<Document> processAttachments = getProcesInstanceDocuments(processInstance);
      List<Document> duplicateDocList = new ArrayList<Document>();

      for (Document document : documentList)
      {
         for (Document processAttachment : processAttachments)
         {
            if (processAttachment.getName().equalsIgnoreCase(document.getName()))
            {
               duplicateDocList.add(document);
            }
         }
      }
      return duplicateDocList;
   }
   
   private static Locale getLocale()
   {
      return FacesUtils.getLocaleFromRequest();
   }
   
   /**
    * @return my documents path
    */
   public static String getMyDocumentsPath()
   {
      User user = DocumentMgmtUtility.getUser();
      return (new StringBuffer("/").append(REALMS_FOLDER).append(user.getRealm().getId()).append("/").append(
            DocumentRepositoryFolderNames.USERS_FOLDER).append(user.getAccount()).append(DOCUMENTS)).toString();
   }
   
   /**
    * @return my documents path
    */
   public static String getMyDocumentsPath(SessionContext sessionContext)
   {
      User user = sessionContext.getUser();
      return (new StringBuffer("/").append(REALMS_FOLDER).append(user.getRealm().getId()).append("/").append(
            DocumentRepositoryFolderNames.USERS_FOLDER).append(user.getAccount()).append(DOCUMENTS)).toString();
   }   
   /**
    * @return
    */
   public static String getMyReportDesignsPath()
   {
      return getMyDocumentsPath() + REPORTS_ROOT_FOLDER + REPORT_DESIGNS; 
   }
   
   /**
    * @return
    */
   public static String getMyArchivedReportsPath()
   {
      return getMyDocumentsPath() + ARCHIVED_REPORTS; 
   }
   
   /**
    * @return reporting base url
    */
   public static String getReportingBaseURL()
   {
      String baseUrl = (String) FacesContext.getCurrentInstance().getExternalContext()
            .getInitParameter(Constants.CONTEXT_PARAM_REPORTING_URI);

      if (org.eclipse.stardust.common.StringUtils.isEmpty(baseUrl))
      {
         baseUrl = FacesUtils.getServerBaseURL();
      }
      return baseUrl;
   }
   
   /**
    * Validates the file name for JCR
    * 
    * @param parentFolderPath
    * @param fileName
    * @return
    */
   public static String isFileNameValid(String parentFolderPath, String fileName)
   {
      String msgKey = null;
      if (StringUtils.isEmpty(fileName))
      {
         msgKey = "views.common.name.error";
      }
      else if (!DocumentMgmtUtility.validateFileName(fileName))
      {
         msgKey = "views.common.invalidCharater.error";
      }
      if (DocumentMgmtUtility.isFolderPresent(parentFolderPath, fileName))
      {
         msgKey = "views.genericRepositoryView.folderExist.error";
      }
      if (null != DocumentMgmtUtility.getDocument(parentFolderPath, fileName))
      {
         msgKey = "views.genericRepositoryView.fileExist.error";
      }
      return msgKey;
   }
   
   /**
    * @param documentOID
    * @return
    */
   public static String getDocumentDownloadURL(String documentOID, HttpServletRequest request, DocumentManagementService dms)
   {     
      return new StringBuffer(request.getScheme()).append("://").append(request.getServerName()).append(":").append(
            request.getServerPort()).append(request.getContextPath()).append("/dms-content/").append(dms.requestDocumentContentDownload(documentOID)).toString();
   }
   
   /**
    * returns owner of the document
    * 
    * @param document
    * @return
    */
   public static User getOwnerOfDocument(Document document)
   {
      if (null != document && StringUtils.isNotEmpty(document.getOwner()))
      {
         try
         {
            return UserUtils.getUser(document.getOwner());
         }
         catch (Exception e)
         {
            trace.info("Invalid User Id " + document.getOwner() + " associated to the document " + document.getPath());
         }
      }
      return null;
   }
   
  /**
   * 
   * @param filesize
   * @return
   */
   public static String getHumanReadableFileSize(Long filesize)
   {
      double fileLengthLong = Double.parseDouble(filesize.toString());
      BigDecimal bd = null;
      String howBig = null;
      if (fileLengthLong <= 1024)
      {
         bd = new BigDecimal(Math.abs(fileLengthLong));
         howBig = " bytes";
      }
      if (fileLengthLong >= 1024 && fileLengthLong < 1048576)
      {
         bd = new BigDecimal(Math.abs((fileLengthLong / 1024)));
         bd = bd.setScale(1, BigDecimal.ROUND_DOWN);
         howBig = " KB";
      }
      if (fileLengthLong >= 1048576)
      {
         bd = new BigDecimal(Math.abs((fileLengthLong / (1024 * 1024))));
         bd = bd.setScale(1, BigDecimal.ROUND_DOWN);
         howBig = " MB";
      }
      return bd.toString() + howBig;
   }
   
   /**
    * @param s
    * @return
    */
   public static String replaceIllegalXpathSearchChars(String s)
   {
      return s.replaceAll("'", "%");
   }

   /**
    * method to delete all old document versions of given document
    */
   public static void deleteOldVersions(Document document) throws DocumentManagementServiceException
   {
      DocumentManagementService documentManagementService = ServiceFactoryUtils.getDocumentManagementService();
      JCRVersionTracker tracker = new JCRVersionTracker(document);
      while (tracker.hasPreviousVersion())
      {
         JCRDocument version = tracker.shiftToPreviousVersion();
         documentManagementService.removeDocumentVersion(document.getId(), version.getId());
      }
   }

   /**
    * method to delete a document and all it's versions
    */
   public static void deleteDocumentWithVersions(Document document) throws DocumentManagementServiceException
   {
      deleteOldVersions(document);
      
      DocumentManagementService documentManagementService = ServiceFactoryUtils.getDocumentManagementService();
      documentManagementService.removeDocument(document.getId());
   }
   
   /**
    * method return processes by document
    * 
    * @param document
    * @return
    */
   public static ProcessInstances findProcessesHavingDocument(Document document)
   {
      ProcessInstanceQuery processQuery = ProcessInstanceQuery.findHavingDocument(document);
      return DocumentSearchProvider.getQueryService().getAllProcessInstances(processQuery);
   }
   
   /**
    * @return
    */
   public static String getPublicReportDefinitionsPath()
   {
      return REPORTS_ROOT_FOLDER + REPORT_DESIGNS; 
   }
   
   /**
    * @return
    */
   public static String getPrivateSavedReportsPath()
   {
      return getMyDocumentsPath() + REPORTS_ROOT_FOLDER + SAVED_REPORTS; 
   }
   
   /**
    * @return
    */
   public static String getPublicSavedReportsPath()
   {
      return REPORTS_ROOT_FOLDER + SAVED_REPORTS; 
   }
   
   /**
    * @return
    */
   public static String getPrivateSavedReportsAdHocPath()
   {
      return getPrivateSavedReportsPath() + AD_HOC; 
   }
   
   /**
    * @return
    */
   public static String getPublicSavedReportsAdHocPath()
   {
      return getPublicSavedReportsPath() + AD_HOC; 
   }
   
   /**
    * @return
    */
   public static List<Grant> getRoleOrgReportDefinitionsGrants()
   {
      User user = DocumentMgmtUtility.getUser();
      List<Grant> allGrants = user.getAllGrants();
      return allGrants; 
   }
   
   /**
    * @return
    */
   public static String getRoleOrgReportDefinitionsPath(String qualifiedId)
   {
      return REPORTS_ROOT_FOLDER + "/" + qualifiedId  + REPORT_DESIGNS; 
   }
   
   /**
    * @return
    */
   public static String getRoleOrgSavedReportsPath(String qualifiedId, boolean isAdHoc)
   {
      return (isAdHoc) ? REPORTS_ROOT_FOLDER + "/" + qualifiedId  + SAVED_REPORTS + AD_HOC : 
      REPORTS_ROOT_FOLDER + "/" + qualifiedId  + SAVED_REPORTS; 
   }
   
   /**
    * @param id
    * @return
    */
   public static String checkAndGetCorrectResourceId(String id){
      if (!id.startsWith("{") && !id.startsWith("/"))
      {
         id = "/" + id;
      }
      return id;
   }
}