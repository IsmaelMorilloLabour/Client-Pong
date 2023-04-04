import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
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

    private boolean gameOver;

    private int playerPoints = 0;
    private double playerX;
    private double playerY = 200;

    private int enemyPoints = 0;
    private double enemyX;
    private double enemyY = 200;

    private final double playerWidth = 10;
    private double playerSpeed = 200;
    private final double playerSpeedIncrement = 5;
    public String playerDirection = "none";
    public String enemyDirection = "none";

    private double ballX = 200;
    private double ballY = 200;
    private final double ballSize = 25;
    private final double ballHalf = ballSize / 2;
    private double ballSpeed = 150;
    private final double ballSpeedIncrement = 0;
    private String ballDirection = "upLeft";

    // Iniciar el context i bucle de dibuix
    public void start(Canvas canvas) {

        cnv = canvas;

        // Define drawing context
        gc = canvas.getGraphicsContext2D();

        // Set initial positions
        ballX = 200;
        ballY = 300;

        playerX = 30;
        enemyX = 770;

        // Init drawing bucle
        animationTimer = new UtilsFps(this::run, this::draw);
        animationTimer.start();
    }

    // Aturar el bucle de dibuix
    public void stop() {
        animationTimer.stop();
    }

    public void sendCurrentState(double p1X, double p1Y, double p2X, double p2Y) {
        JSONObject obj = new JSONObject("{}");
        obj.put("type", "currentStatePlayers");
        obj.put("p1X", p1X);
        obj.put("p1Y", p1Y);

        obj.put("p2X", p2X);
        obj.put("p2Y", p2Y);

        Main.socketClient.safeSend(obj.toString());
    }

    public void sendCurrentStateBall(double bX, double bY) {
        JSONObject obj = new JSONObject("{}");
        obj.put("type", "currentStateBall");

        obj.put("bY", bY);
        obj.put("bX", bX);

        Main.socketClient.safeSend(obj.toString());
    }

    public void receive(JSONObject messageObj) {
        try {
            String type = messageObj.getString("type");

            if (type.equals("listPlayers")) {
                JSONArray list = messageObj.getJSONArray("list");
                Main.playersConnected = list.length();

            } else if (type.equals("currentStatePlayers")) {

                JSONObject obj = messageObj.getJSONObject("gameState");
                JSONObject objPlayer = obj.getJSONObject("player");
                JSONObject objPlayer2 = obj.getJSONObject("player2");

                playerX = objPlayer.getDouble("x");
                playerY = objPlayer.getDouble("y");

                enemyX = objPlayer2.getDouble("x");
                enemyY = objPlayer2.getDouble("y");

            } else if (type.equals("currentStateBall")) {

                JSONObject obj = messageObj.getJSONObject("gameState");
                JSONObject objBall = obj.getJSONObject("ball");

                ballX = objBall.getDouble("x");
                ballY = objBall.getDouble("y");

            } else if (type.equals("gameOver")) {
                Main.socketClient.close();
            }

        } catch (JSONException e) {
            System.err.println("Error processing JSON message: " + e.getMessage());
        }
    }

    // Animar
    private void run(double fps) {

        if (fps < 1)
            return;

        final double boardWidth = cnv.getWidth();
        final double boardHeight = cnv.getHeight();

        // Move player
        switch (playerDirection) {
            case "up":
                playerY -= playerSpeed / fps;
                sendCurrentState(playerX, playerY, enemyX, enemyY);
                break;

            case "down":
                playerY += playerSpeed / fps;
                sendCurrentState(playerX, playerY, enemyX, enemyY);
                break;
        }

        // Move player
        switch (enemyDirection) {
            case "up":
                enemyY -= playerSpeed / fps;
                sendCurrentState(playerX, playerY, enemyX, enemyY);
                break;

            case "down":
                enemyY += playerSpeed / fps;
                sendCurrentState(playerX, playerY, enemyX, enemyY);
                break;
        }

        // Keep player in bounds
        final double playerMinY = boardHeight - 600;
        final double playerMaxY = boardHeight - 200;

        final double enemyMinY = boardHeight - 600;
        final double enemyMaxY = boardHeight - 200;

        if (playerY < playerMinY) {
            playerY = playerMinY;
        } else if (playerY > playerMaxY) {
            playerY = playerMaxY;
        }

        if (enemyY < enemyMinY) {
            enemyY = enemyMinY;
        } else if (enemyY > enemyMaxY) {
            enemyY = enemyMaxY;
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

        sendCurrentStateBall(ballNextX, ballNextY);

        // Check ball collision with board sides
        final double[][] lineBall = { { ballX, ballY }, { ballNextX, ballNextY } };

        final double[][] lineBoardTop = { { 0, borderSize }, { boardWidth, borderSize } };
        final double[] intersectionTop = findIntersection(lineBall, lineBoardTop);

        final double[][] lineBoardBottom = { { 0, boardHeight - borderSize },
                { boardWidth, boardHeight - borderSize } };
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

            if (ballNextY >= boardHeight - borderSize) {
                switch (ballDirection) {
                    case "downRight":
                        ballDirection = "upRight";
                        break;
                    case "downLeft":
                        ballDirection = "upLeft";
                        break;
                }

                ballX = intersectionBottom[0];
                ballY = intersectionBottom[1] - 1;
            } else {
                ballX = ballNextX;
                ballY = ballNextY;
            }

        } else {
            if (ballNextX < 0) {
                if (playerPoints > 4 || enemyPoints > 4) {
                    gameOver = true;
                } else {
                    playerPoints += 1;
                    ballX = boardWidth / 2;
                    ballY = boardHeight / 2;
                }

            } else if (ballNextX > boardWidth) {
                if (playerPoints > 4 || enemyPoints > 4) {
                    gameOver = true;
                } else {
                    enemyPoints += 1;
                    ballX = boardWidth / 2;
                    ballY = boardHeight / 2;
                }

            } else {
                ballX = ballNextX;
                ballY = ballNextY;
            }
        }

        final double[][] linePlayer = { { playerX, playerY }, { playerX + 5, playerY + 150 } };
        final double[] intersectionPlayer = findIntersection(lineBall, linePlayer);

        if (intersectionPlayer != null) {
            switch (ballDirection) {
                case "upLeft":
                    ballDirection = "upRight";
                    break;
                case "downLeft":
                    ballDirection = "downRight";
                    break;
            }
            ballX = intersectionPlayer[0];
            ballY = intersectionPlayer[1] - 1;
            ballSpeed += ballSpeedIncrement;
            playerSpeed += playerSpeedIncrement;
        }

        final double[][] lineEnemy = { { enemyX, enemyY }, { enemyX + 5, enemyY + 150 } };
        final double[] intersectionEnemy = findIntersection(lineBall, lineEnemy);

        if (intersectionEnemy != null) {
            switch (ballDirection) {
                case "upRight":
                    ballDirection = "upLeft";
                    break;
                case "downRight":
                    ballDirection = "downLeft";
                    break;
            }
            ballX = intersectionEnemy[0];
            ballY = intersectionEnemy[1] + 1;
            ballSpeed += ballSpeedIncrement;
            playerSpeed += playerSpeedIncrement;
        }
    }

    // Dibuixar
    private void draw() {

        // Clean drawing area
        gc.setFill(Color.WHITE);
        gc.clearRect(0, 0, cnv.getWidth(), cnv.getHeight());

        // Draw board
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(borderSize);
        gc.strokeRect(0, 0, cnv.getWidth(), borderSize);
        gc.strokeRect(0, cnv.getHeight() - borderSize, cnv.getWidth(), borderSize);

        drawPlayer(playerX, playerY, Color.GREEN);
        drawPlayer(enemyX, enemyY, Color.PURPLE);

        // Draw ball
        gc.fillArc(ballX - ballHalf, ballY - ballHalf, ballSize, ballSize, 0.0, 360, ArcType.ROUND);

        // Draw text with points
        gc.setFont(new Font("Arial", 40));
        drawText(gc, String.valueOf(playerPoints), (cnv.getWidth() / 2) + 40, 20, "right");
        drawText(gc, "-", cnv.getWidth() / 2, 20, "right");
        drawText(gc, String.valueOf(enemyPoints), 340, 20, "left");

        // Draw game over text
        if (gameOver) {
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

        // Check if the intersection point is within the bounding boxes of both line
        // segments
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

    public void drawPlayer(double x, double y, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(playerWidth);
        gc.strokeRect(x, y, 5, 200);
    }
}