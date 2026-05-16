package com.salonnbooking.desktop.ui.auth;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class AuthFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel container = new JPanel(cardLayout);

    public AuthFrame() {
        super("Salon Booking Manager - Auth");

        LoginPanel loginPanel = new LoginPanel(this::showRegister);
        RegisterPanel registerPanel = new RegisterPanel(this::showLogin);

        container.add(loginPanel, "login");
        container.add(registerPanel, "register");

        setContentPane(container);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(null);

        showLogin();
    }

    public void showLogin() {
        cardLayout.show(container, "login");
    }

    public void showRegister() {
        cardLayout.show(container, "register");
    }
}
