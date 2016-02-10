/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.dto.response;

import java.util.List;
import java.util.UUID;

import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;

/**
 * @author Abhay.Thappan
 *
 */
public class WorklistParticipantDTO extends AbstractDTO implements Comparable<WorklistParticipantDTO>
{
   // exposed properties

   public List<WorklistParticipantDTO> children;

   public String uuid = UUID.randomUUID().toString(); // necessary for tree directive

   public long activityCount; // used in case My Assignments Panel

   public String icon; // used in case My Assignments Panel

   public String tooltip;

   public String name;

   public String viewKey;

   public String id;

   public String participantQId;

   public String userId;

   public String labelName;

   public boolean isAssemblyLineParticipant;

   public WorklistParticipantDTO()
   {}

   @Override
   public int compareTo(WorklistParticipantDTO o)
   {
      // TODO Auto-generated method stub
      return 0;
   }

}
