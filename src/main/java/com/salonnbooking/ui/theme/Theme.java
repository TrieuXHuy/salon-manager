package com.salonnbooking.ui.theme;

import java.awt.Color;
import java.awt.Font;

public class Theme {
    // Global font scale
    public static final float FONT_SCALE = 1.5f;
    // Primary Colors
    public static final Color PRIMARY = new Color(109, 73, 224);
    public static final Color PRIMARY_HOVER = new Color(92, 62, 210);
    public static final Color ACCENT = new Color(237, 233, 255);
    
    // Background Colors
    public static final Color BG_MAIN = new Color(248, 250, 253);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = Color.WHITE;
    
    // Text Colors
    public static final Color TEXT_MAIN = new Color(30, 41, 59);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color TEXT_WHITE = Color.WHITE;
    
    // Status Colors
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color SUCCESS_BG = new Color(220, 252, 231);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color DANGER_BG = new Color(254, 226, 226);
    
    // Borders
    public static final Color BORDER = new Color(226, 232, 240);
    
    // Fonts
    public static final Font FONT_REGULAR = scaleFont(new Font("Segoe UI", Font.PLAIN, 14));
    public static final Font FONT_MEDIUM = scaleFont(new Font("Segoe UI", Font.PLAIN, 15));
    public static final Font FONT_BOLD = scaleFont(new Font("Segoe UI", Font.BOLD, 14));
    public static final Font FONT_H1 = scaleFont(new Font("Segoe UI", Font.BOLD, 24));
    public static final Font FONT_H2 = scaleFont(new Font("Segoe UI", Font.BOLD, 20));
    public static final Font FONT_H3 = scaleFont(new Font("Segoe UI", Font.BOLD, 18));

    public static Font scaleFont(Font base) {
        if (base == null) {
            return null;
        }
        return base.deriveFont(base.getSize2D() * FONT_SCALE);
    }
}
