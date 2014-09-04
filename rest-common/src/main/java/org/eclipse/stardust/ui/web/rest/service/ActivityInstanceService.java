/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.ActivityInstanceDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class ActivityInstanceService
{

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   /**
    * @param activityInstanceOid
    * @return
    */
   public ActivityInstanceDTO getActivityInstance(long activityInstanceOid)
   {
      ActivityInstance ai = activityInstanceUtils
            .getActivityInstance(activityInstanceOid);

      return ActivityInstanceDTOBuilder.build(ai);
   }

   /**
    * @param activityInstanceOid
    * @return
    */
   public List<DocumentDTO> getProcessAttachmentsForActivityInstance(
         long activityInstanceOid)
   {
      List<Document> processAttachments = activityInstanceUtils
            .getProcessAttachments(activityInstanceOid);

      List<DocumentDTO> processAttachmentsDTO = DocumentDTOBuilder
            .build(processAttachments);

      return processAttachmentsDTO;
   }

   /**
    * @param oid
    * @param documentId
    * @return
    */
   public List<ProcessInstanceDTO> completeRendezvous(long oid, String documentId)
   {
      ActivityInstance completedAi = activityInstanceUtils.completeRendezvous(oid,
            documentId);

      // TODO: Change method return type
      // return completedAi;

      return null;
   }
}
