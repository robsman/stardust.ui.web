/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.common.controller;

import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;

/**
 * @author Subodh.Godbole
 *
 */
public class ActivityInteractionControllerUtils
{
   /**
    * @param scenario
    */
   public static boolean isExternalWebAppInterventionRequired(ClosePanelScenario scenario)
   {
      if ((ClosePanelScenario.COMPLETE == scenario) || ClosePanelScenario.COMPLETE_AND_NEXT_IN_WORKLIST == scenario
            || (ClosePanelScenario.SUSPEND_AND_SAVE == scenario)
            || ClosePanelScenario.SUSPEND_AND_SAVE_TO_DEFAULT_PERFORMER == scenario
            || ClosePanelScenario.SUSPEND_AND_SAVE_TO_USER_WORKLIST == scenario
            || (ClosePanelScenario.QA_PASS == scenario) || (ClosePanelScenario.QA_FAIL == scenario))
      {
         return true;
      }
      
      return false;
   }
}
