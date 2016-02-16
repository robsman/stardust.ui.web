package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.rest.component.util.ActivitySearchUtil;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.ActivitySearchDTO;
import org.springframework.stereotype.Component;

@Component
public class ActivitySearchService
{

   @Resource
   private ActivitySearchUtil activitySearchUtil;

   public ActivitySearchDTO getAllResubmissionActivityInstances()
   {
      return activitySearchUtil.getAllResubmissionActivityInstances();
   }

   public ActivitySearchDTO getAllActivityInstances()
   {
      return activitySearchUtil.getAllActivityInstances();
   }

   public ActivitySearchDTO getWorklistForUser(long userOID)
   {
      return activitySearchUtil.getWorklistForUser(userOID);
   }

   public List<UserDTO> getUsers_anyLike(String firstName, String lastName)
   {
      return activitySearchUtil.getUsers_anyLike(firstName, lastName);
   }
}
