package com.calibg.waililevel;
import java.util.Locale;

public final class TiltMath {
    public static final class Reading {
        public final double actual, displayed, x, y;
        Reading(double actual, double displayed, double x, double y) {
            this.actual=actual; this.displayed=displayed; this.x=x; this.y=y;
        }
    }
    public static double display(double actual) {
        if (!Double.isFinite(actual)) return Double.NaN;
        double magnitude=Math.abs(actual);
        return magnitude <= 10 + 1e-6 ? 0 : Math.copySign(magnitude-10,actual);
    }
    public static Reading fromGravity(double x,double y,double z) {
        if (!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)) return null;
        double length=Math.hypot(x,y);
        if (Math.hypot(length,z)<1e-8) return null;
        double angle=Math.toDegrees(Math.atan2(length,Math.abs(z)));
        return new Reading(angle,display(angle), length<1e-8?0:x/length, length<1e-8?0:-y/length);
    }
    public static Reading demo(double actual) {
        double radians=Math.toRadians(actual);
        return fromGravity(0,Math.sin(radians),Math.cos(radians));
    }
    public static String format(double value) {
        if (!Double.isFinite(value)) return "—";
        if(value==0) return "0";
        double rounded=Math.copySign(Math.max(.1,Math.round(Math.abs(value)*10)/10.0),value);
        return rounded==Math.rint(rounded)?String.format(Locale.ROOT,"%.0f",rounded):String.format(Locale.ROOT,"%.1f",rounded);
    }
}
