import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BikeParser {
    public Queue<String> bikes;

    public BikeParser(String path) {
        bikes = new LinkedList<>();

        List<String> list = null;
        try {
            list = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bikes.addAll(list);
    }
}
