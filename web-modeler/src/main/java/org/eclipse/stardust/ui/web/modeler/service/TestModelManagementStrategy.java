package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.model.xpdl.builder.strategy.AbstractModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerModelManager;
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
	public List<ModelDescriptor> loadModels() {

		List<ModelDescriptor> models = newArrayList();
		List<Document> candidateModelDocuments = getDocumentManagementService().getFolder(MODELS_DIR).getDocuments();

		for (Document modelDocument : candidateModelDocuments) {
			String documentName = modelDocument.getName();
            if (documentName.endsWith(".xpdl")) {
                try
                {
                    WebModelerModelManager modelMgr = new WebModelerModelManager(this);
                    modelMgr.load(URI.createURI(documentName), new ByteArrayInputStream(readModelContext(modelDocument)));
                    ModelType model = modelMgr.getModel();
                    models.add(new ModelDescriptor(model.getId(), documentName, model, model));
                    break;
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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

   @Override
   public String getModelFileName(ModelType model)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getModelFilePath(ModelType model)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ModelUploadStatus uploadModelFile(String fileName, byte[] fileContent,
         boolean createNewVersion)
   {
      // TODO Auto-generated method stub
      return null;
   }
}
