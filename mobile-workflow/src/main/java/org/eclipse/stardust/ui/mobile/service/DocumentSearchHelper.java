package org.eclipse.stardust.ui.mobile.service;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.engine.api.query.DocumentQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;

/**
 * @author Shrikant.Gangal
 *
 */
public class DocumentSearchHelper
{
   /**
    * @author Shrikant.Gangal
    *
    */
   public static class DocumentSearchCriteria
   {
      private long createFromTimestamp;

      private long createToTimestamp;

      private List<String> documentTypeIds;

      private String searchText;

      private String sortKey;

      private int rowFrom;

      private int pageSize;
   }

   /**
    * @param searchText
    * @param createFromTimestamp
    * @param createToTimestamp
    * @param documentTypeIds
    * @param sortKey
    * @param rowFrom
    * @param pageSize
    * @return
    */
   public static DocumentSearchCriteria getDocumentSearchCriteria(String searchText,
         String createFromTimestamp, String createToTimestamp, String documentTypeIds,
         String sortKey, String rowFrom, String pageSize)
   {
      DocumentSearchCriteria criteria = new DocumentSearchCriteria();
      criteria.createFromTimestamp = SearchHelperUtil.stringToLong(createFromTimestamp, -1);
      criteria.createToTimestamp = SearchHelperUtil.stringToLong(createToTimestamp, -1);
      criteria.documentTypeIds = SearchHelperUtil.csvStringToList(documentTypeIds);
      criteria.searchText = searchText;
      criteria.sortKey = sortKey;
      criteria.rowFrom = SearchHelperUtil.stringToInt(rowFrom, -1);
      criteria.pageSize = SearchHelperUtil.stringToInt(pageSize, -1);

      return criteria;
   }

   /**
    * @param criteria
    * @return
    */
   public static DocumentQuery buildDocumentSearchQuery(DocumentSearchCriteria criteria)
   {
      DocumentQuery query = DocumentQuery.findAll();
      FilterAndTerm filter = query.where(DocumentQuery.NAME.like(QueryUtils.getFormattedString(criteria.searchText)));

      if ( -1 != criteria.createFromTimestamp && -1 != criteria.createToTimestamp)
      {
         filter.and(DocumentQuery.DATE_CREATED.between(criteria.createFromTimestamp,
               criteria.createToTimestamp));
      }

      if (criteria.documentTypeIds.size() > 0)
      {
         FilterOrTerm orTerm = filter.addOrTerm();
         for (String id : criteria.documentTypeIds)
         {
            orTerm.add(DocumentQuery.DOCUMENT_TYPE_ID.isEqual(id));
         }
      }

      if (criteria.rowFrom > -1 && criteria.pageSize > 0)
      {
         SubsetPolicy pagePolicy = new SubsetPolicy(criteria.pageSize, criteria.rowFrom,
               true);
         query.setPolicy(pagePolicy);
      }

      if (null != criteria.sortKey)
      {
         if (criteria.sortKey.equals("oldest"))
         {
            query.orderBy(DocumentQuery.DATE_CREATED, true);
         }
         else if (criteria.sortKey.equals("newest"))
         {
            query.orderBy(DocumentQuery.DATE_CREATED, false);
         }
         else if (criteria.sortKey.equals("modified"))
         {
            query.orderBy(DocumentQuery.DATE_LAST_MODIFIED, false);
         }
      }
      
      return query;
   }
}
