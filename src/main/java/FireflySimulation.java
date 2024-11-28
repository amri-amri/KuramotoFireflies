import javax.swing.*;
import java.awt.*;

/**
 * This class handles the visualisation of the world containing the fireflies.
 * The torus is visualised as a rectangular grid.
 */
public class FireflySimulation extends JFrame {

    final private World world;
    final private int rows, cols;

    /**
     * Creates a world and its visualisation and starts the simulation.
     *
     * @param rows amount of rows in the torus
     * @param cols amount of columns in the torus
     */
    public FireflySimulation(int rows, int cols) {

        // model
        this.rows = rows;
        this.cols = cols;
        world = new World(rows, cols);

        // view
        setTitle("Firefly Synchronization Simulation");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // start simulation
        world.startSimulation();
    }

    @Override
    public void paint(Graphics g) {
        Firefly[][] world = this.world.getGrid();

        // the grid cell sizes adjust according to the window size
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // set the fireflies color
                Firefly firefly = world[i][j];
                g.setColor(firefly.isFlashing() ? Color.YELLOW : Color.DARK_GRAY);
                g.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
            }
        }

        try {
            Thread.sleep(10); // Update-Rate
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // refresh the visualisation
        repaint();
    }

    public static void main(String[] args) {
        new FireflySimulation(30, 30);
    }
}
