/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.api.runtime.ScanDirection;
import org.eclipse.stardust.engine.api.runtime.TransitionOptions;
import org.eclipse.stardust.engine.api.runtime.TransitionReport;
import org.eclipse.stardust.engine.api.runtime.TransitionTarget;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUser;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Shrikant.Gangal
 *
 */
public class RelocateActivityDialogBean extends PopupUIComponentBean
      implements ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;

   private static final String BEAN_NAME = "relocateActivityDialogBean";

   private List<SelectItem> relocationTargets;

   private boolean relocationEligible;

   private String selectedTarget;

   private ActivityInstance activityInstance;

   private ICallbackHandler callbackHandler;

   private Map<String, TransitionTarget> activityVsTarget;

   private MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

   @Override
   public void initialize()
   {
      loadTargets();
   }

   /**
    * 
    */
   private void loadTargets()
   {
      relocationTargets = new ArrayList<SelectItem>();
      if (null != activityInstance)
      {
         List<TransitionTarget> targets = ServiceFactoryUtils.getWorkflowService()
               .getAdHocTransitionTargets(activityInstance.getOID(), TransitionOptions.DEFAULT,
                     ScanDirection.BACKWARD);
         if (null != targets)
         {
            activityVsTarget = new HashMap<String, TransitionTarget>();
            for (TransitionTarget target : targets)
            {
               if (null != selectedTarget)
               {
                  selectedTarget = target.getActivityId();
               }
               relocationTargets.add(new SelectItem(target.getActivityId(),
                     target.getActivityName()));
               activityVsTarget.put(target.getActivityId(), target);
            }
            relocationEligible = targets.size() > 0;
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.PopupUIComponentBean#openPopup()
    */
   @Override
   public void openPopup()
   {
      try
      {
         initialize();
         super.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * 
    */
   public boolean accept()
   {
      relocateActivity();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      return true;
   }

   /**
    * 
    */
   public void relocateActivity()
   {
      if (null != selectedTarget)
      {
         try
         {
            boolean canRelocate = false;

            // TODO - check what happens in case of application activity
            if (activityInstance.getState().equals(ActivityInstanceState.Application))
            {
               canRelocate = true;
            }
            else if (activityInstance.getState().equals(ActivityInstanceState.Suspended))
            {
               ServiceFactoryUtils.getWorkflowService().activate(activityInstance.getOID());
               canRelocate = true;
            }

            if (canRelocate)
            {
               TransitionReport report = ServiceFactoryUtils.getWorkflowService()
                     .performAdHocTransition(activityVsTarget.get(selectedTarget), false);
               ActivityInstance target = report.getTargetActivityInstance();
               IppUser loggedInUser = new IppUser();
               
               // Activate the activity if it's interactive and can be activated by current user
               if (null != target
                     && target.getActivity().isInteractive()
                     && loggedInUser.isInRole(target.getActivity()
                           .getDefaultPerformer()
                           .getQualifiedId()))
               {
                  // TODO check what parameters to pass
                  ActivityInstanceUtils.openActivity(target, new HashMap<String, Object>());
               }

               if (null != callbackHandler)
               {
                  callbackHandler.handleEvent(ICallbackHandler.EventType.APPLY);
               }
            }
         }
         catch (ConcurrencyException ce)
         {
            ExceptionHandler.handleException(ce,
                  MessagePropertiesBean.getInstance().getString("views.activityPanel.concurrencyError"));
         }
         catch (AccessForbiddenException af)
         {
            ExceptionHandler.handleException(af,
                  MessagePropertiesBean.getInstance().getString("relocation.dialog.notAuthorized"));
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      
      closePopup();
   }

   /**
    * 
    * @return
    */
   public static RelocateActivityDialogBean getInstance()
   {
      return (RelocateActivityDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * 
    * @author Sidharth.Singh
    * 
    */
   public class SelectItemComparator implements Comparator<SelectItem>
   {
      public int compare(SelectItem s1, SelectItem s2)
      {
         return s1.getLabel().compareTo(s2.getLabel());
      }
   }

   /**
    * @return
    */
   public List<SelectItem> getRelocationTargets()
   {
      return relocationTargets;
   }

   /**
    * @param relocationTargets
    */
   public void setRelocationTargets(List<SelectItem> relocationTargets)
   {
      this.relocationTargets = relocationTargets;
   }

   public boolean isRelocationEligible()
   {
      return relocationEligible;
   }

   public void setActivityInstance(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
   }

   public String getSelectedTarget()
   {
      return selectedTarget;
   }

   public void setSelectedTarget(String selectedTarget)
   {
      this.selectedTarget = selectedTarget;
   }

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public MessagesViewsCommonBean getCOMMON_MESSAGE_BEAN()
   {
      return COMMON_MESSAGE_BEAN;
   }
}
