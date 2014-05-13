/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.core.handler.process;

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.reporting.core.Constants.PiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractColumnHandlerRegistry;

public class PiColumnHandlerRegistry extends AbstractColumnHandlerRegistry<ProcessInstance, ProcessInstanceQuery>
{
   public PiColumnHandlerRegistry()
   {
      register(new PiDescriptorColumnHandler());
      register(new PiComputedColumnHandler());
      register(PiDimensionField.OID.getId(), new PiOidColumnHandler());
      register(PiDimensionField.DURATION.getId(), new PiDurationColumnHandler());
      register(PiDimensionField.ROOT_DURATION.getId(), new PiRootDurationColumnHandler());
      register(PiDimensionField.PROCESS_NAME.getId(), new PiNameColumnHandler());
      register(PiDimensionField.PRIORITY.getId(), new PiPriorityColumnHandler());
      register(PiDimensionField.START_TIMESTAMP.getId(), new PiStartTimestampColumnHandler());
      register(PiDimensionField.ROOT_START_TIMESTAMP.getId(), new PiRootStartTimestampColumnHandler());
      register(PiDimensionField.TERMINATION_TIMESTAMP.getId(), new PiTerminationTimestampColumnHandler());
      register(PiDimensionField.STARTING_USER_NAME.getId(), new PiStartingUserNameColumnHandler());
      register(PiDimensionField.STATE.getId(), new PiStateColumnHandler());
   }
}
