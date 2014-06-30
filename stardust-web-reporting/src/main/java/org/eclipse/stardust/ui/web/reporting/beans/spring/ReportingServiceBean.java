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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.UnsupportedFilterException;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.reporting.rt.ReportParameter;
import org.eclipse.stardust.reporting.rt.mapping.ReportDefinition;
import org.eclipse.stardust.reporting.rt.mapping.ReportRequest;
import org.eclipse.stardust.reporting.rt.service.ReportFormat;
import org.eclipse.stardust.reporting.rt.service.ReportingService;
import org.eclipse.stardust.reporting.rt.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.reporting.beans.spring.portal.CriticalityConfigurationService;
import org.eclipse.stardust.ui.web.reporting.beans.spring.portal.SearchHandlerChain;
import org.eclipse.stardust.ui.web.reporting.beans.spring.portal.XPathCacheManager;
import org.eclipse.stardust.ui.web.reporting.common.portal.DescriptorUtils;
import org.eclipse.stardust.ui.web.reporting.common.portal.DescriptorUtils.DescriptorMetadata;
import org.eclipse.stardust.ui.web.reporting.common.portal.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.reporting.common.portal.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.reporting.scheduling.SchedulingFactory;
import org.eclipse.stardust.ui.web.reporting.scheduling.SchedulingRecurrence;
import org.eclipse.stardust.ui.web.reporting.ui.UiHelper;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;

/**
 *
 * @author Yogesh.Manware
 *
 */

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReportingServiceBean
{
   @Resource
   UserProvider userProvider;

   private static final Logger trace = LogManager.getLogger(ReportingServiceBean.class);

   private static final String REPORTS_ROOT_FOLDER = "/reports";
   private static final String REPORT_DESIGN = "/designs";
   private static final String PUBLIC_REPORT_DEFINITIONS_DIR = REPORTS_ROOT_FOLDER + REPORT_DESIGN;
   private static final String PARTICIPANT_REPORT_DEFINITIONS_DIR = REPORTS_ROOT_FOLDER + "/PARTICIPANT_ID" + REPORT_DESIGN;
   private static final String USER_REPORT_DIR = "/documents/reports/designs";
   private static final String REPORT_DEFINITION_EXT = ".bpmrptdesign";
   
   //Report Instance Constant
   private static final String REPORT_INSTANCE_EXT = ".bpmrpt";
   private static final String REPORT_INSTANCE = "/saved-reports";
   private static final String AD_HOC = "/ad-hoc";
   private static final String PUBLIC_REPORT_INSTANCE_DIR = REPORTS_ROOT_FOLDER + REPORT_INSTANCE;
   private static final String PUBLIC_REPORT_INSTANCE_ADHOC_DIR = PUBLIC_REPORT_INSTANCE_DIR + AD_HOC;
   private static final String PARTICIPANT_REPORT_INSTANCE_DIR = REPORTS_ROOT_FOLDER + "/PARTICIPANT_ID" + REPORT_INSTANCE;
   private static final String PARTICIPANT_REPORT_INSTANCE_ADHOC_DIR = PARTICIPANT_REPORT_INSTANCE_DIR + AD_HOC;
   private static final String USER_REPORT_INSTANCE_DIR = "/documents/reports/saved-reports";
   private static final String USER_REPORT_INSTANCE_ADHOC_DIR = USER_REPORT_INSTANCE_DIR + AD_HOC;
   
   private DocumentManagementService documentManagementService;

   private UserService userService;

   @Resource
   private SearchHandlerChain searchHandlerChain;

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

   @Resource(name=XPathCacheManager.BEAN_ID)
   private XPathCacheManager xPathCacheManager;

   @Resource
   private CriticalityConfigurationService criticalityConfigurationService;

   private JsonMarshaller jsonMarshaller;

   private Gson gson = new Gson();

   public ReportingServiceBean()
   {
      jsonMarshaller = new JsonMarshaller();
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
      try
      {
         JsonObject resultJson = new JsonObject();
         JsonObject processesJson = new JsonObject();
         JsonObject descriptorsJson = new JsonObject();

         resultJson.add("processDefinitions", processesJson);
         resultJson.add("descriptors", descriptorsJson);

         // Ensures uniqueness of descriptor entries across all Process
         // Definitions

         Map<String, Object> descriptorsMap = new HashMap<String, Object>();

         for (ProcessDefinition processDefinition : modelService.getAllProcessDefinitions(false, null))
         {
            JsonObject processJson = new JsonObject();

            processJson.addProperty("id", processDefinition.getQualifiedId());
            processJson.addProperty("name", processDefinition.getName());
            processJson.addProperty("auxiliary", ProcessDefinitionUtils.isAuxiliaryProcess(processDefinition));

            processesJson.add(processDefinition.getId(), processJson);

            Map<DataPath, DescriptorMetadata> dataPaths = DescriptorUtils.getAllDescriptors(processDefinition, true,
                  modelService, servletContext, xPathCacheManager);

            for (Entry<DataPath, DescriptorMetadata> dataPathEntry : dataPaths.entrySet())
            {
               DataPath dataPath = dataPathEntry.getKey();
               if (dataPath.isDescriptor())
               {
                  if (!descriptorsMap.containsKey(dataPath.getId()))
                  {
                     JsonObject descriptorJson = new JsonObject();

                     descriptorsJson.add(dataPath.getId(), descriptorJson);

                        descriptorJson.addProperty("id",
                              processDefinition.getQualifiedId() + ":" + dataPath.getQualifiedId());
                     descriptorJson.addProperty("name", dataPath.getName());
                     descriptorJson.addProperty("type", UiHelper.mapDesciptorType(dataPath.getMappedType()).getId());

                     // metadata for Engine
                     DescriptorUtils.DescriptorMetadata metadata = dataPathEntry.getValue();
                     JsonObject metadataJson = new JsonObject();
                     metadataJson.addProperty("isDescriptor", true);
                     metadataJson.addProperty("isStructuredType", metadata.isStructured());
                     metadataJson.addProperty("xPath", metadata.getxPath());
                     metadataJson.addProperty("javaType", dataPath.getMappedType().getName());

                     descriptorJson.add("metadata", metadataJson);

                     descriptorsMap.put(dataPath.getId(), dataPath);
                  }
               }
            }

            // add all activities
            JsonArray activities = new JsonArray();

            for (Object activityObj : processDefinition.getAllActivities())
            {
               Activity activity = (Activity) activityObj;
               JsonObject activityJsonObj = new JsonObject();

               activityJsonObj.addProperty("id", processDefinition.getQualifiedId() + ":" + activity.getQualifiedId());
               activityJsonObj.addProperty("name", activity.getName());
               activityJsonObj.addProperty("auxiliary", ActivityInstanceUtils.isAuxiliaryActivity(activity));
               activityJsonObj.addProperty("interactive", activity.isInteractive());
               activities.add(activityJsonObj);
            }
            processJson.add("activities", activities);
         }

         JsonObject participantsJson = new JsonObject();

         resultJson.add("participants", participantsJson);

         List<QualifiedModelParticipantInfo> qParticipantInfoList = modelService.getAllModelParticipants(false);
         for (QualifiedModelParticipantInfo participant : qParticipantInfoList)
         {
            JsonObject participantJson = new JsonObject();

            participantJson.addProperty("id", participant.getQualifiedId());
            participantJson.addProperty("name", participant.getName());

            participantsJson.add(participant.getId(), participantJson);
         }

         return resultJson;
      }
      finally
      {
      }
   }

   /**
    * @return preference data
    */
   public JsonObject getPreferenceData()
   {
      List<CriticalityCategory> criticalityPrefs = CriticalityConfigurationUtil
            .getCriticalityCategoriesList(criticalityConfigurationService.readCriticalityCategoryPrefsMap());

      JsonObject preferencesJson = new JsonObject();

      // criticality
      ArrayList<CriticalityCategory> criticalityList = new ArrayList<CriticalityCategory>();
      CriticalityCategory cat = CriticalityConfigurationUtil.getAllCriticalityCategory();
      cat.setName("All"); //TODO I18n
      criticalityList.add(cat);
      criticalityList.addAll(criticalityPrefs);
      cat = CriticalityConfigurationUtil.getUndefinedCriticalityCategory();
      cat.setName("Undefined"); //TODO I18n
      criticalityList.add(cat);
      preferencesJson.add("criticality", gson.toJsonTree(criticalityList));

      //Favorite Reports
      ArrayList<String> favoriteReportList = new ArrayList<String>();
      HashMap<String, String> favoriteReportsMap = RepositoryUtility.getFavoriteReports();
      for (Entry<String, String> docIdName : favoriteReportsMap.entrySet())
      {
         favoriteReportList.add(docIdName.getKey());
      }
      
      preferencesJson.add("favoriteReports", gson.toJsonTree(favoriteReportList));
      
      //add other preferences here
      
      return preferencesJson;
   }

   /**
    *
    * @return
    * @throws ParseException
    * @throws UnsupportedFilterException
    */
   public String getReportData(String reportJson, HttpServletRequest httpRequest) throws UnsupportedFilterException, ParseException
   {
      ReportingService reportingService = getServiceFactory().getService(ReportingService.class);
      ReportDefinition reportDefinition = jsonMarshaller.gson().fromJson(reportJson, ReportDefinition.class);
      Collection<ReportParameter> reportParameters = new ArrayList<ReportParameter>();

      @SuppressWarnings("unchecked")
      Map<String, String[]> parameterMap = httpRequest.getParameterMap();
      for(String paramId: parameterMap.keySet())
      {
         if(paramId != "reportPath"){
            String[] paramValues = parameterMap.get(paramId);
            ReportParameter rp  = new ReportParameter(paramId, paramValues);
            reportParameters.add(rp);   
         }
      }

      ReportRequest reportRequest = new ReportRequest(reportDefinition.getDataSet(), reportParameters);
      return reportingService.getReport(reportRequest, ReportFormat.JSON);
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
            folder = findOrCreateFolder(getUserDocumentFolderPath(USER_REPORT_DIR));
         }
         else if (location.equals("participantFolder"))
         {
            folder = findOrCreateFolder(getParticipantDocumentFolderPath(PARTICIPANT_REPORT_DEFINITIONS_DIR, storageJson.get("participant").getAsString()));
         }

         // Mark Report Definition as saved
         reportJson.get("storage").getAsJsonObject().addProperty("state", "saved");
         
         String path = saveReportDocument(reportJson, folder, name + REPORT_DEFINITION_EXT);
         
         reportJson.get("storage").getAsJsonObject().addProperty("path", path);

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
   public String renameReportDefinition(String path, String name)
   {
      try
      {
         // Replace in cache
         return renameReportDefinitionDocument(path, name);
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
      Document document = getDocumentManagementService().getDocument(path);

      if (document != null)
      {
         JsonObject reportDefinitionJson = jsonMarshaller.readJsonObject(new String(getDocumentManagementService().retrieveDocumentContent(
               document.getId())));
          
         //add report specific meta-data
         JsonObject metaDataObj = new JsonObject(); 
         metaDataObj.addProperty("documentId", document.getId());
         reportDefinitionJson.add("metadata", metaDataObj);
         
         //update storage data
         JsonObject storage = GsonUtils.extractObject(reportDefinitionJson, "storage");
         JsonObject definition = GsonUtils.extractObject(reportDefinitionJson, "definition");
         if (definition != null)
         {
            storage = GsonUtils.extractObject(definition, "storage");
         }
         if (storage != null)
         {
            storage.addProperty("path", document.getPath());
         }

         return reportDefinitionJson;
      }
      else
      {
         throw new ObjectNotFoundException("Document " + path + " does not exist.");
      }
   }

   /**
    * @param reportId
    * @return
    */
   public byte[] downloadReportDefinition(String reportId)
   {
      try
      {
         return getDocumentManagementService().retrieveDocumentContent(reportId);
      }
      catch (Exception e)
      {
         trace.error("Exception while Download Report Definition " + e, e);
      }
      return null;
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
   private String getUserDocumentFolderPath(String path)
   {
      return "/realms/" + getUserService().getUser().getRealm().getId() + "/users/"
            + getUserService().getUser().getId() + path;
   }

   /**
    *
    * @return
    */
   private String getParticipantDocumentFolderPath(String basePath, String participant)
   {
      return basePath.replace("PARTICIPANT_ID", participant);
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
         Folder personalFolder = findOrCreateFolder(getUserDocumentFolderPath(USER_REPORT_DIR));
         Folder participantFolder = findOrCreateFolder(REPORTS_ROOT_FOLDER);

         JsonObject rootFolderJson = new JsonObject();
         JsonArray subFoldersJson = new JsonArray();
         rootFolderJson.add("subFolders", subFoldersJson);

         //Prepare Participants subfolders
         //JsonObject participantsFolderJson = new JsonObject();
         //participantsFolderJson.addProperty("name", "Participants Report Definitions"); // I18N
         //participantsFolderJson.addProperty("id", participantFolder.getId());
         //participantsFolderJson.addProperty("path", participantFolder.getPath());

         //subFoldersJson.add(participantsFolderJson);

         List<Folder> subfolders = participantFolder.getFolders();
         User loggedInUser = userProvider.getUser();

         //add relevant participants
         //JsonArray participantFoldersJson = new JsonArray();
         //participantsFolderJson.add("subFolders", participantFoldersJson);

         for (Folder participantSubFolder : subfolders)
         {
            participantSubFolder = findOrCreateFolder(participantSubFolder.getPath());

            // check the permissions to current user
            if (loggedInUser.isInRole(participantSubFolder.getName()) || loggedInUser.isAdministrator())
            {
               Participant  participant = modelService.getParticipant(participantSubFolder.getName(), null);
               JsonObject participantFolderJson = null;

               if (participant != null)
               {
                  participantFolderJson = getReportDefinitions(findOrCreateFolder(participantSubFolder.getPath() + REPORT_DESIGN), participant.getName() + " Report Definitions"); //TODO: I18N
               }

               if (participantFolderJson != null)
               {
                  subFoldersJson.add(participantFolderJson);
               }
            }
         }

         subFoldersJson.add(getReportDefinitions(publicFolder, "Public Report Definitions")); // I18N
         subFoldersJson.add(getReportDefinitions(personalFolder, "Personal Report Definitions")); // I18N

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
    * @param serviceName
    * @param searchValue
    * @return
    */
   public String searchData(String serviceName, String searchValue)
   {
      return searchHandlerChain.handleRequest(serviceName, searchValue);
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
            if (reportDefinitionDocument.getName().endsWith(REPORT_DEFINITION_EXT))
            {
               String content = new String(getDocumentManagementService().retrieveDocumentContent(
                     reportDefinitionDocument.getId()));

               JsonObject reportDefinitionJson = jsonMarshaller.readJsonObject(content);

               reportDefinitionsJson.add(reportDefinitionJson);
               reportDefinitionJson.addProperty("id", reportDefinitionDocument.getId());
               reportDefinitionJson.addProperty(
                     "name",
                     reportDefinitionDocument.getName().substring(0,
                           reportDefinitionDocument.getName().indexOf(REPORT_DEFINITION_EXT)));
               reportDefinitionJson.addProperty("path", reportDefinitionDocument.getPath());
            }
         }
      }

      return folderJson;
   }

   /**
     *
     */
   private String saveReportDocument(JsonObject reportDefinitionJson, Folder folder, String name)
   {
      String reportContent = reportDefinitionJson.toString();
      String path = folder.getPath() + "/" + name;
      
      Document reportDesignDocument = getDocumentManagementService().getDocument(path);

      if (null == reportDesignDocument)
      {
         DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name);

         documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
         MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
               "ippMimeTypesHelper", servletContext);
         
         documentInfo.setContentType(mimeTypesHelper.detectMimeTypeI(name, "").getType());

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
      
      return path;
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
      DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name + REPORT_DEFINITION_EXT);

      documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
      documentInfo.setContentType(MimeTypesHelper.DEFAULT.getType());

      byte[] content = getDocumentManagementService().retrieveDocumentContent(path);

      getDocumentManagementService().createDocument(folderPath, documentInfo, content, null);
      getDocumentManagementService().removeDocument(reportDefinitionDocument.getId());

      return folderPath + "/" + name + REPORT_DEFINITION_EXT;
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
    * Calculates an estimation date when the report will be executed next.
    *
    * @param json - The json object representing the scheduling object
    * @return - The next possible execution time in json format
    */
   public String getNextExecutionDate(JsonObject json)
   {
      trace.info(json.toString());

      SchedulingRecurrence sc = SchedulingFactory.getSchedular(json);

      return sc.prcoessSchedule(json);

   }
   
   /**
    * @param reportInstanceJson
    * @return
    */
   public JsonObject saveReportInstance(JsonObject reportInstanceJson)
   {
      try
      {
         String location;
         String participant;
         String name;

         // fetch report definition
         JsonObject reportJson = GsonUtils.extractObject(reportInstanceJson, "definition");

         // evaluate storage location
         JsonObject storageJson = GsonUtils.extractObject(reportJson, "storage");
         JsonObject metadataJson = GsonUtils.extractObject(reportInstanceJson, "metadata");

         if (metadataJson != null)
         {
            location = GsonUtils.extractString(metadataJson, "location");
            participant = GsonUtils.extractString(metadataJson, "participant");
            name = GsonUtils.extractString(metadataJson, "name");
         }
         else
         {
            location = GsonUtils.extractString(storageJson, "location");
            participant = GsonUtils.extractString(storageJson, "participant");
            name = GsonUtils.extractString(reportJson, "name");
         }

         if(StringUtils.isEmpty(name)){
            name = "Report";
         }
         
         Folder folder = null;

         String reportFilePath = "/" + name;

         boolean isSaved = "saved".equals(reportJson.get("storage").getAsJsonObject().get("state").getAsString());
         String basePath = "";

         if (location.equals("publicFolder"))
         {
            if (isSaved)
            {
               basePath = PUBLIC_REPORT_INSTANCE_DIR + reportFilePath;
            }
            else
            {
               basePath = PUBLIC_REPORT_INSTANCE_ADHOC_DIR;
            }
         }
         else if (location.equals("personalFolder"))
         {
            if (isSaved)
            {
               basePath = getUserDocumentFolderPath(USER_REPORT_INSTANCE_DIR) + reportFilePath;
            }
            else
            {
               basePath = getUserDocumentFolderPath(USER_REPORT_INSTANCE_ADHOC_DIR);
            }
         }
         else if (location.equals("participantFolder"))
         {
            if (isSaved)
            {
               basePath = getParticipantDocumentFolderPath(PARTICIPANT_REPORT_INSTANCE_DIR, participant)
                     + reportFilePath;
            }
            else
            {
               basePath = getParticipantDocumentFolderPath(PARTICIPANT_REPORT_INSTANCE_ADHOC_DIR, participant);
            }
         }

         folder = findOrCreateFolder(basePath);

         saveReportDocument(reportInstanceJson, folder, name + "-" + getTimeStampString() + REPORT_INSTANCE_EXT);

         return reportInstanceJson;
      }
      finally
      {
         //TODO:
      }
   }
   
   /**
    * @param reportMetadata
    * @return
    */
   public void addToFavorites(JsonObject reportMetadata)
   {
      RepositoryUtility.addToFavorite(GsonUtils.extractString(reportMetadata, "name"),
            GsonUtils.extractString(reportMetadata, "id"));
   }

   /**
    * @param reportId
    * @return
    */
   public void removeFromFavorites(String reportId)
   {
      RepositoryUtility.removeFromFavorite(reportId);
   }
   
   /**
    * TODO: Localize date, also consider time zone of user
    * @return
    */
   public static String getTimeStampString()
   {
      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-hhmmss");
      return DMSUtils.replaceAllSpecialChars(DATE_FORMAT.format(new Date()));
   }
   
   /**
    * @param uuid
    * @return
    */
   public JsonObject uploadReport(String uuid)
   {
      try
      {
         FileStorage fileStorage = (FileStorage) RestControllerUtils.resolveSpringBean(
               "fileStorage", servletContext);

         if (StringUtils.isNotEmpty(uuid))
         {
            String path = fileStorage.pullPath(uuid);
            if (StringUtils.isNotEmpty(path))
            {
               File file = new File(path);
               InputStream is = new FileInputStream(path);
               String reportJsonTxt = IOUtils.toString(is);
               
               JsonMarshaller jsonIo = new JsonMarshaller();
               JsonObject reportJson = jsonIo.readJsonObject(reportJsonTxt);
               reportJson.addProperty("reportUID", new Date().getTime());
               reportJson.get("storage").getAsJsonObject().addProperty("path", "");
               
               trace.info(reportJson);
               
               return saveReportDefinition(reportJson);
            }
         }
         return null;
      }
      catch (Exception e)
      {
         trace.error("Exception while Uploading Report Definition " + e, e);
      }
      return null;
   }
}
