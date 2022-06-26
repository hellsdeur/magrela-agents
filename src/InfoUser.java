public class InfoUser implements jade.util.leap.Serializable {
    String bike;
    double latitude;
    double longitude;

    public InfoUser(String bike, double latitude, double longitude) {
        this.bike = bike;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
