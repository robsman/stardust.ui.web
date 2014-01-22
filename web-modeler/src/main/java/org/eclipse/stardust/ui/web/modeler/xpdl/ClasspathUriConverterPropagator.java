package org.eclipse.stardust.ui.web.modeler.xpdl;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ThreadInitializer;

/**
 *
 * @author Robert.Sauer
 *
 */
@Service
@Scope("singleton")
public class ClasspathUriConverterPropagator implements ThreadInitializer
{
   @Resource
   private ModelService modelService;

   @Override
   public void initialize()
   {
      TypeDeclarationUtils.defaultURIConverter.set(modelService
            .getClasspathUriConverter());
   }
}
