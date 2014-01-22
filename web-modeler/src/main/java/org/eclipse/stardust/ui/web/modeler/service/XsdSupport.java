package org.eclipse.stardust.ui.web.modeler.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.impl.XSDImportImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.ui.web.modeler.spi.ModelingSessionScoped;

@Service
@ModelingSessionScoped
public class XsdSupport
{
   private static final Logger trace = LogManager.getLogger(XsdSupport.class);

   /* TODO remove */
   private static final String EXTERNAL_SCHEMA_MAP = "com.infinity.bpm.rt.data.structured.ExternalSchemaMap";

   /* TODO remove */
   private static final XSDResourceFactoryImpl XSD_RESOURCE_FACTORY = new XSDResourceFactoryImpl();

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
      Parameters parameters = Parameters.instance();
      Map<String, Object> loadedSchemas = null;
      synchronized (StructuredTypeRtUtils.class)
      {
         loadedSchemas = parameters.getObject(EXTERNAL_SCHEMA_MAP);
         if (loadedSchemas == null)
         {
            // (fh) using Hashtable to avoid concurrency problems.
            loadedSchemas = new Hashtable<String, Object>();
            parameters.set(EXTERNAL_SCHEMA_MAP, loadedSchemas);
         }
      }
      Object o = loadedSchemas.get(location);
      if (o != null)
      {
         return o instanceof XSDSchema ? (XSDSchema) o : null;
      }

      ResourceSetImpl resourceSet = new ResourceSetImpl();
      URI uri = URI.createURI(location);
      if (uri.scheme() == null)
      {
         resourceSet.setURIConverter(modelService.getClasspathUriConverter());
         if (location.startsWith("/"))
         {
            location = location.substring(1);
         }
         uri = URI.createURI(WebModelerUriConverter.CLASSPATH_SCHEME + ":/" + location);
      }
      // (fh) register the resource factory directly with the resource set and do not
      // tamper with the global registry.
      resourceSet.getResourceFactoryRegistry()
            .getProtocolToFactoryMap()
            .put(uri.scheme(), XSD_RESOURCE_FACTORY);
      resourceSet.getResourceFactoryRegistry()
            .getExtensionToFactoryMap()
            .put("xsd", XSD_RESOURCE_FACTORY);
      org.eclipse.emf.ecore.resource.Resource resource = resourceSet.createResource(uri);
      Map<Object, Object> options = new HashMap<Object, Object>();
      options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
      resource.load(options);

      for (EObject eObject : resource.getContents())
      {
         if (eObject instanceof XSDSchema)
         {
            XSDSchema schema = (XSDSchema) eObject;
            resolveImports(schema);
            if (trace.isDebugEnabled())
            {
               trace.debug("Found schema for namespace: " + schema.getTargetNamespace()
                     + " at location: " + uri.toString());
            }
            loadedSchemas.put(location, schema);
            return schema;
         }
      }
      loadedSchemas.put(location, "NULL");
      return null;
   }

   /**
    * Should be removed together with loadSchema
    */
   private static void resolveImports(XSDSchema schema)
   {
      for (XSDSchemaContent item : schema.getContents())
      {
         if (item instanceof XSDImportImpl)
         {
            // force schema resolving.
            // it's a noop if the schema is already resolved.
            ((XSDImportImpl) item).importSchema();
         }
      }
   }
}
