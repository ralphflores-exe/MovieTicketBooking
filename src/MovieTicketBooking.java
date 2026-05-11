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

    // Updated: 5 rows (A-E) * 14 seats = 70 total seats
    private final int ROWS = 5;
    private final int COLS = 14;
    private final int TOTAL_SEATS = ROWS * COLS;
    private final int TICKET_PRICE = 300;

    private final int POPCORN_PRICE = 150;
    private final int SODA_PRICE = 80;

    public MovieTicketBooking() {
        Map<String, boolean[]> savedData = DatabaseManager.loadBookings();
        if (savedData != null) bookedSeatsMap.putAll(savedData);

        for (Movie movie : moviesList) {
            for (String time : movie.showtimes) {
                String key = movie.title + "-" + movie.cinema + "-" + time;
                bookedSeatsMap.putIfAbsent(key, new boolean[TOTAL_SEATS]);
            }
        }

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("CineServe");
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
        timeComboBox.addActionListener(e -> updateMovieSelection());
        selectionPanel.add(timeComboBox);

        seatsRemainingLabel = new JLabel("Seats Left: " + TOTAL_SEATS);
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
        exitButton.setBackground(new Color(180, 0, 0));
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e -> System.exit(0));
        rightHeaderPanel.add(exitButton);

        topControlsPanel.add(rightHeaderPanel, BorderLayout.EAST);
        headerPanel.add(topControlsPanel, BorderLayout.NORTH);

        posterLabel = new JLabel("", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(200, 150));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(new Color(30, 30, 30));
        posterLabel.setForeground(Color.WHITE);
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
                int w = getWidth() - 400, h = 40, x = (getWidth() - w) / 2, y = 30;
                g2d.setColor(new Color(30, 30, 30));
                g2d.fillRect(x, y, w, h);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x, y, w, h);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString("S  C  R  E  E  N", (getWidth() - fm.stringWidth("S  C  R  E  E  N")) / 2, y + (h / 2) + (fm.getAscent() / 2) - 2);
            }
        };
        screenPanel.setPreferredSize(new Dimension(1000, 100));
        screenPanel.setBackground(Color.BLACK);
        cinemaLayout.add(screenPanel, BorderLayout.NORTH);

        seatPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        seatPanel.setBackground(Color.BLACK);
        seatPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Updated: GridLayouts now use 5 rows (A-E)
        JPanel leftBlock = new JPanel(new GridLayout(ROWS, 4, 12, 12));
        JPanel centerBlock = new JPanel(new GridLayout(ROWS, 6, 12, 12));
        JPanel rightBlock = new JPanel(new GridLayout(ROWS, 4, 12, 12));

        leftBlock.setBackground(Color.BLACK);
        centerBlock.setBackground(Color.BLACK);
        rightBlock.setBackground(Color.BLACK);

        seatButtons = new JButton[TOTAL_SEATS];

        // Updated: Loop now runs for ROWS (5 iterations, A to E)
        for (int r = 0; r < ROWS; r++) {
            char rowLetter = (char) ('A' + r);
            for (int c = 0; c < COLS; c++) {
                int index = r * COLS + c;
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
        Movie movie = (Movie) movieComboBox.getSelectedItem();
        if (movie == null) return;
        cinemaIndicatorLabel.setText("Location: " + movie.cinema);
        timeComboBox.removeAllItems();
        for (String time : movie.showtimes) timeComboBox.addItem(time);
        if (screenPanel != null) screenPanel.repaint();
        updateMovieSelection();
    }

    private String getCurrentSessionKey() {
        Movie movie = (Movie) movieComboBox.getSelectedItem();
        String time = (String) timeComboBox.getSelectedItem();
        return (movie == null || time == null) ? null : movie.title + "-" + movie.cinema + "-" + time;
    }

    private void updateMovieSelection() {
        String sessionKey = getCurrentSessionKey();
        if (sessionKey == null) return;

        Movie movie = (Movie) movieComboBox.getSelectedItem();
        String time = (String) timeComboBox.getSelectedItem();
        posterLabel.setText("<html><center><font size='5'>" + movie.title + "</font><br><br>" + movie.cinema + " | " + time + "</center></html>");

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
            if (JOptionPane.showConfirmDialog(this, "Seat already booked. Cancel reservation?", "Cancel?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                bookedSeats[index] = false;
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

        int totalTickets = currentSelection.size(), discountedCount = 0;
        try {
            String input = JOptionPane.showInputDialog(this, "Total Seats: " + totalTickets + "\nHow many Student/Senior discounts (20% off)?", "0");
            discountedCount = Math.min(totalTickets, Math.max(0, Integer.parseInt(input)));
        } catch (Exception e) { discountedCount = 0; }

        int popcornQty = 0, sodaQty = 0;
        JPanel snackPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        JSpinner popSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        JSpinner sodaSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        snackPanel.add(new JLabel("Popcorn (PHP " + POPCORN_PRICE + "):")); snackPanel.add(popSpinner);
        snackPanel.add(new JLabel("Soda (PHP " + SODA_PRICE + "):")); snackPanel.add(sodaSpinner);

        if (JOptionPane.showConfirmDialog(this, snackPanel, "Cinema Concessions", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            popcornQty = (int) popSpinner.getValue();
            sodaQty = (int) sodaSpinner.getValue();
        }

        int reg = totalTickets - discountedCount;
        int total = (reg * TICKET_PRICE) + (discountedCount * (int)(TICKET_PRICE * 0.8)) + (popcornQty * POPCORN_PRICE) + (sodaQty * SODA_PRICE);

        Movie movie = (Movie) movieComboBox.getSelectedItem();
        String time = (String) timeComboBox.getSelectedItem();

        StringBuilder seats = new StringBuilder();
        for (int i = 0; i < currentSelection.size(); i++) {
            int idx = currentSelection.get(i);
            seats.append((char) ('A' + (idx / COLS))).append((idx % COLS) + 1).append(i == currentSelection.size() - 1 ? "" : ", ");
        }

        String summary = String.format("ORDER SUMMARY\nMovie: %s\nTickets: %d\nTOTAL: PHP %d\n\nFinalize?", movie.title, totalTickets, total);

        if (JOptionPane.showConfirmDialog(this, summary, "Payment", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            boolean[] booked = bookedSeatsMap.get(getCurrentSessionKey());
            for (int idx : currentSelection) booked[idx] = true;
            DatabaseManager.saveBookings(bookedSeatsMap);
            ReceiptPrinter.generateReceipt(movie.title, movie.cinema, time, seats.toString(), reg, discountedCount, popcornQty, sodaQty, total, TICKET_PRICE);
            JOptionPane.showMessageDialog(this, "Success! Receipt generated.");
            updateMovieSelection();
        }
    }

    private JLabel createLegendLabel(String text, Color color) {
        JLabel label = new JLabel("  " + text + "  ");
        label.setOpaque(true); label.setBackground(color); label.setForeground(Color.BLACK);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        return label;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MovieTicketBooking().setVisible(true));
    }
}