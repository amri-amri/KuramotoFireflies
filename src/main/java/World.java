/**
 * This class represents a collection of fireflies.
 * The fireflies are arranged in a torus.
 */
public class World {

    final private Firefly[][] grid; // grid containing all fireflies
    final private int rows, cols; // amount of rows and columns

    /**
     * Creates a world and instantiates the fireflies.
     * @param rows amount of rows in the torus
     * @param cols amount of columns in the torus
     * @param frequency flashing frequency of all fireflies
     * @param couplingConstant the coupling constant that determines the strength of synchronisation
     * @param adjustmentFrequency the frequency of phase shift adjustments per second
     */
    public World(int rows, int cols, double frequency, double couplingConstant, double adjustmentFrequency) {
        this.rows = rows;
        this.cols = cols;
        grid = new Firefly[rows][cols];

        // create fireflies
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Firefly(frequency, couplingConstant, adjustmentFrequency, this, i, j);
            }
        }
    }

    /**
     * Starts the firefly threads.
     */
    public void startSimulation() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j].start();
            }
        }
    }

    /**
     * @return 2-dim. array of fireflies
     */
    public Firefly[][] getGrid() {
        return grid;
    }
}
