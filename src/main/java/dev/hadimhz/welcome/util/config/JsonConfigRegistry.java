package dev.hadimhz.welcome.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonConfigRegistry extends AbstractConfigRegistry {

    private static final Logger LOGGER = Logger.getLogger(JsonConfigRegistry.class.getName());
    private static final String EXT = ".json";
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().excludeFieldsWithModifiers(new int[]{8, 128, 64}).serializeNulls().disableHtmlEscaping().create();

    public JsonConfigRegistry() {
    }

    public void save(Object obj, File file) {
        this.trySave(obj, file, (f) -> {
            return f.getName().endsWith(EXT);
        }, (o, f) -> {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                GSON.toJson(obj, obj.getClass(), writer);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to save config to " + file.getPath(), e);
            }

        });
    }

    public <Type> Optional<Type> load(Class<Type> clazz, File file) {
        return Optional.ofNullable(this.tryLoad(clazz, file, (f) -> f.getName().endsWith(EXT),
                (f, instance) -> {
                    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                        return (Type) GSON.fromJson(reader, clazz);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Failed to load config from " + file.getPath(), e);
                        return null;
                    }
                }));
    }
}
