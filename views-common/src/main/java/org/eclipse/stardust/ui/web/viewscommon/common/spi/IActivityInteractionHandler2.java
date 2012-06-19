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

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;


/**
 * @author sauer
 * @version $Revision: $
 */
public interface IActivityInteractionHandler2 extends IActivityInteractionHandler
{

   PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai);
   
   void initializePanel(ActivityInstance ai, Map inData);
   
   /**
    * Callback to support customizations to the panel's URI, i.e. to dynamically add query
    * parameters. The returned URI will be used as is.
    * <p>
    * If the URI from the model should be used without any modifications,
    * <code>null</code> or an empty string should be returned.
    * 
    * @param ai The current activity instance.
    * @return The URI of the panel to be displayed, or <code>null</code> if the URI from the model should be used.
    */
   String providePanelUri(ActivityInstance ai);

   boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario);

}
