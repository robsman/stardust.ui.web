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

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ActivityInstanceUtils
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ProcessInstanceUtils processInstanceUtils;

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
   public List<Document> getProcessAttachments(long oid)
   {
      List<Document> processAttachments = CollectionUtils.newArrayList();

      ActivityInstance ai = getActivityInstance(oid);

      if (ai != null)
      {
         processAttachments = processInstanceUtils.getProcessAttachments(ai
               .getProcessInstanceOID());
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
