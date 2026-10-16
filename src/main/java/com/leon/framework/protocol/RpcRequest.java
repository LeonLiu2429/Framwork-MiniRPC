
package com.leon.framework.protocol;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String serviceName;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Object[] parameters;

    public RpcRequest(
            String serviceName,
            String methodName,
            Class<?>[] parameterTypes,
            Object[] parameters) {

        this.serviceName = serviceName;
        this.methodName = methodName;

        this.parameterTypes =
                parameterTypes == null
                        ? new Class<?>[0]
                        : parameterTypes.clone();

        this.parameters =
                parameters == null
                        ? new Object[0]
                        : parameters.clone();

        if (this.parameterTypes.length
                != this.parameters.length) {
            throw new IllegalArgumentException(
                    "参数类型与参数数量不一致");
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }

    public Object[] getParameters() {
        return parameters.clone();
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" +
                Arrays.toString(parameterTypes) +
                '}';
    }
}
