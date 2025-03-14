// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.REVLibError;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class BrushlessSparkMax extends SubsystemBase {
  private final SparkMax m_SparkMax;
  /** Creates a new BrushlessSparkMax. */
  public BrushlessSparkMax(int canId) {
    m_SparkMax = new SparkMax(58, MotorType.kBrushless);
    SparkMaxConfig config = new SparkMaxConfig();
    config
      .idleMode(IdleMode.kBrake)
      .smartCurrentLimit(50);
    REVLibError error = m_SparkMax.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    if (!error.equals(REVLibError.kOk)) {
      DataLogManager.log("Error configuring SparkMax " + canId + ": " + error);
    }
    m_SparkMax.set(0.0);
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
