package com.salonnbooking.ui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

public class HeaderPanel extends JPanel {
    public HeaderPanel() {
        setBackground(Theme.BG_CARD);
        setLayout(new MigLayout("insets 15 30 15 30, fillx", "[left]push[right]"));
        setBorder(new MatteBorder(0, 0, 1, 0, Theme.BORDER));

        JLabel title = new JLabel("Lịch hẹn hôm nay");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.TEXT_MAIN);
        add(title);

        JLabel userLabel = new JLabel("Admin 👤");
        userLabel.setFont(Theme.FONT_MEDIUM);
        userLabel.setForeground(Theme.TEXT_MUTED);
        add(userLabel);
    }
}
