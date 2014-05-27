package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerModelManager;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.VariableContext;
import org.eclipse.stardust.ui.web.modeler.common.ModelPersistenceService;
import org.eclipse.stardust.ui.web.modeler.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/**
 *
 * @author Marc.Gille
 * @author Robert Sauer
 *
 */
public class DefaultModelManagementStrategy extends
		AbstractModelManagementStrategy {

   private static final Logger trace = LogManager.getLogger(DefaultModelManagementStrategy.class);

	public static final String MODELS_DIR = "/process-models/";

   private ServiceFactory serviceFactory;
	private DocumentManagementService documentManagementService;

	/**
	 *
	 */
	private Map<String, String> modelFileNameMap = new HashMap<String, String>();

   private final ModelPersistenceService persistenceService;

   private final ServiceFactoryLocator serviceFactoryLocator;

   private final DmsPersistenceUtils persistenceUtils = new DmsPersistenceUtils()
   {
      @Override
      public User getUser()
      {
         return getServiceFactory().getUserService().getUser();
      }

      @Override
      public DocumentManagementService getDocumentManagementService()
      {
         return getServiceFactory().getDocumentManagementService();
      }
   };

   @Autowired
	public DefaultModelManagementStrategy(ModelPersistenceService persistenceService, ServiceFactoryLocator serviceFactoryLocator)
   {
	   this.persistenceService = persistenceService;
      this.serviceFactoryLocator = serviceFactoryLocator;
   }

   /**
	 *
	 */
   public List<ModelDescriptor> loadModels()
   {
      List<ModelDescriptor> models = newArrayList();

      @SuppressWarnings("unchecked")
      List<Document> candidateModelDocuments = getDocumentManagementService().getFolder(
            MODELS_DIR).getDocuments();

      for (Document modelDocument : candidateModelDocuments)
      {
         ModelType xpdlModel = null;
         EObject model = null;

         byte[] modelContent = readModelContext(modelDocument);
         ByteArrayInputStream baos = new ByteArrayInputStream(modelContent);
         ModelPersistenceHandler.ModelDescriptor<?> descriptor = persistenceService.loadModel(
               modelDocument.getName(), baos);
         if (null != descriptor)
         {
            model = descriptor.model;
            if (descriptor.model instanceof ModelType)
            {
               xpdlModel = (ModelType) descriptor.model;
            }
            else
            {
               // use just the most basic XPDL representation, rest will be handled
               // directly from native format (e.g. BPMN2)
               xpdlModel = BpmModelBuilder.newBpmModel()
                     .withIdAndName(descriptor.id,
                           !isEmpty(descriptor.name) ? descriptor.name : descriptor.id)
                     .build();
            }
         }

         if (null != xpdlModel)
         {
            // TODO - This method needs to move to some place where it will be called only
            // once for
            loadEObjectUUIDMap(xpdlModel);
            mapModelFileName(xpdlModel, modelDocument.getName());

            models.add(new ModelDescriptor(xpdlModel.getId(), modelDocument.getName(),
                  model, xpdlModel));
         }
      }

      return models;
   }

    /**
     *
     */
    public ModelType loadModel(String id) {

       Folder folder = documentManagementService.getFolder(MODELS_DIR);
       List<Document> candidateModelDocuments = folder.getDocuments();
       for (Document modelDocument : candidateModelDocuments) {
          String documentName = modelDocument.getName();
          if (documentName.endsWith(".xpdl"))
          {
             if (documentName.split("\\.")[0].equals(id))
             {
                try
                {
                   ModelType model = loadModel(modelDocument);
                   mapModelFileName(model, documentName);
                   return model;
                }
                catch (IOException ex)
                {
                   trace.warn("Unable to load model '" + id + "'", ex);
                }
             }
          }
       }
       return null;
    }

    private ModelType loadModel(Document modelDocument) throws IOException {
        WebModelerModelManager modelMgr = new WebModelerModelManager(this);
        modelMgr.load(URI.createURI(modelDocument.getName()), new ByteArrayInputStream(readModelContext(modelDocument)));
        ModelType model = modelMgr.getModel();
        loadEObjectUUIDMap(model);
        return model;
    }

	/**
	 *
	 */
	public ModelType attachModel(String id) {
        try
        {
           Document document = getDocumentManagementService().getDocument(MODELS_DIR + id + ".xpdl");
           ModelType model = loadModel(document);
           getModels().put(id, model);
           return model;
        }
        catch (IOException ex)
        {
           trace.warn("Unable to load model '" + id + "'", ex);
        }
		return null;
	}

	/**
	 *
	 */
   public void saveModel(ModelType model)
   {
      if (model != null)
      {
         VariableContext variableContext = new VariableContext();
         variableContext.initializeVariables(model);
         variableContext.refreshVariables(model);
         variableContext.saveVariables();
      }

      EObject nativeModel = getNativeModel(model.getId());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      if (persistenceService.saveMode(nativeModel, baos))
      {
         Document modelDocument = (null != getModelFileName(model))
               ? getDocumentManagementService().getDocument(getModelFilePath(model))
               : null;

         if (null == modelDocument)
         {
            DocumentInfo docInfo = DmsUtils.createDocumentInfo(persistenceService.generateDefaultFileName(nativeModel));

            docInfo.setOwner(getServiceFactory().getUserService()
                  .getUser()
                  .getAccount());
            docInfo.setContentType(MediaType.TEXT_XML_VALUE); // TODO shouldn't this be application/xml? if so, how to migrate existing data?

            modelDocument = getDocumentManagementService().createDocument(MODELS_DIR,
                  docInfo, baos.toByteArray(), null);

            if (null != modelDocument)
            {
               // create initial version
               getDocumentManagementService()
                     .versionDocument(modelDocument.getId(), null, null);
               mapModelFileName(model, modelDocument.getName());
            }
         }
         else
         {
            getDocumentManagementService().updateDocument(modelDocument,
                  baos.toByteArray(), null, false, null, null, false);
         }
      }
   }

	/**
	 *
	 * @param model
	 */
	public void deleteModel(ModelType model) {
      Document modelDocument = getDocumentManagementService().getDocument(
            getModelFilePath(model));

		if (modelDocument != null)
		{
			getDocumentManagementService().removeDocument(modelDocument.getId());
		}

      removeModelFileNameMapping(model);
      getModels().remove(model.getId());
	}

   /**
    * @param fileName
    * @param fileContent
    * @param createNewVersion
    * @return
    */
   @Override
   public ModelUploadStatus uploadModelFile(String fileName, byte[] fileContent,
         boolean createNewVersion)
   {

      if (persistenceUtils.isExistingResource("/process-models", fileName))
      {
         if (createNewVersion)
         {
            Document modelDocument = getDocumentManagementService().getDocument(
                  MODELS_DIR + fileName);
            persistenceUtils.updateDocument(modelDocument, fileContent,
                  modelDocument.getDescription(), "", false);

            return ModelUploadStatus.NEW_MODEL_VERSION_CREATED;
         }

         return ModelUploadStatus.MODEL_ALREADY_EXISTS;
      }
      else
      {
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);

         docInfo.setOwner(getServiceFactory().getUserService().getUser().getAccount());
         docInfo.setContentType(MediaType.TEXT_XML_VALUE);

         getDocumentManagementService().createDocument(MODELS_DIR, docInfo, fileContent,
               null);

         return ModelUploadStatus.NEW_MODEL_CREATED;
      }
   }

	/**
	 *
	 */
	public void versionizeModel(ModelType model) {
	}

   /**
    *
    * @param model
    */
   public String getModelFileName(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);
      return modelFileNameMap.get(modelUUID);
   }

   /**
    *
    * @param model
    */
   public String getModelFilePath(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);
      return MODELS_DIR + modelFileNameMap.get(modelUUID);
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

	private ServiceFactory getServiceFactory() {
		// TODO Replace

		if (serviceFactory == null) {
         serviceFactory = serviceFactoryLocator.get();
		}

		return serviceFactory;
	}

	/**
	 *
	 * @param modelDocument
	 * @return
	 */
	private byte[] readModelContext(Document modelDocument) {
		return getDocumentManagementService().retrieveDocumentContent(
				modelDocument.getId());
	}

   /**
    * @param model
    */
   private void mapModelFileName(ModelType model, String fileName)
   {
      String modelUUID = uuidMapper().getUUID(model);
      modelFileNameMap.put(modelUUID, fileName);
   }

   /**
    * @param model
    */
   private void removeModelFileNameMapping(ModelType model)
   {
      String modelUUID = uuidMapper().getUUID(model);
      modelFileNameMap.remove(modelUUID);
   }
}
