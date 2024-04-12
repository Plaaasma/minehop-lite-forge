package net.nerdorg.minehop.math;

import net.minecraft.Util;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class MathHelper {
    private static final long field_29852 = 61440L;
    private static final long HALF_PI_RADIANS_SINE_TABLE_INDEX = 16384L;
    private static final long field_29854 = -4611686018427387904L;
    private static final long field_29855 = -9223372036854775808L;
    public static final float PI = 3.1415927F;
    public static final float HALF_PI = 1.5707964F;
    public static final float TAU = 6.2831855F;
    public static final float RADIANS_PER_DEGREE = 0.017453292F;
    public static final float DEGREES_PER_RADIAN = 57.295776F;
    public static final float EPSILON = 1.0E-5F;
    public static final float SQUARE_ROOT_OF_TWO = sqrt(2.0F);
    private static final float DEGREES_TO_SINE_TABLE_INDEX = 10430.378F;
    public static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
    public static final Vector3f X_AXIS = new Vector3f(1.0F, 0.0F, 0.0F);
    public static final Vector3f Z_AXIS = new Vector3f(0.0F, 0.0F, 1.0F);
    private static final float[] SINE_TABLE = (float[]) Util.make(new float[65536], (sineTable) -> {
        for (int i = 0; i < sineTable.length; ++i) {
            sineTable[i] = (float) Math.sin((double) i * 3.141592653589793D * 2.0D / 65536.0D);
        }

    });
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
    private static final double ARCSINE_MACLAURIN_3 = 0.16666666666666666D;
    private static final int field_29858 = 8;
    private static final int ARCSINE_TABLE_LENGTH = 257;
    private static final double ROUNDER_256THS = Double.longBitsToDouble(4805340802404319232L);
    private static final double[] ARCSINE_TABLE = new double[257];
    private static final double[] COSINE_OF_ARCSINE_TABLE = new double[257];

    public MathHelper() {
    }

    public static float sin(float value) {
        return SINE_TABLE[(int) (value * 10430.378F) & '\uffff'];
    }

    public static float cos(float value) {
        return SINE_TABLE[(int) (value * 10430.378F + 16384.0F) & '\uffff'];
    }

    public static float sqrt(float value) {
        return (float) Math.sqrt((double) value);
    }

    public static int floor(float value) {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static long lfloor(double value) {
        long l = (long) value;
        return value < (double) l ? l - 1L : l;
    }

    public static float abs(float value) {
        return Math.abs(value);
    }

    public static int abs(int value) {
        return Math.abs(value);
    }

    public static int ceil(float value) {
        int i = (int) value;
        return value > (float) i ? i + 1 : i;
    }

    public static int ceil(double value) {
        int i = (int) value;
        return value > (double) i ? i + 1 : i;
    }

    public static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static long clamp(long value, long min, long max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }

    public static double clampedLerp(double start, double end, double delta) {
        if (delta < 0.0D) {
            return start;
        } else {
            return delta > 1.0D ? end : lerp(delta, start, end);
        }
    }

    public static float clampedLerp(float start, float end, float delta) {
        if (delta < 0.0F) {
            return start;
        } else {
            return delta > 1.0F ? end : lerp(delta, start, end);
        }
    }

    public static double absMax(double a, double b) {
        if (a < 0.0D) {
            a = -a;
        }

        if (b < 0.0D) {
            b = -b;
        }

        return Math.max(a, b);
    }

    public static int floorDiv(int dividend, int divisor) {
        return Math.floorDiv(dividend, divisor);
    }

    public static boolean approximatelyEquals(float a, float b) {
        return Math.abs(b - a) < 1.0E-5F;
    }

    public static boolean approximatelyEquals(double a, double b) {
        return Math.abs(b - a) < 9.999999747378752E-6D;
    }

    public static int floorMod(int dividend, int divisor) {
        return Math.floorMod(dividend, divisor);
    }

    public static float floorMod(float dividend, float divisor) {
        return (dividend % divisor + divisor) % divisor;
    }

    public static double floorMod(double dividend, double divisor) {
        return (dividend % divisor + divisor) % divisor;
    }

    public static boolean isMultipleOf(int a, int b) {
        return a % b == 0;
    }

    public static int wrapDegrees(int degrees) {
        int i = degrees % 360;
        if (i >= 180) {
            i -= 360;
        }

        if (i < -180) {
            i += 360;
        }

        return i;
    }

    public static float wrapDegrees(float degrees) {
        float f = degrees % 360.0F;
        if (f >= 180.0F) {
            f -= 360.0F;
        }

        if (f < -180.0F) {
            f += 360.0F;
        }

        return f;
    }

    public static double wrapDegrees(double degrees) {
        double d = degrees % 360.0D;
        if (d >= 180.0D) {
            d -= 360.0D;
        }

        if (d < -180.0D) {
            d += 360.0D;
        }

        return d;
    }

    public static float subtractAngles(float start, float end) {
        return wrapDegrees(end - start);
    }

    public static float angleBetween(float first, float second) {
        return abs(subtractAngles(first, second));
    }

    public static float clampAngle(float value, float mean, float delta) {
        float f = subtractAngles(value, mean);
        float g = clamp(f, -delta, delta);
        return mean - g;
    }

    public static float stepTowards(float from, float to, float step) {
        step = abs(step);
        return from < to ? clamp(from + step, from, to) : clamp(from - step, to, from);
    }

    public static float stepUnwrappedAngleTowards(float from, float to, float step) {
        float f = subtractAngles(from, to);
        return stepTowards(from, from + f, step);
    }

    public static int parseInt(String string, int fallback) {
        return NumberUtils.toInt(string, fallback);
    }

    public static int smallestEncompassingPowerOfTwo(int value) {
        int i = value - 1;
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return i + 1;
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    public static int ceilLog2(int value) {
        value = isPowerOfTwo(value) ? value : smallestEncompassingPowerOfTwo(value);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int) ((long) value * 125613361L >> 27) & 31];
    }

    public static int floorLog2(int value) {
        return ceilLog2(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    public static float fractionalPart(float value) {
        return value - (float) floor(value);
    }

    public static double fractionalPart(double value) {
        return value - (double) lfloor(value);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static long hashCode(Vec3i vec) {
        return hashCode(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static long hashCode(int x, int y, int z) {
        long l = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        l = l * l * 42317861L + l * 11L;
        return l >> 16;
    }

    public static double getLerpProgress(double value, double start, double end) {
        return (value - start) / (end - start);
    }

    public static float getLerpProgress(float value, float start, float end) {
        return (value - start) / (end - start);
    }

    public static boolean method_34945(Vec3 origin, Vec3 direction, AABB box) {
        double d = (box.minX + box.maxX) * 0.5D;
        double e = (box.maxX - box.minX) * 0.5D;
        double f = origin.x - d;
        if (Math.abs(f) > e && f * direction.x >= 0.0D) {
            return false;
        } else {
            double g = (box.minY + box.maxY) * 0.5D;
            double h = (box.maxY - box.minY) * 0.5D;
            double i = origin.y - g;
            if (Math.abs(i) > h && i * direction.y >= 0.0D) {
                return false;
            } else {
                double j = (box.minZ + box.maxZ) * 0.5D;
                double k = (box.maxZ - box.minZ) * 0.5D;
                double l = origin.z - j;
                if (Math.abs(l) > k && l * direction.z >= 0.0D) {
                    return false;
                } else {
                    double m = Math.abs(direction.x);
                    double n = Math.abs(direction.y);
                    double o = Math.abs(direction.z);
                    double p = direction.y * l - direction.z * i;
                    if (Math.abs(p) > h * o + k * n) {
                        return false;
                    } else {
                        p = direction.z * f - direction.x * l;
                        if (Math.abs(p) > e * o + k * m) {
                            return false;
                        } else {
                            p = direction.x * i - direction.y * f;
                            return Math.abs(p) < e * n + h * m;
                        }
                    }
                }
            }
        }
    }

    public static double atan2(double y, double x) {
        double d = x * x + y * y;
        if (Double.isNaN(d)) {
            return 0.0D / 0.0;
        } else {
            boolean bl = y < 0.0D;
            if (bl) {
                y = -y;
            }

            boolean bl2 = x < 0.0D;
            if (bl2) {
                x = -x;
            }

            boolean bl3 = y > x;
            double e;
            if (bl3) {
                e = x;
                x = y;
                y = e;
            }

            e = fastInverseSqrt(d);
            x *= e;
            y *= e;
            double f = ROUNDER_256THS + y;
            int i = (int) Double.doubleToRawLongBits(f);
            double g = ARCSINE_TABLE[i];
            double h = COSINE_OF_ARCSINE_TABLE[i];
            double j = f - ROUNDER_256THS;
            double k = y * h - x * j;
            double l = (6.0D + k * k) * k * 0.16666666666666666D;
            double m = g + l;
            if (bl3) {
                m = 1.5707963267948966D - m;
            }

            if (bl2) {
                m = 3.141592653589793D - m;
            }

            if (bl) {
                m = -m;
            }

            return m;
        }
    }

    public static float inverseSqrt(float x) {
        return org.joml.Math.invsqrt(x);
    }

    public static double inverseSqrt(double x) {
        return org.joml.Math.invsqrt(x);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static double fastInverseSqrt(double x) {
        double d = 0.5D * x;
        long l = Double.doubleToRawLongBits(x);
        l = 6910469410427058090L - (l >> 1);
        x = Double.longBitsToDouble(l);
        x *= 1.5D - d * x * x;
        return x;
    }

    public static float fastInverseCbrt(float x) {
        int i = Float.floatToIntBits(x);
        i = 1419967116 - i / 3;
        float f = Float.intBitsToFloat(i);
        f = 0.6666667F * f + 1.0F / (3.0F * f * f * x);
        f = 0.6666667F * f + 1.0F / (3.0F * f * f * x);
        return f;
    }

    public static int idealHash(int value) {
        value ^= value >>> 16;
        value *= -2048144789;
        value ^= value >>> 13;
        value *= -1028477387;
        value ^= value >>> 16;
        return value;
    }

    public static int binarySearch(int min, int max, IntPredicate predicate) {
        int i = max - min;

        while (i > 0) {
            int j = i / 2;
            int k = min + j;
            if (predicate.test(k)) {
                i = j;
            } else {
                min = k + 1;
                i -= j + 1;
            }
        }

        return min;
    }

    public static int lerp(float delta, int start, int end) {
        return start + floor(delta * (float) (end - start));
    }

    public static int lerpPositive(float delta, int start, int end) {
        int i = end - start;
        return start + floor(delta * (float) (i - 1)) + (delta > 0.0F ? 1 : 0);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static double lerp2(double deltaX, double deltaY, double x0y0, double x1y0, double x0y1, double x1y1) {
        return lerp(deltaY, lerp(deltaX, x0y0, x1y0), lerp(deltaX, x0y1, x1y1));
    }

    public static double lerp3(double deltaX, double deltaY, double deltaZ, double x0y0z0, double x1y0z0, double x0y1z0, double x1y1z0, double x0y0z1, double x1y0z1, double x0y1z1, double x1y1z1) {
        return lerp(deltaZ, lerp2(deltaX, deltaY, x0y0z0, x1y0z0, x0y1z0, x1y1z0), lerp2(deltaX, deltaY, x0y0z1, x1y0z1, x0y1z1, x1y1z1));
    }

    public static float catmullRom(float delta, float p0, float p1, float p2, float p3) {
        return 0.5F * (2.0F * p1 + (p2 - p0) * delta + (2.0F * p0 - 5.0F * p1 + 4.0F * p2 - p3) * delta * delta + (3.0F * p1 - p0 - 3.0F * p2 + p3) * delta * delta * delta);
    }

    public static double perlinFade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    public static double perlinFadeDerivative(double value) {
        return 30.0D * value * value * (value - 1.0D) * (value - 1.0D);
    }

    public static int sign(double value) {
        if (value == 0.0D) {
            return 0;
        } else {
            return value > 0.0D ? 1 : -1;
        }
    }

    public static float lerpAngleDegrees(float delta, float start, float end) {
        return start + delta * wrapDegrees(end - start);
    }

    public static double lerpAngleDegrees(double delta, double start, double end) {
        return start + delta * wrapDegrees(end - start);
    }

    public static float wrap(float value, float maxDeviation) {
        return (Math.abs(value % maxDeviation - maxDeviation * 0.5F) - maxDeviation * 0.25F) / (maxDeviation * 0.25F);
    }

    public static float square(float n) {
        return n * n;
    }

    public static double square(double n) {
        return n * n;
    }

    public static int square(int n) {
        return n * n;
    }

    public static long square(long n) {
        return n * n;
    }

    public static double clampedMap(double value, double oldStart, double oldEnd, double newStart, double newEnd) {
        return clampedLerp(newStart, newEnd, getLerpProgress(value, oldStart, oldEnd));
    }

    public static float clampedMap(float value, float oldStart, float oldEnd, float newStart, float newEnd) {
        return clampedLerp(newStart, newEnd, getLerpProgress(value, oldStart, oldEnd));
    }

    public static double map(double value, double oldStart, double oldEnd, double newStart, double newEnd) {
        return lerp(getLerpProgress(value, oldStart, oldEnd), newStart, newEnd);
    }

    public static float map(float value, float oldStart, float oldEnd, float newStart, float newEnd) {
        return lerp(getLerpProgress(value, oldStart, oldEnd), newStart, newEnd);
    }

    public static int roundUpToMultiple(int value, int divisor) {
        return ceilDiv(value, divisor) * divisor;
    }

    public static int ceilDiv(int a, int b) {
        return -Math.floorDiv(-a, b);
    }

    public static int nextBetween(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static float nextBetween(Random random, float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    public static float nextGaussian(Random random, float mean, float deviation) {
        return mean + (float) random.nextGaussian() * deviation;
    }

    public static double squaredHypot(double a, double b) {
        return a * a + b * b;
    }

    public static double hypot(double a, double b) {
        return Math.sqrt(squaredHypot(a, b));
    }

    public static double squaredMagnitude(double a, double b, double c) {
        return a * a + b * b + c * c;
    }

    public static double magnitude(double a, double b, double c) {
        return Math.sqrt(squaredMagnitude(a, b, c));
    }

    public static int roundDownToMultiple(double a, int b) {
        return floor(a / (double) b) * b;
    }

    public static IntStream stream(int seed, int lowerBound, int upperBound) {
        return stream(seed, lowerBound, upperBound, 1);
    }

    public static IntStream stream(int seed, int lowerBound, int upperBound, int steps) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "upperbound %d expected to be > lowerBound %d", upperBound, lowerBound));
        } else if (steps < 1) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "steps expected to be >= 1, was %d", steps));
        } else {
            return seed >= lowerBound && seed <= upperBound ? IntStream.iterate(seed, (i) -> {
                int m = Math.abs(seed - i);
                return seed - m >= lowerBound || seed + m <= upperBound;
            }, (i) -> {
                boolean bl = i <= seed;
                int n = Math.abs(seed - i);
                boolean bl2 = seed + n + steps <= upperBound;
                if (!bl || !bl2) {
                    int o = seed - n - (bl ? steps : 0);
                    if (o >= lowerBound) {
                        return o;
                    }
                }

                return seed + n + steps;
            }) : IntStream.empty();
        }
    }

    public static Quaternionf rotateAround(Vector3f axis, Quaternionf rotation, Quaternionf result) {
        float f = axis.dot(rotation.x, rotation.y, rotation.z);
        return result.set(axis.x * f, axis.y * f, axis.z * f, rotation.w).normalize();
    }

    static {
        for (int i = 0; i < 257; ++i) {
            double d = (double) i / 256.0D;
            double e = Math.asin(d);
            COSINE_OF_ARCSINE_TABLE[i] = Math.cos(e);
            ARCSINE_TABLE[i] = e;
        }

    }
}

