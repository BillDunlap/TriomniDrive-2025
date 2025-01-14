// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
// import com.kauailabs.navx.AHRSProtocol;
// import com.kauailabs.navx.frc.AHRS;
import com.studica.frc.AHRS;
import com.studica.frc.AHRS.NavXComType;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructArrayPublisher;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
// import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
// import frc.robot.subsystems.OmniWheel; // no need to import class in same directory
import frc.robot.Constants;
// import frc.robot.TuningVariables;

public class DriveTrain extends SubsystemBase {
  private final OmniWheel m_front;
  private final OmniWheel m_right;
  private final OmniWheel m_left;
  private double m_kF;
  private double m_kP;
  private final AHRS m_ahrs;

  /** Creates a new DriveTrain. */
  public DriveTrain() {
    m_front = new OmniWheel(1, "front", Constants.kWheelDiameterFt);
    //SmartDashboard.putData(m_front);
    m_right = new OmniWheel(2, "right", Constants.kWheelDiameterFt);
    //SmartDashboard.putData(m_right);
    m_left = new OmniWheel(3, "left", Constants.kWheelDiameterFt);
    //SmartDashboard.putData("m_left", m_left);
    // m_ahrs = new AHRS(SPI.Port.kMXP);
    m_ahrs = new AHRS(NavXComType.kMXP_SPI);
    SendableRegistry.setName(m_ahrs, "DriveTr", "gyro");
    SmartDashboard.putData("Gyro", m_ahrs);
    // The AHRS has not settled down on an angle yet when
    // the constructor for drive train is called.
    setZeroAngleDegrees(0.0); // assume 'front' wheel points 'ahead'
    setKF(1.15);
    setKP(0.750);
    beStill();
  }

  @Override
  public void initSendable(SendableBuilder builder){
    builder.addDoubleProperty("kF", () -> m_kF, this::setKF);
    builder.addDoubleProperty("kP", () -> m_kP, this::setKP);
  }

  private void setKF(double kF){
    m_kF = kF;
    m_front.setKF(m_kF);
    m_left.setKF(m_kF);
    m_right.setKF(m_kF);
  }
  private void setKP(double kP){
    m_kP = kP;
    m_front.setKP(m_kP);
    m_left.setKP(m_kP);
    m_right.setKP(m_kP);
  }

  public void setZeroAngleDegrees(double degrees) {
    int nTries = 1;
    while (m_ahrs.isCalibrating()  & nTries < 100) { //wait to zero yaw if calibration is still running
      try {
        Thread.sleep(20);
        System.out.println("----calibrating gyro---- " + nTries);
      } catch (InterruptedException e) {

      }
      nTries++;
      if (nTries >= 50 && nTries%10==0) {
        System.out.println("Having trouble calibrating NavX");
      }
    }
    System.out.println("Setting angle adj to " + (-m_ahrs.getYaw()) + " + " + degrees + " after " + nTries + " attempts");
    m_ahrs.setAngleAdjustment(-m_ahrs.getYaw() + degrees);
  }

  public void setMotorsFractionPower(double front, double right, double left){
    m_front.setFractionPower(front);
    m_right.setFractionPower(right);
    m_left.setFractionPower(left);
  }

  /**
   * Set velocities of the motors in clicks per 100 ms.
   * @param front: velocity of front motor
   * @param right: velocity of right motor
   * @param left: velocity of left motor
   */
  public void setMotorVelocitiesNativeUnits(double front, double right, double left) {
    m_front.setVelocityNativeUnits(front);
    m_right.setVelocityNativeUnits(right);
    m_left.setVelocityNativeUnits(left);
  }

  public void setWheelVelocitiesFps(double frontFeetPerSecond, double rightFeetPerSecond, double leftFeetPerSecond){
    m_front.setVelocityFeetPerSecond(frontFeetPerSecond);
    m_right.setVelocityFeetPerSecond(rightFeetPerSecond);
    m_left.setVelocityFeetPerSecond(leftFeetPerSecond); 
  }

  /** spin robot using same power to all omniwheels
   *  @param fractionPower: number in [-1,1] saying what fraction of maximum power
   * to give each wheel.  Positive for clockwise, negative counterclockwise.
   * Useful for testing, but not recommended for general use.
   */
  public void spinFractionPower(double fractionPower) {
    setMotorsFractionPower(fractionPower, fractionPower, fractionPower);
  }

  /**
   * 
   * @param velocity: turn all wheels at this many clicks per 100 ms.
   *   This is useful for testing, but not recommended for general use.
   */
  public void spinNativeUnits(double velocity) {
    setMotorVelocitiesNativeUnits(velocity, velocity, velocity);
  }

  /**
   * 
   * @param degreesPerSecond: the robot itself (not a wheel) should turn this many degrees per second.
   * Positive degrees means clockwise, negative counterclockwise
   */
  public void spinDegreesPerSecond(double degreesPerSecond){
    double wheelVelocityFeetPerSecond = 2.0 * Math.PI * Constants.kCenterToWheelFt / 360. * degreesPerSecond;
    setWheelVelocitiesFps(wheelVelocityFeetPerSecond, wheelVelocityFeetPerSecond, wheelVelocityFeetPerSecond);
  }

  public void beStill() {
    setMotorsFractionPower(0.0, 0.0, 0.0);
  }

  /**
   * Make robot travel in a straight line
   * @param feetPerSecond: desired robot speed
   * @param radians: direction of travel in radians counterclockwise from straight ahead
   */
  public void setVelocityFpsRadians(double feetPerSecond, double radians){
    double frontFeetPerSecond = feetPerSecond * Math.cos( radians - 9./6. * Math.PI);
    double rightFeetPerSecond = feetPerSecond * Math.cos( radians - 5./6. * Math.PI);
    double leftFeetPerSecond = feetPerSecond * Math.cos( radians - 1./6. * Math.PI);
    setWheelVelocitiesFps(frontFeetPerSecond, rightFeetPerSecond, leftFeetPerSecond);
  }

  public void setVelocityFpsDegrees(double feetPerSecond, double degrees){
    setVelocityFpsRadians(feetPerSecond, -degrees / 180. * Math.PI);
  }

  public void setVelocityFpsDegrees_FieldOriented(double feetPerSecond, double degrees){
    double orientationDegrees = m_ahrs.getAngle();
    setVelocityFpsDegrees(feetPerSecond, degrees - orientationDegrees);
  }


  @Override
  public void periodic() {}
  private static class OmniWheel extends SubsystemBase {
    private final TalonSRX m_talonsrx;
    private double m_fractionPower;
    private final double m_diameter;
     // The triomnidrive robot has US Digital E4P-360-250 encoders.
     //    360 cycles (== 1440 clicks) per revolution and hole for quarter inch shaft.
    private final double m_clicksPerRevolution = 360 * 4;
    private final double m_nativeUnitsOverRevolutionsPerSecond;
    private final double m_nativeUnitsOverFeetPerSecond;  
    private double m_kF;
    private double m_kP;
    private double m_targetClicksPer100Ms;
    private double m_currentClicksPer100Ms;
    private double m_errorClicksPer100Ms;
    private StructArrayPublisher<Translation2d> publisher = 
      NetworkTableInstance.getDefault()
      .getStructArrayTopic("Volts,Speed", Translation2d.struct).publish();
    /** Creates a new OmniWheel, controlled by a TalonSRX motor controller
     *  @param canID:  CAN bus identifier for the motor controller
     *  @param name: a name for the wheel to be used in SmartDashboard displays
     *  @param diameter: diameter of wheel in feet
     */ 
    OmniWheel(int canId, String name, double diameter) {
      m_talonsrx = new TalonSRX(canId);
      setName(name);
      m_fractionPower = 0;
      m_diameter = diameter;
      m_talonsrx.configFactoryDefault();
      m_talonsrx.clearStickyFaults();
      m_nativeUnitsOverRevolutionsPerSecond = m_clicksPerRevolution * 
                                              0.1 ;    // seconds per 100ms
      m_nativeUnitsOverFeetPerSecond = 1.0 / (Math.PI * m_diameter) *    // revolutions per feet travelled
                                       m_nativeUnitsOverRevolutionsPerSecond ;
      m_talonsrx.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, Constants.TalonSRX.kDriveTrainPIDLoopIndex, Constants.TalonSRX.kTimeoutMs);
      m_talonsrx.setSensorPhase(true);
      // for robot up on blocks, kF=1.23 and kP1.6 give ok results.
      // The question is, why?  Won't kF change when we put robot on floor?
      // Later I found that kI is essential when kF is not known well.
      // When on blocks typical accumulated deficits were 25-75 thousand when
      // requested speed was 150 or 200.
      // The above was written by me a few years ago.  It is wrong.  I should
      // figure out a better (bigger, I think - units are Volts/SpeedUnit) kF and set kI to 0.
      setKF(0.0);
      setKP(0.0);
      m_talonsrx.config_kI(Constants.TalonSRX.kDriveTrainPIDLoopIndex, 0.0);
      m_talonsrx.config_kD(Constants.TalonSRX.kDriveTrainPIDLoopIndex, 0.0); 
      SmartDashboard.putData(this); 
    }

    /**
     * set feed forward parameter.
     * @param kF feed forward parameter.  Units are Volts per Clicks/100Milliseconds.
     */
    public void setKF(double kF){
      m_kF = kF;
      m_talonsrx.config_kF(Constants.TalonSRX.kDriveTrainPIDLoopIndex, m_kF); 
    }
    public void setKP(double kP){
      m_kP = kP;
      m_talonsrx.config_kP(Constants.TalonSRX.kDriveTrainPIDLoopIndex, m_kP);
    }
 
    /** Set the speed of the wheel on a scale of-1 to 1. 
        @param fractionPower: -1 is maximum power in reverse, 1 is maximum power forward.
          0 means stopped.
        */
    public void setFractionPower(double fractionPower) {
      m_targetClicksPer100Ms = -1;  // code for don't care
      m_fractionPower = fractionPower;
      m_talonsrx.set(ControlMode.PercentOutput,m_fractionPower);
    }

    public void setVelocityRevolutionsPerSecond(double revolutionsPerSecond){
      double targetClicksPer100Ms = revolutionsPerSecond * m_nativeUnitsOverRevolutionsPerSecond;
      setVelocityNativeUnits(targetClicksPer100Ms);
    }
    public void setVelocityFeetPerSecond(double feetPerSecond){
      double clicksPer100Ms = feetPerSecond * m_nativeUnitsOverFeetPerSecond;
      setVelocityNativeUnits(clicksPer100Ms);
    }

    /** set target velocity in "native" units: ticks per 100 milliseconds */
    public void setVelocityNativeUnits(double targetClicksPer100Ms) {
      m_targetClicksPer100Ms = targetClicksPer100Ms;
      m_talonsrx.set(ControlMode.Velocity, m_targetClicksPer100Ms);
    }

    @Override
    public void periodic() {
      // use this to collect sensor information to avoid double collection.
      m_currentClicksPer100Ms = m_talonsrx.getSelectedSensorVelocity();
      m_errorClicksPer100Ms = m_targetClicksPer100Ms - m_currentClicksPer100Ms;
      publisher.set(new Translation2d[]{new Translation2d(m_talonsrx.getMotorOutputVoltage(), m_currentClicksPer100Ms)});
    }

    @Override
    public void initSendable(SendableBuilder builder) {
      // super.initSendable(builder); // add .hasDefault, .default, .hasCommand, and .command properties
      builder.addDoubleProperty("percentPower", () -> m_talonsrx.getMotorOutputPercent(), null);
      builder.addDoubleProperty("motorVoltage", () -> m_talonsrx.getMotorOutputVoltage(), null);
      builder.addDoubleProperty("target velocity (native units)", () -> m_targetClicksPer100Ms, null);
      builder.addDoubleProperty("vel. error (native units)", () -> m_errorClicksPer100Ms, null);
      builder.addDoubleProperty("velocity (native units)", () -> m_currentClicksPer100Ms, null);
      builder.addDoubleProperty("V_over_sp", () -> { return Math.abs(m_talonsrx.getSelectedSensorVelocity())>5
          ? m_talonsrx.getMotorOutputVoltage()/m_talonsrx.getSelectedSensorVelocity()
          : 0.0;}, null);
      //builder.addDoubleProperty("integral accumulation",() -> m_talonsrx.getIntegralAccumulator(), null);
      builder.addDoubleProperty("kF", () -> m_kF, null);
      builder.addDoubleProperty("kP", () -> m_kP, null);
      // builder.addDoubleProperty("setpoint", this::getSetpoint, this::setSetpoint);     builder.addDoubleProperty("velocity", m_talonsrx.getSelectedSensorVelocity());
    }
  }
}
