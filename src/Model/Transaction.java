package Model;

public class Transaction {
    private String billNumber;
    private String itemCode;
    private double internalPrice;
    private double discount;
    private double salePrice;
    private int quantity;
    private int importedChecksum;
    private int currentChecksum;
    private boolean valid;
    private double profit;
    private double rawTotal;

    // Original values from CSV for comparison
    private String originalInternalPrice;
    private String originalDiscount;
    private String originalSalePrice;
    private String originalQuantity;
    private String originalRawTotal;

    public Transaction(String billNumber, String itemCode,
                       double internalPrice, double discount, double salePrice,
                       int quantity, int checksum,
                       String originalInternalPrice, String originalDiscount,
                       String originalSalePrice, String originalQuantity) {
        this.billNumber = billNumber;
        this.itemCode = itemCode;
        this.internalPrice = internalPrice;
        this.discount = discount;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.importedChecksum = checksum;
        this.currentChecksum = checksum; // Initially same as imported
        this.valid = true; // Default to valid

        // Store original string values
        this.originalInternalPrice = originalInternalPrice;
        this.originalDiscount = originalDiscount;
        this.originalSalePrice = originalSalePrice;
        this.originalQuantity = originalQuantity;
    }

    // Getters and setters
    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public double getInternalPrice() {
        return internalPrice;
    }

    public void setInternalPrice(double internalPrice) {
        this.internalPrice = internalPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getImportedChecksum() {
        return importedChecksum;
    }

    public void setImportedChecksum(int importedChecksum) {
        this.importedChecksum = importedChecksum;
    }

    public int getCurrentChecksum() {
        return currentChecksum;
    }

    public void setCurrentChecksum(int currentChecksum) {
        this.currentChecksum = currentChecksum;
    }

    // For backward compatibility with existing code
    public int getChecksum() {
        return currentChecksum;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getRawTotal() {
        return rawTotal;
    }

    public void setRawTotal(double rawTotal) {
        this.rawTotal = rawTotal;
    }

    public String getOriginalInternalPrice() {
        return originalInternalPrice;
    }

    public String getOriginalDiscount() {
        return originalDiscount;
    }

    public String getOriginalSalePrice() {
        return originalSalePrice;
    }

    public String getOriginalQuantity() {
        return originalQuantity;
    }

    public String getOriginalRawTotal() {
        return originalRawTotal;
    }

    public void setOriginalRawTotal(String originalRawTotal) {
        this.originalRawTotal = originalRawTotal;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "billNumber='" + billNumber + '\'' +
                ", itemCode='" + itemCode + '\'' +
                ", internalPrice=" + internalPrice +
                ", discount=" + discount +
                ", salePrice=" + salePrice +
                ", quantity=" + quantity +
                ", valid=" + valid +
                ", profit=" + profit +
                '}';
    }
}