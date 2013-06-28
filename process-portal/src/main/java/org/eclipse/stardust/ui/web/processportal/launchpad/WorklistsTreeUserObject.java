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
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
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
      Participant participant= ParticipantUtils.getParticipant(participantInfo);
      Map<String, Object> params = CollectionUtils.newTreeMap();
      boolean leafNode= Boolean.valueOf((FacesUtils.getRequestParameter("leafNode")));
      WorklistQuery query = ParticipantWorklistCacheManager.getInstance().getWorklistQuery(participantInfo);
      if (!leafNode)
      {
         Set<ParticipantInfo> partInfo = ParticipantWorklistCacheManager.getInstance().getWorklistParticipants()
               .get(participantInfo.getId());
         for (ParticipantInfo participantInfo1 : partInfo)
         {
            if (!participantInfo1.getId().equals(participantInfo.getId()))
            {
               query.setParticipantContribution(PerformingParticipantFilter.forParticipant(participantInfo1, false));
            }
         }
         // As Personal WL also has same viewKey,append Qualifier Id to make Unique
         // viewKey at parent Node
         viewKey = viewKey + participantInfo.getQualifiedId();
      }
      params.put(Query.class.getName(), query);
      params.put("participantInfo", participantInfo);
      params.put("id", participant.getQualifiedId());
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

   public String getActivityCount()
   {
      if (this.isLeaf())
      {
         Long totalCount = ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo);
         Long totalCountThreshold = ParticipantWorklistCacheManager.getInstance().getWorklistCountThreshold(
               participantInfo);
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
            count = count
                  + Integer.valueOf(((WorklistsTreeUserObject) childObjects.nextElement().getUserObject())
                        .getActivityCount());
         }
         return count.toString();
      }
   }
   
   public boolean isDisabled()
   {
      return disabled;
   }
}
