/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.PathDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.helpers.ModelHelper;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
public class ActivityInstanceUtils
{
   private static final Logger trace = LogManager.getLogger(ActivityInstanceUtils.class);
   
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ModelUtils modelUtils;

   @Resource
   private DocumentUtils documentUtils;

   /**
    * @param oid
    * @return
    */
   public ActivityInstance getActivityInstance(long oid)
   {
      ActivityInstance ai = null;
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.where(ActivityInstanceQuery.OID.isEqual(oid));
      ActivityInstances ais = serviceFactoryUtils.getQueryService()
            .getAllActivityInstances(query);

      if (!ais.isEmpty())
      {
         ai = ais.get(0);
      }

      return ai;
   }

   /**
    * @param oid
    * @return
    */
   public List<ActivityInstance> getActivityInstances(List<Long> oids)
   {
      if (oids.size() == 0)
      {
         return new ArrayList<ActivityInstance>();
      }
      
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      FilterOrTerm filterOrTerm = query.getFilter().addOrTerm();
      for (Long oid : oids)
      {
         filterOrTerm.add(ActivityInstanceQuery.OID.isEqual(oid));
      }

      ActivityInstances ais = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      return ais;
   }

   /**
    * @param ai
    * @param context
    * @return
    */
   public String getAllDataMappingsAsJson(ActivityInstance ai, String context)
   {
      // TODO: Add process-portal dependency. Till then use reflection!
      try
      {
         Object manualActivityUi = Reflect.createInstance("org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityUi", 
               new Class<?>[]{ActivityInstance.class, ApplicationContext.class, QueryService.class}, 
               new Object[]{ai, ai.getActivity().getApplicationContext(context), serviceFactoryUtils.getQueryService()});
         
         Object manualActivityPath = ReflectionUtils.invokeGetterMethod(manualActivityUi, "manualActivityPath");    
         Object json = ReflectionUtils.invokeMethod(manualActivityPath, "toJsonString");
         
         return json.toString();
      }
      catch (Exception e)
      {
         trace.error("Error in processing data mappings", e);
      }    

      return "";
   }

   /**
    * @param oids
    * @param context
    * @return
    */
   public Map<String, TrivialManualActivityDTO> getTrivialManualActivitiesDetails(List<Long> oids, String context)
   {
      Map<String, TrivialManualActivityDTO> ret = new LinkedHashMap<String, TrivialManualActivityDTO>();

      Map<String, List<PathDTO>> cache = new LinkedHashMap<String, List<PathDTO>>();
      
      List<ActivityInstance> ais = getActivityInstances(oids);
      for (ActivityInstance ai : ais)
      {
         if (isTrivialManualActivity(ai, context))
         {
            if (!cache.containsKey(ai.getActivity().getId()))
            {
               // Get Data Mappings
               List<PathDTO> dataMappings = PathDTO.toList(getAllDataMappingsAsJson(ai, context));

               // Remove readonly mappings
               Iterator<PathDTO> it = dataMappings.iterator();
               while (it.hasNext())
               {
                  PathDTO pathDto = it.next();
                  if (pathDto.readonly)
                  {
                     it.remove();
                  }
               }
               
               cache.put(ai.getActivity().getId(), dataMappings);
            }

            TrivialManualActivityDTO dto = new TrivialManualActivityDTO();
            dto.dataMappings = cache.get(ai.getActivity().getId());

            // Get (IN_)OUT Data
            dto.inOutData = new LinkedHashMap<String, Serializable>();
            Map<String, Serializable> dataValues = getAllInDataValues(ai, context);
            for (Entry<String, Serializable> entry : dataValues.entrySet())
            {
               for (PathDTO pathDto : dto.dataMappings)
               {
                  if (entry.getKey().equals(pathDto.id))
                  {
                     dto.inOutData.put(entry.getKey(), entry.getValue());
                     break;
                  }
               }
            }

            ret.put(String.valueOf(ai.getOID()), dto);
         }
         else
         {
            trace.debug("Skipping Activity Instance.. Not trivial... OID: " + ai.getOID());
         }
      }

      return ret;
   }

   /**
    * @param ai
    * @param context
    * @return
    */
   public boolean isTrivialManualActivity(ActivityInstance ai, String context)
   {
      Model model = modelUtils.getModel(ai.getModelOID());
      ApplicationContext appContext = ai.getActivity().getApplicationContext(context);
      List<?> mappings = appContext.getAllOutDataMappings();
      if (mappings.size() > 0 && mappings.size() <= 2)
      {
         for (Object object : mappings)
         {
            DataMapping dataMapping = (DataMapping) object;
            if (ModelHelper.isEnumerationType(model, dataMapping)
                  || ModelHelper.isPrimitiveType(model, dataMapping))
            {
               // Okay!
            }
            else
            {
               return false;
            }
         }
         
         return true;
      }
      
      return false;
   }

   /**
    * @param ai
    * @param context
    * @return
    */
   public Map<String, Serializable> getAllInDataValues(ActivityInstance ai, String context)
   {
      return serviceFactoryUtils.getWorkflowService().getInDataValues(ai.getOID(), context, null);
   }

   /**
    * @param oid
    * @param outData
    * @return
    */
   public ActivityInstance complete(long oid, String context, Map<String, Serializable> outData)
   {
      return serviceFactoryUtils.getWorkflowService().activateAndComplete(oid, context, outData);
   }

   /**
    * @param oid
    * @return
    */
   public List<Document> getProcessAttachments(long oid)
   {
      List<Document> processAttachments = CollectionUtils.newArrayList();

      ActivityInstance ai = getActivityInstance(oid);

      if (ai != null)
      {
         /*processAttachments = processInstanceUtils.getProcessAttachments(ai
               .getProcessInstanceOID());*/
      }

      return processAttachments;
   }

   /**
    * @param oid
    * @param documentId
    * @return
    */
   public ActivityInstance completeRendezvous(long oid, String documentId)
   {
      ActivityInstance completedAi = null;

      ActivityInstance ai = getActivityInstance(oid);
      Document document = documentUtils.getDocument(documentId);

      if (ai != null && document != null)
      {
         ApplicationContext defaultContext = ai.getActivity().getApplicationContext(
               PredefinedConstants.DEFAULT_CONTEXT);

         if (defaultContext != null)
         {
            // TODO: Code assumes that there is exactly one Document OUT data mapping
            @SuppressWarnings("unchecked")
            List<DataMapping> outDataMappings = defaultContext.getAllOutDataMappings();

            if (outDataMappings != null && outDataMappings.size() == 1)
            {
               DataMapping outDataMapping = outDataMappings.get(0);
               String dataMappingId = outDataMapping.getId();

               Map<String, Object> outData = CollectionUtils.newHashMap();
               outData.put(dataMappingId, (Object) document);

               completedAi = serviceFactoryUtils.getWorkflowService()
                     .activateAndComplete(oid, PredefinedConstants.DEFAULT_CONTEXT,
                           outData);
            }
         }
      }

      return completedAi;
   }

}
