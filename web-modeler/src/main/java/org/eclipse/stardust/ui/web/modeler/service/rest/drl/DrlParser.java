package org.eclipse.stardust.ui.web.modeler.service.rest.drl;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.core.compatibility.el.SyntaxError;

public class DrlParser
{
   private static final String GLOBAL = "global";

   private static final String RULE = "rule";

   private static final String END = "end";

   private static final String WHEN = "when";

   private static final String THEN = "then";

   private static final String MODIFY = "modify";

   public StreamTokenizer tokenizer;

   /**
    * 
    * @param drl
    * @return
    * @throws SyntaxError
    * @throws IOException
    */
   public JsonObject parseDrl(String drl) throws SyntaxError, IOException
   {
      tokenizer = new StreamTokenizer(new StringReader(drl));

      tokenizer.slashSlashComments(true);
      tokenizer.slashStarComments(true);

      JsonObject ruleSetJson = new JsonObject();

      ruleSetJson.addProperty("uuid", UUID.randomUUID().toString());

      JsonObject rulesJson = new JsonObject();

      ruleSetJson.add("rules", rulesJson);

      ParameterDefinition.clear();

      // Get initial token

      tokenizer.nextToken();

      while (tokenizer.ttype != StreamTokenizer.TT_EOF)
      {
         System.out.println(tokenizer.sval);

         if (checkKeyword(GLOBAL))
         {
            parseGlobal();
         }
         else if (checkKeyword(RULE))
         {
            parseRule(rulesJson);
         }
         else
         {
            throw new SyntaxError("Rule definition expected.");
         }
      }

      ruleSetJson.add("parameterDefinitions",
            ParameterDefinition.createParameterDefinitionJson());

      return ruleSetJson;
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private void parseGlobal() throws IOException, SyntaxError
   {
      consumeIdentifier("Class");
      consumeIdentifier("Name");
      consumeCharacter(';');
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private void parseRule(JsonObject rulesJson) throws IOException, SyntaxError
   {
      JsonObject ruleJson = new JsonObject();

      rulesJson.add(tokenizer.sval, ruleJson);

      ruleJson.add("conditions", new JsonArray());
      ruleJson.add("actions", new JsonArray());      
      ruleJson.addProperty("uuid", UUID.randomUUID().toString());

      String name = consumeIdentifier("Rule name");

      ruleJson.addProperty("id", name);
      ruleJson.addProperty("name", name);

      parseWhenClause(ruleJson);
      parseThenClause(ruleJson);
      consumeKeyword(END);
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private void parseWhenClause(JsonObject ruleJson) throws IOException, SyntaxError
   {
      consumeKeyword(WHEN);

      while (checkCharacter('$'))
      {
         String name = consumeIdentifier("Binding");

         System.out.println("Fact: " + name);

         consumeCharacter(':');

         String type = consumeIdentifier("Class");

         System.out.println("Class: " + type);

         ParameterDefinition.addParameterDefinition(name, ParameterDefinition.IN, type);
         consumeCharacter('(');
         
         JsonObject factConditionJson = new JsonObject();
         
         ruleJson.get("conditions").getAsJsonArray().add(factConditionJson);
         
         factConditionJson.addProperty("fact", name);
         
         consumeCharacter(')');
      }
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private void parseThenClause(JsonObject ruleJson) throws IOException, SyntaxError
   {
      consumeKeyword(THEN);

      while (checkKeyword(MODIFY))
      {
         consumeCharacter('(');
         consumeCharacter('$');

         String fact = consumeIdentifier("Fact");
         
         System.out.println("Fact: " + fact);

         consumeCharacter(')');
         consumeCharacter('{');
         
         JsonObject factActionsJson = new JsonObject();
         
         ruleJson.get("actions").getAsJsonArray().add(factActionsJson);
         
         factActionsJson.addProperty("fact", fact);

         consumeCharacter('}');
      }
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private String consumeIdentifier(String role) throws IOException, SyntaxError
   {
      if (tokenizer.ttype == StreamTokenizer.TT_WORD)
      {
         String returnValue = tokenizer.sval;

         tokenizer.nextToken();

         return returnValue;
      }
      else
      {
         throw new SyntaxError(role + " expected instead of " + tokenizer.sval + ".");
      }
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private boolean checkKeyword(String keyword) throws IOException
   {
      if (tokenizer.ttype == StreamTokenizer.TT_WORD && tokenizer.sval.equals(keyword))
      {
         tokenizer.nextToken();

         return true;
      }

      return false;
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private void consumeKeyword(String keyword) throws IOException, SyntaxError
   {
      if (tokenizer.ttype != StreamTokenizer.TT_WORD || !tokenizer.sval.equals(keyword))
      {
         throw new SyntaxError(keyword + " expected instead of " + tokenizer.sval + ".");
      }

      tokenizer.nextToken();
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private boolean checkCharacter(char c) throws IOException
   {
      if (tokenizer.ttype == c)
      {
         tokenizer.nextToken();

         return true;
      }

      return false;
   }

   /**
    * @throws IOException
    * @throws SyntaxError
    * 
    */
   private void consumeCharacter(char c) throws IOException, SyntaxError
   {
      if (tokenizer.ttype == c)
      {
         tokenizer.nextToken();
      }
      else
      {
         throw new SyntaxError("" + c + " expected instead of " + (char) tokenizer.ttype
               + ".");
      }
   }

   /**
    * 
    * @param args
    */
   /*public static void main(String[] args)
   {
      try
      {
         String drl = new Scanner(new File("all-syntax.drl"), "UTF-8").useDelimiter("\\A")
               .next();
         DrlParser parser = new DrlParser();

         System.out.println(drl);
         System.out.println();
         System.out.println(parser.parseDrl(drl).toString());
      }
      catch (FileNotFoundException e1)
      {
         e1.printStackTrace();
      }
      catch (SyntaxError e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }*/
}

/**
 * 
 * @author Marc.Gille
 * 
 */
class ParameterDefinition
{
   public static final String IN = "IN";

   public static final String OUT = "OUT";

   public static final String INOUT = "INOUT";

   public static final String PRIMITIVE = "primitive";

   public static final String STRUCT = "struct";

   private static Map<String, ParameterDefinition> parameterDefinitionMap = new HashMap<String, ParameterDefinition>();

   public String direction;

   public String name;

   public String id;

   public String type;

   /**
    * 
    */
   public static void clear()
   {
      parameterDefinitionMap.clear();
   }

   public static void addParameterDefinition(String name, String direction, String type)
   {
      ParameterDefinition parameterDefinition = parameterDefinitionMap.get(name);

      if (parameterDefinition != null)
      {
         if (direction.equals(parameterDefinition.direction))
         {
            parameterDefinition.direction = INOUT;
         }
      }
      else
      {
         parameterDefinition = new ParameterDefinition();

         parameterDefinition.name = name;
         parameterDefinition.direction = direction;
         parameterDefinition.type = type;

         parameterDefinitionMap.put(name, parameterDefinition);
      }
   }

   public static JsonArray createParameterDefinitionJson() throws SyntaxError
   {
      JsonArray parameterDefinitionsJson = new JsonArray();

      for (String key : parameterDefinitionMap.keySet())
      {
         ParameterDefinition parameterDefinition = parameterDefinitionMap.get(key);
         JsonObject parameterDefinitionJson = new JsonObject();

         parameterDefinitionsJson.add(parameterDefinitionJson);
         parameterDefinitionJson.addProperty("direction", parameterDefinition.direction);
         parameterDefinitionJson.addProperty("id", parameterDefinition.name);
         parameterDefinitionJson.addProperty("name", parameterDefinition.name);

         if (parameterDefinition.type.startsWith("org.eclipse.stardust.types."))
         {
            parameterDefinitionJson.addProperty("dataType", STRUCT);
            parameterDefinitionJson.addProperty(
                  "structuredDataTypeFullId",
                  parameterDefinition.type.substring(
                        parameterDefinition.type.indexOf("org.eclipse.stardust.types.") + 27)
                        .replace('.', ':'));
         }
         else
         {
            parameterDefinitionJson.addProperty("dataType", PRIMITIVE);

            if (parameterDefinition.type.equals("int"))
            {
               parameterDefinitionJson.addProperty("primitiveDataType", "int");
            }
            else
            {
               throw new SyntaxError("Unknown type " + parameterDefinition.type
                     + "for fact.");
            }
         }
      }

      return parameterDefinitionsJson;
   }
}
