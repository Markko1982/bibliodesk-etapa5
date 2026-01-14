package com.bibliodesk.app;

import com.bibliodesk.ui.LoginFrameForm;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Database db = new Database();
            LoginFrameForm login = new LoginFrameForm(db);
            login.setVisible(true);
        });
    }
}
