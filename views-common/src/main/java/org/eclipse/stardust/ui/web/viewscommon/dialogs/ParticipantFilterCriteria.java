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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options;



/**
 * @author Shrikant.Gangal
 * 
 */
public interface ParticipantFilterCriteria
{
   public Options getDefaultParticipantOptions();

   public IDepartmentProvider.Options getDeptParticipantOptions();

   public List<ActivityInstance> getActivityInstances();

   public boolean isLimitedSearchEnabled();
   
   public int getTypeFilter();
}
