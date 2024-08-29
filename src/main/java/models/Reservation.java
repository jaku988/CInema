package models;

public class Reservation {
    private int id;
    private int userId;
    private int screeningId;
    private int seatId;

    // Constructors, getters, and setters
    public Reservation(int userId, int screeningId, int seatId) {
        this.userId = userId;
        this.screeningId = screeningId;
        this.seatId = seatId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public void setScreeningId(int screeningId) {
        this.screeningId = screeningId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }
}
