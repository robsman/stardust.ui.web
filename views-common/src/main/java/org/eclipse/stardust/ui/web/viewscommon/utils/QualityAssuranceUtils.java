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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceResult.ResultState;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;



/**
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceUtils
{
   /**
    * return the relative icon path based on the type of activity
    * 
    * @param ai
    * @return
    */
   public static String getIconfor(ActivityInstance ai)
   {
      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(ai.getQualityAssuranceState()))
      {
         return Constants.ACTIVITY_QA_AWAIT_IMAGE;
      }
      else if (QualityAssuranceState.IS_REVISED.equals(ai.getQualityAssuranceState()))
      {
         return Constants.ACTIVITY_QA_FAILED_IMAGE;
      }
      return "";
   }

   /**
    * 
    * 1. Quality Assured activity processed - AI State: QUALITY_CONTROL_TRIGGERED
    * 
    * 2. Activity Failed at Quality Assurance stage - AI State: IS_QUALITY_CONTROL AI
    * Result State: FAILED
    * 
    * 3. Activity reprocessed - AI State: IS_REVISED
    * 
    * 4. Activity Passed in Quality Assurance stage - AI State: IS_QUALITY_CONTROL AI
    * Result State: PASS_WITH_CORRECTION OR AI Result State: PASS_NO_CORRECTION
    * 
    * @param type
    * @param activityInstance
    * @return
    */
   public static String getQAActivityInstanceType(String type, ActivityInstance activityInstance)
   {
      String instanceType = type;
      QualityAssuranceState qualityAState = activityInstance.getQualityAssuranceState();
      ActivityInstanceAttributes attributes = activityInstance.getAttributes();
      ResultState resultState = null;
      if (null != attributes)
      {
         QualityAssuranceResult qaResult = attributes.getQualityAssuranceResult();
         if (null != qaResult)
         {
            resultState = qaResult.getQualityAssuranceState();
         }
      }

      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(qualityAState))
      { // Activity Failed at Quality Assurance stage
         if (ResultState.FAILED.equals(resultState))
         {
            instanceType = "activityQAFailed";
         }// Activity Passed in Quality Assurance stage
         else if (ResultState.PASS_NO_CORRECTION.equals(resultState)
               || ResultState.PASS_WITH_CORRECTION.equals(resultState))
         {
            instanceType = "activityQAPassed";
         }
         else
         {
            instanceType = "activityQAAwait";
         }
      }
      return instanceType;
   }

   /**
    * validates input QA probability
    * 
    * @param qap
    * @return
    */
   public static boolean isQAProbabilityValid(String qap)
   {
      Integer probability = null;
      boolean validationPassed = true;
      if (StringUtils.isNotEmpty(qap))
      {
         try
         {
            probability = Integer.valueOf(qap);
            if ((probability > 100 || probability < 0))
            {
               validationPassed = false;
            }
         }
         catch (Exception e)
         {
            validationPassed = false;
         }
      }
      return validationPassed;
   }

   /**
    * converts valid QA probability from String to Integer
    * 
    * @param qap
    * @return
    */
   public static Integer getIntegerValueofQAProbability(String qap)
   {
      Integer qapInt = null;
      if (StringUtils.isNotEmpty(qap))
      {
         qapInt = Integer.valueOf(qap);
      }
      return qapInt;
   }

   /**
    * convert Integer QA probability to String value for display
    * 
    * @param qap
    * @return
    */
   public static String getStringValueofQAProbability(Integer qapInt)
   {
      String qap = null;
      if (null != qapInt)
      {
         qap = String.valueOf(qapInt);
      }
      return qap;
   }
}
