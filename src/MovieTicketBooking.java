import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MovieTicketBooking extends JFrame {

    // --- Custom Class to hold Movie Data ---
    static class Movie {
        String title;
        String cinema;
        String[] showtimes;

        public Movie(String title, String cinema, String[] showtimes) {
            this.title = title;
            this.cinema = cinema;
            this.showtimes = showtimes;
        }

        @Override
        public String toString() {
            return title; // This is what shows up in the ComboBox
        }
    }

    // --- Database Simulation ---
    // Here we assign specific cinemas and times (factoring in different movie lengths)
    private final Movie[] moviesList = {
            new Movie("Demon Slayer: Kimetsu No Yaiba The Movie: Infinity Castle", "Cinema 1", new String[]{"10:00 AM", "1:30 PM", "5:00 PM", "8:30 PM"}),
            new Movie("Project Hail Mary", "Cinema 2", new String[]{"10:30 AM", "2:45 PM", "7:00 PM"}), // Longer movie, fewer showtimes
            new Movie("Interstellar", "Cinema 3", new String[]{"11:00 AM", "3:30 PM", "8:00 PM"}),
            new Movie("Dune", "Cinema 4", new String[]{"12:00 PM", "3:45 PM", "7:30 PM", "11:15 PM"})
    };

    private JComboBox<Movie> movieComboBox;
    private JComboBox<String> timeComboBox;
    private JLabel cinemaIndicatorLabel;

    private JPanel seatPanel;
    private JPanel screenPanel;
    private JButton[] seatButtons;
    private JButton bookButton;
    private JLabel posterLabel;
    private JLabel dateTimeLabel;

    private final Map<String, boolean[]> bookedSeatsMap = new HashMap<>();
    private final ArrayList<Integer> currentSelection = new ArrayList<>();

    private final int TOTAL_SEATS = 112; // 8x14 Grid
    private final int TICKET_PRICE = 300;

    public MovieTicketBooking() {
        // 1. Initialize our mock "database" with unique combinations
        for (Movie movie : moviesList) {
            for (String time : movie.showtimes) {
                bookedSeatsMap.put(movie.title + "-" + movie.cinema + "-" + time, new boolean[TOTAL_SEATS]);
            }
        }

        // 2. Set up the Main Frame
        setTitle("Cinema Ticket Booking System - Multi-Cinema Edition");
        setSize(1150, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);

        // 3. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(20, 20));
        headerPanel.setBackground(Color.BLACK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Top Controls Panel
        JPanel topControlsPanel = new JPanel(new BorderLayout());
        topControlsPanel.setBackground(Color.BLACK);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        selectionPanel.setBackground(Color.BLACK);

        // Movie Selection
        JLabel movieLabel = new JLabel("Movie: ");
        movieLabel.setForeground(Color.WHITE);
        selectionPanel.add(movieLabel);

        movieComboBox = new JComboBox<>(moviesList);
        movieComboBox.addActionListener(e -> updateTimeDropdown()); // Changes times based on movie
        selectionPanel.add(movieComboBox);

        // Cinema Indicator (Updates dynamically)
        cinemaIndicatorLabel = new JLabel("Location: Cinema 1");
        cinemaIndicatorLabel.setForeground(Color.GREEN);
        cinemaIndicatorLabel.setFont(new Font("Arial", Font.BOLD, 14));
        selectionPanel.add(cinemaIndicatorLabel);

        // Time Selection
        JLabel timeLabel = new JLabel("Showtime: ");
        timeLabel.setForeground(Color.GREEN);
        selectionPanel.add(timeLabel);

        timeComboBox = new JComboBox<>();
        timeComboBox.addActionListener(e -> updateMovieSelection()); // Changes seats based on time
        selectionPanel.add(timeComboBox);

        topControlsPanel.add(selectionPanel, BorderLayout.WEST);

        // Live Date & Time Clock
        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clockPanel.setBackground(Color.BLACK);
        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateTimeLabel.setForeground(Color.GREEN);
        startLiveClock();
        clockPanel.add(dateTimeLabel);

        topControlsPanel.add(clockPanel, BorderLayout.EAST);
        headerPanel.add(topControlsPanel, BorderLayout.NORTH);

        // Poster Placeholder
        posterLabel = new JLabel("", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(150, 200));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(Color.DARK_GRAY);
        posterLabel.setForeground(Color.WHITE);
        posterLabel.setFont(new Font("Arial", Font.BOLD, 16));
        posterLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        headerPanel.add(posterLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // 4. Center Panel (Screen Graphic + Seat Grid)
        JPanel cinemaLayout = new JPanel(new BorderLayout());
        cinemaLayout.setBackground(Color.BLACK);

        // Screen Graphic
        screenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillArc(100, 10, getWidth() - 200, 60, 0, 180);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));

                // Dynamically fetch the current cinema name to draw on the screen
                Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
                String screenText = (selectedMovie != null ? selectedMovie.cinema.toUpperCase() : "CINEMA") + "   S C R E E N";

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(screenText);
                g2d.drawString(screenText, (getWidth() - textWidth) / 2, 30);
            }
        };
        screenPanel.setPreferredSize(new Dimension(1000, 60));
        screenPanel.setBackground(Color.BLACK);
        cinemaLayout.add(screenPanel, BorderLayout.NORTH);

        // Seat Layout (4x8 Left, 6x8 Center, 4x8 Right)
        seatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 10));
        seatPanel.setBackground(Color.BLACK);
        seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftBlock = new JPanel(new GridLayout(8, 4, 8, 8));
        JPanel centerBlock = new JPanel(new GridLayout(8, 6, 8, 8));
        JPanel rightBlock = new JPanel(new GridLayout(8, 4, 8, 8));

        leftBlock.setBackground(Color.BLACK);
        centerBlock.setBackground(Color.BLACK);
        rightBlock.setBackground(Color.BLACK);

        seatButtons = new JButton[TOTAL_SEATS];

        for (int r = 0; r < 8; r++) {
            char rowLetter = (char) ('A' + r);
            for (int c = 0; c < 14; c++) {
                int index = r * 14 + c;
                String seatLabel = rowLetter + String.valueOf(c + 1);

                seatButtons[index] = new JButton(seatLabel);
                seatButtons[index].setFocusPainted(false);
                seatButtons[index].setFont(new Font("Arial", Font.BOLD, 13));
                seatButtons[index].setPreferredSize(new Dimension(60, 45));
                seatButtons[index].setMargin(new Insets(0, 0, 0, 0));
                seatButtons[index].setForeground(Color.BLACK);

                final int finalIndex = index;
                seatButtons[index].addActionListener(e -> handleSeatClick(finalIndex));

                if (c < 4) {
                    leftBlock.add(seatButtons[index]);
                } else if (c < 10) {
                    centerBlock.add(seatButtons[index]);
                } else {
                    rightBlock.add(seatButtons[index]);
                }
            }
        }

        seatPanel.add(leftBlock);
        seatPanel.add(centerBlock);
        seatPanel.add(rightBlock);

        cinemaLayout.add(seatPanel, BorderLayout.CENTER);
        add(cinemaLayout, BorderLayout.CENTER);

        // 5. Bottom Panel: Legend and Booking
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);

        JPanel legendPanel = new JPanel();
        legendPanel.setBackground(Color.BLACK);
        legendPanel.add(createLegendLabel("Available", Color.GREEN));
        legendPanel.add(createLegendLabel("Selected", Color.YELLOW));
        legendPanel.add(createLegendLabel("Booked", Color.RED));

        bookButton = new JButton("Checkout & Pay");
        bookButton.setFont(new Font("Arial", Font.BOLD, 18));
        bookButton.setBackground(Color.WHITE);
        bookButton.setForeground(Color.BLACK);
        bookButton.setPreferredSize(new Dimension(200, 50));
        bookButton.addActionListener(e -> processPayment());

        bottomPanel.add(legendPanel, BorderLayout.NORTH);
        bottomPanel.add(bookButton, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 30, 20));
        add(bottomPanel, BorderLayout.SOUTH);

        // 6. Initial Load
        updateTimeDropdown(); // This will cascade down to updateMovieSelection()
    }

    private void startLiveClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy  |  hh:mm:ss a");
            dateTimeLabel.setText(formatter.format(new Date()));
        });
        timer.start();
    }

    /**
     * Called when the Movie Dropdown changes. It populates the Time Dropdown
     * with times specific to that movie's length and cinema schedule.
     */
    private void updateTimeDropdown() {
        Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
        if (selectedMovie == null) return;

        // Update the Cinema Label text
        cinemaIndicatorLabel.setText("Location: " + selectedMovie.cinema);

        // Temporarily remove items to replace them
        timeComboBox.removeAllItems();
        for (String time : selectedMovie.showtimes) {
            timeComboBox.addItem(time);
        }

        // Force the screen to redraw so it says "CINEMA 1 SCREEN", "CINEMA 2 SCREEN", etc.
        if (screenPanel != null) {
            screenPanel.repaint();
        }

        // Trigger a seat map refresh for the new first time slot
        updateMovieSelection();
    }

    private String getCurrentSessionKey() {
        Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
        String selectedTime = (String) timeComboBox.getSelectedItem();

        // Prevent errors during dropdown transitions
        if (selectedMovie == null || selectedTime == null) return null;

        return selectedMovie.title + "-" + selectedMovie.cinema + "-" + selectedTime;
    }

    private void updateMovieSelection() {
        String sessionKey = getCurrentSessionKey();
        if (sessionKey == null) return; // Prevent NullPointer during dropdown population

        Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
        String selectedTime = (String) timeComboBox.getSelectedItem();

        // Use HTML to put line breaks in the poster placeholder text
        posterLabel.setText("<html><center>" + selectedMovie.title + "<br><br>" + selectedMovie.cinema + "<br>" + selectedTime + "</center></html>");

        boolean[] bookedSeats = bookedSeatsMap.get(sessionKey);
        currentSelection.clear();

        for (int i = 0; i < TOTAL_SEATS; i++) {
            if (bookedSeats[i]) {
                seatButtons[i].setBackground(Color.RED);
            } else {
                seatButtons[i].setBackground(Color.GREEN);
            }
        }
    }

    private void handleSeatClick(int index) {
        String sessionKey = getCurrentSessionKey();
        if (sessionKey == null) return;

        boolean[] bookedSeats = bookedSeatsMap.get(sessionKey);
        int r = index / 14;
        int c = index % 14;
        String seatName = (char)('A' + r) + String.valueOf(c + 1);

        if (bookedSeats[index]) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    seatName + " is booked. Cancel reservation?",
                    "Cancel Reservation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                bookedSeats[index] = false;
                seatButtons[index].setBackground(Color.GREEN);
            }
            return;
        }

        if (currentSelection.contains(index)) {
            currentSelection.remove(Integer.valueOf(index));
            seatButtons[index].setBackground(Color.GREEN);
        } else {
            currentSelection.add(index);
            seatButtons[index].setBackground(Color.YELLOW);
        }
    }

    private void processPayment() {
        if (currentSelection.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one available seat to proceed.", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalCost = currentSelection.size() * TICKET_PRICE;

        StringBuilder seatsToBook = new StringBuilder();
        for (int i = 0; i < currentSelection.size(); i++) {
            int index = currentSelection.get(i);
            int r = index / 14;
            int c = index % 14;
            seatsToBook.append((char)('A' + r)).append(c + 1);
            if (i < currentSelection.size() - 1) seatsToBook.append(", ");
        }

        Movie movie = (Movie) movieComboBox.getSelectedItem();
        String time = (String) timeComboBox.getSelectedItem();

        String paymentMessage = String.format(
                "Movie: %s\nCinema: %s\nTime: %s\nSeats: %s\n\nTotal Tickets: %d\nPrice per Ticket: PHP %d\n----------------------------\nTOTAL AMOUNT DUE: PHP %d\n\nProceed with payment?",
                movie.title, movie.cinema, time, seatsToBook.toString(), currentSelection.size(), TICKET_PRICE, totalCost
        );

        int confirm = JOptionPane.showConfirmDialog(this, paymentMessage, "Payment Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String sessionKey = getCurrentSessionKey();
            boolean[] bookedSeats = bookedSeatsMap.get(sessionKey);
            for (int index : currentSelection) {
                bookedSeats[index] = true;
            }
            JOptionPane.showMessageDialog(this, "Payment successful! Tickets booked.", "Success", JOptionPane.INFORMATION_MESSAGE);
            updateMovieSelection();
        }
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return label;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MovieTicketBooking().setVisible(true);
        });
    }
}