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
package org.eclipse.stardust.ui.web.viewscommon.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantTree;
import org.eclipse.stardust.ui.web.viewscommon.participantManagement.ParticipantUserObject;

import com.icesoft.faces.component.ext.RowSelectorEvent;

public class SecurityAddParticipantDialog extends PopupUIComponentBean
{

   private static final long serialVersionUID = 5463043887750849872L;
   private static final String BEAN_NAME = "securityAddParticipantDialog";
   private static final Logger trace = LogManager.getLogger(SecurityAddParticipantDialog.class);
   private Map<String, QualifiedModelParticipantInfo> allParticipants;

   public static enum SELECTION_MODE {
      PICK_FROM_LIST, PICK_FROM_TREE
   };

   private SELECTION_MODE selectionMode = SELECTION_MODE.PICK_FROM_LIST;
   private ParticipantTree participantTree;
   private List<Participant> participants;
   private Participant selectedParticipant;
   private ParametricCallbackHandler parametricCallbackHandler;

   public SecurityAddParticipantDialog()
   {
      super("myDocumentsTreeView");
   }

   public static SecurityAddParticipantDialog getInstance()
   {
      return (SecurityAddParticipantDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   @Override
   public void initialize()
   {}

   public void initializeBean()
   {
      generateUserList();
      initializeParticipantTree();
   }

   @Override
   public void openPopup()
   {
      super.openPopup();
      addPopupCenteringScript();
      addPopupCenteringScript(true, getBeanId());
   }

   @Override
   public void closePopup()
   {
      super.closePopup();
      addPopupCenteringScript();
      addPopupCenteringScript(true, getBeanId());
   }

   public void addParticipant()
   {
      if (SELECTION_MODE.PICK_FROM_TREE.equals(selectionMode))
      {
         ParticipantUserObject userObj = participantTree.getSelectedUserObject();
         if (null != userObj)
         {
            if (null != userObj.getQualifiedModelParticipantInfo())
            {
               selectedParticipant = new Participant(userObj.getQualifiedModelParticipantInfo());
            }
            else if (null != userObj.getDepartment())
            {
               Department dept = userObj.getDepartment();
               selectedParticipant = new Participant(dept.getScopedParticipant(dept.getOrganization()));
            }
         }
      }
      closePopup();
      if (null != parametricCallbackHandler)
      {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("selectedParticipant", selectedParticipant);
         parametricCallbackHandler.setParameters(params);
         parametricCallbackHandler.handleEvent(EventType.APPLY);
      }
      selectedParticipant = null;
   }

   private void generateUserList()
   {
      participants = new ArrayList<Participant>();
      try
      {
         Participant everyone = new Participant(CommonProperties.EVERYONE);
         participants.add(everyone);
         for (Entry<String, QualifiedModelParticipantInfo> participantEntry : allParticipants.entrySet())
         {
            Participant p = new Participant(participantEntry.getValue());
            participants.add(p);
         }

         Collections.sort(participants);
      }
      catch (ClassCastException e)
      {
         trace.error(e);
      }
      catch (Exception ex)
      {
         trace.error(ex);
      }
   }

   private void initializeParticipantTree()
   {
      participantTree = new ParticipantTree();
      participantTree.setShowUserNodes(false);
      participantTree.setShowUserGroupNodes(false);
      participantTree.setHighlightUserFilterEnabled(false);
      participantTree.initialize();
   }

   /**
    * @param allParticipants
    */
   public void setAllParticipants(Map<String, QualifiedModelParticipantInfo> allParticipants)
   {
      this.allParticipants = allParticipants;
   }

   public Participant getSelectedParticipant()
   {
      return selectedParticipant;
   }

   public void setSelectedParticipant(Participant selectedParticipant)
   {
      this.selectedParticipant = selectedParticipant;
   }

   public void onRowSelection(RowSelectorEvent re)
   {
      Participant selectedItem = participants.get(re.getRow());
      this.selectedParticipant = selectedItem;
   }

   /**
    * @param parametricCallbackHandler
    *           the parametricCallbackHandler to set
    */
   public void setParametricCallbackHandler(ParametricCallbackHandler parametricCallbackHandler)
   {
      this.parametricCallbackHandler = parametricCallbackHandler;
   }

   public void setPickFromTreeMode()
   {
      this.selectionMode = SELECTION_MODE.PICK_FROM_TREE;
   }

   public void setPickFromListMode()
   {
      this.selectionMode = SELECTION_MODE.PICK_FROM_LIST;
   }

   public boolean isPickFromTreeMode()
   {
      return SELECTION_MODE.PICK_FROM_TREE.equals(selectionMode);
   }

   public boolean isPickFromListMode()
   {
      return SELECTION_MODE.PICK_FROM_LIST.equals(selectionMode);
   }

   public ParticipantTree getParticipantTree()
   {
      return participantTree;
   }

   public List<Participant> getParticipants()
   {
      return participants;
   }
}