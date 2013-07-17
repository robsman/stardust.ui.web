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

import java.util.Iterator;

import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class AuditTrailBean extends PopupUIComponentBean
{

   private WorkflowFacade workflowFacade;

   private AdminMessagesPropertiesBean propsBean;
   
   private AuditTrailConfirmationDialog mappedConfirmationDialog;
   
   private ConfirmationDialog auditTrailAndModelCleanUpDialog;
   
   private ConfirmationDialog recoveryDialog;

   private ConfirmationDialog errorDialog;

   /**
    * 
    */
   public AuditTrailBean()
   {
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
            AdminportalConstants.WORKFLOW_FACADE);
      propsBean = AdminMessagesPropertiesBean.getInstance();
   }

   /**
    * Recovers workflow engine
    * 
    * @param event
    * @throws PortalException
    */
   public void recoverWE() throws PortalException
   {
      try
      {
         int errors = 0;
         QueryService queryService = workflowFacade.getQueryService();
         AdministrationService adminService = workflowFacade.getServiceFactory()
               .getAdministrationService();
         ProcessInstanceQuery query = ProcessInstanceQuery
               .findInState(new ProcessInstanceState[] {
                     ProcessInstanceState.Active, ProcessInstanceState.Interrupted, ProcessInstanceState.Aborting});
         ProcessInstances pi = queryService.getAllProcessInstances(query);
         Iterator<ProcessInstance> itr = pi != null ? pi.iterator() : null;
         while (itr != null && itr.hasNext())
         {
            ProcessInstance details = (ProcessInstance) itr.next();
            try
            {
               adminService.recoverProcessInstance(details.getOID());
            }
            catch (AccessForbiddenException e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE, e);
               break;
            }
            catch (Exception e)
            {
               MessageDialog.addWarningMessage(propsBean
                     .getString("launchPanels.ippAdmAdministrativeActions.auditTrail.processInstanceRecoveringFailed")
                     + " " + details.getOID(), e);
               errors++;
            }
         }
         MessageDialog.addInfoMessage(propsBean
               .getString("launchPanels.ippAdmAdministrativeActions.auditTrail.recoveringCompleted"));

         SessionContext.findSessionContext().resetSession();
      }
      catch (Exception ex)
      {
         FacesMessage message = new FacesMessage(
               propsBean
                     .getString("launchPanels.ippAdmAdministrativeActions.auditTrail.runtimeRecoveryFailed"));
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
      }
   }

   /**
    * Cleans up Audit trail and Model
    * 
    * @param event
    * @throws PortalException
    */
   public String cleanupATMD()
   {

      try
      {
         AdministrationService service = workflowFacade.getServiceFactory()
               .getAdministrationService();
         if (service != null)
         {
            service.cleanupRuntimeAndModels();
            ModelCache.findModelCache().reset();
            SessionContext.findSessionContext().resetSession();
            return "ippPortalLogout";
         }
      }
      // catch (AccessForbiddenException e)
      catch (PublicException e)
      {
         ExceptionHandler.handleException(e);
      }
      return null;
   }

   /**
    * opens confirmation dialog for cleanup
    * 
    * @param event
    */
   public void openCleanupATDConfirm(ActionEvent event)
   {
      if (areAllViewsClosed())
      {
         mappedConfirmationDialog = new AuditTrailConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO,
               ResourcePaths.LP_CleanAuditTrailDB);
         mappedConfirmationDialog.setTitle(propsBean.getString("launchPanels.ippAdmAdministrativeActions.catd.title"));
         mappedConfirmationDialog.setFromlaunchPanels(true);
         mappedConfirmationDialog.openPopup();
      }
      else
      {
         initViewsOpenConfirmationDialog();
         errorDialog.setFromlaunchPanels(true);
         errorDialog.openPopup();
      }
   }

   /**
    * @param event
    */
   public void openCleanupATMDConfirm(ActionEvent event)
   {
      if (areAllViewsClosed())
      {
         ConfirmationDialogHandler dialogHandler = new ConfirmationDialogHandler()
         {
            public boolean cancel()
            {
               auditTrailAndModelCleanUpDialog = null;
               return true;
            }

            public boolean accept()
            {
               auditTrailAndModelCleanUpDialog = null;
               String navigationRuleId = cleanupATMD();
               if (StringUtils.isNotEmpty(navigationRuleId))
               {
                  FacesUtils.handleNavigation(navigationRuleId);
               }
               return true;
            }
         };
         auditTrailAndModelCleanUpDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO,
               dialogHandler);
         auditTrailAndModelCleanUpDialog.setIncludePath(ResourcePaths.LP_CleanAuditAndModelTrailDB);
         auditTrailAndModelCleanUpDialog.setDialogStyle(DialogStyle.COMPACT);
         auditTrailAndModelCleanUpDialog.setTitle(propsBean
               .getString("launchPanels.ippAdmAdministrativeActions.catmd.title"));
         auditTrailAndModelCleanUpDialog.setFromlaunchPanels(true);
         auditTrailAndModelCleanUpDialog.openPopup();
      }
      else
      {
         initViewsOpenConfirmationDialog();
         errorDialog.setFromlaunchPanels(true);
         errorDialog.openPopup();
      }
   }
   
   /**
    * @param event
    */
   public void openRecoveryDialog(ActionEvent event)
   {
      ConfirmationDialogHandler dialogHandler = new ConfirmationDialogHandler()
      {
         public boolean cancel()
         {
            recoveryDialog = null;
            return true;
         }

         public boolean accept()
         {
            try
            {
               recoveryDialog = null;
               recoverWE();
               return true;
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
         }
      };
      recoveryDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO,
            dialogHandler);
      recoveryDialog.setIncludePath(ResourcePaths.LP_Recovery);
      recoveryDialog.setDialogStyle(DialogStyle.COMPACT);
      recoveryDialog.setTitle(propsBean
            .getString("launchPanels.ippAdmAdministrativeActions.recovery.title"));
      recoveryDialog.setFromlaunchPanels(true);
      recoveryDialog.openPopup();
   }
   
   /**
    * Cleans up Audit trail database
    * 
    * @param event
    */
   public String cleanupATD(boolean retainUsersAndDepts)
   {
      AdministrationService service = null;
      try
      {
         service = workflowFacade.getServiceFactory().getAdministrationService();
         if (service != null)
         {
            service.cleanupRuntime(retainUsersAndDepts);
            SessionContext.findSessionContext().resetSession();
            return "ippPortalLogout";
         }
         return null;
      }
      catch (PublicException e)
      {
         ExceptionHandler.handleException(e);
      }
      return null;
   }

   @Override
   public void initialize()
   {
   // TODO Auto-generated method stub

   }

   /**
    * 
    */
   private boolean areAllViewsClosed()
   {
      return CollectionUtils.isEmpty(PortalApplication.getInstance().getOpenViews());
   }

   /**
    * 
    */
   private void initViewsOpenConfirmationDialog()
   {
      if (null == errorDialog)
      {
         errorDialog = new ConfirmationDialog(DialogContentType.ERROR, DialogActionType.OK_CANCEL, DialogType.ACCEPT_ONLY, null);
         errorDialog.setMessage(propsBean.getString("launchPanels.ippAdmAdministrativeActions.auditTrail.viewsOpen.errorMessage"));
      }
   }

   public ConfirmationDialog getMappedConfirmationDialog()
   {
      return mappedConfirmationDialog;
   }
   
   public ConfirmationDialog getAuditTrailAndModelCleanUpDialog()
   {
      return auditTrailAndModelCleanUpDialog;
   }
   
   public ConfirmationDialog getRecoveryDialog()
   {
      return recoveryDialog;
   }

   public ConfirmationDialog getErrorDialog()
   {
      return errorDialog;
   }

   /**
    * 
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   public class AuditTrailConfirmationDialog extends ConfirmationDialog implements ConfirmationDialogHandler
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;
      private boolean retainUsersAndDepts = true;

      public AuditTrailConfirmationDialog(DialogContentType contentType, DialogActionType actionType, String includePath)
      {
         super(contentType, actionType, null);
         setIncludePath(includePath);
         setHandler(this);
      }

      // ************** Default getter and setter methods****************
      public boolean isRetainUsersAndDepts()
      {
         return retainUsersAndDepts;
      }

      public void setRetainUsersAndDepts(boolean retainUsersAndDepts)
      {
         this.retainUsersAndDepts = retainUsersAndDepts;
      }

      public boolean accept()
      {
         mappedConfirmationDialog = null;
         String navigationRuleId = cleanupATD(retainUsersAndDepts);
         if (StringUtils.isNotEmpty(navigationRuleId))
         {
            FacesUtils.handleNavigation(navigationRuleId);
         }
         return true;
      }

      public boolean cancel()
      {
         mappedConfirmationDialog = null;
         return true;
      }

   }
}
