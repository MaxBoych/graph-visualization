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

        /*if (Config.WIDTH > 1280) {
            Config.WIDTH = 1280;
        }
        if (Config.HEIGHT > 720) {
            Config.HEIGHT = 720;
        }*/
    }

    private void calculateCoefficients() {
        k = Math.sqrt(Config.WIDTH * Config.HEIGHT / (double) vertices.size());
        t = (double) Config.WIDTH / 10;
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
    }

    //@SuppressWarnings("StatementWithEmptyBody")
    public void drawingStart() {

        //int amountEdgeIntersections;
        forceDirectedAlgorithm(Config.ITERATION_AMOUNT);


        /*for (int i = 0; i < 100; i++) {
            if (!checkVertexEdgeIntersections()) {
                break;
            }
        }*/

        /*for (int i = 0; i < 100; i++) {
            if (checkEdgeIntersection()) {
                break;
            }
        }*/
        //forceDirectedAlgorithm(15);


        /*do {

            //forceDirectedAlgorithm(Config.ITERATION_AMOUNT);


            //while (checkVertexEdgeIntersections()) ;

            for (int i = 0; i < 100; i++) {
                if (!checkVertexEdgeIntersections()) {
                    break;
                }
            }

            for (int i = 0; i < 100; i++) {
                if (checkEdgeIntersection()) {
                    break;
                }
            }
            //amountEdgeIntersections = calculateEdgeIntersections();

//            if (simpleCheckVertexEdgeIntersections()) {
//                continue;
//            }

            for (Vertex v : vertices.values()) {
                calculateNewPosition(v);
            }

        } while (!edgeNormalization());*/

        //forceDirectedAlgorithm(Config.ITERATION_AMOUNT);

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
        double averageLength = Config.AVERAGE_EDGE_LENGTH; //calculateAverageEdgeLength();
        for (Edge edge : edges) {
            Vertex v = vertices.get(edge.getV());
            Vertex u = vertices.get(edge.getU());

            double dx = v.getPosition().getX() - u.getPosition().getX();
            double dy = v.getPosition().getY() - u.getPosition().getY();
            double distance = calculateVectorLength(new Vector(dx, dy));
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

    private boolean edgeNormalization() {
        double averageLength = Config.AVERAGE_EDGE_LENGTH; //calculateAverageEdgeLength();
        for (Edge edge : edges) {
            Vertex v = vertices.get(edge.getV());
            Vertex u = vertices.get(edge.getU());
            Vector vector = new Vector(
                    u.getPosition().getX() - v.getPosition().getX(),
                    u.getPosition().getY() - v.getPosition().getY()
            );

            double vectorLength = calculateVectorLength(vector);
            double difference = averageLength - vectorLength;
            if (Math.abs(difference) > 0.25 * averageLength) {
                Random random = new Random();
                double newLength = averageLength * 0.75 + averageLength * 0.5 * random.nextDouble();
                double newDifference = Math.abs(newLength - vectorLength);

                double lambda;
                double newX;
                double newY;
                //double angle = 360 * random.nextDouble() * Math.PI / 180;
                if (random.nextBoolean()) {
                    if (difference > 0) {
                        lambda = newDifference / vectorLength;
                        newX = v.getPosition().getX() * (1 + lambda) - u.getPosition().getX() * lambda;
                        newY = v.getPosition().getY() * (1 + lambda) - u.getPosition().getY() * lambda;
                    } else {
                        lambda = newDifference / (vectorLength - newDifference);
                        newX = (v.getPosition().getX() + u.getPosition().getX() * lambda) / (1 + lambda);
                        newY = (v.getPosition().getY() + u.getPosition().getY() * lambda) / (1 + lambda);
                    }
                    vertices.get(edge.getV()).setPosition(new Vector(newX /*+ 40 * Math.sin(angle)*/, newY /*+ 40 * Math.cos(angle)*/));
                } else {
                    if (difference > 0) {
                        lambda = vectorLength / newDifference;
                        newX = (u.getPosition().getX() * (1 + lambda) - v.getPosition().getX()) / lambda;
                        newY = (u.getPosition().getY() * (1 + lambda) - v.getPosition().getY()) / lambda;
                    } else {
                        lambda = (vectorLength - newDifference) / newDifference;
                        newX = (v.getPosition().getX() + u.getPosition().getX() * lambda) / (1 + lambda);
                        newY = (v.getPosition().getY() + u.getPosition().getY() * lambda) / (1 + lambda);
                    }
                    vertices.get(edge.getU()).setPosition(new Vector(newX /*+ 40 * Math.sin(angle)*/, newY /*+ 40 * Math.cos(angle)*/));
                }

                /*if (Double.isNaN(newX)) {
                    System.out.println("PANIC for X in edgeNormalization!");
                }

                if (Double.isNaN(newY)) {
                    System.out.println("PANIC for Y in edgeNormalization!");
                }*/

                System.out.println(newX + " " + newY);
                System.out.println(lambda);
                System.out.println();

                return false;
            }
        }

        return true;
    }

    private double calculateAverageEdgeLength() {
        double averageLength = 0;
        for (Edge edge : edges) {
            Vertex v = vertices.get(edge.getV());
            Vertex u = vertices.get(edge.getU());
            Vector vector = new Vector(
                    u.getPosition().getX() - v.getPosition().getX(),
                    u.getPosition().getY() - v.getPosition().getY()
            );
            averageLength += calculateVectorLength(vector);
        }
        averageLength /= edges.size();

        return averageLength;
    }

    private boolean simpleCheckVertexEdgeIntersections() {
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
                double distance = calculateVectorLength(new Vector(dx, dy));
                if (distance < 2 * Config.VERTEX_DIAMETER) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkVertexEdgeIntersections() {
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
                    //System.out.println(vertex.getName() + " :   " + edge.getV() + " " + edge.getU());
                    //double angle = Math.atan2(y2 - y1, x2 - x1);
                    //double sin = Math.sin(angle);
                    //double cos = Math.cos(angle);

                    Random random = new Random();
                    double newX = vertex.getPosition().getX() + (Config.VERTEX_DIAMETER) * (1 + Math.random()) * (random.nextBoolean() ? 1 : -1);
                    double newY = vertex.getPosition().getY() + (Config.VERTEX_DIAMETER) * (1 + Math.random()) * (random.nextBoolean() ? 1 : -1);
                    Vector newPosition = new Vector(newX, newY);
                    vertex.setPosition(newPosition);
                    wasIntersection = true;

                    /*if (Double.isNaN(newX)) {
                        System.out.println("PANIC for X in checkVertexEdgeIntersections!");
                    }

                    if (Double.isNaN(newY)) {
                        System.out.println("PANIC for Y in checkVertexEdgeIntersections!");
                    }*/
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

        /*if (wasIntersection) {
            //forceDirectedAlgorithm(10);
            calculateCoefficients();
            findNegativePositions();
            adjustPositions();
            checkIntersections();
        }*/
    }

    private void normalizeVertexEdgeIntersection() {
        for (Vertex vertex : vertices.values()) {
            for (Edge edge : edges) {
                if (vertex.getName().equals(edge.getV()) || vertex.getName().equals(edge.getU())) {
                    continue;
                }

                Vertex v = vertices.get(edge.getV());
                Vertex u = vertices.get(edge.getU());

                //System.out.println("HERE");

                Couple<Boolean, Vector> couple = isVertexEdgeIntersection(vertex, edge);
                //System.out.println(couple.getFirst());
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
                double distance = calculateVectorLength(new Vector(dx, dy));

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

    private boolean normalizeEdgeIntersection() {
        boolean wasNotIntersection = true;
        int bestAmount = calculateEdgeIntersections();
        //System.out.println("start: " + bestAmount);
        for (Edge edge1 : edges) {
            for (Edge edge2 : edges) {
                if (edge1 == edge2 ||
                        edge1.getV().equals(edge2.getV()) ||
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
                    int newCase = calculateEdgeIntersections();
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
                    newCase = calculateEdgeIntersections();
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
                    newCase = calculateEdgeIntersections();
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
                    newCase = calculateEdgeIntersections();
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

        //System.out.println("end: " + bestAmount);
        return wasNotIntersection;
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
        //System.out.println(t);
    }

    private void calculateNewPosition(Vertex vertex) {
        Vector displacement = vertex.getDisplacement();
        double vectorLength = calculateVectorLength(displacement);

        double multiplier = Math.min(vectorLength, t)/* / vectorLength*/;
        Vector dividedVector = calculateVectorDivision(displacement, vectorLength);
        Vector multipliedVector = multiplyVector(dividedVector, multiplier);
        Vector newPosition = calculateVectorSum(vertex.getPosition(), multipliedVector);

        double newX = Math.min((double) Config.WIDTH / 2, Math.max(-1.0 * Config.WIDTH / 2.0, newPosition.getX()));
        double newY = Math.min((double) Config.HEIGHT / 2, Math.max(-1.0 * Config.HEIGHT / 2.0, newPosition.getY()));
        vertices.get(vertex.getName()).setPosition(new Vector(newX, newY));

        /*if (Double.isNaN(newX)) {
            System.out.println("PANIC for X in calculateNewPosition!");
        }

        if (Double.isNaN(newY)) {
            System.out.println("PANIC for Y in calculateNewPosition!");
        }*/
    }

    private void calculateAttractiveForces(String v, String u) {
        Vector positionV = vertices.get(v).getPosition();
        Vector positionU = vertices.get(u).getPosition();

        Vector vectorDifference = calculateVectorDifference(positionV, positionU);
        double vectorLength = calculateVectorLength(vectorDifference) /*+ Config.VERTEX_DIAMETER*/;
        double attractionValue = attraction(vectorLength) /*+ Config.VERTEX_DIAMETER*/;

        Vector dividedVector = calculateVectorDivision(vectorDifference, vectorLength);

        //double multiplier = attractionValue / vectorLength;
        Vector multipliedVector = multiplyVector(dividedVector, attractionValue);

        Vector displacementV = vertices.get(v).getDisplacement();
        vertices.get(v).setDisplacement(calculateVectorDifference(displacementV, multipliedVector));

        Vector displacementU = vertices.get(u).getDisplacement();
        vertices.get(u).setDisplacement(calculateVectorSum(displacementU, multipliedVector));
    }

    public void calculateRepulsiveForces(Vertex v, Vertex u) {
        Vector vectorDifference = calculateVectorDifference(v.getPosition(), u.getPosition());
//        System.out.println("position V: " + v.getName() + " " + v.getPosition().getX() + " " + v.getPosition().getY());
//        System.out.println("position U: " + u.getName() + " " + u.getPosition().getX() + " " + u.getPosition().getY());
//        System.out.println("difference: " + vectorDifference.getX() + " " + vectorDifference.getY());
        double vectorLength = calculateVectorLength(vectorDifference);
        double repulsionValue = repulsion(vectorLength);

        Vector dividedVector = calculateVectorDivision(vectorDifference, vectorLength);
        //double multiplier = repulsionValue / vectorLength;
        //System.out.println("repulsion: " + repulsionValue + "   length: " + vectorLength);
        Vector multipliedVector = multiplyVector(dividedVector, repulsionValue);

        vertices.get(v.getName()).setDisplacement(calculateVectorSum(v.getDisplacement(), multipliedVector));

//        if (Double.isNaN(calculateVectorSum(v.getDisplacement(), multipliedVector).getX())) {
//            System.out.println("PANIC for X in calculateRepulsiveForces!");
//            System.exit(0);
//        }
//
//        if (Double.isNaN(calculateVectorSum(v.getDisplacement(), multipliedVector).getY())) {
//            System.out.println("PANIC for Y in calculateRepulsiveForces!");
//        }
    }

    private Vector calculateVectorDivision(Vector vector, double divider) {
        return new Vector(vector.getX() / divider, vector.getY() / divider);
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
        double distance = calculateVectorLength(vector);
        //System.out.println(distance);
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        if (distance <= Config.VERTEX_RADIUS &&
                (x3 + Config.VERTEX_RADIUS >= minX) &&
                (x3 - Config.VERTEX_RADIUS <= maxX) &&
                (y3 + Config.VERTEX_RADIUS >= minY) &&
                (y3 - Config.VERTEX_RADIUS <= maxY)) {

            //System.out.println("INTERSECTION");

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
        double f = x * x / k;
        return (x - Config.AVERAGE_EDGE_LENGTH > 0 ? f : -f);
        //return k * x;
        //return (x - Config.AVERAGE_EDGE_LENGTH) * (x - Config.AVERAGE_EDGE_LENGTH) / k;
    }

    public double repulsion(double x) {
        double f = k * k / x;
        return (x - Config.AVERAGE_EDGE_LENGTH > 0 ? -f : f);
        //return k * k / x * x;
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