/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.beans.spring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.query.UnsupportedFilterException;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.reporting.common.JsonMarshaller;
import org.eclipse.stardust.ui.web.reporting.core.IModelService;
import org.eclipse.stardust.ui.web.reporting.core.ReportingServicePojo;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;


@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReportingServiceBean
{
   @Resource
   UserProvider userProvider;

   private static final Logger trace = LogManager.getLogger(ReportingServiceBean.class);

   private static final String PUBLIC_REPORT_DEFINITIONS_DIR = "/reports";
   private static final String PARTICIPANTS_REPORT_DEFINITIONS_DIR = "/participants/reports/";

   private DocumentManagementService documentManagementService;
   private UserService userService;

   /**
    * Stores uncommitted changes.
    */
   private Map<String, JsonObject> reportDefinitionCache;
   private Map<String, JsonObject> reportDefinitionJsons;

   //a bean with name "modelService" already exists in the spring context
   //see org.eclipse.stardust.ui.web.modeler.service.ModelService
   //to not accidently inject this bean(for example by naming the property
   //"modelService") - use explicit spring bean name or a different property name
   @Resource(name=ModelServiceBean.BEAN_NAME)
   private IModelService modelService;

   @Resource
   private SessionContext sessionContext;

   @Resource
   private ServletContext servletContext;

   private ReportingServicePojo reportingServicePojo;

   private JsonMarshaller jsonMarshaller;

   public ReportingServiceBean()
   {
      jsonMarshaller = new JsonMarshaller();
      reportDefinitionCache = new HashMap<String, JsonObject>();
      reportDefinitionJsons = new HashMap<String, JsonObject>();
   }

   private ReportingServicePojo getReportingServicePojo()
   {
      if(reportingServicePojo == null)
      {
         reportingServicePojo = new ReportingServicePojo(getServiceFactory(), modelService);
      }

      return reportingServicePojo;
   }

   private ServiceFactory getServiceFactory()
   {
      return sessionContext.getServiceFactory();
   }

   /**
    *
    * @return
    */
   private DocumentManagementService getDocumentManagementService()
   {
      if (documentManagementService == null)
      {
         documentManagementService = getServiceFactory().getDocumentManagementService();
      }

      return documentManagementService;
   }

   /**
    *
    * @return
    */
   private UserService getUserService()
   {
      if (userService == null)
      {
         userService = getServiceFactory().getUserService();
      }

      return userService;
   }

   /**
    *
    * @return
    */
   public JsonObject getModelData()
   {
      return getReportingServicePojo().getModelData();
   }

   /**
    *
    * @return
    * @throws ParseException
    * @throws UnsupportedFilterException
    */
   public JsonObject getReportData(JsonObject reportJson) throws UnsupportedFilterException, ParseException
   {
      return getReportingServicePojo().getReportData(reportJson);
   }

   /**
    * Might be invoked for saving of multiple Report Definitions or directly (whereby json
    * contains a top-level element "report").
    *
    * @param json
    */
   public JsonObject saveReportDefinition(JsonObject reportJson)
   {
      try
      {

         JsonObject storageJson = reportJson.get("storage").getAsJsonObject();
         String name = reportJson.get("name").getAsString();
         String location = storageJson.get("location").getAsString();

         Folder folder = null;

         if (location.equals("publicFolder"))
         {
            folder = findOrCreateFolder(PUBLIC_REPORT_DEFINITIONS_DIR);
         }
         else if (location.equals("personalFolder"))
         {
            folder = findOrCreateFolder(getUserDocumentFolderPath());
         }
         else if (location.equals("participantFolder"))
         {
            folder = findOrCreateFolder(getParticipantDocumentFolderPath(storageJson.get("participant").getAsString()));
         }

         // Mark Report Definition as saved

         reportJson.get("storage").getAsJsonObject().addProperty("state", "saved");

         saveReportDefinitionDocument(reportJson, folder, name);

         // Add to cache

         reportDefinitionJsons.put(folder.getPath() + "/" + name, reportJson);

         trace.debug(reportDefinitionJsons);

         return reportJson;
      }
      finally
      {
      }
   }

   /**
    *
    * @param json
    */
   public void saveReportDefinitions(JsonObject json)
   {
      for (Map.Entry<String, JsonElement> entry : json.entrySet())
      {
         saveReportDefinition(entry.getValue().getAsJsonObject());
      }
   }

   /**
    *
    * @param json
    */
   public void renameReportDefinition(String path, String name)
   {
      try
      {
         // Replace in cache

         JsonObject reportJson = reportDefinitionJsons.remove(path);

         reportDefinitionJsons.put(renameReportDefinitionDocument(path, name), reportJson);
      }
      finally
      {
      }
   }

   /**
    *
    * @param json
    */
   public JsonObject loadReportDefinition(String path)
   {
      if (reportDefinitionJsons.get(path) != null)
      {
         return reportDefinitionJsons.get(path);
      }
      else
      {
         Document document = getDocumentManagementService().getDocument(path);

         if (document != null)
         {
            return jsonMarshaller.readJsonObject(new String(getDocumentManagementService().retrieveDocumentContent(
                  document.getId())));
         }
         else
         {
            throw new ObjectNotFoundException("Document " + path + " does not exist.");
         }
      }
   }

   /**
    *
    * @param json
    * @return
    */
   public JsonObject deleteReportDefinition(String reportPath)
   {
      try
      {
         trace.debug("deleting report template: " + reportPath);
         getDocumentManagementService().removeDocument(reportPath);
         return new JsonObject();
      }
      finally
      {
      }
   }

   /**
    * Returns the folder if exist otherwise create new folder
    *
    * @param folderPath
    * @return
    */
   public Folder findOrCreateFolder(String folderPath)
   {
      Folder folder = getDocumentManagementService().getFolder(folderPath);

      if (null == folder)
      {
         // folder does not exist yet, create it
         String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
         String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

         if (StringUtils.isEmpty(parentPath))
         {
            // Top-level reached

            return getDocumentManagementService().createFolder("/", DmsUtils.createFolderInfo(childName));
         }
         else
         {
            Folder parentFolder = findOrCreateFolder(parentPath);

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
    *
    * @return
    */
   private String getUserDocumentFolderPath()
   {
      return "/realms/" + getUserService().getUser().getRealm().getId() + "/users/"
            + getUserService().getUser().getId() + "/documents/reports/designs";
   }

   /**
    *
    * @return
    */
   private String getParticipantDocumentFolderPath(String participant)
   {
      return PARTICIPANTS_REPORT_DEFINITIONS_DIR + participant;
   }

   /**
    * TODO Split off persistence part
    * @param servletContext
    * @param httpRequest
    */
   public JsonObject loadReportDefinitions()
   {
      try
      {
         Folder publicFolder = findOrCreateFolder(PUBLIC_REPORT_DEFINITIONS_DIR);
         Folder personalFolder = findOrCreateFolder(getUserDocumentFolderPath());
         Folder participantFolder = findOrCreateFolder(PARTICIPANTS_REPORT_DEFINITIONS_DIR);

         JsonObject rootFolderJson = new JsonObject();
         JsonArray subFoldersJson = new JsonArray();
         rootFolderJson.add("subFolders", subFoldersJson);

         subFoldersJson.add(getReportDefinitions(publicFolder, "Public Report Definitions")); // I18N
         subFoldersJson.add(getReportDefinitions(personalFolder, "Personal Report Definitions")); // I18N

         //Prepare Participants subfolders
         JsonObject participantsFolderJson = new JsonObject();
         participantsFolderJson.addProperty("name", "Participants Report Definitions"); // I18N
         participantsFolderJson.addProperty("id", participantFolder.getId());
         participantsFolderJson.addProperty("path", participantFolder.getPath());

         subFoldersJson.add(participantsFolderJson);

         List<Folder> subfolders = participantFolder.getFolders();
         User loggedInUser = userProvider.getUser();

         //add relevant participants
         JsonArray participantFoldersJson = new JsonArray();
         participantsFolderJson.add("subFolders", participantFoldersJson);

         for (Folder participantSubFolder : subfolders)
         {
            participantSubFolder = findOrCreateFolder(participantSubFolder.getPath());

            // check the permissions to current user
            if (loggedInUser.isInRole(participantSubFolder.getName()) || loggedInUser.isAdministrator())
            {
               Participant  participant = modelService.getParticipant(participantSubFolder.getName(), null);
               JsonObject participantFolderJson = getReportDefinitions(participantSubFolder, participant.getName());

               if (participantFolderJson != null)
               {
                  participantFoldersJson.add(participantFolderJson);
               }
            }
         }


         return rootFolderJson;
      }
      catch (Exception e)
      {
         //TODO: remove later
         e.printStackTrace();
         trace.debug("Error Occurred while loading report definitions");
         return null;
      }
      finally
      {
      }
   }


   /**
    * @param folder
    * @param label
    * @return
    */
   private JsonObject getReportDefinitions(Folder folder, String label)
   {
      JsonObject folderJson = new JsonObject();

      if (StringUtils.isNotEmpty(label))
      {
         folderJson.addProperty("name", label);
         folderJson.addProperty("id", folder.getId());
         folderJson.addProperty("path", folder.getPath());
      }

      if (folder != null)
      {
         JsonArray reportDefinitionsJson = new JsonArray();
         folderJson.add("reportDefinitions", reportDefinitionsJson);

         @SuppressWarnings("unchecked")
         List<Document> candidateReportDefinitionsDocuments = folder.getDocuments();

         for (Document reportDefinitionDocument : candidateReportDefinitionsDocuments)
         {
            if (reportDefinitionDocument.getName().endsWith(".bpmrpt"))
            {
               String content = new String(getDocumentManagementService().retrieveDocumentContent(
                     reportDefinitionDocument.getId()));

               JsonObject reportDefinitionJson = jsonMarshaller.readJsonObject(content);

               reportDefinitionsJson.add(reportDefinitionJson);
               reportDefinitionJson.addProperty("id", reportDefinitionDocument.getId());
               reportDefinitionJson.addProperty(
                     "name",
                     reportDefinitionDocument.getName().substring(0,
                           reportDefinitionDocument.getName().indexOf(".bpmrpt")));
               reportDefinitionJson.addProperty("path", reportDefinitionDocument.getPath());
            }
         }
      }

      return folderJson;
   }

   /**
	 *
	 */
   private void saveReportDefinitionDocument(JsonObject reportDefinitionJson, Folder folder, String name)
   {
      String reportContent = reportDefinitionJson.toString();
      String path = folder.getPath() + "/" + name + ".bpmrpt";
      Document reportDesignDocument = getDocumentManagementService().getDocument(path);

      if (null == reportDesignDocument)
      {
         DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name + ".bpmrpt");

         documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
         documentInfo.setContentType(MimeTypesHelper.DEFAULT.getType());

         reportDesignDocument = getDocumentManagementService().createDocument(folder.getPath(), documentInfo,
               reportContent.getBytes(), null);

         // Create initial version

         // getDocumentManagementService().versionDocument(
         // reportDesignDocument.getId(), null);
      }
      else
      {
         getDocumentManagementService().updateDocument(reportDesignDocument, reportContent.getBytes(), null, false,
               null, false);
      }
   }

   /**
    * TODO Should be more elegant
    *
    * @param path
    */
   private String renameReportDefinitionDocument(String path, String name)
   {
      Document reportDefinitionDocument = getDocumentManagementService().getDocument(path);
      String folderPath = path.substring(0, path.lastIndexOf('/'));
      DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name + ".bpmrpt");

      documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
      documentInfo.setContentType(MimeTypesHelper.DEFAULT.getType());

      byte[] content = getDocumentManagementService().retrieveDocumentContent(path);

      getDocumentManagementService().createDocument(folderPath, documentInfo, content, null);
      getDocumentManagementService().removeDocument(reportDefinitionDocument.getId());

      return folderPath + "/" + name + ".bpmrpt";
   }

   /**
    * Retrieves external join data via REST and creates a map with the join key as key and
    * a map with all external fields and their 'useAs' field names as keys and their
    * values as values.
    *
    * @param externalJoinJson
    * @return
    */
   public Map<String, Map<String, String>> retrieveExternalData(JsonObject externalJoinJson)
   {
      try
      {
         URL url = new URL(externalJoinJson.get("restUri").getAsString());
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();

         connection.setRequestMethod("GET");
         connection.setRequestProperty("Accept", "application/json");

         if (connection.getResponseCode() != 200)
         {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
         }

         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

         String output;
         StringBuffer buffer = new StringBuffer();

         while ((output = bufferedReader.readLine()) != null)
         {
            buffer.append(output);
         }

         connection.disconnect();

         // TODO Add heuristics on objects or arrays

         JsonArray recordsJson = jsonMarshaller.readJsonObject(buffer.toString()).get("list").getAsJsonArray();

         trace.info("External Data:");
         trace.info(recordsJson.toString());

         Map<String, Map<String, String>> externalData = new HashMap<String, Map<String, String>>();
         JsonArray externalJoinFieldsJson = externalJoinJson.get("fields").getAsJsonArray();

         for (int n = 0; n < recordsJson.size(); n++)
         {
            JsonObject recordJson = recordsJson.get(n).getAsJsonObject();
            Map<String, String> record = new HashMap<String, String>();

            for (int m = 0; m < externalJoinFieldsJson.size(); m++)
            {
               JsonObject externalJoinFieldJson = externalJoinFieldsJson.get(m).getAsJsonObject();

               if (externalJoinFieldJson.get("id").getAsString()
                     .equals(externalJoinJson.get("externalKey").getAsString()))
               {
                  externalData.put(recordJson.get(externalJoinFieldJson.get("id").getAsString()).getAsString(), record);
               }

               // TODO Other type mapping than string (central mapping
               // function f(type,object, container))

               record.put(externalJoinFieldJson.get("useAs").getAsString(),
                     recordJson.get(externalJoinFieldJson.get("id").getAsString()).getAsString());
            }
         }

         trace.info("Map");
         trace.info(externalData);

         return externalData;
      }
      catch (MalformedURLException e)
      {
         trace.error(e);

         throw new RuntimeException(e);
      }
      catch (IOException e)
      {
         trace.error(e);

         throw new RuntimeException(e);
      }
   }
   
   /**
    * Calculates NextExecutionDate depending on UI selected values
    *
    * @param Report Defintion JsonObject
    * @return NextExecutionDate 
    */
   public String getNextExecutionDate(JsonObject json)
   {
      System.out.println(json.toString());
      
//      SchedulingRecurrence sc = SchedulingFactory.getSchedular(json);
      
//      return sc.prcoessSchedule(json);
      return new Date().toString();
      
   }
}
