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

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Reference;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.*;
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
import com.google.gson.JsonObject;

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

      fillDescriptionsFromLocalCache(modelsJson);

      //fillDescriptionsFromQuery(modelsJson);

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

   private void fillDescriptionsFromLocalCache(JsonArray modelsJson)
   {
      ModelCache modelCache = ModelCache.findModelCache();

      // TODO@Florin modelCache is null
      
      if (modelCache == null)
      {
    	  return;
      }
      
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

      BusinessObjects bos = getQueryService().getBusinessObjects(query);
      for (BusinessObject bo : bos)
      {
         businessObjectsJson.add(toJson(bo));
      }
   }*/

   private JsonObject toJson(Data data, TypedXPath xPath, int modelOid)
   {
      JsonObject json = new JsonObject();
      json.addProperty("id", data.getId());
      json.addProperty("name", data.getName());
      json.add("fields", toJson(xPath.getChildXPaths()));
      return json;
   }

   /*private JsonElement toJson(BusinessObject bo)
   {
      JsonObject json = new JsonObject();
      json.addProperty("id", bo.getId());
      json.addProperty("name", bo.getName());
      json.add("fields", toJson(bo.getItems()));
      return json;
   }*/

   private JsonArray toJson(List<?> items)
   {
      JsonArray json = new JsonArray();
      if (items != null)
      {
         for (Object definition : items)
         {
            /*if (definition instanceof Definition)
            {
               json.add(toJson((Definition) definition));
            }
            else*/ if (definition instanceof TypedXPath)
            {
               json.add(toJson((TypedXPath) definition));
            }
         }
      }
      return json;
   }

   private JsonObject toJson(TypedXPath xPath)
   {
      XPathAnnotations annotations = xPath.getAnnotations();
      JsonObject json = new JsonObject();
      json.addProperty("id", xPath.getId());
      json.addProperty("name", xPath.getId()); // TODO fetch from annotations
      if (xPath.getType() == BigData.NULL)
      {
         json.addProperty("type", xPath.getXsdTypeName());
      }
      json.addProperty("key", annotations.isIndexed());
      json.addProperty("primaryKey", false); // TODO
      List<TypedXPath> childXPaths = xPath.getChildXPaths();
      if (childXPaths != null && !childXPaths.isEmpty())
      {
         json.add("fields", toJson(childXPaths));
      }
      return json;
   }

   /*private JsonElement toJson(Definition definition)
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
      /*
      *
      * Code e.g.
      *
      * BusinessObjectInstanceQuery query =
      * BusinessObjectInstanceQuery.findAll(modelOid, businessObjectId);
      *
      * getQueryService().findAllBusinessObjectInstances(query);
      */

      JsonObject resultJson = new JsonObject();
      JsonArray businessObjectInstances = new JsonArray();

      resultJson.add("businessObjectInstances", businessObjectInstances);

      JsonObject businessObjectInstance = null;

      if (businessObjectId.equals("Member")) {
         businessObjectInstance = new JsonObject();

         businessObjectInstances.add(businessObjectInstance);

         businessObjectInstance.addProperty("id", "4711");
         businessObjectInstance.addProperty("firstName", "Haile");
         businessObjectInstance.addProperty("lastName", "Selassie");
         businessObjectInstance.addProperty("scheme", "1");
         businessObjectInstance.addProperty("schemeName", "Scheme-1");
         businessObjectInstance.addProperty("nationalId", "SA");

         businessObjectInstance = new JsonObject();

         businessObjectInstances.add(businessObjectInstance);

         businessObjectInstance.addProperty("id", "0815");
         businessObjectInstance.addProperty("firstName", "Jan");
         businessObjectInstance.addProperty("lastName", "Smuts");
         businessObjectInstance.addProperty("scheme", "2");
         businessObjectInstance.addProperty("schemeName", "Scheme-2");
         businessObjectInstance.addProperty("nationalId", "SA");
      } else if (businessObjectId.equals("Fund")) {
         businessObjectInstance = new JsonObject();

         businessObjectInstances.add(businessObjectInstance);

         businessObjectInstance.addProperty("id", "4711");
         businessObjectInstance.addProperty("name", "Haile");
      }

      return resultJson;
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
