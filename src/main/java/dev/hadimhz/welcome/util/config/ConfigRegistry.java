package dev.hadimhz.welcome.util.config;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

public interface ConfigRegistry {

    <Type> Type register(Class<Type> clazz, Type instance, File file);

    void save(Object obj, File file);

    void save(Object obj);

    <Type> Optional<Type> load(Class<Type> clazz, File file);

    <Type> Optional<Type> load(Class<Type> clazz);

    Collection<Object> getConfigs();
}
