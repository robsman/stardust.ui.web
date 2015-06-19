<<<<<<< Upstream, based on origin/feature/ipp/portal-html5-contrib
/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;


/**
 * 
 * @author Johnson.Quadras
 *
 */
public class ColumnDTO extends AbstractDTO
{

   public String id;
   
   public String label;

   public ColumnDTO(String id, String label)
   {
      super();
      this.id = id;
      this.label = label;
   }
   
   

}
=======
/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;


/**
 * 
 * @author Johnson.Quadras
 *
 */
public class ColumnDTO extends AbstractDTO
{

   public String id;
   
   public String label;

   public ColumnDTO(String id, String label)
   {
      super();
      this.id = id;
      this.label = label;
   }

}
>>>>>>> 30ff258 [CRNT-35275] : Added screen for postponed activities view. Bug: CRNT-35275
