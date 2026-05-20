package com.salonnbooking.ui;

import java.util.function.BiConsumer;

/**
 * Simple global router for Swing screens (CardLayout keys) so panels can request navigation
 * without keeping a direct reference to MainFrame.
 */
public final class ScreenRouter {
    private static volatile BiConsumer<String, String> navigator;

    private ScreenRouter() {}

    public static void setNavigator(BiConsumer<String, String> nav) {
        navigator = nav;
    }

    public static void go(String screenKey, String screenTitle) {
        BiConsumer<String, String> nav = navigator;
        if (nav != null) nav.accept(screenKey, screenTitle);
    }
}

