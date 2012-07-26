package org.wintrisstech.erik.robot.pid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import static java.lang.Math.*;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author ecolban
 */
public class Robot
{

    /*
     * The lane dimensions in mm
     */
    public final static int LANE_LENGTH = 7520;
    public final static int LANE_WIDTH = 1040;
    /*
     * Half the distance between the wheels in mm
     */
    private static final float HALF_WHEEL_DISTANCE = 120F;
    private static final float MAX_SPEED = 500F; // in mm/s
    /*
     * smallest angle in radians that is consdired not straight
     */
    private static final float TURN_TOLERANCE = 0.001F;
    /*
     * The maximum speed above or below the average speed of the two wheels
     */
    private static final float MAX_STEERING = 20F;
    private final Random random = new Random();
    private JPanel panel;
    private Path2D path = new Path2D.Float();
//    private ArrayList<Point> trace = new ArrayList<Point>();
    private final float xPosInit = 0F;
    private float yPosInit;
    private float headingInit;
    private float xPos;
    private float yPos;
    private float heading;
    private float steeringDrift;
    private float steeringNoise;
    private float sensorNoise;
    private int timingNoise; // in ms

    /**
     * Simulates the Robot's driveDirect() method for a given duration.
     *
     * @param leftSpeed left wheel's speed in mm/s
     * @param rightSpeed right wheel's speed in mm/s
     * @param deltaTime the duration
     */
    private void driveDirect(final int leftSpeed, final int rightSpeed, final int deltaTime)
    {
        assert 0 <= leftSpeed && leftSpeed <= MAX_SPEED;
        assert 0 <= rightSpeed && rightSpeed <= MAX_SPEED;
        // Add noise and drift
        float leftSpeedReal = (leftSpeed
                + steeringNoise * (float) random.nextGaussian()) / 1000F; //in mm/ms
        float rightSpeedReal = (rightSpeed
                + steeringNoise * (float) random.nextGaussian()) / 1000F; //in mm/ms
        rightSpeedReal *= steeringDrift;
        float speed = (rightSpeedReal + leftSpeedReal) / 2F;
        float steering = (rightSpeedReal - leftSpeedReal) / 2F;
        // Update position
        double distance = speed * deltaTime; // in mm
        double turn = steering * deltaTime / HALF_WHEEL_DISTANCE;
        if (abs(turn) < TURN_TOLERANCE) { // if going straignt
            xPos += distance * cos(heading);
            yPos += distance * sin(heading);
            heading += turn;

        } else {
            float radius = HALF_WHEEL_DISTANCE * speed / steering;
            // Alternatively: radius = distance / turn
            float centerX = xPos - radius * (float) sin(heading);
            float centerY = yPos + radius * (float) cos(heading);
            heading += turn;
            xPos = centerX + radius * (float) sin(heading);
            yPos = centerY - radius * (float) cos(heading);
        }
        path.lineTo(xPos, yPos);
    }

    /**
     * Runs the Robot along the lane
     *
     * @param kProp the proportional coefficient
     * @param kInt the integral coefficient
     * @param kDiff the differential coefficient
     * @return the average error, which may be used to optimize the
     * coefficients.
     */
    public void run(float kProp, float kInt, float kDiff)
    {
        path.reset();
        path.moveTo(xPosInit, yPosInit);
        xPos = xPosInit;
        yPos = yPosInit;
        heading = headingInit;

        float steering = 0F;
        float crosstrackError;
        float previousCte = readSensors();
        float cteDiff ;
        float cteInt = 0;
        int totalTime = 0;
        while (xPos < LANE_LENGTH && abs(yPos) < LANE_WIDTH / 2) {
            // Cap the steering within the limits:
            steering = max(-MAX_STEERING, min(steering, MAX_STEERING));

            int leftSpeed = round(min(MAX_SPEED, MAX_SPEED - 2 * steering));
            int rightSpeed = round(min(MAX_SPEED, MAX_SPEED + 2 * steering));
            int deltaTime = 100 + random.nextInt(timingNoise); // in ms
            driveDirect(leftSpeed, rightSpeed, deltaTime);
            crosstrackError = 0.8F * readSensors() + 0.2F * previousCte; // exp. avg.
            cteDiff = (crosstrackError - previousCte) / deltaTime;
            cteInt += crosstrackError * deltaTime;
            steering = -kProp * crosstrackError - kDiff * cteDiff - kInt * cteInt;
            totalTime += deltaTime;
            previousCte = crosstrackError;
        }
        System.out.println("time = " + totalTime);

    }

    private float readSensors()
    {
        return (float) (this.yPos + random.nextGaussian() * sensorNoise);
    }

    void paintSelf(Graphics2D g2)
    {
        g2.setColor(Color.BLUE);
        AffineTransform transform = AffineTransform.getTranslateInstance(
                0F, panel.getHeight() / 2F);
        float scale = (float) panel.getWidth() / LANE_LENGTH;
        transform.scale(scale, -scale);
        g2.draw(path.createTransformedShape(transform));
    }

    /**
     * @param panel the panel to set
     */
    public void setPanel(JPanel panel)
    {
        this.panel = panel;
    }

    /**
     * Sets the yPos
     *
     * @param y the value of yPos
     */
    void setYPos(float y)
    {
        yPosInit = y;
    }

    /**
     * Set the heading
     *
     * @param heading the heading to set
     */
    public void setHeading(float heading)
    {
        this.headingInit = heading * (float) PI / 180F;
    }

    /**
     * @param steeringDrift the steeringDrift to set
     */
    public void setSteeringDrift(float steeringDrift)
    {
        this.steeringDrift = steeringDrift;
    }

    public double getSteeringDrift()
    {
        return steeringDrift;
    }

    /**
     * @param steeringNoise the steeringNoise to set
     */
    public void setSteeringNoise(float steeringNoise)
    {
        this.steeringNoise = steeringNoise;
    }

    public double getSteeringNoise()
    {
        return steeringNoise;
    }

    /**
     * @param sensorNoise the sensorNoise to set
     */
    public void setSensorNoise(float sensorNoise)
    {
        this.sensorNoise = sensorNoise;
    }

    public double getSensorNoise()
    {
        return sensorNoise;
    }

    /**
     * @param timingNoise the timingNoise to set
     */
    public void setTimingNoise(int timingNoise)
    {
        this.timingNoise = timingNoise;
    }

    public double getTimingNoise()
    {
        return timingNoise;
    }
}
