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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry;
import org.eclipse.stardust.engine.api.runtime.AccessControlEntry.EntryType;
import org.eclipse.stardust.engine.api.runtime.AccessControlPolicy;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Privilege;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.services.ContextPortalServices;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;


public class SecurityDialog extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(SecurityDialog.class);
   private List<AccessControlBean> accessControlBean;
   private List<AccessControlBean> accessControlBeanInherited;
   private String resourceName;
   private String resourceId;
   private boolean isLeaf;
   private SortableTable<AccessControlBean> securityDialogTable;
   private SortableTable<AccessControlBean> securityDialogInheritedTable;
   private boolean expanded;
   private boolean policyChanged = false;
   private boolean allInheritPolicy = true;
   private Map<String, QualifiedModelParticipantInfo> allParticipants;
 
   
   public List<SelectItem> getPermission()
   {
      List<SelectItem> list = new ArrayList<SelectItem>();
      list.add(new SelectItem(AccessControlBean.INHERIT,getMessages().getString("securityDialog.columnValue.inherit")));
      list.add(new SelectItem(AccessControlBean.ALLOW,getMessages().getString("securityDialog.columnValue.allow")));
      list.add(new SelectItem(AccessControlBean.DENY,getMessages().getString("securityDialog.columnValue.deny")));
      return list;
   }

   public List<AccessControlBean> getAccessControlBean()
   {
      return accessControlBean;
   }

   public String getResourceName()
   {
      return resourceName;
   }

   public void setResourceName(String resourceName)
   {
      this.resourceName = resourceName;
   }

   public String getResourceId()
   {
      return resourceId;
   }

   public void setResourceId(String resourceId)
   {
      this.resourceId = resourceId;
   }

   public SecurityDialog()
   {
      super("myDocumentsTreeView");
      accessControlBean = new ArrayList<AccessControlBean>();
      accessControlBeanInherited = new ArrayList<AccessControlBean>();
      initialize();
   }

   

   public int generateData()
   {
      FacesContext context = FacesContext.getCurrentInstance();

      String resourceName = (String) context.getExternalContext().getRequestParameterMap().get(
            CommonProperties.RESOURCE_NAME);
      String resourceId = (String) context.getExternalContext().getRequestParameterMap().get(
            CommonProperties.RESOURCE_ID);
      String resourceType = (String) context.getExternalContext().getRequestParameterMap().get(
            CommonProperties.IS_LEAF_NODE);

      if (StringUtils.isNotEmpty(resourceName) && StringUtils.isNotEmpty(resourceId))
      {
         if (StringUtils.isNotEmpty(resourceType) && Boolean.valueOf(resourceType))
         {
            setLeaf(true);
         }else{
            setLeaf(false);
         }
         setResourceName(resourceName);
         setResourceId(resourceId);
      }
      if (getResourceId() == null || getResourceId().equals("null"))
      {
         MessageDialog.addErrorMessage(this.getMessages().getString("securityDialog.configurationFolderException"));
         return 1;
      }
      allParticipants = getAllParticipant();
      generatePermissions();
      return 0;
   }

   public void open()
   {
      if (DMSHelper.isSecurityEnabled())
      {
         if (generateData() != 0)
         {
            return;
         }
         initialize();
         securityDialogTable.setList(accessControlBean);
         securityDialogTable.initialize();
         securityDialogInheritedTable.setList(accessControlBeanInherited);
         securityDialogInheritedTable.initialize();
         super.openPopup();
      }
      else
      {
         MessageDialog.addErrorMessage(this.getMessages().getString("securityDialog.configurationException"));
      }
   }

   

   public void addParticipant()
   {
      SecurityAddParticipantDialog addParticipantDialog = SecurityAddParticipantDialog.getInstance();
      addParticipantDialog.setAllParticipants(allParticipants);
      addParticipantDialog.initializeBean();
      addParticipantDialog.setParametricCallbackHandler(new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            addParticipant((Participant) getParameters().get("selectedParticipant"));
         }
      });
      addParticipantDialog.openPopup();
   }

   /**
    * @param selectedParticipant
    */
   protected void addParticipant(Participant selectedParticipant)
   {
      AccessControlBean acb = new AccessControlBean(selectedParticipant, AccessControlBean.INHERIT,
            AccessControlBean.INHERIT, AccessControlBean.INHERIT, AccessControlBean.INHERIT, AccessControlBean.INHERIT,
            AccessControlBean.INHERIT);
      if (!getAccessControlBean().contains(acb))
      {
         acb.setEdit(true);
         getAccessControlBean().add(acb);
         securityDialogTable.setList(accessControlBean);
         securityDialogTable.initialize();
      }
   }

   public void generatePermissions()
   {
      accessControlBean = new ArrayList<AccessControlBean>();
      accessControlBeanInherited = new ArrayList<AccessControlBean>();
      try
      {
         String resourceId = getResourceId();
         DocumentManagementService dms = ContextPortalServices.getDocumentManagementService();
         if (resourceId == null || resourceId.equals(""))
            return;

         Set<AccessControlPolicy> policies = dms.getPolicies(resourceId);
         accessControlBean.addAll(generateACBList(policies, false));

         Set<AccessControlPolicy> effectivePolicies = dms.getEffectivePolicies(resourceId);
         accessControlBeanInherited.addAll(generateACBList(effectivePolicies, true));
      }
      catch (Exception e)
      {
         trace.error(e);
      }
   }

   public List<AccessControlBean> generateACBList(Set<AccessControlPolicy> policies, boolean inheritedList)
   {
      Map<String,AccessControlBean> participantAccessControlMap = CollectionUtils.newHashMap();
      List<AccessControlBean> acbs = new ArrayList<AccessControlBean>();
      Iterator<AccessControlPolicy> effectivePoliciesIter = policies.iterator();
      AccessControlBean acb = null;
      boolean updateACBList = true;
      while (effectivePoliciesIter.hasNext())
      {
         AccessControlPolicy acp = effectivePoliciesIter.next();
         Iterator<AccessControlEntry> aceIter = acp.getAccessControlEntries().iterator();
         while (aceIter.hasNext())
         {
            AccessControlEntry ace = aceIter.next();
            try
            {
               if (null != acb && participantAccessControlMap.containsKey(ace.getPrincipal().getName()))
               {
                  acb = participantAccessControlMap.get(ace.getPrincipal().getName());
                  updateACBList = false;
               }
               else
               {
                  acb = new AccessControlBean(new Participant(ace.getPrincipal(), allParticipants.get(ace
                        .getPrincipal().getName())));
                  participantAccessControlMap.put(acb.getParticipant().getPrincipal().getName(),acb);
                  updateACBList = true;
               }
            }
            catch (Exception e)
            {
               acb = new AccessControlBean(new Participant(ace.getPrincipal().getName()));
               trace.debug("Error occurred while creating permissions: " + ace.getPrincipal().getName());
            }

            Set<Privilege> privileges = ace.getPrivileges();
            Iterator<Privilege> iter = privileges.iterator();
            while (iter.hasNext())
            {
               Privilege privilege = iter.next();
               if (privilege.equals(DmsPrivilege.ALL_PRIVILEGES))
               {
                  acb.setAllPrivilege();
               }
               else
               {
                  if (privilege.equals(DmsPrivilege.CREATE_PRIVILEGE))
                  {
                     acb.setCreate(true == ace.getType().toString().equals(AccessControlBean.ALLOW.toUpperCase())
                           ? AccessControlBean.ALLOW
                           : AccessControlBean.DENY);
                  }
                  if (privilege.equals(DmsPrivilege.DELETE_PRIVILEGE))
                  {
                     acb.setDelete(ace.getType().toString().equals(AccessControlBean.ALLOW.toUpperCase())
                           ? AccessControlBean.ALLOW
                           : AccessControlBean.DENY);
                  }
                  if (privilege.equals(DmsPrivilege.MODIFY_PRIVILEGE))
                  {
                     acb.setModify(ace.getType().toString().equals(AccessControlBean.ALLOW.toUpperCase())
                           ? AccessControlBean.ALLOW
                           : AccessControlBean.DENY);
                  }
                  if (privilege.equals(DmsPrivilege.READ_PRIVILEGE))
                  {
                     acb.setRead(ace.getType().toString().equals(AccessControlBean.ALLOW.toUpperCase())
                           ? AccessControlBean.ALLOW
                           : AccessControlBean.DENY);
                  }
                  if (privilege.equals(DmsPrivilege.READ_ACL_PRIVILEGE))
                  {
                     acb.setReadAcl(ace.getType().toString().equals(AccessControlBean.ALLOW.toUpperCase())
                           ? AccessControlBean.ALLOW
                           : AccessControlBean.DENY);
                  }
                  if (privilege.equals(DmsPrivilege.MODIFY_ACL_PRIVILEGE))
                  {
                     acb.setModifyAcl(ace.getType().toString().equals(AccessControlBean.ALLOW.toUpperCase())
                           ? AccessControlBean.ALLOW
                           : AccessControlBean.DENY);
                  }
               }
            }
            acb.setEdit(false);
            if (inheritedList)
            {
               if (!checkIfInherited(acb) && updateACBList)
               {
                  acbs.add(acb);
               }
            }
            else
            {
               if (updateACBList)
               {
                  // While removing ACB entry, if saved flag is false, means new entry
                  // (not saved to JCR)
                  acb.setSaved(true);
                  acbs.add(acb);
               }
            }
         }
      }
      return acbs;
   }
   
   /**
    * @param acb
    * @return
    */
   private boolean checkIfInherited(AccessControlBean acb)
   {
      for (AccessControlBean acb1 : accessControlBean)
      {
         if (acb1.getParticipant().equals(acb.getParticipant()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * When all the permission's are Inherit, no need to show APPLY btn
    * 
    * @param acb
    * @return
    */
   private boolean checkAllRolesInherit(AccessControlBean acb)
   {
      if (!isLeaf() && !(AccessControlBean.INHERIT.equals(acb.getCreate())))
         return false;
      else if (!AccessControlBean.INHERIT.equals(acb.getRead()))
         return false;
      else if (!AccessControlBean.INHERIT.equals(acb.getModify()))
         return false;
      else if (!AccessControlBean.INHERIT.equals(acb.getDelete()))
         return false;
      else if (!AccessControlBean.INHERIT.equals(acb.getReadAcl()))
         return false;
      else if (!AccessControlBean.INHERIT.equals(acb.getModifyAcl()))
         return false;
      return true;
   }
   
   public void save()
   {
      apply();
      super.closePopup();
   }

   public void apply()
   {
      for (int i = 0; i < accessControlBean.size(); i++)
      {
         AccessControlBean objAcb = accessControlBean.get(i);
         if (checkAllRolesInherit(objAcb))
         {
            allInheritPolicy = true;
            return;
         }
      }
      DocumentManagementService dms = ContextPortalServices.getDocumentManagementService();
      // TODO Delete the print line after perfecting the functionality
      // printDmsSecurity(dms, getResourceId());
      AccessControlPolicy next = getAccessControlPolicy();
      next.removeAllAccessControlEntries();

      for (AccessControlBean acb : accessControlBean)
      {
         if (CommonProperties.ADMINISTRATOR.equals(acb.getParticipant().getId()))
         {
            acb.setEdit(false);
            continue;
         }
         if (next != null)
         {
            acb.setEdit(false);
            // While removing ACB entry, if saved flag is false, means new entry (not
            // saved to JCR)
            acb.setSaved(true);
            if (!acb.getCreate().equals(AccessControlBean.INHERIT))
            {
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.CREATE_PRIVILEGE), acb.getCreate().toUpperCase()
                           .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
            }
            if (!acb.getDelete().equals(AccessControlBean.INHERIT))
            {
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.DELETE_PRIVILEGE), acb.getDelete().toUpperCase()
                           .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.DELETE_CHILDREN_PRIVILEGE), acb.getDelete()
                           .toUpperCase().equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
            }
            if (!acb.getModify().equals(AccessControlBean.INHERIT))
            {
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.MODIFY_PRIVILEGE), acb.getModify().toUpperCase()
                           .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
            }
            if (!acb.getRead().equals(AccessControlBean.INHERIT))
            {
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.READ_PRIVILEGE), acb.getRead().toUpperCase()
                           .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
            }
            if (!acb.getReadAcl().equals(AccessControlBean.INHERIT))
            {
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.READ_ACL_PRIVILEGE), acb.getReadAcl().toUpperCase()
                           .equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
            }
            if (!acb.getModifyAcl().equals(AccessControlBean.INHERIT))
            {
               next.addAccessControlEntry(acb.getParticipant().getPrincipal(),
                     Collections.<Privilege> singleton(DmsPrivilege.MODIFY_ACL_PRIVILEGE), acb.getModifyAcl()
                           .toUpperCase().equals(EntryType.ALLOW.toString()) ? EntryType.ALLOW : EntryType.DENY);
            }
         }
      }
      dms.setPolicy(getResourceId(), next);
      securityDialogTable.setList(accessControlBean);
      securityDialogTable.initialize();
      //reset flag
      policyChanged = false;
     
      // TODO Delete the print line after perfecting the functionality
      // printDmsSecurity(dms, getResourceId());

   }

   private Set<AccessControlPolicy> printDmsSecurity(DocumentManagementService dms, String resourceId)
   {
      trace.debug("Security for: " + resourceId);
      Set<AccessControlPolicy> applicablePolicies = dms.getApplicablePolicies(resourceId);
      trace.debug("applicablePolicies: " + applicablePolicies);
      // assertTrue(applicablePolicies.size() > 0);

      Set<AccessControlPolicy> effectivePolicies = dms.getEffectivePolicies(resourceId);
      trace.debug("effectivePolicies: " + effectivePolicies);
      // assertTrue(effectivePolicies.size() > 0);

      Set<AccessControlPolicy> policies = dms.getPolicies(resourceId);
      trace.debug("policies: " + policies);
      // assertTrue(policies.size() > 0);

      Set<Privilege> privileges = dms.getPrivileges(resourceId);
      trace.debug("privileges: " + privileges);

      return applicablePolicies;
   }

   public void policyChanged(ValueChangeEvent event)
   {
      AccessControlBean acb = (AccessControlBean) event.getComponent().getAttributes().get("acb");
      String property = (String) event.getComponent().getAttributes().get("property");
      if (event.getNewValue() == null)
      {
         return;
      }
      if (!(((String) event.getNewValue()).equals((String) event.getOldValue())))
      {
         String newValue = (String) event.getNewValue();
         if (property.equals(AccessControlBean.CREATE))
         {
            acb.setCreate(newValue);
         }
         else if (property.equals(AccessControlBean.READ))
         {
            acb.setRead(newValue);
         }
         else if (property.equals(AccessControlBean.MODIFY))
         {
            acb.setModify(newValue);
         }
         else if (property.equals(AccessControlBean.DELETE))
         {
            acb.setDelete(newValue);
         }
         else if (property.equals(AccessControlBean.READACL))
         {
            acb.setReadAcl(newValue);
         }
         else if (property.equals(AccessControlBean.MODIFYACL))
         {
            acb.setModifyAcl(newValue);
         }
      }
      for (int i = 0; i < accessControlBean.size(); i++)
      {
        AccessControlBean objAcb = accessControlBean.get(i);
         if (objAcb.getParticipant().getId().equals(acb.getParticipant().getId()))
         {
            accessControlBean.remove(i);
            accessControlBean.add(i, acb);
            allInheritPolicy = checkAllRolesInherit(acb);
         }
      }
      securityDialogTable.setList(accessControlBean);
      securityDialogTable.initialize();
      policyChanged = true;
   }

   public boolean isLeaf()
   {
      return isLeaf;
   }

   public void setLeaf(boolean isLeaf)
   {
      this.isLeaf = isLeaf;
   }

   @Override
   public void initialize()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      List<ColumnPreference> colsInheritedTable = new ArrayList<ColumnPreference>();

      ColumnPreference colParticipant = new ColumnPreference("Participant", "participant", ColumnDataType.NUMBER, this
            .getMessages().getString("securityDialog.column.participant"));
      colParticipant.setColumnContentUrl(ResourcePaths.V_SECURITY_DIALOG_COLUMNS);
      colParticipant.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colCreate = new ColumnPreference("Create", "create", this.getMessages().getString(
            "securityDialog.column.create"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, true);
      colCreate.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colRead = new ColumnPreference("Read", "read", this.getMessages().getString(
            "securityDialog.column.read"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, true);
      colRead.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colModify = new ColumnPreference("Modify", "modify", this.getMessages().getString(
            "securityDialog.column.modify"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, true);
      colModify.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colDelete = new ColumnPreference("Delete", "delete", this.getMessages().getString(
            "securityDialog.column.delete"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, true);
      colDelete.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colReadACL = new ColumnPreference("ReadACL", "readACL", this.getMessages().getString(
            "securityDialog.column.readACL"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, true);
      colReadACL.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colModifyACL = new ColumnPreference("ModifyACL", "modifyACL", this.getMessages().getString(
            "securityDialog.column.modifyACL"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, true);
      colModifyACL.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colActions = new ColumnPreference("Actions", "actions", this.getMessages().getString(
            "securityDialog.column.actions"), ResourcePaths.V_SECURITY_DIALOG_COLUMNS, true, false);
      colActions.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colParticipant);
      colsInheritedTable.add(colParticipant);
      if (!isLeaf())
      {
         cols.add(colCreate);
         colsInheritedTable.add(colCreate);
      }
      cols.add(colRead);
      colsInheritedTable.add(colRead);
      cols.add(colModify);
      colsInheritedTable.add(colModify);
      cols.add(colDelete);
      colsInheritedTable.add(colDelete);
      cols.add(colReadACL);
      colsInheritedTable.add(colReadACL);
      cols.add(colModifyACL);
      colsInheritedTable.add(colModifyACL);
      cols.add(colActions);

      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, CommonProperties.CONTEXT_PORTAL,
            "Participant");
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      securityDialogTable = new SortableTable<AccessControlBean>(colSelecpopup, null,
            new SortableTableComparator<AccessControlBean>("participant", false));
      securityDialogTable.setRowSelector(new DataTableRowSelector("selectedRow",true));
      securityDialogTable.initialize();

      IColumnModel columnModelInherited = new DefaultColumnModel(colsInheritedTable, null, null,
            CommonProperties.CONTEXT_PORTAL, "Participant");
      TableColumnSelectorPopup colSelecpopupInherited = new TableColumnSelectorPopup(columnModelInherited);

      securityDialogInheritedTable = new SortableTable<AccessControlBean>(colSelecpopupInherited, null,
            new SortableTableComparator<AccessControlBean>("participant", false));
      //securityDialogInheritedTable.setRowSelector(new DataTableRowSelector("selectedRow",true));
      securityDialogInheritedTable.initialize();
   }
   
   /**
    * @return 
    */
   private Map<String, QualifiedModelParticipantInfo> getAllParticipant()
   {
      List<QualifiedModelParticipantInfo> allParticipants = ParticipantUtils.fetchAllParticipants(true);
      Map<String, QualifiedModelParticipantInfo> participants = new HashMap<String, QualifiedModelParticipantInfo>();
      for (QualifiedModelParticipantInfo qualifiedModelParticipantInfo : allParticipants)
      {
         DmsPrincipal principal = new DmsPrincipal(qualifiedModelParticipantInfo,
               ModelUtils.extractModelId(qualifiedModelParticipantInfo.getQualifiedId()));
         if (!participants.containsKey(principal.getName()))
         {
            participants.put(principal.getName(), qualifiedModelParticipantInfo);
         }
      }
      return participants;
   }

   public SortableTable<AccessControlBean> getSecurityDialogTable()
   {
      return securityDialogTable;
   }

   public void setSecurityDialogTable(SortableTable<AccessControlBean> securityDialogTable)
   {
      this.securityDialogTable = securityDialogTable;
   }

   public int getSelectedRowCount()
   {
      int count = 0;
      for (AccessControlBean accessControl : accessControlBean)
      {
         if (accessControl.isSelectedRow())
            count++;
      }
      return count;
   }

   public void removeRoles()
   {
      for (int i = 0; i < accessControlBean.size(); i++)
      {
         if (accessControlBean.get(i).isSelectedRow())
         {
            accessControlBean.remove(i);
            i--;
         }
      }
      securityDialogTable.setList(accessControlBean);
      securityDialogTable.initialize();
      policyChanged = true;
   }

   public void editRoles()
   {
      for (int i = 0; i < accessControlBean.size(); i++)
      {
         if (accessControlBean.get(i).isSelectedRow())
         {
            accessControlBean.get(i).setEdit(true);
         }
      }
      securityDialogTable.setList(accessControlBean);
      securityDialogTable.initialize();
   }

   private AccessControlPolicy getAccessControlPolicy()
   {
      DocumentManagementService dms = ContextPortalServices.getDocumentManagementService();
      Set<AccessControlPolicy> applicablePolicies = null;
      AccessControlPolicy next = null;
      try
      {
         applicablePolicies = dms.getPolicies(getResourceId());
         next = applicablePolicies.iterator().next();
      }
      catch (java.util.NoSuchElementException nee)
      {
         try
         {
            applicablePolicies = dms.getApplicablePolicies(getResourceId());
            next = applicablePolicies.iterator().next();
         }
         catch (Exception e)
         {
            trace.error(e);
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }
      return next;
   }
   
   public void removeRole(ActionEvent event)
   {
      AccessControlBean acb = (AccessControlBean) event.getComponent().getAttributes().get("acb");
      AccessControlPolicy next = null;
      AccessControlBean acbRemoveObj = null;
      AccessControlEntry aceRemoveObj = null;

      for (int i = 0; i < accessControlBean.size(); i++)
      {
         if (accessControlBean.get(i).equals(acb))
         {
            acbRemoveObj = accessControlBean.get(i);
            break;
         }
      }

      // If current entry is not yet saved to JCR,no need to read from DMS
      if (acb.isSaved() && acbRemoveObj != null)
      {
         next = getAccessControlPolicy();
         for (AccessControlEntry ace : next.getAccessControlEntries())
         {
            if (ace.getPrincipal().getName().equals(acbRemoveObj.getParticipant().getPrincipal().getName()))
            {
               aceRemoveObj = ace;
               break;
            }
         }
         if (null != aceRemoveObj)
         {
            next.removeAccessControlEntry(aceRemoveObj);
            ContextPortalServices.getDocumentManagementService().setPolicy(getResourceId(), next);
            policyChanged = true;
         }
      }

      accessControlBean.remove(acbRemoveObj);

      // When all roles are removed, do not show Apply btn
      if (accessControlBean.size() == 0)
      {
         allInheritPolicy = true;
      }
      securityDialogTable.setList(accessControlBean);
      securityDialogTable.initialize();
   }

   public List<AccessControlBean> getAccessControlBeanInherited()
   {
      return accessControlBeanInherited;
   }

   public void setAccessControlBeanInherited(List<AccessControlBean> accessControlBeanInherited)
   {
      this.accessControlBeanInherited = accessControlBeanInherited;
   }

   public SortableTable<AccessControlBean> getSecurityDialogInheritedTable()
   {
      return securityDialogInheritedTable;
   }

   public void setSecurityDialogInheritedTable(SortableTable<AccessControlBean> securityDialogInheritedTable)
   {
      this.securityDialogInheritedTable = securityDialogInheritedTable;
   }

   public void toggleExpandCollpase()
   {
      expanded = !expanded;
   }

   public boolean isExpanded()
   {
      return expanded;
   }

   public boolean isSecurityEnabled()
   {
      return DMSHelper.isSecurityEnabled();
   }

   public boolean isReadACL()
   {
      if (getResourceId() != null && DMSHelper.hasPrivilege(getResourceId(), DmsPrivilege.READ_ACL_PRIVILEGE))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean isModifyACL()
   {
      if (getResourceId() != null && DMSHelper.hasPrivilege(getResourceId(), DmsPrivilege.MODIFY_ACL_PRIVILEGE))
      {
         return true;
      }
      else
      {
         return false;
      }
   }
   
   /**
    * @return
    */
   public boolean isEditMode()
   {
      boolean isEditMode = false;
      if (policyChanged)
      {
         isEditMode = true;
      }
      else
      {
         for (int i = 0; i < accessControlBean.size(); i++)
         {
            if (accessControlBean.get(i).isEdit())
            {
               isEditMode = true;
               break;
            }
         }
      }
      return isEditMode;
   }

   public boolean isRolesEditable()
   {
      return isModifyACL() && getSelectedRowCount() > 0;
   }

   public boolean isAllInheritPolicy()
   {
      return allInheritPolicy;
   }
   
   
}