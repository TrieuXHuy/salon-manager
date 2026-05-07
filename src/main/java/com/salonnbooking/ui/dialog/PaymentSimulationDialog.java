package com.salonnbooking.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class PaymentSimulationDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

	private boolean paid;
	private final JButton closeButton = new JButton("Đóng");
	private final JProgressBar progressBar = new JProgressBar();

	public PaymentSimulationDialog(Window owner, Integer appointmentId, String customerName,
			String serviceName, BigDecimal amount, String methodLabel) {
		super(owner, "Thanh toán mô phỏng", ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(430, 520);
		setResizable(false);
		setLocationRelativeTo(owner);
		setContentPane(createContent(appointmentId, customerName, serviceName, amount, methodLabel));
		startAutoSuccessTimer();
	}

	private JPanel createContent(Integer appointmentId, String customerName, String serviceName,
			BigDecimal amount, String methodLabel) {
		JPanel root = new JPanel(new BorderLayout(12, 12));
		root.setBorder(new EmptyBorder(18, 18, 18, 18));
		root.setBackground(UIManager.getColor("Panel.background"));

		JLabel title = new JLabel("Quét QR để thanh toán");
		title.setFont(new Font("Segoe UI", Font.BOLD, 20));
		root.add(title, BorderLayout.NORTH);

		JPanel center = new JPanel(new BorderLayout(10, 10));
		center.setOpaque(false);

		String payload = "SALON|APT=" + appointmentId + "|AMOUNT=" + amount + "|METHOD=" + methodLabel;
		center.add(new FakeQrPanel(payload), BorderLayout.NORTH);

		String summary = "<html>"
				+ "<b>Khách hàng:</b> " + escape(customerName) + "<br>"
				+ "<b>Dịch vụ:</b> " + escape(serviceName) + "<br>"
				+ "<b>Số tiền:</b> " + CURRENCY.format(amount) + "<br>"
				+ "<b>Phương thức:</b> " + escape(methodLabel) + "<br><br>"
				+ "Đây là thanh toán mô phỏng. Hệ thống sẽ tự xác nhận sau 2 giây."
				+ "</html>";
		JLabel info = new JLabel(summary);
		info.setBorder(new EmptyBorder(8, 0, 0, 0));
		center.add(info, BorderLayout.CENTER);
		root.add(center, BorderLayout.CENTER);

		JPanel bottom = new JPanel(new BorderLayout(8, 8));
		bottom.setOpaque(false);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Đang chờ thanh toán...");
		bottom.add(progressBar, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		actions.setOpaque(false);
		closeButton.setEnabled(false);
		closeButton.addActionListener(e -> dispose());
		actions.add(closeButton);
		bottom.add(actions, BorderLayout.SOUTH);
		root.add(bottom, BorderLayout.SOUTH);

		return root;
	}

	private void startAutoSuccessTimer() {
		Timer timer = new Timer(2000, e -> {
			paid = true;
			progressBar.setIndeterminate(false);
			progressBar.setValue(100);
			progressBar.setString("Thanh toán thành công");
			closeButton.setEnabled(true);
			dispose();
		});
		timer.setRepeats(false);
		timer.start();
	}

	public boolean isPaid() {
		return paid;
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static class FakeQrPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private static final int CELLS = 29;
		private final String payload;

		FakeQrPanel(String payload) {
			this.payload = payload;
			setPreferredSize(new Dimension(220, 220));
			setMinimumSize(new Dimension(220, 220));
			setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
			setBackground(Color.WHITE);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int size = Math.min(getWidth(), getHeight()) - 24;
			int cell = size / CELLS;
			int startX = (getWidth() - cell * CELLS) / 2;
			int startY = (getHeight() - cell * CELLS) / 2;

			g.setColor(Color.WHITE);
			g.fillRect(startX, startY, cell * CELLS, cell * CELLS);
			g.setColor(Color.BLACK);
			drawFinder(g, startX, startY, cell);
			drawFinder(g, startX + cell * 22, startY, cell);
			drawFinder(g, startX, startY + cell * 22, cell);

			int hash = payload.hashCode();
			for (int y = 0; y < CELLS; y++) {
				for (int x = 0; x < CELLS; x++) {
					if (inFinder(x, y)) {
						continue;
					}
					int bit = Integer.rotateLeft(hash ^ (x * 73856093) ^ (y * 19349663), (x + y) % 16);
					if ((bit & 0x3) == 0) {
						g.fillRect(startX + x * cell, startY + y * cell, cell, cell);
					}
				}
			}
		}

		private void drawFinder(Graphics g, int x, int y, int cell) {
			g.fillRect(x, y, cell * 7, cell * 7);
			g.setColor(Color.WHITE);
			g.fillRect(x + cell, y + cell, cell * 5, cell * 5);
			g.setColor(Color.BLACK);
			g.fillRect(x + cell * 2, y + cell * 2, cell * 3, cell * 3);
		}

		private boolean inFinder(int x, int y) {
			return (x < 8 && y < 8) || (x > 20 && y < 8) || (x < 8 && y > 20);
		}
	}
}
