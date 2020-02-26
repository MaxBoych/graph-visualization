public class Vector {

    private double x, y;

    Vector() {
    }

    Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector getDifference(Vector vector) {
        return new Vector(x - vector.getX(),
                y - vector.getY());
    }

    public Vector getSum(Vector vector) {
        return new Vector(x + vector.getX(),
                y + vector.getY());
    }

    public Vector getComposition(double multiplier) {
        return new Vector(x * multiplier, y * multiplier);
    }

    public Vector getDivision(double divider) {
        return new Vector(x / divider, y / divider);
    }
}
