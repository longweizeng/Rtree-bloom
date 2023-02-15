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
            throw new IllegalArgumentException("���겻��Ϊ��");
        }
        if(data.length < 2)
        {
            throw new IllegalArgumentException("���ά�ȱ������1");
        }

        this.data = new long[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public Point(int[] data)
    {
        if(data == null)
        {
            throw new IllegalArgumentException("���겻��Ϊ��");
        }
        if(data.length < 2)
        {
            throw new IllegalArgumentException("���ά�ȱ������1");
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
     * @return ����Point��ά��
     */
    public int getDimension()
    {
        return data.length;
    }

    /**
     * @param index
     * @return ����Point�����iλ��floatֵ
     */
    public long getFloatCoordinate(int index)
    {
        return data[index];
    }

    /**
     * @param index
     * @return ����Point�����iλ��intֵ
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
                throw new IllegalArgumentException("ά����ͬ�ĵ���ܱȽ�");

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
