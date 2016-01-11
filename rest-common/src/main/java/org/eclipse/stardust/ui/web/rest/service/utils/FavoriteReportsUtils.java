package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.FavoriteReportDTO;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.springframework.stereotype.Component;

@Component
public class FavoriteReportsUtils
{

   public List<FavoriteReportDTO> getFavoriteReports()
   {
      HashMap<String, String> favoriteReports = RepositoryUtility.getFavoriteReports();
      List<FavoriteReportDTO> listfavoriteReports = buildFavoriteReports(favoriteReports);
      return listfavoriteReports;
   }

   private List<FavoriteReportDTO> buildFavoriteReports(HashMap<String, String> favoriteReports)
   {
      List<FavoriteReportDTO> listfavoriteReports = new ArrayList<FavoriteReportDTO>();
      if (CollectionUtils.isNotEmpty(favoriteReports))
      {
         for (String documentId : favoriteReports.keySet())
         {
            FavoriteReportDTO favoriteReportDTO = new FavoriteReportDTO();
            favoriteReportDTO.documentId = documentId;
            favoriteReportDTO.documentName = favoriteReports.get(documentId);
            listfavoriteReports.add(favoriteReportDTO);
         }
      }
      return listfavoriteReports;
   }

}
