package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.eclipse.stardust.model.xpdl.builder.BpmModelBuilder.newBpmModel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.impl.ModelTypeImpl;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class GsonUseCases
{
   private ModelType model;
   private Gson gson;
   private Gson gsonExt;

   @Before
   public void initModel()
   {
      this.model = newBpmModel().withIdAndName(
            "TestModel",
            "Test Model").build();
      AttributeUtil.setAttribute(model, PredefinedConstants.VERSION_ATT,
            "1");
   }

   @Before
   public void initGson()
   {
      this.gson = new Gson();

      this.gsonExt = new GsonBuilder()
            .registerTypeAdapter(ModelSerializer.ADAPTED_TYPE, new ModelSerializer())
            .create();
   }

   @Test
   public void writeSimpleObjectManually()
   {
      JsonObject struct = new JsonObject();

      struct.addProperty("id", model.getId());
      struct.addProperty("name", model.getName());
      struct.addProperty("type", "model");

      String json = gson.toJson(struct);

      assertEquals("{\"id\":\"TestModel\",\"name\":\"Test Model\",\"type\":\"model\"}",
            json);
   }

   @Test
   public void readSimpleObjectManually()
   {
      JsonObject struct = new JsonParser().parse(
            "{\"id\":\"TestModel\",\"name\":\"Test Model\",\"type\":\"model\"}")
            .getAsJsonObject();

      assertNotNull(struct);
      assertEquals(model.getId(), struct.get("id").getAsString());
      assertEquals(model.getName(), struct.get("name").getAsString());
      assertEquals("model", struct.get("type").getAsString());
   }

   @Test
   public void simpleObjectTyped()
   {
      String json = gson.toJson(ModelJto.fromXpdl(model));

      assertEquals("{\"id\":\"TestModel\",\"name\":\"Test Model\",\"type\":\"model\"}",
            json);

      JsonObject struct = (JsonObject) gson.toJsonTree(ModelJto.fromXpdl(model));
      // ... add non-standard properties freely
      String jsonFromTree = gson.toJson(struct);

      assertEquals("{\"id\":\"TestModel\",\"name\":\"Test Model\",\"type\":\"model\"}",
            jsonFromTree);
   }

   @Test
   public void simpleObjectWrapper()
   {
      String json = gsonExt.toJson(model);

      assertEquals("{\"id\":\"TestModel\",\"name\":\"Test Model\",\"type\":\"model\"}",
            json);

      JsonObject struct = (JsonObject) gsonExt.toJsonTree(model);
      // ... add non-standard properties freely
      String jsonFromTree = gson.toJson(struct);

      assertEquals("{\"id\":\"TestModel\",\"name\":\"Test Model\",\"type\":\"model\"}",
            jsonFromTree);
   }

   static class ModelJto
   {
      public String id;
      public String name;
      public String type = "model";

      public static ModelJto fromXpdl(ModelType model)
      {
         ModelJto jto = new ModelJto();

         jto.id = model.getId();
         jto.name = model.getName();

         return jto;
      }
   }

   static class ModelSerializer implements JsonSerializer<ModelType>
   {
      public static final Class<ModelTypeImpl> ADAPTED_TYPE = ModelTypeImpl.class;

      public JsonElement serialize(ModelType model, Type type,
            JsonSerializationContext context)
      {
         JsonObject result = new JsonObject();

         result.addProperty("id", model.getId());
         result.addProperty("name", model.getName());
         result.addProperty("type", "model");

         return result;
      }
   }
}
