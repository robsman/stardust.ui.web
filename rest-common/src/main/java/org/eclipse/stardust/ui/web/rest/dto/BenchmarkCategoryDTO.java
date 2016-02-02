package org.eclipse.stardust.ui.web.rest.dto;

import java.util.Set;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

@DTOClass
public class BenchmarkCategoryDTO extends AbstractDTO implements Comparable<BenchmarkCategoryDTO>
{

   public String color; 
   public int index;
   public String name;
   public Long count;
   public Set<Long> instanceOids;

   
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((color == null) ? 0 : color.hashCode());
      result = prime * result + ((count == null) ? 0 : count.hashCode());
      result = prime * result + index;
      result = prime * result + ((instanceOids == null) ? 0 : instanceOids.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }


   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      BenchmarkCategoryDTO other = (BenchmarkCategoryDTO) obj;
      if (color == null)
      {
         if (other.color != null)
            return false;
      }
      else if (!color.equals(other.color))
         return false;
      if (count == null)
      {
         if (other.count != null)
            return false;
      }
      else if (!count.equals(other.count))
         return false;
      if (index != other.index)
         return false;
      if (instanceOids == null)
      {
         if (other.instanceOids != null)
            return false;
      }
      else if (!instanceOids.equals(other.instanceOids))
         return false;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }


   @Override
   public int compareTo(BenchmarkCategoryDTO o)
   {
      int index = o.index;       
      //ascending order
      return this.index - index;
   }
}
