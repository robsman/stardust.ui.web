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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * 
 * @author Vikas.Mishra
 * @since 7.0
 */
public class CreateCaseDialogBean extends PopupUIComponentBean
{

   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "createCaseDialogBean";

   private String caseName;
   private String note;
   private String description;
   private boolean openCaseDetail = true;
   private ProcessInstance caseProcessInstance;
   private List<ProcessInstance> sourceProcessInstances;
   private String userLabel;
   

   /**
    * 
    */
   @Override
   public void initialize()
   {
      userLabel = I18nUtils.getUserLabel(SessionContext.findSessionContext().getUser());
   }

   public String getUserLabel()
   {
      return userLabel;
   }

   /**
    * reset instance values
    */
   public void reset()
   {
      caseName = null;
      note = null;
      openCaseDetail = true;
      description = null;
   }

   /**
    * 
    */
   @Override
   public void openPopup()
   {
      initialize();
      super.openPopup();
   }

   @Override
   public void closePopup()
   {
      reset();
      super.closePopup();
   }

   /**
    * 
    * @return CreateCaseDialogBean instance
    */
   public static CreateCaseDialogBean getInstance()
   {
      return (CreateCaseDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * 
    */
   public void apply()
   {
      try
      {
         if (StringUtils.isNotEmpty(caseName) && null != sourceProcessInstances)
         {
            // create Case
            long[] processOIDs = getProcessOIDs(sourceProcessInstances);
            caseProcessInstance = ServiceFactoryUtils.getWorkflowService().createCase(caseName,description, processOIDs);
            CommonDescriptorUtils.reCalculateCaseDescriptors(caseProcessInstance);

            // add notes
            if (StringUtils.isNotEmpty(note))
            {
               ProcessInstanceAttributes attributes = caseProcessInstance.getAttributes();
               attributes.addNote(note, ContextKind.ProcessInstance, caseProcessInstance.getOID());
               ServiceFactoryUtils.getWorkflowService().setProcessInstanceAttributes(attributes);
            }
            // open case
            if (openCaseDetail)
            {
               ProcessInstanceUtils.openProcessContextExplorer(caseProcessInstance);
            }
         }
         closePopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(
               "",
               MessagesViewsCommonBean.getInstance().getString("views.createCase.caseException") + " : "
                     + e.getLocalizedMessage());
      }
   }
   
   

   /**
    * 
    * @param sourceProcessInstances
    * @return
    */
   private long[] getProcessOIDs(List<ProcessInstance> sourceProcessInstances)
   {
      if (null != sourceProcessInstances)
      {
         int size = sourceProcessInstances.size();
         long[] processOIDs = new long[size];

         for (int i = 0; i < size; i++)
         {
            processOIDs[i] = sourceProcessInstances.get(i).getOID();
         }

         return processOIDs;
      }
      return new long[] {};
   }

   public String getCaseName()
   {
      return caseName;
   }

   public void setCaseName(String caseName)
   {
      this.caseName = caseName;
   }

   public String getNote()
   {
      return note;
   }

   public void setNote(String note)
   {
      this.note = note;
   }

   public boolean isOpenCaseDetail()
   {
      return openCaseDetail;
   }

   public void setOpenCaseDetail(boolean openCaseDetail)
   {
      this.openCaseDetail = openCaseDetail;
   }


   public ProcessInstance getCaseProcessInstance()
   {
      return caseProcessInstance;
   }

   public void setCaseProcessInstance(ProcessInstance caseProcessInstance)
   {
      this.caseProcessInstance = caseProcessInstance;
   }

   public List<ProcessInstance> getSourceProcessInstances()
   {
      return sourceProcessInstances;
   }

   public void setSourceProcessInstances(List<ProcessInstance> sourceProcessInstances)
   {
      this.sourceProcessInstances = sourceProcessInstances;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   
}
