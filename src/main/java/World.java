public class World {
    final private Firefly[][] grid;
    final private int rows, cols;

    public World(int rows, int cols, double frequency, double couplingConstant, double adjustmentFrequency) {
        this.rows = rows;
        this.cols = cols;
        grid = new Firefly[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Firefly(frequency, couplingConstant, adjustmentFrequency,this, i, j);
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
