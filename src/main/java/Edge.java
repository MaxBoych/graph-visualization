import java.util.List;

public class Edge {

    private String v, u;
    private String value;

    Edge() {
    }

    Edge(List<String> edge) {
        this.v = edge.get(0);
        this.u = edge.get(1);
        if (edge.size() == 3) {
            this.value = edge.get(2);
        } else {
            this.value = "";
        }
    }

    Edge(String v, String u, String value) {
        this.v = v;
        this.u = u;
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getV() {
        return v;
    }

    public String getU() {
        return u;
    }
}
