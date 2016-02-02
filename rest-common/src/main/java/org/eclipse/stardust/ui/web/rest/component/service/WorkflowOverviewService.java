package org.eclipse.stardust.ui.web.rest.component.service;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.springframework.stereotype.Component;

@Component
public class WorkflowOverviewService
{
   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   /**
    * 
    * @return
    */
   public String getAllAssignedActivitiesCount()
   {
      Long totalCount = SpecialWorklistCacheManager.getInstance().getWorklistCount(
            SpecialWorklistCacheManager.ALL_ACTVITIES);
      Long totalCountThreshold = SpecialWorklistCacheManager.getInstance().getWorklistCountThreshold(
            SpecialWorklistCacheManager.ALL_ACTVITIES);
      if (totalCount < Long.MAX_VALUE)
         return totalCount.toString();
      else
         return restCommonClientMessages.getParamString("common.notification.worklistCountThreshold",
               totalCountThreshold.toString());
   }
   /**
    * 
    * @return
    */
   public String getCriticalActivitiesCount()
   {
      Long totalCount = SpecialWorklistCacheManager.getInstance().getWorklistCount(
            SpecialWorklistCacheManager.CRITICAL_ACTVITIES);
      Long totalCountThreshold = SpecialWorklistCacheManager.getInstance().getWorklistCountThreshold(
            SpecialWorklistCacheManager.CRITICAL_ACTVITIES);
      if (totalCount < Long.MAX_VALUE)
         return totalCount.toString();
      else
         return restCommonClientMessages.getParamString("common.notification.worklistCountThreshold",
               totalCountThreshold.toString());
   }
   /**
    * 
    * @return
    */
   public String getDirectUserWorkCount()
   {
      User user = SessionContext.findSessionContext().getUser();
      Long totalCount = ParticipantWorklistCacheManager.getInstance().getWorklistCount(user, user.getQualifiedId());
      Long totalCountThreshold = ParticipantWorklistCacheManager.getInstance().getWorklistCountThreshold(user,
            user.getQualifiedId());
      if (totalCount < Long.MAX_VALUE)
         return totalCount.toString();
      else
         return restCommonClientMessages.getParamString("common.notification.worklistCountThreshold",
               totalCountThreshold.toString());
   }
}
