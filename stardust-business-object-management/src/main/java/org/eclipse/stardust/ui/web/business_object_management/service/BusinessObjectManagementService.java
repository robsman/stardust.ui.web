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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.runtime.*;
//import org.eclipse.stardust.engine.api.runtime.BusinessObject.Definition;
import org.eclipse.stardust.engine.api.runtime.BusinessObject.Value;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.stardust.ui.web.business_object_management.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
//import org.eclipse.stardust.ui.web.viewscommon.utils.XPathCacheManager;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class BusinessObjectManagementService {
   private static final Logger trace = LogManager
         .getLogger(BusinessObjectManagementService.class);
   @Resource
   private SessionContext sessionContext;

   //@Resource(name = XPathCacheManager.BEAN_ID)
   //private XPathCacheManager xPathCacheManager;

   private DocumentManagementService documentManagementService;
   private UserService userService;
   private QueryService queryService;
   private WorkflowService workflowService;
   private AdministrationService administrationService;

   public BusinessObjectManagementService() {
      super();

      new JsonMarshaller();
   }

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
   * Returns the type definition of all process data across models with an SDT
   * type.
   *
   * @return
   */
   public JsonObject getBusinessObjects() {

      JsonArray modelsJson = new JsonArray();

      fillTestModelsJson(modelsJson);

      ModelCache modelCache = ModelCache.findModelCache();
      if (modelCache == null)
      {
         //fillDescriptionsFromQuery(modelsJson);
      }
      else
      {
         fillDescriptionsFromLocalCache(modelsJson, modelCache);
      }

      JsonObject resultJson = new JsonObject();
      resultJson.add("models", modelsJson);
      return resultJson;
   }

   private void fillTestModelsJson(JsonArray modelsJson)
   {
      JsonArray businessObjectsJson = new JsonArray();
      businessObjectsJson.add(getMemberTestJson());
      businessObjectsJson.add(getFundTestJson());

      JsonObject modelJson = new JsonObject();
      modelJson.addProperty("oid", 1);
      modelJson.addProperty("name", "General Claim Processing");
      modelJson.add("businessObjects", businessObjectsJson);

      modelsJson.add(modelJson);
   }

   private JsonObject getMemberTestJson()
   {
      JsonArray memberFieldsJson = new JsonArray();
      memberFieldsJson.add(toTestField("id", "Member ID", "string", true, true));
      memberFieldsJson.add(toTestField("firstName", "First Name", "string", true, false));
      memberFieldsJson.add(toTestField("lastName", "Last Name", "string", true, false));
      memberFieldsJson.add(toTestField("dateOfBirth", "Date of Birth", "date", true, false));
      memberFieldsJson.add(toTestField("amlChecked", "AML Checked", "boolean", false, false));
      memberFieldsJson.add(toTestField("numberOfDependents", "Number of Dependents", "integer", false, false));
      memberFieldsJson.add(toTestField("annualSalary", "Annual Salary", "decimal", false, false));
      memberFieldsJson.add(toTestField("scheme", "Scheme Name", "string", false, false));
      memberFieldsJson.add(toTestField("schemeId", "Scheme ID", "string", false, false));
      memberFieldsJson.add(toTestField("nationalId", "National ID", "string", false, false));

      JsonObject memberJson = new JsonObject();
      memberJson.addProperty("id", "Member");
      memberJson.addProperty("name", "Member");
      memberJson.add("fields", memberFieldsJson);
      return memberJson;
   }

   private JsonObject getFundTestJson()
   {
      JsonArray fundFieldsJson = new JsonArray();
      fundFieldsJson.add(toTestField("id", "Fund Id", "string", true, true));
      fundFieldsJson.add(toTestField("name", "Fund Name", "string", true, false));

      JsonObject fundJson = new JsonObject();
      fundJson.addProperty("id", "Fund");
      fundJson.addProperty("name", "Fund");
      fundJson.add("fields", fundFieldsJson);
      return fundJson;
   }

   private JsonObject toTestField(String id, String name, String type, boolean key, boolean primaryKey)
   {
      JsonObject json = new JsonObject();
      json.addProperty("id", id);
      json.addProperty("name", name);
      json.addProperty("type", type);
      json.addProperty("key", key);
      json.addProperty("primaryKey", primaryKey);
      return json;
   }

   private void fillDescriptionsFromLocalCache(JsonArray modelsJson, ModelCache modelCache)
   {
      for (DeployedModel deployedModel : modelCache.getAllModels())
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(deployedModel.getId()))
         {
            JsonArray businessObjectsJson = new JsonArray();
            @SuppressWarnings("unchecked")
            List<Data> allData = (List<Data>) deployedModel.getAllData();
            for (Data data : allData)
            {
               if (PredefinedConstants.STRUCTURED_DATA.equals(data.getTypeId()))
               {
                  String typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
                  DeployedModel model = modelCache.getModel(data.getModelOID());
                  Reference ref = data.getReference();
                  if (ref != null)
                  {
                     model = modelCache.getModel(ref.getModelOid());
                     typeDeclarationId = ref.getId();
                  }
                  TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
                  Set<TypedXPath> xPaths = StructuredTypeRtUtils.getAllXPaths(model, typeDeclaration);
                  for (TypedXPath xPath : xPaths)
                  {
                     if (xPath.getXPath().isEmpty()) // root xpath
                     {
                        businessObjectsJson.add(toJson(data, xPath, deployedModel.getModelOID()));
                        break;
                     }
                  }
               }
            }
            if (businessObjectsJson.size() > 0)
            {
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

   /*private void fillDescriptionsFromQuery(JsonArray modelsJson)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findAll();
      query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_DESCRIPTION));

      Map<Long, JsonObject> modelsMap = CollectionUtils.newMap();

      BusinessObjects bos = getQueryService().getBusinessObjects(query);
      for (BusinessObject bo : bos)
      {
         JsonArray businessObjectsJson = null;
         JsonObject modelJson = modelsMap.get(bo.getModelOid());
         if (modelJson == null)
         {
            modelJson = new JsonObject();
            modelJson.addProperty("oid", bo.getModelOid());
            businessObjectsJson = new JsonArray();
            modelJson.add("businessObjects", businessObjectsJson);
            modelsJson.add(modelJson);
         }
         else
         {
            businessObjectsJson = modelJson.getAsJsonArray("businessObjects");
         }
         businessObjectsJson.add(toJson(bo));
      }
   }*/

   private JsonObject toJson(Data data, TypedXPath xPath, int modelOid)
   {
      JsonObject json = new JsonObject();
      json.addProperty("id", data.getId());
      json.addProperty("name", data.getName());
      JsonObject types = new JsonObject();
      addStructuralInformation(json, xPath.getChildXPaths(), types);
      if (types.entrySet().size() > 0)
      {
         json.add("types", types);
      }
      return json;
   }

   /*private JsonObject toJson(BusinessObject bo)
   {
      JsonObject json = new JsonObject();
      json.addProperty("id", bo.getId());
      json.addProperty("name", bo.getName());
      json.add("fields", toJson(bo.getItems()));
      return json;
   }*/

   /*private JsonArray toJson(List<Definition> items)
   {
      JsonArray json = new JsonArray();
      if (items != null)
      {
         for (Definition definition : items)
         {
            json.add(toJson((Definition) definition));
         }
      }
      return json;
   }*/

   private JsonObject toJson(TypedXPath xPath, JsonObject types)
   {
      XPathAnnotations annotations = xPath.getAnnotations();
      JsonObject json = new JsonObject();
      json.addProperty("id", xPath.getId());
      json.addProperty("name", xPath.getId()); // TODO fetch from annotations
      String key = StringUtils.isEmpty(xPath.getXsdTypeNs()) || xPath.getType() != BigData.NULL
            ? xPath.getXsdTypeName()
            : "{" + xPath.getXsdTypeNs() + "}" + xPath.getXsdTypeName();
      json.addProperty("type", key);
      json.addProperty("list", xPath.isList());
      json.addProperty("key", annotations.isIndexed());
      json.addProperty("primaryKey", false); // TODO
      if (StringUtils.isEmpty(xPath.getXsdTypeName()) && xPath.getType() == BigData.NULL)
      {
         List<TypedXPath> childXPaths = xPath.getChildXPaths();
         if (childXPaths != null && !childXPaths.isEmpty())
         {
            addStructuralInformation(json, childXPaths, types);
         }
      }
      return json;
   }

   private void addStructuralInformation(JsonObject json, List<TypedXPath> xPaths, JsonObject types)
   {
      JsonArray fields = new JsonArray();
      if (xPaths != null)
      {
         for (TypedXPath xPath : xPaths)
         {
            fields.add(toJson(xPath, types));
            if (StringUtils.isNotEmpty(xPath.getXsdTypeName()) && xPath.getType() == BigData.NULL)
            {
               String key = StringUtils.isEmpty(xPath.getXsdTypeNs())
                     ? xPath.getXsdTypeName()
                     : "{" + xPath.getXsdTypeNs() + "}" + xPath.getXsdTypeName();
               if (!types.has(key))
               {
                  JsonObject type = new JsonObject();
                  List<TypedXPath> childXPaths = xPath.getChildXPaths();
                  if (childXPaths != null && !childXPaths.isEmpty())
                  {
                     addStructuralInformation(type, childXPaths, types);
                  }
                  types.add(key, type);
               }
            }
         }
      }
      json.add("fields", fields);
   }

   /*private JsonObject toJson(Definition definition)
   {
      JsonObject json = new JsonObject();
      json.addProperty("id", definition.getName());
      json.addProperty("name", definition.getName());
      json.addProperty("type", "string"); // TODO
      json.addProperty("key", definition.isKey());
      json.addProperty("primaryKey", definition.isPrimaryKey());
      json.add("fields", toJson(definition.getItems()));
      return json;
   }*/

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

      if (businessObjectId.equals("Member")) {
         businessObjectInstances.add(getMemberInstance("4711", "Haile", "Selassie", "1", "Scheme-1", "SA"));
         businessObjectInstances.add(getMemberInstance("0815", "Jan", "Smuts", "2", "Scheme-2", "SA"));
      } else if (businessObjectId.equals("Fund")) {
         businessObjectInstances.add(getFundInstance("4711", "Haile"));
      } else {

         BusinessObjectQuery query = BusinessObjectQuery.findForBusinessObject(Long.parseLong(modelOid), businessObjectId);
         query.setPolicy(new BusinessObjectQuery.Policy(BusinessObjectQuery.Option.WITH_VALUES));
         BusinessObjects bos = getQueryService().getBusinessObjects(query);

         for (BusinessObject bo : bos)
         {
            List<Value> values = bo.getValues();
            if (values != null)
            {
               for (Value value : values)
               {
                  businessObjectInstances.add(toJson(value));
               }
            }
         }

      }

      return resultJson;
   }

   private JsonElement toJson(Value value)
   {
      Serializable object = value.getValue();
      if (object instanceof Map)
      {
         return toMapValueJson((Map<?, ?>) object);
      }
      return new JsonObject();
   }

   private JsonElement toMapValueJson(Map<?, ?> map)
   {
      JsonObject json = new JsonObject();
      for (Map.Entry<?, ?> entry : map.entrySet())
      {
         String key = entry.getKey().toString();
         Object value = entry.getValue();
         if (value instanceof Map)
         {
            json.add(key, toMapValueJson((Map<?, ?>) value));
         }
         else if (value instanceof List)
         {
            json.add(key, toListValueJson((List<?>) value));
         }
         else if (value != null)
         {
            json.addProperty(key, value.toString());
         }
         else
         {
            json.add(key, null);
         }
      }
      return json;
   }

   private JsonArray toListValueJson(List< ? > values)
   {
      JsonArray json = new JsonArray();
      for (Object value : values)
      {
         if (value instanceof Map)
         {
            json.add(toMapValueJson((Map<?, ?>) value));
         }
         else if (value instanceof List)
         {
            json.add(toListValueJson((List<?>) value));
         }
         else if (value != null)
         {
            json.add(new JsonPrimitive(value.toString()));
         }
         else
         {
            json.add(null);
         }
      }
      return json;
   }

   private JsonObject getMemberInstance(String id, String firstName, String lastName,
         String scheme, String schemeName, String nationalId)
   {
      JsonObject member = new JsonObject();
      member.addProperty("id", id);
      member.addProperty("firstName", firstName);
      member.addProperty("lastName", lastName);
      member.addProperty("scheme", scheme);
      member.addProperty("schemeName", schemeName);
      member.addProperty("nationalId", nationalId);
      return member;
   }

   private JsonObject getFundInstance(String id, String name)
   {
      JsonObject fund = new JsonObject();
      fund.addProperty("id", id);
      fund.addProperty("name", name);
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
         String businessObjectId, String primaryKey, JsonObject json) {
      System.out.println("Model OID: " + modelOid);
      System.out.println("Business Object ID: " + businessObjectId);
      System.out.println("Primary Key: " + primaryKey);
      System.out.println(json);

      return json;
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
         String businessObjectId, String primaryKey, JsonObject json) {
      System.out.println("Model OID: " + modelOid);
      System.out.println("Business Object ID: " + businessObjectId);
      System.out.println("Primary Key: " + primaryKey);
      System.out.println(json);

      return json;
   }

   /**
   *
   * @param modelOid
   * @param businessObjectId
   * @param primaryKey
   * @return
   */
   public JsonObject getBusinessObjectProcessInstances(String modelOid,
         String businessObjectId, String primaryKey) {
      /*
      * Code e.g.
      *
      * ProcessInstanceQuery query =
      * ProcessInstanceQuery.findAllForBusinessObjectInstance(modelOid,
      * businessObjectId, primaryKey);
      *
      * getQueryService().findAllProcessInstances(query);
      */

      JsonObject resultJson = new JsonObject();

      return resultJson;
   }
}
