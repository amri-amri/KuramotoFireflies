/**
 * This class represents a collection of fireflies.
 * The fireflies are arranged in a torus.
 */
public class World {

    final private Firefly[][] grid; // grid containing all fireflies
    final private int rows, cols; // amount of rows and columns

    /**
     * Creates a world and instantiates the fireflies.
     *
     * @param rows amount of rows in the torus
     * @param cols amount of columns in the torus
     */
    public World(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Firefly[rows][cols];


        // create fireflies
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = new Firefly();
            }
        }
        // init fireflies
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Firefly[] neighbors = new Firefly[]{
                        grid[(y - 1 + grid.length) % grid.length][x],
                        grid[(y + 1 + grid.length) % grid.length][x],
                        grid[y][(x - 1 + grid[0].length) % grid[0].length],
                        grid[y][(x + 1 + grid[0].length) % grid[0].length],
                };
                grid[y][x].init(neighbors);
            }
        }

    }

    /**
     * Starts the firefly threads.
     */
    public void startSimulation() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x].start();
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
