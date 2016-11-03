package stroom.proxy.util.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "parameters")
public class ParameterFile {
    List<Parameter> parameter = new ArrayList<Parameter>();

    @XmlElement
    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

}
