import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Map;

public class VisualizationJSON extends JPanel {

    private static Graph graph;

    public static void main(String[] args) {

        String path = args[0];
        FileJSON fileJSON = new FileJSON(path);
        fileJSON.parse();
        Map<String, Vertex> vertices = fileJSON.getVertices();
        List<Edge> edges = fileJSON.getEdges();
        graph = new Graph(vertices, edges);
        graph.drawingStart();

        //int width = vertices.size() *
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame jFrame = new JFrame(Config.APP_NAME);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setBackground(Color.WHITE);
        jFrame.setSize(Config.WIDTH, Config.HEIGHT);

        VisualizationJSON panel = new VisualizationJSON();
        jFrame.add(panel);
        jFrame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Map<String, Vertex> vertices = graph.getVertices();
        List<Edge> edges = graph.getEdges();

        Graphics2D graphics2D = (Graphics2D) graphics;
        AffineTransform defaultTransform = graphics2D.getTransform();
        for (Edge edge : edges) {
            double xV = vertices.get(edge.getV()).getPosition().getX();
            double yV = vertices.get(edge.getV()).getPosition().getY();
            double xU = vertices.get(edge.getU()).getPosition().getX();
            double yU = vertices.get(edge.getU()).getPosition().getY();

            drawEdgeLine(graphics2D, xV, yV, xU, yU);
            graphics2D.setTransform(defaultTransform);

            drawEdgeValue(graphics2D, edge.getValue(), xV, yV, xU, yU);
            graphics2D.setTransform(defaultTransform);
        }

        for (Vertex vertex : vertices.values()) {
            drawVertexCircle(graphics2D, vertex);
            graphics2D.setTransform(defaultTransform);

            drawVertexNameAndValue(graphics2D, vertex);
            graphics2D.setTransform(defaultTransform);
        }
    }

    private void drawVertexCircle(Graphics2D graphics, Vertex vertex) {
        Ellipse2D ellipse = new Ellipse2D.Double(
                vertex.getPosition().getX() - Config.VERTEX_RADIUS,
                vertex.getPosition().getY() - Config.VERTEX_RADIUS,
                Config.VERTEX_DIAMETER,
                Config.VERTEX_DIAMETER
        );
        graphics.setPaint(Color.PINK);
        graphics.fill(ellipse);
        graphics.draw(ellipse);
    }

    private void drawVertexNameAndValue(Graphics2D graphics, Vertex vertex) {
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font(null, Font.BOLD, 14));
        graphics.drawString(
                vertex.getName() + ": " + vertex.getValue(),
                (float) (vertex.getPosition().getX() - Config.VERTEX_RADIUS + Config.VERTEX_DIAMETER / 2),
                (float) (vertex.getPosition().getY() - Config.VERTEX_RADIUS + Config.VERTEX_DIAMETER / 2)
        );
    }

    private void drawEdgeValue(Graphics2D graphics, String edgeValue,
                               double x1, double y1, double x2, double y2) {
        double centerX = x1 + (x2 - x1) / 2;
        double centerY = y1 + (y2 - y1) / 2;

        double angleDegrees = Math.toDegrees(Math.atan2(centerY - y2, centerX - x2) + Math.PI);
        if (angleDegrees > 90 && angleDegrees < 270) {
            angleDegrees += 180;
        }
        double angleRadians = Math.toRadians(angleDegrees);

        Font font = new Font(null, Font.BOLD, 12);
        FontMetrics metrics = graphics.getFontMetrics();
        int stringWidth = metrics.stringWidth(edgeValue);

        graphics.setFont(font);
        graphics.rotate(angleRadians, centerX, centerY);
        graphics.setColor(Color.BLACK);
        graphics.drawString(edgeValue, (float) (centerX - stringWidth / 2), (float) (centerY - 10));
        graphics.rotate(-1.0 * angleRadians, centerX, centerY);
    }

    private void drawEdgeLine(Graphics2D graphics, double x1, double y1, double x2, double y2) {
        double angleFrom = angleBetweenTwoPoints(x1, y1, x2, y2);
        double angleTo = angleBetweenTwoPoints(x2, y2, x1, y1);

        Point2D pointFrom = getPointOnCircle(x1, y1, angleFrom, Config.VERTEX_RADIUS);
        Point2D pointTo = getPointOnCircle(x2, y2, angleTo, Config.VERTEX_RADIUS);

        graphics.setColor(Color.BLACK);
        graphics.draw(new Line2D.Double(pointFrom, pointTo));

        drawArrowHead(graphics, pointTo, angleFrom);
    }

    private void drawArrowHead(Graphics2D graphics, Point2D pointTo, double angleFrom) {
        ArrowHead arrowHead = new ArrowHead();
        AffineTransform transform = AffineTransform.getTranslateInstance(
                pointTo.getX() - (arrowHead.getBounds2D().getWidth() / 2),
                pointTo.getY()
        );
        transform.rotate(angleFrom, arrowHead.getBounds2D().getCenterX(), 0);
        arrowHead.transform(transform);
        graphics.draw(arrowHead);
    }

    private double angleBetweenTwoPoints(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        double rotation = -Math.atan2(dx, dy);
        rotation = Math.toRadians(Math.toDegrees(rotation) + 180);

        return rotation;
    }

    private Point2D getPointOnCircle(double x, double y, double radians, double radius) {
        radians -= Math.toRadians(90);
        double positionX = x + Math.cos(radians) * radius;
        double positionY = y + Math.sin(radians) * radius;

        return new Point2D.Double(positionX, positionY);
    }
}

class ArrowHead extends Path2D.Double {

    public ArrowHead() {
        double size = Config.ARROW_HEAD_SIZE;
        moveTo(0, size);
        lineTo(size / 2, 0);
        lineTo(size, size);
    }
}
