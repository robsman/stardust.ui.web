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

import org.eclipse.stardust.ui.web.bcc.views.PerformanceTeamLeaderBean.Teamleader;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * @author ankita.Patel
 * @version $Revision: $
 */
public class TeamLeaderTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private String name;

   private Teamleader teamLeader;

   private String userId;

   private String userOid;

   private List<CompletedActivityStatisticsTableEntry> statisticsList;

   public TeamLeaderTableEntry(Teamleader tl, String userId, String userOid,
         List<CompletedActivityStatisticsTableEntry> statisticsList)
   {
      super();
      this.teamLeader = tl;
      this.userId = userId;
      this.userOid = userOid;
      this.statisticsList = statisticsList;
      this.name = I18nUtils.getUserLabel(tl.getUser()) + " (" + tl.getTeamname() + ")";
   }

   /**
    * 
    */
   public TeamLeaderTableEntry()
   {
   // TODO Auto-generated constructor stub
   }

   public List<CompletedActivityStatisticsTableEntry> getStatisticsList()
   {
      return statisticsList;
   }

   public void setStatisticsList(
         List<CompletedActivityStatisticsTableEntry> statisticsList)
   {
      this.statisticsList = statisticsList;
   }

   public Teamleader getTeamLeader()
   {
      return teamLeader;
   }

   public void setTeamLeader(Teamleader teamLeader)
   {
      this.teamLeader = teamLeader;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getUserId()
   {
      return userId;
   }

   public void setUserId(String userId)
   {
      this.userId = userId;
   }

   public String getUserOid()
   {
      return userOid;
   }

   public void setUserOid(String userOid)
   {
      this.userOid = userOid;
   }

}
