/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto.request;

import java.util.Map;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DelegationRequestDTO
{
   private Long[] activities;

   private String participant;

   //[USER, USERGROUP, ROLE, SCOPED_ROLE, ORGANIZATION, SCOPED_ORGANIZATION, DEPARTMENT]
   private String participantType;

   private Boolean updateNotes;
   
   private Boolean buildDefaultNotes;
   
   private String notes;
   
   private boolean delegateCase;
   
/* public static final String DEFAULT_CONTEXT = "default";
   public static final String ENGINE_CONTEXT = "engine";
   public static final String JFC_CONTEXT = "jfc";
   public static final String JSP_CONTEXT = "jsp";
   public static final String JSF_CONTEXT = "jsf";
   public static final String APPLICATION_CONTEXT = "application";
   public static final String PROCESSINTERFACE_CONTEXT = "processInterface";
   public static final String EXTERNALWEBAPP_CONTEXT = "externalWebApp";*/
   private String context = "default";
   
   public Map<String, Object> activityData;

   public Map<String, Object> getActivityData()
   {
      return activityData;
   }

   public void setActivityData(Map<String, Object> activityData)
   {
      this.activityData = activityData;
   }

   public Long[] getActivities()
   {
      return activities;
   }

   public String getParticipant()
   {
      return participant;
   }

   public String getParticipantType()
   {
      return participantType;
   }

   public String getNotes()
   {
      return notes;
   }

   public String getContext()
   {
      return context;
   }

   public boolean isDelegateCase()
   {
      return delegateCase;
   }

   public Boolean getUpdateNotes()
   {
      return updateNotes;
   }

   public void setUpdateNotes(Boolean updateNotes)
   {
      this.updateNotes = updateNotes;
   }

   public Boolean getBuildDefaultNotes()
   {
      return buildDefaultNotes;
   }

   public void setBuildDefaultNotes(Boolean buildDefaultNotes)
   {
      this.buildDefaultNotes = buildDefaultNotes;
   }
}
