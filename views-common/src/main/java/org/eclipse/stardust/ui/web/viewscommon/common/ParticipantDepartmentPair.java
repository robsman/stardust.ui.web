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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;


public final class ParticipantDepartmentPair extends Pair<String, Long>
{
   private static final long serialVersionUID = 1L;

   public ParticipantDepartmentPair(String participantId, long departmentOid)
   {
      super(participantId, departmentOid);
   }

   public String getParticipantId()
   {
      return getFirst();
   }

   public long getDepartmentOid()
   {
      return getSecond();
   }

   public static ParticipantDepartmentPair getParticipantDepartmentPair(ModelParticipantInfo participant)
   {
      long depOid = 0l;
      if (participant == null)
      {
         return null;
      }
      if (participant.getDepartment() != null)
      {
         depOid = participant.getDepartment().getOID();
      }
      if (participant instanceof QualifiedModelParticipantInfo)
      {
         QualifiedModelParticipantInfo qualifiedOrganizationInfo = (QualifiedModelParticipantInfo) participant;
         return new ParticipantDepartmentPair(qualifiedOrganizationInfo.getQualifiedId(), depOid);
      }
      else
      {
         return new ParticipantDepartmentPair(participant.getId(), depOid);
      }
   }
}
