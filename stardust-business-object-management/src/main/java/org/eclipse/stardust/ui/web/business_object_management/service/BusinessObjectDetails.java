/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.business_object_management.service;

import java.io.Serializable;
import java.util.List;

import javax.xml.namespace.QName;

public class BusinessObjectDetails implements BusinessObject
{
   private static final long serialVersionUID = 1L;

   private long modelOid;
   private String id;
   private String name;
   private List<Definition> items;
   private List<Value> values;

   public BusinessObjectDetails(long modelOid, String id, String name, List<Definition> items, List<Value> values)
   {
      this.modelOid = modelOid;
      this.id = id;
      this.name = name;
      this.items = items;
      this.values = values;
   }

   @Override
   public long getModelOid()
   {
      return modelOid;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public List<Definition> getItems()
   {
      return items;
   }

   @Override
   public List<Value> getValues()
   {
      return values;
   }

   public static class ValueDetails implements Value
   {
      private long processInstanceOid;
      private Serializable value;

      public ValueDetails(long processInstanceOid, Serializable value)
      {
         this.processInstanceOid = processInstanceOid;
         this.value = value;
      }

      public long getProcessInstanceOid()
      {
         return processInstanceOid;
      }

      public Serializable getValue()
      {
         return value;
      }

      @Override
      public String toString()
      {
         return "(" + processInstanceOid + ":" + value + ")";
      }
   }

   public static class DefinitionDetails implements Definition
   {
      private String name;
      private int type;
      private QName typeName;
      private boolean key;
      private boolean primaryKey;
      private List<Definition> items;
      private boolean isList;

      public DefinitionDetails(String name, int type, QName typeName, boolean isList, boolean key, boolean primaryKey, List<Definition> items)
      {
         this.name = name;
         this.type = type;
         this.typeName = typeName;
         this.isList = isList;
         this.key = key;
         this.primaryKey = primaryKey;
         this.items = items;
      }

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public int getType()
      {
         return type;
      }

      @Override
      public QName getTypeName()
      {
         return typeName;
      }

      @Override
      public boolean isList()
      {
         return isList;
      }

      @Override
      public boolean isKey()
      {
         return key;
      }

      @Override
      public boolean isPrimaryKey()
      {
         return primaryKey;
      }

      @Override
      public List<Definition> getItems()
      {
         return items;
      }
   }
}
