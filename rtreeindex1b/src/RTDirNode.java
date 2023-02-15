import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RTDirNode extends RTNode{//��Ҷ�ӽڵ�
    protected List<RTNode> children;

    public RTDirNode(RTree rtree, RTNode parent, int level)//1
    {
        super(rtree, parent, level);//1
        children = new ArrayList<RTNode>();
    }

    /**
     * @param index
     * @return ��Ӧ�����µĺ��ӽ��
     */
    public RTNode getChild(int index)
    {
        return children.get(index);
    }

    @Override
    public RTDataNode chooseLeaf(Rectangle rectangle)
    {
        int index;

        switch (rtree.getTreeType())
        {
            case Constants.RTREE_LINEAR://����

            case Constants.RTREE_QUADRATIC://��ά

            case Constants.RTREE_EXPONENTIAL://��ά
                index = findLeastEnlargement(rectangle);
                break;
            case Constants.RSTAR:
                if(level == 1)//���˽��ָ��Ҷ�ڵ�
                {
                    index = findLeastOverlap(rectangle);
                }else{
                    index = findLeastEnlargement(rectangle);
                }
                break;

            default:
                throw new IllegalStateException("Invalid tree type.");
        }

        insertIndex = index;//��¼����·��������

        return getChild(index).chooseLeaf(rectangle);
    }

    /**
     * @param rectangle
     * @return ������С�ص�����Ľ�������������ص���������ѡ������Rectangle�����������С�ģ������������������ѡ�����������С��
     */
    private int findLeastOverlap(Rectangle rectangle)
    {
        long overlap = Long.MAX_VALUE;
        int sel = -1;

        for(int i = 0; i < usedSpace; i ++)
        {
            RTNode node = getChild(i);
            long ol = 0;

            for(int j = 0; j < node.datas.length; j ++)
            {
                ol += rectangle.intersectingArea(node.datas[j]);
            }
            if(ol < overlap)
            {
                overlap = ol;//��¼�ص������С��
                sel = i;//��¼�ڼ������ӵ�����
            }else if(ol == overlap)//����ص���������ѡ������Rectangle�����������С��,�����������������ѡ�����������С��
            {
                long area1 = datas[i].getUnionRectangle(rectangle).getArea() - datas[i].getArea();
                long area2 = datas[sel].getUnionRectangle(rectangle).getArea() - datas[sel].getArea();

                if(area1 == area2)
                {
                    sel = (datas[sel].getArea() <= datas[i].getArea()) ? sel : i;
                }else{
                    sel = (area1 < area2) ? i : sel;
                }
            }
        }
        return sel;
    }

    /**
     * @param rectangle
     * @return ���������С�Ľ������������������������ѡ�����������С��
     */
    private int findLeastEnlargement(Rectangle rectangle)
    {
        long area = Long.MAX_VALUE;
        int sel = -1;

        for(int i = 0; i < usedSpace; i ++)
        {
            long enlargement = datas[i].getUnionRectangle(rectangle).getArea() - datas[i].getArea();
            if(enlargement < area)
            {
                area = enlargement;
                sel = i;
            }else if(enlargement == area)
            {
                sel = (datas[sel].getArea() < datas[i].getArea()) ? sel : i;
            }
        }

        return sel;
    }

    /**
     * �����µ�Rectangle��Ӳ����Ҷ�ڵ㿪ʼ���ϵ���RTree��ֱ�����ڵ�
     * @param node1 ������Ҫ�����ĺ��ӽ��
     * @param node2  ���ѵĽ�㣬��δ������Ϊnull
     */
    public void adjustTree(RTNode node1, RTNode node2)
    {
        //��Ҫ�ҵ�ָ��ԭ���ɵĽ�㣨��δ���Rectangle֮ǰ������Ŀ������
        datas[insertIndex] = node1.getNodeRectangle();//����node1����ԭ���Ľ��
        children.set(insertIndex, node1);//�滻�ɵĽ��

        String s = "";
        bF = new BloomFilter();
        for(int i=0;i<children.size();i++)
        {
            bF.getBits().or(children.get(i).bF.getBits());
            datas[i].setbF(children.get(i).bF);
            datas[i].setRectangleHash(children.get(i).nodeHash);
            s += datas[i] + children.get(i).nodeHash + children.get(i).bF.getBits().toString();
        }
        nodeHash = CryptoUtil.SHA256(s);

        if(node2 != null)
        {
            insert(node2);//�����µĽ��
        }else if(!isRoot())//��û������ڵ�
        {
            RTDirNode parent = (RTDirNode) getParent();
            parent.adjustTree(this, null);//���ϵ���ֱ�����ڵ�
        }
    }

    /**
     * @param node
     * @return ��������Ҫ�����򷵻�true
     */
    protected boolean insert(RTNode node)
    {
        if(usedSpace < rtree.getNodeCapacity())
        {
            datas[usedSpace++] = node.getNodeRectangle();
            children.add(node);//�¼ӵ�
            node.parent = this;//�¼ӵ�

            String s = "";
            bF = new BloomFilter();
            for(int i=0;i<children.size();i++)
            {
                bF.getBits().or(children.get(i).bF.getBits());
                datas[i].setbF(children.get(i).bF);
                datas[i].setRectangleHash(children.get(i).nodeHash);
                s += datas[i] + children.get(i).nodeHash + children.get(i).bF.getBits().toString();
            }
            nodeHash = CryptoUtil.SHA256(s);

            RTDirNode parent = (RTDirNode) getParent();
            if(parent != null)
            {
                parent.adjustTree(this, null);
            }
            return false;
        }else{//��Ҷ�ӽ����Ҫ����
            RTDirNode[] a = splitIndex(node);
            RTDirNode n = a[0];
            RTDirNode nn = a[1];

            String sn = "" ;//1
            n.bF = new BloomFilter();
            for(int m=0;m<n.children.size();m++)
            {
                n.bF.getBits().or(n.children.get(m).bF.getBits());
                n.datas[m].setbF(n.children.get(m).bF);
                n.datas[m].setRectangleHash(n.children.get(m).nodeHash);
                sn += n.datas[m] + n.children.get(m).nodeHash + n.children.get(m).bF.getBits().toString();//��Ҷ�ӽڵ�
            }
            n.nodeHash = CryptoUtil.SHA256(sn);

            String snn = "" ;//1l
            nn.bF = new BloomFilter();
            for(int m=0;m<nn.children.size();m++)
            {
                nn.bF.getBits().or(nn.children.get(m).bF.getBits());
                nn.datas[m].setbF(nn.children.get(m).bF);
                nn.datas[m].setRectangleHash(nn.children.get(m).nodeHash);
                snn += nn.datas[m] + nn.children.get(m).nodeHash + nn.children.get(m).bF.getBits().toString();//��Ҷ�ӽڵ�
            }
            nn.nodeHash = CryptoUtil.SHA256(snn);

            if(isRoot())
            {
                //�½����ڵ㣬������1
                RTDirNode newRoot = new RTDirNode(rtree, Constants.NULL, level + 1);//1
                //����Ҫ��ԭ�����ĺ�����ӵ��������ѵĽ��n��nn�У���ʱn��nn�ĺ��ӽ�㻹Ϊ��
//				for(int i = 0; i < n.usedSpace; i ++)
//				{
//					n.children.add(this.children.get(index));
//				}
//				for(int i = 0; i < nn.usedSpace; i ++)
//				{
//					nn.children.add(this.children.get(index));
//				}

                //���������ѵĽ��n��nn��ӵ����ڵ�
                newRoot.addData(n.getNodeRectangle());
                newRoot.addData(nn.getNodeRectangle());
                newRoot.children.add(n);
                newRoot.children.add(nn);
                //�����������ѵĽ��n��nn�ĸ��ڵ�
                n.parent = newRoot;
                nn.parent = newRoot;
                //�������rtree�ĸ��ڵ�
                String s = "";
                newRoot.bF = new BloomFilter();
                for(int i=0;i<newRoot.children.size();i++)
                {
                    newRoot.bF.getBits().or(newRoot.children.get(i).bF.getBits());
                    newRoot.datas[i].setbF(newRoot.children.get(i).bF);
                    newRoot.datas[i].setRectangleHash(newRoot.children.get(i).nodeHash);
                    s += newRoot.datas[i] + newRoot.children.get(i).nodeHash +newRoot.children.get(i).bF.getBits().toString() ;
                }
                newRoot.nodeHash = CryptoUtil.SHA256(s);

                rtree.setRoot(newRoot);//�¼ӵ�
            }else {
                RTDirNode p = (RTDirNode) getParent();
                p.adjustTree(n,nn);
            }
        }
        return true;
    }
    /**
     * ��Ҷ�ӽ��ķ���
     * @param node
     * @return
     */
    private RTDirNode[] splitIndex(RTNode node)
    {
        int[][] group = null;

        switch (rtree.getTreeType())
        {
            case Constants.RTREE_LINEAR://����
                break;
            case Constants.RTREE_QUADRATIC://��ά

            case Constants.RTREE_EXPONENTIAL://��ά
                group = quadraticSplit(node.getNodeRectangle());
                children.add(node);//�¼ӵ�
                node.parent = this;//�¼ӵ�

                String s = "";
                bF = new BloomFilter();
                for(int i=0;i<children.size();i++)
                {
                    bF.getBits().or(children.get(i).bF.getBits());
                    datas[i].setbF(children.get(i).bF);
                    datas[i].setRectangleHash(children.get(i).nodeHash);
                    s += datas[i] + children.get(i).nodeHash + children.get(i).bF.getBits().toString();
                }
                nodeHash = CryptoUtil.SHA256(s);

                break;
            case Constants.RSTAR://����
                break;
            default:
                throw new IllegalStateException("Invalid tree type.");
        }

        RTDirNode index1 = new RTDirNode(rtree, parent, level);//1
        RTDirNode index2 = new RTDirNode(rtree, parent, level);//1

        int[] group1 = group[0];
        int[] group2 = group[1];

        for(int i = 0; i < group1.length; i ++)
        {
            //Ϊindex1������ݺͺ���
            index1.addData(datas[group1[i]]);
            index1.children.add(this.children.get(group1[i]));//�¼ӵ�
            //��index1��Ϊ�丸�ڵ�
            this.children.get(group1[i]).parent = index1;//�¼ӵ�
        }
        String s = "";
        index1.bF = new BloomFilter();
        for(int l=0;l<index1.children.size();l++)
        {
            index1.bF.getBits().or(index1.children.get(l).bF.getBits());
            index1.datas[l].setbF(index1.children.get(l).bF);
            index1.datas[l].setRectangleHash(index1.children.get(l).nodeHash);
            s +=  index1.datas[l] + index1.children.get(l).nodeHash + index1.children.get(l).bF.getBits().toString();
        }
        index1.nodeHash = CryptoUtil.SHA256(s);

        for(int i = 0; i < group2.length; i ++)
        {
            index2.addData(datas[group2[i]]);
            index2.children.add(this.children.get(group2[i]));//�¼ӵ�
            this.children.get(group2[i]).parent = index2;//�¼ӵ�
        }
        String s2 = "";
        index2.bF = new BloomFilter();
        for(int l=0;l<index2.children.size();l++)
        {
            index2.bF.getBits().or(index2.children.get(l).bF.getBits());
            index2.datas[l].setbF(index2.children.get(l).bF);
            index2.datas[l].setRectangleHash(index2.children.get(l).nodeHash);
            s2 += index2.datas[l] + index2.children.get(l).nodeHash + index2.children.get(l).bF.getBits().toString();
        }
        index2.nodeHash = CryptoUtil.SHA256(s2);

        return new RTDirNode[]{index1,index2};
    }
    protected RTDataNode findLeaf(Rectangle rectangle)
    {
        if(!getNodeRectangle().enclosure(rectangle)){
           return null;
        }
        boolean b1 = bF.contains(String.valueOf(2*rectangle.getHigh().getData()[0]+8*rectangle.getHigh().getData()[1]));
        if(!b1)
        {
            return null;
        }
        List list = new ArrayList();
        int k=1;
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].enclosure(rectangle))
            {
                if(datas[i].getbF().contains(String.valueOf(2*rectangle.getHigh().getData()[0]+8*rectangle.getHigh().getData()[1])))
                {
                    int j = 0;
                    int t = i;
                    if (list.contains(k)) {
                        int index = list.indexOf(k);
                        list.remove(index);
                        list.add(index, "<");
                        index++;
                        while (j != i) {
                            list.add(index, datas[j]);
                            index++;
                            j++;
                        }
                        list.add(index, k);
                        index++;
                        while (t + 1 < usedSpace) {
                            list.add(index, datas[t + 1]);
                            index++;
                            t++;
                        }
                        list.add(index, ">");
                    } else {
                        list.add("<");
                        while (j - 1 >= 0) {
                            list.add(datas[j - 1]);
                            j--;
                        }
                        list.add(k);
                        while (t + 1 < usedSpace) {
                            list.add(datas[t + 1]);
                            t++;
                        }
                        list.add(">");
                    }
                    //System.out.println("tt"+list);
                    deleteIndex = i;//��¼����·��
                    RTDataNode leaf = children.get(i).findLeaf(rectangle);
                    if (leaf != null) {
                        if (list.contains(k)) {
                            int index = list.indexOf(k);
                            list.remove(index);
                            list.add(index, "<");
                            index++;
                            for (int l = 0; l < leaf.usedSpace; l++) {
                                list.add(index, leaf.datas[l].getData());
                                index++;
                            }
                            list.add(index, ">");
                            //System.out.println("dfadfa");
                        }
                        leaf.vailList = list;
                        return leaf;
                    }
                }
            }
        }
        return null;
    }

}
