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
package org.eclipse.stardust.ui.web.reporting.core.handler.activity;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.reporting.core.Constants.AiDimensionField;
import org.eclipse.stardust.ui.web.reporting.core.handler.AbstractColumnHandlerRegistry;

public class AiColumnHandlerRegistry extends AbstractColumnHandlerRegistry<ActivityInstance, ActivityInstanceQuery>
{
   public AiColumnHandlerRegistry()
   {
      register(new AiDescriptorColumnHandler());
      register(AiDimensionField.OID.getId() ,new AiOidColumnHandler());
      register(AiDimensionField.PROCESS_OID.getId(), new AiProcessOidColumnHandler());
      register(AiDimensionField.ACTIVITY_NAME.getId(), new AiNameColumnHandler());
      register(AiDimensionField.STATE.getId(), new AiStateColumnHandler());
      register(AiDimensionField.PROCESS_NAME.getId(), new AiProcessNameColumnHandler());
      register(AiDimensionField.CRITICALITY.getId(), new AiCriticalityColumnHandler());
      register(AiDimensionField.START_TIMESTAMP.getId(), new AiStartTimeStampColumnHandler());
      register(AiDimensionField.LAST_MODIFICATION_TIMESTAMP.getId(), new AiLastModificationTimeStampColumnHandler());
      register(AiDimensionField.PROCESS_INSTANCE_START_TIMESTAMP.getId(), new AiProcessStartTimestampColumnHandler());
      register(AiDimensionField.PROCESS_INSTANCE_ROOT_START_TIMESTAMP.getId(), new AiRootProcessStartTimestampColumnHandler());
      register(AiDimensionField.USER_PERFORMER_NAME.getId(), new AiUserPerformerColumnHandler());
      register(AiDimensionField.PARTICIPANT_PERFORMER_NAME.getId(), new AiParticipantPerformerColumnHandler());
      register(AiDimensionField.DURATION.getId(), new AiDurationColumnHandler());
   }
}
