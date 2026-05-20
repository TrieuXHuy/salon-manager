package com.salonnbooking.ui.theme;

import java.awt.Color;
import java.awt.Font;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;

public class Theme {
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
    
    public static final Font FONT_HERO = new Font(FONT_NAME, Font.BOLD, 28);
    public static final Font FONT_H1 = new Font(FONT_NAME, Font.BOLD, 18);
    public static final Font FONT_H2 = new Font(FONT_NAME, Font.BOLD, 16);
    public static final Font FONT_H3 = new Font(FONT_NAME, Font.BOLD, 14);
    public static final Font FONT_BODY_LG = new Font(FONT_NAME, Font.PLAIN, 13);
    public static final Font FONT_BODY_REG = new Font(FONT_NAME, Font.PLAIN, 12);
    public static final Font FONT_BODY_SM = new Font(FONT_NAME, Font.PLAIN, 11);
    
    /**
     * Scale a font dynamically if needed.
     */
    public static Font scaleFont(Font base) {
        if (base == null) {
            return null;
        }
        return base;
    }
    
    /**
     * Set up FlatLaf theme and UIManager properties (bo góc 8-12px, default 10px).
     */
    public static void setupTheme() {
        try {
            // Setup FlatIntelliJLaf as requested
            FlatIntelliJLaf.setup();
            
            // Set rounded corners (8-12px) - using 10px
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("CheckBox.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("ScrollBar.arc", 10);
            
            // Title pane corner rounding for dialogs
            UIManager.put("TitlePane.arc", 10);
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
}
