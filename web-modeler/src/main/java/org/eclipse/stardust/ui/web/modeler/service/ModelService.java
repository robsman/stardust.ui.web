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

import java.util.List;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.modeler.common.ModelRepository;
import org.eclipse.stardust.ui.web.modeler.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.modeler.common.UserIdProvider;
import org.eclipse.stardust.ui.web.modeler.edit.MissingWritePermissionException;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSessionManager;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;
import org.eclipse.stardust.ui.web.modeler.spi.ThreadInitializer;

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
   private static final Logger trace = LogManager.getLogger(ModelService.class);

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

   @Resource
   private ThreadInitializer[] threadInitializers;

   public ServiceFactory getServiceFactory()
   {
      if (serviceFactory == null)
      {
         serviceFactory = serviceFactoryLocator.get();
      }

      return serviceFactory;
   }

   public ModelingSession currentSession()
   {
      // TODO perform this only once per request handling
      for (ThreadInitializer initializer : threadInitializers)
      {
         initializer.initialize();
      }

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
      currentUserJson.addProperty(ModelerConstants.TYPE_PROPERTY, "WHO_AM_I");
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
      currentUserJson.addProperty(ModelerConstants.TYPE_PROPERTY, "UPDATE_OWNER");
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
            TypeDeclarationUtils.clearExternalSchemaCache();
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
      EObject model = currentSession().modelRepository().findModel(id);
      return currentSession().modelRepository().getModelFileName(model);
   }

   /**
    * TODO - This should probably be delegated to the model management strategy?
    *
    * @param id
    * @return
    */
   public byte[] getModelFile(String id)
   {
      EObject model = currentSession().modelRepository().findModel(id);
      String jcrFilePath = currentSession().modelRepository().getModelFilePath(model);

      return getDocumentManagementService().retrieveDocumentContent(jcrFilePath);
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

   public ModelBuilderFacade getModelBuilderFacade()
   {
      return new ModelBuilderFacade(getModelManagementStrategy());
   }

   /**
    *
    */
   public JsonArray getConfigurationVariables(String modelId)
   {
      ModelRepository modelRepository = currentSession().modelRepository();
      EObject model = modelRepository.findModel(modelId);

      return findModelBinding(model).getMarshaller().retrieveConfigurationVariables(model);
   }

   /**
    * Might be redundant as we could do this entirely on the client, but good test for
    * equivalent Runtime functionality.
    */
   public String retrieveEmbeddedExternalWebApplicationMarkup(String modelId,
         String applicationId)
   {
      ModelRepository modelRepository = currentSession().modelRepository();
      EObject model = modelRepository.findModel(modelId);

      String markup = findModelBinding(model).getMarshaller().retrieveEmbeddedMarkup(model, applicationId);

      // TODO I18N

      return (null != markup) ? markup : "Embedded Web Application is not configured.";
  }
}