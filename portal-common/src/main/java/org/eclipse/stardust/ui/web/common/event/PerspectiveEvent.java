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
package org.eclipse.stardust.ui.web.common.event;

import java.util.Map;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;

/**
 * @author Subodh.Godbole
 *
 */
public class PerspectiveEvent
{
   public static enum PerspectiveEventType
   {
      ACTIVATED,
      DEACTIVATED,
      LAUNCH_PANELS_ACTIVATED,
      LAUNCH_PANELS_DEACTIVATED
   }

   private final PerspectiveEventType type;
   private final IPerspectiveDefinition perspective;
   private final Map<String, Object> params;

   /**
    * @param perspective
    * @param type
    */
   public PerspectiveEvent(IPerspectiveDefinition perspective, PerspectiveEventType type, Map<String, Object> params)
   {
      this.perspective = perspective;
      this.type = type;
      this.params = params;
   }

   public PerspectiveEventType getType()
   {
      return type;
   }

   public IPerspectiveDefinition getPerspective()
   {
      return perspective;
   }

   public Map<String, Object> getParams()
   {
      return params;
   }
}
