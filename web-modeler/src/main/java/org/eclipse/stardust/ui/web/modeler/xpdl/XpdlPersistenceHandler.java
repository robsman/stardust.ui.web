package org.eclipse.stardust.ui.web.modeler.xpdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.WebModelerModelManager;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class XpdlPersistenceHandler implements ModelPersistenceHandler<ModelType>
{
   private static final Logger trace = LogManager.getLogger(XpdlPersistenceHandler.class);

   private final ModelService modelService;

   @Autowired
   public XpdlPersistenceHandler(ModelService modelingSessionManager)
   {
      this.modelService = modelingSessionManager;
   }

   @Override
   public boolean canLoadModel(String contentName)
   {
      return contentName.endsWith(".xpdl");
   }

   @Override
   public ModelDescriptor<ModelType> loadModel(String contentName, InputStream modelContent)
   {
      if (canLoadModel(contentName))
      {
         try
         {
            ModelManagementStrategy strategy = modelService.currentSession().modelManagementStrategy();
            WebModelerModelManager modelMgr = new WebModelerModelManager(strategy);
            modelMgr.load(URI.createURI(contentName), modelContent);
            ModelType xpdlModel = modelMgr.getModel();
            return new ModelDescriptor<ModelType>(xpdlModel.getId(), xpdlModel.getName(), xpdlModel);
         }
         catch (IOException ioe)
         {
            trace.warn("Failed loading XPDL model.", ioe);
         }
      }
      return null;
   }

   @Override
   public String generateDefaultFileName(ModelType model)
   {
      return model.getId() + ".xpdl";
   }

   @Override
   public void saveModel(ModelType model, OutputStream modelContent) throws IOException
   {
      ModelManagementStrategy strategy = modelService.currentSession().modelManagementStrategy();
      WebModelerModelManager modelMgr = new WebModelerModelManager(strategy);
      modelMgr.setModel(model);
      modelMgr.save(URI.createURI(generateDefaultFileName(model)), modelContent);
   }

   @Override
   public void saveDeployableModel(ModelType model, OutputStream modelContent)
         throws IOException
   {
      saveModel(model, modelContent);
   }
}
