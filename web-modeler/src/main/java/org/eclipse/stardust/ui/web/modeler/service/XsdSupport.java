package org.eclipse.stardust.ui.web.modeler.service;

import java.io.IOException;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;
import org.eclipse.xsd.XSDSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@ModelingSessionScoped
public class XsdSupport
{
   private static final Logger trace = LogManager.getLogger(XsdSupport.class);

   private final ModelService modelService;

   @Autowired
   public XsdSupport(ModelService modelService)
   {
      this.modelService = modelService;
   }

   /**
    * Duplicate of StructuredTypeRtUtils.getSchema(String, String).
    * <p>
    * Should be removed after repackaging of XSDSchema for runtime is dropped.
    */
   public XSDSchema loadSchema(String location) throws IOException
   {
      return StructuredTypeRtUtils.getSchema(location, null, null);
   }
}
