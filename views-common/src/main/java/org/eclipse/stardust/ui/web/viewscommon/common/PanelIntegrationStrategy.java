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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.common.StringKey;

/**
 * @author sauer
 * @version $Revision: $
 */
public class PanelIntegrationStrategy extends StringKey
{
   
   private static final long serialVersionUID = 1L;

   public static final PanelIntegrationStrategy UNKNOWN = new PanelIntegrationStrategy("unknown");

   public static final PanelIntegrationStrategy EMBEDDED_FACELET = new PanelIntegrationStrategy("ui:include");

   public static final PanelIntegrationStrategy EMBEDDED_IFRAME = new PanelIntegrationStrategy("iframe");

   public static final PanelIntegrationStrategy REDIRECT = new PanelIntegrationStrategy("redirect");

   public static final PanelIntegrationStrategy FORK = new PanelIntegrationStrategy("fork");

   private PanelIntegrationStrategy(String id)
   {
      super(id, id);
   }

}
