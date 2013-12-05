package org.eclipse.stardust.ui.web.modeler.xsd;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

import org.eclipse.xsd.XSDSchema;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import org.eclipse.stardust.model.xpdl.xpdl2.SchemaTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.XpdlFactory;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.XsdSchemaUtils;

public class XSDInheritanceTest
{

   @Test
   public void testBaseSchema()
   {
      XSDSchema externalSchema = this.loadExternalSchema("/Person.xsd");
      JsonObject personJSON = createPersonJSON();
      assertEquality(externalSchema, personJSON);
   }

   @Test
   public void testUpdateSchema()
   {
      XSDSchema externalSchema = this.loadExternalSchema("/PersonUpdated.xsd");
      JsonObject manualUpdatedPersonSchema = createUpdatedPersonJSON();
      SchemaTypeType schemaTypeType = XpdlFactory.eINSTANCE.createSchemaTypeType();
      schemaTypeType.setSchema(externalSchema);
      XsdSchemaUtils.updateXSDSchemaType(null, schemaTypeType, manualUpdatedPersonSchema);
      //assertEquality(externalSchema, manualUpdatedPersonSchema);
   }

   @Test
   public void testUpdateSchema2()
   {
      XSDSchema externalSchema = this.loadExternalSchema("/PersonUpdated2.xsd");
      JsonObject manualUpdatedPersonSchema = createUpdatedPersonJSON2();
      SchemaTypeType schemaTypeType = XpdlFactory.eINSTANCE.createSchemaTypeType();
      schemaTypeType.setSchema(externalSchema);
      XsdSchemaUtils.updateXSDSchemaType(null, schemaTypeType, manualUpdatedPersonSchema);
      //assertEquality(externalSchema, manualUpdatedPersonSchema);
   }

   private JsonObject createPersonJSON()
   {
      JsonObject personSchema = new JsonObject();
      personSchema.addProperty("targetNamespace", "http://www.example.org/Person");

      JsonObject nsMappings = new JsonObject();
      nsMappings.addProperty("tns", "http://www.example.org/Person");
      nsMappings.addProperty("xs", "http://www.w3.org/2001/XMLSchema");


      personSchema.add("nsMappings", nsMappings);

      JsonArray typesArray = new JsonArray();
      personSchema.add("types", typesArray);

      // Address
      JsonObject addressType = new JsonObject();
      typesArray.add(addressType);
      addressType.addProperty("name", "Address");
      addressType.addProperty("classifier", "complexType");

      addAddressSequence(addressType);

      // Employee
      JsonObject employeeType = new JsonObject();
      typesArray.add(employeeType);
      employeeType.addProperty("name", "Employee");
      employeeType.addProperty("classifier", "complexType");
      employeeType.addProperty("base", "tns:Person");
      employeeType.addProperty("method", "extension");

      JsonArray employeeBody = new JsonArray();
      employeeType.add("body", employeeBody);
      JsonObject employeeSequence = addSequenceIntro(employeeBody);

      JsonArray employeeSequenceBody = new JsonArray();
      employeeSequence.add("body", employeeSequenceBody);

      employeeSequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      employeeSequenceBody.add(createSimpleType("age", "element", "required", "xs:int"));

      JsonObject employeeAddressType = new JsonObject();
      employeeSequenceBody.add(employeeAddressType);
      employeeAddressType.addProperty("name", "address");
      employeeAddressType.addProperty("classifier", "element");

      addAddressSequence(employeeAddressType);

      employeeAddressType.addProperty("cardinality", "required");
      employeeAddressType.addProperty("type", "tns:Address");

      employeeSequence.addProperty("inherited", true);

      JsonObject personExtensionSequence = addSequenceIntro(employeeBody);

      JsonArray personExtensionSequenceBody = new JsonArray();
      personExtensionSequence.add("body", personExtensionSequenceBody);
      personExtensionSequenceBody.add(createSimpleType("empNumber", "element",
            "required", "xs:int"));
      JsonObject employeeSkill = createPrimitiveEnumerationType("skill", "element",
            "xs:string", new String[] {"Rookie", "Advanced", "Professional", "Expert"});
      personExtensionSequenceBody.add(employeeSkill);
      employeeSkill.addProperty("cardinality", "required");
      employeeSkill.addProperty("type", "tns:Skill");

      // Person
      JsonObject personType = new JsonObject();
      typesArray.add(personType);

      personType.addProperty("name", "Person");
      personType.addProperty("classifier", "complexType");

      JsonArray personBody = new JsonArray();
      personType.add("body", personBody);

      JsonObject personSequence = addSequenceIntro(personBody);

      JsonArray personSequenceBody = new JsonArray();
      personSequence.add("body", personSequenceBody);

      personSequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      personSequenceBody.add(createSimpleType("age", "element", "required", "xs:int"));

      JsonObject personAddressType = new JsonObject();
      personSequenceBody.add(personAddressType);
      personAddressType.addProperty("name", "address");
      personAddressType.addProperty("classifier", "element");

      addAddressSequence(personAddressType);

      personAddressType.addProperty("cardinality", "required");
      personAddressType.addProperty("type", "tns:Address");

      // Skill Enumeration
      String[] skillEnumValues = new String[] {
            "Rookie", "Advanced", "Professional", "Expert"};
      typesArray.add(createPrimitiveEnumerationType("Skill", "simpleType", "xs:string",
            skillEnumValues));

      return personSchema;
   }

   private JsonObject createUpdatedPersonJSON()
   {
      JsonObject personJSON = new JsonObject();
      personJSON.addProperty("targetNamespace", "http://www.example.org/Person");

      JsonObject nsMappings = new JsonObject();
      nsMappings.addProperty("tns", "http://www.example.org/Person");
      nsMappings.addProperty("xs", "http://www.w3.org/2001/XMLSchema");

      personJSON.add("nsMappings", nsMappings);

      JsonArray typesArray = new JsonArray();
      personJSON.add("types", typesArray);

      // Address
      JsonObject addressType = new JsonObject();
      typesArray.add(addressType);
      addressType.addProperty("name", "Address");
      addressType.addProperty("classifier", "complexType");

      addAddressSequence(addressType);

      // Employee
      JsonObject employeeType = new JsonObject();
      typesArray.add(employeeType);
      employeeType.addProperty("name", "Employee");
      employeeType.addProperty("classifier", "complexType");
      employeeType.addProperty("base", "tns:Person");
      employeeType.addProperty("method", "extension");

      JsonArray employeeBody = new JsonArray();
      employeeType.add("body", employeeBody);
      JsonObject employeeSequence = addSequenceIntro(employeeBody);

      JsonArray employeeSequenceBody = new JsonArray();
      employeeSequence.add("body", employeeSequenceBody);

      employeeSequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      employeeSequenceBody.add(createSimpleType("theAge", "element", "required", "xs:int"));

      JsonObject employeeAddressType = new JsonObject();
      employeeSequenceBody.add(employeeAddressType);
      employeeAddressType.addProperty("name", "address");
      employeeAddressType.addProperty("classifier", "element");

      addAddressSequence(employeeAddressType);

      employeeAddressType.addProperty("cardinality", "required");
      employeeAddressType.addProperty("type", "tns:Address");

      employeeSequence.addProperty("inherited", true);

      JsonObject personExtensionSequence = addSequenceIntro(employeeBody);

      JsonArray personExtensionSequenceBody = new JsonArray();
      personExtensionSequence.add("body", personExtensionSequenceBody);
      personExtensionSequenceBody.add(createSimpleType("empNumber", "element",
            "required", "xs:int"));
      JsonObject employeeSkill = createPrimitiveEnumerationType("skill", "element",
            "xs:string", new String[] {"Rookie", "Advanced", "Professional", "Expert"});
      personExtensionSequenceBody.add(employeeSkill);
      employeeSkill.addProperty("cardinality", "required");
      employeeSkill.addProperty("type", "tns:Skill");

      // Person
      JsonObject personType = new JsonObject();
      typesArray.add(personType);

      personType.addProperty("name", "Person");
      personType.addProperty("classifier", "complexType");

      JsonArray personBody = new JsonArray();
      personType.add("body", personBody);

      JsonObject personSequence = addSequenceIntro(personBody);

      JsonArray personSequenceBody = new JsonArray();
      personSequence.add("body", personSequenceBody);

      personSequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      personSequenceBody.add(createSimpleType("theAge", "element", "required", "xs:int"));

      JsonObject personAddressType = new JsonObject();
      personSequenceBody.add(personAddressType);
      personAddressType.addProperty("name", "address");
      personAddressType.addProperty("classifier", "element");

      addAddressSequence(personAddressType);

      personAddressType.addProperty("cardinality", "required");
      personAddressType.addProperty("type", "tns:Address");

      // Skill Enumeration
      String[] skillEnumValues = new String[] {
            "Rookie", "Advanced", "Professional", "Expert"};
      typesArray.add(createPrimitiveEnumerationType("Skill", "simpleType", "xs:string",
            skillEnumValues));


      return personJSON;
   }

   private JsonObject createUpdatedPersonJSON2()
   {
      JsonObject personJSON = new JsonObject();
      personJSON.addProperty("targetNamespace", "http://www.example.org/Person");

      JsonObject nsMappings = new JsonObject();
      nsMappings.addProperty("tns", "http://www.example.org/Person");
      nsMappings.addProperty("xs", "http://www.w3.org/2001/XMLSchema");

      personJSON.add("nsMappings", nsMappings);

      JsonArray typesArray = new JsonArray();
      personJSON.add("types", typesArray);

      // Account
      JsonObject accountType = new JsonObject();
      typesArray.add(accountType);
      accountType.addProperty("name", "Account");
      accountType.addProperty("classifier", "complexType");

      addAccountSequence(accountType);


      // Address
      JsonObject addressType = new JsonObject();
      typesArray.add(addressType);
      addressType.addProperty("name", "Address");
      addressType.addProperty("classifier", "complexType");

      addAddressSequence(addressType);

      // Employee
      JsonObject employeeType = new JsonObject();
      typesArray.add(employeeType);
      employeeType.addProperty("name", "Employee");
      employeeType.addProperty("classifier", "complexType");
      employeeType.addProperty("base", "tns:Person");
      employeeType.addProperty("method", "extension");

      JsonArray employeeBody = new JsonArray();
      employeeType.add("body", employeeBody);
      JsonObject employeeSequence = addSequenceIntro(employeeBody);

      JsonArray employeeSequenceBody = new JsonArray();
      employeeSequence.add("body", employeeSequenceBody);

      employeeSequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      employeeSequenceBody.add(createSimpleType("theAge", "element", "required", "xs:int"));

      //Employee -> Address

      JsonObject employeeAddressType = new JsonObject();
      employeeSequenceBody.add(employeeAddressType);
      employeeAddressType.addProperty("name", "address");
      employeeAddressType.addProperty("classifier", "element");

      addAddressSequence(employeeAddressType);

      employeeAddressType.addProperty("cardinality", "required");
      employeeAddressType.addProperty("type", "tns:Address");

      //Employee -> Account

      JsonObject employeeAccountType = new JsonObject();
      employeeSequenceBody.add(employeeAccountType);
      employeeAccountType.addProperty("name", "account");
      employeeAccountType.addProperty("classifier", "element");

      addAccountSequence(employeeAccountType);

      employeeAccountType.addProperty("cardinality", "required");
      employeeAccountType.addProperty("type", "tns:Account");


      employeeSequence.addProperty("inherited", true);

      JsonObject personExtensionSequence = addSequenceIntro(employeeBody);

      JsonArray personExtensionSequenceBody = new JsonArray();
      personExtensionSequence.add("body", personExtensionSequenceBody);
      personExtensionSequenceBody.add(createSimpleType("empNumber", "element",
            "required", "xs:int"));
      JsonObject employeeSkill = createPrimitiveEnumerationType("skill", "element",
            "xs:string", new String[] {"Rookie", "Advanced", "Professional", "Expert"});
      personExtensionSequenceBody.add(employeeSkill);
      employeeSkill.addProperty("cardinality", "required");
      employeeSkill.addProperty("type", "tns:Skill");

      // Person
      JsonObject personType = new JsonObject();
      typesArray.add(personType);

      personType.addProperty("name", "Person");
      personType.addProperty("classifier", "complexType");

      JsonArray personBody = new JsonArray();
      personType.add("body", personBody);

      JsonObject personSequence = addSequenceIntro(personBody);

      JsonArray personSequenceBody = new JsonArray();
      personSequence.add("body", personSequenceBody);

      personSequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      personSequenceBody.add(createSimpleType("theAge", "element", "required", "xs:int"));

      //Person -> Address

      JsonObject personAddressType = new JsonObject();
      personSequenceBody.add(personAddressType);
      personAddressType.addProperty("name", "address");
      personAddressType.addProperty("classifier", "element");

      addAddressSequence(personAddressType);

      personAddressType.addProperty("cardinality", "required");
      personAddressType.addProperty("type", "tns:Address");

      //Person -> Account

      JsonObject personAccountType = new JsonObject();
      personSequenceBody.add(personAccountType);
      personAccountType.addProperty("name", "account");
      personAccountType.addProperty("classifier", "element");

      addAccountSequence(personAccountType);

      personAccountType.addProperty("cardinality", "required");
      personAccountType.addProperty("type", "tns:Account");

      // Skill Enumeration
      String[] skillEnumValues = new String[] {
            "Rookie", "Advanced", "Professional", "Expert"};
      typesArray.add(createPrimitiveEnumerationType("Skill", "simpleType", "xs:string",
            skillEnumValues));


      return personJSON;
   }


   private JsonObject addSequenceIntro(JsonArray employeeBody)
   {
      JsonObject employeeSequence = new JsonObject();
      employeeBody.add(employeeSequence);

      employeeSequence.addProperty("name", "\u003csequence\u003e");
      employeeSequence.addProperty("classifier", "sequence");
      employeeSequence.addProperty("cardinality", "required");
      return employeeSequence;
   }

   private void addAccountSequence(JsonObject accountType)
   {
      JsonArray typeBody = new JsonArray();
      accountType.add("body", typeBody);
      JsonObject sequence = addSequenceIntro(typeBody);

      JsonArray sequenceBody = new JsonArray();
      sequence.add("body", sequenceBody);

      sequenceBody.add(createSimpleType("name", "element", "required", "xs:string"));
      sequenceBody.add(createSimpleType("number", "element", "required", "xs:int"));
   }

   private void addAddressSequence(JsonObject addressType)
   {
      JsonArray typeBody = new JsonArray();
      addressType.add("body", typeBody);
      JsonObject sequence = addSequenceIntro(typeBody);

      JsonArray sequenceBody = new JsonArray();
      sequence.add("body", sequenceBody);

      sequenceBody.add(createSimpleType("streetName", "element", "required", "xs:string"));
      sequenceBody.add(createSimpleType("zipCode", "element", "required", "xs:string"));
      sequenceBody.add(createSimpleType("city", "element", "required", "xs:string"));
   }

   private JsonObject createSimpleType(String name, String classifier,
         String cardinality, String type)
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("name", name);
      jsonObject.addProperty("classifier", classifier);
      jsonObject.addProperty("cardinality", cardinality);
      jsonObject.addProperty("type", type);
      return jsonObject;
   }

   private JsonObject createPrimitiveEnumerationType(String name, String classifier,
         String type, String[] values)
   {
      JsonObject enumType = new JsonObject();
      enumType.addProperty("name", name);
      enumType.addProperty("classifier", classifier);
      enumType.addProperty("base", "xs:string");
      enumType.addProperty("method", "restriction");
      enumType.addProperty("primitiveType", "xs:string");
      JsonArray facets = new JsonArray();
      enumType.add("facets", facets);
      for (int i = 0; i < values.length; i++ )
      {
         facets.add(this.createEnumerationFacet(values[i]));
      }
      return enumType;
   }

   private JsonObject createEnumerationFacet(String name)
   {
      JsonObject facet = new JsonObject();
      facet.addProperty("name", name);
      facet.addProperty("classifier", "enumeration");
      return facet;
   }

   private String toPrettyString(JsonElement json)
   {
      Gson gson = new Gson();
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setIndent("  ");
      // jsonWriter.setHtmlSafe(false); commented out because it's ignored.
      jsonWriter.setLenient(true);
      gson.toJson(json, jsonWriter);
      return writer.toString();
   }

   private XSDSchema loadExternalSchema(String path)
   {
      URL location = XsdSchemaUtils.class.getResource(path);
      try
      {
         return ModelService.loadSchema(location.toString());
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   private void assertEquality(XSDSchema externalSchema, JsonObject jsonSchema)
   {
      String manualUpdatedPersonSchemaString = jsonSchema.toString();

      String updatedPersonSchemaString = XsdSchemaUtils.toSchemaJson(externalSchema)
            .toString();

      if (updatedPersonSchemaString.equals(manualUpdatedPersonSchemaString))
      {
         System.out.println("Schemas are identical!");
      }
      else
      {
         System.out.println("Schemas have differences!");
         System.out.println(updatedPersonSchemaString);
         System.out.println(manualUpdatedPersonSchemaString);
      }

      assertThat(updatedPersonSchemaString, is(manualUpdatedPersonSchemaString));
   }

   private JsonObject loadJSONFromString(String path)
   {
      JsonParser jsonParser = new JsonParser();
      try
      {
         InputStream is = XsdSchemaUtils.class.getResourceAsStream(path);
         String string = convertStreamToString(is);
         return (JsonObject) jsonParser.parse(string);
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
      return null;
   }

   private String convertStreamToString(InputStream is) throws IOException
   {
      if (is != null)
      {
         Writer writer = new StringWriter();

         char[] buffer = new char[1024];
         try
         {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1)
            {
               writer.write(buffer, 0, n);
            }
         }
         finally
         {
            is.close();
         }
         return writer.toString();
      }
      else
      {
         return "";
      }
   }




}
