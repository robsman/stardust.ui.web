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
package org.eclipse.stardust.ui.web.modeler.cap;

import java.util.ArrayList;
import java.util.List;




public class InputContainer
{
   public class Container implements ICheck, IContainerTypes
   {
      String label = "Diagram_Messages.InputContainer_GlobalElements_All";
      public String getLabel()
      {
         return label;
      }

      public class DataTypes implements ICheck, IContainerTypes
      {
         String label = "Diagram_Messages.InputContainer_GlobalElements_All_Data";
         private List content = new ArrayList();
         public List getContent()
         {
            return content;
         }
         public String getLabel()
         {
            return label;
         }
         // iterate over all elements and set checked
         public void setChecked(boolean checked, NameIDCache localNameIdCache)
         {
            for(int i = 0; i < content.size(); i++)
            {
               ((ContentDecorator) content.get(i)).setChecked(checked, localNameIdCache);
            }
         }
      }
      public class Applications implements ICheck, IContainerTypes
      {
         String label = "Diagram_Messages.InputContainer_GlobalElements_All_Applications";
         private List content = new ArrayList();
         public List getContent()
         {
            return content;
         }
         public String getLabel()
         {
            return label;
         }
         public void setChecked(boolean checked, NameIDCache localNameIdCache)
         {
            for(int i = 0; i < content.size(); i++)
            {
               ((ContentDecorator) content.get(i)).setChecked(checked, localNameIdCache);
            }
         }
      }
      public class Participants implements ICheck, IContainerTypes
      {
         String label = "Diagram_Messages.InputContainer_GlobalElements_All_Participants";
         private List content = new ArrayList();
         public List getContent()
         {
            return content;
         }
         public String getLabel()
         {
            return label;
         }
         public void setChecked(boolean checked, NameIDCache localNameIdCache)
         {
            for(int i = 0; i < content.size(); i++)
            {
               ((ContentDecorator) content.get(i)).setChecked(checked, localNameIdCache);
            }
         }
      }
      private DataTypes dataTypes = new DataTypes();
      private Applications applications = new Applications();
      private Participants participants = new Participants();

      public DataTypes getDataTypes()
      {
         return dataTypes;
      }
      public Applications getApplications()
      {
         return applications;
      }
      public Participants getParticipants()
      {
         return participants;
      }
      public void setChecked(boolean checked, NameIDCache localNameIdCache)
      {
         getDataTypes().setChecked(checked, localNameIdCache);
         getApplications().setChecked(checked, localNameIdCache);
         getParticipants().setChecked(checked, localNameIdCache);
      }
      public List getContent()
      {
         List content = new ArrayList();
         if(getDataTypes().getContent().size() > 0)
         {
            content.add(getDataTypes());
         }
         if(getApplications().getContent().size() > 0)
         {
            content.add(getApplications());
         }
         if(getParticipants().getContent().size() > 0)
         {
            content.add(getParticipants());
         }
         return content;
      }
      public List getAllContent()
      {
         List content = new ArrayList();
         if(getDataTypes().getContent().size() > 0)
         {
            content.addAll(getDataTypes().getContent());
         }
         if(getApplications().getContent().size() > 0)
         {
            content.addAll(getApplications().getContent());
         }
         if(getParticipants().getContent().size() > 0)
         {
            content.addAll(getParticipants().getContent());
         }
         return content;
      }
      public boolean hasDuplicateIds()
      {
         List allContent = getAllContent();
         for(int i = 0; i < allContent.size(); i++)
         {
            if(((ContentDecorator) allContent.get(i)).isDuplicateId())
            {
               return true;
            }
         }
         return false;
      }
   }

   private Container container = new Container();

   public Container getContainer()
   {
      return container;
   }
}