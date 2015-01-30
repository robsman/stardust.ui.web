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
package org.eclipse.stardust.ui.web.business_object_management.service;

import org.eclipse.stardust.engine.api.query.*;

/**
 * Query container for building complex queries for data.
 *
 * @author roland.stamm
 *
 */
public class BusinessObjectQuery extends Query
{
   private static final long serialVersionUID = 1L;

   public static final String MODEL_ID_ATTRIBUTE = "modelId";
   public static final String ID_ATTRIBUTE = "businessObjectId";
   public static final String PK_ATTRIBUTE = "primaryKey";

   public static enum Option
   {
      WITH_DESCRIPTION, WITH_VALUES
   }

   public static class Policy implements EvaluationPolicy
   {
      private static final long serialVersionUID = 1L;

      private Option[] options;

      public Policy(Option... options)
      {
         this.options = options;
      }

      public boolean hasOption(Option option)
      {
         if (options != null)
         {
            for (Option opt : options)
            {
               if (opt == option)
               {
                  return true;
               }
            }
         }
         return false;
      }
   }

   /**
    * Attribute to filter for the business object id.
    */
   private static final FilterableAttribute BUSINESS_OBJECT_ID = new FilterableAttributeImpl(
         BusinessObjectQuery.class, ID_ATTRIBUTE);

   /**
    * Attribute to filter for the business object primary key.
    */
   private static final FilterableAttribute PRIMARY_KEY = new FilterableAttributeImpl(
         BusinessObjectQuery.class, PK_ATTRIBUTE);

   /**
    * Attribute to filter for a specific model. <br>
    * <b>Please Note: </b>Currently only supports one single Operator.isEqual(modelId) term to
    * filter for exactly one modelId.
    *
    * @see {@link #findAllForModel(String)}
    * @see {@link #findUsedInProcess(String, String)}
    *
    */
   private static final FilterableAttribute MODEL_ID = new FilterableAttributeImpl(
         BusinessObjectQuery.class, MODEL_ID_ATTRIBUTE);

   public static final FilterVerifier FILTER_VERIFYER = new FilterScopeVerifier(
         new WhitelistFilterVerifyer(new Class[] {
               FilterTerm.class,
               UnaryOperatorFilter.class,
               BinaryOperatorFilter.class,
               TernaryOperatorFilter.class,
               DataFilter.class
         }), BusinessObjectQuery.class);

   private BusinessObjectQuery()
   {
      super(FILTER_VERIFYER);
   }

   /**
    * Creates a query for finding all business objects.
    *
    * @return The configured query.
    */
   public static BusinessObjectQuery findAll()
   {
      return new BusinessObjectQuery();
   }

   public static BusinessObjectQuery findInModel(String modelId)
   {
      BusinessObjectQuery query = findAll();
      query.where(MODEL_ID.isEqual(modelId));
      return query;
   }

   public static BusinessObjectQuery findForBusinessObject(String modelId, String businessObjectId)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findInModel(modelId);
      query.where(BUSINESS_OBJECT_ID.isEqual(businessObjectId));
      return query;
   }

   public static BusinessObjectQuery findWithPrimaryKey(String modelId, String businessObjectId, Object pk)
   {
      BusinessObjectQuery query = BusinessObjectQuery.findInModel(modelId);
      query.where(BUSINESS_OBJECT_ID.isEqual(businessObjectId));
      query.where(pk instanceof Number
            ? PRIMARY_KEY.isEqual(((Number) pk).longValue())
            : PRIMARY_KEY.isEqual(pk.toString()));
      return query;
   }
}
