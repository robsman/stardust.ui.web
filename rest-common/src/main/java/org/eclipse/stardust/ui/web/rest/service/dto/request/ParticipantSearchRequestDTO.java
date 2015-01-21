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

package org.eclipse.stardust.ui.web.rest.service.dto.request;

/**
 * @author Yogesh.Manware
 *
 */
public class ParticipantSearchRequestDTO
{
   // text to be searched
   private String searchText = "";

   // true means search only in the context of provided activities
   private boolean limitedSearch = true;

   // DelegationComponent.ParticipantType
   private String participantType = "All";

   /**
    * Indicates that the predefined <code>ADMINISTRATOR</code> role is not a valid
    * delegate, and should not be used to find valid users, too.
    * <p>
    * Any user being a member of the <code>ADMINISTRATOR</code> role plus an additional
    * qualifying role, too, should be included in the list of valid users, still.
    */
   private boolean disableAdministrator;

   // User types will not be included in the search result
   // selectedParticipantCase
   private boolean excludeUserType;

   private Long[] activities;

   public Long[] getActivities()
   {
      return activities;
   }

   public void setActivities(Long[] activities)
   {
      this.activities = activities;
   }

   public String getSearchText()
   {
      return searchText;
   }

   public void setSearchText(String searchText)
   {
      this.searchText = searchText;
   }

   public boolean isLimitedSearch()
   {
      return limitedSearch;
   }

   public void setLimitedSearch(boolean limitedSearch)
   {
      this.limitedSearch = limitedSearch;
   }

   public String getParticipantType()
   {
      return participantType;
   }

   public void setParticipantType(String participantType)
   {
      this.participantType = participantType;
   }

   public boolean isDisableAdministrator()
   {
      return disableAdministrator;
   }

   public void setDisableAdministrator(boolean disableAdministrator)
   {
      this.disableAdministrator = disableAdministrator;
   }

   public boolean isExcludeUserType()
   {
      return excludeUserType;
   }

   public void setExcludeUserType(boolean excludeUserType)
   {
      this.excludeUserType = excludeUserType;
   }
}
