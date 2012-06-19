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
package org.eclipse.stardust.ui.web.viewscommon.views.casemanagement;

import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;


/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class CaseManagerBean extends UIComponentBean implements ViewEventHandler
{

   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "caseManagerBean";
   private ProcessTableHelper processTableHelper;

   /**
    * 
    */
   @Override
   public void initialize()
   {
      processTableHelper = new ProcessTableHelper();
   }

   /**
    * method to get CaseManagerBean instance
    * 
    * @return CaseManagerBean object
    */
   public static CaseManagerBean getInstance()
   {
      return (CaseManagerBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * 
    */
   public void handleEvent(ViewEvent event)
   {

   }

   /**
    * 
    * @return
    */
   public ProcessTableHelper getProcessTableHelper()
   {
      return processTableHelper;
   }

}
