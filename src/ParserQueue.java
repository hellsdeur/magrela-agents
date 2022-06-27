import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ParserQueue {
    public Queue<String> queue;

    public ParserQueue(String path) {
        queue = new LinkedList<>();

        List<String> list = null;
        try {
            list = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        queue.addAll(list);
    }
}
