package net.nerdorg.minehop.client.mixin;

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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    private boolean wasOnGround;

    public LivingEntityMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Shadow public abstract MobType getMobType();

    @Shadow public abstract boolean isFallFlying();

    @Shadow public abstract float getYHeadRot();

    @Shadow public float yHeadRotO;

    @Shadow public float yBodyRot;

    @Shadow private float speed;

    @Shadow protected abstract Vec3 handleOnClimbable(Vec3 p_21298_);

    @Shadow protected boolean jumping;

    @Shadow public abstract boolean onClimbable();

    @Shadow public abstract boolean hasEffect(MobEffect p_21024_);

    @Shadow @Nullable public abstract MobEffectInstance getEffect(MobEffect p_21125_);

    @Shadow protected abstract float getJumpPower();

    @Shadow public abstract void calculateEntityAnimation(boolean p_268129_);

    @Shadow public abstract boolean isDeadOrDying();

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

    @Shadow public abstract void setLastHurtByMob(@org.jetbrains.annotations.Nullable LivingEntity p_21039_);

    @Shadow protected int lastHurtByPlayerTime;

    @Shadow @Nullable protected Player lastHurtByPlayer;

    @Shadow public abstract void knockback(double p_147241_, double p_147242_, double p_147243_);

    @Shadow public abstract void indicateDamage(double p_270514_, double p_270826_);

    @Shadow protected abstract boolean checkTotemDeathProtection(DamageSource p_21263_);

    @Shadow @Nullable protected abstract SoundEvent getDeathSound();

    @Shadow protected abstract float getSoundVolume();

    @Shadow public abstract float getVoicePitch();

    @Shadow public abstract void die(DamageSource p_21014_);

    @Shadow protected abstract void playHurtSound(DamageSource p_21160_);

    @Shadow @Nullable private DamageSource lastDamageSource;

    @Shadow private long lastDamageStamp;

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
                    var ev = net.minecraftforge.event.ForgeEventFactory.onShieldBlock(self, source, amount);
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
                        if (!source.is(DamageTypes.FALL)) {
                            this.markHurt();
                        }
                    }

                    if (entity1 != null && !source.is(DamageTypeTags.NO_KNOCKBACK)) {
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

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(Vec3 movementInput, CallbackInfo ci) {
        if (!MinehopConfig.enabled) { return; }

        double sv_friction = MinehopConfig.sv_friction;
        double sv_accelerate = MinehopConfig.sv_accelerate;
        double sv_airaccelerate = MinehopConfig.sv_airaccelerate;
        double sv_maxairspeed = MinehopConfig.sv_maxairspeed;
        double speed_mul = MinehopConfig.speed_mul;
        double sv_gravity = MinehopConfig.sv_gravity;

        //Enable for Players only
        if (this.getType() != EntityType.PLAYER) { return; }

        if (!this.level().isClientSide && !this.isLogicalSideForUpdatingMovement()) { return; }

        //Cancel override if not in plain walking state.
        if (this.isInWater() || this.isInLava() || this.isFallFlying()) { return; }

        //I don't have a better clue how to do this atm.
        LivingEntity self = (LivingEntity) this.level().getEntity(this.getId());

        //Disable on creative flying.
        if (this.getType() == EntityType.PLAYER && isFlying((Player) self)) { return; }

        //Reverse multiplication done by the function that calls this one.
        double sI = movementInput.x / 0.98F;
        double fI = movementInput.z / 0.98F;

        //Get Slipperiness and Movement speed.
        BlockPos blockPos = getPosWithYOffset(0.500001F);
        float slipperiness = this.level().getBlockState(blockPos).getBlock().getFriction();
        float friction = 1-(slipperiness*slipperiness);


        //
        //Apply Friction
        //
        boolean fullGrounded = this.wasOnGround && this.onGround(); //Allows for no friction 1-frame upon landing.
        if (fullGrounded) {
            if (!Minehop.groundedList.contains(this.getScoreboardName())) {
                Minehop.groundedList.add(this.getScoreboardName());
            }
        }
        else {
            Minehop.groundedList.remove(this.getScoreboardName());
        }
        if (fullGrounded) {
            Vec3 velFin = this.getDeltaMovement();
            Vec3 horFin = new Vec3(velFin.x,0.0F,velFin.z);
            float speed = (float) horFin.length();
            if (speed > 0.001F) {
                float drop = 0.0F;

                drop += (speed * sv_friction * friction);

                float newspeed = Math.max(speed - drop, 0.0F);
                newspeed /= speed;
                this.setDeltaMovement(
                        horFin.x * newspeed,
                        velFin.y,
                        horFin.z * newspeed
                );
            }
        }
        this.wasOnGround = this.onGround();

        //
        // Accelerate
        //
        float yawDifference = MathHelper.wrapDegrees(this.getYHeadRot() - this.yHeadRotO);
        if (yawDifference < 0) {
            yawDifference = yawDifference * -1;
        }

        if (!fullGrounded) {
            sI = sI * yawDifference;
            fI = fI * yawDifference;
        }
        if (this.onGround()) {
            if (Minehop.efficiencyListMap.containsKey(this.getScoreboardName())) {
                List<Double> efficiencyList = Minehop.efficiencyListMap.get(this.getScoreboardName());
                if (efficiencyList != null && efficiencyList.size() > 0) {
                    double averageEfficiency = efficiencyList.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
                    if (self instanceof Player playerEntity) {
                        Minehop.efficiencyUpdateMap.put(playerEntity.getScoreboardName(), averageEfficiency);
                    }
                    Minehop.efficiencyListMap.put(this.getScoreboardName(), new ArrayList<>());
                }
            }
        }

        if (sI != 0.0F || fI != 0.0F) {
            Vec3 moveDir = movementInputToVelocity(new Vec3(sI, 0.0F, fI), 1.0F, this.getYHeadRot());
            Vec3 accelVec = this.getDeltaMovement();

            double projVel = new Vec3(accelVec.x, 0.0F, accelVec.z).dot(moveDir);
            double accelVel = (this.onGround() ? sv_accelerate : (sv_airaccelerate));

            float maxVel;
            if (fullGrounded) {
                maxVel = (float) (this.speed * speed_mul);
            } else {
                maxVel = (float) (sv_maxairspeed);

                double angleBetween = Math.acos(accelVec.normalize().dot(moveDir.normalize()));

                maxVel *= (angleBetween * angleBetween * angleBetween);
            }

            if (projVel + accelVel > maxVel) {
                accelVel = maxVel - projVel;
            }
            Vec3 accelDir = moveDir.scale(Math.max(accelVel, 0.0F));

            Vec3 newVelocity = accelVec.add(accelDir);

            if (!this.onGround()) {
                double v = Math.sqrt((newVelocity.x * newVelocity.x) + (newVelocity.z * newVelocity.z));
                double nogainv2 = (accelVec.x * accelVec.x) + (accelVec.z * accelVec.z);
                double nogainv = Math.sqrt(nogainv2);
                double maxgainv = Math.sqrt(nogainv2 + (maxVel * maxVel));
                double strafeEfficiency = MathHelper.clamp((((v - nogainv) / (maxgainv - nogainv)) * 100), 0D, 100D);
                Minehop.efficiencyMap.put(this.getScoreboardName(), strafeEfficiency);
                List<Double> efficiencyList = Minehop.efficiencyListMap.containsKey(this.getScoreboardName()) ? Minehop.efficiencyListMap.get(this.getScoreboardName()) : new ArrayList<>();
                efficiencyList.add(strafeEfficiency);
                Minehop.efficiencyListMap.put(this.getScoreboardName(), efficiencyList);
            }

            this.setDeltaMovement(newVelocity);
        }

        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());

        //u8
        //Ladder Logic
        //
        Vec3 preVel = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && this.onClimbable()) {
            preVel = new Vec3(preVel.x * 0.7D, 0.2D, preVel.z * 0.7D);
        }

        //
        //Apply Gravity (If not in Water)
        //
        double yVel = preVel.y;
        double gravity = sv_gravity;
        if (preVel.y <= 0.0D && this.hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = 0.01D;
            this.fallDistance = 0.0F;
        }
        if (this.hasEffect(MobEffects.LEVITATION)) {
            yVel += (0.05D * (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - preVel.y) * 0.2D;
            this.fallDistance = 0.0F;
        } else if (this.level().isClientSide && !this.level().isLoaded(blockPos)) {
            yVel = 0.0D;
        } else if (!this.isNoGravity()) {
            yVel -= gravity;
        }

//        BlockState belowState = this.level().getBlockState(this.blockPosition());
//        if (belowState.isOf(ModBlocks.BOOSTER_BLOCK) && (this.getWorld().getTime() > this.boostTime + 5 || this.getWorld().getTime() < this.boostTime)) {
//            this.boostTime = this.getWorld().getTime();
//            BoostBlockEntity boostBlockEntity = (BoostBlockEntity) this.getWorld().getBlockEntity(this.getBlockPos());
//            if (boostBlockEntity != null) {
//                preVel = preVel.add(boostBlockEntity.getXPower(), 0, boostBlockEntity.getZPower());
//                yVel += boostBlockEntity.getYPower();
//            }
//        }
        this.setDeltaMovement(preVel.x,yVel,preVel.z);

        //
        //Update limbs.
        //
        this.calculateEntityAnimation(!self.onGround());

        //Override original method.
        ci.cancel();
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void jump(CallbackInfo ci) {
        Vec3 vecFin = this.getDeltaMovement();
        double yVel = this.getJumpPower();
        if (this.hasEffect(MobEffects.JUMP)) {
            yVel += 0.1F * (this.getEffect(MobEffects.JUMP).getAmplifier() + 1);
        }

        this.setDeltaMovement(vecFin.x, yVel, vecFin.z);

        ci.cancel();
    }

    private boolean isLogicalSideForUpdatingMovement() {
        LivingEntity var2 = this.getControllingPassenger();
        if (var2 instanceof Player) {
            return false;
        } else {
            return !this.level().isClientSide;
        }
    }

    private static boolean isFlying(Player player) {
        return player != null && player.getAbilities().flying;
    }

    private BlockPos withY(BlockPos original, int y) {
        return new BlockPos(original.getX(), y, original.getZ());
    }

    private BlockPos getPosWithYOffset(float offset) {
        if (this.mainSupportingBlockPos.isPresent()) {
            BlockPos blockPos = (BlockPos)this.mainSupportingBlockPos.get();
            if (!(offset > 1.0E-5F)) {
                return blockPos;
            } else {
                BlockState blockState = this.level().getBlockState(blockPos);
                return (!((double)offset <= 0.5D) || !blockState.is(BlockTags.FENCES)) && !blockState.is(BlockTags.WALLS) && !(blockState.getBlock() instanceof FenceGateBlock) ? withY(blockPos, (int) Math.floor(this.position().y - (double)offset)) : blockPos;
            }
        } else {
            int i = (int) Math.floor(this.position().x);
            int j = (int) Math.floor(this.position().y - (double)offset);
            int k = (int) Math.floor(this.position().z);
            return new BlockPos(i, j, k);
        }
    }

    private static Vec3 movementInputToVelocity(Vec3 movementInput, float speed, float yaw) {
        double d = movementInput.lengthSqr();
        Vec3 vec3d = (d > 1.0D ? movementInput.normalize() : movementInput).scale(speed);
        float f = MathHelper.sin(yaw * 0.017453292F);
        float g = MathHelper.cos(yaw * 0.017453292F);
        return new Vec3(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
    }
}
