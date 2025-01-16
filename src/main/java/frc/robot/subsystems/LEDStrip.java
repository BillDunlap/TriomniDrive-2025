// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Seconds;

import edu.wpi.first.units.Measure;
import edu.wpi.first.units.measure.Distance;
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

  private LEDPattern m_allOff = LEDPattern.solid(Color.kBlack);
  private LEDPattern m_allBlue = LEDPattern.solid(Color.kBlue);

  private boolean m_doScroll = false;

  /** Creates a new LEDStrip. */
  public LEDStrip(int pwmPort, int length) {
    m_ledStrip = new AddressableLED(pwmPort);
    m_ledStrip.setLength(length);
    m_ledBuffer = new AddressableLEDBuffer(length);
    m_bottom = m_ledBuffer.createView(0, length/2-1);
    m_top = m_ledBuffer.createView(length/2, length-1).reversed();
    //m_gradient.breathe(Seconds.of(1)).applyTo(m_top);
    //m_gradient.breathe(Seconds.of(1)).applyTo(m_bottom);
    //Distance ledSpacing = Inches.of(3.0 / 9.0);
    // LEDPattern pattern = base.scrollAtRelativeSpeed(Percent.per(Second).of(25));
    m_ledStrip.start();
    m_allBlue.applyTo(m_ledBuffer);
    m_ledStrip.setData(m_ledBuffer);
    //setDefaultCommand(scrollFromCenter());
    setDefaultCommand(breath());
    System.out.println("Started LEDStrip: length=" + length + ", pwmPort=" + pwmPort);
  }

  //@Override
  //public void initSendable(SendableBuilder builder) {
  //  super.initSendable(builder);
  //  builder.addDoubleProperty("Blink Timer", () -> m_timer.get(), null);
  //}
  
  /**
   * set all leds to dark.
   */
  public void turnAllOff(){
    m_allOff.applyTo(m_ledBuffer);
    m_ledStrip.setData(m_ledBuffer);
  }
 
  
  @Override
  public void periodic() {}

  public Command scrollFromCenter() {
    return new RunCommand(() ->
      {
        m_scrollingGradient.reversed().applyTo(m_bottom);
        m_scrollingGradient.reversed().applyTo(m_top);
        m_ledStrip.setData(m_ledBuffer);
      },
      this
    );
  }
  public Command breath(){
    return new RunCommand(() ->
    {
      m_allBlue.breathe(Seconds.of(1.0)).applyTo(m_ledBuffer);
      m_ledStrip.setData(m_ledBuffer);
    },
    this);
  }
}
