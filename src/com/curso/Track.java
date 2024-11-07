package com.curso;

import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdOriented;

public class Track extends TLcdLonLatHeightPoint implements ILcdOriented {
    private final double fHeading;

    public Track(double aLon, double aLat, double aZ, double aHeading) {
        super(aLon, aLat, aZ);
        fHeading = aHeading;
    }

    @Override
    public double getOrientation() {
        return fHeading;
    }
}
