package org.eclipse.stardust.ui.web.viewscommon.test;

import static org.junit.Assert.*;

import org.junit.Test;

import org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl.CurrentVersion;

public class CurrentVersionTest
{
   @Test
   public void testCopyRight()
   {
      assertEquals("2014 SunGard CSA LLC and others, {0}", CurrentVersion.COPYRIGHT_MESSAGE);
   }
}
