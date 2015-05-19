package org.eclipse.stardust.ui.web.modeler.edit.batch;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class TestJsonPathEvaluation
{
   private final JsonPathEvaluator varEvaluator = new JsonPathEvaluator();

   private final String testObjectJson = "{" //
         + "'booleanMember': true," //
         + "'stringMember': 'abc-def'," //
         + "'numbersArrayMember': [" //
         + "  10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0" //
         + "]," //
         + "'objectsArrayMember': [" //
         + "  {'x': 1, 'y': 'first'}," //
         + "  {'x': 2, 'y': 'second'}," //
         + "  {'x': 3, 'y': 'last'}" //
         + "]," //
         + "'objectMember': {" //
         + "  'x': 1," //
         + "  'y': 2" //
         + "  }" //
         + "}";

   private JsonElement testObject;

   @Before
   public void init()
   {
      this.testObject = new JsonParser().parse(testObjectJson);
   }

   @Test
   public void extractingABooleanFieldFromAnObjectMustSucceed()
   {
      JsonElement booleanField = varEvaluator.resolveExpression(testObject,
            "booleanMember");

      assertThat(booleanField, is(notNullValue()));
      assertThat(booleanField, is(instanceOf(JsonPrimitive.class)));
      assertThat(booleanField.getAsJsonPrimitive().isBoolean(), is(true));
      assertThat(booleanField.getAsBoolean(), is(true));
   }

   @Test
   public void extractingAStringFieldFromAnObjectMustSucceed()
   {
      JsonElement stringField = varEvaluator.resolveExpression(testObject,
            "stringMember");

      assertThat(stringField, is(notNullValue()));
      assertThat(stringField, is(instanceOf(JsonPrimitive.class)));
      assertThat(stringField.getAsJsonPrimitive().isString(), is(true));
      assertThat(stringField.getAsString(), is("abc-def"));
   }

   @Test
   public void extractingAnArrayFieldFromAnObjectMustSucceed()
   {
      JsonElement numbersArray = varEvaluator.resolveExpression(testObject,
            "numbersArrayMember");

      assertThat(numbersArray, is(notNullValue()));
      assertThat(numbersArray, is(instanceOf(JsonArray.class)));
      assertThat(numbersArray.getAsJsonArray().size(), is(11));
   }

   @Test
   public void extractingTheNthArrayElementFromAnObjectsArrayMemberMustSucceed()
   {
      JsonElement firstElement = varEvaluator.resolveExpression(testObject,
            "numbersArrayMember[0]");

      assertThat(firstElement, is(notNullValue()));
      assertThat(firstElement, is(instanceOf(JsonPrimitive.class)));
      assertThat(firstElement.getAsInt(), is(10));

      JsonElement lastElement = varEvaluator.resolveExpression(testObject,
            "numbersArrayMember[10]");

      assertThat(lastElement, is(notNullValue()));
      assertThat(lastElement, is(instanceOf(JsonPrimitive.class)));
      assertThat(lastElement.getAsInt(), is(0));
   }

   @Test
   public void extractingAnArrayElementByPredicateFromAnObjectsArrayMemberMustSucceed()
   {
      JsonElement firstElement = varEvaluator.resolveExpression(testObject,
            "objectsArrayMember[y='first']");

      assertThat(firstElement, is(notNullValue()));
      assertThat(firstElement, is(instanceOf(JsonObject.class)));
      assertThat(firstElement.getAsJsonObject().getAsJsonPrimitive("x").getAsInt(), is(1));

      JsonElement lastElement = varEvaluator.resolveExpression(testObject,
            "objectsArrayMember[y='last']");

      assertThat(lastElement, is(notNullValue()));
      assertThat(lastElement, is(instanceOf(JsonObject.class)));
      assertThat(lastElement.getAsJsonObject().getAsJsonPrimitive("x").getAsInt(), is(3));
   }

   @Test
   public void extractingAnObjectFieldFromAnObjectMustSucceed()
   {
      JsonElement objectField = varEvaluator.resolveExpression(testObject,
            "objectMember");

      assertThat(objectField, is(notNullValue()));
      assertThat(objectField, is(instanceOf(JsonObject.class)));
      assertThat(objectField.getAsJsonObject().getAsJsonPrimitive("x").getAsInt(), is(1));
      assertThat(objectField.getAsJsonObject().getAsJsonPrimitive("y").getAsInt(), is(2));
   }

   @Test
   public void extractingANamedPropertyFromAnObjectFieldFromAnObjectMustSucceed()
   {
      JsonElement xField = varEvaluator
            .resolveExpression(testObject, "objectMember/x");

      assertThat(xField, is(notNullValue()));
      assertThat(xField, is(instanceOf(JsonPrimitive.class)));
      assertThat(xField.getAsInt(), is(1));

      JsonElement yField = varEvaluator
            .resolveExpression(testObject, "objectMember/y");

      assertThat(yField, is(notNullValue()));
      assertThat(yField, is(instanceOf(JsonPrimitive.class)));
      assertThat(yField.getAsInt(), is(2));
   }

   @Test
   public void extractingTheDefaultPoolOidFromANewProcessMustSucceed()
   {
      String testData = "{added: [{\"poolSymbols\":{\"_default_pool__1\":{\"oid\":32,\"id\":\"_default_pool__1\",\"name\":null,\"x\":0,\"y\":0,\"width\":409,\"height\":670,\"processId\":\"SimpleChecklist1\",\"orientation\":\"DIAGRAM_FLOW_ORIENTATION_VERTICAL\",\"laneSymbols\":[{\"oid\":31,\"id\":\"DefaultLane\",\"name\":null,\"x\":12,\"y\":32,\"width\":375,\"height\":600,\"type\":\"swimlaneSymbol\",\"uuid\":\"da904387-6e4c-4dc3-9d07-00d174be5030\",\"activitySymbols\":{},\"gatewaySymbols\":{},\"eventSymbols\":{},\"dataSymbols\":{},\"annotationSymbols\":{}}]}},\"oid\":33,\"orientation\":\"DIAGRAM_FLOW_ORIENTATION_VERTICAL\",\"connections\":{}}]}";

      JsonElement testResults = new JsonParser().parse(testData);

      JsonElement poolOid = varEvaluator
            .resolveExpression(
                  testResults,
                  "added/[orientation='DIAGRAM_FLOW_ORIENTATION_VERTICAL']/poolSymbols/_default_pool__1/oid");

      assertThat(poolOid, is(notNullValue()));
      assertThat(poolOid, is(instanceOf(JsonPrimitive.class)));
      assertThat(poolOid.getAsLong(), is(32L));
   }

}
