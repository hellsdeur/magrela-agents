public class InfoUser implements jade.util.leap.Serializable {
    String name;
    String bike;
    double latitude;
    double longitude;

    public InfoUser(String name, String bike, double latitude, double longitude) {
        this.name = name;
        this.bike = bike;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
