public class Point implements Cloneable{

    private long[] data;

    public long[] getData() {
        return data;
    }

    public void setData(long[] data) {
        this.data = data;
    }

    public  Point(long[] data)
    {
        if(data == null)
        {
            throw new IllegalArgumentException("坐标不能为空");
        }
        if(data.length < 2)
        {
            throw new IllegalArgumentException("点的维度必须大于1");
        }

        this.data = new long[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public Point(int[] data)
    {
        if(data == null)
        {
            throw new IllegalArgumentException("坐标不能为空");
        }
        if(data.length < 2)
        {
            throw new IllegalArgumentException("点的维度必须大于1");
        }

        this.data = new long[data.length];
        for(int i = 0 ; i < data.length ; i ++)
        {
            this.data[i] = data[i];
        }
    }

    @Override
    protected Object clone()
    {
        long[] copy = new long[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return new Point(copy);
    }

    @Override
    public String toString()
    {
        StringBuffer sBuffer = new StringBuffer("(");

        for(int i = 0 ; i < data.length - 1 ; i ++)
        {
            sBuffer.append(data[i]).append(",");
        }

        sBuffer.append(data[data.length - 1]).append(")");

        return sBuffer.toString();
    }

    public static void main(String[] args)
    {

    }

    /**
     * @return 返回Point的维度
     */
    public int getDimension()
    {
        return data.length;
    }

    /**
     * @param index
     * @return 返回Point坐标第i位的float值
     */
    public long getFloatCoordinate(int index)
    {
        return data[index];
    }

    /**
     * @param index
     * @return 返回Point坐标第i位的int值
     */
    public int getIntCoordinate(int index)
    {
        return (int) data[index];
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Point)
        {
            Point point = (Point) obj;

            if(point.getDimension() != getDimension())
                throw new IllegalArgumentException("维度相同的点才能比较");

            for(int i = 0; i < getDimension(); i ++)
            {
                if(getFloatCoordinate(i) != point.getFloatCoordinate(i))
                    return false;
            }
        }

        if(!(obj instanceof Point))
            return false;

        return true;
    }
}
