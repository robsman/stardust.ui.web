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
package org.eclipse.stardust.ui.web.viewscommon.common.spi;

import java.util.Map;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ActivityInteractionHandler2Adapter implements IActivityInteractionController
{

   private final IActivityInteractionHandler2 aih2;

   public ActivityInteractionHandler2Adapter(IActivityInteractionHandler2 aih2)
   {
      this.aih2 = aih2;
   }

   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.JSF_CONTEXT;
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      return aih2.closePanel(ai, scenario);
   }

   public Map getOutDataValues(ActivityInstance ai)
   {
      // TODO this method was not part of the IActivityInteractionHandler2 contract
      return null;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return aih2.getPanelIntegrationStrategy(ai);
   }

   public void initializePanel(ActivityInstance ai, Map inData)
   {
      aih2.initializePanel(ai, inData);
   }

   public String providePanelUri(ActivityInstance ai)
   {
      return aih2.providePanelUri(ai);
   }

}
