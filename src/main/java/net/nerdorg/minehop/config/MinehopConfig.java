package net.nerdorg.minehop.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.nerdorg.minehop.Minehop;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Minehop.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MinehopConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Movement master switch.")
            .define("enabled", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_SSJ = BUILDER
            .comment("Show your SSJ (the white numbers) under your crosshair.")
            .define("showSSJ", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_EFFICIENCY = BUILDER
            .comment("Show your strafe efficiency (the percentage) under your crosshair.")
            .define("showEfficiency", true);

    private static final ForgeConfigSpec.BooleanValue SHOW_SPEED = BUILDER
            .comment("Show your current speed in the bottom left.")
            .define("showSpeed", true);

    private static final ForgeConfigSpec.ConfigValue<Double> SV_FRICTION = BUILDER
            .comment("Ground friction.")
            .define("sv_friction", 0.35);

    private static final ForgeConfigSpec.ConfigValue<Double> SV_ACCELERATE = BUILDER
            .comment("Ground acceleration.")
            .define("sv_accelerate", 0.1);

    private static final ForgeConfigSpec.ConfigValue<Double> SV_AIRACCELERATE = BUILDER
            .comment("Air acceleration.")
            .define("sv_airaccelerate", 1.0E99);

    private static final ForgeConfigSpec.ConfigValue<Double> SV_MAXAIRSPEED = BUILDER
            .comment("Max air acceleration per tick.")
            .define("sv_maxairspeed", 0.02325);

    private static final ForgeConfigSpec.ConfigValue<Double> SPEED_MUL = BUILDER
            .comment("Ground speed multiplier.")
            .define("speed_mul", 2.2);

    private static final ForgeConfigSpec.ConfigValue<Double> SV_GRAVITY = BUILDER
            .comment("Gravity value. Yes it can be negative.")
            .define("sv_gravity", 0.066);


    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enabled = true;
    public static boolean show_ssj = true;
    public static boolean show_efficiency = true;
    public static boolean show_current_speed = true;
    public static double sv_friction = 0.35;
    public static double sv_accelerate = 0.1;
    public static double sv_airaccelerate = 1.0E99;
    public static double sv_maxairspeed = 0.02325;
    public static double speed_mul = 2.2;
    public static double sv_gravity = 0.066;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        enabled = ENABLED.get();
        show_ssj = SHOW_SSJ.get();
        show_efficiency = SHOW_EFFICIENCY.get();
        show_current_speed = SHOW_SPEED.get();
        sv_friction = SV_FRICTION.get();
        sv_accelerate = SV_ACCELERATE.get();
        sv_airaccelerate = SV_AIRACCELERATE.get();
        sv_maxairspeed = SV_MAXAIRSPEED.get();
        speed_mul = SPEED_MUL.get();
        sv_gravity = SV_GRAVITY.get();
    }
}
