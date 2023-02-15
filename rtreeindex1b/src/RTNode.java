import java.util.List;

public abstract class RTNode {
    protected RTree rtree;//结点所在的树
    protected int level;//结点所在的层
    protected Rectangle[] datas;//相当于条目
//	protected int capacity;//结点的容量
    protected RTNode parent;//父节点
    protected int usedSpace;//结点已用的空间
    protected int insertIndex;//记录插入的搜索路径索引
    protected int deleteIndex;//记录删除的查找路径索引
    protected BloomFilter bF;
    protected String nodeHash;//节点哈希//1
    protected List vailList;

    public RTNode(RTree rtree, RTNode parent, int level)//1
    {
        this.rtree = rtree;
        this.parent = parent;
        this.level = level;
//		this.capacity = capacity;
        datas = new Rectangle[rtree.getNodeCapacity() + 1];//多出的一个用于结点分裂
        usedSpace = 0;
        //this.nodeHash = nodeHash;//1
    }
    /**
     * @return 返回父节点
     */
    public RTNode getParent()
    {
        return parent;
    }

//	/**
//	 * @return 结点容量
//	 */
//	public int getNodeCapacity()
//	{
//		return capacity;
//	}

    /**
     * 向结点中添加Rectangle，即添加条目
     * @param rectangle
     */
    protected void addData(Rectangle rectangle)
    {
        if(usedSpace == rtree.getNodeCapacity())
        {
            throw new IllegalArgumentException("节点是空！！！");
        }
        datas[usedSpace++] = rectangle;
    }

    /**
     * 删除结点中的第i个条目
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
     * @param list 存储删除结点中剩余条目
     */
    protected void condenseTree(List<RTNode> list)
    {
        if(isRoot())
        {
            //根节点只有一个条目了，即只有左孩子或者右孩子
            if(! isLeaf() && usedSpace == 1)
            {
                RTDirNode root = (RTDirNode) this;
                RTNode child = root.getChild(0);
                root.children.remove(this);//gc
                child.parent = null;
//				if(child.level > 0)
//					child.level --;
                rtree.setRoot(child);
//				//所有结点的level减1，即树降低一层
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
                parent.deleteData(parent.deleteIndex);//其父节点中删除此条目
                ((RTDirNode)parent).children.remove(this);
                this.parent = null;
                list.add(this);//之前已经把数据删除了
            }else{
                parent.datas[parent.deleteIndex] = getNodeRectangle();
            }
            parent.condenseTree(list);
        }
    }

    /**
     * @param rectangle 导致分裂的溢出Rectangle
     * @return 两个组中的条目的索引
     */
    protected int[][] quadraticSplit(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("矩形不能为空");
        }

        datas[usedSpace] = rectangle;	//先添加进去
//		if(this instanceof RTDirNode)
//		{
//			(RTDirNode)(this).children.add()
//		}
        int total = usedSpace + 1;		//结点总数

        //标记访问的条目
        int[] mask = new int[total];
        for(int i = 0; i < total; i++)
        {
            mask[i] = 1;
        }

        //每个组只是有total/2个条目
        int c = total/2 + 1;
        //每个结点最小条目个数
        int minNodeSize = Math.round(rtree.getNodeCapacity() * rtree.getFillFactor());
        //至少有两个
        if(minNodeSize < 2)
            minNodeSize = 2;

        //记录没有被检查的条目的个数
        int rem = total;

        int[] group1 = new int[c];//记录分配的条目的索引
        int[] group2 = new int[c];//记录分配的条目的索引
        //跟踪被插入每个组的条目的索引
        int i1 = 0, i2 = 0;

        int[] seed = pickSeeds();
        group1[i1 ++] = seed[0];
        group2[i2 ++] = seed[1];
        rem -=2;
        mask[group1[0]] = -1;
        mask[group2[0]] = -1;

        while(rem > 0)
        {
            //将剩余的所有条目全部分配到group1组中，算法终止
            if(minNodeSize - i1 == rem)
            {
                for(int i = 0; i < total; i ++)//总共rem个
                {
                    if(mask[i] != -1)//还没有被分配
                    {
                        group1[i1 ++] = i;
                        mask[i] = -1;
                        rem --;
                    }
                }
                //将剩余的所有条目全部分配到group1组中，算法终止
            }else if(minNodeSize - i2 == rem)
            {
                for(int i = 0; i < total; i ++)//总共rem个
                {
                    if(mask[i] != -1)//还没有被分配
                    {
                        group2[i2 ++] = i;
                        mask[i] = -1;
                        rem --;
                    }
                }
            }else
            {
                //求group1中所有条目的最小外包矩形
                Rectangle mbr1 = (Rectangle) datas[group1[0]].clone();
                for(int i = 1; i < i1; i ++)
                {
                    mbr1 = mbr1.getUnionRectangle(datas[group1[i]]);
                }
                //求group2中所有条目的外包矩形
                Rectangle mbr2 = (Rectangle) datas[group2[0]].clone();
                for(int i = 1; i < i2; i ++)
                {
                    mbr2 = mbr2.getUnionRectangle(datas[group2[i]]);
                }

                //找出下一个进行分配的条目
                long dif = Long.MIN_VALUE;
                long areaDiff1 = 0, areaDiff2 = 0;
                int sel = -1;
                for(int i = 0; i < total; i ++)
                {
                    if(mask[i] != -1)//还没有被分配的条目
                    {
                        //计算把每个条目加入每个组之后面积的增量，选择两个组面积增量差最大的条目索引
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

                if(areaDiff1 < areaDiff2)//先比较面积增量
                {
                    group1[i1 ++] = sel;
                }else if(areaDiff1 > areaDiff2)
                {
                    group2[i2 ++] = sel;
                }else if(mbr1.getArea() < mbr2.getArea())//再比较自身面积
                {
                    group1[i1 ++] = sel;
                }else if(mbr1.getArea() > mbr2.getArea())
                {
                    group2[i2 ++] = sel;
                }else if(i1 < i2)//最后比较条目个数
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
     * @return 返回两个条目如果放在一起会有最多的冗余空间的条目索引
     */
    protected int[] pickSeeds()
    {
        long inefficiency = Long.MIN_VALUE;
        int i1 = 0, i2 = 0;
        //
        for(int i = 0; i < usedSpace; i ++)
        {
            for(int j = i + 1; j <= usedSpace; j ++)//注意此处的j值
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
     * @return 返回包含结点中所有条目的最小Rectangle
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
     * @return 是否根节点
     */
    public boolean isRoot()
    {
        return (parent == Constants.NULL);
    }

    /**
     * @return 是否非叶子结点
     */
    public boolean isIndex()
    {
        return (level != 0);
    }

    /**
     * @return 是否叶子结点
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
     * @return 返回包含rectangle的叶节点
     */
    protected abstract RTDataNode findLeaf(Rectangle rectangle);
}
