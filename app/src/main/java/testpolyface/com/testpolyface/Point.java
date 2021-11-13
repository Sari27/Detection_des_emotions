package testpolyface.com.testpolyface;

/**
 * Point class represent a 2D point
 */
public class Point
{
    private double x;
    private double y;

    public Point(double X, double Y)
    {
        this.x = X;
        this.y = Y;
    }

    /**
     * Compare current point with reference point on y axis when the reference point is located under the current point ( Ref.y > Actual.y)
     * @param ref
     * @return
     */
    public double compareUpSideY(Point ref)
    {
        return (ref.getY()-y);
    }

    /**
     * Compare current point with reference point on y axis when the reference point is located above the current point ( Actual.y > Ref.y)
     * @param ref
     * @return
     */
    public double compareDownSideY(Point ref)
    {
        return (y - ref.getY());
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString()
    {
        String toReturn="";
        toReturn = "X:"+x+" Y:"+y;
        return toReturn;
    }
}
