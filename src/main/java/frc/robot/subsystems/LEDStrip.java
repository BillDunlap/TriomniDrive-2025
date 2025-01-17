// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;

import java.util.Map;

import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
//import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.AddressableLEDBufferView;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.util.Color;

public class LEDStrip extends SubsystemBase {
  private AddressableLED m_ledStrip;
  private AddressableLEDBuffer m_ledBuffer;
  private AddressableLEDBufferView m_bottom;
  private AddressableLEDBufferView m_top;
  private LEDPattern m_gradient = LEDPattern.gradient(LEDPattern.GradientType.kDiscontinuous, Color.kRed, Color.kYellow);
  private Distance m_ledSpacing = Inches.of(3.0 / 9.0);
  private LEDPattern m_scrollingGradient = m_gradient.scrollAtAbsoluteSpeed(Inches.per(Second).of(2.0), m_ledSpacing);

  private LEDPattern m_allBlue = LEDPattern.solid(Color.kBlue);

  /** Creates a new LEDStrip. */
  public LEDStrip(int pwmPort, int length) {
    m_ledStrip = new AddressableLED(pwmPort);
    m_ledStrip.setLength(length);
    m_ledBuffer = new AddressableLEDBuffer(length);
    m_bottom = m_ledBuffer.createView(0, length/2-1);
    m_top = m_ledBuffer.createView(length/2, length-1).reversed();
    m_ledStrip.start();
    setDefaultCommand(defaultCommand());
  }
  
/**
 * periodic() does nothing: all repeated behavior is done via commands 
 **/  
  @Override
  public void periodic() {}

  public Command defaultCommand() {
    return new InstantCommand(() ->
    {
      m_allBlue.applyTo(m_ledBuffer);
      m_ledStrip.setData(m_ledBuffer);
    },
    this).withName("Default Command");
  }

  public Command scrollFromCenter() {
    return new RunCommand(() ->
      {
        m_scrollingGradient.reversed().applyTo(m_bottom);
        m_scrollingGradient.reversed().applyTo(m_top);
        m_ledStrip.setData(m_ledBuffer);
      },
      this
    ).withName("Scroll Out From Center");
  }
  public Command breath(){
    return new RunCommand(() ->
    {
      m_allBlue.breathe(Seconds.of(2.5)).applyTo(m_ledBuffer);
      m_ledStrip.setData(m_ledBuffer);
    },
    this).withName("breath");
  }
  /**
   * Scroll a sequence of colors
   * @param positionColorMap - Map.of(startingPosition0,color0, startingPosition1,color1, ...)
   *   where starting positions are increasing numbers between 0 (start of LED strip) and 1 (end
   *   of LED strip)
   * @param inchesPerSecond - speed of scrolling.  E.g., Inches.per(Second).of(2.0).
   * @return - a command that will scroll that pattern at given speed
   */
  public Command scrollSteps(Map<Double,Color> positionColorMap, LinearVelocity inchesPerSecond) {
    LEDPattern pattern = LEDPattern.steps(positionColorMap).scrollAtAbsoluteSpeed(inchesPerSecond, m_ledSpacing);
    return new RunCommand(() ->
    {
      pattern.applyTo(m_ledBuffer);
      m_ledStrip.setData(m_ledBuffer);
    },
    this).withName("Scroll Steps");
  }
}
