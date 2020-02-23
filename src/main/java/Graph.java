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

    @SuppressWarnings("StatementWithEmptyBody")
    public void drawingStart() {

        forceDirectedAlgorithm(Config.ITERATION_AMOUNT);

        while (checkVertexEdgeIntersections()) ;

        //forceDirectedAlgorithm(50);

        /*findNegativePositions();
        adjustPositions();
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;*/

        for (int i = 0; i < 100; i++) {
            if (checkEdgesIntersection()) {
                break;
            }
        }

        while (checkVertexEdgeIntersections()) ;
        findNegativePositions();
        adjustPositions();
    }

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
    }

    private boolean checkVertexEdgeIntersections() {
        boolean wasNotIntersection = true;
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : edges) {
                if (vertex.getName().equals(edge.getV()) || vertex.getName().equals(edge.getU())) {
                    continue;
                }

                double x1 = vertices.get(edge.getV()).getPosition().getX();
                double y1 = vertices.get(edge.getV()).getPosition().getY();
                double x2 = vertices.get(edge.getU()).getPosition().getX();
                double y2 = vertices.get(edge.getU()).getPosition().getY();
                if (isVertexEdgeIntersection(vertex, edge)) {
                    //System.out.println(vertex.getName() + " :   " + edge.getV() + " " + edge.getU());
                    double angle = Math.atan2(y2 - y1, x2 - x1);
                    double sin = Math.sin(angle);
                    double cos = Math.cos(angle);

                    double newX = vertex.getPosition().getX() + (2 * Config.VERTEX_DIAMETER) * sin /* Math.random()*/;
                    double newY = vertex.getPosition().getY() + (2 * Config.VERTEX_DIAMETER) * cos /* Math.random()*/;
                    Vector newPosition = new Vector(newX, newY);
                    vertex.setPosition(newPosition);
                    wasNotIntersection = false;
                }
            }

            for (Vertex vertex1 : vertices.values()) {
                if (vertex == vertex1) {
                    continue;
                }

                double dx = vertex.getPosition().getX() - vertex1.getPosition().getX();
                double dy = vertex.getPosition().getY() - vertex1.getPosition().getY();
                double distance = calculateVectorLength(new Vector(dx, dy));
                if (distance < 2 * Config.VERTEX_DIAMETER) {
                    vertex1.setPosition(new Vector(
                            vertex1.getPosition().getX() + 1.2*Config.VERTEX_DIAMETER,
                            vertex1.getPosition().getY() + 1.2*Config.VERTEX_DIAMETER)
                    );

                    vertex.setPosition(new Vector(
                            vertex.getPosition().getX() - 1.2*Config.VERTEX_DIAMETER,
                            vertex.getPosition().getY() - 1.2*Config.VERTEX_DIAMETER)
                    );
                    wasNotIntersection = false;
                }
            }
        }

        return wasNotIntersection;

        /*if (wasIntersection) {
            //forceDirectedAlgorithm(10);
            calculateCoefficients();
            findNegativePositions();
            adjustPositions();
            checkIntersections();
        }*/
    }

    private boolean checkEdgesIntersection() {
        boolean wasNotIntersection = true;
        int bestAmount = calculateEdgesIntersection();
        System.out.println("start: " + bestAmount);
        for (Edge edge1 : edges) {
            for (Edge edge2 : edges) {
                if (edge1 == edge2 || edge1.getV().equals(edge2.getV()) ||
                        edge1.getV().equals(edge2.getU()) ||
                        edge1.getU().equals(edge2.getV()) ||
                        edge1.getU().equals(edge2.getU())) {
                    //System.out.println("HERE");
                    continue;
                }

                if (isEdgesIntersection(edge1, edge2)) {
                    wasNotIntersection = false;

                    Vertex v1 = vertices.get(edge1.getV());
                    Vertex u1 = vertices.get(edge1.getU());
                    Vertex v2 = vertices.get(edge2.getV());
                    Vertex u2 = vertices.get(edge2.getU());

                    //System.out.println("before: " + v1.getPosition().getX() + " " + v1.getPosition().getY());

                    Vector v1_pos = v1.getPosition();
                    Vector u1_pos = u1.getPosition();
                    Vector v2_pos = v2.getPosition();
                    Vector u2_pos = u2.getPosition();

                    int bestCase = 0;

                    Vertex v1_case1 = new Vertex(v2_pos.getX(), v2_pos.getY(), v1.getName(), v1.getValue());
                    Vertex v2_case1 = new Vertex(v1_pos.getX(), v1_pos.getY(), v2.getName(), v2.getValue());
                    swapVertices(edge1.getV(), v1_case1, edge2.getV(), v2_case1);
                    int newCase = calculateEdgesIntersection();
                    //System.out.println("case1: " + newCase);
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 1;
                    }
                    swapVertices(edge1.getV(), v1, edge2.getV(), v2);

                    //System.out.println("after 1: " + v1.getPosition().getX() + " " + v1.getPosition().getY());

                    Vertex v2_case2 = new Vertex(u1_pos.getX(), u1_pos.getY(), v2.getName(), v2.getValue());
                    Vertex u1_case2 = new Vertex(v2_pos.getX(), v2_pos.getY(), u1.getName(), u1.getValue());
                    swapVertices(edge2.getV(), v2_case2, edge1.getU(), u1_case2);
                    newCase = calculateEdgesIntersection();
                    //System.out.println("case2: " + newCase);
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 2;
                    }
                    swapVertices(edge2.getV(), v2, edge1.getU(), u1);

                    //System.out.println("after 2: " + v1.getPosition().getX() + " " + v1.getPosition().getY());

                    Vertex u1_case3 = new Vertex(u2_pos.getX(), u2_pos.getY(), u1.getName(), u1.getValue());
                    Vertex u2_case3 = new Vertex(u1_pos.getX(), u1_pos.getY(), u2.getName(), u2.getValue());
                    swapVertices(edge1.getU(), u1_case3, edge2.getU(), u2_case3);
                    newCase = calculateEdgesIntersection();
                    //System.out.println("case3: " + newCase);
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 3;
                    }
                    swapVertices(edge1.getU(), u1, edge2.getU(), u2);

                    //System.out.println("after 3: " + v1.getPosition().getX() + " " + v1.getPosition().getY());

                    Vertex u2_case4 = new Vertex(v1_pos.getX(), v1_pos.getY(), u2.getName(), u2.getValue());
                    Vertex v1_case4 = new Vertex(u2_pos.getX(), u2_pos.getY(), v1.getName(), v1.getValue());
                    swapVertices(edge2.getU(), u2_case4, edge1.getV(), v1_case4);
                    newCase = calculateEdgesIntersection();
                    //System.out.println("case4: " + newCase);
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 4;
                    }
                    swapVertices(edge2.getU(), u2, edge1.getV(), v1);

                    //System.out.println("afte: " + v1.getPosition().getX() + " " + v1.getPosition().getY());

                    switch (bestCase) {
                        case 0:
                            break;
                        case 1:
                            swapVertices(edge1.getV(), v1_case1, edge2.getV(), v2_case1);
                            break;
                        case 2:
                            swapVertices(edge2.getV(), v2_case2, edge1.getU(), u1_case2);
                            break;
                        case 3:
                            swapVertices(edge1.getU(), u1_case3, edge2.getU(), u2_case3);
                            break;
                        case 4:
                            swapVertices(edge2.getU(), u2_case4, edge1.getV(), v1_case4);
                            break;
                    }
                }
            }
        }

        System.out.println("end: " + bestAmount);
        return wasNotIntersection;
    }

    private void swapVertices(String key1, Vertex v1, String key2, Vertex v2) {
        vertices.put(key1, v1);
        vertices.put(key2, v2);
    }

    private int calculateEdgesIntersection() {
        int amount = 0;
        for (Edge edge1 : edges) {
            for (Edge edge2 : edges) {
                if (edge1 == edge2 || edge1.getV().equals(edge2.getV()) ||
                        edge1.getV().equals(edge2.getU()) ||
                        edge1.getU().equals(edge2.getV()) ||
                        edge1.getU().equals(edge2.getU())) {
                    continue;
                }

                if (isEdgesIntersection(edge1, edge2)) {
                    amount++;
                }
            }
        }

        return amount;
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
        double vectorLength = calculateVectorLength(vectorDifference) /*+ Config.VERTEX_DIAMETER*/;
        double attractionValue = attraction(vectorLength) /*+ Config.VERTEX_DIAMETER*/;

        double multiplier = attractionValue / vectorLength;
        Vector multipliedVector = multiplyVector(vectorDifference, multiplier);

        Vector displacementV = vertices.get(v).getDisplacement();
        vertices.get(v).setDisplacement(calculateVectorDifference(displacementV, multipliedVector));

        Vector displacementU = vertices.get(u).getDisplacement();
        vertices.get(u).setDisplacement(calculateVectorSum(displacementU, multipliedVector));
    }

    public void calculateRepulsiveForces(Vertex v, Vertex u) {
        Vector vectorDifference = calculateVectorDifference(v.getPosition(), u.getPosition());
        double vectorLength = calculateVectorLength(vectorDifference);
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

    private boolean isVertexEdgeIntersection(Vertex vertex, Edge edge) {
        double x1 = vertices.get(edge.getV()).getPosition().getX() - vertex.getPosition().getX();
        double y1 = vertices.get(edge.getV()).getPosition().getY() - vertex.getPosition().getY();
        double x2 = vertices.get(edge.getU()).getPosition().getX() - vertex.getPosition().getX();
        double y2 = vertices.get(edge.getU()).getPosition().getY() - vertex.getPosition().getY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double a = dx * dx + dy * dy;
        double b = 2 * (x1 * dx + y1 * dy);
        double c = x1 * x1 + y1 * y1 - Config.VERTEX_RADIUS * Config.VERTEX_RADIUS;

       /* double x1 = vertices.get(edge.getV()).getPosition().getX();
        double y1 = vertices.get(edge.getV()).getPosition().getY();
        double x2 = vertices.get(edge.getU()).getPosition().getX();
        double y2 = vertices.get(edge.getU()).getPosition().getY();
        double x3 = vertex.getPosition().getX();
        double y3 = vertex.getPosition().getY();

        double a = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        double b = 2 * ((x2 - x1) * (x1 - x3) + (y2 - y1) * (y1 - y3));
        double c = x3 * x3 + y3 * y3 + x1 * x1 + y1 * y1 - 2 * (x3 * x1 + y3 * y1) - Config.VERTEX_DIAMETER * Config.VERTEX_DIAMETER;*/

        if (-b < 0) {
            return c < 0;
        } else if (-b < 2 * a) {
            return 4 * a * c - b * b < 0;
        } else {
            return a + b + c < 0;
        }
    }

    private boolean isEdgesIntersection(Edge edge1, Edge edge2) {
        Vector v1 = vertices.get(edge1.getV()).getPosition();
        Vector v2 = vertices.get(edge1.getU()).getPosition();
        Vector v3 = vertices.get(edge2.getV()).getPosition();
        Vector v4 = vertices.get(edge2.getU()).getPosition();

        /*double x1 = v1.getX();
        double y1 = v1.getY();
        double x2 = v2.getX();
        double y2 = v2.getY();
        double x3 = v3.getX();
        double y3 = v3.getY();
        double x4 = v4.getX();
        double y4 = v4.getY();*/

        long x1 = (long) v1.getX();
        long y1 = (long) v1.getY();
        long x2 = (long) v2.getX();
        long y2 = (long) v2.getY();
        long x3 = (long) v3.getX();
        long y3 = (long) v3.getY();
        long x4 = (long) v4.getX();
        long y4 = (long) v4.getY();

        /*double dx1, dx2, dy1, dy2, D, t;
        dx1 = x2 - x1;
        dy1 = y2 - y1;
        dx2 = x4 - x3;
        dy2 = y4 - y3;

        D = dx2 * dy1 - dx1 * dy2;

        t = dx1 * (y3 - y1) - dy1 * (x3 - x1);
        if (D*t < 0 || Math.abs(t) > Math.abs(D))
            return false;

        t = dx2 * (y3 - y1) - dy2 * (x3 - x1);
        if (D*t < 0 || Math.abs(t) > Math.abs(D))
            return false;

        return true;*/

        long denominator = (y4 - y3) * (x1 - x2) - (x4 - x3) * (y1 - y2);
        if (denominator == 0) {
            return false;
            //return (x1 * y2 - x2 * y1) * (x4 - x3) - (x3 * y4 - x4 * y3) * (x2 - x1) == 0 &&
              //      (x1 * y2 - x2 * y1) * (y4 - y3) - (x3 * y4 - x4 * y3) * (y2 - y1) == 0;
        } else {
            double numerator_a = (x4 - x2) * (y4 - y3) - (x4 - x3) * (y4 - y2);
            double numerator_b = (x1 - x2) * (y4 - y2) - (x4 - x2) * (y1 - y2);
            double Ua = numerator_a / denominator;
            double Ub = numerator_b / denominator;

            return Ua >= 0 && Ua <= 1 && Ub >= 0 && Ub <= 1;
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