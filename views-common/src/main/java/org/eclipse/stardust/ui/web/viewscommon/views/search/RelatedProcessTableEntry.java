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
package org.eclipse.stardust.ui.web.viewscommon.views.search;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 * @since 7.0
 */
public class RelatedProcessTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private boolean selected;
   private final String processName;
   private final long oid;
   private final String priority;
   private final Date startTime;
   private final ProcessInstance processInstance;
   private final ProcessDefinition processDefinition;
   private Map<String, Object> descriptorValues = new HashMap<String, Object>();
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   private final String caseOwner;

   /**
    * 
    * @param processInstance
    */
   public RelatedProcessTableEntry(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
      this.processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      this.processName = I18nUtils.getProcessName(processDefinition);
      this.oid = processInstance.getOID();

      this.startTime = processInstance.getStartTime();
      descriptorValues = ((ProcessInstanceDetails) this.processInstance).getDescriptors();

      if (processInstance.getPriority() == 1)
      {
         this.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.high");
      }
      else if (processInstance.getPriority() == -1)
      {
         this.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.low");
      }
      else
      {
         this.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.normal");
      }
      if (processInstance.isCaseProcessInstance())
      {
         caseOwner = ProcessInstanceUtils.getCaseOwnerName(processInstance);
      }
      else
      {
         caseOwner = null;
      }

   }

   public String getProcessName()
   {
      return processName;
   }

   public long getOid()
   {
      return oid;
   }

   public String getPriority()
   {
      return priority;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public Map<String, Object> getDescriptorValues()
   {
      return descriptorValues;
   }

   public void setDescriptorValues(Map<String, Object> descriptorValues)
   {
      this.descriptorValues = descriptorValues;
   }

   public String getCaseOwner()
   {
      return caseOwner;
   }
   
}
