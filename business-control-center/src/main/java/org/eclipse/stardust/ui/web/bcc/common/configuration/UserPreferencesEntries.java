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
package org.eclipse.stardust.ui.web.bcc.common.configuration;

public interface UserPreferencesEntries
{
   public static final String M_BCC = "ipp-business-control-center";
   // Admin Module is used for fetching User Preference 
   public static final String M_ADMIN_PORTAL = "ipp-admin-portal";
  
   public static final String V_PRIORITY_VIEW = "priorityView";
   public static final String V_COMPLETED_ACTIVITY = "CompletedActivity";
   public static final String V_COST_AND_CONTROLLING = "CostsandControlling";
   public static final String V_CUSTOM_AllCOLUMNS = "allColumns";
   public static final String V_GNATT_CHART = "ganttChart";
   public static final String V_LOGIN_TIME = "LoginTime";
   public static final String V_PENDING_ACTIVITIES = "pendingActivities";
   public static final String V_PERFORMANCE_TEAM_LEADER = "performanceTeamLeader";
   public static final String V_POSTPONED_ACTIVITIES = "postponedActivities";
   public static final String V_PROCESS_RESOURCE_USR_MGT = "ProcessResourceUserMgmt";
   public static final String V_PROCESS_RESOURCE_ROLE_MGT = "ProcessResourceRoleMgmt";
   public static final String V_RESOURCE_PERFORMANCE = "ResourcePerformance";
   public static final String V_ROLE_ASSIGNMENT = "roleAssignment";
   public static final String V_USER_ASSIGNED = "userAssigned";
   public static final String V_USER_ASSIGNABLE = "userAssignable";
   public static final String V_TRAFFIC_LIGHT = "TrafficLightView";

   public static final String V_ROLE_ASSIGNED = "roleAssigned";
   public static final String V_ROLE_ASSIGNABLE = "roleAssignable";
   public static final String V_ACTIVITY_CRITICALITY_VIEW = "activityCriticalityView";
   
   public static final String V_DEPUTY_TEAM_MEMBER_VIEW = "deputyTeamMemberView";

   public static final String V_PROCESS_SEARCH = "processSearch";
   public static final String F_PROCESS_SEARCH_PROCESS_TABLE = "processTable";
   public static final String F_PROCESS_SEARCH_ACTIVITY_TABLE = "activityTable";
}
