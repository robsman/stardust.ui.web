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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * The Instance Manager manages the loading of the process instances which must be
 * considered for the Gantt Diagram View.
 * 
 * @author mueller1
 * 
 */
public class InstanceManager
{

   private final static String PROCESS_INSTANCE_OID = "processInstanceOid";

   private final static String CORRELATION_ID = "CorrelationID";

   private static Logger logger = LogManager.getLogger(InstanceManager.class);

   private Long processInstanceOid = null;

   private ProcessInstances pInstances;

   private String processDefinitionId = null;

   private Date referenceDate = null;

   private Map<String, ProcessProgressInstance> instanceMap = null;

   private ServiceFactory sFactory = null;

   public InstanceManager()
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();

      this.sFactory = sessionCtx.getServiceFactory();
      this.processInstanceOid = (Long) sessionCtx.lookup(PROCESS_INSTANCE_OID);

      this.instanceMap = CollectionUtils.newHashMap();
      loadProcessInstances();
   }

   public Long getProcessInstanceOid()
   {
      return processInstanceOid;
   }

   public ProcessInstances getProcesses()
   {
      return pInstances;
   }

   private QueryService getQueryService()
   {
      return this.sFactory.getQueryService();
   }

   /**
    * Method loads all process instances which must be considered for the Gantt Diagram
    * View.
    */
   private void loadProcessInstances()
   {

      // reads all process instances
      pInstances = getProcessInstances();
      if (pInstances == null)
      {
         return;
      }

      for (Iterator<ProcessInstance> _iterator = pInstances.iterator(); _iterator.hasNext();)
      {

         ProcessInstance child = _iterator.next();

         if (child.getOID() == this.processInstanceOid.longValue())
         {
            ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(child.getModelOID(),
                  child.getProcessID());
            this.processDefinitionId = pd.getQualifiedId();
            this.referenceDate = child.getStartTime();
         }

         String businessInstanceId = processDefinitionId; // "root";

         if (child.getOID() != this.processInstanceOid.longValue())
         {
            businessInstanceId = getBusinessInstanceId(child);
         }

         logger.info("Put instance with id " + child.getProcessID() + " into cache with the key " + businessInstanceId
               + ".");

         instanceMap.put(businessInstanceId, new ProcessProgressInstance(child));
         // }
      }

   }

   /**
    * Reads all descriptor values needed to differ between all the instances.
    * 
    * @param pInstance
    * @return
    */
   private String readInstanceDescriptorValues(ProcessInstance pInstance)
   {

      StringBuffer descriptorValues = new StringBuffer();

      ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(pInstance.getModelOID(),
            pInstance.getProcessID());

      String descriptorIDs = PropertyProvider.getInstance().getProperty(pd.getQualifiedId(),
            PropertyProvider.INSTANCE_DESCRIPTOR_KEY);

      if (StringUtils.isNotEmpty(descriptorIDs))
      {
         StringTokenizer strTok = new StringTokenizer(descriptorIDs, ".");

         while (strTok.hasMoreElements())
         {
            String descriptorId = (String) strTok.nextElement();
            Object value = ((IDescriptorProvider) pInstance).getDescriptorValue(descriptorId);
            descriptorValues.append(value);

            if (strTok.hasMoreElements())
            {
               descriptorValues.append('.');
            }
         }
      }

      return descriptorValues.toString().replace(' ', '_');
   }

   /**
    * Method reads the descriptor value of the given process instance. Which descriptor
    * has to be read is defined in the configuration file.
    * 
    * e.g. TradeIntegration.instance.descriptor.key=FundType
    * 
    * 
    * @param pInstance
    * @return
    */
   private String readInstanceDescriptorValue(ProcessInstance pInstance)
   {
      ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(pInstance.getModelOID(),
            pInstance.getProcessID());
      String value = PropertyProvider.getInstance().getProperty(pd.getQualifiedId(),
            PropertyProvider.INSTANCE_DESCRIPTOR_KEY);

      Object obj = ((IDescriptorProvider) pInstance).getDescriptorValue(value);

      return (String) obj;
   }

   /**
    * Method computes an ID needed to differ process instances belonging to the same
    * process definition
    * 
    * @param pInstance
    * @return
    */
   private String getBusinessInstanceId(ProcessInstance pInstance)
   {
      ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(pInstance.getModelOID(),
            pInstance.getProcessID());

      String businessInstanceId = null;

      String descriptorValues = readInstanceDescriptorValues(pInstance);

      logger.info("Descriptor Values: " + descriptorValues);

      if (StringUtils.isNotEmpty(descriptorValues))
      {
         businessInstanceId = pd.getQualifiedId() + "." + descriptorValues;
      }
      else
      {
         businessInstanceId = pd.getQualifiedId();
      }

      return businessInstanceId;
   }

   /**
    * Method loads the whole process instance hierarchy for a given process instance OID.
    * Furthermore all process instances are considered which have the given process
    * instance OID assigned as CorrelationID.
    * 
    * Descriptor values will be evaluated.
    * 
    * @return
    */
   private ProcessInstances getProcessInstances()
   {
      if (this.processInstanceOid == null)
      {
         return null;
      }

      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter().add(new ProcessInstanceFilter(this.processInstanceOid.longValue(), true));
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

      ProcessInstanceQuery query2 = ProcessInstanceQuery.findAll();
      query2.getFilter().add((DataFilter.isEqual(CORRELATION_ID, this.processInstanceOid)));
      query2.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

      ProcessInstances allProcessInstances = getQueryService().getAllProcessInstances(query);
      ProcessInstances allProcessInstances2 = getQueryService().getAllProcessInstances(query2);

      allProcessInstances.addAll(allProcessInstances2);

      return allProcessInstances;
   }

   public Date getReferenceDate()
   {
      return referenceDate;
   }

   public Map<String, ProcessProgressInstance> getInstanceMap()
   {
      return instanceMap;
   }

   public String getProcessDefinitionId()
   {
      return processDefinitionId;
   }

}