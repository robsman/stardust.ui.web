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
package org.eclipse.stardust.ui.web.bcc.jsf;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils;

public class BusinessControlCenterConstants
{
   protected final static Logger trace = LogManager.getLogger(BusinessControlCenterConstants.class);

   public static final String USER_DETAILS_ASSIGNABLE_ROLE_MODEL = "carnotBc/UserDetailsAssignableRoleModel";

   public static final String USER_DETAILS_ASSIGNED_ROLE_MODEL = "carnotBc/UserDetailsAssignedRoleModel";

   public static final String USER_DETAILS_ACTIVITIES_MODEL = "carnotBc/UserDetailsActivitiesModel";

   private final static ResourceBundle propBundle;

   private static String baseDiagramUrl;

   public static final String ADD_REVOKE_GRANTS_FROM_ALL_MODELS = "Carnot.AddAndRevokeGrantsFromAllModels";

   public static final String THRESHOLD_PROVIDER = "Carnot.Threshold.Provider";

   private static final IThresholdProvider thresholdProvider;

   public static final int YELLOW_THRESHOLD = 0;
   public static final int RED_THRESHOLD = 1;

   private static final String PROCESSING_TIME_THRESHOLD = "Carnot.ProcessingTimeThreshold";

   private static Float processingTimeThreshold[] = null;

   private static final String INSTANCE_COST_THRESHOLD = "Carnot.InstanceCostThreshold";

   private static Float instanceCostThreshold[] = null;

   private static final String WORKING_HOURS_PER_DAY = "Carnot.WorkingHoursPerDay";
   private static final String WORKING_HOURS_PER_DAY_PATTERN = "k:m";

   private static Integer workingMinutesPerDay;

   public static synchronized float getProcessingTimeThreshold(int type, float defaultValue)
   {
      if (processingTimeThreshold == null)
      {
         processingTimeThreshold = new Float[2];
         Float value = getFloatPropertyValue(PROCESSING_TIME_THRESHOLD + ".Yellow");
         processingTimeThreshold[YELLOW_THRESHOLD] = value;
         value = getFloatPropertyValue(PROCESSING_TIME_THRESHOLD + ".Red");
         processingTimeThreshold[RED_THRESHOLD] = value;
      }
      return processingTimeThreshold[type] == null ? defaultValue : processingTimeThreshold[type].floatValue();
   }

   public static synchronized float getInstanceCostThreshold(int type, float defaultValue)
   {
      if (instanceCostThreshold == null)
      {
         instanceCostThreshold = new Float[2];
         Float value = getFloatPropertyValue(INSTANCE_COST_THRESHOLD + ".Yellow");
         instanceCostThreshold[YELLOW_THRESHOLD] = value;
         value = getFloatPropertyValue(INSTANCE_COST_THRESHOLD + ".Red");
         instanceCostThreshold[RED_THRESHOLD] = value;
      }
      return instanceCostThreshold[type] == null ? defaultValue : instanceCostThreshold[type].floatValue();
   }

   public static IThresholdProvider getThresholdProvider()
   {
      return thresholdProvider;
   }

   public static String getDiagramUrl(String[] parameter)
   {
      if (baseDiagramUrl == null)
      {
         String url = FacesContext.getCurrentInstance().getExternalContext()
               .getInitParameter("ag.carnot.processportal.integration.LIVE_DIAGRAMS_URL");
         baseDiagramUrl = StringUtils.isEmpty(url) ? "" : url;
      }
      if (!StringUtils.isEmpty(baseDiagramUrl) && parameter.length > 0)
      {
         StringBuffer url = new StringBuffer(baseDiagramUrl);
         String SEP = baseDiagramUrl.indexOf("?") == -1 ? "?" : "&";
         for (int i = 0; i < parameter.length; ++i)
         {
            url.append(SEP).append(parameter[i]);
            SEP = "&";
         }
         return url.toString();
      }

      return baseDiagramUrl;
   }

   private static Float getFloatPropertyValue(String key)
   {
      String value = getStringPropertyValue(key);
      return StringUtils.isEmpty(value) ? null : new Float(value);
   }

   public static String getStringPropertyValue(String key)
   {
      try
      {
         return propBundle.getString(key);
      }
      catch (MissingResourceException e)
      {
         trace.warn("Key '" + key + "' not found in resource bundle");
      }
      return null;
   }

   public static int getWorkingMinutesPerDay()
   {
      if (workingMinutesPerDay == null)
      {
         workingMinutesPerDay = new Integer(DateUtils.MINUTES_PER_DAY);
         String value = getStringPropertyValue(WORKING_HOURS_PER_DAY);
         if (!StringUtils.isEmpty(value))
         {
            DateFormat dateFormat = new SimpleDateFormat(WORKING_HOURS_PER_DAY_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            value = value.replaceAll("[hHmM]", "");
            try
            {
               Date date = dateFormat.parse(value);
               long timeInMs = date != null ? date.getTime() : -1;
               if (timeInMs >= (DateUtils.MILLISECONDS_PER_MINUTE * 60) && timeInMs <= DateUtils.MILLISECONDS_PER_DAY)
               {
                  workingMinutesPerDay = new Integer((int) (timeInMs / DateUtils.MILLISECONDS_PER_MINUTE));
               }
               else
               {
                  trace.warn("Time declaration for property " + WORKING_HOURS_PER_DAY
                        + " is out of range. It should be between 1h:00m and 24h:00m");
               }
            }
            catch (ParseException e)
            {
               trace.error("Unable to parse time declaration for property " + WORKING_HOURS_PER_DAY, e);
            }
         }
      }
      return workingMinutesPerDay.intValue();
   }

   static
   {
      ResourceBundle properties = null;
      try
      {
         properties = ResourceBundle.getBundle("businessControlCenter");
      }
      catch (MissingResourceException e)
      {
         properties = new DefaultBusinessControlCenterResourceBundle();
      }
      propBundle = properties;

      String provider = getStringPropertyValue(THRESHOLD_PROVIDER);
      if (!StringUtils.isEmpty(provider))
      {
         Object thresholdCandidate = Reflect.createInstance(provider);
         if (thresholdCandidate instanceof IThresholdProvider)
         {
            thresholdProvider = (IThresholdProvider) thresholdCandidate;
         }
         else
         {
            thresholdProvider = new ThresholdProvider();
         }
      }
      else
      {
         thresholdProvider = new ThresholdProvider();
      }
   }

   private final static class DefaultBusinessControlCenterResourceBundle extends ListResourceBundle
   {

      protected Object[][] getContents()
      {
         return contents;
      }

      private static final String[][] contents = {
            {ADD_REVOKE_GRANTS_FROM_ALL_MODELS, "true"}, {THRESHOLD_PROVIDER, ""},
            {PROCESSING_TIME_THRESHOLD + ".Red", ""}, {PROCESSING_TIME_THRESHOLD + ".Yellow", ""},
            {INSTANCE_COST_THRESHOLD + ".Red", ""}, {INSTANCE_COST_THRESHOLD + ".Yellow", ""},
            {WORKING_HOURS_PER_DAY, ""}};
   }
}
