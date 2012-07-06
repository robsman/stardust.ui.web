package org.eclipse.stardust.ui.web.modeler.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;

/**
 * 
 * @author Marc.Gille
 *
 */
public class TestModelManagementStrategy extends
		AbstractModelManagementStrategy {

	private static final String MODELS_DIR = "/process-models/";

	private ModelService modelService;
	private ModelManagementStrategy previousModelManagementStrategy;
	private ServiceFactory serviceFactory;
	private DocumentManagementService documentManagementService;

	
	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
		this.previousModelManagementStrategy = modelService.getModelManagementStrategy();
	}

	public ModelManagementStrategy getPreviousModelManagementStrategy() {
		return previousModelManagementStrategy;
	}

	/**
	 * 
	 */
	public String getFirstModelId()
	{
		getModelService().setModelManagementStrategy(this);
		getModels(true);

		return getModels().values().iterator().next().getId();
	}
	
	/**
	 * 
	 */
	public void deactivate()
	{
		getModelService().setModelManagementStrategy(getPreviousModelManagementStrategy());
	}

	/**
	 * Return only the first model file for testing
	 */
	public List<ModelType> loadModels() {

		List<ModelType> models = new ArrayList<ModelType>();
		List<Document> candidateModelDocuments = getDocumentManagementService()
				.getFolder(MODELS_DIR).getDocuments();

		for (Document modelDocument : candidateModelDocuments) {
			if (modelDocument.getName().endsWith(".xpdl")) {

				ModelType model = XpdlModelIoUtils
						.loadModel(readModelContext(modelDocument));

				getModels().put(model.getId(), model);
				
				break;
			}
		}

		return models;
	}

	/**
	 * 
	 */
	public ModelType attachModel(String id) {
		return null;
	}
	
	/**
	 * 
	 */
	public void saveModel(ModelType model) {
	}

	/**
	 * 
	 * @param model
	 */
	public void deleteModel(ModelType model) {
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
    * 
    * @return
    */
    public ModelType loadModel(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }
}
