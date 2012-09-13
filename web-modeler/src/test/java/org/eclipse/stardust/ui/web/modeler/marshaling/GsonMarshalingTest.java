package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GsonMarshalingTest
{
   private final String testData = "{a: '1', b: {b1:2, b2:'3'}, c: null, d:{d1:{d11:true}, d2:{d22:false} }, e: [{e_id: 'e1', e_tags: ['e1a']}, {e_id: 'e2', e_tags: ['e2a', 'e2b']}] }";

   private JsonMarshaller jsonIo = new JsonMarshaller();

   private JsonObject master;

   @Before
   public void initGson()
   {
      this.jsonIo = new JsonMarshaller();
   }

   @Before
   public void initTestData()
   {
      this.master = jsonIo.readJsonObject(testData);
   }

   @Test
   public void mergingPrmitiveFieldsMustApplyUpdates()
   {
      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));

      Partial_a update = jsonIo.gson().fromJson(master, Partial_a.class);
      update.a = "11";

      jsonIo.writeIntoJsonObject(update, master);

      assertThat(master.get("a").getAsString(), is("11"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));
   }

   @Test
   public void mergingObjectFieldsMustApplyUpdates()
   {
      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").getAsJsonObject().get("b1").getAsInt(), is(2));
      assertThat(master.get("b").getAsJsonObject().get("b2").getAsString(), is("3"));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));

      Partial_b update = jsonIo.gson().fromJson(master, Partial_b.class);
      update.b.b1 = 11;

      jsonIo.writeIntoJsonObject(update, master);

      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").getAsJsonObject().get("b1").getAsInt(), is(11));
      assertThat(master.get("b").getAsJsonObject().get("b2").getAsString(), is("3"));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));
   }

   @Test
   public void mergingObjectFieldNullsMustSetJsonNull()
   {
      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").getAsJsonObject().get("b1").getAsInt(), is(2));
      assertThat(master.get("b").getAsJsonObject().get("b2").getAsString(), is("3"));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));

      Partial_b update = jsonIo.gson().fromJson(master, Partial_b.class);
      update.b.b2 = null;

      jsonIo.writeIntoJsonObject(update, master);

      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").getAsJsonObject().get("b1").getAsInt(), is(2));
      assertThat(master.get("b").getAsJsonObject().get("b2").isJsonNull(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));
   }

   @Test
   public void settingObjectFieldNullsMustMergeValue()
   {
      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));

      Partial_c update = jsonIo.gson().fromJson(master, Partial_c.class);
      update.c = new JsonObject();
      update.c.addProperty("c1", 42);
      update.c.addProperty("c2", "blub");

      jsonIo.writeIntoJsonObject(update, master);

      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(false));
      assertThat(master.get("c").isJsonObject(), is(true));
      assertThat(master.get("c").getAsJsonObject().get("c1").getAsInt(), is(42));
      assertThat(master.get("c").getAsJsonObject().get("c2").getAsString(), is("blub"));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));
   }

   @Test
   public void updatedJsonTreeMustBeMerged()
   {
      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d1").isJsonObject(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d1").getAsJsonObject().get("d11").getAsBoolean(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d2").isJsonObject(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d2").getAsJsonObject().get("d22").getAsBoolean(), is(false));
      assertThat(master.get("e").isJsonArray(), is(true));

      Partial_d update = jsonIo.gson().fromJson(master, Partial_d.class);

      assertThat(update.d.d1.get("d11").getAsBoolean(), is(true));
      assertThat(update.d.d2.get("d22").getAsBoolean(), is(false));

      update.d.d1.addProperty("d11", 42);
      update.d.d2.addProperty("d22", "blub");

      jsonIo.writeIntoJsonObject(update, master);

      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d1").isJsonObject(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d1").getAsJsonObject().get("d11").getAsInt(), is(42));
      assertThat(master.get("d").getAsJsonObject().get("d2").isJsonObject(), is(true));
      assertThat(master.get("d").getAsJsonObject().get("d2").getAsJsonObject().get("d22").getAsString(), is("blub"));
      assertThat(master.get("e").isJsonArray(), is(true));
   }

   @Test
   public void updatesToArrayMembersMustPropagate()
   {
      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));
      assertThat(master.get("e").getAsJsonArray().size(), is(2));

      Partial_e update = jsonIo.gson().fromJson(master, Partial_e.class);

      assertThat(update.e.size(), is(2));
      assertThat(update.e.get(0).e_id, is("e1"));
      assertThat(update.e.get(0).e_tags.size(), is(1));
      assertThat(update.e.get(1).e_id, is("e2"));
      assertThat(update.e.get(1).e_tags.size(), is(2));

      update.e.add(new Full_e("e3", "e3a", "e3b", "e3c"));

      jsonIo.writeIntoJsonObject(update, master);

      assertThat(master.get("a").getAsString(), is("1"));
      assertThat(master.get("b").isJsonObject(), is(true));
      assertThat(master.get("c").isJsonNull(), is(true));
      assertThat(master.get("d").isJsonObject(), is(true));
      assertThat(master.get("e").isJsonArray(), is(true));
      assertThat(master.get("e").getAsJsonArray().size(), is(3));
   }

   public static class Partial_a
   {
      public String a;
   }

   public static class Partial_b
   {
      public Full_b b;
   }

   public static class Partial_c
   {
      public JsonObject c;
   }

   public static class Partial_d
   {
      public Full_d d;
   }

   public static class Partial_e
   {
      public List<Full_e> e;
   }

   public static class Full_b
   {
      public int b1;
      public String b2;
   }

   public static class Full_d
   {
      public JsonObject d1;
      public JsonObject d2;
   }

   public static class Full_e
   {
      public String e_id;
      public JsonArray e_tags;

      public Full_e()
      {
      }

      public Full_e(String e_id, String... tags)
      {
         this.e_id = e_id;
         this.e_tags = new JsonArray();
         for (String tag : tags)
         {
            e_tags.add(new JsonPrimitive(tag));
         }
      }
   }

}
