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
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class AuditTrailBean extends PopupUIComponentBean
{

   private WorkflowFacade workflowFacade;

   private AdminMessagesPropertiesBean propsBean;
   
   private AuditTrailConfirmationDialog mappedConfirmationDialog;

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
   public void recoverWE(ActionEvent event) throws PortalException
   {
      try
      {
         int errors = 0;
         QueryService queryService = workflowFacade.getQueryService();
         AdministrationService adminService = workflowFacade.getServiceFactory()
               .getAdministrationService();
         ProcessInstanceQuery query = ProcessInstanceQuery
               .findInState(new ProcessInstanceState[] {
                     ProcessInstanceState.Active, ProcessInstanceState.Interrupted});
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
                     + " " + details.getOID());
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
   public String cleanupATMD() throws PortalException
   {

      try
      {
         AdministrationService service = workflowFacade.getServiceFactory()
               .getAdministrationService();
         if (service != null)
         {
            service.cleanupRuntimeAndModels();
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
    * @param event
    */
   public void openCleanupATDConfirm(ActionEvent event)
   {
      mappedConfirmationDialog = new AuditTrailConfirmationDialog(DialogContentType.NONE,DialogActionType.YES_NO,ResourcePaths.LP_CleanAuditTrailDB);
      mappedConfirmationDialog.setTitle(propsBean.getString("launchPanels.ippAdmAdministrativeActions.catd.title"));
      mappedConfirmationDialog.openPopup();
      }
   
   /**
    * Cleans up Audit trail database
    * 
    * @param event
    */
   public boolean cleanupATD(boolean retainUsersAndDepts)
   {
      AdministrationService service = null;
      try
      {
         service = workflowFacade.getServiceFactory().getAdministrationService();
         if (service != null)
         {
            service.cleanupRuntime(retainUsersAndDepts);
            FacesMessage message = new FacesMessage(
                  propsBean
                        .getString("launchPanels.ippAdmAdministrativeActions.auditTrail.cleanUpAuditTrailDbCompleted"));
            message.setSeverity(FacesMessage.SEVERITY_INFO);
            MessageDialog.addInfoMessage(message.getDetail());

            SessionContext.findSessionContext().resetSession();
         }
         return true;
      }
      catch (PublicException e)
      {
         ExceptionHandler.handleException(e);
      }
      return false;
   }

   @Override
   public void initialize()
   {
   // TODO Auto-generated method stub

   }

   public ConfirmationDialog getMappedConfirmationDialog()
   {
      return mappedConfirmationDialog;
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
         return cleanupATD(retainUsersAndDepts);
      }

      public boolean cancel()
      {
         mappedConfirmationDialog = null;
         return true;
      }

   }
   
}
