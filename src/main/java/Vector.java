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
}
