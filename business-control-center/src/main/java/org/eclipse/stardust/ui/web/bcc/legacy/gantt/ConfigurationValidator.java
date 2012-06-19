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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

public class ConfigurationValidator
{
   private static final Pattern TIME_PATTERN = Pattern
         .compile("^((0[0-9]|1[0-9]|2[0-3])[0-5][0-9])$");

   private static final Pattern NUMERIC_PATTERN = Pattern.compile("^([0-9]*)$");

   public static boolean validatePlannedStartTime(Object input)
   {
      return validateTimes(input, GanttChartLocalizerKey.INVALID_START_TIME);
   }

   public static boolean validatePlannedTerminationTime(Object input)
   {
      return validateTimes(input, GanttChartLocalizerKey.INVALID_TERM_TIME);
   }

   public static boolean validateDurationSeconds(Object input)
   {
      return validateNumericValue(input, GanttChartLocalizerKey.INVALID_DURATION);
   }

   public static boolean validateThresholdPercentage(Object input)
   {
      return validateNumericValue(input, GanttChartLocalizerKey.INVALID_THRES_PCT);
   }

   private static boolean validateNumericValue(Object input, LocalizerKey key)
   {
      return validateRegexp(input, key, NUMERIC_PATTERN);
   }

   private static boolean validateTimes(Object input, LocalizerKey key)
   {
      return validateRegexp(input, key, TIME_PATTERN);
   }

   private static boolean validateRegexp(Object input, LocalizerKey key, Pattern pattern)
   {
      boolean validConfigValues = true;
      String value = (String) input;
      Matcher matcher = pattern.matcher(value);
      if (!matcher.find())
      {
         validConfigValues = false;
//         FacesUtils
//         .executeJScript("TrPanelPopup.showPopup('ganttConfigPopup_popupContainer', 'ganttConfigPopup', '', 'click','centered',true,600,0,0,0);");
         throw new ValidatorException(new FacesMessage(Localizer.getString(key)));
      }
      return validConfigValues;
   }
}
