package org.eclipse.stardust.ui.web.modeler.bpmn2.serialization;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.util.ImportHelper;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.xmi.UnresolvedReferenceException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.xml.sax.SAXException;
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
      return new EmbeddedXsdAwareBpmnXmlHandler(resource, helper, options);
   }

   @Override
   protected SAXParser makeParser() throws ParserConfigurationException, SAXException
   {
      SAXParserFactory factory = XmlUtils.newSaxParserFactory(false);
      return factory.newSAXParser();
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
}
