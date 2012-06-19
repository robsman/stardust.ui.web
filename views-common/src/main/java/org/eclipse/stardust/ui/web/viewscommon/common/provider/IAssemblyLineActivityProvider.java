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
package org.eclipse.stardust.ui.web.viewscommon.common.provider;

import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;


/**
 * Allows customization of the assembly line (work on whatever task is available next)
 * behavior, including:
 * <ul>
 * <li>Classification of pending activity instances (which pending activity instances
 * should be put to the assembly line)
 * <li>The relative priority of tasks (providing the "next" task if the user chooses to
 * pull one from the assembly line).
 * </ul>
 * 
 * @see org.eclipse.stardust.ui.web.viewscommon.common.provider.DefaultAssemblyLineActivityProvider
 * @author rsauer
 * @version $Revision$
 */
public interface IAssemblyLineActivityProvider
{
   /**
    * Returns the next assembly line activity instance, if existent. The returned activity
    * instance must already be locked for exclusive use by the current user, i.e. being
    * activated. Furthermore the default performer of the activity instance should
    * be one of the participants given by the corresponding parameter.
    * 
    * @param portal The execution portal representing the current session.
    * @param participantIds List of assembly line participants (roles).
    * @return Activity instance that should be performed by the user
    * @throws PortalException
    */
   ActivityInstance getNextAssemblyLineActivity(AbstractProcessExecutionPortal portal,
         Set participantIds) throws PortalException;
   
   /**
    * Returns the number of activity instances, that are theoretical available.
    * It is made up of the user worklist count and the cumulated count of the 
    * activity instances per participant (given by the corresponding parameter).
    * 
    * @param portal The execution portal representing the current session.
    * @param participantIds List of assembly line participants (roles).
    * @return total number of available activity instances
    * @throws PortalException
    */
   long getAssemblyLineActivityCount(AbstractProcessExecutionPortal portal,
         Set participantIds) throws PortalException;
}