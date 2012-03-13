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

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler.MessageDisplayMode;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.search.RelatedProcessSearchBean;
import org.eclipse.stardust.ui.web.viewscommon.views.search.RelatedProcessTableEntry;



/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class AttachToCaseDialogBean extends RelatedProcessSearchBean implements ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "attachToCaseDialogBean";
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   private String instanceOid;
   private Scope scope;
   private long[] members;
   private ProcessInstance caseInstance;
   private ConfirmationDialog attachCaseConfirmationDialog;

   /**
    * 
    * @return RelatedProcessSearchBean instance
    */
   public static AttachToCaseDialogBean getInstance()
   {
      return (AttachToCaseDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * 
    */
   @Override
   public void openPopup()
   {
      resolveScope();
      if (Scope.Process.equals(scope))
      {
         List<ProcessInstance> pis = getSourceProcessInstances();
         List<Long> oids = CollectionUtils.newArrayList();
         for (ProcessInstance pi : pis)
         {
            oids.add(pi.getOID());
         }
         pis = ProcessInstanceUtils.getProcessInstances(oids);
         if (ProcessInstanceUtils.isRootProcessInstances(pis))
         {
            setSourceProcessInstances(pis);
         }
         else
         {
            MessageDialog.addErrorMessage(COMMON_MESSAGE_BEAN
                  .getString("views.attachToCase.nonRootProcessSelectedToCreateCase"));
            return;
         }

      }
      
      getRelatedProcessSearchHelper().setSearchCases(Scope.Case.equals(scope) ? false : true);

      if ((Scope.Case.equals(scope) && hasManageCasePermission()) || Scope.Process.equals(scope))
      {
         super.openPopup();
      }
      else
      {
         ExceptionHandler.handleException(null,
               COMMON_MESSAGE_BEAN.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }

   }

   /**
    * 
    * @return
    */
   private boolean hasManageCasePermission()
   {      
      for (ProcessInstance pi : getSourceProcessInstances())
      {
         if(!AuthorizationUtils.hasManageCasePermission(pi))
         {
           return false; 
         }
      }
      return true;
   }
   

   /**
    * reset instance values
    */
   public void reset()
   {
      instanceOid = null;
      scope = null;
      members = null;
      super.reset();
   }

   /**
 * 
 */
   private void resolveScope()
   {
      if (getSourceProcessInstances().size() == 1 && getSourceProcessInstances().get(0).isCaseProcessInstance())
      {
         scope = Scope.Case;
      }
      else
      {
         scope = Scope.Process;
      }
   }

   /**
    * 
    */
   public void openCase()
   {

      ProcessInstanceUtils.openProcessContextExplorer(caseInstance);
      closeCasePopup();
   }

   /**
    * 
    * @return
    */
   private boolean validateProcess()
   {
      
         if(StringUtils.isEmpty(instanceOid))
         {
            if (Scope.Case.equals(scope))
            {
               FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.processRequired.message"));
            }
            else
            {
               FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.caseRequired.message"));
            }
            return false;
         }
         ProcessInstance instance = null;
         try
         {
            Long instanceOID = Long.valueOf(instanceOid);
            instance = (null != instanceOid) ? ProcessInstanceUtils.getProcessInstance(instanceOID) : null;
         }
         catch (Exception e)
         {
            //do nothing
         }
        
         if (null == instance)
         {
            if (Scope.Case.equals(scope))
            {
               FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.inputIsCase.message"));
            }
            else
            {
               FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.inputIsProcess.message"));
            }
            return false;
         }
         boolean isCase = instance.isCaseProcessInstance();

         if (Scope.Process.equals(scope) && !isCase)
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.inputIsProcess.message"));
            return false;
         }
         else if (Scope.Process.equals(scope) && isCase && ! AuthorizationUtils.hasManageCasePermission(instance))
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase"));
            return false;
         }
         else if(Scope.Process.equals(scope) && !ProcessInstanceUtils.isRootProcessInstance(instance))
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.nonRootProcessSelectedToCreateCase"));
            return false;
         }
         else if (Scope.Case.equals(scope) && isCase)
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.inputIsCase.message"));
            return false;
         }
         else if (Scope.Case.equals(scope) && !ProcessInstanceUtils.isActiveProcessInstance(instance))
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.specifyActiveProcess"));
            return false;
         }
         else if (Scope.Process.equals(scope)
               && (ProcessInstanceState.ABORTED == instance.getState().getValue() || ProcessInstanceState.COMPLETED == instance
                     .getState().getValue()))
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.specifyActiveCase"));
            return false;
         }
         
      return true;

   }

   /**
    * 
    */
   public void attachToCase()
   {

      try
      {
         Long caseOID = null;
         
         if (Page.ADVANCE.equals(getCurrentPage()))
         {
            if (!validateProcess())
            {
               return;
            }
            Long instanceOID = Long.valueOf(instanceOid);
            if (Scope.Process.equals(scope))
            {
               caseOID = instanceOID;
               members = getOidArray(getSourceProcessInstances());
            }
            else
            {
               caseOID = getSourceProcessInstances().get(0).getOID();
               members = new long[] {instanceOID};
            }
            setTargetProcessInstance(ProcessInstanceUtils.getProcessInstance(instanceOID));
         }
         else
         {
            List<RelatedProcessTableEntry> selectedRows = getRelatedProcessSearchHelper().getSelectedProcessInstances();
            if (CollectionUtils.isNotEmpty(selectedRows))
            {             
               if (Scope.Process.equals(scope))
               {
                  caseOID = selectedRows.get(0).getOid();
                  members = getOidArray(getSourceProcessInstances());
               }
               else
               {
                  caseOID = getSourceProcessInstances().get(0).getOID();
                  members = new long[selectedRows.size()];
                  for (int i = 0; i < members.length; i++)
                  {
                     members[i] = selectedRows.get(i).getOid();
                  }
                  
               } 
               
            }
         }
         if (null != members)
         {
            caseInstance = ProcessInstanceUtils.getProcessInstance(caseOID);
            if (AuthorizationUtils.hasManageCasePermission(caseInstance))
            {
               ServiceFactoryUtils.getWorkflowService().joinCase(caseOID.longValue(), members);
               CommonDescriptorUtils.reCalculateCaseDescriptors(caseInstance);
               setCurrentPage(Page.NOTIFICATION);
               openConfirmationDialog();
            }
            else
            {
               ExceptionHandler.handleException(null,
                     COMMON_MESSAGE_BEAN.getString("views.attachToCase.caseAttach.notAuthorizedToManageCase"),
                     MessageDisplayMode.ONLY_CUSTOM_MSG);
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException("",e);
      }
   }
   
   /**
    * Confirmation dialog showing Process attached to Case Info
    */
   public void openConfirmationDialog()
   {
      attachCaseConfirmationDialog = new ConfirmationDialog(DialogContentType.INFO, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      attachCaseConfirmationDialog.setIncludePath(ResourcePaths.V_ATTACH_CASE_CONF_DLG);
      super.closePopup();
      attachCaseConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      attachCaseConfirmationDialog = null;
      openCase();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      attachCaseConfirmationDialog = null;
      closeCasePopup();
      return true;
   }

   /**
    * 
    * @return
    */
   private long[] getOidArray(List<ProcessInstance> pis)
   {
      int size = pis.size();
      long[] ids = new long[size];

      for (int i = 0; i < size; i++)
      {
         ids[i] = pis.get(i).getOID();
      }
      return ids;
   }   

   public String getInstanceOid()
   {
      return instanceOid;
   }

   public void setInstanceOid(String instanceOid)
   {
      this.instanceOid = instanceOid;
   }

   public Scope getScope()
   {
      return scope;
   }

   public void setScope(Scope scope)
   {
      this.scope = scope;
   }
   
   public ConfirmationDialog getAttachCaseConfirmationDialog()
   {
      return attachCaseConfirmationDialog;
   }



   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   public enum Scope {
      Case, Process;
   }

   /**
    * 
    * @return
    */
   public String getTitle()
   {
      if (Scope.Process.equals(scope))
      {
         if (getSourceProcessInstances().size() == 1)
         {
            return COMMON_MESSAGE_BEAN.getParamString("views.attachToCase.scope_process.title",
                  ProcessInstanceUtils.getProcessLabel(getSourceProcessInstances().get(0)));
         }
         else
         {
            return COMMON_MESSAGE_BEAN.getString("views.attachToCase.title");
         }
      }
      else
      {
         return COMMON_MESSAGE_BEAN.getParamString("views.attachToCase.scope_case.title",
               ProcessInstanceUtils.getProcessLabel(getSourceProcessInstances().get(0)));
      }

   }

   /**
    * 
    * @return
    */
   public String getMessage()
   {
      if (Page.NOTIFICATION.equals(getCurrentPage()))
      {
         if (Scope.Process.equals(scope) && getSourceProcessInstances().size() == 1)
         {

            return COMMON_MESSAGE_BEAN.getParamString("views.attachToCase.successProcessAttachToCase.message",
                  ProcessInstanceUtils.getProcessLabel(getSourceProcessInstances().get(0)),
                  ProcessInstanceUtils.getProcessLabel(caseInstance));
         }
         else
         {
            return COMMON_MESSAGE_BEAN.getParamString("views.attachToCase.successProcessesAttachToCase.message",
                  ProcessInstanceUtils.getProcessLabel(caseInstance));
         }
      }
      else
      {
         if (Scope.Case.equals(scope) && Page.ADVANCE.equals(getCurrentPage()))
         {
            return COMMON_MESSAGE_BEAN.getString("views.attachToCase.specifyProcess.message");
         }
         else if (Scope.Case.equals(scope))
         {
            return COMMON_MESSAGE_BEAN.getString("views.attachToCase.selectProcesses.message");
         }
         else 
         {
            return COMMON_MESSAGE_BEAN.getString("views.attachToCase.selectCase.message");
         }
      }
   }
   
}
