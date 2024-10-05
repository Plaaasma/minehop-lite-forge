package net.nerdorg.minehop.server.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nerdorg.minehop.Minehop;
import net.nerdorg.minehop.config.MinehopConfig;
import net.nerdorg.minehop.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity  {
    @Shadow public abstract boolean isDeadOrDying();

    @Shadow public abstract boolean hasEffect(MobEffect p_21024_);

    @Shadow public abstract boolean isSleeping();

    @Shadow public abstract void stopSleeping();

    @Shadow protected int noActionTime;

    @Shadow public abstract boolean isDamageSourceBlocked(DamageSource p_21276_);

    @Shadow protected abstract void hurtCurrentlyUsedShield(float p_21316_);

    @Shadow protected abstract void blockUsingShield(LivingEntity p_21200_);

    @Shadow @Final public WalkAnimationState walkAnimation;

    @Shadow protected float lastHurt;

    @Shadow protected abstract void actuallyHurt(DamageSource p_21240_, float p_21241_);

    @Shadow public int hurtDuration;

    @Shadow public int hurtTime;

    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot p_21127_);

    @Shadow protected abstract void hurtHelmet(DamageSource p_147213_, float p_147214_);

    @Shadow public abstract void setLastHurtByMob(@Nullable LivingEntity p_21039_);

    @Shadow protected int lastHurtByPlayerTime;

    @Shadow @javax.annotation.Nullable protected Player lastHurtByPlayer;

    @Shadow public abstract void knockback(double p_147241_, double p_147242_, double p_147243_);

    @Shadow public abstract void indicateDamage(double p_270514_, double p_270826_);

    @Shadow protected abstract boolean checkTotemDeathProtection(DamageSource p_21263_);

    @Shadow @javax.annotation.Nullable protected abstract SoundEvent getDeathSound();

    @Shadow protected abstract float getSoundVolume();

    @Shadow public abstract float getVoicePitch();

    @Shadow public abstract void die(DamageSource p_21014_);

    @Shadow protected abstract void playHurtSound(DamageSource p_21160_);

    @Shadow @javax.annotation.Nullable private DamageSource lastDamageSource;

    @Shadow private long lastDamageStamp;

    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.is(DamageTypes.FALL) && !MinehopConfig.fall_damage) {
            cir.setReturnValue(false);
            cir.cancel();
        }
        else {
            LivingEntity self = (LivingEntity) this.level().getEntity(this.getId());

            if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(self, source, amount)) cir.setReturnValue(false);
            if (this.isInvulnerableTo(source)) {
                cir.setReturnValue(false);
            } else if (this.level().isClientSide) {
                cir.setReturnValue(false);
            } else if (this.isDeadOrDying()) {
                cir.setReturnValue(false);
            } else if (source.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                cir.setReturnValue(false);
            } else {
                if (this.isSleeping() && !this.level().isClientSide) {
                    this.stopSleeping();
                }

                this.noActionTime = 0;
                float f = amount;
                boolean flag = false;
                float f1 = 0.0F;
                if (amount > 0.0F && this.isDamageSourceBlocked(source)) {
                    net.minecraftforge.event.entity.living.ShieldBlockEvent ev = net.minecraftforge.common.ForgeHooks.onShieldBlock(self, source, amount);
                    if (!ev.isCanceled()) {
                        if (ev.shieldTakesDamage()) this.hurtCurrentlyUsedShield(amount);
                        f1 = ev.getBlockedDamage();
                        amount -= ev.getBlockedDamage();
                        if (!source.is(DamageTypeTags.IS_PROJECTILE)) {
                            Entity entity = source.getDirectEntity();
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingentity = (LivingEntity) entity;
                                this.blockUsingShield(livingentity);
                            }
                        }

                        flag = amount <= 0;
                    }
                }

                if (source.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                    amount *= 5.0F;
                }

                this.walkAnimation.setSpeed(1.5F);
                boolean flag1 = true;
                if ((float) this.invulnerableTime > 10.0F && !source.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                    if (amount <= this.lastHurt) {
                        cir.setReturnValue(false);
                    }

                    this.actuallyHurt(source, amount - this.lastHurt);
                    this.lastHurt = amount;
                    flag1 = false;
                } else {
                    this.lastHurt = amount;
                    this.invulnerableTime = 20;
                    this.actuallyHurt(source, amount);
                    this.hurtDuration = 10;
                    this.hurtTime = this.hurtDuration;
                }

                if (source.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                    this.hurtHelmet(source, amount);
                    amount *= 0.75F;
                }

                Entity entity1 = source.getEntity();
                if (entity1 != null) {
                    if (entity1 instanceof LivingEntity) {
                        LivingEntity livingentity1 = (LivingEntity) entity1;
                        if (!source.is(DamageTypeTags.NO_ANGER)) {
                            this.setLastHurtByMob(livingentity1);
                        }
                    }

                    if (entity1 instanceof Player) {
                        Player player1 = (Player) entity1;
                        this.lastHurtByPlayerTime = 100;
                        this.lastHurtByPlayer = player1;
                    } else if (entity1 instanceof net.minecraft.world.entity.TamableAnimal tamableEntity) {
                        if (tamableEntity.isTame()) {
                            this.lastHurtByPlayerTime = 100;
                            LivingEntity livingentity2 = tamableEntity.getOwner();
                            if (livingentity2 instanceof Player) {
                                Player player = (Player) livingentity2;
                                this.lastHurtByPlayer = player;
                            } else {
                                this.lastHurtByPlayer = null;
                            }
                        }
                    }
                }

                if (flag1) {
                    if (flag) {
                        this.level().broadcastEntityEvent(this, (byte) 29);
                    } else {
                        this.level().broadcastDamageEvent(this, source);
                    }

                    if (!source.is(DamageTypeTags.NO_IMPACT) && (!flag || amount > 0.0F)) {
                        this.markHurt();
                    }

                    if (entity1 != null && !source.is(DamageTypeTags.IS_EXPLOSION)) {
                        double d0 = entity1.getX() - this.getX();

                        double d1;
                        for (d1 = entity1.getZ() - this.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }

                        if (!source.is(DamageTypes.FALL)) {
                            this.knockback((double) 0.4F, d0, d1);
                        }
                        if (!flag) {
                            this.indicateDamage(d0, d1);
                        }
                    }
                }

                if (this.isDeadOrDying()) {
                    if (!this.checkTotemDeathProtection(source)) {
                        SoundEvent soundevent = this.getDeathSound();
                        if (flag1 && soundevent != null) {
                            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
                        }

                        this.die(source);
                    }
                } else if (flag1) {
                    this.playHurtSound(source);
                }

                boolean flag2 = !flag || amount > 0.0F;
                if (flag2) {
                    this.lastDamageSource = source;
                    this.lastDamageStamp = this.level().getGameTime();
                }

                if (self instanceof ServerPlayer) {
                    CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer) self, source, f, amount, flag);
                    if (f1 > 0.0F && f1 < 3.4028235E37F) {
                        ((ServerPlayer) self).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD), Math.round(f1 * 10.0F));
                    }
                }

                if (entity1 instanceof ServerPlayer) {
                    CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer) entity1, this, source, f, amount, flag);
                }

                cir.setReturnValue(flag2);
            }
        }

        cir.cancel();
    }
}
