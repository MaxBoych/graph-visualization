import java.util.List;
import java.util.Map;
import java.util.Random;

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
    }

    private void calculateCoefficients() {
        k = Math.sqrt(Config.WIDTH * Config.HEIGHT / (double) vertices.size());
        t = (double) Config.WIDTH / 10;
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
    }

    public void drawingStart() {
        forceDirectedAlgorithm(Config.ITERATION_AMOUNT);

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
                    if (v == u) {
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

            //normalize vertex and edge intersections and lengths
            normalizeEdgeLength();
            normalizeVertexIntersection();
            normalizeEdgeIntersection();
            normalizeVertexEdgeIntersection();

            //calculate new temperature
            cool(i);
        }
    }

    private void normalizeEdgeLength() {
        double averageLength = Config.AVERAGE_EDGE_LENGTH;
        for (Edge edge : edges) {
            Vertex v = vertices.get(edge.getV());
            Vertex u = vertices.get(edge.getU());

            double dx = v.getPosition().getX() - u.getPosition().getX();
            double dy = v.getPosition().getY() - u.getPosition().getY();
            double distance = (new Vector(dx, dy)).getLength();
            double difference = averageLength - distance;

            if (Math.abs(difference) > 0.25 * averageLength) {
                double xV = v.getPosition().getX();
                double yV = v.getPosition().getY();
                double xU = u.getPosition().getX();
                double yU = u.getPosition().getY();

                double newDistance = averageLength * 0.75 + averageLength * 0.5 * (new Random()).nextDouble();
                if (difference <= 0) {
                    double shortening = (distance - newDistance) / 2;
                    if (dx < 0) {
                        xV += shortening;
                        xU -= shortening;
                    } else {
                        xV -= shortening;
                        xU += shortening;
                    }

                    if (dy < 0) {
                        yV += shortening;
                        yU -= shortening;
                    } else {
                        yV -= shortening;
                        yU += shortening;
                    }
                } else {
                    double elongation = (newDistance - distance) / 2;
                    if (dx < 0) {
                        xV -= elongation;
                        xU += elongation;
                    } else {
                        xV += elongation;
                        xU -= elongation;
                    }

                    if (dy < 0) {
                        yV -= elongation;
                        yU += elongation;
                    } else {
                        yV += elongation;
                        yU -= elongation;
                    }
                }

                v.setPosition(new Vector(xV, yV));
                u.setPosition(new Vector(xU, yU));
            }
        }
    }

    /*private boolean edgeNormalization() {
        double averageLength = Config.AVERAGE_EDGE_LENGTH; //calculateAverageEdgeLength();
        for (Edge edge : edges) {
            Vertex v = vertices.get(edge.getV());
            Vertex u = vertices.get(edge.getU());
            Vector vector = new Vector(
                    u.getPosition().getX() - v.getPosition().getX(),
                    u.getPosition().getY() - v.getPosition().getY()
            );

            //double vectorLength = calculateVectorLength(vector);
            double distance = vector.getLength();
            double difference = averageLength - distance;
            if (Math.abs(difference) > 0.25 * averageLength) {
                Random random = new Random();
                double newLength = averageLength * 0.75 + averageLength * 0.5 * random.nextDouble();
                double newDifference = Math.abs(newLength - distance);

                double lambda;
                double newX;
                double newY;
                //double angle = 360 * random.nextDouble() * Math.PI / 180;
                if (random.nextBoolean()) {
                    if (difference > 0) {
                        lambda = newDifference / distance;
                        newX = v.getPosition().getX() * (1 + lambda) - u.getPosition().getX() * lambda;
                        newY = v.getPosition().getY() * (1 + lambda) - u.getPosition().getY() * lambda;
                    } else {
                        lambda = newDifference / (distance - newDifference);
                        newX = (v.getPosition().getX() + u.getPosition().getX() * lambda) / (1 + lambda);
                        newY = (v.getPosition().getY() + u.getPosition().getY() * lambda) / (1 + lambda);
                    }
                    vertices.get(edge.getV()).setPosition(new Vector(newX, newY));
                } else {
                    if (difference > 0) {
                        lambda = distance / newDifference;
                        newX = (u.getPosition().getX() * (1 + lambda) - v.getPosition().getX()) / lambda;
                        newY = (u.getPosition().getY() * (1 + lambda) - v.getPosition().getY()) / lambda;
                    } else {
                        lambda = (distance - newDifference) / newDifference;
                        newX = (v.getPosition().getX() + u.getPosition().getX() * lambda) / (1 + lambda);
                        newY = (v.getPosition().getY() + u.getPosition().getY() * lambda) / (1 + lambda);
                    }
                    vertices.get(edge.getU()).setPosition(new Vector(newX, newY));
                }

                System.out.println(newX + " " + newY);
                System.out.println(lambda);
                System.out.println();

                return false;
            }
        }

        return true;
    }*/

    /*private double calculateAverageEdgeLength() {
        double averageLength = 0;
        for (Edge edge : edges) {
            Vertex v = vertices.get(edge.getV());
            Vertex u = vertices.get(edge.getU());
            Vector vector = new Vector(
                    u.getPosition().getX() - v.getPosition().getX(),
                    u.getPosition().getY() - v.getPosition().getY()
            );
            averageLength += vector.getLength();
        }
        averageLength /= edges.size();

        return averageLength;
    }*/

    /*private boolean simpleCheckVertexEdgeIntersections() {
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : edges) {
                if (vertex.getName().equals(edge.getV()) || vertex.getName().equals(edge.getU())) {
                    continue;
                }

                if (isVertexEdgeIntersection(vertex, edge).getFirst()) {
                    return true;
                }
            }

            for (Vertex vertex1 : vertices.values()) {
                if (vertex == vertex1) {
                    continue;
                }

                double dx = vertex.getPosition().getX() - vertex1.getPosition().getX();
                double dy = vertex.getPosition().getY() - vertex1.getPosition().getY();
                double distance = (new Vector(dx, dy)).getLength();
                if (distance < 2 * Config.VERTEX_DIAMETER) {
                    return true;
                }
            }
        }

        return false;
    }*/

    /*private boolean checkVertexEdgeIntersections() {
        boolean wasIntersection = false;
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : edges) {
                if (vertex.getName().equals(edge.getV()) || vertex.getName().equals(edge.getU())) {
                    continue;
                }

                //double x1 = vertices.get(edge.getV()).getPosition().getX();
                //double y1 = vertices.get(edge.getV()).getPosition().getY();
                //double x2 = vertices.get(edge.getU()).getPosition().getX();
                //double y2 = vertices.get(edge.getU()).getPosition().getY();
                if (isVertexEdgeIntersection(vertex, edge).getFirst()) {
                    Random random = new Random();
                    double newX = vertex.getPosition().getX() + (Config.VERTEX_DIAMETER) * (1 + Math.random()) * (random.nextBoolean() ? 1 : -1);
                    double newY = vertex.getPosition().getY() + (Config.VERTEX_DIAMETER) * (1 + Math.random()) * (random.nextBoolean() ? 1 : -1);
                    Vector newPosition = new Vector(newX, newY);
                    vertex.setPosition(newPosition);
                    wasIntersection = true;
                }
            }

            for (Vertex vertex1 : vertices.values()) {
                if (vertex == vertex1) {
                    continue;
                }

                double dx = vertex.getPosition().getX() - vertex1.getPosition().getX();
                double dy = vertex.getPosition().getY() - vertex1.getPosition().getY();
                double distance = (new Vector(dx, dy)).getLength();
                if (distance < 2 * Config.VERTEX_DIAMETER) {
                    vertex1.setPosition(new Vector(
                            vertex1.getPosition().getX() + 1.2 * Config.VERTEX_DIAMETER,
                            vertex1.getPosition().getY() + 1.2 * Config.VERTEX_DIAMETER)
                    );

                    vertex.setPosition(new Vector(
                            vertex.getPosition().getX() - 1.2 * Config.VERTEX_DIAMETER,
                            vertex.getPosition().getY() - 1.2 * Config.VERTEX_DIAMETER)
                    );
                    wasIntersection = true;
                }
            }
        }

        return wasIntersection;
    }*/

    private void normalizeVertexEdgeIntersection() {
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : edges) {
                if (vertex.getName().equals(edge.getV()) || vertex.getName().equals(edge.getU())) {
                    continue;
                }

                Vertex v = vertices.get(edge.getV());
                Vertex u = vertices.get(edge.getU());

                Couple<Boolean, Vector> couple = isVertexEdgeIntersection(vertex, edge);
                if (couple.getFirst()) {
                    System.out.println(vertex.getName() + "     " + edge.getV() + " ---> " + edge.getU());
                    Vector vector = couple.getSecond();

                    double xVertex = vertex.getPosition().getX();
                    double yVertex = vertex.getPosition().getY();
                    double xV = v.getPosition().getX();
                    double yV = v.getPosition().getY();
                    double xU = u.getPosition().getX();
                    double yU = u.getPosition().getY();
                    if (vector.getX() < 0) {
                        xVertex -= Config.VERTEX_RADIUS;
                        xV += Config.VERTEX_RADIUS;
                        xU += Config.VERTEX_RADIUS;
                    } else {
                        xVertex += Config.VERTEX_RADIUS;
                        xV -= Config.VERTEX_RADIUS;
                        xU -= Config.VERTEX_RADIUS;
                    }

                    if (vector.getY() < 0) {
                        yVertex -= Config.VERTEX_RADIUS;
                        yV += Config.VERTEX_RADIUS;
                        yU += Config.VERTEX_RADIUS;
                    } else {
                        yVertex += Config.VERTEX_RADIUS;
                        yV -= Config.VERTEX_RADIUS;
                        yU -= Config.VERTEX_RADIUS;
                    }

                    vertex.setPosition(new Vector(xVertex, yVertex));
                    v.setPosition(new Vector(xV, yV));
                    u.setPosition(new Vector(xU, yU));
                }
            }
        }
    }

    private void normalizeVertexIntersection() {
        for (Vertex v : vertices.values()) {
            for (Vertex u : vertices.values()) {
                if (v == u) {
                    continue;
                }

                double dx = v.getPosition().getX() - u.getPosition().getX();
                double dy = v.getPosition().getY() - u.getPosition().getY();
                double distance = (new Vector(dx, dy)).getLength();

                if (distance <= Config.VERTEX_DIAMETER + 15) {
                    double xV = v.getPosition().getX();
                    double yV = v.getPosition().getY();
                    double xU = u.getPosition().getX();
                    double yU = u.getPosition().getY();
                    if (dx < 0) {
                        xV -= Config.VERTEX_RADIUS;
                        xU += Config.VERTEX_RADIUS;
                    } else {
                        xV += Config.VERTEX_RADIUS;
                        xU -= Config.VERTEX_RADIUS;
                    }

                    if (dy < 0) {
                        yV -= Config.VERTEX_RADIUS;
                        yU += Config.VERTEX_RADIUS;
                    } else {
                        yV += Config.VERTEX_RADIUS;
                        yU -= Config.VERTEX_RADIUS;
                    }

                    v.setPosition(new Vector(xV, yV));
                    u.setPosition(new Vector(xU, yU));
                }
            }
        }
    }

    private void normalizeEdgeIntersection() {
        int bestAmount = calculateEdgeIntersections();
        for (Edge edge1 : edges) {
            for (Edge edge2 : edges) {
                if (edge1 == edge2 ||
                        edge1.getV().equals(edge2.getV()) ||
                        edge1.getV().equals(edge2.getU()) ||
                        edge1.getU().equals(edge2.getV()) ||
                        edge1.getU().equals(edge2.getU())) {
                    continue;
                }

                if (isEdgesIntersection(edge1, edge2)) {
                    Vertex v1 = vertices.get(edge1.getV());
                    Vertex u1 = vertices.get(edge1.getU());
                    Vertex v2 = vertices.get(edge2.getV());
                    Vertex u2 = vertices.get(edge2.getU());

                    Vector v1_pos = v1.getPosition();
                    Vector u1_pos = u1.getPosition();
                    Vector v2_pos = v2.getPosition();
                    Vector u2_pos = u2.getPosition();

                    int bestCase = 0;

                    Vertex v1_case1 = new Vertex(v2_pos.getX(), v2_pos.getY(), v1.getName(), v1.getValue());
                    Vertex v2_case1 = new Vertex(v1_pos.getX(), v1_pos.getY(), v2.getName(), v2.getValue());
                    swapVertices(edge1.getV(), v1_case1, edge2.getV(), v2_case1);
                    int newCase = calculateEdgeIntersections();
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 1;
                    }
                    swapVertices(edge1.getV(), v1, edge2.getV(), v2);

                    Vertex v2_case2 = new Vertex(u1_pos.getX(), u1_pos.getY(), v2.getName(), v2.getValue());
                    Vertex u1_case2 = new Vertex(v2_pos.getX(), v2_pos.getY(), u1.getName(), u1.getValue());
                    swapVertices(edge2.getV(), v2_case2, edge1.getU(), u1_case2);
                    newCase = calculateEdgeIntersections();
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 2;
                    }
                    swapVertices(edge2.getV(), v2, edge1.getU(), u1);

                    Vertex u1_case3 = new Vertex(u2_pos.getX(), u2_pos.getY(), u1.getName(), u1.getValue());
                    Vertex u2_case3 = new Vertex(u1_pos.getX(), u1_pos.getY(), u2.getName(), u2.getValue());
                    swapVertices(edge1.getU(), u1_case3, edge2.getU(), u2_case3);
                    newCase = calculateEdgeIntersections();
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 3;
                    }
                    swapVertices(edge1.getU(), u1, edge2.getU(), u2);

                    Vertex u2_case4 = new Vertex(v1_pos.getX(), v1_pos.getY(), u2.getName(), u2.getValue());
                    Vertex v1_case4 = new Vertex(u2_pos.getX(), u2_pos.getY(), v1.getName(), v1.getValue());
                    swapVertices(edge2.getU(), u2_case4, edge1.getV(), v1_case4);
                    newCase = calculateEdgeIntersections();
                    if (newCase < bestAmount) {
                        bestAmount = newCase;
                        bestCase = 4;
                    }
                    swapVertices(edge2.getU(), u2, edge1.getV(), v1);


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
    }

    private void swapVertices(String key1, Vertex v1, String key2, Vertex v2) {
        vertices.put(key1, v1);
        vertices.put(key2, v2);
    }

    private int calculateEdgeIntersections() {
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
    }

    private void calculateNewPosition(Vertex vertex) {
        Vector displacement = vertex.getDisplacement();
        double distance = displacement.getLength();

        double multiplier = Math.min(distance, t)/* / vectorLength*/;
        Vector dividedVector = displacement.getDivision(distance);
        Vector multipliedVector = dividedVector.getComposition(multiplier);
        Vector newPosition = vertex.getPosition().getSum(multipliedVector);

        double newX = Math.min((double) Config.WIDTH / 2, Math.max(-1.0 * Config.WIDTH / 2.0, newPosition.getX()));
        double newY = Math.min((double) Config.HEIGHT / 2, Math.max(-1.0 * Config.HEIGHT / 2.0, newPosition.getY()));
        vertices.get(vertex.getName()).setPosition(new Vector(newX, newY));
    }

    private void calculateAttractiveForces(String v, String u) {
        Vector positionV = vertices.get(v).getPosition();
        Vector positionU = vertices.get(u).getPosition();

        Vector vectorDifference = positionV.getDifference(positionU);
        double distance = vectorDifference.getLength();
        double attractionValue = attraction(distance) /*+ Config.VERTEX_DIAMETER*/;

        Vector dividedVector = vectorDifference.getDivision(distance);

        Vector multipliedVector = dividedVector.getComposition(attractionValue);

        Vector displacementV = vertices.get(v).getDisplacement();
        vertices.get(v).setDisplacement(displacementV.getDifference(multipliedVector));

        Vector displacementU = vertices.get(u).getDisplacement();
        vertices.get(u).setDisplacement(displacementU.getSum(multipliedVector));
    }

    public void calculateRepulsiveForces(Vertex v, Vertex u) {
        Vector vectorDifference = v.getPosition().getDifference(u.getPosition());
        double distance = vectorDifference.getLength();
        double repulsionValue = repulsion(distance);

        Vector dividedVector = vectorDifference.getDivision(distance);
        Vector multipliedVector = dividedVector.getComposition(repulsionValue);

        vertices.get(v.getName()).setDisplacement(v.getDisplacement().getSum(multipliedVector));
    }

    /*private Vector calculateVectorDivision(Vector vector, double divider) {
        return new Vector(vector.getX() / divider, vector.getY() / divider);
    }*/

    /*private Vector multiplyVector(Vector vector, double multiplier) {
        return new Vector(vector.getX() * multiplier, vector.getY() * multiplier);
    }*/

    /*private double calculateVectorLength(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        return Math.sqrt(x * x + y * y);
    }*/

    /*private Vector calculateVectorDifference(Vector v, Vector u) {
        return new Vector(v.getX() - u.getX(),
                v.getY() - u.getY());
    }*/

    /*private Vector calculateVectorSum(Vector v, Vector u) {
        return new Vector(v.getX() + u.getX(),
                v.getY() + u.getY());
    }*/

    private Couple<Boolean, Vector> isVertexEdgeIntersection(Vertex vertex, Edge edge) {
        double x1 = vertices.get(edge.getV()).getPosition().getX();
        double y1 = vertices.get(edge.getV()).getPosition().getY();
        double x2 = vertices.get(edge.getU()).getPosition().getX();
        double y2 = vertices.get(edge.getU()).getPosition().getY();
        double x3 = vertex.getPosition().getX();
        double y3 = vertex.getPosition().getY();

        double x4 = ((x2 - x1) * (y2 - y1) * (y3 - y1) + x1 * Math.pow(y2 - y1, 2) + x3 * Math.pow(x2 - x1, 2)) / (Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
        double y4 = (y2 - y1) * (x4 - x1) / (x2 - x1) + y1;

        Vector vector = new Vector(x3 - x4, y3 - y4);
        double distance = vector.getLength();
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        if (distance <= Config.VERTEX_RADIUS &&
                (x3 + Config.VERTEX_RADIUS >= minX) &&
                (x3 - Config.VERTEX_RADIUS <= maxX) &&
                (y3 + Config.VERTEX_RADIUS >= minY) &&
                (y3 - Config.VERTEX_RADIUS <= maxY)) {

            return new Couple<Boolean, Vector>(true, vector);
        }

        return new Couple<Boolean, Vector>(false, null);
    }

    /*private boolean isVertexEdgeIntersection(Vertex vertex, Edge edge) {
        double x1 = vertices.get(edge.getV()).getPosition().getX() - vertex.getPosition().getX();
        double y1 = vertices.get(edge.getV()).getPosition().getY() - vertex.getPosition().getY();
        double x2 = vertices.get(edge.getU()).getPosition().getX() - vertex.getPosition().getX();
        double y2 = vertices.get(edge.getU()).getPosition().getY() - vertex.getPosition().getY();

        double dx = x2 - x1;
        double dy = y2 - y1;

        double a = dx * dx + dy * dy;
        double b = 2 * (x1 * dx + y1 * dy);
        double c = x1 * x1 + y1 * y1 - Config.VERTEX_RADIUS * Config.VERTEX_RADIUS;

//        double x1 = vertices.get(edge.getV()).getPosition().getX();
//        double y1 = vertices.get(edge.getV()).getPosition().getY();
//        double x2 = vertices.get(edge.getU()).getPosition().getX();
//        double y2 = vertices.get(edge.getU()).getPosition().getY();
//        double x3 = vertex.getPosition().getX();
//        double y3 = vertex.getPosition().getY();
//
//        double a = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
//        double b = 2 * ((x2 - x1) * (x1 - x3) + (y2 - y1) * (y1 - y3));
//        double c = x3 * x3 + y3 * y3 + x1 * x1 + y1 * y1 - 2 * (x3 * x1 + y3 * y1) - Config.VERTEX_DIAMETER * Config.VERTEX_DIAMETER;

        if (-b < 0) {
            return c < 0;
        } else if (-b < 2 * a) {
            return 4 * a * c - b * b < 0;
        } else {
            return a + b + c < 0;
        }
    }*/

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
        double f = x * x / k;
        return (x - Config.AVERAGE_EDGE_LENGTH > 0 ? f : -f);
    }

    public double repulsion(double x) {
        double f = k * k / x;
        return (x - Config.AVERAGE_EDGE_LENGTH > 0 ? -f : f);
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