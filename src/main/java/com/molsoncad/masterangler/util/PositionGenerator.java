package com.molsoncad.masterangler.util;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.ToDoubleFunction;

public class PositionGenerator
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Method GENERATE_RANDOM_POS = ObfuscationReflectionHelper.findMethod(RandomPositionGenerator.class,
            "generateRandomPos", CreatureEntity.class, int.class, int.class, int.class, Vector3d.class, boolean.class, double.class, ToDoubleFunction.class, boolean.class, int.class, int.class, boolean.class);

    public static Vector3d getRandomPosAvoid(CreatureEntity entity, int spread, int height, Vector3d target)
    {
        target = entity.position().subtract(target);
        return generateRandomPos(entity, spread, height, 0, target, true, Math.PI / 2.0, entity::getWalkTargetValue, false, 0, 0, true);
    }

    private static Vector3d generateRandomPos(CreatureEntity entity, int spread, int height, int offsetY, Vector3d target, boolean allowWater, double rotation, ToDoubleFunction<BlockPos> walkValueFunction, boolean aboveSolid, int aboveSolidAmount, int aboveSolidOffset, boolean isStable)
    {
        try
        {
            return (Vector3d) GENERATE_RANDOM_POS.invoke(null, entity, spread, height, offsetY, target, allowWater, rotation, walkValueFunction, aboveSolid, aboveSolidAmount, aboveSolidOffset, isStable);
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            LOGGER.error("Unable to invoke reflected method RandomPositionGenerator::generateRandomPos");
            return null;
        }
    }
}
