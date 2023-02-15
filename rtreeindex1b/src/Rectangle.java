
public class Rectangle implements Cloneable {

    private Point low;
    private Point high;
    private String rectangleHash;
    private String data;
    private BloomFilter bF;

    public BloomFilter getbF() {
        return bF;
    }

    public void setbF(BloomFilter bF) {
        this.bF = bF;
    }

    public String getRectangleHash() {
        return rectangleHash;
    }
    public void setRectangleHash(String rectangleHash) {
        this.rectangleHash = rectangleHash;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
    public Rectangle(Point p1, Point p2)
    {
        if(p1 == null || p2 == null)
        {
            throw new IllegalArgumentException("Points cannot be null.");
        }
        if(p1.getDimension() != p2.getDimension())
        {
            throw new IllegalArgumentException("Points must be of same dimension.");
        }
        //�����½Ǻ����Ͻ�
        for(int i = 0; i < p1.getDimension(); i ++)
        {
            if(p1.getFloatCoordinate(i) > p2.getFloatCoordinate(i))
            {
                throw new IllegalArgumentException("�����Ϊ�����½Ǻ����Ͻ�");
            }
        }
        low = (Point) p1.clone();
        high = (Point) p2.clone();
    }

    /**
     * ����Rectangle���½ǵ�Point
     * @return Point
     */
    public Point getLow()
    {
        return (Point) low.clone();
    }

    /**
     * ����Rectangle���Ͻǵ�Point
     * @return Point
     */
    public Point getHigh()
    {
        return high;
    }

    /**
     * @param rectangle
     * @return ��Χ����Rectangle����СRectangle
     */
    public Rectangle getUnionRectangle(Rectangle rectangle)
    {
        if(rectangle == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");

        if(rectangle.getDimension() != getDimension())
        {
            throw new IllegalArgumentException("Rectangle must be of same dimension.");
        }

        long[] min = new long[getDimension()];
        long[] max = new long[getDimension()];

        for(int i = 0; i < getDimension(); i ++)
        {
            min[i] = Math.min(low.getFloatCoordinate(i), rectangle.low.getFloatCoordinate(i));
            max[i] = Math.max(high.getFloatCoordinate(i), rectangle.high.getFloatCoordinate(i));
        }

        return new Rectangle(new Point(min), new Point(max));
    }

    /**
     * @return ����Rectangle�����
     */
    public long getArea()
    {
        long area = 1;
        for(int i = 0; i < getDimension(); i ++)
        {
            area *= high.getFloatCoordinate(i) - low.getFloatCoordinate(i);
        }

        return area;
    }

    /**
     * @param rectangles
     * @return ��Χһϵ��Rectangle����СRectangle
     */
    public static Rectangle getUnionRectangle(Rectangle[] rectangles)
    {
        if(rectangles == null || rectangles.length == 0)
            throw new IllegalArgumentException("Rectangle array is empty.");

        Rectangle r0 = (Rectangle) rectangles[0].clone();
        for(int i = 1; i < rectangles.length; i ++)
        {
            r0 = r0.getUnionRectangle(rectangles[i]);
        }

        return r0;
    }

    @Override
    protected Object clone()
    {
        Point p1 = (Point) low.clone();
        Point p2 = (Point) high.clone();
        return new Rectangle(p1, p2);
    }

    @Override
    public String toString()
    {
        return "���ε����½�:" + low + " ���Ͻ�:" + high;
    }

    public static void main(String[] args)
    {

    }

    /**
     * ����Rectangle�ཻ�����
     * @param rectangle Rectangle
     * @return float
     */
    public long intersectingArea(Rectangle rectangle)
    {
        if(! isIntersection(rectangle))
        {
            return 0;
        }

        long ret = 1;
        for(int i = 0; i < rectangle.getDimension(); i ++)
        {
            long l1 = this.low.getFloatCoordinate(i);
            long h1 = this.high.getFloatCoordinate(i);
            long l2 = rectangle.low.getFloatCoordinate(i);
            long h2 = rectangle.high.getFloatCoordinate(i);

            //rectangle1��rectangle2�����
            if(l1 <= l2 && h1 <= h2)
            {
                ret *= (h1 - l1) - (l2 - l1);
            }else if(l1 >= l2 && h1 >= h2)
            //rectangle1��rectangle2���ұ�
            {
                ret *= (h2 - l2) - (l1 - l2);
            }else if(l1 >= l2 && h1 <= h2)
            //rectangle1��rectangle2����
            {
                ret *= h1 - l1;
            }else if(l1 <= l2 && h1 >= h2)
            //rectangle1����rectangle2
            {
                ret *= h2 - l2;
            }
        }
        return ret;
    }

    /**
     * @param rectangle
     * @return �ж�����Rectangle�Ƿ��ཻ
     */
    public boolean isIntersection(Rectangle rectangle)
    {
        if(rectangle == null)
            throw new IllegalArgumentException("���β���Ϊ��");

        if(rectangle.getDimension() != getDimension())
        {
            throw new IllegalArgumentException("���α�����ͬά��");
        }


        for(int i = 0; i < getDimension(); i ++)
        {
            if(low.getFloatCoordinate(i) > rectangle.high.getFloatCoordinate(i) ||
                    high.getFloatCoordinate(i) < rectangle.low.getFloatCoordinate(i))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return ����Rectangle��ά��
     */
    private int getDimension()
    {
        return low.getDimension();
    }

    /**
     * �ж�rectangle�Ƿ񱻰�Χ
     * @param rectangle
     * @return
     */
    public boolean enclosure(Rectangle rectangle)
    {
        if(rectangle == null)
            throw new IllegalArgumentException("���β���Ϊ��");

        if(rectangle.getDimension() != getDimension())
            throw new IllegalArgumentException("���α�����ͬά��");

        for(int i = 0; i < getDimension(); i ++)
        {
            if(rectangle.low.getFloatCoordinate(i) < low.getFloatCoordinate(i) ||
                    rectangle.high.getFloatCoordinate(i) > high.getFloatCoordinate(i))
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Rectangle)
        {
            Rectangle rectangle = (Rectangle) obj;
            if(low.equals(rectangle.getLow()) && high.equals(rectangle.getHigh()))
                return true;
        }
        return false;
    }
}
