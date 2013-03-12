package org.eclipse.stardust.ui.web.modeler.integration;

import static java.util.Collections.singletonMap;
import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSchemaContent;
import org.eclipse.xsd.impl.XSDImportImpl;
import org.eclipse.xsd.util.XSDResourceFactoryImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.service.WebModelerUriConverter;

@Component
@Scope("singleton")
public class ExternalXmlSchemaManager
{
   private static final Logger trace = LogManager.getLogger(ExternalXmlSchemaManager.class);

   private static final WebModelerUriConverter CLASSPATH_URI_CONVERTER = new WebModelerUriConverter();

   private static final XSDResourceFactoryImpl XSD_RESOURCE_FACTORY = new XSDResourceFactoryImpl();

   private ConcurrentHashMap<String, XSDSchema> schemaCache = newConcurrentHashMap();

   public XSDSchema resolveSchemaFromUri(String schemaUri)
   {
      XSDSchema schema = schemaCache.get(schemaUri);
      if (null == schema)
      {
         schema = loadSchemaFromUri(schemaUri);
         if (null != schema)
         {
            schemaCache.putIfAbsent(schemaUri, schema);
         }
         else
         {
            // TODO remember resolution failed once?
         }
         schema = schemaCache.get(schemaUri);
      }

      return schema;
   }

   private XSDSchema loadSchemaFromUri(String schemaUri)
   {
      ResourceSetImpl resourceSet = new ResourceSetImpl();
      URI uri = URI.createURI(schemaUri);
      if (uri.scheme() == null)
      {
         resourceSet.setURIConverter(CLASSPATH_URI_CONVERTER);
         if(schemaUri.startsWith("/"))
         {
            schemaUri = schemaUri.substring(1);
         }
         uri = URI.createURI(WebModelerUriConverter.CLASSPATH_SCHEME + ":/" + schemaUri);
      }
      Resource xsdResource = XSD_RESOURCE_FACTORY.createResource(uri);

      try
      {
         xsdResource.load(singletonMap(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE));
         for (EObject eObject : xsdResource.getContents())
         {
            if (eObject instanceof XSDSchema)
            {
               XSDSchema schema = (XSDSchema) eObject;

               resolveImports(schema);

               return schema;
            }
         }
      }
      catch (IOException ioe)
      {
         trace.warn("Failed loading schema from URI " + schemaUri, ioe);
      }
      return null;
   }

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
