package ru.malygin.crawler.model.entity;

import java.io.Serializable;

public interface BaseEntity extends Serializable {
    boolean hasRequiredField();
}
