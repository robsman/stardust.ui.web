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

package org.eclipse.stardust.ui.web.business_object_management.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.BusinessObjectDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Definition;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.*;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations.XPathAnnotation;
import org.eclipse.stardust.ui.web.business_object_management.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
//import org.eclipse.stardust.ui.web.viewscommon.utils.XPathCacheManager;

import com.google.gson.*;

public class BusinessObjectManagementService {
	private static final Logger trace = LogManager
			.getLogger(BusinessObjectManagementService.class);
	@Resource
	private SessionContext sessionContext;

	// @Resource(name = XPathCacheManager.BEAN_ID)
	// private XPathCacheManager xPathCacheManager;

	private DocumentManagementService documentManagementService;
	private UserService userService;
	private QueryService queryService;
	private WorkflowService workflowService;
	private AdministrationService administrationService;

	// TODO Removed as soon as real implementation is complete

	private static final String MOCK_MODE = "MOCK_MODE";
	private static final String PRODUCTION_MODE = "PRODUCTION_MODE";

	private String mode = MOCK_MODE;
	private JsonArray testModelsJson;
	private Map<String, JsonObject> members;
	private Map<String, JsonObject> funds;

	/**
	 * 
	 */
	public BusinessObjectManagementService() {
		super();

		new JsonMarshaller(); // TODO What is this

		if (mode == MOCK_MODE) {
			initializeMockMode();
		}
	}

	/**
	 * 
	 * @return
	 */
	private ServiceFactory getServiceFactory() {
		return sessionContext.getServiceFactory();
	}

	/**
	 *
	 * @return
	 */
	DocumentManagementService getDocumentManagementService() {
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
	private AdministrationService getAdministrationService() {
		if (administrationService == null) {
			administrationService = getServiceFactory()
					.getAdministrationService();
		}

		return administrationService;
	}

	/**
	 * 
	 */
	private void initializeMockMode() {
		testModelsJson = new JsonArray();

		JsonArray businessObjectsJson = new JsonArray();

		businessObjectsJson.add(getMemberTestJson());

		JsonObject modelJson = new JsonObject();

		modelJson.addProperty("oid", 1);
		modelJson.addProperty("id", "GeneralClaimProcessing");
		modelJson.addProperty("name", "General Claim Processing");
		modelJson.add("businessObjects", businessObjectsJson);

		testModelsJson.add(modelJson);

		businessObjectsJson = new JsonArray();

		businessObjectsJson.add(getFundTestJson());

		modelJson = new JsonObject();

		modelJson.addProperty("oid", 2);
		modelJson.addProperty("id", "DailyFundProcessing");
		modelJson.addProperty("name", "Daily Fund Processing");
		modelJson.add("businessObjects", businessObjectsJson);

		testModelsJson.add(modelJson);

		members = new HashMap<String, JsonObject>();

		members.put(
				"4711",
				getMemberInstance("4711", "Haile", "Selassie", "1", "Scheme-1",
						"SA"));
		members.put(
				"0815",
				getMemberInstance("0815", "Jan", "Smuts", "2", "Scheme-2", "SA"));

		funds = new HashMap<String, JsonObject>();

		funds.put("Europe Top 100",
				getFundInstance("Europe Top 100", "Europe Top 100", "Mixed"));
		funds.put(
				"Asia Fixed Income 200",
				getFundInstance("Asia Fixed Income 200",
						"Asia Fixed Income 200", "Fixed Income"));
		funds.put(
				"Africa Sub-Saharan Real Estate",
				getFundInstance("Africa Sub-Saharan Real Estate",
						"Africa Sub-Saharan Real Estate", "Private Equity"));
	}

	/**
	 * Returns the type definition of all process data across models with an SDT
	 * type.
	 *
	 * @return
	 */
	public JsonObject getBusinessObjects() {
		JsonArray modelsJson = new JsonArray();

		if (mode == MOCK_MODE) {
			modelsJson.addAll(this.testModelsJson);
		}

		ModelCache modelCache = ModelCache.findModelCache();

		if (modelCache == null) {
			fillDescriptionsFromQuery(modelsJson);
		} else {
			fillDescriptionsFromLocalCache(modelsJson, modelCache);
		}

		JsonObject resultJson = new JsonObject();
		resultJson.add("models", modelsJson);
		return resultJson;
	}

	/**
	 * 
	 * @param modelOid
	 * @param businessObjectId
	 * @return
	 */
	public JsonObject getBusinessObject(String modelOid, String businessObjectId) {
		// Caters for mock and production mode

		JsonArray modelsJson = getBusinessObjects().get("models")
				.getAsJsonArray();

		for (int n = 0; n < modelsJson.size(); ++n) {
			JsonObject modelJson = modelsJson.get(n).getAsJsonObject();

			if (modelJson.get("oid").getAsString().equals(modelOid)) {
				JsonArray businessObjects = modelJson.get("businessObjects")
						.getAsJsonArray();

				for (int m = 0; m < businessObjects.size(); ++m) {
					JsonObject businessObject = businessObjects.get(m)
							.getAsJsonObject();

					if (businessObject.get("id").getAsString()
							.equals(businessObjectId)) {
						return businessObject;
					}
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @return
	 */
	private JsonObject getMemberTestJson() {
		JsonArray memberFieldsJson = new JsonArray();
		memberFieldsJson.add(toTestField("id", "Member ID", "string", true,
				true));
		memberFieldsJson.add(toTestField("firstName", "First Name", "string",
				true, false));
		memberFieldsJson.add(toTestField("lastName", "Last Name", "string",
				true, false));
		memberFieldsJson.add(toTestField("dateOfBirth", "Date of Birth",
				"date", true, false));
		memberFieldsJson.add(toTestField("amlChecked", "AML Checked",
				"boolean", false, false));
		memberFieldsJson.add(toTestField("numberOfDependents",
				"Number of Dependents", "integer", false, false));
		memberFieldsJson.add(toTestField("annualSalary", "Annual Salary",
				"decimal", false, false));
		memberFieldsJson.add(toTestField("scheme", "Scheme Name", "string",
				false, false));
		memberFieldsJson.add(toTestField("schemeId", "Scheme ID", "string",
				false, false));
		memberFieldsJson.add(toTestField("nationalId", "National ID", "string",
				false, false));
		memberFieldsJson.add(toTestField("address", "Address", "Address",
				false, false));
		memberFieldsJson.add(toTestToManyField("policies", "Policies",
				"Policy", false, false));

		JsonObject memberJson = new JsonObject();

		memberJson.addProperty("id", "Member");
		memberJson.addProperty("name", "Member");
		memberJson.add("fields", memberFieldsJson);

		JsonObject typesJson = new JsonObject();

		memberJson.add("types", typesJson);

		JsonObject addressJson = new JsonObject();

		typesJson.add("Address", addressJson);

		JsonArray fieldsJson = new JsonArray();

		addressJson.add("fields", fieldsJson);

		fieldsJson.add(toTestField("addressline1", "Addressline 1", "string",
				false, false));
		fieldsJson.add(toTestField("addressline2", "Addressline 2", "string",
				false, false));
		fieldsJson.add(toTestField("city", "City", "string", false, false));
		fieldsJson.add(toTestField("zipCode", "ZIP Code", "string", false,
				false));

		JsonObject policyJson = new JsonObject();

		typesJson.add("Policy", policyJson);

		fieldsJson = new JsonArray();

		policyJson.add("fields", fieldsJson);

		fieldsJson.add(toTestField("id", "ID", "string", false, false));
		fieldsJson.add(toTestField("name", "Name", "string", false, false));
		fieldsJson.add(toTestField("monthlyFee", "Monthly Fee", "decimal",
				false, false));
		fieldsJson.add(toTestField("closingDate", "Closing Date", "date",
				false, false));
		fieldsJson.add(toTestField("review", "Rate Review", "boolean", false,
				false));

		return memberJson;
	}

	/**
	 *
	 * @return
	 */
	private JsonObject getFundTestJson() {
		JsonArray fundFieldsJson = new JsonArray();

		fundFieldsJson.add(toTestField("id", "Id", "string", true, true));
		fundFieldsJson.add(toTestField("name", "Name", "string", true, false));
		fundFieldsJson.add(toTestField("type", "Type", "string", true, false));

		JsonObject fundJson = new JsonObject();

		fundJson.addProperty("id", "Fund");
		fundJson.addProperty("name", "Fund");
		fundJson.add("fields", fundFieldsJson);

		return fundJson;
	}

	/**
	 *
	 * @param id
	 * @param name
	 * @param type
	 * @param key
	 * @param primaryKey
	 * @return
	 */
	private JsonObject toTestField(String id, String name, String type,
			boolean key, boolean primaryKey) {
		return toTestField(id, name, type, key, primaryKey, false);
	}

	/**
	 *
	 * @param id
	 * @param name
	 * @param type
	 * @param key
	 * @param primaryKey
	 * @return
	 */
	private JsonObject toTestToManyField(String id, String name, String type,
			boolean key, boolean primaryKey) {
		return toTestField(id, name, type, key, primaryKey, true);
	}

	/**
	 *
	 * @param id
	 * @param name
	 * @param type
	 * @param key
	 * @param primaryKey
	 * @return
	 */
	private JsonObject toTestField(String id, String name, String type,
			boolean key, boolean primaryKey, boolean isList) {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json.addProperty("name", name);
		json.addProperty("type", type);
		json.addProperty("key", key);
		json.addProperty("primaryKey", primaryKey);
		json.addProperty("list", isList);

		return json;
	}

	/**
	 * 
	 * @param testModelsJson
	 * @param modelCache
	 */
	private void fillDescriptionsFromLocalCache(JsonArray modelsJson,
			ModelCache modelCache) {
		for (DeployedModel deployedModel : modelCache.getAllModels()) {
			if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(deployedModel
					.getId())) {
				JsonArray businessObjectsJson = new JsonArray();
				@SuppressWarnings("unchecked")
				List<Data> allData = (List<Data>) deployedModel.getAllData();
				for (Data data : allData) {
					String pkAttribute = (String) data
							.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
					if (PredefinedConstants.STRUCTURED_DATA.equals(data
							.getTypeId()) && !StringUtils.isEmpty(pkAttribute)) {
						String typeDeclarationId = (String) data
								.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
						DeployedModel model = modelCache.getModel(data
								.getModelOID());
						Reference ref = data.getReference();
						if (ref != null) {
							model = modelCache.getModel(ref.getModelOid());
							typeDeclarationId = ref.getId();
						}
						TypeDeclaration typeDeclaration = model
								.getTypeDeclaration(typeDeclarationId);
						Set<TypedXPath> xPaths = StructuredTypeRtUtils
								.getAllXPaths(model, typeDeclaration);
						for (TypedXPath xPath : xPaths) {
							if (xPath.getXPath().isEmpty()) // root xpath
							{
								businessObjectsJson.add(toJson(data, xPath,
										deployedModel.getModelOID()));
								break;
							}
						}
					}
				}
				if (businessObjectsJson.size() > 0) {
					JsonObject modelJson = new JsonObject();
					modelJson.addProperty("oid", deployedModel.getModelOID());
					modelJson.addProperty("id", deployedModel.getId());
					modelJson.addProperty("name", deployedModel.getName());
					modelJson.add("businessObjects", businessObjectsJson);
					modelsJson.add(modelJson);
				}
			}
		}
	}

	/**
	 * 
	 * @param testModelsJson
	 */
	private void fillDescriptionsFromQuery(JsonArray modelsJson) {
		BusinessObjectQuery query = BusinessObjectQuery.findAll();
		query.setPolicy(new BusinessObjectQuery.Policy(
				BusinessObjectQuery.Option.WITH_DESCRIPTION));

		Map<Long, JsonObject> modelsMap = CollectionUtils.newMap();
		BusinessObjects bos = getQueryService().getBusinessObjects(query);

		for (BusinessObject bo : bos) {
			JsonArray businessObjectsJson = null;
			JsonObject modelJson = modelsMap.get(bo.getModelOid());
			if (modelJson == null) {
				modelJson = new JsonObject();
				modelJson.addProperty("oid", bo.getModelOid());
				businessObjectsJson = new JsonArray();
				modelJson.add("businessObjects", businessObjectsJson);
				modelsJson.add(modelJson);
			} else {
				businessObjectsJson = modelJson
						.getAsJsonArray("businessObjects");
			}

			businessObjectsJson.add(toJson(bo));
		}
	}

	/**
	 * 
	 * @param data
	 * @param xPath
	 * @param modelOid
	 * @return
	 */
	private JsonObject toJson(Data data, TypedXPath xPath, int modelOid) {
		String pkAttribute = (String) data
				.getAttribute(PredefinedConstants.PRIMARY_KEY_ATT);
		JsonObject json = new JsonObject();
		json.addProperty("id", data.getId());
		json.addProperty("name", data.getName());
		JsonObject types = new JsonObject();
		addStructuralInformation(json, xPath.getChildXPaths(), types,
				pkAttribute);
		if (types.entrySet().size() > 0) {
			json.add("types", types);
		}

		return json;
	}

	/**
	 * 
	 * @param bo
	 * @return
	 */
	private JsonObject toJson(BusinessObject bo) {
		JsonObject json = new JsonObject();
		json.addProperty("id", bo.getId());
		json.addProperty("name", bo.getName());
		JsonObject types = new JsonObject();
		json.add("fields", getStructuralInformation(bo.getItems(), types));
		if (types.entrySet().size() > 0) {
			json.add("types", types);
		}
		return json;
	}

	/**
	 * 
	 * @param items
	 * @param types
	 * @return
	 */
	private JsonArray getStructuralInformation(List<Definition> items,
			JsonObject types) {
		JsonArray fields = new JsonArray();
		if (items != null) {
			for (Definition definition : items) {
				fields.add(toJson((Definition) definition, types));
				if (definition.getType() == BigData.NULL) {
					String key = definition.getTypeName() == null ? "anonymous"
							+ (types.entrySet().size() + 1) : definition
							.getItems().isEmpty() ? definition.getTypeName()
							.getLocalPart() : definition.getTypeName()
							.toString();
					if (!types.has(key)) {
						JsonObject type = new JsonObject();
						List<Definition> childItems = definition.getItems();
						if (childItems != null && !childItems.isEmpty()) {
							type.add("fields",
									getStructuralInformation(childItems, types));
						}
						types.add(key, type);
					}

				}
			}
		}

		return fields;
	}

	/**
	 * 
	 * @param xPath
	 * @param types
	 * @param pkAttribute
	 * @return
	 */
	private JsonObject toJson(TypedXPath xPath, JsonObject types,
			String pkAttribute) {
		XPathAnnotation uiAnnotation = null;
		XPathAnnotations annotations = xPath.getAnnotations();
		for (XPathAnnotation annotation : annotations) {
			if (XPathAnnotations.IPP_ANNOTATIONS_NAMESPACE.equals(annotation
					.getNamespace()) && "ui".equals(annotation.getName())) {
				uiAnnotation = annotation;
				break;
			}
		}
		JsonObject json = new JsonObject();
		json.addProperty("id", xPath.getId());
		json.addProperty("name", xPath.getId()); // TODO fetch from annotations
		String key = StringUtils.isEmpty(xPath.getXsdTypeNs()) ? "anonymous"
				+ (types.entrySet().size() + 1)
				: xPath.getType() != BigData.NULL ? xPath.getXsdTypeName()
						: "{" + xPath.getXsdTypeNs() + "}"
								+ xPath.getXsdTypeName();
		json.addProperty("type", key);
		if (xPath.isList()) {
			json.addProperty("list", true);
		}
		if (annotations.isIndexed()) {
			json.addProperty("key", true);
		}
		if (pkAttribute != null && pkAttribute.equals(xPath.getId())) {
			json.addProperty("primaryKey", true);
		}
		if (uiAnnotation != null) {
			json.add("ui", toJson(uiAnnotation));
		}
		return json;
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	private JsonElement toJson(XPathAnnotation parent) {
		JsonObject json = new JsonObject();
		for (XPathAnnotation annotation : parent) {
			if (annotation.iterator().hasNext()) {
				json.add(annotation.getName(), toJson(annotation));
			} else {
				json.addProperty(annotation.getName(), annotation.getValue());
			}
		}
		return json;
	}

	/**
	 * 
	 * @param json
	 * @param xPaths
	 * @param types
	 * @param pkAttribute
	 */
	private void addStructuralInformation(JsonObject json,
			List<TypedXPath> xPaths, JsonObject types, String pkAttribute) {
		JsonArray fields = new JsonArray();
		if (xPaths != null) {
			for (TypedXPath xPath : xPaths) {
				fields.add(toJson(xPath, types, pkAttribute));
				if (xPath.getType() == BigData.NULL) {
					String key = StringUtils.isEmpty(xPath.getXsdTypeNs()) ? "anonymous"
							+ (types.entrySet().size() + 1)
							: StringUtils.isEmpty(xPath.getXsdTypeNs()) ? xPath
									.getXsdTypeName() : "{"
									+ xPath.getXsdTypeNs() + "}"
									+ xPath.getXsdTypeName();
					if (!types.has(key)) {
						JsonObject type = new JsonObject();
						List<TypedXPath> childXPaths = xPath.getChildXPaths();
						if (childXPaths != null && !childXPaths.isEmpty()) {
							addStructuralInformation(type, childXPaths, types,
									null);
						}
						types.add(key, type);
					}
				}
			}
		}
		json.add("fields", fields);
	}

	/**
	 * 
	 * @param definition
	 * @param types
	 * @return
	 */
	private JsonObject toJson(Definition definition, JsonObject types) {
		JsonObject json = new JsonObject();
		json.addProperty("id", definition.getName());
		json.addProperty("name", definition.getName());
		String key = definition.getTypeName() == null ? "anonymous"
				+ (types.entrySet().size() + 1) : definition.getItems()
				.isEmpty() ? definition.getTypeName().getLocalPart()
				: definition.getTypeName().toString();
		json.addProperty("type", key);
		json.addProperty("list", definition.isList());
		json.addProperty("key", definition.isKey());
		json.addProperty("primaryKey", definition.isPrimaryKey());
		return json;
	}

	/**
	 * Returns all distinct entries (distinguished by the primary key) of the
	 * structured_data_value table for a given Business Object (= Process Data).
	 *
	 * @param modelOid
	 * @param businessObjectId
	 * @return
	 */
	public JsonObject getBusinessObjectInstances(String modelOid,
			String businessObjectId) {
		JsonObject resultJson = new JsonObject();
		JsonArray businessObjectInstances = new JsonArray();

		resultJson.add("businessObjectInstances", businessObjectInstances);

		if (mode == MOCK_MODE) {
			// TODO More generic

			Map<String, JsonObject> map = null;

			if (businessObjectId.equals("Member")) {
				map = members;
			} else {
				map = funds;
			}

			for (JsonObject businessObjectInstance : map.values()) {
				businessObjectInstances.add(businessObjectInstance);
			}
		} else {
			addInstancesFromQuery(
					businessObjectInstances,
					BusinessObjectQuery.findForBusinessObject(
							Long.parseLong(modelOid), businessObjectId));
		}

		return resultJson;
	}

	/**
	 * 
	 * @param businessObjectInstances
	 * @param query
	 */
	private void addInstancesFromQuery(JsonArray businessObjectInstances,
			BusinessObjectQuery query) {
		query.setPolicy(new BusinessObjectQuery.Policy(
				BusinessObjectQuery.Option.WITH_VALUES));
		BusinessObjects bos = getQueryService().getBusinessObjects(query);

		for (BusinessObject bo : bos) {
			List<Value> values = bo.getValues();
			if (values != null) {
				for (Value value : values) {
					businessObjectInstances.add(toJson(value));
				}
			}
		}
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	private JsonElement toJson(Value value) {
		return value == null ? null : objectToJsonElement(value.getValue());
	}

	/**
	 * 
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param scheme
	 * @param schemeName
	 * @param nationalId
	 * @return
	 */
	private JsonObject getMemberInstance(String id, String firstName,
			String lastName, String scheme, String schemeName, String nationalId) {
		JsonObject member = new JsonObject();
		member.addProperty("id", id);
		member.addProperty("firstName", firstName);
		member.addProperty("lastName", lastName);
		member.addProperty("scheme", scheme);
		member.addProperty("schemeName", schemeName);
		member.addProperty("nationalId", nationalId);
		return member;
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @return
	 */
	private JsonObject getFundInstance(String id, String name, String type) {
		JsonObject fund = new JsonObject();

		fund.addProperty("id", id);
		fund.addProperty("name", name);
		fund.addProperty("type", type);

		return fund;
	}

	/**
	 *
	 * @param modelOid
	 * @param businessObjectId
	 * @param primaryKey
	 * @param json
	 * @return
	 */
	public JsonObject createBusinessObjectInstance(String modelOid,
			String businessObjectId, String primaryKey, JsonObject jsonObject) {
		if (mode == MOCK_MODE) {
			if (businessObjectId.equals("Member")) {
				members.put(jsonObject.get("id").getAsString(), jsonObject);
			} else if (businessObjectId.equals("Fund")) {
				funds.put(jsonObject.get("id").getAsString(), jsonObject);
			}

			return jsonObject;
		} else {
			BusinessObject boi = getWorkflowService()
					.createBusinessObjectInstance(
							Long.parseLong(modelOid),
							businessObjectId,
							new BusinessObjectDetails.ValueDetails(
									-1,
									(Serializable) jsonElementToObject(jsonObject)));
			return (JsonObject) toJson(getFirstValue(boi));
		}
	}

	/**
	 *
	 * @param modelOid
	 * @param businessObjectId
	 * @param primaryKey
	 * @param json
	 * @return
	 */
	public JsonObject updateBusinessObjectInstance(String modelOid,
			String businessObjectId, String primaryKey, JsonObject jsonObject) {
		if (mode == MOCK_MODE) {
			if (businessObjectId.equals("Member")) {
				members.put(jsonObject.get("id").getAsString(), jsonObject);
			} else if (businessObjectId.equals("Fund")) {
				funds.put(jsonObject.get("id").getAsString(), jsonObject);
			}

			return jsonObject;
		} else {
			BusinessObject boi = getWorkflowService()
					.createBusinessObjectInstance(
							Long.parseLong(modelOid),
							businessObjectId,
							new BusinessObjectDetails.ValueDetails(
									-1,
									(Serializable) jsonElementToObject(jsonObject)));
			return (JsonObject) toJson(getFirstValue(boi));
		}
	}

	/**
	 * 
	 * @param boi
	 * @return
	 */
	private Value getFirstValue(BusinessObject boi) {
		Value value = null;
		List<Value> values = boi.getValues();
		if (values != null && !values.isEmpty()) {
			value = values.get(0);
		}
		return value;
	}

	/**
	 *
	 * @param modelOid
	 * @param businessObjectId
	 * @param primaryKey
	 * @return
	 */
	public JsonArray getBusinessObjectProcessInstances(String modelOid,
			String businessObjectId, String primaryKey) {
		ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
		JsonArray resultJson = new JsonArray();

		for (ProcessInstance processInstance : getQueryService()
				.getAllProcessInstances(query)) {
			JsonObject processInstanceJson = new JsonObject();

			resultJson.add(processInstanceJson);

			processInstanceJson.addProperty("oid", processInstance.getOID());
			processInstanceJson.addProperty("id",
					processInstance.getProcessID());
			processInstanceJson.addProperty("name",
					processInstance.getProcessName());
			processInstanceJson.addProperty("startTime", processInstance
					.getStartTime().getTime());
		}

		return resultJson;
	}

	/**
	 *
	 * @param jsonElement
	 * @return
	 */
	private Object jsonElementToObject(JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			return jsonPrimitiveToObject(jsonElement.getAsJsonPrimitive());
		} else if (jsonElement.isJsonArray()) {
			return jsonArrayToList(jsonElement.getAsJsonArray());
		} else {
			return jsonObjectToMap(jsonElement.getAsJsonObject());
		}
	}

	/**
	 *
	 * @param jsonObject
	 * @return
	 */
	private Map jsonObjectToMap(JsonObject jsonObject) {
		Map map = new HashMap();

		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			map.put(entry.getKey(), jsonElementToObject(entry.getValue()));
		}

		return map;
	}

	/**
	 *
	 * @param jsonPrimitive
	 * @return
	 */
	private Object jsonPrimitiveToObject(JsonPrimitive jsonPrimitive) {
		if (jsonPrimitive.isBoolean()) {
			return jsonPrimitive.getAsBoolean();
		} else if (jsonPrimitive.isNumber()) {
			return jsonPrimitive.getAsNumber();
		} else if (jsonPrimitive.isString()) {
			return jsonPrimitive.getAsString();
		}

		// TODO Cater for dates

		throw new IllegalArgumentException(
				"Unknown primitive type for JSON Primitive.");
	}

	/**
	 *
	 * @param jsonArray
	 * @return
	 */
	private List jsonArrayToList(JsonArray jsonArray) {
		List list = new ArrayList();

		for (int n = 0; n < jsonArray.size(); ++n) {
			list.add(jsonElementToObject(jsonArray.get(n)));
		}

		return list;
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	private JsonElement objectToJsonElement(Object object) {
		if (object instanceof Map) {
			return mapToJsonObject((Map) object);
		} else if (object instanceof List) {
			return listToJsonArray((List) object);
		} else {
			return objectToJsonPrimitive(object);
		}
	}

	/**
	 *
	 * @param map
	 * @return
	 */
	private JsonObject mapToJsonObject(Map map) {
		JsonObject jsonObject = new JsonObject();

		for (Object key : map.keySet()) {
			jsonObject.add(key.toString(), objectToJsonElement(map.get(key)));
		}

		return jsonObject;
	}

	/**
	 *
	 * @param list
	 * @return
	 */
	private JsonArray listToJsonArray(List list) {
		JsonArray jsonArray = new JsonArray();

		for (Object object : list) {
			jsonArray.add(objectToJsonElement(object));
		}

		return jsonArray;
	}

	/**
	 *
	 * @param object
	 * @return
	 */
	private JsonPrimitive objectToJsonPrimitive(Object object) {
		if (object instanceof Boolean) {
			return new JsonPrimitive((Boolean) object);
		} else if (object instanceof Number) {
			return new JsonPrimitive((Number) object);
		} else if (object instanceof String) {
			return new JsonPrimitive((String) object);
		}

		throw new IllegalArgumentException("Unknown primitive object type");
	}
}
