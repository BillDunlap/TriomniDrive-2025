// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.TuningVariables;
import frc.robot.subsystems.ControllerRumbler;
import frc.robot.subsystems.DriveTrain;
import edu.wpi.first.wpilibj.XboxController;

public class DefaultTeleop extends Command {
  /** Creates a new DefaultTeleop. */
  private final DriveTrain m_driveTrain;
  private final XboxController m_controller;
  private final ControllerRumbler m_controllerRumbler;
  private double defaultSpinRate_DegreesPerSecond;
  private double defaultTravelRate_FeetPerSecond;
  
  /** Creates a new DefaultTeleop. */
  public DefaultTeleop(DriveTrain driveTrain, XboxController controller, ControllerRumbler controllerRumbler) {
    m_driveTrain = driveTrain;
    m_controller = controller;
    m_controllerRumbler = controllerRumbler;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(m_driveTrain);
  }
  
  // Called when the command is initially scheduled.
  // A default command for subsystem is scheduled after and interrupting command is done.
  // E.g., if default command handles quantitative inputs, like joystick or gyroscope
  // readings, for a subsystem but you have other commands attached to button presses,
  // then just before each time a button-press-initiated command runs the default command's
  // end(interrupted=TRUE) method will be called and after the button-press-initiated command
  // is done the default command's initialize method will be called again.  Hence, don't
  // do things like zero the gyro in initialize.
  @Override
  public void initialize() {
    m_driveTrain.beStill();
    m_controllerRumbler.beQuiet();
    defaultSpinRate_DegreesPerSecond = TuningVariables.defaultSpinRate_DegreesPerSecond.get();
    defaultTravelRate_FeetPerSecond = TuningVariables.defaultTravelRate_FeetPerSecond.get();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    double z;
    boolean rumbleNeeded = false;
    if ((z = m_controller.getLeftTriggerAxis()) > 0.0) { // z is in [0,1]
      rumbleNeeded = z > .90;
      m_driveTrain.spinDegreesPerSecond(-z * 90.0);
    } else if ((z = m_controller.getRightTriggerAxis()) > 0.0) {
      rumbleNeeded = z > 0.90;
      m_driveTrain.spinDegreesPerSecond(+z * 90.0);
    } else if (m_controller.getLeftBumperButton()) {
      m_driveTrain.spinDegreesPerSecond(-defaultSpinRate_DegreesPerSecond);
    } else if (m_controller.getRightBumperButton()) {
      m_driveTrain.spinDegreesPerSecond(+defaultSpinRate_DegreesPerSecond);
    //} else if ((z = m_controller.getPOV()) != -1) { // if POV button is pressed, z is multiple of 45 degrees, if not pressed then -1
    //  m_driveTrain.setVelocityFpsDegrees_FieldOriented(defaultTravelRate_FeetPerSecond, z);
    } else { // if nothing else pressed, go according to left joystick
      double x = m_controller.getLeftX();
      double y = m_controller.getLeftY();
      // SmartDashboard.putNumber("Left X", x);
      // SmartDashboard.putNumber("Left y", y);
      if (Math.abs(x) <= 0.025 && Math.abs(y) <= 0.025) {
        m_driveTrain.beStill();
        m_controllerRumbler.beQuiet();
      } else {
        double radians = Math.atan2(-y, x) - Math.PI/2 ;
        double fps = Math.min(Math.hypot(x, y), 1.0) * 4.0 ; // 4 ft/s is easy walking speed
        rumbleNeeded = fps >= 4.0;
        m_driveTrain.setVelocityFpsDegrees_FieldOriented(fps, -radians / Math.PI * 180);
      }
    }
    if (rumbleNeeded) {
      m_controllerRumbler.setRumble(.5); // gentle rumble if speeding
    } else {
      m_controllerRumbler.beQuiet();
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_driveTrain.beStill();
    m_controllerRumbler.beQuiet();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
