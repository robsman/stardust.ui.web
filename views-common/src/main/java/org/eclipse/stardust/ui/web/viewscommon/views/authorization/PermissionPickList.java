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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.dto.RuntimePermissionsDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteSelector.IAutocompleteSelectorListener;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
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
   private static final String EMPTY_STRING = "";
   private static final String GENERAL_PERMISSION = "generalPermissions";
   private static final String UI_PERMISSION = "uiPermissions";
   private final AdministrationService administrationService;
   private final DualListModel dualListModel;
   private final ParticipantAutocompleteSelector participantSelector;
   private ParticipantWrapper selectedParticipant;
   private PermissionsDetails permissions;
   private SelectInputText autoCompleteText;
   private boolean dirty;
   private boolean show;
   private Map<String, String> labels = new HashMap<String, String>();
   private Map<String, UiPermission> uiPermissionDefs;
   private List<FilterToolbarItem> filterToolbarItems;

   /**
    * Constructor
    */
   public PermissionPickList()
   {
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
               applyFilters();
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
      initializeFilters();
      
      show = false;
      resetPermissions();

      participantSelector.setSearchValue(EMPTY_STRING);

      if (autoCompleteText != null)
      {
         autoCompleteText.setValue(EMPTY_STRING);
      }

      dualListModel.clear();
   }

   public boolean isDirty()
   {
      return dirty;
   }

   public boolean isShow()
   {
      return show;
   }

   private void resetPermissions()
   {
      // UI permissions
      permissions = new PermissionsDetails(UiPermissionUtils.getAllPermissions(administrationService, true));

      // general Permissions
      RuntimePermissionsDetails runtimePermissionsDetails = (RuntimePermissionsDetails) administrationService
            .getGlobalPermissions();
      permissions.setGeneralPermission(runtimePermissionsDetails);
   }

   
   public void refresh()
   {
      resetPermissions();
      
      if (selectedParticipant != null)
      {
         initializeModel(selectedParticipant);
         applyFilters();
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
                           permissions.getGrants(permissionId));
                     participants.add((ModelParticipantInfo) selectedParticipant.getParticipantInfo());
                     permissions.setGrants(permissionId, participants);
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
               Set<ModelParticipantInfo> participants = permissions.getGrants(permissionId);

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
               permissions.setGrants(permissionId, participants);

            }
         }

         try
         {
            administrationService.setGlobalPermissions(permissions.getGeneralPermission());
            UiPermissionUtils.savePreferences(administrationService, permissions.getUIPermissionMap());   
            
            dirty = false;
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   /**
    * @param ae
    */
   public void togglePermissions(ActionEvent ae)
   {
     FilterToolbarItem.togglePermissions(filterToolbarItems, ae);
     applyFilters();
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
      Set<ModelParticipantInfo> participants = permissions.getGrants(permissionId);

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
    * @author Yogesh.Manware
    * @param participantWrapper
    */
   private void initializeModel(ParticipantWrapper participantWrapper)
   {
      if (null == participantWrapper)
      {
         return;
      }
      
      uiPermissionDefs = UiPermissionUtils.getUiPermssions();
 
      dualListModel.clear();
      
      List<String> allPermissionIds = new ArrayList<String>();
      allPermissionIds.addAll(permissions.getGeneralPermission().getAllPermissionIds());
      allPermissionIds.addAll(uiPermissionDefs.keySet());

      for (String permissionId : allPermissionIds)
      {
         String label = getLabel(permissionId);
         boolean addToSource = true;
         boolean hasAllGrant = permissions.hasAllGrant(permissionId);
         
         if (hasAllGrant)
         {
            dualListModel.getTarget().add(new SelectItemModel(label, permissionId, true));
            addToSource = false;
         }
         else
         {
            Set<ModelParticipantInfo> participants = permissions.getGrants(permissionId);
            for (ModelParticipantInfo participant : participants)
            {
               if (participant instanceof QualifiedModelParticipantInfo)
               {
                  if (compareFQID(participant, participantWrapper))
                  {
                     dualListModel.getTarget().add(new SelectItemModel(label, permissionId, false));
                     addToSource = false;
                     break;
                  }
               }
               else if (participant.getName().equals(participantWrapper.getID()))
               {
                  dualListModel.getTarget().add(new SelectItemModel(label, permissionId, false));
                  addToSource = false;
                  break;
               }
            }
         }
         if (addToSource)
         {
            dualListModel.getSource().add(new SelectItemModel(label, permissionId, false));
         }
      }
   }

   private void applyFilters()
   {
      dualListModel.setFilteredSource(filterItems(dualListModel.getSource()));
      dualListModel.setFilteredTarget(filterItems(dualListModel.getTarget()));
   }

   /**
    * @param list
    * @return
    */
   private List<SelectItemModel> filterItems(List<SelectItemModel> list)
   {
      List<SelectItemModel> filteredList = new LinkedList<SelectItemModel>();
      for (SelectItemModel item : list)
      {
         String permissionId = (String) item.getValue();
         if (UiPermissionUtils.isGeneralPermissionId(permissionId))
         {
            if (isGeneralPermissionSwitchOn())
            {
               filteredList.add(item);
            }
         }
         else
         {
            if (isUiPermissionSwitchOn())
            {
               filteredList.add(item);
            }
         }
      }
      return filteredList;
   }
   
   /**
    * @param permissionId
    * @return
    */
   private String getLabel(String permissionId)
   {
      if (!UiPermissionUtils.isGeneralPermissionId(permissionId))
      {
         return uiPermissionDefs.get(permissionId).getLabel();
      }
      else if (!labels.containsKey(permissionId))
      {
         labels.put(permissionId, UiPermissionUtils.getPermisionLabel(permissionId));
      }
      return labels.get(permissionId);
   }
   
   /**
    * @author Yogesh.Manware
    */
   private void initializeFilters()
   {
      filterToolbarItems = new ArrayList<FilterToolbarItem>();
      FilterToolbarItem genPermissionFilter = new FilterToolbarItem("0", GENERAL_PERMISSION,
            "views.authorizationManagerView.participantDualList.generalPermissions.show",
            "views.authorizationManagerView.participantDualList.generalPermissions.hide",
            "/plugins/views-common/images/icons/server_key.png");
      genPermissionFilter.setActive(true);
      filterToolbarItems.add(genPermissionFilter);

      FilterToolbarItem uiPermissionFilter = new FilterToolbarItem("0", UI_PERMISSION,
            "views.authorizationManagerView.participantDualList.uiPermissions.show",
            "views.authorizationManagerView.participantDualList.uiPermissions.hide",
            "/plugins/views-common/images/icons/computer_key.png");
      uiPermissionFilter.setActive(true);
      filterToolbarItems.add(uiPermissionFilter);
   }

   private boolean isGeneralPermissionSwitchOn()
   {
      return FilterToolbarItem.isSwitchOn(filterToolbarItems, GENERAL_PERMISSION);
   }
   
   private boolean isUiPermissionSwitchOn()
   {
      return FilterToolbarItem.isSwitchOn(filterToolbarItems, UI_PERMISSION);
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
   public List<FilterToolbarItem> getFilterToolbarItems()
   {
      return filterToolbarItems;
   }
}
