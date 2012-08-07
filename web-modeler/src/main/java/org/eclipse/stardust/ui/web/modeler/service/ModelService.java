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

package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newApplicationActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newStructVariable;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.w3c.dom.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementHelper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.JcrConnectionManager;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.PepperIconFactory;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.CarnotConstants;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.modeling.repository.common.descriptors.ReplaceModelElementDescriptor;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationService;
import org.eclipse.stardust.modeling.validation.ValidatorRegistry;
import org.eclipse.stardust.ui.web.modeler.edit.EditingSessionManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 * 
 * @author Shrikant.Gangal, Marc.Gille
 * 
 */
public class ModelService
{
   public static final String MODELING_DOCUMENTS_DIR = "/process-modeling-documents/";

   public static final String NULL_VALUE = "null";

   public static final String DIRECTORY_MODE = "DIRECTORY_MODE";

   public static final String SINGLE_FILE_MODE = "SINGLE_FILE_MODE";

   public static final String TYPE_PROPERTY = "type";

   public static final String ATTRIBUTES_PROPERTY = "attributes";

   public static final String OID_PROPERTY = "oid";

   public static final String NEW_OBJECT_PROPERTY = "newObject";

   public static final String OLD_OBJECT_PROPERTY = "oldObject";

   public static final String X_PROPERTY = "x";

   public static final String Y_PROPERTY = "y";

   public static final String WIDTH_PROPERTY = "width";

   public static final String HEIGHT_PROPERTY = "height";

   public static final String DESCRIPTION_PROPERTY = "description";

   public static final String MODEL_ID_PROPERTY = "modelId";

   public static final String PARENT_SYMBOL_ID_PROPERTY = "parentSymbolId";

   public static final String ACTIVITIES_PROPERTY = "activities";

   public static final String GATEWAYS_PROPERTY = "gateways";

   public static final String EVENTS_PROPERTY = "events";

   public static final String ACTIVITY_KEY = "activity";

   public static final String ACTIVITY_SYMBOLS = "activitySymbols";

   public static final String GATEWAY_SYMBOLS = "gatewaySymbols";

   public static final String APPLICATION_TYPE_PROPERTY = "applicationType";

   public static final String ACCESS_POINTS_PROPERTY = "accessPoints";

   public static final String IN_ACCESS_POINT_KEY = "IN_ACCESS_POINT";

   public static final String OUT_ACCESS_POINT_KEY = "OUT_ACCESS_POINT";

   public static final String INOUT_ACCESS_POINT_KEY = "INOUT_ACCESS_POINT";

   public static final String ACCESS_POINT_TYPE_PROPERTY = "accessPointType";

   public static final String PRIMITIVE_ACCESS_POINT_KEY = "PRIMITIVE_ACCESS_POINT";

   public static final String DATA_STRUCTURE_ACCESS_POINT_KEY = "DATA_STRUCTURE_ACCESS_POINT";

   public static final String JAVA_CLASS_ACCESS_POINT_KEY = "JAVA_CLASS_ACCESS_POINT";

   public static final String ANY_ACCESS_POINT_KEY = "ANY_ACCESS_POINT";

   public static final String CONNECTION = "connection";

   public static final String DIRECTION_PROPERTY = "direction";

   public static final String CONTROL_FLOW_LITERAL = "controlFlow";

   public static final String DATA_FLOW_LITERAL = "dataFlow";

   public static final String FROM_ANCHOR_POINT_ORIENTATION_PROPERTY = "fromAnchorPointOrientation";

   public static final String TO_ANCHOR_POINT_ORIENTATION_PROPERTY = "toAnchorPointOrientation";

   public static final int UNDEFINED_ORIENTATION_KEY = -1;

   public static final int NORTH_KEY = 0;

   public static final int EAST_KEY = 1;

   public static final int SOUTH_KEY = 2;

   public static final int WEST_KEY = 3;

   public static final String GATEWAY = "gateway";

   public static final String GATEWAY_ACTIVITY = "Gateway";

   public static final String GATEWAY_TYPE_PROPERTY = "gatewayType";

   public static final String AND_GATEWAY_TYPE = "and";

   public static final String XOR_GATEWAY_TYPE = "xor";

   public static final String OR_GATEWAY_TYPE = "or";

   public static final String EVENT_KEY = "event";

   public static final String EVENT_SYMBOLS = "eventSymbols";

   public static final String EVENT_TYPE_PROPERTY = "eventType";

   public static final String START_EVENT = "startEvent";

   public static final String STOP_EVENT = "stopEvent";

   public static final String DATA = "data";

   public static final String DATA_SYMBOLS = "dataSymbols";

   public static final String STRUCTURED_DATA_TYPE_FULL_ID = "structuredDataTypeFullId";

   public static final String TYPE_DECLARATION_PROPERTY = "typeDeclaration";

   public static final String CONNECTIONS_PROPERTY = "connections";

   public static final String CONTROL_FLOWS_PROPERTY = "controlFlows";

   public static final String DATA_FLOWS_PROPERTY = "dataFlows";

   public static final String CONDITION_EXPRESSION_PROPERTY = "conditionExpression";

   public static final String IN_DATA_MAPPING_PROPERTY = "inDataMapping";

   public static final String OUT_DATA_MAPPING_PROPERTY = "outDataMapping";

   public static final String DATA_PATH_PROPERTY = "dataPath";

   public static final String APPLICATION_PATH_PROPERTY = "applicationPath";

   public static final String OTHERWISE_PROPERTY = "otherwise";

   public static final String CONDITION_KEY = "CONDITION";

   public static final String OTHERWISE_KEY = "OTHERWISE";

   public static final String POOL_SYMBOLS = "poolSymbols";

   public static final String LANE_SYMBOLS = "laneSymbols";

   public static final String FROM_MODEL_ELEMENT_OID = "fromModelElementOid";

   public static final String FROM_MODEL_ELEMENT_TYPE = "fromModelElementType";

   public static final String TO_MODEL_ELEMENT_OID = "toModelElementOid";

   public static final String TO_MODEL_ELEMENT_TYPE = "toModelElementType";

   public static final String WEB_SERVICE_APPLICATION_TYPE_ID = "webservice";

   public static final String MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID = "messageTransformationBean";

   public static final String CAMEL_APPLICATION_TYPE_ID = "camelBean";

   public static final String MAIL_APPLICATION_TYPE_ID = "mailBean";

   public static final String INTERACTIVE_APPLICATION_TYPE_KEY = "interactive";

   public static final String CONTEXTS_PROPERTY = "contexts";

   public static final String JSF_CONTEXT_TYPE_KEY = "jsf";

   public static final String EXTERNAL_WEB_APP_CONTEXT_TYPE_KEY = "externalWebApp";

   public static final int POOL_LANE_MARGIN = 5;

   public static final int POOL_SWIMLANE_TOP_BOX_HEIGHT = 20;

   /*
    * Half the size of the review why this adjustment is needed start event symbol used in
    * Pepper TODO - may need to be handled on the client side down the line.
    */
   public static final int START_END_SYMBOL_LEFT_OFFSET = 12;

   private static final String MODEL_DOCUMENTATION_TEMPLATES_FOLDER = "/documents/templates/modeling/";

   private ServiceFactory serviceFactory;

   private DocumentManagementService documentManagementService;

   private UserService userService;

   private QueryService queryService;

   private ModelManagementStrategy modelManagementStrategy;

   // Modeling Session Management

   private Map<String, User> prospectUsers = new HashMap<String, User>();

   private Map<String, User> participantUsers = new HashMap<String, User>();

   private Map<String, String> imageUris = new HashMap<String, String>();

   /**
    * Contains all loaded and newly created getModelManagementStrategy().getModels().
    */
   private JsonObject modelsJson = new JsonObject();

   @Resource
   private EditingSessionManager editingSessionManager;

   @Resource
   private ModelElementMarshaller modelElementMarshaller;

   @Resource
   private EObjectUUIDMapper eObjectUUIDMapper;

   public ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         serviceFactory = ServiceFactoryLocator.get("motu", "motu");
      }

      return serviceFactory;
   }

   /**
    * 
    * @return
    */
   public ModelManagementStrategy getModelManagementStrategy()
   {
      return modelManagementStrategy;
   }

   /**
    * 
    * @param modelManagementStrategy
    */
   public void setModelManagementStrategy(ModelManagementStrategy modelManagementStrategy)
   {
      this.modelManagementStrategy = modelManagementStrategy;
      ModelManagementHelper.getInstance().setModelManagementStrategy(
            modelManagementStrategy);
   }

   /**
    * 
    * @param attrs
    * @param attrType
    */
   private void removeIfExists(List<AttributeType> attrs, AttributeType attrType)
   {
      Iterator<AttributeType> iter = attrs.iterator();
      while (iter.hasNext())
      {
         if (iter.next().getName().equals(attrType.getName()))
         {
            iter.remove();
         }
      }
   }

   /**
    * 
    * @param json
    * @param element
    * @throws JSONException
    */
   private void storeAttributes(JsonObject json, IIdentifiableModelElement element)
   {
      if ( !json.has(ATTRIBUTES_PROPERTY))
      {
         return;
      }

      JsonObject attributes = json.getAsJsonObject(ATTRIBUTES_PROPERTY);

      if (attributes != null)
      {
         for (Map.Entry<String, ? > entry : attributes.entrySet())
         {
            String key = entry.getKey();
            JsonElement value = attributes.get(key);

            System.out.println("Storing attribute " + key + " " + value);

            if (value instanceof JsonObject)
            {
            }
            else
            {
               AttributeUtil.setAttribute(element, key, value.toString());
            }
         }
      }
   }

   /**
    * 
    * @param model
    * @param processDefinition
    * @return
    */
   private EditingSession getEditingSession(ModelType model)
   {
      return editingSessionManager.getSession(model);
   }

   /**
    * 
    * @param postedData
    * @return
    */
   public String requestJoin(JsonObject postedData)
   {
      System.out.println("Account: "
            + postedData.getAsJsonObject(NEW_OBJECT_PROPERTY)
                  .get("account")
                  .getAsString());

      requestJoin(postedData.getAsJsonObject(NEW_OBJECT_PROPERTY)
            .get("account")
            .getAsString());

      return postedData.toString();
   }

   /**
    * 
    * @param postedData
    * @return
    */
   public void requestJoin(String account)
   {
      User prospect = getUserService().getUser(account);

      prospectUsers.put(prospect.getAccount(), prospect);
      // imageUris.put("prospect.getAccount()", );
   }

   /**
    * 
    * @param postedData
    * @return
    */
   public String confirmJoin(JsonObject postedData)
   {
      String account = postedData.getAsJsonObject(OLD_OBJECT_PROPERTY)
            .get("account")
            .getAsString();
      User participant = prospectUsers.get(account);

      participantUsers.put(account, participant);
      prospectUsers.remove(participant);

      // TODO @Francesca
      // Push CONFIRM_JOIN_COMMAND to all browser sessions

      return postedData.toString();
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

   /**
    * 
    * @param userAccountList
    */
   public void inviteUsers(List<String> userAccountList)
   {
      // TODO @Francesca
      // Push REQUEST_JOIN_COMMAND to originating browser session for testing
   }

   /**
    * 
    * @param modelId
    * @param commandJson
    * @return
    */
   public String deleteModel(String modelId, JsonObject commandJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);

      getModelManagementStrategy().deleteModel(model);

      return commandJson.toString();
   }

   /**
    * 
    * @param modelId
    * @param commandJson
    * @return
    */
   public String deleteProcess(String modelId, String processId, JsonObject commandJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);

      if (null == model)
      {
         return null;
      }
      else
      {
         model.getProcessDefinition().remove(processDefinition);
      }

      return commandJson.toString();
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param postedData
    * @return
    */
   public String deleteStructuredDataType(String modelId, String structuredDataTypeId,
         JsonObject commandJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      TypeDeclarationType structuredDataType = MBFacade.findStructuredDataType(model,
            structuredDataTypeId);
      synchronized (model)
      {
         model.getTypeDeclarations().getTypeDeclaration().remove(structuredDataType);
      }
      return commandJson.toString();
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param postedData
    * @return
    */
   public String deleteParticipant(String modelId, String participantId,
         JsonObject commandJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      IModelParticipant modelParticipantInfo = MBFacade.findParticipant(model,
            participantId);
      synchronized (model)
      {
         model.getRole().remove(modelParticipantInfo);
      }
      return commandJson.toString();
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param newProcessName
    * @param postedData
    * @return
    */
   public String deleteApplication(String modelId, String applicationId,
         JsonObject commandJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ApplicationType application = MBFacade.findApplication(model, applicationId);

      synchronized (model)
      {
         model.getApplication().remove(application);
      }
      return commandJson.toString();
   }

   /**
    * Retrieves all the stored models and returns a json array of references of these
    * getModelManagementStrategy().getModels().
    * 
    * @return
    */
   public String getAllModels()
   {
      try
      {
         // Reload only if model map was empty
         // TODO Review
         getModelManagementStrategy().getModels(
               getModelManagementStrategy().getModels().isEmpty());

         // Refresh JSON
         // TODO Smarter caching

         modelsJson = new JsonObject();

         for (ModelType model : getModelManagementStrategy().getModels().values())
         {
            modelsJson.add(model.getId(), loadModelOutline(model));
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
      ModelType model = getModelManagementStrategy().getModels().get(modelId);

      getModelManagementStrategy().saveModel(model);
   }

   /**
	 *
	 */
   public void saveAllModels()
   {
      // TODO
      // Temporarily commenting selective save as not all changes have moved to change
      // protocol yet.
      // After that this can be uncommented
      /*
       * Set<String> changedModels =
       * UnsavedModelsTracker.getInstance().getUnsavedModels(); for (String modelId :
       * changedModels) { ModelType model =
       * getModelManagementStrategy().getModels().get(modelId); if (null != model) {
       * getModelManagementStrategy().saveModel(model); } }
       * 
       * //Clear the unsaved models' list.
       * UnsavedModelsTracker.getInstance().notifyAllModelsSaved();
       */

      // TODO
      // Temporarily saving all models as not all changes have moved to change protocol
      // yet.
      // After that happens this code can be deleted.
      Collection<ModelType> models = getModelManagementStrategy().getModels().values();
      for (ModelType model : models)
      {
         getModelManagementStrategy().saveModel(model);
      }
   }

   /**
    * 
    * @param id
    * @return
    */
   public ModelType getModel(String id)
   {
      return getModelManagementStrategy().getModels().get(id);
   }

   /**
    * 
    * @param modelId
    * @param id
    * @param postedData
    * @return
    */
   public JsonObject createProcessJson(String modelId, JsonObject postedData)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      String name = extractString(postedData, NEW_OBJECT_PROPERTY,
            ModelerConstants.NAME_PROPERTY);
      String id = MBFacade.createIdFromName(name);
      ProcessDefinitionType processDefinition = MBFacade.createProcess(model, name, id);

      JsonObject processDefinitionJson = new JsonObject();

      processDefinitionJson.addProperty(TYPE_PROPERTY, "process");
      processDefinitionJson.addProperty(ModelerConstants.ID_PROPERTY, id);
      processDefinitionJson.addProperty(ModelerConstants.NAME_PROPERTY, name);
      processDefinitionJson.addProperty(MODEL_ID_PROPERTY, modelId);
      processDefinitionJson.addProperty(TYPE_PROPERTY, "process");
      processDefinitionJson.add(ATTRIBUTES_PROPERTY, new JsonObject());
      processDefinitionJson.add(ACTIVITIES_PROPERTY, new JsonObject());
      processDefinitionJson.add(GATEWAYS_PROPERTY, new JsonObject());
      processDefinitionJson.add(EVENTS_PROPERTY, new JsonObject());
      processDefinitionJson.add(DATA_FLOWS_PROPERTY, new JsonObject());
      processDefinitionJson.add(CONTROL_FLOWS_PROPERTY, new JsonObject());

      postedData.add(NEW_OBJECT_PROPERTY, processDefinitionJson);

      return postedData;
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param activityId
    * @param postedData
    * @return
    */
   public String renameActivity(String modelId, String processId, String activityId,
         JsonObject commandJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);
      ActivityType activity = MBFacade.findActivity(processDefinition, activityId);
      EditingSession editingSession = getEditingSession(model);

      synchronized (model)
      {
         editingSession.beginEdit();

         JsonObject newNameJson = commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY);

         // TODO Auto-generate ID
         activity.setId(extractString(newNameJson, ModelerConstants.NAME_PROPERTY));
         activity.setName(extractString(newNameJson, ModelerConstants.NAME_PROPERTY));
         newNameJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());

         editingSession.endEdit();
      }

      return commandJson.toString();
   }

   /**
    * 
    * @param gatewaySymbol
    * @param gatewaySymbolJson
    * @return
    * @throws JSONException
    */
   private JsonObject updateGateway(ActivitySymbolType gatewaySymbol,
         LaneSymbol laneSymbol, JsonObject gatewaySymbolJson)
   {
      ActivityType gateway = gatewaySymbol.getActivity();

      if (gatewaySymbolJson.has(ModelerConstants.MODEL_ELEMENT_PROPERTY))
      {
         gateway.setName(extractString(gatewaySymbolJson,
               ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.NAME_PROPERTY));
         setDescription(gateway,
               gatewaySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));
      }

      gatewaySymbol.setXPos(extractInt(gatewaySymbolJson, X_PROPERTY)
            - laneSymbol.getXPos());
      gatewaySymbol.setYPos(extractInt(gatewaySymbolJson, Y_PROPERTY)
            - laneSymbol.getYPos());

      if (gatewaySymbolJson.has(WIDTH_PROPERTY))
      {
         gatewaySymbol.setWidth(extractInt(gatewaySymbolJson, WIDTH_PROPERTY));
      }
      if (gatewaySymbolJson.has(HEIGHT_PROPERTY))
      {
         gatewaySymbol.setHeight(extractInt(gatewaySymbolJson, HEIGHT_PROPERTY));
      }

      return gatewaySymbolJson;
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
    * 
    * @param modelElementJson
    * @param element
    */
   private void loadDescription(JsonObject modelElementJson,
         IIdentifiableModelElement element)
   {
      if (null != element.getDescription())
      {
         modelElementJson.addProperty(DESCRIPTION_PROPERTY,
               (String) element.getDescription().getMixed().get(0).getValue());
      }
      else
      {
         modelElementJson.addProperty(DESCRIPTION_PROPERTY, "");
      }
   }

   /**
    * 
    * @param orientation
    * @return
    */
   private String mapAnchorOrientation(int orientation)
   {
      if (orientation == NORTH_KEY)
      {
         return "top";
      }
      else if (orientation == EAST_KEY)
      {
         return "right";
      }
      else if (orientation == SOUTH_KEY)
      {
         return "bottom";
      }
      else if (orientation == WEST_KEY)
      {
         return "left";
      }

      throw new IllegalArgumentException("Illegal orientation key " + orientation + ".");
   }

   /**
    * 
    * @param connectionJson
    * @param processDefinition
    * @param sourceActivitySymbol
    * @param dataSymbol
    * @param maxOid
    */
   private void createDataFlowConnection(JsonObject connectionJson,
         ProcessDefinitionType processDefinition, ActivitySymbolType activitySymbol,
         DataSymbolType dataSymbol, long maxOid)
   {

      System.out.println("Create data flow connection");

      DataType data = dataSymbol.getData();
      ActivityType activity = activitySymbol.getActivity();

      DataMappingType dataMapping = AbstractElementBuilder.F_CWM.createDataMappingType();
      DataMappingConnectionType dataMappingConnection = AbstractElementBuilder.F_CWM.createDataMappingConnectionType();

      // TODO Add index

      dataMapping.setId(data.getId());
      dataMapping.setName(data.getName());

      dataMapping.setDirection(DirectionType.get(DirectionType.OUT));
      dataMapping.setData(data);

      // TODO Incomplete

      // if (activity.getImplementation().getLiteral().equals("Application"))
      // {
      // dataMapping.setContext(PredefinedConstants.APPLICATION_CONTEXT);
      // dataMapping.setApplicationAccessPoint(element.getProps().getEnds()
      // .getAccesspoint());
      // }
      // else
      // {
      dataMapping.setContext(PredefinedConstants.DEFAULT_CONTEXT);
      // }

      activity.getDataMapping().add(dataMapping);

      // TODO Obtain pool from call

      processDefinition.getDiagram()
            .get(0)
            .getPoolSymbols()
            .get(0)
            .getDataMappingConnection()
            .add(dataMappingConnection);

      dataMappingConnection.setElementOid(++maxOid);
      dataMappingConnection.setActivitySymbol(activitySymbol);
      dataMappingConnection.setDataSymbol(dataSymbol);
      activitySymbol.getDataMappings().add(dataMappingConnection);
      dataSymbol.getDataMappings().add(dataMappingConnection);
      dataMappingConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
            connectionJson, FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
      dataMappingConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
            connectionJson, TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

   }

   /**
    * 
    * TODO From DynamicConnectionCommand. Refactor?
    * 
    * @param activity
    * @return
    */
   private String getDefaultDataMappingContext(ActivityType activity)
   {
      if (ActivityImplementationType.ROUTE_LITERAL == activity.getImplementation())
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }
      if (ActivityImplementationType.MANUAL_LITERAL == activity.getImplementation())
      {
         return PredefinedConstants.DEFAULT_CONTEXT;
      }
      if (ActivityImplementationType.APPLICATION_LITERAL == activity.getImplementation()
            && activity.getApplication() != null)
      {
         ApplicationType application = activity.getApplication();
         if (application.isInteractive())
         {
            if (application.getContext().size() > 0)
            {
               ContextType context = (ContextType) application.getContext().get(0);
               return context.getType().getId();
            }
            return PredefinedConstants.DEFAULT_CONTEXT;
         }
         return PredefinedConstants.APPLICATION_CONTEXT;
      }
      if (ActivityImplementationType.SUBPROCESS_LITERAL == activity.getImplementation()
            && activity.getImplementationProcess() != null)
      {
         ProcessDefinitionType process = activity.getImplementationProcess();
         if (process.getFormalParameters() != null)
         {
            return PredefinedConstants.PROCESSINTERFACE_CONTEXT;
         }
      }
      return PredefinedConstants.ENGINE_CONTEXT;
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param connectionId
    * @param postedData
    * @return
    */
   public String updateConnection(String modelId, String processId, long connectionOid,
         JsonObject connectionJson)
   {
      JsonObject modelElementJson = connectionJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);
      EditingSession editSession = getEditingSession(model);

      synchronized (model)
      {
         editSession.beginEdit();

         System.out.println("Updateing Connection " + connectionOid + " "
               + connectionJson.toString());

         if (extractString(modelElementJson, TYPE_PROPERTY).equals(CONTROL_FLOW_LITERAL))
         {
            TransitionConnectionType transitionConnection = MBFacade.findTransitionConnectionByModelOid(
                  processDefinition, connectionOid);
            transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
                  connectionJson, FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
            transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
                  connectionJson, TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

            if (transitionConnection.getTransition() != null)
            {

               if (extractBoolean(modelElementJson, OTHERWISE_PROPERTY))
               {
                  transitionConnection.getTransition().setCondition(OTHERWISE_KEY);
               }
               else
               {
                  transitionConnection.getTransition().setCondition(CONDITION_KEY);

                  XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE.createXmlTextNode();

                  ModelUtils.setCDataString(expression.getMixed(),
                        extractString(modelElementJson, CONDITION_EXPRESSION_PROPERTY),
                        true);

                  transitionConnection.getTransition().setExpression(expression);
               }

               setDescription(transitionConnection.getTransition(), modelElementJson);
               storeAttributes(modelElementJson, transitionConnection.getTransition());
            }
         }
         else
         {
            DataMappingConnectionType dataMappingConnection = MBFacade.findDataMappingConnectionByModelOid(
                  processDefinition, connectionOid);

            dataMappingConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
                  connectionJson, FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
            dataMappingConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
                  connectionJson, TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
         }

         editSession.endEdit();
      }

      return connectionJson.toString();
   }

   /**
    * 
    * @param poolSymbol
    * @param poolSymbolJson
    * @return
    */
   public JsonObject updatePool(ModelType model, PoolSymbol poolSymbol,
         JsonObject poolSymbolJson)
   {
      poolSymbol.setXPos(extractInt(poolSymbolJson, X_PROPERTY));
      poolSymbol.setYPos(extractInt(poolSymbolJson, Y_PROPERTY));
      poolSymbol.setWidth(extractInt(poolSymbolJson, WIDTH_PROPERTY));
      poolSymbol.setHeight(extractInt(poolSymbolJson, HEIGHT_PROPERTY));
      poolSymbol.setName(extractString(poolSymbolJson, ModelerConstants.NAME_PROPERTY));

      // TODO is array
      // JSONObject laneSymbolsJson =
      // poolSymbolJson.getJSONObject(LANE_SYMBOLS);
      JsonArray laneSymbolsJson = poolSymbolJson.getAsJsonArray(LANE_SYMBOLS);

      for (int n = 0; n < laneSymbolsJson.size(); ++n)
      {
         // for (Iterator iterator = laneSymbolsJson.keys();
         // iterator.hasNext();) {
         // String key = (String)iterator.next();
         // JSONObject laneSymbolJson = laneSymbolsJson.getJSONObject(key);
         JsonObject laneSymbolJson = laneSymbolsJson.get(n).getAsJsonObject();
         LaneSymbol laneSymbol = MBFacade.findLaneSymbolByElementOid(poolSymbol,
               extractLong(laneSymbolJson, OID_PROPERTY));

         updateLane(model, laneSymbol, laneSymbolJson);
      }

      return poolSymbolJson;
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param postedData
    * @return
    */
   public String updateLane(String modelId, String processId, String laneId,
         JsonObject laneSymbolJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);
      LaneSymbol laneSymbol = MBFacade.findLaneSymbolById(processDefinition, laneId);

      EditingSession editingSession = getEditingSession(model);

      synchronized (model)
      {
         editingSession.beginEdit();

         laneSymbolJson = updateLane(model, laneSymbol, laneSymbolJson);

         editingSession.endEdit();

         return laneSymbolJson.toString();
      }
   }

   /**
    * 
    * @param laneSymbol
    * @param laneSymbolJson
    * @return
    * @throws JSONException
    */
   public JsonObject updateLane(ModelType model, LaneSymbol laneSymbol,
         JsonObject laneSymbolJson)
   {
      laneSymbol.setName(extractString(laneSymbolJson, ModelerConstants.NAME_PROPERTY));
      laneSymbol.setXPos(extractInt(laneSymbolJson, X_PROPERTY));
      laneSymbol.setYPos(extractInt(laneSymbolJson, Y_PROPERTY));
      laneSymbol.setWidth(extractInt(laneSymbolJson, WIDTH_PROPERTY));
      laneSymbol.setHeight(extractInt(laneSymbolJson, HEIGHT_PROPERTY));

      // TODO Deal with full Ids

      if (laneSymbolJson.has(ModelerConstants.PARTICIPANT_FULL_ID))
      {
         System.out.println("Participant Full ID"
               + extractString(laneSymbolJson, ModelerConstants.PARTICIPANT_FULL_ID));
         System.out.println("Participant "
               + MBFacade.findParticipant(model, MBFacade.stripFullId(extractString(
                     laneSymbolJson, ModelerConstants.PARTICIPANT_FULL_ID))));

         String participantModelID = MBFacade.getModelId(extractString(laneSymbolJson,
               ModelerConstants.PARTICIPANT_FULL_ID));
         if (StringUtils.isEmpty(participantModelID))
         {
            participantModelID = model.getId();
         }

         ModelType participantModel = model;
         if ( !participantModelID.equals(model.getId()))
         {
            participantModel = getModelManagementStrategy().getModels().get(
                  participantModelID);
         }

         IModelParticipant modelParticipant = MBFacade.findParticipant(
               getModelManagementStrategy().getModels().get(participantModelID),
               MBFacade.stripFullId(extractString(laneSymbolJson,
                     ModelerConstants.PARTICIPANT_FULL_ID)));

         if ( !participantModelID.equals(model.getId()))
         {
            String fileConnectionId = JcrConnectionManager.createFileConnection(model,
                  participantModel);

            String bundleId = CarnotConstants.DIAGRAM_PLUGIN_ID;
            URI uri = URI.createURI("cnx://" + fileConnectionId + "/");

            ReplaceModelElementDescriptor descriptor = new ReplaceModelElementDescriptor(
                  uri, modelParticipant, bundleId, null, true);

            PepperIconFactory iconFactory = new PepperIconFactory();

            descriptor.importElements(iconFactory, model, true);
         }

         laneSymbol.setParticipant(modelParticipant);
      }

      JsonObject activitySymbolsJson = laneSymbolJson.getAsJsonObject(ACTIVITY_SYMBOLS);
      for (Map.Entry<String, JsonElement> entry : activitySymbolsJson.entrySet())
      {
         JsonObject activitySymbolJson = entry.getValue().getAsJsonObject();

         ActivitySymbolType activitySymbol = MBFacade.findActivitySymbol(laneSymbol,
               extractLong(activitySymbolJson, OID_PROPERTY));

         // updateActivity(activitySymbol, laneSymbol, activitySymbolJson);

      }

      JsonObject gatewaySymbolsJson = laneSymbolJson.getAsJsonObject(GATEWAY_SYMBOLS);
      for (Map.Entry<String, JsonElement> entry : gatewaySymbolsJson.entrySet())
      {
         JsonObject gatewaySymbolJson = entry.getValue().getAsJsonObject();

         ActivitySymbolType gatewaySymbol = MBFacade.findActivitySymbol(laneSymbol,
               extractLong(gatewaySymbolJson, OID_PROPERTY));

         updateGateway(gatewaySymbol, laneSymbol, gatewaySymbolJson);
      }

      JsonObject eventSymbolsJson = laneSymbolJson.getAsJsonObject(EVENT_SYMBOLS);
      for (Map.Entry<String, JsonElement> entry : eventSymbolsJson.entrySet())
      {
         JsonObject eventSymbolJson = entry.getValue().getAsJsonObject();

         AbstractEventSymbol eventSymbol = MBFacade.findStartEventSymbol(laneSymbol,
               extractLong(eventSymbolJson, OID_PROPERTY));

         if (eventSymbol == null)
         {
            eventSymbol = MBFacade.findEndEventSymbol(laneSymbol,
                  extractLong(eventSymbolJson, OID_PROPERTY));
         }

         // updateEvent(eventSymbol, laneSymbol, eventSymbolJson);
      }

      JsonObject dataSymbolsJson = laneSymbolJson.getAsJsonObject(DATA_SYMBOLS);
      for (Map.Entry<String, JsonElement> entry : dataSymbolsJson.entrySet())
      {
         JsonObject dataSymbolJson = entry.getValue().getAsJsonObject();

         DataSymbolType dataSymbol = MBFacade.findDataSymbolRecursively(laneSymbol,
               extractLong(dataSymbolJson, OID_PROPERTY));

         // updateData(dataSymbol, dataSymbolJson);
      }

      return laneSymbolJson;
   }

   /**
    * @return
    */
   public String loadProcessDiagram(String modelId, String processId)
   {
      // TODO Try to ModelBuilderFascade.find in loaded models first. Correct?
      ModelType model = getModelManagementStrategy().getModels().get(modelId);

      // TODO Very ugly - only for newly created models

      if (model == null)
      {
         getModelManagementStrategy().attachModel(modelId);
      }

      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);

      return modelElementMarshaller.toProcessDefinitionDiagram(processDefinition)
            .toString();
   }

   /**
    * @return
    */
   public String updateProcessDiagram(String modelId, String processId, String diagramId,
         JsonObject diagramJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);
      DiagramType diagram = processDefinition.getDiagram().get(0);
      EditingSession editSession = getEditingSession(model);

      editSession.beginEdit();

      JsonObject poolSymbolsJson = diagramJson.getAsJsonObject(POOL_SYMBOLS);

      for (Map.Entry<String, JsonElement> entry : poolSymbolsJson.entrySet())
      {
         JsonObject poolSymbolJson = entry.getValue().getAsJsonObject();

         PoolSymbol poolSymbol = MBFacade.findPoolSymbolByElementOid(processDefinition,
               extractLong(poolSymbolJson, OID_PROPERTY));

         updatePool(model, poolSymbol, poolSymbolJson);
      }

      editSession.endEdit();

      return diagramJson.toString();
   }

   /**
    * 
    * @param httpRequest
    * @param modelId
    * @return
    */
   private JsonObject loadModelOutline(ModelType model)
   {
      JsonObject modelJson = new JsonObject();

      modelJson.addProperty(ModelerConstants.ID_PROPERTY, model.getId());
      modelJson.addProperty(ModelerConstants.NAME_PROPERTY, model.getName());
      modelJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper.getUUID(model));

      if (model.getDescription() != null)
      {
         modelJson.addProperty(DESCRIPTION_PROPERTY, (String) model.getDescription()
               .getMixed()
               .get(0)
               .getValue());
      }
      else
      {
         modelJson.addProperty(DESCRIPTION_PROPERTY, (String) null);
      }

      JsonObject processesJson = new JsonObject();

      modelJson.add("processes", processesJson);

      for (ProcessDefinitionType processDefinition : model.getProcessDefinition())
      {
         processesJson.add(processDefinition.getId(),
               modelElementMarshaller.toProcessDefinition(processDefinition));
      }

      JsonObject participantsJson = new JsonObject();
      modelJson.add("participants", participantsJson);

      for (RoleType role : model.getRole())
      {
         if (!hasParentParticipant(model, role))
         {
            JsonObject participantJson = new JsonObject();
            participantsJson.add(role.getId(), participantJson);

            participantJson.addProperty(ModelerConstants.ID_PROPERTY, role.getId());
            participantJson.addProperty(ModelerConstants.NAME_PROPERTY, role.getName());
            participantJson.addProperty(ModelerConstants.OID_PROPERTY, role.getElementOid());
            participantJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                  ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY);
            participantJson.addProperty(ModelerConstants.UUID_PROPERTY,
                  eObjectUUIDMapper.getUUID(role));
            loadDescription(participantJson, role);
         }
      }

      for (OrganizationType organization : model.getOrganization())
      {
         if (!hasParentParticipant(model, organization))
         {
            JsonObject participantJson = new JsonObject();
            participantsJson.add(organization.getId(), participantJson);

            participantJson.addProperty(ModelerConstants.ID_PROPERTY, organization.getId());
            participantJson.addProperty(ModelerConstants.NAME_PROPERTY,
                  organization.getName());
            participantJson.addProperty(ModelerConstants.OID_PROPERTY, organization.getElementOid());
            participantJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                  ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY);
            participantJson.addProperty(ModelerConstants.UUID_PROPERTY,
                  eObjectUUIDMapper.getUUID(organization));
            loadDescription(participantJson, organization);
            
            //Adds children if any
            addChildParticipantsJson(participantJson, organization);
         }
      }

      for (ConditionalPerformerType conditionalPerformer : model.getConditionalPerformer())
      {
         JsonObject participantJson = new JsonObject();
         participantsJson.add(conditionalPerformer.getId(), participantJson);

         participantJson.addProperty(ModelerConstants.ID_PROPERTY,
               conditionalPerformer.getId());
         participantJson.addProperty(ModelerConstants.NAME_PROPERTY,
               conditionalPerformer.getName());
         participantJson.addProperty(ModelerConstants.TYPE_PROPERTY,
               ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY);
         participantJson.addProperty(ModelerConstants.UUID_PROPERTY,
               eObjectUUIDMapper.getUUID(conditionalPerformer));
         loadDescription(participantJson, conditionalPerformer);
      }

      JsonObject applicationsJson = new JsonObject();

      modelJson.add("applications", applicationsJson);

      for (ApplicationType application : model.getApplication())
      {
         applicationsJson.add(application.getId(),
               modelElementMarshaller.toApplication(application));
      }

      JsonObject dataItemsJson = new JsonObject();
      modelJson.add("dataItems", dataItemsJson);

      for (DataType data : model.getData())
      {
         dataItemsJson.add(data.getId(), loadData(model, data));
      }

      JsonObject structuredDataTypesJson = new JsonObject();
      modelJson.add("structuredDataTypes", structuredDataTypesJson);

      // TODO Check needed?

      if (null != model.getTypeDeclarations())
      {
         for (TypeDeclarationType typeDeclaration : model.getTypeDeclarations()
               .getTypeDeclaration())
         {
            JsonObject structuredDataTypeJson = new JsonObject();
            structuredDataTypesJson.add(typeDeclaration.getId(), structuredDataTypeJson);

            structuredDataTypeJson.addProperty(ModelerConstants.ID_PROPERTY,
                  typeDeclaration.getId());
            structuredDataTypeJson.addProperty(ModelerConstants.NAME_PROPERTY,
                  typeDeclaration.getName());
            structuredDataTypeJson.addProperty(ModelerConstants.UUID_PROPERTY,
                  eObjectUUIDMapper.getUUID(typeDeclaration));
            // TODO Review why different from other descriptions
            structuredDataTypeJson.addProperty(DESCRIPTION_PROPERTY,
                  typeDeclaration.getDescription());

            JsonObject typeDeclarationJson = new JsonObject();
            structuredDataTypeJson.add(TYPE_DECLARATION_PROPERTY, typeDeclarationJson);
            JsonObject childrenJson = new JsonObject();
            typeDeclarationJson.add("children", childrenJson);

            // TODO Review code below, very heuristic ...

            SchemaTypeType schemaType = typeDeclaration.getSchemaType();

            if (schemaType != null)
            {
               org.eclipse.xsd.XSDSchema xsdSchema = schemaType.getSchema();

               // Determine prefix

               String prefix = null;

               for (Iterator iterator = xsdSchema.getQNamePrefixToNamespaceMap()
                     .keySet()
                     .iterator(); iterator.hasNext();)
               {
                  String key = (String) iterator.next();
                  String value = xsdSchema.getQNamePrefixToNamespaceMap().get(key);

                  if (value.equals(xsdSchema.getTargetNamespace()))
                  {
                     prefix = key;

                     break;
                  }
               }

               typeDeclarationJson.addProperty(ModelerConstants.NAME_PROPERTY, prefix
                     + ":" + typeDeclaration.getId());

               for (org.eclipse.xsd.XSDTypeDefinition xsdTypeDefinition : xsdSchema.getTypeDefinitions())
               {

                  if (xsdTypeDefinition.getName().equals(typeDeclaration.getId()))
                  {

                     if (xsdTypeDefinition.getComplexType() != null)
                     {

                        typeDeclarationJson.addProperty(TYPE_PROPERTY, "STRUCTURE_TYPE");

                        for (int n = 0; n < xsdTypeDefinition.getComplexType()
                              .getElement()
                              .getChildNodes()
                              .getLength(); ++n)
                        {
                           Node node = xsdTypeDefinition.getComplexType()
                                 .getElement()
                                 .getChildNodes()
                                 .item(n);
                           JsonObject schemaElementJson = new JsonObject();

                           schemaElementJson.addProperty(ModelerConstants.NAME_PROPERTY,
                                 node.getAttributes().getNamedItem("name").getNodeValue());
                           schemaElementJson.addProperty("typeName", node.getAttributes()
                                 .getNamedItem("type")
                                 .getNodeValue());
                           childrenJson.add(node.getAttributes()
                                 .getNamedItem("name")
                                 .getNodeValue(), schemaElementJson);
                        }
                     }
                     else if (xsdTypeDefinition.getSimpleType() != null)
                     {
                        Node restriction = xsdTypeDefinition.getSimpleType()
                              .getElement()
                              .getChildNodes()
                              .item(0);

                        typeDeclarationJson.addProperty(TYPE_PROPERTY, "ENUMERATION_TYPE");

                        for (int n = 0; n < restriction.getChildNodes().getLength(); ++n)
                        {
                           Node node = restriction.getChildNodes().item(n);
                           JsonObject schemaElementJson = new JsonObject();

                           schemaElementJson.addProperty(ModelerConstants.NAME_PROPERTY,
                                 node.getAttributes()
                                       .getNamedItem("value")
                                       .getNodeValue());
                           schemaElementJson.addProperty("typeName", "xsd:string");
                           childrenJson.add(node.getAttributes()
                                 .getNamedItem("value")
                                 .getNodeValue(), schemaElementJson);
                        }
                     }
                  }
               }
            }
         }
      }

      return modelJson;
   }

   /**
    * @param model
    * @param participant
    * @return
    */
   private boolean hasParentParticipant(ModelType model, IModelParticipant participant)
   {
      List<OrganizationType> parentOrgs = MBFacade.getParentOrganizations(model,
            participant);
      if (parentOrgs.size() > 0)
      {
         return true;
      }

      return false;
   }

   /**
    * @param parentJson
    * @param parent
    */
   private void addChildParticipantsJson(JsonObject parentJson, OrganizationType parent)
   {
      EList<ParticipantType> children = parent.getParticipant();
      if (children.size() > 0)
      {
         JsonArray childrenArray = new JsonArray();
         parentJson.add(ModelerConstants.CHILD_PARTICIPANTS_KEY, childrenArray);
         for (ParticipantType child : children)
         {
            IModelParticipant childParticipant = child.getParticipant();
            JsonObject childJson = new JsonObject();
            childrenArray.add(childJson);

            childJson.addProperty(ModelerConstants.ID_PROPERTY, childParticipant.getId());
            childJson.addProperty(ModelerConstants.NAME_PROPERTY,
                  childParticipant.getName());
            childJson.addProperty(ModelerConstants.OID_PROPERTY,
                  childParticipant.getElementOid());
            childJson.addProperty(ModelerConstants.UUID_PROPERTY,
                  eObjectUUIDMapper.getUUID(childParticipant));
            childJson.addProperty(ModelerConstants.PARENT_UUID_PROPERTY,
                  eObjectUUIDMapper.getUUID(parent));
            loadDescription(childJson, childParticipant);

            if (childParticipant instanceof OrganizationType)
            {
               childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                     ModelerConstants.ORGANIZATION_PARTICIPANT_TYPE_KEY);
               addChildParticipantsJson(childJson, (OrganizationType) childParticipant);
            }
            else if (childParticipant instanceof RoleType)
            {
               childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                     ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY);
            }
            else if (childParticipant instanceof ConditionalPerformerType)
            {
               childJson.addProperty(ModelerConstants.TYPE_PROPERTY,
                     ModelerConstants.CONDITIONAL_PERFORMER_PARTICIPANT_TYPE_KEY);
            }
         }
      }
   }

   /**
    * 
    * @param data
    * @return
    * @throws JSONException
    */
   private JsonObject loadData(ModelType model, DataType data)
   {
      JsonObject dataJson = new JsonObject();

      dataJson.addProperty(ModelerConstants.ID_PROPERTY, data.getId());
      dataJson.addProperty(ModelerConstants.NAME_PROPERTY, data.getName());
      dataJson.addProperty(ModelerConstants.UUID_PROPERTY,
            eObjectUUIDMapper.getUUID(data));
      loadDescription(dataJson, data);
      if (data.getType() != null)
      {
         dataJson.addProperty(TYPE_PROPERTY, data.getType().getId());
      }

      return dataJson;
   }

   /**
    * 
    * @param modelId
    * @param processId
    * @param postedData
    * @return
    */
   public String dropDataSymbol(String modelId, String processId,
         JsonObject dataSymbolJson)
   {
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            processId);
      EditingSession editSession = getEditingSession(model);

      synchronized (model)
      {
         editSession.beginEdit();
         editSession.endEdit();
      }

      return dataSymbolJson.toString();
   }

   // ======================== TODO Put in separate resource as we are not
   // going to share this with Eclipse =====================

   /**
	 *
	 */
   public String createWrapperProcess(String modelId, JsonObject json)
   {
      ModelType model = getModel(modelId);
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);

      // Create process definition

      System.out.println(json);

      JsonObject wizardParameterJson = (JsonObject) json.get(NEW_OBJECT_PROPERTY);
      JsonObject processDefinitionJson = (JsonObject) createProcessJson(modelId, json);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
            extractString(json, NEW_OBJECT_PROPERTY, ModelerConstants.ID_PROPERTY));
      LaneSymbol parentLaneSymbol = MBFacade.findLaneInProcess(processDefinition,
            ModelerConstants.DEF_LANE_ID);

      // Create Start Event

      StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM.createStartEventSymbol();
      startEventSymbol.setElementOid(++maxOid);

      startEventSymbol.setXPos(250);
      startEventSymbol.setYPos(50);

      processDefinition.getDiagram().get(0).getStartEventSymbols().add(startEventSymbol);
      parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

      // Request data

      // DataTypeType structuredDataType = AbstractElementBuilder.F_CWM
      // .createDataTypeType();
      //
      // structuredDataType.setId(id + "Request");
      // structuredDataType.setName(name + " Request");
      //
      // model.getDataType().add(structuredDataType);

      DataType data = newStructVariable(model).withIdAndName(
            MBFacade.createIdFromName(extractString(wizardParameterJson,
                  "requestParameterDataNameInput")),
            extractString(wizardParameterJson, "requestParameterDataNameInput"))
            .ofType(
                  /* Dummy */MBFacade.stripFullId(extractString(wizardParameterJson,
                        "serviceRequestParameterTypeId")))
            .build();

      model.getData().add(data);

      DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

      dataSymbol.setElementOid(++maxOid);
      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(100);
      dataSymbol.setYPos(50);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      // Create Request Transformation Activity

      ActivityType activity = newApplicationActivity(processDefinition).withIdAndName(
            MBFacade.createIdFromName(extractString(wizardParameterJson,
                  "requestTransformationActivityName")),
            extractString(wizardParameterJson, "requestTransformationActivityName"))
            .invokingApplication(
                  MBFacade.getApplication(modelId,
                        extractString(wizardParameterJson, "applicationId")))
            .build();

      // setDescription(activity,
      // "Invocation of wrapped application.");

      ActivitySymbolType activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setElementOid(++maxOid);

      activitySymbol.setXPos(200);
      activitySymbol.setYPos(100);
      activitySymbol.setWidth(180);
      activitySymbol.setHeight(50);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Request data

      data = newStructVariable(model).withIdAndName(
            MBFacade.createIdFromName("Service Request"), "Service Request")
            .ofType(
                  MBFacade.stripFullId(extractString(wizardParameterJson,
                        "serviceRequestParameterTypeId")))
            .build();

      model.getData().add(data);

      dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

      dataSymbol.setElementOid(++maxOid);
      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(100);
      dataSymbol.setYPos(150);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      // Create Application Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(
            MBFacade.createIdFromName(extractString(wizardParameterJson,
                  "serviceInvocationActivityName")),
            extractString(wizardParameterJson, "serviceInvocationActivityName"))
            .invokingApplication(
                  MBFacade.getApplication(modelId,
                        extractString(wizardParameterJson, "applicationId")))
            .build();

      // setDescription(activity,
      // "Invocation of wrapped application.");

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setElementOid(++maxOid);

      activitySymbol.setXPos(200);
      activitySymbol.setYPos(200);
      activitySymbol.setWidth(180);
      activitySymbol.setHeight(50);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Response data

      data = newStructVariable(model).withIdAndName(
            MBFacade.createIdFromName("Service Response"), "Service Response")
            .ofType(
                  MBFacade.stripFullId(extractString(wizardParameterJson,
                        "serviceResponseParameterTypeId")))
            .build();

      model.getData().add(data);

      dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

      dataSymbol.setElementOid(++maxOid);
      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(100);
      dataSymbol.setYPos(250);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      // Create Response Transformation Activity

      activity = newApplicationActivity(processDefinition).withIdAndName(
            MBFacade.createIdFromName(extractString(wizardParameterJson,
                  "responseTransformationActivityName")),
            extractString(wizardParameterJson, "responseTransformationActivityName"))
            .invokingApplication(
                  MBFacade.getApplication(modelId,
                        extractString(wizardParameterJson, "applicationId")))
            .build();

      // setDescription(activity,
      // "Invocation of wrapped application.");

      activitySymbol = AbstractElementBuilder.F_CWM.createActivitySymbolType();

      activitySymbol.setElementOid(++maxOid);

      activitySymbol.setXPos(200);
      activitySymbol.setYPos(300);
      activitySymbol.setWidth(180);
      activitySymbol.setHeight(50);
      activitySymbol.setActivity(activity);
      activity.getActivitySymbols().add(activitySymbol);

      processDefinition.getDiagram().get(0).getActivitySymbol().add(activitySymbol);
      parentLaneSymbol.getActivitySymbol().add(activitySymbol);

      // Create Response Data

      // structuredDataType = AbstractElementBuilder.F_CWM
      // .createDataTypeType();
      //
      // structuredDataType.setId(id + "Response");
      // structuredDataType.setName(name + " Response");
      //
      // model.getDataType().add(structuredDataType);

      data = newStructVariable(model).withIdAndName(
            MBFacade.createIdFromName(extractString(wizardParameterJson,
                  "responseParameterDataNameInput")),
            extractString(wizardParameterJson, "responseParameterDataNameInput"))
            .ofType(
                  /* Dummy */MBFacade.stripFullId(extractString(wizardParameterJson,
                        "serviceResponseParameterTypeId")))
            .build();

      dataSymbol = AbstractElementBuilder.F_CWM.createDataSymbolType();

      dataSymbol.setElementOid(++maxOid);
      dataSymbol.setData(data);
      processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
      data.getDataSymbols().add(dataSymbol);
      dataSymbol.setXPos(100);
      dataSymbol.setYPos(350);

      parentLaneSymbol.getDataSymbol().add(dataSymbol);

      // Create End Symbol

      EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM.createEndEventSymbol();
      endEventSymbol.setElementOid(++maxOid);

      endEventSymbol.setXPos(250);
      endEventSymbol.setYPos(400);

      processDefinition.getDiagram().get(0).getEndEventSymbols().add(endEventSymbol);

      parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);

      processDefinitionJson.addProperty("scope", "all");

      return processDefinitionJson.toString();
   }

   /**
	 *
	 */
   public String createDocumentation(String modelId, JsonObject json)
   {
      return createModelElementDocumentation(modelId, json);
   }

   /**
	 *
	 */
   public String createDocumentation(String modelId, String processId, JsonObject json)
   {
      return createModelElementDocumentation(modelId + "-" + processId, json);
   }

   /**
	 *
	 */
   private String createModelElementDocumentation(String pathPrefix, JsonObject json)
   {

      // TODO Make folder structure

      String fileName = pathPrefix + "-" + extractString(json, "id") + ".html";

      DocumentInfo documentInfo = DmsUtils.createDocumentInfo(fileName);
      documentInfo.setOwner(getServiceFactory().getWorkflowService()
            .getUser()
            .getAccount());
      documentInfo.setContentType(MimeTypesHelper.HTML.getType());
      Document document = getDocumentManagementService().getDocument(
            MODELING_DOCUMENTS_DIR + fileName);

      if (null == document)
      {
         document = getDocumentManagementService().createDocument(MODELING_DOCUMENTS_DIR,
               documentInfo,
               replaceProperties("", json, getTemplateContent("activity")).getBytes(),
               null);

         getDocumentManagementService().versionDocument(document.getId(), null);
      }

      JsonObject result = new JsonObject();

      result.addProperty("documentUrl", document.getId());

      return result.toString();
   }

   /**
    * 
    * @param elementType
    * @return
    */
   private String getTemplateContent(String elementType)
   {
      Document document = getDocumentManagementService().getDocument(
            MODEL_DOCUMENTATION_TEMPLATES_FOLDER + elementType + "-template.html");

      // Try extension ".htm"

      if (document == null)
      {
         getDocumentManagementService().getDocument(
               MODEL_DOCUMENTATION_TEMPLATES_FOLDER + elementType + "-template.html");
      }

      if (document != null)
      {
         return new String(getDocumentManagementService().retrieveDocumentContent(
               document.getId()));
      }

      return "";
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
   private QueryService getQueryService()
   {
      if (queryService == null)
      {
         queryService = getServiceFactory().getQueryService();
      }

      return queryService;
   }

   /**
	 */
   private String replaceProperties(String path, JsonObject json, String content)
   {
      if (path.length() > 0)
      {
         path += ".";
      }

      for (Map.Entry<String, JsonElement> entry : json.entrySet())
      {
         String key = entry.getKey();
         JsonElement value = entry.getValue();

         if (value != null)
         {
            if (value.isJsonObject())
            {
               content = replaceProperties(path + key, value.getAsJsonObject(), content);
            }
            else
            {
               content = content.replace("#{" + path + key + "}", value.toString());
            }
         }
      }

      return content;
   }

   public ProcessDefinitionType findProcessDefinition(ModelType model, String id)
   {
      return MBFacade.findProcessDefinition(model, id);
   }

   public ModelType findModel(String modelId)
   {
      return MBFacade.findModel(modelId);
   }

   /**
    * 
    * @param modelId
    * @return
    */
   public JsonArray validateModel(String modelId)
   {
      System.out.println("Validating model " + modelId);
      
      ModelType model = getModelManagementStrategy().getModels().get(modelId);

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

         String modelElemendId = null;

         if (modelElement != null && modelElement instanceof IIdentifiableModelElement)
         {
            modelElemendId = modelId + "/"
                  + ((IIdentifiableModelElement) modelElement).getId() + "/"
                  + ((IIdentifiableModelElement) modelElement).getElementOid();
         }

         issueJson.addProperty("modelElement", modelElemendId);
         issuesJson.add(issueJson);
      }

      return issuesJson;
   }
}