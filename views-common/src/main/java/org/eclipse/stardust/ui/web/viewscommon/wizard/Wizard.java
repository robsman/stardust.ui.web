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
package org.eclipse.stardust.ui.web.viewscommon.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardFlowEvent.WizardFlowEventType;
import org.eclipse.stardust.ui.web.viewscommon.wizard.WizardPageEvent.WizardPageEventType;


/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public abstract class Wizard extends PopupUIComponentBean implements FlowEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final String ACTION = "ACTION";
   private static final int FIRST_PAGE = 0;
   private List<WizardPage> pages;
   private int index = FIRST_PAGE;
   private final Stack<Integer> breadcrumbStack = new Stack<Integer>();
   private MessagesViewsCommonBean propsBean;
   


   /**
    * Constructor
    */
   public Wizard()
   {
      breadcrumbStack.push(index);
      propsBean = MessagesViewsCommonBean.getInstance();
   }

   /**
    * Action method to process action event for flow of wizard page
    * 
    * @param event
    */
   public final void flowAction()
   {
      try
      {
         String action = FacesUtils.getRequestParameter(ACTION);
         processFlow(action);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * method to process flow of wizard page based on action type
    * 
    * @param event
    */
   private void processFlow(String action)
   {
      WizardFlowEvent event = null;
      if ("FINISH".equals(action))
      {
         event = new WizardFlowEvent(WizardFlowEventType.FINISH, pages.get(index), pages.get(index));

      }
      else if ("NEXT".equals(action))
      {
         int currentIndex = index;
         int nextIndex = currentIndex + 1;
         event = next(currentIndex, nextIndex);
      }
      else if ("PREVIOUS".equals(action))
      {
         if (isFirstPage()) // throws exception if back reaches first page and event type
         // is PREVIOUS
         {
            throw new FlowException("can't show previous page ");
         }

         WizardPage oldPage = pages.get(index);
         WizardPage newPage = pages.get(--index);

         event = new WizardFlowEvent(WizardFlowEventType.PREVIOUS, newPage, oldPage);

      }
      else
      {
         event = new WizardFlowEvent(WizardFlowEventType.UNKNOWN, pages.get(index), pages.get(index));
      }

      flowEvent(event);// fire event

      if (event.isJumped() && event.getType().equals(WizardFlowEvent.WizardFlowEventType.NEXT))
      {
         --index;
         event = next(index, event.getJumpToIndex());

      }

      if (!event.isVetoed() && "NEXT".equals(action))
      {
         breadcrumbStack.push(index);
      }
      else if (!event.isVetoed() && "PREVIOUS".equals(action))
      {
         breadcrumbStack.pop();
      }

      if (!event.isVetoed() && event.getOldPage() != event.getNewPage())
      {
         WizardPageEvent pageEventDeActive = new WizardPageEvent(WizardPageEvent.WizardPageEventType.PAGE_DEACTIVATE,
               event);
         event.getOldPage().handleEvent(pageEventDeActive);

         WizardPageEvent pageEventActive = new WizardPageEvent(WizardPageEvent.WizardPageEventType.PAGE_ACTIVATE, event);
         event.getNewPage().handleEvent(pageEventActive);
      }
      else if (!event.isVetoed() && event.getType().equals(WizardFlowEventType.FINISH))
      {
         WizardPageEvent pageEventDeActive = new WizardPageEvent(WizardPageEvent.WizardPageEventType.PAGE_DEACTIVATE,
               event);
         event.getOldPage().handleEvent(pageEventDeActive);
      }

      if ("FINISH".equals(action) && !event.isVetoed())
      {
         closePopup();
         breadcrumbStack.clear();
         breadcrumbStack.add(index);
      }
      else if ("NEXT".equals(action) && event.isVetoed())
      {
         breadcrumbStack.pop();
      }
   }

   /**
    * method to process next page
    * 
    * @param currentIndex
    * @param nextIndex
    * @return
    */
   private WizardFlowEvent next(int currentIndex, int nextIndex)
   {
      if (isLastPage())// throws exception if next reaches last page and event type is
      // NEXT
      {
         throw new FlowException("can't show next page ");
      }
      WizardPage oldPage = pages.get(currentIndex);
      WizardPage newPage = pages.get(nextIndex);
      WizardFlowEvent event = new WizardFlowEvent(WizardFlowEventType.NEXT, newPage, oldPage);

      index = nextIndex;
      return event;

   }

   /**
    * ActionListener method to process action event for flow of wizard page
    * 
    * @param event
    */
   public void flowActionListener(ActionEvent event)
   {
      try
      {
         String action = (String) event.getComponent().getAttributes().get("ACTION");
         processFlow(action);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * abstract method to handle call back WizardFlowEvent
    */
   public abstract void flowEvent(WizardFlowEvent event);

   public WizardPage getCurrentPage()
   {
      return pages.get(index);
   }

   public List<WizardPage> getPages()
   {
      return pages;
   }

   public void initializePages(List<WizardPage> pages)
   {
      this.pages = pages;
   }

   /**
    * method to check current page is first page or not
    * 
    * @return
    */
   public boolean isFirstPage()
   {
      if (index == FIRST_PAGE)
      {
         return true;
      }

      return false;
   }

   /**
    * method to check current page is last page or not
    * 
    * @return
    */
   public boolean isLastPage()
   {
      if (index == (pages.size() - 1))
      {
         return true;
      }

      return false;
   }

   @Override
   public void reset()
   {
      index = FIRST_PAGE;
   }

   @Override
   public void closePopup()
   {
      reset();
      super.closePopup();
   }

   /**
    * to make Finish button enable/disable
    * 
    * @return
    */
   public boolean isFinishEnable()
   {
      return true;
   }

   /**
    * to make Next button enable/disable
    * 
    * @return
    */

   public boolean isNextEnable()
   {
      return !isLastPage();
   }

   /**
    * to make Previous button enable/disable
    * 
    * @return
    */
   public boolean isPreviousEnable()
   {
      return !isFirstPage();
   }

   /**
    * to render Previous button
    * 
    * @return
    */
   public boolean isPreviousRender()
   {
      return !isFirstPage();
   }

   /**
    * to render Next button
    * 
    * @return
    */
   public boolean isNextRender()
   {
      return !isLastPage();
   }

   /**
    * to render Finish button
    * 
    * @return
    */
   public boolean isFinishRender()
   {
      return true;
   }

   public String getTitle()
   {
      return getCurrentPage().getTitle();
   }

   public String getNextLabel()
   {
      return propsBean.getString("common.wizard.next");
   }

   public String getPreviousLabel()
   {
      return propsBean.getString("common.wizard.previous");
   }

   public String getFinishLabel()
   {
      return propsBean.getString("common.wizard.finish");
   }
  
   public List<WizardPage> getBreadcrumb()
   {
      List<WizardPage> breadcrumbPages=new ArrayList<WizardPage>();
      
      for(int index:breadcrumbStack)
      {
         breadcrumbPages.add(pages.get(index));
      }
      return breadcrumbPages;

   }


   
   public void onActionListener(ActionEvent event)
   {
       String action = (String) event.getComponent().getAttributes().get("ACTION");   
       if (WizardPageEventType.PAGE_ONLOAD.name().equals(action))
       {
          try
          {
             // WizardPage previousPage=index>0?pages.get(-index):getCurrentPage();
             // WizardFlowEvent event = new
             // WizardFlowEvent(WizardFlowEventType.UNKNOWN,getCurrentPage(),
             // previousPage);
             WizardPageEvent pageEventDeActive = new WizardPageEvent(WizardPageEvent.WizardPageEventType.PAGE_ONLOAD);
             getCurrentPage().handleEvent(pageEventDeActive);
          }
          catch (Exception e)
          {
             //do nothing
          }
       }
     
   }



}
