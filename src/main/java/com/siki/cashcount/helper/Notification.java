package com.siki.cashcount.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Notification {
    private Notification() {}

    public static Optional<ButtonType> showYesNoCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mentés");
        alert.setHeaderText("Mentés szükséges");
        alert.setContentText("Az adatok megváltoztak! Szeretnéd menteni a változásokat?");
        ButtonType buttonTypeYes = new ButtonType("Igen", ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Nem", ButtonData.NO);
        ButtonType buttonTypeCancel = new ButtonType("Mégsem", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);
        return alert.showAndWait();
    }
}
