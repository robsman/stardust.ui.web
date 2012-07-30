package org.eclipse.stardust.ui.web.modeler.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
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

	@Resource
   private EObjectUUIDMapper eObjectUUIDMapper;

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
						.loadModel(readModelContext(modelDocument));
				//TODO - This method needs to move to some place where it will be called only once for
				loadEObjectUUIDMap(model);

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
                   .loadModel(readModelContext(modelDocument));
                loadEObjectUUIDMap(model);
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
				.getDocument(MODELS_DIR + id + ".xpdl")));
		
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
	}

	/**
	 * 
	 * @param model
	 */
	public void deleteModel(ModelType model) {
		Document modelDocument = getDocumentManagementService().getDocument(
				MODELS_DIR + model.getName() + ".xpdl");

		if (modelDocument != null)
		{
			getDocumentManagementService().removeDocument(modelDocument.getId());
		}

		getModels().remove(model.getId());
	}

	/**
	 * 
	 */
	public void versionizeModel(ModelType model) {
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
    * TODO - This method needs to move to some place where it will be called only once for
    * a session.
    */
   private void loadEObjectUUIDMap(ModelType model)
   {
      // Load if not already loaded.
      if (null == eObjectUUIDMapper.getUUID(model))
      {
         eObjectUUIDMapper.map(model);
         for (Iterator<EObject> i = model.eAllContents(); i.hasNext();)
         {
            eObjectUUIDMapper.map(i.next());
         }
      }
   }
}
