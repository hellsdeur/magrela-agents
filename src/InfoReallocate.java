public class InfoReallocate implements jade.util.leap.Serializable {
    String station;
    String address;

    int sendNumBikes;

    public InfoReallocate(String station, String address, int sendNumBikes) {
        this.station = station;
        this.address = address;
        this.sendNumBikes = sendNumBikes;
    }
}