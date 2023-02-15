
import java.util.ArrayList;
import java.util.List;

public class RTDataNode extends RTNode {//Ҷ�ӽڵ�
    public RTDataNode(RTree rTree, RTNode parent)
    {
        super(rTree, parent, 0);//1
    }
    /**
     * @param rectangle
     * @return
     */
    public boolean insert(Rectangle rectangle)
    {
        if(usedSpace < rtree.getNodeCapacity())
        {
            datas[usedSpace++] = rectangle;

            String s = "" ;//1
            bF[0] = new BloomFilter();
            bF[1] = new BloomFilter();
            for(int m=0;m<usedSpace;m++)
            {
                bF[0].getBits().or(datas[m].getbF()[0].getBits());
                bF[1].getBits().or(datas[m].getbF()[1].getBits());
                s += datas[m].getRectangleHash();//Ҷ�ӽڵ�
            }
            nodeHash = CryptoUtil.SHA256(s);//1
            RTDirNode parent = (RTDirNode) getParent();

            if(parent != null)
                parent.adjustTree(this, null);
            return true;

        }else{//�����������
            RTDataNode[] splitNodes = splitLeaf(rectangle);
            RTDataNode l = splitNodes[0];
            RTDataNode ll = splitNodes[1];
            String sl = "" ;//1
            l.bF[0] = new BloomFilter();
            l.bF[1] = new BloomFilter();
            for(int m=0;m<l.usedSpace;m++)
            {
                l.bF[0].getBits().or(l.datas[m].getbF()[0].getBits());
                l.bF[1].getBits().or(l.datas[m].getbF()[1].getBits());
                sl += l.datas[m].getRectangleHash();//Ҷ�ӽڵ�
            }
            l.nodeHash = CryptoUtil.SHA256(sl);

            String sll = "" ;//1l
            ll.bF[0] = new BloomFilter();
            ll.bF[1] = new BloomFilter();
            for(int m=0;m<ll.usedSpace;m++)
            {
                ll.bF[0].getBits().or(ll.datas[m].getbF()[0].getBits());
                ll.bF[1].getBits().or(ll.datas[m].getbF()[1].getBits());
                sll += ll.datas[m].getRectangleHash();//Ҷ�ӽڵ�
            }
            ll.nodeHash = CryptoUtil.SHA256(sll);

            if(isRoot())
            {
                //���ڵ���������Ҫ���ѡ������µĸ��ڵ�
                RTDirNode rDirNode = new RTDirNode(rtree, Constants.NULL, level + 1);//1
                rtree.setRoot(rDirNode);
                rDirNode.addData(l.getNodeRectangle());
                rDirNode.addData(ll.getNodeRectangle());
                l.parent = rDirNode;
                ll.parent = rDirNode;
                rDirNode.children.add(l);
                rDirNode.children.add(ll);
                rDirNode.datas[0].setRectangleHash(l.nodeHash);
                rDirNode.datas[1].setRectangleHash(ll.nodeHash);

                String s = "";
                rDirNode.bF[0] = new BloomFilter();
                rDirNode.bF[1] = new BloomFilter();
                String ssl = l.getNodeRectangle() + l.nodeHash + l.bF[0].getBits().toString() +l.bF[1].getBits().toString();
                String ssll = ll.getNodeRectangle() + ll.nodeHash + ll.bF[0].getBits().toString() +ll.bF[1].getBits().toString();
                s += ssl + ssll;

                rDirNode.bF[0].getBits().or(l.bF[0].getBits());
                rDirNode.bF[0].getBits().or(ll.bF[0].getBits());
                rDirNode.bF[1].getBits().or(l.bF[1].getBits());
                rDirNode.bF[1].getBits().or(ll.bF[1].getBits());


                rDirNode.nodeHash = CryptoUtil.SHA256(s);
            }
            else{//���Ǹ��ڵ�
                RTDirNode parentNode = (RTDirNode) getParent();
                parentNode.adjustTree(l, ll);
            }


        }
        return true;
    }

    /**
     * ����Rectangle֮�󳬹�������Ҫ����
     * @param rectangle
     * @return
     */
    public RTDataNode[] splitLeaf(Rectangle rectangle)
    {
        int[][] group = null;

        switch(rtree.getTreeType())
        {
            case Constants.RTREE_LINEAR://����
                break;
            case Constants.RTREE_QUADRATIC://��ά

            case Constants.RTREE_EXPONENTIAL://��ά
                group = quadraticSplit(rectangle);
                break;
            case Constants.RSTAR://����
                break;
            default:
                throw new IllegalArgumentException("Invalid tree type.");
        }

        RTDataNode l = new RTDataNode(rtree, parent);
        RTDataNode ll = new RTDataNode(rtree, parent);

        int[] group1 = group[0];
        int[] group2 = group[1];

        for(int i = 0; i < group1.length; i++)
        {
            l.addData(datas[group1[i]]);
        }
        String s = "";
        l.bF[0] = new BloomFilter();
        l.bF[1] = new BloomFilter();
        for(int i = 0; i < l.usedSpace; i++)
        {
            l.bF[0].getBits().or(l.datas[i].getbF()[0].getBits());
            l.bF[1].getBits().or(l.datas[i].getbF()[1].getBits());
            s += l.datas[i].getRectangleHash();
        }
        l.nodeHash = CryptoUtil.SHA256(s);

        for(int i = 0; i < group2.length; i++)
        {
            ll.addData(datas[group2[i]]);
        }
        String s2 = "";
        ll.bF[0] = new BloomFilter();
        ll.bF[1] = new BloomFilter();
        for(int i = 0; i < ll.usedSpace; i++)
        {
            ll.bF[0].getBits().or(ll.datas[i].getbF()[0].getBits());
            ll.bF[1].getBits().or(ll.datas[i].getbF()[1].getBits());
            s2 += ll.datas[i].getRectangleHash();
        }
        ll.nodeHash = CryptoUtil.SHA256(s2);

        return new RTDataNode[]{l, ll};
    }

    @Override
    public RTDataNode chooseLeaf(Rectangle rectangle)
    {
        insertIndex = usedSpace;//��¼����·��������
        return this;
    }

    /**
     * @param rectangle
     * @return
     */
    protected int delete(Rectangle rectangle)
    {
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].equals(rectangle))
            {
                deleteData(i);
                List<RTNode> deleteEntriesList = new ArrayList<RTNode>();
                condenseTree(deleteEntriesList);

                //���²���ɾ�������ʣ�����Ŀ
                for(int j = 0; j < deleteEntriesList.size(); j ++)
                {
                    RTNode node = deleteEntriesList.get(j);
                    if(node.isLeaf())//Ҷ�ӽ�㣬ֱ�Ӱ����ϵ��������²���
                    {
                        for(int k = 0; k < node.usedSpace; k ++)
                        {
                            rtree.insert(node.datas[k]);
                        }
                    }else{//��Ҷ�ӽ�㣬��Ҫ�Ⱥ�����������ϵ����н��
                        List<RTNode> traverseNodes = rtree.traversePostOrder(node);

                        //�����е�Ҷ�ӽ���е���Ŀ���²���
                        for(int index = 0; index < traverseNodes.size(); index ++)
                        {
                            RTNode traverseNode = traverseNodes.get(index);
                            if(traverseNode.isLeaf())
                            {
                                for(int t = 0; t < traverseNode.usedSpace; t ++)
                                {
                                    rtree.insert(traverseNode.datas[t]);
                                }
                            }
                        }

                    }
                }

                return deleteIndex;
            }//end if
        }//end for
        return -1;
    }

    @Override
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
        for(int i = 0; i < usedSpace; i ++)
        {
            if(datas[i].enclosure(rectangle))
            {
                boolean b11 = datas[i].getbF()[0].contains(String.valueOf(rectangle.getHigh().getData()[0]));
                boolean b22 = datas[i].getbF()[1].contains(String.valueOf(rectangle.getHigh().getData()[1]));
                if(b11 && b22)
                {
                    deleteIndex = i;//��¼����·��
                    return this;
                }
            }
        }
        return null;
    }

}
