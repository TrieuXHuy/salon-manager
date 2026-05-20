package com.salonnbooking.ui.panels;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.api.dto.BookingDtos;
import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.util.JsonUtil;
import com.salonnbooking.ui.ScreenLifecycle;
import com.salonnbooking.ui.ScreenRouter;
import com.salonnbooking.ui.components.CircleAvatar;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class BookingWizardPanel extends JPanel implements ScreenLifecycle {

    private static final String BASE_URL = "http://localhost:8080";

    private final ApiClient apiClient = new ApiClient(BASE_URL);
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
    private JLabel selectedTimeLabel;
    private JPanel slotsGrid;

    // Step 4 widgets
    private JPanel confirmServicesPanel;
    private JLabel confirmStaffLabel;
    private JLabel confirmTimeLabel;
    private JLabel confirmTotalLabel;
    private JTextArea noteArea;

    public record BookingSummary(
        List<ServiceDtos.Response> services,
        Long staffId,
        String staffName,
        String staffRole,
        LocalDate date,
        LocalTime time,
        String note,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total
    ) {}

    private Consumer<BookingSummary> onBookingConfirmed;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    private static int s(int value) {
        return Theme.scaleDimension(value);
    }

    private static int gap(int value) {
        float scale = Math.min(Theme.getDPIScaleFactor(), 1.25f);
        return Math.round(value * scale);
    }

    private record StaffInfo(Long id, String name, String role, boolean active) {}

    public BookingWizardPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout(
                "fill, wrap 1, insets " + gap(20),
                "[fill]",
                "[" + s(76) + "!][grow][" + s(70) + "!]"));

        // 1. Step Indicator
        stepIndicator = new StepIndicatorPanel();
        add(stepIndicator, "growx");

        // 2. Content Panel (CardLayout)
        contentPanel.setOpaque(false);
        contentPanel.add(createStep1(), "step1");
        contentPanel.add(createStep2(), "step2");
        contentPanel.add(createStep3(), "step3");
        contentPanel.add(createStep4(), "step4");
        add(contentPanel, "grow");

        // 3. Navigation Bar
        RoundedPanel navBar = new RoundedPanel(gap(12), Theme.BG_CARD, true);
        navBar.setLayout(new MigLayout(
                "insets " + gap(12) + " " + gap(20) + " " + gap(12) + " " + gap(20) + ", fillx",
                "[left]push[right]"));

        backBtn = createNavButton("← Quay lại", Theme.SLATE);
        backBtn.setVisible(false);
        backBtn.addActionListener(e -> goBack());
        navBar.add(backBtn, "h " + s(42) + "!, w " + s(150) + "!");

        nextBtn = createNavButton("Tiếp theo →", Theme.EMERALD);
        nextBtn.addActionListener(e -> goNext());
        navBar.add(nextBtn, "h " + s(42) + "!, w " + s(170) + "!");

        add(navBar, "growx");

        updateStep();
    }

    public void setOnBookingConfirmed(Consumer<BookingSummary> callback) {
        this.onBookingConfirmed = callback;
    }

    @Override
    public void onScreenShown() {
        resetForNewBooking();
    }

    // ========================= STEP 1: Chọn dịch vụ =========================
    private JPanel createStep1() {
        JPanel panel = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[fill]", "[][grow][]"));
        panel.setOpaque(false);

        JLabel title = new JLabel("Chọn dịch vụ bạn muốn đặt");
        title.setFont(Theme.FONT_H2);
        title.setForeground(Theme.NAVY);
        panel.add(title, "gapbottom " + gap(10));

        // Service cards list
        JPanel grid = new JPanel(new MigLayout(
                "wrap 1, gapy " + gap(12) + ", fillx, insets 0",
                "[fill]",
                ""));
        grid.setOpaque(false);

        List<ServiceDtos.Response> services = loadServices();
        for (ServiceDtos.Response svc : services) {
            grid.add(createServiceCard(svc), "h " + s(92) + "!, growx");
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, "grow");

        // Summary bar
        RoundedPanel summaryBar = new RoundedPanel(gap(10), Theme.BG_CARD, true);
        summaryBar.setLayout(new MigLayout(
                "insets " + gap(10) + " " + gap(16) + " " + gap(10) + " " + gap(16) + ", fillx",
                "[left]push[right]"));
        summaryLabel = new JLabel("Chưa chọn dịch vụ nào");
        summaryLabel.setFont(Theme.FONT_H3);
        summaryLabel.setForeground(Theme.TEXT_MUTED);
        summaryBar.add(summaryLabel);
        panel.add(summaryBar, "h " + s(46) + "!, gaptop " + gap(8));

        return panel;
    }

    private RoundedPanel createServiceCard(ServiceDtos.Response svc) {
        RoundedPanel card = new RoundedPanel(gap(12), Theme.BG_CARD, true) {
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
        card.setLayout(new MigLayout(
                "insets " + gap(12) + ", fillx",
                "[" + s(46) + "!]" + gap(12) + "[grow, fill][]",
                "[]"));
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
        iconBadge.setPreferredSize(new Dimension(s(46), s(46)));
        iconBadge.setLayout(new MigLayout("fill, insets 0", "[center]", "[center]"));
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(Theme.FONT_H2);
        iconBadge.add(iconLbl);
        card.add(iconBadge, "w " + s(46) + "!, h " + s(46) + "!, aligny center");

        // Info
        JPanel infoPanel = new JPanel(new MigLayout("wrap 1, insets 0, gapy " + gap(4), "[fill]"));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(svc.name());
        nameLabel.setFont(Theme.FONT_H3);
        nameLabel.setForeground(Theme.NAVY);
        infoPanel.add(nameLabel);

        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);

        // Bottom row
        JPanel bottomRow = new JPanel(new MigLayout("insets 0, gap " + gap(8), "[][]push[]"));
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

        infoPanel.add(bottomRow, "growx");
        card.add(infoPanel, "growx");
        card.add(checkBox, "aligny center");

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
        panel.add(title, "gapbottom " + gap(10));

        JPanel grid = new JPanel(new MigLayout(
                "wrap 1, gapy " + gap(12) + ", fillx, insets 0",
                "[fill]",
                ""));
        grid.setOpaque(false);

        ButtonGroup staffGroup = new ButtonGroup();
        List<StaffInfo> staffList = loadStaff();
        if (staffList.isEmpty()) {
            JLabel empty = new JLabel("Không có nhân viên đang hoạt động được phân công đủ dịch vụ đã chọn", SwingConstants.CENTER);
            empty.setFont(Theme.FONT_H3);
            empty.setForeground(Theme.TEXT_MUTED);
            grid.add(empty, "h " + s(80) + "!, growx");
        }
        for (StaffInfo staff : staffList) {
            grid.add(createStaffCard(staff, staffGroup), "h " + s(104) + "!, growx");
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
        RoundedPanel card = new RoundedPanel(gap(12), Theme.BG_CARD, true);
        card.setLayout(new MigLayout(
                "insets " + gap(14) + ", fillx",
                "[" + s(52) + "!]" + gap(14) + "[grow, fill][]",
                "[]"));
        card.setCursor(staff.active ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        // Avatar
        CircleAvatar avatar = new CircleAvatar(staff.name, s(52), Theme.NAVY, Theme.TEXT_WHITE);
        card.add(avatar, "w " + s(52) + "!, h " + s(52) + "!, aligny center");

        JPanel infoPanel = new JPanel(new MigLayout("wrap 1, insets 0, gapy " + gap(3), "[fill]"));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(staff.name);
        nameLabel.setFont(Theme.FONT_H2);
        nameLabel.setForeground(staff.active ? Theme.NAVY : Theme.SLATE);
        infoPanel.add(nameLabel);

        JLabel roleLabel = new JLabel(staff.role);
        roleLabel.setFont(Theme.FONT_BODY_LG);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        infoPanel.add(roleLabel);

        JPanel bottomPanel = new JPanel(new MigLayout("insets 0, gap " + gap(8), "[][]"));
        bottomPanel.setOpaque(false);

        JLabel ratingLabel = new JLabel("⭐ 4.9 (100+)");
        ratingLabel.setFont(Theme.FONT_BODY_SM);
        ratingLabel.setForeground(Theme.AMBER);
        bottomPanel.add(ratingLabel);

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
        bottomPanel.add(statusBadge, "w min!, h " + s(22) + "!");
        infoPanel.add(bottomPanel);

        JRadioButton radio = new JRadioButton();
        radio.setOpaque(false);
        radio.setEnabled(staff.active);
        group.add(radio);
        card.add(infoPanel, "growx");
        card.add(radio, "aligny center");

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
        panel.add(title, "gapbottom " + gap(10));

        JPanel mainArea = new JPanel(new MigLayout(
                "fill, insets 0, gap " + gap(16),
                "[fill, grow 55][fill, grow 45]",
                "[fill]"));
        mainArea.setOpaque(false);

        RoundedPanel datePanel = new RoundedPanel(gap(12), Theme.BG_CARD, true);
        datePanel.setLayout(new MigLayout(
                "wrap 1, insets " + gap(20) + ", fillx",
                "[center]",
                "[]" + gap(18) + "[]" + gap(18) + "[]push"));

        JLabel dateTitle = new JLabel("📅 Chọn ngày");
        dateTitle.setFont(Theme.FONT_H3);
        dateTitle.setForeground(Theme.NAVY);
        datePanel.add(dateTitle, "align left, growx");

        dateLabel = new JLabel(formatSelectedDate());
        dateLabel.setFont(Theme.FONT_H2);
        dateLabel.setForeground(Theme.NAVY);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateLabel.setPreferredSize(new Dimension(s(320), s(32)));
        datePanel.add(dateLabel, "w " + s(320) + "!, h " + s(32) + "!");

        JButton calendarBtn = createNavButton("📅 Mở lịch", Theme.EMERALD);
        calendarBtn.setFont(Theme.FONT_BODY_LG);
        calendarBtn.addActionListener(e -> showCalendarDialog());
        datePanel.add(calendarBtn, "h " + s(42) + "!, w " + s(220) + "!");

        mainArea.add(datePanel, "grow");

        // Time slots grid
        RoundedPanel slotsPanel = new RoundedPanel(gap(12), Theme.BG_CARD, true);
        slotsPanel.setLayout(new MigLayout(
                "wrap 1, insets " + gap(20) + ", fill",
                "[fill]",
                "[]" + gap(12) + "[grow]"));

        JLabel slotsTitle = new JLabel("⏰ Khung giờ trống");
        slotsTitle.setFont(Theme.FONT_H3);
        slotsTitle.setForeground(Theme.NAVY);
        slotsPanel.add(slotsTitle);

        JPanel legend = new JPanel(new MigLayout("insets 0, gap " + gap(10), "[][]"));
        legend.setOpaque(false);
        legend.add(createSlotLegend("Trống", new Color(209, 250, 229), new Color(6, 95, 70)));
        legend.add(createSlotLegend("Đã đặt / không hợp lệ", new Color(229, 231, 235), Theme.SLATE));
        slotsPanel.add(legend, "growx");

        slotsGrid = new JPanel(new MigLayout(
                "wrap 3, gap " + gap(8) + ", fillx, insets 0",
                "[fill, grow][fill, grow][fill, grow]"));
        slotsGrid.setOpaque(false);

        JScrollPane slotsScroll = new JScrollPane(slotsGrid);
        slotsScroll.setBorder(null);
        slotsScroll.setOpaque(false);
        slotsScroll.getViewport().setOpaque(false);
        slotsPanel.add(slotsScroll, "grow");

        mainArea.add(slotsPanel, "grow");
        panel.add(mainArea, "grow");

        // Selected time display
        RoundedPanel selectedBar = new RoundedPanel(gap(10), Theme.BG_CARD, true);
        selectedBar.setLayout(new MigLayout(
                "insets " + gap(10) + " " + gap(20) + " " + gap(10) + " " + gap(20) + ", fillx",
                "[center]"));
        selectedTimeLabel = new JLabel(selectedTimeText());
        selectedTimeLabel.setFont(Theme.FONT_H3);
        selectedTimeLabel.setForeground(Theme.TEXT_MUTED);
        selectedBar.add(selectedTimeLabel);
        panel.add(selectedBar, "h " + s(44) + "!, gaptop " + gap(8));

        loadAvailableSlots();
        return panel;
    }

    private JButton createTimeSlotButton(LocalTime time, boolean available) {
        String text = time.format(DateTimeFormatter.ofPattern("HH:mm"));
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_H3);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        if (!available) {
            btn.setBackground(new Color(229, 231, 235));
            btn.setForeground(Theme.SLATE);
            btn.setEnabled(false);
            btn.setToolTipText("Khung giờ đã có lịch hoặc không còn hợp lệ");
            return btn;
        }

        btn.setBackground(new Color(209, 250, 229));
        btn.setForeground(new Color(6, 95, 70));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Khung giờ trống");
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
            if (selectedTimeButton != null) {
                selectedTimeButton.setBackground(new Color(209, 250, 229));
                selectedTimeButton.setForeground(new Color(6, 95, 70));
            }
            selectedTimeButton = btn;
            btn.setBackground(Theme.EMERALD);
            btn.setForeground(Theme.TEXT_WHITE);
            selectedTime = time;
            if (selectedTimeLabel != null) {
                selectedTimeLabel.setText(selectedTimeText());
                selectedTimeLabel.setForeground(Theme.EMERALD);
            }
        });

        return btn;
    }

    private JLabel createSlotLegend(String text, Color bg, Color fg) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(Theme.FONT_BODY_SM);
        label.setForeground(fg);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return label;
    }

    private void loadAvailableSlots() {
        if (slotsGrid == null || selectedStaffId == null || selectedServices.isEmpty()) {
            return;
        }

        selectedTime = null;
        selectedTimeButton = null;
        if (selectedTimeLabel != null) {
            selectedTimeLabel.setText("Đang tải khung giờ trống...");
            selectedTimeLabel.setForeground(Theme.TEXT_MUTED);
        }
        slotsGrid.removeAll();
        JLabel loading = new JLabel("Đang tải...", SwingConstants.CENTER);
        loading.setFont(Theme.FONT_BODY_LG);
        loading.setForeground(Theme.TEXT_MUTED);
        slotsGrid.add(loading, "span 3, growx, h " + s(48) + "!");
        slotsGrid.revalidate();
        slotsGrid.repaint();

        Long staffId = selectedStaffId;
        LocalDate date = selectedDate;
        List<Long> serviceIds = selectedServices.stream().map(ServiceDtos.Response::id).toList();

        SwingWorker<List<BookingDtos.AvailableSlotResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookingDtos.AvailableSlotResponse> doInBackground() throws Exception {
                String ids = serviceIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
                String path = "/api/booking/available-slots?staffId=" + staffId
                        + "&date=" + date
                        + "&serviceIds=" + URLEncoder.encode(ids, StandardCharsets.UTF_8);
                String json = apiClient.getRaw(path);
                Type type = new TypeToken<List<BookingDtos.AvailableSlotResponse>>() {}.getType();
                return JsonUtil.fromJson(json, type);
            }

            @Override
            protected void done() {
                if (!staffId.equals(selectedStaffId) || !date.equals(selectedDate)
                        || !serviceIds.equals(selectedServices.stream().map(ServiceDtos.Response::id).toList())) {
                    return;
                }
                slotsGrid.removeAll();
                try {
                    List<BookingDtos.AvailableSlotResponse> slots = get();
                    if (slots == null || slots.isEmpty()) {
                        JLabel empty = new JLabel("Nhân viên không có ca làm trong ngày này", SwingConstants.CENTER);
                        empty.setFont(Theme.FONT_BODY_LG);
                        empty.setForeground(Theme.TEXT_MUTED);
                        slotsGrid.add(empty, "span 3, growx, h " + s(56) + "!");
                    } else {
                        boolean hasAvailableSlot = false;
                        for (BookingDtos.AvailableSlotResponse slot : slots) {
                            boolean available = Boolean.TRUE.equals(slot.available());
                            hasAvailableSlot = hasAvailableSlot || available;
                            slotsGrid.add(createTimeSlotButton(
                                    slot.start().toLocalTime(),
                                    available), "h " + s(40) + "!, growx");
                        }
                        if (!hasAvailableSlot && selectedTimeLabel != null) {
                            selectedTimeLabel.setText(noAvailableSlotText());
                            selectedTimeLabel.setForeground(Theme.AMBER);
                        }
                    }
                    if (selectedTimeLabel != null && slots != null && slots.stream().anyMatch(slot -> Boolean.TRUE.equals(slot.available()))) {
                        selectedTimeLabel.setText(selectedTimeText());
                        selectedTimeLabel.setForeground(Theme.TEXT_MUTED);
                    }
                } catch (Exception ex) {
                    JLabel error = new JLabel("Không tải được khung giờ trống", SwingConstants.CENTER);
                    error.setFont(Theme.FONT_BODY_LG);
                    error.setForeground(Theme.CRIMSON);
                    slotsGrid.add(error, "span 3, growx, h " + s(56) + "!");
                    JOptionPane.showMessageDialog(BookingWizardPanel.this,
                            "Không tải được khung giờ trống: " + rootMessage(ex),
                            "Lỗi tải dữ liệu",
                            JOptionPane.ERROR_MESSAGE);
                }
                slotsGrid.revalidate();
                slotsGrid.repaint();
            }
        };
        worker.execute();
    }

    private String noAvailableSlotText() {
        if (selectedDate.equals(LocalDate.now())) {
            return "Hôm nay không còn khung giờ trống, vui lòng chọn ngày khác";
        }
        return "Ngày này không còn khung giờ trống, vui lòng chọn ngày khác";
    }

    private void showCalendarDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chọn ngày", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new MigLayout(
                "wrap 1, insets " + gap(16) + ", fillx",
                "[fill]",
                "[][]"));
        root.setBackground(Theme.BG_CARD);

        YearMonth[] visibleMonth = {YearMonth.from(selectedDate)};
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Theme.FONT_H2);
        monthLabel.setForeground(Theme.NAVY);

        JPanel header = new JPanel(new MigLayout("insets 0, fillx", "[]push[grow]push[]"));
        header.setOpaque(false);
        JButton prevMonth = createNavButton("‹", Theme.SLATE);
        JButton nextMonth = createNavButton("›", Theme.EMERALD);
        header.add(prevMonth, "w " + s(42) + "!, h " + s(34) + "!");
        header.add(monthLabel, "growx");
        header.add(nextMonth, "w " + s(42) + "!, h " + s(34) + "!");
        root.add(header);

        JPanel calendarGrid = new JPanel();
        calendarGrid.setOpaque(false);
        root.add(calendarGrid, "growx");

        Runnable refreshCalendar = () -> {
            renderCalendarGrid(calendarGrid, monthLabel, visibleMonth[0], dialog);
            YearMonth currentMonth = YearMonth.from(LocalDate.now());
            prevMonth.setEnabled(visibleMonth[0].isAfter(currentMonth));
            root.revalidate();
            root.repaint();
            dialog.pack();
        };

        prevMonth.addActionListener(e -> {
            YearMonth currentMonth = YearMonth.from(LocalDate.now());
            if (visibleMonth[0].isAfter(currentMonth)) {
                visibleMonth[0] = visibleMonth[0].minusMonths(1);
                refreshCalendar.run();
            }
        });
        nextMonth.addActionListener(e -> {
            visibleMonth[0] = visibleMonth[0].plusMonths(1);
            refreshCalendar.run();
        });

        dialog.setContentPane(root);
        refreshCalendar.run();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void renderCalendarGrid(JPanel calendarGrid, JLabel monthLabel, YearMonth month, JDialog dialog) {
        calendarGrid.removeAll();
        calendarGrid.setLayout(new MigLayout(
                "wrap 7, insets 0, gap " + gap(6),
                repeatFixedColumns(7, s(44)),
                ""));

        monthLabel.setText("Tháng " + month.getMonthValue() + "/" + month.getYear());

        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String dayName : dayNames) {
            JLabel label = new JLabel(dayName, SwingConstants.CENTER);
            label.setFont(Theme.FONT_BODY_SM.deriveFont(Font.BOLD));
            label.setForeground(Theme.TEXT_MUTED);
            calendarGrid.add(label, "w " + s(44) + "!, h " + s(24) + "!");
        }

        int firstDayOffset = month.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < firstDayOffset; i++) {
            calendarGrid.add(Box.createRigidArea(new Dimension(s(44), s(38))), "w " + s(44) + "!, h " + s(38) + "!");
        }

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(Theme.FONT_BODY_LG);
            dayBtn.setFocusPainted(false);
            dayBtn.setBorderPainted(false);

            boolean disabled = date.isBefore(today);
            boolean selected = date.equals(selectedDate);
            if (disabled) {
                dayBtn.setEnabled(false);
                dayBtn.setBackground(new Color(229, 231, 235));
                dayBtn.setForeground(Theme.SLATE);
            } else if (selected) {
                dayBtn.setBackground(Theme.EMERALD);
                dayBtn.setForeground(Theme.TEXT_WHITE);
            } else {
                dayBtn.setBackground(new Color(209, 250, 229));
                dayBtn.setForeground(new Color(6, 95, 70));
                dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            dayBtn.addActionListener(e -> {
                updateSelectedDate(date);
                dialog.dispose();
            });
            calendarGrid.add(dayBtn, "w " + s(44) + "!, h " + s(38) + "!");
        }
    }

    private void updateSelectedDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return;
        }
        if (selectedTimeButton != null) {
            selectedTimeButton.setBackground(new Color(209, 250, 229));
            selectedTimeButton.setForeground(new Color(6, 95, 70));
        }
        selectedDate = date;
        selectedTime = null;
        selectedTimeButton = null;
        if (dateLabel != null) {
            dateLabel.setText(formatSelectedDate());
        }
        if (selectedTimeLabel != null) {
            selectedTimeLabel.setText(selectedTimeText());
            selectedTimeLabel.setForeground(Theme.TEXT_MUTED);
        }
        loadAvailableSlots();
    }

    private void ensureTodayAsDefaultDate() {
        selectedDate = LocalDate.now();
        selectedTime = null;
        selectedTimeButton = null;
        if (dateLabel != null) {
            dateLabel.setText(formatSelectedDate());
        }
        if (selectedTimeLabel != null) {
            selectedTimeLabel.setText(selectedTimeText());
            selectedTimeLabel.setForeground(Theme.TEXT_MUTED);
        }
    }

    private String formatSelectedDate() {
        return selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN")));
    }

    private String selectedTimeText() {
        if (selectedTime == null) {
            return "Vui lòng chọn khung giờ phía trên";
        }
        return "Đã chọn: " + selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + formatSelectedDate();
    }

    private static String repeatFixedColumns(int count, int width) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("[").append(width).append("!]");
        }
        return builder.toString();
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
            note = noteArea.getText();
            confirmBooking();
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
        if (currentStep == 1) {
            refreshStep2();
        }
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

    private void refreshStep2() {
        contentPanel.removeAll();
        contentPanel.add(createStep1(), "step1");
        contentPanel.add(createStep2(), "step2");
        contentPanel.add(createStep3(), "step3");
        contentPanel.add(createStep4(), "step4");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private BookingSummary buildSummary() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ServiceDtos.Response svc : selectedServices) {
            if (svc.price() != null) subtotal = subtotal.add(svc.price());
        }

        // Demo discount rule: 5% when selecting 2+ services.
        BigDecimal discount = BigDecimal.ZERO;
        if (selectedServices.size() >= 2) {
            discount = subtotal.multiply(new BigDecimal("0.05"));
        }
        BigDecimal total = subtotal.subtract(discount);

        return new BookingSummary(
            List.copyOf(selectedServices),
            selectedStaffId,
            selectedStaffName,
            selectedStaffRole,
            selectedDate,
            selectedTime,
            noteArea != null ? noteArea.getText() : note,
            subtotal,
            discount,
            total
        );
    }

    private void confirmBooking() {
        nextBtn.setEnabled(false);
        backBtn.setEnabled(false);

        SwingWorker<BookingDtos.AppointmentResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingDtos.AppointmentResponse doInBackground() throws Exception {
                BookingDtos.AdminCreateAppointmentRequest request = new BookingDtos.AdminCreateAppointmentRequest(
                        null,
                        selectedStaffId,
                        LocalDateTime.of(selectedDate, selectedTime),
                        selectedServices.stream().map(ServiceDtos.Response::id).toList(),
                        note);
                return apiClient.post("/api/admin/appointments", request, BookingDtos.AppointmentResponse.class);
            }

            @Override
            protected void done() {
                nextBtn.setEnabled(true);
                backBtn.setEnabled(true);
                try {
                    BookingDtos.AppointmentResponse response = get();
                    showBookingSuccessDialog(response);
                    if (onBookingConfirmed != null) {
                        onBookingConfirmed.accept(buildSummary());
                    }
                    resetForNewBooking();
                    ScreenRouter.go("appointments");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(BookingWizardPanel.this,
                            "Không lưu được lịch hẹn: " + rootMessage(ex),
                            "Lỗi đặt lịch",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showBookingSuccessDialog(BookingDtos.AppointmentResponse response) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Đặt lịch thành công", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);

        JPanel root = new JPanel(new MigLayout(
                "wrap 1, insets " + gap(24) + ", fillx",
                "[fill]",
                "[]12[]18[]"));
        root.setBackground(Theme.BG_CARD);

        JPanel header = new JPanel(new MigLayout("insets 0, gap " + gap(12), "[]12[grow]", "[]"));
        header.setOpaque(false);

        JLabel check = new JLabel("✓", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.EMERALD);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        check.setOpaque(false);
        check.setForeground(Theme.TEXT_WHITE);
        check.setFont(Theme.FONT_H1);
        header.add(check, "w " + s(48) + "!, h " + s(48) + "!");

        JPanel titleBlock = new JPanel(new MigLayout("wrap 1, insets 0, gapy 2", "[fill]"));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Đặt lịch thành công");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.NAVY);
        titleBlock.add(title);
        JLabel subtitle = new JLabel("Lịch hẹn đã được lưu vào hệ thống");
        subtitle.setFont(Theme.FONT_BODY_LG);
        subtitle.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(subtitle);
        header.add(titleBlock, "growx");
        root.add(header);

        JPanel details = new JPanel(new MigLayout(
                "wrap 2, insets " + gap(14) + ", gapx " + gap(18) + ", gapy " + gap(8),
                "[right][grow]",
                ""));
        details.setBackground(new Color(248, 250, 252));
        details.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));

        String idText = response != null && response.id() != null ? "#" + response.id() : "-";
        String serviceText = selectedServices.size() + " dịch vụ";
        String staffText = selectedStaffName != null ? selectedStaffName : "-";
        String timeText = selectedDate + " " + selectedTime;
        addSuccessRow(details, "Mã lịch", idText);
        addSuccessRow(details, "Dịch vụ", serviceText);
        addSuccessRow(details, "Nhân viên", staffText);
        addSuccessRow(details, "Thời gian", timeText);
        root.add(details, "growx");

        JButton ok = createNavButton("Hoàn tất", Theme.EMERALD);
        ok.addActionListener(e -> dialog.dispose());
        root.add(ok, "align right, w " + s(130) + "!, h " + s(40) + "!");

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addSuccessRow(JPanel parent, String label, String value) {
        JLabel labelView = new JLabel(label + ":");
        labelView.setFont(Theme.FONT_BODY_LG);
        labelView.setForeground(Theme.TEXT_MUTED);
        parent.add(labelView);

        JLabel valueView = new JLabel(value);
        valueView.setFont(Theme.FONT_H3);
        valueView.setForeground(Theme.NAVY);
        parent.add(valueView, "growx");
    }

    private List<ServiceDtos.Response> loadServices() {
        try {
            String json = apiClient.getRaw("/api/admin/services");
            Type type = new TypeToken<List<ServiceDtos.Response>>() {}.getType();
            List<ServiceDtos.Response> services = JsonUtil.fromJson(json, type);
            return services.stream()
                    .filter(service -> Boolean.TRUE.equals(service.isActive()))
                    .toList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách dịch vụ từ API: " + rootMessage(ex),
                    "Lỗi tải dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private List<StaffInfo> loadStaff() {
        try {
            if (selectedServices.isEmpty()) {
                return List.of();
            }
            String serviceIds = selectedServices.stream()
                    .map(ServiceDtos.Response::id)
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
            String encodedServiceIds = URLEncoder.encode(serviceIds, StandardCharsets.UTF_8);
            String json = apiClient.getRaw("/api/booking/staff?serviceIds=" + encodedServiceIds);
            Type type = new TypeToken<List<BookingDtos.StaffResponse>>() {}.getType();
            List<BookingDtos.StaffResponse> staffList = JsonUtil.fromJson(json, type);
            return staffList.stream()
                    .map(staff -> new StaffInfo(staff.id(), staff.fullName(), "Nhân viên", Boolean.TRUE.equals(staff.isActive())))
                    .toList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách nhân viên từ API: " + rootMessage(ex),
                    "Lỗi tải dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
            return List.of();
        }
    }

    private void resetForNewBooking() {
        currentStep = 0;
        selectedServices.clear();
        selectedStaffId = null;
        selectedStaffName = null;
        selectedStaffRole = null;
        ensureTodayAsDefaultDate();
        selectedTime = null;
        selectedTimeButton = null;
        note = "";
        if (noteArea != null) {
            noteArea.setText("");
        }
        contentPanel.removeAll();
        contentPanel.add(createStep1(), "step1");
        contentPanel.add(createStep2(), "step2");
        contentPanel.add(createStep3(), "step3");
        contentPanel.add(createStep4(), "step4");
        updateServiceSummary();
        updateStep();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
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

    // ========================= Step Indicator Panel =========================
    private static class StepIndicatorPanel extends RoundedPanel {
        private int currentStep = 0;
        private final JLabel[] numberLabels = new JLabel[STEP_LABELS.length];
        private final JLabel[] textLabels = new JLabel[STEP_LABELS.length];

        StepIndicatorPanel() {
            super(gap(12), Theme.BG_CARD, true);
            setLayout(new MigLayout(
                    "insets " + gap(10) + " " + gap(18) + " " + gap(10) + " " + gap(18) + ", fillx",
                    repeatColumns(STEP_LABELS.length),
                    "[center]"));
            for (int i = 0; i < STEP_LABELS.length; i++) {
                JPanel item = new JPanel(new MigLayout("wrap 1, insets 0, gapy " + gap(4), "[center]"));
                item.setOpaque(false);

                JLabel number = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
                number.setFont(Theme.FONT_H3);
                number.setOpaque(true);
                number.setPreferredSize(new Dimension(s(30), s(30)));
                number.setMinimumSize(new Dimension(s(30), s(30)));
                number.setBorder(BorderFactory.createEmptyBorder());

                JLabel label = new JLabel(STEP_LABELS[i], SwingConstants.CENTER);
                label.setFont(Theme.FONT_BODY_SM);

                numberLabels[i] = number;
                textLabels[i] = label;
                item.add(number, "w " + s(30) + "!, h " + s(30) + "!");
                item.add(label);
                add(item, "growx");
            }
            refreshState();
        }

        void setCurrentStep(int step) {
            this.currentStep = step;
            refreshState();
        }

        private void refreshState() {
            for (int i = 0; i < STEP_LABELS.length; i++) {
                boolean complete = i < currentStep;
                boolean active = i == currentStep;
                JLabel number = numberLabels[i];
                number.setText(complete ? "✓" : String.valueOf(i + 1));
                number.setBackground(active || complete ? Theme.EMERALD : Theme.BORDER);
                number.setForeground(active || complete ? Theme.TEXT_WHITE : Theme.SLATE);
                textLabels[i].setForeground(active || complete ? Theme.NAVY : Theme.SLATE);
            }
            revalidate();
            repaint();
        }

        private static String repeatColumns(int count) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                builder.append("[fill, grow]");
            }
            return builder.toString();
        }
    }
}
