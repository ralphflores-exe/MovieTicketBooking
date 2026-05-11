import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MovieTicketBooking extends JFrame {

    private final Movie[] moviesList = {
            new Movie("Demon Slayer: Kimetsu No Yaiba The Movie: Infinity Castle", "Cinema 1", new String[]{"10:00 AM", "1:30 PM", "5:00 PM", "8:30 PM"}),
            new Movie("Project Hail Mary", "Cinema 2", new String[]{"10:30 AM", "2:45 PM", "7:00 PM"}),
            new Movie("Interstellar", "Cinema 3", new String[]{"11:00 AM", "3:30 PM", "8:00 PM"}),
            new Movie("Dune", "Cinema 4", new String[]{"12:00 PM", "3:45 PM", "7:30 PM", "11:15 PM"})
    };

    private JComboBox<Movie> movieComboBox;
    private JComboBox<String> timeComboBox;
    private JLabel cinemaIndicatorLabel;
    private JLabel seatsRemainingLabel;

    private JPanel seatPanel;
    private JPanel screenPanel;
    private JButton[] seatButtons;
    private JButton bookButton;
    private JLabel posterLabel;
    private JLabel dateTimeLabel;

    private final Map<String, boolean[]> bookedSeatsMap = new HashMap<>();
    private final ArrayList<Integer> currentSelection = new ArrayList<>();

    private final int TOTAL_SEATS = 112;
    private final int TICKET_PRICE = 300;

    private final int POPCORN_PRICE = 150;
    private final int SODA_PRICE = 80;

    public MovieTicketBooking() {
        Map<String, boolean[]> savedData = DatabaseManager.loadBookings();
        if (savedData != null) {
            bookedSeatsMap.putAll(savedData);
        }

        for (Movie movie : moviesList) {
            for (String time : movie.showtimes) {
                String key = movie.title + "-" + movie.cinema + "-" + time;
                bookedSeatsMap.putIfAbsent(key, new boolean[TOTAL_SEATS]);
            }
        }

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setTitle("Cinema Ticket Booking System - Pro Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.BLACK);

        JPanel headerPanel = new JPanel(new BorderLayout(20, 20));
        headerPanel.setBackground(Color.BLACK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        JPanel topControlsPanel = new JPanel(new BorderLayout());
        topControlsPanel.setBackground(Color.BLACK);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        selectionPanel.setBackground(Color.BLACK);

        JLabel movieLabel = new JLabel("Movie: ");
        movieLabel.setForeground(Color.WHITE);
        movieLabel.setFont(new Font("Arial", Font.BOLD, 16));
        selectionPanel.add(movieLabel);

        movieComboBox = new JComboBox<>(moviesList);
        movieComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        movieComboBox.addActionListener(e -> updateTimeDropdown());
        selectionPanel.add(movieComboBox);

        cinemaIndicatorLabel = new JLabel("Location: Cinema 1");
        cinemaIndicatorLabel.setForeground(Color.WHITE);
        cinemaIndicatorLabel.setFont(new Font("Arial", Font.BOLD, 16));
        selectionPanel.add(cinemaIndicatorLabel);

        JLabel timeLabel = new JLabel("Showtime: ");
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        selectionPanel.add(timeLabel);

        timeComboBox = new JComboBox<>();
        timeComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        timeComboBox.addActionListener(e -> updateMovieSelection());
        selectionPanel.add(timeComboBox);

        seatsRemainingLabel = new JLabel("Seats Left: 112");
        seatsRemainingLabel.setForeground(Color.ORANGE);
        seatsRemainingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        selectionPanel.add(seatsRemainingLabel);

        topControlsPanel.add(selectionPanel, BorderLayout.WEST);

        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        rightHeaderPanel.setBackground(Color.BLACK);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dateTimeLabel.setForeground(Color.WHITE);
        startLiveClock();
        rightHeaderPanel.add(dateTimeLabel);

        JButton exitButton = new JButton("EXIT");
        exitButton.setFont(new Font("Arial", Font.BOLD, 12));
        exitButton.setBackground(new Color(180, 0, 0));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        exitButton.addActionListener(e -> System.exit(0));
        rightHeaderPanel.add(exitButton);

        topControlsPanel.add(rightHeaderPanel, BorderLayout.EAST);
        headerPanel.add(topControlsPanel, BorderLayout.NORTH);

        posterLabel = new JLabel("", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(200, 150));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(new Color(30, 30, 30));
        posterLabel.setForeground(Color.WHITE);
        posterLabel.setFont(new Font("Arial", Font.BOLD, 18));
        posterLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        headerPanel.add(posterLabel, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        JPanel cinemaLayout = new JPanel(new BorderLayout());
        cinemaLayout.setBackground(Color.BLACK);

        screenPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Color.DARK_GRAY);
                g2d.fillArc(200, 10, getWidth() - 400, 80, 0, 180);

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String screenText = "S   C   R   E   E   N";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(screenText);
                g2d.drawString(screenText, (getWidth() - textWidth) / 2, 45);
            }
        };
        screenPanel.setPreferredSize(new Dimension(1000, 100));
        screenPanel.setBackground(Color.BLACK);
        cinemaLayout.add(screenPanel, BorderLayout.NORTH);

        seatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        seatPanel.setBackground(Color.BLACK);
        seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftBlock = new JPanel(new GridLayout(8, 4, 12, 12));
        JPanel centerBlock = new JPanel(new GridLayout(8, 6, 12, 12));
        JPanel rightBlock = new JPanel(new GridLayout(8, 4, 12, 12));

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
                seatButtons[index].setFont(new Font("Arial", Font.BOLD, 14));
                seatButtons[index].setPreferredSize(new Dimension(70, 55));
                seatButtons[index].setMargin(new Insets(0, 0, 0, 0));
                seatButtons[index].setForeground(Color.BLACK);

                final int finalIndex = index;
                seatButtons[index].addActionListener(e -> handleSeatClick(finalIndex));

                if (c < 4) leftBlock.add(seatButtons[index]);
                else if (c < 10) centerBlock.add(seatButtons[index]);
                else rightBlock.add(seatButtons[index]);
            }
        }

        seatPanel.add(leftBlock);
        seatPanel.add(centerBlock);
        seatPanel.add(rightBlock);

        cinemaLayout.add(seatPanel, BorderLayout.CENTER);
        add(cinemaLayout, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);

        JPanel legendPanel = new JPanel();
        legendPanel.setBackground(Color.BLACK);
        legendPanel.add(createLegendLabel("Available", Color.GREEN));
        legendPanel.add(createLegendLabel("Selected", Color.YELLOW));
        legendPanel.add(createLegendLabel("Booked", Color.RED));

        bookButton = new JButton("PROCEED TO CHECKOUT");
        bookButton.setFont(new Font("Arial", Font.BOLD, 22));
        bookButton.setBackground(Color.WHITE);
        bookButton.setForeground(Color.BLACK);
        bookButton.setPreferredSize(new Dimension(300, 60));
        bookButton.addActionListener(e -> processPayment());

        bottomPanel.add(legendPanel, BorderLayout.NORTH);
        bottomPanel.add(bookButton, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));
        add(bottomPanel, BorderLayout.SOUTH);

        updateTimeDropdown();
    }

    private void startLiveClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy  |  hh:mm:ss a");
            dateTimeLabel.setText(formatter.format(new Date()));
        });
        timer.start();
    }

    private void updateTimeDropdown() {
        Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
        if (selectedMovie == null) return;

        cinemaIndicatorLabel.setText("Location: " + selectedMovie.cinema);
        timeComboBox.removeAllItems();
        for (String time : selectedMovie.showtimes) {
            timeComboBox.addItem(time);
        }

        if (screenPanel != null) screenPanel.repaint();
        updateMovieSelection();
    }

    private String getCurrentSessionKey() {
        Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
        String selectedTime = (String) timeComboBox.getSelectedItem();
        if (selectedMovie == null || selectedTime == null) return null;
        return selectedMovie.title + "-" + selectedMovie.cinema + "-" + selectedTime;
    }

    private void updateMovieSelection() {
        String sessionKey = getCurrentSessionKey();
        if (sessionKey == null) return;

        Movie selectedMovie = (Movie) movieComboBox.getSelectedItem();
        String selectedTime = (String) timeComboBox.getSelectedItem();
        posterLabel.setText("<html><center><font size='5'>" + selectedMovie.title + "</font><br><br>" + selectedMovie.cinema + " | " + selectedTime + "</center></html>");

        boolean[] bookedSeats = bookedSeatsMap.get(sessionKey);
        int bookedCount = 0;
        currentSelection.clear();

        for (int i = 0; i < TOTAL_SEATS; i++) {
            if (bookedSeats[i]) {
                seatButtons[i].setBackground(Color.RED);
                bookedCount++;
            } else {
                seatButtons[i].setBackground(Color.GREEN);
            }
        }
        seatsRemainingLabel.setText("Seats Left: " + (TOTAL_SEATS - bookedCount));
    }

    private void handleSeatClick(int index) {
        String sessionKey = getCurrentSessionKey();
        if (sessionKey == null) return;

        boolean[] bookedSeats = bookedSeatsMap.get(sessionKey);
        if (bookedSeats[index]) {
            int confirm = JOptionPane.showConfirmDialog(this, "Seat already booked. Cancel reservation?", "Cancel?", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                bookedSeats[index] = false;
                // PERSISTENCE: Save state after cancelling a booking
                DatabaseManager.saveBookings(bookedSeatsMap);
                updateMovieSelection();
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
            JOptionPane.showMessageDialog(this, "Please select at least one seat to proceed.", "Selection Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalTickets = currentSelection.size();
        int discountedCount = 0;
        String discountInput = JOptionPane.showInputDialog(this, "Total Seats: " + totalTickets + "\nHow many Student/Senior discounts (20% off)?", "0");
        try {
            discountedCount = Math.min(totalTickets, Math.max(0, Integer.parseInt(discountInput)));
        } catch (Exception e) { discountedCount = 0; }

        int popcornQty = 0;
        int sodaQty = 0;

        JPanel snackPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        snackPanel.add(new JLabel("Popcorn (PHP " + POPCORN_PRICE + "):"));
        JSpinner popSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        snackPanel.add(popSpinner);
        snackPanel.add(new JLabel("Soda (PHP " + SODA_PRICE + "):"));
        JSpinner sodaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        snackPanel.add(sodaSpinner);

        int snackChoice = JOptionPane.showConfirmDialog(this, snackPanel, "Cinema Concessions", JOptionPane.OK_CANCEL_OPTION);
        if (snackChoice == JOptionPane.OK_OPTION) {
            popcornQty = (int) popSpinner.getValue();
            sodaQty = (int) sodaSpinner.getValue();
        }

        int regularCount = totalTickets - discountedCount;
        int ticketTotal = (regularCount * TICKET_PRICE) + (discountedCount * (int)(TICKET_PRICE * 0.8));
        int snackTotal = (popcornQty * POPCORN_PRICE) + (sodaQty * SODA_PRICE);
        int finalTotal = ticketTotal + snackTotal;

        Movie movie = (Movie) movieComboBox.getSelectedItem();
        String time = (String) timeComboBox.getSelectedItem();

        StringBuilder seatsText = new StringBuilder();
        for (int i = 0; i < currentSelection.size(); i++) {
            int idx = currentSelection.get(i);
            char row = (char) ('A' + (idx / 14));
            int col = (idx % 14) + 1;
            seatsText.append(row).append(col).append(i == currentSelection.size() - 1 ? "" : ", ");
        }

        String summary = String.format(
                "ORDER SUMMARY\n------------------------------------\nMovie: %s\nTickets: %d (Reg: %d, Disc: %d)\nSnacks: %d Popcorn, %d Soda\n------------------------------------\nTOTAL: PHP %d\n\nFinalize and Print Receipt?",
                movie.title, totalTickets, regularCount, discountedCount, popcornQty, sodaQty, finalTotal
        );

        if (JOptionPane.showConfirmDialog(this, summary, "Payment Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean[] booked = bookedSeatsMap.get(getCurrentSessionKey());
            for (int idx : currentSelection) booked[idx] = true;

            // PERSISTENCE: Save the entire map to file once payment is confirmed
            DatabaseManager.saveBookings(bookedSeatsMap);

            ReceiptPrinter.generateReceipt(
                    movie.title,
                    movie.cinema,
                    time,
                    seatsText.toString(),
                    regularCount,
                    discountedCount,
                    popcornQty,
                    sodaQty,
                    finalTotal,
                    TICKET_PRICE
            );

            JOptionPane.showMessageDialog(this, "Payment Successful!\nReceipt generated in 'Receipts' folder.");
            updateMovieSelection();
        }
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setOpaque(true);
        label.setBackground(color);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return label;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MovieTicketBooking().setVisible(true));
    }
}