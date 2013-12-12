/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.processportal.service.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.struct.ClientXPathMap;
import org.eclipse.stardust.engine.core.struct.IXPathMap;
import org.eclipse.stardust.engine.core.struct.StructuredDataConverter;
import org.eclipse.stardust.engine.core.struct.StructuredDataXPathUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.sxml.Document;
import org.eclipse.stardust.engine.core.struct.sxml.Element;
import org.eclipse.stardust.engine.core.struct.sxml.Node;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.view.manual.ModelUtils;
import org.eclipse.stardust.ui.web.processportal.view.manual.RawDocument;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileStorage;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.DocumentHandlerBean.InputParameters;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemDocument.FileSystemDocumentAttributes;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemJCRDocument;

/**
 * @author Subodh.Godbole
 * @author Yogesh.Manware
 *
 */
public class InteractionDataUtils
{
   private static final Logger trace = LogManager.getLogger(InteractionDataUtils.class);
   
   /**
    * 
    * @param interaction
    * @param inData
    * @param servletContext
    * @return
    */
   @SuppressWarnings("unchecked")
   public static JsonObject marshalData(Interaction interaction,
         Map<String, ? extends Serializable> inData, ServletContext servletContext)
   {
      Model model = interaction.getModel();
      ApplicationContext context =  interaction.getDefinition();
      
      JsonObject root = new JsonObject();
      JsonHelper jsonHelper = new JsonHelper();
      List<DataMapping> dataMappings = context.getAllInDataMappings();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, ? extends Serializable> entry : inData.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               JsonObject elemDM = new JsonObject();
               
               if (ModelUtils.isDocumentType(model, dm))
               {
                  jsonHelper.toJsonDocument(entry.getValue(), dm, elemDM, model,
                        servletContext, interaction);
               }
               else
               {
                  jsonHelper.toJson(entry.getKey(), entry.getValue(), elemDM);
               }
               
               root.add(dm.getId(), elemDM);
               
               break;
            }
         }
      }

      return root;
   }
   
   /**
    * Converts Data back as per the Data Types
    * @param elem
    * @param context
    * @param interaction 
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Serializable> unmarshalData(Model model,
         ApplicationContext context, Map<String, Object> elem, Interaction interaction,
         ServletContext servletContext) throws DataException
   {
      Map<String, Throwable> errors = new HashMap<String, Throwable>();

      Map<String, Serializable> ret = new HashMap<String, Serializable>();

      List<DataMapping> dataMappings = context.getAllOutDataMappings();

      for (DataMapping dm : dataMappings)
      {
         for (Entry<String, Object> entry : elem.entrySet())
         {
            if (entry.getKey().equals(dm.getId()))
            {
               try
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("DM: " + entry.getKey());
                  }
   
                  Object value = evaluateClientSideOutMapping(model, entry.getValue(), dm, interaction, servletContext);
   
                  if (trace.isDebugEnabled())
                  {
                     trace.debug(", Value: " + value);
                  }
   
                  if (value instanceof Serializable)
                  {
                     ret.put(entry.getKey(), (Serializable) value);
                  }
                  else
                  {
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("value is not serializable for " + dm.getId());
                     }
                  }
               }
               catch(Exception e)
               {
                  errors.put(entry.getKey(), e);
               }
               break;
            }
         }
      }

      if (errors.size() > 0)
      {
         throw new DataException(errors);
      }
         
      return ret;
   }

   /**
    * @param model
    * @param value
    * @param outMapping
    * @param interaction 
    * @return
    */
   public static Object evaluateClientSideOutMapping(Model model, Object value,
         DataMapping outMapping, Interaction interaction, ServletContext servletContext)
   {
      Object result = null;

      if (ModelUtils.isStructuredType(model, outMapping))
      {
         Data data = model.getData(outMapping.getDataId());
         if (data.getModelOID() != model.getModelOID())
         {
            model = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
         }
         result = evaluateStructMapping(model, outMapping, value, outMapping.getApplicationPath());
      }
      else if (ModelUtils.isPrimitiveType(model, outMapping))
      {
         result = (Serializable)DataFlowUtils.unmarshalPrimitiveValue(model, outMapping, value.toString());
      }
      else if (ModelUtils.isDocumentType(model, outMapping))
      {
         if (value instanceof Map<? , ? >)
         {
            Map<String, Object> details = (Map<String, Object>) value;
            
            if (details.get(DOCUMENT.TYPEJ).equals(DOCUMENT.TYPE.FILE_SYSTEM.toString()))
            {
               result = createDocument(details, model, outMapping, interaction,
                     servletContext);
            }
            else if (details.get(DOCUMENT.TYPEJ).equals(DOCUMENT.TYPE.JCR.toString()))
            {
               // JCR document
               result = getDocument(interaction, outMapping);
            }
            else
            {
               // document removed or no change
            }
         }
      }
      else
      {
         // TODO support additional types?
      }

      return result;
   }
   
   /**
    * 
    * @param details
    * @param model
    * @param outMapping
    * @param interaction
    * @param servletContext
    * @return
    */
   private static FileSystemJCRDocument createDocument(Map<String, Object> details,
         Model model, DataMapping outMapping, Interaction interaction,
         ServletContext servletContext)
   {
      // pull physical path
      FileStorage fileStorage = (FileStorage) RestControllerUtils.resolveSpringBean(
            "fileStorage", servletContext);
      String uuid = (String) details.get(DOCUMENT.ID);
      String path = fileStorage.pullPath(uuid);
      InputParameters inputParam = fileStorage.pullFile(uuid);
      FileSystemJCRDocument fileSystemJCRDoc;
      
      if (inputParam == null)
      {
         // File system document
         RawDocument rawDocument = new RawDocument();

         rawDocument.setName((String) details.get(DOCUMENT.FILE_NAME));
         rawDocument.setContentType((String) details.get(DOCUMENT.CONTENT_TYPE));

         rawDocument.setDescription((String) details.get(DOCUMENT.DESCRIPTION));
         rawDocument.setComments((String) details.get(DOCUMENT.VERSION_COMMENT));

         rawDocument.setPhysicalPath(path);

         // set Document Type
         DocumentType dType = DocumentTypeUtils.getDocumentType(
               (String) details.get(DOCUMENT.TYPE_ID), model);
         rawDocument.setDocumentType(dType);

         fileSystemJCRDoc = getFileSystemDocument(rawDocument, interaction,
               servletContext);

         inputParam = new InputParameters();
         inputParam.setDocumentContentInfo(fileSystemJCRDoc);
         inputParam.setDataId(outMapping.getDataId());
         inputParam.setDataPathId(outMapping.getDataPath());
         inputParam.setProcessInstancOid(interaction.getActivityInstance()
               .getProcessInstance()
               .getOID());

         fileStorage.pushFile(uuid, inputParam);

      }
      else
      {
         fileSystemJCRDoc = (FileSystemJCRDocument) inputParam.getDocumentContentInfo();
      }

      return fileSystemJCRDoc;
   }
   
   /**
    * 
    * @param interaction
    * @param outMapping
    * @return
    */
   private static org.eclipse.stardust.engine.api.runtime.Document getDocument(Interaction interaction, DataMapping outMapping)
   {
      for (Entry<String, ? extends Serializable> entry : interaction.getInDataValues()
            .entrySet())
      {
         if (entry.getKey().equals(outMapping.getDataId()))
         {
            JsonObject elemDM = new JsonObject();
            if (ModelUtils.isDocumentType(interaction.getModel(), outMapping))
            {
               String typeDeclarationId = DocumentTypeUtils.getMetaDataTypeDeclarationId(interaction.getModel().getData(outMapping.getDataId()));

               elemDM.add("docTypeId", new JsonPrimitive(typeDeclarationId));

               return (org.eclipse.stardust.engine.api.runtime.Document) entry.getValue();
            }
         }
      }
      return null;
   }  
   
   /**
    * @param doc
    * @return
    */
   private static FileSystemJCRDocument getFileSystemDocument(org.eclipse.stardust.engine.api.runtime.Document doc, Interaction interaction, ServletContext servletContext)
   {
      if (doc instanceof RawDocument)
      {
         RawDocument rawDocument = (RawDocument) doc;

         //         String parentFolder = DocumentMgmtUtility.getTypedDocumentsFolderPath(interaction.getActivityInstance()
         //               .getProcessInstance());
         
         FileSystemDocumentAttributes fileSystemDocumentAttributes = new FileSystemDocumentAttributes();
         MessagesViewsCommonBean viewBean = (MessagesViewsCommonBean) RestControllerUtils.resolveSpringBean(
               "views_common_msgPropsBean", servletContext);

         fileSystemDocumentAttributes.setResourcePath(rawDocument.getPhysicalPath());
         fileSystemDocumentAttributes.setDocumentType(doc.getDocumentType());
         
         MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
               "ippMimeTypesHelper", servletContext);
         
         MIMEType mimeType = mimeTypesHelper.detectMimeTypeI(rawDocument.getName(),
               rawDocument.getContentType());
         
         fileSystemDocumentAttributes.setMimeType(mimeType);
         fileSystemDocumentAttributes.setEditable(true);

         fileSystemDocumentAttributes.setDefaultAuthor(viewBean.getString("views.documentView.properties.author.default"));
         fileSystemDocumentAttributes.setDefaultAuthor(viewBean.getString("views.documentView.properties.id.default"));
         
         return new FileSystemJCRDocument(fileSystemDocumentAttributes, null,
               rawDocument.getDescription(), rawDocument.getComments());
      }

      return null;
   }
   
   /**
    * This code was copied from
    *   org.eclipse.stardust.ui.web.viewscommon.utils.ClientSideDataFlowUtils#evaluateStructOutMapping(...)
    * and stripped down.
    *
    */
   private static Object evaluateStructMapping(Model model, DataMapping mapping, Object data, String outPath)
   {
      Set<TypedXPath> xPaths = ModelUtils.getXPaths(model, mapping);

      final IXPathMap xPathMap = new ClientXPathMap(xPaths);

      StructuredDataConverter converter = new StructuredDataConverter(xPathMap);
      Document document;

      Node[] nodes = converter.toDom(data, "", true);
      Assert.condition(nodes.length == 1);
      document = new Document((Element) nodes[0]);

      boolean namespaceAware = StructuredDataXPathUtils.isNamespaceAware(document);

      Object returnValue = converter.toCollection(document.getRootElement(), outPath, namespaceAware);

      if (trace.isDebugEnabled())
      {
         if (null == returnValue)
         {
            trace.debug("returning null for outPath '" + outPath + "'");
         }
         else
         {
            trace.debug("returning returnValue of type '"
                  + returnValue.getClass().getName() + "' for outPath '" + outPath + "'");
         }
      }

      return returnValue;
   }
   
   protected static class DOCUMENT
   {
      protected enum TYPE
      {
         FILE_SYSTEM ("FILE_SYSTEM"), JCR ("JCR"), NONE ("none");
         private String type;

         TYPE(String type)
         {
            this.type = type;
         }

         @Override
         public String toString()
         {
            return this.type;
         }
      }

      protected static final String DOC_PATH = "../../plugins/views-common/images/icons/";
      
      protected static final String TYPE_ID = "docTypeId";
      
      protected static final String TYPE_NAME = "docTypeName";

      protected static final String ID = "docId";

      protected static final String NAME = "docName";

      protected static final String ICON = "docIcon";

      protected static final String FILE_NAME = "fileName";

      protected static final String CONTENT_TYPE = "contentType";

      protected static final String DESCRIPTION = "fileDescription";

      protected static final String VERSION_COMMENT = "versionComment";
      
      protected static final String TYPEJ = "type";
   }
}
