package com.company;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;


public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        myFrame f = new myFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(myFrame.WIDTH, myFrame.HEIGHT);
        f.setVisible(true);
        f.setup();

        f.draw();
    }

}
enum BackGroundColor {
    black, blue, green
}
class myFrame extends JFrame {
    public static final int WIDTH = 1050, HEIGHT = 700;
    public static final int ROW = 14, COL = 21;
    public static final double block_width = WIDTH / COL, block_height = HEIGHT / ROW;

    public static myFrame instance;
    private Image raster;
    private Graphics rasterGraphics;
    private boolean STARTGAME, TUTORIAL, PLAYAGAIN, OPTION;
    private int[] highestScores = { 0, 0, 0, 0, 0 };
    public boolean GOODGAME;
    private BackGroundColor background = BackGroundColor.black;

    public ArrayList<Block> blocks = new ArrayList<Block>();
    public ArrayList<Hammer> hammers = new ArrayList<Hammer>();
    public Monster m = new Monster(block_width, block_height);
    private ArrayList<String> lines = new ArrayList<String>();

    Player p = new Player((int) block_width, (int) block_height);
    Trampoline t = new Trampoline(block_width, block_height);
    ArrayList<Coin> coins = new ArrayList<Coin>();
    Door d = new Door(block_width, block_height);

    Button start = new Button(WIDTH / 2 - WIDTH / 8, (int) (HEIGHT * 0.35), WIDTH / 4, HEIGHT / 8, "START");
    Button tutorial = new Button(WIDTH / 2 - WIDTH / 8, (int) (HEIGHT * 0.6), WIDTH / 4, HEIGHT / 8, "TUTORIAL");
    Button option = new Button(WIDTH / 2 - WIDTH / 8, (int) (HEIGHT * 0.85), WIDTH / 4, HEIGHT / 8, "OPTION");
    Button menu = new Button((int) (WIDTH * 0.6), (int) (HEIGHT * 0.1), WIDTH / 4, HEIGHT / 8, "MENU");
    Button playAgain = new Button((int) (WIDTH * 0.65), (int) (HEIGHT * 0.1), (int) (WIDTH * 0.26), HEIGHT / 8,
            "PLAY AGAIN");
    ColorButton black = new ColorButton((int) (WIDTH * 0.2), (int) (HEIGHT * 0.1), WIDTH / 4, HEIGHT / 8, "DEFAULT",
            BackGroundColor.black);
    ColorButton blue = new ColorButton((int) (WIDTH * 0.2), (int) (HEIGHT * 0.3), WIDTH / 4, HEIGHT / 8, "SKY BLUE",
            BackGroundColor.blue);
    ColorButton green = new ColorButton((int) (WIDTH * 0.2), (int) (HEIGHT * 0.5), (int) (WIDTH / 3.8), HEIGHT / 8,
            "MIST GREEN", BackGroundColor.green);



    public myFrame() {
        instance = this;
    }

    public static BlockType returnType(int x, int y) {
        for (Block b : instance.blocks) {
            if (b.x == x && b.y == y)
                return b.type;
        }
        return null;
    }

    public void setup() throws FileNotFoundException {
        raster = this.createImage(WIDTH, HEIGHT);
        rasterGraphics = raster.getGraphics();
        this.readBlocks();
        this.readTutorial();
        addKeyListener(p);
        addMouseListener(start);
        addMouseListener(tutorial);
        addMouseListener(option);
        addMouseListener(menu);
        addMouseListener(playAgain);
        addMouseListener(black);
        addMouseListener(blue);
        addMouseListener(green);
        coins.add(new Coin(block_width, block_height, blocks));
        hammers.add(new Hammer(block_width, block_height));
    }

    public void draw() throws FileNotFoundException {

        while (true) {
            if (PLAYAGAIN) {
                p.location.set(6 * block_width, 9 * block_height);
                p.carry = CarryState.Nothing;
                t.x = 2;
                t.y = 7;
                PLAYAGAIN = false;
            }
            rasterGraphics.setColor(Color.black);
            rasterGraphics.fillRect(0, 0, this.WIDTH, this.HEIGHT);

            if (TUTORIAL) {
                menu.draw(rasterGraphics);
                if (menu.CLICK) {
                    TUTORIAL = false;
                }
                drawTutorial(rasterGraphics, this.lines);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else if (OPTION) {
                menu.draw(rasterGraphics);
                if (menu.CLICK) {
                    OPTION = false;
                }
                drawOption(rasterGraphics, WIDTH, HEIGHT);
            } else if (STARTGAME) {
                if (this.background == BackGroundColor.black)
                    rasterGraphics.setColor(Color.black);
                else if (this.background == BackGroundColor.blue)
                    rasterGraphics.setColor(new Color(213, 226, 239));
                else if (this.background == BackGroundColor.green)
                    rasterGraphics.setColor(new Color(62, 188, 163));
                rasterGraphics.fillRect(0, 0, this.WIDTH, this.HEIGHT);
                // end of level screen
                if (GOODGAME || p.location.getY() > HEIGHT || p.health == 0) {
                    rasterGraphics.setColor(Color.white);
                    rasterGraphics.setFont(new Font("Arial", Font.PLAIN, 50));
                    if (!GOODGAME) {
                        rasterGraphics.drawString("YOU DIE", WIDTH / 2 - 150, HEIGHT / 2 - 50);

                    } else {
                        rasterGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
                        rasterGraphics.drawString("GOOD GAME! YOU COMPLETED THIS LEVEL! More to come...",
                                (int) (WIDTH * 0.2), HEIGHT / 2 - 50);

                    }
                    rasterGraphics.setFont(new Font("Arial", Font.PLAIN, 50));
                    rasterGraphics.drawString("Your Score: " + p.score, WIDTH / 2 - 150, HEIGHT / 2 + 50);

                    // sort scores
                    sortScores(p.score);
                    rasterGraphics.setFont(new Font("Arial", Font.PLAIN, 30));
                    rasterGraphics.drawString("Your Best Score Table:", WIDTH / 2 - 150, HEIGHT / 2 + 100);
                    int margin = 30;
                    for (int i = 0; i < highestScores.length; i++) {
                        rasterGraphics.drawString("#" + (i + 1) + ":                  " + highestScores[i],
                                WIDTH / 2 - 150, HEIGHT / 2 + 100 + 30 * (i + 1));
                    }

//					for(int i = 0;i<highestScores.length;i++) {
//						if(p.score > highestScores[i])
//
//					}
//					if (p.score > highestScore)
//						highestScore = p.score;

//					rasterGraphics.drawString("Your Best Score: " + highestScore, WIDTH / 2 - 150, HEIGHT / 2 + 100);
                    if (playAgain.CLICK) {
                        System.out.println("true");
                        STARTGAME = true;
                        PLAYAGAIN = true;
                        GOODGAME = false;
                    }
                    playAgain.draw(rasterGraphics);
                }

                // game screen
                else {
                    this.drawBlocks(rasterGraphics);
                    // check each of the blocks
                    Coin c = coins.get(0);
                    Hammer h = hammers.get(0);
                    p.Act(h, t, c, m, d, rasterGraphics);
                    m.Act();
                    p.draw(rasterGraphics);
                    h.draw(rasterGraphics);
                    c.draw(rasterGraphics);
                    t.draw(rasterGraphics);
                    m.draw(rasterGraphics);
                    if (p.score >= 50) {
                        d.draw(rasterGraphics);
                    }
                    if (c.isPicked) {
                        coins.remove(0);
                        coins.add(new Coin(block_width, block_height, blocks));
                    }
                    if (hammers.size() == 0) {
                        hammers.add(new Hammer(block_width, block_height));
                        System.out.println("add hammer");
                    }

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Graphics2D g2d = (Graphics2D) rasterGraphics;
                g2d.setColor(Color.white);
                g2d.setFont(new Font("Arial", Font.PLAIN, 50));
                g2d.drawString("PLATFORMER", (int) (WIDTH * 0.335), (int) (0.2 * HEIGHT));

                STARTGAME = start.CLICK;
                start.draw(rasterGraphics);

                TUTORIAL = tutorial.CLICK;
                tutorial.draw(rasterGraphics);

                OPTION = option.CLICK;
                option.draw(rasterGraphics);
            }
            this.getGraphics().drawImage(raster, 0, 0, WIDTH, HEIGHT, null);
        }
    }

    private void sortScores(int input) {
        boolean duplicate = false;
        for (int num : this.highestScores) {
            if (num == input) {
                duplicate = true;
                break;
            }
        }
        if (!duplicate) {
            for (int i = 0; i < this.highestScores.length; i++) {
                if (input > this.highestScores[i]) {
                    int[] temp = new int[this.highestScores.length - i];
                    for (int j = i; j < this.highestScores.length; j++) {
                        temp[j - i] = this.highestScores[j];
                    }
                    for (int k = 1; k < temp.length; k++) {
                        this.highestScores[i + k] = temp[k - 1];
                    }

                    this.highestScores[i] = input;
                    break;
                }

            }
        }
    }

    private void readTutorial() throws FileNotFoundException {
        Scanner iF = new Scanner(new File("tutorial.txt"));
        while (iF.hasNextLine()) {
            String line = iF.nextLine();
            this.lines.add(line);
        }
    }

    private void drawOption(Graphics g, double width, double height) {

        black.draw(g);
        blue.draw(g);
        green.draw(g);
        if (black.CLICK)
            this.background = BackGroundColor.black;
        else if (blue.CLICK) {
            this.background = BackGroundColor.blue;
            System.out.println("blue");
        }

        else if (green.CLICK)
            this.background = BackGroundColor.green;
    }

    private void drawTutorial(Graphics g, ArrayList<String> lines) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.white);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        int lineHeight = g2d.getFontMetrics().getHeight();
        // here comes the iteration over all lines
        for (int lineCount = 0; lineCount < lines.size(); lineCount++) { // lines from above
            int xPos = 50;
            int yPos = 50 + lineCount * lineHeight;
            String line = lines.get(lineCount);
            g2d.drawString(line, xPos, yPos);
        }
    }

    public void drawBlocks(Graphics g) {
        for (Block b : blocks) {
            b.draw(g);
        }
    }

    // read from file to get information about blocks
    private void readBlocks() throws FileNotFoundException {
        Scanner iF = new Scanner(new File("blocks.txt"));
        int row = 0, col = 0;
        while (iF.hasNextLine()) {
            String line = iF.nextLine();
            String[] strs = line.split("");
            for (String s : strs) {
                // 0 means blank block
//				if (s.equals("0"))
//					;
//					this.blocks[row][col] = new Block(col, row, BlockType.None, block_width, block_height);
                // A means brown blocks with grass on top
                if (s.equals("A"))
                    blocks.add(new Block(col, row, BlockType.A, block_width, block_height));
                    // a means pure brown blocks
                else if (s.equals("a"))
                    blocks.add(new Block(col, row, BlockType.a, block_width, block_height));
                    // b means grey blocks
                else if (s.equals("b"))
                    blocks.add(new Block(col, row, BlockType.b, block_width, block_height));
                else if (s.equals("c"))
                    blocks.add(new Block(col, row, BlockType.gold, block_width, block_height));
                col++;
            }
            row++;
            col = 0;
        }
    }

}

class Button implements MouseListener {
    public int x, y, width, height;
    public Rectangle rec;
    public boolean CLICK;
    public String text;

    public Button(int x, int y, int width, int height, String text) {
        this.x = x;
        this.y = y;
        this.rec = new Rectangle(x, y, width, height);
        this.text = text;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.blue);
        g2d.fill(this.rec);
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 50));
        double charLength = width / 10;
        int textLength = text.length();
        double outputWidth = charLength * textLength;
        g2d.drawString(text, (int) (x + width / 2 - outputWidth / 2), (int) (y + 0.7 * height));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        Point p = e.getPoint();
        if (rec.contains(p)) {
            this.CLICK = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        Point p = e.getPoint();
        if (rec.contains(p)) {

            this.CLICK = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public boolean click() {
        // TODO Auto-generated method stub
        if (this.CLICK == true) {
//			System.out.println("here");
            return true;
        }
        return false;
    }

}

class ColorButton extends Button {
    BackGroundColor color;

    public ColorButton(int x, int y, int width, int height, String text, BackGroundColor color) {
        super(x, y, width, height, text);
        this.color = color;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (this.color == BackGroundColor.black)
            g2d.setColor(Color.blue);
        else if (this.color == BackGroundColor.blue)
            g2d.setColor(new Color(213, 226, 239));
        else if (this.color == BackGroundColor.green)
            g2d.setColor(new Color(62, 188, 163));
        g2d.fill(this.rec);
        g2d.setColor(Color.white);
        g2d.setFont(new Font("Consolas", Font.PLAIN, 50));
        double charLength = width / 10;
        int textLength = text.length();
        double outputWidth = charLength * textLength;
        g2d.drawString(text, (int) (x + width / 2 - outputWidth / 2), (int) (y + 0.7 * height));
    }

}

enum Direction {
    RIGHT, LEFT;
}

class Sprite {
    public BufferedImage currentFrame;
    private int width, height;
    public Direction direction;
    public boolean hasHammer, HURT;
    public Player owner;



    public Sprite(int block_width, int block_height) {
        this.currentFrame = new BufferedImage(block_width, block_height, BufferedImage.TYPE_INT_RGB);
        this.width = block_width;
        this.height = block_height;
        this.direction = Direction.RIGHT;
        this.currentFrame = defaultImage();
        this.HURT = false;
    }

    public void withHammer() {
        if (this.owner != null)
            this.HURT = this.owner.HURT;
        BufferedImage newFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics g = newFrame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        double bigRadius = (this.height * 3.5f) / 20;
        double bigHeight = (this.height * 4.5) / 10;
        Ellipse2D.Double bigCircle = new Ellipse2D.Double(this.width / 2 - bigRadius, 0, bigRadius * 2, bigRadius * 2);
        Ellipse2D.Double smallCircle = new Ellipse2D.Double(this.width / 2 - bigRadius + bigRadius * 0.5,
                bigRadius * 0.5, bigRadius, bigRadius);
        Rectangle2D.Double bigRec = new Rectangle2D.Double(this.width / 2 - bigRadius * 1.1, bigRadius * 2,
                bigRadius * 2.2, bigHeight);
        Rectangle2D.Double smallRec = new Rectangle2D.Double(this.width / 2 - bigRadius * 1.1 * 0.75,
                bigRadius * 2 + bigHeight * 0.125, bigRadius * 1.5, bigHeight * 0.75);
        Line2D.Double leftleg = new Line2D.Double(this.width / 2 - bigRadius * 0.75, bigRadius * 2 + bigHeight,
                this.width / 2 - bigRadius * 0.75, this.height);
        Line2D.Double rightleg = new Line2D.Double(this.width / 2 + bigRadius * 0.75, bigRadius * 2 + bigHeight,
                this.width / 2 + bigRadius * 0.75, this.height);

        double hammerWidth = this.width / 2 - bigRadius;
        double hammerHeight = bigRadius / 3;
        double stickWidth = hammerWidth / 6;

        Rectangle2D.Double hammerHead = new Rectangle2D.Double(width - hammerWidth, bigRadius, hammerWidth,
                hammerHeight);
        Rectangle2D.Double hammerStick = new Rectangle2D.Double(width - hammerWidth / 2 - stickWidth / 2,
                bigRadius + hammerHeight, stickWidth, hammerHeight * 2.5);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.fill(bigCircle);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(140, 209, 230));
        g2d.fill(smallCircle);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.fill(bigRec);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(140, 209, 230));
        g2d.fill(smallRec);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.draw(leftleg);
        g2d.draw(rightleg);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(110, 109, 109));
        g2d.fill(hammerHead);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(167, 102, 76));
        g2d.fill(hammerStick);

        if (direction.equals(Direction.RIGHT))
            this.currentFrame = newFrame;
        else {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-newFrame.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            newFrame = op.filter(newFrame, null);
            this.currentFrame = newFrame;
        }

    }

    public void withTrampoline() {
        if (this.owner != null)
            this.HURT = this.owner.HURT;
        BufferedImage newFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics g = newFrame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        double bigRadius = (this.height * 3.5f) / 20;
        double bigHeight = (this.height * 4.5) / 10;
        Ellipse2D.Double bigCircle = new Ellipse2D.Double(this.width / 2 - bigRadius, 0, bigRadius * 2, bigRadius * 2);
        Ellipse2D.Double smallCircle = new Ellipse2D.Double(this.width / 2 - bigRadius + bigRadius * 0.5,
                bigRadius * 0.5, bigRadius, bigRadius);
        Rectangle2D.Double bigRec = new Rectangle2D.Double(this.width / 2 - bigRadius * 1.1, bigRadius * 2,
                bigRadius * 2.2, bigHeight);
        Rectangle2D.Double smallRec = new Rectangle2D.Double(this.width / 2 - bigRadius * 1.1 * 0.75,
                bigRadius * 2 + bigHeight * 0.125, bigRadius * 1.5, bigHeight * 0.75);
        Line2D.Double leftleg = new Line2D.Double(this.width / 2 - bigRadius * 0.75, bigRadius * 2 + bigHeight,
                this.width / 2 - bigRadius * 0.75, this.height);
        Line2D.Double rightleg = new Line2D.Double(this.width / 2 + bigRadius * 0.75, bigRadius * 2 + bigHeight,
                this.width / 2 + bigRadius * 0.75, this.height);
        double bagWidth = (this.width / 2 - bigRadius * 1.1) / 2;
        Rectangle2D.Double bag = new Rectangle2D.Double(bagWidth, bigRadius * 2 + bigHeight / 4, bagWidth,
                bigHeight / 2);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.fill(bigCircle);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(140, 209, 230));
        g2d.fill(smallCircle);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.fill(bigRec);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(140, 209, 230));
        g2d.fill(smallRec);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.draw(leftleg);
        g2d.draw(rightleg);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(125, 203, 230));
        g2d.fill(bag);

        if (direction.equals(Direction.RIGHT))
            this.currentFrame = newFrame;
        else {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-newFrame.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            newFrame = op.filter(newFrame, null);
            this.currentFrame = newFrame;
        }
    }

    public BufferedImage defaultImage() {
        if (this.owner != null)
            this.HURT = this.owner.HURT;
        BufferedImage newFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics g = newFrame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        double bigRadius = (this.height * 3.5f) / 20;
        double bigHeight = (this.height * 4.5) / 10;
        Ellipse2D.Double bigCircle = new Ellipse2D.Double(this.width / 2 - bigRadius, 0, bigRadius * 2, bigRadius * 2);
        Ellipse2D.Double smallCircle = new Ellipse2D.Double(this.width / 2 - bigRadius + bigRadius * 0.5,
                bigRadius * 0.5, bigRadius, bigRadius);
        Rectangle2D.Double bigRec = new Rectangle2D.Double(this.width / 2 - bigRadius * 1.1, bigRadius * 2,
                bigRadius * 2.2, bigHeight);
        Rectangle2D.Double smallRec = new Rectangle2D.Double(this.width / 2 - bigRadius * 1.1 * 0.75,
                bigRadius * 2 + bigHeight * 0.125, bigRadius * 1.5, bigHeight * 0.75);
        Line2D.Double leftleg = new Line2D.Double(this.width / 2 - bigRadius * 0.75, bigRadius * 2 + bigHeight,
                this.width / 2 - bigRadius * 0.75, this.height);
        Line2D.Double rightleg = new Line2D.Double(this.width / 2 + bigRadius * 0.75, bigRadius * 2 + bigHeight,
                this.width / 2 + bigRadius * 0.75, this.height);
        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.fill(bigCircle);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(140, 209, 230));
        g2d.fill(smallCircle);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.fill(bigRec);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(140, 209, 230));
        g2d.fill(smallRec);

        if (this.HURT)
            g2d.setColor(Color.red);
        else
            g2d.setColor(new Color(45, 51, 196));
        g2d.draw(leftleg);
        g2d.draw(rightleg);

        if (direction.equals(Direction.RIGHT))
            return newFrame;
        else {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-newFrame.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            newFrame = op.filter(newFrame, null);
            return newFrame;
        }

    }

}

enum CarryState {
    Nothing, Hammer, Trampoline;
}

class Player implements KeyListener {
    Vector2D location, velocity;
    private int width, height;
    public int score, health;
    private Sprite sprite;
    public boolean UP, DOWN, LEFT, RIGHT, NODOWN, SPACE, COLLIDEHAMMER, COLLIDETRAMPOLINE, DIG, JUMP, PICK, THROW, HURT;
    public boolean NOUP, NOLEFT, NORIGHT, COLLIDE;
    public static final double MOVE_SPEED = 1;
    public final Vector2D GRAVITY = new Vector2D(0, 1);
    public CarryState carry;
    private int hurtCoolDown = 0;

    public Hammer hammer;
    public Trampoline trampoline;

    public Player(int block_width, int block_height) {
        this.score = 0;
        this.health = 5;

        location = new Vector2D(6 * block_width, 9 * block_height);
        velocity = new Vector2D(0, 0);
        this.width = block_width;
        this.height = block_height;
        this.sprite = new Sprite(width, height);
        this.sprite.owner = this;

        this.hammer = null;
        this.trampoline = null;

        this.carry = CarryState.Nothing;
    }

    public void Act(Hammer h, Trampoline t, Coin c, Monster m, Door d, Graphics g) {
        // update location
        location = location.add(velocity);

        if (velocity.getY() < 0) // I'm moving up so I can't be on the ground
            NODOWN = false;

        // first check collision
        for (Block b : myFrame.instance.blocks) {
            checkCollision(b.getCollision());
        }

        // Set velocity
        if (SPACE && NODOWN) {
            velocity = velocity.add(new Vector2D(0, -MOVE_SPEED * 25));
        } // because we don't want player jumping in the air
        if (DOWN) {
            velocity = velocity.add(new Vector2D(0, MOVE_SPEED));
        }
        if (RIGHT) {
            this.sprite.direction = Direction.RIGHT;
            velocity = velocity.add(new Vector2D(MOVE_SPEED, 0));
        }
        if (LEFT) {
            this.sprite.direction = Direction.LEFT;
            velocity = velocity.add(new Vector2D(-MOVE_SPEED, 0));
        }

        if (NORIGHT || NOLEFT) {
            velocity.setX(-velocity.getX());
        }

        // gravity
        velocity = velocity.add(GRAVITY);

        velocity = velocity.multiply(0.95f);

        // friction
        if (NODOWN)
            velocity = velocity.multiply(0.85f);

        checkCoin(c);
        // checking hammer
//		if (this.carry == CarryState.Hammer || this.carry == CarryState.Nothing) {
//			if (!COLLIDEHAMMER)
//				checkHammer(h);
//			if (COLLIDEHAMMER) {
//				this.hammer = h;
//				h.owner = this;
//				COLLIDEHAMMER = false;
//			}
//
//			dig();
//		}

        checkMonster(m);
        // checking trampoline
        if (this.carry == CarryState.Nothing) {
            if (this.hammer != null) {
                this.hammer = null;
            }
//			else if(this.trampoline!=null) {
//				this.trampoline = null;
//			}

//			if (myFrame.instance.hammers.size() != 0) {
//				myFrame.instance.hammers.remove(0);
//			}
            // check for hammer
//			if (!COLLIDEHAMMER)
            checkHammer(h);// if it collides with the hammer block
//			if (!COLLIDETRAMPOLINE)
            checkTrampoline(t);

            // check for trampoline
            if (COLLIDETRAMPOLINE && this.carry != CarryState.Trampoline) {

                pick(t);
                jump();

                COLLIDETRAMPOLINE = false;
            }

            else if (COLLIDEHAMMER) {

                this.hammer = h;
                h.owner = this;
                this.carry = CarryState.Hammer;
                COLLIDEHAMMER = false;
            }

        }

        if (this.carry == CarryState.Hammer) {
            dig();

            sprite.withHammer();
        } else if (this.carry == CarryState.Trampoline) {
            trampoline.x = (int) (this.location.getX() / width);
            trampoline.y = (int) (this.location.getY() / height);
            discard(t);
            sprite.withTrampoline();
        }

        else if (this.carry == CarryState.Nothing)
            sprite.currentFrame = sprite.defaultImage();

        checkDoor(d);
        NOUP = false;
        NODOWN = false;
        NOLEFT = false;
        NORIGHT = false;

    }

    private void pick(PickableItem p) {
        if (PICK && p instanceof Trampoline) {
            this.trampoline = (Trampoline) p;
            ((Trampoline) p).owner = this;
            this.carry = CarryState.Trampoline;
        }
    }

    private void discard(PickableItem p) {
        if (THROW && this.trampoline != null) {
            trampoline.x = (int) (this.location.getX() / width);
            trampoline.y = (int) (this.location.getY() / height);
            this.carry = CarryState.Nothing;
            this.trampoline = null;
            ((Trampoline) p).owner = null;
        }
    }

    private void jump() {
        if (JUMP && NODOWN) {
            this.velocity = this.velocity.add(new Vector2D(0, -1 * 35));
        }
    }

    private void dig() {
        ArrayList<Block> possibleBlocks = new ArrayList<>();
        Block blockBeingDigged = null;
        Direction direction = this.sprite.direction;
        // can only dig left or right
        if (DIG && this.hammer != null) {
            for (Block b : myFrame.instance.blocks) {
                Area myArea = new Area(this.getCollision());
                Area otherArea = new Area(b.getCollision());
                myArea.intersect(otherArea);
                if (!myArea.isEmpty() && !b.type.equals(BlockType.b))
                    possibleBlocks.add(b);
            }

            for (Block b : possibleBlocks) {
                System.out.println(b.x * width + " " + this.location.getX());
                // if sprite is facing towards right and there is a brown block on the right
                if (direction.equals(Direction.RIGHT) && b.x * width >= this.location.getX()) {
                    blockBeingDigged = b;
                }
                // if sprite is facing towards left and there is a brown block on the left
                else if (direction.equals(Direction.LEFT) && b.x * width < this.location.getX()) {
                    blockBeingDigged = b;
                }
            }
            if (blockBeingDigged != null) {
                // color of block becomes red, block durability decreases
                blockBeingDigged.beingDigged();
                this.hammer.use();
                // if block reaches durability, it is removed from the list
                if (blockBeingDigged.durability == 0) {
                    if (blockBeingDigged.type == BlockType.gold)
                        this.score += 10;
                    myFrame.instance.blocks.remove(blockBeingDigged);
                    // hammer durability decreases

                }

                // if hammer reaches durability, it disappears
                if (this.hammer.durability == 0) {
                    myFrame.instance.hammers.remove(this.hammer);
                    this.hammer = null;
                    this.carry = CarryState.Nothing;
                    System.out.println(this.carry);
                    this.sprite.defaultImage();
                }
            }

        }
    }

    public void checkCollision(Shape s) {
        Area myArea = new Area(this.getCollision());
        Area otherArea = new Area(s);
        myArea.intersect(otherArea);
        if (!myArea.isEmpty())
            resolveCollision(s);
    }

    public void resolveCollision(Shape s) {
        int x = (int) (this.location.getX());
        int y = (int) (this.location.getY());
        if (s.intersects(new Rectangle(x + 10, y, width - 20, 10))) {
            // if it tries to breakthrough the block, which means it's slightly higher than
            // the block
            int diff = s.getBounds().y + s.getBounds().height - y;
            this.location.setY(this.location.getY() + diff);
            NOUP = true;
        }
        if (s.intersects(new Rectangle(x + 10, y + height - 10, width - 20, 10))) {
            // MTD
            int bottomY = y + height;
            int diff = bottomY - s.getBounds().y;
            if (diff > GRAVITY.getY()) // more than gravity
            {
                location.setY(location.getY() - diff);
                // update the collision (this stops the right and left from colliding when there
                // is a big MTD adjustment
                y -= diff;
            } else // Gravity deflection
                location.setY(location.getY() - GRAVITY.getY()); // push them out of the object (the same amount that
            // gravity pushes it in)
            // then i'm standing on something
            velocity.setY(0);
            NODOWN = true;

        }

        if (s.intersects(new Rectangle((int) x, (int) y + 10, 10, height - 20))) {
            if (this.velocity.getX() > -1) {
                int diff = s.getBounds().x + s.getBounds().width - (int) this.location.getX();
                this.location.setX(this.location.getX() + diff);
                this.velocity.setX(0);
            } else {
                NOLEFT = true;
            }

        }

        if (s.intersects(new Rectangle((int) x + width - 10, (int) y + 10, 10, height - 20))) {
            if (this.velocity.getX() < 1) {
                int diff = (int) this.location.getX() + width - s.getBounds().x;

                this.location.setX(this.location.getX() - diff);
                this.velocity.setX(0);
            } else {
                NORIGHT = true;
            }

        }

    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g.drawImage(sprite.currentFrame, (int) location.getX(), (int) location.getY(), width, height, null);
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + this.score, 10, 50);
        g.drawString("Health: " + this.health, 10, 75);
        this.HURT = false;
    }

    private Rectangle getCollision() {
        Rectangle collision = new Rectangle((int) this.location.getX(), (int) this.location.getY(), width, height);
        return collision;
    }

    public void checkHammer(Hammer h) {
        Area thisArea = new Area(this.getCollision());
        Area otherArea = new Area(h.getCollision());
        thisArea.intersect(otherArea);
        COLLIDEHAMMER = !thisArea.isEmpty();
    }

    public void checkTrampoline(Trampoline t) {
        Area thisArea = new Area(this.getCollision());
        Area otherArea = new Area(t.getCollision());
        thisArea.intersect(otherArea);
        COLLIDETRAMPOLINE = !thisArea.isEmpty();
    }

    public void checkMonster(Monster m) {
        Area thisArea = new Area(this.getCollision());
        Area otherArea = new Area(m.getCollision());
        thisArea.intersect(otherArea);
        if (!thisArea.isEmpty()) {
            HURT = true;
            this.hurtCoolDown++;
            if (hurtCoolDown > 10) {
                this.health--;
                hurtCoolDown = 0;
            }
        }
    }

    public void checkDoor(Door d) {
        if (this.score >= 50) {
            Area thisArea = new Area(this.getCollision());
            Area otherArea = new Area(d.getCollision());
            thisArea.intersect(otherArea);
            if (!thisArea.isEmpty()) {
                myFrame.instance.GOODGAME = true;
            }
        }

    }

    public void checkCoin(Coin c) {
        Area thisArea = new Area(this.getCollision());
        Area otherArea = new Area(c.getCollision());
        thisArea.intersect(otherArea);
        if (!thisArea.isEmpty()) {
            c.isPicked = true;
            this.score++;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            UP = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            DOWN = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            RIGHT = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            LEFT = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            SPACE = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            DIG = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
            JUMP = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_1) {
            PICK = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_2) {
            THROW = true;
        }
        if (velocity.getLength() <= 1.0f)
            velocity = velocity.multiply(1.05f);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            UP = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            DOWN = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            RIGHT = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            LEFT = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            SPACE = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_Q) {
            DIG = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
            JUMP = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_1) {
            PICK = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_2) {
            THROW = false;
        }
    }
}

class Monster {
    Vector2D location, velocity;

    private double width, height;
    public static final double MOVE_SPEED = 3;
    public final Vector2D GRAVITY = new Vector2D(0, 1);
    public Direction direction;
    private BufferedImage currentFrame;

    public Monster(double block_width, double block_height) {
        location = new Vector2D(6 * block_width, 9 * block_height);
        velocity = new Vector2D(0, 0);
        this.width = block_width;
        this.height = block_height;
        this.location.setX(5 * width);
        this.location.setY(5 * height);
        this.direction = Direction.LEFT;
        updateBuffer();
    }

    public void Act() {
        if (this.direction == Direction.LEFT && this.location.getX() < 8 * width) {
            this.location.setX(this.location.getX() + MOVE_SPEED);
            if (this.location.getX() == 8 * width) {
                this.direction = Direction.RIGHT;
            }
        } else if (this.direction == Direction.RIGHT && this.location.getX() > 5 * width) {
            this.location.setX(this.location.getX() - MOVE_SPEED);
            if (this.location.getX() == 5 * width) {
                this.direction = Direction.LEFT;
            }
        }
    }

    public void draw(Graphics g) {
        updateBuffer();
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(this.currentFrame, (int) (this.location.getX()), (int) (this.location.getY()), (int) width,
                (int) height, null);
    }

    private void updateBuffer() {
        BufferedImage newFrame = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) newFrame.getGraphics();

        double eyeHeight = (height / 4) * 1.3;
        Arc2D eye = new Arc2D.Double(20, 4, eyeHeight, eyeHeight, 0, -180, Arc2D.OPEN);
        Ellipse2D.Double pupil = new Ellipse2D.Double(28, 13, 5, 5);
        g2d.setColor(new Color(218, 0, 26));

        g2d.fillArc((int) (0.15 * width), (int) (0.05 * height), (int) (0.7 * width), (int) (0.9 * height), 200, 300);
        g2d.setColor(new Color(252, 238, 0));
        g2d.fill(eye);
        g2d.setColor(new Color(218, 0, 26));
        g2d.fill(pupil);

        if (direction.equals(Direction.RIGHT)) {
            this.currentFrame = newFrame;
        }

        else {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-newFrame.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            newFrame = op.filter(newFrame, null);
            this.currentFrame = newFrame;
        }
    }

    public Rectangle getCollision() {
        Rectangle collision = new Rectangle((int) this.location.getX(), (int) this.location.getY(), (int) width,
                (int) height);
        return collision;
    }
}

enum BlockType {
    A, b, a, gold;
}

class Door {
    public int x, y;
    private double width, height;

    public Door(double width, double height) {
        this.x = 20;
        this.y = 2;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Ellipse2D.Double door = new Ellipse2D.Double(x * width, y * height, width, height);
        g2d.setColor(Color.yellow);
        g2d.fill(door);
    }

    public Rectangle getCollision() {

        Rectangle collision = new Rectangle((int) (this.x * width), (int) (this.y * height), (int) width, (int) height);
        return collision;
    }
}

class Block {
    public BlockType type;
    public int x, y, durability;
    private double width, height;
    private boolean isDigged;

    public Block(int x, int y, BlockType type, double width, double height) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.width = width;
        this.height = height;

        if (this.type == BlockType.gold)
            this.durability = 5;
        else
            this.durability = 3;
    }

    public void beingDigged() {
        isDigged = true;
        this.durability--;
    }

    public Rectangle getCollision() {

        Rectangle collision = new Rectangle((int) (this.x * width), (int) (this.y * height), (int) width, (int) height);
        return collision;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        double margin = width / 10;
        Rectangle2D.Double bigRec = new Rectangle2D.Double(this.x * width, this.y * height, width, height);
        Rectangle2D.Double smallRec = new Rectangle2D.Double(this.x * width + margin, this.y * height + margin,
                width - 2 * margin, height - 2 * margin);
        if (this.isDigged) {
            g2d.setColor(Color.red);
            g2d.fill(bigRec);
        } else if (this.type == BlockType.A) {
            Rectangle2D.Double top = new Rectangle2D.Double(this.x * width, this.y * height, width, margin);

            g2d.setColor(Color.white);
            g2d.fill(bigRec);

            Color brown = new Color(167, 101, 68);
            g2d.setColor(brown);
            g2d.fill(smallRec);

            g2d.setColor(Color.green);
            g2d.fill(top);

        }

        else if (this.type == BlockType.a) {
            g2d.setColor(Color.white);
            g2d.fill(bigRec);

            Color brown = new Color(167, 101, 68);
            g2d.setColor(brown);
            g2d.fill(smallRec);
        }

        else if (this.type == BlockType.b) {
            g2d.setColor(Color.white);
            g2d.fill(bigRec);

            g2d.setColor(Color.gray);
            g2d.fill(smallRec);
        }

        else if (this.type == BlockType.gold) {
            g2d.setColor(Color.white);
            g2d.fill(bigRec);
            g2d.setColor(new Color(243, 194, 2));
            g2d.fill(smallRec);
        }
        isDigged = false;
    }
}

interface PickableItem {

}

class Hammer implements PickableItem {
    int x, y, durability;
    double width, height;
    boolean isPicked;
    public Player owner;

    public Hammer(double width, double height) {
        this.x = 16;
        this.y = 7;
        this.width = width;
        this.height = height;
        isPicked = false;
        this.owner = null;
        this.durability = 9;
    }

    public void use() {
        this.durability--;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (this.owner == null) {
            double headHeight = height / 6;
            double headWidth = width / 2;
            double startX = this.x * width;
            double startY = this.y * height;
            Rectangle2D.Double head = new Rectangle2D.Double(startX + width / 2 - headWidth / 2,
                    startY + height / 2 - headHeight / 2, headWidth, headHeight);
            double stickWidth = headWidth / 6;
            double stickHeight = (width * 5) / 12;
            Rectangle2D.Double stick = new Rectangle2D.Double(startX + width / 2 - stickWidth / 2,
                    startY + height / 2 + headHeight / 2, stickWidth, stickHeight);
            g2d.setColor(new Color(110, 109, 109));
            g2d.fill(head);
            g2d.setColor(new Color(167, 102, 76));
            g2d.fill(stick);
        }
    }

    public Rectangle getCollision() {
        Rectangle collision = new Rectangle((int) (this.x * width), (int) (this.y * height), (int) width, (int) height);
        return collision;
    }
}

class Coin {
    int x, y;
    double width, height;
    boolean isPicked;

    public Coin(double width, double height, ArrayList<Block> blocks) {
        this.isPicked = false;
        this.width = width;
        this.height = height;

        // TODO randomly generating coins above blocks
        this.generateRandomPosition(blocks);
    }

    private void generateRandomPosition(ArrayList<Block> blocks) {
        int length = blocks.size();
        int index = -1;
        while (true) {
            index = (int) (Math.random() * (length - 1));
            if (blocks.get(index).x != 0 && blocks.get(index).type.equals(BlockType.A))
                break;
        }
        if (index != -1) {
            this.x = blocks.get(index).x;
            this.y = blocks.get(index).y - 1;
        }

    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if (!isPicked) {
            double bigWidth = width * 0.7;
            double bigHeight = height * 0.9;
            double smallWidth = bigWidth * 0.3;
            double smallHeight = bigHeight * 0.5;
            double startX = this.x * width;
            double startY = this.y * height;
            Ellipse2D.Double big = new Ellipse2D.Double(startX + width / 2 - bigWidth / 2,
                    startY + height / 2 - bigHeight / 2, bigWidth, bigHeight);
            Ellipse2D.Double small = new Ellipse2D.Double(startX + width / 2 - smallWidth / 2,
                    startY + height / 2 - smallHeight / 2, smallWidth, smallHeight);
            g2d.setColor(new Color(255, 240, 0));
            g2d.fill(big);
            g2d.setColor(new Color(252, 189, 0));
            g2d.fill(small);
        }
    }

    public Rectangle getCollision() {
        Rectangle collision = new Rectangle((int) (this.x * width), (int) (this.y * height), (int) width, (int) height);
        return collision;
    }
}

class Trampoline implements PickableItem {
    int x, y;
    double width, height;
    boolean isPicked;
    public Player owner;

    public Trampoline(double width, double height) {
        this.x = 2;
        this.y = 7;
        this.width = width;
        this.height = height;
        this.owner = null;
    }

    public void draw(Graphics g) {
        if (this.owner == null) {
            Graphics2D g2d = (Graphics2D) g;
            double startX = x * width;
            double startY = y * height;
            double margin = width / 8;
            double boardWidth = (width * 3) / 4;
            double boardHeight = ((height - margin) * 2) / 3;
            double leglength = boardHeight / 2;
            Rectangle2D.Double bigRec = new Rectangle2D.Double(startX + margin, startY + margin, boardWidth,
                    boardHeight);
            Rectangle2D.Double smallRec = new Rectangle2D.Double(startX + margin + boardWidth / 4,
                    startY + margin + boardHeight / 4, boardWidth / 2, boardHeight / 2);
            Line2D.Double leftleg = new Line2D.Double(startX + margin * 2, startY + margin + boardHeight,
                    startX + margin * 2, startY + margin + boardHeight + leglength);
            Line2D.Double rightleg = new Line2D.Double(startX + boardWidth, startY + margin + boardHeight,
                    startX + boardWidth, startY + margin + boardHeight + leglength);

            g2d.setColor(new Color(125, 203, 230));
            g2d.fill(bigRec);
            g2d.draw(leftleg);
            g2d.draw(rightleg);
            g2d.setColor(new Color(28, 145, 229));
            g2d.fill(smallRec);
        }

    }

    public Rectangle getCollision() {
        Rectangle collision = new Rectangle((int) (this.x * width), (int) (this.y * height), (int) width, (int) height);
        return collision;
    }
}

class Vector2D {
    private double x;
    private double y;

    public Vector2D() {
        this.setX(0);
        this.setY(0);
    }

    public Vector2D(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    public Vector2D(Vector2D v) {
        this.setX(v.getX());
        this.setY(v.getY());
    }

    public static double Distance(Vector2D position2, Vector2D position3) {
        return Math.sqrt(
                Math.pow(position2.getX() - position3.getX(), 2) + Math.pow(position2.getY() - position3.getY(), 2));
    }

    public double Distance(Vector2D position3) {
        return Math.sqrt(Math.pow(getX() - position3.getX(), 2) + Math.pow(getY() - position3.getY(), 2));
    }

    public void set(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void rotate(double angle) {
        Vector2D newVect = new Vector2D(this);
        newVect.setX(
                getX() * (float) Math.cos(Math.toRadians(angle)) + getY() * (float) Math.sin(Math.toRadians(angle)));
        newVect.setY(
                -getX() * (float) Math.sin(Math.toRadians(angle)) + getY() * (float) Math.cos(Math.toRadians(angle)));
        this.set(newVect.getX(), newVect.getY());
    }

    // U x V = Ux*Vy-Uy*Vx
    public static double Cross(Vector2D U, Vector2D V) {
        return U.x * V.y - U.y * V.x;
    }

    // Specialty method used during calculations of ball to ball collisions.
    public double dot(Vector2D v2) {
        double result = 0.0f;
        result = this.getX() * v2.getX() + this.getY() * v2.getY();
        return result;
    }

    public float getLength() {
        return (float) Math.sqrt(getX() * getX() + getY() * getY());
    }

    public Vector2D add(Vector2D v2) {
        Vector2D result = new Vector2D();
        result.setX(getX() + v2.getX());
        result.setY(getY() + v2.getY());
        return result;
    }

    public Vector2D subtract(Vector2D v2) {
        Vector2D result = new Vector2D();
        result.setX(this.getX() - v2.getX());
        result.setY(this.getY() - v2.getY());
        return result;
    }

    public Vector2D multiply(float scaleFactor) {
        Vector2D result = new Vector2D();
        result.setX(this.getX() * scaleFactor);
        result.setY(this.getY() * scaleFactor);
        return result;
    }

    // Specialty method used during calculations of ball to ball collisions.
    public Vector2D normalize() {
        float length = getLength();
        if (length != 0.0f) {
            this.setX(this.getX() / length);
            this.setY(this.getY() / length);
        } else {
            this.setX(0.0f);
            this.setY(0.0f);
        }
        return this;
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}
