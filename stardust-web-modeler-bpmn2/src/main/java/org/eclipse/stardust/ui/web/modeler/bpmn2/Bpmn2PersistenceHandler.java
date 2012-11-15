package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2Resource;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.DirectStreamsURIHandler;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler;

@Service
public class Bpmn2PersistenceHandler implements ModelPersistenceHandler<Definitions>
{
   private static final Logger trace = LogManager.getLogger(Bpmn2PersistenceHandler.class);

   @Override
   public boolean canLoadModel(String contentName)
   {
      return contentName.endsWith(".bpmn");
   }

   @Override
   public ModelDescriptor<Definitions> loadModel(String contentName,
         InputStream modelContent)
   {
      if (canLoadModel(contentName))
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
                  DocumentRoot rootElement = (DocumentRoot) eObj;
                  Definitions definitions = rootElement.getDefinitions();
                  try
                  {
                     // test if current ID already is a UUID ...
                     UUID.fromString(definitions.getId());
                  }
                  catch (IllegalArgumentException iae)
                  {
                     // ... nope
                     if (isEmpty(definitions.getName()))
                     {
                        definitions.setName(definitions.getId());
                     }
                     definitions.setId(UUID.randomUUID().toString());
                  }

                  return new ModelDescriptor<Definitions>(definitions.getId(),
                        definitions.getName(), definitions);
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

   @Override
   public String generateDefaultFileName(Definitions model)
   {
      if ( !isEmpty(model.getName()))
      {
         return model.getName() + ".bpmn";
      }
      else if ( !isEmpty(model.getId()))
      {
         return model.getId() + ".bpmn";
      }
      else
      {
         return Bpmn2Utils.createInternalId() + ".bpmn";
      }
   }

   public void saveModel(Definitions model, OutputStream modelContent)
   {
      try
      {
         DirectStreamsURIHandler streamsUriHandler = new DirectStreamsURIHandler();
         URI resourceStreamUri = streamsUriHandler.registerOutputStream(modelContent);

         ResourceSet context = new ResourceSetImpl();
         context.getResourceFactoryRegistry()
               .getProtocolToFactoryMap()
               .put(resourceStreamUri.scheme(), new Bpmn2ResourceFactoryImpl());

         Bpmn2Resource bpmnModel;
         if (null != model.eResource())
         {
            bpmnModel = (Bpmn2Resource) model.eResource();
         }
         else
         {
            bpmnModel = (Bpmn2Resource) context.createResource(resourceStreamUri);
            bpmnModel.getContents().add(
                  (null == model.eContainer()) ? model : model.eContainer());
         }

         // must pass stream directly as save(options) will close the stream internally
         bpmnModel.save(modelContent, getDefaultXmlSaveOptions());
      }
      catch (IOException ioe)
      {
         trace.warn("Failed loading BPMN2 model.", ioe);
      }
   }

   private static Map<Object, Object> getDefaultXmlLoadOptions()
   {
      Map<Object, Object> options = new HashMap<Object, Object>();
      options.put(XMLResource.OPTION_RECORD_ANY_TYPE_NAMESPACE_DECLARATIONS, Boolean.TRUE);
      options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
      options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
      return options;
   }

   private static Map<Object, Object> getDefaultXmlSaveOptions()
   {
      Map<Object, Object> options = new HashMap<Object, Object>();
      options.put(XMLResource.OPTION_RECORD_ANY_TYPE_NAMESPACE_DECLARATIONS, Boolean.TRUE);
      options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
      options.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
      options.put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, Boolean.TRUE);
      return options;
   }
}
