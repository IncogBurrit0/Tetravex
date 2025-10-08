import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents a single Tetravex tile with four colored (numbered) edges.
 */
class Tile {
    public final int id;
    private int top, right, bottom, left;
    private int rotation = 0; // 0=original, 1=90deg, 2=180deg, 3=270deg
    public TileComponent component; // Link to the visual component

    public Tile(int id, int top, int right, int bottom, int left) {
        this.id = id;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    /**
     * Rotates the tile 90 degrees clockwise by shifting the edge values.
     */
    public void rotate() {
        int temp = this.top;
        this.top = this.left;
        this.left = this.bottom;
        this.bottom = this.right;
        this.right = temp;
        this.rotation = (this.rotation + 1) % 4;
        if (component != null) {
            component.repaint(); // Redraw the rotated tile
        }
    }

    public int getTop() { return top; }
    public int getRight() { return right; }
    public int getBottom() { return bottom; }
    public int getLeft() { return left; }
    
    /**
     * Allows the TetravexGUI to reset the internal rotation counter when the 
     * tile is returned to the bank. This addresses the encapsulation error.
     * @param newRotation The new rotation counter value (should be 0).
     */
    public void setRotationCounter(int newRotation) {
        this.rotation = newRotation;
    }
}

/**
 * Custom JPanel component to visually represent a single Tetravex tile.
 * Handles the drawing of the four numbered, colored triangular quadrants.
 */
class TileComponent extends JPanel {
    private Tile tile;

    // Static color map for visual representation (Updated for 9 colors)
    private static final Color[] COLORS = {
        new Color(40, 40, 40),      // 0: Dark Gray (Unused/Background)
        new Color(255, 165, 0),     // 1: Orange
        new Color(192, 192, 192),   // 2: Light Gray
        new Color(0, 0, 128),       // 3: Navy Blue
        new Color(255, 0, 0),       // 4: Red
        new Color(255, 255, 0),     // 5: Yellow
        new Color(0, 128, 0),       // 6: Green
        new Color(128, 0, 128),     // 7: Purple
        new Color(165, 42, 42),     // 8: Brown/Maroon
        new Color(0, 191, 255)      // 9: Deep Sky Blue
    };

    public TileComponent(Tile tile) {
        this.tile = tile;
        tile.component = this;
        setPreferredSize(new Dimension(100, 100));
        setBackground(new Color(30, 30, 30)); // Background for the tile container
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    }

    // Helper method to draw the quadrant triangle and the number
    private void drawTriangle(Graphics2D g2d, int[] xPoints, int[] yPoints, Color color, String text, int w, int h, int side) {
        g2d.setColor(color);
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Determine text color based on background darkness for contrast
        Color textColor = (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114) > 186 ? Color.BLACK : Color.WHITE;

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textAscent = fm.getAscent();

        int drawX;
        int drawY;
        int padding = 15; // Padding from the outside edge

        // Calculate text position to place the number near its corresponding edge
        switch (side) {
            case 0: // Top
                drawX = (w / 2) - (textWidth / 2);
                drawY = padding + textAscent;
                break;
            case 1: // Right
                drawX = w - padding - textWidth;
                drawY = (h / 2) + (textAscent / 2);
                break;
            case 2: // Bottom
                drawX = (w / 2) - (textWidth / 2);
                drawY = h - padding; 
                break;
            case 3: // Left
                drawX = padding;
                drawY = (h / 2) + (textAscent / 2);
                break;
            default:
                return;
        }

        g2d.setColor(textColor);
        g2d.drawString(text, drawX, drawY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cx = w / 2;
        int cy = h / 2;
        
        // Draw overall background
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, 0, w, h);

        // 1. Top Quadrant (Points: Top-Left Corner, Top-Right Corner, Center)
        drawTriangle(g2d, new int[]{0, w, cx}, new int[]{0, 0, cy}, COLORS[tile.getTop()], String.valueOf(tile.getTop()), w, h, 0);

        // 2. Right Quadrant (Points: Top-Right Corner, Bottom-Right Corner, Center)
        drawTriangle(g2d, new int[]{w, w, cx}, new int[]{0, h, cy}, COLORS[tile.getRight()], String.valueOf(tile.getRight()), w, h, 1);

        // 3. Bottom Quadrant (Points: Bottom-Right Corner, Bottom-Left Corner, Center)
        drawTriangle(g2d, new int[]{w, 0, cx}, new int[]{h, h, cy}, COLORS[tile.getBottom()], String.valueOf(tile.getBottom()), w, h, 2);

        // 4. Left Quadrant (Points: Bottom-Left Corner, Top-Left Corner, Center)
        drawTriangle(g2d, new int[]{0, 0, cx}, new int[]{h, 0, cy}, COLORS[tile.getLeft()], String.valueOf(tile.getLeft()), w, h, 3);


        // Draw a small dark center box to visually separate the quadrants
        g2d.setColor(new Color(30, 30, 30));
        g2d.fillOval(cx - 4, cy - 4, 8, 8);
        
        // Draw tile ID text in the center bottom corner (less prominent)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, 8));
        String idText = "ID:" + tile.id;
        FontMetrics fm = g2d.getFontMetrics();
        int idX = (w - fm.stringWidth(idText)) / 2;
        int idY = h - 2;
        g2d.drawString(idText, idX, idY);
    }
}

/**
 * The main Tetravex GUI application extending JFrame.
 */
public class TetravexGUI extends JFrame {
    // UPDATED: Increased COLOR_COUNT to 9 to match the complexity of the visual style.
    private static final int GRID_SIZE = 3;
    private static final int TOTAL_TILES = GRID_SIZE * GRID_SIZE;
    private static final int COLOR_COUNT = 9;
    private final Random random = new Random();

    // scrambledTiles will remain an empty list as all tiles are pre-placed
    private List<Tile> scrambledTiles; 
    private Tile[][] gameGrid = new Tile[GRID_SIZE][GRID_SIZE]; // Tiles currently placed on the board

    // GUI Components
    private JPanel gridPanel;
    private JPanel bankPanel;
    private JLabel statusLabel;

    public TetravexGUI() {
        super("Tetravex Puzzle Game (Rotation Mode)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); // Main frame layout
        getContentPane().setBackground(new Color(30, 30, 30));

        // Initialize components and layout
        initializeGUI();
        newGame();

        pack();
        setResizable(false);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private void initializeGUI() {
        // --- 1. Puzzle Grid Panel ---
        gridPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 5, 5));
        gridPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.ORANGE.darker(), 2),
            "Puzzle Grid (Click to Rotate)", 0, 0, null, Color.WHITE));
        gridPanel.setBackground(new Color(50, 50, 50));

        // Create empty slots (placeholders) in the grid
        for (int i = 0; i < TOTAL_TILES; i++) {
            JPanel slot = new JPanel(new BorderLayout());
            slot.setBackground(Color.GRAY.darker());
            slot.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY.darker(), 1));
            // Removed GridSlotListener as it's not used in this mode
            gridPanel.add(slot);
        }

        // --- 2. Tile Bank Panel (Kept for layout simplicity, but made empty/invisible) ---
        bankPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        bankPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.CYAN.darker(), 2),
            "Tile Bank (Unused in Rotation Mode)", 0, 0, null, Color.GRAY));
        bankPanel.setBackground(new Color(50, 50, 50));
        // Ensure it takes up minimal space
        bankPanel.setPreferredSize(new Dimension(10, 10)); 

        // --- 3. Control Panel and Status ---
        JPanel controlPanel = new JPanel(new BorderLayout(0, 10));
        controlPanel.setBackground(new Color(30, 30, 30));

        JButton newGameButton = new JButton("New Game");
        newGameButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.addActionListener(e -> newGame());
        controlPanel.add(newGameButton, BorderLayout.NORTH);

        statusLabel = new JLabel("Click 'New Game' to begin.", SwingConstants.CENTER);
        statusLabel.setForeground(Color.LIGHT_GRAY);
        controlPanel.add(statusLabel, BorderLayout.SOUTH);

        // --- 4. Assemble the Frame ---
        add(gridPanel, BorderLayout.CENTER);
        add(bankPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Starts a new game: generates a solved grid, scrambles rotations, and places all tiles.
     */
    private void newGame() {
        // 1. Generate Solved Tiles and place them in the gameGrid array
        List<Tile> solvedTiles = generatePuzzle();
        
        // Populate the gameGrid array with the tiles in their solved positions
        for (int i = 0; i < TOTAL_TILES; i++) {
            int r = i / GRID_SIZE;
            int c = i % GRID_SIZE;
            gameGrid[r][c] = solvedTiles.get(i);
        }

        // 2. Scramble ONLY rotations (position is fixed)
        scrambleRotations(solvedTiles);

        // 3. Set bank to empty list (satisfies win condition check requirement)
        scrambledTiles = new ArrayList<>();

        // 4. Render the fully populated grid
        setupInitialGrid();

        statusLabel.setText("All tiles are placed. Click any tile to rotate it into position.");
    }
    
    /**
     * Populates the visual grid panel with the tiles from gameGrid.
     */
    private void setupInitialGrid() {
        clearGridComponents();

        Component[] slots = gridPanel.getComponents();
        for (int i = 0; i < TOTAL_TILES; i++) {
            int r = i / GRID_SIZE;
            int c = i % GRID_SIZE;
            Tile tile = gameGrid[r][c];
            
            JPanel targetSlot = (JPanel) slots[i];
            
            // Re-create the component and listener for the newly placed tile
            TileComponent tc = new TileComponent(tile);
            tc.addMouseListener(new PlacedTileListener(tile, i));

            targetSlot.add(tc, BorderLayout.CENTER);
            targetSlot.revalidate();
            targetSlot.repaint();
        }
        
        // Ensure the bank panel is visually empty
        bankPanel.removeAll();
        bankPanel.revalidate();
        bankPanel.repaint();

        // Check the initial condition (only rotation changes matter now)
        checkWinCondition(); 
    }

    /**
     * Clears the visual representation of the grid slots.
     */
    private void clearGridComponents() {
        Component[] slots = gridPanel.getComponents();
        for (Component c : slots) {
            JPanel slot = (JPanel) c;
            slot.removeAll();
            slot.revalidate();
            slot.repaint();
        }
    }
    
    // Renders the tile bank (now disabled/unused)
    private void renderTileBank(List<Tile> tiles) {
        // Unused in Rotation Mode
    }


    // --- LISTENERS (Simplified) ---

    // TileBankListener, findNextEmptySlot, placeTile, removeTile are now irrelevant and removed.

    /**
     * Rotates a tile and checks for a win condition.
     * @param tile The tile to rotate.
     */
    private void rotateTile(Tile tile) {
        tile.rotate();
        checkWinCondition();
    }

    /**
     * Listener for tiles placed on the grid. Only Left-click rotates.
     */
    private class PlacedTileListener extends MouseAdapter {
        private final Tile tile;
        private final int index;

        public PlacedTileListener(Tile tile, int index) {
            this.tile = tile;
            this.index = index;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // Only left-click (or general click) is needed for rotation
            if (!SwingUtilities.isRightMouseButton(e)) {
                rotateTile(tile);
            }
        }
    }

    /**
     * Listener for the empty grid slots (removed as grid is always full).
     */
    private class GridSlotListener extends MouseAdapter {
        // Placeholder, no longer used
    }

    // removeTile method is removed as tiles cannot be removed in this mode.

    // --- Core Game Logic ---

    private int getRandomColorValue() {
        return random.nextInt(COLOR_COUNT) + 1; // Generates 1 to 9
    }

    public List<Tile> generatePuzzle() {
        Tile[][] solvedGrid = new Tile[GRID_SIZE][GRID_SIZE];
        int tileId = 1;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int top = (r == 0) ? getRandomColorValue() : solvedGrid[r - 1][c].getBottom();
                int right = getRandomColorValue();
                int bottom = getRandomColorValue();
                int left = (c == 0) ? getRandomColorValue() : solvedGrid[r][c - 1].getRight();

                solvedGrid[r][c] = new Tile(tileId++, top, right, bottom, left);
            }
        }

        List<Tile> allTiles = new ArrayList<>();
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                allTiles.add(solvedGrid[r][c]);
            }
        }
        return allTiles;
    }
    
    /**
     * Scrambles the rotations of the given list of tiles.
     * @param tiles The list of tiles to rotate randomly.
     */
    public void scrambleRotations(List<Tile> tiles) {
        for (Tile tile : tiles) {
            // Ensure tile starts at rotation 0 before scrambling
            tile.setRotationCounter(0); 
            
            int rotations = random.nextInt(4);
            for (int i = 0; i < rotations; i++) {
                tile.rotate();
            }
        }
        // NOTE: Position shuffling is intentionally skipped for rotation mode
    }

    /**
     * Checks if the currently placed tiles in the grid are a valid Tetravex solution.
     * In Rotation Mode, this only checks for matches.
     */
    public void checkWinCondition() {
        // Since we ensure scrambledTiles is empty in newGame(), we skip the size check.

        // Check if all adjacent edges match
        boolean isSolved = true;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Tile currentTile = gameGrid[r][c];

                // Check North match (with tile above)
                if (r > 0) {
                    Tile northTile = gameGrid[r - 1][c];
                    if (currentTile.getTop() != northTile.getBottom()) {
                        isSolved = false;
                        break;
                    }
                }

                // Check West match (with tile to the left)
                if (c > 0) {
                    Tile westTile = gameGrid[r][c - 1];
                    if (currentTile.getLeft() != westTile.getRight()) {
                        isSolved = false;
                        break;
                    }
                }
            }
            if (!isSolved) break;
        }

        if (isSolved) {
            statusLabel.setText("CONGRATULATIONS! Puzzle Solved!");
            JOptionPane.showMessageDialog(this, "Puzzle Solved!", "Victory!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setText("Keep rotating tiles until all adjacent numbers match.");
        }
    }

    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new TetravexGUI());
    }
}
