package com.rootbeerutils.client.quickpack;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Drop-in replacement for vanilla's {@code FilePackResources} that holds the pack open as a
 * single {@link ZipFile} and indexes its entries once into a sorted tree, so listing resources
 * uses {@link TreeSet#subSet(Object, Object)} (logarithmic) instead of scanning every entry.
 *
 * <p>Hot path tradeoff: vanilla re-opens the zip per query and walks every entry; this class
 * pays the indexing cost once per pack on the first read, then answers all later reads and
 * listings in O(log n + k).
 */
public class FastFilePackResources extends AbstractPackResources {

    public static final Logger LOGGER = LogUtils.getLogger();

    private TreeSet<String> fileTree = new TreeSet<>();
    private Map<String, Set<String>> namespaces = new HashMap<>();
    private ZipFile zipFile;

    /**
     * Overlay path prefixes (priority-ordered, highest first), with a final {@code ""} base entry.
     */
    private final List<String> prefixStack;
    private final Set<String> overlays;
    private boolean extracted;

    public FastFilePackResources(PackLocationInfo packLocationInfo, ZipFile zipFile, List<String> overlays) {
        super(packLocationInfo);

        this.zipFile = zipFile;
        this.overlays = new HashSet<>(overlays);
        this.prefixStack = new ArrayList<>(overlays.size() + 1);

        // Reverse-iterate so later overlays are checked first.
        for (int i = overlays.size() - 1; i >= 0; i--) {
            this.prefixStack.add(overlays.get(i) + "/");
        }

        this.prefixStack.add("");
    }

    /**
     * One-shot zip walk: indexes paths into the file tree and bins namespaces by pack type.
     */
    private void ensureFileTree() {
        if (this.extracted) {
            return;
        }

        this.extracted = true;

        if (this.zipFile == null) {
            return;
        }

        Enumeration<? extends ZipEntry> entries = this.zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String path = entry.getName();
            if (entry.isDirectory()) {
                path = path.substring(0, path.length() - 1);
            }

            extractNamespace(path);
            this.fileTree.add(path);
        }
    }

    private void extractNamespace(String path) {
        String[] parts = path.split("/");
        if (parts.length == 0) return;
        boolean isOverlay = this.overlays.contains(parts[0]);

        String type, namespace;
        if (isOverlay && parts.length >= 3) {
            type = parts[1];
            namespace = parts[2];
        } else if (!isOverlay && parts.length >= 2) {
            type = parts[0];
            namespace = parts[1];
        } else {
            return;
        }

        if (!Identifier.isValidNamespace(namespace)) {
            LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring",
                        namespace, this.zipFile);
            return;
        }

        this.namespaces.computeIfAbsent(type, _ -> new HashSet<>()).add(namespace);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NonNull ... parts) {
        return getResource(String.join("/", parts));
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NonNull PackType packType, @NonNull Identifier resourceLocation) {
        for (String prefix : this.prefixStack) {
            IoSupplier<InputStream> supplier = getResource(
                prefix + packType.getDirectory() + "/" + resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
            if (supplier != null) {
                return supplier;
            }
        }

        return null;
    }

    private @Nullable IoSupplier<InputStream> getResource(String path) {
        if (this.zipFile == null) {
            return null;
        }

        ZipEntry entry = this.zipFile.getEntry(path);

        if (entry == null) {
            return null;
        }

        return IoSupplier.create(this.zipFile, entry);
    }

    @Override
    public void listResources(@NonNull PackType packType, @NonNull String namespace, @NonNull String path,
                              PackResources.@NonNull ResourceOutput resourceOutput) {
        ensureFileTree();
        Map<Identifier, IoSupplier<InputStream>> map = new HashMap<>();
        for (String prefix : this.prefixStack) {
            String namespacePrefix = prefix + packType.getDirectory() + "/" + namespace + "/";
            String dirPrefix = namespacePrefix + path + "/";
            String end = dirPrefix + "?";
            this.fileTree.subSet(dirPrefix, end).forEach(filePath -> {
                String rlPath = filePath.substring(namespacePrefix.length());
                Identifier location = Identifier.tryBuild(namespace, rlPath);
                if (location == null) {
                    LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, rlPath);
                    return;
                }

                map.putIfAbsent(location, getResource(filePath));
            });
        }

        map.forEach(resourceOutput);
    }

    @Override
    public @NonNull Set<String> getNamespaces(PackType packType) {
        ensureFileTree();
        return this.namespaces.getOrDefault(packType.getDirectory(), Collections.emptySet());
    }

    @Override
    public void close() {
        if (this.zipFile == null) {
            return;
        }

        IOUtils.closeQuietly(this.zipFile);

        this.zipFile = null;
        this.namespaces = null;
        this.fileTree = null;
    }
}
