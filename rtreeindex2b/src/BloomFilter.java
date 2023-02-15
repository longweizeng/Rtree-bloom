import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BloomFilter {
    private static final int DEFAULT_SIZE = 32768;
    //64,  128,  256,  512,   1024, 2048, 4096,  8192(4),16384, 32768,  65536
    //2048 4096  8192  16384  32768 65536 131072 262144  524288 1048576 2097152
    //����Ϊ33554432����λ32000(1),64000(2),96000(3),128000(4),160000(5),
    //Ϊ�˽��ʹ����ʣ�ʹ�üӷ�Hash�㷨�����Զ���һ��8��Ԫ�ص��������� 192000(6),224000(7),256000(8)
    private static final int[] seeds = new int[] { 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 61, 71, 73 };
    //private static final int[] seeds = new int[] { 3, 5, 7, 11, 13, 17, 19};
    //��ʼ��Bloom������bitmap
    private BitSet bits = new BitSet(DEFAULT_SIZE);
    //�൱�ڹ���8����ͬ��hash�㷨
    private SimpleHash[] func = new SimpleHash[seeds.length];

    public BitSet getBits() {
        return bits;
    }

    public void setBits(BitSet bits) {
        this.bits = bits;
    }

    public BloomFilter() {
        for (int i = 0; i < seeds.length; i++) {
            func[i] = new SimpleHash(DEFAULT_SIZE, seeds[i]);
        }
    }
    //�������
    public void add(String value) {
        if(value != null)
        {
            for (SimpleHash f : func) {
                //����hashֵ���޸�bitmap����Ӧλ��Ϊtrue
                bits.set(f.hash(value), true);
            }
        }
    }
    //�ж�Ԫ���Ƿ����
    public boolean contains(String value) {
        if (value == null) {
            return false;
        }
        boolean ret = true;
        for (SimpleHash f : func) {
            ret = ret && bits.get(f.hash(value));
            //һ��hash��������false������ѭ��
            if(!ret)
            {
                break;
            }
        }
        return ret;
    }

    // �ڲ��࣬simpleHash
    public static class SimpleHash {
        private int cap;
        private int seed;
        public SimpleHash(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }
        public int hash(String value) {
            int result = 0;
            int len = value.length();
            for (int i = 0; i < len; i++) {
                result = seed * result + value.charAt(i);
            }
            return (cap - 1) & result;
        }
    }
}