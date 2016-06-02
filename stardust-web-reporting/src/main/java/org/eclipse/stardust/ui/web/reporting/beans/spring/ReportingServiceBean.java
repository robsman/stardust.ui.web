/*******************************************************************************
 * Copyright (c) 2013, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.beans.spring;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.UnsupportedFilterException;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingFactory;
import org.eclipse.stardust.engine.core.runtime.scheduling.SchedulingRecurrence;
import org.eclipse.stardust.reporting.rt.ReportParameter;
import org.eclipse.stardust.reporting.rt.mapping.ReportDefinition;
import org.eclipse.stardust.reporting.rt.mapping.ReportRequest;
import org.eclipse.stardust.reporting.rt.service.ReportFormat;
import org.eclipse.stardust.reporting.rt.service.ReportingService;
import org.eclipse.stardust.reporting.rt.util.CriticalityUtilities;
import org.eclipse.stardust.reporting.rt.util.JsonMarshaller;
import org.eclipse.stardust.reporting.rt.util.ReportUtilities;
import org.eclipse.stardust.ui.web.common.spi.user.User;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.reporting.beans.spring.portal.SearchHandlerChain;
import org.eclipse.stardust.ui.web.reporting.common.LanguageUtil;
import org.eclipse.stardust.ui.web.reporting.common.portal.criticality.Criticality;
import org.eclipse.stardust.ui.web.reporting.ui.DataTypes;
import org.eclipse.stardust.ui.web.reporting.ui.UiHelper;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils.DataPathMetadata;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.I18nFolderUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.BenchmarkUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.XPathCacheManager;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
   
   public static final String BENCHMARK_RUNTIME_OID = "id";
   
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
    * @param locale
    * @return
    */
   public JsonObject getModelData(Locale locale)
   {      
      QueryService qs = getServiceFactory().getService(QueryService.class);
      AdministrationService as = getServiceFactory().getService(AdministrationService.class);
            
      return ReportUtilities.getModelData(locale, qs, as);
   }

   /**
    * @return preference data
    */
   public JsonObject getPreferenceData()
   {    
      JsonObject preferenceData = CriticalityUtilities.getPreferenceData(new AdministrationServiceImpl());

      JsonArray criticalitiesArray = preferenceData.get("criticality").getAsJsonArray();
      List<Criticality> criticalities = new ArrayList<Criticality>();

      for (int i = 0; i < criticalitiesArray.size(); i++)
      {
         JsonObject category = criticalitiesArray.get(i).getAsJsonObject();

         CriticalityCategory critCat = new CriticalityCategory();
         critCat.setRangeFrom(category.get("rangeFrom").getAsInt());
         critCat.setRangeTo(category.get("rangeTo").getAsInt());
         critCat.setLabel(category.get("name").getAsString());

         String categoryId = category.get("id").getAsString();
         //Reporting still don't support "All" and "Undefined" Criticality Category so filtering them out. CRNT-33822       
         if (!(categoryId.equals("All") || categoryId.equals("Undefined")))
         {
            if (categoryId.equals("Low"))
            {
               criticalities.add(new Criticality(critCat, 1));
            }
            else if (categoryId.equals("Medium"))
            {
               criticalities.add(new Criticality(critCat, 2));
            }
            else if (categoryId.equals("High"))
            {
               criticalities.add(new Criticality(critCat, 3));
            }
         }
      }

      JsonObject preferencesJson = new JsonObject();
      preferencesJson.add("criticality", gson.toJsonTree(criticalities));
      
      //Favorite Reports
      ArrayList<String> favoriteReportList = new ArrayList<String>();
      HashMap<String, String> favoriteReportsMap = RepositoryUtility.getFavoriteReports();
      for (Entry<String, String> docIdName : favoriteReportsMap.entrySet())
      {
         favoriteReportList.add(docIdName.getKey());
      }
      preferencesJson.add("favoriteReports", gson.toJsonTree(favoriteReportList));
            
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
            String[] paramV = parameterMap.get(paramId);
            String[] paramValues;
            if (paramV.length == 1)
            {
               paramValues = paramV[0].split(",");
            }
            else
            {
               paramValues = paramV;
            }
            ReportParameter rp  = new ReportParameter(paramId, paramValues);
            reportParameters.add(rp);   
         }
      }
 
      String userLanguage = getLanguage(httpRequest);

      if (userLanguage == null)
      {
         userLanguage = reportDefinition.getUserLanguage();
      }
      
      ReportRequest reportRequest = new ReportRequest(reportDefinition, reportParameters, userLanguage);
      
      return reportingService.getReport(reportRequest, ReportFormat.JSON);
   }

   /**
    * @param httpRequest
    * @return
    */
   public String getLanguage(HttpServletRequest httpRequest)
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-Language"), ",");
      if (tok.hasMoreTokens())
      {
         return LanguageUtil.getLocale(tok.nextToken());
      }
      trace.debug("could not find user language from httpRequest header");
      return "";
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

      Document document = saveReportDocument(reportJson, folder, name + REPORT_DEFINITION_EXT);

      reportJson.get("storage").getAsJsonObject().addProperty("path", document.getPath());

      //add report specific meta-data
      JsonObject metaDataObj = new JsonObject(); 
      metaDataObj.addProperty("documentId", document.getId());
      reportJson.add("metadata", metaDataObj);

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
         String docStr = null;
         try
         {
            docStr = new String(getDocumentManagementService().retrieveDocumentContent(document.getId()), "UTF-8");
         }
         catch (DocumentManagementServiceException e)
         {
            trace.error("Exception occurred while retrieving report definition: " + document.getName(), e);
            return null;
         }
         catch (UnsupportedEncodingException e)
         {
            trace.error("Exception occurred while retrieving report definition: " + document.getName(), e);
            return null;
         }
         
         JsonObject reportDefinitionJson = jsonMarshaller.readJsonObject(docStr);
          
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
         //UTF-8 conversion not necessary as browsers default conversion to string is using UTF-8
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
            if (loggedInUser.isAdministrator() || loggedInUser.isInOrganization(participantSubFolder.getName()))
            {
               Participant  participant = modelService.getParticipant(participantSubFolder.getName(), null);
               JsonObject participantFolderJson = null;

               if (participant != null)
               {
                  participantFolderJson = getReportDefinitions(findOrCreateFolder(participantSubFolder.getPath() + REPORT_DESIGN), I18nUtils.getParticipantName(ParticipantUtils.getParticipant(participant))
                        + " " + MessagesViewsCommonBean.getInstance().getString("views.genericRepositoryView.systemFolders.myReportDesigns"));
               }

               if (participantFolderJson != null)
               {
                  subFoldersJson.add(participantFolderJson);
               }
            }
         }

         subFoldersJson.add(getReportDefinitions(publicFolder, I18nFolderUtils.getLabel(I18nFolderUtils.PUBLIC_REPORT_DEFINITIONS)));
         subFoldersJson.add(getReportDefinitions(personalFolder, I18nFolderUtils.getLabel(I18nFolderUtils.PERSONAL_REPORT_DEFINITIONS)));

         return rootFolderJson;
      }
      catch (Exception e)
      {
         trace.error("Error Occurred while loading report definitions", e);
         return null;
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

         List<Document> candidateReportDefinitionsDocuments = folder.getDocuments();

         for (Document reportDefinitionDocument : candidateReportDefinitionsDocuments)
         {
            if (reportDefinitionDocument.getName().endsWith(REPORT_DEFINITION_EXT))
            {
               String content = null;
               try
               {
                  content = new String(getDocumentManagementService().retrieveDocumentContent(reportDefinitionDocument.getId()), "UTF-8");
               }
               catch (DocumentManagementServiceException e)
               {
                  trace.error("Exception Occurred while retrieving report definition with Name" + reportDefinitionDocument.getName(), e);
               }
               catch (UnsupportedEncodingException e)
               {
                  trace.error("Exception Occurred while retrieving report definition with Name" + reportDefinitionDocument.getName(), e);
               }

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
   private Document saveReportDocument(JsonObject reportDefinitionJson, Folder folder, String name)
   {
      String reportContent = reportDefinitionJson.toString();
      String path = folder.getPath() + "/" + name;
      
      Document reportDesignDocument = getDocumentManagementService().getDocument(path);
      try
      {

         if (null == reportDesignDocument)
         {
            DocumentInfo documentInfo = DmsUtils.createDocumentInfo(name);

            documentInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
            MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
                  "ippMimeTypesHelper", servletContext);

            documentInfo.setContentType(mimeTypesHelper.detectMimeTypeI(name, "").getType());

            reportDesignDocument = getDocumentManagementService().createDocument(folder.getPath(), documentInfo,
                  reportContent.getBytes("UTF-8"), null);
         }
         else
         {
            reportDesignDocument = getDocumentManagementService().updateDocument(reportDesignDocument,
                  reportContent.getBytes("UTF-8"), null, false, null, null, false);
         }
      }
      catch (DocumentManagementServiceException e)
      {
         trace.error("Error Occurred while creating/updating document", e);
      }
      catch (UnsupportedEncodingException e)
      {
         trace.error("Error Occurred while creating/updating document", e);
      }     
      
      return reportDesignDocument;
   }

   /**
    * TODO Should be more elegant
    *
    * @param path
    */
   private String renameReportDefinitionDocument(String path, String name)
   {
      Document reportDefinitionDocument = getDocumentManagementService().getDocument(path);
      reportDefinitionDocument.setName(name + REPORT_DEFINITION_EXT);
      String folderPath = path.substring(0, path.lastIndexOf('/'));
      byte[] content = getDocumentManagementService().retrieveDocumentContent(path);
      
      String updatedPath = folderPath + "/" + name + REPORT_DEFINITION_EXT;
      
      JsonObject reportDefinitionJson = jsonMarshaller.readJsonObject(new String(content));
      //update report definition name
      reportDefinitionJson.addProperty("name", name);
       
      JsonObject storageJson = reportDefinitionJson.get("storage").getAsJsonObject();
      //update report definition path
      storageJson.addProperty("path", updatedPath);
      
      String updatedContent = jsonMarshaller.writeJsonObject(reportDefinitionJson);
      
      try
      {
         reportDefinitionDocument = getDocumentManagementService().updateDocument(
               reportDefinitionDocument, updatedContent.getBytes("UTF-8"), null, false,
               null, null, false);
      }
      catch (DocumentManagementServiceException e)
      {
         e.printStackTrace();
      }
      catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }

      return updatedPath;
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

      SchedulingRecurrence sc = SchedulingFactory.getScheduler(json);

      return sc.processSchedule(json);

   }
   
   /**s
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
         
         JsonObject tempRreportJson = GsonUtils.extractObject(reportInstanceJson, "definition");
         String tempName = GsonUtils.extractString(tempRreportJson, "name");
         tempName = name + "-" + getTimeStampString() + REPORT_INSTANCE_EXT;
         tempRreportJson.addProperty("instanceName", tempName);

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
               String reportJsonTxt = IOUtils.toString(is, "UTF-8");
               
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
   
   /**
    * Calculates all execution dates appearing in between start date and end date
    * 
    * @param json - The json object representing the scheduling object
    * @param startDate 
    * @param endDate 
    * @return - The next possible execution dates in json format
    */
   public JsonObject getNextExecutionDates(JsonObject json, String startDate, String endDate)
   {
      trace.info(json.toString());

      SchedulingRecurrence sc = SchedulingFactory.getScheduler(json);

      List<String> calculateSchedule = sc.calculateSchedule(json, startDate, endDate);
      
      JsonElement element = gson.toJsonTree(calculateSchedule, new TypeToken<ArrayList<String>>(){}.getType());

      JsonArray jsonArray = element.getAsJsonArray();

      JsonObject jsonObject = new JsonObject();
      
      jsonObject.add("executionDates", jsonArray);
      
      return jsonObject;

   }
   
   /**
    * Might be invoked for renaming and saving  Report Definition (whereby json
    * contains a top-level element "report").
    *
    * @param json
    */
   public JsonObject renameAndSaveReportDefinition(JsonObject reportJson)
   {
      JsonObject storageJson = reportJson.get("storage").getAsJsonObject();

      String updatedReportPath = renameReportDefinition(storageJson.get("path")
            .getAsString(), reportJson.get("name").getAsString());

      // Update report definition path
      storageJson.addProperty("path", updatedReportPath);

      return saveReportDefinition(reportJson);

   }
   
   private static DataTypes determineDateType(DataPath dataPath)
   {
      Class mappedType = dataPath.getMappedType();

      if (Date.class.equals(mappedType))
      {
         GenericDataMapping mapping = new GenericDataMapping(dataPath);
         DataMappingWrapper dmWrapper = new DataMappingWrapper(mapping, null, false);
         if (ProcessPortalConstants.TIMESTAMP_TYPE.equals(dmWrapper.getType()))
         {
            return DataTypes.TIMESTAMP;
         }
         else
         {
            return DataTypes.DATE;
         }
      }
      return DataTypes.TIMESTAMP;
   }
}
