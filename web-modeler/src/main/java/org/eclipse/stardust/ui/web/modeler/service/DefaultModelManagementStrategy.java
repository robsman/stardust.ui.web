package org.eclipse.stardust.ui.web.modeler.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 *
 * @author Marc.Gille
 *
 */
public class DefaultModelManagementStrategy extends
		AbstractModelManagementStrategy {

	private static final String MODELS_DIR = "/process-models/";

	private ServiceFactory serviceFactory;
	private DocumentManagementService documentManagementService;

	/**
	 *
	 */
	private Map<String, String> modelFileNameMap = new HashMap<String, String>();

	/**
	 *
	 */
	public List<ModelType> loadModels() {

		List<ModelType> models = new ArrayList<ModelType>();
		List<Document> candidateModelDocuments = getDocumentManagementService()
				.getFolder(MODELS_DIR).getDocuments();

		XpdlModelIoUtils.clearModelsMap();

		for (Document modelDocument : candidateModelDocuments) {
			if (modelDocument.getName().endsWith(".xpdl")) {

				ModelType model = XpdlModelIoUtils
						.loadModel(readModelContext(modelDocument), this);
				//TODO - This method needs to move to some place where it will be called only once for
				loadEObjectUUIDMap(model);
				mapModelFileName(model, modelDocument.getName());

				getModels().put(model.getId(), model);
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
	public void saveModel(ModelType model) {
			String modelContent = new String(XpdlModelIoUtils.saveModel(model));
			Document modelDocument;

				modelDocument = getDocumentManagementService().getDocument(
						MODELS_DIR + model.getName() + ".xpdl");

			if (null == modelDocument) {
				DocumentInfo docInfo = DmsUtils.createDocumentInfo(model.getName()
						+ ".xpdl");

				docInfo.setOwner(getServiceFactory().getWorkflowService().getUser()
						.getAccount());
				docInfo.setContentType(MimeTypesHelper.XML.getType());

				modelDocument = getDocumentManagementService().createDocument(
						MODELS_DIR, docInfo, modelContent.getBytes(), null);

				// Create initial version

				getDocumentManagementService().versionDocument(
						modelDocument.getId(), null);
			} else {
				getDocumentManagementService().updateDocument(modelDocument,
						modelContent.getBytes(), null, false, null, false);
			}

			mapModelFileName(model);
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
			serviceFactory = ServiceFactoryLocator.get("motu", "motu");
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
      mapModelFileName(model, model.getName() + ".xpdl");

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
