package org.wintrisstech.erik.robot.pid;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.*;

/**
 * 
 * @author ecolban
 */
@SuppressWarnings("serial")
public class UserInterface extends JPanel implements Runnable, ActionListener,
		PropertyChangeListener {

	private JFormattedTextField sensorNoiseField;
	private JFormattedTextField steeringNoiseField;
	private JFormattedTextField steeringDriftField;
	private JFormattedTextField timingNoiseField;
	private JFormattedTextField yOffsetField;
	private JFormattedTextField headingField;
	private JFormattedTextField coeffProportionalField;
	private JFormattedTextField coeffIntegralField;
	private JFormattedTextField coeffDifferentialField;
	private float coeffProportional = 0F;
	private float coeffIntegral = 0F;
	private float coeffDifferential = 0F;
	private final Robot robot;
	private float yOffset = 0F;
	private float heading = 0F;

	/**
	 * Constructs an instance of the application.
	 * 
	 * @param robot
	 *            an instance of a Robot
	 */
	public UserInterface(Robot robot) {
		this.robot = robot;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new UserInterface(new Robot()));
	}

	@Override
	public void run() {
		robot.setPanel(this);
		JFrame frame = new JFrame("PID Illustrator");
		frame.setLayout(new BorderLayout());
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double w = dim.getWidth(); // subtract the border width
		dim.setSize(w, w * Robot.LANE_WIDTH / Robot.LANE_LENGTH);
		setPreferredSize(dim);
		frame.add(this, BorderLayout.CENTER);
		FlowLayout controlPanelLayout = new FlowLayout();
		/* Create a panel with the controls */
		JPanel controlPanel = new JPanel(controlPanelLayout);
		controlPanel.setPreferredSize(new Dimension((int) w, 72));
		controlPanel
				.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		// controlPanelLayout.setAlignment(FlowLayout.RIGHT);

		NumberFormat numFormat = NumberFormat.getNumberInstance();
		numFormat.setMaximumFractionDigits(7);
		NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMaximumFractionDigits(2);

		/*
		 * Measurement Noise
		 */
		controlPanel.add(new JLabel("Sensor Noise (mm):"));
		sensorNoiseField = new JFormattedTextField(numFormat);
		sensorNoiseField.setValue(new Double(20));
		sensorNoiseField.setColumns(5);
		sensorNoiseField.addPropertyChangeListener(this);
		sensorNoiseField.addActionListener(this);
		controlPanel.add(sensorNoiseField);
		/*
		 * Steering Noise
		 */
		controlPanel.add(new JLabel("Steering Noise (mm/s):"));
		steeringNoiseField = new JFormattedTextField(numFormat);
		steeringNoiseField.setValue(new Double(5.0));
		steeringNoiseField.setColumns(5);
		steeringNoiseField.addPropertyChangeListener(this);
		steeringNoiseField.addActionListener(this);
		controlPanel.add(steeringNoiseField);
		/*
		 * Steering Noise
		 */
		controlPanel.add(new JLabel("Steering Drift(%):"));
		steeringDriftField = new JFormattedTextField(percentFormat);
		steeringDriftField.setValue(new Double(0.03));
		steeringDriftField.setColumns(5);
		steeringDriftField.addPropertyChangeListener(this);
		steeringDriftField.addActionListener(this);
		controlPanel.add(steeringDriftField);
		/*
		 * Timing Noise
		 */
		controlPanel.add(new JLabel("Timing Noise (ms):"));
		timingNoiseField = new JFormattedTextField(numFormat);
		timingNoiseField.setValue(new Integer(50));
		timingNoiseField.setColumns(5);
		timingNoiseField.addPropertyChangeListener(this);
		timingNoiseField.addActionListener(this);
		controlPanel.add(timingNoiseField);
		/*
		 * Y-offset field
		 */
		controlPanel.add(new JLabel("Y Offset (mm):"));
		yOffsetField = new JFormattedTextField(numFormat);
		yOffsetField.setValue(new Double(yOffset));
		yOffsetField.setColumns(5);
		yOffsetField.addPropertyChangeListener(this);
		yOffsetField.addActionListener(this);
		controlPanel.add(yOffsetField);
		/*
		 * Heading field
		 */
		controlPanel.add(new JLabel("Heading (deg.):"));
		headingField = new JFormattedTextField(numFormat);
		headingField.setValue(new Double(heading));
		headingField.setColumns(5);
		headingField.addPropertyChangeListener(this);
		headingField.addActionListener(this);
		controlPanel.add(headingField);
		/*
		 * Proportional field
		 */
		controlPanel.add(new JLabel("Proportional:"));
		coeffProportionalField = new JFormattedTextField(numFormat);
		coeffProportionalField.setValue(new Double(coeffProportional));
		coeffProportionalField.setColumns(5);
		coeffProportionalField.addPropertyChangeListener(this);
		coeffProportionalField.addActionListener(this);
		controlPanel.add(coeffProportionalField);
		/*
		 * Integral field
		 */
		controlPanel.add(new JLabel("Integral:"));
		coeffIntegralField = new JFormattedTextField(numFormat);
		coeffIntegralField.setValue(new Double(coeffIntegral));
		coeffIntegralField.setColumns(5);
		coeffIntegralField.addPropertyChangeListener(this);
		coeffIntegralField.addActionListener(this);
		controlPanel.add(coeffIntegralField);
		/*
		 * Differential field
		 */
		controlPanel.add(new JLabel("Differential:"));
		coeffDifferentialField = new JFormattedTextField(numFormat);
		coeffDifferentialField.setValue(new Double(coeffDifferential));
		coeffDifferentialField.setColumns(5);
		coeffDifferentialField.addPropertyChangeListener(this);
		coeffDifferentialField.addActionListener(this);
		controlPanel.add(coeffDifferentialField);
		/*
		 * Run button
		 */
		JButton runButton = new JButton("Run");
		runButton.addActionListener(this);
		controlPanel.add(runButton);
		frame.add(controlPanel, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		/* Initialize the robot with default values*/
		robot.setSensorNoise(((Number) sensorNoiseField.getValue())
				.floatValue());
		robot.setSteeringDrift(1F + ((Number) steeringDriftField.getValue())
				.floatValue());
		robot.setSteeringNoise(((Number) steeringNoiseField.getValue())
				.floatValue());
		robot.setTimingNoise(((Number) timingNoiseField.getValue()).intValue());

	}

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setStroke(new BasicStroke(2.0F));
		g2.setColor(Color.GREEN);
		g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
		robot.paintSelf(g2);
	}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		yOffset = ((Number) yOffsetField.getValue()).floatValue();
		heading = ((Number) headingField.getValue()).floatValue();
		coeffIntegral = ((Number) coeffIntegralField.getValue()).floatValue();
		coeffDifferential = ((Number) coeffDifferentialField.getValue())
				.floatValue();
		coeffProportional = ((Number) coeffProportionalField.getValue())
				.floatValue();
		robot.setSensorNoise(((Number) sensorNoiseField.getValue())
				.floatValue());
		robot.setSteeringDrift(1F + ((Number) steeringDriftField.getValue())
				.floatValue());
		robot.setSteeringNoise(((Number) steeringNoiseField.getValue())
				.floatValue());
		robot.setTimingNoise(((Number) timingNoiseField.getValue()).intValue());
		robot.setYPos(((Number) yOffsetField.getValue()).floatValue());
		robot.setHeading(((Number) headingField.getValue()).floatValue());
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		robot.run(coeffProportional, coeffIntegral, coeffDifferential);
		repaint();
	}
}
