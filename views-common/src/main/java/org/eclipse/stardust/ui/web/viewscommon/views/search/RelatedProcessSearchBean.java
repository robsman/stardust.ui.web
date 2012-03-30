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
package org.eclipse.stardust.ui.web.viewscommon.views.search;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.casemanagement.CreateCaseDialogBean;



/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 * @since 7.0
 */
public class RelatedProcessSearchBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "relatedProcessSearchBean";
   private Page currentPage;
   private String linkComment;
   private Long processOid;
   private final RelatedProcessSearchHelper relatedProcessSearchHelper = new RelatedProcessSearchHelper();
   private List<ProcessInstance> sourceProcessInstances;
   private ProcessInstance targetProcessInstance;
   private boolean skipNotification;
   private ICallbackHandler callbackHandler;

   @Override
   public void initialize()
   {
      currentPage = Page.SEARCH;      
      relatedProcessSearchHelper.setSourceProcessInstances(sourceProcessInstances);
      relatedProcessSearchHelper.initialize();
      relatedProcessSearchHelper.update();
   }
   
   
   
   /**
    * reset instance values
    */
   public void reset()
   {
      relatedProcessSearchHelper.reset();
      linkComment = null;    
   }
   

   @Override
   public void openPopup()
   {
      
      if (null == sourceProcessInstances)
      {
         List<ProcessInstance> list = CollectionUtils.newArrayList();
         list.add(getProcessInstance());
         sourceProcessInstances = list;
      }
      if (null != sourceProcessInstances)
      {
         initialize();
         super.openPopup();
      }
   }   
   
   /**
    * 
    */
   public void closeCasePopup()
   { 
      reset();
      super.closePopup();
   }



   /**
    * jsf action method to toggle page toggle Search to Advanced or Advanced to Search
    * page
    */
   public void toggleView()
   {
      if (Page.SEARCH.equals(currentPage))
      {
         currentPage = Page.ADVANCE;
      }
      else
      {
         currentPage = Page.SEARCH;
      }
      targetProcessInstance = null;
      linkComment = null;
      RelatedProcessTableEntry tableEntry = relatedProcessSearchHelper.getSelectedProcessInstance();
      if (null != tableEntry)
      {
         tableEntry.setSelected(false);
      }
      FacesUtils.clearFacesTreeValues();
   }
   
   /**
    * 
    * @return
    */
   private ProcessInstance getProcessInstance()
   {
      
      ProcessInstance processInstance = null;
      try
      {
         String processInstanceOID = FacesUtils.getRequestParameter("processInstanceOID");
         if (processInstanceOID != null)
         {
            processInstance = ProcessInstanceUtils.getProcessInstance(Long.valueOf(processInstanceOID),false,true);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      return processInstance;
   }


   /**
    * 
    * @return RelatedProcessSearchBean instance
    */
   public static RelatedProcessSearchBean getInstance()
   {
      return (RelatedProcessSearchBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }
   
   
   /**
    * JSF action to abort source process instance and join with selected or given process
    * instance. 
    * also @see WorkflowService#joinProcessInstance(long, long, String);
    */
   public void abortAndJoin()
   {

   }

   /**
    * JSF action to to create case
    * 
    */
   public void createCase()
   {
      closeCasePopup();
      CreateCaseDialogBean createCaseDialog = CreateCaseDialogBean.getInstance();
      createCaseDialog.openPopup();
   }

   /**
    * JSF action to to create case
    * 
    */
   public void addToCase()
   {

   }

   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    * @since 7.0
    */
   public static enum Page 
   {
      SEARCH, ADVANCE, NOTIFICATION;
   }

   public Page getCurrentPage()
   {
      return currentPage;
   }

   public void setCurrentPage(Page currentPage)
   {
      this.currentPage = currentPage;
   }

   public List<ProcessInstance> getSourceProcessInstances()
   {
      return sourceProcessInstances;
   }

   public void setSourceProcessInstances(List<ProcessInstance> sourceProcessInstances)
   {
      this.sourceProcessInstances = sourceProcessInstances;
   }

   public ProcessInstance getTargetProcessInstance()
   {
      return targetProcessInstance;
   }

   public void setTargetProcessInstance(ProcessInstance targetProcessInstance)
   {
      this.targetProcessInstance = targetProcessInstance;
   }

   public RelatedProcessSearchHelper getRelatedProcessSearchHelper()
   {
      return relatedProcessSearchHelper;
   }

   public String getLinkComment()
   {
      return linkComment;
   }

   public void setLinkComment(String linkComment)
   {
      this.linkComment = linkComment;
   }

   public Long getProcessOid()
   {
      return processOid;
   }

   public void setProcessOid(Long processOid)
   {
      this.processOid = processOid;
   } 
   /**
    * change search result from Match any to Match All or vise versa.
    */
   public void toggleMatch()
   {
      boolean matchAny = relatedProcessSearchHelper.isMatchAny();
      relatedProcessSearchHelper.setMatchAny(!matchAny);
      relatedProcessSearchHelper.update();
   }

   public boolean isSkipNotification()
   {
      return skipNotification;
   }

   public void setSkipNotification(boolean skipNotification)
   {
      this.skipNotification = skipNotification;
   }

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

}
