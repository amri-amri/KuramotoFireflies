public class World {
    private Firefly[][] grid;
    private int rows, cols;

    public World(int rows, int cols, double frequency, double couplingConstant) {
        this.rows = rows;
        this.cols = cols;
        grid = new Firefly[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Firefly(frequency, couplingConstant, this, i, j);
            }
        }
    }

    public void startSimulation() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j].start();
            }
        }
    }

    public Firefly[][] getGrid() {
        return grid;
    }
}
