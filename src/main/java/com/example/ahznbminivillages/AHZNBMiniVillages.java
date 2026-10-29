package com.example.ahznbminivillages;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.IWorldGenerator;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.Random;

@Mod(modid = AHZNBMiniVillages.MODID, name = "AHZNB Mini Villages", version = "1.0.0",
     dependencies = "after:ahznb")
public class AHZNBMiniVillages {
    public static final String MODID = "ahznbminivillages";
    static int spacingBlocks = 300;
    static String[] ahznbLoot = new String[0];

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration cfg = new Configuration(new File(event.getModConfigurationDirectory(), MODID + ".cfg"));
        try {
            cfg.load();
            spacingBlocks = cfg.getInt("spacingBlocks", "worldgen", 300, 200, 500,
                    "Approximate target spacing in blocks. Set between 200 and 500.");
            ahznbLoot = cfg.getStringList("ahznbLoot", "loot", new String[0],
                    "Exact item registry IDs, e.g. modid:item_name or modid:item_name@metadata. Empty by default; fill after verifying AHZNB IDs.");
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
        GameRegistry.registerWorldGenerator(new MiniVillageGenerator(), 10);
    }

    private static class MiniVillageGenerator implements IWorldGenerator {
        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world,
                             IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            if (world.isRemote || world.provider.getDimension() != 0) return;

            // Approximate density by area: expected one site per spacingBlocks^2 area.
            // The realized nearest-neighbor distance varies with terrain and random placement.
            double chancePerChunk = Math.min(1.0, 256.0 / (spacingBlocks * (double) spacingBlocks));
            if (random.nextDouble() >= chancePerChunk) return;

            int x = chunkX * 16 + 3 + random.nextInt(10);
            int z = chunkZ * 16 + 3 + random.nextInt(10);
            BlockPos surface = world.getHeight(new BlockPos(x, 0, z));
            int y = surface.getY();
            if (y < 63 || y > 115) return;
            if (!isMostlyClear(world, new BlockPos(x, y, z))) return;

            int villageType = Math.floorMod((chunkX * 73428767) ^ (chunkZ * 912931), 5);
            buildVillage(world, new BlockPos(x, y, z), villageType, random);
        }

        private boolean isMostlyClear(World world, BlockPos p) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    Block b = world.getBlockState(p.add(dx, -1, dz)).getBlock();
                    if (b != Blocks.GRASS && b != Blocks.DIRT && b != Blocks.SAND &&
                        b != Blocks.STONE && b != Blocks.GRAVEL) return false;
                    if (!world.isAirBlock(p.add(dx, 1, dz))) return false;
                }
            }
            return true;
        }

        private void buildVillage(World world, BlockPos p, int type, Random random) {
            IBlockState wall;
            IBlockState roof;
            IBlockState floor;
            IBlockState accent;
            switch (type) {
                case 0: // Leaf
                    wall = Blocks.PLANKS.getStateFromMeta(0); roof = Blocks.LEAVES.getDefaultState();
                    floor = Blocks.PLANKS.getStateFromMeta(0); accent = Blocks.LOG.getDefaultState(); break;
                case 1: // Sand
                    wall = Blocks.SANDSTONE.getDefaultState(); roof = Blocks.SANDSTONE.getStateFromMeta(2);
                    floor = Blocks.SANDSTONE.getDefaultState(); accent = Blocks.DEADBUSH.getDefaultState(); break;
                case 2: // Mist
                    wall = Blocks.STONEBRICK.getDefaultState(); roof = Blocks.PRISMARINE.getDefaultState();
                    floor = Blocks.STONEBRICK.getDefaultState(); accent = Blocks.PACKED_ICE.getDefaultState(); break;
                case 3: // Cloud
                    wall = Blocks.QUARTZ_BLOCK.getDefaultState(); roof = Blocks.STAINED_GLASS.getStateFromMeta(7);
                    floor = Blocks.QUARTZ_BLOCK.getDefaultState(); accent = Blocks.IRON_BLOCK.getDefaultState(); break;
                default: // Sound
                    wall = Blocks.NETHER_BRICK.getDefaultState(); roof = Blocks.NETHER_BRICK.getDefaultState();
                    floor = Blocks.NETHER_BRICK.getDefaultState(); accent = Blocks.OBSIDIAN.getDefaultState(); break;
            }

            // Ground pad 11x11.
            for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
                world.setBlockState(p.add(dx, -1, dz), floor, 2);
            }
            // Four small huts around a central chest plaza.
            hut(world, p.add(-3, 0, -3), wall, roof, accent, random);
            hut(world, p.add(3, 0, -3), wall, roof, accent, random);
            hut(world, p.add(-3, 0, 3), wall, roof, accent, random);
            hut(world, p.add(3, 0, 3), wall, roof, accent, random);
            world.setBlockState(p, Blocks.CHEST.getDefaultState(), 2);
            if (world.getTileEntity(p) instanceof TileEntityChest) fillChest((TileEntityChest) world.getTileEntity(p), random);
            world.setBlockState(p.add(0, 1, 0), Blocks.TORCH.getDefaultState(), 2);
            // Small marker tower.
            for (int dy = 0; dy < 3; dy++) world.setBlockState(p.add(0, dy, -5), accent, 2);
            world.setBlockState(p.add(0, 3, -5), Blocks.GLOWSTONE.getDefaultState(), 2);
        }

        private void hut(World world, BlockPos p, IBlockState wall, IBlockState roof, IBlockState accent, Random random) {
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(p.add(dx, 0, dz), wall, 2);
                world.setBlockState(p.add(dx, 3, dz), roof, 2);
                for (int dy = 1; dy <= 2; dy++) {
                    boolean edge = Math.abs(dx) == 1 || Math.abs(dz) == 1;
                    if (edge && !(dy == 1 && dx == 0 && dz == -1)) world.setBlockState(p.add(dx, dy, dz), wall, 2);
                    else world.setBlockToAir(p.add(dx, dy, dz));
                }
            }
            world.setBlockState(p.add(0, 1, 1), accent, 2);
            world.setBlockState(p.add(0, 2, 1), accent, 2);
            world.setBlockState(p.add(0, 4, 0), Blocks.TORCH.getDefaultState(), 2);
        }

        private void fillChest(TileEntityChest chest, Random random) {
            put(chest, new ItemStack(Items.IRON_INGOT, 2 + random.nextInt(5)), random);
            put(chest, new ItemStack(Items.GOLD_INGOT, 1 + random.nextInt(3)), random);
            put(chest, new ItemStack(Items.ENDER_PEARL, 1 + random.nextInt(2)), random);
            put(chest, new ItemStack(Items.EXPERIENCE_BOTTLE, 2 + random.nextInt(5)), random);
            put(chest, new ItemStack(Items.ARROW, 8 + random.nextInt(17)), random);

            // AHZNB items are only inserted when the exact registry ID is present in config.
            for (String raw : ahznbLoot) {
                if (random.nextBoolean()) {
                    String[] parts = raw.trim().split("@");
                    ResourceLocation id = new ResourceLocation(parts[0]);
                    Item item = Item.REGISTRY.getObject(id);
                    if (item != null && item != Items.AIR) {
                        int meta = 0;
                        if (parts.length > 1) {
                            try { meta = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                        }
                        put(chest, new ItemStack(item, 1, meta), random);
                    }
                }
            }
        }

        private void put(TileEntityChest chest, ItemStack stack, Random random) {
            for (int i = 0; i < 20; i++) {
                int slot = random.nextInt(chest.getSizeInventory());
                if (chest.getStackInSlot(slot).isEmpty()) {
                    chest.setInventorySlotContents(slot, stack);
                    return;
                }
            }
        }
    }
                                }
