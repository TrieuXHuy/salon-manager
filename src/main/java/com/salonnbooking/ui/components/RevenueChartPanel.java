package com.salonnbooking.ui.components;

import com.salonnbooking.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class RevenueChartPanel extends JPanel {

    private String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
    private double[] values = {1.8, 2.5, 1.5, 3.2, 4.0, 6.5, 5.2}; // Revenue in Million VND
    private double maxValue = 8.0;

    private final int padding = 50;
    private final int labelPadding = 25;

    public void setData(String[] newDays, double[] newValues) {
        if (newDays == null || newValues == null || newDays.length != newValues.length || newDays.length == 0) {
            return;
        }
        this.days = newDays;
        this.values = newValues;
        
        double max = 1.0;
        for (double v : newValues) {
            if (v > max) max = v;
        }
        this.maxValue = Math.ceil(max * 1.2);
        if (this.maxValue < 1.0) this.maxValue = 1.0;
        
        repaint();
    }

    private Point hoveredPoint = null;
    private int hoveredIndex = -1;

    public RevenueChartPanel() {
        setBackground(Theme.BG_CARD);
        setOpaque(false);

        // Add mouse move listener to show interactive tooltips
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                calculateHover(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredPoint = null;
                hoveredIndex = -1;
                repaint();
            }
        };
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
    }

    private void calculateHover(Point mousePt) {
        int width = getWidth();
        int height = getHeight();
        int chartWidth = width - (padding * 2);
        int chartHeight = height - (padding * 2) - labelPadding;

        int closestIdx = -1;
        double closestDist = Double.MAX_VALUE;
        Point closestPt = null;

        for (int i = 0; i < values.length; i++) {
            int x = padding + (i * chartWidth / (values.length - 1));
            int y = (int) ((maxValue - values[i]) * chartHeight / maxValue) + padding;

            double dist = mousePt.distance(x, y);
            // Highlight if mouse is close enough horizontally
            double xDist = Math.abs(mousePt.x - x);
            if (xDist < (chartWidth / (values.length * 2.0)) && dist < closestDist) {
                closestDist = dist;
                closestIdx = i;
                closestPt = new Point(x, y);
            }
        }

        if (closestIdx != hoveredIndex) {
            hoveredIndex = closestIdx;
            hoveredPoint = closestPt;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int chartWidth = width - (padding * 2);
        int chartHeight = height - (padding * 2) - labelPadding;

        // 1. Draw Background Card
        g2.setColor(Theme.BG_CARD);
        g2.fillRoundRect(0, 0, width, height, 16, 16);

        // 2. Draw Horizontal Grid Lines & Y-Axis Labels
        g2.setFont(Theme.FONT_BODY_SM.deriveFont(Font.BOLD));
        int gridCount = 4;
        for (int i = 0; i <= gridCount; i++) {
            int y = padding + (i * chartHeight / gridCount);
            double val = maxValue - (i * maxValue / gridCount);

            // Draw line
            g2.setColor(new Color(241, 245, 249)); // Slate 100
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(padding, y, width - padding, y);

            // Draw label
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(String.format("%.1fM", val), 12, y + 4);
        }

        // 3. Collect Data Points coordinates
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            int x = padding + (i * chartWidth / (values.length - 1));
            int y = (int) ((maxValue - values[i]) * chartHeight / maxValue) + padding;
            points.add(new Point(x, y));
        }

        // 4. Draw Gradient Fill Area below the line
        if (points.size() > 1) {
            GeneralPath path = new GeneralPath();
            path.moveTo(points.get(0).x, points.get(0).y);
            
            // Draw smooth curve segments
            for (int i = 1; i < points.size(); i++) {
                Point pPrev = points.get(i - 1);
                Point pCurr = points.get(i);
                int controlX1 = pPrev.x + (pCurr.x - pPrev.x) / 2;
                int controlY1 = pPrev.y;
                int controlX2 = pPrev.x + (pCurr.x - pPrev.x) / 2;
                int controlY2 = pCurr.y;
                path.curveTo(controlX1, controlY1, controlX2, controlY2, pCurr.x, pCurr.y);
            }

            // Close the path to form a polygon down to chart bottom
            GeneralPath fillPath = (GeneralPath) path.clone();
            fillPath.lineTo(points.get(points.size() - 1).x, padding + chartHeight);
            fillPath.lineTo(points.get(0).x, padding + chartHeight);
            fillPath.closePath();

            // Paint gradient
            GradientPaint gp = new GradientPaint(
                0, padding, new Color(16, 185, 129, 80), // Emerald with opacity
                0, padding + chartHeight, new Color(16, 185, 129, 0) // Fading to zero
            );
            g2.setPaint(gp);
            g2.fill(fillPath);

            // 5. Draw the thick curve Line on top
            g2.setColor(Theme.EMERALD);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(path);
        }

        // 6. Draw X-Axis Labels & Points
        g2.setFont(Theme.FONT_BODY_SM);
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);

            // Draw small dot on line
            g2.setColor(Theme.BG_CARD);
            g2.fillOval(p.x - 5, p.y - 5, 10, 10);
            g2.setColor(Theme.EMERALD);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(p.x - 5, p.y - 5, 10, 10);

            // Draw bottom label (Day of week)
            g2.setColor(Theme.TEXT_MUTED);
            int labelWidth = g2.getFontMetrics().stringWidth(days[i]);
            g2.drawString(days[i], p.x - (labelWidth / 2), height - padding + 15);
        }

        // 7. Draw Interactive Hover Details
        if (hoveredIndex != -1 && hoveredPoint != null) {
            // Draw vertical dashed line guide
            g2.setColor(Theme.EMERALD);
            float[] dash = {4f, 4f};
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash, 0f));
            g2.drawLine(hoveredPoint.x, padding, hoveredPoint.x, padding + chartHeight);

            // Draw glowing hover dot
            g2.setColor(new Color(16, 185, 129, 100));
            g2.fillOval(hoveredPoint.x - 8, hoveredPoint.y - 8, 16, 16);
            g2.setColor(Theme.EMERALD);
            g2.fillOval(hoveredPoint.x - 4, hoveredPoint.y - 4, 8, 8);

            // Draw Tooltip Box
            String text = String.format("%.1fM VNĐ", values[hoveredIndex]);
            g2.setFont(Theme.FONT_BODY_SM.deriveFont(Font.BOLD));
            int boxWidth = g2.getFontMetrics().stringWidth(text) + 20;
            int boxHeight = 28;
            int boxX = hoveredPoint.x - (boxWidth / 2);
            int boxY = hoveredPoint.y - boxHeight - 12;

            // Constrain tooltip to panel boundaries
            if (boxX < 10) boxX = 10;
            if (boxX + boxWidth > width - 10) boxX = width - boxWidth - 10;

            // Draw shadow box
            g2.setColor(new Color(15, 23, 42, 30));
            g2.fillRoundRect(boxX + 2, boxY + 2, boxWidth, boxHeight, 8, 8);
            
            // Draw box fill
            g2.setColor(Theme.NAVY);
            g2.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);

            // Draw box text
            g2.setColor(Theme.TEXT_WHITE);
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, boxX + (boxWidth - textWidth) / 2, boxY + 18);
        }

        g2.dispose();
    }
}
