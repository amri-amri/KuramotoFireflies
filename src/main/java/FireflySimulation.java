import javax.swing.*;
import java.awt.*;

public class FireflySimulation extends JFrame {
    private World world;
    private int rows, cols;

    public FireflySimulation(int rows, int cols, double frequency, double couplingConstant, double adjustmentFrequency) {
        this.rows = rows;
        this.cols = cols;
        world = new World(rows, cols, frequency, couplingConstant, adjustmentFrequency);
        setTitle("Firefly Synchronization Simulation");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        world.startSimulation();
    }

    @Override
    public void paint(Graphics g) {
        Firefly[][] world = this.world.getGrid();
        int cellWidth = getWidth() / cols;
        int cellHeight = getHeight() / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
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

        repaint(); // Aktualisiert die Anzeige
    }

    public static void main(String[] args) {
        new FireflySimulation(100, 100, 0.5, 0.1, 1);
    }
}
