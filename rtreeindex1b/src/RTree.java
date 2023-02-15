import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class RTree {
    private RTNode root;//根节点
    private int tree_type;//树类型
    private int nodeCapacity = -1;//结点容量
    private float fillFactor = -1;//结点填充因子
    private int dimension ;//维度
    private String nodeHash ;//维度
    public RTree(int capacity, float fillFactor, int type, int dimension)
    {
        nodeCapacity = capacity;
        this.fillFactor = fillFactor;
        tree_type = type;
        this.dimension = dimension;
        root = new RTDataNode(this,Constants.NULL);
    }
    /**
     * @return RTree的维度
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
     * @return 返回结点容量
     */
    public int getNodeCapacity()
    {
        return nodeCapacity;
    }
    /**
     * @return 返回树的类型
     */
    public int getTreeType()
    {
        return tree_type;
    }
    /**
     * 向Rtree中插入Rectangle<p>
     * 1、先找到合适的叶节点 <br>
     * 2、再向此叶节点中插入<br>
     * @param rectangle
     */
    public boolean insert(Rectangle rectangle)
    {
        if(rectangle == null)
            throw new IllegalArgumentException("矩形不能为空");
        if(rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("矩形维度必须相同");
        }
        RTDataNode leaf = root.chooseLeaf(rectangle);
        return leaf.insert(rectangle);
    }
    /**
     * 从R树中删除Rectangle <p>
     * 1、寻找包含记录的结点--调用算法findLeaf()来定位包含此记录的叶子结点L，如果没有找到则算法终止。<br>
     * 2、删除记录--将找到的叶子结点L中的此记录删除<br>
     * 3、调用算法condenseTree<br>
     * @param rectangle
     * @return
     */
    public int delete(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("矩形为空");
        }
        if(rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("矩形必须相同维度");
        }
        RTDataNode leaf = root.findLeaf(rectangle);
        if(leaf != null)
        {
            return leaf.delete(rectangle);
        }
        return -1;
    }
    /**
     * 从给定的结点root开始遍历所有的结点
     * @param root
     * @return 所有遍历的结点集合
     */
    public List<RTNode> traversePostOrder(RTNode root)
    {
        if(root == null)
            throw new IllegalArgumentException("节点不能为空");
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
    //查询叶子节点
    public void findLeafNode(Rectangle rectangle)
    {
        if(rectangle == null)
        {
            throw new IllegalArgumentException("矩形不能为空");
        }

        if(rectangle.getHigh().getDimension() != getDimension())
        {
            throw new IllegalArgumentException("矩型需要相同维度");
        }

        RTDataNode leaf = root.findLeaf(rectangle);
        if(leaf != null)
        {
            for(Rectangle rectangle2:leaf.datas)
            {
                if(rectangle2.getLow().equals(rectangle.getLow()) && rectangle2.getHigh().equals(rectangle.getHigh()))
                {
                    System.out.println("查找的数据为："+rectangle2.getData());
                    System.out.println("验证序列："+leaf.vailList);
//                    String s = vailHash(leaf.vailList);
//                    if(s.equals(root.nodeHash))
//                    {
//                        System.out.println("此数据存在！！！");
//                    }
                    return;
                }
            }
            System.out.println("没有此数据！！！");
        }
        System.out.println("没有此数据！！！");
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
                        rectangles[l].setData(s);//数据
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
        out.write("维度为2，数据为,16384 \n");
        out.flush();
        //Thread.sleep(5000);
        Runtime r = Runtime.getRuntime();
        r.gc();//计算内存前先垃圾回收一次
        long start = System.currentTimeMillis();//开始Time
        long startMem = r.freeMemory(); // 开始Memory
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
            rectangle.setData(line);//数据
            rectangle.setRectangleHash(CryptoUtil.SHA256(line));//对数据进行哈希
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
        long endMem = r.freeMemory(); // 末尾Memory
        long all2 = r.totalMemory();

        long end = System.currentTimeMillis();//末尾Time

        //输出
        System.out.println("用时消耗: " + String.valueOf((double) (end - start)) + "ms");
        System.out.println("内存消耗: " + String.valueOf((double) (all2 - all + startMem - endMem) / 1024 / 1024) + "M");
        System.out.println("根矩阵：" + tree.root.getNodeRectangle());
        System.out.println("根哈希：" + tree.root.nodeHash);
//            out.write(i+":  "+String.valueOf((double)(end - start))+"   "+String.valueOf((double)(all2-all+startMem-endMem)/1024/1024)+"\r\n");
//            out.flush();
        out.write("查询消耗时间:\r\n");
        out.flush();
        BufferedReader reader2 = new BufferedReader(new FileReader(new File(datafile2)));
        String line2 = reader2.readLine();
        String[] splits2 = line2.split(" ");
        long cx = Long.parseLong(splits2[0]);
        long cy = Long.parseLong(splits2[1]);
        long start2 = System.nanoTime();//开始Time
        Point p1 = new Point(new long[]{cx, cy});
        Point p2 = new Point(new long[]{cx, cy});
        final Rectangle rectangle = new Rectangle(p1, p2);
        tree.findLeafNode(rectangle);
        long end2 = System.nanoTime();//末尾Time
        long sum = end2-start2;
        System.out.println("用时消耗: "+ sum + ",ns");
        out.write("平均时间为： "+sum+ "\r\n");
        out.flush();
        out.close();
    }

}
