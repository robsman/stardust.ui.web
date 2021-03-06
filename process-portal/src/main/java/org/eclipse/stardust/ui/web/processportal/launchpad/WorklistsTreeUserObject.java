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
package org.eclipse.stardust.ui.web.processportal.launchpad;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantLabel;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * @author roland.stamm
 * 
 */
public class WorklistsTreeUserObject extends IceUserObject
{
   private ParticipantInfo participantInfo;

   private String style;

   private boolean disabled;
   
   private boolean refreshWorklistTable;
   
   private String userParticipantId;
   
   /**
    * @param wrapper
    */
   public WorklistsTreeUserObject(DefaultMutableTreeNode wrapper)
   {
      super(wrapper);
      disabled = false;
      style = "";
      participantInfo = null;
      refreshWorklistTable = true;
   }

   /**
    * @param event
    */
   public void select(ActionEvent event)
   {
      String viewKey = ParticipantUtils.getWorklistViewKey(participantInfo);
      Map<String, Object> params = CollectionUtils.newTreeMap();
      Query query = null;
      boolean leafNode= Boolean.valueOf((FacesUtils.getRequestParameter("leafNode")));
      if (!leafNode)
      {
         // Use Activity Instance Query for Unified worklist
         query = ParticipantWorklistCacheManager.getInstance().getActivityInstanceQuery(participantInfo, userParticipantId);
         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(new PerformingUserFilter(((UserInfo) participantInfo).getOID()));
         Set<ParticipantInfo> partInfo = ParticipantWorklistCacheManager.getInstance().getWorklistParticipants()
               .get(participantInfo.getQualifiedId());
         for (ParticipantInfo participantInfo1 : partInfo)
         {
            if (!(participantInfo1 instanceof UserInfo))
               or.add(PerformingParticipantFilter.forParticipant(participantInfo1));
         }
         // As Personal WL also has same viewKey,append Qualifier Id to make Unique
         // viewKey at parent Node
         viewKey = viewKey + participantInfo.getQualifiedId();
      }
      else
      {
         query = ParticipantWorklistCacheManager.getInstance().getWorklistQuery(participantInfo, userParticipantId);
      }
      params.put(Query.class.getName(), query);
      params.put("participantInfo", participantInfo);
      params.put("userParticipantId", userParticipantId);
      params.put("id", participantInfo.getQualifiedId());
      ParticipantLabel label = ModelHelper.getParticipantLabel(participantInfo);
      params.put("name", label.getLabel());
      params.put("wrappedLabel", label.getWrappedLabel());
      params.put("showAllWorklist", !leafNode);
      params.put("refreshWorklistTable", refreshWorklistTable);
      PPUtils.openWorklistView("id=" + viewKey, params);
      PPUtils.selectWorklist(participantInfo);
      refreshWorklistTable = false;
   }
   
   /**
    * @param event
    */
   public void selectHTML5(ActionEvent event)
   {
      String viewKey = ParticipantUtils.getWorklistViewKey(participantInfo);
      Map<String, Object> params = CollectionUtils.newTreeMap();
      boolean leafNode = Boolean.valueOf((FacesUtils.getRequestParameter("leafNode")));
      ParticipantLabel label = ModelHelper.getParticipantLabel(participantInfo);
      String labelName = null;

      if (!leafNode)
      {
         params.put("type", "unified");
         params.put("userId", userParticipantId);
         viewKey = viewKey + participantInfo.getQualifiedId();
         String unifiedLabel = MessagePropertiesBean.getInstance().getParamString(
               "views.worklistPanel.label.unifiedWorklist");
         labelName = unifiedLabel + " - " + label.getLabel();
      }
      else
      {
         if (userParticipantId.equals(participantInfo.getQualifiedId()))
         {

            String personalLabel = MessagePropertiesBean.getInstance().getParamString(
                  "views.worklistPanel.label.personalWorklist");
            params.put("userId", userParticipantId);
            params.put("type", "personal");
            labelName = personalLabel + " - " + label.getLabel();
         }
         else
         {
            if (participantInfo instanceof OrganizationInfo  && null != ((OrganizationInfo) participantInfo).getDepartment())
            {
               OrganizationInfo organization = (OrganizationInfo) participantInfo;
               params.put("participantQId", participantInfo.getQualifiedId());
               params.put("departmentQId",  organization.getDepartment().toString());
            }
            else if (participantInfo instanceof RoleInfo && null != ((RoleInfo) participantInfo).getDepartment()) {
               RoleInfo role = (RoleInfo) participantInfo;
               params.put("participantQId", participantInfo.getQualifiedId());
               params.put("departmentQId",  role.getDepartment().toString());
            }
            else
            {
               params.put("participantQId", participantInfo.getQualifiedId());
            }
            params.put("userId", userParticipantId);
            labelName = label.getLabel();
         }
      }
      params.put("id", participantInfo.getQualifiedId());
      // TODO revisit the label name
      params.put("name", labelName);
      PPUtils.openWorklistViewHTML5("id=" + viewKey, params);
      PPUtils.selectWorklist(participantInfo);
      refreshWorklistTable = false;
   }
   
   /**
    * @param worklist
    */
   public void setModel(ParticipantInfo participantInfo)
   {
      this.participantInfo = participantInfo;
      ParticipantLabel label = ModelHelper.getParticipantLabel(participantInfo);
      this.setText(label.getWrappedLabel() + ": ");
      this.setTooltip(label.getLabel());
      this.setLeafIcon(PPUtils.getParticipantIcon(participantInfo));
      this.setBranchContractedIcon(PPUtils.getParticipantIcon(participantInfo));
      this.setBranchExpandedIcon(PPUtils.getParticipantIcon(participantInfo));
   }
   
   public ParticipantInfo getParticipantInfo()
   {
      return participantInfo;
   }

   public void setStyle(String style)
   {
      this.style = style;
   }

   public String getStyle()
   {
      return style;
   }

   public String getUserParticipantId()
   {
      return userParticipantId;
   }

   public void setUserParticipantId(String userParticipantId)
   {
      this.userParticipantId = userParticipantId;
   }

   public String getActivityCount()
   {
      if (this.isLeaf())
      {
         Long totalCount = ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo, userParticipantId);
         Long totalCountThreshold = ParticipantWorklistCacheManager.getInstance().getWorklistCountThreshold(
               participantInfo, userParticipantId);
         if (totalCount < Long.MAX_VALUE)
            return totalCount.toString();
         else
            return MessagesViewsCommonBean.getInstance().getParamString("common.notification.worklistCountThreshold",
                  totalCountThreshold.toString());
      }
      else
      {
         // Get the child activity count and compute the totalCount for Root Node(User
         // worklist)
         Enumeration<DefaultMutableTreeNode> childObjects = (Enumeration<DefaultMutableTreeNode>) this.wrapper
               .children();
         Integer count = 0;
         while (childObjects.hasMoreElements())
         {
            String activityCnt = ((WorklistsTreeUserObject) childObjects.nextElement().getUserObject())
                  .getActivityCount();
            try
            {
               count = count + Integer.valueOf(activityCnt);
            }
            catch (NumberFormatException e)
            {
               return activityCnt;
            }
         }
         return count.toString();
      }
   }
   
   public boolean isDisabled()
   {
      return disabled;
   }
}
