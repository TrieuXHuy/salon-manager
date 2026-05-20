package com.salonnbooking.ui;

import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JLabel loadingLabel;

    private static int s(int value) {
        return Theme.scaleDimension(value);
    }

    private static int gap(int value) {
        float scale = Math.min(Theme.getDPIScaleFactor(), 1.25f);
        return Math.round(value * scale);
    }

    private static int controlHeight(int value) {
        float scale = Math.min(Theme.getDPIScaleFactor(), 1.35f);
        return Math.round(value * scale);
    }

    public LoginFrame() {
        // Apply FlatLaf global style rules (10px bo góc, v.v.)
        Theme.setupTheme();

        setTitle("Salon Booking Manager - Đăng nhập");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        // Scale window size based on DPI
        setSize(s(950), s(620));
        setMinimumSize(new Dimension(s(850), s(550)));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BG_MAIN);

        // 1. Left Pane: Brand promotion banner (40% width)
        JPanel brandPane = new JPanel(new MigLayout(
                "wrap 1, fill, insets " + gap(40) + " " + gap(30) + " " + gap(40) + " " + gap(30),
                "[fill]",
                "[]" + gap(36) + "[]push[]"));
        brandPane.setBackground(Theme.BG_SIDEBAR);
        brandPane.setPreferredSize(new Dimension(s(380), 0));

        // Brand Logo
        JLabel logoLabel = new JLabel("SalonManager");
        logoLabel.setFont(Theme.FONT_HERO);
        logoLabel.setForeground(Theme.EMERALD);
        brandPane.add(logoLabel);

        // Features list
        JPanel featuresPanel = new JPanel(new MigLayout("wrap 1, insets 0, gapy " + gap(16), "[fill]"));
        featuresPanel.setOpaque(false);

        JLabel introLabel = new JLabel("Hệ thống quản lý đặt lịch chuyên nghiệp");
        introLabel.setFont(Theme.FONT_H2);
        introLabel.setForeground(Theme.TEXT_WHITE);
        featuresPanel.add(introLabel, "gapbottom " + gap(10));

        featuresPanel.add(createFeatureItem("⚡  Đặt lịch nhanh chóng trong 30 giây"));
        featuresPanel.add(createFeatureItem("👥  Sắp xếp nhân sự & ca làm việc thông minh"));
        featuresPanel.add(createFeatureItem("📈  Báo cáo doanh thu & hiệu suất trực quan"));
        featuresPanel.add(createFeatureItem("🎨  Trải nghiệm giao diện Swing cao cấp"));

        brandPane.add(featuresPanel);

        // Footer version info
        JLabel versionLabel = new JLabel("Phiên bản 1.0.0 • Phát triển bởi Antigravity");
        versionLabel.setFont(Theme.FONT_BODY_SM);
        versionLabel.setForeground(Theme.SLATE);
        brandPane.add(versionLabel);

        rootPanel.add(brandPane, BorderLayout.WEST);

        // 2. Right Pane: Login Form area (60% width)
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Theme.BG_MAIN);

        // Login Card with 16px corner radius and shadow
        RoundedPanel card = new RoundedPanel(gap(16), Theme.BG_CARD, true);
        card.setPreferredSize(new Dimension(s(430), s(545)));
        card.setLayout(new MigLayout(
                "wrap 1, fillx, insets " + gap(34) + " " + gap(30) + " " + gap(28) + " " + gap(30),
                "[fill]"));

        // Card header
        JLabel cardTitle = new JLabel("ĐĂNG NHẬP");
        cardTitle.setFont(Theme.FONT_H1);
        cardTitle.setForeground(Theme.NAVY);
        cardTitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(cardTitle, "align center, gapbottom " + gap(2));

        card.add(Box.createVerticalStrut(gap(26)));

        // Input 1: Username
        JLabel usernameLabel = new JLabel("Tài khoản");
        usernameLabel.setFont(Theme.FONT_H3);
        usernameLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(usernameLabel, "gapbottom " + gap(6));

        usernameField = new JTextField();
        usernameField.setFont(Theme.FONT_BODY_LG);
        usernameField.putClientProperty("JTextField.placeholderText", "example@example.com");
        card.add(usernameField, "h " + controlHeight(40) + "!, gapbottom " + gap(18));

        // Input 2: Password
        JLabel passwordLabel = new JLabel("Mật khẩu");
        passwordLabel.setFont(Theme.FONT_H3);
        passwordLabel.setForeground(Theme.TEXT_PRIMARY);
        card.add(passwordLabel, "gapbottom " + gap(6));

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.FONT_BODY_LG);
        passwordField.putClientProperty("JTextField.placeholderText", "••••••••");
        card.add(passwordField, "h " + controlHeight(40) + "!, gapbottom " + gap(26));

        // Login Button
        loginBtn = new JButton("Đăng nhập");
        loginBtn.setFont(Theme.FONT_H3);
        loginBtn.setForeground(Theme.TEXT_WHITE);
        loginBtn.setBackground(Theme.EMERALD);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> handleLogin());
        card.add(loginBtn, "h " + controlHeight(44) + "!, gapbottom " + gap(10));

        registerBtn = new JButton("Đăng ký tài khoản khách hàng");
        registerBtn.setFont(Theme.FONT_H3);
        registerBtn.setForeground(Theme.EMERALD);
        registerBtn.setContentAreaFilled(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> showRegisterDialog());
        card.add(registerBtn, "h " + controlHeight(34) + "!, gapbottom " + gap(4));

        // Loading spinner placeholder
        loadingLabel = new JLabel("Đang xác thực thông tin...", SwingConstants.CENTER);
        loadingLabel.setFont(Theme.FONT_BODY_SM);
        loadingLabel.setForeground(Theme.EMERALD);
        loadingLabel.setVisible(false);
        card.add(loadingLabel, "align center, h " + controlHeight(20) + "!");

        // Center card inside right pane
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        formContainer.add(card, gbc);

        rootPanel.add(formContainer, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private final com.salonnbooking.desktop.api.ApiClient apiClient = new com.salonnbooking.desktop.api.ApiClient("http://localhost:8080");

    private JLabel createFeatureItem(String text) {
        JLabel item = new JLabel(text);
        item.setFont(Theme.FONT_BODY_LG);
        item.setForeground(new Color(203, 213, 225)); // Light slate color
        return item;
    }

    private void handleLogin() {
        String email = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng điền đầy đủ tài khoản và mật khẩu!", 
                "Thông báo", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show loading progress
        loginBtn.setEnabled(false);
        registerBtn.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loadingLabel.setVisible(true);

        // Perform login via API. Authentication must be validated by the backend.
        SwingWorker<com.salonnbooking.desktop.model.AuthModels.MeResponse, Void> worker = new SwingWorker<>() {
            private String token;

            @Override
            protected com.salonnbooking.desktop.model.AuthModels.MeResponse doInBackground() throws Exception {
                com.salonnbooking.desktop.model.AuthModels.AuthResponse auth = apiClient.post("/api/auth/login",
                        new com.salonnbooking.desktop.model.AuthModels.LoginRequest(email, password),
                        com.salonnbooking.desktop.model.AuthModels.AuthResponse.class);
                token = auth.token();

                // Set token to Client/Session to authorize subsequent requests
                com.salonnbooking.desktop.session.AuthSession.getInstance().setToken(token);

                return apiClient.get("/api/auth/me", com.salonnbooking.desktop.model.AuthModels.MeResponse.class);
            }

            @Override
            protected void done() {
                try {
                    com.salonnbooking.desktop.model.AuthModels.MeResponse me = get();
                    if (me != null) {
                        com.salonnbooking.desktop.session.AuthSession session = com.salonnbooking.desktop.session.AuthSession.getInstance();
                        session.setToken(token);
                        session.setUserId(me.id());
                        session.setFullName(me.fullName());
                        session.setRole(me.role());

                        java.awt.EventQueue.invokeLater(() -> {
                            switch (me.role()) {
                                case ADMIN -> new com.salonnbooking.desktop.ui.shared.AdminDashboardFrame().setVisible(true);
                                case STAFF -> new com.salonnbooking.desktop.ui.shared.StaffDashboardFrame().setVisible(true);
                                case CUSTOMER -> new com.salonnbooking.desktop.ui.shared.CustomerDashboardFrame().setVisible(true);
                            }
                        });
                        dispose();
                    } else {
                        throw new Exception("Xác thực không thành công.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this, 
                        "Đăng nhập thất bại. Vui lòng kiểm tra tài khoản, mật khẩu hoặc trạng thái máy chủ.", 
                        "Lỗi", 
                        JOptionPane.ERROR_MESSAGE);
                    resetLoginState();
                }
            }
        };
        worker.execute();
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Đăng ký khách hàng", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        RoundedPanel panel = new RoundedPanel(gap(14), Theme.BG_CARD, false);
        panel.setLayout(new MigLayout(
                "wrap 1, fillx, insets " + gap(24) + " " + gap(24) + " " + gap(22) + " " + gap(24),
                "[fill]"));

        JLabel title = new JLabel("TẠO TÀI KHOẢN");
        title.setFont(Theme.FONT_H1);
        title.setForeground(Theme.NAVY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title, "align center, gapbottom " + gap(12));

        JTextField fullNameField = createRegisterTextField("Nguyễn Văn A");
        JTextField emailField = createRegisterTextField("email@example.com");
        JTextField phoneField = createRegisterTextField("0900000000");
        JPasswordField registerPasswordField = new JPasswordField();
        registerPasswordField.setFont(Theme.FONT_BODY_LG);
        registerPasswordField.putClientProperty("JTextField.placeholderText", "Tối thiểu 6 ký tự");
        JComboBox<com.salonnbooking.desktop.model.Gender> genderBox =
                new JComboBox<>(com.salonnbooking.desktop.model.Gender.values());
        genderBox.setFont(Theme.FONT_BODY_LG);

        addRegisterField(panel, "Họ tên", fullNameField);
        addRegisterField(panel, "Email", emailField);
        addRegisterField(panel, "Số điện thoại", phoneField);
        addRegisterField(panel, "Mật khẩu", registerPasswordField);
        addRegisterField(panel, "Giới tính", genderBox);

        JButton submitBtn = new JButton("Đăng ký");
        submitBtn.setFont(Theme.FONT_H3);
        submitBtn.setForeground(Theme.TEXT_WHITE);
        submitBtn.setBackground(Theme.EMERALD);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(submitBtn, "h " + controlHeight(42) + "!, gaptop " + gap(10));

        submitBtn.addActionListener(event -> {
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String registerPassword = new String(registerPasswordField.getPassword());
            com.salonnbooking.desktop.model.Gender gender =
                    (com.salonnbooking.desktop.model.Gender) genderBox.getSelectedItem();

            if (fullName.isBlank() || email.isBlank() || registerPassword.isBlank()) {
                JOptionPane.showMessageDialog(dialog,
                        "Vui lòng nhập họ tên, email và mật khẩu.",
                        "Thiếu thông tin",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            submitBtn.setEnabled(false);
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    apiClient.post("/api/auth/register",
                            new com.salonnbooking.desktop.model.AuthModels.RegisterRequest(
                                    fullName,
                                    email,
                                    phone,
                                    registerPassword,
                                    gender),
                            com.salonnbooking.desktop.model.AuthModels.ProfileResponse.class);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        usernameField.setText(email);
                        passwordField.setText(registerPassword);
                        JOptionPane.showMessageDialog(dialog,
                                "Đăng ký thành công. Bạn có thể đăng nhập ngay.",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Đăng ký thất bại: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        submitBtn.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(s(420), s(520)));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JTextField createRegisterTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(Theme.FONT_BODY_LG);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private void addRegisterField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_H3);
        label.setForeground(Theme.TEXT_PRIMARY);
        panel.add(label, "gapbottom " + gap(4));
        panel.add(field, "h " + controlHeight(38) + "!, gapbottom " + gap(10));
    }

    private void resetLoginState() {
        loginBtn.setEnabled(true);
        registerBtn.setEnabled(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loadingLabel.setVisible(false);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
