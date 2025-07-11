package com.dprince.configuration.database.converters;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;

@Converter(autoApply = true)
public class ListOfStringConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> longList) {
        if(longList==null || longList.isEmpty()) return null;
        return String.join(DbConversion.LIST_DELIMITER, longList);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if(StringUtils.isEmpty(dbData)) return null;
        return Arrays.asList(dbData.split(DbConversion.LIST_DELIMITER));
    }
}
