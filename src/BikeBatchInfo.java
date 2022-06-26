import java.util.LinkedList;
import java.util.Queue;

public class BikeBatchInfo implements jade.util.leap.Serializable {
    public Queue<String> bikes;

    public BikeBatchInfo() {
        this.bikes = new LinkedList<String>();
    }
}
