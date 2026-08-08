package com.horseinspector.plugin;

public record HorseStats(double health, double speed, double jump) {

    public static HorseStats averageOf(HorseStats a, HorseStats b) {
        return new HorseStats(
                (a.health() + b.health()) / 2.0,
                (a.speed() + b.speed()) / 2.0,
                (a.jump() + b.jump()) / 2.0
        );
    }
}
