import java.util.ArrayList;

import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CtrlGameCanvas {
  
    private Canvas cnv;
    private GraphicsContext gc;
    private AnimationTimer animationTimer;

    private double borderSize = 2.5;

    private String gameStatus = "playing";

    private int playerPoints = 0;
    private double playerX = 10;
    private double playerY = 10;
    
    private int enemyPoints = 0;
    private double enemyX = 10;
    private double enemyY = 10;

    int actualWidthPlayer = 0;
    int actualHeightPlayer = 0;
    int actualWidthEnemy = 0;
    int actualHeightEnemy = 0;
    

    private final double playerWidth = 200;
    private final double playerHalf = playerWidth / 2;
    private final double playerHeight = 5;
    private double playerSpeed = 250;
    private final double playerSpeedIncrement = 15;
    public String playerDirection = "none";
    
    private double ballX = Double.POSITIVE_INFINITY;
    private double ballY = Double.POSITIVE_INFINITY;
    private final double ballSize = 15;
    private final double ballHalf = ballSize / 2;
    private double ballSpeed = 200;
    private final double ballSpeedIncrement = 25;
    private String ballDirection = "upLeft";

    // Iniciar el context i bucle de dibuix
    public void start (Canvas canvas) {

        cnv = canvas;

        // Define drawing context
        gc = canvas.getGraphicsContext2D();

        // Set initial positions
        ballX = cnv.getWidth() / 2;
        ballY = cnv.getHeight() / 2;
        playerX = cnv.getWidth() / 2;

        // Init drawing bucle
        animationTimer = new UtilsFps(this::run, this::draw);
        animationTimer.start();
    }

    // Aturar el bucle de dibuix
    public void stop() {
        animationTimer.stop();
    }
    
    private void sendMessage() {
        JSONObject obj = new JSONObject("{}");
        obj.put("type", "broadcast");
        obj.put("actualWidthEnemy", actualWidthEnemy);
        obj.put("actualHeightEnemy", actualHeightEnemy);
        obj.put("actualWidthPlayer", actualWidthPlayer);
        obj.put("actualHeightPlayer", actualHeightPlayer);

        Main.socketClient.safeSend(obj.toString());
        System.out.println("Send WebSocket: " + obj.toString());
    }

    public void receiveMessage(JSONObject messageObj) {
        System.out.println("Receive WebSocket: " + messageObj.toString());
        String type = messageObj.getString("type");


        if (type.equals("broadcast")) {

            // TODO 

        }
    }

    // Animar
    private void run(double fps) {

        if (fps < 1) return;

        final double boardWidth = cnv.getWidth();
        final double boardHeight = cnv.getHeight();

        // Move player
        switch (playerDirection) {
            case "up":
                playerY = playerY + playerSpeed / fps; 
                break;
            case "down":
                playerY = playerY - playerSpeed / fps;
                break;
        }

        // Keep player in bounds
        final double playerMinY = playerHalf;
        final double playerMaxY = boardWidth - playerHalf;

        if (playerY < playerMinY) {

            playerY = playerMinY;

        } else if (playerY > playerMaxY) {

            playerY = playerMaxY;
        }

        // Move ball
        double ballNextX = ballX;
        double ballNextY = ballY;
        switch (ballDirection) {
            case "upRight": 
                ballNextX = ballX + ballSpeed / fps;
                ballNextY = ballY - ballSpeed / fps;
                break;
            case "upLeft": 
                ballNextX = ballX - ballSpeed / fps;
                ballNextY = ballY - ballSpeed / fps;
                break;
            case "downRight": 
                ballNextX = ballX + ballSpeed / fps;
                ballNextY = ballY + ballSpeed / fps;
                break;
            case "downLeft": 
                ballNextX = ballX - ballSpeed / fps;
                ballNextY = ballY + ballSpeed / fps;
                break;
        }

        // Check ball collision with board sides
        final double[][] lineBall = { {ballX, ballY}, {ballNextX, ballNextY} };

        final double[][] lineBoardLeft = { {borderSize, 0}, {borderSize, boardHeight} };
        final double[] intersectionLeft = findIntersection(lineBall, lineBoardLeft);

        final double boardMaxX = boardWidth - borderSize;
        final double boardMaxY = boardHeight;
        final double[][] lineBoardRight = { {boardMaxX, 0}, {boardMaxX, boardHeight} };
        final double[] intersectionRight = findIntersection(lineBall, lineBoardRight);

        final double[][] lineBoardTop = { {0, borderSize}, {boardWidth, borderSize} };
        final double[] intersectionTop = findIntersection(lineBall, lineBoardTop);

        final double[][] lineBoardBottom = { { 0, boardMaxY - 50}, { boardHeight, borderSize } };
        final double[] intersectionBottom = findIntersection(lineBall, lineBoardBottom);

        if (intersectionTop != null) {
            switch (ballDirection) {
                case "upRight": 
                    ballDirection = "downRight"; 
                    break;
                case "upLeft": 
                    ballDirection = "downLeft"; 
                    break;
            }
            ballX = intersectionTop[0];
            ballY = intersectionTop[1] + 1;

        } else if (intersectionBottom != null) {

            switch (ballDirection) {
                case "downRight":
                    ballDirection = "upLeft";
                    break;
                case "downLeft":
                    ballDirection = "upRight";
                    break;
            }

            ballX = intersectionBottom[0];
            ballY = intersectionBottom[1] - 1;

        } else {
            if (ballNextX < 0) {
                playerPoints += 1;
                ballX = boardWidth / 2;
                ballY = boardHeight / 2;
                
            } else if(ballNextX > boardWidth){
                enemyPoints += 1;
                ballX = boardWidth / 2;
                ballY = boardHeight / 2;
                
            } else {
                ballX = ballNextX;
                ballY = ballNextY;
            }
        }

        // Check ball collision with player
        final double[][] linePlayer = { {playerX - playerHalf, playerY}, {playerX + playerHalf, playerY} };
        final double[] intersectionPlayer = findIntersection(lineBall, linePlayer);

        if (intersectionPlayer != null) {

            switch (ballDirection) {
                case "downRight": 
                    ballDirection = "upRight";
                    break;
                case "downLeft": 
                    ballDirection = "upLeft";
                    break;
            }
            ballX = intersectionPlayer[0];
            ballY = intersectionPlayer[1] - 1;
            playerPoints = playerPoints + 1;
            ballSpeed = ballSpeed + ballSpeedIncrement;
            playerSpeed = playerSpeed + playerSpeedIncrement;
        }

        // Set player Y position
        playerY = cnv.getHeight() - playerHeight - 10;
    }

    // Dibuixar
    private void draw() {

        // Clean drawing area
        gc.clearRect(0, 0, cnv.getWidth(), cnv.getHeight());

        // Draw board
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(borderSize);
        gc.strokeRect(0, 0, borderSize, cnv.getHeight());
        gc.strokeRect(0, 0, cnv.getWidth(), borderSize);
        gc.strokeRect(cnv.getWidth() - borderSize, 0, borderSize, cnv.getHeight());

        drawPlayer(150, 200, Color.GREEN);
        drawPlayer(cnv.getWidth() + 50, 200, Color.PURPLE);

        // Draw ball
        gc.setFill(Color.BLACK);
        gc.fillArc(ballX - ballHalf, ballY - ballHalf, ballSize, ballSize, 0.0, 360, ArcType.ROUND);

        // Draw text with points
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 20));
        String pointsPlayer = "Points: " + playerPoints;
        String pointsEnemy = "Points: " + enemyPoints;
        drawText(gc, pointsPlayer, cnv.getWidth() - 20, 20, "right");
        drawText(gc, pointsEnemy, 20, 20, "left");

        // Draw game over text
        if (playerPoints >= 5 || enemyPoints >= 5) {
            final double boardCenterX = cnv.getWidth() / 2;
            final double boardCenterY = cnv.getHeight() / 2;

            gc.setFont(new Font("Arial", 40));
            drawText(gc, "GAME OVER", boardCenterX, boardCenterY - 20, "center");

            gc.setFont(new Font("Arial", 20));
            drawText(gc, "You are a loser!", boardCenterX, boardCenterY + 20, "center");
        }

    }

    public static void drawText(GraphicsContext gc, String text, double x, double y, String alignment) {
        Text tempText = new Text(text);
        tempText.setFont(gc.getFont());
        final double textWidth = tempText.getLayoutBounds().getWidth();
        final double textHeight = tempText.getLayoutBounds().getHeight();
        switch (alignment) {
            case "center":
                x = x - textWidth / 2;
                y = y + textHeight / 2;
                break;
            case "right":
                x = x - textWidth;
                y = y + textHeight / 2;
                break;
            case "left":
                y = y + textHeight / 2;
                break;
        }
        gc.fillText(text, x, y);
    }

    public static double[] findIntersection(double[][] lineA, double[][] lineB) {
        double[] result = new double[2];

        final double aX0 = lineA[0][0];
        final double aY0 = lineA[0][1];
        final double aX1 = lineA[1][0];
        final double aY1 = lineA[1][1];

        final double bX0 = lineB[0][0];
        final double bY0 = lineB[0][1];
        final double bX1 = lineB[1][0];
        final double bY1 = lineB[1][1];

        double x, y;

        if (aX1 == aX0) { // lineA is vertical
            if (bX1 == bX0) { // lineB is vertical too
                return null;
            }
            x = aX0;
            final double bM = (bY1 - bY0) / (bX1 - bX0);
            final double bB = bY0 - bM * bX0;
            y = bM * x + bB;
        } else if (bX1 == bX0) { // lineB is vertical
            x = bX0;
            final double aM = (aY1 - aY0) / (aX1 - aX0);
            final double aB = aY0 - aM * aX0;
            y = aM * x + aB;
        } else {
            final double aM = (aY1 - aY0) / (aX1 - aX0);
            final double aB = aY0 - aM * aX0;

            final double bM = (bY1 - bY0) / (bX1 - bX0);
            final double bB = bY0 - bM * bX0;

            final double tolerance = 1e-5;
            if (Math.abs(aM - bM) < tolerance) {
                return null;
            }

            x = (bB - aB) / (aM - bM);
            y = aM * x + aB;
        }

        // Check if the intersection point is within the bounding boxes of both line segments
        final double boundingBoxTolerance = 1e-5;
        final boolean withinA = x >= Math.min(aX0, aX1) - boundingBoxTolerance &&
                x <= Math.max(aX0, aX1) + boundingBoxTolerance &&
                y >= Math.min(aY0, aY1) - boundingBoxTolerance &&
                y <= Math.max(aY0, aY1) + boundingBoxTolerance;
        final boolean withinB = x >= Math.min(bX0, bX1) - boundingBoxTolerance &&
                x <= Math.max(bX0, bX1) + boundingBoxTolerance &&
                y >= Math.min(bY0, bY1) - boundingBoxTolerance &&
                y <= Math.max(bY0, bY1) + boundingBoxTolerance;

        if (withinA && withinB) {
            result[0] = x;
            result[1] = y;
        } else {
            return null;
        }

        return result;
    }
    
    public void drawPlayer(double x, int y, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(playerHeight);
        gc.strokeRect(x - playerHalf, y, 5, 200);
    }
}