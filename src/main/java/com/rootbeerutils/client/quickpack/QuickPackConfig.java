package com.rootbeerutils.client.quickpack;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class QuickPackConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("rootbeerutils/quickpack");
    private static final String FILE_NAME   = "rootbeerutils-quickpack.properties";
    private static final String KEY_ENABLED = "enabled";

    private static volatile boolean enabled = true;
    private static volatile boolean loaded  = false;

    private QuickPackConfig() {
    }

    /**
     * @return whether QuickPack is currently enabled.
     *
     * <p>Suppression note: IntelliJ flags this with {@code BooleanMethodIsAlwaysInverted}
     * because the two current callers both negate the result ({@code !isEnabled()}). Inverting
     * to {@code isDisabled()} would contradict the rest of the user-facing model — the chat
     * overlay reads "QuickPack enabled / disabled", the lang keys are
     * {@code quickpack.enabled / disabled}, and the on-disk property is {@code enabled=true}.
     * The negation pattern at the call sites is idiomatic ("guard, then continue" / "toggle
     * to the opposite of current"), so the suppression stays here rather than the API getting
     * twisted.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEnabled() {
        if (!loaded) {
            load();
        }

        return enabled;
    }

    public static void setEnabled(boolean value) {
        if (!loaded) {
            load();
        }

        enabled = value;
        save();
    }

    private static synchronized void load() {
        if (loaded) {
            return;
        }

        Path path = configPath();
        if (!Files.exists(path)) {
            loaded = true;
            return;
        }

        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            props.load(reader);
        } catch (IOException e) {
            LOGGER.error("Failed to read {}", path, e);
            loaded = true;
            return;
        }

        String value = props.getProperty(KEY_ENABLED);
        if (value != null) {
            enabled = Boolean.parseBoolean(value);
        }

        loaded = true;
    }

    private static synchronized void save() {
        Path path = configPath();
        Properties props = new Properties();
        props.setProperty(KEY_ENABLED, Boolean.toString(enabled));
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                props.store(writer, "RootBeer-Utils QuickPack settings");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write {}", path, e);
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }
}
