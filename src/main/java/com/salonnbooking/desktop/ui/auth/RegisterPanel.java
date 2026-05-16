package com.salonnbooking.desktop.ui.auth;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.api.ApiException;
import com.salonnbooking.desktop.model.AuthModels;
import com.salonnbooking.desktop.model.Gender;

public class RegisterPanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";

    private final JTextField fullNameField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);
    private final JComboBox<Gender> genderBox = new JComboBox<>(Gender.values());

    private final JButton registerButton = new JButton("Register");
    private final JButton backButton = new JButton("Back to Login");

    private final ApiClient apiClient = new ApiClient(BASE_URL);

    public RegisterPanel(Runnable onBackToLogin) {
        super(new BorderLayout(0, 16));

        JLabel title = new JLabel("Register Customer");
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Full name"), gbc);
        gbc.gridx = 1;
        form.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        form.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        form.add(new JLabel("Phone"), gbc);
        gbc.gridx = 1;
        form.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        form.add(new JLabel("Password"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Gender"), gbc);
        gbc.gridx = 1;
        form.add(genderBox, gbc);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(backButton);
        actions.add(registerButton);
        add(actions, BorderLayout.SOUTH);

        backButton.addActionListener(e -> onBackToLogin.run());
        registerButton.addActionListener(e -> doRegister(onBackToLogin));
    }

    private void setLoading(boolean loading) {
        registerButton.setEnabled(!loading);
        backButton.setEnabled(!loading);
        fullNameField.setEnabled(!loading);
        emailField.setEnabled(!loading);
        phoneField.setEnabled(!loading);
        passwordField.setEnabled(!loading);
        genderBox.setEnabled(!loading);
        setCursor(loading ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    private void doRegister(Runnable onSuccessBackToLogin) {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());
        Gender gender = (Gender) genderBox.getSelectedItem();

        if (email.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Email and password are required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        setLoading(true);
        SwingWorker<AuthModels.ProfileResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthModels.ProfileResponse doInBackground() throws Exception {
                return apiClient.post("/api/auth/register",
                        new AuthModels.RegisterRequest(fullName, email, phone, password, gender),
                        AuthModels.ProfileResponse.class);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    get();
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Register success. Please login.", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    onSuccessBackToLogin.run();
                } catch (Exception ex) {
                    String message = ex.getMessage();
                    if (ex.getCause() instanceof ApiException apiEx) {
                        message = apiEx.getResponseBody();
                    }
                    JOptionPane.showMessageDialog(RegisterPanel.this, "Register failed: " + message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
