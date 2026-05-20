package com.salonnbooking.ui;

import java.util.function.Consumer;
import java.util.function.BiConsumer;

/**
 * Simple global router for Swing screens (CardLayout keys) so panels can request navigation
 * without keeping a direct reference to MainFrame.
 */
public final class ScreenRouter {
    private static volatile BiConsumer<String, String> navigator;
    private static volatile Consumer<String> navigatorKeyOnly;

    private ScreenRouter() {}

    public static void setNavigator(BiConsumer<String, String> nav) {
        navigator = nav;
    }

    public static void setNavigator(Consumer<String> nav) {
        navigatorKeyOnly = nav;
    }

    public static void go(String screenKey, String screenTitle) {
        BiConsumer<String, String> nav = navigator;
        if (nav != null) nav.accept(screenKey, screenTitle);
    }

    public static void go(String screenKey) {
        Consumer<String> nav = navigatorKeyOnly;
        if (nav != null) {
            nav.accept(screenKey);
            return;
        }
        BiConsumer<String, String> nav2 = navigator;
        if (nav2 != null) nav2.accept(screenKey, screenKey);
    }
}
