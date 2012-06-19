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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.List;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;


public class OpenActivitiesUserObject extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String overviewLabel;

   private List<OpenActivitiesDynamicUserObject> participantList;

   /**
    * @param overviewLabel
    * @param participantList
    */
   public OpenActivitiesUserObject(String overviewLabel,
         List<OpenActivitiesDynamicUserObject> participantList)
   {
      super();
      this.overviewLabel = overviewLabel;
      this.participantList = participantList;
   }

   public List<OpenActivitiesDynamicUserObject> getParticipantList()
   {
      return participantList;
   }

   public void setParticipantList(List<OpenActivitiesDynamicUserObject> participantList)
   {
      this.participantList = participantList;
   }

   public String getOverviewLabel()
   {
      return overviewLabel;
   }

   public void setOverviewLabel(String overviewLabel)
   {
      this.overviewLabel = overviewLabel;
   }

}
