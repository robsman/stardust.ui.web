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
package org.eclipse.stardust.ui.web.viewscommon.user;

import org.eclipse.stardust.engine.api.dto.DepartmentDetails;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;



/**
 * @author Shrikant.Gangal
 *
 */
public class ParticipantWrapper extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private static final String BASE_IMAGE_PATH = "/plugins/processportal/images/icons/";
   private Participant participant;
   private DepartmentInfo department;
   private boolean onlineStatus = false;
   private ParticipantAutocompleteSelector autocompleteParticipantSelector;
   private boolean isRemoveable = true;

   /**
    * @param participant
    */
   public ParticipantWrapper(Participant participant)
   {
      initialize();
      this.participant = participant;
   }

   /**
    * @param department
    */
   public ParticipantWrapper(DepartmentInfo department)
   {
      initialize();
      this.department = department;
   }

   /**
    * @param participant
    */
   public ParticipantWrapper(Participant participant, boolean online)
   {
      initialize();
      this.participant = participant;
      this.onlineStatus = online;
   }
   
   /**
    * 
    */
   private void initialize()
   {}
   
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.user.IParticipantWrapper#getIcon()
    */
   public String getIcon()
   {
      if (null != participant)
      {
         if (participant instanceof User)
         {
            return MyPicturePreferenceUtils.getUsersImageURI((User) participant);
         }
         else if (participant instanceof Role)
         {
            return BASE_IMAGE_PATH + "role.png";
         }
         else
         {
            return BASE_IMAGE_PATH + "organization.png";
         }
      }
      else
      {
         return BASE_IMAGE_PATH + "department.png";
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.user.IParticipantWrapper#getObject()
    */
   public Object getObject()
   {
      // TODO Auto-generated method stub
      if (participant != null)
      {
         return participant;
      }
      else
      {
         return department;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.user.IParticipantWrapper#getText()
    */
   public String getText()
   {
      if (department != null)
      {
         return getDepartmentLabel(department);
      }
      else
      {
         return getParticipantLabel(participant);
      }
   }

   /**
    * return participant Label
    * 
    * @param participant
    * @return
    */
   public static String getParticipantLabel(Participant participant)
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      if (participant != null)
      {
         if (participant instanceof User)
         {
            User user = (User) participant;
            return I18nUtils.getUserLabel(user);
         }
         if (participant instanceof Role)
         {
            return I18nUtils.getParticipantName(participant) + " ("
                  + propsBean.getString("delegation.search.roleNamePostFix") + ")";
         }
         if (participant instanceof Organization)
         {
            return I18nUtils.getParticipantName(participant) + " ("
                  + propsBean.getString("delegation.search.organizationNamePostFix") + ")";
         }
      }
      return "";
   }

   /**
    * return department Label
    * 
    * @param department
    * @return
    */
   public static String getDepartmentLabel(DepartmentInfo department)
   {
      StringBuilder departmentName = new StringBuilder().append(department.getName()).append(" (")
            .append(MessagesViewsCommonBean.getInstance().getString("delegation.search.departmentNamePostFix"))
            .append(")");

      if (department instanceof DepartmentDetails)
      {
         DepartmentDetails deptDetail = (DepartmentDetails) department;
         if (null != deptDetail.getOrganization() && deptDetail.getOrganization().isDepartmentScoped())
         {
            String orgName = I18nUtils.getParticipantName(deptDetail.getOrganization());
            departmentName.insert(0, orgName + " - ");
         }
      }

      return departmentName.toString();
   }

   /**
    * @return
    */
   public long getOID()
   {
      if (participant != null)
      {
         if (participant instanceof User)
         {
            User u = (User) participant;
            return u.getOID();
         }
         else if (participant instanceof Role)
         {
            return ((Role) participant).getElementOID();
         }
         else if (participant instanceof Organization)
         {
            return ((Organization) participant).getRuntimeElementOID();
         }
         else
         {
            return -1l;
         }
      }
      else
      {
         return department.getOID();
      }
   }
   
   /**
    * @return
    */
   public ParticipantInfo getParticipantInfo()
   {
      return participant;
   }

   /**
    * @return
    */
   public DepartmentInfo getDeparment()
   {
      return department;
   }
   
   /**
    * @return
    */
   public String getID()
   {
      if (participant != null)
      {
        return participant.getId();
      }
      else
      {
         return department.getId();
      }
   }

   /**
    * @return
    */
   public boolean isParticipantAUser()
   {
      if (participant != null && participant instanceof User)
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public String getUserImageURL()
   {

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.user.IParticipantWrapper#isOnline()
    */
   public boolean isOnline()
   {

      return onlineStatus;
   }
   

   public ParticipantAutocompleteSelector getAutocompleteParticipantSelector()
   {
      return autocompleteParticipantSelector;
   }

   public void setAutocompleteParticipantSelector(ParticipantAutocompleteSelector autocompleteUserSelector)
   {
      this.autocompleteParticipantSelector = autocompleteUserSelector;
   }

   public boolean isRemoveable()
   {
      return isRemoveable;
   }

   public void setRemoveable(boolean isRemoveable)
   {
      this.isRemoveable = isRemoveable;
   }
   
   public void removeParticipant() {
      autocompleteParticipantSelector.removeSelectedparticipant(this);
   }
   
   /**
    * @return
    */
   public boolean isUser() {
      if (null != participant && participant instanceof User) {
         return true;
      }
      
      return false;
   }
   
   /**
    * @return
    */
   public boolean isRole() {
      if (null != participant && participant instanceof Role) {
         return true;
      }
      
      return false;
   }
   
   /**
    * @return
    */
   public boolean isOrganization() {
      if (null != participant && participant instanceof Organization) {
         return true;
      }
      
      return false;
   }
   
   /**
    * @return
    */
   public boolean isDepartment() {
      if (null != department) {
         return true;
      }
      
      return false;
   }

   @Override
   public boolean equals(Object equateTo)
   {
      Object obj = ((ParticipantWrapper) equateTo).getObject();
      if (participant != null && obj != null)
      {
         if (participant instanceof User)
         {
            if (!(obj instanceof User)) {
               return false;
            }
            
            return ((User) participant).getAccount().equals(((User) obj).getAccount()); 
         }
         if (participant instanceof Role)
         {
            if (!(obj instanceof Role)) {
               return false;
            }
            
            return ((Role) participant).getId().equals(((Role) obj).getId());
         }
         if (participant instanceof Organization)
         {
            if (!(obj instanceof Organization)) {
               return false;
            }
            
            return ((Organization) participant).getId().equals(((Organization) obj).getId());
         }
      }
      else if (department != null && obj != null)
      {
         if (!(obj instanceof DepartmentInfo)) {
            return false;
         }
         
         return ((DepartmentInfo) participant).getId().equals(((DepartmentInfo) obj).getId());
      }

      return super.equals(obj);
   }
}