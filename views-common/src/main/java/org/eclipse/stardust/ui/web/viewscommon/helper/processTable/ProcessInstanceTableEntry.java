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
package org.eclipse.stardust.ui.web.viewscommon.helper.processTable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ContextKind;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLink;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ProcessInstanceTableEntry extends DefaultRowModel
{  
   private static final long serialVersionUID = 1L;
   private static final int TEXT_PREVIEW_LENGTH = 20;

   private String processInstanceName;

   private long processInstanceRootOID;

   private long processInstanceOID;

   private int priority;

   private int oldPriority;

   private Date startTime;

   private Date endTime;

   private String startingUser;

   private String status;

   private String duration;
   

   private List<ProcessDescriptor> processDescriptorsList;

   private boolean checkSelection;

   private int notesCount;

   private ProcessInstance processInstance;

   private boolean enableTerminate;

   private boolean enableRecover;
   
   private boolean modifyProcessInstance;

   private Map<String, Object> descriptorValues;

   private String linkType;

   private Date createDate;

   private String createUser;

   private String notePreview;

   private NoteInfo noteInfo;
   
   private boolean caseInstance;
   
   private String caseOwner;   


   
   
   
   /**
    * 
    * @param processInstance
    */
   public ProcessInstanceTableEntry(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
      
      ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      
      this.processInstanceRootOID = processInstance.getRootProcessInstanceOID();
      this.processInstanceOID = processInstance.getOID();
      this.priority = processInstance.getPriority();
      this.startTime = processInstance.getStartTime();
      this.duration = ProcessInstanceUtils.getDuration(processInstance);
      this.processInstanceName = I18nUtils.getProcessName(processDefinition);
      this.createUser = UserUtils.getUserDisplayLabel(processInstance.getStartingUser());
      this.descriptorValues = ((ProcessInstanceDetails) processInstance).getDescriptors();
      this.processDescriptorsList = getProcessDescriptor(processInstance, processDefinition);
      
      this.endTime = processInstance.getTerminationTime();
      this.startingUser = UserUtils.getUserDisplayLabel(processInstance.getStartingUser());
      this.status = ProcessInstanceUtils.getProcessStateLabel(processInstance);
      this.enableTerminate = ProcessInstanceUtils.isAbortable(processInstance);
      this.enableRecover = true;
      this.checkSelection = false;
      this.modifyProcessInstance = AuthorizationUtils.hasPIModifyPermission(processInstance);
      
      List<Note> notes=ProcessInstanceUtils.getNotes(processInstance);
      if(null!=notes)
      {
         this.notesCount = notes.size();   
      }
      this.caseInstance = processInstance.isCaseProcessInstance();
      if (caseInstance)
      {
         caseOwner=ProcessInstanceUtils.getCaseOwnerName(processInstance);
      }      
    
      oldPriority = priority;
   }


   /**
    * Process Link information is set along with other details from above process
    * 
    * @param linkType
    * @param createDate
    * @param createUser
    * @param comment
    * @param noteInfo
    */
   public ProcessInstanceTableEntry(ProcessInstance processInstance, ProcessInstanceLink processInstanceLink)
   {
      this(processInstance);
      initProcessInstanceLink(processInstanceLink);
   }

   /**
    * 
    * @param processInstanceLink
    */
   public void initProcessInstanceLink(ProcessInstanceLink processInstanceLink)
   {
      this.createDate = processInstanceLink.getCreateTime();
      linkType = getLinkType(processInstanceLink, processInstance);

      // NotesInfo populated to display the LinkComment
      this.noteInfo = new NoteInfo(processInstanceLink.getComment(), UserUtils.getUserDisplayLabel(processInstance
            .getStartingUser()), DateUtils.formatDateTime(processInstanceLink.getCreateTime()), linkType);

      if (StringUtils.isNotEmpty(processInstanceLink.getComment()))
      {
         this.noteInfo.userImageURL=MyPicturePreferenceUtils.getUsersImageURI(processInstance.getStartingUser());
         this.notePreview = processInstanceLink.getComment().substring(0,
               Math.min(processInstanceLink.getComment().length(), TEXT_PREVIEW_LENGTH));
         if (notePreview.length() < processInstanceLink.getComment().length())
         {
            notePreview += " ...";
         }
      }

   }
   
   
   /**
    * 
    */
   public ProcessInstanceTableEntry()
   {
      // TODO Auto-generated constructor stub
   }

   public boolean isEnableTerminate()
   {
      return enableTerminate;
   }

   public boolean isEnableRecover()
   {
      return enableRecover;
   }

   public boolean isCheckSelection()
   {
      return checkSelection;
   }

   public void setCheckSelection(boolean checkSelection)
   {
      this.checkSelection = checkSelection;
   }

   public String getProcessInstanceName()
   {
      return processInstanceName;
   }

   public void setProcessInstanceName(String processInstanceName)
   {
      this.processInstanceName = processInstanceName;
   }

   public long getProcessInstanceRootOID()
   {
      return processInstanceRootOID;
   }

   public void setProcessInstanceRootOID(long processInstanceRootOID)
   {
      this.processInstanceRootOID = processInstanceRootOID;
   }

   public long getProcessInstanceOID()
   {
      return processInstanceOID;
   }

   public void setProcessInstanceOID(long processInstanceOID)
   {
      this.processInstanceOID = processInstanceOID;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setPriority(int priority)
   {
      this.priority = priority;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public void setStartTime(Date startTime)
   {
      this.startTime = startTime;
   }

   public String getDuration()
   {
      return duration;
   }

   public void setDuration(String duration)
   {
      this.duration = duration;
   }

   public boolean isPriorityChanged()
   {
      if (this.oldPriority != this.priority)
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   public List<ProcessDescriptor> getProcessDescriptorsList()
   {
      return processDescriptorsList;
   }

   public void setProcessDescriptorsList(List<ProcessDescriptor> processDescriptorsList)
   {
      this.processDescriptorsList = processDescriptorsList;
   }

   public Date getEndTime()
   {
      return endTime;
   }

   public String getStartingUser()
   {
      return startingUser;
   }

   public String getStatus()
   {
      return status;
   }

   public int getNotesCount()
   {
      return notesCount;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public int getOldPriority()
   {
      return oldPriority;
   }

   @Override
   public String toString()
   {
      return "ProcessInstance: " + processInstanceName;
   }

   public Map<String, Object> getDescriptorValues()
   {
      return descriptorValues;
   }

   public void setDescriptorValues(Map<String, Object> descriptorValues)
   {
      this.descriptorValues = descriptorValues;
   }

   public boolean isModifyProcessInstance()
   {
      return modifyProcessInstance;
   }

   public String getLinkType()
   {
      return linkType;
   }

   public Date getCreateDate()
   {
      return createDate;
   }

   public String getCreateUser()
   {
      return createUser;
   }

   public String getNotePreview()
   {
      return notePreview;
   }

   public NoteInfo getNoteInfo()
   {
      return noteInfo;
   }

   public boolean isCaseInstance()
   {
      return caseInstance;
   }

   public String getCaseOwner()
   {
      return caseOwner;
   }
   
   /*
    * 
    */
   private List<ProcessDescriptor> getProcessDescriptor(ProcessInstance processInstance,
         ProcessDefinition processDefinition)
   {
      List<ProcessDescriptor> processDescriptorsList = null;
      if (processDefinition != null)
      {
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
         Map<String, Object> descriptorValues = processInstanceDetails.getDescriptors();
         
         if (processInstance.isCaseProcessInstance())
         {
            processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                  processInstanceDetails.getDescriptorDefinitions(), descriptorValues, processDefinition, true);
         }
         else
         {
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(
                  processInstanceDetails.getDescriptors(), processDefinition, true);
         }
      }
      else
      {
         processDescriptorsList = CollectionUtils.newArrayList();
      }
      return processDescriptorsList;
   }
   
   /**
    * 
    * @param link
    * @param processInstance
    * @return
    */
   private String getLinkType(ProcessInstanceLink link, ProcessInstance processInstance)
   {
      String linkType = null;

      String JOIN_TO = MessagesViewsCommonBean.getInstance().getString("views.joinProcessDialog.label.join_to");
      String SWITCH_TO = MessagesViewsCommonBean.getInstance().getString("view.linkedProcess.label.switch_to");
      String JOIN_FROM = MessagesViewsCommonBean.getInstance().getString("views.joinProcessDialog.label.join_from");
      String SWITCH_FROM = MessagesViewsCommonBean.getInstance().getString("view.linkedProcess.label.switch_from");
      String MIGRATE_TO = MessagesViewsCommonBean.getInstance().getString("view.linkedProcess.label.migrate_to");
      String MIGRATE_FROM = MessagesViewsCommonBean.getInstance().getString("view.linkedProcess.label.migrate_from");

      long sourceLinkOID = link.getSourceOID();
      long targetLinkOID = link.getTargetOID();
      // If processOID of current process is Sources for LinkedProcess,
      // then use the source Process linked information
      if (processInstance.getOID() == sourceLinkOID)
      {
         if (PredefinedProcessInstanceLinkTypes.UPGRADE.getId().equals(link.getLinkType().getId()))
         {
            linkType = MIGRATE_FROM;
         }
         else
            // If the link type is Join , set the To Process Link Type
            linkType = PredefinedProcessInstanceLinkTypes.JOIN.getId().equals(link.getLinkType().getId()) ? JOIN_FROM : SWITCH_FROM;
      }
      else if (processInstance.getOID() == targetLinkOID)
      {
         if (PredefinedProcessInstanceLinkTypes.UPGRADE.getId().equals(link.getLinkType().getId()))
         {
            linkType = MIGRATE_TO;
         }
         else
            linkType = PredefinedProcessInstanceLinkTypes.JOIN.getId().equals(link.getLinkType().getId()) ? JOIN_TO : SWITCH_TO;
      }
      return linkType;

   }
   public void applyChanges()
   {
      this.oldPriority = priority;
   }
   
   /**
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   public static class NoteInfo implements Serializable
   {
      private final static long serialVersionUID = 1l;

      private String text;

      private String user;

      private String timeStamp;

      private Date timeStampAsDate;

      private String title;

      private String scopeType;

      private boolean readOnly = true;
      
      private String type;
      
      private String userImageURL;

      public NoteInfo(Note note)
      {
         text = note.getText();
         User u = note.getUser();
         user = u != null ? u.getFirstName() + " " + u.getLastName() : null;
         user = user != null && user.length() >= 30
               ? user.substring(0, 29) + "..."
               : user;

         if(note.getTimestamp() != null)
         {
            timeStamp = org.eclipse.stardust.ui.web.common.util.DateUtils.formatDateTime(note.getTimestamp());
            timeStampAsDate = note.getTimestamp();
         }
         title = note.getText() != null && note.getText().length() >= 30 ? note.getText()
               .substring(0, 29)
               + "..." : note.getText();
         RuntimeObject contextObject = note.getContextObject();
         ContextKind context = note.getContextKind();
         if (contextObject == null)
         {
            scopeType = null;
         }
         else if (ContextKind.ActivityInstance.equals(context))
         {
            Activity activity = ((ActivityInstance) contextObject).getActivity();
            scopeType = Localizer.getString(LocalizerKey.ACTIVITY) + " "
                  + I18nUtils.getActivityName(activity);
         }
         else
         {
            Converter converter = FacesContext.getCurrentInstance().getApplication()
                  .createConverter("modelElementLabelProvider");//TODO modelElementLabelProvider is removed
            scopeType = Localizer.getString(LocalizerKey.PROCESS_DEFINITION)
                  + " "
                  + converter.getAsString(FacesContext.getCurrentInstance(), null, contextObject);

         }
      }
      
      public NoteInfo(String text,String user, String timeStamp, String type)
      {
         this.text = text;
         this.user = user;
         this.timeStamp = timeStamp;
         this.type = type;
      }



      public NoteInfo(boolean readOnly)
      {
         this.readOnly = readOnly;
      }

      public String getText()
      {
         return text;
      }

      public void setText(String text)
      {
         this.text = text;
      }

      public String getUser()
      {
         return user;
      }

      public String getTimeStamp()
      {
         return timeStamp;
      }

      public Date getTimeStampAsDate()
      {
         return timeStampAsDate;
      }

      public String getTitle()
      {
         return title;
      }

      public String getScopeType()
      {
         return scopeType;
      }

      public boolean isReadOnly()
      {
         return readOnly;
      }

      public void setReadOnly(boolean readOnly)
      {
         this.readOnly = readOnly;
      }

      public String getType()
      {
         return type;
      }

      public String getUserImageURL()
      {
         return userImageURL;
      }
      
      
   }

}
