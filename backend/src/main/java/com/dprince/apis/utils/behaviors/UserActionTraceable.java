package com.dprince.apis.utils.behaviors;

public interface UserActionTraceable {
    void setDoneBy(Long userId);
    Long getDoneBy();
}
