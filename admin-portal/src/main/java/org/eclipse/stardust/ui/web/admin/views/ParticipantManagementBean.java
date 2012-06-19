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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;



/**
 * @author anoop.nair
 * @version $Revision: $
 */
public class ParticipantManagementBean extends PopupUIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private boolean initialized;
   
   private WorkflowFacade workflowFacade;

   /**
    * 
    */
   public ParticipantManagementBean()
   {
      super(ResourcePaths.V_participantMgmt);
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
    		  AdminportalConstants.WORKFLOW_FACADE);
   }

   /**
    * @return
    */
   public static ParticipantManagementBean getInstance()
   {
      return (ParticipantManagementBean) FacesUtils.getBeanFromContext("participantMgmtBean");
   }
   
   @Override
   public void initialize()
   {
	  ParticipantTree.getInstance().initialize();
	  refreshUserManagementTable();
	  UserManagementBean.getCurrent().setParametricCallbackHandler(new HighlightUsersCallbackHandler());
	  initialized = true;
   }

   public void refreshParticipantTree()
   {
      ParticipantTree.getInstance().refresh();
   }
   
   /**
    * Refresh only user management table
    */
   public void refreshUserManagementTable()
   {
      UserManagementBean.getCurrent().initialize();
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         // if model is not already initialized
         if(!initialized)
         {
            initialize();
         }
      }
   }

   /**
    * Updates the changes
    */
   public void update()
   {
      workflowFacade.reset();
      refreshUserManagementTable();
      refreshParticipantTree();
   }

   /**
    * helps to pass the event from user management table to participant tree
    * @author Yogesh.Manware
    * 
    */
   private static class HighlightUsersCallbackHandler implements IParametricCallbackHandler
   {
      private Map<String, Object> parameters;

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler#handleEvent(
       * org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType)
       */
      public void handleEvent(EventType eventType)
      {
         if (CollectionUtils.isNotEmpty(parameters))
         {
            ParticipantTree.getInstance().highlightSelectedUser(parameters.get("selectedUser"));
         }
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler#getParameters
       * (java.util.Map)
       */
      public Map<String, Object> getParameters()
      {
         return this.parameters;
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler#setParameters
       * (java.util.Map)
       */
      public void setParameters(Map<String, Object> parameters)
      {
         this.parameters = parameters;
      }
   }
}