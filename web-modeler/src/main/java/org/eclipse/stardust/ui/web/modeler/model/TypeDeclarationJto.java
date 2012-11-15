package org.eclipse.stardust.ui.web.modeler.model;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class TypeDeclarationJto extends ModelElementJto
{
   public TypeDeclarationJto()
   {
      this.type = ModelerConstants.TYPE_DECLARATION_PROPERTY;
   }

   public TypeDeclarationDetails typeDeclaration = new TypeDeclarationDetails();

   public static class TypeDeclarationDetails
   {
      public TypeDetails type = new TypeDetails();

      public JsonObject schema = new JsonObject();

      public static class TypeDetails
      {
         public String classifier;
         public String location;
         public String xref;
      }
   }

   // TODO more details
}
