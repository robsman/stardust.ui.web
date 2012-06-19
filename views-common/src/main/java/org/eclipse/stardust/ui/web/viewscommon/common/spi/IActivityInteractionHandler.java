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

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;


/**
 * @author rsauer
 * @version $Revision$
 */
public interface IActivityInteractionHandler
{

   String launchWorkshopActivity(ActivityInstance activityInstance,
         AbstractProcessExecutionPortal portal);

   interface Factory
   {

      IActivityInteractionHandler getInteractionHandler(Activity activity);

   }

}
