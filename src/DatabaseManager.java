import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static final String DATA_FILE = "cinema_data.ser";
    
    public static void saveBookings(Map<String, boolean[]> bookedSeatsMap) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(bookedSeatsMap);
            System.out.println("Data successfully saved to local storage.");
        } catch (IOException e) {
            System.err.println("Error saving local data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, boolean[]> loadBookings() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No existing local storage found. Starting fresh.");
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (Map<String, boolean[]>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading local data: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void clearLocalStorage() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}