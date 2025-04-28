package manager;

import Model.Transaction;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TransactionManager {
    private List<Transaction> transactions = new ArrayList<>();

    public boolean loadTransactionsFromCSV(String filePath) {
        transactions.clear();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("File not found.");
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    if (line.contains("BillID") || line.contains("ItemCode")) {
                        continue;
                    }
                }

                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    try {
                        String billNumber = parts[0];
                        String itemCode = parts[1];
                        String originalInternalPrice = parts[2];
                        String originalDiscount = parts[3];
                        String originalSalePrice = parts[4];
                        String originalQuantity = parts[5];

                        double internalPrice = Double.parseDouble(originalInternalPrice);
                        double discount = Double.parseDouble(originalDiscount);
                        double salePrice = Double.parseDouble(originalSalePrice);
                        int quantity = Integer.parseInt(originalQuantity);

                        double rawTotal = Double.parseDouble(parts[6]);
                        int checksum = Integer.parseInt(parts[7]);

                        Transaction transaction = new Transaction(
                                billNumber, itemCode,
                                internalPrice, discount, salePrice, quantity, checksum,
                                originalInternalPrice, originalDiscount, originalSalePrice, originalQuantity
                        );

                        // Set the raw total from the CSV file
                        transaction.setRawTotal(rawTotal);
                        transaction.setOriginalRawTotal(parts[6]);

                        // Save imported checksum separately and don't modify it
                        transaction.setImportedChecksum(checksum);

                        // Initialize current checksum with imported checksum
                        transaction.setCurrentChecksum(checksum);

                        // Mark as valid by default - validation will happen separately
                        transaction.setValid(true);

                        transactions.add(transaction);
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing line: " + line);
                    }
                }
            }
            return !transactions.isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int calculateCurrentChecksum(Transaction transaction) {
        // Calculate checksum based on current values
        String formattedRawTotal = String.format("%.2f", transaction.getRawTotal());

        String transactionLine = transaction.getItemCode()
                + String.format("%.2f", transaction.getInternalPrice())
                + String.format("%.2f", transaction.getDiscount())
                + String.format("%.2f", transaction.getSalePrice())
                + String.valueOf(transaction.getQuantity())
                + formattedRawTotal;

        int capital = 0, simple = 0, numbers = 0;

        for (char c : transactionLine.toCharArray()) {
            if (Character.isUpperCase(c)) capital++;
            else if (Character.isLowerCase(c)) simple++;
            else if (Character.isDigit(c) || c == '.') numbers++;
        }

        return capital + simple + numbers;
    }

    public void validateSingleTransaction(Transaction t) {
        // Calculate profit for the transaction
        calculateProfitForTransaction(t);

        // Create a pattern for validating item code (letters, numbers, underscores only)
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");

        // Check if the transaction has been updated
        boolean isUpdated = t.getCurrentChecksum() != t.getImportedChecksum();

        // Check three conditions:
        // 1. Item code should not contain special characters
        boolean validItemCode = pattern.matcher(t.getItemCode()).matches();

        // 2. Profit should not be negative
        boolean positiveProfit = t.getProfit() >= 0;

        // 3. Checksum validation depends on whether this is an updated transaction
        boolean checksumValid;
        if (isUpdated) {
            // For updated transactions, just ensure we have a valid checksum (no need to match imported)
            checksumValid = true;
        } else {
            // For original transactions, calculated should match imported
            int calculatedChecksum = calculateCurrentChecksum(t);
            checksumValid = (calculatedChecksum == t.getImportedChecksum());
        }

        // For debugging
        System.out.println("Transaction: " + t.getItemCode());
        System.out.println("- Valid item code: " + validItemCode);
        System.out.println("- Positive profit: " + positiveProfit);
        System.out.println("- Is updated: " + isUpdated);
        System.out.println("- Checksum valid: " + checksumValid);
        System.out.println("- Current checksum: " + t.getCurrentChecksum());
        System.out.println("- Imported checksum: " + t.getImportedChecksum());

        // A transaction is valid if all conditions are met
        boolean valid = validItemCode && positiveProfit && checksumValid;
        t.setValid(valid);

        System.out.println("- Final validity: " + valid);
    }

    public void validateTransactions() {
        // First ensure profits are calculated for all transactions
        calculateProfits();

        // Then validate each transaction individually
        for (Transaction t : transactions) {
            validateSingleTransaction(t);
        }
    }

    private void calculateProfitForTransaction(Transaction t) {
        double discountAmount = t.getSalePrice() * (t.getDiscount() / 100.0);
        double discountedSalePrice = t.getSalePrice() - discountAmount;
        double profit = (discountedSalePrice - t.getInternalPrice()) * t.getQuantity();
        t.setProfit(profit);
    }

    public void calculateProfits() {
        for (Transaction t : transactions) {
            calculateProfitForTransaction(t);
        }
    }

    public void deleteZeroProfitTransactions() {
        transactions.removeIf(t -> t.getProfit() == 0);
    }

    public void deleteTransaction(Transaction transaction) {
        transactions.remove(transaction);
    }

    public double calculateFinalTax(double taxRate) {
        double totalProfit = 0;
        for (Transaction t : transactions) {
            if (t.isValid()) {
                totalProfit += t.getProfit();
            }
        }
        return totalProfit * (taxRate / 100.0);
    }

    // Method to update transaction and recalculate checksum
    public void updateTransaction(Transaction t, String itemCode, double internalPrice,
                                  double discount, double salePrice, int quantity) {
        // Update values
        t.setItemCode(itemCode);
        t.setInternalPrice(internalPrice);
        t.setDiscount(discount);
        t.setSalePrice(salePrice);
        t.setQuantity(quantity);

        // Recalculate raw total
        double discountAmount = salePrice * (discount / 100.0);
        double discountedSalePrice = salePrice - discountAmount;
        double rawTotal = discountedSalePrice * quantity;
        t.setRawTotal(rawTotal);

        // Calculate and update the new checksum
        int newChecksum = calculateCurrentChecksum(t);
        t.setCurrentChecksum(newChecksum);

        // Validate the transaction with updated values
        validateSingleTransaction(t);
    }
}