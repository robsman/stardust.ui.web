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
package org.eclipse.stardust.ui.web.bcc;

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.springframework.beans.factory.InitializingBean;

/**
 * Gives info about oldest Process Instance
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class AuditTrailProcessInstanceInfo implements Serializable, InitializingBean
{
   /**
    * 
    */
   private static final long serialVersionUID = -1097027664142516696L;
   private ProcessInstance processInstance;

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void afterPropertiesSet() throws Exception
   {
      SessionContext sessionContext = SessionContext.findSessionContext();
      ServiceFactory serviceFactory = (null != sessionContext) ? sessionContext.getServiceFactory() : null;
      if (serviceFactory != null)
      {
         QueryService qs = serviceFactory.getQueryService();
         ProcessInstanceQuery query = new ProcessInstanceQuery();

         query.orderBy(ProcessInstanceQuery.START_TIME, true);
         query.setPolicy(new SubsetPolicy(1));
         List<ProcessInstance> instances = qs.getAllProcessInstances(query);
         processInstance = !CollectionUtils.isEmpty(instances) ? instances.get(0) : null;
      }
   }

}
