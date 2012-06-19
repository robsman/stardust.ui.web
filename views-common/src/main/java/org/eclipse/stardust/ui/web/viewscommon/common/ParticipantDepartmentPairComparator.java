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

import java.util.Comparator;

public class ParticipantDepartmentPairComparator implements Comparator<ParticipantDepartmentPair>
{
   public int compare(ParticipantDepartmentPair o1, ParticipantDepartmentPair o2)
   {
      String participantId1, participantId2;
      Long departmentOid1, departmentOid2;
      
      participantId1 = o1.getParticipantId();
      participantId2 = o2.getParticipantId();
      
      departmentOid1 = o1.getDepartmentOid();
      departmentOid2 = o2.getDepartmentOid();
      
      if (participantId1.equals(participantId2))
      {
         return departmentOid1.compareTo(departmentOid2);
      }
      else
      {
         return participantId1.compareTo(participantId2);
      }
   }

}
