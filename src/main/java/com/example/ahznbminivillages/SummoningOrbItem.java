package com.example.ahznbminivillages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SummoningOrbItem extends Item {
    private static final String[] BIJUU = {
        "one_tail", "two_tails", "three_tails", "four_tails", "five_tails",
        "six_tails", "seven_tails", "eight_tails", "nine_tails"
    };

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            List<EntityEntry> choices = new ArrayList<EntityEntry>();
            for (String name : BIJUU) {
                EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("narutomod", name));
                if (entry != null) choices.add(entry);
            }

            if (choices.isEmpty()) {
                player.sendMessage(new TextComponentString("Không tìm thấy entity vĩ thú của AHZNB Naruto!"));
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            }

            Entity beast = choices.get(new Random().nextInt(choices.size())).newInstance(world);
            if (beast == null) {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            }

            double distance = 6.0D;
            double x = player.posX - Math.sin(Math.toRadians(player.rotationYaw)) * distance;
            double z = player.posZ + Math.cos(Math.toRadians(player.rotationYaw)) * distance;
            BlockPos spawnPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, player.posY, z));
            beast.setPosition(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);

            if (world.spawnEntity(beast)) {
                if (!player.capabilities.isCreativeMode) stack.shrink(1);
                player.sendMessage(new TextComponentString("Bạn đã triệu hồi: " + beast.getName() + "!"));
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }
}
