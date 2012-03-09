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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.DocumentAnnotations;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentTypeWrapper;
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
   private static final String YYYYMMDD_FORMAT = "yyyyMMdd";
   private static final String REALMS_FOLDER = "realms/";
   private static final String REPORT_DESIGNS = "/reports/designs";
   private static final String ARCHIVED_REPORTS = "/reports/archived";
   private static final Logger trace = LogManager.getLogger(DocumentMgmtUtility.class); 
   private static final String UNVERSIONED = "UNVERSIONED";
   
   private static final String CONTENT_TYPE = "text/plain";
   private static final String SPECIAL_CHARACTER_SET = "[\\\\/:*?\"<>|]";
   private static final String VALID_FILENAME_PATTERN = "[^\\\\/:*?\"<>|]*";

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
      document = getDocumentManagementService().versionDocument(document.getId(), CommonProperties.ZERO);
      return document;
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
         
         properties.put(CommonProperties.DESCRIPTION, description);
         properties.put(CommonProperties.COMMENTS, comments);
         doc = getDocumentManagementService().createDocument(targetId, docInfo, byteContents, null);
         doc = getDocumentManagementService().versionDocument(doc.getId(), CommonProperties.ZERO);
      }
      return doc;
   }

   public static Document updateDocument(Document existingDocument, byte[] fileData, String description, String comments)
   {
      Document doc = null;
      existingDocument.getProperties().put(CommonProperties.DESCRIPTION, description);
      existingDocument.getProperties().put(CommonProperties.COMMENTS, comments);
      
      if (null != fileData)
      {
         List versions = getDocumentVersions(existingDocument);
         Integer version = 0;
         if (null != versions)
         {
            version = versions.size() + 1;
         }
         doc = getDocumentManagementService().updateDocument(existingDocument, fileData, "", true,
               (version.toString()), false);
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
    * creates copy of the provided document
    * 
    * @param srcDoc
    * @param targetFolderPath
    * @return Document
    */
   public static Document createDocumentCopy(Document srcDoc, String targetFolderPath)
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
      document = getDocumentManagementService().versionDocument(document.getId(), CommonProperties.ZERO);
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
    * 
    * @param path
    * @return
    */
   public static Folder getFolder(String path)
   {
      Folder folder = null;
      String searchString = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(path, "/");
      searchString = DocumentMgmtUtility.replaceIllegalXpathSearchChars(searchString);
      List<Folder> newlist = getDocumentManagementService().findFoldersByName(searchString, Folder.LOD_NO_MEMBERS);
      for (Folder tempFolder : newlist)
      {
         if (path.equalsIgnoreCase(tempFolder.getPath()))
         {
            folder = tempFolder;
            break;
         }
      }

      return folder;
   }

   /**
    * Returns the folder if exist otherwise create new folder
    * 
    * @param folderPath
    * @return
    */
   public static Folder createFolderIfNotExists(String folderPath)
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
               return getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
            }
            else
            {
               Folder parentFolder = createFolderIfNotExists(parentPath);
               return getDocumentManagementService().createFolder(parentFolder.getId(),
                     DmsUtils.createFolderInfo(childName));
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
         Object o = ws.getInDataPath(processInstance.getOID(), DmsConstants.PATH_ID_ATTACHMENTS);

         DataDetails data = (DataDetails) ModelCache.findModelCache().getModel(processInstance.getModelOID()).getData(
               DmsConstants.PATH_ID_ATTACHMENTS);
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(data.getTypeId()))
         {
            processAttachments = (List<Document>) o;
         }

         if (processAttachments == null)
         {
            processAttachments = new ArrayList<Document>();
         }
      }
      return processAttachments;
   }

   /**
    * return document types from all active models
    * 
    * @return map of Map<Model, List<DocumentType>
    */
   public static Set<DocumentTypeWrapper> getDeclaredDocumentTypes()
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Collection<DeployedModel> allModels = modelCache.getAllModels();
      Set<DocumentTypeWrapper> declaredDocTypes = CollectionUtils.newSet();

      for (DeployedModel deployedModel : allModels)
      {
         List<DocumentType> documentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(deployedModel);

         for (DocumentType documentType : documentTypes)
         {
            declaredDocTypes.add(new DocumentTypeWrapper(documentType, deployedModel.getModelOID()));
         }
      }
      return declaredDocTypes;
   }
   
   /**
    * return localized label for declared document type
    * 
    * @param model
    * @param docTypeId
    * @return
    */
   public static String getDeclaredDocumentTypeLabel(DocumentTypeWrapper docTypeWrapper)
   {
      String docTypeId = docTypeWrapper.getDocumentTypeId();
      Model model = ModelCache.findModelCache().getModel(docTypeWrapper.getModelOID());
      TypeDeclaration typeDeclaration = model.getTypeDeclaration(QName.valueOf(docTypeId).getLocalPart());
      return I18nUtils.getLabel(model, model.getName()) + " - "
            + I18nUtils.getLabel(typeDeclaration, typeDeclaration.getName());
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
                  // use default encoding, should not be a problem
                  Document document = getDocumentManagementService().createDocument(folder.getId(), DmsUtils.createDocumentInfo(documentName),
                        documentContent, null);
                  getDocumentManagementService().versionDocument(document.getId(), CommonProperties.ZERO);
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
   public static String getNewDocumentName(String contentType)
   {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, getLocale());
      StringBuilder builder = new StringBuilder(MessagesViewsCommonBean.getInstance().getString(
            "views.genericRepositoryView.newFile.name"));
      builder.append(" ").append(DMSUtils.replaceAllSpecialChars(format.format(new Date(System.currentTimeMillis())))).append(".")
            .append(MimeTypesHelper.getExtension(contentType));
      return builder.toString();
   }

   /**
    * @return
    */
   public static String getNewFolderName()
   {
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, getLocale());
      return MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.newFolder.name")
            + DMSUtils.replaceAllSpecialChars(format.format(new Date(System.currentTimeMillis())));
   }

   /**
    * @return
    */
   public static String getNewReportName(String reportName)
   {
      StringBuffer reportNameB = new StringBuffer(org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils
            .substringBeforeLast(reportName, "."));
      return reportNameB.append("_").append(DateUtils.format(new Date(System.currentTimeMillis()), YYYYMMDD_FORMAT)).append(".pdf")
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
         throw new ResourceNotFoundException(MessagesViewsCommonBean.getInstance().getString(
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
            trace.error("Error in verifyExistenceOfDocumentAndShowMessage()", e);
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
      return DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) + "/" + "process-attachments";
   }
   
   
   /**
    * return Typed Documents folder path
    * 
    * @param pi
    * @return
    */
   public static String getTypedDocumentsFolderPath(ProcessInstance pi)
   {
      return DmsUtils.composeDefaultPath(pi.getOID(), pi.getStartTime()) + "/" + "specific-documents";
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
         document = createDocumentCopy(document, processAttachmentPath);
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
      UIViewRoot view = FacesContext.getCurrentInstance().getViewRoot();
      Locale locale = Locale.getDefault();
      if (view != null)
      {
         locale = view.getLocale();
      }
      return locale;
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
      return getMyDocumentsPath() + REPORT_DESIGNS; 
   }
   
   /**
    * @return
    */
   public static String getMyArchivedReportsPath()
   {
      return getMyDocumentsPath() + ARCHIVED_REPORTS; 
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
            UserService service = ServiceFactoryUtils.getUserService();
            return service.getUser(document.getOwner());
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
    * @param document
    * @return
    */
   public static ProcessInstances findProcessesHavingDocument(Document document)
   {
      long processInstanceOID = DmsUtils.getProcessInstanceOID(document.getPath());
      if (processInstanceOID > 0)
      {
         ProcessInstance processInstance = ProcessInstanceUtils.getProcessInstance(processInstanceOID);
         if (null != processInstance)
         {
            DeployedModel model = ModelUtils.getModel(processInstance.getModelOID());
            String modelID = model.getId();

            ProcessInstanceQuery processQuery = ProcessInstanceQuery.findHavingDocument(document, modelID);
            return DocumentSearchProvider.getQueryService().getAllProcessInstances(processQuery);

         }
      }
      return null;
   }
}