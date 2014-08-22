/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Sauer (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.documenttriage.service;

import static java.util.Collections.emptyMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.dto.ActivityInstanceDetails;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IDataMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityThread;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

public class PerformDocumentRendezvousCommand implements ServiceCommand
{
   private static final long serialVersionUID = 1L;

   private final long rendezvousAiOid;

   private final Document document;

   public PerformDocumentRendezvousCommand(long rendezvousAiOid, Document document)
   {
      this.rendezvousAiOid = rendezvousAiOid;
      this.document = document;
   }

   public ActivityInstance execute(ServiceFactory sf)
   {
      // activate rendezvous AI (this will trigger both IN and OUT route mappings, but
      // effectively doing nothing)
      sf.getWorkflowService().activate(rendezvousAiOid);

      ActivityInstanceBean rendezvousAi = ActivityInstanceBean.findByOID(rendezvousAiOid);

      // TODO
      Map<String, Document> outValues = newHashMap();
      for (IDataMapping mapping : rendezvousAi.getActivity().getDataMappings())
      {
         if (Direction.OUT.isCompatibleWith(mapping.getDirection())
               && PredefinedConstants.DEFAULT_CONTEXT.equals(mapping.getContext()))
         {
            IData targetVar = mapping.getData();
            if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(targetVar.getType().getId()))
            {
               outValues.put(mapping.getActivityAccessPointId(), document);
            }
         }
      }

      // use internal API to re-trigger route OUT mappings, but using given accessPoint
      // values
      rendezvousAi.processRouteOutDataMappings(rendezvousAi.getActivity(), outValues);

      // complete rendezvous AI, resuming process execution
      ActivityThread.schedule(null, null, rendezvousAi, true, null, emptyMap(), false);

      return DetailsFactory.create(rendezvousAi, IActivityInstance.class,
            ActivityInstanceDetails.class);
   }
}
