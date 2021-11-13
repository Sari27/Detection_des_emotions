package testpolyface.com.testpolyface.graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class LineOverlay extends GraphicOverlay.Graphic
{
    private Paint dotPaint;

    private GraphicOverlay graphicOverlay;
    private float startX,startY,stopX,stopY;

    public LineOverlay(GraphicOverlay graphicOverlay, float STARTX, float STARTY, float STOPX, float STOPY)
    {

        super(graphicOverlay);
        dotPaint = new Paint();
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.GREEN);
        postInvalidate();
        this.graphicOverlay = graphicOverlay;
        startX = STARTX;
        startY = STARTY;
        stopX = STOPX;
        stopY = STOPY;
    }

    @Override
    public void draw(Canvas canvas)
    {
       canvas.drawLine(startX,startY,stopX,stopY,dotPaint);
    }
}
