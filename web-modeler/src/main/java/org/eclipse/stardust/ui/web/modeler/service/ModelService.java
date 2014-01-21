/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newApplicationActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.extensions.jaxws.app.WSConstants;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.NameIdUtilsExtension;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DescriptionType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.spi.SpiExtensionRegistry;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.FormalParameterType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationService;
import org.eclipse.stardust.modeling.validation.ValidatorRegistry;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;
import org.eclipse.stardust.ui.web.modeler.edit.MissingWritePermissionException;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.impl.XSDImportImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 *
 * @author Shrikant.Gangal, Marc.Gille
 *
 */
public class ModelService
{
   public static final String TYPE_PROPERTY = "type";

   public static final String X_PROPERTY = "x";

   public static final String Y_PROPERTY = "y";

   public static final String WIDTH_PROPERTY = "width";

   public static final String HEIGHT_PROPERTY = "height";

   public static final String DESCRIPTION_PROPERTY = "description";

   public static final String EVENT_TYPE_PROPERTY = "eventType";

   public static final String START_EVENT = "startEvent";

   private static final Logger trace = LogManager.getLogger(ModelService.class);

   /*
    * Half the size of the review why this adjustment is needed start event symbol used in
    * Pepper TODO - may need to be handled on the client side down the line.
    */
   public static final int START_END_SYMBOL_LEFT_OFFSET = 12;

   @Resource
   private ApplicationContext context;

   @Resource
   private UserIdProvider me;

   @Resource
   @Qualifier("default")
   private ServiceFactoryLocator serviceFactoryLocator;

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   private UserService userService;

   private QueryService queryService;

   private String currentUserId;

   // Modeling Session Management

   @Resource
   private ModelingSessionManager sessionManager;

   public ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         serviceFactory = serviceFactoryLocator.get();
         SpiExtensionRegistry.instance().setExtensionRegistry(StardustExtensionRegistry.instance());
      }

      return serviceFactory;
   }

   public ModelingSession currentSession()
   {
      TypeDeclarationUtils.defaultURIConverter.set(getClasspathUriConverter());
      boolean wasNull = currentUserId == null;
      currentUserId = me.getCurrentUserId();
      if (wasNull) // (fh) workaround for ejb case where the destroyModelingSession does not get invoked.
      {
         destroyModelingSession();
      }
      ModelingSession currentSession = sessionManager.getOrCreateSession(me);
      if (me.isAdministrator())
      {
         currentSession.setSessionAttribute(ModelingSession.SUPERUSER, true);
      }

      return currentSession;
   }

   /**
    * Removes the modeling session from cached list when user session ends. TODO -
    * commented pending review by Robert S
    *
    */
   @PreDestroy
   public void destroyModelingSession()
   {
      if (null != currentUserId)
      {
         sessionManager.destroySession(currentUserId);
      }
   }

   /**
    *
    * @return
    */
   public ModelManagementStrategy getModelManagementStrategy()
   {
      getServiceFactory();
      return currentSession().modelManagementStrategy();
   }

   /**
    * Only used for ORION integration
    *
    * @param modelManagementStrategy
    */
   public void setModelManagementStrategy(ModelManagementStrategy modelManagementStrategy)
   {
      currentSession().setModelManagementStrategy(modelManagementStrategy);
   }

   public EObjectUUIDMapper uuidMapper()
   {
      return currentSession().uuidMapper();
   }

   /**
    *
    * @return
    */
   public List<User> getNotInvitedUsers()
   {
      UserQuery userQuery = UserQuery.findActive();

      return getQueryService().getAllUsers(userQuery);
   }

   public String getLoggedInUser()
   {
      JsonObject currentUserJson = new JsonObject();
      currentUserJson.addProperty(TYPE_PROPERTY, "WHO_AM_I");
      currentUserJson.addProperty("firstName", me.getFirstName());
      currentUserJson.addProperty("lastName", me.getLastName());
      currentUserJson.addProperty("account", me.getLoginName());
      return currentUserJson.toString();
   }

   public String getSessionOwner(String sessionId)
   {
      ModelingSession currentSession = sessionManager.findById(sessionId);
      User currentUser = getUserService().getUser(
            unwrapUsername(currentSession.getOwnerId()));
      JsonObject currentUserJson = new JsonObject();
      currentUserJson.addProperty(TYPE_PROPERTY, "UPDATE_OWNER");
      currentUserJson.addProperty("firstName", currentUser.getFirstName());
      currentUserJson.addProperty("lastName", currentUser.getLastName());
      currentUserJson.addProperty("account", currentUser.getAccount());
      currentUserJson.addProperty("email", currentUser.getEMail());
      return currentUserJson.toString();
   }

   private String unwrapUsername(String owner)
   {
      String[] parts = owner.split(":");
      return parts[parts.length - 1];
   }

   /**
    * Retrieves all the stored models and returns a json array of references of these
    * getModelManagementStrategy().getModels().
    *
    * @return
    */
   public String getAllModels(boolean reload)
   {
      try
      {
         if (reload || getModelManagementStrategy().getModels().isEmpty())
         {
            // reload upon request or if never loaded before
            // TODO Review
            getModelManagementStrategy().getModels(true);
         }

         JsonObject modelsJson = new JsonObject();
         JsonObject loaded = new JsonObject();
         JsonArray failed = new JsonArray();
         modelsJson.add("loaded", loaded);
         modelsJson.add("failed", failed);

         ModelRepository modelRepository = currentSession().modelRepository();
         for (EObject model : modelRepository.getAllModels())
         {
            try
            {
               JsonObject modelJson = modelRepository.getModelBinding(model)
                     .getMarshaller()
                     .toModelJson(model);
               loaded.add(extractString(modelJson, ModelerConstants.ID_PROPERTY),
                     modelJson);
            }
            catch (Exception e)
            {
               JsonObject failedModel = new JsonObject();
               failedModel.addProperty("id", modelRepository.getModelBinding(model).getModelId(model));
               failedModel.addProperty("uuid",  currentSession().uuidMapper().getUUID(model));
               failedModel.addProperty("error", e.getMessage());
               failed.add(failedModel);
               e.printStackTrace();
            }
         }

         return modelsJson.toString();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   /**
    *
    * @param httpRequest
    * @param modelId
    * @return
    */
   public void saveModel(String modelId)
   {
      ModelType model = findModel(modelId);

      if (!currentSession().canSaveModel(modelId))
      {
         throw new MissingWritePermissionException(
               "Failed to (re-)validate edit lock on model " + modelId);
      }

      getModelManagementStrategy().saveModel(model);
   }

   /**
	 *
	 */
   public void saveAllModels()
   {
      ModelRepository modelRepository = currentSession().modelRepository();
      List<ModelType> modelsToBeSaved = newArrayList();
      for (ModelType xpdlModel : getModelManagementStrategy().getModels().values())
      {
         // do only save if the model was actually changed (which implies an edit lock)
         EObject nativeModel = modelRepository.findModel(xpdlModel.getId());
         if (currentSession().getSession().isTrackingModel(nativeModel))
         {
            if (!currentSession().canSaveModel(xpdlModel.getId()))
            {
               throw new MissingWritePermissionException(
                     "Failed to (re-)validate edit lock on model " + xpdlModel.getId());
            }

            modelsToBeSaved.add(xpdlModel);
         }
      }

      for (ModelType xpdlModel : modelsToBeSaved)
      {
         try
         {
            if (!getModelBuilderFacade().isReadOnly(xpdlModel))
            {
               getModelManagementStrategy().saveModel(xpdlModel);
            }
         }
         catch (Exception e)
         {
            trace.warn("Failed saving model " + getModelFileName(xpdlModel.getId()), e);
         }
      }
      currentSession().reset();
   }

   /**
    *
    * @param id
    * @return
    */
   public String getModelFileName(String id)
   {
      return getModelManagementStrategy().getModelFileName(findModel(id));
   }

   /**
    * TODO - This should probably be delegated to the model management strategy?
    *
    * @param id
    * @return
    */
   public byte[] getModelFile(String id)
   {
      String jcrFilePath = getModelManagementStrategy().getModelFilePath(findModel(id));
      return getDocumentManagementService().retrieveDocumentContent(jcrFilePath);
   }

   /**
    * @param element
    * @param description
    * @throws JSONException
    */
   public static void setDescription(IIdentifiableModelElement element, JsonObject json)
   {
      String description = null;

      if (json.has(DESCRIPTION_PROPERTY))
      {
         description = extractString(json, DESCRIPTION_PROPERTY);
      }

      if (StringUtils.isNotEmpty(description))
      {
         DescriptionType dt = AbstractElementBuilder.F_CWM.createDescriptionType();
         dt.getMixed().add(FeatureMapUtil.createRawTextEntry(description));
         element.setDescription(dt);
      }
   }

   /**
    * @return
    */
   public String loadProcessDiagram(String modelId, String processId)
   {
      ModelRepository modelRepository = currentSession().modelRepository();
      EObject model = modelRepository.findModel(modelId);
      if (null != model)
      {
         return modelRepository.getModelBinding(model)
               .getMarshaller()
               .toProcessDiagramJson(model, processId)
               .toString();
      }

      // TODO Try to ModelBuilderFascade.find in loaded models first. Correct?
      ModelType xpdlModel = findModel(modelId);

      // TODO Very ugly - only for newly created models

      if (model == null)
      {
         xpdlModel = getModelManagementStrategy().attachModel(modelId);
      }

      return currentSession().xpdlMarshaller().toProcessDiagramJson(xpdlModel, processId)
            .toString();
   }

   // ======================== TODO Put in separate resource as we are not
   // going to share this with Eclipse =====================

   /**
	 *
	 */
   public void createWrapperProcess(String modelId, JsonObject json)
   {
      ModelType model = findModel(modelId);

      // Create process definition

      ProcessDefinitionType processDefinition = getModelBuilderFacade().createProcess(
            model, null, extractString(json, "processDefinitionName"), "Default",
            "Default");
      currentSession().uuidMapper().map(processDefinition);

      // TODO Correct flags

      if (extractBoolean(json, "createWebService"))
      {
         if (extractBoolean(json, "createRestService"))
         {
            ModelBuilderFacade.setAttribute(processDefinition,
                  "carnot:engine:externalInvocationType",
                  PredefinedConstants.PROCESSINTERFACE_INVOCATION_BOTH);
         }
         else
         {
            ModelBuilderFacade.setAttribute(processDefinition,
                  "carnot:engine:externalInvocationType",
                  PredefinedConstants.PROCESSINTERFACE_INVOCATION_SOAP);
         }
      }
      else
      {
         if (extractBoolean(json, "createRestService"))
         {
            ModelBuilderFacade.setAttribute(processDefinition,
                  "carnot:engine:externalInvocationType",
                  PredefinedConstants.PROCESSINTERFACE_INVOCATION_REST);
         }
      }

      if (extractBoolean(json, "transientProcess"))
      {
         ModelBuilderFacade.setAttribute(processDefinition,
               "carnot:engine:auditTrailPersistence", "TRANSIENT"); // Values are
                                                                    // TRANSIENT|DEFERRED|IMMEDIATE
      }

      LaneSymbol parentLaneSymbol = getModelBuilderFacade().findLaneInProcess(
            processDefinition, ModelerConstants.DEF_LANE_ID);

      int yOffset = 50;
      int xDataOffset = 10;
      int xActivityOffset = 150;
      int xEventOffset = 200;

      // Create Start Event

      StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();

      startEventSymbol.setXPos(xEventOffset);
      startEventSymbol.setYPos(yOffset);

      yOffset += 100;

      processDefinition.getDiagram().get(0).getStartEventSymbols().add(startEventSymbol);
      parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

      // Request data

      String dataId = NameIdUtilsExtension.createIdFromName(extractString(json, "requestDataName"));

      // TODO Weird programming because Model Builder Facade throws
      // ObjectNotFoundException

      DataType data = null;

      try
      {
         data = getModelBuilderFacade().findData(model, dataId);
      }
      catch (Exception x)
      {
      }
      finally
      {
         if (data == null)
         {
            data = getModelBuilderFacade().createStructuredData(model, dataId,
                  extractString(json, "requestDataName"),
                  extractString(json, "requestDataTypeFullId"));
         }
      }

      getModelBuilderFacade().createStructuredParameter(processDefinition, data,
            NameIdUtilsExtension.createIdFromName(extractString(json, "requestDataName")),
            extractString(json, "requestDataName"),
            extractString(json, "requestDataTypeFullId"), ModeType.IN);

      DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();
      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(xDataOffset);
      dataSymbol.setYPos(yOffset);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      yOffset += 100;

      ActivityType activity;
      ActivitySymbolType activitySymbol;
      ActivitySymbolType previousActivitySymbol = null;

      if (extractString(json, "preprocessingApplicationFullId") != null)
      {
         activity = newApplicationActivity(processDefinition).withIdAndName(null,
               "Preprocessing App").build();

         activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);

         getModelBuilderFacade().setApplication(activity,
               extractString(json, "preprocessingApplicationFullId"));

         activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

         activitySymbol.setXPos(xActivityOffset);
         activitySymbol.setYPos(yOffset);
         activitySymbol.setWidth(180);
         activitySymbol.setHeight(50);
         activitySymbol.setActivity(activity);
         activity.getActivitySymbols().add(activitySymbol);

         processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
         parentLaneSymbol.getActivitySymbol().add(activitySymbol);

         AccessPointType inAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.IN_LITERAL);
         AccessPointType outAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.OUT_LITERAL);

         getModelBuilderFacade().createDataFlowConnection(processDefinition,
               activitySymbol, dataSymbol, PredefinedConstants.APPLICATION_CONTEXT,
               inAccessPoint != null ? inAccessPoint.getId() : null, PredefinedConstants.APPLICATION_CONTEXT,
               outAccessPoint != null ? outAccessPoint.getId() : null, "left", "right");

         previousActivitySymbol = activitySymbol;
         yOffset += 100;
      }

      // Create Application Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(null,
            extractString(json, "serviceInvocationActivityName")).build();

      activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);
      getModelBuilderFacade().setApplication(activity,
            extractString(json, "applicationFullId"));

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(yOffset);
      activitySymbol.setWidth(180);
      activitySymbol.setHeight(50);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Create connection from Start Event

      // TODO Host activity for start event?

      if (previousActivitySymbol != null)
      {
         getModelBuilderFacade().createTransitionSymbol(processDefinition,
               startEventSymbol, previousActivitySymbol, null, "bottom", "top");

         getModelBuilderFacade().createControlFlowConnection(processDefinition,
               previousActivitySymbol, activitySymbol, "Transition1", "", "", false,
               "true", "bottom", "top");
      }
      else
      {
         getModelBuilderFacade().createTransitionSymbol(processDefinition,
               startEventSymbol, activitySymbol, null, "bottom", "top");
      }

      AccessPointType inAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
            activity.getApplication(), DirectionType.IN_LITERAL);

      getModelBuilderFacade().createDataFlowConnection(processDefinition, activitySymbol,
            dataSymbol, PredefinedConstants.APPLICATION_CONTEXT,
            inAccessPoint != null ? inAccessPoint.getId() : null, null, null, "left",
            "right");

      previousActivitySymbol = activitySymbol;
      yOffset += 100;

      // Create Response Data

      dataId = NameIdUtilsExtension.createIdFromName(extractString(json, "responseDataName"));

      // TODO Weird programming because Model Builder Facade throws
      // ObjectNotFoundException

      data = null;

      try
      {
         data = getModelBuilderFacade().findData(model, dataId);
      }
      catch (Exception x)
      {
      }
      finally
      {
         if (data == null)
         {
            data = getModelBuilderFacade().createStructuredData(model, dataId,
                  extractString(json, "responseDataName"),
                  extractString(json, "responseDataTypeFullId"));
         }
      }

      getModelBuilderFacade().createStructuredParameter(processDefinition, data,
            NameIdUtilsExtension.createIdFromName(extractString(json, "responseDataName")),
            extractString(json, "responseDataName"),
            extractString(json, "responseDataTypeFullId"), ModeType.OUT);

      dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(xDataOffset);
      dataSymbol.setYPos(yOffset);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      AccessPointType outAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
            activity.getApplication(), DirectionType.OUT_LITERAL);

      getModelBuilderFacade().createDataFlowConnection(processDefinition, activitySymbol,
            dataSymbol, null, null, PredefinedConstants.APPLICATION_CONTEXT,
            outAccessPoint != null ? outAccessPoint.getId() : null, "left", "right");

      yOffset += 100;

      if (extractString(json, "postprocessingApplicationFullId") != null)
      {
         activity = newApplicationActivity(processDefinition).withIdAndName(null,
               "Postprocessing App").build();

         activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);
         getModelBuilderFacade().setApplication(activity,
               extractString(json, "postprocessingApplicationFullId"));

         activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

         activitySymbol.setXPos(xActivityOffset);
         activitySymbol.setYPos(yOffset);
         activitySymbol.setWidth(180);
         activitySymbol.setHeight(50);
         activitySymbol.setActivity(activity);
         activity.getActivitySymbols().add(activitySymbol);

         processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
         parentLaneSymbol.getActivitySymbol().add(activitySymbol);

         getModelBuilderFacade().createControlFlowConnection(processDefinition,
               previousActivitySymbol, activitySymbol, "Transition2", "", "", false,
               "true", "bottom", "top");

         inAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.IN_LITERAL);
         outAccessPoint = getModelBuilderFacade().findFirstApplicationAccessPointForType(
               activity.getApplication(), DirectionType.OUT_LITERAL);

         getModelBuilderFacade().createDataFlowConnection(processDefinition,
               activitySymbol, dataSymbol, PredefinedConstants.APPLICATION_CONTEXT,
               inAccessPoint != null ? inAccessPoint.getId() : null, PredefinedConstants.APPLICATION_CONTEXT,
               outAccessPoint != null ? outAccessPoint.getId() : null, "left", "right");

         yOffset += 100;
      }

      // Create End Symbol

      EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();

      endEventSymbol.setXPos(xEventOffset);
      endEventSymbol.setYPos(yOffset);

      processDefinition.getDiagram().get(0).getEndEventSymbols().add(endEventSymbol);

      parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);

      // Create connection to End Event

      // TODO Host activity for end event?

      getModelBuilderFacade().createTransitionSymbol(processDefinition, activitySymbol,
            endEventSymbol, null, "bottom", "top");

      if (extractBoolean(json, "generateTestWrapper"))
      {
         JsonObject wrapperJson = new JsonObject();

         wrapperJson.addProperty("processDefinitionName", processDefinition.getName()
               + " Test");
         wrapperJson.addProperty("processFullId",
               getModelBuilderFacade().createFullId(model, processDefinition));
         wrapperJson.addProperty(
               "participantFullId",
               getModelBuilderFacade().createFullId(model,
                     getModelBuilderFacade().findParticipant(model, "Administrator")));
         wrapperJson.addProperty("dataInputActivityName", "Enter Data");
         wrapperJson.addProperty("subprocessActivityName", processDefinition.getName());
         wrapperJson.addProperty("dataOutputActivityName", "Retrieve Data");

         createProcessInterfaceTestWrapperProcess(modelId, wrapperJson);
      }
   }

   /**
   *
   */
   public void createProcessInterfaceTestWrapperProcess(String modelId, JsonObject json)
   {
      ModelType model = findModel(modelId);

      ProcessDefinitionType processDefinition = getModelBuilderFacade().createProcess(
            model, null, extractString(json, "processDefinitionName"), "Default",
            "Default");
      currentSession().uuidMapper().map(processDefinition);

      ModelBuilderFacade.setBooleanAttribute(processDefinition, PredefinedConstants.PROCESS_IS_AUXILIARY_ATT, true);

      ProcessDefinitionType processInterface = getModelBuilderFacade().findProcessDefinition(
            extractString(json, "processFullId"));

      LaneSymbol parentLaneSymbol = getModelBuilderFacade().findLaneInProcess(
            processDefinition, ModelerConstants.DEF_LANE_ID);

      parentLaneSymbol.setParticipant(getModelBuilderFacade().findParticipant(
            extractString(json, "participantFullId")));

      int activityWidth = 180;
      int activityHeight = 50;
      int eventWidth = 20;
      int xActivityOffset = 100;
      int xDataOffset = 10;

      // Create Start Event

      StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();

      startEventSymbol.setXPos(xActivityOffset + activityWidth / 2 - eventWidth / 2);
      startEventSymbol.setYPos(50);

      processDefinition.getDiagram().get(0).getStartEventSymbols().add(startEventSymbol);
      parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

      TriggerType manualTrigger = newManualTrigger(processDefinition).accessibleTo(
            getModelBuilderFacade().findParticipant(
                  extractString(json, "participantFullId"))).build();
      manualTrigger.setId("StartTest");
      manualTrigger.setName("Start Test");
      startEventSymbol.setTrigger(manualTrigger);

      // Create Enter Data Activity

      ActivityType activity = newApplicationActivity(processDefinition).withIdAndName(
            null, extractString(json, "dataInputActivityName")).build();

      activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);

      ActivitySymbolType activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(100);
      activitySymbol.setWidth(activityWidth);
      activitySymbol.setHeight(activityHeight);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Create connection from Start Event

      // TODO Host activity for start event?

      getModelBuilderFacade().createTransitionSymbol(processDefinition, startEventSymbol,
            activitySymbol, null, "bottom", "top");

      ActivitySymbolType previousActivitySymbol = activitySymbol;

      // Create Subprocess Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(null,
            extractString(json, "subprocessActivityName")).build();

      activity.setImplementation(ActivityImplementationType.SUBPROCESS_LITERAL);
      getModelBuilderFacade().setSubProcess(activity,
            extractString(json, "processFullId"));

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(300);
      activitySymbol.setWidth(activityWidth);
      activitySymbol.setHeight(activityHeight);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      getModelBuilderFacade().createControlFlowConnection(processDefinition,
            previousActivitySymbol, activitySymbol, "Transition1", "Transition1", "", true, "",
            "bottom", "top");

      // Create Input Data

      int yDataOffset = 200;

      if (processInterface.getFormalParameters() != null)
      {
         for (FormalParameterType formalParameter : processInterface.getFormalParameters()
               .getFormalParameter())
         {
            if (formalParameter.getMode() == ModeType.IN
                  || formalParameter.getMode() == ModeType.INOUT)
            {
               String typeDeclarationId = formalParameter.getDataType()
                     .getDeclaredType()
                     .getId();
               TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                     .getTypeDeclaration(typeDeclarationId);
               String structuredDataTypeFullId = getModelBuilderFacade().createFullId(
                     model, typeDeclaration);

               // TODO Weird programming because Model Builder Facade throws
               // ObjectNotFoundException

               DataType data = null;

               try
               {
                  data = getModelBuilderFacade().findData(model, formalParameter.getId());
               }
               catch (Exception x)
               {
               }
               finally
               {
                  if (data == null)
                  {
                     data = getModelBuilderFacade().createStructuredData(model,
                           formalParameter.getId(), formalParameter.getName(),
                           structuredDataTypeFullId);
                  }
               }

               DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();
               dataSymbol.setData(data);
               processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
               data.getDataSymbols().add(dataSymbol);
               dataSymbol.setXPos(xDataOffset);
               dataSymbol.setYPos(yDataOffset);

               parentLaneSymbol.getDataSymbol().add(dataSymbol);

               yDataOffset += 50;

               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     previousActivitySymbol, dataSymbol, DirectionType.OUT_LITERAL,
                     "left", "right", PredefinedConstants.DEFAULT_CONTEXT, null);
               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     activitySymbol, dataSymbol, DirectionType.IN_LITERAL, "right",
                     "left", PredefinedConstants.PROCESSINTERFACE_CONTEXT, formalParameter.getId());
            }
         }
      }

      previousActivitySymbol = activitySymbol;

      // Create Retrieve Data Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(null,
            extractString(json, "dataOutputActivityName")).build();

      activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setXPos(xActivityOffset);
      activitySymbol.setYPos(500);
      activitySymbol.setWidth(activityWidth);
      activitySymbol.setHeight(activityHeight);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      getModelBuilderFacade().createControlFlowConnection(processDefinition,
            previousActivitySymbol, activitySymbol, "Transition2", "Transition2", "", true, "",
            "bottom", "top");

      // Create Output Data

      yDataOffset = 400;

      if (processInterface.getFormalParameters() != null)
      {
         for (FormalParameterType formalParameter : processInterface.getFormalParameters()
               .getFormalParameter())
         {
            if (formalParameter.getMode() == ModeType.OUT
                  || formalParameter.getMode() == ModeType.INOUT)
            {
               String typeDeclarationId = formalParameter.getDataType()
                     .getDeclaredType()
                     .getId();
               TypeDeclarationType typeDeclaration = model.getTypeDeclarations()
                     .getTypeDeclaration(typeDeclarationId);
               String structuredDataTypeFullId = getModelBuilderFacade().createFullId(
                     model, typeDeclaration);

               // TODO Weird programming because Model Builder Facade throws
               // ObjectNotFoundException

               DataType data = null;

               try
               {
                  data = getModelBuilderFacade().findData(model, formalParameter.getId());
               }
               catch (Exception x)
               {
               }
               finally
               {
                  if (data == null)
                  {
                     data = getModelBuilderFacade().createStructuredData(model,
                           formalParameter.getId(), formalParameter.getName(),
                           structuredDataTypeFullId);
                  }
               }

               DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

               dataSymbol.setData(data);
               processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
               data.getDataSymbols().add(dataSymbol);
               dataSymbol.setXPos(xDataOffset);
               dataSymbol.setYPos(yDataOffset);

               parentLaneSymbol.getDataSymbol().add(dataSymbol);

               yDataOffset += 50;

               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     previousActivitySymbol, dataSymbol, DirectionType.OUT_LITERAL,
                     "left", "right", PredefinedConstants.PROCESSINTERFACE_CONTEXT, formalParameter.getId());
               getModelBuilderFacade().createDataFlowConnection(processDefinition,
                     activitySymbol, dataSymbol, DirectionType.IN_LITERAL, "right",
                     "left", PredefinedConstants.DEFAULT_CONTEXT, null);
            }
         }
      }

      // Create End Symbol

      EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();

      endEventSymbol.setXPos(xActivityOffset + activityWidth / 2 - eventWidth / 2);
      endEventSymbol.setYPos(600);

      processDefinition.getDiagram().get(0).getEndEventSymbols().add(endEventSymbol);

      parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);

      // Create connection to End Event

      getModelBuilderFacade().createTransitionSymbol(processDefinition, activitySymbol,
            endEventSymbol, null, "bottom", "top");
   }

   /**
    *
    * @return
    */
   DocumentManagementService getDocumentManagementService()
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
   private QueryService getQueryService()
   {
      if (queryService == null)
      {
         queryService = getServiceFactory().getQueryService();
      }

      return queryService;
   }

   public ModelType findModel(String modelId)
   {
      return getModelManagementStrategy().getModels().get(modelId);
   }

   public <M extends EObject> ModelBinding<M> findModelBinding(M model)
   {
      return currentSession().modelRepository().getModelBinding(model);
   }

   /**
    *
    * @param modelId
    * @return
    */
   public JsonArray validateModel(String modelId)
   {
      trace.debug("Validating model " + modelId);

      ModelType model = findModel(modelId);
      VariableContextHelper instance = VariableContextHelper.getInstance();
      instance.clear();
      instance.storeVariables(model, false);

      ValidatorRegistry.setFilters(new HashMap<String, String>());
      ValidatorRegistry.setValidationExtensionRegistry(ValidationExtensionRegistry.getInstance());
      ValidationService validationService = ValidationService.getInstance();

      JsonArray issuesJson = new JsonArray();

      Issue[] issues = validationService.validateModel(model);

      for (int i = 0; i < issues.length; i++ )
      {
         Issue issue = issues[i];
         JsonObject issueJson = new JsonObject();

         System.out.println("Found issue " + issue);

         issueJson.addProperty("message", issue.getMessage());
         issueJson.addProperty("severity", issue.getSeverity());

         EObject modelElement = issue.getModelElement();

         String modelElementId = null;

         if (modelElement != null && modelElement instanceof IIdentifiableModelElement)
         {
            modelElementId = modelId + "/"
                  + ((IIdentifiableModelElement) modelElement).getId() + "/"
                  + ((IIdentifiableModelElement) modelElement).getElementOid();
         }
         else if (modelElement != null && modelElement instanceof ModelType)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((ModelType) modelElement).getOid();
         }
         else if (modelElement != null && modelElement instanceof TypeDeclarationType)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((TypeDeclarationType) modelElement).getId();
         }
         else if (modelElement != null && modelElement instanceof ExternalPackage)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((ExternalPackage) modelElement).getId();
         }
         else if (modelElement != null && modelElement instanceof DataMappingType)
         {
            modelElementId = modelId + "/" + modelId + "/"
                  + ((DataMappingType) modelElement).getId();
         }

         issueJson.addProperty("modelElement", modelElementId);
         issuesJson.add(issueJson);
      }

      return issuesJson;
   }

   /**
    * Returns a JSON representation of the service structure underneath the
    * <code>wsdlUrl</code> provided with the input JSON.
    * <p>
    * <b>Members:</b>
    * <ul>
    * <li><code>WSConstants.WS_WSDL_URL_ATT</code> a string containing the URL from which
    * the WSDL document was loaded.</li>
    * <li><code>"services"</code> a JsonArray of JsonObjects each containing specification
    * of one service, including the dynamically bound meta service, having the structure:
    * <ul>
    * <li><code>"name"</code> a string containing the local name of the service (for
    * display purposes).</li>
    * <li><code>WSConstants.WS_SERVICE_NAME_ATT</code> a string containing the qualified
    * name of the service.</li>
    * <li><code>"ports"</code> a JsonArray of JsonObjects each containing specification of
    * one port, with the structure:
    * <ul>
    * <li><code>"name"</code> a string containing the local name of the port (for display
    * purposes).</li>
    * <li><code>WSConstants.WS_PORT_NAME_ATT</code> a string containing the qualified name
    * of the port.</li>
    * <li><code>"style"</code> a string containing the binding style, i.e. "document" (for
    * display purposes). This may be displayed if the operation does not provide a style</li>
    * <li><code>"operations"</code> a JsonArray of JsonObjects each containing
    * specification of one operation, with the structure:
    * <ul>
    * <li><code>"name"</code> a string containing the operation name (for display
    * purposes).</li>
    * <li><code>WSConstants.WS_OPERATION_NAME_ATT</code> a string containing the qualified
    * operation name.</li>
    * <li><code>"style"</code> a string containing the operation style, i.e. "document"
    * (for display purposes).</li>
    * <li><code>"use"</code> a string containing the operation use, i.e. "literal" (for
    * display purposes).</li>
    * <li><code>WSConstants.WS_OPERATION_INPUT_NAME_ATT</code> a string containing the
    * input name.</li>
    * <li><code>WSConstants.WS_OPERATION_OUTPUT_NAME_ATT</code> a string containing the
    * output name.</li>
    * <li><code>WSConstants.WS_SOAP_ACTION_URI_ATT</code> a string containing the SOAP
    * action URI.</li>
    * <li><code>WSConstants.WS_SOAP_PROTOCOL_ATT</code> a string containing the SOAP
    * protocol.</li>
    * <li><code>WSConstants.WS_INPUT_ORDER_ATT</code> a string containing the list of
    * parts composing the input message.</li>
    * <li><code>WSConstants.WS_OUTPUT_ORDER_ATT</code> a string containing the list of
    * parts composing the output message.</li>
    * </ul>
    * </li>
    * </ul>
    * </li>
    * </ul>
    * </li>
    * </ul>
    *
    * @param postedData
    *           a JsonObject that contains a primitive (String) member with the name
    *           "wsdlUrl" that specifies the URL from where the WSDL should be loaded.
    * @return the JsonObject containing the representation of the services.
    */
   public JsonObject getWebServiceStructure(JsonObject postedData)
   {
      String wsdlUrl = postedData.get("wsdlUrl").getAsString();
      System.out.println("===> Get Web Service Structure for URL " + wsdlUrl);

      Definition definition = JaxWSResource.getDefinition(wsdlUrl);

      @SuppressWarnings("unchecked")
      Collection<Service> services = definition.getServices().values();
      @SuppressWarnings("unchecked")
      Collection<Binding> bindings = definition.getBindings().values();

      JsonObject webServiceJson = new JsonObject();
      webServiceJson.addProperty(WSConstants.WS_WSDL_URL_ATT, wsdlUrl);
      addServices(webServiceJson, services, bindings);
      return webServiceJson;
   }

   /**
    * Adds the service definitions to the parent json object.
    *
    * @param webServiceJson
    *           the parent json object.
    * @param services
    *           the list of services declared in the wsdl document.
    * @param bindings
    *           the list of bindings declared in the wsdl document.
    */
   private void addServices(JsonObject webServiceJson, Collection<Service> services,
         Collection<Binding> bindings)
   {
      JsonObject servicesJson = new JsonObject();
      webServiceJson.add("services", servicesJson);

      for (Service service : services)
      {
         QName qname = service.getQName();

         @SuppressWarnings("unchecked")
         Collection<Port> ports = service.getPorts().values();

         JsonObject serviceJson = new JsonObject();

         serviceJson.addProperty("name", qname.getLocalPart());
         serviceJson.addProperty(WSConstants.WS_SERVICE_NAME_ATT, qname.toString());
         addPorts(serviceJson, ports);
         servicesJson.add(qname.getLocalPart(), serviceJson);
      }

      JsonObject serviceJson = new JsonObject();
      serviceJson.addProperty("name", DYNAMIC_BOUND_SERVICE_QNAME.getLocalPart());
      serviceJson.addProperty(WSConstants.WS_SERVICE_NAME_ATT,
            DYNAMIC_BOUND_SERVICE_QNAME.toString());
      addPorts(serviceJson, bindings);
      servicesJson.add(DYNAMIC_BOUND_SERVICE_QNAME.getLocalPart(), serviceJson);
   }

   /**
    * Adds port or binding definitions to the service json.
    *
    * @param serviceJson
    *           the json object representing the parent service.
    * @param ports
    *           the list of ports or bindings declared for the service.
    */
   private void addPorts(JsonObject serviceJson, Collection<? > ports)
   {
      JsonObject portsJson = new JsonObject();

      serviceJson.add("ports", portsJson);

      for (Object port : ports)
      {
         String name = port instanceof Port
               ? ((Port) port).getName()
               : ((Binding) port).getQName().getLocalPart();
         Binding binding = port instanceof Port
               ? ((Port) port).getBinding()
               : (Binding) port;

         @SuppressWarnings("unchecked")
         Collection<BindingOperation> operations = binding.getBindingOperations();

         JsonObject portJson = new JsonObject();
         portJson.addProperty("name", name);
         portJson.addProperty(WSConstants.WS_PORT_NAME_ATT, name);
         portJson.addProperty("style", JaxWSResource.getBindingStyle(binding));
         addOperations(portJson, operations);
         portsJson.add(name, portJson);
      }
   }

   /**
    * Duplicated from WSConstants in 7.1
    */
   public static final String WS_OPERATION_INPUT_NAME_ATT = "carnot:engine:wsOperationInputName";

   public static final String WS_OPERATION_OUTPUT_NAME_ATT = "carnot:engine:wsOperationOutputName";

   public static final String WS_SOAP_ACTION_URI_ATT = "carnot:engine:wsSoapActionUri";

   public static final String WS_SOAP_PROTOCOL_ATT = "carnot:engine:wsSoapProtocol";

   public static final String WS_INPUT_ORDER_ATT = "carnot:engine:wsInputOrder";

   public static final String WS_OUTPUT_ORDER_ATT = "carnot:engine:wsOutputOrder";

   public static final QName DYNAMIC_BOUND_SERVICE_QNAME = new QName(
         "http://www.carnot.ag/ws", "Dynamically bound Service");

   /**
    * Adds operation definitions to the port json.
    *
    * @param portJson
    *           the json object representing the parent port.
    * @param operations
    *           the list of operations declared for the port.
    */
   private void addOperations(JsonObject portJson, Collection<BindingOperation> operations)
   {
      JsonObject operationsJson = new JsonObject();

      portJson.add("operations", operationsJson);

      for (BindingOperation operation : operations)
      {
         String name = getOperationName(operation);
         BindingInput bindingInput = operation.getBindingInput();
         String inputName = bindingInput == null ? null : bindingInput.getName();
         BindingOutput bindingOutput = operation.getBindingOutput();
         String outputName = bindingOutput == null ? null : bindingOutput.getName();
         Input input = operation.getOperation().getInput();
         Output output = operation.getOperation().getOutput();

         JsonObject operationJson = new JsonObject();

         operationJson.addProperty("name", name);
         operationJson.addProperty(WSConstants.WS_OPERATION_NAME_ATT, operation.getName());
         String style = JaxWSResource.getOperationStyle(operation);
         if (style == null)
         {
            if ( !(portJson.get("style") instanceof JsonNull))
            {
               style = portJson.get("style").getAsString();
            }
         }
         operationJson.addProperty("style", style);
         operationJson.addProperty("use", JaxWSResource.getOperationUse(operation));
         operationJson.addProperty(WS_OPERATION_INPUT_NAME_ATT, inputName);
         operationJson.addProperty(WS_OPERATION_OUTPUT_NAME_ATT, outputName);
         operationJson.addProperty(WS_SOAP_ACTION_URI_ATT,
               JaxWSResource.getSoapActionUri(operation));
         operationJson.addProperty(WS_SOAP_PROTOCOL_ATT,
               JaxWSResource.getOperationProtocol(operation));
         operationJson.addProperty(WS_INPUT_ORDER_ATT, getPartsOrder(input == null
               ? null
               : input.getMessage()));
         operationJson.addProperty(WS_OUTPUT_ORDER_ATT, getPartsOrder(output == null
               ? null
               : output.getMessage()));

         operationsJson.add(name, operationJson);
         if (portJson.get("style") instanceof JsonNull)
         {
            portJson.addProperty("style", style);
         }
      }
   }

   /**
    * Computes a string containing a comma separated list of the parts composing the
    * message.
    *
    * @param message
    *           the Message
    * @return the computed list of parts
    */
   public static String getPartsOrder(Message message)
   {
      if (message == null)
      {
         return "";
      }

      @SuppressWarnings("unchecked")
      List<Part> parts = message.getOrderedParts(null);

      if (parts.isEmpty())
      {
         return "";
      }

      StringBuffer buffer = new StringBuffer();

      for (Part part : parts)
      {
         if (buffer.length() > 0)
         {
            buffer.append(',');
         }
         buffer.append(part.getName());
      }

      return buffer.toString();
   }

   /**
    * Computes a unique label for the operation by appending the input and output names to
    * the operation name.
    *
    * @param operation
    *           the BindingOperation
    * @return the computed label
    */
   private String getOperationName(BindingOperation operation)
   {
      String name = operation.getName();
      BindingInput bindingInput = operation.getBindingInput();
      String inputName = bindingInput == null ? null : bindingInput.getName();
      BindingOutput bindingOutput = operation.getBindingOutput();
      String outputName = bindingOutput == null ? null : bindingOutput.getName();

      if (name != null)
      {
         if (inputName == null)
         {
            if (outputName == null)
            {
               return name;
            }
            else
            {
               return name + "(:none," + outputName + ")";
            }
         }
         else
         {
            if (outputName == null)
            {
               return name + "(" + inputName + ",:none)";
            }
            else
            {
               return name + "(" + inputName + "," + outputName + ")";
            }
         }
      }

      return "";
   }

   /**
    * Loads a JSON representation of a type hierarchy loaded from an XSD or WSDL URL.
    * <p>
    * <b>Members:</b>
    * <ul>
    * <li><code>targetNamespace</code> the schema namespace.</li>
    * <li><code>elements</code> a list of elements declared in the schema.</li>
    * <li><code>types</code> a list of types declared in the schema.</li>
    * </ul>
    * <p>
    * Each <b>element</b> declaration has the following structure:
    * <ul>
    * <li><code>name</code> a string containing the name of the item (for display
    * purposes).</li>
    * <li><code>type</code> the xsd type of the element (optional).</li>
    * <li><code>attributes</code> a list of attributes (optional).</li>
    * <li><code>body</code> the body of the element (optional).</li>
    * </ul>
    * <p>
    * Each <b>type</b> declaration has the following structure:
    * <ul>
    * <li><code>name</code> a string containing the name of the item (for display
    * purposes).</li>
    * <li><code>attributes</code> a list of attributes (optional).</li>
    * <li><code>facets</code> the constraining facets if the type is a simple type
    * (optional).</li>
    * <li><code>body</code> the body of the type (optional).</li>
    * </ul>
    * <p>
    * Each <b>attribute</b> declaration has the following structure:
    * <ul>
    * <li><code>name</code> a string containing the name of the item (for display
    * purposes).</li>
    * <li><code>type</code> the xsd type of the attribute.</li>
    * <li><code>cardinality</code> the cardinality of the attribute (<code>required</code>
    * | <code>optional</code>).</li>
    * </ul>
    * <p>
    * Each <b>body</b> declaration has the following structure:
    * <ul>
    * <li><code>name</code> a string containing the name of the item (for display
    * purposes).</li>
    * <li><code>classifier</code> a string identifying the category of the item (
    * <code>sequence</code> | <code>choice</code> | <code>all</code>).</li>
    * <li><code>elements</code> a list containing element references.</li>
    * </ul>
    * <p>
    * Each <b>element</b> reference has the following structure:
    * <ul>
    * <li><code>name</code> a string containing the name of the item (for display
    * purposes).</li>
    * <li><code>type</code> the xsd type of the element reference.</li>
    * <li><code>cardinality</code> the cardinality of the element reference (
    * <code>required</code> | <code>optional</code> | <code>many</code> |
    * <code>at least one</code>).</li>
    * <li><code>body</code> the body of the element reference (optional).</li>
    * </ul>
    * Each <b>facet</b> has the following structure:
    * <ul>
    * <li><code>name</code> a string containing the value of the facet.</li>
    * <li><code>classifier</code> a string identifying the type of the facet, i.e.
    * <code>enumeration</code>, <code>pattern</code>, etc.</li>
    * </ul>
    *
    * Each item described above has a member <code>icon</code> that specifies the
    * corresponding icon.
    *
    * @param postedData
    *           a JsonObject that contains a primitive (String) member with the name "url"
    *           that specifies the URL from where the XSD should be loaded.
    * @return the JsonObject containing the representation of the element and type
    *         declarations.
    */
   public JsonObject getXsdStructure(JsonObject postedData)
   {
      String xsdUrl = postedData.get("url").getAsString();
      System.out.println("===> Get XSD Structure for URL " + xsdUrl);

      JsonObject json = null;

      try
      {
         XSDSchema schema = loadSchema(xsdUrl);

         json = XsdSchemaUtils.toSchemaJson(schema);
      }
      catch (IOException ioex)
      {
         throw new RuntimeException(ioex);
      }

      System.out.println("===> Result " + json);

      return json;
   }

   /* remove */private static final String EXTERNAL_SCHEMA_MAP = "com.infinity.bpm.rt.data.structured.ExternalSchemaMap";

   /* remove */private static final XSDResourceFactoryImpl XSD_RESOURCE_FACTORY = new XSDResourceFactoryImpl();

   private WebModelerUriConverter uriConverter;

   public WebModelerUriConverter getClasspathUriConverter()
   {
      if (uriConverter == null)
      {
         uriConverter = new WebModelerUriConverter();
         uriConverter.setModelService(this);
      }
      return uriConverter;
   }

   /**
    * Duplicate of StructuredTypeRtUtils.getSchema(String, String).
    * <p>
    * Should be removed after repackaging of XSDSchema for runtime is dropped.
    */
   public XSDSchema loadSchema(String location) throws IOException
   {
      Parameters parameters = Parameters.instance();
      Map<String, Object> loadedSchemas = null;
      synchronized (StructuredTypeRtUtils.class)
      {
         loadedSchemas = parameters.getObject(EXTERNAL_SCHEMA_MAP);
         if (loadedSchemas == null)
         {
            // (fh) using Hashtable to avoid concurrency problems.
            loadedSchemas = new Hashtable<String, Object>();
            parameters.set(EXTERNAL_SCHEMA_MAP, loadedSchemas);
         }
      }
      Object o = loadedSchemas.get(location);
      if (o != null)
      {
         return o instanceof XSDSchema ? (XSDSchema) o : null;
      }

      ResourceSetImpl resourceSet = new ResourceSetImpl();
      URI uri = URI.createURI(location);
      if (uri.scheme() == null)
      {
         resourceSet.setURIConverter(getClasspathUriConverter());
         if (location.startsWith("/"))
         {
            location = location.substring(1);
         }
         uri = URI.createURI(WebModelerUriConverter.CLASSPATH_SCHEME + ":/" + location);
      }
      // (fh) register the resource factory directly with the resource set and do not
      // tamper with the global registry.
      resourceSet.getResourceFactoryRegistry()
            .getProtocolToFactoryMap()
            .put(uri.scheme(), XSD_RESOURCE_FACTORY);
      resourceSet.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .put("xsd", XSD_RESOURCE_FACTORY);
      org.eclipse.emf.ecore.resource.Resource resource = resourceSet.createResource(uri);
      Map<Object, Object> options = new HashMap<Object, Object>();
      options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
      resource.load(options);

      for (EObject eObject : resource.getContents())
      {
         if (eObject instanceof XSDSchema)
         {
            XSDSchema schema = (XSDSchema) eObject;
            resolveImports(schema);
            if (trace.isDebugEnabled())
            {
               trace.debug("Found schema for namespace: " + schema.getTargetNamespace()
                     + " at location: " + uri.toString());
            }
            loadedSchemas.put(location, schema);
            return schema;
         }
      }
      loadedSchemas.put(location, "NULL");
      return null;
   }

   /**
    * Should be removed together with loadSchema
    */
   private static void resolveImports(XSDSchema schema)
   {
      for (XSDSchemaContent item : schema.getContents())
      {
         if (item instanceof XSDImportImpl)
         {
            // force schema resolving.
            // it's a noop if the schema is already resolved.
            ((XSDImportImpl) item).importSchema();
         }
      }
   }

   public ModelBuilderFacade getModelBuilderFacade()
   {
      return new ModelBuilderFacade(getModelManagementStrategy());
   }

   /**
    *
    */
   public JsonArray getConfigurationVariables(String modelId)
   {
      JsonArray variablesJson = new JsonArray();

      ModelType model = findModel(modelId);

      VariableContext variableContext = new VariableContext();

      variableContext.initializeVariables(model);
      variableContext.refreshVariables(model);
      variableContext.saveVariables();

      for (Iterator<ModelVariable> i = variableContext.getVariables().iterator(); i.hasNext();)
      {
         ModelVariable modelVariable = i.next();
         JsonObject variableJson = new JsonObject();

         variablesJson.add(variableJson);

         String cleanName = getModelVariableName(modelVariable.getName());
         variableJson.addProperty("type", VariableContextHelper.getType(cleanName));
         variableJson.addProperty("name", modelVariable.getName());
         variableJson.addProperty("defaultValue", modelVariable.getDefaultValue());
         variableJson.addProperty("description", modelVariable.getDescription());
         List<EObject> refList = variableContext.getReferences(modelVariable);

         JsonArray referencesJson = new JsonArray();

         variableJson.add("references", referencesJson);

         // TODO Why is there no empty list

         if (refList != null)
         {
            for (Iterator<EObject> j = refList.iterator(); j.hasNext();)
            {
               Object reference = j.next();
               JsonObject referenceJson = new JsonObject();

               referencesJson.add(referenceJson);

               if (reference instanceof AttributeType)
               {
                  AttributeType attribute = (AttributeType) reference;

                  referenceJson.addProperty("elementName", attribute.getName());
                  referenceJson.addProperty("elementType", "attribute");

                  if (attribute.eContainer() instanceof IIdentifiableModelElement)
                  {
                     referenceJson.addProperty("scopeName",
                           ((IIdentifiableModelElement) attribute.eContainer()).getName());
                     referenceJson.addProperty("scopeType", "modelElement");
                  }
                  else if (attribute.eContainer() instanceof ModelType)
                  {
                     referenceJson.addProperty("scopeName", model.getName());
                     referenceJson.addProperty("scopeType", "model");
                  }
                  else
                  {
                     referenceJson.addProperty("scopeType", "other");
                  }
               }
               else if (reference instanceof DescriptionType)
               {
                  DescriptionType description = (DescriptionType) reference;

                  referenceJson.addProperty("elementType", "description");

                  if (description.eContainer() instanceof IIdentifiableModelElement)
                  {
                     referenceJson.addProperty(
                           "scopeName",
                           ((IIdentifiableModelElement) description.eContainer()).getName());
                     referenceJson.addProperty("scopeType", "modelElement");
                  }
                  else if (description.eContainer() instanceof ModelType)
                  {
                     referenceJson.addProperty("scopeName", model.getName());
                     referenceJson.addProperty("scopeType", "model");
                  }
                  else
                  {
                     referenceJson.addProperty("scopeType", "other");
                  }
               }
               else
               {
                  referenceJson.addProperty("elementType", "other");
               }
            }
         }
      }

      return variablesJson;
   }

   /**
    * Might be redundant as we could do this entirely on the client, but good test for
    * equivalent Runtime functionality.
    */
   public String retrieveEmbeddedExternalWebApplicationMarkup(String modelId,
         String applicationId)
   {
      ApplicationType application = getModelBuilderFacade().findApplication(
            modelId + ":" + applicationId);

      // TODO Improper coding - need better ways to find context

      for (ContextType context : application.getContext())
      {
         Object attribute = getModelBuilderFacade().getAttribute(context,
               "carnot:engine:ui:externalWebApp:markup");

         if (attribute != null)
         {
            return getModelBuilderFacade().getAttributeValue(attribute);
         }
      }

      // TODO I18N

      return "Embedded Web Application is not configured.";
   }

   private String getModelVariableName(String name)
   {
      if (name.startsWith("${")) //$NON-NLS-1$
      {
         name = name.substring(2, name.length() - 1);
      }
      return name;
   }
}