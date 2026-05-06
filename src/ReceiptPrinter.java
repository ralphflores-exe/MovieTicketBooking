import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptPrinter {

    /**
     * Generates a text-based receipt saved in a 'Receipts' folder.
     * Updated to handle snack quantities and detailed ticket breakdowns.
     */
    public static void generateReceipt(String movieTitle, String cinema, String showtime, String seats,
                                       int regCount, int discCount, int popcornQty, int sodaQty,
                                       int totalAmount, int unitPrice) {

        // Ensure the receipts directory exists
        File directory = new File("Receipts");
        if (!directory.exists()) {
            directory.mkdir();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Receipts/Ticket_" + timestamp + ".txt";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("==========================================\n");
            writer.write("       PREMIUM CINEMA EXPERIENCE         \n");
            writer.write("==========================================\n");
            writer.write("Date: " + new SimpleDateFormat("EEEE, MMM dd, yyyy").format(new Date()) + "\n");
            writer.write("Time: " + new SimpleDateFormat("hh:mm a").format(new Date()) + "\n");
            writer.write("------------------------------------------\n");
            writer.write("MOVIE:    " + movieTitle + "\n");
            writer.write("CINEMA:   " + cinema + "\n");
            writer.write("SHOWTIME: " + showtime + "\n");
            writer.write("SEATS:    " + seats + "\n");
            writer.write("------------------------------------------\n");
            writer.write("ITEMS Breakdown:\n");

            if (regCount > 0) {
                writer.write(String.format("- Regular Ticket (x%d):   PHP %d\n", regCount, regCount * unitPrice));
            }
            if (discCount > 0) {
                int discountedPrice = (int) (unitPrice * 0.8);
                writer.write(String.format("- Discounted Ticket (x%d): PHP %d\n", discCount, discCount * discountedPrice));
            }

            if (popcornQty > 149 || popcornQty > 0) { // Using standard PHP 150 price logic
                writer.write(String.format("- Popcorn (x%d):           PHP %d\n", popcornQty, popcornQty * 150));
            }
            if (sodaQty > 0) {
                writer.write(String.format("- Soda (x%d):              PHP %d\n", sodaQty, sodaQty * 80));
            }

            writer.write("------------------------------------------\n");
            writer.write("TOTAL AMOUNT:           PHP " + totalAmount + "\n");
            writer.write("------------------------------------------\n");
            writer.write("       Thank you for choosing us!        \n");
            writer.write("    Please present this at the entrance.  \n");
            writer.write("==========================================\n");

            System.out.println("Receipt generated successfully: " + fileName);

        } catch (IOException e) {
            System.err.println("Error generating receipt: " + e.getMessage());
        }
    }
}