public class Movie {
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