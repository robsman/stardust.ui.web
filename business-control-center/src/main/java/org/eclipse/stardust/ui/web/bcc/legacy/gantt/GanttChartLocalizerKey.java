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

import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

public class GanttChartLocalizerKey extends LocalizerKey
{
   private static final long serialVersionUID = 1L;

   private static final String MESSAGE_BUNDLE = "GanttDiagramViewMessages";

   public static final LocalizerKey INVALID_START_TIME = new GanttChartLocalizerKey(
         "invalidStartTime");

   public static final LocalizerKey INVALID_TERM_TIME = new GanttChartLocalizerKey(
         "invalidTermTime");

   public static final LocalizerKey INVALID_DURATION = new GanttChartLocalizerKey(
         "invalidDuration");

   public static final LocalizerKey INVALID_THRES_PCT = new GanttChartLocalizerKey(
         "invalidThresholdPercentage");

   public GanttChartLocalizerKey(String key)
   {
      super(MESSAGE_BUNDLE, key);
   }
}
