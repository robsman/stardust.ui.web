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

import java.io.Serializable;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;


/**
 * This portal supports the contexts jsf, cad and jsp.
 * 
 * @author mgille
 * 
 */
public class JSFProcessExecutionPortal extends AbstractProcessExecutionPortal implements Resetable, Serializable
{
   private final static long serialVersionUID = 1l;
   
   private final static String EMPTY_JSF_PAGE = "/ipp/process/empty-panel.xhtml";

   private final static String GENERIC_JSF_PAGE = "/ipp/process/manual-activity-panel.xhtml";
   
  // private SortManager sortManager;

   /**
    * 
    * 
    */
   public JSFProcessExecutionPortal()
   {
      super();
   }

   protected void onCurrentActivityInstanceChanged()
   {
   /*
    * // TODO Auto-generated method stub PortalSidebar portalSidebar = (PortalSidebar)
    * SessionContext.findBindContextValue(WorkArea.BEAN_ID);
    * 
    * if(null != portalSidebar) { portalSidebar.setWorkareaDirty(true); }
    */
      
//      Object portalHeader = SessionContext.findBindContextValue(PortalHeader.BEAN_ID);
//      if (portalHeader instanceof PortalHeader)
//      {
//         ((PortalHeader) portalHeader).refreshWorklistOutline();
//
//         if (null == getCurrentActivityInstance())
//         {
//            ((PortalHeader) portalHeader).refreshTasksTable();
//         }
//      }
   }

   /**
    * 
    * @return the URL the JSP for JSP, CAD Servlet or plain JSP can be invoked with.
    */
   public String getUrl()
   {
      String url = null;
      
      ActivityInstance ai = getCurrentActivityInstance();
      if (null != ai)
      {
         // give the interaction handler a chance to provide a customized panel URI 
         IActivityInteractionController interactionController = SpiUtils.getInteractionController(ai.getActivity());
         if (null != interactionController)
         {
            String customizedUri = interactionController.providePanelUri(ai);
            
            if ( !StringUtils.isEmpty(customizedUri))
            {
               if (GENERIC_PANEL.equals(customizedUri))
               {
                  // translate from generic string to portal specific variant
                  url = GENERIC_JSF_PAGE;
               }
               else if (EMPTY_PANEL.equals(customizedUri))
               {
                  // translate from generic string to portal specific variant
                  url = EMPTY_JSF_PAGE;
               }
               else
               {
                  url = customizedUri;
               }
            }
         }
         else
         {
            logInfo("Did not find an interaction controller for the current activity instance.");
         }
      }
      
      return !StringUtils.isEmpty(url) ? url : EMPTY_JSF_PAGE;
   }

 /*  public SortManager getSortManager()
   {
      if (null == sortManager)
      {
         sortManager = new SortManager(getActivityDataModel(),
                  ProcessportalConstants.ACTIVITY_INSTANCE_MODEL);
      }
      return sortManager;
   }*/

   protected void onChangeActivityList()
   {
      // propagate sorting to new model
      
     /* Object portalHeader = SessionContext.findBindContextValue(PortalHeader.BEAN_ID);
      if (portalHeader instanceof PortalHeader)
      {
         List sortCriteria = sortManager != null ? sortManager.getSortCriteria() : null;
         sortManager = null; // force creation of a new SortManager
         sortManager = getSortManager();
         if ((null != sortCriteria) && !sortCriteria.isEmpty())
         {
            for (int i = 0; i < sortCriteria.size(); ++i)
            {
               SortCriterion criterion = (SortCriterion) sortCriteria.get(i);
               sortManager.setSorted(criterion.getProperty(), criterion.isAscending());
            }
         }
         else
         {
            sortManager.resetSorting();
         }
         
         if(!ProcessPortalSession.getData().getProcessPortalSettings().isGlobalWorklistFilter())
         {
            IFilterProvider[] filterProvider = getFilterProviders();
            for(int i = 0; i < filterProvider.length; ++i)
            {
               if(filterProvider[i] instanceof IWorklistFilterProvider)
               {
                  filterProvider[i].setFilterModel(null);
               }
            }
         }

         // trigger UI refresh
         ((PortalHeader) portalHeader).refreshWorklistOutline();
         ((PortalHeader) portalHeader).refreshTasksTable();
      }*/
   }

   public boolean isValueBindingNullable()
   {
      return false;
   }
}
