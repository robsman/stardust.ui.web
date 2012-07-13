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

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.ADMINISTRATOR_ROLE;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newApplicationActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newBpmModel;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newManualTrigger;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newPrimitiveVariable;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newProcessDefinition;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newRole;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newRouteActivity;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newStructVariable;
import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newSubProcessActivity;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractBoolean;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractInt;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractLong;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
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
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.model.xpdl.builder.activity.BpmApplicationActivityBuilder;
import org.eclipse.stardust.model.xpdl.builder.activity.BpmSubProcessActivityBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.session.EditingSession;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementHelper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.JcrConnectionManager;
import org.eclipse.stardust.model.xpdl.builder.utils.MBFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.PepperIconFactory;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelUtils;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivitySymbolType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.model.xpdl.carnot.ConditionalPerformerType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataSymbolType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DescriptionType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramModeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EndEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.GatewaySymbol;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.IModelParticipant;
import org.eclipse.stardust.model.xpdl.carnot.JoinSplitType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.OrganizationType;
import org.eclipse.stardust.model.xpdl.carnot.OrientationType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.RoleType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TransitionConnectionType;
import org.eclipse.stardust.model.xpdl.carnot.TransitionType;
import org.eclipse.stardust.model.xpdl.carnot.XmlTextNode;
import org.eclipse.stardust.model.xpdl.carnot.util.ActivityUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.model.xpdl.carnot.util.CarnotConstants;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.XpdlFactory;
import org.eclipse.stardust.modeling.repository.common.descriptors.ReplaceModelElementDescriptor;
import org.eclipse.stardust.ui.web.modeler.common.UnsavedModelsTracker;
import org.eclipse.stardust.ui.web.modeler.edit.EditingSessionManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelElementMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.w3c.dom.Node;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author Shrikant.Gangal, Marc.Gille
 *
 */
public class ModelService {
	private static final String MODELING_DOCUMENTS_DIR = "/process-modeling-documents/";
	private static final String NULL_VALUE = "null";
	private static final String DIRECTORY_MODE = "DIRECTORY_MODE";
	private static final String SINGLE_FILE_MODE = "SINGLE_FILE_MODE";
	public static final String TYPE_PROPERTY = "type";
	public static final String ATTRIBUTES_PROPERTY = "attributes";
	public static final String OID_PROPERTY = "oid";
	public static final String NEW_OBJECT_PROPERTY = "newObject";
	private static final String OLD_OBJECT_PROPERTY = "oldObject";
	public static final String X_PROPERTY = "x";
	public static final String Y_PROPERTY = "y";
	public static final String WIDTH_PROPERTY = "width";
	public static final String HEIGHT_PROPERTY = "height";
	private static final String DESCRIPTION_PROPERTY = "description";
	public static final String MODEL_ID_PROPERTY = "modelId";
	private static final String PARENT_SYMBOL_ID_PROPERTY = "parentSymbolId";
	public static final String ACTIVITIES_PROPERTY = "activities";
	public static final String GATEWAYS_PROPERTY = "gateways";
	public static final String EVENTS_PROPERTY = "events";
	private static final String ACTIVITY_KEY = "activity";
	private static final String ACTIVITY_SYMBOLS = "activitySymbols";
	private static final String GATEWAY_SYMBOLS = "gatewaySymbols";
	private static final String APPLICATION_TYPE_PROPERTY = "applicationType";
	private static final String ACCESS_POINTS_PROPERTY = "accessPoints";
	private static final String IN_ACCESS_POINT_KEY = "IN_ACCESS_POINT";
	private static final String OUT_ACCESS_POINT_KEY = "OUT_ACCESS_POINT";
	private static final String INOUT_ACCESS_POINT_KEY = "INOUT_ACCESS_POINT";
	private static final String ACCESS_POINT_TYPE_PROPERTY = "accessPointType";
	private static final String PRIMITIVE_ACCESS_POINT_KEY = "PRIMITIVE_ACCESS_POINT";
	private static final String DATA_STRUCTURE_ACCESS_POINT_KEY = "DATA_STRUCTURE_ACCESS_POINT";
	private static final String JAVA_CLASS_ACCESS_POINT_KEY = "JAVA_CLASS_ACCESS_POINT";
	private static final String ANY_ACCESS_POINT_KEY = "ANY_ACCESS_POINT";
	private static final String CONNECTION = "connection";
	private static final String DIRECTION_PROPERTY = "direction";
	private static final String CONTROL_FLOW_LITERAL = "controlFlow";
	private static final String DATA_FLOW_LITERAL = "dataFlow";
	private static final String FROM_ANCHOR_POINT_ORIENTATION_PROPERTY = "fromAnchorPointOrientation";
	private static final String TO_ANCHOR_POINT_ORIENTATION_PROPERTY = "toAnchorPointOrientation";
	private static final int UNDEFINED_ORIENTATION_KEY = -1;
	private static final int NORTH_KEY = 0;
	private static final int EAST_KEY = 1;
	private static final int SOUTH_KEY = 2;
	private static final int WEST_KEY = 3;
	private static final String GATEWAY = "gateway";
	private static final String GATEWAY_ACTIVITY = "Gateway";
	private static final String GATEWAY_TYPE_PROPERTY = "gatewayType";
	private static final String AND_GATEWAY_TYPE = "and";
	private static final String XOR_GATEWAY_TYPE = "xor";
	private static final String OR_GATEWAY_TYPE = "or";
	public static final String EVENT_KEY = "event";
	private static final String EVENT_SYMBOLS = "eventSymbols";
	public static final String EVENT_TYPE_PROPERTY = "eventType";
	public static final String START_EVENT = "startEvent";
	public static final String STOP_EVENT = "stopEvent";
	private static final String DATA = "data";
	private static final String DATA_SYMBOLS = "dataSymbols";
	private static final String STRUCTURED_DATA_TYPE_FULL_ID = "structuredDataTypeFullId";
	private static final String TYPE_DECLARATION_PROPERTY = "typeDeclaration";
	private static final String CONNECTIONS_PROPERTY = "connections";
	public static final String CONTROL_FLOWS_PROPERTY = "controlFlows";
	public static final String DATA_FLOWS_PROPERTY = "dataFlows";
	private static final String CONDITION_EXPRESSION_PROPERTY = "conditionExpression";
	private static final String IN_DATA_MAPPING_PROPERTY = "inDataMapping";
	private static final String OUT_DATA_MAPPING_PROPERTY = "outDataMapping";
	private static final String DATA_PATH_PROPERTY = "dataPath";
	private static final String APPLICATION_PATH_PROPERTY = "applicationPath";
	private static final String OTHERWISE_PROPERTY = "otherwise";
	private static final String CONDITION_KEY = "CONDITION";
	private static final String OTHERWISE_KEY = "OTHERWISE";
	private static final String POOL_SYMBOLS = "poolSymbols";
	private static final String LANE_SYMBOLS = "laneSymbols";
	private static final String FROM_MODEL_ELEMENT_OID = "fromModelElementOid";
	private static final String FROM_MODEL_ELEMENT_TYPE = "fromModelElementType";
	private static final String TO_MODEL_ELEMENT_OID = "toModelElementOid";
	private static final String TO_MODEL_ELEMENT_TYPE = "toModelElementType";
	private static final String WEB_SERVICE_APPLICATION_TYPE_ID = "webservice";
	private static final String MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID = "messageTransformationBean";
	private static final String CAMEL_APPLICATION_TYPE_ID = "camelBean";
	private static final String MAIL_APPLICATION_TYPE_ID = "mailBean";
	private static final String INTERACTIVE_APPLICATION_TYPE_KEY = "interactive";
	private static final String CONTEXTS_PROPERTY = "contexts";
	private static final String JSF_CONTEXT_TYPE_KEY = "jsf";
	private static final String EXTERNAL_WEB_APP_CONTEXT_TYPE_KEY = "externalWebApp";
	public static final int POOL_LANE_MARGIN = 5;
	public static final int POOL_SWIMLANE_TOP_BOX_HEIGHT = 20;

	/* Half the size of the review why this adjustment is needed start event symbol used in Pepper
	 * TODO - may need to be handled on the client side down the line. */
	public static final int START_END_SYMBOL_LEFT_OFFSET = 12;
	private static final String MODEL_DOCUMENTATION_TEMPLATES_FOLDER = "/documents/templates/modeling/";

	private ServiceFactory serviceFactory;
	private DocumentManagementService documentManagementService;
	private UserService userService;
	private QueryService queryService;
	private ModelManagementStrategy modelManagementStrategy;

	// TODO For testing only >>>

	private Stack<JsonObject> commandLog = new Stack<JsonObject>();
	private int commandLogIndex = -1;

	/**
	 *
	 * @param commandJson
	 */
	private void pushCommand(JsonObject commandJson) {
		commandLogIndex++;

		while (commandLog.size() > commandLogIndex && commandLog.size() > 0) {
			commandLog.pop();
		}

		commandLog.push(commandJson);
	}

	/**
	 *
	 * @return
	 */
	private JsonObject undoCommand() {
		if (hasMoreToUndo()) {
			commandLogIndex--;
			return commandLog.get(commandLogIndex + 1);
		} else {
			return null;
		}
	}

	/**
	 *
	 * @return
	 */
	private JsonObject redoCommand() {
		if (hasMoreToRedo()) {
			commandLogIndex++;
			return commandLog.get(commandLogIndex);
		} else {
			return null;
		}
	}

	private boolean hasMoreToUndo() {
		if (commandLog.size() > 0 && commandLogIndex >= 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean hasMoreToRedo() {
		if (commandLog.size() > 0 && commandLogIndex + 1 < commandLog.size()) {
			return true;
		} else {
			return false;
		}
	}

	// TODO For testing only <<<

	// Modeling Session Management

	private Map<String, User> prospectUsers = new HashMap<String, User>();
	private Map<String, User> participantUsers = new HashMap<String, User>();
	private Map<String, String> imageUris = new HashMap<String, String>();

	/**
	 * Contains all loaded and newly created
	 * getModelManagementStrategy().getModels().
	 */
	private JsonObject modelsJson = new JsonObject();

	@Resource
	private EditingSessionManager editingSessionManager;

	public ServiceFactory getServiceFactory() {
		if (serviceFactory == null) {
			serviceFactory = ServiceFactoryLocator.get("motu", "motu");
		}

		return serviceFactory;
	}

	/**
	 *
	 * @return
	 */
	public ModelManagementStrategy getModelManagementStrategy() {
		return modelManagementStrategy;
	}

	/**
	 *
	 * @param modelManagementStrategy
	 */
	public void setModelManagementStrategy(
			ModelManagementStrategy modelManagementStrategy) {
		this.modelManagementStrategy = modelManagementStrategy;
		ModelManagementHelper.getInstance().setModelManagementStrategy(modelManagementStrategy);
	}

	/**
	 *
	 * @param attrs
	 * @param attrType
	 */
	private void removeIfExists(List<AttributeType> attrs,
			AttributeType attrType) {
		Iterator<AttributeType> iter = attrs.iterator();
		while (iter.hasNext()) {
			if (iter.next().getName().equals(attrType.getName())) {
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
	private void storeAttributes(JsonObject json,
			IIdentifiableModelElement element) {
		if (!json.has(ATTRIBUTES_PROPERTY)) {
			return;
		}

		JsonObject attributes = json.getAsJsonObject(ATTRIBUTES_PROPERTY);

		if (attributes != null) {
			for (Map.Entry<String, ?> entry : attributes.entrySet()) {
				String key = entry.getKey();
				JsonElement value = attributes.get(key);

				System.out.println("Storing attribute " + key + " " + value);

				if (value instanceof JsonObject) {
				} else {
					AttributeUtil.setAttribute(element, key, value.toString());
				}
			}
		}
	}

	/**
	 *
	 * @param element
	 * @param json
	 * @throws JSONException
	 */
	private void loadAttributes(IIdentifiableModelElement element,
			JsonObject json) {
		JsonObject attributes;

		if (!json.has(ATTRIBUTES_PROPERTY)) {
			json.add(ATTRIBUTES_PROPERTY, attributes = new JsonObject());
		} else {
			attributes = json.getAsJsonObject(ATTRIBUTES_PROPERTY);
		}

		for (AttributeType attribute : element.getAttribute()) {
			attributes.addProperty(attribute.getName(), attribute.getValue());
		}
	}

	/**
	 *
	 * @param model
	 * @param processDefinition
	 * @return
	 */
	private EditingSession getEditingSession(ModelType model) {
	   return editingSessionManager.getSession(model);
	}

	/**
	 *
	 * @param postedData
	 * @return
	 */
	public String requestJoin(JsonObject postedData) {
		System.out.println("Account: " + postedData.getAsJsonObject(NEW_OBJECT_PROPERTY).get("account").getAsString());

		requestJoin(postedData.getAsJsonObject(NEW_OBJECT_PROPERTY).get("account").getAsString());

		return postedData.toString();
	}

	/**
	 *
	 * @param postedData
	 * @return
	 */
	public void requestJoin(String account) {
		User prospect = getUserService().getUser(account);

		prospectUsers.put(prospect.getAccount(), prospect);
		//imageUris.put("prospect.getAccount()", );
	}

	/**
	 *
	 * @param postedData
	 * @return
	 */
	public String confirmJoin(JsonObject postedData) {
		String account = postedData.getAsJsonObject(OLD_OBJECT_PROPERTY).get("account").getAsString();
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
	 * @param id
	 * @param postedData
	 * @return
	 */
	public String createModel(String id, JsonObject postedData) {
		JsonObject newObjectJson = postedData
				.getAsJsonObject(NEW_OBJECT_PROPERTY);

		ModelType model = newBpmModel().withIdAndName(
				newObjectJson.get(ModelerConstants.ID_PROPERTY).getAsString(),
				newObjectJson.get(ModelerConstants.NAME_PROPERTY).getAsString()).build();
		long maxOid = XpdlModelUtils.getMaxUsedOid(model);
		AttributeUtil.setAttribute(model, PredefinedConstants.VERSION_ATT, "1");

		RoleType admin = AbstractElementBuilder.F_CWM.createRoleType();
		admin.setName(ADMINISTRATOR_ROLE);
		admin.setId(ADMINISTRATOR_ROLE);
		long adminOid = ++maxOid;
		admin.setElementOid(adminOid);

		model.getRole().add(admin);

		getModelManagementStrategy().getModels().put(model.getId(), model);

		newObjectJson.addProperty(ModelerConstants.ID_PROPERTY, model.getId());
		newObjectJson.addProperty(ModelerConstants.NAME_PROPERTY, model.getName());

		return postedData.toString();
	}

	/**
	 *
	 * @param modelId
	 * @param commandJson
	 * @return
	 */
	public String deleteModel(String modelId, JsonObject commandJson) {
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
	public String deleteProcess(String modelId, String processId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);

		if (null == model) {
			return null;
		} else {
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
	public String deleteStructuredDataType(String modelId,
			String structuredDataTypeId, JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		TypeDeclarationType structuredDataType = MBFacade.findStructuredDataType(model,
				structuredDataTypeId);
		synchronized (model) {
			model.getTypeDeclarations().getTypeDeclaration()
					.remove(structuredDataType);
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
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		IModelParticipant modelParticipantInfo = MBFacade.findParticipant(model,
				participantId);
		synchronized (model) {
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
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ApplicationType application = MBFacade.findApplication(model, applicationId);

		synchronized (model) {
			model.getApplication().remove(application);
		}
		return commandJson.toString();
	}

	/**
	 * Retrieves all the stored models and returns a json array of references of
	 * these getModelManagementStrategy().getModels().
	 *
	 * @return
	 */
	public String getAllModels() {
		try {
			// Reload only if model map was empty
			// TODO Review
			getModelManagementStrategy().getModels(getModelManagementStrategy().getModels().isEmpty());

			// Refresh JSON
			// TODO Smarter caching

			modelsJson = new JsonObject();

			for (ModelType model : getModelManagementStrategy().getModels()
					.values()) {
				modelsJson.add(model.getId(), loadModelOutline(model));
			}

			return modelsJson.toString();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param modelId
	 * @param commandJson
	 * @return
	 */
	public String renameModel(String modelId, JsonObject commandJson) {
		JsonObject newObjectJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		String newName = newObjectJson.get(ModelerConstants.NAME_PROPERTY).getAsString();
		newObjectJson.addProperty(ModelerConstants.ID_PROPERTY, MBFacade.createIdFromName(newName));

		ModelType model = getModelManagementStrategy().getModels().get(modelId);

		getModelManagementStrategy().deleteModel(model);

		String oldName = model.getName();
		model.setName(newName);
		model.setId(newObjectJson.get(ModelerConstants.ID_PROPERTY).getAsString());
		// TODO Use corresponding modeler function for auto ID generation

		getModelManagementStrategy().getModels().put(model.getId(), model);
		getModelManagementStrategy().saveModel(model);

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
	public String renameProcess(String modelId, String processId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);

		// TODO Use corresponding modeler function for auto ID generation
		processDefinition.setId(MBFacade.createIdFromName(extractString(commandJson,
				NEW_OBJECT_PROPERTY, ModelerConstants.NAME_PROPERTY)));
		processDefinition.setName(extractString(commandJson,
				NEW_OBJECT_PROPERTY, ModelerConstants.NAME_PROPERTY));

		commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY).addProperty(
				ModelerConstants.ID_PROPERTY, processDefinition.getId());

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
	public String renameApplication(String modelId, String applicationId,
			JsonObject commandJson) {
		JsonObject newObject = commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ApplicationType application = MBFacade.findApplication(model, applicationId);

		// TODO Use corresponding modeler function for auto ID generation
		application.setId(MBFacade.createIdFromName(newObject.get(ModelerConstants.NAME_PROPERTY)
				.getAsString()));
		application.setName(newObject.get(ModelerConstants.NAME_PROPERTY).getAsString());

		newObject.addProperty(ModelerConstants.ID_PROPERTY, application.getId());

		return commandJson.toString();
	}

	/**
	 * @param modelId
	 * @param participantId
	 * @param newName
	 * @param commandJson
	 * @return
	 */
	public String renameParticipant(String modelId, String participantId,
			JsonObject commandJson) {
		JsonObject newObject = commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		IModelParticipant participant = MBFacade.findParticipant(model, participantId);

		// TODO Use corresponding modeler function for auto ID generation
		participant.setId(MBFacade.createIdFromName(newObject.get(ModelerConstants.NAME_PROPERTY)
				.getAsString()));
		participant.setName(newObject.get(ModelerConstants.NAME_PROPERTY).getAsString());

		newObject.addProperty(ModelerConstants.ID_PROPERTY, participant.getId());

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
	public String renameStructuredDataType(String modelId,
			String structuredDataTypeId, JsonObject commandJson) {
		JsonObject newObject = commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		TypeDeclarationType structuredDataType = MBFacade.findStructuredDataType(model,
				structuredDataTypeId);

		// TODO Use corresponding modeler function for auto ID generation
		structuredDataType.setId(MBFacade.createIdFromName(newObject.get(ModelerConstants.NAME_PROPERTY)
				.getAsString()));
		structuredDataType.setName(newObject.get(ModelerConstants.NAME_PROPERTY).getAsString());

		newObject.addProperty(ModelerConstants.ID_PROPERTY, structuredDataType.getId());

		return commandJson.toString();
	}

	/**
	 *
	 * @param httpRequest
	 * @param modelId
	 * @return
	 */
	public void saveModel(String modelId) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);

		getModelManagementStrategy().saveModel(model);
	}

	/**
	 *
	 */
	public void saveAllModels() {
	   //TODO
	   //Temporarily commenting selective save as not all changes have moved to change protocol yet.
	   // After that this can be uncommented
/*	   Set<String> changedModels = UnsavedModelsTracker.getInstance().getUnsavedModels();
	   for (String modelId : changedModels) {
	        ModelType model = getModelManagementStrategy().getModels().get(modelId);
	        if (null != model) {
	            getModelManagementStrategy().saveModel(model);
	        }
	   }

	   //Clear the unsaved models' list.
	   UnsavedModelsTracker.getInstance().notifyAllModelsSaved();*/

	 //TODO
	   //Temporarily saving all models as not all changes have moved to change protocol yet.
	   //After that happens this code can be deleted.
	   Collection<ModelType> models = getModelManagementStrategy().getModels().values();
	   for (ModelType model : models) {
	      getModelManagementStrategy().saveModel(model);
	   }
	}

	/**
	 *
	 * @param id
	 * @return
	 */
	public ModelType getModel(String id) {
		return getModelManagementStrategy().getModels().get(id);
	}

	/**
	 *
	 * @param modelId
	 * @param id
	 * @param postedData
	 * @return
	 */
	public String createProcess(String modelId, JsonObject postedData) {
		return createProcessJson(modelId, postedData).toString();
	}

	/**
	 *
	 * @param modelId
	 * @param id
	 * @param postedData
	 * @return
	 */
	public JsonObject createProcessJson(String modelId, JsonObject postedData) {
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
	 * @param postedData
	 * @return
	 */
	public String createActivity(String modelId, String processId,
			JsonObject commandJson) {
		JsonObject activitySymbolJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);

		String activityType = extractString(activitySymbolJson,
              ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ACTIVITY_TYPE);
		String participantFullID = extractString(activitySymbolJson, ModelerConstants.MODEL_ELEMENT_PROPERTY,
              ModelerConstants.PARTICIPANT_FULL_ID);
		String modelID = extractString(activitySymbolJson,
              ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY);
		String modelName = extractString(activitySymbolJson,
              ModelerConstants.MODEL_ELEMENT_PROPERTY,
              ModelerConstants.NAME_PROPERTY);
		String applicationFullID = extractString(
              activitySymbolJson,
              ModelerConstants.MODEL_ELEMENT_PROPERTY,
              ModelerConstants.APPLICATION_FULL_ID_PROPERTY);
		String subProcessID = extractString(
              activitySymbolJson,
              ModelerConstants.MODEL_ELEMENT_PROPERTY,
              ModelerConstants.SUBPROCESS_ID);
		String parentSymbolID = extractString(activitySymbolJson, PARENT_SYMBOL_ID_PROPERTY);
		int xProperty = extractInt(activitySymbolJson, X_PROPERTY);
		int yProperty = extractInt(activitySymbolJson, Y_PROPERTY);
		int widthProperty = extractInt(activitySymbolJson,
              WIDTH_PROPERTY);
	    int heightProperty = extractInt(activitySymbolJson,
	          HEIGHT_PROPERTY);


		synchronized (model)
		{
			long maxOid = XpdlModelUtils.getMaxUsedOid(model);

			editSession.beginEdit();

			ActivityType activity = MBFacade.createActivity(modelId, processDefinition, activityType,
               participantFullID, modelID, modelName, applicationFullID, subProcessID, maxOid);

			setDescription(activity,
					activitySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));

			ActivitySymbolType activitySymbol = MBFacade.createActivitySymbol(processDefinition,
               parentSymbolID, xProperty, yProperty, widthProperty, heightProperty,
               maxOid, activity);

	        activitySymbolJson.addProperty(OID_PROPERTY,
                   activitySymbol.getElementOid());
			editSession.endEdit();
		}

		return commandJson.toString();
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param activityId
	 * @param postedData
	 * @return
	 */
	public String renameActivity(String modelId, String processId,
			String activityId, JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		ActivityType activity = MBFacade.findActivity(processDefinition, activityId);
		EditingSession editingSession = getEditingSession(model);

		synchronized (model) {
			editingSession.beginEdit();

			JsonObject newNameJson = commandJson
					.getAsJsonObject(NEW_OBJECT_PROPERTY);

			// TODO Auto-generate ID
			activity.setId(extractString(newNameJson, ModelerConstants.NAME_PROPERTY));
			activity.setName(extractString(newNameJson, ModelerConstants.NAME_PROPERTY));
			newNameJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());

			// TODO For testing

			pushCommand(commandJson);

			editingSession.endEdit();
		}

		return commandJson.toString();
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param activityId
	 * @param postedData
	 * @return
	 * 
	 * @deprecated
	 */
	public String updateActivity(String modelId, String processId,
			String activityId, JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		ActivitySymbolType activitySymbol = MBFacade.findActivity(processDefinition,
				activityId).getActivitySymbols().get(0);
		LaneSymbol laneSymbol = MBFacade.findLaneContainingActivitySymbol(
				processDefinition.getDiagram().get(0),
				activitySymbol.getElementOid());
		EditingSession editingSession = getEditingSession(model);

		synchronized (model) {
			editingSession.beginEdit();

			if (extractString(commandJson, TYPE_PROPERTY).equals(
					"UPDATE_GEOMETRY_COMMAND")) {
				JsonObject newGeometryJson = commandJson
						.getAsJsonObject(NEW_OBJECT_PROPERTY);

				activitySymbol.setXPos(extractInt(newGeometryJson, X_PROPERTY)
						- laneSymbol.getXPos());
				activitySymbol.setYPos(extractInt(newGeometryJson, Y_PROPERTY)
						- laneSymbol.getYPos());
				if (newGeometryJson.has(WIDTH_PROPERTY)) {
					activitySymbol.setWidth(extractInt(newGeometryJson,
							WIDTH_PROPERTY));
				}
				if (newGeometryJson.has(HEIGHT_PROPERTY)) {
					activitySymbol.setHeight(extractInt(newGeometryJson,
							HEIGHT_PROPERTY));
				}
				pushCommand(commandJson);
			} else {
				JsonObject activitySymbolJson = commandJson;

				updateActivity(activitySymbol, laneSymbol, activitySymbolJson);
			}

			editingSession.endEdit();
		}

		return commandJson.toString();
	}

	/**
	 *
	 * @param activitySymbol
	 * @param activitySymbolJson
	 * @return
	 * @throws JSONException
	 * 
	 * @deprecated
	 */
	private JsonObject updateActivity(ActivitySymbolType activitySymbol,
			LaneSymbol laneSymbol, JsonObject activitySymbolJson) {
		ActivityType activity = activitySymbol.getActivity();
		JsonObject activityJson = activitySymbolJson
				.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);

		// Store activity data

		activity.setName(extractString(activityJson, ModelerConstants.NAME_PROPERTY));
		setDescription(activity, activityJson);
		storeAttributes(activityJson, activity);

		if (ModelerConstants.MANUAL_ACTIVITY.equals(extractString(activityJson, ModelerConstants.ACTIVITY_TYPE))) {
			activity.setImplementation(ActivityImplementationType.MANUAL_LITERAL);
		} else if (ModelerConstants.SUBPROCESS_ACTIVITY.equals(extractString(activityJson,
				ModelerConstants.ACTIVITY_TYPE))) {
			activity.setImplementation(ActivityImplementationType.SUBPROCESS_LITERAL);

			String subprocessFullId = extractString(activityJson, ModelerConstants.SUBPROCESS_ID);


			ProcessDefinitionType subProcessDefinition = MBFacade.findProcessDefinition(
               getModel(MBFacade.getModelId(subprocessFullId)),
               MBFacade.stripFullId(subprocessFullId));
         ModelType subProcessModel = ModelUtils.findContainingModel(subProcessDefinition);
         BpmSubProcessActivityBuilder subProcessActivity = newSubProcessActivity(ModelUtils.findContainingProcess(activity));
         subProcessActivity.setActivity(activity);
         subProcessActivity.setSubProcessModel(subProcessModel);
         subProcessActivity.invokingProcess(subProcessDefinition);


		} else if (ModelerConstants.APPLICATION_ACTIVITY.equals(extractString(activityJson,
				ModelerConstants.ACTIVITY_TYPE))) {
			activity.setImplementation(ActivityImplementationType.APPLICATION_LITERAL);

			String applicationFullId = extractString(activityJson,
					ModelerConstants.APPLICATION_FULL_ID_PROPERTY);

			System.out.println("Updating application to "
					+ MBFacade.getApplication(MBFacade.getModelId(applicationFullId),
							MBFacade.stripFullId(applicationFullId)));

			ApplicationType application = MBFacade.getApplication(
               MBFacade.getModelId(applicationFullId),
               MBFacade.stripFullId(applicationFullId));

         BpmApplicationActivityBuilder applicationActivity = newApplicationActivity(ModelUtils.findContainingProcess(activity));
         applicationActivity.setActivity(activity);
         ModelType applicationModel = ModelUtils.findContainingModel(application);
         applicationActivity.setApplicationModel(applicationModel);
         applicationActivity.invokingApplication(application);
		}

		// Store symbol data

		activitySymbol.setXPos(extractInt(activitySymbolJson, X_PROPERTY)
				- laneSymbol.getXPos());
		activitySymbol.setYPos(extractInt(activitySymbolJson, Y_PROPERTY)
				- laneSymbol.getYPos());
		activitySymbol.setWidth(extractInt(activitySymbolJson, WIDTH_PROPERTY));
		activitySymbol
				.setHeight(extractInt(activitySymbolJson, HEIGHT_PROPERTY));

		return activitySymbolJson;
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param activityId
	 * @return
	 */
	public String deleteActivity(String modelId, String processId,
			String activityId, JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editingSession = getEditingSession(model);
		ActivityType activity = MBFacade.findActivity(processDefinition, activityId);
		ActivitySymbolType activitySymbol = activity.getActivitySymbols()
				.get(0);

		synchronized (model) {
			editingSession.beginEdit();

			processDefinition.getActivity().remove(activity);
			processDefinition.getDiagram().get(0).getActivitySymbol()
					.remove(activitySymbol);

			LaneSymbol parentLaneSymbol = MBFacade.findLaneContainingActivitySymbol(
					processDefinition, activitySymbol);

			parentLaneSymbol.getActivitySymbol().remove(activitySymbol);

			// TODO For testing

			pushCommand(commandJson);

			editingSession.endEdit();
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
	public String createGateway(String modelId, String processId,
			JsonObject commandJson) {
		JsonObject gatewaySymbolJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editingSession = getEditingSession(model);

		synchronized (model) {
			long maxOid = XpdlModelUtils.getMaxUsedOid(model);

			editingSession.beginEdit();

			ActivityType gateway = null;

			// TODO Should be Route
			gateway = newManualActivity(processDefinition)
					.withIdAndName(
							extractString(gatewaySymbolJson,
									ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.ID_PROPERTY),
							extractString(gatewaySymbolJson,
									ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.NAME_PROPERTY))
					.havingDefaultPerformer(ADMINISTRATOR_ROLE).build();

			processDefinition.getActivity().add(gateway);

			ActivitySymbolType gatewaySymbol = AbstractElementBuilder.F_CWM
					.createActivitySymbolType();
			LaneSymbol parentLaneSymbol = MBFacade.findLaneInProcess(processDefinition,
					extractString(gatewaySymbolJson, PARENT_SYMBOL_ID_PROPERTY));

			gatewaySymbol.setElementOid(++maxOid);

			gatewaySymbolJson.addProperty(OID_PROPERTY,
					gatewaySymbol.getElementOid());

			gatewaySymbol.setXPos(extractInt(gatewaySymbolJson, X_PROPERTY)
					- parentLaneSymbol.getXPos());
			gatewaySymbol.setYPos(extractInt(gatewaySymbolJson, Y_PROPERTY)
					- parentLaneSymbol.getYPos());
			gatewaySymbol.setActivity(gateway);

			gateway.getActivitySymbols().add(gatewaySymbol);
			processDefinition.getDiagram().get(0).getActivitySymbol()
					.add(gatewaySymbol);
			parentLaneSymbol.getActivitySymbol().add(gatewaySymbol);

			editingSession.endEdit();
		}

		return commandJson.toString();
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param gatewayId
	 * @param postedData
	 * @return
	 * 
	 * @deprecated
	 */
	public String updateGateway(String modelId, String processId,
			String gatewayId, JsonObject commandJson) {
		JsonObject gatewaySymbolJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		ActivityType gateway = MBFacade.findActivity(processDefinition, gatewayId);
		ActivitySymbolType gatewaySymbol = gateway.getActivitySymbols().get(0);
		LaneSymbol laneSymbol = MBFacade.findLaneContainingActivitySymbol(
				processDefinition.getDiagram().get(0),
				gatewaySymbol.getElementOid());
		EditingSession editingSession = getEditingSession(model);

		synchronized (model) {
			editingSession.beginEdit();

			gatewaySymbolJson = updateGateway(gatewaySymbol, laneSymbol,
					gatewaySymbolJson);

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
			LaneSymbol laneSymbol, JsonObject gatewaySymbolJson) {
		ActivityType gateway = gatewaySymbol.getActivity();

		if (gatewaySymbolJson.has(ModelerConstants.MODEL_ELEMENT_PROPERTY)) {
			gateway.setName(extractString(gatewaySymbolJson,
					ModelerConstants.MODEL_ELEMENT_PROPERTY, ModelerConstants.NAME_PROPERTY));
			setDescription(gateway,
					gatewaySymbolJson.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY));
		}

		gatewaySymbol.setXPos(extractInt(gatewaySymbolJson, X_PROPERTY)
				- laneSymbol.getXPos());
		gatewaySymbol.setYPos(extractInt(gatewaySymbolJson, Y_PROPERTY)
				- laneSymbol.getYPos());

		if (gatewaySymbolJson.has(WIDTH_PROPERTY)) {
			gatewaySymbol
					.setWidth(extractInt(gatewaySymbolJson, WIDTH_PROPERTY));
		}
		if (gatewaySymbolJson.has(HEIGHT_PROPERTY)) {
			gatewaySymbol.setHeight(extractInt(gatewaySymbolJson,
					HEIGHT_PROPERTY));
		}

		return gatewaySymbolJson;
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param gatewayId
	 * @return
	 */
	public String deleteGateway(String modelId, String processId,
			String gatewayId) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editingSession = getEditingSession(model);
		ActivityType gateway = MBFacade.findActivity(processDefinition, gatewayId);
		ActivitySymbolType gatewaySymbol = gateway.getActivitySymbols().get(0);

		synchronized (model) {
			editingSession.beginEdit();

			processDefinition.getActivity().remove(gateway);
			processDefinition.getDiagram().get(0).getActivitySymbol()
					.remove(gatewaySymbol);

			LaneSymbol parentLaneSymbol = MBFacade.findLaneContainingActivitySymbol(
					processDefinition, gatewaySymbol);

			parentLaneSymbol.getActivitySymbol().remove(gatewaySymbol);

			editingSession.endEdit();
		}

		return new JsonObject().toString();
	}

	/**
	 * @param element
	 * @param description
	 * @throws JSONException
	 */
	public static void setDescription(IIdentifiableModelElement element,
			JsonObject json) {
		String description = null;

		if (json.has(DESCRIPTION_PROPERTY)) {
			description = extractString(json, DESCRIPTION_PROPERTY);
		}

		if (StringUtils.isNotEmpty(description)) {
			DescriptionType dt = AbstractElementBuilder.F_CWM
					.createDescriptionType();
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
			IIdentifiableModelElement element) {
		if (null != element.getDescription()) {
			modelElementJson.addProperty(DESCRIPTION_PROPERTY, (String) element
					.getDescription().getMixed().get(0).getValue());
		} else {
			modelElementJson.addProperty(DESCRIPTION_PROPERTY, "");
		}
	}

	/**
	 *
	 */
	private RoleType getRoleFromModel(String modelId, String roleId) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		List<RoleType> roles = model.getRole();
		for (RoleType role : roles) {
			if (roleId.equals(role.getId())) {
				return role;
			}
		}

		return null;
	}

	/**
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public String createConnection(String modelId, String processId,
			JsonObject commandJson) {
		JsonObject connectionJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);
		long maxOid = XpdlModelUtils.getMaxUsedOid(model);

		synchronized (model) {
			editSession.beginEdit();

			if (ACTIVITY_KEY.equals(extractString(connectionJson,
					FROM_MODEL_ELEMENT_TYPE))
					|| GATEWAY.equals(extractString(connectionJson,
							FROM_MODEL_ELEMENT_TYPE))) {
				if (ACTIVITY_KEY.equals(extractString(connectionJson,
						TO_MODEL_ELEMENT_TYPE))
						|| GATEWAY.equals(extractString(connectionJson,
								TO_MODEL_ELEMENT_TYPE))) {
					createControlFlowConnection(
							connectionJson,
							processDefinition,
							MBFacade.findActivitySymbol(
									processDefinition.getDiagram().get(0),
									extractLong(connectionJson,
											FROM_MODEL_ELEMENT_OID)),
							MBFacade.findActivitySymbol(
									processDefinition.getDiagram().get(0),
									extractLong(connectionJson,
											TO_MODEL_ELEMENT_OID)), maxOid);
				} else if (EVENT_KEY.equals(extractString(connectionJson,
						TO_MODEL_ELEMENT_TYPE))) {
					try {
						StartEventSymbol startEventSymbol = MBFacade.findStartEventSymbol(
								processDefinition.getDiagram().get(0),
								extractLong(connectionJson,
										TO_MODEL_ELEMENT_OID));

						createControlFlowConnection(
								connectionJson,
								processDefinition,
								startEventSymbol,
								MBFacade.findActivitySymbol(
										processDefinition.getDiagram().get(0),
										extractLong(connectionJson,
												FROM_MODEL_ELEMENT_OID)),
								maxOid);
					} catch (ObjectNotFoundException x) {
						EndEventSymbol endEventSymbol = MBFacade.findEndEventSymbol(
								processDefinition.getDiagram().get(0),
								extractLong(connectionJson,
										TO_MODEL_ELEMENT_OID));
						createControlFlowConnection(
								connectionJson,
								processDefinition,
								MBFacade.findActivitySymbol(
										processDefinition.getDiagram().get(0),
										extractLong(connectionJson,
												FROM_MODEL_ELEMENT_OID)),
								endEventSymbol, maxOid);
					}
				} else if (DATA.equals(extractString(connectionJson,
						TO_MODEL_ELEMENT_TYPE))) {
					createDataFlowConnection(
							connectionJson,
							processDefinition,
							MBFacade.findActivitySymbol(
									processDefinition.getDiagram().get(0),
									extractLong(connectionJson,
											FROM_MODEL_ELEMENT_OID)),
							MBFacade.findDataSymbol(
									processDefinition.getDiagram().get(0),
									extractLong(connectionJson,
											TO_MODEL_ELEMENT_OID)), maxOid);
				} else {
					throw new IllegalArgumentException(
							"Unknown target symbol type "
									+ extractString(connectionJson,
											TO_MODEL_ELEMENT_TYPE)
									+ " for connection.");
				}
			} else if (EVENT_KEY.equals(extractString(connectionJson,
					FROM_MODEL_ELEMENT_TYPE))) {
				if (ACTIVITY_KEY.equals(extractString(connectionJson,
						TO_MODEL_ELEMENT_TYPE))) {
					try {
						StartEventSymbol startEventSymbol = MBFacade.findStartEventSymbol(
								processDefinition.getDiagram().get(0),
								extractLong(connectionJson,
										FROM_MODEL_ELEMENT_OID));

						createControlFlowConnection(
								connectionJson,
								processDefinition,
								startEventSymbol,
								MBFacade.findActivitySymbol(
										processDefinition.getDiagram().get(0),
										extractLong(connectionJson,
												TO_MODEL_ELEMENT_OID)), maxOid);
					} catch (ObjectNotFoundException x) {
						EndEventSymbol endEventSymbol = MBFacade.findEndEventSymbol(
								processDefinition.getDiagram().get(0),
								extractLong(connectionJson,
										FROM_MODEL_ELEMENT_OID));
						createControlFlowConnection(
								connectionJson,
								processDefinition,
								MBFacade.findActivitySymbol(
										processDefinition.getDiagram().get(0),
										extractLong(connectionJson,
												TO_MODEL_ELEMENT_OID)),
								endEventSymbol, maxOid);
					}
				} else {
					throw new IllegalArgumentException(
							"Unknown target symbol type "
									+ extractString(connectionJson,
											TO_MODEL_ELEMENT_TYPE)
									+ " for connection.");
				}
			} else if (DATA.equals(extractString(connectionJson,
					FROM_MODEL_ELEMENT_TYPE))) {
				if (ACTIVITY_KEY.equals(extractString(connectionJson,
						TO_MODEL_ELEMENT_TYPE))) {
					createDataFlowConnection(
							connectionJson,
							processDefinition,
							MBFacade.findActivitySymbol(
									processDefinition.getDiagram().get(0),
									extractLong(connectionJson,
											TO_MODEL_ELEMENT_OID)),
							MBFacade.findDataSymbol(
									processDefinition.getDiagram().get(0),
									extractLong(connectionJson,
											FROM_MODEL_ELEMENT_OID)), maxOid);
				} else {
					throw new IllegalArgumentException(
							"Unknown target symbol type "
									+ extractString(connectionJson,
											TO_MODEL_ELEMENT_TYPE)
									+ " for connection.");
				}
			} else {
				throw new IllegalArgumentException(
						"Unsupported source symbol type "
								+ extractString(connectionJson,
										FROM_MODEL_ELEMENT_TYPE)
								+ " for connection.");
			}

			editSession.endEdit();
		}

		return connectionJson.toString();
	}

	/**
	 *
	 * @param sourceActivitySymbol
	 * @param targetActivitySymbol
	 * @throws JSONException
	 */
	private void createControlFlowConnection(JsonObject connectionJson,
			ProcessDefinitionType processDefinition,
			ActivitySymbolType sourceActivitySymbol,
			ActivitySymbolType targetActivitySymbol, long maxOid) {
		JsonObject controlFlowJson = connectionJson
				.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
		TransitionType transition = AbstractElementBuilder.F_CWM
				.createTransitionType();

		processDefinition.getTransition().add(transition);

		transition.setElementOid(++maxOid);
		transition.setFrom(sourceActivitySymbol.getActivity());
		transition.setTo(targetActivitySymbol.getActivity());
		transition.setId(extractString(controlFlowJson, ModelerConstants.ID_PROPERTY));

		if (extractBoolean(controlFlowJson, OTHERWISE_PROPERTY)) {
			transition.setCondition(OTHERWISE_KEY);
		} else {
			transition.setCondition(CONDITION_KEY);

			XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE
					.createXmlTextNode();

			ModelUtils.setCDataString(expression.getMixed(), "true", true);
			transition.setExpression(expression);
		}

		// setDescription(transition,
		// controlFlowJson.getString(DESCRIPTION_PROPERTY));

		TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM
				.createTransitionConnectionType();

		transition.getTransitionConnections().add(transitionConnection);
		transitionConnection.setTransition(transition);

		transitionConnection.setElementOid(++maxOid);
		transitionConnection.setSourceActivitySymbol(sourceActivitySymbol);
		transitionConnection.setTargetActivitySymbol(targetActivitySymbol);
		transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
				connectionJson, FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
		transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
				connectionJson, TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

		// TODO Obtain pool from call

		processDefinition.getDiagram().get(0).getPoolSymbols().get(0)
				.getTransitionConnection().add(transitionConnection);
	}

	/**
	 *
	 * @param orientation
	 * @return
	 */
	private String mapAnchorOrientation(int orientation) {
		if (orientation == NORTH_KEY) {
			return "top";
		} else if (orientation == EAST_KEY) {
			return "right";
		} else if (orientation == SOUTH_KEY) {
			return "bottom";
		} else if (orientation == WEST_KEY) {
			return "left";
		}

		throw new IllegalArgumentException("Illegal orientation key "
				+ orientation + ".");
	}

	/**
	 *
	 * @param connectionJson
	 * @param processDefinition
	 * @param sourceActivitySymbol
	 * @param targetActivitySymbol
	 * @param maxOid
	 */
	private void createControlFlowConnection(JsonObject connectionJson,
			ProcessDefinitionType processDefinition,
			StartEventSymbol startEventSymbol,
			ActivitySymbolType targetActivitySymbol, long maxOid) {
		TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM
				.createTransitionConnectionType();

		transitionConnection.setElementOid(++maxOid);
		transitionConnection.setSourceNode(startEventSymbol);
		transitionConnection.setTargetNode(targetActivitySymbol);
		transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
				connectionJson, FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
		transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
				connectionJson, TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
		processDefinition.getDiagram().get(0).getPoolSymbols().get(0)
				.getTransitionConnection().add(transitionConnection);
	}

	/**
	 *
	 * @param connectionJson
	 * @param processDefinition
	 * @param sourceActivitySymbol
	 * @param targetActivitySymbol
	 * @param maxOid
	 */
	private void createControlFlowConnection(JsonObject connectionJson,
			ProcessDefinitionType processDefinition,
			ActivitySymbolType sourceActivitySymbol,
			EndEventSymbol endEventSymbol, long maxOid) {
		TransitionConnectionType transitionConnection = AbstractElementBuilder.F_CWM
				.createTransitionConnectionType();
		transitionConnection.setElementOid(++maxOid);
		transitionConnection.setSourceNode(sourceActivitySymbol);
		transitionConnection.setTargetNode(endEventSymbol);
		transitionConnection.setSourceAnchor(mapAnchorOrientation(extractInt(
				connectionJson, FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
		transitionConnection.setTargetAnchor(mapAnchorOrientation(extractInt(
				connectionJson, TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
		// TODO Obtain pool from call

		processDefinition.getDiagram().get(0).getPoolSymbols().get(0)
				.getTransitionConnection().add(transitionConnection);
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
			ProcessDefinitionType processDefinition,
			ActivitySymbolType activitySymbol, DataSymbolType dataSymbol,
			long maxOid) {

		System.out.println("Create data flow connection");

		DataType data = dataSymbol.getData();
		ActivityType activity = activitySymbol.getActivity();

		DataMappingType dataMapping = AbstractElementBuilder.F_CWM
				.createDataMappingType();
		DataMappingConnectionType dataMappingConnection = AbstractElementBuilder.F_CWM
				.createDataMappingConnectionType();

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

		processDefinition.getDiagram().get(0).getPoolSymbols().get(0)
				.getDataMappingConnection().add(dataMappingConnection);

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
	private String getDefaultDataMappingContext(ActivityType activity) {
		if (ActivityImplementationType.ROUTE_LITERAL == activity
				.getImplementation()) {
			return PredefinedConstants.DEFAULT_CONTEXT;
		}
		if (ActivityImplementationType.MANUAL_LITERAL == activity
				.getImplementation()) {
			return PredefinedConstants.DEFAULT_CONTEXT;
		}
		if (ActivityImplementationType.APPLICATION_LITERAL == activity
				.getImplementation() && activity.getApplication() != null) {
			ApplicationType application = activity.getApplication();
			if (application.isInteractive()) {
				if (application.getContext().size() > 0) {
					ContextType context = (ContextType) application
							.getContext().get(0);
					return context.getType().getId();
				}
				return PredefinedConstants.DEFAULT_CONTEXT;
			}
			return PredefinedConstants.APPLICATION_CONTEXT;
		}
		if (ActivityImplementationType.SUBPROCESS_LITERAL == activity
				.getImplementation()
				&& activity.getImplementationProcess() != null) {
			ProcessDefinitionType process = activity.getImplementationProcess();
			if (process.getFormalParameters() != null) {
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
	public String updateConnection(String modelId, String processId,
			long connectionOid, JsonObject connectionJson) {
		JsonObject modelElementJson = connectionJson
				.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);

		synchronized (model) {
			editSession.beginEdit();

			System.out.println("Updateing Connection " + connectionOid + " "
					+ connectionJson.toString());

			if (extractString(modelElementJson, TYPE_PROPERTY).equals(
					CONTROL_FLOW_LITERAL)) {
				TransitionConnectionType transitionConnection = MBFacade.findTransitionConnectionByModelOid(
						processDefinition, connectionOid);
				transitionConnection
						.setSourceAnchor(mapAnchorOrientation(extractInt(
								connectionJson,
								FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
				transitionConnection
						.setTargetAnchor(mapAnchorOrientation(extractInt(
								connectionJson,
								TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));

				if (transitionConnection.getTransition() != null) {

					if (extractBoolean(modelElementJson, OTHERWISE_PROPERTY)) {
						transitionConnection.getTransition().setCondition(
								OTHERWISE_KEY);
					} else {
						transitionConnection.getTransition().setCondition(
								CONDITION_KEY);

						XmlTextNode expression = CarnotWorkflowModelFactory.eINSTANCE
								.createXmlTextNode();

						ModelUtils.setCDataString(
								expression.getMixed(),
								extractString(modelElementJson,
										CONDITION_EXPRESSION_PROPERTY), true);

						transitionConnection.getTransition().setExpression(
								expression);
					}

					setDescription(transitionConnection.getTransition(),
							modelElementJson);
					storeAttributes(modelElementJson,
							transitionConnection.getTransition());
				}
			} else {
				DataMappingConnectionType dataMappingConnection = MBFacade.findDataMappingConnectionByModelOid(
						processDefinition, connectionOid);

				dataMappingConnection
						.setSourceAnchor(mapAnchorOrientation(extractInt(
								connectionJson,
								FROM_ANCHOR_POINT_ORIENTATION_PROPERTY)));
				dataMappingConnection
						.setTargetAnchor(mapAnchorOrientation(extractInt(
								connectionJson,
								TO_ANCHOR_POINT_ORIENTATION_PROPERTY)));
			}

			editSession.endEdit();
		}

		return connectionJson.toString();
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param connectionId
	 * @return
	 */
	public String deleteConnection(String modelId, String processId,
			long connectionOid) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);

		synchronized (model) {
			editSession.beginEdit();

			try {
				TransitionConnectionType transitionConnection = MBFacade.findTransitionConnectionByModelOid(
						processDefinition, connectionOid);

				processDefinition.getDiagram().get(0).getTransitionConnection()
						.remove(transitionConnection);

				if (transitionConnection.getTransition() != null) {
					processDefinition.getTransition().remove(
							transitionConnection.getTransition());
				}
			} catch (ObjectNotFoundException x) {
				DataMappingConnectionType dataMappingConnection = MBFacade.findDataMappingConnectionByModelOid(
						processDefinition, connectionOid);

				processDefinition.getDiagram().get(0)
						.getDataMappingConnection()
						.remove(dataMappingConnection);
			}

			editSession.endEdit();
		}

		return new JsonObject().toString();
	}

	/**
	 *
	 * @param poolSymbol
	 * @param poolSymbolJson
	 * @return
	 */
	public JsonObject updatePool(ModelType model, PoolSymbol poolSymbol,
			JsonObject poolSymbolJson) {
		poolSymbol.setXPos(extractInt(poolSymbolJson, X_PROPERTY));
		poolSymbol.setYPos(extractInt(poolSymbolJson, Y_PROPERTY));
		poolSymbol.setWidth(extractInt(poolSymbolJson, WIDTH_PROPERTY));
		poolSymbol.setHeight(extractInt(poolSymbolJson, HEIGHT_PROPERTY));
		poolSymbol.setName(extractString(poolSymbolJson, ModelerConstants.NAME_PROPERTY));

		// TODO is array
		// JSONObject laneSymbolsJson =
		// poolSymbolJson.getJSONObject(LANE_SYMBOLS);
		JsonArray laneSymbolsJson = poolSymbolJson.getAsJsonArray(LANE_SYMBOLS);

		for (int n = 0; n < laneSymbolsJson.size(); ++n) {
			// for (Iterator iterator = laneSymbolsJson.keys();
			// iterator.hasNext();) {
			// String key = (String)iterator.next();
			// JSONObject laneSymbolJson = laneSymbolsJson.getJSONObject(key);
			JsonObject laneSymbolJson = laneSymbolsJson.get(n)
					.getAsJsonObject();
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
   public String createLane(String modelId, String processId, JsonObject commandJson)
   {
      JsonObject laneSymbolJson = commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY);
      ModelType model = getModelManagementStrategy().getModels().get(modelId);
      ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model, processId);
      EditingSession editSession = getEditingSession(model);
      long maxOid = XpdlModelUtils.getMaxUsedOid(model);

      String laneId = extractString(laneSymbolJson, ModelerConstants.ID_PROPERTY);
      String laneName = extractString(laneSymbolJson, ModelerConstants.NAME_PROPERTY);
      int xPos = extractInt(laneSymbolJson, X_PROPERTY);
      int yPos = extractInt(laneSymbolJson, Y_PROPERTY);
      int width = extractInt(laneSymbolJson, WIDTH_PROPERTY);
      int height = extractInt(laneSymbolJson, HEIGHT_PROPERTY);
      String orientation = extractString(laneSymbolJson, ModelerConstants.ORIENTATION_PROPERTY);
      String participantFullID = extractString(laneSymbolJson, ModelerConstants.PARTICIPANT_FULL_ID);

      synchronized (model)
      {
         editSession.beginEdit();
         LaneSymbol laneSymbol = MBFacade.createLane(modelId, model, processDefinition, laneId, laneName, xPos, yPos, width, height, orientation, participantFullID);
         laneSymbolJson.addProperty(OID_PROPERTY, laneSymbol.getElementOid());
         editSession.endEdit();
         return commandJson.toString();
      }
   }

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public String updateLane(String modelId, String processId, String laneId,
			JsonObject laneSymbolJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		LaneSymbol laneSymbol = MBFacade.findLaneSymbolById(processDefinition, laneId);

		EditingSession editingSession = getEditingSession(model);

		synchronized (model) {
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
			JsonObject laneSymbolJson) {
		laneSymbol.setName(extractString(laneSymbolJson, ModelerConstants.NAME_PROPERTY));
		laneSymbol.setXPos(extractInt(laneSymbolJson, X_PROPERTY));
		laneSymbol.setYPos(extractInt(laneSymbolJson, Y_PROPERTY));
		laneSymbol.setWidth(extractInt(laneSymbolJson, WIDTH_PROPERTY));
		laneSymbol.setHeight(extractInt(laneSymbolJson, HEIGHT_PROPERTY));

		// TODO Deal with full Ids

		if (laneSymbolJson.has(ModelerConstants.PARTICIPANT_FULL_ID)) {
			System.out.println("Participant Full ID"
					+ extractString(laneSymbolJson, ModelerConstants.PARTICIPANT_FULL_ID));
			System.out.println("Participant "
					+ MBFacade.findParticipant(
							model,
							MBFacade.stripFullId(extractString(laneSymbolJson,
									ModelerConstants.PARTICIPANT_FULL_ID))));

         String participantModelID = MBFacade.getModelId(extractString(laneSymbolJson,
               ModelerConstants.PARTICIPANT_FULL_ID));
         if (StringUtils.isEmpty(participantModelID))
         {
            participantModelID = model.getId();
         }

         ModelType participantModel = model;
         if(!participantModelID.equals(model.getId()))
         {
            participantModel = getModelManagementStrategy().getModels().get(participantModelID);
         }

         IModelParticipant modelParticipant = MBFacade.findParticipant(
               getModelManagementStrategy().getModels().get(participantModelID),
               MBFacade.stripFullId(extractString(laneSymbolJson, ModelerConstants.PARTICIPANT_FULL_ID)));

         if(!participantModelID.equals(model.getId()))
         {
            String fileConnectionId = JcrConnectionManager.createFileConnection(model, participantModel);

            String bundleId = CarnotConstants.DIAGRAM_PLUGIN_ID;
            URI uri = URI.createURI("cnx://" + fileConnectionId + "/");

            ReplaceModelElementDescriptor descriptor = new ReplaceModelElementDescriptor(uri,
                  modelParticipant, bundleId, null, true);

            PepperIconFactory iconFactory = new PepperIconFactory();

            descriptor.importElements(iconFactory, model, true);
         }

			laneSymbol.setParticipant(modelParticipant);
		}

		JsonObject activitySymbolsJson = laneSymbolJson
				.getAsJsonObject(ACTIVITY_SYMBOLS);
		for (Map.Entry<String, JsonElement> entry : activitySymbolsJson
				.entrySet()) {
			JsonObject activitySymbolJson = entry.getValue().getAsJsonObject();

			ActivitySymbolType activitySymbol = MBFacade.findActivitySymbol(laneSymbol,
					extractLong(activitySymbolJson, OID_PROPERTY));

			updateActivity(activitySymbol, laneSymbol, activitySymbolJson);

		}

		JsonObject gatewaySymbolsJson = laneSymbolJson
				.getAsJsonObject(GATEWAY_SYMBOLS);
		for (Map.Entry<String, JsonElement> entry : gatewaySymbolsJson
				.entrySet()) {
			JsonObject gatewaySymbolJson = entry.getValue().getAsJsonObject();

			ActivitySymbolType gatewaySymbol = MBFacade.findActivitySymbol(laneSymbol,
					extractLong(gatewaySymbolJson, OID_PROPERTY));

			updateGateway(gatewaySymbol, laneSymbol, gatewaySymbolJson);
		}

		JsonObject eventSymbolsJson = laneSymbolJson
				.getAsJsonObject(EVENT_SYMBOLS);
		for (Map.Entry<String, JsonElement> entry : eventSymbolsJson.entrySet()) {
			JsonObject eventSymbolJson = entry.getValue().getAsJsonObject();

			AbstractEventSymbol eventSymbol = MBFacade.findStartEventSymbol(laneSymbol,
					extractLong(eventSymbolJson, OID_PROPERTY));

			if (eventSymbol == null) {
				eventSymbol = MBFacade.findEndEventSymbol(laneSymbol,
						extractLong(eventSymbolJson, OID_PROPERTY));
			}

			updateEvent(eventSymbol, laneSymbol, eventSymbolJson);
		}

		JsonObject dataSymbolsJson = laneSymbolJson
				.getAsJsonObject(DATA_SYMBOLS);
		for (Map.Entry<String, JsonElement> entry : dataSymbolsJson.entrySet()) {
			JsonObject dataSymbolJson = entry.getValue().getAsJsonObject();

			DataSymbolType dataSymbol = MBFacade.findDataSymbolRecursively(laneSymbol,
					extractLong(dataSymbolJson, OID_PROPERTY));

			updateData(dataSymbol, dataSymbolJson);
		}

		return laneSymbolJson;
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public JsonObject createWebServiceApplication(String modelId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		JsonObject webServiceApplicationJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			ApplicationType webServiceApplication = AbstractElementBuilder.F_CWM
					.createApplicationType();

			model.getApplication().add(webServiceApplication);

			webServiceApplication.setId(extractString(
					webServiceApplicationJson, ModelerConstants.ID_PROPERTY));
			webServiceApplication.setName(extractString(
					webServiceApplicationJson, ModelerConstants.NAME_PROPERTY));
			webServiceApplication.setType(MBFacade.findApplicationTypeType(model,
					WEB_SERVICE_APPLICATION_TYPE_ID));

			webServiceApplicationJson.addProperty(APPLICATION_TYPE_PROPERTY,
					WEB_SERVICE_APPLICATION_TYPE_ID);
			webServiceApplicationJson.addProperty(MODEL_ID_PROPERTY,
					model.getId());

			// AttributeUtil.setAttribute(application, PredefinedConstants.,
			// "");

			// editSession.endEdit();

			return commandJson;
		}
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public JsonObject createMessageTransformationApplication(String modelId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		JsonObject messageTransformationApplicationJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			ApplicationType messageTransformationApplication = AbstractElementBuilder.F_CWM
					.createApplicationType();

			model.getApplication().add(messageTransformationApplication);

			messageTransformationApplication.setId(extractString(
					messageTransformationApplicationJson, ModelerConstants.ID_PROPERTY));
			messageTransformationApplication.setName(extractString(
					messageTransformationApplicationJson, ModelerConstants.NAME_PROPERTY));
			// messageTransformationApplication.setType(ModelBuilderFascade.findApplicationTypeType(model,
			// MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID));

			messageTransformationApplicationJson.addProperty(
					APPLICATION_TYPE_PROPERTY,
					MESSAGE_TRANSFORMATION_APPLICATION_TYPE_ID);
			messageTransformationApplicationJson.addProperty(MODEL_ID_PROPERTY,
					model.getId());

			JsonObject accessPoints = new JsonObject();
			messageTransformationApplicationJson.add(ACCESS_POINTS_PROPERTY,
					accessPoints);

			JsonArray fieldMappings = new JsonArray();
			messageTransformationApplicationJson.add("fieldMappings",
					fieldMappings);

			// editSession.endEdit();

			return commandJson;
		}
	}

	public JsonObject updateMessageTransformationApplication(String modelId,
			String applicationId,
			JsonObject messageTransformationApplicationJson) {
		// ModelType model =
		// getModelManagementStrategy().getModels().get(modelId);
		// ApplicationType application = ModelBuilderFascade.findApplication(model,
		// applicationId);

		// synchronized (model) {

		// Merge Access Points

		JsonObject accessPointsJson = messageTransformationApplicationJson
				.getAsJsonObject(ACCESS_POINTS_PROPERTY);

		for (Map.Entry<String, JsonElement> entry : accessPointsJson.entrySet()) {
			JsonObject accessPointJson = entry.getValue().getAsJsonObject();

			System.out.println("JSON: " + accessPointJson.toString());

			// AccessPointType accessPoint = null;
			//
			// accessPoint.setId(accessPointJson.getString(ID_PROPERTY));
			// accessPoint.setName(accessPointJson.getString(NAME_PROPERTY));
			//
			// if (accessPointJson.get(DIRECTION_PROPERTY).equals("IN")) {
			// accessPoint.setDirection(DirectionType.IN_LITERAL);
			// } else if (accessPointJson.get(DIRECTION_PROPERTY)
			// .equals("OUT")) {
			// accessPoint.setDirection(DirectionType.IN_LITERAL);
			// } else {
			// accessPoint.setDirection(DirectionType.INOUT_LITERAL);
			// }
			//
			// accessPoint.setType(arg0);
			//
			// storeAttributes(accessPointJson, accessPoint);
		}
		// <carnot:AccessPoints>
		// <carnot:AccessPoint Oid="10062" Id="Person1"
		// Name="Person1 (Person)" Direction="IN" Type="struct">
		// <carnot:Attributes>
		// <carnot:Attribute Name="carnot:engine:dataType" Value="Person"/>
		// <carnot:Attribute Name="carnot:engine:path:separator" Value="/"/>
		// <carnot:Attribute Name="carnot:engine:data:bidirectional"
		// Value="true" Type="boolean"/>
		// <carnot:Attribute Name="RootElement" Value="Person1"/>
		// <carnot:Attribute Name="FullXPath" Value="Person1/"/>
		// </carnot:Attributes>
		// </carnot:AccessPoint>
		// <carnot:AccessPoint Oid="10063" Id="Order1" Name="Order1 (Order)"
		// Direction="OUT" Type="struct">
		// <carnot:Attributes>
		// <carnot:Attribute Name="carnot:engine:dataType" Value="Order"/>
		// <carnot:Attribute Name="carnot:engine:path:separator" Value="/"/>
		// <carnot:Attribute Name="carnot:engine:data:bidirectional"
		// Value="true" Type="boolean"/>
		// <carnot:Attribute Name="RootElement" Value="Order1"/>
		// <carnot:Attribute Name="FullXPath" Value="Order1/"/>
		// </carnot:Attributes>
		// </carnot:AccessPoint>
		// </carnot:AccessPoints>

		// TODO Dummy

		JsonObject json = new JsonObject();
		JsonObject attributes = new JsonObject();

		json.add(ATTRIBUTES_PROPERTY, attributes);

		attributes.addProperty("carnot:engine:visibility", "Public");
		attributes.addProperty("synchronous:retry:enable", false);

		JsonArray fieldMappings = messageTransformationApplicationJson
				.getAsJsonArray("fieldMappings");

		String transformationProperty = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;ASCII&quot;?&gt;&#13;&#10;&lt;mapping:TransformationProperty xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xmlns:mapping=&quot;java://com.infinity.bpm.messaging.model&quot; xsi:schemaLocation=&quot;java://com.infinity.bpm.messaging.model java://com.infinity.bpm.messaging.model.mapping.MappingPackage&quot;&gt;&#13;&#10;";

		for (int n = 0; n < fieldMappings.size(); ++n) {
			JsonObject fieldMapping = fieldMappings.get(n).getAsJsonObject();

			transformationProperty += "&lt;fieldMappings";
			transformationProperty += " fieldPath=&quot;";
			transformationProperty += extractString(fieldMapping, "fieldPath");
			transformationProperty += "&quot; mappingExpression=&quot;";
			transformationProperty += extractString(fieldMapping,
					"mappingExpression");
			transformationProperty += "&quot;/&gt;&#13;&#10;";
		}

		transformationProperty += ";&lt;/mapping:TransformationProperty&gt;&#13;&#10;";

		System.out
				.println("Transformation Property: " + transformationProperty);

		attributes.addProperty("messageTransformation:TransformationProperty",
				transformationProperty);

		// storeAttributes(json, application);

		return messageTransformationApplicationJson;
		// }
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public JsonObject createCamelApplication(String modelId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		JsonObject camelApplicationJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			ApplicationType camelApplication = AbstractElementBuilder.F_CWM
					.createApplicationType();

			model.getApplication().add(camelApplication);

			camelApplication.setId(extractString(camelApplicationJson,
					ModelerConstants.ID_PROPERTY));
			camelApplication.setName(extractString(camelApplicationJson,
					ModelerConstants.NAME_PROPERTY));
			// messageTransformationApplication.setType(ModelBuilderFascade.findApplicationTypeType(model,
			// CAMEL_APPLICATION_TYPE_ID));

			camelApplicationJson.addProperty(APPLICATION_TYPE_PROPERTY,
					CAMEL_APPLICATION_TYPE_ID);
			camelApplicationJson.addProperty(MODEL_ID_PROPERTY, model.getId());

			JsonObject accessPoints = new JsonObject();
			camelApplicationJson.add(ACCESS_POINTS_PROPERTY, accessPoints);

			JsonObject accessPoint = new JsonObject();
			accessPoints.add("InputMessage", accessPoint);

			accessPoint.addProperty(ModelerConstants.ID_PROPERTY, "RequestMessage");
			accessPoint.addProperty(ModelerConstants.NAME_PROPERTY, "Request Message");
			accessPoint.addProperty(ACCESS_POINT_TYPE_PROPERTY,
					JAVA_CLASS_ACCESS_POINT_KEY);
			accessPoint.addProperty(DIRECTION_PROPERTY, IN_ACCESS_POINT_KEY);

			accessPoint = new JsonObject();
			accessPoints.add("OutputMessage", accessPoint);

			accessPoint.addProperty(ModelerConstants.ID_PROPERTY, "ResponseMessage");
			accessPoint.addProperty(ModelerConstants.NAME_PROPERTY, "Response Message");
			accessPoint.addProperty(ACCESS_POINT_TYPE_PROPERTY,
					JAVA_CLASS_ACCESS_POINT_KEY);
			accessPoint.addProperty(DIRECTION_PROPERTY, OUT_ACCESS_POINT_KEY);

			JsonObject fieldMappings = new JsonObject();
			camelApplicationJson.add("fieldMappings", fieldMappings);

			// editSession.endEdit();

			return commandJson;
		}
	}

	public JsonObject updateCamelApplication(String modelId,
			String applicationId, JsonObject camelApplicationJson) {
		// ModelType model =
		// getModelManagementStrategy().getModels().get(modelId);
		// ApplicationType application = ModelBuilderFascade.findApplication(model,
		// applicationId);

		// synchronized (model) {

		// Merge Access Points

		JsonObject accessPointsJson = camelApplicationJson
				.getAsJsonObject(ACCESS_POINTS_PROPERTY);

		for (Map.Entry<String, JsonElement> entry : accessPointsJson.entrySet()) {
			JsonObject accessPointJson = entry.getValue().getAsJsonObject();

			// AccessPointType accessPoint = null;
			//
			// accessPoint.setId(accessPointJson.getString(ID_PROPERTY));
			// accessPoint.setName(accessPointJson.getString(NAME_PROPERTY));
			//
			// if (accessPointJson.get(DIRECTION_PROPERTY).equals("IN")) {
			// accessPoint.setDirection(DirectionType.IN_LITERAL);
			// } else if (accessPointJson.get(DIRECTION_PROPERTY)
			// .equals("OUT")) {
			// accessPoint.setDirection(DirectionType.IN_LITERAL);
			// } else {
			// accessPoint.setDirection(DirectionType.INOUT_LITERAL);
			// }
			//
			// accessPoint.setType(arg0);
			//
			// storeAttributes(accessPointJson, accessPoint);
		}

		return camelApplicationJson;
		// }
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public JsonObject createExternalWebApplication(String modelId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		JsonObject externalWebApplicationJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			ApplicationType externalWebApplication = AbstractElementBuilder.F_CWM
					.createApplicationType();

			model.getApplication().add(externalWebApplication);

			externalWebApplication.setId(extractString(
					externalWebApplicationJson, ModelerConstants.ID_PROPERTY));
			externalWebApplication.setName(extractString(
					externalWebApplicationJson, ModelerConstants.NAME_PROPERTY));
			// TODO
			// externalWebApplication.setType(ModelBuilderFascade.findApplicationContextTypeType(model,
			// EXTERNAL_WEB_APPLICATION_TYPE_ID));

			// AttributeUtil.setAttribute(application, PredefinedConstants.,
			// "");

			externalWebApplicationJson.addProperty(APPLICATION_TYPE_PROPERTY,
					INTERACTIVE_APPLICATION_TYPE_KEY);

			JsonObject contextsJson = new JsonObject();

			JsonObject contextJson = new JsonObject();
			contextsJson.add(EXTERNAL_WEB_APP_CONTEXT_TYPE_KEY, contextJson);

			externalWebApplicationJson.add(CONTEXTS_PROPERTY, contextsJson);
			externalWebApplicationJson.addProperty(MODEL_ID_PROPERTY,
					model.getId());

			// editSession.endEdit();

			return commandJson;
		}
	}

	/**
	 *
	 * @param modelId
	 * @param postedData
	 * @return
	 */
	public String updateWebServiceApplication(String modelId,
			JsonObject webServiceApplicationJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			ApplicationType application = MBFacade.findApplication(model,
					extractString(webServiceApplicationJson, ModelerConstants.ID_PROPERTY));

			application.setName(extractString(webServiceApplicationJson,
					ModelerConstants.NAME_PROPERTY));

			// AttributeUtil.setAttribute(application, PredefinedConstants.,
			// "");

			// editSession.endEdit();

			return webServiceApplicationJson.toString();
		}
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public JsonObject createStructuredDataType(String modelId,
			JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		JsonObject structuredDataTypeJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);

		String typeId = extractString(structuredDataTypeJson,
              ModelerConstants.ID_PROPERTY);
		String typeName = extractString(structuredDataTypeJson,
              ModelerConstants.NAME_PROPERTY);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			MBFacade.createTypeDeclaration(model, typeId, typeName);

			structuredDataTypeJson
					.addProperty(MODEL_ID_PROPERTY, model.getId());

			JsonObject typeDeclarationJson = new JsonObject();
			structuredDataTypeJson.add(TYPE_DECLARATION_PROPERTY,
					typeDeclarationJson);

			JsonObject childrenJson = new JsonObject();
			typeDeclarationJson.add("children", childrenJson);

			// editSession.endEdit();

			return commandJson;
		}
	}



	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param eventType
	 * @param postedData
	 * @return
	 */
	public String createEvent(String modelId, String processId,
			JsonObject commandJson) {
		JsonObject eventSymbolJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);

		synchronized (model) {
			editSession.beginEdit();

			long maxOid = XpdlModelUtils.getMaxUsedOid(model);

			LaneSymbol parentLaneSymbol = MBFacade.findLaneInProcess(processDefinition,
					extractString(eventSymbolJson, PARENT_SYMBOL_ID_PROPERTY));

			if (START_EVENT.equals(extractString(eventSymbolJson,
					ModelerConstants.MODEL_ELEMENT_PROPERTY, EVENT_TYPE_PROPERTY))) {
				StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM
						.createStartEventSymbol();
				startEventSymbol.setElementOid(++maxOid);

				eventSymbolJson.addProperty(OID_PROPERTY,
						startEventSymbol.getElementOid());

				startEventSymbol
						.setXPos(extractInt(eventSymbolJson, X_PROPERTY)
								- parentLaneSymbol.getXPos());
				startEventSymbol
						.setYPos(extractInt(eventSymbolJson, Y_PROPERTY)
								- parentLaneSymbol.getYPos());
				startEventSymbol.setWidth(extractInt(eventSymbolJson,
						WIDTH_PROPERTY));
				startEventSymbol.setHeight(extractInt(eventSymbolJson,
						HEIGHT_PROPERTY));

				// TODO evaluate other properties

				processDefinition.getDiagram().get(0).getStartEventSymbols()
						.add(startEventSymbol);
				parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);
			} else {
				EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM
						.createEndEventSymbol();
				endEventSymbol.setElementOid(++maxOid);

				eventSymbolJson.addProperty(OID_PROPERTY,
						endEventSymbol.getElementOid());

				endEventSymbol.setXPos(extractInt(eventSymbolJson, X_PROPERTY)
						- parentLaneSymbol.getXPos());
				endEventSymbol.setYPos(extractInt(eventSymbolJson, Y_PROPERTY)
						- parentLaneSymbol.getYPos());
				endEventSymbol.setWidth(extractInt(eventSymbolJson,
						WIDTH_PROPERTY));
				endEventSymbol.setHeight(extractInt(eventSymbolJson,
						HEIGHT_PROPERTY));

				processDefinition.getDiagram().get(0).getEndEventSymbols()
						.add(endEventSymbol);

				parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);
			}

			editSession.endEdit();
		}

		return commandJson.toString();
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param eventType
	 * @param postedData
	 * @return
	 */
	public String updateEvent(String modelId, String processId, String eventId,
			JsonObject commandJson) {
		JsonObject eventSymbolJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		JsonObject modelElementJson = commandJson
				.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);

		synchronized (model) {
			editSession.beginEdit();

			AbstractEventSymbol eventSymbol;
			LaneSymbol laneSymbol;

			if (START_EVENT.equals(extractString(modelElementJson,
					EVENT_TYPE_PROPERTY))) {
				eventSymbol = MBFacade.findStartEventSymbol(processDefinition
						.getDiagram().get(0),
						extractLong(eventSymbolJson, OID_PROPERTY));
				laneSymbol = MBFacade.findLaneContainingStartEventSymbol(
						processDefinition.getDiagram().get(0),
						eventSymbol.getElementOid());
			} else {
				eventSymbol = MBFacade.findEndEventSymbol(processDefinition.getDiagram()
						.get(0), extractLong(eventSymbolJson, OID_PROPERTY));
				laneSymbol = MBFacade.findLaneContainingEndEventSymbol(processDefinition
						.getDiagram().get(0), eventSymbol.getElementOid());
			}

			eventSymbolJson = updateEvent(eventSymbol, laneSymbol,
					eventSymbolJson);

			editSession.endEdit();
		}

		return eventSymbolJson.toString();
	}

	/**
	 *
	 * @param eventSymbol
	 * @param eventSymbolJson
	 * @return
	 */
	private JsonObject updateEvent(AbstractEventSymbol eventSymbol,
			LaneSymbol laneSymbol, JsonObject eventSymbolJson) {
		JsonObject eventJson = eventSymbolJson
				.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY);
		IIdentifiableModelElement event = eventSymbol.getModelElement();

		eventSymbol.setXPos(extractInt(eventSymbolJson, X_PROPERTY)
				- laneSymbol.getXPos());
		eventSymbol.setYPos(extractInt(eventSymbolJson, Y_PROPERTY)
				- laneSymbol.getYPos());
		if (eventSymbolJson.has(WIDTH_PROPERTY)) {
			eventSymbol.setWidth(extractInt(eventSymbolJson, WIDTH_PROPERTY));
		}
		if (eventSymbolJson.has(HEIGHT_PROPERTY)) {
			eventSymbol.setHeight(extractInt(eventSymbolJson, HEIGHT_PROPERTY));
		}
		// setDescription(event, eventJson);
		// storeAttributes(eventJson, event);
		return eventSymbolJson;
	}

	/**
	 *
	 * @param modelId
	 * @param transferObject
	 * @return
	 */
	public String createRole(String modelId, JsonObject roleJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);

		synchronized (model) {
			// EditingSession editSession = model.getEditSession();
			//
			// editSession.beginEdit();

			RoleType role = newRole(model).withIdAndName(
					extractString(roleJson, ModelerConstants.ID_PROPERTY),
					extractString(roleJson, ModelerConstants.NAME_PROPERTY)).build();
		}

		return roleJson.toString();
	}

	/**
	 * @return
	 */
	public String loadProcessDiagram(String modelId, String processId) {
		// TODO Try to ModelBuilderFascade.find in loaded models first. Correct?
		ModelType model = getModelManagementStrategy().getModels().get(modelId);

		// TODO Very ugly - only for newly created models

		if (model == null) {
			getModelManagementStrategy().attachModel(modelId);
		}

		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);

		JsonObject diagramJson = new JsonObject();

		// Pools and Lanes

		JsonObject poolSymbolsJson = new JsonObject();
		diagramJson.add(POOL_SYMBOLS, poolSymbolsJson);

		for (PoolSymbol poolSymbol : processDefinition.getDiagram().get(0)
				.getPoolSymbols()) {
			JsonObject poolSymbolJson = new JsonObject();
			poolSymbolsJson.add(poolSymbol.getId(), poolSymbolJson);

			poolSymbolJson
					.addProperty(OID_PROPERTY, poolSymbol.getElementOid());
			poolSymbolJson.addProperty(ModelerConstants.ID_PROPERTY, poolSymbol.getId());
			poolSymbolJson.addProperty(ModelerConstants.NAME_PROPERTY, poolSymbol.getName());
			poolSymbolJson.addProperty(X_PROPERTY, poolSymbol.getXPos());
			poolSymbolJson.addProperty(Y_PROPERTY, poolSymbol.getYPos());
			poolSymbolJson.addProperty(WIDTH_PROPERTY, poolSymbol.getWidth());
			poolSymbolJson.addProperty(HEIGHT_PROPERTY, poolSymbol.getHeight());

			if (poolSymbol.getOrientation().equals(
					OrientationType.HORIZONTAL_LITERAL)) {
				poolSymbolJson.addProperty(ModelerConstants.ORIENTATION_PROPERTY,
						ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL);
			} else {
				poolSymbolJson.addProperty(ModelerConstants.ORIENTATION_PROPERTY,
						ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL);
			}

			JsonArray laneSymbols = new JsonArray();
			poolSymbolJson.add(LANE_SYMBOLS, laneSymbols);

			for (LaneSymbol laneSymbol : poolSymbol.getChildLanes()) {
				JsonObject laneSymbolJson = new JsonObject();
				laneSymbols.add(laneSymbolJson);

				laneSymbolJson.addProperty(OID_PROPERTY,
						laneSymbol.getElementOid());
				laneSymbolJson.addProperty(ModelerConstants.ID_PROPERTY, laneSymbol.getId());
				laneSymbolJson.addProperty(ModelerConstants.NAME_PROPERTY, laneSymbol.getName());
				laneSymbolJson.addProperty(X_PROPERTY, laneSymbol.getXPos());
				laneSymbolJson.addProperty(Y_PROPERTY, laneSymbol.getYPos());
				laneSymbolJson.addProperty(WIDTH_PROPERTY,
						laneSymbol.getWidth());
				laneSymbolJson.addProperty(HEIGHT_PROPERTY,
						laneSymbol.getHeight());

				if (laneSymbol.getParticipant() != null) {
					// TODO Scope handling

					laneSymbolJson.addProperty(ModelerConstants.PARTICIPANT_FULL_ID,
							MBFacade.createFullId(model, laneSymbol.getParticipant()));
				}

				JsonObject activitySymbolsJson = new JsonObject();
				JsonObject gatewaySymbolsJson = new JsonObject();

				laneSymbolJson.add(ACTIVITY_SYMBOLS, activitySymbolsJson);
				laneSymbolJson.add(GATEWAY_SYMBOLS, gatewaySymbolsJson);

				for (ActivitySymbolType activitySymbol : laneSymbol
						.getActivitySymbol()) {
					JsonObject activitySymbolJson = new JsonObject();

					activitySymbolJson.addProperty(OID_PROPERTY,
							activitySymbol.getElementOid());
					activitySymbolJson.addProperty(X_PROPERTY,
							activitySymbol.getXPos() + laneSymbol.getXPos()
									+ POOL_LANE_MARGIN);
					activitySymbolJson.addProperty(Y_PROPERTY,
							activitySymbol.getYPos() + laneSymbol.getYPos()
									+ POOL_LANE_MARGIN
									+ POOL_SWIMLANE_TOP_BOX_HEIGHT);
					activitySymbolJson.addProperty(WIDTH_PROPERTY,
							activitySymbol.getWidth());
					activitySymbolJson.addProperty(HEIGHT_PROPERTY,
							activitySymbol.getHeight());

					ActivityType activity = activitySymbol.getActivity();
					JsonObject activityJson = new JsonObject();

					activitySymbolJson
							.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, activityJson);
					activityJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());
					activityJson.addProperty(ModelerConstants.NAME_PROPERTY, activity.getName());
					loadDescription(activityJson, activity);
					loadAttributes(activity, activityJson);

					// TODO Hack to identify gateways
					if (activity.getId().toLowerCase().startsWith("gateway")) {
						gatewaySymbolsJson.add(activity.getId(),
								activitySymbolJson);

						activityJson.addProperty(TYPE_PROPERTY, ACTIVITY_KEY);
						activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE,
								GATEWAY_ACTIVITY);

						if (activity.getJoin() == JoinSplitType.XOR_LITERAL) {
							activityJson.addProperty(GATEWAY_TYPE_PROPERTY,
									XOR_GATEWAY_TYPE);
						} else if (activity.getJoin() == JoinSplitType.AND_LITERAL) {
							activityJson.addProperty(GATEWAY_TYPE_PROPERTY,
									AND_GATEWAY_TYPE);
						} else if (activity.getSplit() == JoinSplitType.XOR_LITERAL) {
							activityJson.addProperty(GATEWAY_TYPE_PROPERTY,
									XOR_GATEWAY_TYPE);
						} else if (activity.getSplit() == JoinSplitType.AND_LITERAL) {
							activityJson.addProperty(GATEWAY_TYPE_PROPERTY,
									AND_GATEWAY_TYPE);
						}

						//Identify the gateway symbol for this activity and update the
						//location and dimension attributes.
				        GatewaySymbol thisGatewaySymbol = null;
				        for (GatewaySymbol gs : laneSymbol.getGatewaySymbol()) {
				           if (gs.getActivitySymbol().getActivity().equals(activity)) {
				              thisGatewaySymbol = gs;
				              break;
				           }
				        }
				        if (null != thisGatewaySymbol) {
	                       activitySymbolJson.remove(X_PROPERTY);
	                        activitySymbolJson.addProperty(X_PROPERTY,
	                              thisGatewaySymbol.getXPos() + laneSymbol.getXPos()
	                                        + POOL_LANE_MARGIN);
	                        activitySymbolJson.remove(Y_PROPERTY);
	                        activitySymbolJson.addProperty(Y_PROPERTY,
	                              thisGatewaySymbol.getYPos() + laneSymbol.getYPos()
	                                        + POOL_LANE_MARGIN + POOL_SWIMLANE_TOP_BOX_HEIGHT);
	                        activitySymbolJson.remove(WIDTH_PROPERTY);
	                        activitySymbolJson.addProperty(WIDTH_PROPERTY,
	                              thisGatewaySymbol.getWidth());
	                        activitySymbolJson.remove(HEIGHT_PROPERTY);
	                        activitySymbolJson.addProperty(HEIGHT_PROPERTY,
	                              thisGatewaySymbol.getHeight());
				        }
					} else {
						activitySymbolsJson.add(activity.getId(),
								activitySymbolJson);

						activityJson.addProperty(TYPE_PROPERTY, ACTIVITY_KEY);
						activityJson.addProperty(ModelerConstants.ACTIVITY_TYPE, activity
								.getImplementation().getLiteral());
						if (activity.getImplementationProcess() != null) {
							activityJson
									.addProperty(
											ModelerConstants.SUBPROCESS_ID,
											MBFacade.createFullId(model, activity
													.getImplementationProcess()));
						} else if (activity.getApplication() != null) {
							activityJson.addProperty(
									ModelerConstants.APPLICATION_FULL_ID_PROPERTY,
									MBFacade.createFullId(model,
											activity.getApplication()));
						}

						// TODO Obtain access points on client

						JsonObject accessPointsJson = new JsonObject();
						activityJson.add(ACCESS_POINTS_PROPERTY,
								accessPointsJson);

						// TODO Access points need to be obtained from all
						// contexts

						for (AccessPointType accessPoint : ActivityUtil
								.getAccessPoints(activitySymbol.getActivity(),
										true,
										getDefaultDataMappingContext(activity))) {
							JsonObject accessPointJson = new JsonObject();

							accessPointsJson.add(accessPoint.getId(),
									accessPointJson);
							accessPointJson.addProperty(ModelerConstants.ID_PROPERTY,
									accessPoint.getId());
							accessPointJson.addProperty(ModelerConstants.NAME_PROPERTY,
									accessPoint.getName());
							accessPointJson.addProperty(DIRECTION_PROPERTY,
									accessPoint.getDirection().getLiteral());

							loadDescription(accessPointJson, accessPoint);
						}

						/*
						 * if (null != activity.getPerformer()) {
						 * act.getProps().setPerformerid(
						 * activity.getPerformer().getId()); }
						 */
					}
				}

				JsonObject eventSymbols = new JsonObject();
				laneSymbolJson.add(EVENT_SYMBOLS, eventSymbols);

				for (StartEventSymbol startEventSymbol : laneSymbol
						.getStartEventSymbols()) {
					JsonObject startEventJson = ModelElementMarshaller.toStartEventJson(startEventSymbol);
					eventSymbols.add(String.valueOf(startEventSymbol.getElementOid()),
							startEventJson);

				}

				for (EndEventSymbol endEventSymbol : laneSymbol
						.getEndEventSymbols()) {
					JsonObject eventSymbolJson = new JsonObject();

					eventSymbolJson.addProperty(OID_PROPERTY,
							endEventSymbol.getElementOid());
					eventSymbolJson.addProperty(X_PROPERTY,
							endEventSymbol.getXPos() + laneSymbol.getXPos()
									+ POOL_LANE_MARGIN
									+ START_END_SYMBOL_LEFT_OFFSET);
					eventSymbolJson.addProperty(Y_PROPERTY,
							endEventSymbol.getYPos() + laneSymbol.getYPos()
									+ POOL_LANE_MARGIN
									+ POOL_SWIMLANE_TOP_BOX_HEIGHT);

					eventSymbols.add(
							String.valueOf(endEventSymbol.getElementOid()),
							eventSymbolJson);

					JsonObject eventJson = new JsonObject();
					eventSymbolJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, eventJson);

					eventJson.addProperty(TYPE_PROPERTY, EVENT_KEY);
					eventJson.addProperty(EVENT_TYPE_PROPERTY, STOP_EVENT);
					// eventJson.put(ID_PROPERTY,
					// String.valueOf(endEventSymbol.getModelElement().getId()));
					// loadDescription(eventJson,
					// endEventSymbol.getModelElement());
					// loadAttributes(endEventSymbol.getModelElement(),
					// eventJson);
				}

				// Data

				JsonObject dataSymbolsJson = new JsonObject();
				laneSymbolJson.add(DATA_SYMBOLS, dataSymbolsJson);

				for (DataSymbolType dataSymbol : laneSymbol.getDataSymbol()) {

					JsonObject dataSymbolJson = new JsonObject();

					dataSymbolJson.addProperty(OID_PROPERTY,
							dataSymbol.getElementOid());
					dataSymbolJson
							.addProperty(X_PROPERTY, dataSymbol.getXPos());
					dataSymbolJson
							.addProperty(Y_PROPERTY, dataSymbol.getYPos());

					// TODO Scoping
					dataSymbolJson.addProperty(ModelerConstants.DATA_FULL_ID_PROPERTY,
					      MBFacade.createFullId(model, dataSymbol.getData()));
					dataSymbolsJson.add(dataSymbol.getData().getId(),
							dataSymbolJson);
				}
			}

			JsonObject connectionsJson = new JsonObject();
			diagramJson.add(CONNECTIONS_PROPERTY, connectionsJson);

			// Data Mappings

			for (DataMappingConnectionType dataMappingConnection : poolSymbol
					.getDataMappingConnection()) {
				JsonObject connectionJson = new JsonObject();

				connectionJson.addProperty(OID_PROPERTY,
						dataMappingConnection.getElementOid());
				connectionJson.addProperty(FROM_MODEL_ELEMENT_OID,
						dataMappingConnection.getDataSymbol().getElementOid());
				connectionJson.addProperty(FROM_MODEL_ELEMENT_TYPE, DATA);
				connectionJson.addProperty(TO_MODEL_ELEMENT_OID,
						dataMappingConnection.getActivitySymbol()
								.getElementOid());
				connectionJson.addProperty(TO_MODEL_ELEMENT_TYPE, ACTIVITY_KEY);
				connectionJson.addProperty(
						FROM_ANCHOR_POINT_ORIENTATION_PROPERTY,
						mapAnchorOrientation(dataMappingConnection
								.getSourceAnchor()));
				connectionJson.addProperty(
						TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
						mapAnchorOrientation(dataMappingConnection
								.getTargetAnchor()));

				JsonObject dataFlowJson = new JsonObject();
				connectionJson.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, dataFlowJson);

				dataFlowJson.addProperty(TYPE_PROPERTY, DATA_FLOW_LITERAL);
				dataFlowJson.addProperty(ModelerConstants.ID_PROPERTY, ""
						+ dataMappingConnection.getElementOid());

				// if (dataMappingC.getDirection() ==
				// DirectionType.IN_LITERAL)
				// {
				// dataFlow.put(IN_DATA_MAPPING_PROPERTY, true);
				// dataFlow.put(OUT_DATA_MAPPING_PROPERTY, false);
				// } else if (dataMapping.getDirection() ==
				// DirectionType.OUT_LITERAL) {
				// dataFlow.put(IN_DATA_MAPPING_PROPERTY, false);
				// dataFlow.put(OUT_DATA_MAPPING_PROPERTY, true);
				// } else {
				// dataFlow.put(IN_DATA_MAPPING_PROPERTY, true);
				// dataFlow.put(OUT_DATA_MAPPING_PROPERTY, true);
				// }

				// dataFlow.put(DATA_PATH_PROPERTY,
				// dataMapping.getApplicationPath());
				// dataFlow.put(APPLICATION_PATH_PROPERTY,
				// dataMapping.getApplicationPath());
				connectionsJson.add(extractString(dataFlowJson, ModelerConstants.ID_PROPERTY),
						connectionJson);
			}

			// Transitions

			for (TransitionConnectionType transitionConnection : poolSymbol
					.getTransitionConnection()) {
				JsonObject connectionJson = new JsonObject();
				JsonObject modelElementJson = new JsonObject();

				// Common settings

				connectionJson.addProperty(
						FROM_ANCHOR_POINT_ORIENTATION_PROPERTY,
						mapAnchorOrientation(transitionConnection
								.getSourceAnchor()));
				connectionJson.addProperty(
						TO_ANCHOR_POINT_ORIENTATION_PROPERTY,
						mapAnchorOrientation(transitionConnection
								.getTargetAnchor()));

				if (transitionConnection.getTransition() != null) {
					TransitionType transition = transitionConnection
							.getTransition();

					connectionJson.addProperty(OID_PROPERTY,
							transitionConnection.getElementOid());
					connectionJson
							.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);

					modelElementJson.addProperty(TYPE_PROPERTY,
							CONTROL_FLOW_LITERAL);
					modelElementJson.addProperty(ModelerConstants.ID_PROPERTY,
							transition.getId());

					if (transition.getCondition().equals("CONDITION")) {
						modelElementJson.addProperty(
								CONDITION_EXPRESSION_PROPERTY,
								(String) transition.getExpression().getMixed()
										.getValue(0));
						modelElementJson.addProperty(OTHERWISE_PROPERTY, false);
					} else {
						modelElementJson.addProperty(OTHERWISE_PROPERTY, true);
					}

					loadDescription(modelElementJson, transition);
					loadAttributes(transition, modelElementJson);

					connectionJson.addProperty(FROM_MODEL_ELEMENT_OID,
							transition.getFrom().getActivitySymbols().get(0)
									.getElementOid());

					// TODO Hack to identify gateways

					if (transition.getFrom().getId().toLowerCase()
							.startsWith("gateway")) {
						connectionJson.addProperty(FROM_MODEL_ELEMENT_TYPE,
								GATEWAY);
					} else {
						connectionJson.addProperty(FROM_MODEL_ELEMENT_TYPE,
								ACTIVITY_KEY);
					}

					connectionJson.addProperty(TO_MODEL_ELEMENT_OID, transition
							.getTo().getActivitySymbols().get(0)
							.getElementOid());

					if (transition.getTo().getId().toLowerCase()
							.startsWith("gateway")) {
						connectionJson.addProperty(TO_MODEL_ELEMENT_TYPE,
								GATEWAY);
                        connectionJson.remove(TO_ANCHOR_POINT_ORIENTATION_PROPERTY);
                        connectionJson.addProperty(
                                TO_ANCHOR_POINT_ORIENTATION_PROPERTY, NORTH_KEY);
					} else {
						connectionJson.addProperty(TO_MODEL_ELEMENT_TYPE,
								ACTIVITY_KEY);
					}

					connectionsJson.add(
							extractString(modelElementJson, ModelerConstants.ID_PROPERTY),
							connectionJson);
				} else if (transitionConnection.getSourceNode() instanceof StartEventSymbol) {

					connectionJson.addProperty(OID_PROPERTY,
							transitionConnection.getElementOid());
					connectionJson
							.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);

					modelElementJson.addProperty(TYPE_PROPERTY,
							CONTROL_FLOW_LITERAL);
					modelElementJson
							.addProperty(
									ModelerConstants.ID_PROPERTY,
									transitionConnection.getSourceNode()
											.getElementOid()
											+ "-"
											+ ((ActivitySymbolType) transitionConnection
													.getTargetActivitySymbol())
													.getActivity().getId());

					connectionJson.addProperty(FROM_MODEL_ELEMENT_OID,
							transitionConnection.getSourceNode()
									.getElementOid());
					connectionJson.addProperty(FROM_MODEL_ELEMENT_TYPE,
							EVENT_KEY);
					connectionJson.addProperty(TO_MODEL_ELEMENT_OID,
							transitionConnection.getTargetActivitySymbol()
									.getElementOid());
					connectionJson.addProperty(TO_MODEL_ELEMENT_TYPE,
							ACTIVITY_KEY);
					connectionsJson.add(
							extractString(modelElementJson, ModelerConstants.ID_PROPERTY),
							connectionJson);
				} else if (transitionConnection.getTargetNode() instanceof EndEventSymbol) {
					connectionJson.addProperty(OID_PROPERTY,
							transitionConnection.getElementOid());
					connectionJson
							.add(ModelerConstants.MODEL_ELEMENT_PROPERTY, modelElementJson);
					modelElementJson.addProperty(TYPE_PROPERTY,
							CONTROL_FLOW_LITERAL);
					modelElementJson.addProperty(
							ModelerConstants.ID_PROPERTY,
							((ActivitySymbolType) transitionConnection
									.getSourceActivitySymbol()).getActivity()
									.getId()
									+ "-"
									+ String.valueOf(transitionConnection
											.getTargetNode().getElementOid()));
					connectionJson.addProperty(FROM_MODEL_ELEMENT_OID,
							transitionConnection.getSourceActivitySymbol()
									.getElementOid());
					connectionJson.addProperty(FROM_MODEL_ELEMENT_TYPE,
							ACTIVITY_KEY);
					connectionJson.addProperty(TO_MODEL_ELEMENT_OID, String
							.valueOf(transitionConnection.getTargetNode()
									.getElementOid()));
					connectionJson
							.addProperty(TO_MODEL_ELEMENT_TYPE, EVENT_KEY);
					connectionsJson.add(
							extractString(modelElementJson, ModelerConstants.ID_PROPERTY),
							connectionJson);

					//For end event symbol the anchorpoint orientation is set to "bottom", in the eclipse modeler.
					//This causes wrong routing of the the connector.
					//Hence overriding the property with "center" / or "undefined"
					connectionJson.remove(TO_ANCHOR_POINT_ORIENTATION_PROPERTY);
		            connectionJson.addProperty(
		                        TO_ANCHOR_POINT_ORIENTATION_PROPERTY, UNDEFINED_ORIENTATION_KEY);
				}
			}
		}

		return diagramJson.toString();
	}

	/**
	 *
	 * @param orientation
	 * @return
	 */
	private int mapAnchorOrientation(String orientation) {
		if (orientation.equals("top")) {
			return NORTH_KEY;
		} else if (orientation.equals("right")) {
			return EAST_KEY;
		} else if (orientation.equals("bottom")) {
			return SOUTH_KEY;
		} else if (orientation.equals("left")) {
			return WEST_KEY;
		} else if (orientation.equals("center") || orientation == null) {
			return UNDEFINED_ORIENTATION_KEY;
		}

		throw new IllegalArgumentException("Illegal orientation key "
				+ orientation + ".");
	}

	/**
	 * @return
	 */
	public String updateProcessDiagram(String modelId, String processId,
			String diagramId, JsonObject diagramJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		DiagramType diagram = processDefinition.getDiagram().get(0);
		EditingSession editSession = getEditingSession(model);

		editSession.beginEdit();

		JsonObject poolSymbolsJson = diagramJson.getAsJsonObject(POOL_SYMBOLS);

		for (Map.Entry<String, JsonElement> entry : poolSymbolsJson.entrySet()) {
			JsonObject poolSymbolJson = entry.getValue().getAsJsonObject();

			PoolSymbol poolSymbol = MBFacade.findPoolSymbolByElementOid(
					processDefinition,
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
	private JsonObject loadModelOutline(ModelType model) {
		JsonObject modelJson = new JsonObject();

		modelJson.addProperty(ModelerConstants.ID_PROPERTY, model.getId());
		modelJson.addProperty(ModelerConstants.NAME_PROPERTY, model.getName());

		if (model.getDescription() != null) {
			modelJson.addProperty(DESCRIPTION_PROPERTY, (String) model
					.getDescription().getMixed().get(0).getValue());
		} else {
			modelJson.addProperty(DESCRIPTION_PROPERTY, (String) null);
		}

		JsonObject processesJson = new JsonObject();
		modelJson.add("processes", processesJson);

		for (ProcessDefinitionType processDefinition : model
				.getProcessDefinition()) {

			JsonObject processJson = new JsonObject();
			processesJson.add(processDefinition.getId(), processJson);

			processJson.addProperty(ModelerConstants.ID_PROPERTY, processDefinition.getId());
			processJson.addProperty(ModelerConstants.NAME_PROPERTY, processDefinition.getName());
			loadDescription(processJson, processDefinition);

			JsonObject attributesJson = new JsonObject();
			processJson.add(ATTRIBUTES_PROPERTY, attributesJson);

			JsonObject activitiesJson = new JsonObject();
			processJson.add(ACTIVITIES_PROPERTY, activitiesJson);

			for (ActivityType activity : processDefinition.getActivity()) {
				JsonObject activityJson = new JsonObject();
				activitiesJson.add(activity.getId(), activityJson);

				activityJson.addProperty(ModelerConstants.ID_PROPERTY, activity.getId());
				activityJson.addProperty(ModelerConstants.NAME_PROPERTY, activity.getName());
				loadDescription(activityJson, activity);
			}

			JsonObject gatewaysJson = new JsonObject();
			processJson.add(GATEWAYS_PROPERTY, gatewaysJson);

			JsonObject eventsJson = new JsonObject();
			processJson.add(EVENTS_PROPERTY, eventsJson);

			JsonObject controlFlowsJson = new JsonObject();
			processJson.add(CONTROL_FLOWS_PROPERTY, controlFlowsJson);

			JsonObject dataFlowsJson = new JsonObject();
			processJson.add(DATA_FLOWS_PROPERTY, dataFlowsJson);
		}

		JsonObject participantsJson = new JsonObject();
		modelJson.add("participants", participantsJson);

		for (RoleType role : model.getRole()) {
			JsonObject participantJson = new JsonObject();
			participantsJson.add(role.getId(), participantJson);

			participantJson.addProperty(ModelerConstants.ID_PROPERTY, role.getId());
			participantJson.addProperty(ModelerConstants.NAME_PROPERTY, role.getName());
			loadDescription(participantJson, role);
		}

		for (OrganizationType organization : model.getOrganization()) {
			JsonObject participantJson = new JsonObject();
			participantsJson.add(organization.getId(), participantJson);

			participantJson.addProperty(ModelerConstants.ID_PROPERTY, organization.getId());
			participantJson.addProperty(ModelerConstants.NAME_PROPERTY, organization.getName());
			loadDescription(participantJson, organization);
		}

		for (ConditionalPerformerType conditionalPerformer : model
				.getConditionalPerformer()) {
			JsonObject participantJson = new JsonObject();
			participantsJson.add(conditionalPerformer.getId(), participantJson);

			participantJson.addProperty(ModelerConstants.ID_PROPERTY,
					conditionalPerformer.getId());
			participantJson.addProperty(ModelerConstants.NAME_PROPERTY,
					conditionalPerformer.getName());
			loadDescription(participantJson, conditionalPerformer);
		}

		JsonObject applicationsJson = new JsonObject();
		modelJson.add("applications", applicationsJson);

		for (ApplicationType application : model.getApplication()) {

			JsonObject applicationJson = new JsonObject();
			applicationsJson.add(application.getId(), applicationJson);

			applicationJson.addProperty(ModelerConstants.ID_PROPERTY, application.getId());
			applicationJson.addProperty(ModelerConstants.NAME_PROPERTY, application.getName());
			loadDescription(applicationJson, application);

			if (application.getType() != null) {
				applicationJson.addProperty(APPLICATION_TYPE_PROPERTY,
						application.getType().getId());
			} else {
				applicationJson.addProperty(APPLICATION_TYPE_PROPERTY,
						INTERACTIVE_APPLICATION_TYPE_KEY);

				JsonObject contextsJson = new JsonObject();
				applicationJson.add(CONTEXTS_PROPERTY, contextsJson);

				for (ContextType context : application.getContext()) {
					JsonObject contextJson = new JsonObject();
					applicationJson.add(context.getType().getId(), contextJson);
				}
			}

			// TODO Review

			for (AttributeType attribute : application.getAttribute()) {
				if ("carnot:engine:methodName".equals(attribute.getName())) {
					applicationJson.addProperty("accessPoint",
							attribute.getValue());
					break;
				}
			}
		}

		JsonObject dataItemsJson = new JsonObject();
		modelJson.add("dataItems", dataItemsJson);

		for (DataType data : model.getData()) {
			dataItemsJson.add(data.getId(), loadData(model, data));
		}

		JsonObject structuredDataTypesJson = new JsonObject();
		modelJson.add("structuredDataTypes", structuredDataTypesJson);

		// TODO Check needed?

		if (null != model.getTypeDeclarations()) {
			for (TypeDeclarationType typeDeclaration : model
					.getTypeDeclarations().getTypeDeclaration()) {
				JsonObject structuredDataTypeJson = new JsonObject();
				structuredDataTypesJson.add(typeDeclaration.getId(),
						structuredDataTypeJson);

				structuredDataTypeJson.addProperty(ModelerConstants.ID_PROPERTY,
						typeDeclaration.getId());
				structuredDataTypeJson.addProperty(ModelerConstants.NAME_PROPERTY,
						typeDeclaration.getName());
				// TODO Review why different from other descriptions
				structuredDataTypeJson.addProperty(DESCRIPTION_PROPERTY,
						typeDeclaration.getDescription());

				JsonObject typeDeclarationJson = new JsonObject();
				structuredDataTypeJson.add(TYPE_DECLARATION_PROPERTY,
						typeDeclarationJson);
				JsonObject childrenJson = new JsonObject();
				typeDeclarationJson.add("children", childrenJson);

				// TODO Review code below, very heuristic ...

				SchemaTypeType schemaType = typeDeclaration.getSchemaType();

				if (schemaType != null) {
					org.eclipse.xsd.XSDSchema xsdSchema = schemaType
							.getSchema();

					// Determine prefix

					String prefix = null;

					for (Iterator iterator = xsdSchema
							.getQNamePrefixToNamespaceMap().keySet().iterator(); iterator
							.hasNext();) {
						String key = (String) iterator.next();
						String value = xsdSchema.getQNamePrefixToNamespaceMap()
								.get(key);

						if (value.equals(xsdSchema.getTargetNamespace())) {
							prefix = key;

							break;
						}
					}

					typeDeclarationJson.addProperty(ModelerConstants.NAME_PROPERTY, prefix + ":"
							+ typeDeclaration.getId());

					for (org.eclipse.xsd.XSDTypeDefinition xsdTypeDefinition : xsdSchema
							.getTypeDefinitions()) {

						if (xsdTypeDefinition.getName().equals(
								typeDeclaration.getId())) {

							if (xsdTypeDefinition.getComplexType() != null) {

								typeDeclarationJson.addProperty(TYPE_PROPERTY,
										"STRUCTURE_TYPE");

								for (int n = 0; n < xsdTypeDefinition
										.getComplexType().getElement()
										.getChildNodes().getLength(); ++n) {
									Node node = xsdTypeDefinition
											.getComplexType().getElement()
											.getChildNodes().item(n);
									JsonObject schemaElementJson = new JsonObject();

									schemaElementJson.addProperty(
											ModelerConstants.NAME_PROPERTY, node.getAttributes()
													.getNamedItem("name")
													.getNodeValue());
									schemaElementJson.addProperty(
											"typeName",
											node.getAttributes()
													.getNamedItem("type")
													.getNodeValue());
									childrenJson.add(node.getAttributes()
											.getNamedItem("name")
											.getNodeValue(), schemaElementJson);
								}
							} else if (xsdTypeDefinition.getSimpleType() != null) {
								Node restriction = xsdTypeDefinition
										.getSimpleType().getElement()
										.getChildNodes().item(0);

								typeDeclarationJson.addProperty(TYPE_PROPERTY,
										"ENUMERATION_TYPE");

								for (int n = 0; n < restriction.getChildNodes()
										.getLength(); ++n) {
									Node node = restriction.getChildNodes()
											.item(n);
									JsonObject schemaElementJson = new JsonObject();

									schemaElementJson.addProperty(
											ModelerConstants.NAME_PROPERTY, node.getAttributes()
													.getNamedItem("value")
													.getNodeValue());
									schemaElementJson.addProperty("typeName",
											"xsd:string");
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
	 *
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	private JsonObject loadData(ModelType model, DataType data) {
		JsonObject dataJson = new JsonObject();

		dataJson.addProperty(ModelerConstants.ID_PROPERTY, data.getId());
		dataJson.addProperty(ModelerConstants.NAME_PROPERTY, data.getName());
		loadDescription(dataJson, data);
		if(data.getType() != null)
		{
		   dataJson.addProperty(TYPE_PROPERTY, data.getType().getId());
		}

		return dataJson;
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @return
	 */
	public String undo(String modelId, String processId) {
		try {
			// TODO For testing only

			JsonObject undoJson = new JsonObject();

			undoJson.add("command", undoCommand());
			undoJson.addProperty("hasMoreToUndo", hasMoreToUndo());
			undoJson.addProperty("hasMoreToRedo", hasMoreToRedo());

			return undoJson.toString();

			// ModelType model =
			// getModelManagementStrategy().getModels().get(modelId);
			// ProcessDefinitionType processDefinition = ModelBuilderFascade.findProcessDefinition(
			// model, processId);
			// EditingSession editSession = getEditingSession(model,
			// processDefinition);
			// Modification modification = editSession.undoLast();
			//
			// String result = createUndoResponse(modification);
			//
			// /*
			// * for(Entry<EObject, EList<FeatureChange>> entry :
			// * modification.getChangeDescription
			// * ().getObjectChanges().entrySet()) {
			// * System.out.println("EObject details : " + entry.getKey());
			// * for(FeatureChange change : entry.getValue()) {
			// * System.out.println("Change details : ");
			// * System.out.println("change.getFeatureName() : " +
			// * change.getFeatureName());
			// * System.out.println("change.getDataValue() : " +
			// * change.getDataValue());
			// System.out.println("change.getValue() : "
			// * + change.getValue()); } }
			// *
			// * System.out.println("Detached object details : ");
			// EList<EObject>
			// * objectsToDetach =
			// * modification.getChangeDescription().getObjectsToDetach();
			// * for(EObject obj : objectsToDetach) {
			// System.out.println("EObj = "
			// * + obj); }
			// *
			// * System.out.println("Attached object details : ");
			// EList<EObject>
			// * objectsToAttach =
			// * modification.getChangeDescription().getObjectsToAttach();
			// * for(EObject obj : objectsToAttach) {
			// System.out.println("EObj = "
			// * + obj); }
			// */
			//
			// return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @return
	 */
	public String redo(String modelId, String processId) {
		try {
			// TODO For testing only

			JsonObject redoJson = new JsonObject();

			redoJson.add("command", redoCommand());
			redoJson.addProperty("hasMoreToUndo", hasMoreToUndo());
			redoJson.addProperty("hasMoreToRedo", hasMoreToRedo());

			return redoJson.toString();
		} catch (Exception e) {
			e.printStackTrace();

			return "{}";
		}
		// ModelType model =
		// getModelManagementStrategy().getModels().get(modelId);
		// ProcessDefinitionType processDefinition =
		// ModelBuilderFascade.findProcessDefinition(model,
		// processId);
		// EditingSession editSession = getEditingSession(model,
		// processDefinition);
		// Modification modification = editSession.redoNext();
		//
		// String result = createRedoResponse(modification);
		//
		// /*
		// * for(Entry<EObject, EList<FeatureChange>> entry :
		// * modification.getChangeDescription().getObjectChanges().entrySet())
		// {
		// * System.out.println("EObject details : " + entry.getKey());
		// * for(FeatureChange change : entry.getValue()) {
		// * System.out.println("Change details : ");
		// * System.out.println("change.getFeatureName() : " +
		// * change.getFeatureName());
		// * System.out.println("change.getDataValue() : " +
		// * change.getDataValue()); System.out.println("change.getValue() : " +
		// * change.getValue()); } }
		// *
		// * System.out.println("Detached object details : "); EList<EObject>
		// * objectsToDetach =
		// * modification.getChangeDescription().getObjectsToDetach();
		// for(EObject
		// * obj : objectsToDetach) { System.out.println("EObj = " + obj); }
		// *
		// * /*System.out.println("Attached object details : "); EList<EObject>
		// * objectsToAttach =
		// * modification.getChangeDescription().getObjectsToAttach();
		// for(EObject
		// * obj : objectsToAttach) { System.out.println("EObj = " + obj); }
		// */
		//
		// return result;
	}

	private String createRedoResponse(Modification modification) {
		// String result = "{}";
		// if (modification != null) {
		// ModelValue resultValue = new ModelValue();
		// // check detach section for redo undo create -
		// EList<EObject> objectsToDetach = modification
		// .getChangeDescription().getObjectsToDetach();
		// for (EObject eObj : objectsToDetach) {
		// if (eObj instanceof ActivityType) {
		// resultValue.setType("activity");
		// resultValue.setAction("Create");
		// resultValue.setId(((ActivityType) eObj).getId());
		// resultValue.setName(((ActivityType) eObj).getName());
		// } else if (eObj instanceof ActivitySymbolType) {
		// resultValue.getAttrs().put(
		// "xPos",
		// String.valueOf(((ActivitySymbolType) eObj)
		// .getXPos()));
		// resultValue.getAttrs().put(
		// "yPos",
		// String.valueOf(((ActivitySymbolType) eObj)
		// .getYPos()));
		// } else if (eObj instanceof TransitionType) {
		// TransitionType obj = (TransitionType) eObj;
		// resultValue.setType("connector");
		// resultValue.setAction("Create");
		// resultValue.setId(((TransitionType) eObj).getId());
		// resultValue.setName(((TransitionType) eObj).getName());
		//
		// resultValue.getAttrs().put("fromId", obj.getFrom().getId());
		// resultValue.getAttrs().put("toId", obj.getTo().getId());
		// }
		// }
		//
		// result = getGson().toJson(resultValue);
		// }
		//
		// return result;
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 * @param modification
	 * @return
	 */
	private String createUndoResponse(Modification modification) {
		// String result = "{}";
		//
		// if (modification != null) {
		// ModelValue resultValue = new ModelValue();
		// // check attach section for undo create -
		// EList<EObject> objectsToAttach = modification
		// .getChangeDescription().getObjectsToAttach();
		// for (EObject eObj : objectsToAttach) {
		// if (eObj instanceof ActivityType) {
		// resultValue.setType("activity");
		// resultValue.setAction("delete");
		// resultValue.setId(((ActivityType) eObj).getId());
		// } else if (eObj instanceof TransitionType) {
		// resultValue.setType("connector");
		// resultValue.setAction("delete");
		// resultValue.setId(((TransitionType) eObj).getId());
		// }
		// }
		// result = getGson().toJson(resultValue);
		// }
		//
		// return result;
		throw new UnsupportedOperationException();
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public String createData(String modelId, JsonObject commandJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
	    JsonObject dataJson = commandJson.getAsJsonObject(NEW_OBJECT_PROPERTY);
        String stripFullId_ = MBFacade.getModelId(extractString(
              dataJson,
              STRUCTURED_DATA_TYPE_FULL_ID));
        if(StringUtils.isEmpty(stripFullId_))
        {
           stripFullId_ = modelId;
        }
		synchronized (model) {

			long maxOid = XpdlModelUtils.getMaxUsedOid(model);

			DataType data;
            String id = MBFacade.stripFullId(extractString(dataJson, ModelerConstants.ID_PROPERTY));
            String name = MBFacade.stripFullId(extractString(dataJson, ModelerConstants.NAME_PROPERTY));
            String typeKey = extractString(dataJson, TYPE_PROPERTY);
            String primitiveType = extractString(dataJson, ModelerConstants.PRIMITIVE_TYPE);
            String structuredDataFullId = MBFacade.stripFullId(extractString(dataJson,
                  STRUCTURED_DATA_TYPE_FULL_ID));

			if (primitiveType != null && primitiveType.equals(
			      ModelerConstants.PRIMITIVE_DATA_TYPE_KEY)) {
			   MBFacade.createPrimitiveData(model, id, name, primitiveType);
			} else if (typeKey.equals(
			      ModelerConstants.STRUCTURED_DATA_TYPE_KEY)) {

            id = extractString(dataJson, ModelerConstants.ID_PROPERTY);
            name = extractString(dataJson, ModelerConstants.NAME_PROPERTY);
            MBFacade.createStructuredData(model, stripFullId_, id, name, structuredDataFullId);
			}

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
	public String createDataSymbol(String modelId, String processId,
			JsonObject commandJson) {
		JsonObject dataSymbolJson = commandJson
				.getAsJsonObject(NEW_OBJECT_PROPERTY);
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);
		String dataFullID = extractString(dataSymbolJson,
              ModelerConstants.DATA_FULL_ID_PROPERTY);
		String dataID = extractString(dataSymbolJson, ModelerConstants.DATA_ID_PROPERTY);
		String dataName = extractString(dataSymbolJson, ModelerConstants.DATA_NAME_PROPERTY);
		int xProperty = extractInt(dataSymbolJson, X_PROPERTY);
		int yProperty = extractInt(dataSymbolJson, Y_PROPERTY);
		int widthProperty = extractInt(dataSymbolJson, WIDTH_PROPERTY);
		int heightProperty = extractInt(dataSymbolJson, HEIGHT_PROPERTY);
		String parentSymbolID = extractString(dataSymbolJson, PARENT_SYMBOL_ID_PROPERTY);

		synchronized (model) {
			editSession.beginEdit();

			long maxOid = XpdlModelUtils.getMaxUsedOid(model);

			DataType data;

			try {
				data = MBFacade.getDataFromExistingModel(modelId, model, dataFullID);
			} catch (ObjectNotFoundException x) {
				if (true) {
					data = MBFacade.createNewPrimitive(model, dataID, dataName);

					JsonObject dataJson = loadData(model, data);

					dataSymbolJson.add("data", dataJson);
					dataSymbolJson.addProperty("dataFullId",
					      MBFacade.createFullId(model, data));
				}
			}

			DataSymbolType dataSymbol = MBFacade.createDataSymbol(processDefinition, xProperty,
               yProperty, widthProperty, heightProperty, parentSymbolID, maxOid, data);

            dataSymbolJson
            .addProperty(OID_PROPERTY, dataSymbol.getElementOid());
			editSession.endEdit();
		}

		return commandJson.toString();
	}



	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param dataId
	 * @param postedData
	 * @return
	 */
	public String updateData(String modelId, String processId, String dataId,
			JsonObject dataSymbolJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		DataType data = MBFacade.findData(model, dataId);

		/*
      ModelType typeDeclarationModel = getModelManagementStrategy().getModels().get(typeDeclarationModelId);
      BpmStructVariableBuilder structVariable = newStructVariable(model);
      structVariable.setData(data);
      structVariable.setTypeDeclarationModel(typeDeclarationModel);
      */

		DataSymbolType dataSymbol = (DataSymbolType) data.getSymbols().get(0);
		EditingSession editSession = getEditingSession(model);

		synchronized (model) {
			editSession.beginEdit();

			dataSymbolJson = updateData(dataSymbol, dataSymbolJson);

			editSession.endEdit();
		}

		return dataSymbolJson.toString();
	}

	/**
	 *
	 * @param dataSymbol
	 * @param dataSymbolJson
	 * @return
	 */
	private JsonObject updateData(DataSymbolType dataSymbol,
			JsonObject dataSymbolJson) {
		// JSONObject dataJson = dataSymbolJson
		// .getJSONObject(MODEL_ELEMENT_PROPERTY);
		// DataType data = dataSymbol.getData();
		//
		// data.setName(dataSymbolJson.getString(NAME_PROPERTY));
		// setDescription(data, dataSymbolJson.getString(DESCRIPTION_PROPERTY));

		dataSymbol.setXPos(extractInt(dataSymbolJson, X_PROPERTY));
		dataSymbol.setYPos(extractInt(dataSymbolJson, Y_PROPERTY));

		return dataSymbolJson;
	}

	/**
	 *
	 * @param modelId
	 * @param processId
	 * @param postedData
	 * @return
	 */
	public String dropDataSymbol(String modelId, String processId,
			JsonObject dataSymbolJson) {
		ModelType model = getModelManagementStrategy().getModels().get(modelId);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				processId);
		EditingSession editSession = getEditingSession(model);

		synchronized (model) {
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
	public String createWrapperProcess(String modelId, JsonObject json) {
		ModelType model = getModel(modelId);
		long maxOid = XpdlModelUtils.getMaxUsedOid(model);

		// Create process definition

		System.out.println(json);

		JsonObject wizardParameterJson = (JsonObject) json
				.get(NEW_OBJECT_PROPERTY);
		JsonObject processDefinitionJson = (JsonObject) createProcessJson(
				modelId, json);
		ProcessDefinitionType processDefinition = MBFacade.findProcessDefinition(model,
				extractString(json, NEW_OBJECT_PROPERTY, ModelerConstants.ID_PROPERTY));
		LaneSymbol parentLaneSymbol = MBFacade.findLaneInProcess(processDefinition,
				ModelerConstants.DEF_LANE_ID);

		// Create Start Event

		StartEventSymbol startEventSymbol = AbstractElementBuilder.F_CWM
				.createStartEventSymbol();
		startEventSymbol.setElementOid(++maxOid);

		startEventSymbol.setXPos(250);
		startEventSymbol.setYPos(50);

		processDefinition.getDiagram().get(0).getStartEventSymbols()
				.add(startEventSymbol);
		parentLaneSymbol.getStartEventSymbols().add(startEventSymbol);

		// Request data

		// DataTypeType structuredDataType = AbstractElementBuilder.F_CWM
		// .createDataTypeType();
		//
		// structuredDataType.setId(id + "Request");
		// structuredDataType.setName(name + " Request");
		//
		// model.getDataType().add(structuredDataType);

		DataType data = newStructVariable(model)
				.withIdAndName(
						MBFacade.createIdFromName(extractString(wizardParameterJson,
								"requestParameterDataNameInput")),
						extractString(wizardParameterJson,
								"requestParameterDataNameInput"))
				.ofType(/* Dummy */MBFacade.stripFullId(extractString(
						wizardParameterJson, "serviceRequestParameterTypeId")))
				.build();

		model.getData().add(data);

		DataSymbolType dataSymbol = AbstractElementBuilder.F_CWM
				.createDataSymbolType();

		dataSymbol.setElementOid(++maxOid);
		dataSymbol.setData(data);
		processDefinition.getDiagram().get(0).getDataSymbol().add(dataSymbol);
		data.getDataSymbols().add(dataSymbol);
		dataSymbol.setXPos(100);
		dataSymbol.setYPos(50);

		parentLaneSymbol.getDataSymbol().add(dataSymbol);

		// Create Request Transformation Activity

		ActivityType activity = newApplicationActivity(processDefinition)
				.withIdAndName(
						MBFacade.createIdFromName(extractString(wizardParameterJson,
								"requestTransformationActivityName")),
						extractString(wizardParameterJson,
								"requestTransformationActivityName"))
				.invokingApplication(
						MBFacade.getApplication(
								modelId,
								extractString(wizardParameterJson,
										"applicationId"))).build();

		// setDescription(activity,
		// "Invocation of wrapped application.");

		ActivitySymbolType activitySymbol = AbstractElementBuilder.F_CWM
				.createActivitySymbolType();

		activitySymbol.setElementOid(++maxOid);

		activitySymbol.setXPos(200);
		activitySymbol.setYPos(100);
		activitySymbol.setWidth(180);
		activitySymbol.setHeight(50);
		activitySymbol.setActivity(activity);
		activity.getActivitySymbols().add(activitySymbol);

		processDefinition.getDiagram().get(0).getActivitySymbol()
				.add(activitySymbol);
		parentLaneSymbol.getActivitySymbol().add(activitySymbol);

		// Request data

		data = newStructVariable(model)
				.withIdAndName(MBFacade.createIdFromName("Service Request"),
						"Service Request")
				.ofType(MBFacade.stripFullId(extractString(wizardParameterJson,
						"serviceRequestParameterTypeId"))).build();

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

		activity = newApplicationActivity(processDefinition)
				.withIdAndName(
						MBFacade.createIdFromName(extractString(wizardParameterJson,
								"serviceInvocationActivityName")),
						extractString(wizardParameterJson,
								"serviceInvocationActivityName"))
				.invokingApplication(
						MBFacade.getApplication(
								modelId,
								extractString(wizardParameterJson,
										"applicationId"))).build();

		// setDescription(activity,
		// "Invocation of wrapped application.");

		activitySymbol = AbstractElementBuilder.F_CWM
				.createActivitySymbolType();

		activitySymbol.setElementOid(++maxOid);

		activitySymbol.setXPos(200);
		activitySymbol.setYPos(200);
		activitySymbol.setWidth(180);
		activitySymbol.setHeight(50);
		activitySymbol.setActivity(activity);
		activity.getActivitySymbols().add(activitySymbol);

		processDefinition.getDiagram().get(0).getActivitySymbol()
				.add(activitySymbol);
		parentLaneSymbol.getActivitySymbol().add(activitySymbol);

		// Response data

		data = newStructVariable(model)
				.withIdAndName(MBFacade.createIdFromName("Service Response"),
						"Service Response")
				.ofType(MBFacade.stripFullId(extractString(wizardParameterJson,
						"serviceResponseParameterTypeId"))).build();

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

		activity = newApplicationActivity(processDefinition)
				.withIdAndName(
						MBFacade.createIdFromName(extractString(wizardParameterJson,
								"responseTransformationActivityName")),
						extractString(wizardParameterJson,
								"responseTransformationActivityName"))
				.invokingApplication(
						MBFacade.getApplication(
								modelId,
								extractString(wizardParameterJson,
										"applicationId"))).build();

		// setDescription(activity,
		// "Invocation of wrapped application.");

		activitySymbol = AbstractElementBuilder.F_CWM
				.createActivitySymbolType();

		activitySymbol.setElementOid(++maxOid);

		activitySymbol.setXPos(200);
		activitySymbol.setYPos(300);
		activitySymbol.setWidth(180);
		activitySymbol.setHeight(50);
		activitySymbol.setActivity(activity);
		activity.getActivitySymbols().add(activitySymbol);

		processDefinition.getDiagram().get(0).getActivitySymbol()
				.add(activitySymbol);
		parentLaneSymbol.getActivitySymbol().add(activitySymbol);

		// Create Response Data

		// structuredDataType = AbstractElementBuilder.F_CWM
		// .createDataTypeType();
		//
		// structuredDataType.setId(id + "Response");
		// structuredDataType.setName(name + " Response");
		//
		// model.getDataType().add(structuredDataType);

		data = newStructVariable(model)
				.withIdAndName(
						MBFacade.createIdFromName(extractString(wizardParameterJson,
								"responseParameterDataNameInput")),
						extractString(wizardParameterJson,
								"responseParameterDataNameInput"))
				.ofType(/* Dummy */MBFacade.stripFullId(extractString(
						wizardParameterJson, "serviceResponseParameterTypeId")))
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

		EndEventSymbol endEventSymbol = AbstractElementBuilder.F_CWM
				.createEndEventSymbol();
		endEventSymbol.setElementOid(++maxOid);

		endEventSymbol.setXPos(250);
		endEventSymbol.setYPos(400);

		processDefinition.getDiagram().get(0).getEndEventSymbols()
				.add(endEventSymbol);

		parentLaneSymbol.getEndEventSymbols().add(endEventSymbol);

		processDefinitionJson.addProperty("scope", "all");

		return processDefinitionJson.toString();
	}

	/**
	 *
	 */
	public String createDocumentation(String modelId, JsonObject json) {
		return createModelElementDocumentation(modelId, json);
	}

	/**
	 *
	 */
	public String createDocumentation(String modelId, String processId,
			JsonObject json) {
		return createModelElementDocumentation(modelId + "-" + processId, json);
	}

	/**
	 *
	 */
	private String createModelElementDocumentation(String pathPrefix,
			JsonObject json) {

		// TODO Make folder structure

		String fileName = pathPrefix + "-" + extractString(json, "id")
				+ ".html";

		DocumentInfo documentInfo = DmsUtils.createDocumentInfo(fileName);
		documentInfo.setOwner(getServiceFactory().getWorkflowService()
				.getUser().getAccount());
		documentInfo.setContentType(MimeTypesHelper.HTML.getType());
		Document document = getDocumentManagementService().getDocument(
				MODELING_DOCUMENTS_DIR + fileName);

		if (null == document) {
			document = getDocumentManagementService().createDocument(
					MODELING_DOCUMENTS_DIR,
					documentInfo,
					replaceProperties("", json, getTemplateContent("activity"))
							.getBytes(), null);

			getDocumentManagementService().versionDocument(document.getId(),
					null);
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
	private String getTemplateContent(String elementType) {
		Document document = getDocumentManagementService().getDocument(
				MODEL_DOCUMENTATION_TEMPLATES_FOLDER + elementType
						+ "-template.html");

		// Try extension ".htm"

		if (document == null) {
			getDocumentManagementService().getDocument(
					MODEL_DOCUMENTATION_TEMPLATES_FOLDER + elementType
							+ "-template.html");
		}

		if (document != null) {
			return new String(getDocumentManagementService()
					.retrieveDocumentContent(document.getId()));
		}

		return "";
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
	private UserService getUserService() {
		if (userService == null) {
			userService = getServiceFactory()
					.getUserService();
		}

		return userService;
	}

	/**
	 *
	 * @return
	 */
	private QueryService getQueryService() {
		if (queryService == null) {
			queryService = getServiceFactory()
					.getQueryService();
		}

		return queryService;
	}

	/**
	 */
	private String replaceProperties(String path, JsonObject json,
			String content) {
		if (path.length() > 0) {
			path += ".";
		}

		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
			String key = entry.getKey();
			JsonElement value = entry.getValue();

			if (value != null) {
				if (value.isJsonObject()) {
					content = replaceProperties(path + key,
							value.getAsJsonObject(), content);
				} else {
					content = content.replace("#{" + path + key + "}",
							value.toString());
				}
			}
		}

		return content;
	}

	public ProcessDefinitionType findProcessDefinition(ModelType model, String id) {
	   return MBFacade.findProcessDefinition(model, id);
	}

	public ModelType findModel(String modelId)
	{
	    return MBFacade.findModel(modelId);
	}
}
