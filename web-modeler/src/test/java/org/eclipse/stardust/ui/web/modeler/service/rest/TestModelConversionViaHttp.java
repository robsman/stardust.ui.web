package org.eclipse.stardust.ui.web.modeler.service.rest;

import org.junit.Test;

import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.HttpRequestExecutor;
import org.eclipse.stardust.ui.web.modeler.model.conversion.ModelConverter;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;

public class TestModelConversionViaHttp
{
   public static final String SESSION_COOKIE = "9F9D9577E7281FCC742477449A4410D5";

   private RequestExecutor requestExecutor = new HttpRequestExecutor(SESSION_COOKIE);

   private ModelConverter modelConverter = new ModelConverter(new JsonMarshaller(), requestExecutor);

   @Test
   public void test() throws Exception
   {
      modelConverter.convertModel("Statement P1");
   }


}
