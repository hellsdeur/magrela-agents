import java.util.LinkedList;
import java.util.Queue;

public class InfoBikeBatch implements jade.util.leap.Serializable {
    public Queue<String> bikes;

    public InfoBikeBatch() {
        this.bikes = new LinkedList<String>();
    }
}
