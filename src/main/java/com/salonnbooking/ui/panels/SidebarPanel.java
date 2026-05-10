package com.salonnbooking.ui.panels;

import javax.swing.JLabel;
import javax.swing.JPanel;
import com.salonnbooking.ui.components.SidebarButton;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;
import java.awt.Dimension;

public class SidebarPanel extends JPanel {
    public SidebarPanel() {
        setBackground(Theme.BG_SIDEBAR);
        setPreferredSize(new Dimension(250, 0));
        setLayout(new MigLayout("wrap 1, fillx, insets 20 0 20 0", "[fill]"));

        // Logo / Title
        JLabel titleLabel = new JLabel("SalonManager");
        titleLabel.setFont(Theme.FONT_H1);
        titleLabel.setForeground(Theme.PRIMARY);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, "gapbottom 30, center");

        // Menu Items
        String[] menus = {
            "Dashboard",
            "Quản lý lịch hẹn",
            "Khách hàng",
            "Nhân viên",
            "Dịch vụ",
            "Thanh toán",
            "Thống kê"
        };

        boolean first = true;
        for (String menu : menus) {
            SidebarButton btn = new SidebarButton("  " + menu);
            if (first) {
                btn.setActive(true);
                first = false;
            }
            add(btn, "h 45!, gapt 5");
        }
    }
}
