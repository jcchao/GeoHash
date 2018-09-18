public class GeoPoint {
    public double x;
    public double y;
    public final int dim = 2;

    // 0: 未被标记
    // 1: 边界点
    // 2: Noise
    // 3: Cluster内部点
    public int label = 0;
    public int clusterID = 0;

    public GeoPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getValueByAxis(int axis) {
        if (axis == 0)
            return this.x;
        else
            return this.y;
    }

    public void setValueByAxis(double v, int axis) {
        if (axis == 0)
            this.x = v;
        else
            this.y = v;
    }

    public boolean equal(GeoPoint p, int axis) {
        return this.getValueByAxis(axis) == p.getValueByAxis(axis);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
