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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.utils.FacesUtils;


public class ConfigurationValidator
{
   private static final Pattern PROC_THRES_PATTERN = Pattern
         .compile("^((0[0-9]|1[0-9]|2[0-3])[0-5][0-9])$");

   public static boolean checkClassExists(Object input, LocalizerKey key)
   {
      boolean validConfigValues = true;
      String clazz = (String) input;
      if (!StringUtils.isEmpty(clazz))
      {
         try
         {
            Class.forName(clazz);
         }
         catch (ClassNotFoundException e)
         {
            validConfigValues = false;
            throw new ValidatorException(new FacesMessage(Localizer.getString(key)));
         }
      }
      return validConfigValues;
   }

   public static boolean validateProcessingThreshold(Object input)
         throws ValidatorException
   {
      boolean validConfigValues = true;
      String threshold = (String) input;
      Matcher matcher = PROC_THRES_PATTERN.matcher(threshold);
      if (!matcher.find())
      {
         validConfigValues = false;
//         FacesUtils
//               .executeJScript("TrPanelPopup.showPopup('trafficLightView_tlvConfigPopup_popupContainer', 'trafficLightView_tlvConfigPopup', '', 'click','centered',true,0,0,0,0);");
         throw new ValidatorException(new FacesMessage(Localizer
               .getString(TrafficlightLocalizerKey.INVALID_PROC_THRES_PATTERN)));
      }
      return validConfigValues;
   }
}
