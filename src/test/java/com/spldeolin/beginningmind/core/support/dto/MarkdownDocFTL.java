package com.spldeolin.beginningmind.core.support.dto;

import java.util.List;
import com.spldeolin.beginningmind.core.support.dto.annotation.ClassRequestMappingDTO;
import com.spldeolin.beginningmind.core.support.dto.annotation.DocClassDTO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <pre>
 * c  common
 * h  http
 * p  param
 * r  return
 * </pre>
 */
@Data
@Accessors(chain = true)
public class MarkdownDocFTL {

    private String commonDesc;

    private String httpUrl;

    private String httpMethod;

    private Boolean paramShow;

    private String paramJson;

    private List<PField> paramFields;

    private Boolean returnShow;

    private String returnJson;

    private Boolean returnFieldsShow;

    private List<RField> returnFields;

    private String commonDeveloper;

    private String commonDate;

    @Data
    public static class PField {

        private String paramName;

        private String paramPlace;

        private String paramRequired;

        private String paramType;

        private String paramDesc;

    }

    @Data
    public static class RField {

        private String returnName;

        private String returnType;

        private String returnDesc;

    }

    public void fromDocClassDTO(DocClassDTO dto) {
        commonDeveloper = dto.getDeveloper();
        commonDate = dto.getDate();
    }

    public void fromClassRequestMappingDTO(ClassRequestMappingDTO dto) {
        httpUrl = dto.getMapping();
    }

}
