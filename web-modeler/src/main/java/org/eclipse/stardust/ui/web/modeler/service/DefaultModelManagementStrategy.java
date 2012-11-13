package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.springframework.beans.factory.annotation.Autowired;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.common.ModelPersistenceService;
import org.eclipse.stardust.ui.web.modeler.common.ServiceFactoryLocator;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 *
 * @author Marc.Gille
 * @author Robert Sauer
 *
 */
public class DefaultModelManagementStrategy extends
		AbstractModelManagementStrategy {

   private static final Logger trace = LogManager.getLogger(DefaultModelManagementStrategy.class);

	private static final String MODELS_DIR = "/process-models/";

	private ServiceFactory serviceFactory;
	private DocumentManagementService documentManagementService;

	/**
	 *
	 */
	private Map<String, String> modelFileNameMap = new HashMap<String, String>();

   private final ModelPersistenceService persistenceService;

   private final ServiceFactoryLocator serviceFactoryLocator;

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
          if (modelDocument.getName().endsWith(".xpdl"))
          {
             if(modelDocument.getName().equals(id))
             {
                ModelType model = XpdlModelIoUtils
                   .loadModel(readModelContext(modelDocument), this);
                loadEObjectUUIDMap(model);
                mapModelFileName(model, modelDocument.getName());

                return model;
             }
          }
       }
       return null;
    }



	/**
	 *
	 */
	public ModelType attachModel(String id) {
		ModelType model = XpdlModelIoUtils
		.loadModel(readModelContext(getDocumentManagementService()
				.getDocument(MODELS_DIR + id + ".xpdl")), this);
		loadEObjectUUIDMap(model);

		getModels().put(id, model);

		return model;
	}

	/**
	 *
	 */
   public void saveModel(ModelType model)
   {
      EObject nativeModel = getNativeModel(model.getId());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      if (persistenceService.saveMode(nativeModel, baos))
      {
         Document modelDocument = getDocumentManagementService().getDocument(
               getModelFilePath(model));

         if (null == modelDocument)
         {
            DocumentInfo docInfo = DmsUtils.createDocumentInfo(persistenceService.generateDefaultFileName(nativeModel));

            docInfo.setOwner(getServiceFactory().getWorkflowService()
                  .getUser()
                  .getAccount());
            docInfo.setContentType(MimeTypesHelper.XML.getType());

            modelDocument = getDocumentManagementService().createDocument(MODELS_DIR,
                  docInfo, baos.toByteArray(), null);

            // create initial version
            getDocumentManagementService().versionDocument(modelDocument.getId(), null);
            mapModelFileName(model);
         }
         else
         {
            getDocumentManagementService().updateDocument(modelDocument,
                  baos.toByteArray(), null, false, null, false);
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

      if (DocumentMgmtUtility.isExistingResource("/process-models", fileName))
      {
         if (createNewVersion)
         {
            Document modelDocument = getDocumentManagementService().getDocument(
                  MODELS_DIR + fileName);
            DocumentMgmtUtility.updateDocument(modelDocument, fileContent,
                  modelDocument.getDescription(), "");

            return ModelUploadStatus.NEW_MODEL_VERSION_CREATED;
         }

         return ModelUploadStatus.MODEL_ALREADY_EXISTS;
      }
      else
      {
         DocumentInfo docInfo = DmsUtils.createDocumentInfo(fileName);

         docInfo.setOwner(getServiceFactory().getWorkflowService().getUser().getAccount());
         docInfo.setContentType(MimeTypesHelper.XML.getType());

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
   private void mapModelFileName(ModelType model)
   {
      mapModelFileName(model, model.getId() + ".xpdl");

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
