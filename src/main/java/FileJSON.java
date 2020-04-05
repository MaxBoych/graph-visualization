import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
        double start = 0; //mapOfVertices.size();
        //double end = 100 * start;
        Random random = new Random();
        for (String name : mapOfVertices.keySet()) {
            double randomX = start + (random.nextDouble() * 100);
            double randomY = start + (random.nextDouble() * 100);
            vertices.put(name, new Vertex(randomX, randomY, name, (String) mapOfVertices.get(name)));
        }

        List<Object> listOfEdges = json.getJSONArray(Config.EDGES_FIELD).toList();
        for (Object object : listOfEdges) {
            @SuppressWarnings("unchecked")
            List<String> edge = (List<String>) object;
            edges.add(new Edge(edge));
        }

        convertToDot();

       /* for (Vertex vertex : vertices.values()) {
            System.out.println(vertex.getName());
        }*/

        /*for (Edge edge : edges) {
            System.out.println(edge.getV() + " " + edge.getU());
        }*/
    }

    private void convertToDot() {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph G {\n");
        for (Edge edge : edges) {
            builder.append("\t\"")
                    .append(vertices.get(edge.getV()).getValue())
                    .append("\" -> \"")
                    .append(vertices.get(edge.getU()).getValue())
                    .append("\" [label = \"")
                    .append(edge.getValue())
                    .append("\"];\n");
        }
        builder.append("}");

        try {
            PrintWriter writer = new PrintWriter("/home/max/gitwatch/lab6_vlad/VisualizationJSON/fileDOT.txt", "UTF-8");
            writer.println(builder.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }
}
