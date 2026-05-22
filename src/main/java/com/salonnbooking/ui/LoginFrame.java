package com.salonnbooking.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.salonnbooking.api.dto.AuthRequests;
import com.salonnbooking.client.ApiClient;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Color BG_MAIN = new Color(248, 250, 252);
	private static final Color PRIMARY = new Color(109, 73, 224);
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);

	private final JTextField usernameField = new JTextField();
	private final JPasswordField passwordField = new JPasswordField();
	private final JButton loginButton = new JButton("Login");
	private final JButton registerButton = new JButton("Register");
	private final Consumer<AuthRequests.Response> onLoginSuccess;

	public LoginFrame(Consumer<AuthRequests.Response> onLoginSuccess) {
		this.onLoginSuccess = onLoginSuccess;
		setTitle("Salon Manager - Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(420, 360);
		setLocationRelativeTo(null);
		setResizable(false);
		initUI();
	}

	private void initUI() {
		JPanel root = new JPanel(new GridBagLayout());
		root.setBackground(BG_MAIN);
		root.setBorder(new EmptyBorder(24, 24, 24, 24));

		RoundedPanel card = new RoundedPanel(18, Color.WHITE, true);
		card.setLayout(new BorderLayout(0, 18));
		card.setBorder(new EmptyBorder(24, 28, 24, 28));

		JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
		titlePanel.setOpaque(false);
		JLabel title = new JLabel("Salon Manager");
		title.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 24)));
		title.setForeground(PRIMARY);
		JLabel subtitle = new JLabel("Login with username and password");
		subtitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13)));
		subtitle.setForeground(TEXT_MUTED);
		titlePanel.add(title, BorderLayout.NORTH);
		titlePanel.add(subtitle, BorderLayout.CENTER);
		card.add(titlePanel, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 0, 6, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.weightx = 1;
		addField(form, gbc, 0, "Username", usernameField);
		addField(form, gbc, 2, "Password", passwordField);
		card.add(form, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new GridBagLayout());
		buttons.setOpaque(false);
		GridBagConstraints buttonGbc = new GridBagConstraints();
		buttonGbc.insets = new Insets(0, 0, 0, 8);
		buttonGbc.fill = GridBagConstraints.HORIZONTAL;
		buttonGbc.weightx = 1;
		stylePrimaryButton(loginButton);
		styleSecondaryButton(registerButton);
		loginButton.addActionListener(e -> submit(true));
		registerButton.addActionListener(e -> submit(false));
		buttons.add(loginButton, buttonGbc);
		buttonGbc.gridx = 1;
		buttonGbc.insets = new Insets(0, 8, 0, 0);
		buttons.add(registerButton, buttonGbc);
		card.add(buttons, BorderLayout.SOUTH);

		root.add(card);
		setContentPane(root);
		getRootPane().setDefaultButton(loginButton);
	}

	private void addField(JPanel form, GridBagConstraints gbc, int row, String labelText, JTextField field) {
		JLabel label = new JLabel(labelText);
		label.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		label.setForeground(TEXT_MAIN);
		gbc.gridy = row;
		form.add(label, gbc);

		field.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 14)));
		field.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(203, 213, 225)),
				new EmptyBorder(9, 10, 9, 10)));
		gbc.gridy = row + 1;
		form.add(field, gbc);
	}

	private void stylePrimaryButton(JButton button) {
		button.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 13)));
		button.setForeground(Color.WHITE);
		button.setBackground(PRIMARY);
		button.setFocusPainted(false);
		button.setBorder(new EmptyBorder(10, 16, 10, 16));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void styleSecondaryButton(JButton button) {
		button.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 13)));
		button.setForeground(PRIMARY);
		button.setBackground(new Color(237, 233, 255));
		button.setFocusPainted(false);
		button.setBorder(new EmptyBorder(10, 16, 10, 16));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void submit(boolean login) {
		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword());
		if (username.isBlank() || password.isBlank()) {
			JOptionPane.showMessageDialog(this, "Username and password are required.", "Validation",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		setButtonsEnabled(false);
		new SwingWorker<AuthRequests.Response, Void>() {
			@Override
			protected AuthRequests.Response doInBackground() throws Exception {
				return login ? ApiClient.login(username, password) : ApiClient.register(username, password);
			}

			@Override
			protected void done() {
				setButtonsEnabled(true);
				try {
					AuthRequests.Response response = get();
					if (!login) {
						JOptionPane.showMessageDialog(LoginFrame.this,
								"Register successful. Logged in as " + response.username() + ".");
					}
					onLoginSuccess.accept(response);
					dispose();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(LoginFrame.this, cleanError(ex), "Authentication failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	private void setButtonsEnabled(boolean enabled) {
		loginButton.setEnabled(enabled);
		registerButton.setEnabled(enabled);
	}

	private String cleanError(Exception ex) {
		Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
		String message = cause.getMessage();
		return message == null || message.isBlank() ? "Cannot connect to server." : message;
	}
}
