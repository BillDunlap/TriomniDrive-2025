// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static edu.wpi.first.units.Units.Inches;

import java.util.ArrayList;
import java.util.Map;

import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.util.sendable.SendableBuilder;
//import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.AddressableLEDBufferView;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.util.Color;

public class LEDStrip extends SubsystemBase {
  private final AddressableLED m_ledStrip;
  private final int m_length;
  private final AddressableLEDBuffer m_ledBuffer;
  private Distance m_ledSpacing;

  /**
   * Creates a new LEDStrip object.  Default ledSpacing is 1/3 of an inch.
   * @param pwmPort - the numer of the PWN port on the Roborio that the strip's
   * signal wire is attached to.
   * @param length - the number of LEDs in the strip.
   */
  public LEDStrip(int pwmPort, int length) {
    this(pwmPort, length, Inches.of(3.0 / 9.0));
  }
  /**
  * Creates a new LEDStrip object.  Default ledSpacing is 1/3 of an inch.
  * @param pwmPort - the numer of the PWN port on the Roborio that the strip's
  * signal wire is attached to.
  * @param length - the number of LEDs in the strip.
  * @param ledSpacing - the distance between the LEDs in the strip.
  */
  public LEDStrip(int pwmPort, int length, Distance ledSpacing){
    m_length = length;
    m_ledStrip = new AddressableLED(pwmPort);
    m_ledStrip.setLength(m_length);
    m_ledBuffer = new AddressableLEDBuffer(m_length);
    m_ledStrip.start();

    m_ledSpacing = ledSpacing;
  }
  
  @Override
  public void periodic() {
    /* Send current data to the LED strip.  This really only
     * needs to be done if the data has changed, but we currently
     * do not insist that the writers set a flag saying if
     * they change the data.
     */
    m_ledStrip.setData(m_ledBuffer);
  }

  @Override
  public void initSendable(SendableBuilder builder) {
      super.initSendable(builder); // add .hasDefault, .default, .hasCommand, and .command properties
  }

  private AddressableLEDBuffer getLedBuffer() {
    return m_ledBuffer;
  }

  private Distance getLedSpacing(){
    return m_ledSpacing;
  }

  private int getLength(){
    return m_length;
  }

  public static class ViewDatum {
    public int m_startingIndex;
    public int m_endingIndex;
    public boolean m_reversed;
    /**
     * Describe a view into one segment of an LED strip.  A Writer contains
     * any number of such views.
     * @param startingIndex - the starting index of the segment (start at 0)
     * @param endingIndex - the ending index (inclusive) of the segment
     * @param reversed - should a pattern be reversed when applied to this segment?
     */
    public ViewDatum(int startingIndex, int endingIndex, boolean reversed){
      m_startingIndex = startingIndex;
      m_endingIndex = endingIndex;
      m_reversed = reversed;
    }
  }

  /**
   * A LEDStrip.Writer lets you apply the same pattern to multiple segments of a
   * strips of LEDs.
   * @param viewData - zero or more LEDString.viewData objects giving the starting
   * index, length and whether a pattern should be reversed for each segment. 
   * If no viewData arguments are given, the view will be of the entire LED strip.
   */
  public Writer createWriter(ViewDatum... viewData){
    return new Writer(viewData);
  }

  public class Writer extends SubsystemBase {
    private AddressableLEDBuffer m_ledBuffer;
    private ArrayList<AddressableLEDBufferView> m_views = new ArrayList<>();
    /**
     * A LEDStrip.Writer lets you apply the same pattern to multiple segments of a
     * strips of LEDs.
     * @param viewData - zero or more LEDString.viewData objects giving the starting
     * index, length and whether a pattern should be reversed for each segment. 
     * If no viewData arguments are given, the view will be of the entire LED strip.
     * (LedStrip.createWriter() is a more convenient way to use this constructor.)
     */

    public Writer(LEDStrip.ViewDatum... viewData){
      m_ledBuffer = getLedBuffer();
      AddressableLEDBufferView view;
      // System.out.println("### getLength() -> " + getLength());
      if (viewData.length == 0) {
        m_views.add(m_ledBuffer.createView(0, getLength()-1));
      } else {
        for (ViewDatum viewDatum : viewData) {
          //System.out.println("### from " + viewDatum.m_startingIndex + " to " + viewDatum.m_endingIndex);
          if (viewDatum.m_reversed){
            view = m_ledBuffer.createView(viewDatum.m_startingIndex, viewDatum.m_endingIndex).reversed();
          } else {
            view = m_ledBuffer.createView(viewDatum.m_startingIndex, viewDatum.m_endingIndex);
          }
          m_views.add(view);
        }
      }
      LEDPattern defaultPattern = LEDPattern.solid(Color.kBlack);
      setDefaultCommand(new InstantCommand(() -> apply(defaultPattern), this));
    }

    /**
     * Apply the LED pattern to all the views in this LEDWriter.  This should
     * be used by all commands to a LEDStrip.Writer.
     * @param ledPattern - the pattern to apply
     */
    public void apply(LEDPattern ledPattern){
      for(AddressableLEDBufferView view: m_views){
        ledPattern.applyTo(view);
      }
    }

    /* The following command factory methods are mainly intended to be examples
     * of how to use a Writer.  You might want to put things like this in a
     * new class with methods like Alarm or Warning or in a Collector class with
     * a method called GamePieceInCollector.
     */
    /**
     * Cycle brightness of LEDs
     * @param color - the color of all the LEDs
     * @param period - how long it takes for one cycle of dimming and brightening
     * @return - a command to do the "breathing"
     */
    public Command breathe(Color color, Time period){
      LEDPattern pattern = LEDPattern.solid(color).breathe(period);
      return new RunCommand(() -> apply(pattern), this).withName("Breathe");
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
      LEDPattern pattern = LEDPattern.steps(positionColorMap).scrollAtAbsoluteSpeed(inchesPerSecond, getLedSpacing());
      return new RunCommand(() -> apply(pattern), this).withName("Scroll Steps");
    }
  }
}
