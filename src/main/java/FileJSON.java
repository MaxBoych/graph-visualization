import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FileJSON {

    private String path;
    private Map<String, Vertex> vertices;
    private List<Edge> edges;

    FileJSON(String path) {
        this.path = path;
        vertices = new HashMap<String, Vertex>();
        edges = new ArrayList<Edge>();
    }

    private String readJSONFile() {
        String text = null;
        try {
            Scanner in = new Scanner(new File(path), Config.ENCODING_UTF_8);
            text = in.useDelimiter("\\A").next();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (text == null) {
            System.out.println(Config.FILE_PATH_ERROR);
            System.exit(0);
        }
        return text;
    }

    public void parse() {
        String text = readJSONFile();
        JSONObject json = new JSONObject(text);

        Map<String, Object> mapOfVertices = json.getJSONObject(Config.VERTICES_FIELD).toMap();
        double start = mapOfVertices.size();
        double end = 100 * start;
        Random random = new Random();
        for (String name : mapOfVertices.keySet()) {
            double randomX = start + (random.nextDouble() * (end - start));
            double randomY = start + (random.nextDouble() * (end - start));
            vertices.put(name, new Vertex(randomX, randomY, name, (String) mapOfVertices.get(name)));
        }

        List<Object> listOfEdges = json.getJSONArray(Config.EDGES_FIELD).toList();
        for (Object object : listOfEdges) {
            @SuppressWarnings("unchecked")
            List<String> edge = (List<String>) object;
            edges.add(new Edge(edge));
        }
    }

    public Map<String, Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}