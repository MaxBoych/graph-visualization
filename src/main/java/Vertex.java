public class Vertex {

    private String name, value;
    private Vector position;
    private Vector displacement;

    Vertex() {
    }

    Vertex(String name, String value) {
        this.name = name;
        this.value = value;
    }

    Vertex(double x, double y, String name, String value) {
        position = new Vector(x, y);
        this.name = name;
        this.value = value;
        displacement = new Vector(0.0, 0.0);
    }

    public void updatePosition(Vector vector) {
        this.position = vector;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setDisplacement(Vector displacement) {
        this.displacement = displacement;
    }

    public Vector getDisplacement() {
        return displacement;
    }

    public String getName() {
        return name;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public Vector getPosition() {
        return position;
    }
}
