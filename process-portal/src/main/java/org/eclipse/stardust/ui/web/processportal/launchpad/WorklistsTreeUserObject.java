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

import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
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
      params.put(Query.class.getName(), ParticipantWorklistCacheManager.getInstance().getWorklistQuery(participantInfo));
      params.put("participantInfo", participantInfo);
      params.put("id", participant.getQualifiedId());
      String name = ModelHelper.getParticipantName(participantInfo);
      params.put("name", name);
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
      this.setText(ModelHelper.getParticipantName(participantInfo) + ": ");
      this.setLeafIcon(PPUtils.getParticipantIcon(participantInfo));
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

   public long getActivityCount()
   {
      return ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo);
   }

   public boolean isDisabled()
   {
      return disabled;
   }
}
