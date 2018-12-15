package com.ifillbrito.idea.immutable.dataholders;

import org.apache.commons.lang.StringUtils;

class ClassField {
    private String type;
    private String name;

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    String getPlainName() {
        return name;
    }

    String getCapitalizedName() {
        return StringUtils.capitalize(name);
    }

    void setName(String name) {
        this.name = name;
    }
}
