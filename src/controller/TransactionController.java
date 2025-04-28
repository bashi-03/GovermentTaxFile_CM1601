package controller;

import manager.TransactionManager;
import Model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.Optional;

public class TransactionController {

    @FXML
    private TextField filePathField;

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, String> billNumberColumn;

    @FXML
    private TableColumn<Transaction, String> itemCodeColumn;

    @FXML
    private TableColumn<Transaction, Double> internalPriceColumn;

    @FXML
    private TableColumn<Transaction, Double> discountColumn;

    @FXML
    private TableColumn<Transaction, Double> salePriceColumn;

    @FXML
    private TableColumn<Transaction, Integer> quantityColumn;

    @FXML
    private TableColumn<Transaction, Integer> checksumColumn;

    @FXML
    private TableColumn<Transaction, Double> profitColumn;

    @FXML
    private Label totalRecordsLabel;

    @FXML
    private Label validRecordsLabel;

    @FXML
    private Label invalidRecordsLabel;

    @FXML
    private TextField taxRateField;

    @FXML
    private Label finalTaxLabel;

    private TransactionManager transactionManager = new TransactionManager();

    @FXML
    private void initialize() {
        billNumberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBillNumber()));
        itemCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemCode()));
        internalPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getInternalPrice()).asObject());
        discountColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDiscount()).asObject());
        salePriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSalePrice()).asObject());
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        checksumColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCurrentChecksum()).asObject());
        profitColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProfit()).asObject());

        transactionTable.setRowFactory(tv -> new TableRow<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isValid()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    @FXML
    private void handleBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Transaction CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(transactionTable.getScene().getWindow());
        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleImport() {
        String path = filePathField.getText();
        if (path.isEmpty()) {
            showAlert("Error", "Please enter a file path or use Browse to select a file.");
            return;
        }

        if (transactionManager.loadTransactionsFromCSV(path)) {
            refreshTable();
        } else {
            showAlert("Error", "Could not load the file! Please check the file format and try again.");
        }
    }

    private void refreshTable() {
        ObservableList<Transaction> list = FXCollections.observableArrayList(transactionManager.getTransactions());
        transactionTable.setItems(list);
        updateSummary();
    }

    private void updateSummary() {
        int total = transactionManager.getTransactions().size();
        int valid = (int) transactionManager.getTransactions().stream().filter(Transaction::isValid).count();
        int invalid = total - valid;

        totalRecordsLabel.setText("Total: " + total);
        validRecordsLabel.setText("Valid: " + valid);
        invalidRecordsLabel.setText("Invalid: " + invalid);
    }

    @FXML
    private void handleValidate() {
        transactionManager.validateTransactions();
        transactionTable.refresh(); // Only refresh the table, not reload all data
        updateSummary();
    }

    @FXML
    private void handleCalculateProfit() {
        transactionManager.calculateProfits();
        transactionTable.refresh(); //  Only refresh the table, NOT reload
    }

    @FXML
    private void handleDeleteZeroProfit() {
        transactionManager.deleteZeroProfitTransactions();
        refreshTable();
    }

    @FXML
    private void handleCalculateFinalTax() {
        try {
            double rate = Double.parseDouble(taxRateField.getText());
            double tax = transactionManager.calculateFinalTax(rate);
            finalTaxLabel.setText(String.format("Final Tax: %.2f", tax));
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid tax rate.");
        }
    }

    @FXML
    private void handleEditTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert("Error", "Please select a transaction to edit.");
            return;
        }

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");
        dialog.setHeaderText("Edit transaction details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField itemCodeField = new TextField(selectedTransaction.getItemCode());
        TextField internalPriceField = new TextField(String.valueOf(selectedTransaction.getInternalPrice()));
        TextField discountField = new TextField(String.valueOf(selectedTransaction.getDiscount()));
        TextField salePriceField = new TextField(String.valueOf(selectedTransaction.getSalePrice()));
        TextField quantityField = new TextField(String.valueOf(selectedTransaction.getQuantity()));

        grid.add(new Label("Item Code:"), 0, 0);
        grid.add(itemCodeField, 1, 0);
        grid.add(new Label("Internal Price:"), 0, 1);
        grid.add(internalPriceField, 1, 1);
        grid.add(new Label("Discount Rate (%):"), 0, 2);
        grid.add(discountField, 1, 2);
        grid.add(new Label("Sale Price:"), 0, 3);
        grid.add(salePriceField, 1, 3);
        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(quantityField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String newItemCode = itemCodeField.getText();
                    double newInternalPrice = Double.parseDouble(internalPriceField.getText());
                    double newDiscount = Double.parseDouble(discountField.getText());
                    double newSalePrice = Double.parseDouble(salePriceField.getText());
                    int newQuantity = Integer.parseInt(quantityField.getText());

                    // Use the new updateTransaction method to update values and recalculate checksum
                    transactionManager.updateTransaction(
                            selectedTransaction,
                            newItemCode,
                            newInternalPrice,
                            newDiscount,
                            newSalePrice,
                            newQuantity
                    );

                    return selectedTransaction;
                } catch (NumberFormatException e) {
                    showAlert("Error", "Please enter valid numeric values.");
                    return null;
                }
            }
            return null;
        });

        Optional<Transaction> result = dialog.showAndWait();
        if (result.isPresent()) {
            // No need to validate or calculate profit here as updateTransaction already does this
            transactionTable.refresh();
            updateSummary();
        }
    }

    @FXML
    private void handleDeleteTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showAlert("Error", "Please select a transaction to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Transaction");
        confirmAlert.setContentText("Are you sure you want to delete this transaction?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            transactionManager.deleteTransaction(selectedTransaction);
            refreshTable();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}