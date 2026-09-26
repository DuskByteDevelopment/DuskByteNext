package dev.duskbyte.gui.util;

/**
 * Simple animated value with easing for GUI transitions.
 */
public class AnimatedValue {
    private double value;
    private double target;
    private double velocity = 0;
    private final double speed;
    private final double threshold = 0.001;

    public AnimatedValue(double initial, double speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public void setImmediate(double value) {
        this.value = value;
        this.target = value;
        this.velocity = 0;
    }

    public void update(double deltaMs) {
        double dt = deltaMs / 1000.0;
        // Spring-damper system for smooth easing
        double stiffness = speed;
        double damping = 0.7;
        double force = (target - value) * stiffness;
        velocity += force * dt;
        velocity *= Math.pow(damping, dt * 60);
        value += velocity * dt;
        if (Math.abs(value - target) < threshold && Math.abs(velocity) < threshold) {
            value = target;
            velocity = 0;
        }
    }

    public double get() { return value; }
    public float getFloat() { return (float) value; }
    public float getProgress() { return (float) Math.max(0, Math.min(1, value)); }
}
