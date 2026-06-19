package com.gunlock.util;

import java.util.random.RandomGenerator;

/**
 * Pure aim math — no Minecraft types, so it is unit-testable on its own.
 * The fire service converts to/from the engine's Vec3 at the call site.
 *
 * <p>Spread model: each weapon's {@code spreadDegrees} is the half-angle of
 * the cone the bullet may deviate within at the weapon's resting state.
 * Movement/fire-rate "bloom" is a later layer that adds to this half-angle;
 * the slice uses the resting cone directly. {@code accuracy} feeds that
 * later bloom layer and is not double-counted here.
 *
 * <p>Sampling is uniform over the spherical cap, so the distribution looks
 * natural rather than clustering at the cone edge.
 */
public final class AimMath {
    private AimMath() {}

    /** A unit 3-vector. */
    public record Dir(double x, double y, double z) {
        public Dir normalized() {
            double len = Math.sqrt(x * x + y * y + z * z);
            if (len < 1e-9) return new Dir(0, 0, 1);
            return new Dir(x / len, y / len, z / len);
        }
    }

    /**
     * Perturb {@code aim} by a random direction inside a cone of half-angle
     * {@code halfAngleDeg}. Returns a unit vector.
     */
    public static Dir applySpread(Dir aim, double halfAngleDeg, RandomGenerator rng) {
        Dir d = aim.normalized();
        if (halfAngleDeg <= 1e-6) return d;

        double maxTheta = Math.toRadians(halfAngleDeg);
        // Uniform over the cap: cos(theta) in [cos(maxTheta), 1].
        double cosTheta = 1.0 - rng.nextDouble() * (1.0 - Math.cos(maxTheta));
        double sinTheta = Math.sqrt(Math.max(0.0, 1.0 - cosTheta * cosTheta));
        double phi = 2.0 * Math.PI * rng.nextDouble();

        // Orthonormal basis (d, u, v) around the aim vector.
        Dir helper = Math.abs(d.x()) < 0.9 ? new Dir(1, 0, 0) : new Dir(0, 1, 0);
        Dir u = cross(d, helper).normalized();
        Dir v = cross(d, u); // already unit since d,u orthonormal

        double cx = Math.cos(phi) * sinTheta;
        double vy = Math.sin(phi) * sinTheta;
        return new Dir(
                d.x() * cosTheta + u.x() * cx + v.x() * vy,
                d.y() * cosTheta + u.y() * cx + v.y() * vy,
                d.z() * cosTheta + u.z() * cx + v.z() * vy
        ).normalized();
    }

    private static Dir cross(Dir a, Dir b) {
        return new Dir(
                a.y() * b.z() - a.z() * b.y(),
                a.z() * b.x() - a.x() * b.z(),
                a.x() * b.y() - a.y() * b.x()
        );
    }

    /** Angle in degrees between two unit vectors — used by tests. */
    public static double angleDeg(Dir a, Dir b) {
        double dot = a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
        return Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));
    }
}
