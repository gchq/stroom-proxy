package stroom.proxy.util.config;

import javax.xml.bind.annotation.XmlElement;

public class Parameter {
    private String name;
    private String value;
    private String regEx;
    private String description;

    public Parameter() {
    }

    public Parameter(final String name, final String value, final String description, final String regEx) {
        this.name = name;
        this.value = value;
        this.description = description;
        this.regEx = regEx;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @XmlElement
    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean validate() {
        if (getRegEx() != null && getRegEx().length() > 0) {
            return getValue().matches(getRegEx());
        }
        return true;
    }

}
