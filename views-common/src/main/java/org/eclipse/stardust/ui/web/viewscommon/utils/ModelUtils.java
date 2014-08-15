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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.engine.extensions.xml.data.XPathUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



/**
 * @author anair
 * @version $Revision: $
 */
public class ModelUtils
{
   private static final Logger trace = LogManager.getLogger(ModelUtils.class);

   private ModelUtils()
   {
      // Utility class
   }

   /**
    * @return
    */
   public static ModelCache getModelCache()
   {
      return ModelCache.findModelCache();
   }

   public static Collection<DeployedModel> getAllModels()
   {
      ModelCache modelCache = getModelCache();
      return modelCache != null ? modelCache.getAllModels() : Collections.<DeployedModel> emptyList();
   }

   /**
    * Returns all models sorted by active models first
    *
    * @return
    */
   public static List<DeployedModel> getAllModelsActiveFirst()
   {
      List<DeployedModel> allModels = CollectionUtils.newList(getAllModels());
      Iterator<DeployedModel> iterateModel = allModels.iterator();
      List<DeployedModel> activeModels = CollectionUtils.newArrayList();
      while (iterateModel.hasNext())
      {
         DeployedModel model = iterateModel.next();
         if (model.isActive())
         {
            activeModels.add(model);
            iterateModel.remove();
         }
      }
      activeModels.addAll(allModels);
      return activeModels;
   }

   /**
    * @return
    */
   @Deprecated
   // TODO: ActiveModel
   public static Model getActiveModel()
   {
      ModelCache modelCache = getModelCache();
      return modelCache != null ? modelCache.getActiveModel() : null;
   }

   /**
    * @return
    */
   public static List<DeployedModel> getActiveModels()
   {
      ModelCache modelCache = getModelCache();
      return modelCache != null ? modelCache.getActiveModels() : null;
   }

   /**
    * @return
    */
   public static DeployedModel getActiveModel(String modelId)
   {
      ModelCache modelCache = getModelCache();
      return modelCache != null ? modelCache.getActiveModel(modelId) : null;
   }

   /**
    * @param modelOid
    * @return
    */
   public static DeployedModel getModel(long modelOid)
   {
      ModelCache modelCache = getModelCache();
      return modelCache != null ? modelCache.getModel(modelOid) : null;
   }

   /**
    * Returns the model with the given Id. Initial search is in "Active" models (see
    * {@link #getActiveModel(String)}). If an "Active" model is not found, then search in
    * "All" models (see {@link #getAllModels()}).
    *
    * @param modelId
    * @return
    */
   public static DeployedModel getModel(String modelId)
   {
      // Initial search is "Active" models
      DeployedModel model = getActiveModel(modelId);

      // If an "Active" model is not found, then search in "All" models
      if (model == null)
      {
         Collection<DeployedModel> allModels = getAllModels();
         for (DeployedModel deployedModel : allModels)
         {
            if (deployedModel.getId().equals(modelId))
            {
               model = deployedModel;
               break;
            }
         }
      }

      return model;
   }

   /**
    * @param qualifiedParticipantId
    * @return
    */
   public static String extractModelId(String qualifiedParticipantId)
   {
      String modelId = null;

      if (qualifiedParticipantId.startsWith("{"))
      {
         QName qname = QName.valueOf(qualifiedParticipantId);
         modelId = qname.getNamespaceURI();
      }

      return modelId;
   }


   /**
    * @param qualifiedParticipantId
    * @return
    */
   public static String extractParticipantId(String qualifiedParticipantId)
   {
      String participantId = null;

      if (qualifiedParticipantId.startsWith("{"))
      {
         QName qname = QName.valueOf(qualifiedParticipantId);
         participantId = qname.getLocalPart();
      }

      return participantId;
   }

   /**
    * returns admin participant if at least one active model exist
    *
    * @return
    */
   public static Participant getAdminParticipant()
   {
      List<DeployedModel> activeModels = getActiveModels();
      if (CollectionUtils.isNotEmpty(activeModels))
      {
         return activeModels.get(0).getParticipant(PredefinedConstants.ADMINISTRATOR_ROLE);
      }
      return null;
   }



   /**
    * method returns Map of DataPath from all active models (exclude PredefinedModel)
    * @return
    */
   public static  Map<String, DataPath> getAllDataPath()
   {
      List<DataPath> datas = CollectionUtils.newArrayList();
      Collection<DeployedModel> models = ModelCache.findModelCache().getActiveModels();
      for (DeployedModel model : models)
      {
         if (!PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
         {
            @SuppressWarnings("unchecked")
            List<ProcessDefinition> pds = model.getAllProcessDefinitions();
            for (ProcessDefinition pd : pds)
            {
               @SuppressWarnings("unchecked")
               List<DataPath> paths = pd.getAllDataPaths();
               datas.addAll(paths);
            }
         }
      }
      Map<String, DataPath> dataMap = CollectionUtils.newHashMap();
      for (DataPath path : datas)
      {
         if (path.isKeyDescriptor())
         {
            dataMap.put(path.getId(), path);
         }
      }
      return dataMap;
   }

   /**
    * Returns the Document Types for all models with keeping Active Model at Priority
    * @return
    */
   public static Set<DocumentTypeWrapper> getAllDeclaredDocumentTypes()
   {
      Set<DocumentTypeWrapper> allDocumentTypes = CollectionUtils.newHashSet();

      // Add Active First
      allDocumentTypes.addAll(getAllActiveDeclaredDocumentTypes());

      // Add Other Models
      for (DeployedModel model : getModelCache().getActiveModels())
      {
         if (!model.isActive())
         {
            allDocumentTypes.addAll(getDeclaredDocumentTypes(model));
         }
      }

      return allDocumentTypes;
   }

   /**
    * Returns the Document Types for all Active Models
    * @return
    */
   public static Set<DocumentTypeWrapper> getAllActiveDeclaredDocumentTypes()
   {
      Set<DocumentTypeWrapper> allDocumentTypes = CollectionUtils.newHashSet();
      for (DeployedModel model : getModelCache().getActiveModels())
      {
         allDocumentTypes.addAll(getDeclaredDocumentTypes(model));
      }

      return allDocumentTypes;
   }

   /**
    * @param model
    * @return
    */
   public static Set<DocumentTypeWrapper> getDeclaredDocumentTypes(DeployedModel model)
   {
      Set<DocumentTypeWrapper> allDocumentTypes = CollectionUtils.newHashSet();
      for (DocumentType documentType : DocumentTypeUtils.getDeclaredDocumentTypes(model))
      {
         allDocumentTypes.add(new DocumentTypeWrapper(documentType, model));
      }
      // Get the reference document data for given model and get DocType using Data
      // modelID
      List<Data> referedDocData = DocumentTypeUtils.getReferencedDocumentData(model);
      for (Data docData : referedDocData)
      {
         DeployedModel dataModel = getModel(docData.getModelOID());
         DocumentType documentType = getDocumentTypeFromData(dataModel, docData);
         if (documentType != null)
         {
            allDocumentTypes.add(new DocumentTypeWrapper(documentType, dataModel));
         }
         else
         {
            trace.error("Could not resolve type for Document " + docData.getQualifiedId());
         }
      }
      return allDocumentTypes;
   }

   /**
    *
    * @param dataMapping
    * @return
    */
   public static Set<TypedXPath> getXPaths(DataMapping dataMapping)
   {
      Model model = ModelCache.findModelCache().getModel(dataMapping.getModelOID());
      Data data = model.getData(dataMapping.getDataId());
      Reference ref = data.getReference();
      Model refModel = model;

      String typeDeclarationId = null;
      if (ref == null)
      {
         typeDeclarationId = (String) data.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);
         if (data.getModelOID() != refModel.getModelOID())
         {
            refModel = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(data.getModelOID());
         }
         if (!StringUtils.isEmpty(typeDeclarationId) && typeDeclarationId.indexOf("typeDeclaration") == 0)
         {
            // For data created in current model, Structured type in different model
            try
            {
               String parts[] = typeDeclarationId.split("\\{")[1].split("\\}");
               typeDeclarationId = parts[1];
               Model newRefModel = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.getModel(parts[0]);
               refModel = newRefModel != null ? newRefModel : refModel;
            }
            catch (Exception e)
            {
               trace.error("Error occured in Type declaration parsing", e);
            }
         }
      }
      else
      {
         typeDeclarationId = ref.getId();
         refModel = ModelCache.findModelCache().getModel(ref.getModelOid());
      }
      return XPathUtils.getXPaths(refModel, typeDeclarationId, dataMapping.getDataPath());
   }

   public static DocumentType getDocumentTypeFromData(Model model, Data data)
   {
      DocumentType result = null;
      int modelOid = model.getModelOID();

      if (DocumentTypeUtils.isDmsDocumentData(data.getTypeId()))
      {
         if(data.getModelOID() != modelOid && null != model.getData(data.getId()))
         {
            model = getModel(data.getModelOID());
         }

         String typeDeclarationId = DocumentTypeUtils.getMetaDataTypeDeclarationId(data);
         Reference ref = data.getReference();
         if (ref != null)
         {
            DeployedModel otherModel = getModel(ref.getModelOid());
            if (otherModel != null)
            {
               model = otherModel;
               typeDeclarationId = ref.getId();
            }
         }
         if (typeDeclarationId != null)
         {
            result = DocumentTypeUtils.getDocumentType(typeDeclarationId, model);

         }
      }

      return result;
   }

   /**
    * @param model
    * @param activity
    * @param ap
    * @return
    */
   public static Set<TypedXPath> getXPaths(Model model, Activity activity, AccessPoint ap)
   {
      Model refModel = model;

      String typeDeclarationId = (String) ap.getAttribute(StructuredDataConstants.TYPE_DECLARATION_ATT);

      if (!StringUtils.isEmpty(typeDeclarationId) && typeDeclarationId.indexOf("typeDeclaration:") == 0)
      {
         // For data created in current model, Structured type in different model
         try
         {
            String parts[] = typeDeclarationId.split("\\{")[1].split("\\}");
            typeDeclarationId = parts[1];
            Model newRefModel = getModel(model.getResolvedModelOid(parts[0]));
            refModel = newRefModel != null ? newRefModel : refModel;
         }
         catch (Exception e)
         {
            trace.error("Error occured in Type declaration parsing", e);
         }
      }
      return XPathUtils.getXPaths(refModel, typeDeclarationId);
   }

   /**
    * @param documentType
    * @return
    */
   public static DeployedModel getModelForDocumentType(DocumentType documentType)
   {
      if(null != documentType)
      {
         try
         {
            // For Internal XSDs
            String schemaLocation = documentType.getSchemaLocation();
            String modelNo = schemaLocation.substring(schemaLocation.indexOf("?") + 1);

            long modelOID = Long.parseLong(modelNo);
            return ModelCache.findModelCache().getModel(modelOID);
         }
         catch (Exception e)
         {
            // For External XSDs, Lookup all Models, starting with Active

            // Process Active Models
            for (DeployedModel model : getActiveModels())
            {
               if (null != model.getTypeDeclaration(documentType))
               {
                  return model;
               }
            }

            // Process In-Active Models
            for (DeployedModel model : getAllModels())
            {
               if (!model.isActive() && null != model.getTypeDeclaration(documentType))
               {
                  return model;
               }
            }
         }
      }
      return null;
   }

   /**
    * returns Models referring to the provided model
    *
    * @param modelId
    * @return
    */
   public static List<DeployedModel> findReferringModels(String modelId)
   {
      List<DeployedModel> usingModels = new ArrayList<DeployedModel>();

      if (StringUtils.isEmpty(modelId))
      {
         return usingModels;
      }

      DeployedModel model = getModel(modelId);

      if (null == model)
      {
         return usingModels;
      }

      SessionContext sessionContext = SessionContext.findSessionContext();
      ServiceFactory serviceFactory = sessionContext.getServiceFactory();
      if (serviceFactory != null)
      {
         try
         {
            Models models = serviceFactory.getQueryService().getModels(
                  DeployedModelQuery.findUsing(model.getModelOID()));
            for (DeployedModelDescription deployedModelDescription : models)
            {
               usingModels.add(getModel(deployedModelDescription.getModelOID()));
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      return usingModels;
   }
}
