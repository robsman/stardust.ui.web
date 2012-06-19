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
/**
 * 
 */
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.springframework.beans.factory.InitializingBean;



/**
 * @author roland.stamm
 * 
 */
public class MyProcessesPanelBean extends AbstractLaunchPanel implements InitializingBean
{
   private static final long serialVersionUID = -1188695396112164126L;
   
   private List<MyProcessesModel> items = CollectionUtils.newArrayList();
   private boolean initialized;
   
   public MyProcessesPanelBean()
   {
      super("myProcesses");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      buildList();
   }

   /**
    * 
    */
   public void update()
   {
      initialized = true;
      ProcessWorklistCacheManager.getInstance().reset();
      buildList();
   }
   
   /**
    * 
    */
   private void buildList()
   {
      if (isExpanded())
      {
         items.clear();
   
         Set<ProcessDefinition> processDefs = ProcessWorklistCacheManager.getInstance().getProcesses();
         for (ProcessDefinition processDefinition : processDefs)
         {
            items.add(new MyProcessesModel(processDefinition));         
         }
      }
   }

   @Override
   public void setExpanded(boolean expanded)
   {
      super.setExpanded(expanded);

      if (!initialized && isExpanded())
      {
         update();
      }
   }

   public List<MyProcessesModel> getItems()
   {
      return items;
   }
}
