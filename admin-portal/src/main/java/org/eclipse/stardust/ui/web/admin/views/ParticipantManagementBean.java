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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ContextMenuItem;
import org.eclipse.stardust.ui.web.viewscommon.common.IContextMenuActionHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantTree;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantUserObject;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author anoop.nair
 * @version $Revision: $
 */
public class ParticipantManagementBean extends PopupUIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private boolean initialized;
   
   private WorkflowFacade workflowFacade;

   private ParticipantTree participantTree;

   private List<ContextMenuItem> roleNodeContextMenu;
   private List<ContextMenuItem> userNodeContextMenu;
   private List<ContextMenuItem> userGrpNodeContextMenu;

   /**
    * 
    */
   public ParticipantManagementBean()
   {
      super(ResourcePaths.V_participantMgmt);
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext()
            .lookup(AdminportalConstants.WORKFLOW_FACADE);
      initializeContextMenus();
      participantTree = new ParticipantTree();
      participantTree.initialize();
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
	  refreshUserManagementTable();
	  UserManagementBean.getCurrent().setParametricCallbackHandler(new HighlightUsersCallbackHandler());
	  initialized = true;
   }

   public void refreshParticipantTree()
   {
      participantTree.refresh();
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

   public void initializeContextMenus()
   {
      // Role node context menu
      roleNodeContextMenu = new ArrayList<ContextMenuItem>();
      ContextMenuItem createUser = new ContextMenuItem();
      createUser.setValue(getMessages().getString("participantTree.contextMenu.createUser"));
      createUser.setIcon("/plugins/admin-portal/images/icons/user_add.png");
      createUser.setMenuActionhandler(new RoleNodeContextMenuActionHandler());
      roleNodeContextMenu.add(createUser);

      // User node context menu
      userNodeContextMenu = new ArrayList<ContextMenuItem>();
      ContextMenuItem removeUserFromParticipant = new ContextMenuItem();
      removeUserFromParticipant.setValue(getMessages().getString("participantTree.contextMenu.removeUserGrant"));
      removeUserFromParticipant.setIcon("/plugins/views-common/images/icons/user_delete.png");
      removeUserFromParticipant.setMenuActionhandler(new UserNodeContextMenuActionHandler());
      userNodeContextMenu.add(removeUserFromParticipant);

      ContextMenuItem modifyUser = new ContextMenuItem();
      modifyUser.setValue(getMessages().getString("participantTree.contextMenu.modifyUser"));
      modifyUser.setMenuActionhandler(new UserNodeContextMenuActionHandler());
      userNodeContextMenu.add(modifyUser);

      ContextMenuItem deleteUser = new ContextMenuItem();
      deleteUser.setValue(getMessages().getString("participantTree.contextMenu.invalidateUser"));
      deleteUser.setMenuActionhandler(new UserNodeContextMenuActionHandler());
      userNodeContextMenu.add(deleteUser);

      // User group node context menu
      userGrpNodeContextMenu = new ArrayList<ContextMenuItem>();
      ContextMenuItem createUser2 = new ContextMenuItem();
      createUser2.setValue(getMessages().getString("participantTree.contextMenu.createUser"));
      createUser2.setIcon("/plugins/views-common/images/icons/user_add.png");
      createUser2.setMenuActionhandler(new UserGrpNodeContextMenuActionHandler());
      userGrpNodeContextMenu.add(createUser2);

      ContextMenuItem modifyUserGroup = new ContextMenuItem();
      modifyUserGroup.setValue(getMessages().getString("participantTree.contextMenu.modifyUserGroup"));
      modifyUserGroup.setMenuActionhandler(new UserGrpNodeContextMenuActionHandler());
      userGrpNodeContextMenu.add(modifyUserGroup);

      ContextMenuItem deleteUserGroup = new ContextMenuItem();
      deleteUserGroup.setValue(getMessages().getString("participantTree.contextMenu.invalidateUserGroup"));
      deleteUserGroup.setMenuActionhandler(new UserGrpNodeContextMenuActionHandler());
      userGrpNodeContextMenu.add(deleteUserGroup);
   }

   /**
    * @return
    */
   public ParticipantTree getParticipantTree()
   {
      return participantTree;
   }

   /**
    * @return
    */
   public List<ContextMenuItem> getRoleNodeContextMenu()
   {
      return roleNodeContextMenu;
   }
   
   /**
    * @return
    */
   public List<ContextMenuItem> getUserGrpNodeContextMenu()
   {
      return userGrpNodeContextMenu;
   }

   /**
    * @return
    */
   public List<ContextMenuItem> getUserNodeContextMenu()
   {
      return userNodeContextMenu;
   }   
   /**
    * helps to pass the event from user management table to participant tree
    * @author Yogesh.Manware
    * 
    */
   private class HighlightUsersCallbackHandler implements IParametricCallbackHandler
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
         Set<User> selecteUsers = new HashSet<User>();
         User recentlySelectedUser = null;
         if (CollectionUtils.isNotEmpty(parameters))
         {
            if (null != parameters.get("selectedUser"))
            {
               UserDetailsTableEntry userTabEntry = (UserDetailsTableEntry) parameters.get("selectedUser");
               if (userTabEntry.isSelectedRow())
               {
                  recentlySelectedUser = ServiceFactoryUtils.getUserService().getUser(userTabEntry.getUser().getOID());
                  selecteUsers.add(recentlySelectedUser);
               }
            }
         }

         PaginatorDataTable<UserDetailsTableEntry, User> userDetailsTable = UserManagementBean.getCurrent()
               .getUserDetailsTable();
         if (null != userDetailsTable)
         {
            List<UserDetailsTableEntry> usersList = userDetailsTable.getCurrentList();
            for (UserDetailsTableEntry userDetailsTableEntry : usersList)
            {
               if (userDetailsTableEntry.isSelectedRow() && (null == recentlySelectedUser || recentlySelectedUser.getOID() != userDetailsTableEntry.getUser().getOID()))
               {
                  selecteUsers.add(userDetailsTableEntry.getUser());
               }
            }
         }
         participantTree.setSelectedUsers(selecteUsers);
         participantTree.highlightSelectedUsers();
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

   /**
    * @author Shrikant.Gangal
    * 
    */
   private class RoleNodeContextMenuActionHandler implements IContextMenuActionHandler
   {
      public void handle(ActionEvent event)
      {
         ParticipantUserObject userObj = (ParticipantUserObject) event.getComponent().getAttributes().get("userObject");
         userObj.createUser(event, new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               if (eventType.equals(EventType.APPLY))
               {
                  refreshUserManagementTable();
               }
            }
         });
      }
   }

   /**
    * @author Shrikant.Gangal
    * 
    */
   private class UserGrpNodeContextMenuActionHandler implements IContextMenuActionHandler
   {
      public void handle(ActionEvent event)
      {
         ParticipantUserObject userObj = (ParticipantUserObject) event.getComponent().getAttributes().get("userObject");
         String menuOption = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
               .get("menuOption");

         if (StringUtils.isNotEmpty(menuOption))
         {
            if ("createUser".equals(menuOption))
            {
               userObj.createUser(event, new ICallbackHandler()
               {
                  public void handleEvent(EventType eventType)
                  {
                     if (eventType.equals(EventType.APPLY))
                     {
                        refreshUserManagementTable();
                     }
                  }
               });
            }
         }
      }
   }

   /**
    * @author Shrikant.Gangal
    * 
    */
   private class UserNodeContextMenuActionHandler implements IContextMenuActionHandler
   {
      public void handle(ActionEvent event)
      {
         String menuOption = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
               .get("menuOption");
         if (StringUtils.isNotEmpty(menuOption))
         {
            if ("removeUserFromParticipant".equals(menuOption))
            {
               getParticipantTree().removeUserFromParticipant();
            }
         }
      }
   }
}