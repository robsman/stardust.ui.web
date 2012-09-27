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
/**
 * 
 */
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.uielement.AbstractLaunchPanel;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.springframework.beans.factory.InitializingBean;



/**
 * @author roland.stamm
 * 
 */
public class OverviewBean extends AbstractLaunchPanel implements InitializingBean
{
   private static final long serialVersionUID = 2351221683195395611L;

   // constants have to equal IDs from selectOneMenu
   private static final String TODAY = "today";

   private static final String THIS_WEEK = "thisWeek";

   private static final String THIS_MONTH = "thisMonth";

   private static final String THIS_QUARTER = "thisQuarter";

   private static final String LAST_SIX_MONTHS = "lastSixMonths";

   private static final String LAST_YEAR = "lastYear";

   private static final String ALL = "all";

   private static final int MAX_PRIORITY_ITEM_COUNT = 10;

   private transient Map<String, LastNWorkedOnQueryBuilder> lastNWorkedOnQueryBuilders;

   private transient PriorityActivityQueryBuilder priorityActivityQueryBuilder;

   public OverviewBean()
   {
      super("workflowOverview");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
   }

   public void lastNSelectListener(ValueChangeEvent event)
   {
      String newValue = ((String) event.getNewValue());
      Calendar calendar = new GregorianCalendar(PortalApplication.getInstance().getTimeZone());
      if (newValue.equals(TODAY))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         selectLastNWorklist(calendar.getTime(), TODAY);
      }
      else if (newValue.equals(THIS_WEEK))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

         selectLastNWorklist(calendar.getTime(), THIS_WEEK);
      }
      else if (newValue.equals(THIS_MONTH))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         calendar.set(Calendar.DAY_OF_MONTH, 1);

         selectLastNWorklist(calendar.getTime(), THIS_MONTH);
      }
      else if (newValue.equals(THIS_QUARTER))
      {
         int month = calendar.get(Calendar.MONTH);

         int quarter = month / 3;

         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         calendar.set(Calendar.MONTH, quarter * 3);
         calendar.set(Calendar.DAY_OF_MONTH, 1);

         selectLastNWorklist(calendar.getTime(), THIS_QUARTER);
      }
      else if (newValue.equals(LAST_SIX_MONTHS))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         calendar.add(Calendar.MONTH, -6);

         selectLastNWorklist(calendar.getTime(), LAST_SIX_MONTHS);
      }
      else if (newValue.equals(LAST_YEAR))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         // TODO did FS mean since last year, only last year or since begin
         // of last year
         // ect.
         // right now uses the interval of [(now - 1year), now]
         calendar.add(Calendar.YEAR, -1);

         selectLastNWorklist(calendar.getTime(), LAST_YEAR);
      }
      else if (newValue.equals(ALL))
      {
         calendar.setTime(new Date(0));

         selectLastNWorklist(calendar.getTime(), ALL);
      }
   }

   /**
    * @return
    */
   public String selectDirectUserWorkAction()
   {
      selectDirectUserWorklist();
      return null;
   }

   /**
    * @return
    */
   public String selectPriorityActivityInstancesAction()
   {
      selectPriorityActivityInstances();
      return null;
   }

   /**
    * 
    */
   private void selectPriorityActivityInstances()
   {
      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put(Query.class.getName(), getPriorityActivityQueryBuilder().createQuery());
      // TODO use unique id
      params.put("id", "priorityActivityInstances");
      params.put("name", "High Priority"); //$NON-NLS-N$

      PPUtils.openWorklistView("id=" + "priorityActivityInstances", params);

      PPUtils.selectWorklist(null);
   }

   /**
    * 
    */
   private void selectDirectUserWorklist()
   {
      ParticipantInfo participantInfo = SessionContext.findSessionContext().getUser();

      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put(Query.class.getName(), ParticipantWorklistCacheManager.getInstance().getWorklistQuery(participantInfo));
      params.put("participantInfo", participantInfo);
      params.put("id", participantInfo.getId());
      String name = I18nUtils.getParticipantName(ParticipantUtils.getParticipant(participantInfo));
      params.put("name", name);

      PPUtils.openWorklistView("id=" + participantInfo.getId(), params);

      PPUtils.selectWorklist(participantInfo);
   }

   public void selectAllAssignedActivitiesAction()
   {
      openWorklist(SpecialWorklistCacheManager.ALL_ACTVITIES, SpecialWorklistCacheManager.getInstance()
            .getWorklistQuery(SpecialWorklistCacheManager.ALL_ACTVITIES));
   }
   
   public void selectCriticalActivitiesAction()
   {
      openWorklist(SpecialWorklistCacheManager.CRITICAL_ACTVITIES, SpecialWorklistCacheManager.getInstance()
            .getWorklistQuery(SpecialWorklistCacheManager.CRITICAL_ACTVITIES));
   }

   public long getAllAssignedActivitiesCount()
   {
      return SpecialWorklistCacheManager.getInstance().getWorklistCount(SpecialWorklistCacheManager.ALL_ACTVITIES);
   }      
  
   public long getCriticalActivitiesCount()
   {
      return SpecialWorklistCacheManager.getInstance().getWorklistCount(SpecialWorklistCacheManager.CRITICAL_ACTVITIES);
   }
   
   private void openWorklist(String id, Object activityInstanceQuery)
   {
      ParticipantInfo participantInfo = SessionContext.findSessionContext().getUser();

      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put(Query.class.getName(), activityInstanceQuery);
      params.put("participantInfo", participantInfo);
      params.put("id", id);
      String name = I18nUtils.getParticipantName(ParticipantUtils.getParticipant(participantInfo));
      params.put("name", name);

      PPUtils.openWorklistView("id=" + id, params);

      PPUtils.selectWorklist(participantInfo);
   }

   /**
    * @param date
    * @param dateId
    */
   private void selectLastNWorklist(Date date, String dateId)
   {
      LastNWorkedOnQueryBuilder lastNWorkedOnQueryBuilder = getLastNWorkedOnQueryBuilders().get(dateId);
      if (null == lastNWorkedOnQueryBuilder)
      {
         lastNWorkedOnQueryBuilder = new LastNWorkedOnQueryBuilder(date, dateId);
         getLastNWorkedOnQueryBuilders().put(dateId, lastNWorkedOnQueryBuilder);
      }
      lastNWorkedOnQueryBuilder.executeCountQuery();

      Map<String, Object> params = CollectionUtils.newTreeMap();
      params.put(Query.class.getName(), lastNWorkedOnQueryBuilder.createQuery());
      params.put("id", "lastNWorkedOn");
      params.put("name", this.getMessages().getString("pastProcessInstances." + dateId));

      PPUtils.openWorklistView("id=" + "lastNWorkedOn" + "&dateID=" + dateId, params);

      PPUtils.selectWorklist(null);
   }

   public long getDirectUserWorkCount()
   {
      return ParticipantWorklistCacheManager.getInstance().getWorklistCount(
            SessionContext.findSessionContext().getUser());
   }

   public long getPriorityActivityInstancesCount()
   {
      // TODO: This will be removed
      return 7000;
   }

   private class PriorityActivityQueryBuilder implements IQueryBuilder
   {
      private int maxCount;

      private ActivityInstances countQueryResult;

      public PriorityActivityQueryBuilder(int maxCount)
      {
         this.maxCount = maxCount;
      }

      public Query createQuery()
      {
         executeCountQuery();
         return countQueryResult.getQuery();
      }

      public ActivityInstances executeCountQuery()
      {
         countQueryResult = PPUtils.getActivityInstances_nCriticalForUser(maxCount);
         return countQueryResult;
      }

      public ActivityInstances getCountQueryResult()
      {
         return countQueryResult;
      }

      public void setMaxCount(int maxCount)
      {
         this.maxCount = maxCount;
      }
   }

   /**
    * @return
    */
   private Map<String, LastNWorkedOnQueryBuilder> getLastNWorkedOnQueryBuilders()
   {
      if (null == lastNWorkedOnQueryBuilders)
      {
         lastNWorkedOnQueryBuilders = new HashMap<String, LastNWorkedOnQueryBuilder>();
      }
      return lastNWorkedOnQueryBuilders;
   }
   
   /**
    * @return
    */
   private PriorityActivityQueryBuilder getPriorityActivityQueryBuilder()
   {
      if (null == priorityActivityQueryBuilder)
      {
         priorityActivityQueryBuilder = new PriorityActivityQueryBuilder(MAX_PRIORITY_ITEM_COUNT);
      }
      return priorityActivityQueryBuilder;
   }
   
   private class LastNWorkedOnQueryBuilder implements IQueryBuilder
   {
      private ActivityInstances countQueryResult;

      private Date date;

      private String dateId;

      public LastNWorkedOnQueryBuilder(Date date, String dateId)
      {
         this.date = date;
         this.dateId = dateId;
      }

      public Query createQuery()
      {
         executeCountQuery();
         return countQueryResult.getQuery();
      }

      public ActivityInstances getCountQueryResult()
      {
         return countQueryResult;
      }

      public ActivityInstances executeCountQuery()
      {
         // TODO fix query to search all past activity instances the user
         // worked on
         countQueryResult = PPUtils.getActivityInstances_lastNPerformedForUser(date);
         return countQueryResult;
      }

      public void setDate(Date date)
      {
         this.date = date;
      }

      public Date getDate()
      {
         return date;
      }

      public void setDateId(String dateId)
      {
         this.dateId = dateId;
      }

      public String getDateId()
      {
         return dateId;
      }
   }

   @Override
   public void update()
   {}
}
