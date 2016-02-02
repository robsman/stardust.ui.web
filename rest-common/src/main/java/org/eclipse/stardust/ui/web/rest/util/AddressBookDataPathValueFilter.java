/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.AddressBookDataPathValueDTO;
import org.eclipse.stardust.ui.web.viewscommon.core.EMailAddressValidator;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class AddressBookDataPathValueFilter implements IDataPathValueFilter
{
   private static final String FAX_PATTERN = "^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$";
   //Note: from Correspondence Configuration ^\(?([0-9]{3})\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$
   
   private String faxFormat = FAX_PATTERN;

   public AddressBookDataPathValueFilter(String faxFormat)
   {
      if (StringUtils.isNotEmpty(faxFormat))
      {
         this.faxFormat = faxFormat;
      }
   }

   @Override
   public List< ? extends AbstractDTO> filter(DataPath dataPath, Object dataValue)
   {
      List<AddressBookDataPathValueDTO> addressBook = new ArrayList<AddressBookDataPathValueDTO>();

      if (dataValue != null)
      {
         searchAddresses(dataPath.getId(), dataValue, addressBook);
      }
      return addressBook;
   }

   /**
    * @param key
    * @param dataValue
    * @param addressBook
    */
   @SuppressWarnings("rawtypes")
   public void searchAddresses(String key, Object dataValue, List<AddressBookDataPathValueDTO> addressBook)
   {
      if (dataValue != null)
      {
         if (isFaxNumber(dataValue.toString()) || EMailAddressValidator.validateEmailAddress(dataValue.toString()))
         {
            AddressBookDataPathValueDTO addressBookDTO = new AddressBookDataPathValueDTO();
            addressBookDTO.value = dataValue.toString();
            addressBookDTO.name = key;
            addressBook.add(addressBookDTO);
            if (isFaxNumber(dataValue.toString()))
            {
               addressBookDTO.type = AddressBookDataPathValueDTO.DataValueType.fax.name();
            }
         }
         else
         {
            // check if the value is map
            if (dataValue != null && dataValue instanceof Map)
            {
               Map dataValMap = (Map) dataValue;
               Iterator itr = dataValMap.entrySet().iterator();
               while (itr.hasNext())
               {
                  Map.Entry pair = (Map.Entry) itr.next();
                  searchAddresses(key + "." + pair.getKey(), pair.getValue(), addressBook);
               }
            }
         }
      }
   }

   /**
    * @param dataValue
    * @return
    */
   private boolean isFaxNumber(String dataValue)
   {
      Pattern faxNumber = Pattern.compile(faxFormat);
      Matcher faxNumberMatcher = faxNumber.matcher(dataValue);

      if (faxNumberMatcher.matches())
      {
         return true;
      }
      return false;
   }
}
