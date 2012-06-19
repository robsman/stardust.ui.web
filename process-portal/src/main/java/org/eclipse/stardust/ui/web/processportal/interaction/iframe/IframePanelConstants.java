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
package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;

/**
 * @author sauer
 * @version $Revision: $
 */
public class IframePanelConstants
{

   public static String KEY_COMMAND = "ippIframePanelPanelCommand";
   
   public static String CMD_IFRAME_PANEL_INITIALIZE = "initialize";
   
   public static String CMD_IFRAME_PANEL_COMPLETE = ClosePanelScenario.COMPLETE.getId();

   public static String CMD_IFRAME_PANEL_SUSPEND_AND_SAVE = ClosePanelScenario.SUSPEND_AND_SAVE.getId();
   
   public static String KEY_INTERACTION_ID = "ippIframePanelInteractionId";

   public static String KEY_VIEW_ID = "ippIframePanelViewId";
   
}
