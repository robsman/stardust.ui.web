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
package org.eclipse.stardust.ui.web.viewscommon.views.authorization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.RuntimePermissions;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteSelector.IAutocompleteSelectorListener;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantAutocompleteSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;


import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * 
 * @author Vikas.Mishra
 * @version $Revision: $
 */
public class PermissionPickList
{
   private static final String PERMISSION_KEY_PREFIX = "views.authorizationManagerView.permission.model.";
   private static final String EMPTY_STRING = "";
   private final AdministrationService administrationService;
   private final DualListModel dualListModel;
   private MessagesViewsCommonBean viewsCommonBean;
   private final ParticipantAutocompleteSelector participantSelector;
   private ParticipantWrapper selectedParticipant;
   private RuntimePermissions runtimePermissions;
   private SelectInputText autoCompleteText;
   private boolean dirty;
   private boolean show;

   /**
    * Constructor
    */
   public PermissionPickList()
   {
      viewsCommonBean = MessagesViewsCommonBean.getInstance();
      dualListModel = new DualListModel();

      SessionContext sessionContext = SessionContext.findSessionContext();
      administrationService = sessionContext.getServiceFactory().getAdministrationService();

      participantSelector = new ParticipantAutocompleteSelector(new ParticipantsDataProvider(), true);
      participantSelector.setMaxRows(5);
      participantSelector.setSearchValue(EMPTY_STRING);

      participantSelector.setAutocompleteSelectorListener(new IAutocompleteSelectorListener()
      {
         public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
         {
            if (selectedItem.getValue() instanceof ParticipantWrapper) // Safety check
            {
               selectedParticipant = (ParticipantWrapper) selectedItem.getValue();
               initializeModel(selectedParticipant);
               autoCompleteText = autoComplete;
               show = true;
            }
            else
            {
               show = false;
            }
         }
      });
   }

   /**
    * Add action method
    * 
    * @param event
    */
   public void add(ActionEvent event)
   {
      dualListModel.add();
      dirty = true;
   }

   /**
    * AddAll action method
    * 
    * @param event
    */
   public void addAll(ActionEvent event)
   {
      dualListModel.addAll();
      dirty = true;
   }

   public DualListModel getDualListModel()
   {
      return dualListModel;
   }

   public String getParticipantLabel()
   {
      return (selectedParticipant != null) ? selectedParticipant.getText() : "";
   }

   public ParticipantAutocompleteSelector getParticipantSelector()
   {
      return participantSelector;
   }

   /**
    * method to initialize pick list
    */
   public void initialize()
   {
      show = false;
      runtimePermissions = administrationService.getGlobalPermissions();
      participantSelector.setSearchValue(EMPTY_STRING);

      if (autoCompleteText != null)
      {
         autoCompleteText.setValue(EMPTY_STRING);
      }

      dualListModel.getSource().clear();
      dualListModel.getTarget().clear();
   }

   public boolean isDirty()
   {
      return dirty;
   }

   public boolean isShow()
   {
      return show;
   }

   public void refresh()
   {
      runtimePermissions = administrationService.getGlobalPermissions();

      if (selectedParticipant != null)
      {
         initializeModel(selectedParticipant);
      }
   }

   /**
    * Remove action method
    * 
    * @param event
    */
   public void remove(ActionEvent event)
   {
      dualListModel.remove();
      dirty = true;
   }

   /**
    * Remove All action method
    * 
    * @param event
    */
   public void removeAll(ActionEvent event)
   {
      dualListModel.removeAll();
      dirty = true;
   }

   /**
    * method to reset pick list
    */
   public void reset()
   {
      dirty = false;

      if (autoCompleteText != null)
      {
         autoCompleteText.setValue(EMPTY_STRING);
      }

      participantSelector.setSearchValue(EMPTY_STRING);
      selectedParticipant = null;
   }

   private boolean compareFQID(ModelParticipantInfo participant, ParticipantWrapper participantWrapper)
   {
      QualifiedModelParticipantInfo qualifiedParticipant = (QualifiedModelParticipantInfo) participant;

      QualifiedModelParticipantInfo selectedQualifiedParticipant = (QualifiedModelParticipantInfo) participantWrapper
            .getParticipantInfo();

      if (qualifiedParticipant.getQualifiedId().equals(selectedQualifiedParticipant.getQualifiedId()))
      {
         return true;
      }
      return false;
   }

   /**
    * save action to save modified permissions for participant
    */
   public void save()
   {
      if (dirty)
      {
         try
         {
            // add participant
            for (SelectItemModel item : dualListModel.getTarget())
            {
               if (!item.isDisable())
               {
                  String permissionId = (String) item.getValue();

                  boolean hasParticipant = hasParticipant(permissionId, selectedParticipant);

                  if (!hasParticipant)
                  {
                     Set<ModelParticipantInfo> participants = new HashSet<ModelParticipantInfo>(
                           runtimePermissions.getGrants(permissionId));
                     participants.add((ModelParticipantInfo) selectedParticipant.getParticipantInfo());
                     runtimePermissions.setGrants(permissionId, participants);
                  }
               }
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }

         // remove participant
         for (SelectItemModel item : dualListModel.getSource())
         {
            if (!item.isDisable())
            {

               String permissionId = (String) item.getValue();
               Set<ModelParticipantInfo> participants = runtimePermissions.getGrants(permissionId);

               for (Iterator<ModelParticipantInfo> itr = participants.iterator(); itr.hasNext();)
               {
                  ModelParticipantInfo participant = itr.next();

                  if (participant instanceof QualifiedModelParticipantInfo)
                  {
                     if (compareFQID(participant, selectedParticipant))
                     {
                        itr.remove();
                        break;
                     }

                  }
                  else
                  {
                     if (participant.getId().equals(selectedParticipant.getID()))
                     {
                        itr.remove();
                        break;
                     }

                  }
               }
               runtimePermissions.setGrants(permissionId, participants);

            }
         }

         try
         {
            administrationService.setGlobalPermissions(runtimePermissions);
            dirty = false;
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   public void setDirty(boolean dirty)
   {
      this.dirty = dirty;
   }

   public void setSelectedParticipant(ParticipantWrapper selectedParticipant)
   {
      this.selectedParticipant = selectedParticipant;
   }

   public void setShow(boolean show)
   {
      this.show = show;
   }

   /**
    * 
    * @param permissionId
    * @return
    */
   private boolean hasParticipant(String permissionId, ParticipantWrapper participantWrapper)
   {
      Set<ModelParticipantInfo> participants = runtimePermissions.getGrants(permissionId);

      for (ModelParticipantInfo participant : participants)
      {
         if (participant instanceof QualifiedModelParticipantInfo)
         {

            if (compareFQID(participant, participantWrapper))
               ;
            {
               return true;
            }
         }
         else
         {
            if (participantWrapper.getID().equals(participant.getName()))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * 
    * @param participantId
    */
   private void initializeModel(ParticipantWrapper participantWrapper)
   {
      dualListModel.getSource().clear();
      dualListModel.getTarget().clear();

      List<String> tempList = new ArrayList<String>(runtimePermissions.getAllPermissionIds());

      for (String permissionId : runtimePermissions.getAllPermissionIds())
      {
         boolean hasAllGrant = runtimePermissions.hasAllGrant(permissionId);
         if (hasAllGrant)
         {
            dualListModel.getTarget().add(
                  new SelectItemModel(viewsCommonBean.getString(PERMISSION_KEY_PREFIX + permissionId), permissionId,
                        true));
            tempList.remove(permissionId);
         }
         else
         {

            Set<ModelParticipantInfo> participants = runtimePermissions.getGrants(permissionId);

            for (ModelParticipantInfo participant : participants)
            {

               if (participant instanceof QualifiedModelParticipantInfo)
               {
                  if (compareFQID(participant, participantWrapper))
                  {
                     dualListModel.getTarget().add(
                           new SelectItemModel(viewsCommonBean.getString(PERMISSION_KEY_PREFIX + permissionId),
                                 permissionId, false));
                     tempList.remove(permissionId);
                     break;
                  }

               }
               else if (participant.getName().equals(participantWrapper.getID()))
               {
                  dualListModel.getTarget().add(
                        new SelectItemModel(viewsCommonBean.getString(PERMISSION_KEY_PREFIX + permissionId),
                              permissionId, false));
                  tempList.remove(permissionId);
                  break;
               }
            }
         }
      }

      for (String permissionId : tempList)
      {
         dualListModel.getSource()
               .add(new SelectItemModel(viewsCommonBean.getString(PERMISSION_KEY_PREFIX + permissionId), permissionId,
                     false));
      }
   }

   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   class ParticipantsDataProvider implements IAutocompleteDataProvider
   {
      public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
      {
         List<SelectItem> selectItems = new ArrayList<SelectItem>();
         String regex = (!StringUtils.isEmpty(searchValue)) ? (searchValue.replaceAll("\\*", ".*") + ".*") : "";

         List<Participant> participantList=ParticipantUtils.getAllUnScopedModelParticipant(true);

         // Compile the pattern first as we are using this multiple times below in for
         // loop
         Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

         if (!StringUtils.isEmpty(regex))
         {
            for (Participant participant : participantList)
            {
               if (pattern.matcher(I18nUtils.getParticipantName(participant)).matches())
               {
                  selectItems.add(new SelectItem(new ParticipantWrapper(participant), I18nUtils
                        .getParticipantName(participant)));
               }
            }
         }
         else
         {
            for (Participant participant : participantList)
            {
               selectItems.add(new SelectItem(new ParticipantWrapper(participant), I18nUtils
                     .getParticipantName(participant)));
            }
         }

         return selectItems;
      }
   }
}
