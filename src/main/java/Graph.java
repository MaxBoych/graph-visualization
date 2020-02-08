import java.util.List;
import java.util.Map;

public class Graph {

    private Map<String, Vertex> vertices;
    private List<Edge> edges;
    private double k;
    private double t;
    private double minX;
    private double minY;

    Graph() {
    }

    Graph(Map<String, Vertex> vertices, List<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;

        Config.WIDTH = 50 * vertices.size() + Config.RESERVE;
        Config.HEIGHT = 30 * vertices.size() + Config.RESERVE;

        if (Config.WIDTH > 1280) {
            Config.WIDTH = 1280;
        }
        if (Config.HEIGHT > 720) {
            Config.HEIGHT = 720;
        }
    }

    private void calculateCoefficients() {
        k = Math.sqrt(Config.WIDTH * Config.HEIGHT / (double) vertices.size());
        t = (double) Config.WIDTH / 10;
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
    }

    public void drawingStart() {
        forceDirectedAlgorithm(Config.ITERATION_AMOUNT);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void forceDirectedAlgorithm(int amount) {
        calculateCoefficients();
        for (int i = 0; i < amount; i++) {

            //calculate repulsive forces
            for (Vertex v : vertices.values()) {
                vertices.get(v.getName()).setDisplacement(new Vector(0.0, 0.0));
                for (Vertex u : vertices.values()) {
                    if (v.getName().equals(u.getName())) {
                        continue;
                    }
                    calculateRepulsiveForces(v, u);
                }
            }

            //calculate attractive forces
            for (Edge e : edges) {
                String v = e.getV();
                String u = e.getU();
                calculateAttractiveForces(v, u);
            }

            //calculate new positions
            for (Vertex v : vertices.values()) {
                calculateNewPosition(v);
            }

            //calculate new temperature
            cool(i);
        }

        while (checkIntersections());

        findNegativePositions();
        adjustPositions();
    }

    private boolean checkIntersections() {
        boolean wasIntersection = false;
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : edges) {
                if (vertex.getName().equals(edge.getV()) || vertex.getName().equals(edge.getU())) {
                    continue;
                }

                double x1 = vertices.get(edge.getV()).getPosition().getX();
                double y1 = vertices.get(edge.getV()).getPosition().getY();
                double x2 = vertices.get(edge.getU()).getPosition().getX();
                double y2 = vertices.get(edge.getU()).getPosition().getY();
                if (isIntersection(vertex, edge)) {
                    System.out.println(vertex.getName() + " :   " + edge.getV() + " " + edge.getU());
                    double angle = Math.atan2(y2 - y1, x2 - x1);
                    double sin = Math.sin(angle);
                    double cos = Math.cos(angle);

                    double newX = vertex.getPosition().getX() + (Config.VERTEX_DIAMETER + Config.VERTEX_RADIUS) * sin;
                    double newY = vertex.getPosition().getY() + (Config.VERTEX_DIAMETER + Config.VERTEX_RADIUS) * cos;
                    Vector newPosition = new Vector(newX, newY);
                    vertex.setPosition(newPosition);
                    wasIntersection = true;
                }
            }
        }

        return wasIntersection;

        /*if (wasIntersection) {
            //forceDirectedAlgorithm(10);
            calculateCoefficients();
            findNegativePositions();
            adjustPositions();
            checkIntersections();
        }*/
    }

    private void adjustPositions() {
        if (minX < Config.RESERVE && minY < Config.RESERVE) {
            for (Vertex v : vertices.values()) {
                Vector oldPosition = v.getPosition();
                v.setPosition(new Vector(oldPosition.getX() - minX + Config.RESERVE, oldPosition.getY() - minY + Config.RESERVE));
            }
        } else if (minX < Config.RESERVE) {
            for (Vertex v : vertices.values()) {
                Vector oldPosition = v.getPosition();
                v.setPosition(new Vector(oldPosition.getX() - minX + Config.RESERVE, oldPosition.getY()));
            }
        } else if (minY < Config.RESERVE) {
            for (Vertex v : vertices.values()) {
                Vector oldPosition = v.getPosition();
                v.setPosition(new Vector(oldPosition.getX(), oldPosition.getY() - minY + Config.RESERVE));
            }
        }
    }

    private void findNegativePositions() {
        for (Vertex v : vertices.values()) {
            minX = Math.min(minX, v.getPosition().getX());
            minY = Math.min(minY, v.getPosition().getY());
        }
    }

    private void cool(int currentIteration) {
        t *= (1.0 - (double) currentIteration / Config.ITERATION_AMOUNT);
        //System.out.println(t);
    }

    private void calculateNewPosition(Vertex vertex) {
        Vector displacement = vertex.getDisplacement();
        double vectorLength = calculateVectorLength(displacement);

        double multiplier = Math.min(vectorLength, t) / vectorLength;
        Vector multipliedVector = multiplyVector(displacement, multiplier);
        Vector newPosition = calculateVectorSum(vertex.getPosition(), multipliedVector);

        double newX = Math.min((double) Config.WIDTH / 2, Math.max(-1.0 * Config.WIDTH / 2.0, newPosition.getX()));
        double newY = Math.min((double) Config.HEIGHT / 2, Math.max(-1.0 * Config.HEIGHT / 2.0, newPosition.getY()));
        vertices.get(vertex.getName()).setPosition(new Vector(newX, newY));
    }

    private void calculateAttractiveForces(String v, String u) {
        Vector positionV = vertices.get(v).getPosition();
        Vector positionU = vertices.get(u).getPosition();

        Vector vectorDifference = calculateVectorDifference(positionV, positionU);
        double vectorLength = calculateVectorLength(vectorDifference) + Config.VERTEX_DIAMETER;
        double attractionValue = attraction(vectorLength);

        double multiplier = attractionValue / vectorLength;
        Vector multipliedVector = multiplyVector(vectorDifference, multiplier);

        Vector displacementV = vertices.get(v).getDisplacement();
        vertices.get(v).setDisplacement(calculateVectorDifference(displacementV, multipliedVector));

        Vector displacementU = vertices.get(u).getDisplacement();
        vertices.get(u).setDisplacement(calculateVectorSum(displacementU, multipliedVector));
    }

    public void calculateRepulsiveForces(Vertex v, Vertex u) {
        Vector vectorDifference = calculateVectorDifference(v.getPosition(), u.getPosition());
        double vectorLength = calculateVectorLength(vectorDifference) - Config.VERTEX_DIAMETER;
        double repulsionValue = repulsion(vectorLength);

        double multiplier = repulsionValue / vectorLength;
        Vector multipliedVector = multiplyVector(vectorDifference, multiplier);

        vertices.get(v.getName()).setDisplacement(calculateVectorSum(v.getDisplacement(), multipliedVector));
    }

    private Vector multiplyVector(Vector vector, double multiplier) {
        return new Vector(vector.getX() * multiplier, vector.getY() * multiplier);
    }

    private double calculateVectorLength(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        return Math.sqrt(x * x + y * y);
    }

    private Vector calculateVectorDifference(Vector v, Vector u) {
        return new Vector(v.getX() - u.getX(),
                v.getY() - u.getY());
    }

    private Vector calculateVectorSum(Vector v, Vector u) {
        return new Vector(v.getX() + u.getX(),
                v.getY() + u.getY());
    }

    private boolean isIntersection(Vertex vertex, Edge edge) {
        double x1 = vertices.get(edge.getV()).getPosition().getX() - vertex.getPosition().getX();
        double y1 = vertices.get(edge.getV()).getPosition().getY() - vertex.getPosition().getY();
        double x2 = vertices.get(edge.getU()).getPosition().getX() - vertex.getPosition().getX();
        double y2 = vertices.get(edge.getU()).getPosition().getY() - vertex.getPosition().getY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double a = dx * dx + dy * dy;
        double b = 2 * (x1 * dx + y1 * dy);
        double c = x1 * x1 + y1 * y1 - Config.VERTEX_DIAMETER * Config.VERTEX_DIAMETER;

        if (-b < 0) {
            return c < 0;
        } else if (-b < 2 * a) {
            return 4 * a * c - b * b < 0;
        } else {
            return a + b + c < 0;
        }
    }

    public double attraction(double x) {
        return x * x / k;
    }

    public double repulsion(double x) {
        return k * k / x;
    }

    public Map<String, Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vertex getVertex(String name) {
        return vertices.get(name);
    }
}