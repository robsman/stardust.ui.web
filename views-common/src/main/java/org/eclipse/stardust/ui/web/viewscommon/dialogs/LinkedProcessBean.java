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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;



/**
 * 
 * @author Sidharth.Singh
 * @since 7.0 bean for LinkedProcess Panel
 * 
 */
public class LinkedProcessBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "linkedProcessBean";
   private ProcessInstance fromLinkedProcess;
   private String fromProcessName;
   private ProcessInstance joinLinkedProcess;
   private String joinProcessName;

   /**
    * @return
    */
   public static LinkedProcessBean getCurrent()
   {
      LinkedProcessBean bean = (LinkedProcessBean) FacesUtils.getBeanFromContext(BEAN_NAME);
      return bean;

   }

   public ProcessInstance getFromLinkedProcess()
   {
      return fromLinkedProcess;
   }

   public void setFromLinkedProcess(ProcessInstance fromLinkedProcess)
   {
      this.fromLinkedProcess = fromLinkedProcess;
   }

   public String getFromProcessName()
   {
      return fromProcessName;
   }

   public void setFromProcessName(String fromProcessName)
   {
      this.fromProcessName = fromProcessName;
   }

   public ProcessInstance getJoinLinkedProcess()
   {
      return joinLinkedProcess;
   }

   public void setJoinLinkedProcess(ProcessInstance joinLinkedProcess)
   {
      this.joinLinkedProcess = joinLinkedProcess;
   }

   public String getJoinProcessName()
   {
      return joinProcessName;
   }

   public void setJoinProcessName(String joinProcessName)
   {
      this.joinProcessName = joinProcessName;
   }

   /**
    * 
    */
   public void openProcessDetial()
   {
      ProcessInstance pi = null;
      // Either linkage will be Switch or Join for current process
      if (null != getFromLinkedProcess())
      {
         pi = getFromLinkedProcess();
      }
      else
         pi = getJoinLinkedProcess();
      Map<String, Object> params = CollectionUtils.newHashMap();
      params.put("processInstanceOID", String.valueOf(pi.getOID()));
      params.put("processInstanceName", pi.getProcessName());
      String key = "processInstanceOID=" + pi.getOID();

      PortalApplication.getInstance().openViewById("processInstanceDetailsView", key, params, null, true);
   }

}
