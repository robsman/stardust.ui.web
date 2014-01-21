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
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.model.xpdl.builder.common.AbstractElementBuilder;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DescriptionType;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableModelElement;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.spi.SpiExtensionRegistry;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelVariable;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContextHelper;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
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
      ModelRepository modelRepository = currentSession().modelRepository();
      EObject model = modelRepository.findModel(modelId);

      return findModelBinding(model).validateModel(model);
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