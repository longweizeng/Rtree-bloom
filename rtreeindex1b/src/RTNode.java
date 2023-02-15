import java.util.List;

public abstract class RTNode {
    protected RTree rtree;//������ڵ���
    protected int level;//������ڵĲ�
    protected Rectangle[] datas;//�൱����Ŀ
//	protected int capacity;//��������
    protected RTNode parent;//���ڵ�
    protected int usedSpace;//������õĿռ�
    protected int insertIndex;//��¼���������·������
    protected int deleteIndex;//��¼ɾ���Ĳ���·������
    protected BloomFilter bF;
    protected String nodeHash;//�ڵ��ϣ//1
    protected List vailList;

    public RTNode(RTree rtree, RTNode parent, int level)//1
    {
        this.rtree = rtree;
        this.parent = parent;
        this.level = level;
//		this.capacity = capacity;
        datas = new Rectangle[rtree.getNodeCapacity() + 1];//�����һ�����ڽ�����
        usedSpace = 0;
        //this.nodeHash = nodeHash;//1
    }
    /**
     * @return ���ظ��ڵ�
     */
    public RTNode getParent()
    {
        return parent;
    }

//	/**
//	 * @return �������
//	 */
//	public int getNodeCapacity()
//	{
//		return capacity;
//	}

    /**
     * ���������Rectangle���������Ŀ
     * @param rectangle
     */
    protected void addData(Rectangle rectangle)
    {
        if(usedSpace == rtree.getNodeCapacity())
        {
            throw new IllegalArgumentException("�ڵ��ǿգ�����");
        }
        datas[usedSpace++] = rectangle;
    }

    /**
     * ɾ������еĵ�i����Ŀ
     * @param i
     */
    protected void deleteData(int i)
    {
        if(datas[i + 1] != null)
        {
            System.arraycopy(datas, i + 1, datas, i, usedSpace - i -1);
            datas[usedSpace - 1] = null;
        }
        else
            datas[i] = null;
        usedSpace--;
    }
    /**
     * @param list �洢ɾ�������ʣ����Ŀ
     */
    protected void condenseTree(List<RTNode> list)
    {
        if(isRoot())
        {
            //���ڵ�ֻ��һ����Ŀ�ˣ���ֻ�����ӻ����Һ���
            if(! isLeaf() && usedSpace == 1)
            {
                RTDirNode root = (RTDirNode) this;
                RTNode child = root.getChild(0);
                root.children.remove(this);//gc
                child.parent = null;
//				if(child.level > 0)
//					child.level --;
                rtree.setRoot(child);
//				//���н���level��1����������һ��
//				List<RTNode> nodes = rtree.traversePostOrder(child);
//				for(int i = 0; i < nodes.size(); i ++)
//				{
//					nodes.get(i).level -= 1;
//				}
            }
        }else{
            RTNode parent = getParent();

            int min = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
            if(usedSpace < min)
            {
                parent.deleteData(parent.deleteIndex);//�丸�ڵ���ɾ������Ŀ
                ((RTDirNode)parent).children.remove(this);
                this.parent = null;
                list.add(this);//֮ǰ�Ѿ�������ɾ����
            }else{
                parent.datas[parent.deleteIndex] = getNodeRectangle();
            }
            parent.condenseTree(list);
        }
    }

    /**
     * @param rectangle ���·��ѵ����Rectangle
     * @return �������е���Ŀ������
     */
    protected int[][] quadraticSplit(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("���β���Ϊ��");
        }

        datas[usedSpace] = rectangle;	//����ӽ�ȥ
//		if(this instanceof RTDirNode)
//		{
//			(RTDirNode)(this).children.add()
//		}
        int total = usedSpace + 1;		//�������

        //��Ƿ��ʵ���Ŀ
        int[] mask = new int[total];
        for(int i = 0; i < total; i++)
        {
            mask[i] = 1;
        }

        //ÿ����ֻ����total/2����Ŀ
        int c = total/2 + 1;
        //ÿ�������С��Ŀ����
        int minNodeSize = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
        //����������
        if(minNodeSize < 2)
            minNodeSize = 2;

        //��¼û�б�������Ŀ�ĸ���
        int rem = total;

        int[] group1 = new int[c];//��¼�������Ŀ������
        int[] group2 = new int[c];//��¼�������Ŀ������
        //���ٱ�����ÿ�������Ŀ������
        int i1 = 0, i2 = 0;

        int[] seed = pickSeeds();
        group1[i1 ++] = seed[0];
        group2[i2 ++] = seed[1];
        rem -=2;
        mask[group1[0]] = -1;
        mask[group2[0]] = -1;

        while(rem > 0)
        {
            //��ʣ���������Ŀȫ�����䵽group1���У��㷨��ֹ
            if(minNodeSize - i1 == rem)
            {
                for(int i = 0; i < total; i ++)//�ܹ�rem��
                {
                    if(mask[i] != -1)//��û�б�����
                    {
                        group1[i1 ++] = i;
                        mask[i] = -1;
                        rem --;
                    }
                }
                //��ʣ���������Ŀȫ�����䵽group1���У��㷨��ֹ
            }else if(minNodeSize - i2 == rem)
            {
                for(int i = 0; i < total; i ++)//�ܹ�rem��
                {
                    if(mask[i] != -1)//��û�б�����
                    {
                        group2[i2 ++] = i;
                        mask[i] = -1;
                        rem --;
                    }
                }
            }else
            {
                //��group1��������Ŀ����С�������
                Rectangle mbr1 = (Rectangle) datas[group1[0]].clone();
                for(int i = 1; i < i1; i ++)
                {
                    mbr1 = mbr1.getUnionRectangle(datas[group1[i]]);
                }
                //��group2��������Ŀ���������
                Rectangle mbr2 = (Rectangle) datas[group2[0]].clone();
                for(int i = 1; i < i2; i ++)
                {
                    mbr2 = mbr2.getUnionRectangle(datas[group2[i]]);
                }

                //�ҳ���һ�����з������Ŀ
                long dif = Long.MIN_VALUE;
                long areaDiff1 = 0, areaDiff2 = 0;
                int sel = -1;
                for(int i = 0; i < total; i ++)
                {
                    if(mask[i] != -1)//��û�б��������Ŀ
                    {
                        //�����ÿ����Ŀ����ÿ����֮�������������ѡ�����������������������Ŀ����
                        Rectangle a = mbr1.getUnionRectangle(datas[i]);
                        areaDiff1 = a.getArea() - mbr1.getArea();

                        Rectangle b = mbr2.getUnionRectangle(datas[i]);
                        areaDiff2 = b.getArea() - mbr2.getArea();

                        if(Math.abs(areaDiff1 - areaDiff2) > dif)
                        {
                            dif = Math.abs(areaDiff1 - areaDiff2);
                            sel = i;
                        }
                    }
                }

                if(areaDiff1 < areaDiff2)//�ȱȽ��������
                {
                    group1[i1 ++] = sel;
                }else if(areaDiff1 > areaDiff2)
                {
                    group2[i2 ++] = sel;
                }else if(mbr1.getArea() < mbr2.getArea())//�ٱȽ��������
                {
                    group1[i1 ++] = sel;
                }else if(mbr1.getArea() > mbr2.getArea())
                {
                    group2[i2 ++] = sel;
                }else if(i1 < i2)//���Ƚ���Ŀ����
                {
                    group1[i1 ++] = sel;
                }else if(i1 > i2)
                {
                    group2[i2 ++] = sel;
                }else {
                    group1[i1 ++] = sel;
                }
                mask[sel] = -1;
                rem --;

            }
        }//end while

        int[][] ret = new int[2][];
        ret[0] = new int[i1];
        ret[1] = new int[i2];

        for(int i = 0; i < i1; i ++)
        {
            ret[0][i] = group1[i];
        }
        for(int i = 0; i < i2; i ++)
        {
            ret[1][i] = group2[i];
        }
        return ret;
    }
    /**
     * @return ����������Ŀ�������һ�������������ռ����Ŀ����
     */
    protected int[] pickSeeds()
    {
        long inefficiency = Long.MIN_VALUE;
        int i1 = 0, i2 = 0;
        //
        for(int i = 0; i < usedSpace; i ++)
        {
            for(int j = i + 1; j <= usedSpace; j ++)//ע��˴���jֵ
            {
                Rectangle rectangle = datas[i].getUnionRectangle(datas[j]);
                long d = rectangle.getArea() - datas[i].getArea() - datas[j].getArea();

                if(d > inefficiency)
                {
                    inefficiency = d;
                    i1 = i;
                    i2 = j;
                }
            }
        }
        return new int[]{i1, i2};
    }

    /**
     * @return ���ذ��������������Ŀ����СRectangle
     */
    public Rectangle getNodeRectangle()
    {
        if(usedSpace > 0)
        {
            Rectangle[] rectangles = new Rectangle[usedSpace];
            System.arraycopy(datas, 0, rectangles, 0, usedSpace);
            return Rectangle.getUnionRectangle(rectangles);
        }
        else {
            return new Rectangle(new Point(new long[]{0,0}), new Point(new long[]{0,0}));
        }
    }

    /**
     * @return �Ƿ���ڵ�
     */
    public boolean isRoot()
    {
        return (parent == Constants.NULL);
    }

    /**
     * @return �Ƿ��Ҷ�ӽ��
     */
    public boolean isIndex()
    {
        return (level != 0);
    }

    /**
     * @return �Ƿ�Ҷ�ӽ��
     */
    public boolean isLeaf()
    {
        return (level == 0);
    }


    /**
     * @param
     * @return RTDataNode
     */
    protected abstract RTDataNode chooseLeaf(Rectangle rectangle);

    /**
     * @param rectangle
     * @return ���ذ���rectangle��Ҷ�ڵ�
     */
    protected abstract RTDataNode findLeaf(Rectangle rectangle);
}
