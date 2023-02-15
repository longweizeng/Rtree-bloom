import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RTree {
    private RTNode root;//���ڵ�
    private int tree_type;//������
    private int nodeCapacity = -1;//�������
    private float fillFactor = -1;//����������
    private int dimension ;//ά��
    private String nodeHash ;//ά��
    public RTree(int capacity, float fillFactor, int type, int dimension)
    {
        nodeCapacity = capacity;
        this.fillFactor = fillFactor;
        tree_type = type;
        this.dimension = dimension;
        root = new RTDataNode(this,Constants.NULL);
    }
    /**
     * @return RTree��ά��
     */
    public int getDimension()
    {
        return dimension;
    }
    public void setRoot(RTNode root)
    {
        this.root = root;
    }

    public float getFillFactor()
    {
        return fillFactor;
    }
    /**
     * @return ���ؽ������
     */
    public int getNodeCapacity()
    {
        return nodeCapacity;
    }
    /**
     * @return ������������
     */
    public int getTreeType()
    {
        return tree_type;
    }
    /**
     * ��Rtree�в���Rectangle<p>
     * 1�����ҵ����ʵ�Ҷ�ڵ� <br>
     * 2�������Ҷ�ڵ��в���<br>
     * @param rectangle
     */
    public boolean insert(Rectangle rectangle)
    {
        if(rectangle == null)
            throw new IllegalArgumentException("���β���Ϊ��");
        if(rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("����ά�ȱ�����ͬ");
        }
        RTDataNode leaf = root.chooseLeaf(rectangle);
        return leaf.insert(rectangle);
    }
    /**
     * ��R����ɾ��Rectangle <p>
     * 1��Ѱ�Ұ�����¼�Ľ��--�����㷨findLeaf()����λ�����˼�¼��Ҷ�ӽ��L�����û���ҵ����㷨��ֹ��<br>
     * 2��ɾ����¼--���ҵ���Ҷ�ӽ��L�еĴ˼�¼ɾ��<br>
     * 3�������㷨condenseTree<br>
     * @param rectangle
     * @return
     */
    public int delete(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("����Ϊ��");
        }
        if(rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("���α�����ͬά��");
        }
        RTDataNode leaf = root.findLeaf(rectangle);
        if(leaf != null)
        {
            return leaf.delete(rectangle);
        }
        return -1;
    }
    /**
     * �Ӹ����Ľ��root��ʼ�������еĽ��
     * @param root
     * @return ���б����Ľ�㼯��
     */
    public List<RTNode> traversePostOrder(RTNode root)
    {
        if(root == null)
            throw new IllegalArgumentException("�ڵ㲻��Ϊ��");
        List<RTNode> list = new ArrayList<RTNode>();
        list.add(root);
        if(! root.isLeaf())
        {
            for(int i = 0; i < root.usedSpace; i ++)
            {
                List<RTNode> a = traversePostOrder(((RTDirNode)root).getChild(i));
                for(int j = 0; j < a.size(); j ++)
                {
                    list.add(a.get(j));
                }
            }
        }
        return list;
    }
    public String jS(float a,float b)
    {
        float c = 2 * a + 8 * b;
        return String.valueOf(c);
    }
    //��ѯҶ�ӽڵ�
    public void findLeafNode(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("���β���Ϊ��");
        }

        if(rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("������Ҫ��ͬά��");
        }

        RTDataNode leaf = root.findLeaf(rectangle);
        if(leaf != null)
        {
            for(Rectangle rectangle2:leaf.datas)
            {
                if(rectangle2.getLow().equals(rectangle.getLow()) && rectangle2.getHigh().equals(rectangle.getHigh()))
                {
                    System.out.println("���ҵ�����Ϊ��"+rectangle2.getData());
                    System.out.println("��֤���У�"+leaf.vailList);
//                    String s = vailHash(leaf.vailList);
//                    if(s.equals(root.nodeHash))
//                    {
//                        System.out.println("�����ݴ��ڣ�����");
//                    }
                    return;
                }
            }
            System.out.println("û�д����ݣ�����");
        }
        System.out.println("û�д����ݣ�����");
    }
    public String  vailHash(List list)
    {
        String s="";
        boolean flag = false;
        List list2 = new ArrayList();
        for(int i=0;i<list.size();i++)
        {
            if(!list.get(i).equals(">"))
            {
                list2.add(list.get(i));
            }else{
                int j = list2.size();
                int k = list2.lastIndexOf("<");
                list2.remove(k);
                if(!flag)
                {
                    Rectangle[] rectangles = new Rectangle[list2.size()-k];
                    int l=0;
                    String s1 = "";
                    BloomFilter bf = new BloomFilter();
                    while(k<list2.size())
                    {
                        s = (String)list2.get(k);
                        String[] splits = s.split(" ");
                        long lx = Long.parseLong(splits[1]);
                        long ly = Long.parseLong(splits[2]);
                        Point p1 = new Point(new long[]{lx,ly});
                        Point p2 = new Point(new long[]{lx,ly});
                        rectangles[l] = new Rectangle(p1, p2);
                        rectangles[l].setData(s);//����
                        rectangles[l].setRectangleHash(CryptoUtil.SHA256(s));
                        String bfs = jS(lx,ly);
                        BloomFilter bf2 = new BloomFilter();
                        bf2.add(bfs);
                        rectangles[l].setbF(bf2);

                        bf.getBits().or(bf2.getBits());
                        s1 +=  rectangles[l].getRectangleHash();
                        l++;
                        list2.remove(k);
                    }
                    Rectangle r = Rectangle.getUnionRectangle(rectangles);

                    r.setbF(bf);
                    r.setRectangleHash(CryptoUtil.SHA256(s1));
                    list2.add(r);
//                    System.out.println(r);
//                    System.out.println("1111:"+CryptoUtil.SHA256(s1+r+bf));
//                    System.out.println(bf[0].getBits());
                    flag = true;
                }else{
                    //System.out.println(list2);
                    s = "";
                    Rectangle[] rectangles = new Rectangle[list2.size()-k];
                    int l=0;
                    BloomFilter bf = new BloomFilter();
                    while(k<list2.size())
                    {
                        rectangles[l] = ((Rectangle) list2.get(k));
                        s += rectangles[l] + rectangles[l].getRectangleHash() + rectangles[l].getbF().getBits().toString();
                        bf.getBits().or(rectangles[l].getbF().getBits());
                        l++;
                        list2.remove(k);
                    }
                    Rectangle r = Rectangle.getUnionRectangle(rectangles);
                    r.setbF(bf);
                    r.setRectangleHash(CryptoUtil.SHA256(s));
                    list2.add(r);
                    //System.out.println("ffff:"+CryptoUtil.SHA256(s+r+bf));
                    //System.out.println("sss"+list2);
                }
            }
        }
        return ((Rectangle)list2.get(0)).getRectangleHash();
    }
    public static void main(String[] args) throws Exception
    {

        String datafile = "C:\\Workspaces\\data\\d2\\datafile1.txt";
        String datafile2 = "C:\\Workspaces\\data\\d2\\datafile11.txt";
        String f = "C:\\Workspaces\\data\\d2\\rt1_11.txt";
        //64,128,256,512,1024, 2048, 4096,  8192(4),16384, 32768,  65536
        File file = new File(f);
        if(!file.exists()){
            file.createNewFile();
        }
        Writer out = new FileWriter(file,true);
        out.write("ά��Ϊ2������Ϊ,16384 \n");
        out.flush();
        //Thread.sleep(5000);
        Runtime r = Runtime.getRuntime();
        r.gc();//�����ڴ�ǰ����������һ��
        long start = System.currentTimeMillis();//��ʼTime
        long startMem = r.freeMemory(); // ��ʼMemory
        long all = r.totalMemory();
        RTree tree = new RTree(5, 0.4f, Constants.RTREE_QUADRATIC, 2);;
        BufferedReader reader = new BufferedReader(new FileReader(new File(datafile)));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] splits = line.split(" ");
            long lx = Long.parseLong(splits[0]);
            long ly = Long.parseLong(splits[1]);
            Point p1 = new Point(new long[]{lx, ly});
            Point p2 = new Point(new long[]{lx, ly});
            final Rectangle rectangle = new Rectangle(p1, p2);
            rectangle.setData(line);//����
            rectangle.setRectangleHash(CryptoUtil.SHA256(line));//�����ݽ��й�ϣ
            BloomFilter bf = new BloomFilter();
            String bfs = tree.jS(lx, ly);
            bf.add(bfs);
            rectangle.setbF(bf);
            tree.insert(rectangle);

            //            Rectangle[] rectangles = tree.root.datas;
            //            System.out.println(tree.root.level);
            //            for(int j = 0; j < rectangles.length; j++)
            //                System.out.println(rectangles[j]);
        }
        long endMem = r.freeMemory(); // ĩβMemory
        long all2 = r.totalMemory();

        long end = System.currentTimeMillis();//ĩβTime

        //���
        System.out.println("��ʱ����: " + String.valueOf((double) (end - start)) + "ms");
        System.out.println("�ڴ�����: " + String.valueOf((double) (all2 - all + startMem - endMem) / 1024 / 1024) + "M");
        System.out.println("������" + tree.root.getNodeRectangle());
        System.out.println("����ϣ��" + tree.root.nodeHash);
//            out.write(i+":  "+String.valueOf((double)(end - start))+"   "+String.valueOf((double)(all2-all+startMem-endMem)/1024/1024)+"\r\n");
//            out.flush();
        out.write("��ѯ����ʱ��:\r\n");
        out.flush();
        BufferedReader reader2 = new BufferedReader(new FileReader(new File(datafile2)));
        String line2 = reader2.readLine();
        String[] splits2 = line2.split(" ");
        long cx = Long.parseLong(splits2[0]);
        long cy = Long.parseLong(splits2[1]);
        long start2 = System.nanoTime();//��ʼTime
        Point p1 = new Point(new long[]{cx, cy});
        Point p2 = new Point(new long[]{cx, cy});
        final Rectangle rectangle = new Rectangle(p1, p2);
        tree.findLeafNode(rectangle);
        long end2 = System.nanoTime();//ĩβTime
        long sum = end2-start2;
        System.out.println("��ʱ����: "+ sum + ",ns");
        out.write("ƽ��ʱ��Ϊ�� "+sum+ "\r\n");
        out.flush();
        out.close();
    }

}
