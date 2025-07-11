package com.dprince.configuration.database.converters;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class ListOfLongConverter implements AttributeConverter<List<Long>, String> {
    @Override
    public String convertToDatabaseColumn(List<Long> longList) {
        if(longList==null || longList.isEmpty()) return null;
        return longList.parallelStream()
                .map(String::valueOf)
                .collect(Collectors.joining(DbConversion.LIST_DELIMITER));
    }

    @Override
    public List<Long> convertToEntityAttribute(String dbData) {
        if(StringUtils.isEmpty(dbData)) return null;
        return Arrays.asList(dbData.split(DbConversion.LIST_DELIMITER))
                .parallelStream().map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
