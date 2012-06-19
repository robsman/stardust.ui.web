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
public class ClosePanelScenario extends StringKey
{
   
   private static final long serialVersionUID = 1L;

   public static final ClosePanelScenario UNKNOWN = new ClosePanelScenario("unknown");

   public static final ClosePanelScenario COMPLETE = new ClosePanelScenario("complete");

   public static final ClosePanelScenario QA_PASS = new ClosePanelScenario("qaPass");
   
   public static final ClosePanelScenario QA_FAIL = new ClosePanelScenario("qaFail");

   public static final ClosePanelScenario SUSPEND = new ClosePanelScenario("suspend");

   public static final ClosePanelScenario SUSPEND_AND_SAVE = new ClosePanelScenario(
         "suspendAndSave");

   public static final ClosePanelScenario ABORT = new ClosePanelScenario("abort");

   private ClosePanelScenario(String id)
   {
      super(id, id);
   }

}
