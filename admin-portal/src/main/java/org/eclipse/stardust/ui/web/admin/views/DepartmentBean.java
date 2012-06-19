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
package org.eclipse.stardust.ui.web.admin.views;

import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.runtime.Department;

//TODO;review code:it should not be name as bean
public class DepartmentBean
{
   private Department parentDepartment;

   private OrganizationInfo organizationInfo;

   private long oid;

   private String id;

   private String name;

   private String description;

   public DepartmentBean(Department department)
   {
      this(department.getOID(), department.getId(), department.getName(), department.getDescription(), department
            .getParentDepartment(), department.getOrganization());
   }

   public DepartmentBean(Department parentDepartment, OrganizationInfo organizationInfo)
   {
      this(-1, "", "", "", parentDepartment, organizationInfo);
   }

   public DepartmentBean(long oid, String id, String name, String description, Department parentDepartment,
         OrganizationInfo organizationInfo)
   {
      super();
      this.oid = oid;
      this.id = id;
      this.name = name;
      this.description = description;
      this.setOrganization(organizationInfo);
      this.setParentDepartment(parentDepartment);
   }

   public long getOID()
   {
      return oid;
   }

   public void setOID(long oid)
   {
      this.oid = oid;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setOrganization(OrganizationInfo organizationInfo)
   {
      this.organizationInfo = organizationInfo;
   }

   public OrganizationInfo getOrganization()
   {
      return organizationInfo;
   }

   public void setParentDepartment(Department parentDepartment)
   {
      this.parentDepartment = parentDepartment;
   }

   public Department getParentDepartment()
   {
      return parentDepartment;
   }
}