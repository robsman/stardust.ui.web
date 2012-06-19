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
package org.eclipse.stardust.ui.web.processportal.view.manual;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class DMSAdapter extends AbstractMap<String, Serializable> implements Serializable
{
   private static final long serialVersionUID = 1L;

   public static final String FOLDER = "Folder";
   public static final String DOCUMENT = "Document";
   public static final String PROCESS_ATTACHMENTS = "ProcessAttachments";

   public static DMSAttribute[] FOLDER_ATTRIBUTES = new DMSAttribute[] {
         new DMSAttribute("id"), new DMSAttribute("name"), new DMSAttribute("path"),
         new DMSAttribute("description"), new DMSAttribute("owner"),
         new DMSAttribute("dateCreated", Date.class),
         new DMSAttribute("dateLastModified", Date.class),
         new DMSAttribute("documentCount", Integer.class),
         new DMSAttribute("folderCount", Integer.class),
         new DMSAttribute("properties", List.class), // Properties is actually Map but it is represented as List here
         new DMSAttribute("folders", List.class),
         new DMSAttribute("documents", List.class)};

   public static DMSAttribute[] DOCUMENT_ATTRIBUTES = new DMSAttribute[] {
         new DMSAttribute("id"), new DMSAttribute("name"),
         new DMSAttribute("path"), new DMSAttribute("description"),
         new DMSAttribute("owner"),
         new DMSAttribute("dateCreated", Date.class),
         new DMSAttribute("dateLastModified", Date.class),
         new DMSAttribute("documentCount", Integer.class),
         new DMSAttribute("size", Integer.class), new DMSAttribute("contentType"),
         new DMSAttribute("revisionId"), new DMSAttribute("revisionName"),
         new DMSAttribute("lockOwner"), new DMSAttribute("encoding"),
         new DMSAttribute("properties", List.class), // Properties is actually Map but it is represented as List here
         new DMSAttribute("versionLabels", List.class),
         new DMSAttribute("documentAnnotations", List.class)};

   private final Interaction interaction;
   private final Serializable value;
   private final Map<String, Serializable> attributes;

   /**
    * @param interaction
    * @param serializable
    */
   public DMSAdapter(Interaction interaction, Serializable value, String dataId)
   {
      this.interaction = interaction;
      this.value = value;
      this.attributes = new HashMap<String, Serializable>();

      if("PROCESS_ATTACHMENTS".equals(dataId) && value instanceof List<?>)
      {
         List<?> listObject = (List<?>)value;
         ArrayList<Serializable> newList = new ArrayList<Serializable>();
         for (Object obj : (List<?>)listObject)
         {
            if(obj instanceof Document)
            {
               newList.add(new DMSDocumentAdapter((Document)obj));
            }
            else
            {
               newList.add(new DMSObjectAdapter<Object>(obj));
            }
         }
         attributes.put(PROCESS_ATTACHMENTS, newList);
      }
      else
      {
         attributes.put(PROCESS_ATTACHMENTS, value);
      }
   }

   /**
    * @param interaction
    * @param serializable
    */
   public DMSAdapter(Interaction interaction, Serializable value)
   {
      this.interaction = interaction;
      this.value = value;
      this.attributes = new HashMap<String, Serializable>();

      DMSAttribute[] dmsAttributes = new DMSAttribute[0];
      if(value instanceof Folder)
      {
         dmsAttributes = FOLDER_ATTRIBUTES;
      }
      else if(value instanceof Document)
      {
         dmsAttributes = DOCUMENT_ATTRIBUTES;
      }

      for (DMSAttribute attr : dmsAttributes)
      {
         try
         {
            Serializable serObject = (Serializable) ReflectionUtils.invokeGetterMethod(
                  value, attr.getName());

            if(serObject != null && serObject instanceof List<?>)
            {
               List<?> listObject = (List<?>)serObject;
               ArrayList<Serializable> newList = new ArrayList<Serializable>();
               for (Object obj : (List<?>)listObject)
               {
                  if(obj instanceof Folder)
                     newList.add(new DMSFolderAdapter((Folder)obj));
                  else if(value instanceof Document)
                     newList.add(new DMSDocumentAdapter((Document)obj));
                  else
                     newList.add(new DMSObjectAdapter<Object>(obj));
               }
               attributes.put(attr.getName(), newList);
            }
            else if(serObject != null && serObject instanceof Map<?, ?>)
            {
               Map<?, ?> mapObject = (Map<?, ?>)serObject;
               ArrayList<Serializable> newMapList = new ArrayList<Serializable>();

               for (Object key : mapObject.keySet())
               {
                  newMapList.add(new DMSMapAdapter(new Pair(key, mapObject.get(key)), "Property"));
               }
               attributes.put(attr.getName(), newMapList);
            }
            else
            {
               attributes.put(attr.getName(), serObject);
            }
         }
         catch(Exception e)
         {
            //IGNORE
         }
      }
   }

   public Interaction getInteraction()
   {
      return interaction;
   }

   public Serializable getValue()
   {
      return value;
   }

   @Override
   public Set<Entry<String, Serializable>> entrySet()
   {
      return attributes.entrySet();
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static class DMSAttribute implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private String name;
      private Class<?> clazz;

      public DMSAttribute(String name)
      {
         this(name, String.class);
      }

      public DMSAttribute(String name, Class<?> clazz)
      {
         super();
         this.name = name;
         this.clazz = clazz;
      }

      public String getName()
      {
         return name;
      }
      public Class<?> getClazz()
      {
         return clazz;
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class DMSObjectAdapter<T> implements Serializable
   {
      private static final long serialVersionUID = 1L;

      protected T object;
      protected boolean expanded = true;

      public DMSObjectAdapter(T object)
      {
         this.object = object;
      }

      public String getHeader()
      {
         return getContentAsString();
      }

      public String getContentAsString()
      {
         return object != null ? object.toString() : "<Null Object>";
      }

      public void toggleExpanded()
      {
         expanded = !expanded;
      }

      public T getObject()
      {
         return object;
      }

      public boolean isExpanded()
      {
         return expanded;
      }

      public void setExpanded(boolean expanded)
      {
         this.expanded = expanded;
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class DMSFolderAdapter extends DMSObjectAdapter<Folder> implements Serializable
   {
      private static final long serialVersionUID = 1L;

      public DMSFolderAdapter(Folder folder)
      {
         super(folder);
      }

      @Override
      public String getHeader()
      {
         return object != null ? object.getName() : "Folder";
      }

      @Override
      public String getContentAsString()
      {
         String SEPARATOR = ", ";
         StringBuffer sb = new StringBuffer();

         if(object != null)
         {
            for (DMSAttribute dmsAttribute : FOLDER_ATTRIBUTES)
            {
               try
               {
                  Serializable serObject = (Serializable) ReflectionUtils.invokeGetterMethod(
                        object, dmsAttribute.getName());

                  if( serObject != null && !(serObject instanceof List) && !(serObject instanceof Map))
                  {
                     sb.append(SEPARATOR + dmsAttribute.getName() + "=");
                     sb.append(serObject);
                  }
               }
               catch(Exception e)
               {
                  sb.append(SEPARATOR + dmsAttribute.getName() + "=");
                  sb.append("<ERROR>");
               }
            }

            if(sb.length() > 0)
            {
               return sb.substring(2, sb.length());
            }
         }
         else
         {
            sb.append("<Null Folder Object>");
         }

         return sb.toString();
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class DMSDocumentAdapter extends DMSObjectAdapter<Document> implements Serializable
   {
      private static final long serialVersionUID = 1L;

      public DMSDocumentAdapter(Document document)
      {
         super(document);
      }

      @Override
      public String getHeader()
      {
         return object != null ? object.getName() : "Document";
      }

      @Override
      public String getContentAsString()
      {
         String SEPARATOR = ", ";
         StringBuffer sb = new StringBuffer();

         if(object != null)
         {
            for (DMSAttribute dmsAttribute : DOCUMENT_ATTRIBUTES)
            {
               try
               {
                  Serializable serObject = (Serializable) ReflectionUtils.invokeGetterMethod(
                        object, dmsAttribute.getName());

                  if( serObject != null && !(serObject instanceof List) && !(serObject instanceof Map))
                  {
                     sb.append(SEPARATOR + dmsAttribute.getName() + "=");
                     sb.append(serObject);
                  }
               }
               catch(Exception e)
               {
                  sb.append(SEPARATOR + dmsAttribute.getName() + "=");
                  sb.append("<ERROR>");
               }
            }

            if(sb.length() > 0)
            {
               return sb.substring(2, sb.length());
            }
         }
         else
         {
            sb.append("<Null Document Object>");
         }

         return sb.toString();
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class DMSMapAdapter extends DMSObjectAdapter<Pair> implements Serializable
   {
      private static final long serialVersionUID = 1L;

      private String type;

      public DMSMapAdapter(Pair pair, String type)
      {
         super(pair);
      }

      public DMSMapAdapter(Pair pair)
      {
         this(pair, "Map");
      }

      @Override
      public String getHeader()
      {
         return (object != null && object.getFirst() != null) ? object.getFirst().toString() : type;
      }

      @Override
      public String getContentAsString()
      {
         StringBuffer sb = new StringBuffer();

         if(object != null)
         {
            if(object.getFirst() != null)
            {
               sb.append(object.getFirst());
               sb.append("=");
            }

            sb.append(object.getSecond());
         }
         else
         {
            sb.append("<Null " + type + " Object>");
         }

         return sb.toString();
      }
   }
}
