import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;


public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        // Generate a new file and then read it back in
        makeTestData(25000);
        File inFile = new File("250000points.txt");
        Scanner in = new Scanner(inFile);
        int pointCount = Integer.parseInt(in.nextLine());
        Point[] points = new Point[pointCount];
        for (int i = 0; i < pointCount; i++) {
            points[i] = new Point(in.nextLine());
        }

        // Benchmarks for both algorithms:

        long divideStart = System.nanoTime();

        // Sort points via their x location
        Arrays.sort(points, Comparator.comparing(Point::getX));

        // This array is the indexes of the points, sorted by y value
        int[] indexesSortedByY = IntStream.range(0, points.length)
                .boxed()
                .sorted(Comparator.comparingDouble(z -> points[z].getY()))
                .mapToInt(ele -> ele)
                .toArray();
        // .range() creates a stream of [0, 1, 2, ..., points.length]
        // .boxed() changes them from int primitives to Integers for comparator
        // .sorted() sorts them according to the y axis value when they are
        // used as indexes into the points array
        //.mapToInt() to convert back to ints and .toArray() to return as array

        Point[] divWinners = divideAndConquer(points, 0, points.length - 1, indexesSortedByY);
        long divideEnd = System.nanoTime();
        System.out.println("Divide and Conquer took " + (divideEnd - divideStart) / 1_000_000_000.0 + " seconds for " + points.length + " points");
        System.out.println(divWinners[0].getX() + ", " + divWinners[0].getY() + " and "
                + divWinners[1].getX() + ", " + divWinners[1].getY() + " are closest with a" +
                " distance of " + distance(divWinners[0], divWinners[1]));

        System.out.println("-------------------------------------------------");

        long bruteStart = System.nanoTime();
        Point[] bruteWinners = bruteForce(points, 0, points.length - 1);
        long bruteEnd = System.nanoTime();
        System.out.println("Brute Force took " + (bruteEnd - bruteStart)/ 1_000_000_000.0 + " seconds for " + points.length + " points");
        System.out.println(bruteWinners[0].getX() + ", " + bruteWinners[0].getY() + " and "
                + bruteWinners[1].getX() + ", " + bruteWinners[1].getY() + " are closest with a" +
                " distance of " + distance(bruteWinners[0], bruteWinners[1]));

    }

    /**
     * divideAndConquer
     * Receives our array of Point objects, pre-sorted by x location.
     * Receives s and e, the start and end indexes for what subset of Point
     * objects in our array we are considering on this particular call
     * Lastly receives yIndexes, a list of indexes corresponding to points,
     * pre-sorted by Y location.
     *
     * Returns an array of two Point objects; the two that have the
     * shortest path between them.
     */
    static Point[] divideAndConquer(Point[] points, int s, int e, int[] yIndexes) {

        // Our subset contains only two points, so return them.
        if (e - s == 1) {
            return new Point[]{points[s], points[e]};
        }
        // Our subset contains three points, so call brute force.
        else if (e - s == 2) {
            // p. 1040, 2nd paragraph: "A given recursive invocation...first
            // checks whether |P| <= 3. If so, the invocation simply performs
            // the brute-force method..."
            return bruteForce(points, s, e);
        }

        // If we reach this point our subset contains more than 3 points.
        // So now we have to divide it into two pieces and perform some
        // recursive calls.

        // Calculates the midpoint of our subset (lower mid if size is even).
        int middleIndex = s + ((e - s) / 2);

        // Book says to make the left partition have the extra element
        // if the list cannot be split perfectly in half (p.1040)
        int[] leftYIndexes = new int[middleIndex - s + 1];
        int[] rightYIndexes = new int[e - middleIndex];
        int lCounter = 0, rCounter = 0;

        // Each yIndex is a reference to a location in the points array, so
        // if that index is lower or equal to the middle index, we put it in
        // the left set of Y indexes. Otherwise it goes to the right.
        for (int yIndex : yIndexes) {
            if (yIndex <= middleIndex) {
                leftYIndexes[lCounter] = yIndex;
                lCounter++;
            } else {
                rightYIndexes[rCounter] = yIndex;
                rCounter++;
            }
        }

        // Recurse into left subset in range [start, midpoint]
        Point[] left = divideAndConquer(points, s, middleIndex, leftYIndexes);
        // Recurse into right subset in range [(midpoint + 1), end]
        Point[] right = divideAndConquer(points, middleIndex + 1, e, rightYIndexes);

        // New array to hold whichever set of points is shorter
        Point[] shortestReturned;

        // If left is shorter than right, left wins and is stored.
        if (distance(left[0], left[1]) < distance(right[0], right[1])) {
            shortestReturned = left;
        }
        // Otherwise if right is shorter than left, right wins and is stored.
        else {
            shortestReturned = right;
        }

        // Our delta is now the distance between these two points.
        double delta = distance(shortestReturned[0], shortestReturned[1]);

        // Get the x location of the middle element (location of the vertical
        // line) so we can compare in the following loop.
        double lineXLocation = points[middleIndex].getX();

        // Don't know how many points will be within delta of the dividing
        // line, so we use an ArrayList
        ArrayList<Integer> withinDelta = new ArrayList<>();

        // yIndexes holds reference to all the points within the left and
        // right subsets that were just returned
        // Importantly, yIndexes is also sorted from bottom to top on the y
        // axis, meaning that the left set and right set will also be ordered
        // this way as we are appending to the end of the arrayList
        for (int yIndex : yIndexes) {

            // If the absolute difference of the point and the line is less than
            // delta then it's a point we should consider
            if (Math.abs(points[yIndex].getX() - lineXLocation) <= delta) {
                withinDelta.add(yIndex);
            }
        }

        // Now that we have a group of potential points we can compare each
        // of them to the next seven.
        double shortestSoFar = Double.POSITIVE_INFINITY;
        Point shortestA = null, shortestB = null;



        // Important to remember that this arrayList of points is sorted from
        // bottom to top on the Y axis
        // We have to compare each point with the next seven
        for (int i = 0; i < withinDelta.size(); i++) {

            // Loop over the next seven (indexes 1-7) or until you reach the
            // end of the list
            for (int j = (i + 1); j <= (i + 7) && j < withinDelta.size(); j++) {
                double currentDistance = distance(points[withinDelta.get(i)], points[withinDelta.get(j)]);

                // If it's shorter, we store it and it's points.
                if (currentDistance < shortestSoFar) {
                    shortestSoFar = currentDistance;
                    shortestA = points[withinDelta.get(i)];
                    shortestB = points[withinDelta.get(j)];
                }
            }
        }
        // If the shortest we found in that previous set of loops is shorter
        // than delta, we've found an even shorter path, so return it.
        if (shortestSoFar < delta) {
            return new Point[]{shortestA, shortestB};
        }
        // Otherwise we had the shortest path the first time, so return it.
        else {
            return new Point[]{shortestReturned[0], shortestReturned[1]};
        }
    }

    /**
     * bruteForce
     * Takes an array of Point objects, a start index, an end index, and
     * returns an array of the two Point objects that are closest to each other.
     *
     * @param s starting position, inclusive
     * @param e ending position, inclusive
     * @param points array of Point objects
     */
    static Point[] bruteForce(Point[] points, int s, int e) {
        double shortestSoFar = Double.POSITIVE_INFINITY;
        Point shortestA = null, shortestB = null;

        // From s up to and including e
        for (int i = s; i <= e; i++) {

            // From s up to and including e again
            for (int j = s; j <= e; j++) {

                // If we aren't comparing the same points to each other.
                if (!(i == j)){

                    // Calculate distance
                    double currentDistance = distance(points[i], points[j]);

                    // If it's shorter, keep track of it and it's points.
                    if (currentDistance < shortestSoFar){
                        shortestSoFar = currentDistance;
                        shortestA = points[i];
                        shortestB = points[j];
                    }
                }
            }
        }
        return new Point[]{shortestA, shortestB};
    }


    // Distance between two points in 2D Euclidean space
    static double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    /**
     * Creates a random set of input data and writes it to a file
     * @param lineCount The number of x,y coordinate pairs are written to the file
     * @throws FileNotFoundException
     */
    static void makeTestData (int lineCount) throws FileNotFoundException {
        File outputPairs = new File(lineCount + "points.txt");
        PrintWriter pw = new PrintWriter(outputPairs);
        pw.println(lineCount);
        for (int i = 0; i < lineCount; i++) {
            pw.print(ThreadLocalRandom.current().nextDouble(50000));
            pw.print(" ");
            pw.println(ThreadLocalRandom.current().nextDouble(50000));
        }
        pw.close();
    }
}
