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
package org.eclipse.stardust.ui.web.common.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.stardust.ui.web.common.LaunchPanel;
import org.eclipse.stardust.ui.web.common.MenuSection;
import org.eclipse.stardust.ui.web.common.PerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.PerspectiveExtension;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
      "classpath*:META-INF/spring/*Ui-context.xml", "*Ui-context.xml"})
public class UiSchemaTest
{

   @Autowired
   PerspectiveDefinition pd;

   @Autowired
   List<PerspectiveExtension> extensions;

   @Test
   public void testPerspective()
   {
      assertNotNull(pd);

      assertEquals("ippProcessPortalPerspective", pd.getName());

      assertEquals(2, pd.getMenuSections().size());

      MenuSection msCommon = pd.getMenuSections().get(0);
      assertEquals("common", msCommon.getName());
      assertNotNull(msCommon.getInclude());

      MenuSection msAdmin = pd.getMenuSections().get(1);
      assertEquals("administration", msAdmin.getName());
      assertNotNull(msAdmin.getInclude());

      assertEquals(5, pd.getLaunchPanels().size());

      LaunchPanel lsOverview = pd.getLaunchPanels().get(0);
      assertEquals("overview", lsOverview.getName());
      assertNotNull(lsOverview.getInclude());
      assertFalse(lsOverview.isCollapsible());

      LaunchPanel lsMswl = pd.getLaunchPanels().get(1);
      assertEquals("mySharedWorklists", lsMswl.getName());
      assertNotNull(lsMswl.getInclude());
      assertTrue(lsMswl.isCollapsible());

      assertEquals(1, pd.getToolbarSections().size());

      assertEquals(2, pd.getViews().size());
   }

   @Test
   public void testPerspectiveExtensions()
   {
      assertNotNull(pd);

      assertNotNull(extensions);
      assertTrue(3 <= extensions.size());

      boolean foundBccExt = false;
      boolean foundFrameworkExt = false;

      for (PerspectiveExtension pe : extensions)
      {
         if ("ippPortalFrameworkPerspective".equals(pe.getName()))
         {
            foundFrameworkExt = true;

            assertEquals(1, pe.getMenuExtensions().size());
            assertEquals(1, pe.getMenuExtensions().get(0).getElements().size());
            assertEquals("*", pe.getMenuExtensions().get(0).getBefore());
            assertNull(pe.getMenuExtensions().get(0).getAfter());

            assertEquals(0, pe.getLaunchpadExtensions().size());

            assertEquals(0, pe.getToolbarExtensions().size());

            assertEquals(0, pe.getViewsExtensions().size());
         }
         else if ("ippBccExtensions".equals(pe.getName()))
         {

            foundBccExt = true;

            assertEquals(1, pe.getMenuExtensions().size());
            assertEquals(1, pe.getMenuExtensions().get(0).getElements().size());

            assertEquals(1, pe.getLaunchpadExtensions().size());
            assertEquals(2, pe.getLaunchpadExtensions().get(0).getElements().size());

            assertEquals(1, pe.getToolbarExtensions().size());
            assertEquals(1, pe.getToolbarExtensions().get(0).getElements().size());

            assertEquals(1, pe.getViewsExtensions().size());
            assertEquals(1, pe.getViewsExtensions().get(0).getElements().size());
         }
      }
      assertTrue("Portal Framework perspective extension must be present.",
            foundFrameworkExt);

      assertTrue("BCC persepctive extension must be present.", foundBccExt);
   }
}
