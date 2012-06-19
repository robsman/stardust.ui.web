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
package org.eclipse.stardust.ui.web.viewscommon.common.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceInfo;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult;
import org.eclipse.stardust.engine.api.model.QualityAssuranceCode;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;

import com.icesoft.faces.component.menubar.MenuItem;
import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * Assist in displaying Quality Assurance Codes iframe popup and few other common functions
 * around Quality Assurance codes
 * 
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceCodesBean
{
   private boolean qualityAssuranceCodesPopupOpened = false;
   private List<String> qualityAssuranceCodes = null;
   private ActivityInstance activityInstance;
   private boolean visible = false;
   private boolean disabled = false;
   private boolean qualityAssuranceActivityInstance;
   private boolean revisedActivityInstance;
   List<MenuItem> qualityACMenuItems;

   /**
    * @param activityInstance
    */
   public QualityAssuranceCodesBean(ActivityInstance activityInstance)
   {
      super();
      this.activityInstance = activityInstance;
   }

   /**
    * initialized Quality Assurance Codes Iframe - to be displayed on Activity Panel
    */
   public void initializeIframeDisplay()
   {
      if (null != activityInstance && activityInstance.getActivity().isQualityAssuranceEnabled())
      {
         // set Visibility
         if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(activityInstance.getQualityAssuranceState()))
         {
            qualityAssuranceActivityInstance = true;
            visible = true;
         }
         else if (QualityAssuranceState.IS_REVISED.equals(activityInstance.getQualityAssuranceState()))
         {
            revisedActivityInstance = true;
            visible = true;
         }
         // Set Quality Assurance Codes
         if (visible)
         {
            QualityAssuranceInfo qualityAssuranceInfo = activityInstance.getQualityAssuranceInfo();
            if (null != qualityAssuranceInfo)
            {
               ActivityInstance failedQAInstance = qualityAssuranceInfo.getFailedQualityAssuranceInstance();
               if (null != failedQAInstance)
               {
                  ActivityInstanceAttributes attributes = failedQAInstance.getAttributes();
                  if (null != attributes)
                  {
                     QualityAssuranceResult qaResult = attributes.getQualityAssuranceResult();
                     if (null != qaResult)
                     {
                        Set<QualityAssuranceCode> qualityACodes = qaResult.getQualityAssuranceCodes();
                        qualityAssuranceCodes = new ArrayList<String>();
                        for (QualityAssuranceCode qualityAssuranceCode : qualityACodes)
                        {
                           qualityAssuranceCodes.add(I18nUtils.getQualityAssuranceDesc(qualityAssuranceCode,
                                 activityInstance.getModelOID()));
                        }
                     }
                  }
               }
            }
         }
         // check if the command is disabled
         if (CollectionUtils.isEmpty(qualityAssuranceCodes))
         {
            disabled = true;
         }
         else
         {
            Collections.sort(qualityAssuranceCodes);
         }
      }
   }

   /**
    * initialized Quality Assurance Codes Menu - to be displayed process history - activity
    * table
    */
   public void initializeMenu()
   {
      if (null != activityInstance && activityInstance.getActivity().isQualityAssuranceEnabled())
      {
         // set Visibility
         if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(activityInstance.getQualityAssuranceState()))
         {
            qualityAssuranceActivityInstance = true;
            visible = true;
         }
         if (visible)
         {
            Set<QualityAssuranceCode> qualityAssuranceCodes = null;
            // if the activity is in completed state
            if (ActivityInstanceState.Completed.equals(activityInstance.getState()))
            {
               ActivityInstanceAttributes attributes = activityInstance.getAttributes();
               if (null != attributes)
               {
                  QualityAssuranceResult qaResult = attributes.getQualityAssuranceResult();
                  if (null != qaResult)
                  {
                     qualityAssuranceCodes = qaResult.getQualityAssuranceCodes();
                  }
               }
            }

            qualityACMenuItems = new ArrayList<MenuItem>();
            if (null != qualityAssuranceCodes)
            {
               long modelOID = activityInstance.getModelOID();
               for (QualityAssuranceCode assuranceCode : qualityAssuranceCodes)
               {
                  if (null != assuranceCode)
                  {
                     MenuItem codeItem = new MenuItem();
                     codeItem.setIcon("/plugins/views-common/images/icons/wrench_exclamation.png");
                     codeItem.setValue(I18nUtils.getQualityAssuranceDesc(assuranceCode, modelOID));
                     qualityACMenuItems.add(codeItem);
                  }
               }
            }
         }
         // check if the command is disabled
         if (CollectionUtils.isEmpty(qualityACMenuItems))
         {
            disabled = true;
         }
         else
         {
            // sort codes
            Comparator<MenuItem> comparator = new Comparator<MenuItem>()
            {
               public int compare(MenuItem menuItem1, MenuItem menuItem2)
               {
                  String code1 = (String) menuItem1.getValue();
                  String code2 = (String) menuItem2.getValue();
                  return code1.compareTo(code2);
               }
            };
            Collections.sort(qualityACMenuItems, comparator);
         }
      }
   }

   /**
    * @return
    */
   public String getQualityAssuranceCodesIframePopupArgs()
   {
      String advanceArgs = "{anchorId:'ippQualityAssuranceCodesAnchor', width:100, height:30, maxWidth:500, maxHeight:550, "
            + "openOnRight:false, anchorXAdjustment:130, anchorYAdjustment:5, zIndex:200, border:'1px solid black', noUnloadWarning: 'true'}";
      return advanceArgs;
   }

   /**
    * @return
    */
   public String getQualityAssuranceCodesIframePopupId()
   {
      try
      {
         String iFrameId = "'QA" + activityInstance.getOID() + "'";
         return iFrameId;
      }
      catch (Exception e)
      {
         return "''"; // Consume Exception
      }
   }

   /**
    * Close QA codes popup
    */
   public void closeQualityAssuranceCodesIframePopup()
   {
      if (qualityAssuranceCodesPopupOpened)
      {
         String iFrameId = getQualityAssuranceCodesIframePopupId();
         String script = "InfinityBpm.ProcessPortal.closeContentFrame(" + iFrameId + ");";

         PortalApplication.getInstance().addEventScript(script);
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);

         qualityAssuranceCodesPopupOpened = false;
      }
   }

   /**
    * Open QA codes popup
    */
   public void openQualityAssuranceCodesIframePopup()
   {
      String iFrameId = getQualityAssuranceCodesIframePopupId();
      String url = "'" + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()
            + "/plugins/processportal/toolbar/qualityAssuranceCodesIframePopup.iface?random=" + System.currentTimeMillis()
            + "'";
      String script = "InfinityBpm.ProcessPortal.createOrActivateContentFrame(" + iFrameId + ", " + url + ", "
            + getQualityAssuranceCodesIframePopupArgs() + ");";
      PortalApplication.getInstance().addEventScript(script);
      JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), script);
      qualityAssuranceCodesPopupOpened = true;
   }

   public List<MenuItem> getQualityACItems()
   {
      return qualityACMenuItems;
   }

   /**
    * @return
    */
   public boolean isQualityAssuranceActivityInstance()
   {
      return qualityAssuranceActivityInstance;
   }

   /**
    * @return Quality Assurance codes of the activity instance
    */
   public List<String> getQualityAssuranceCodes()
   {
      return qualityAssuranceCodes;
   }

   /**
    * Don't show Quality Assurance Codes icon to normal user in first activity execution
    * 
    * @return
    */
   public boolean isQualityAssuranceCodesVisible()
   {
      return visible;
   }

   /**
    * if the activity instance is in review state - first time disable the QA codes icon
    * for QA manager
    * 
    * @return
    */
   public boolean isQualityAssuranceCodesDisabled()
   {
      return disabled;
   }

   public boolean isQualityAssuranceCodesPopupOpened()
   {
      return qualityAssuranceCodesPopupOpened;
   }

   /**
    * @return the revisedActivityInstance
    */
   public boolean isRevisedActivityInstance()
   {
      return revisedActivityInstance;
   }

   /**
    * @param revisedActivityInstance the revisedActivityInstance to set
    */
   public void setRevisedActivityInstance(boolean revisedActivityInstance)
   {
      this.revisedActivityInstance = revisedActivityInstance;
   }
}