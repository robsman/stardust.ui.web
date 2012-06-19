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
package org.eclipse.stardust.ui.web.common.log;

/**
 * @author subodh.godbole
 *
 */
public class DefaultLogger implements Logger
{
   private org.slf4j.Logger logger;
   
   /**
    * @param logger
    */
   public DefaultLogger(org.slf4j.Logger logger)
   {
      this.logger = logger;
   }
   
   public void debug(Object o)
   {
      logger.debug(o.toString());
   }

   public void debug(Object o, Throwable throwable)
   {
      logger.debug(o.toString(), throwable);
   }

   public void error(Object o)
   {
      logger.error(o.toString());
   }

   public void error(Object o, Throwable throwable)
   {
      logger.error(o.toString(), throwable);
   }

   public void fatal(Object o)
   {
      logger.trace(o.toString());
   }

   public void fatal(Object o, Throwable throwable)
   {
      logger.trace(o.toString(), throwable);
   }

   public void info(Object o)
   {
      logger.info(o.toString());
   }

   public void info(Object o, Throwable throwable)
   {
      logger.info(o.toString(), throwable);
   }

   public void warn(Object o)
   {
      logger.warn(o.toString());
   }

   public void warn(Object o, Throwable throwable)
   {
      logger.warn(o.toString(), throwable);
   }

   public boolean isDebugEnabled()
   {
      return logger.isDebugEnabled();
   }

   public boolean isInfoEnabled()
   {
      return logger.isInfoEnabled();
   }
}
