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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.ValidationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

public class MovePagesDialog extends PopupUIComponentBean
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   public static String PAGE_RANGE_REG_EX = "^(\\d+)-(\\d+)$";
   public static String NUMBER_VAL_REG_EX = "^\\d+$";
   private static final String MOVE_PAGE_UP = "0";
   private static final String MOVE_PAGE_DOWN = "1";
   private static final String VALIDATION_STYLE = "messagePanel";
   private String range = null;
   private String targetAction;
   private String targetPage;
   private int targetPageNumber;
   private int maxPageIndex;
   private List<SelectItem> pageActions;
   private Set<Integer> pageRange;
   private ValidationMessageBean validationMessageBean;
   private TIFFDocumentHolder docHolder;
   private boolean overlappingRange = false;
   private boolean pageRangeError = false;
   private boolean targetPageError = false;

   @Override
   public void initialize()
   {
      pageActions = CollectionUtils.newArrayList();
      pageActions.add(new SelectItem(MOVE_PAGE_UP, COMMON_MESSAGE_BEAN.getString("views.movePagesDialog.beforeAction")));
      pageActions.add(new SelectItem(MOVE_PAGE_DOWN, COMMON_MESSAGE_BEAN.getString("views.movePagesDialog.afterAction")));
      targetAction = pageActions.get(0).getValue().toString();
      validationMessageBean = new ValidationMessageBean();
      validationMessageBean.setStyleClass(VALIDATION_STYLE);
   }

   /**
    * @return
    */
   public static MovePagesDialog getCurrent()
   {
      return (MovePagesDialog) FacesUtils.getBeanFromContext("movePagesDialog");
   }

   @Override
   public void openPopup()
   {
      initialize();
      super.openPopup();
   }

   @Override
   public void apply()
   {
      try
      {
         validationMessageBean.reset();
         // if contains error message in context then not allowed to submit
         if (pageRangeError)
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidPageRange"), "movePageMsg");
            return;
         }
         else if(targetPageError)
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidTargetPage"), "movePageMsg");
            return;
         }
         if (CollectionUtils.isEmpty(pageRange))
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidPageRange"), "movePageMsg");
         }
         else if (StringUtils.isEmpty(targetPage) || targetPageNumber <= 0)
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidTargetPage"), "movePageMsg");
         }
         else if(overlappingRange)
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.overlappingPageRange"),
                  "movePageMsg");
         }
         else
         {
            int lastValue = ((TreeSet<Integer>) pageRange).last();
            int firstValue = ((TreeSet<Integer>) pageRange).first();
            validationMessageBean.reset();
            if (targetAction.equals(MOVE_PAGE_UP))
            {
               if(firstValue < targetPageNumber)
               {
                  validationMessageBean.addError(
                        COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidPageRange"), "movePageMsg");
                  return;
               }
               else
               {
                  docHolder.movePagesInRange(pageRange, targetPageNumber, firstValue, lastValue, true);   
               }
            }
            else
            {
               if(lastValue > targetPageNumber)
               {
                  validationMessageBean.addError(
                        COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidPageRange"), "movePageMsg");
                  return;
               }
               else
               {
                  docHolder.movePagesInRange(pageRange, targetPageNumber, firstValue, lastValue, false);   
               }
            }
            if (!validationMessageBean.isContainsMessage())
            {
               resetData();
               super.closePopup();
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(
               "",
               COMMON_MESSAGE_BEAN.getString("common.exception")
                     + " : "
                     + (StringUtils.isNotEmpty(e.getLocalizedMessage()) ? e.getLocalizedMessage() : COMMON_MESSAGE_BEAN
                           .getString("common.unknownError")));
      }
   }

   @Override
   public void closePopup()
   {
      resetData();
      super.closePopup();
   }
   
   private void resetData()
   {
      pageRange.clear();
      range = null;
      targetPage = null;
   }

   public void moveActionChange(ValueChangeEvent event)
   {
      String value = event.getNewValue().toString();
      if (value.equals(MOVE_PAGE_DOWN))
      {
         setTargetAction(MOVE_PAGE_DOWN);
      }
      else
      {
         setTargetAction(MOVE_PAGE_UP);
      }
   }

   public void pageRangeChange(ValueChangeEvent event)
   {
      try
      {
         range = event.getNewValue().toString();
         if (StringUtils.isEmpty(range))
         {
            return;
         }
         pageRange = validatePageRange(range, maxPageIndex);
         validationMessageBean.reset();
         if(pageRangeError)
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidPageRange"), "movePageMsg");
         }
         else if(overlappingRange)
         {
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.overlappingPageRange"), "movePageMsg");
         }
      }
      catch (Exception e)
      {
         FacesUtils.addErrorMessage(null, COMMON_MESSAGE_BEAN.getString("common.invalidValue.error"));
      }
   }
   
   public void targetPageChange(ValueChangeEvent event)
   {
      try
      {
         targetPage = event.getNewValue().toString();
         targetPageError = false;
         if (StringUtils.isEmpty(targetPage))
         {
            return;
         }
         else if (!targetPage.matches(NUMBER_VAL_REG_EX))
         {
            targetPageError = true;
         }
         else
         {
            targetPageNumber = Integer.valueOf(targetPage);
            if (targetPageNumber < 1)
            {
               targetPageError = true;
            }
         }
         if(targetPageError)
         {
            validationMessageBean.reset();
            validationMessageBean.addError(
                  COMMON_MESSAGE_BEAN.getParamString("views.movePagesDialog.error.invalidTargetPage"), "movePageMsg");
         }
      }
      catch (Exception e)
      {
         FacesUtils.addErrorMessage(null, COMMON_MESSAGE_BEAN.getString("common.invalidValue.error"));
      }
   }

   public Set<Integer> validatePageRange(String pageRange, int maxPageIndex)
   {
      String[] rangeArr = pageRange.split(",");
      int[] rangeResults = new int[2];
      pageRangeError = false;
      Set<Integer> ranges = org.eclipse.stardust.common.CollectionUtils.newTreeSet();
      if (rangeArr != null && rangeArr.length > 0)
      {
         for (int i = 0; i < rangeArr.length; i++)
         {
            String rangeTemp = rangeArr[i];
            if (rangeTemp.matches(PAGE_RANGE_REG_EX))
            {
               String[] numbers = rangeTemp.split("-");
               rangeResults[0] = Integer.parseInt(numbers[0]);
               rangeResults[1] = Integer.parseInt(numbers[1]);
               if ((rangeResults[0] < 1 || rangeResults[1] < 1) || rangeResults[0] > maxPageIndex
                     || rangeResults[1] > maxPageIndex)
               {
                  pageRangeError = true;
                  break;
               }
               for (int j = rangeResults[0]; j <= rangeResults[1]; j++)
               {
                  if (ranges.contains(j - 1))
                  {
                     overlappingRange = true;
                     break;
                  }
                  overlappingRange = false;
                  ranges.add(j - 1);
               }
            }
            else if (rangeTemp.matches(NUMBER_VAL_REG_EX))
            {
               Integer val = Integer.valueOf(rangeTemp);
               if (val > 0 && 2 <= maxPageIndex)
               {
                  if (ranges.contains(val - 1))
                  {
                     overlappingRange = true;
                     break;
                  }
                  overlappingRange = false;
                  ranges.add(val - 1);
               }
               else
               {
                  pageRangeError = true;
                  break;
               }
            }
            else
            {
               pageRangeError = true;
               break;
            }
         }
      }
      return ranges;
   }
   
   public int getMaxPageIndex()
   {
      return maxPageIndex;
   }

   public void setMaxPageIndex(int maxPageIndex)
   {
      this.maxPageIndex = maxPageIndex;
   }

   public String getRange()
   {
      return range;
   }

   public void setRange(String range)
   {
      this.range = range;
   }

   public String getTargetAction()
   {
      return targetAction;
   }

   public void setTargetAction(String targetAction)
   {
      this.targetAction = targetAction;
   }

   public String getTargetPage()
   {
      return targetPage;
   }

   public void setTargetPage(String targetPage)
   {
      this.targetPage = targetPage;
   }

   public List<SelectItem> getPageActions()
   {
      return pageActions;
   }

   public void setPageActions(List<SelectItem> pageActions)
   {
      this.pageActions = pageActions;
   }

   public TIFFDocumentHolder getDocHolder()
   {
      return docHolder;
   }

   public void setDocHolder(TIFFDocumentHolder docHolder)
   {
      this.docHolder = docHolder;
   }

   public ValidationMessageBean getValidationMessageBean()
   {
      return validationMessageBean;
   }

}
