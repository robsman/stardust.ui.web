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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;



/**
 * @author anair
 * @version $Revision: $
 */
public class ModelUtils
{
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
      List<DeployedModel> activeModels = ModelUtils.getActiveModels();
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
            List<ProcessDefinition> pds = model.getAllProcessDefinitions();

            for (ProcessDefinition pd : pds)
            {
               datas.addAll(pd.getAllDataPaths());
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
         allDocumentTypes.add(new DocumentTypeWrapper(DocumentTypeUtils.getDocumentTypeFromData(dataModel, docData),
               dataModel));
      }
      return allDocumentTypes;
   }
   
   /**
    * @param documentType
    * @return
    */
   public static DeployedModel getModelForDocumentType(DocumentType documentType)
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

      return null;
   }
}
