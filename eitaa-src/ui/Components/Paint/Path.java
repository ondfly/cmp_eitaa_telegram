package ir.eitaa.ui.Components.Paint;

import java.util.Arrays;
import java.util.Vector;

public class Path
{
  private float baseWeight;
  private Brush brush;
  private int color;
  private Vector<Point> points = new Vector();
  public double remainder;
  
  public Path(Point paramPoint)
  {
    this.points.add(paramPoint);
  }
  
  public Path(Point[] paramArrayOfPoint)
  {
    this.points.addAll(Arrays.asList(paramArrayOfPoint));
  }
  
  public float getBaseWeight()
  {
    return this.baseWeight;
  }
  
  public Brush getBrush()
  {
    return this.brush;
  }
  
  public int getColor()
  {
    return this.color;
  }
  
  public int getLength()
  {
    if (this.points == null) {
      return 0;
    }
    return this.points.size();
  }
  
  public Point[] getPoints()
  {
    Point[] arrayOfPoint = new Point[this.points.size()];
    this.points.toArray(arrayOfPoint);
    return arrayOfPoint;
  }
  
  public void setup(int paramInt, float paramFloat, Brush paramBrush)
  {
    this.color = paramInt;
    this.baseWeight = paramFloat;
    this.brush = paramBrush;
  }
}


/* Location:              /dex2jar/eitaa-dex2jar.jar!/ir/eitaa/ui/Components/Paint/Path.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */