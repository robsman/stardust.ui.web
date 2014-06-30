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

package org.eclipse.stardust.ui.mobile.service;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.core.interactions.Interaction.getInteractionId;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Documents;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.mobile.rest.JsonMarshaller;
import org.eclipse.stardust.ui.mobile.service.ActivitySearchHelper.ActivitySearchCriteria;
import org.eclipse.stardust.ui.mobile.service.DocumentSearchHelper.DocumentSearchCriteria;
import org.eclipse.stardust.ui.mobile.service.ProcessSearchHelper.ProcessSearchCriteria;
import org.eclipse.stardust.ui.mobile.service.WorklistHelper.WorklistCriteria;
import org.eclipse.stardust.ui.web.common.messages.CommonPropertiesMessageBean;
import org.eclipse.stardust.ui.web.processportal.service.rest.DataException;
import org.eclipse.stardust.ui.web.processportal.service.rest.InteractionDataUtils;
import org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityUi;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.ExternalWebAppActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl.IppCopyrightInfo;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl.IppVersion;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientSideDataFlowUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;

public class MobileWorkflowService implements ServletContextAware {
	private ServiceFactory serviceFactory;
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private DocumentManagementService documentManagementService;
	private User loginUser;
	private Folder userDocumentsRootFolder;
	private Folder publicDocumentsRootFolder;
	private @Autowired HttpServletRequest httpRequest;
	private ServletContext servletContext;
	
	@Autowired
	private org.springframework.context.ApplicationContext appContext;
		  
   @Resource
   private org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry interactionRegistryManual;

	public MobileWorkflowService() {
		super();

		new JsonMarshaller();
	}

   @Override
   public void setServletContext(ServletContext servletContext) {
      this.servletContext = servletContext;
   }
   
	/**
	 * 
	 * @return
	 */
	private ServiceFactory getServiceFactory() {
		return serviceFactory;
	}

	/**
	 * 
	 * @return
	 */
	private UserService getUserService() {
		if (userService == null) {
			userService = getServiceFactory().getUserService();
		}

		return userService;
	}

	/**
	 * 
	 * @return
	 */
	private QueryService getQueryService() {
		if (queryService == null) {
			queryService = getServiceFactory().getQueryService();
		}

		return queryService;
	}

	/**
	 * 
	 * @return
	 */
	private DocumentManagementService getDocumentManagementService() {
		if (documentManagementService == null) {
			documentManagementService = getServiceFactory()
					.getDocumentManagementService();
		}

		return documentManagementService;
	}

	/**
	 * 
	 * @return
	 */
	private WorkflowService getWorkflowService() {
		if (workflowService == null) {
			workflowService = getServiceFactory().getWorkflowService();
		}

		return workflowService;
	}

	/**
	 * 
	 * @return
	 */
	private User getLoginUser() {
		return loginUser;
	}

	/**
	 * 
	 * @return
	 */
	public JsonObject login(JsonObject credentialsJson) {
		Map<String, String> credentials = new HashMap<String, String>();

		String partition = credentialsJson.get("partition").getAsString();

		credentials.put(SecurityProperties.PARTITION, partition);

		System.out.println("Partition: " + partition);

		serviceFactory = ServiceFactoryLocator.get(
				credentialsJson.get("account").getAsString(), credentialsJson
						.get("password").getAsString(), credentials);
		loginUser = getServiceFactory().getWorkflowService().getUser();

		// Initialize session
		httpRequest.getSession();
		
		JsonObject userJson = marshalUser(loginUser);

		userDocumentsRootFolder = (Folder) getDocumentManagementService()
				.getFolder(getUserDocumentsRootFolderPath(),
						Folder.LOD_LIST_MEMBERS);
		publicDocumentsRootFolder = (Folder) getDocumentManagementService()
				.getFolder(getPublicDocumentsRootFolderPath(),
						Folder.LOD_LIST_MEMBERS);

		return userJson;
	}
	

   /**
    * 
    * @return
    */
   public JsonObject logout() {      
      HttpSession session = httpRequest.getSession(false);
      if (null != session)
      {
         session.invalidate();
      }
      
      // TODO
      return new JsonObject();
   }

	/**
	 * 
	 * @return
	 */
	public JsonObject getProcessDefinitions(boolean startable) {
		JsonObject resultJson = new JsonObject();
		JsonArray processDefinitionsJson = new JsonArray();

		resultJson.add("processDefinitions", processDefinitionsJson);

		List<ProcessDefinition> processDefinitions = null;
		if (startable)
		{
		   processDefinitions = getWorkflowService().getStartableProcessDefinitions();
		}
		else {
		   processDefinitions = getQueryService().getProcessDefinitions(ProcessDefinitionQuery.findAll());
		}

      for (ProcessDefinition processDefinition : processDefinitions) {
         JsonObject processDefinitionJson = new JsonObject();

         processDefinitionsJson.add(processDefinitionJson);

         processDefinitionJson.addProperty("id", processDefinition.getId());
         processDefinitionJson.addProperty("name",
               processDefinition.getName());
         processDefinitionJson.addProperty("description",
               processDefinition.getDescription());
      }

      return resultJson;
	}

   /**
    * @param procIds
    * @return
    */
   public JsonObject getActivities(String procIds) {
      List<String> processDefinitionIds = new ArrayList<String>();
      StringTokenizer tok = new StringTokenizer(procIds, ",");
      while (tok.hasMoreTokens()) {
         processDefinitionIds.add(tok.nextToken());
      }
      JsonObject resultJson = new JsonObject();
      JsonArray activitiesJson = new JsonArray();

      resultJson.add("activities", activitiesJson);

      List<Activity> activities = null;
      List<ProcessDefinition> processDefinitions = getQueryService().getProcessDefinitions(ProcessDefinitionQuery.findAll());

      if (CollectionUtils.isEmpty(processDefinitionIds))
      {
         for (ProcessDefinition processDefinition : processDefinitions) {
            activities = processDefinition.getAllActivities();
            for (Activity activity : activities)
            {
               JsonObject activityJson = new JsonObject();
               activitiesJson.add(activityJson);
               
               activityJson.addProperty("id", activity.getId());
               activityJson.addProperty("oid", activity.getElementOID());
               activityJson.addProperty("name", activity.getName());
               activityJson.addProperty("description", activity.getDescription());
            }
         }
      }
      else
      {
         for (String processDefinitionId : processDefinitionIds)
         {
            for (ProcessDefinition processDefinition : processDefinitions) {
               if (processDefinition.getId().equals(processDefinitionId))
               {
                  activities = processDefinition.getAllActivities();
                  for (Activity activity : activities)
                  {
                     JsonObject activityJson = new JsonObject();
                     activitiesJson.add(activityJson);
                     
                     activityJson.addProperty("id", activity.getId());
                     activityJson.addProperty("oid", activity.getElementOID());
                     activityJson.addProperty("name", activity.getName());
                     activityJson.addProperty("description", activity.getDescription());
                  }
               }
            }
         }
      }

      return resultJson;
   }

   /**
    * 
    * @return
    */
   public JsonObject getWorklistCount() {
      JsonObject resultJson = new JsonObject();
      
      // TODO: Use count query
      resultJson.addProperty("total",
            getWorkflowService().getWorklist(WorklistQuery.findCompleteWorklist())
                  .getCumulatedItems().size());
      
      return resultJson;
   }
   
   /**
    * @param criteria
    * @return
    */
   public JsonObject getWorklist(WorklistCriteria criteria)
   {
      JsonObject resultJson = new JsonObject();
      JsonArray worklistJson = new JsonArray();

      resultJson.add("worklist", worklistJson);

      ActivityInstanceQuery query = WorklistHelper.buildWorklistQuery(criteria);

      // TODO - review
      List<ActivityInstance> worklistItems;
      ActivityInstances activityInstances = getQueryService().getAllActivityInstances(query);
      for (ActivityInstance activityInstance : activityInstances)
      {
         JsonObject activityInstanceJson = new JsonObject();

         worklistJson.add(activityInstanceJson);

         long timeInMillis = Calendar.getInstance().getTimeInMillis();
         if (activityInstance.getState() == ActivityInstanceState.Completed
               || activityInstance.getState() == ActivityInstanceState.Aborted)
         {
            timeInMillis = activityInstance.getLastModificationTime().getTime();
         }
         long duration = timeInMillis - activityInstance.getStartTime().getTime();

         String lastPerformer;
         UserInfo userInfo = activityInstance.getPerformedBy();
         if (null != userInfo)
         {
            User user = UserUtils.getUser(userInfo.getId());
            lastPerformer = "motu"; // I18nUtils.getUserLabel(user);
         }
         else
         {
            lastPerformer = activityInstance.getPerformedByName();
         }

         activityInstanceJson.addProperty("oid", activityInstance.getOID());
         activityInstanceJson.addProperty("criticality",
               activityInstance.getCriticality());
         activityInstanceJson.addProperty("status", activityInstance.getState().getName()); // TODO:
                                                                                            // i18n

         activityInstanceJson.addProperty("lastPerformer", lastPerformer);
         activityInstanceJson.addProperty("assignedTo", "motu" /*
                                                                * ActivityInstanceUtils.
                                                                * getAssignedToLabel
                                                                * (activityInstance)
                                                                */); // TODO
         activityInstanceJson.addProperty("duration", duration);
         activityInstanceJson.addProperty("activityId", activityInstance.getActivity()
               .getId());
         activityInstanceJson.addProperty("activityName", activityInstance.getActivity()
               .getName());
         activityInstanceJson.addProperty("processId", activityInstance.getActivity()
               .getProcessDefinitionId());
         activityInstanceJson.addProperty("processName", activityInstance.getActivity()
               .getProcessDefinitionId());
         activityInstanceJson.addProperty("processInstanceOid",
               activityInstance.getProcessInstanceOID());
         activityInstanceJson.addProperty("startTime", activityInstance.getStartTime()
               .getTime());
         activityInstanceJson.addProperty("lastModificationTime",
               activityInstance.getLastModificationTime().getTime());

         JsonObject descriptorsJson = new JsonObject();

         activityInstanceJson.add("descriptors", descriptorsJson);

         for (DataPath dataPath : activityInstance.getDescriptorDefinitions())
         {
            descriptorsJson.addProperty(
                  dataPath.getId(),
                  formatDescriptorValue(
                        activityInstance.getDescriptorValue(dataPath.getId()),
                        dataPath.getId()));
         }
      }
      resultJson.add("paginationResponse", SearchHelperUtil.getPaginationResponseObject(activityInstances));
      return resultJson;
   }

   /**
    * @param activityInstanceOid
    * @return
    */
   public JsonObject activateActivity(long activityInstanceOid) {
      ActivityInstance ai = getWorkflowService().activate(activityInstanceOid);
      JsonObject activityJson = getActivityInstanceJson(ai);
      // TODO @SG
      String context = SpiUtils.getInteractionController(ai.getActivity()).getContextId(ai);
      Map inData = workflowService.getInDataValues(activityInstanceOid, context, null);
      try {

         if (null != ai.getActivity().getApplicationContext(ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID))
         {
            // UI Mashup
            Interaction interaction = new Interaction(null,
                  serviceFactory.getQueryService().getModel(ai.getModelOID(), false), ai,
                  ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID,
                  serviceFactory);
            Map<String, Serializable> inParams = newHashMap();
            for (DataMapping inMapping : (List<DataMapping>) interaction.getDefinition().getAllInDataMappings())
            {
               Serializable inValue = (Serializable) inData.get(inMapping.getId());
               if (null != inValue)
               {
                  try
                  {
                     String paramId = inMapping.getApplicationAccessPoint().getId();
      
                     Object inParam = ClientSideDataFlowUtils.evaluateClientSideInMapping(
                           interaction.getModel(), ai.getActivity(), inParams.get(paramId), inMapping, inValue);
      
                     inParams.put(paramId, (Serializable) inParam);
                  }
                  catch (Exception e)
                  {
                     System.out.println("Failed evaluating client side of IN data mapping "
                           + inMapping.getId() + " on activity instance " + ai);
                  }
               }
            }
            interaction.setInDataValues(inParams);
            getInteractionRegistry().registerInteraction(interaction);
         }
         else
         {
            // Manual activity
            org.eclipse.stardust.ui.web.processportal.interaction.Interaction interaction = new org.eclipse.stardust.ui.web.processportal.interaction.Interaction(
                  serviceFactory.getQueryService().getModel(ai.getModelOID(), false), ai,
                  PredefinedConstants.DEFAULT_CONTEXT, serviceFactory);
            ManualActivityUi manualActivityUi = new ManualActivityUi(ai, ai.getActivity()
                  .getApplicationContext(PredefinedConstants.DEFAULT_CONTEXT),
                  getQueryService());
            interaction.setManualActivityPath(manualActivityUi.getManualActivityPath());
            Map<String, Serializable> configuration = new HashMap<String, Serializable>();
            // TODO - need this to be configurable for mobile?
            configuration.put("layoutColumns", 1);
            configuration.put("tableColumns", 1);
            interaction.setConfiguration(configuration);
            interaction.setInDataValues(inData);
            
            // TODO - check 
            // The line below is commented as it has dependency on FacesContext
            // We can possibly get around it but it may not be needed as we are not supporting
            // document data anyways
            // DocumentHelper.initializeDocumentControllers(interaction, inData);

            getInteractionRegistryManual().registerInteraction(interaction);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
        
      return activityJson;
   }

   /**
    * @param oid
    * @return
    */
   public JsonObject completeActivity(String oid) {
      ActivityInstance ai = getWorkflowService().getActivityInstance(new Long(oid).longValue());
//    IActivityInteractionController interactionController = SpiUtils
//          .getInteractionController(ai.getActivity());
      //Map<String, Serializable>outDataValues = interactionController.getOutDataValues(activityInstance);

      Map<String, Serializable> outData = null;
      if (null != ai.getActivity().getApplicationContext(
            ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID))
      {
         InteractionRegistry registry = getInteractionRegistry();
         if (null != registry)
         {
            // retrieve out data
            Interaction interaction = registry.getInteraction(getInteractionId(ai));
            if (null != interaction)
            {
               Map<String, Serializable> outParams = interaction.getOutDataValues();
               if (null != outParams)
               {
                  // performing client side OUT mappings
                  outData = newHashMap();
                  for (DataMapping outMapping : (List<DataMapping>) interaction.getDefinition().getAllOutDataMappings())
                  {
                     Serializable outParam = outParams.get(outMapping.getApplicationAccessPoint().getId());
                     if (null != outParam)
                     {
                        try
                        {
                           Object outValue = ClientSideDataFlowUtils.evaluateClientSideOutMapping(
                                 interaction.getModel(), ai.getActivity(), outParam, outMapping);

                           outData.put(outMapping.getId(), (Serializable) outValue);
                        }
                        catch (Exception e)
                        {
                         System.out.println("Failed evaluating client side of OUT data mapping "
                                 + outMapping.getId() + " on activity instance " + ai);
                        }
                     }
                     else
                     {
                       System.out.println("Missing value for data mapping " + outMapping.getId()
                              + " on activity instance " + ai);
                     }
                  }
               }

               // destroy interaction resource
               registry.unregisterInteraction(interaction.getId());
            
            ai = getWorkflowService().complete(
                  new Long(oid).longValue(), ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID, outData);
            }
            else
            {
               System.out.println("Failed resolving interaction resource for activity instance " + ai);
            }
         }  
      } else {
         org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry registry = getInteractionRegistryManual();
         if (null != registry)
         {
            // retrieve out data
            org.eclipse.stardust.ui.web.processportal.interaction.Interaction interaction = registry.getInteraction(getInteractionId(ai));
            if (null != interaction)
            {
               if (null != interaction.getOutDataValues())
               {
                  try
                  {
                     outData = InteractionDataUtils.unmarshalData(interaction.getModel(),
                           interaction.getDefinition(), (Map)interaction.getOutDataValues(), interaction, null);
                  }
                  catch (DataException e)
                  {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                  //interaction.setOutDataValues(outData);
               }

               // destroy interaction resource
               registry.unregisterInteraction(interaction.getId());

               ai = getWorkflowService().complete(
                     new Long(oid).longValue(), PredefinedConstants.DEFAULT_CONTEXT, outData);
            }
            else
            {
               System.out.println("Failed resolving interaction resource for activity instance "
                     + ai);
            }
         }
      }
      
       // TODO @SG
      JsonObject activityInstanceJson = new JsonObject();
      return activityInstanceJson;
   }

	/**
	 * 
	 * @param oid
	 * @param registry
	 * @return
	 */
	public JsonObject suspendActivity(String oid) {
    	getWorkflowService().suspendToDefaultPerformer(new Long(oid).longValue());
		
	    // TODO @SG
		JsonObject activityInstanceJson = new JsonObject();
		return activityInstanceJson;
	}
	
   /**
    * @param oid
    * @return
    */
   public JsonObject suspendAndSaveActivity(String oid)
   {
      ActivityInstance ai = getWorkflowService().getActivityInstance(
            new Long(oid).longValue());
      // IActivityInteractionController interactionController = SpiUtils
      // .getInteractionController(ai.getActivity());

      Map<String, Serializable> outData = null;

      if (null != ai.getActivity().getApplicationContext(
            ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID))
      {
         InteractionRegistry registry = getInteractionRegistry();
         if (null != registry)
         {
            // retrieve out data
            Interaction interaction = registry.getInteraction(getInteractionId(ai));
            if (null != interaction)
            {
               Map<String, Serializable> outParams = interaction.getOutDataValues();
               if (null != outParams)
               {
                  // performing client side OUT mappings
                  outData = newHashMap();
                  for (DataMapping outMapping : (List<DataMapping>) interaction.getDefinition()
                        .getAllOutDataMappings())
                  {
                     Serializable outParam = outParams.get(outMapping.getApplicationAccessPoint()
                           .getId());
                     if (null != outParam)
                     {
                        try
                        {
                           Object outValue = ClientSideDataFlowUtils.evaluateClientSideOutMapping(
                                 interaction.getModel(), ai.getActivity(), outParam, outMapping);

                           outData.put(outMapping.getId(), (Serializable) outValue);
                        }
                        catch (Exception e)
                        {
                           System.out.println("Failed evaluating client side of OUT data mapping "
                                 + outMapping.getId() + " on activity instance " + ai);
                        }
                     }
                     else
                     {
                        System.out.println("Missing value for data mapping "
                              + outMapping.getId() + " on activity instance " + ai);
                     }
                  }
               }

               // destroy interaction resource
               registry.unregisterInteraction(interaction.getId());

               ai = getWorkflowService().suspendToDefaultPerformer(
                     new Long(oid).longValue(),
                     ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID,
                     outData);
            }
            else
            {
               System.out.println("Failed resolving interaction resource for activity instance "
                     + ai);
            }
         }
      }
      else
      {
         org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry registry = getInteractionRegistryManual();
         if (null != registry)
         {
            // retrieve out data
            org.eclipse.stardust.ui.web.processportal.interaction.Interaction interaction = registry.getInteraction(getInteractionId(ai));
            if (null != interaction)
            {
               if (null != interaction.getOutDataValues())
               {
                  try
                  {
                     outData = InteractionDataUtils.unmarshalData(interaction.getModel(),
                           interaction.getDefinition(),
                           (Map) interaction.getOutDataValues(), interaction, null);
                  }
                  catch (DataException e)
                  {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
                  // interaction.setOutDataValues(outData);
               }

               // destroy interaction resource
               registry.unregisterInteraction(interaction.getId());

               ai = getWorkflowService().suspendToDefaultPerformer(
                     new Long(oid).longValue(), PredefinedConstants.DEFAULT_CONTEXT, outData);
            }
            else
            {
               System.out.println("Failed resolving interaction resource for activity instance "
                     + ai);
            }
         }
      }

      // TODO @SG
      JsonObject activityInstanceJson = new JsonObject();
      return activityInstanceJson;
   }

   /**
    * @param criteria
    * @return
    */
   public JsonObject getActivityInstances(ActivitySearchCriteria criteria)
   {
      JsonObject resultJson = new JsonObject();
      JsonArray activityInstancesJson = new JsonArray();

      resultJson.add("activityInstances", activityInstancesJson);

      QueryResult<ActivityInstance> activityInstances = getQueryService().getAllActivityInstances(
            ActivitySearchHelper.buildActivitySearchQuery(criteria));

      for (ActivityInstance activityInstance : activityInstances)
      {
         activityInstancesJson.add(getActivityInstanceJson(activityInstance));
      }
      
      resultJson.add("paginationResponse", SearchHelperUtil.getPaginationResponseObject(activityInstances));

      return resultJson;
   }

   /**
    * 
    * @return
    */
   public JsonObject getActivityInstanceStates() {
      JsonObject resultJson = new JsonObject();
      JsonArray activityInstanceStatesJson = new JsonArray();

      resultJson.add("activityInstanceStates", activityInstanceStatesJson);

      Set<ActivityInstanceState> activityInstanceStates = new LinkedHashSet<ActivityInstanceState>();
      activityInstanceStates.add(ActivityInstanceState.Created);
      activityInstanceStates.add(ActivityInstanceState.Application);
      activityInstanceStates.add(ActivityInstanceState.Suspended);
      activityInstanceStates.add(ActivityInstanceState.Completed);
      activityInstanceStates.add(ActivityInstanceState.Aborting);
      activityInstanceStates.add(ActivityInstanceState.Aborted);
      activityInstanceStates.add(ActivityInstanceState.Interrupted);
      activityInstanceStates.add(ActivityInstanceState.Hibernated);
      
      for (ActivityInstanceState activityInstanceState : activityInstanceStates)
      {
         JsonObject activityInstanceStateJson = new JsonObject();
         activityInstanceStatesJson.add(activityInstanceStateJson);
         
         activityInstanceStateJson.addProperty("name", activityInstanceState.getName());
         activityInstanceStateJson.addProperty("value", activityInstanceState.getValue());
      }
      
      return resultJson;
   }

   /**
    * 
    * @return
    */
   public ActivityInstance getActivityInstance(long activityInstanceOid) {
      ActivityInstanceQuery activityInstanceQuery = ActivityInstanceQuery.findAll();

      activityInstanceQuery.where(ActivityInstanceQuery.OID.isEqual(activityInstanceOid));
      activityInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      
      ActivityInstanceDetails activityInstance = (ActivityInstanceDetails) getQueryService()
            .getAllActivityInstances(activityInstanceQuery).get(0);
      
      return activityInstance;
   }

   /**
    * @param activityInstance
    * @return
    */
   public JsonObject getActivityInstanceJson(ActivityInstance activityInstance) {
      JsonObject activityInstanceJson = new JsonObject();

      long timeInMillis = Calendar.getInstance().getTimeInMillis();
      if (activityInstance.getState() == ActivityInstanceState.Completed
            || activityInstance.getState() == ActivityInstanceState.Aborted) {
         timeInMillis = activityInstance.getLastModificationTime().getTime();
      }
      long duration = timeInMillis - activityInstance.getStartTime().getTime();
      
      String lastPerformer;
      UserInfo userInfo = activityInstance.getPerformedBy();
      if (null != userInfo) {
//           User user = UserUtils.getUser(userInfo.getId());
           lastPerformer = "motu"; // I18nUtils.getUserLabel(user);
      }
      else {
         lastPerformer = activityInstance.getPerformedByName();
      }

      activityInstanceJson.addProperty("oid", activityInstance.getOID());
      activityInstanceJson.addProperty("criticality", activityInstance.getCriticality());
      activityInstanceJson.addProperty("status", activityInstance.getState().getName()); // TODO: i18n
      
      activityInstanceJson.addProperty("lastPerformer", lastPerformer);
      activityInstanceJson.addProperty("assignedTo", "motu" /*ActivityInstanceUtils.getAssignedToLabel(activityInstance)*/);
      activityInstanceJson.addProperty("duration", duration);
      activityInstanceJson.addProperty("activityId", activityInstance
            .getActivity().getId());
      activityInstanceJson.addProperty("activityImplementationType", ActivityInstanceUtils.getActivityType(activityInstance.getActivity(), false));
      activityInstanceJson.addProperty("activityName", activityInstance
            .getActivity().getName());
      activityInstanceJson.addProperty("processId", activityInstance
            .getActivity().getProcessDefinitionId());
      activityInstanceJson.addProperty("processName", activityInstance
            .getActivity().getProcessDefinitionId());
      activityInstanceJson.addProperty("processInstanceOid",
            activityInstance.getProcessInstanceOID());
      activityInstanceJson.addProperty("startTime", activityInstance
            .getStartTime().getTime());
      activityInstanceJson.addProperty("lastModificationTime",
            activityInstance.getLastModificationTime().getTime());
      activityInstanceJson.addProperty("activatable",
            ActivityInstanceUtils.isActivatable(activityInstance) && (SpiUtils.getInteractionController(activityInstance.getActivity()) == SpiUtils.DEFAULT_EXTERNAL_WEB_APP_CONTROLLER || SpiUtils.getInteractionController(activityInstance.getActivity()) == SpiUtils.DEFAULT_MANUAL_ACTIVITY_CONTROLLER));

      /*JsonObject descriptorsJson = new JsonObject();

      activityInstanceJson.add("descriptors", descriptorsJson);
      
      for (DataPath dataPath : activityInstance
            .getDescriptorDefinitions()) {
         
         descriptorsJson.addProperty(dataPath.getId(),
               (String) activityInstance.getDescriptorValue(dataPath
                     .getId()));
      }*/
      
//      if (activityInstance.getState().getValue() == ActivityInstanceState.APPLICATION) {
      
      ApplicationContext applicationContext = null;
      JsonObject applicationContextsJson = new JsonObject();

      activityInstanceJson.add("contexts", applicationContextsJson);

      if (activityInstance.getActivity().getImplementationType() == ImplementationType.Manual) {
         applicationContext = activityInstance.getActivity()
               .getApplicationContext(PredefinedConstants.DEFAULT_CONTEXT);
         activityInstanceJson.addProperty("implementation", "manual");

         JsonObject applicationContextJson = new JsonObject();

         applicationContextsJson.add(PredefinedConstants.DEFAULT_CONTEXT, applicationContextJson);
         setIPPInteractionParamsForManualActivity(applicationContextJson, activityInstance);
      } else {
         activityInstanceJson.addProperty("implementation", "application");

         JsonObject applicationContextJson = new JsonObject();

         // TODO Handle others

         applicationContextsJson.add(ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID,
               applicationContextJson);

         applicationContext = activityInstance.getActivity()
               .getApplicationContext(ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID);
         setIPPInteractionParams(applicationContextJson, activityInstance);
      }
//      }
      
      JsonObject processInstanceJson = new JsonObject();
      ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findAll();

      processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(activityInstance.getProcessInstanceOID()));
      processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      
      ProcessInstance processInstance = (ProcessInstanceDetails) getQueryService()
            .getAllProcessInstances(processInstanceQuery).get(0);
      
      activityInstanceJson.add("processInstance", processInstanceJson);

      JsonObject descriptorsJson = new JsonObject();
      processInstanceJson.add("descriptors", descriptorsJson);
      
      // Map descriptors

      for (String key : ((ProcessInstanceDetails) processInstance)
            .getDescriptors().keySet()) {
         Object value = ((ProcessInstanceDetails) processInstance)
               .getDescriptorValue(key);

         JsonObject descriptorJson = new JsonObject();

         descriptorsJson.add(key, descriptorJson);

         descriptorJson.addProperty("id", key);
         descriptorJson.addProperty("name", key);

         if (value == null) {
            descriptorJson.addProperty("value", (String) null);
         } else if (value instanceof Boolean) {
            descriptorJson.addProperty("value", (Boolean) value);

         } else if (value instanceof Character) {
            descriptorJson.addProperty("value", (Character) value);

         } else if (value instanceof Number) {
            descriptorJson.addProperty("value", (Number) value);

         } else {
            descriptorJson.addProperty("value", value.toString());
         }
      }

      JsonArray documentsJson = new JsonArray();
      processInstanceJson.add("documents", documentsJson);
      List<Document> processAttachments = fetchProcessAttachments(processInstance);
//    List<TypedDocument> typedDocuments = getTypeDocuments(processInstance);
      for (Document document : processAttachments) {
         documentsJson.add(marshalDocument(document));
      }

      JsonArray notesJson = new JsonArray();

      processInstanceJson.add("notes", notesJson);

      ProcessInstance scopedProcessInstance = getScopedProcessInstance(processInstance);
      for (Note note : scopedProcessInstance.getAttributes().getNotes()) {
         JsonObject noteJson = marshalNote(note);

         notesJson.add(noteJson);
      }

      return activityInstanceJson;
   }
   
   /**
    * @param request
    * @param ir
    * @return
    */
   public JsonObject startProcessInstance(JsonObject request, InteractionRegistry ir)
   {
      ProcessInstance processInstance = getWorkflowService().startProcess(request.get("processDefinitionId").getAsString(), null, true);
      
      JsonObject processInstanceJson = getProcessInstanceJson(processInstance);
      
      if (!(ProcessInstanceUtils.isTransientProcess(processInstance) || ProcessInstanceUtils
            .isCompletedProcess(processInstance)))
      {
         ActivityInstance ai = getWorkflowService()
               .activateNextActivityInstanceForProcessInstance(processInstance.getOID());
         if (ai != null)
         {
            JsonObject activityInstanceJson = getActivityInstanceJson(ai);            
            processInstanceJson.add("activatedActivityInstance", activityInstanceJson);            
            try
            {
               String context = SpiUtils.getInteractionController(ai.getActivity()).getContextId(ai);
               Map inData = workflowService.getInDataValues(ai.getOID(), context, null);
               if (null != ai.getActivity().getApplicationContext(ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID))
               {
                  // TODO @SG
                  Interaction interaction = new Interaction(null, serviceFactory.getQueryService().getModel(ai.getModelOID(), false), ai, ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID, serviceFactory);
                  // performing client side IN mappings
                  Map<String, Serializable> inParams = newHashMap();
                  for (DataMapping inMapping : (List<DataMapping>) interaction.getDefinition().getAllInDataMappings())
                  {
                     Serializable inValue = (Serializable) inData.get(inMapping.getId());
                     if (null != inValue)
                     {
                        try
                        {
                           String paramId = inMapping.getApplicationAccessPoint().getId();
      
                           Object inParam = ClientSideDataFlowUtils.evaluateClientSideInMapping(
                                 interaction.getModel(), ai.getActivity(), inParams.get(paramId), inMapping, inValue);
      
                           inParams.put(paramId, (Serializable) inParam);
                        }
                        catch (Exception e)
                        {
                           System.out.println("Failed evaluating client side of IN data mapping "
                                 + inMapping.getId() + " on activity instance " + ai);
                        }
                     }
                  }
      
                  interaction.setInDataValues(inParams);
                  
                  ir.registerInteraction(interaction);
               }
               else
               {
                  // Manual activity
                  org.eclipse.stardust.ui.web.processportal.interaction.Interaction interaction = new org.eclipse.stardust.ui.web.processportal.interaction.Interaction(
                        serviceFactory.getQueryService().getModel(ai.getModelOID(), false), ai,
                        PredefinedConstants.DEFAULT_CONTEXT, serviceFactory);
                  ManualActivityUi manualActivityUi = new ManualActivityUi(ai, ai.getActivity()
                        .getApplicationContext(PredefinedConstants.DEFAULT_CONTEXT),
                        getQueryService());
                  interaction.setManualActivityPath(manualActivityUi.getManualActivityPath());
                  Map<String, Serializable> configuration = new HashMap<String, Serializable>();
                  // TODO - need this to be configurable for mobile?
                  configuration.put("layoutColumns", 1);
                  configuration.put("tableColumns", 1);
                  interaction.setConfiguration(configuration);
                  interaction.setInDataValues(inData);
                  
                  // TODO - check 
                  // The line below is commented as it has dependency on FacesContext
                  // We can possibly get around it but it may not be needed as we are not supporting
                  // document data anyways
                  // DocumentHelper.initializeDocumentControllers(interaction, inData);

                  getInteractionRegistryManual().registerInteraction(interaction);
               }
            }
            catch (Exception e)
            {
               System.out.println("No externalWebapp for this AI.");
            }
            
         }
      }
      
      return processInstanceJson;
   }
   
   /**
    * @param activityInstanceOid
    * @param delegateName
    * @return
    */
   public JsonObject getDelegates(String activityInstanceOid, String delegateName)
   {
      DelegationHelper helper = new DelegationHelper(getQueryService(),
            getWorkflowService());
      return helper.getMatchingDelegates(activityInstanceOid, delegateName);
   }

   /**
    * @param activityInstanceOid
    * @param delegateId
    * @return
    */
   public JsonObject delegateActivity(String activityInstanceOid, String delegateId)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      FilterAndTerm filter = query.getFilter().addAndTerm();
      filter.and(ActivityInstanceQuery.OID.isEqual(Long.parseLong(activityInstanceOid)));
      QueryResult<ActivityInstance> activityInstances = getQueryService().getAllActivityInstances(
            query);

      DelegationHelper helper = new DelegationHelper(getQueryService(),
            getWorkflowService());
      return getActivityInstanceJson(helper.delegateActivity(activityInstances.get(0),
            delegateId));
   }
   
   /**
    * @param criteria
    * @return
    */
   public JsonObject getProcessInstances(ProcessSearchCriteria criteria)
   {
      JsonObject resultJson = new JsonObject();
      JsonArray processInstancesJson = new JsonArray();

      resultJson.add("processInstances", processInstancesJson);
      
      QueryResult<ProcessInstance> processInstances = getQueryService().getAllProcessInstances(
            ProcessSearchHelper.buildProcessSearchQuery(criteria));

      for (ProcessInstance processInstance : processInstances)
      {
         processInstancesJson.add(getProcessInstanceJson(processInstance));
      }

      resultJson.add("paginationResponse", SearchHelperUtil.getPaginationResponseObject(processInstances));
      
      return resultJson;
   }

   /**
    * 
    * @return
    */
   public JsonObject getProcessInstanceStates() {
      JsonObject resultJson = new JsonObject();
      JsonArray processInstanceStatesJson = new JsonArray();

      resultJson.add("processInstanceStates", processInstanceStatesJson);

      Set<ProcessInstanceState> processInstanceStates = new LinkedHashSet<ProcessInstanceState>();
      processInstanceStates.add(ProcessInstanceState.Active);
      processInstanceStates.add(ProcessInstanceState.Completed);
      processInstanceStates.add(ProcessInstanceState.Aborted);
      processInstanceStates.add(ProcessInstanceState.Created);
      processInstanceStates.add(ProcessInstanceState.Aborting);
      
      for (ProcessInstanceState processInstanceState : processInstanceStates)
      {
         JsonObject processInstanceStateJson = new JsonObject();
         processInstanceStatesJson.add(processInstanceStateJson);

         processInstanceStateJson.addProperty("name", processInstanceState.getName());
         processInstanceStateJson.addProperty("value", processInstanceState.getValue());
      }
      
      return resultJson;
   }

   /**
    * 
    * @return
    */
   public ProcessInstance getProcessInstance(long oid) {
      ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery
            .findAll();

      processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(oid));
      processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

      ProcessInstanceDetails processInstance = (ProcessInstanceDetails) getQueryService()
            .getAllProcessInstances(processInstanceQuery).get(0);
      
      return processInstance;
   }

   /**
	 * 
	 * @return
	 */
	public JsonObject getProcessInstanceJson(ProcessInstance processInstance) {
		JsonObject processInstanceJson = new JsonObject();

      processInstanceJson.addProperty("oid",
            processInstance.getOID());
		processInstanceJson.addProperty("processId",
				processInstance.getProcessID());
		processInstanceJson.addProperty("processName",
				processInstance.getProcessName());
		processInstanceJson.addProperty("startTimestamp", processInstance
				.getStartTime().getTime());

		if (processInstance.getTerminationTime() != null) {
			processInstanceJson.addProperty("terminationTimestamp",
					processInstance.getTerminationTime().getTime());
		}

		processInstanceJson.addProperty("state", processInstance.getState()
				.getName());
		processInstanceJson.addProperty("priority",
				processInstance.getPriority());
//		processInstanceJson.add("startingUser", marshalUser(processInstance.getStartingUser()));
      processInstanceJson.addProperty("startingUser", "motu");

		JsonObject descriptorsJson = new JsonObject();

		processInstanceJson.add("descriptors", descriptorsJson);

		// Map descriptors

		for (String key : ((ProcessInstanceDetails) processInstance)
				.getDescriptors().keySet()) {
			Object value = ((ProcessInstanceDetails) processInstance)
					.getDescriptorValue(key);

			JsonObject descriptorJson = new JsonObject();

			descriptorsJson.add(key, descriptorJson);

			descriptorJson.addProperty("id", key);
			descriptorJson.addProperty("name", key);

			if (value == null) {
				descriptorJson.addProperty("value", (String) null);
			} else if (value instanceof Boolean) {
				descriptorJson.addProperty("value", (Boolean) value);

			} else if (value instanceof Character) {
				descriptorJson.addProperty("value", (Character) value);

			} else if (value instanceof Number) {
				descriptorJson.addProperty("value", (Number) value);

			} else {
				descriptorJson.addProperty("value", value.toString());
			}
		}

		JsonObject historicalEventsJson = new JsonObject();

		processInstanceJson.add("events", historicalEventsJson);

		for (HistoricalEvent historicalEvent : processInstance
				.getHistoricalEvents()) {
			JsonObject historicalEventJson = new JsonObject();

			historicalEventJson.addProperty("timestamp", historicalEvent
					.getEventTime().getTime());
			historicalEventJson.addProperty("type", historicalEvent
					.getEventType().getName());
		}
		
		JsonArray documentsJson = new JsonArray();
		processInstanceJson.add("documents", documentsJson);
		List<Document> processAttachments = fetchProcessAttachments(processInstance);
//		List<TypedDocument> typedDocuments = getTypeDocuments(processInstance);
		for (Document document : processAttachments) {
			documentsJson.add(marshalDocument(document));
		}

		JsonArray notesJson = new JsonArray();

		processInstanceJson.add("notes", notesJson);

		for (Note note : processInstance.getAttributes().getNotes()) {
			JsonObject noteJson = marshalNote(note);

			notesJson.add(noteJson);
		}

		// TODO Replace

		JsonObject participantsJson = new JsonObject();

		processInstanceJson.add("participants", participantsJson);

		UserQuery userQuery = UserQuery.findAll();

		for (User user : getQueryService().getAllUsers(userQuery)) {
			participantsJson.add(user.getId(), marshalUser(user));
		}

		return processInstanceJson;
	}

   /**
    * returns IN data paths details associated with provided process instance data in
    * Association between IN datapath and corresponding OUT datapath is determined by
    * comparing document data in different datapaths datapaths having duplicate document
    * data will be ignored
    * 
    * @param processInstance
    * @return
    */
   public List<TypedDocument> getTypeDocuments(ProcessInstance processInstance)
   {
      DeployedModel model = getQueryService().getModel(processInstance.getModelOID());
      ProcessDefinition processDefinition = model.getProcessDefinition(processInstance
            .getProcessID());

      Map<String, TypedDocument> typedDocumentsData = new HashMap<String, TypedDocument>();
      Map<String, DataPath> outDataMappings = new HashMap<String, DataPath>();
      @SuppressWarnings("rawtypes")
      List dataPaths = processDefinition.getAllDataPaths();

      TypedDocument typedDocument;
      String dataDetailsQId;

      for (Object objectDataPath : dataPaths)
      {
         DataPath dataPath = (DataPath) objectDataPath;
         DataDetails dataDetails = (DataDetails) model.getData(dataPath.getData());
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()))
         {
            dataDetailsQId = dataDetails.getQualifiedId();
            Direction direction = dataPath.getDirection();
            if (Direction.IN.equals(direction)
                  && !typedDocumentsData.containsKey(dataDetailsQId))
            {
               try
               {
                  typedDocument = new TypedDocument(processInstance, dataPath,
                        dataDetails);
                  if (outDataMappings.containsKey(dataDetailsQId))
                  {
                     typedDocument.setDataPath(outDataMappings.get(dataDetailsQId));
                     typedDocument.setOutMappingExist(true);
                  }
                  typedDocumentsData.put(dataDetailsQId, typedDocument);
               }
               catch (Exception e)
               {
                  System.out.println(e);
               }
            }
            else if (Direction.OUT.equals(direction))
            {
               if (typedDocumentsData.containsKey(dataDetailsQId))
               {
                  typedDocument = typedDocumentsData.get(dataDetailsQId);
                  if (!typedDocument.isOutMappingExist())
                  {
                     typedDocument.setDataPath(dataPath);
                     typedDocument.setOutMappingExist(true);
                  }
               }
               else
               {
                  outDataMappings.put(dataDetailsQId, dataPath);
               }
            }
         }
      }

      return new ArrayList<TypedDocument>((typedDocumentsData.values()));
   }

   /**
    * 
    * @return
    */
   public JsonObject getNotes(long processInstanceOid)
   {
      ProcessInstance processInstance = getWorkflowService().getProcessInstance(
            processInstanceOid);
      ProcessInstance scopeProcessInstance = processInstance.getScopeProcessInstance();
      JsonObject resultJson = new JsonObject();
      JsonArray notesJson = new JsonArray();

      resultJson.add("notes", notesJson);

      for (Note note : scopeProcessInstance.getAttributes().getNotes())
      {
         notesJson.add(marshalNote(note));
      }

      return resultJson;
   }

   /**
    * 
    * @return
    */
   public JsonObject getProcessInstanceDocuments(long processInstanceOid)
   {
      ProcessInstance processInstance = getWorkflowService().getProcessInstance(
            processInstanceOid);
      JsonObject resultJson = new JsonObject();
      JsonArray documentsJson = new JsonArray();

      List<Document> processAttachments = fetchProcessAttachments(processInstance);
      // List<TypedDocument> typedDocuments =
      // TypedDocumentsUtil.getTypeDocuments(processInstance);
      resultJson.add("documents", documentsJson);

      for (Document document : processAttachments)
      {
         documentsJson.add(marshalDocument(document));
      }

      return resultJson;
   }

   /**
    * @param oid
    * @param postedData
    * @return
    */
   public JsonObject updateProcessInstance(String oid, JsonObject postedData)
   {
      if (postedData.has("priority"))
      {
         serviceFactory.getAdministrationService().setProcessInstancePriority(
               Long.parseLong(oid), postedData.get("priority").getAsInt(), true);
      }

      ProcessInstance processInstance = getWorkflowService().getProcessInstance(
            Long.parseLong(oid));
      return getProcessInstanceJson(processInstance);
   }

   public JsonObject getProcessInstanceDocument(long processInstanceOid, String documentId)
   {
      Document document = getDocumentManagementService().getDocument(documentId);
      
      JsonObject documentJson = marshalDocument(document);
      
      return documentJson;
   }
   
	/**
	 * 
	 * @return
	 */
	public JsonObject getRepositoryFolder(String folderId) {
		JsonObject folderJson = new JsonObject();
		JsonObject childrenJson = new JsonObject();
		JsonArray subFoldersJson = new JsonArray();
      JsonArray documentsJson = new JsonArray();

      childrenJson.add("folders", subFoldersJson);
      childrenJson.add("documents", documentsJson);
		folderJson.add("children", childrenJson);

		if (folderId == null || folderId.equals("") || folderId.equals("root")) {
	      folderJson.addProperty("id", "root");
	      folderJson.addProperty("name", "Root");
	      folderJson.addProperty("path", "/");
	      folderJson.addProperty("childCount", 2);
	      
			JsonObject subFolderJson = new JsonObject();

			subFoldersJson.add(subFolderJson);

			subFolderJson.addProperty("id", publicDocumentsRootFolder.getId());
			subFolderJson.addProperty("path",
					publicDocumentsRootFolder.getPath());

//			getFolderContent(subFolderJson, publicDocumentsRootFolder);

			// Overwrite name
			
			subFolderJson.addProperty("name", "Common Documents");
			subFolderJson.addProperty("childCount", publicDocumentsRootFolder.getFolderCount() + publicDocumentsRootFolder.getDocumentCount());

			if (userDocumentsRootFolder != null) {
				subFolderJson = new JsonObject();

				subFoldersJson.add(subFolderJson);

				subFolderJson
						.addProperty("id", userDocumentsRootFolder.getId());
				subFolderJson.addProperty("path",
						userDocumentsRootFolder.getPath());
				subFolderJson.addProperty("childCount", userDocumentsRootFolder.getFolderCount() + userDocumentsRootFolder.getDocumentCount());

//				getFolderContent(subFolderJson, userDocumentsRootFolder);

				// Overwrite name 
				
				subFolderJson.addProperty("name", "My Documents");
			}
		} else {
			getFolderContent(folderJson, getDocumentManagementService()
					.getFolder(folderId, Folder.LOD_LIST_MEMBERS_OF_MEMBERS));
		}

		return folderJson;
	}

	/**
	 * 
	 * @param folderJson
	 * @param folder
	 * @return
	 */
	private JsonObject getFolderContent(JsonObject folderJson, Folder folder) {
		folderJson.addProperty("id", folder.getId());
		folderJson.addProperty("name", folder.getName());
		folderJson.addProperty("path", folder.getPath());
		
		JsonArray subFoldersJson = null;

		/*if (!folderJson.has("subFolders")) {
			folderJson.add("subFolders", subFoldersJson = new JsonArray());
		} else {
			subFoldersJson = folderJson.get("subFolders").getAsJsonArray();
		}*/

      subFoldersJson = folderJson.get("children").getAsJsonObject().get("folders").getAsJsonArray();
		
		for (Folder subFolder : (List<Folder>) folder.getFolders()) {
			JsonObject subFolderJson = new JsonObject();

			subFoldersJson.add(subFolderJson);

			subFolderJson.addProperty("id", subFolder.getId());
			subFolderJson.addProperty("name", subFolder.getName());
			subFolderJson.addProperty("path", subFolder.getPath());
         subFolderJson.addProperty("childCount", subFolder.getFolderCount() + subFolder.getDocumentCount());
		}

		JsonArray documentsJson = null;

		/*if (!folderJson.has("documents")) {
			folderJson.add("documents", documentsJson = new JsonArray());
		} else {
			documentsJson = folderJson.get("documents").getAsJsonArray();
		}*/
		documentsJson = folderJson.get("children").getAsJsonObject().get("documents").getAsJsonArray();

		for (Document document : (List<Document>) folder.getDocuments()) {
			JsonObject documentJson = marshalDocument(document);

			documentsJson.add(documentJson);
		}

		return folderJson;
	};

	private String getUserDocumentsRootFolderPath() {
		return "/realms/" + getLoginUser().getRealm().getId() + "/users/"
				+ getLoginUser().getAccount() + "/documents";
	}

	private String getPublicDocumentsRootFolderPath() {
		return "/documents";
	}

	/**
	 * 
	 * @param request
	 */
	public JsonObject createNote(JsonObject request) {
		ProcessInstance processInstance = getWorkflowService()
				.getProcessInstance(
						request.get("processInstanceOid").getAsLong());
		ProcessInstance scopeProcessInstance = processInstance.getScopeProcessInstance();
		ProcessInstanceAttributes attributes = scopeProcessInstance.getAttributes();

		Note note = attributes.addNote(request.get("content").getAsString(),
				ContextKind.ProcessInstance, processInstance.getOID());
		getWorkflowService().setProcessInstanceAttributes(attributes);

		JsonObject noteJson = new JsonObject();

		noteJson.addProperty("content", note.getText());
		noteJson.addProperty("timestamp", new Date().getTime());

		return noteJson;
	}

	/**
	 * 
	 * @param request
	 */
	private JsonObject marshalNote(Note note) {
		JsonObject noteJson = new JsonObject();

		noteJson.addProperty("content", note.getText());
		noteJson.addProperty("timestamp", note.getTimestamp().getTime());

		JsonObject userJson = marshalUser(note.getUser());
		noteJson.add("user", userJson);

		return noteJson;
	}

   /**
    * 
    * @param request
    */
   private JsonObject marshalUser(User user) {
      JsonObject userJson = new JsonObject();

      userJson.addProperty("id", user.getId());
      userJson.addProperty("firstName", user.getFirstName());
      userJson.addProperty("lastName", user.getLastName());
      userJson.addProperty("name", user.getName());
      userJson.addProperty("eMail", user.getEMail());
      userJson.addProperty("description", user.getDescription());

      return userJson;
   }

	/**
	 * 
	 * @param request
	 */
	private JsonObject marshalDocument(Document document) {
		JsonObject documentJson = new JsonObject();

		documentJson.addProperty("id", document.getId());
		documentJson.addProperty("name", document.getName());
		documentJson.addProperty("contentType", document.getContentType());
      documentJson.addProperty("createdTimestamp", document.getDateCreated().getTime());
		documentJson.addProperty("lastModifiedTimestamp", document.getDateLastModified().getTime());
		documentJson.addProperty("size", document.getSize());
      documentJson.addProperty("downloadToken",
            getDocumentManagementService()
                  .requestDocumentContentDownload(document.getId()));

		return documentJson;
	}

   /**
    * @param pi
    * @return
    */
   private List<Document> fetchProcessAttachments(ProcessInstance processInstance)
   {
      List<Document> processAttachments = new ArrayList<Document>();

      DeployedModel model = getQueryService().getModel(processInstance.getModelOID());
      ProcessDefinition processDefinition = model.getProcessDefinition(processInstance.getProcessID()) ;
      List dataPaths = processDefinition.getAllDataPaths();
      
      for (int n = 0; n < dataPaths.size(); ++n)
      {
         DataPath dataPath = (DataPath) dataPaths.get(n);

         if (!dataPath.getDirection().equals(Direction.IN))
         {
            continue;
         }

         try
         {
            if (dataPath.getId().equals(CommonProperties.PROCESS_ATTACHMENTS))
            {
               Object object = getWorkflowService().getInDataPath(processInstance.getOID(), dataPath.getId());

               if (object != null)
               {
                  processAttachments.addAll((Collection) object);
                  break;
               }
            }
         }
         catch (Exception e)
         {
            System.out.println("Error fetching Process Attachments: " + e.getMessage());
         }
      }
      
      return processAttachments;
   }

	public void addProcessAttachment(long processOid, File uploadedFile) {
		  ProcessInstance processInstance = getWorkflowService().getProcessInstance(processOid);
	      List<Document> processAttachments = fetchProcessAttachments(processInstance);

	        FileInputStream fis = null;
			try {
				fis = new FileInputStream(uploadedFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //System.out.println(file.exists() + "!!");
	        //InputStream in = resource.openStream();
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] buf = new byte[1024];
	        try {
	            for (int readNum; (readNum = fis.read(buf)) != -1;) {
	                bos.write(buf, 0, readNum); //no doubt here is 0
	                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
	                System.out.println("read " + readNum + " bytes,");
	            }
	        } catch (IOException ex) {
	            System.out.println(ex);
	        }
	        byte[] bytes = bos.toByteArray();	      
	        
	      DocumentInfo docInfo = DmsUtils.createDocumentInfo(uploadedFile.getName());
	      String folderPath = DocumentMgmtUtility.getProcessAttachmentsFolderPath(processInstance);
	      createFolderIfNotExists(folderPath);
	      Document document = getDocumentManagementService().createDocument(folderPath, docInfo, bytes, "");
	      
	      processAttachments.add(document);
	      getWorkflowService().setOutDataPath(processInstance.getOID(),
	              CommonProperties.PROCESS_ATTACHMENTS, processAttachments);
	}

   /**
    * Returns the folder if exist otherwise create new folder
    * 
    * @param folderPath
    * @return
    */
   private Folder createFolderIfNotExists(String folderPath)
   {
      Folder folder = getDocumentManagementService().getFolder(folderPath,
            Folder.LOD_NO_MEMBERS);

      if (null == folder)
      {
         // folder does not exist yet, create it
         String parentPath = folderPath.substring(0, folderPath.lastIndexOf('/'));
         String childName = folderPath.substring(folderPath.lastIndexOf('/') + 1);

         if (StringUtils.isEmpty(parentPath))
         {
            // top-level reached
            return getDocumentManagementService().createFolder("/",
                  DmsUtils.createFolderInfo(childName));
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

   public JsonObject getRepositoryDocument(String folderId, String documentId)
   {
      Document document = getDocumentManagementService().getDocument(documentId);
      
      JsonObject documentJson = marshalDocument(document);
      
      return documentJson;
   }

   public JsonObject getProcessHistory(long processOid, long selectedProcessInstanceOid)
   {
      JsonObject historyJson = new JsonObject();

      selectedProcessInstanceOid = (selectedProcessInstanceOid == 0)
            ? processOid
            : selectedProcessInstanceOid;
      
      ProcessInstanceQuery processInstanceQuery = ProcessInstanceQuery.findAll();
      processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(processOid));
      processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      ProcessInstanceDetails processInstance = (ProcessInstanceDetails) getQueryService()
            .getAllProcessInstances(processInstanceQuery).get(0);

      ProcessInstanceDetails selectedProcessInstance = null;
      List<ProcessInstance> allPIs = getAllProcesses(processInstance, true);
      
      if (selectedProcessInstanceOid == processOid)
      {
         selectedProcessInstance = processInstance;
      }
      else
      {
         processInstanceQuery = ProcessInstanceQuery.findAll();
         processInstanceQuery.where(ProcessInstanceQuery.OID.isEqual(processOid));
         processInstanceQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         selectedProcessInstance = (ProcessInstanceDetails) getQueryService()
               .getAllProcessInstances(processInstanceQuery).get(0);
      }
      
      historyJson.add("selectedProcessInstance", getProcessInstanceJson(selectedProcessInstance));
      
      List<ActivityInstance> allAIs = getAllActivities(selectedProcessInstance, true);
      
      Map<Long, List<ProcessInstance>> tree = new HashMap<Long, List<ProcessInstance>>();
      List<ProcessInstance> roots = new ArrayList<ProcessInstance>();
      for (ProcessInstance pi : allPIs)
      {
         if (pi.getParentProcessInstanceOid() == ProcessInstance.UNKNOWN_OID)
            roots.add(pi);
         else
         {
            if (!tree.containsKey(pi.getParentProcessInstanceOid()))
               tree.put(pi.getParentProcessInstanceOid(), new ArrayList<ProcessInstance>());
            tree.get(pi.getParentProcessInstanceOid()).add(pi);
         }
      }

      JsonArray parentProcessInstancesJson = new JsonArray();
      historyJson.add("parentProcessInstances", parentProcessInstancesJson);
      
      
      Map<Long, ProcessInstance> startingActivityToProcessMap = new HashMap<Long, ProcessInstance>();

      for (ProcessInstance subProcess : allPIs)
      {
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) subProcess;
         startingActivityToProcessMap.put(processInstanceDetails.getStartingActivityInstanceOID(), subProcess);
      }

      JsonArray activityInstancesJson = new JsonArray();
      historyJson.add("activityInstances", activityInstancesJson);
      for (ActivityInstance activityInstance : allAIs)
      {
         JsonObject activityInstanceJson = getActivityInstanceJson(activityInstance); 
         activityInstancesJson.add(activityInstanceJson);
         if (startingActivityToProcessMap.containsKey(activityInstance.getOID()))
         {
            activityInstanceJson.add("childProcessInstance", getProcessInstanceJson(startingActivityToProcessMap.get(activityInstance.getOID())));
         }
      }
      
      return historyJson;
   }

   private List<ProcessInstance> getAllProcesses(ProcessInstance process, boolean includeEvents)
   {
      long rootProcessOid = process.getRootProcessInstanceOID();
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter().and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootProcessOid));
      query.orderBy(ProcessInstanceQuery.START_TIME);
      
      if (includeEvents)
      {
         query.setPolicy(HistoricalEventPolicy.ALL_EVENTS);
      }
      
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      
      ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
            ProcessInstanceDetailsLevel.Default);
      processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
      query.setPolicy(processInstanceDetailsPolicy);
      
      return queryService.getAllProcessInstances(query);
   }

   /**
    * @param processInstance
    * @param includeEvents
    * @return
    */
   private List<ActivityInstance> getAllActivities(ProcessInstance processInstance, boolean includeEvents)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      ProcessInstanceFilter processFilter = new ProcessInstanceFilter(processInstance.getOID(), false);
      aiQuery.where(processFilter);
      aiQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      aiQuery.orderBy(ActivityInstanceQuery.START_TIME).and(ActivityInstanceQuery.OID);
      
      if (includeEvents)
      {
         aiQuery.setPolicy(HistoricalEventPolicy.ALL_EVENTS);
      }
      
      return queryService.getAllActivityInstances(aiQuery);
   }

   /**
    * @param criteria
    * @return
    */
   public JsonObject getDocuments(DocumentSearchCriteria criteria)
   {
      DocumentQuery query = DocumentSearchHelper.buildDocumentSearchQuery(criteria);

      Documents documents = documentManagementService.findDocuments(query);
      
      JsonObject resultJson = new JsonObject();
      JsonArray documentsJson = new JsonArray();

      resultJson.add("documents", documentsJson);

      for (Document document : documents)
      {
         documentsJson.add(marshalDocument(document));
      }

      resultJson.add("paginationResponse", SearchHelperUtil.getPaginationResponseObject(documents));

      return resultJson;
   }
   
   /**
    * @return
    */
   public JsonObject getDocument(String documentId)
   {
      Document document = getDocumentManagementService().getDocument(documentId);
      
      JsonObject documentJson = marshalDocument(document);
      
      return documentJson;
   }

   /**
    * @return
    */
   public JsonObject getDocumentTypes()
   {
      JsonObject resultJson = new JsonObject();
      JsonArray documentTypesJson = new JsonArray();

      resultJson.add("documentTypes", documentTypesJson);

      Set<DocumentType> allDocumentTypes = CollectionUtils.newHashSet();
      List<DeployedModelDescription> models = getQueryService().getModels(DeployedModelQuery.findActive());
      for (DeployedModelDescription model : models)
      {
         DeployedModel deployedModel = getQueryService().getModel(model.getModelOID());
         allDocumentTypes.addAll(DocumentTypeUtils.getDeclaredDocumentTypes(deployedModel));
      }

      for (DocumentType documentType : allDocumentTypes)
      {
         JsonObject documentTypeJson = new JsonObject();

         documentTypesJson.add(documentTypeJson);

         //TODO: Return Name based on TypeDeclaration
         String documentTypeId = documentType.getDocumentTypeId();
         String documentTypeName = documentTypeId; 
         documentTypeJson.addProperty("id", documentTypeId);
         if (documentTypeId.contains("}"))
         {
            documentTypeName = documentTypeId.substring(documentTypeId.lastIndexOf("}") + 1);
         }
         documentTypeJson.addProperty("name", documentTypeName);
      }
      
      return resultJson;
   }
   
   /**
    * @return
    */
   public JsonObject getVersionAndCopyrightInfo()
   {
      JsonObject job = new JsonObject();
      job.addProperty("version", new IppVersion().getCompleteString());
      job.addProperty("copyrightInfo", new IppCopyrightInfo().getMessage());

      return job;
   }

   /**
    * 
    * @return
    */
   private InteractionRegistry getInteractionRegistry() {
      InteractionRegistry registry = (InteractionRegistry) servletContext.getAttribute(InteractionRegistry.BEAN_ID);
      if (registry != null) return registry;
      
      synchronized (this) {
         if (null == registry) {
            registry = new InteractionRegistry();
            servletContext.setAttribute(InteractionRegistry.BEAN_ID, registry);
         }
      }
      
      return registry;
   }
   
   /**
    * 
    * @return
    */
   private org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry getInteractionRegistryManual() {
//      System.out.println("@@@@@@@@@@@@ servletContext " + servletContext);
//    org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry registry = (org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry) servletContext.getAttribute(org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry.BEAN_ID);
//    if (registry != null) return registry;
//    
//    synchronized (this) {
//       if (null == registry) {
//          registry = new org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry();
//          servletContext.setAttribute(org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry.BEAN_ID, registry);
//       }
//    }
      
      return interactionRegistryManual;
   }
   
   /**
    * TODO - need to move to a util as it's almost duplicated from
    * ExternalWebAppActivityInteractionController
    * 
    * @param applicationContextJson
    * @param ai
    */
   private void setIPPInteractionParams(JsonObject applicationContextJson,
         ActivityInstance ai)
   {
      ApplicationContext context = ai.getActivity().getApplicationContext(
            ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID);

      Boolean embedded = (Boolean) context.getAttribute("carnot:engine:ui:externalWebApp:embedded");

      String servicesBaseUri = "";
      String portalBaseUri = "";
      if (null != embedded && embedded)
      {
         servicesBaseUri = "/${request.contextPath}/services/";
         portalBaseUri = "/${request.contextPath}";
      }
      else
      {
         // allow base URI override via parameter
         servicesBaseUri = servletContext.getInitParameter("InfinityBpm.ServicesBaseUri");
         if (isEmpty(servicesBaseUri))
         {
            servicesBaseUri = "${request.scheme}://${request.serverName}:${request.serverPort}/${request.contextPath}/services/";
         }

         portalBaseUri = servletContext.getInitParameter("InfinityBpm.PortalBaseUri");
         if (isEmpty(portalBaseUri))
         {
            portalBaseUri = "${request.scheme}://${request.serverName}:${request.serverPort}/${request.contextPath}";
         }
      }

      servicesBaseUri = expandUriTemplate(servicesBaseUri, httpRequest);
      portalBaseUri = expandUriTemplate(portalBaseUri, httpRequest);
      String interactionId = Interaction.getInteractionId(ai);
      String ippInteractionUri = servicesBaseUri + "rest/engine/interactions/"
            + interactionId;
      applicationContextJson.addProperty("ippPortalBaseURi", portalBaseUri);
      applicationContextJson.addProperty("ippServicesBaseUri", servicesBaseUri);
      applicationContextJson.addProperty("ippInteractionUri", ippInteractionUri);
      applicationContextJson.addProperty("interactionId", interactionId);
      if (null != embedded && embedded)
      {
         applicationContextJson.addProperty("mashupUri", ippInteractionUri
               + "/embeddedMarkup");
      }
      else
      {
         ApplicationContext applicationContext = ai.getActivity().getApplicationContext(
               ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID);
         applicationContextJson.addProperty(
               "mashupUri",
               (String) applicationContext.getAttribute("carnot:engine:ui:externalWebApp:uri"));
      }
   }
   
   /**
    * TODO - need to move to a util as it's almost duplicated from
    * ExternalWebAppActivityInteractionController
    * 
    * @param applicationContextJson
    * @param ai
    */
   private void setIPPInteractionParamsForManualActivity(JsonObject applicationContextJson,
         ActivityInstance ai) {
      String portalBaseUri = expandUriTemplate("/${request.contextPath}/plugins/processportal/manualActivityPanel.html", httpRequest);
      applicationContextJson.addProperty("ippPortalBaseURi", portalBaseUri);
      String interactionId = Interaction.getInteractionId(ai);
      applicationContextJson.addProperty("interactionId", interactionId);
   }

   /**
    * TODO - need to move to a util as it's duplicated from
    * ExternalWebAppActivityInteractionController
    * 
    * @param uriTemplate
    * @param req
    * @return
    */
   private String expandUriTemplate(String uriTemplate, HttpServletRequest req) {
      String uri = uriTemplate;

      if (uri.contains("${request.scheme}")) {
         uri = uri.replace("${request.scheme}", req.getScheme());
      }
      if (uri.contains("${request.serverName}")) {
         uri = uri.replace("${request.serverName}", req.getServerName());
      }
      if (uri.contains("${request.serverLocalName}")
            && !isEmpty(req.getLocalName())) {
         uri = uri.replace("${request.serverLocalName}", req.getLocalName());
      }
      if (uri.contains("${request.serverPort}")) {
         uri = uri.replace("${request.serverPort}",
               Integer.toString(req.getServerPort()));
      }
      if (uri.contains("${request.serverLocalPort}")) {
         uri = uri.replace("${request.serverLocalPort}",
               Integer.toString(req.getLocalPort()));
      }
      if (uri.contains("/${request.contextPath}")) {
         uri = uri.replace("/${request.contextPath}", req.getContextPath());
      }
      return uri;
   }
   
   /**
    * Format Descriptors based on their types
    * 
    * @param valueObj
    * @param accessPath
    * @return
    */
   private String formatDescriptorValue(Object valueObj, String accessPath)
   {
      String value = "";
      CommonPropertiesMessageBean props = new CommonPropertiesMessageBean(
            httpRequest.getLocale());
      if (valueObj instanceof Date)
      {
         if (StringUtils.isNotEmpty(accessPath))
         {
            String dateFormat = props.getString("portalFramework.formats.defaultDateTimeFormat");
            if (accessPath.equalsIgnoreCase(ProcessPortalConstants.DATE_TYPE))
            {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                     props.getString("portalFramework.formats.defaultDateFormat"),
                     httpRequest.getLocale());
               value = simpleDateFormat.format(valueObj);
}
            else if (accessPath.equalsIgnoreCase(ProcessPortalConstants.TIME_TYPE))
            {
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                     props.getString("portalFramework.formats.defaultTimeFormat"),
                     httpRequest.getLocale());
               value = simpleDateFormat.format(valueObj);
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat,
                  httpRequest.getLocale());
            value = simpleDateFormat.format(valueObj);
         }
      }
      else if (valueObj instanceof Boolean)
      {
         value = (Boolean) valueObj
               ? props.getString("common.true")
               : props.getString("common.false");
      }
      else
      {
         value = valueObj != null ? valueObj.toString() : "";
      }
      return value;
   }
   
   /**
    * @param pi
    * @return
    */
   private ProcessInstance getScopedProcessInstance(ProcessInstance pi)
   {
      try
      {
         // Get scoped process instance.
         ProcessInstanceQuery spiq = ProcessInstanceQuery.findAll();
         spiq.where(ProcessInstanceQuery.OID.isEqual(pi.getScopeProcessInstanceOID()));
         spiq.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         return (ProcessInstanceDetails) getQueryService().getAllProcessInstances(spiq)
               .get(0);
      }
      catch (Exception e)
      {
         return pi;
      }
   }
}
