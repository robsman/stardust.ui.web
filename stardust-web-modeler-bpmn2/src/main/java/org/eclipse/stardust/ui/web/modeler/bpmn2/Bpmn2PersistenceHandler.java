package org.eclipse.stardust.ui.web.modeler.bpmn2;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.DirectStreamsURIHandler;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

public class Bpmn2PersistenceHandler implements ModelPersistenceHandler
{
   private static final Logger trace = LogManager.getLogger(Bpmn2PersistenceHandler.class);

   @Override
   public ModelDescriptor loadModel(String contentName, InputStream modelContent)
   {
      if (contentName.endsWith(".bpmn"))
      {
         try
         {
            DirectStreamsURIHandler streamsUriHandler = new DirectStreamsURIHandler();
            URI resourceStreamUri = streamsUriHandler.registerInputStream(modelContent);

            ResourceSet context = new ResourceSetImpl();
            context.getResourceFactoryRegistry()
                  .getProtocolToFactoryMap()
                  .put(resourceStreamUri.scheme(), new Bpmn2ResourceFactoryImpl());

            Bpmn2Resource bpmnModel = (Bpmn2Resource) context.createResource(resourceStreamUri);

            // must pass stream directly as load(options) will close the stream internally
            bpmnModel.load(modelContent, getDefaultXmlLoadOptions());

            for (EObject eObj : bpmnModel.getContents())
            {
               if ((eObj instanceof DocumentRoot)
                     && (null != ((DocumentRoot) eObj).getDefinitions()))
               {
                  Definitions definitions = ((DocumentRoot) eObj).getDefinitions();
                  return new ModelDescriptor(definitions.getId(), definitions.getName(),
                        definitions);
               }
            }
         }
         catch (IOException ioe)
         {
            trace.warn("Failed loading BPMN2 model.", ioe);
         }
      }
      return null;
   }

   private static Map<Object, Object> getDefaultXmlLoadOptions()
   {
      Map<Object, Object> options = new HashMap<Object, Object>();
      options.put(XMLResource.OPTION_RECORD_ANY_TYPE_NAMESPACE_DECLARATIONS, Boolean.TRUE);
      options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
      options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
      return options;
   }

}
