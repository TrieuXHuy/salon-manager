package com.salonnbooking.desktop.ui.auth;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.api.ApiException;
import com.salonnbooking.desktop.model.AuthModels;
import com.salonnbooking.desktop.session.AuthSession;
import com.salonnbooking.desktop.ui.shared.AdminDashboardFrame;
import com.salonnbooking.desktop.ui.shared.CustomerDashboardFrame;
import com.salonnbooking.desktop.ui.shared.StaffDashboardFrame;

public class LoginPanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";

    private final JTextField emailField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);
    private final JButton loginButton = new JButton("Login");
    private final JButton toRegisterButton = new JButton("Register (Customer)");

    private final ApiClient apiClient = new ApiClient(BASE_URL);

    public LoginPanel(Runnable onNavigateRegister) {
        super(new BorderLayout(0, 16));

        JLabel title = new JLabel("Login");
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Email"), gbc);

        gbc.gridx = 1;
        form.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(toRegisterButton);
        actions.add(loginButton);
        add(actions, BorderLayout.SOUTH);

        toRegisterButton.addActionListener(e -> onNavigateRegister.run());
        loginButton.addActionListener(e -> doLogin());
    }

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        toRegisterButton.setEnabled(!loading);
        emailField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        setCursor(loading ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Email and password are required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        setLoading(true);
        SwingWorker<AuthModels.MeResponse, Void> worker = new SwingWorker<>() {
            private String token;

            @Override
            protected AuthModels.MeResponse doInBackground() throws Exception {
                AuthModels.AuthResponse auth = apiClient.post("/api/auth/login",
                        new AuthModels.LoginRequest(email, password),
                        AuthModels.AuthResponse.class);
                token = auth.token();
                AuthSession.getInstance().setToken(token);
                return apiClient.get("/api/auth/me", AuthModels.MeResponse.class);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    AuthModels.MeResponse me = get();
                    AuthSession session = AuthSession.getInstance();
                    session.setUserId(me.id());
                    session.setFullName(me.fullName());
                    session.setRole(me.role());

                    java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(LoginPanel.this);
                    if (window != null) {
                        window.dispose();
                    }

                    switch (me.role()) {
                        case ADMIN -> new AdminDashboardFrame().setVisible(true);
                        case STAFF -> new StaffDashboardFrame().setVisible(true);
                        case CUSTOMER -> new CustomerDashboardFrame().setVisible(true);
                        default -> JOptionPane.showMessageDialog(LoginPanel.this, "Unknown role: " + me.role());
                    }
                } catch (Exception ex) {
                    AuthSession.getInstance().clear();
                    String message = ex.getMessage();
                    if (ex.getCause() instanceof ApiException apiEx) {
                        message = apiEx.getResponseBody();
                    }
                    JOptionPane.showMessageDialog(LoginPanel.this, "Login failed: " + message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
