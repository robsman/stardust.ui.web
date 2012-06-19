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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import org.eclipse.stardust.engine.api.runtime.User;

/**
 * The implementing class is responsible to search for a <code>Worklist</code> <br>
 * This is used to pass UserWorklist-ActivitySearch requests from the UI model to the
 * responsible SearchHandler that is performing the actual Query.
 * 
 * @author roland.stamm
 * 
 */
public interface IActivitySearchUserSearchHandler
{
   /**
    * @param user
    *           the user to search the <code>Worklist</code> for
    */
   void searchWorklistFor(User user);
}
