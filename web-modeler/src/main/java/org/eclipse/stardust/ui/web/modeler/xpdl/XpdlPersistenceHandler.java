package org.eclipse.stardust.ui.web.modeler.xpdl;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.model.xpdl.builder.utils.XpdlModelIoUtils;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

public class XpdlPersistenceHandler implements ModelPersistenceHandler
{
   private static final Logger trace = LogManager.getLogger(XpdlPersistenceHandler.class);

   private final ModelManagementStrategy modelManagementStrategy;

   public XpdlPersistenceHandler(ModelManagementStrategy modelManagementStrategy)
   {
      this.modelManagementStrategy = modelManagementStrategy;
   }

   @Override
   public ModelDescriptor loadModel(String contentName, InputStream modelContent)
   {
      if (contentName.endsWith(".xpdl"))
      {
         try
         {
            ModelType xpdlModel = XpdlModelIoUtils.loadModel(modelContent,
                  modelManagementStrategy);
            return new ModelDescriptor(xpdlModel.getId(), xpdlModel.getName(), xpdlModel);
         }
         catch (IOException ioe)
         {
            trace.warn("Failed loading XPDL model.", ioe);
         }
      }
      return null;
   }
}
