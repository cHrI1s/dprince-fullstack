package com.dprince.configuration.database.converters;

import com.dprince.entities.enums.AppFeature;
import com.dprince.entities.enums.CommunicationWay;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class ListOfAppFeatureConverter implements AttributeConverter<List<AppFeature>, String> {
    @Override
    public String convertToDatabaseColumn(List<AppFeature> longList) {
        if(longList==null || longList.isEmpty()) return null;
        List<String> strings = new ArrayList<>();
        longList.forEach(feature-> strings.add(feature.toString()));
        return String.join(DbConversion.LIST_DELIMITER, strings);
    }

    @Override
    public List<AppFeature> convertToEntityAttribute(String dbData) {
        if(StringUtils.isEmpty(dbData)) return null;
        List<String> strings = Arrays.asList(dbData.split(DbConversion.LIST_DELIMITER));
        return strings.stream()
                .map(AppFeature::valueOf)
                .collect(Collectors.toList());
    }
}
