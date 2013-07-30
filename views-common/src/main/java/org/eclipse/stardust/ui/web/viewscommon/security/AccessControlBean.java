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

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

public class AccessControlBean extends DefaultRowModel
{
   private static final long serialVersionUID = 6611664925943574939L;

   private Participant participant;
   
   private boolean read;

   private boolean modify;

   private boolean create;

   private boolean delete;

   private boolean readAcl;

   private boolean modifyAcl;

   private boolean edit;

   private boolean selectedRow;

   private String createPolicyChangedValue;

   private String readPolicyChangedValue;

   private String modifyPolicyChangedValue;

   private String deletePolicyChangedValue;

   private String readACLPolicyChangedValue;

   private String modifyACLPolicyChangedValue;
   
   private String modelLabel = null;

   public static final String ALLOW = "Allow";

   public static final String DENY = "Deny";

   public static final String CREATE = "Create";

   public static final String READ = "Read";

   public static final String MODIFY = "Modify";

   public static final String DELETE = "Delete";

   public static final String READACL = "Read ACL";

   public static final String MODIFYACL = "Modify ACL";

   private boolean newOrModified;

   public boolean isEdit()
   {
      return edit;
   }

   public void setEdit(boolean edit)
   {
      this.edit = edit;
   }

   public Participant getParticipant()
   {
      return participant;
   }

   public boolean isRead()
   {
      return read;
   }

   public boolean isModify()
   {
      return modify;
   }

   public boolean isCreate()
   {
      return create;
   }

   public boolean isDelete()
   {
      return delete;
   }

   public AccessControlBean(Participant participant)
   {
      super();
      this.participant = participant;
      setModelLabel();
      setRead(false);
      setModify(false);
      setCreate(false);
      setDelete(false);
      setReadAcl(false);
      setModifyAcl(false);
      setNewOrModified(true);
   }

   public AccessControlBean(Participant participant, boolean read, boolean modify,
         boolean create, boolean delete, boolean readAcl, boolean modifyAcl)
   {
      super();
      this.participant = participant;
      setModelLabel();
      setRead(read);
      setModify(modify);
      setCreate(create);
      setDelete(delete);
      setReadAcl(readAcl);
      setModifyAcl(modifyAcl);
      setNewOrModified(true);
   }

   private void setModelLabel()
   {
      if (StringUtils.isNotEmpty(this.participant.getModelId()))
      {
         Model model = ModelCache.findModelCache().getActiveModel(this.participant.getModelId());
         this.modelLabel = null != model ? MessagesViewsCommonBean.getInstance().getParamString(
               "views.myDocumentsTreeView.securityDialog.modelName.label", model.getName()) : null;
      }
   }
   
   public boolean isReadAcl()
   {
      return readAcl;
   }

   public boolean isModifyAcl()
   {
      return modifyAcl;
   }

   public void editClicked(ActionEvent ae)
   {
      setEdit(true);
      setNewOrModified(true);
   }

   

   public void setParticipant(Participant participant)
   {
      this.participant = participant;
   }

   public void setRead(boolean read)
   {
      this.read = read;
      if (this.read)
      {
         this.readPolicyChangedValue = ALLOW;
      }
      else
      {
         this.readPolicyChangedValue = DENY;
      }
   }

   public void setModify(boolean modify)
   {
      this.modify = modify;
      if (this.modify)
      {
         this.modifyPolicyChangedValue = ALLOW;
      }
      else
      {
         this.modifyPolicyChangedValue = DENY;
      }
   }

   public void setCreate(boolean create)
   {
      this.create = create;
      if (this.create)
      {
         this.createPolicyChangedValue = ALLOW;
      }
      else
      {
         this.createPolicyChangedValue = DENY;
      }
   }

   public void setDelete(boolean delete)
   {
      this.delete = delete;
      if (this.delete)
      {
         this.deletePolicyChangedValue = ALLOW;
      }
      else
      {
         this.deletePolicyChangedValue = DENY;
      }
   }

   public void setReadAcl(boolean readAcl)
   {
      this.readAcl = readAcl;
      if (this.readAcl)
      {
         this.readACLPolicyChangedValue = ALLOW;
      }
      else
      {
         this.readACLPolicyChangedValue = DENY;
      }
   }

   public void setModifyAcl(boolean modifyAcl)
   {
      this.modifyAcl = modifyAcl;
      if (this.modifyAcl)
      {
         this.modifyACLPolicyChangedValue = ALLOW;
      }
      else
      {
         this.modifyACLPolicyChangedValue = DENY;
      }
   }

   public void setAllPrivilege()
   {
      setRead(true);
      setModify(true);
      setCreate(true);
      setDelete(true);
      setReadAcl(true);
      setModifyAcl(true);
   }

   public boolean isNewOrModified()
   {
      return newOrModified;
   }

   public void setNewOrModified(boolean newOrModified)
   {
   // this.newOrModified = newOrModified;
   }

   public String getCreatePolicyChangedValue()
   {
      return createPolicyChangedValue;
   }

   public void setCreatePolicyChangedValue(String createPolicyChangedValue)
   {
   // this.createPolicyChangedValue = createPolicyChangedValue;
   }

   public String getReadPolicyChangedValue()
   {
      return readPolicyChangedValue;
   }

   public void setReadPolicyChangedValue(String readPolicyChangedValue)
   {
   // this.readPolicyChangedValue = readPolicyChangedValue;
   }

   public String getModifyPolicyChangedValue()
   {
      return modifyPolicyChangedValue;
   }

   public void setModifyPolicyChangedValue(String modifyPolicyChangedValue)
   {
   // this.modifyPolicyChangedValue = modifyPolicyChangedValue;
   }

   public String getDeletePolicyChangedValue()
   {
      return deletePolicyChangedValue;
   }

   public void setDeletePolicyChangedValue(String deletePolicyChangedValue)
   {
   // this.deletePolicyChangedValue = deletePolicyChangedValue;
   }

   public String getReadACLPolicyChangedValue()
   {
      return readACLPolicyChangedValue;
   }

   public void setReadACLPolicyChangedValue(String readACLPolicyChangedValue)
   {
   // this.readACLPolicyChangedValue = readACLPolicyChangedValue;
   }

   public String getModifyACLPolicyChangedValue()
   {
      return modifyACLPolicyChangedValue;
   }

   public void setModifyACLPolicyChangedValue(String modifyACLPolicyChangedValue)
   {
   // this.modifyACLPolicyChangedValue = modifyACLPolicyChangedValue;
   }

   public boolean isSelectedRow()
   {
      return selectedRow;
   }

   public void setSelectedRow(boolean selectedRow)
   {
      this.selectedRow = selectedRow;
   }
   
   public String getModelLabel()
   {
      return modelLabel;
   }

   public boolean equals(Object acb)
   {
      if (!(acb instanceof AccessControlBean))
      {
         return false;
      }
      if (((AccessControlBean) acb).getParticipant().getPrincipal().getName()
            .equals(this.getParticipant().getPrincipal().getName()))
      {
         return true;
      }
      else
      {
         return false;
      }
   }
}
