import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParserMap {
    private HashMap<String,HashMap<String, String>> map;

    public ParserMap(String path, String [] keys) throws IOException {
        map = new HashMap<>();

        List<String> list = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        String[] lines = list.toArray(new String[list.size()]);

        for (int i = 1; i < lines.length; ++i) {
            String line = lines[i];
            String[] columns = line.split(",");
            HashMap<String, String> current = new HashMap<>();
            for (int j = 1; j < keys.length; ++j) {
                current.put(keys[j], columns[j]);
            }
            map.put(columns[0], current);
         }
    }

    public String getData(String name, String key) {
        return map.get(name).get(key);
    }

    public Set<String> getNames() {
        return map.keySet();
    }

    public String toString() {
        String s = "";
        for (String key: map.keySet()) {
            String name = key.toString();
            s += "Name: " + name + "\n";
            HashMap<String, String> map2 = map.get(name);
            for (String key2: map2.keySet()) {
                String name2 = key2.toString();
                String value = map2.get(key2).toString();
                s+= "\t" + key2 + ": " + value + "\n";
            }
        }
        return s;
    }
}
