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
import java.util.EnumSet;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ContextMenuItem;
import org.eclipse.stardust.ui.web.viewscommon.common.GlobalPageMessage;
import org.eclipse.stardust.ui.web.viewscommon.common.IContextMenuActionHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantUserObject;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantUserObject.NODE_TYPE;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;



/**
 * @author anoop.nair
 * @version $Revision: $
 */
public class CreateOrModifyDepartmentBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 8792439670664153104L;

   private boolean createMode;

   private boolean modifyMode;

   private DepartmentBean departmentBean;

   private Department selectedDepartment;

   private WorkflowFacade workflowFacade;

   private List<ContextMenuItem> scopedOrgNodeContextMenu;

   private List<ContextMenuItem> deptNodeContextMenu;

   // TODO: To be reviewed!
   private DefaultMutableTreeNode parentNodeToRefresh;

   /**
    * 
    */
   public CreateOrModifyDepartmentBean()
   {
      super(ResourcePaths.V_participantMgmt);
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext()
            .lookup(AdminportalConstants.WORKFLOW_FACADE);
      initializeContextMenus();
   }

   /**
    * 
    */
   public void initializeContextMenus()
   {
      // Scoped organization node menu
      scopedOrgNodeContextMenu = new ArrayList<ContextMenuItem>();
      ContextMenuItem createDept = new ContextMenuItem();
      createDept.setValue(getMessages().getString("participantTree.contextMenu.createDepartment"));
      createDept.setMenuActionhandler(new ScopedOrgNodeContextMenuActionHandler());
      scopedOrgNodeContextMenu.add(createDept);

      // Department node menu
      deptNodeContextMenu = new ArrayList<ContextMenuItem>();
      ContextMenuItem modifyDepartment = new ContextMenuItem();
      modifyDepartment.setValue(getMessages().getString("participantTree.contextMenu.modifyDepartment"));
      modifyDepartment.setMenuActionhandler(new DeptNodeContextMenuActionHandler());
      deptNodeContextMenu.add(modifyDepartment);

      ContextMenuItem deleteDepartment = new ContextMenuItem();
      deleteDepartment.setValue(getMessages().getString("participantTree.contextMenu.deleteDepartment"));
      deleteDepartment.setMenuActionhandler(new DeptNodeContextMenuActionHandler());
      deptNodeContextMenu.add(deleteDepartment);

      ContextMenuItem createUser = new ContextMenuItem();
      createUser.setValue(getMessages().getString("participantTree.contextMenu.createUser"));
      createUser.setIcon("/plugins/views-common/images/icons/user_add.png");
      createUser.setMenuActionhandler(new DeptNodeContextMenuActionHandler());
      deptNodeContextMenu.add(createUser);
   }
   
   /**
    * create or modifies user group as per edit mode and selected user group
    */
   public void apply()
   {
      try
      {
         UserService service = workflowFacade.getServiceFactory().getUserService();
         AdministrationService adminService = workflowFacade.getServiceFactory().getAdministrationService();
         if (!modifyMode)
         {
            if (service != null && departmentBean != null)
            {
               adminService.createDepartment(departmentBean.getId(), departmentBean.getName(), departmentBean
                     .getDescription(), departmentBean.getParentDepartment(), departmentBean.getOrganization());
            }
         }
         else
         {
            if (selectedDepartment != null && departmentBean != null)
            {
               adminService.modifyDepartment(departmentBean.getOID(), departmentBean.getName(), departmentBean
                     .getDescription());
            }
         }
      }
      catch (AccessForbiddenException e)
      {
         GlobalPageMessage.storeMessage(FacesContext.getCurrentInstance(), new javax.faces.application.FacesMessage(
               javax.faces.application.FacesMessage.SEVERITY_WARN, Localizer.getString(LocalizerKey.ACCESS_FORBIDDEN),
               null), 1);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      closePopup();
      ParticipantManagementBean.getInstance().getParticipantTree().refreshParticipantNode(parentNodeToRefresh,
            EnumSet.of(NODE_TYPE.DEPARTMENT, NODE_TYPE.DEPARTMENT_DEFAULT));
   }

   /**
    * opens user group dialog and gets selected user group data
    * 
    * @param ae
    */
   public void openDepartmentDialog(ActionEvent ae)
   {
      UIComponent source = ae.getComponent();

      ParticipantUserObject participantUserObject = (ParticipantUserObject) source.getAttributes().get("userObject");
      if (participantUserObject.isReferencesDepartment())
      {
         parentNodeToRefresh = (DefaultMutableTreeNode) participantUserObject.getWrapper().getParent();
         selectedDepartment = participantUserObject.getDepartment();
         if (selectedDepartment != null)
         {
            createMode = false;
            modifyMode = true;

            this.departmentBean = new DepartmentBean(selectedDepartment);
         }
      }
      else
      {
         modifyMode = false;
         createMode = false;

         Department parentDepartment = null;
         OrganizationInfo assignedOrganization = null;

         if (participantUserObject.isReferencesScopedOrganization())
         {
            parentNodeToRefresh = (DefaultMutableTreeNode) participantUserObject.getWrapper();
            assignedOrganization = participantUserObject.getScopedOrganization();

            TreeNode node = participantUserObject.getWrapper().getParent();
            if (node instanceof DefaultMutableTreeNode)
            {
               createMode = true;

               DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node;
               if (parentNode.getUserObject() instanceof ParticipantUserObject)
               {
                  ParticipantUserObject parentParticipantUserObject = (ParticipantUserObject) parentNode
                        .getUserObject();
                  if (parentParticipantUserObject.isReferencesDepartment())
                  {
                     parentDepartment = parentParticipantUserObject.getDepartment();
                  }
                  else if (parentParticipantUserObject.isReferencesImplicitlyScopedOrganization())
                  {
                     DepartmentInfo parentDepartmentInfo = parentParticipantUserObject.getQualifiedModelParticipantInfo()
                           .getDepartment();
                     AdministrationService adminService = workflowFacade.getServiceFactory().getAdministrationService();
                     parentDepartment = adminService.getDepartment(parentDepartmentInfo.getOID());
                  }
               }
            }
         }
         this.departmentBean = new DepartmentBean(parentDepartment, assignedOrganization);
      }

      if (createMode || modifyMode)
      {
         super.openPopup();
      }
      FacesUtils.refreshPage();
   }

   // ********************* Default Getter and Setter Methods *******************

   public String getId()
   {
      return departmentBean.getId();
   }

   public void setId(String id)
   {
      departmentBean.setId(id);
   }

   public String getName()
   {
      return departmentBean.getName();
   }

   public void setName(String name)
   {
      departmentBean.setName(name);
   }

   public String getDescription()
   {
      return departmentBean.getDescription();
   }

   public void setDescription(String description)
   {
      departmentBean.setDescription(description);
   }

   public OrganizationInfo getOrganization()
   {
      return (null != departmentBean) ? departmentBean.getOrganization() : null;
   }

   public Department getParentDepartment()
   {
      return (null != departmentBean) ? departmentBean.getParentDepartment() : null;
   }

   public boolean isModifyMode()
   {
      return modifyMode;
   }

   public boolean isCreateMode()
   {
      return createMode;
   }

   public List<ContextMenuItem> getScopedOrgNodeContextMenu()
   {
      return scopedOrgNodeContextMenu;
   }

   public List<ContextMenuItem> getDeptNodeContextMenu()
   {
      return deptNodeContextMenu;
   }

   @Override
   public void initialize()
   {
   // TODO Auto-generated method stub
   }
   public String getOrganizationName()
   {
     return I18nUtils.getParticipantName(ParticipantUtils.getParticipant(getOrganization()));      
   }

   /**
    * @author Shrikant.Gangal
    * 
    */
   public class ScopedOrgNodeContextMenuActionHandler implements IContextMenuActionHandler
   {
      public void handle(ActionEvent event)
      {
         openDepartmentDialog(event);
      }
   }

   /**
    * @author Shrikant.Gangal
    * 
    */
   public class DeptNodeContextMenuActionHandler implements IContextMenuActionHandler
   {
      public void handle(ActionEvent event)
      {
         String menuOption = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
               .get("menuOption");
         if (StringUtils.isNotEmpty(menuOption))
         {
            if ("modifyDepartment".equals(menuOption))
            {
               openDepartmentDialog(event);
            }
            else if ("deleteDepartment".equals(menuOption))
            {
               ParticipantManagementBean.getInstance().getParticipantTree().deleteDepartment(event);
            }
            else if ("createUser".equals(menuOption))
            {
               ParticipantUserObject userObj = (ParticipantUserObject) event.getComponent().getAttributes()
                     .get("userObject");
               userObj.createUser(event, new ICallbackHandler()
               {
                  public void handleEvent(EventType eventType)
                  {
                     if (eventType.equals(EventType.APPLY))
                     {
                        ParticipantManagementBean.getInstance().refreshUserManagementTable();
                     }
                  }
               });
            }
         }
      }
   }
}
