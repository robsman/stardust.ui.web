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

import org.eclipse.stardust.ui.web.common.util.SecurityUtils;

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
   
   public void debug(Throwable throwable)
   {
      logger.debug("", throwable);
   }   
   
   public void debug(Object o)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.debug(log);
   }

   public void debug(Object o, Throwable throwable)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.debug(log, throwable);
   }

   public void error(Throwable throwable)
   {
      logger.error("", throwable);
   }
   
   public void error(Object o)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.error(log);
   }

   public void error(Object o, Throwable throwable)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.error(log, throwable);
   }

   public void fatal(Throwable throwable)
   {
      logger.trace("", throwable);
   }
   
   public void fatal(Object o)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.trace(log);
   }

   public void fatal(Object o, Throwable throwable)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.trace(log, throwable);
   }

   public void info(Throwable throwable)
   {
      logger.info("", throwable);
   }
   
   public void info(Object o)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.info(log);
   }

   public void info(Object o, Throwable throwable)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.info(log, throwable);
   }

   public void warn(Throwable throwable)
   {
      logger.warn("", throwable);
   }
   
   public void warn(Object o)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.warn(log);
   }

   public void warn(Object o, Throwable throwable)
   {
      String log = SecurityUtils.sanitizeForLog(o.toString());
      logger.warn(log, throwable);
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
