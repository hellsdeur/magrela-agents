public class InfoBike implements jade.util.leap.Serializable {
    String bike;
    String user;
    String station;

    public InfoBike(String bike, String user, String station) {
        this.bike = bike;
        this.user = user;
        this.station = station;
    }
}
