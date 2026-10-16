package com.leon.framework.spi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class SpiLoader<T> {

    private static final String RESOURCE_DIRECTORY = "META-INF/leon/";

    private static final ConcurrentMap<Class<?>, SpiLoader<?>> LOADERS =
            new ConcurrentHashMap<>();

    private final Class<T> spiType;
    private final Map<String, Class<? extends T>> extensionClasses;
    private final ConcurrentMap<String, T> instances =
            new ConcurrentHashMap<>();

    private SpiLoader(Class<T> spiType) {
        this.spiType = spiType;
        this.extensionClasses = Map.copyOf(loadExtensionClasses());
    }

    @SuppressWarnings("unchecked")
    public static <T> SpiLoader<T> getLoader(Class<T> spiType) {
        Objects.requireNonNull(spiType, "SPI 接口不能为空");

        if (!spiType.isInterface()) {
            throw new IllegalArgumentException("SPI 类型必须是接口");
        }

        return (SpiLoader<T>) LOADERS.computeIfAbsent(
                spiType,
                SpiLoader::new
        );
    }

    public T getExtension(String name) {
        Class<? extends T> extensionClass = extensionClasses.get(name);

        if (extensionClass == null) {
            throw new IllegalArgumentException(
                    "未找到 SPI 扩展: " + name
            );
        }

        return instances.computeIfAbsent(name, key -> {
            try {
                return extensionClass.getConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "无法创建 SPI 扩展: " + name,
                        e
                );
            }
        });
    }

    public Iterable<String> getSupportedNames() {
        return Collections.unmodifiableSet(extensionClasses.keySet());
    }

    private Map<String, Class<? extends T>> loadExtensionClasses() {
        Map<String, Class<? extends T>> result = new HashMap<>();

        String resourceName =
                RESOURCE_DIRECTORY + spiType.getName();

        ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();

        if (classLoader == null) {
            classLoader = spiType.getClassLoader();
        }

        try {
            Enumeration<URL> resources =
                    classLoader.getResources(resourceName);

            if(!resources.hasMoreElements()){
                throw new IllegalStateException("路径下没有资源：" + resourceName);
            }

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                try (InputStream input = resource.openStream()) {
                    Properties properties = new Properties();
                    properties.load(input);

                    for (String name : properties.stringPropertyNames()) {
                        String className =
                                properties.getProperty(name).trim();

                        Class<?> rawClass =
                                Class.forName(
                                        className,
                                        false,
                                        classLoader
                                );

                        if (!spiType.isAssignableFrom(rawClass)) {
                            throw new IllegalStateException(
                                    className + " 没有实现 "
                                            + spiType.getName()
                            );
                        }

                        if (Modifier.isAbstract(rawClass.getModifiers())) {
                            throw new IllegalStateException(
                                    className + " 不能是抽象类"
                            );
                        }

                        Class<? extends T> extensionClass =
                                rawClass.asSubclass(spiType);

                        // 提前验证是否存在 public 无参构造器。
                        extensionClass.getConstructor();

                        Class<? extends T> previous =
                                result.putIfAbsent(name, extensionClass);

                        if (previous != null) {
                            throw new IllegalStateException(
                                    "SPI 扩展名重复: " + name
                            );
                        }
                    }
                } catch (ClassNotFoundException e){
                    throw new IllegalStateException("没有类",e);
                } catch (NoSuchMethodException e){
                    throw new IllegalStateException("类没有构造方法",e);
                }
            }

            return result;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "加载 SPI 配置失败: " + resourceName,
                    e
            );
        }
    }
}