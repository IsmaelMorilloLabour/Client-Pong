import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Pala {

    private double width;
    private double height;
    private double positionX;
    private double positionY;

    private Color color;
    private Rectangle shape;

    public Pala(double width, double height, Color color) {
        this.width = width;
        this.height = height;
        this.color = color;
        this.shape = new Rectangle(width, height, color);
    }

    public void setPosition(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        shape.setX(positionX);
        shape.setY(positionY);
    }

    public double getPositionY() {
        return positionY;
    }

    public double getHeight() {
        return height;
    }

    public Rectangle getShape() {
        return shape;
    }

    public void moveUp(double speed) {
        if (positionY >= speed) {
            positionY -= speed;
            shape.setY(positionY);
        }
    }

    public void moveDown(double speed) {
        /**
         * 
         if (positionY <= CtrlGame.height - height - speed) {
             positionY += speed;
             shape.setY(positionY);
            }
            */
    }

}
