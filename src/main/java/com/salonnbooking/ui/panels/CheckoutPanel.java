package com.salonnbooking.ui.panels;

import com.salonnbooking.api.dto.ServiceDtos;
import com.salonnbooking.ui.components.RoundedPanel;
import com.salonnbooking.ui.theme.Theme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CheckoutPanel extends JPanel {

    public enum PaymentMethod { CASH, MOMO, CARD }

    private final NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final JLabel bookingInfoLabel = new JLabel("Chưa có thông tin đặt lịch.");
    private final DefaultTableModel tableModel;
    private final JLabel subtotalValue = new JLabel("0đ");
    private final JLabel discountValue = new JLabel("0đ");
    private final JLabel totalValue = new JLabel("0đ");

    private final JRadioButton cashRb = new JRadioButton("CASH");
    private final JRadioButton momoRb = new JRadioButton("MOMO");
    private final JRadioButton cardRb = new JRadioButton("CARD");

    private BookingWizardPanel.BookingSummary summary;

    public CheckoutPanel() {
        setBackground(Theme.BG_MAIN);
        setLayout(new MigLayout("fill, insets 24", "[grow]", "[][grow][]"));

        JLabel title = new JLabel("Checkout");
        title.setFont(Theme.FONT_H1.deriveFont(20f));
        title.setForeground(Theme.NAVY);
        add(title, "wrap");

        RoundedPanel content = new RoundedPanel(12, Theme.BG_CARD, true);
        content.setLayout(new MigLayout("fill, insets 18", "[grow][360!]", "[][grow]"));

        // Left: invoice details
        JPanel left = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[grow]", "[]12[grow]"));
        left.setOpaque(false);

        bookingInfoLabel.setFont(Theme.FONT_BODY_LG);
        bookingInfoLabel.setForeground(Theme.TEXT_MUTED);
        left.add(bookingInfoLabel, "growx");

        tableModel = new DefaultTableModel(new Object[]{"Dịch vụ", "Đơn giá"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(Theme.FONT_BODY_REG);
        table.setFont(Theme.FONT_BODY_LG);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(right);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        left.add(sp, "grow");

        content.add(left, "grow");

        // Right: totals + payment method
        JPanel rightCol = new JPanel(new MigLayout("fill, wrap 1, insets 0", "[grow]", "[]16[]16[]push[]"));
        rightCol.setOpaque(false);

        rightCol.add(sectionTitle("Tổng kết"), "growx");
        rightCol.add(totalsCard(), "growx");

        rightCol.add(sectionTitle("Phương thức thanh toán"), "growx");
        rightCol.add(paymentCard(), "growx");

        content.add(rightCol, "grow, wrap");

        add(content, "grow, wrap");

        JButton confirmPayBtn = new JButton("Xác nhận thanh toán");
        confirmPayBtn.setFont(Theme.FONT_H2);
        confirmPayBtn.setForeground(Theme.TEXT_WHITE);
        confirmPayBtn.setBackground(Theme.EMERALD);
        confirmPayBtn.setFocusPainted(false);
        confirmPayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        confirmPayBtn.setBorderPainted(false);
        confirmPayBtn.addActionListener(e -> onConfirmPayment());
        add(confirmPayBtn, "h 46!, w 260!, align right");

        // default selection
        cashRb.setSelected(true);
    }

    public void setBookingSummary(BookingWizardPanel.BookingSummary summary) {
        this.summary = summary;
        renderSummary();
    }

    private void renderSummary() {
        tableModel.setRowCount(0);
        if (summary == null) {
            bookingInfoLabel.setText("Chưa có thông tin đặt lịch.");
            subtotalValue.setText("0đ");
            discountValue.setText("0đ");
            totalValue.setText("0đ");
            return;
        }

        List<ServiceDtos.Response> services = summary.services() != null ? summary.services() : List.of();
        for (ServiceDtos.Response svc : services) {
            String price = svc.price() != null ? nf.format(svc.price()) + "đ" : "0đ";
            tableModel.addRow(new Object[]{svc.name(), price});
        }

        String staff = summary.staffName() != null ? summary.staffName() : "N/A";
        String role = summary.staffRole() != null ? (" - " + summary.staffRole()) : "";
        String timeStr = "N/A";
        if (summary.date() != null && summary.time() != null) {
            LocalDateTime dt = LocalDateTime.of(summary.date(), summary.time());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy", new Locale("vi", "VN"));
            timeStr = dt.format(dtf);
        }
        bookingInfoLabel.setText("Nhân viên: " + staff + role + " | Thời gian: " + timeStr);

        subtotalValue.setText(formatMoney(summary.subtotal()));
        discountValue.setText(formatMoney(summary.discount()));
        totalValue.setText(formatMoney(summary.total()));
    }

    private String formatMoney(BigDecimal money) {
        if (money == null) return "0đ";
        return nf.format(money) + "đ";
    }

    private JComponent totalsCard() {
        RoundedPanel card = new RoundedPanel(10, new Color(248, 250, 252), true);
        card.setLayout(new MigLayout("fillx, insets 14", "[grow][]", "[]10[]10[]"));

        JLabel sub = new JLabel("Tạm tính");
        sub.setFont(Theme.FONT_BODY_LG);
        sub.setForeground(Theme.TEXT_MUTED);
        card.add(sub);
        subtotalValue.setFont(Theme.FONT_H3);
        subtotalValue.setForeground(Theme.TEXT_PRIMARY);
        card.add(subtotalValue, "align right, wrap");

        JLabel dis = new JLabel("Chiết khấu");
        dis.setFont(Theme.FONT_BODY_LG);
        dis.setForeground(Theme.TEXT_MUTED);
        card.add(dis);
        discountValue.setFont(Theme.FONT_H3);
        discountValue.setForeground(Theme.AMBER);
        card.add(discountValue, "align right, wrap");

        JLabel total = new JLabel("Tổng tiền");
        total.setFont(Theme.FONT_H2);
        total.setForeground(Theme.NAVY);
        card.add(total);
        totalValue.setFont(Theme.FONT_H1.deriveFont(18f));
        totalValue.setForeground(Theme.EMERALD);
        card.add(totalValue, "align right");

        return card;
    }

    private JComponent paymentCard() {
        RoundedPanel card = new RoundedPanel(10, new Color(248, 250, 252), true);
        card.setLayout(new MigLayout("fillx, wrap 1, insets 14", "[grow]", "[]8[]8[]"));

        ButtonGroup group = new ButtonGroup();
        group.add(cashRb);
        group.add(momoRb);
        group.add(cardRb);

        stylePaymentRb(cashRb);
        stylePaymentRb(momoRb);
        stylePaymentRb(cardRb);

        card.add(cashRb, "growx");
        card.add(momoRb, "growx");
        card.add(cardRb, "growx");

        return card;
    }

    private void stylePaymentRb(AbstractButton rb) {
        rb.setFont(Theme.FONT_BODY_LG);
        rb.setOpaque(false);
        rb.setForeground(Theme.TEXT_PRIMARY);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JLabel sectionTitle(String text) {
        JLabel lb = new JLabel(text);
        lb.setFont(Theme.FONT_H2);
        lb.setForeground(Theme.NAVY);
        return lb;
    }

    private PaymentMethod getSelectedMethod() {
        if (momoRb.isSelected()) return PaymentMethod.MOMO;
        if (cardRb.isSelected()) return PaymentMethod.CARD;
        return PaymentMethod.CASH;
    }

    private void onConfirmPayment() {
        if (summary == null) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu đặt lịch để thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PaymentMethod method = getSelectedMethod();
        JOptionPane.showMessageDialog(this,
            "Thanh toán thành công!\n\n" +
                "Phương thức: " + method + "\n" +
                "Tổng tiền: " + formatMoney(summary.total()),
            "Xác nhận thanh toán",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}

