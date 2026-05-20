package com.salonnbooking.ui.panels;

import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.ui.components.CircleAvatar;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingWizardPanel extends JPanel {

    private int currentStep = 0;
    private static final String[] STEP_LABELS = {"Chọn dịch vụ", "Chọn nhân viên", "Chọn ngày giờ", "Xác nhận"};

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final StepIndicatorPanel stepIndicator;
    private final JButton backBtn;
    private final JButton nextBtn;

    // State
    private final List<ServiceDtos.Response> selectedServices = new ArrayList<>();
    private Long selectedStaffId;
    private String selectedStaffName;
    private String selectedStaffRole;
    private LocalDate selectedDate = LocalDate.now();
    private LocalTime selectedTime;
    private String note = "";

    // Summary labels for step 1
    private JLabel summaryLabel;

    // Step 3 state
    private JButton selectedTimeButton;
    private JLabel dateLabel;

    // Step 4 widgets
    private JPanel confirmServicesPanel;
    private JLabel confirmStaffLabel;
    private JLabel confirmTimeLabel;
    private JLabel confirmTotalLabel;
    private JTextArea noteArea;

    private Runnable onBookingConfirmed;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    // Mock data
    private record StaffInfo(Long id, String name, String role, boolean active) {}

    private final List<StaffInfo> mockStaff = List.of(
        new StaffInfo(201L, "Trần Bình", "Stylist chính", true),
        new StaffInfo(202L, "Lê Thảo", "Stylist chính", true),
        new StaffInfo(203L, "Phạm Huy", "Thợ phụ", true),
        new StaffInfo(204L, "Nguyễn Mai", "Thợ phụ", false),
        new StaffInfo(205L, "Hoàng Kim", "Thu ngân", true),
        new StaffInfo(206L, "Đỗ Quốc Anh", "Stylist chính", true)
    );

    public BookingWizardPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("fill, wrap 1, insets 24", "[fill]", "[][grow][]"));

        // 1. Step Indicator
        stepIndicator = new StepIndicatorPanel();
        add(stepIndicator, "h 90!");

        // 2. Content Panel (CardLayout)
        contentPanel.setOpaque(false);
        contentPanel.add(createStep1(), "step1");
        contentPanel.add(createStep2(), "step2");
        contentPanel.add(createStep3(), "step3");
        contentPanel.add(createStep4(), "step4");
        add(contentPanel, "grow");

        // 3. Navigation Bar
        RoundedPanel navBar = new RoundedPanel(12, Theme.BG_CARD, true);
        navBar.setLayout(new MigLayout("insets 16 24 16 24, fillx", "[left]push[right]"));

        backBtn = createNavButton("← Quay lại", Theme.SLATE);
        backBtn.setVisible(false);
        backBtn.addActionListener(e -> goBack());
        navBar.add(backBtn, "h 44!, w 160!");

        nextBtn = createNavButton("Tiếp theo →", Theme.EMERALD);
        nextBtn.addActionListener(e -> goNext());
        navBar.add(nextBtn, "h 44!, w 180!");

        add(navBar, "h 76!");

        updateStep();
    }

    public void setOnBookingConfirmed(Runnable callback) {
        this.onBookingConfirmed = callback;
    }

    // ========================= STEP 1: Chọn dịch vụ =========================
    private JPanel createStep1() {
        JPanel panel = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[fill]", "[][grow][]"));
        panel.setOpaque(false);

        JLabel title = new JLabel("Chọn dịch vụ bạn muốn đặt");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.NAVY);
        panel.add(title, "gapbottom 12");

        // Service cards grid
        JPanel grid = new JPanel(new MigLayout("wrap 3, gap 16, fillx", "[fill, 33%][fill, 33%][fill, 33%]"));
        grid.setOpaque(false);

        List<ServiceDtos.Response> services = loadMockServices();
        for (ServiceDtos.Response svc : services) {
            grid.add(createServiceCard(svc), "h 130!");
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, "grow");

        // Summary bar
        RoundedPanel summaryBar = new RoundedPanel(10, Theme.BG_CARD, true);
        summaryBar.setLayout(new MigLayout("insets 12 20 12 20, fillx", "[left]push[right]"));
        summaryLabel = new JLabel("Chưa chọn dịch vụ nào");
        summaryLabel.setFont(Theme.FONT_H3);
        summaryLabel.setForeground(Theme.TEXT_MUTED);
        summaryBar.add(summaryLabel);
        panel.add(summaryBar, "h 48!, gaptop 8");

        return panel;
    }

    private RoundedPanel createServiceCard(ServiceDtos.Response svc) {
        RoundedPanel card = new RoundedPanel(12, Theme.BG_CARD, true) {
            boolean hovered = false;
            boolean selected = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selected) {
                    g2.setColor(Theme.EMERALD);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                } else if (hovered) {
                    g2.setColor(new Color(16, 185, 129, 40));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                }
                g2.dispose();
            }
            public void setSelectedState(boolean sel) { this.selected = sel; repaint(); }
        };
        card.setLayout(new MigLayout("insets 14, fillx", "[]12[grow][]", "[][]"));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon
        String icon = getServiceIcon(svc.categoryName());
        Color iconBg = getServiceIconBg(svc.categoryName());
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(48, 48));
        iconBadge.setLayout(new MigLayout("fill, insets 0", "[center]", "[center]"));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(Theme.FONT_H1.deriveFont(20f));
        iconBadge.add(iconLbl);
        card.add(iconBadge, "span 1 2, aligny top, w 48!, h 48!");

        // Info
        JLabel nameLabel = new JLabel(svc.name());
        nameLabel.setFont(Theme.FONT_H3);
        nameLabel.setForeground(Theme.NAVY);
        card.add(nameLabel, "growx");

        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        card.add(checkBox, "span 1 2, aligny top");

        // Bottom row
        JPanel bottomRow = new JPanel(new MigLayout("insets 0, gap 8", "[][]push[]"));
        bottomRow.setOpaque(false);

        JLabel catLabel = new JLabel(svc.categoryName() != null ? svc.categoryName() : "");
        catLabel.setFont(Theme.FONT_BODY_SM);
        catLabel.setForeground(Theme.TEXT_MUTED);
        bottomRow.add(catLabel);

        JLabel durLabel = new JLabel("⏱ " + svc.durationMinutes() + " phút");
        durLabel.setFont(Theme.FONT_BODY_SM);
        durLabel.setForeground(Theme.SLATE);
        bottomRow.add(durLabel);

        JLabel priceLabel = new JLabel(nf.format(svc.price()) + "đ");
        priceLabel.setFont(Theme.FONT_H3);
        priceLabel.setForeground(Theme.EMERALD);
        bottomRow.add(priceLabel);

        card.add(bottomRow, "growx, skip 1");

        // Card click toggles checkbox
        Runnable toggle = () -> {
            checkBox.setSelected(!checkBox.isSelected());
            if (checkBox.isSelected()) {
                selectedServices.add(svc);
            } else {
                selectedServices.remove(svc);
            }
            // Reflectively update card border
            try {
                card.getClass().getMethod("setSelectedState", boolean.class).invoke(card, checkBox.isSelected());
            } catch (Exception ignored) {}
            updateServiceSummary();
        };

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { toggle.run(); }
        });
        checkBox.addActionListener(e -> {
            if (checkBox.isSelected()) {
                if (!selectedServices.contains(svc)) selectedServices.add(svc);
            } else {
                selectedServices.remove(svc);
            }
            try {
                card.getClass().getMethod("setSelectedState", boolean.class).invoke(card, checkBox.isSelected());
            } catch (Exception ignored) {}
            updateServiceSummary();
        });

        return card;
    }

    private void updateServiceSummary() {
        if (selectedServices.isEmpty()) {
            summaryLabel.setText("Chưa chọn dịch vụ nào");
            summaryLabel.setForeground(Theme.TEXT_MUTED);
        } else {
            BigDecimal total = selectedServices.stream()
                .map(s -> s.price() != null ? s.price() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            summaryLabel.setText("Đã chọn " + selectedServices.size() + " dịch vụ • Tổng: " + nf.format(total) + "đ");
            summaryLabel.setForeground(Theme.EMERALD);
        }
    }

    // ========================= STEP 2: Chọn nhân viên =========================
    private JPanel createStep2() {
        JPanel panel = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[fill]", "[][grow]"));
        panel.setOpaque(false);

        JLabel title = new JLabel("Chọn nhân viên phục vụ");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.NAVY);
        panel.add(title, "gapbottom 12");

        JPanel grid = new JPanel(new MigLayout("wrap 3, gap 16, fillx", "[fill, 33%][fill, 33%][fill, 33%]"));
        grid.setOpaque(false);

        ButtonGroup staffGroup = new ButtonGroup();
        for (StaffInfo staff : mockStaff) {
            grid.add(createStaffCard(staff, staffGroup), "h 170!");
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, "grow");

        return panel;
    }

    private RoundedPanel createStaffCard(StaffInfo staff, ButtonGroup group) {
        RoundedPanel card = new RoundedPanel(12, Theme.BG_CARD, true);
        card.setLayout(new MigLayout("insets 16, fillx", "[]16[grow]", "[][][][]"));
        card.setCursor(staff.active ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        // Avatar
        CircleAvatar avatar = new CircleAvatar(staff.name, 56, Theme.NAVY, Theme.TEXT_WHITE);
        card.add(avatar, "span 1 4, aligny top, w 56!, h 56!");

        // Name
        JLabel nameLabel = new JLabel(staff.name);
        nameLabel.setFont(Theme.FONT_H2);
        nameLabel.setForeground(staff.active ? Theme.NAVY : Theme.SLATE);
        card.add(nameLabel, "wrap");

        // Role
        JLabel roleLabel = new JLabel(staff.role);
        roleLabel.setFont(Theme.FONT_BODY_LG);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        card.add(roleLabel, "wrap");

        // Rating
        JLabel ratingLabel = new JLabel("⭐ 4.9 (100+)");
        ratingLabel.setFont(Theme.FONT_BODY_SM);
        ratingLabel.setForeground(Theme.AMBER);
        card.add(ratingLabel, "wrap");

        // Status + Radio
        JPanel bottomPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        bottomPanel.setOpaque(false);

        String statusText = staff.active ? "Đang làm việc" : "Nghỉ phép";
        Color statusColor = staff.active ? Theme.EMERALD : Theme.AMBER;
        JLabel statusBadge = new JLabel(statusText) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusBadge.setFont(Theme.FONT_BODY_SM.deriveFont(Font.BOLD));
        statusBadge.setForeground(Theme.TEXT_WHITE);
        statusBadge.setOpaque(false);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        bottomPanel.add(statusBadge, "w min!");

        JRadioButton radio = new JRadioButton();
        radio.setOpaque(false);
        radio.setEnabled(staff.active);
        group.add(radio);
        bottomPanel.add(radio);

        card.add(bottomPanel, "growx");

        // Click handler
        if (staff.active) {
            MouseAdapter clickHandler = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    radio.setSelected(true);
                    selectedStaffId = staff.id;
                    selectedStaffName = staff.name;
                    selectedStaffRole = staff.role;
                }
            };
            card.addMouseListener(clickHandler);
            radio.addActionListener(e -> {
                selectedStaffId = staff.id;
                selectedStaffName = staff.name;
                selectedStaffRole = staff.role;
            });
        }

        return card;
    }

    // ========================= STEP 3: Chọn ngày giờ =========================
    private JPanel createStep3() {
        JPanel panel = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[fill]", "[][grow][]"));
        panel.setOpaque(false);

        JLabel title = new JLabel("Chọn ngày và khung giờ");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.NAVY);
        panel.add(title, "gapbottom 12");

        // Main content: Date picker left, Time slots right
        JPanel mainArea = new JPanel(new MigLayout("fill, insets 0", "[35%]16[65%]", "[fill]"));
        mainArea.setOpaque(false);

        // Date picker
        RoundedPanel datePanel = new RoundedPanel(12, Theme.BG_CARD, true);
        datePanel.setLayout(new MigLayout("wrap 1, insets 24, fillx", "[fill]", "[]16[]16[]"));

        JLabel dateTitle = new JLabel("📅 Chọn ngày");
        dateTitle.setFont(Theme.FONT_H3);
        dateTitle.setForeground(Theme.NAVY);
        datePanel.add(dateTitle);

        dateLabel = new JLabel(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"))));
        dateLabel.setFont(Theme.FONT_H1);
        dateLabel.setForeground(Theme.NAVY);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        datePanel.add(dateLabel, "gaptop 8");

        JPanel dateButtons = new JPanel(new MigLayout("insets 0, fillx", "[50%]8[50%]"));
        dateButtons.setOpaque(false);

        JButton prevDay = createNavButton("← Trước", Theme.SLATE);
        prevDay.setFont(Theme.FONT_BODY_LG);
        prevDay.addActionListener(e -> {
            if (selectedDate.isAfter(LocalDate.now())) {
                selectedDate = selectedDate.minusDays(1);
                dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"))));
            }
        });
        dateButtons.add(prevDay, "h 36!, growx");

        JButton nextDay = createNavButton("Sau →", Theme.EMERALD);
        nextDay.setFont(Theme.FONT_BODY_LG);
        nextDay.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"))));
        });
        dateButtons.add(nextDay, "h 36!, growx");

        datePanel.add(dateButtons);
        mainArea.add(datePanel);

        // Time slots grid
        RoundedPanel slotsPanel = new RoundedPanel(12, Theme.BG_CARD, true);
        slotsPanel.setLayout(new MigLayout("wrap 1, insets 24, fill", "[fill]", "[]12[grow]"));

        JLabel slotsTitle = new JLabel("⏰ Khung giờ trống");
        slotsTitle.setFont(Theme.FONT_H3);
        slotsTitle.setForeground(Theme.NAVY);
        slotsPanel.add(slotsTitle);

        JPanel slotsGrid = new JPanel(new MigLayout("wrap 4, gap 8, fillx", "[25%][25%][25%][25%]"));
        slotsGrid.setOpaque(false);

        // Generate time slots from 08:00 to 20:00
        List<LocalTime> unavailableSlots = List.of(
            LocalTime.of(10, 0), LocalTime.of(10, 30),
            LocalTime.of(14, 0), LocalTime.of(14, 30)
        );

        for (int h = 8; h < 20; h++) {
            for (int m = 0; m < 60; m += 30) {
                LocalTime time = LocalTime.of(h, m);
                boolean available = !unavailableSlots.contains(time);
                JButton slotBtn = createTimeSlotButton(time, available);
                slotsGrid.add(slotBtn, "h 40!, growx");
            }
        }

        JScrollPane slotsScroll = new JScrollPane(slotsGrid);
        slotsScroll.setBorder(null);
        slotsScroll.setOpaque(false);
        slotsScroll.getViewport().setOpaque(false);
        slotsPanel.add(slotsScroll, "grow");

        mainArea.add(slotsPanel, "grow");
        panel.add(mainArea, "grow");

        // Selected time display
        RoundedPanel selectedBar = new RoundedPanel(10, Theme.BG_CARD, true);
        selectedBar.setLayout(new MigLayout("insets 10 20 10 20, fillx", "[center]"));
        JLabel selLabel = new JLabel("Vui lòng chọn khung giờ phía trên");
        selLabel.setFont(Theme.FONT_H3);
        selLabel.setForeground(Theme.TEXT_MUTED);
        selectedBar.add(selLabel);
        panel.add(selectedBar, "h 44!, gaptop 8");

        return panel;
    }

    private JButton createTimeSlotButton(LocalTime time, boolean available) {
        String text = time.format(DateTimeFormatter.ofPattern("HH:mm"));
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_H3);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        if (available) {
            btn.setBackground(new Color(209, 250, 229));
            btn.setForeground(new Color(6, 95, 70));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (btn != selectedTimeButton) {
                        btn.setBackground(new Color(167, 243, 208));
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (btn != selectedTimeButton) {
                        btn.setBackground(new Color(209, 250, 229));
                    }
                }
            });

            btn.addActionListener(e -> {
                // Deselect previous
                if (selectedTimeButton != null) {
                    selectedTimeButton.setBackground(new Color(209, 250, 229));
                    selectedTimeButton.setForeground(new Color(6, 95, 70));
                }
                selectedTimeButton = btn;
                btn.setBackground(Theme.EMERALD);
                btn.setForeground(Theme.TEXT_WHITE);
                selectedTime = time;
            });
        } else {
            btn.setBackground(new Color(229, 231, 235));
            btn.setForeground(Theme.SLATE);
            btn.setEnabled(false);
        }

        return btn;
    }

    // ========================= STEP 4: Xác nhận =========================
    private JPanel createStep4() {
        JPanel panel = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[fill]", "[grow]"));
        panel.setOpaque(false);

        JScrollPane scroll = new JScrollPane();
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        RoundedPanel card = new RoundedPanel(16, Theme.BG_CARD, true);
        card.setLayout(new MigLayout("wrap 1, insets 30, fillx", "[fill]", ""));

        JLabel headerLabel = new JLabel("XÁC NHẬN ĐẶT LỊCH");
        headerLabel.setFont(Theme.FONT_H1);
        headerLabel.setForeground(Theme.NAVY);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(headerLabel, "gapbottom 20");

        // Services section
        JLabel svcTitle = new JLabel("📋 Dịch vụ đã chọn");
        svcTitle.setFont(Theme.FONT_H2);
        svcTitle.setForeground(Theme.NAVY);
        card.add(svcTitle, "gapbottom 8");

        confirmServicesPanel = new JPanel(new MigLayout("wrap 1, insets 0, gap 4, fillx", "[fill]"));
        confirmServicesPanel.setOpaque(false);
        card.add(confirmServicesPanel, "gapbottom 16");

        // Divider
        card.add(createDivider(), "h 1!, gapbottom 16");

        // Staff section
        JLabel staffTitle = new JLabel("👤 Nhân viên");
        staffTitle.setFont(Theme.FONT_H2);
        staffTitle.setForeground(Theme.NAVY);
        card.add(staffTitle, "gapbottom 8");

        confirmStaffLabel = new JLabel("Chưa chọn");
        confirmStaffLabel.setFont(Theme.FONT_BODY_LG);
        confirmStaffLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(confirmStaffLabel, "gapbottom 16");

        // Divider
        card.add(createDivider(), "h 1!, gapbottom 16");

        // Time section
        JLabel timeTitle = new JLabel("📅 Thời gian");
        timeTitle.setFont(Theme.FONT_H2);
        timeTitle.setForeground(Theme.NAVY);
        card.add(timeTitle, "gapbottom 8");

        confirmTimeLabel = new JLabel("Chưa chọn");
        confirmTimeLabel.setFont(Theme.FONT_BODY_LG);
        confirmTimeLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(confirmTimeLabel, "gapbottom 16");

        // Divider
        card.add(createDivider(), "h 1!, gapbottom 16");

        // Total section
        JLabel totalTitle = new JLabel("💰 Tổng tiền");
        totalTitle.setFont(Theme.FONT_H2);
        totalTitle.setForeground(Theme.NAVY);
        card.add(totalTitle, "gapbottom 8");

        confirmTotalLabel = new JLabel("0đ");
        confirmTotalLabel.setFont(Theme.FONT_HERO.deriveFont(24f));
        confirmTotalLabel.setForeground(Theme.EMERALD);
        card.add(confirmTotalLabel, "gapbottom 20");

        // Divider
        card.add(createDivider(), "h 1!, gapbottom 16");

        // Note
        JLabel noteTitle = new JLabel("📝 Ghi chú");
        noteTitle.setFont(Theme.FONT_H2);
        noteTitle.setForeground(Theme.NAVY);
        card.add(noteTitle, "gapbottom 8");

        noteArea = new JTextArea(3, 40);
        noteArea.setFont(Theme.FONT_BODY_LG);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        noteArea.putClientProperty("JTextField.placeholderText", "Nhập ghi chú cho nhân viên (tùy chọn)...");
        card.add(noteArea, "h 80!");

        scroll.setViewportView(card);
        panel.add(scroll, "grow");

        return panel;
    }

    private void populateConfirmation() {
        // Services
        confirmServicesPanel.removeAll();
        BigDecimal total = BigDecimal.ZERO;
        int idx = 1;
        for (ServiceDtos.Response svc : selectedServices) {
            JPanel row = new JPanel(new MigLayout("insets 6 12 6 12, fillx", "[]8[grow]push[]", "[center]"));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)));

            JLabel numLabel = new JLabel(idx + ".");
            numLabel.setFont(Theme.FONT_BODY_LG);
            numLabel.setForeground(Theme.TEXT_MUTED);
            row.add(numLabel);

            JLabel nameLabel = new JLabel(svc.name() + "  (" + svc.durationMinutes() + " phút)");
            nameLabel.setFont(Theme.FONT_BODY_LG);
            nameLabel.setForeground(Theme.TEXT_PRIMARY);
            row.add(nameLabel);

            JLabel priceLabel = new JLabel(nf.format(svc.price()) + "đ");
            priceLabel.setFont(Theme.FONT_H3);
            priceLabel.setForeground(Theme.EMERALD);
            row.add(priceLabel);

            confirmServicesPanel.add(row);
            total = total.add(svc.price() != null ? svc.price() : BigDecimal.ZERO);
            idx++;
        }
        confirmServicesPanel.revalidate();
        confirmServicesPanel.repaint();

        // Staff
        if (selectedStaffName != null) {
            confirmStaffLabel.setText(selectedStaffName + " - " + (selectedStaffRole != null ? selectedStaffRole : "Nhân viên"));
        }

        // Time
        if (selectedTime != null) {
            LocalDateTime dt = LocalDateTime.of(selectedDate, selectedTime);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
            confirmTimeLabel.setText(dt.format(dtf));
        }

        // Total
        confirmTotalLabel.setText(nf.format(total) + "đ");

        // Note
        note = noteArea.getText();
    }

    // ========================= Navigation =========================
    private void goBack() {
        if (currentStep > 0) {
            currentStep--;
            updateStep();
        }
    }

    private void goNext() {
        // Validate current step
        if (!validateCurrentStep()) return;

        if (currentStep == 3) {
            // Confirm booking
            note = noteArea.getText();
            JOptionPane.showMessageDialog(this,
                "Đặt lịch thành công!\n\n" +
                "Dịch vụ: " + selectedServices.size() + " dịch vụ\n" +
                "Nhân viên: " + selectedStaffName + "\n" +
                "Thời gian: " + selectedDate + " " + selectedTime + "\n\n" +
                "Cảm ơn bạn đã sử dụng dịch vụ!",
                "✅ Đặt lịch thành công",
                JOptionPane.INFORMATION_MESSAGE
            );
            if (onBookingConfirmed != null) {
                onBookingConfirmed.run();
            }
            return;
        }

        currentStep++;
        if (currentStep == 3) {
            populateConfirmation();
        }
        updateStep();
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 0:
                if (selectedServices.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 dịch vụ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                return true;
            case 1:
                if (selectedStaffId == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên phục vụ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                return true;
            case 2:
                if (selectedTime == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn khung giờ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    private void updateStep() {
        cardLayout.show(contentPanel, "step" + (currentStep + 1));
        stepIndicator.setCurrentStep(currentStep);
        backBtn.setVisible(currentStep > 0);

        if (currentStep == 3) {
            nextBtn.setText("✅ Xác nhận đặt lịch");
            nextBtn.setBackground(Theme.EMERALD);
        } else {
            nextBtn.setText("Tiếp theo →");
            nextBtn.setBackground(Theme.EMERALD);
        }
    }

    // ========================= Helpers =========================
    private JButton createNavButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_H3);
        btn.setForeground(Theme.TEXT_WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        return btn;
    }

    private JPanel createDivider() {
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER);
        divider.setPreferredSize(new Dimension(0, 1));
        return divider;
    }

    private String getServiceIcon(String category) {
        if (category == null) return "💅";
        if (category.contains("Cắt")) return "💇";
        if (category.contains("Nhuộm") || category.contains("Uốn")) return "🎨";
        if (category.contains("Chăm sóc")) return "💆";
        return "💅";
    }

    private Color getServiceIconBg(String category) {
        if (category == null) return new Color(251, 207, 232);
        if (category.contains("Cắt")) return new Color(219, 234, 254);
        if (category.contains("Nhuộm") || category.contains("Uốn")) return new Color(254, 243, 199);
        if (category.contains("Chăm sóc")) return new Color(209, 250, 229);
        return new Color(251, 207, 232);
    }

    private List<ServiceDtos.Response> loadMockServices() {
        List<ServiceDtos.Response> list = new ArrayList<>();
        list.add(new ServiceDtos.Response(10L, 1L, "Cắt & Tạo kiểu", "Cắt tóc nam (bao gồm gội)", "Cắt tóc cơ bản và gội đầu", new BigDecimal("150000"), 30, true, null));
        list.add(new ServiceDtos.Response(11L, 1L, "Cắt & Tạo kiểu", "Cắt tóc nữ & Tạo kiểu", "Tạo kiểu layer, uốn lọn nhẹ", new BigDecimal("250000"), 45, true, null));
        list.add(new ServiceDtos.Response(12L, 2L, "Nhuộm & Uốn", "Uốn tóc nữ kiểu Hàn Quốc", "Uốn sóng nước, uốn cụp", new BigDecimal("800000"), 120, true, null));
        list.add(new ServiceDtos.Response(13L, 2L, "Nhuộm & Uốn", "Nhuộm màu thời trang (L'Oreal)", "Tẩy tóc, nhuộm khói, xám tro", new BigDecimal("650000"), 90, true, null));
        list.add(new ServiceDtos.Response(14L, 3L, "Chăm sóc tóc", "Gội đầu dưỡng sinh thảo dược", "Massage đầu, gội dầu thảo dược", new BigDecimal("200000"), 45, true, null));
        list.add(new ServiceDtos.Response(15L, 3L, "Chăm sóc tóc", "Phục hồi tóc hư tổn Olaplex", "Ủ dưỡng chất phục hồi tóc xơ", new BigDecimal("500000"), 60, true, null));
        list.add(new ServiceDtos.Response(16L, 4L, "Dịch vụ khác", "Nối mi Volume tự nhiên", "Nối mi dầy, uốn mi cong", new BigDecimal("350000"), 75, true, null));
        return list;
    }

    // ========================= Step Indicator Panel =========================
    private static class StepIndicatorPanel extends RoundedPanel {
        private int currentStep = 0;

        StepIndicatorPanel() {
            super(12, Theme.BG_CARD, true);
        }

        void setCurrentStep(int step) {
            this.currentStep = step;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int circleSize = 40;
            int totalSteps = STEP_LABELS.length;
            int spacing = (w - 80) / (totalSteps - 1);
            int startX = 40;
            int centerY = 32;

            // Draw connecting lines
            for (int i = 0; i < totalSteps - 1; i++) {
                int x1 = startX + i * spacing + circleSize / 2;
                int x2 = startX + (i + 1) * spacing - circleSize / 2;
                g2.setColor(i < currentStep ? Theme.EMERALD : Theme.BORDER);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(x1, centerY, x2, centerY);
            }

            // Draw circles and labels
            for (int i = 0; i < totalSteps; i++) {
                int cx = startX + i * spacing;
                int cy = centerY;

                Color circleBg;
                Color textColor;
                String text;

                if (i < currentStep) {
                    // Completed
                    circleBg = Theme.EMERALD;
                    textColor = Theme.TEXT_WHITE;
                    text = "✓";
                } else if (i == currentStep) {
                    // Current
                    circleBg = Theme.EMERALD;
                    textColor = Theme.TEXT_WHITE;
                    text = String.valueOf(i + 1);
                } else {
                    // Future
                    circleBg = Theme.BORDER;
                    textColor = Theme.SLATE;
                    text = String.valueOf(i + 1);
                }

                // Circle
                g2.setColor(circleBg);
                g2.fillOval(cx - circleSize / 2, cy - circleSize / 2, circleSize, circleSize);

                // Number/check
                g2.setColor(textColor);
                g2.setFont(Theme.FONT_H2);
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(text);
                int th = fm.getAscent();
                g2.drawString(text, cx - tw / 2, cy + th / 2 - 2);

                // Label below
                g2.setColor(i <= currentStep ? Theme.NAVY : Theme.SLATE);
                g2.setFont(Theme.FONT_BODY_SM);
                fm = g2.getFontMetrics();
                tw = fm.stringWidth(STEP_LABELS[i]);
                g2.drawString(STEP_LABELS[i], cx - tw / 2, cy + circleSize / 2 + 18);
            }

            g2.dispose();
        }
    }
}
