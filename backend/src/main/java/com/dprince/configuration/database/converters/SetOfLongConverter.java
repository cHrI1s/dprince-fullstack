package com.dprince.configuration.database.converters;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class SetOfLongConverter  implements AttributeConverter<Set<Long>, String> {
    @Override
    public String convertToDatabaseColumn(Set<Long> longList) {
        if(longList==null || longList.isEmpty()) return null;
        return longList.parallelStream()
                .map(String::valueOf)
                .collect(Collectors.joining(DbConversion.LIST_DELIMITER));
    }

    @Override
    public Set<Long> convertToEntityAttribute(String dbData) {
        if(StringUtils.isEmpty(dbData)) return null;
        return Arrays.asList(dbData.split(DbConversion.LIST_DELIMITER))
                .parallelStream().map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}
