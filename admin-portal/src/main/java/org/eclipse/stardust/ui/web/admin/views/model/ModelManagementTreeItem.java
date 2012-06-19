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
package org.eclipse.stardust.ui.web.admin.views.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;


public class ModelManagementTreeItem
{

   private static final String MODEL_IMAGE_PATH = "/plugins/views-common/images/icons/model.gif";
   private static final String MODEL_VERSION_IMAGE_PATH = "/plugins/admin-portal/images/icons/book_open.png";
   private static final String MODEL_VERSION_CONSUMER_IMAGE_PATH = "/plugins/admin-portal/images/icons/resultset_previous.png";
   private static final String MODEL_VERSION_PROVIDER_IMAGE_PATH = "/plugins/admin-portal/images/icons/resultset_next.png";
   private static final String PRIMARY_INTERFACE_IMAGE_PATH = "/plugins/admin-portal/images/icons/cog.png";
   private static final String BLANK_IMAGE_PATH = "/plugins/admin-portal/images/t.gif";

   private List<ModelManagementTreeItem> children = new ArrayList<ModelManagementTreeItem>();
   private Date validFrom;
   private ModelManagementTreeItem parent;
   private String comment;
   private String label;
   private Integer oid;
   private Type type;
   private String processId;

   private String version;
   private String versionLabel;
   private boolean activeVersion;

   private DeployedModelDescription modelDescription;// use
                                                     // DeployedModelDescriptionDetails

   public ModelManagementTreeItem(ModelManagementTreeItem parent)
   {
      this.parent = parent;
   }

   public List<ModelManagementTreeItem> getChildren()
   {
      return children;
   }

   public String getComment()
   {
      return comment;
   }

   public String getLabel()
   {
      return label;
   }

   public ModelManagementTreeItem getParent()
   {
      return parent;
   }

   public Type getType()
   {
      return type;
   }

   public Date getValidFrom()
   {
      return validFrom;
   }

   public String getVersion()
   {
      return version;
   }

   public String getVersionLabel()
   {
      return versionLabel;
   }

   public void setVersionLabel(String versionLabel)
   {
      this.versionLabel = versionLabel;
   }

   public void setChildren(List<ModelManagementTreeItem> children)
   {
      this.children = children;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public Integer getOid()
   {
      return oid;
   }

   public void setOid(Integer oid)
   {
      this.oid = oid;
   }

   public void setParent(ModelManagementTreeItem parent)
   {
      this.parent = parent;
   }

   public void setType(Type type)
   {
      this.type = type;
   }

   public void setValidFrom(Date validFrom)
   {
      this.validFrom = validFrom;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public static enum Type {

      MODEL(MODEL_IMAGE_PATH), MODEL_VERSION(MODEL_VERSION_IMAGE_PATH), MODEL_VERSION_CONSUMER(
            MODEL_VERSION_CONSUMER_IMAGE_PATH), MODEL_VERSION_PROVIDER(MODEL_VERSION_PROVIDER_IMAGE_PATH), NONE(
            BLANK_IMAGE_PATH), PRIMARY_INTERFACE(PRIMARY_INTERFACE_IMAGE_PATH);
      private String imagePath;

      Type(String path)
      {
         imagePath = path;
      }

      public String getImage()
      {
         return imagePath;

      }
   }

   public DeployedModelDescription getModelDescription()
   {
      return modelDescription;
   }

   public void setModelDescription(DeployedModelDescription modelDescription)
   {
      this.modelDescription = modelDescription;
   }

   public boolean isActiveVersion()
   {
      return activeVersion;
   }

   public void setActiveVersion(boolean activeVersion)
   {
      this.activeVersion = activeVersion;
   }

   /**
    * @return the processId
    */
   public String getProcessId()
   {
      return processId;
   }

   /**
    * @param processId the processId to set
    */
   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

}
