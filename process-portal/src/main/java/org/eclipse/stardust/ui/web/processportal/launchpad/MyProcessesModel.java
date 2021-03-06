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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;




/**
 * @author roland.stamm
 * 
 */
public class MyProcessesModel
{
   private ProcessDefinition processDefinition;

   /**
    * @param processDefinition
    */
   public MyProcessesModel(ProcessDefinition processDefinition)
   {
      super();
      this.processDefinition = processDefinition;
   }

   /**
    * @return
    */
   public String select()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put(Query.class.getName(), ProcessWorklistCacheManager.getInstance().getActivityInstanceQuery(processDefinition));
      params.put("id", processDefinition.getQualifiedId());
      params.put("name", I18nUtils.getProcessName(processDefinition));
      params.put("processDefinition", processDefinition);

      PPUtils.openWorklistView("id=" + processDefinition.getQualifiedId(), params);

      PPUtils.selectWorklist(null);
      return null;
   }
   
   /**
    * @return
    */
   public String selectHTML5()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put("id", processDefinition.getQualifiedId());
      params.put("name", I18nUtils.getProcessName(processDefinition));
      params.put("processQId", processDefinition.getQualifiedId());

      PPUtils.openWorklistViewHTML5("id=" +processDefinition.getQualifiedId() , params);
      PPUtils.selectWorklist(null);
      return null;
   }

   public String getTotalCount()
   {
      Long totalCount = ProcessWorklistCacheManager.getInstance().getWorklistCount(processDefinition);
      Long totalCountThreshold = ProcessWorklistCacheManager.getInstance().getWorklistCountThreshold(processDefinition);
      if (totalCount < Long.MAX_VALUE)
         return totalCount.toString();
      else
         return MessagesViewsCommonBean.getInstance().getParamString("common.notification.worklistCountThreshold",
               totalCountThreshold.toString());
   }

   public String getName()
   {
      return I18nUtils.getProcessName(processDefinition);
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }
   
   public String getTitle()
   {
      return I18nUtils.getDescriptionAsHtml(processDefinition, processDefinition.getDescription());
   }
}
