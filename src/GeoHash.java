import java.util.HashMap;

public class GeoHash {
    private final double lng_lower = -180.0;
    private final double lng_upper = 180.0;
    private final double lat_lower = -90.0;
    private final double lat_upper = 90.0;

    public int length;
    public int latCodeLength;
    public int lngCodeLength;

    //编码表
    private HashMap<Integer, String> base32Encoder = new HashMap<Integer, String>(){
        {
            put(0, "0");
            put(1, "1");
            put(2, "2");
            put(3, "3");
            put(4, "4");
            put(5, "5");
            put(6, "6");
            put(7, "7");
            put(8, "8");
            put(9, "9");
            put(10, "b");
            put(11, "c");
            put(12, "d");
            put(13, "e");
            put(14, "f");
            put(15, "g");
            put(16, "h");
            put(17, "j");
            put(18, "k");
            put(19, "m");
            put(20, "n");
            put(21, "p");
            put(22, "q");
            put(23, "r");
            put(24, "s");
            put(25, "t");
            put(26, "u");
            put(27, "v");
            put(28, "w");
            put(29, "x");
            put(30, "y");
            put(31, "z");
        }
    };

    //解码表
    private HashMap<String, Integer> base32Decoder = new HashMap<String, Integer>();


    /**
     * 构造函数
     * @param length　geohash编码长度
     */
    public GeoHash(int length) {
        this.length = length;
        this.latCodeLength = (int)Math.floor(this.length*5/2.0);
        this.lngCodeLength = (int)Math.ceil(this.length*5/2.0);
        for (int key: this.base32Encoder.keySet()) {
            this.base32Decoder.put(this.base32Encoder.get(key), key);
        }
    }

    private String hash(double target, double lower, double upper, int currentLength, int maxLength, StringBuilder s) {
        if (currentLength >= maxLength)
            return s.toString();
        double split = lower + (upper-lower) / 2;
        if (target <= split) {
            s.append("0");
            return hash(target, lower, split, currentLength + 1, maxLength, s);
        } else {
            s.append("1");
            return hash(target, split, upper, currentLength + 1, maxLength, s);
        }
    }


    /**
     * 偶数位放经度，奇数位放纬度(经度编码的长度总是大于等于纬度编码的长度)
     * @param a　经度
     * @param b　维度
     * @return
     */
    private String mergeCode(String a, String b) {
        StringBuilder s = new StringBuilder();
        int i = 0;
        while (i < a.length() && i < b.length()) {
            s.append(a.charAt(i));
            s.append(b.charAt(i));
            i++;
        }
        if (i == a.length()) {
            return s.toString();
        } else {
            s.append(a.charAt(i));
            return s.toString();
        }
    }


    /**
     * 精确度以米为单位
     * 经度小数点位数    精确度(m)
     * 0.0000001       0.01
     * 0.000001        0.1
     * 0.00001         1.0
     * 0.0001          10.0
     * 0.001           100.0
     * 0.01            1000.0
     * 0.1             10,000.0
     * GeoHash分裂次数与经度小数点位数的关系：
     * 小数点位数＝split-3 (split>=3)
     * GeoHash分裂次数与纬度小数点位数的关系
     * 小数点位数＝split-2 (split>=2)
     * @param p
     * @return
     */
    private String binaryEncode(GeoPoint p) {
        StringBuilder lngCode = new StringBuilder();
        this.hash(p.x, this.lng_lower, this.lng_upper, 0, this.lngCodeLength, lngCode);
        StringBuilder latCode = new StringBuilder();
        this.hash(p.y, this.lat_lower, this.lat_upper, 0, this.latCodeLength, latCode);
//        System.out.println("longitude code: " + lngCode);
//        System.out.println("lantitude code: " + latCode);
        return mergeCode(lngCode.toString(), latCode.toString());
    }

    /**
     * 二进制转十进制
     * @param s　二进制字符串
     * @return
     */
    private int deciBase(String s) {
        int sum = 0;
        int count = 0;
        for (int i=s.length()-1; i>=0; i--) {
            if (s.charAt(i) == '1')
                sum += Math.pow(2, count);
            count++;
        }
        return sum;
    }

    /**
     * 十进制转二进制
     * @param a　十进制数
     * @return
     */
    private String binaBase(int a) {
        StringBuilder s = new StringBuilder();
        int base = 0;
        int div = a;
        while (base < 5) {
            s.append(div%2);
            div = div/2;
            base += 1;
        }
        return s.reverse().toString();
    }

    /**
     * 将base３２编码转成二进制编码
     * @param code　geohash base32编码
     * @return
     */
    private String decodeBinary(String code) {
        StringBuilder s = new StringBuilder();
        for (int i=0; i<code.length(); i++) {
            s.append(this.binaBase(this.base32Decoder.get(String.valueOf(code.charAt(i)))));
        }
        return s.toString();
    }

    private double parse(String code, double lower, double upper) {
        for (int i=0; i<code.length(); i++) {
            double split = lower + (upper-lower) / 2;
            if (code.charAt(i) == '0')
                upper = split;
            else
                lower = split;
        }
//        选取左区间或者右区间或者区间中点作为估计值
//        return (lower + upper) / 2;
        return upper;
    }

    public GeoPoint decode(String code) {
        String binaryCode = this.decodeBinary(code);
        StringBuilder lngCode = new StringBuilder();
        StringBuilder latCode = new StringBuilder();
        for (int i=0; i<binaryCode.length(); i++) {
            if (i%2==0)
                lngCode.append(binaryCode.charAt(i));
            else
                latCode.append(binaryCode.charAt(i));
        }

        double x = this.parse(lngCode.toString(), this.lng_lower, this.lng_upper);
        double y = this.parse(latCode.toString(), this.lat_lower, this.lat_upper);
        return new GeoPoint(x, y);

    }

    public String encode(GeoPoint p) {
        StringBuilder s = new StringBuilder();
        String binaryCode = this.binaryEncode(p);
        int i = 0;
        while (i<=binaryCode.length()-5) {
            s.append(this.base32Encoder.get(this.deciBase(binaryCode.substring(i, i+5))));
            i = i+5;
        }
        return s.toString();
    }

    public static void main(String args[]) {
        GeoPoint p = new GeoPoint(117.149243,39.110847);
        GeoHash geoHash  = new GeoHash(10);
        System.out.println("original point: " + p);
        System.out.println("geohash encode: " + geoHash.encode(p));
        System.out.println("geohash decode: " + geoHash.decode("wwgq3zcmpn"));

    }
}
