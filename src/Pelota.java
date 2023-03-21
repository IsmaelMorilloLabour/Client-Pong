import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class Pelota {
    
    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;
    
    private double radius;
    private Color color;
    private Circle shape;

    public Pelota(double positionX, double positionY, double radius, Color color) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        this.color = color;
        this.velocityX = 5;
        this.velocityY = 5;
        this.shape = new Circle(radius, color);
    }

    public void addToCanvas(Canvas canvas) {
        // TODO Auto-generated method stub
    }

    public void setPosition(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.shape.setCenterX(positionX);
        this.shape.setCenterY(positionY);
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public Circle getShape() {
        return shape;
    }

    public void move() {
        positionX += velocityX;
        positionY += velocityY;
        shape.setCenterX(positionX);
        shape.setCenterY(positionY);
    }

    public void bounceOffWall() {
        velocityX = -velocityX;
    }

    public void bounceOffPaddle(Pala pala) {
        velocityX = -velocityX;
        velocityY += (positionY - pala.getPositionY() - pala.getHeight() / 2) / 10;
    }
}