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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Subodh.Godbole
 * 
 */
public abstract class UIViewComponentBean extends UIComponentBean
{
   private static final long serialVersionUID = 1L;

   private static final String DEFAULT_EMBEDDED_BASE_PATH = ".";

   private static final String DEFAULT_NON_EMBEDDED_BASE_PATH = "../..";   

   private ProcessInstance currentProcessInstance;

   private ActivityInstance currentActivityInstance;

   private boolean embedded = true;

   private String basePath;

   // ************* PROTECTED METHODS **********************

   /**
    * Provided for backward compatibility
    */
   public UIViewComponentBean()
   {}

   /**
    * @param viewName
    */
   public UIViewComponentBean(String viewName)
   {
      super(viewName);
   }

   /**
    * @return
    */
   protected QueryService getQueryService()
   {
      return ServiceFactoryUtils.getQueryService();
   }

   // ************* MODIFIED GETTER SETTER METHODS **********************

   public String getBasePath()
   {
      if (StringUtils.isEmpty(basePath))
      {
         basePath = isEmbedded() ? DEFAULT_EMBEDDED_BASE_PATH : DEFAULT_NON_EMBEDDED_BASE_PATH;
      }

      return basePath;
   }

   // ************* DEFAULT GETTER SETTER METHODS **********************

   public ProcessInstance getCurrentProcessInstance()
   {
      return currentProcessInstance;
   }

   public void setCurrentProcessInstance(ProcessInstance currentProcessInstance)
   {
      this.currentProcessInstance = currentProcessInstance;
   }

   public ActivityInstance getCurrentActivityInstance()
   {
      return currentActivityInstance;
   }

   public void setCurrentActivityInstance(ActivityInstance currentActivityInstance)
   {
      this.currentActivityInstance = currentActivityInstance;
   }

   public boolean isEmbedded()
   {
      return embedded;
   }

   public void setEmbedded(boolean embedded)
   {
      this.embedded = embedded;
   }

   public void setBasePath(String basePath)
   {
      this.basePath = basePath;
   }

}
