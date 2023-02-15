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
        bF[0] = new BloomFilter();
        bF[1] = new BloomFilter();
        for(int i=0;i<children.size();i++)
        {
            bF[0].getBits().or(children.get(i).bF[0].getBits());
            bF[1].getBits().or(children.get(i).bF[1].getBits());
            datas[i].setbF(children.get(i).bF);
            datas[i].setRectangleHash(children.get(i).nodeHash);
            s += datas[i] + children.get(i).nodeHash + children.get(i).bF[0].getBits().toString() + children.get(i).bF[1].getBits().toString();
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
            bF[0] = new BloomFilter();
            bF[1] = new BloomFilter();
            for(int i=0;i<children.size();i++)
            {
                bF[0].getBits().or(children.get(i).bF[0].getBits());
                bF[1].getBits().or(children.get(i).bF[1].getBits());
                datas[i].setbF(children.get(i).bF);
                datas[i].setRectangleHash(children.get(i).nodeHash);
                s += datas[i] + children.get(i).nodeHash + children.get(i).bF[0].getBits().toString()+children.get(i).bF[1].getBits().toString();
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
            n.bF[0] = new BloomFilter();
            n.bF[1] = new BloomFilter();
            for(int m=0;m<n.children.size();m++)
            {
                n.bF[0].getBits().or(n.children.get(m).bF[0].getBits());
                n.bF[1].getBits().or(n.children.get(m).bF[1].getBits());
                n.datas[m].setbF(n.children.get(m).bF);
                n.datas[m].setRectangleHash(n.children.get(m).nodeHash);
                sn += n.datas[m] + n.children.get(m).nodeHash + n.bF[0].getBits().toString()+n.bF[1].getBits().toString();//��Ҷ�ӽڵ�
            }
            n.nodeHash = CryptoUtil.SHA256(sn);

            String snn = "" ;//1l
            nn.bF[0] = new BloomFilter();
            nn.bF[1] = new BloomFilter();
            for(int m=0;m<nn.children.size();m++)
            {
                nn.bF[0].getBits().or(nn.children.get(m).bF[0].getBits());
                nn.bF[1].getBits().or(nn.children.get(m).bF[1].getBits());
                nn.datas[m].setbF(nn.children.get(m).bF);
                nn.datas[m].setRectangleHash(nn.children.get(m).nodeHash);
                snn += nn.datas[m] +nn.children.get(m).nodeHash + nn.bF[0].getBits().toString()+nn.bF[1].getBits().toString();//��Ҷ�ӽڵ�
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
                newRoot.bF[0] = new BloomFilter();
                newRoot.bF[1] = new BloomFilter();
                for(int i=0;i<newRoot.children.size();i++)
                {
                    newRoot.bF[0].getBits().or(newRoot.children.get(i).bF[0].getBits());
                    newRoot.bF[1].getBits().or(newRoot.children.get(i).bF[1].getBits());
                    newRoot.datas[i].setbF(newRoot.children.get(i).bF);
                    newRoot.datas[i].setRectangleHash(newRoot.children.get(i).nodeHash);
                    s += newRoot.datas[i] + newRoot.children.get(i).nodeHash + newRoot.bF[0].getBits().toString()+newRoot.bF[1].getBits().toString();
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
                bF[0] = new BloomFilter();
                bF[1] = new BloomFilter();
                for(int i=0;i<children.size();i++)
                {
                    datas[i].setbF(children.get(i).bF);
                    bF[0].getBits().or(children.get(i).bF[0].getBits());
                    bF[1].getBits().or(children.get(i).bF[1].getBits());
                    datas[i].setRectangleHash(children.get(i).nodeHash);
                    s += datas[i] + children.get(i).nodeHash + bF[0].getBits().toString()+bF[1].getBits().toString();
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
        index1.bF[0] = new BloomFilter();
        index1.bF[1] = new BloomFilter();
        for(int l=0;l<index1.children.size();l++)
        {
            index1.bF[0].getBits().or(index1.children.get(l).bF[0].getBits());
            index1.bF[1].getBits().or(index1.children.get(l).bF[1].getBits());
            index1.datas[l].setbF(index1.children.get(l).bF);
            index1.datas[l].setRectangleHash(index1.children.get(l).nodeHash);
            s += index1.datas[l] + index1.children.get(l).nodeHash + index1.bF[0].getBits().toString()+index1.bF[1].getBits().toString();
        }
        index1.nodeHash = CryptoUtil.SHA256(s);

        for(int i = 0; i < group2.length; i ++)
        {
            index2.addData(datas[group2[i]]);
            index2.children.add(this.children.get(group2[i]));//�¼ӵ�
            this.children.get(group2[i]).parent = index2;//�¼ӵ�
        }
        String s2 = "";
        index2.bF[0] = new BloomFilter();
        index2.bF[1] = new BloomFilter();
        for(int l=0;l<index2.children.size();l++)
        {
            index2.bF[0].getBits().or(index2.children.get(l).bF[0].getBits());
            index2.bF[1].getBits().or(index2.children.get(l).bF[1].getBits());
            index2.datas[l].setbF(index2.children.get(l).bF);
            index2.datas[l].setRectangleHash(index2.children.get(l).nodeHash);
            s2 += index2.datas[l] + index2.children.get(l).nodeHash + index2.bF[0].getBits().toString()+index2.bF[1].getBits().toString();
        }
        index2.nodeHash = CryptoUtil.SHA256(s2);

        return new RTDirNode[]{index1,index2};
    }

    protected RTDataNode findLeaf(Rectangle rectangle)
    {
        if(!getNodeRectangle().enclosure(rectangle)){
           return null;
        }

        boolean b1 = bF[0].contains(String.valueOf(rectangle.getHigh().getData()[0]));
        boolean b2 = bF[1].contains(String.valueOf(rectangle.getHigh().getData()[1]));
        if(!(b1 && b2 ))
        {
            return null;
        }
        int k=1;
        List list = new ArrayList();
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].enclosure(rectangle))
            {
                boolean b11 = datas[i].getbF()[0].contains(String.valueOf(rectangle.getHigh().getData()[0]));
                boolean b22 = datas[i].getbF()[1].contains(String.valueOf(rectangle.getHigh().getData()[1]));
                if(b11 && b22 ) {
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
                        while (j != t) {
                            list.add(datas[j]);
                            j++;
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
