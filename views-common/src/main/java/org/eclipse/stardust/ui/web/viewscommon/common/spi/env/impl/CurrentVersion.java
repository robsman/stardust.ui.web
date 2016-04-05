/*******************************************************************************
 * Copyright (c) 2011, 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;



/**
 * Represents the current version of Infinity including major, minor, micro,
 * branch and build number. It is set automatically by the build process based
 * on the <code>version.properties</code>.
 * 
 * This class is a copy of <code>org.eclipse.stardust.common.config.CurrentVersion</code>
 */
public class CurrentVersion
{
   private static final Logger trace = LogManager.getLogger(CurrentVersion.class);
   
   public static final String COPYRIGHT_YEARS = "2000-2016";
   public static final String VERSION;
   public static final String BUILD;
   public static final String COPYRIGHT_MESSAGE;

   static
   {
      // load version.properties from 
      // org.eclipse.stardust.ui.web.viewscommon.common.spi.env.impl package
      ResourceBundle versionBundle = ResourceBundle.getBundle(
            CurrentVersion.class.getPackage().getName() + ".version",
            Locale.getDefault(), CurrentVersion.class.getClassLoader());
      String version = versionBundle.getString("version");
      VERSION = version.replaceFirst("-.*SNAPSHOT", "");
      StringBuilder build = new StringBuilder(versionBundle.getString("build"));
      // if the version contains a snapshot identifier like -RC1-SNAPSHOT...
      if(!VERSION.equals(version))
      { 
         // ..put it to the build identifier so that the info isn't lost
         String snapshotAlias = version.replace(VERSION, "").replace("-SNAPSHOT", "");
         if(StringUtils.isNotEmpty(snapshotAlias))
         {
            if(snapshotAlias.charAt(0) != '-')
            {
               build.append("-");
            }
            build.append(snapshotAlias);
         }
      }
      BUILD = build.toString();
      COPYRIGHT_MESSAGE = versionBundle.getString("copyright.message");
   }
   
   /**
    * String representation in the form
    */
   public static String getVersionName()
   {
      return VERSION;
   }

   public static Version getVersion()
   {
      return new Version(getVersionName());
   }

   public static Version getBuildVersion()
   {
      try
      {
         return new Version(getBuildVersionName());
      }
      catch (NumberFormatException e)
      {
         trace.error("Could not retrieve Version Information " + e.getLocalizedMessage());
         return null;
      }
   }

   public static String getCopyrightMessage()
   {
      return  MessageFormat.format(COPYRIGHT_MESSAGE, new Object[] {
            getVersionName(), COPYRIGHT_YEARS});
   }

   public static String getBuildVersionName()
   {
      StringBuffer name = new StringBuffer(VERSION);

      name.append(".");
      name.append(BUILD);

      return name.toString();
   }

}
