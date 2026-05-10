package com.salonnbooking.ui.auth;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.salonnbooking.SwingClient;
import com.salonnbooking.domain.UserAccount;
import com.salonnbooking.service.AuthService;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;

import net.miginfocom.swing.MigLayout;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final Color BG_MAIN = new Color(244, 247, 252);
	private static final Color TEXT_MAIN = new Color(15, 23, 42);
	private static final Color TEXT_MUTED = new Color(100, 116, 139);
	private static final Color BORDER = new Color(203, 213, 225);

	private final ApplicationContext applicationContext;
	private final JTextField usernameField = new JTextField("admin");
	private final JPasswordField passwordField = new JPasswordField("admin123");
	private final JLabel statusLabel = new JLabel("Đăng nhập để vào hệ thống");
	private final JButton loginButton = new JButton("Đăng nhập");

	public LoginFrame(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;

		setTitle("Salon Manager - Đăng nhập");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(520, 360);
		setLocationRelativeTo(null);
		setResizable(false);

		initUi();
	}

	private void initUi() {
		JPanel root = new JPanel(new MigLayout("fill, insets 24", "[grow]", "[grow]"));
		root.setBackground(BG_MAIN);

		RoundedPanel card = new RoundedPanel(20, Color.WHITE, true);
		card.setLayout(new MigLayout("wrap 1, fillx, insets 28 30 28 30", "[grow, fill]", "[]12[]20[]10[]18[]14[]"));

		JLabel title = new JLabel("Đăng nhập hệ thống");
		title.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 18)));
		title.setForeground(TEXT_MAIN);

		JLabel subtitle = new JLabel("Tài khoản được kiểm tra trực tiếp từ database");
		subtitle.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		subtitle.setForeground(TEXT_MUTED);

		card.add(title);
		card.add(subtitle);
		card.add(createFieldBlock("Tên đăng nhập", usernameField), "growx");
		card.add(createFieldBlock("Mật khẩu", passwordField), "growx");

		statusLabel.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 11)));
		statusLabel.setForeground(TEXT_MUTED);
		card.add(statusLabel);

		loginButton.setFont(Theme.scaleFont(new Font("Segoe UI", Font.BOLD, 12)));
		loginButton.setBackground(Theme.PRIMARY);
		loginButton.setForeground(Color.WHITE);
		loginButton.setFocusPainted(false);
		loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		loginButton.setBorder(new EmptyBorder(10, 14, 10, 14));
		loginButton.addActionListener(e -> attemptLogin());
		card.add(loginButton, "growx");

		getRootPane().setDefaultButton(loginButton);
		root.add(card, "align center, w 100%, h 100%");
		setContentPane(root);
	}

	private JPanel createFieldBlock(String labelText, javax.swing.JComponent field) {
		JPanel panel = new JPanel(new MigLayout("wrap 1, insets 0, fillx", "[grow, fill]", "[]6[]"));
		panel.setOpaque(false);

		JLabel label = new JLabel(labelText);
		label.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 12)));
		label.setForeground(TEXT_MAIN);

		field.setFont(Theme.scaleFont(new Font("Segoe UI", Font.PLAIN, 13)));
		field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				javax.swing.BorderFactory.createLineBorder(BORDER),
				new EmptyBorder(10, 12, 10, 12)));

		panel.add(label);
		panel.add(field, "growx");
		return panel;
	}

	private void attemptLogin() {
		String username = usernameField.getText().trim();
		String password = new String(passwordField.getPassword());

		if (username.isBlank() || password.isBlank()) {
			statusLabel.setText("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
			statusLabel.setForeground(Theme.DANGER);
			return;
		}

		setLoading(true);

		SwingWorker<UserAccount, Void> worker = new SwingWorker<>() {
			@Override
			protected UserAccount doInBackground() {
				AuthService authService = applicationContext.getBean(AuthService.class);
				return authService.authenticate(username, password);
			}

			@Override
			protected void done() {
				try {
					UserAccount user = get();
					dispose();
					SwingClient.showDashboard(user);
				} catch (Exception ex) {
					String message = "Đăng nhập thất bại";
					Throwable cause = ex.getCause();
					if (cause instanceof ResponseStatusException responseStatusException) {
						message = responseStatusException.getReason();
					}
					statusLabel.setText(message);
					statusLabel.setForeground(Theme.DANGER);
					JOptionPane.showMessageDialog(LoginFrame.this, message, "Đăng nhập thất bại",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					setLoading(false);
				}
			}
		};

		worker.execute();
	}

	private void setLoading(boolean loading) {
		usernameField.setEnabled(!loading);
		passwordField.setEnabled(!loading);
		loginButton.setEnabled(!loading);
		statusLabel.setText(loading ? "Đang kiểm tra tài khoản..." : statusLabel.getText());
		statusLabel.setForeground(loading ? TEXT_MUTED : statusLabel.getForeground());
	}
}
