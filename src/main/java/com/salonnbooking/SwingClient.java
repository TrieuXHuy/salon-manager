package com.salonnbooking;

import com.salonnbooking.ui.fx.SalonFxApplication;

import javafx.application.Application;

public class SwingClient {
	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");
		System.setProperty("sun.jnu.encoding", "UTF-8");
		Application.launch(SalonFxApplication.class, args);
	}
}
