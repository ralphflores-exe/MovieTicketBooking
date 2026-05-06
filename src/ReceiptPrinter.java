import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptPrinter {

    public static void generateReceipt(String movieTitle, String cinema, String time, String seats, int regular, int discounted, int totalCost, int ticketPrice) {
        String folderName = "Receipts";
        File directory = new File(folderName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filename = folderName + File.separator + "Receipt_" + System.currentTimeMillis() + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=========================================");
            writer.println("        CINEMA TICKET RECEIPT");
            writer.println("=========================================");
            writer.println("Date: " + new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(new Date()));
            writer.println("Movie: " + movieTitle);
            writer.println("Location: " + cinema);
            writer.println("Showtime: " + time);
            writer.println("Seats: " + seats);
            writer.println("-----------------------------------------");
            writer.println(String.format("Regular Tickets   (x%d): PHP %d", regular, regular * ticketPrice));
            writer.println(String.format("Discounted Tickets(x%d): PHP %d", discounted, discounted * (int)(ticketPrice * 0.8)));
            writer.println("-----------------------------------------");
            writer.println("TOTAL PAID: PHP " + totalCost);
            writer.println("=========================================");
            writer.println("Thank you for your purchase!");
        } catch (IOException e) {
            System.err.println("Failed to save receipt: " + e.getMessage());
        }
    }
}