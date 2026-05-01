/*
 * Derived from BetterBlockEntities (LGPL-3.0). See com.rootbeerutils.client.bbe.BBE for details.
 */
package com.rootbeerutils.client.bbe.config;

public final class EnumTypes {

    private EnumTypes() {
    }

    public enum UpdateSchedulerType {
        FAST,
        SMART;

        public static UpdateSchedulerType map(int value) {
            return (value == 0) ? FAST : SMART;
        }

        public static int map(UpdateSchedulerType type) {
            return (type == FAST) ? 0 : 1;
        }
    }

    public enum BannerGraphicsType {
        FAST,
        FANCY;

        public static BannerGraphicsType map(int value) {
            return (value == 0) ? FAST : FANCY;
        }

        public static int map(BannerGraphicsType type) {
            return (type == FAST) ? 0 : 1;
        }
    }
}
