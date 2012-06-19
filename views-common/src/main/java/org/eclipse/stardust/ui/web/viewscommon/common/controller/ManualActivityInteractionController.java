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
package org.eclipse.stardust.ui.web.viewscommon.common.controller;

import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ManualActivityInteractionController implements IActivityInteractionController
{

   private static final Logger trace = LogManager.getLogger(ManualActivityInteractionController.class);

   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.DEFAULT_CONTEXT;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.EMBEDDED_FACELET;
   }

   public void initializePanel(ActivityInstance ai, Map inData)
   {

   }

   public String providePanelUri(ActivityInstance ai)
   {
      return AbstractProcessExecutionPortal.GENERIC_PANEL;
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      // TODO any preconditions to check?
      return true;
   }

   public Map getOutDataValues(ActivityInstance ai)
   {
      trace.info("Manual Application");

      Map outData = CollectionUtils.newMap();

      return outData;
   }

}
