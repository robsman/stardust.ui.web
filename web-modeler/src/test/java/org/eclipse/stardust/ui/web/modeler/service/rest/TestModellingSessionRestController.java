package org.eclipse.stardust.ui.web.modeler.service.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"embedded-test-context.xml"})
public class TestModellingSessionRestController
{
   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   ModelerResource modelResource;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionRestController restController;

   @Before
   public void initServiceFactory()
   {
      mockServiceFactoryLocator.init();
   }

   @Test
   public void test()
   {
      assertThat(modelResource, is(not(nullValue())));

      assertThat(modelService, is(not(nullValue())));

      assertThat(restController, is(not(nullValue())));
   }
}
