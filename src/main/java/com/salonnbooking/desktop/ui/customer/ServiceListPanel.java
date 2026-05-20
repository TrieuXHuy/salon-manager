package com.salonnbooking.desktop.ui.customer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.google.gson.reflect.TypeToken;
import com.salonnbooking.desktop.api.ApiClient;
import com.salonnbooking.desktop.api.ApiException;
import com.salonnbooking.desktop.model.ServiceModels;
import com.salonnbooking.desktop.util.JsonUtil;

public class ServiceListPanel extends JPanel {

    private static final String BASE_URL = "http://localhost:8080";

    private final ApiClient apiClient = new ApiClient(BASE_URL);

    private final JTextField categoryIdField = new JTextField(8);
    private final JButton refreshButton = new JButton("Refresh");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "ID", "Category", "Name", "Price", "Duration (min)", "Active" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public ServiceListPanel() {
        super(new BorderLayout(0, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Category ID:"));
        top.add(categoryIdField);
        top.add(refreshButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshButton.addActionListener(e -> loadServices());

        loadServices();
    }

    private void setLoading(boolean loading) {
        refreshButton.setEnabled(!loading);
        categoryIdField.setEnabled(!loading);
        setCursor(loading ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    private void loadServices() {
        String categoryText = categoryIdField.getText().trim();
        Long categoryId = null;
        if (!categoryText.isBlank()) {
            try {
                categoryId = Long.valueOf(categoryText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "categoryId must be a number", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        final Long resolvedCategoryId = categoryId;

        setLoading(true);
        SwingWorker<List<ServiceModels.ServiceResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ServiceModels.ServiceResponse> doInBackground() throws Exception {
                // Skip API call in mock mode
                if (com.salonnbooking.desktop.session.AuthSession.getInstance().isMockSession()) {
                    return List.of(
                        new ServiceModels.ServiceResponse(10L, 1L, "Cắt & Tạo kiểu", "Cắt tóc nam (bao gồm gội)", "Cắt tóc cơ bản và gội đầu", new BigDecimal("150000"), 30, true, null),
                        new ServiceModels.ServiceResponse(11L, 1L, "Cắt & Tạo kiểu", "Cắt tóc nữ & Tạo kiểu", "Tạo kiểu layer, uốn lọn nhẹ", new BigDecimal("250000"), 45, true, null),
                        new ServiceModels.ServiceResponse(12L, 2L, "Nhuộm & Uốn", "Uốn tóc nữ kiểu Hàn Quốc", "Uốn sóng nước, uốn cụp", new BigDecimal("800000"), 120, true, null),
                        new ServiceModels.ServiceResponse(13L, 2L, "Nhuộm & Uốn", "Nhuộm màu thời trang", "Tẩy tóc, nhuộm khói", new BigDecimal("650000"), 90, true, null),
                        new ServiceModels.ServiceResponse(14L, 3L, "Chăm sóc tóc", "Gội đầu dưỡng sinh thảo dược", "Massage đầu, gội dầu thảo dược", new BigDecimal("200000"), 45, true, null)
                    );
                }
                String path = resolvedCategoryId == null ? "/api/services"
                        : ("/api/services?categoryId=" + resolvedCategoryId);
                String json = apiClient.getRaw(path);
                Type type = new TypeToken<List<ServiceModels.ServiceResponse>>() {
                }.getType();
                return JsonUtil.fromJson(json, type);
            }

            @Override
            protected void done() {
                setLoading(false);
                try {
                    List<ServiceModels.ServiceResponse> services = get();
                    renderTable(services);
                } catch (Exception ex) {
                    String message = ex.getMessage();
                    if (ex.getCause() instanceof ApiException apiEx) {
                        message = apiEx.getResponseBody();
                    }
                    JOptionPane.showMessageDialog(ServiceListPanel.this, "Failed to load services: " + message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void renderTable(List<ServiceModels.ServiceResponse> services) {
        tableModel.setRowCount(0);
        for (ServiceModels.ServiceResponse s : services) {
            tableModel.addRow(new Object[] {
                    s.id(),
                    s.categoryName() != null ? s.categoryName() : (s.categoryId() != null ? s.categoryId() : ""),
                    s.name(),
                    formatMoney(s.price()),
                    s.durationMinutes(),
                    Boolean.TRUE.equals(s.isActive())
            });
        }
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.toPlainString();
    }
}
