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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.PerformerType;

//import org.eclipse.stardust.engine.api.runtime.PerformerType;

/**
 * @author sauer
 * @version $Revision: $
 */
public interface IDelegatesProvider
{
   /**
    * Indicates that the set of valid delegates may contain real users.
    */
   public final static Integer USER_TYPE = new Integer(1);

   /**
    * Indicates that the set of valid delegates may contain roles from the BPM model.
    */
   public final static Integer ROLE_TYPE = new Integer(2);

   /**
    * Indicates that the set of valid delegates may contain organizations from the BPM
    * model.
    */
   public final static Integer ORGANIZATION_TYPE = new Integer(3);
   
   
   public final static Integer DEPARTMENT_TYPE = new Integer(4);
   
   /**
    * Indicates that the predefined <code>ADMINISTRATOR</code> role is not a valid
    * delegate, and should not be used to find valid users, too.
    * <p>
    * Any user being a member of the <code>ADMINISTRATOR</code> role plus an additional
    * qualifying role, too, should be included in the list of valid users, still.
    */
   public final static Integer DISABLE_ADMINISTRATOR_ROLE = new Integer(10);

   /**
    * Configuration object providing further specifics about the domain of valid
    * delegates.
    */
   interface Options
   {
      /**
       * Defines the set of valid performer categories. May be used to i.e.
       * <li>exclusively delegate to real users
       * <li>delegates to roles but not to user groups
       * <p>
       * An empty sets means any performer category is valid.
       * 
       * @return The set of valid performer categories.
       * 
       * @see #USER_TYPE
       * @see #ROLE_TYPE
       * @see #ORGANIZATION_TYPE
       * @see #DISABLE_ADMINISTRATOR_ROLE
       */
      Set<Integer> getPerformerTypes();

      /**
       * Indicates if the domain of delegates should strictly be calculated according to
       * the affected activities' default performers or not.
       * 
       * @return <code>true</code> if the search should be strict, <code>false</code> otherwise.
       */
      boolean isStrictSearch();

      /**
       * Optional predicate for restricting participant names. May contain <code>*</code> for wildcard matching.
       *  
       * @return The name filter (either the exact name or a pattern containing wildcards). 
       */
      String getNameFilter();
   }

   /**
    * 
    * @param activityInstances The list of activity instances being about to be delegated.
    * @param options Additional options further describing the domain of valid delegates.
    * @return A list of valid delegates per requested participant category.
    */
   Map<PerformerType, List<? extends ParticipantInfo>> findDelegates(
         List<ActivityInstance> activityInstances, Options options);
   
   interface Factory
   {
      IDelegatesProvider getProvider(List<ActivityInstance> activityInstances);
   }
}