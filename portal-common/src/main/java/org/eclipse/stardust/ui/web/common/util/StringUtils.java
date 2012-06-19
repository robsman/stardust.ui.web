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
package org.eclipse.stardust.ui.web.common.util;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.ui.web.common.util.CollectionUtils.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author subodh.godbole
 *
 */
public class StringUtils
{
   /**
    * @param string
    * @return
    */
   public static boolean isEmpty(String string)
   {
      return (null == string) || (0 == string.length());
   }
   
   /**
    * @param string
    * @return
    */
   public static boolean isNotEmpty(String string)
   {
      return !isEmpty(string);
   }
   
   /**
    * @param <T>
    * @param set
    * @return
    */
   public static <T> boolean isEmpty(Set<T> set)
   {
      return (null == set) || (0 == set.size());
   }
   
   /**
    * @param <T>
    * @param list
    * @return
    */
   public static <T> boolean isEmpty(List<T> list)
   {
      return (null == list) || (0 == list.size());
   }

   /**
    * @param source
    * @param repl
    * @param with
    * @return
    */
   public static String replace(String source, String repl, String with)
   {
      if (source == null)
      {
         return null;
      }

      StringBuilder buf = new StringBuilder(source.length());
      int start = 0, end;
      while ((end = source.indexOf(repl, start)) != -1)
      {
         buf.append(source.substring(start, end)).append(with);
         start = end + repl.length();
      }
      buf.append(source.substring(start));
      return buf.toString();
   }

   /**
    * @param lhs
    * @param rhs
    * @param joinToken
    * @return
    */
   public static String join(String lhs, String rhs, String joinToken)
   {
      StringBuilder buffer = new StringBuilder((lhs == null ? 0 : lhs.length()) +
            (rhs == null ? 0 : rhs.length()) +
            (joinToken == null ? 0 : joinToken.length()));

      if (!isEmpty(lhs))
      {
         buffer.append(lhs);
      }
      
      if (!isEmpty(rhs))
      {
         if (0 < buffer.length())
         {
            buffer.append(joinToken);
         }
         buffer.append(rhs);
      }
      
      return buffer.toString();
   }

   /**
    * @param parts
    * @param joinToken
    * @return
    */
   public static String join(Iterator<?> parts, String joinToken)
   {
      if (parts.hasNext())
      {
         StringBuilder buffer = new StringBuilder();

         String token = "";
         while (parts.hasNext())
         {
            buffer.append(token).append(parts.next());
            token = joinToken;
         }

         return buffer.toString();
      }
      else
      {
         return "";
      }
   }
   
 
   /**
    * 
    * @param joinToken
    * @param parts
    * @return
    */
   public static String join(String joinToken, String... parts)
   {
      if (null != parts)
      {
         StringBuilder builder = new StringBuilder();
         String token = "";
         for (String p : parts)
         {
            builder.append(token).append(p);
            token = joinToken;
         }
         return builder.toString();
      }
      else
      {
         return "";
      }
   }
  
   /**
    * @param source
    * @param token
    * @return
    */
   public static final Set<String> splitUnique(String source, String token)
   {
      Set<String> list = new HashSet<String>();

      if(isNotEmpty(source))
      {
         String value;
         StringTokenizer st = new StringTokenizer(source, token);
         while(st.hasMoreTokens())
         {
            value = st.nextToken().trim();
            if(!list.contains(value))
            {
               list.add(value);
            }
         }
      }
      
      return list;
   }
   
   /**
    * @param source
    * @param token
    * @return
    */
   public static final List<String> splitAndKeepOrder(String source, String token)
   {
      List<String> list = new ArrayList<String>();

      if(isNotEmpty(source))
      {
         String value;
         StringTokenizer st = new StringTokenizer(source, token);
         while(st.hasMoreTokens())
         {
            value = st.nextToken().trim();
            list.add(value);
         }
      }
      
      return list;
   }

   /**
    * @param source
    * @param splitToken
    * @return
    */
   public static final Iterator<String> split(String source, char splitToken)
   {
      return split(source, splitToken, false);
   }

   /**
    * @param source
    * @param splitToken
    * @param includeSplitToken
    * @return
    */
   public static final Iterator<String> split(String source, char splitToken,
         boolean includeSplitToken)
   {
      if ( !isEmpty(source))
      {
         StringTokenizer token = new StringTokenizer(source,
               Character.valueOf(splitToken).toString(), includeSplitToken); 
         return new EnumerationIteratorWrapper<String>(new StringEnumeration(token));
      }
      else
      {
         return Collections.<String>emptyList().iterator();
      }
   }

   /**
    * @param source
    * @param splitToken
    * @return
    */
   public static final Iterator<String> split(String source, String splitToken)
   {
      return split(source, splitToken, false);
   }

   /**
    * @param source
    * @param splitToken
    * @param includeSplitToken
    * @return
    */
   public static final Iterator<String> split(String source, String splitToken,
         boolean includeSplitToken)
   {
      List<String> tokens;

      if ( !isEmpty(source))
      {
         tokens = newArrayList();

         int tokenStart = 0;
         int tokenEnd = source.indexOf(splitToken, tokenStart);
         while (-1 != tokenEnd)
         {
            tokens.add(source.substring(tokenStart, tokenEnd));

            if (includeSplitToken)
            {
               tokens.add(splitToken);
            }

            tokenStart = tokenEnd + splitToken.length();
            tokenEnd = source.indexOf(splitToken, tokenStart);
         }

         if (tokenStart < source.length())
         {
            tokens.add(source.substring(tokenStart));
         }

         return tokens.iterator();
      }
      else
      {
         tokens = emptyList();
      }
      
      return tokens.iterator();
   }

   /**
    * @param object1
    * @param object2
    * @return
    */
   public static boolean areEqual(Object object1, Object object2)
   {
      if (object1 == null)
      {
         return object2 == null;
      }
      else
      {
         return object2 != null && object1.equals(object2);
      }
   }
   
   /**
    * Alternates the case of the first letter of the give string.
    *  
    * @param field
    * @return
    */
   public static String alternateFirstLetterCase(String field)
   {
      String firstLetter = field.substring(0, 1);
      if (firstLetter.equals(firstLetter.toLowerCase()))
      {
         firstLetter = firstLetter.toUpperCase();
      }
      else
      {
         firstLetter = firstLetter.toLowerCase();
      }
      
      return firstLetter + field.substring(1);
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   private static class StringEnumeration implements Enumeration<String>
   {
      private StringTokenizer tokenizer;
      
      public StringEnumeration(StringTokenizer tokenizer)
      {
         this.tokenizer = tokenizer;
      }
      
      public boolean hasMoreElements()
      {
         return tokenizer.hasMoreElements();
      }

      public String nextElement()
      {
         return tokenizer.nextToken();
      }
   }
}
