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
   
   private String read;

   private String modify;

   private String create;

   private String delete;

   private String readAcl;

   private String modifyAcl;

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
   
   public static final String INHERIT = "Inherit";

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

   public String getRead()
   {
      return read;
   }

   public String getModify()
   {
      return modify;
   }

   public String getCreate()
   {
      return create;
   }

   public String getDelete()
   {
      return delete;
   }

   public AccessControlBean(Participant participant)
   {
      super();
      this.participant = participant;
      setModelLabel();
      setRead(INHERIT);
      setModify(INHERIT);
      setCreate(INHERIT);
      setDelete(INHERIT);
      setReadAcl(INHERIT);
      setModifyAcl(INHERIT);
   }

   public AccessControlBean(Participant participant, String read, String modify,
         String create, String delete, String readAcl, String modifyAcl)
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
   
   public String getReadAcl()
   {
      return readAcl;
   }

   public String getModifyAcl()
   {
      return modifyAcl;
   }

   public void editClicked(ActionEvent ae)
   {
      setEdit(true);
   }

   public void setParticipant(Participant participant)
   {
      this.participant = participant;
   }

   public void setRead(String read)
   {
      this.read = read;
       this.readPolicyChangedValue = read;
   }

   public void setModify(String modify)
   {
      this.modify = modify;
      this.modifyPolicyChangedValue = modify;
   }

   public void setCreate(String create)
   {
      this.create = create;
      this.createPolicyChangedValue = create;
   }

   public void setDelete(String delete)
   {
      this.delete = delete;
      this.deletePolicyChangedValue = delete;
   }

   public void setReadAcl(String readAcl)
   {
      this.readAcl = readAcl;
      this.readACLPolicyChangedValue = readAcl;
   }

   public void setModifyAcl(String modifyAcl)
   {
      this.modifyAcl = modifyAcl;
      this.modifyACLPolicyChangedValue = modifyAcl;
   }

   public void setAllPrivilege()
   {
      setRead(ALLOW);
      setModify(ALLOW);
      setCreate(ALLOW);
      setDelete(ALLOW);
      setReadAcl(ALLOW);
      setModifyAcl(ALLOW);
   }

   public boolean isNewOrModified()
   {
      return newOrModified;
   }

   public String getCreatePolicyChangedValue()
   {
      return createPolicyChangedValue;
   }

   public String getReadPolicyChangedValue()
   {
      return readPolicyChangedValue;
   }

   public String getModifyPolicyChangedValue()
   {
      return modifyPolicyChangedValue;
   }

   public String getDeletePolicyChangedValue()
   {
      return deletePolicyChangedValue;
   }


   public String getReadACLPolicyChangedValue()
   {
      return readACLPolicyChangedValue;
   }

   public String getModifyACLPolicyChangedValue()
   {
      return modifyACLPolicyChangedValue;
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
