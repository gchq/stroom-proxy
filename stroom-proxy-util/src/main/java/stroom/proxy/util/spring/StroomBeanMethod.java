package stroom.proxy.util.spring;

import java.lang.reflect.Method;

import stroom.proxy.util.shared.EqualsBuilder;
import stroom.proxy.util.shared.HashCodeBuilder;

public class StroomBeanMethod {
    private String beanName;
    private Class<?> beanClass;
    private final Method beanMethod;

    public StroomBeanMethod(final String beanName, final Method beanMethod) {
        this.beanName = beanName;
        this.beanMethod = beanMethod;
    }

    public StroomBeanMethod(final Class<?> clazz, final Method beanMethod) {
        this.beanClass = clazz;
        this.beanMethod = beanMethod;
    }

    public Method getBeanMethod() {
        return beanMethod;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        // for (Annotation annotation :beanMethod.getAnnotations()) {
        // builder.append("@");
        // builder.append(annotation.annotationType().getSimpleName());
        // builder.append(" ");
        // }
        builder.append(beanName);
        builder.append(".");
        builder.append(beanMethod.getName());
        return builder.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof StroomBeanMethod)) {
            return false;
        }
        final StroomBeanMethod other = (StroomBeanMethod) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.beanName, other.beanName);
        builder.append(this.beanClass, other.beanClass);
        builder.append(this.beanMethod, other.beanMethod);
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(this.beanName);
        builder.append(this.beanClass);
        builder.append(this.beanMethod);
        return builder.toHashCode();
    }
}
