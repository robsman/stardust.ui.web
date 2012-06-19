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

import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

public class TrafficlightLocalizerKey extends LocalizerKey
{
   private static final String MESSAGE_BUNDLE = "TrafficLightViewMessages";

   public static final LocalizerKey TRAFFICLIGHT_FIRST_COLUMN_NAME = new TrafficlightLocalizerKey(
         "trafficLightFirstColumnName");

   public static final LocalizerKey TRAFFICLIGHT_TOOLTIP = new TrafficlightLocalizerKey(
         "trafficLightTooltip");

   public static final LocalizerKey INVALID_PROC_THRES_PATTERN = new TrafficlightLocalizerKey(
         "invalidProcThresPattern");

   public static final LocalizerKey STATE_CALC_CLASS_NOT_FOUND = new TrafficlightLocalizerKey(
         "stateCalcClassNotFound");

   public static final LocalizerKey DESC_FILTER_CLASS_NOT_FOUND = new TrafficlightLocalizerKey(
         "descFilterClassNotFound");

   public TrafficlightLocalizerKey(String key)
   {
      super(MESSAGE_BUNDLE, key);
   }
}
