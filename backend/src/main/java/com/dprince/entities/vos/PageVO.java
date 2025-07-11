package com.dprince.entities.vos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * * @author Chris Ndayishimiye
 * * @created 8/13/23
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVO {
    private List<?> content = new ArrayList<>();

    private int totalPages = 0;

    private long totalElements = 0;

    private int currentPage = 1;

    @JsonIgnore
    @Transient
    public static PageVO getPageVO(@Nullable Page<?> page){
        PageVO pageVO = new PageVO();
        if(page!=null) {
            pageVO.setTotalPages(page.getTotalPages());
            pageVO.setCurrentPage(page.getNumber());
            pageVO.setContent(page.getContent());
            pageVO.setTotalElements(page.getTotalElements());
        }
        return pageVO;
    }
}
