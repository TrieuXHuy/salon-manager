package com.salonnbooking.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class Theme {
    // DPI Scaling factor (will be set at runtime)
    private static float dpiScaleFactor = 1.0f;
    
    // Primary Colors
    public static final Color NAVY = new Color(13, 27, 42);          // #0D1B2A
    public static final Color EMERALD = new Color(16, 185, 129);      // #10B981
    public static final Color EMERALD_HOVER = new Color(19, 168, 115); // #13a873
    public static final Color EMERALD_PRESSED = new Color(13, 138, 96); // #0d8a60
    
    // Secondary Colors
    public static final Color AMBER = new Color(245, 158, 11);        // #F59E0B
    public static final Color CRIMSON = new Color(220, 38, 38);       // #DC2626
    public static final Color SLATE = new Color(100, 116, 139);        // #64748B
    
    // Neutral Colors
    public static final Color BG_MAIN = new Color(242, 244, 246);      // #F2F4F6
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = new Color(13, 27, 42);      // #0D1B2A (Navy)
    
    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);    // #111827
    public static final Color TEXT_MUTED = new Color(107, 114, 128);   // #6B7280
    public static final Color TEXT_WHITE = Color.WHITE;
    
    // Additional Colors
    public static final Color BLUE = new Color(59, 130, 246);          // #3B82F6
    public static final Color GREEN = new Color(34, 197, 94);          // #22C55E
    public static final Color PURPLE = new Color(168, 85, 247);        // #A855F7
    public static final Color BORDER = new Color(229, 231, 235);        // #E5E7EB
    
    // Active Sidebar Highlight
    public static final Color SIDEBAR_ACTIVE_BG = new Color(26, 43, 63); // Subtle highlight
    
    // Fonts (Segoe UI as recommended, fallback to SansSerif)
    private static final String FONT_NAME = "Segoe UI";
    
    // Base font sizes (will be scaled by DPI factor)
    private static final int SIZE_HERO = 28;
    private static final int SIZE_H1 = 18;
    private static final int SIZE_H2 = 16;
    private static final int SIZE_H3 = 14;
    private static final int SIZE_BODY_LG = 13;
    private static final int SIZE_BODY_REG = 12;
    private static final int SIZE_BODY_SM = 11;
    
    // Scaled fonts (initialized after DPI factor is set)
    public static Font FONT_HERO;
    public static Font FONT_H1;
    public static Font FONT_H2;
    public static Font FONT_H3;
    public static Font FONT_BODY_LG;
    public static Font FONT_BODY_REG;
    public static Font FONT_BODY_SM;
    
    public static void setDPIScaleFactor(float scaleFactor) {
        dpiScaleFactor = clampScale(scaleFactor);
        // Re-initialize fonts with scaling applied
        initializeFonts();
    }
    
    private static void initializeFonts() {
        FONT_HERO = new Font(FONT_NAME, Font.BOLD, Math.round(SIZE_HERO * dpiScaleFactor));
        FONT_H1 = new Font(FONT_NAME, Font.BOLD, Math.round(SIZE_H1 * dpiScaleFactor));
        FONT_H2 = new Font(FONT_NAME, Font.BOLD, Math.round(SIZE_H2 * dpiScaleFactor));
        FONT_H3 = new Font(FONT_NAME, Font.BOLD, Math.round(SIZE_H3 * dpiScaleFactor));
        FONT_BODY_LG = new Font(FONT_NAME, Font.PLAIN, Math.round(SIZE_BODY_LG * dpiScaleFactor));
        FONT_BODY_REG = new Font(FONT_NAME, Font.PLAIN, Math.round(SIZE_BODY_REG * dpiScaleFactor));
        FONT_BODY_SM = new Font(FONT_NAME, Font.PLAIN, Math.round(SIZE_BODY_SM * dpiScaleFactor));
    }
    
    /**
     * Scale a font dynamically if needed.
     */
    public static Font scaleFont(Font base) {
        if (base == null) {
            return null;
        }
        return base.deriveFont(base.getSize2D() * dpiScaleFactor);
    }
    
    /**
     * Get the current DPI scale factor
     */
    public static float getDPIScaleFactor() {
        return dpiScaleFactor;
    }
    
    /**
     * Scale a dimension (width/height) based on DPI
     */
    public static int scaleDimension(int baseDimension) {
        return Math.round(baseDimension * dpiScaleFactor);
    }
    
    /**
     * Set up FlatLaf theme and UIManager properties (bo góc 8-12px, default 10px).
     */
    public static void setupTheme() {
        try {
            initializeDPIScaleFactor();
            if (FONT_BODY_REG == null) {
                initializeFonts();
            }

            // Setup FlatIntelliJLaf as requested
            FlatIntelliJLaf.setup();
            
            // Set rounded corners (8-12px) - using 10px
            UIManager.put("Button.arc", scaleDimension(10));
            UIManager.put("Component.arc", scaleDimension(10));
            UIManager.put("TextComponent.arc", scaleDimension(10));
            UIManager.put("CheckBox.arc", scaleDimension(10));
            UIManager.put("ProgressBar.arc", scaleDimension(10));
            UIManager.put("ScrollBar.arc", scaleDimension(10));
            
            // Title pane corner rounding for dialogs
            UIManager.put("TitlePane.arc", scaleDimension(10));
            UIManager.put("TitlePane.unifiedBackground", true);
            
            // Global Colors customization in Look and Feel
            UIManager.put("Panel.background", BG_MAIN);
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
            UIManager.put("Table.selectionBackground", new Color(219, 242, 241)); // Light emerald tint #DBF2F1 equivalent
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("TableHeader.background", BG_CARD);
            UIManager.put("TableHeader.foreground", TEXT_MUTED);
            UIManager.put("TableHeader.bottomSeparatorColor", BORDER);
            UIManager.put("Component.borderColor", BORDER);
            UIManager.put("Component.focusColor", EMERALD);
            
            // Tooltip and menus
            UIManager.put("ToolTip.background", BG_CARD);
            UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
            
            // Fonts customization
            UIManager.put("defaultFont", FONT_BODY_REG);
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
    }

    private static void initializeDPIScaleFactor() {
        if (FONT_BODY_REG != null) {
            return;
        }

        Float override = readScaleProperty("salon.ui.scale");
        if (override == null) {
            override = readScaleProperty("flatlaf.uiScale");
        }
        if (override != null) {
            dpiScaleFactor = clampScale(override);
            return;
        }

        float detectedScale = 1.0f;
        try {
            double transformScale = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getDefaultTransform()
                    .getScaleX();
            detectedScale = Math.max(detectedScale, (float) transformScale);
        } catch (Exception ignored) {
        }

        try {
            float dpiScale = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0f;
            detectedScale = Math.max(detectedScale, dpiScale);
        } catch (Exception ignored) {
        }

        try {
            int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
            int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
            if (detectedScale < 1.25f && screenWidth >= 3200) {
                detectedScale = 2.0f;
            } else if (detectedScale < 1.25f && screenWidth >= 2400) {
                detectedScale = 1.5f;
            } else if (detectedScale < 1.25f && screenWidth >= 1900 && screenHeight >= 1000) {
                detectedScale = 1.5f;
            } else if (detectedScale < 1.25f && screenWidth >= 1600) {
                detectedScale = 1.25f;
            }
        } catch (Exception ignored) {
        }

        dpiScaleFactor = clampScale(detectedScale);
    }

    private static Float readScaleProperty(String propertyName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            String normalized = value.trim().toLowerCase()
                    .replace("x", "")
                    .replace("%", "");
            float scale = Float.parseFloat(normalized);
            if (value.contains("%")) {
                scale = scale / 100.0f;
            }
            return scale;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static float clampScale(float scaleFactor) {
        if (scaleFactor < 1.0f) {
            return 1.0f;
        }
        if (scaleFactor > 2.5f) {
            return 2.5f;
        }
        return scaleFactor;
    }
}
