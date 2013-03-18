package org.eclipse.stardust.ui.web.modeler.bpmn2.serialization;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Extension;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.util.ImportHelper;
import org.eclipse.bpmn2.util.QNameURIHandler;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.UnresolvedReferenceException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.eclipse.xsd.ecore.XSDEcoreBuilder;
import org.xml.sax.helpers.DefaultHandler;

public class StardustBpmn2XmlLoad extends XMLLoadImpl
{
   public StardustBpmn2XmlLoad(XMLHelper helper)
   {
      super(helper);
   }

   @Override
   protected DefaultHandler makeDefaultHandler()
   {
      return new BpmnXmlHandler(resource, helper, options);
   }

   @Override
   protected void handleErrors() throws IOException
   {
      // traverse errors, mask missing Java classes
      if ( !resource.getErrors().isEmpty())
      {
         List<String> importedJavaTypes = newArrayList();

         Definitions model = ImportHelper.getDefinitions(resource);
         for (Import importSpec : model.getImports())
         {
            if ("http://www.java.com/javaTypes".equals(importSpec.getImportType()))
            {
               importedJavaTypes.add(importSpec.getLocation());
            }
         }

         for (Iterator<Diagnostic> i = resource.getErrors().iterator(); i.hasNext();)
         {
            Diagnostic error = i.next();
            if (error instanceof UnresolvedReferenceException)
            {
               String reference = ((UnresolvedReferenceException) error).getReference();
               for (String importedJavaType : importedJavaTypes)
               {
                  if (reference.startsWith(importedJavaType))
                  {
                     // ignore missing java type errors
                     i.remove();
                     break;
                  }
               }
            }
         }
      }

      super.handleErrors();
   }

   /**
    * Copied from org.eclipse.bpmn2 due to lack of visibility.
    *
    * @see org.eclipse.bpmn2.util.Bpmn2ResourceImpl$BpmnXmlHandler
    *
    */
   protected static class BpmnXmlHandler extends SAXXMLHandler {

       public BpmnXmlHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
           super(xmiResource, helper, options);
       }

       /**
        * Overridden to be able to convert QName references in attributes to URIs during load.
        * @param ids
        *  In our case the parameter will contain exactly one QName that we resolve to URI.
        */
       @Override
       protected void setValueFromId(EObject object, EReference eReference, String ids) {
           super.setValueFromId(
                   object,
                   eReference,
                   eReference.isResolveProxies() ? ((QNameURIHandler) uriHandler)
                           .convertQNameToUri(ids) : ids);
       }

       /**
        * Used from the <extension><definition> tag to load referenced extension schemes.
        * The extension scheme will be loaded and converted to EMF Ecore on the fly.
        *
        * @param id
        */
       private EObject loadExtensionSchema(QName xsdQname) {
           EPackage extensionPackage = extendedMetaData.getPackage(xsdQname.getNamespaceURI());
           if (extensionPackage == null) {

               try {
                   @SuppressWarnings("unchecked")
                   Class<XSDEcoreBuilder> theXSDEcoreBuilderClass = (Class<XSDEcoreBuilder>) CommonPlugin
                           .loadClass("org.eclipse.xsd", "org.eclipse.xsd.ecore.XSDEcoreBuilder");

                   Constructor<XSDEcoreBuilder> theXSDEcoreBuilderConstructor = theXSDEcoreBuilderClass
                           .getConstructor(new Class[] { ExtendedMetaData.class, Map.class });
                   Field theOptionField = theXSDEcoreBuilderClass
                           .getField("OPTION_REUSE_REGISTERED_PACKAGES");
                   Object theXsdOption = theOptionField.get(null);

                   URI location = urisToLocations.get(xsdQname.getNamespaceURI());
                   Map<Object, Object> options = new HashMap<Object, Object>();
                   options.put(theXsdOption, Boolean.TRUE);
                   XSDEcoreBuilder builder = theXSDEcoreBuilderConstructor.newInstance(
                           extendedMetaData, options);
                   builder.generate(location);
               } catch (Exception e) {
               }
           }

           return extendedMetaData.getElement(xsdQname.getNamespaceURI(), xsdQname.getLocalPart());
       }

       @Override
       public void endElement(String uri, String localName, String name) {
           // Detect Extension object
           EObject peekObject = objects.peek();
           if (peekObject instanceof Extension) {
               Extension extension = (Extension) peekObject;
               if (extension.isMustUnderstand() && null != extension.getXsdDefinition()) {
                   loadExtensionSchema(extension.getXsdDefinition());
               }
           }
           super.endElement(uri, localName, name);
       }
   }
}
