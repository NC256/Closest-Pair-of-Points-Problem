import java.util.Objects;

/**
 * Pair
 * Is used to hold the x and y values of an individual pair object
 * (point on our grid).
 */
class Point {
    private final double x, y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Point(String input) {

        // Splitting on whitespace
        String[] inputParse = input.split(" ");

        // Parse x and y from the first two elements
        x = Double.parseDouble(inputParse[0]);
        y = Double.parseDouble(inputParse[1]);

    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }


    @Override
    public boolean equals(Object that) {
        if (that == this) {
            return true;
        }
        if (!(that instanceof Point)) {
            return false;
        } else {
            Point thatPoint = (Point) that;

            // Shouldn't have to worry about NaN or infinities if input is clean
            return this.x == thatPoint.getX() && this.y == thatPoint.getY();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
