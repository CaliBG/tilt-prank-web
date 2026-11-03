package com.calibg.waililevel;
import org.junit.Test;
import static org.junit.Assert.*;
public class TiltMathTest {
 @Test public void deadzone(){for(double a:new double[]{-10,-1,0,1,10})assertEquals(0,TiltMath.display(a),0);}
 @Test public void increments(){assertEquals(5,TiltMath.display(15),0);assertEquals(10,TiltMath.display(20),0);assertEquals(80,TiltMath.display(90),0);assertEquals(-5,TiltMath.display(-15),0);}
 @Test public void tinyValues(){assertEquals("0.1",TiltMath.format(TiltMath.display(10.001)));assertEquals("5",TiltMath.format(5));}
 @Test public void gravityAxes(){double s=Math.sin(Math.toRadians(15)),c=Math.cos(Math.toRadians(15));assertEquals(5,TiltMath.fromGravity(s,0,c).displayed,1e-8);assertEquals(5,TiltMath.fromGravity(0,s,c).displayed,1e-8);assertEquals(5,TiltMath.fromGravity(-s,0,c).displayed,1e-8);}
 @Test public void diagonal(){assertTrue(TiltMath.fromGravity(.16,.16,.974).displayed>2);}
 @Test public void faceDown(){assertEquals(0,TiltMath.fromGravity(0,0,-9.8).displayed,0);assertEquals(80,TiltMath.fromGravity(9.8,0,0).displayed,1e-8);}
 @Test public void invalid(){assertNull(TiltMath.fromGravity(0,0,0));assertNull(TiltMath.fromGravity(Double.NaN,0,1));assertEquals("—",TiltMath.format(Double.NaN));}
 @Test public void exactTen(){assertEquals(0,TiltMath.demo(10).displayed,0);}
}
