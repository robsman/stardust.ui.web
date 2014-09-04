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

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ProcessInstanceUtils
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ModelUtils modelUtils;

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;

   /**
    * @param oid
    * @return
    */
   public ProcessInstance getProcessInstance(long oid)
   {
      ProcessInstance pi = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(oid));
      ProcessInstances pis = serviceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

      if (!pis.isEmpty())
      {
         pi = pis.get(0);
      }

      return pi;
   }

   /**
    * @param oid
    * @return
    */
   public List<Document> getProcessAttachments(long oid)
   {
      List<Document> processAttachments = CollectionUtils.newArrayList();

      boolean supportsProcessAttachments = supportsProcessAttachments(oid);
      if (supportsProcessAttachments)
      {
         ProcessInstance processInstance = getProcessInstance(oid);

         WorkflowService ws = serviceFactoryUtils.getWorkflowService();
         Object o = ws.getInDataPath(processInstance.getOID(),
               DmsConstants.PATH_ID_ATTACHMENTS);

         DataDetails data = (DataDetails) modelUtils.getModel(
               processInstance.getModelOID()).getData(DmsConstants.PATH_ID_ATTACHMENTS);
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(data.getTypeId()))
         {
            processAttachments = (List<Document>) o;
         }
      }

      return processAttachments;
   }

   /**
    * return true if the provided Process Instance supports Process Attachments
    * 
    * @param oid
    * @return
    */
   public boolean supportsProcessAttachments(long oid)
   {
      boolean supportsProcessAttachments = false;

      ProcessInstance processInstance = getProcessInstance(oid);

      if (processInstance != null)
      {
         Model model = modelUtils.getModel(processInstance.getModelOID());
         ProcessDefinition pd = model != null ? model
               .getProcessDefinition(processInstance.getProcessID()) : null;

         supportsProcessAttachments = processDefinitionUtils
               .supportsProcessAttachments(pd);
      }

      return supportsProcessAttachments;
   }

}
