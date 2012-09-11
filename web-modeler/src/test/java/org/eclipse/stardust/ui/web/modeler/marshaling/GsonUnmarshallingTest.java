package org.eclipse.stardust.ui.web.modeler.marshaling;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GsonUnmarshallingTest
{
   private final String testData = "{a: '1', b: {c:2, d:'3'}}";

   private Gson gson;

   @Before
   public void initGson()
   {
      this.gson = new Gson();
   }

   @Test
   public void unmarshallingPartialStructuresMustIgnoreUnmappedAttributes()
   {
      Partial_a partial_a = gson.fromJson(testData, Partial_a.class);

      assertThat(partial_a, is(not(nullValue())));
      assertThat(partial_a.a, is("1"));
      assertThat(partial_a.b, is(instanceOf(JsonObject.class)));

      Partial_b partial_b = gson.fromJson(testData, Partial_b.class);

      assertThat(partial_b, is(not(nullValue())));
      assertThat(partial_b.a, is("1"));
      assertThat(partial_b.b, is(instanceOf(Partial_bc.class)));
      assertThat(partial_b.b.c, is(2));
   }

   public static class Partial_a
   {
      public String a;

      public JsonObject b;
   }

   public static class Partial_b
   {
      public String a;

      public Partial_bc b;
   }

   public static class Partial_bc
   {
      public int c;
   }
}
