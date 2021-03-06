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

import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;

public class OpenActivitiesDynamicUserObject extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;

   private Long today;

   private Long yesterday;

   private Double month;
   
   private Long hibernated;
   
   private Set<Long> openActivitiesTodayOids;
   private Set<Long> openActivitiesYesterdayOids;
   private Set<Long> openActivitiesHibernateOids;
   
   /**
    * @param today
    * @param yesterday
    * @param month
    */
   public OpenActivitiesDynamicUserObject(Long today, Long yesterday, Double month, Long hibernated)
   {
      super();
      this.today = today;
      this.yesterday = yesterday;
      this.month = month;
      this.hibernated = hibernated;
   }

   public void doPriorityAction(ActionEvent event)
   {
      Map param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Object timeFrameType = param.get("timeFrame");
      Object hibernateType = param.get("hibernate");
      Set<Long> oids = null;
      if (hibernateType != null && !StringUtils.isEmpty(hibernateType.toString()))
      {
         oids = openActivitiesHibernateOids;
      }
      else
      {
         if (null != timeFrameType)
         {
            if (Integer.valueOf(timeFrameType.toString()).intValue() == 0)
            {
               oids = openActivitiesTodayOids;
            }
            else
            {
               oids = openActivitiesYesterdayOids;
            }
         }
      }
      OpenActivitiesBean openActivityBean = (OpenActivitiesBean) ManagedBeanUtils.getManagedBean(OpenActivitiesBean.BEAN_ID);
      openActivityBean.fetchActivityAndRefresh(oids);
   }
   
   public Long getToday()
   {
      return today;
   }

   public void setToday(Long today)
   {
      this.today = today;
   }

   public Long getYesterday()
   {
      return yesterday;
   }

   public void setYesterday(Long yesterday)
   {
      this.yesterday = yesterday;
   }

   public Double getMonth()
   {
      return month;
   }

   public void setMonth(String month)
   {
      month = month;
   }

   public Long getHibernated()
   {
      return hibernated;
   }

   public void setHibernated(Long hibernated)
   {
      this.hibernated = hibernated;
   }
   
   public Set<Long> getOpenActivitiesTodayOids()
   {
      return openActivitiesTodayOids;
   }

   public void setOpenActivitiesTodayOids(Set<Long> openActivitiesTodayOids)
   {
      this.openActivitiesTodayOids = openActivitiesTodayOids;
   }

   public Set<Long> getOpenActivitiesYesterdayOids()
   {
      return openActivitiesYesterdayOids;
   }

   public void setOpenActivitiesYesterdayOids(Set<Long> openActivitiesYesterdayOids)
   {
      this.openActivitiesYesterdayOids = openActivitiesYesterdayOids;
   }

   public Set<Long> getOpenActivitiesHibernateOids()
   {
      return openActivitiesHibernateOids;
   }

   public void setOpenActivitiesHibernateOids(Set<Long> openActivitiesHibernateOids)
   {
      this.openActivitiesHibernateOids = openActivitiesHibernateOids;
   }
}
