/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.wpi.first.wpilibj.templates;


import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends IterativeRobot {
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        try {
            jag1 = new CANJaguar(2);
            jag2 = new CANJaguar(3);
            jag1.configFaultTime(0.5);
            jag2.configFaultTime(0.5);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        RRCPServer.getInstance();
        RRCPCommandHandler.getInstance();
        RRCPServer.startServer();
    }
    static CANJaguar jag1, jag2;
    static int left = 0, right = 0;
    public static void setMotors(int l, int r) {
        left = l;
        right = r;
    }
    private static void driveMotors(int l, int r) {
        try {
            jag1.setX(-l);
            jag2.setX(r);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {

    }
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        driveMotors(left, right);
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
